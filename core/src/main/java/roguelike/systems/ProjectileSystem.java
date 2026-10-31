package roguelike.systems;

import com.badlogic.gdx.math.Vector2;
import roguelike.model.GameWorld;
import roguelike.model.Projectile;
import roguelike.model.TileType;
import roguelike.model.Unit;

public final class ProjectileSystem {
    private static final float HOMING_SEARCH_RANGE = 450f;
    private static final float ARENA_ESCAPE_MARGIN = 40f;

    private ProjectileSystem() {
    }

    public static void update(GameWorld world, float delta) {
        for (Projectile projectile : world.projectiles) {
            if (projectile.isDestroyed) {
                continue;
            }
            if (projectile.age == 0f) {
                EffectSystem.muzzleFlash(world, projectile.position, projectile.velocity.angleDeg(), projectile.color);
            }
            projectile.age += delta;
            if (projectile.isHoming) {
                Unit target = Targeting.nearestEnemy(world, projectile.team, projectile.position, HOMING_SEARCH_RANGE, projectile.unitsAlreadyHit);
                if (target != null) {
                    float speed = projectile.velocity.len();
                    Vector2 desiredVelocity = new Vector2(target.position).sub(projectile.position).nor().scl(speed);
                    projectile.velocity.lerp(desiredVelocity, Math.min(1f, projectile.homingStrength * delta)).setLength(speed);
                }
            }
            projectile.position.mulAdd(projectile.velocity, delta);
            projectile.lifetimeRemaining -= delta;
            if (projectile.onUpdate != null) {
                projectile.onUpdate.run(world, projectile, delta);
            }
            EffectSystem.trail(world, projectile.position, projectile.color, projectile.radius * 1.4f, 0.22f);
            if (world.layout.typeAt(projectile.position.x, projectile.position.y) == TileType.WALL) {
                projectile.isDestroyed = true;
                EffectSystem.burst(world, projectile.position, projectile.color, 7, 150f, 4f, 0.3f);
                if (projectile.onExpire != null) {
                    projectile.onExpire.run(world, projectile, null);
                }
                continue;
            }
            for (Unit unit : world.units) {
                if (unit.isDead || unit.team == projectile.team || projectile.unitsAlreadyHit.contains(unit)) {
                    continue;
                }
                if (unit.position.dst(projectile.position) > unit.radius + projectile.radius) {
                    continue;
                }
                projectile.unitsAlreadyHit.add(unit);
                CombatSystem.dealHit(world, projectile.damageSpec.toHit(projectile.owner, unit));
                EffectSystem.burst(world, projectile.position, projectile.color, 6, 180f, 4f, 0.25f);
                if (projectile.onImpact != null) {
                    projectile.onImpact.run(world, projectile, unit);
                }
                if (projectile.pierceRemaining <= 0) {
                    projectile.isDestroyed = true;
                    break;
                }
                projectile.pierceRemaining--;
            }
            boolean isOutsideArena = projectile.position.x < world.arena.x - ARENA_ESCAPE_MARGIN
                    || projectile.position.x > world.arena.x + world.arena.width + ARENA_ESCAPE_MARGIN
                    || projectile.position.y < world.arena.y - ARENA_ESCAPE_MARGIN
                    || projectile.position.y > world.arena.y + world.arena.height + ARENA_ESCAPE_MARGIN;
            if (!projectile.isDestroyed && (projectile.lifetimeRemaining <= 0f || isOutsideArena)) {
                projectile.isDestroyed = true;
                if (!isOutsideArena) {
                    EffectSystem.burst(world, projectile.position, projectile.color, 5, 110f, 3.5f, 0.25f);
                }
                if (projectile.onExpire != null) {
                    projectile.onExpire.run(world, projectile, null);
                }
            }
        }
        world.projectiles.removeIf(projectile -> projectile.isDestroyed);
    }
}
