package roguelike;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.mock.input.MockInput;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import roguelike.content.Content;
import roguelike.content.EnemyContent;
import roguelike.content.SkillContent;
import roguelike.content.WeaponContent;
import roguelike.definitions.ContentRegistry;
import roguelike.definitions.PerkDefinition;
import roguelike.definitions.SkillDefinition;
import roguelike.definitions.StageDefinition;
import roguelike.definitions.WeaponDefinition;
import roguelike.model.DamageSpec;
import roguelike.model.GameWorld;
import roguelike.model.PerkInstance;
import roguelike.model.Projectile;
import roguelike.model.RoomLayout;
import roguelike.model.RunState;
import roguelike.model.SkillInstance;
import roguelike.model.Stat;
import roguelike.model.TileType;
import roguelike.model.Unit;
import roguelike.model.WeaponInstance;
import roguelike.systems.Attacks;
import roguelike.systems.EnemySystem;
import roguelike.systems.MovementSystem;
import roguelike.systems.PerkSystem;
import roguelike.systems.ProjectileSystem;
import roguelike.systems.RoomSystem;
import roguelike.systems.RunSystem;
import roguelike.systems.SkillSystem;
import roguelike.systems.StatSystem;
import roguelike.systems.Targeting;
import roguelike.systems.WeaponSystem;
import roguelike.systems.WorldUpdate;

class ContentSimulationTest {
    private static final float FRAME_DELTA = 1f / 60f;

    @BeforeAll
    static void loadContent() {
        Gdx.input = new MockInput();
        if (ContentRegistry.perks.isEmpty()) {
            Content.loadAll();
        }
    }

    @Test
    void everyPerkSkillAndWeaponSurvivesSimulatedRooms() {
        List<SkillDefinition> allSkills = new ArrayList<>(ContentRegistry.skills.values());
        List<WeaponDefinition> allWeapons = new ArrayList<>(ContentRegistry.weapons.values());
        for (int trial = 0; trial < allWeapons.size(); trial++) {
            RunState run = RunSystem.createRun(WeaponContent.RUSTY_SWORD, null);
            run.weapon = new WeaponInstance(allWeapons.get(trial));
            for (int slot = 0; slot < RunState.SKILL_SLOT_COUNT; slot++) {
                run.skills[slot] = new SkillInstance(allSkills.get((trial * RunState.SKILL_SLOT_COUNT + slot) % allSkills.size()));
            }
            run.player.baseStats.put(Stat.MAX_HEALTH, 1_000_000f);
            for (PerkDefinition perk : ContentRegistry.perks.values()) {
                PerkSystem.addPerk(run, run.player, perk);
            }
            for (int room = 1; room <= 5; room++) {
                run.roomNumber = room;
                simulateRoom(run);
            }
        }
    }

    @Test
    void everyStageEnemyAndBossCanBeDefeated() {
        Set<String> enemiesSeen = new HashSet<>();
        RunState run = RunSystem.createRun(WeaponContent.STORM_CALLER, SkillContent.METEOR);
        run.skills[1] = new SkillInstance(ContentRegistry.skill(SkillContent.WHIRLWIND));
        run.skills[2] = new SkillInstance(ContentRegistry.skill(SkillContent.POISON_CLOUD));
        run.skills[3] = new SkillInstance(ContentRegistry.skill(SkillContent.FROST_NOVA));
        run.weapon.level = 8;
        run.player.baseStats.put(Stat.MAX_HEALTH, 1_000_000f);
        for (PerkDefinition perk : ContentRegistry.perks.values()) {
            PerkSystem.addPerk(run, run.player, perk);
        }
        int lastRoom = RoomSystem.BOSS_ROOM_INTERVAL * ContentRegistry.stages.size();
        for (int room = 1; room <= lastRoom; room++) {
            run.roomNumber = room;
            enemiesSeen.addAll(simulateRoom(run));
        }
        for (StageDefinition stage : ContentRegistry.stages.values()) {
            assertTrue(enemiesSeen.contains(stage.bossId), "The boss of stage " + stage.id + " never appeared");
        }
    }

