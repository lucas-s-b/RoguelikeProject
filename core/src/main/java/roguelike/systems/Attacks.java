package roguelike.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import roguelike.model.DamageSpec;
import roguelike.model.DamageType;
import roguelike.model.GameWorld;
import roguelike.model.Projectile;
import roguelike.model.Stat;
import roguelike.model.Unit;

public final class Attacks {
    private Attacks() {
    }

    public static void hitUnit(GameWorld world, Unit attacker, Unit target, DamageSpec damage) {
        CombatSystem.dealHit(world, damage.toHit(attacker, target));
    }

    public static List<Unit> meleeArc(GameWorld world, Unit attacker, float angleDegrees, float range, float arcDegrees, DamageSpec damage, Color color) {
        List<Unit> unitsHit = new ArrayList<>();
        for (Unit target : Targeting.enemiesOf(world, attacker.team)) {
            float distance = attacker.position.dst(target.position);
            if (distance > range + target.radius) {
                continue;
            }
            float angleToTarget = Targeting.angleBetween(attacker.position, target.position);
            float angleDifference = Math.abs(Targeting.angleDifference(angleDegrees, angleToTarget));
            if (distance > target.radius + attacker.radius && angleDifference > arcDegrees / 2f) {
                continue;
            }
            unitsHit.add(target);
        }
        for (Unit target : unitsHit) {
            CombatSystem.dealHit(world, damage.toHit(attacker, target));
        }
        attacker.swingSide = -attacker.swingSide;
        EffectSystem.arc(world, attacker.position, angleDegrees, arcDegrees, range, color, 0.22f).sweepSide = attacker.swingSide;
        return unitsHit;
    }

    public static List<Unit> circleBlast(GameWorld world, Unit attacker, Vector2 center, float radius, DamageSpec damage, Color color) {
        List<Unit> unitsHit = Targeting.enemiesInRadius(world, attacker.team, center, radius);
        for (Unit target : unitsHit) {
            CombatSystem.dealHit(world, damage.toHit(attacker, target));
        }
        EffectSystem.explosion(world, center, radius, color);
        return unitsHit;
    }

    public static List<Unit> lineStrike(GameWorld world, Unit attacker, Vector2 start, Vector2 unclippedEnd, float width, DamageSpec damage, Color color) {
        Vector2 end = Navigation.clipToWalls(world, start, unclippedEnd);
        List<Unit> unitsHit = new ArrayList<>();
        for (Unit target : Targeting.enemiesOf(world, attacker.team)) {
            if (Intersector.distanceSegmentPoint(start, end, target.position) <= width / 2f + target.radius) {
                unitsHit.add(target);
            }
        }
        for (Unit target : unitsHit) {
            CombatSystem.dealHit(world, damage.toHit(attacker, target));
        }
        EffectSystem.line(world, start, end, width, color, 0.18f);
        EffectSystem.sparks(world, end, Targeting.angleBetween(start, end), 50f, color, 6, 300f);
        return unitsHit;
    }

    public static Projectile fireProjectile(GameWorld world, Unit attacker, float angleDegrees, float speed, DamageSpec damage, Color color) {
        Projectile projectile = new Projectile();
        projectile.team = attacker.team;
        projectile.owner = attacker;
        projectile.velocity.set(speed, 0f).setAngleDeg(angleDegrees);
        projectile.position.set(attacker.position).mulAdd(new Vector2(projectile.velocity).nor(), attacker.radius);
        projectile.damageSpec = damage;
        projectile.color.set(color);
        world.addProjectile(projectile);
        return projectile;
    }

    public static List<Projectile> fireSpread(GameWorld world, Unit attacker, float centerAngle, int count, float spreadDegrees, float speed, DamageSpec damage, Color color) {
        List<Projectile> projectiles = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            float angle = count == 1 ? centerAngle : centerAngle - spreadDegrees / 2f + spreadDegrees * index / (count - 1);
            projectiles.add(fireProjectile(world, attacker, angle, speed, damage, color));
        }
        return projectiles;
    }

    public static List<Unit> chainLightning(GameWorld world, Unit attacker, Unit firstTarget, int bounces, float bounceRange, DamageSpec damage) {
        return chainLightning(world, attacker, attacker.position, firstTarget, bounces, bounceRange, damage, Set.of());
    }

    public static List<Unit> chainLightning(GameWorld world, Unit attacker, Vector2 start, Unit firstTarget, int bounces, float bounceRange, DamageSpec damage, Set<Unit> unitsToSkip) {
        List<Unit> unitsHit = new ArrayList<>();
        Set<Unit> visited = new HashSet<>(unitsToSkip);
        Vector2 previousPosition = new Vector2(start);
        Unit current = firstTarget;
        int totalBounces = bounces + Math.round(attacker.stat(Stat.EXTRA_CHAIN_BOUNCES));
        for (int bounce = 0; bounce <= totalBounces && current != null; bounce++) {
            EffectSystem.lightning(world, previousPosition, current.position, DamageType.LIGHTNING.color, 3f, 0.22f);
            visited.add(current);
            unitsHit.add(current);
            previousPosition.set(current.position);
            CombatSystem.dealHit(world, damage.toHit(attacker, current));
            if (attacker.stat(Stat.CHAIN_ITSELF) == 1) {
                visited.clear();
            }
            current = Targeting.nearestEnemy(world, attacker.team, previousPosition, bounceRange, visited);
        }
        return unitsHit;
    }

    public static void dash(Unit unit, float angleDegrees, float distance, float durationSeconds) {
        unit.dashVelocity.set(distance / durationSeconds, 0f).setAngleDeg(angleDegrees);
        unit.dashTimeRemaining = durationSeconds;
        unit.invulnerableTimeRemaining = Math.max(unit.invulnerableTimeRemaining, durationSeconds + 0.1f);
    }

    public static Vector2 randomPointAround(Vector2 center, float maximumDistance) {
        return new Vector2(MathUtils.random(maximumDistance), 0f).setAngleDeg(MathUtils.random(360f)).add(center);
    }
}
