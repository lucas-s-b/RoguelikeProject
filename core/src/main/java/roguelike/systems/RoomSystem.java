package roguelike.systems;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import roguelike.definitions.ContentRegistry;
import roguelike.definitions.EnemyDefinition;
import roguelike.definitions.StageDefinition;
import roguelike.model.GameWorld;
import roguelike.model.PerkInstance;
import roguelike.model.Projectile;
import roguelike.model.RunState;
import roguelike.model.SkillInstance;
import roguelike.model.Team;
import roguelike.model.Unit;

public final class RoomSystem {
    public static final int BOSS_ROOM_INTERVAL = 5;
    public static final int SHOP_ROOM_INTERVAL = 2;
    private static final float BASE_SPAWN_BUDGET = 4f;
    private static final float SPAWN_BUDGET_PER_ROOM = 2.2f;
    private static final float BOSS_ROOM_BUDGET_FRACTION = 0.4f;
    private static final float ELITE_SPAWN_COST_MULTIPLIER = 2.5f;
    private static final float MINIMUM_SPAWN_DISTANCE_FROM_PLAYER = 320f;
    private static final float SPAWN_TELEGRAPH_SECONDS = 0.8f;

    private RoomSystem() {
    }

    public static int stageNumber(int roomNumber) {
        return (roomNumber - 1) / BOSS_ROOM_INTERVAL + 1;
    }

    public static int roomInStage(int roomNumber) {
        return (roomNumber - 1) % BOSS_ROOM_INTERVAL + 1;
    }

    public static StageDefinition stageFor(int roomNumber) {
        List<StageDefinition> stages = new ArrayList<>(ContentRegistry.stages.values());
        return stages.get((stageNumber(roomNumber) - 1) % stages.size());
    }

    public static int completedLoops(int roomNumber) {
        return (stageNumber(roomNumber) - 1) / ContentRegistry.stages.size();
    }

    public static boolean isBossRoom(int roomNumber) {
        return roomInStage(roomNumber) == BOSS_ROOM_INTERVAL;
    }

    public static boolean isShopAfterRoom(int roomNumber) {
        return roomInStage(roomNumber) % SHOP_ROOM_INTERVAL == 0 && !isBossRoom(roomNumber);
    }

    public static int roomClearGold(int roomNumber) {
        return 10 + roomNumber * 2;
    }

    public static GameWorld createRoom(RunState run) {
        GameWorld world = new GameWorld(run);
        LayoutGenerator.generate(world.layout, stageFor(run.roomNumber), isBossRoom(run.roomNumber));
        Unit player = run.player;
        player.position.set(world.layout.centerX(world.layout.columns / 2), world.layout.centerY(world.layout.rows / 2));
        player.lastSafePosition.set(player.position);
        player.statuses.clear();
        player.buffs.clear();
        player.knockbackVelocity.setZero();
        player.dashTimeRemaining = 0f;
        player.attackCooldownRemaining = 0f;
        player.invulnerableTimeRemaining = 1f;
        player.age = 0f;
        for (SkillInstance skill : run.skills) {
            if (skill != null) {
                skill.cooldownRemaining = 0f;
            }
        }
        StatSystem.recalculate(player);
        world.units.add(player);
        Navigation.update(world);
        spawnEnemies(world);
        for (PerkInstance perk : player.perks) {
            if (perk.definition.onRoomStart != null) {
                perk.definition.onRoomStart.run(world, player, perk);
            }
        }
        return world;
    }

    public static void update(GameWorld world, float delta) {
        if (world.isRoomCleared) {
            world.timeSinceRoomCleared += delta;
            return;
        }
        if (world.pendingSpawnCount > 0 || world.hasPendingUnits() || !Targeting.enemiesOf(world, Team.PLAYER).isEmpty()) {
            return;
        }
        world.isRoomCleared = true;
        world.run.roomsCleared++;
        world.run.gold += roomClearGold(world.run.roomNumber);
        for (Projectile projectile : world.projectiles) {
            if (projectile.team == Team.ENEMY) {
                projectile.isDestroyed = true;
                EffectSystem.burst(world, projectile.position, projectile.color, 5, 120f, 4f, 0.3f);
            }
        }
        Unit player = world.player();
        EffectSystem.shockwave(world, player.position, 320f, EffectSystem.GOLD_COLOR, 0.9f);
        EffectSystem.rising(world, player.position, EffectSystem.GOLD_COLOR, 30, 80f);
        for (PerkInstance perk : player.perks) {
            if (perk.definition.onRoomClear != null) {
                perk.definition.onRoomClear.run(world, player, perk);
            }
        }
    }