    @Test
    void flurryAppliesOnHitOncePerSwing() {
        int[] onHitCount = {0};
        PerkDefinition counter = PerkDefinition.create("test_on_hit_counter").onHit((world, hit, perk) -> onHitCount[0]++);
        RunState run = RunSystem.createRun(WeaponContent.RUSTY_SWORD, SkillContent.FLURRY);
        run.player.perks.add(new PerkInstance(counter));
        GameWorld world = new GameWorld(run);
        run.player.position.set(world.layout.centerX(world.layout.columns / 2), world.layout.centerY(world.layout.rows / 2));
        world.units.add(run.player);
        Unit dummy = EnemySystem.spawn(world, ContentRegistry.enemy(EnemyContent.SLIME), new Vector2(run.player.position).add(40f, 0f));
        dummy.baseStats.put(Stat.MAX_HEALTH, 1_000_000f);
        dummy.baseStats.put(Stat.MOVE_SPEED, 0f);
        StatSystem.recalculate(dummy);
        dummy.health = dummy.maxHealth();
        dummy.contactDamage = 0f;
        world.addPendingObjects();
        run.player.aimAngle = 0f;
        SkillSystem.cast(world, run.player, run.skills[0].definition, 1, dummy.position, false);
        for (int frame = 0; frame < 120; frame++) {
            world.addPendingObjects();
            world.timedActions.forEach(action -> action.delayRemaining -= FRAME_DELTA);
            world.timedActions.stream().filter(action -> action.delayRemaining <= 0f).toList().forEach(action -> action.action.run());
            world.timedActions.removeIf(action -> action.delayRemaining <= 0f);
        }
        assertEquals(10, onHitCount[0]);
    }

    @Test
    void fallingIntoTheVoidHurtsAndReturnsToSafety() {
        RunState run = RunSystem.createRun(WeaponContent.RUSTY_SWORD, null);
        GameWorld world = new GameWorld(run);
        Unit player = run.player;
        int column = world.layout.columns / 2;
        int row = world.layout.rows / 2;
        world.layout.setTile(column + 1, row, TileType.VOID);
        player.position.set(world.layout.centerX(column), world.layout.centerY(row));
        player.lastSafePosition.set(player.position);
        world.units.add(player);
        float healthBefore = player.health;
        Attacks.dash(player, 0f, RoomLayout.TILE_SIZE, 0.1f);
        for (int frame = 0; frame < 30; frame++) {
            MovementSystem.update(world, FRAME_DELTA);
        }
        assertTrue(player.health < healthBefore, "Falling into the void should deal damage");
        assertEquals(TileType.FLOOR, world.layout.typeAt(player.position.x, player.position.y));
    }

    @Test
    void wallsStopProjectiles() {
        RunState run = RunSystem.createRun(WeaponContent.RUSTY_SWORD, null);
        GameWorld world = new GameWorld(run);
        int column = world.layout.columns / 2;
        int row = world.layout.rows / 2;
        world.layout.setTile(column + 2, row, TileType.WALL);
        run.player.position.set(world.layout.centerX(column), world.layout.centerY(row));
        world.units.add(run.player);
        Projectile projectile = Attacks.fireProjectile(world, run.player, 0f, 400f, DamageSpec.enemy(5f), Color.WHITE);
        float wallRight = world.layout.originX + (column + 3) * RoomLayout.TILE_SIZE;
        for (int frame = 0; frame < 60; frame++) {
            world.addPendingObjects();
            ProjectileSystem.update(world, FRAME_DELTA);
            assertTrue(projectile.position.x < wallRight, "A projectile went through a wall");
        }
        assertTrue(projectile.isDestroyed);
    }

    private static Set<String> simulateRoom(RunState run) {
        GameWorld world = RoomSystem.createRoom(run);
        Unit player = run.player;
        Set<String> enemiesSeen = new HashSet<>();
        for (int frame = 0; frame < 60 * 60 && !world.isRoomCleared; frame++) {
            Unit target = Targeting.nearestEnemy(world, player.team, player.position, 2000f, null);
            Vector2 aimPoint = target == null ? new Vector2(player.position).add(100f, 0f) : new Vector2(target.position);
            WorldUpdate.update(world, FRAME_DELTA, aimPoint);
            player.aimAngle = Targeting.angleBetween(player.position, aimPoint);
            WeaponSystem.tryAttack(world, player, run.weapon, player.aimAngle);
            for (SkillInstance skill : run.skills) {
                SkillSystem.tryCast(world, player, skill, aimPoint);
            }
            player.health = player.maxHealth();
            for (Unit unit : world.units) {
                if (unit.enemyDefinition != null) {
                    enemiesSeen.add(unit.enemyDefinition.id);
                }
            }
            world.particles.clear();
        }
        assertTrue(world.isRoomCleared, "Room " + run.roomNumber + " was not cleared in time");
        return enemiesSeen;
    }
}
