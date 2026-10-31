package roguelike.systems;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import roguelike.model.GameWorld;
import roguelike.model.Team;
import roguelike.model.Unit;

public final class Targeting {
    private Targeting() {
    }

    public static List<Unit> enemiesOf(GameWorld world, Team team) {
        List<Unit> enemies = new ArrayList<>();
        for (Unit unit : world.units) {
            if (!unit.isDead && unit.team != team) {
                enemies.add(unit);
            }
        }
        return enemies;
    }

    public static List<Unit> enemiesInRadius(GameWorld world, Team team, Vector2 center, float radius) {
        List<Unit> enemies = new ArrayList<>();
        for (Unit unit : enemiesOf(world, team)) {
            if (unit.position.dst(center) <= radius + unit.radius) {
                enemies.add(unit);
            }
        }
        return enemies;
    }

    public static Unit nearestEnemy(GameWorld world, Team team, Vector2 from, float maximumDistance, Collection<Unit> excluded) {
        Unit nearest = null;
        float nearestDistance = maximumDistance;
        for (Unit unit : enemiesOf(world, team)) {
            if (excluded != null && excluded.contains(unit)) {
                continue;
            }
            float distance = unit.position.dst(from);
            if (distance <= nearestDistance) {
                nearest = unit;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    public static Unit randomEnemy(GameWorld world, Team team) {
        List<Unit> enemies = enemiesOf(world, team);
        return enemies.isEmpty() ? null : enemies.get(MathUtils.random(enemies.size() - 1));
    }

    public static float angleDifference(float fromDegrees, float toDegrees) {
        return ((toDegrees - fromDegrees) % 360f + 540f) % 360f - 180f;
    }

    public static float angleBetween(Vector2 from, Vector2 to) {
        return new Vector2(to).sub(from).angleDeg();
    }
}
