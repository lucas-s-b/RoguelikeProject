package roguelike.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import roguelike.model.DamageSpec;
import roguelike.model.DamageType;
import roguelike.model.GameWorld;
import roguelike.model.RoomLayout;
import roguelike.model.Stat;
import roguelike.model.Team;
import roguelike.model.TileType;
import roguelike.model.Unit;

public final class MovementSystem {
    private static final float KNOCKBACK_DECAY_PER_SECOND = 8f;
    private static final float FORCED_MOVEMENT_SPEED = 120f;
    private static final float PLAYER_FALL_DAMAGE_FRACTION = 0.12f;
    private static final float ENEMY_FALL_DAMAGE_FRACTION = 0.25f;
    private static final float FALL_INVULNERABILITY_SECONDS = 1f;
    private static final int COLLISION_PASSES = 2;
    private static final Color FALL_DUST_COLOR = new Color(0.12f, 0.1f, 0.14f, 1f);

    private MovementSystem() {
    }

    public static void update(GameWorld world, float delta) {
        for (Unit unit : world.units) {
            if (unit.isDead) {
                continue;
            }
            if (unit.dashTimeRemaining > 0f) {
                unit.velocity.set(unit.dashVelocity);
                unit.dashTimeRemaining -= delta;
                EffectSystem.afterimage(world, unit);
            } else {
                float speed = unit.stat(Stat.MOVE_SPEED) * unit.speedMultiplier * StatusSystem.moveSpeedMultiplier(unit);
                unit.velocity.set(unit.moveDirection).scl(speed);
            }
            unit.velocity.add(unit.knockbackVelocity);
            unit.position.mulAdd(unit.velocity, delta);
            unit.knockbackVelocity.scl(Math.max(0f, 1f - KNOCKBACK_DECAY_PER_SECOND * delta));
        }
        for (int firstIndex = 0; firstIndex < world.units.size(); firstIndex++) {
            Unit first = world.units.get(firstIndex);
            if (first.isDead || first.team == Team.PLAYER) {
                continue;
            }
            for (int secondIndex = firstIndex + 1; secondIndex < world.units.size(); secondIndex++) {
                Unit second = world.units.get(secondIndex);
                if (second.isDead || second.team == Team.PLAYER) {
                    continue;
                }
                float minimumDistance = first.radius + second.radius;
                float distance = first.position.dst(second.position);
                if (distance >= minimumDistance || distance < 0.01f) {
                    continue;
                }
                Vector2 push = new Vector2(second.position).sub(first.position).nor().scl((minimumDistance - distance) / 2f);
                first.position.sub(push);
                second.position.add(push);
            }
        }
        RoomLayout layout = world.layout;
        for (Unit unit : world.units) {
            if (unit.isDead) {
                continue;
            }
            boolean isForcedMovement = !unit.isBoss && (unit.dashTimeRemaining > 0f || unit.knockbackVelocity.len() > FORCED_MOVEMENT_SPEED);
            boolean canCrossVoid = unit.isFlying || isForcedMovement;
            if (!canCrossVoid && layout.typeAt(unit.position.x, unit.position.y) == TileType.VOID) {
                fall(world, unit);
            }
            for (int pass = 0; pass < COLLISION_PASSES; pass++) {
                pushOutOfBlockedTiles(layout, unit, canCrossVoid);
            }
            unit.position.x = MathUtils.clamp(unit.position.x, world.arena.x + unit.radius, world.arena.x + world.arena.width - unit.radius);
            unit.position.y = MathUtils.clamp(unit.position.y, world.arena.y + unit.radius, world.arena.y + world.arena.height - unit.radius);
            if (!isForcedMovement && layout.typeAt(unit.position.x, unit.position.y) == TileType.FLOOR) {
                unit.lastSafePosition.set(unit.position);
            }
        }
    }

    private static void pushOutOfBlockedTiles(RoomLayout layout, Unit unit, boolean canCrossVoid) {
        int firstColumn = layout.columnOf(unit.position.x - unit.radius);
        int lastColumn = layout.columnOf(unit.position.x + unit.radius);
        int firstRow = layout.rowOf(unit.position.y - unit.radius);
        int lastRow = layout.rowOf(unit.position.y + unit.radius);
        for (int row = firstRow; row <= lastRow; row++) {
            for (int column = firstColumn; column <= lastColumn; column++) {
                TileType tile = layout.tileAt(column, row);
                if (!layout.isInside(column, row) || tile == TileType.FLOOR || (tile == TileType.VOID && canCrossVoid)) {
                    continue;
                }
                float left = layout.originX + column * RoomLayout.TILE_SIZE;
                float bottom = layout.originY + row * RoomLayout.TILE_SIZE;
                float closestX = MathUtils.clamp(unit.position.x, left, left + RoomLayout.TILE_SIZE);
                float closestY = MathUtils.clamp(unit.position.y, bottom, bottom + RoomLayout.TILE_SIZE);
                float offsetX = unit.position.x - closestX;
                float offsetY = unit.position.y - closestY;
                float distanceSquared = offsetX * offsetX + offsetY * offsetY;
                if (distanceSquared >= unit.radius * unit.radius) {
                    continue;
                }
                if (distanceSquared > 0.0001f) {
                    float distance = (float) Math.sqrt(distanceSquared);
                    unit.position.add(offsetX / distance * (unit.radius - distance), offsetY / distance * (unit.radius - distance));
                    continue;
                }
                float toLeft = unit.position.x - left;
                float toRight = left + RoomLayout.TILE_SIZE - unit.position.x;
                float toBottom = unit.position.y - bottom;
                float toTop = bottom + RoomLayout.TILE_SIZE - unit.position.y;
                float nearest = Math.min(Math.min(toLeft, toRight), Math.min(toBottom, toTop));
                if (nearest == toLeft) {
                    unit.position.x = left - unit.radius;
                } else if (nearest == toRight) {
                    unit.position.x = left + RoomLayout.TILE_SIZE + unit.radius;
                } else if (nearest == toBottom) {
                    unit.position.y = bottom - unit.radius;
                } else {
                    unit.position.y = bottom + RoomLayout.TILE_SIZE + unit.radius;
                }
            }
        }
    }

    private static void fall(GameWorld world, Unit unit) {
        EffectSystem.pop(world, unit);
        EffectSystem.burst(world, unit.position, FALL_DUST_COLOR, 16, 140f, 6f, 0.5f);
        unit.position.set(unit.lastSafePosition);
        unit.knockbackVelocity.setZero();
        unit.velocity.setZero();
        unit.smoothedVelocity.setZero();
        unit.dashTimeRemaining = 0f;
        unit.age = 0f;
        EffectSystem.shockwave(world, unit.position, unit.radius * 3f, unit.color, 0.4f);
        unit.invulnerableTimeRemaining = 0f;
        if (unit.team == Team.PLAYER) {
            Attacks.hitUnit(world, null, unit, DamageSpec.enemy(unit.maxHealth() * PLAYER_FALL_DAMAGE_FRACTION));
            unit.invulnerableTimeRemaining = Math.max(unit.invulnerableTimeRemaining, FALL_INVULNERABILITY_SECONDS);
        } else {
            Attacks.hitUnit(world, world.player(), unit, DamageSpec.perk(unit.maxHealth() * ENEMY_FALL_DAMAGE_FRACTION, DamageType.PHYSICAL));
        }
    }
}