    private static void spawnEnemies(GameWorld world) {
        int roomNumber = world.run.roomNumber;
        StageDefinition stage = stageFor(roomNumber);
        Set<String> enemiesWithStage = new HashSet<>();
        for (StageDefinition anyStage : ContentRegistry.stages.values()) {
            enemiesWithStage.addAll(anyStage.enemyIds);
            enemiesWithStage.add(anyStage.bossId);
        }
        List<EnemyDefinition> regularEnemies = new ArrayList<>();
        List<EnemyDefinition> bosses = new ArrayList<>();
        for (EnemyDefinition definition : ContentRegistry.enemies.values()) {
            boolean belongsHere = !enemiesWithStage.contains(definition.id) || stage.enemyIds.contains(definition.id) || definition.id.equals(stage.bossId);
            if (definition.minimumRoom > roomNumber || !belongsHere || definition.isSummonOnly) {
                continue;
            }
            if (definition.isBoss) {
                bosses.add(definition);
            } else {
                regularEnemies.add(definition);
            }
        }
        float budget = BASE_SPAWN_BUDGET + roomNumber * SPAWN_BUDGET_PER_ROOM;
        int spawnIndex = 0;
        if (isBossRoom(roomNumber) && !bosses.isEmpty()) {
            EnemyDefinition boss = bosses.get(MathUtils.random(bosses.size() - 1));
            Vector2 bossPosition = new Vector2(world.arena.x + world.arena.width / 2f, world.arena.y + world.arena.height - 120f);
            queueSpawn(world, boss, bossPosition, SPAWN_TELEGRAPH_SECONDS, false);
            budget *= BOSS_ROOM_BUDGET_FRACTION;
            spawnIndex++;
        }
        while (budget > 0f) {
            List<EnemyDefinition> affordable = new ArrayList<>();
            for (EnemyDefinition definition : regularEnemies) {
                if (definition.spawnCost <= budget) {
                    affordable.add(definition);
                }
            }
            if (affordable.isEmpty()) {
                break;
            }
            EnemyDefinition picked = affordable.get(MathUtils.random(affordable.size() - 1));
            boolean isElite = MathUtils.random() < stage.eliteChance && picked.spawnCost * ELITE_SPAWN_COST_MULTIPLIER <= budget;
            budget -= picked.spawnCost * (isElite ? ELITE_SPAWN_COST_MULTIPLIER : 1f);
            Vector2 position = new Vector2();
            for (int attempt = 0; attempt < 40; attempt++) {
                int column = MathUtils.random(world.layout.columns - 1);
                int row = MathUtils.random(world.layout.rows - 1);
                position.set(world.layout.centerX(column), world.layout.centerY(row));
                boolean isReachable = world.layout.groundDistances[row * world.layout.columns + column] != Navigation.UNREACHABLE;
                if (isReachable && position.dst(world.player().position) >= MINIMUM_SPAWN_DISTANCE_FROM_PLAYER) {
                    break;
                }
            }
            queueSpawn(world, picked, position, SPAWN_TELEGRAPH_SECONDS + spawnIndex * 0.12f, isElite);
            spawnIndex++;
        }
    }

    private static void queueSpawn(GameWorld world, EnemyDefinition definition, Vector2 position, float delaySeconds, boolean isElite) {
        world.pendingSpawnCount++;
        EffectSystem.summon(world, position, definition.radius + 6f, isElite ? EffectSystem.GOLD_COLOR : definition.color, delaySeconds);
        world.schedule(delaySeconds, () -> {
            Unit enemy = EnemySystem.spawn(world, definition, position);
            if (isElite) {
                EnemySystem.makeElite(enemy);
            }
            world.pendingSpawnCount--;
        });
    }
}
