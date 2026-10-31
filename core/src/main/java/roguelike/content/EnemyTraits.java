package roguelike.content;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import roguelike.definitions.ContentRegistry;
import roguelike.definitions.PerkDefinition;
import roguelike.model.DamageSpec;
import roguelike.model.Stat;
import roguelike.systems.Attacks;
import roguelike.systems.EffectSystem;
import roguelike.systems.EnemySystem;

public final class EnemyTraits {
    private static final Color TELEGRAPH_COLOR = new Color(1f, 0.2f, 0.2f, 1f);
    private static final Color SHIELD_COLOR = new Color(0.6f, 0.85f, 1f, 1f);
    private static final float VOLATILE_RADIUS = 90f;
    private static final float VOLATILE_FUSE_SECONDS = 0.6f;
    private static final float SHIELD_RECHARGE_SECONDS = 3f;

    private EnemyTraits() {
    }

    public static void register() {
        ContentRegistry.eliteTraits.add(PerkDefinition.create("elite_hardened").stat(Stat.ARMOR, 6f));

        ContentRegistry.eliteTraits.add(PerkDefinition.create("elite_swift").stat(Stat.MOVE_SPEED_PERCENT, 45f));

        ContentRegistry.eliteTraits.add(PerkDefinition.create("elite_regenerating")
                .onTick((world, owner, perk, delta) -> owner.health = Math.min(owner.maxHealth(), owner.health + owner.maxHealth() * 0.03f * delta)));

        ContentRegistry.eliteTraits.add(PerkDefinition.create("elite_volatile")
                .onDeath((world, killer, victim, perk) -> {
                    Vector2 center = new Vector2(victim.position);
                    EffectSystem.telegraphCircle(world, center, VOLATILE_RADIUS, TELEGRAPH_COLOR, VOLATILE_FUSE_SECONDS);
                    float blastDamage = Math.max(victim.contactDamage, victim.attackDamage) * 1.5f;
                    world.schedule(VOLATILE_FUSE_SECONDS, () ->
                            Attacks.circleBlast(world, victim, center, VOLATILE_RADIUS, DamageSpec.enemy(blastDamage), victim.color));
                }));

        ContentRegistry.eliteTraits.add(PerkDefinition.create("elite_shielded")
                .onTick((world, owner, perk, delta) -> {
                    perk.timer += delta;
                    if (perk.timer >= SHIELD_RECHARGE_SECONDS && owner.shield <= 0f) {
                        owner.shield = owner.maxHealth() * 0.3f;
                        EffectSystem.ring(world, owner.position, owner.radius + 12f, SHIELD_COLOR, 0.4f);
                    }
                })
                .onDamaged((world, hit, perk) -> perk.timer = 0f));
    }

    public static PerkDefinition splitsInto(String enemyId, int count) {
        return PerkDefinition.create("splits_into_" + enemyId)
                .onDeath((world, killer, victim, perk) -> {
                    for (int index = 0; index < count; index++) {
                        EnemySystem.spawn(world, ContentRegistry.enemy(enemyId), Attacks.randomPointAround(victim.position, victim.radius));
                    }
                });
    }
}
