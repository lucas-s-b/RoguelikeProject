package roguelike.systems;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import roguelike.definitions.ContentRegistry;
import roguelike.definitions.EnemyDefinition;
import roguelike.definitions.PerkDefinition;
import roguelike.definitions.StageDefinition;
import roguelike.model.DamageSpec;
import roguelike.model.GameWorld;
import roguelike.model.PerkInstance;
import roguelike.model.Stat;
import roguelike.model.Team;
import roguelike.model.Unit;

public final class EnemySystem {
    public static final float HEALTH_SCALING_PER_ROOM = 0.18f;
    public static final float DAMAGE_SCALING_PER_ROOM = 0.08f;
    public static final float ENDLESS_HEALTH_SCALING = 3f;
    public static final float ENDLESS_DAMAGE_SCALING = 1.5f;
    public static final float ELITE_HEALTH_MULTIPLIER = 2.5f;
    public static final float ELITE_DAMAGE_MULTIPLIER = 1.3f;
    public static final int ELITE_GOLD_MULTIPLIER = 3;
    private static final float CONTACT_DAMAGE_COOLDOWN = 0.8f;

    private EnemySystem() {
    }

    public static Unit spawn(GameWorld world, EnemyDefinition definition, Vector2 position) {
        int roomNumber = world.run.roomNumber;
        StageDefinition stage = RoomSystem.stageFor(roomNumber);
        int roomIndexInStage = RoomSystem.roomInStage(roomNumber) - 1;
        int completedLoops = RoomSystem.completedLoops(roomNumber);
        float healthScale = stage.healthMultiplier * (1f + HEALTH_SCALING_PER_ROOM * roomIndexInStage) * (float) Math.pow(ENDLESS_HEALTH_SCALING, completedLoops);
        float damageScale = stage.damageMultiplier * (1f + DAMAGE_SCALING_PER_ROOM * roomIndexInStage) * (float) Math.pow(ENDLESS_DAMAGE_SCALING, completedLoops);
        Vector2 spawnPosition = Navigation.nearestOpenPosition(world.layout, position, definition.isFlying);
        Unit enemy = new Unit();
        enemy.team = Team.ENEMY;
        enemy.enemyDefinition = definition;
        enemy.position.set(spawnPosition);
        enemy.lastSafePosition.set(Navigation.nearestOpenPosition(world.layout, position, false));
        enemy.isFlying = definition.isFlying;
        enemy.radius = definition.radius;
        enemy.color.set(definition.color);
        enemy.isBoss = definition.isBoss;
        enemy.bodyShape = definition.shape;
        enemy.bodyAngle = Targeting.angleBetween(spawnPosition, world.player().position);
        enemy.baseStats.put(Stat.MAX_HEALTH, definition.maxHealth * healthScale);
        enemy.baseStats.put(Stat.MOVE_SPEED, definition.moveSpeed);
        enemy.baseStats.put(Stat.ARMOR, definition.armor + stage.enemyArmor + completedLoops * stage.enemyArmor);
        enemy.contactDamage = definition.contactDamage * damageScale;
        enemy.attackDamage = definition.attackDamage * damageScale;
        enemy.goldReward = definition.goldReward;
        enemy.attackCooldownRemaining = MathUtils.random(0.6f, 1.6f);
        for (PerkDefinition trait : definition.traits) {
            enemy.perks.add(new PerkInstance(trait));
        }
        StatSystem.recalculate(enemy);
        enemy.health = enemy.maxHealth();
        EffectSystem.shockwave(world, spawnPosition, enemy.radius * 2.5f, enemy.color, 0.45f);
        EffectSystem.burst(world, spawnPosition, enemy.color, 10, 160f, 5f, 0.45f);
        world.addUnit(enemy);
        return enemy;
    }

    public static void makeElite(Unit enemy) {
        enemy.isElite = true;
        enemy.radius *= 1.15f;
        enemy.baseStats.put(Stat.MAX_HEALTH, enemy.baseStats.get(Stat.MAX_HEALTH) * ELITE_HEALTH_MULTIPLIER);
        enemy.contactDamage *= ELITE_DAMAGE_MULTIPLIER;
        enemy.attackDamage *= ELITE_DAMAGE_MULTIPLIER;
        enemy.goldReward *= ELITE_GOLD_MULTIPLIER;
        if (!ContentRegistry.eliteTraits.isEmpty()) {
            PerkDefinition trait = ContentRegistry.eliteTraits.get(MathUtils.random(ContentRegistry.eliteTraits.size() - 1));
            enemy.perks.add(new PerkInstance(trait));
        }
        StatSystem.recalculate(enemy);
        enemy.health = enemy.maxHealth();
    }

    public static void update(GameWorld world, Unit enemy, float delta) {
        Unit player = world.player();
        if (StatusSystem.isStunned(enemy) || player.isDead) {
            enemy.moveDirection.setZero();
            return;
        }
        if (enemy.enemyDefinition.behavior != null) {
            enemy.enemyDefinition.behavior.run(world, enemy, delta);
        }
        boolean isTouchingPlayer = enemy.position.dst(player.position) <= enemy.radius + player.radius;
        if (enemy.contactDamage > 0f && enemy.contactCooldownRemaining <= 0f && isTouchingPlayer) {
            enemy.contactCooldownRemaining = CONTACT_DAMAGE_COOLDOWN;
            Attacks.hitUnit(world, enemy, player, DamageSpec.enemy(enemy.contactDamage));
        }
    }
}
