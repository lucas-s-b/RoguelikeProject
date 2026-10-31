package roguelike.systems;

import java.util.ArrayList;
import roguelike.definitions.ContentRegistry;
import roguelike.definitions.StatusDefinition;
import roguelike.model.GameWorld;
import roguelike.model.StatusInstance;
import roguelike.model.Unit;

public final class StatusSystem {
    private StatusSystem() {
    }

    public static void apply(Unit target, String statusId, int stacks, Unit source) {
        StatusDefinition definition = ContentRegistry.status(statusId);
        if (target.isDead || (target.isBoss && definition.isStun)) {
            return;
        }
        StatusInstance status = find(target, statusId);
        if (status == null) {
            status = new StatusInstance(definition);
            target.statuses.add(status);
        }
        status.stacks = Math.min(definition.maxStacks, status.stacks + stacks);
        status.durationRemaining = definition.duration;
        status.source = source;
    }

    public static StatusInstance find(Unit unit, String statusId) {
        for (StatusInstance status : unit.statuses) {
            if (status.definition.id.equals(statusId)) {
                return status;
            }
        }
        return null;
    }

    public static int stacksOf(Unit unit, String statusId) {
        StatusInstance status = find(unit, statusId);
        return status == null ? 0 : status.stacks;
    }

    public static boolean has(Unit unit, String statusId) {
        return find(unit, statusId) != null;
    }

    public static void remove(Unit unit, String statusId) {
        unit.statuses.removeIf(status -> status.definition.id.equals(statusId));
    }

    public static boolean isStunned(Unit unit) {
        for (StatusInstance status : unit.statuses) {
            if (status.definition.isStun) {
                return true;
            }
        }
        return false;
    }

    public static float moveSpeedMultiplier(Unit unit) {
        float multiplier = 1f;
        for (StatusInstance status : unit.statuses) {
            if (status.definition.isStun) {
                return 0f;
            }
            multiplier *= 1f - status.definition.slowPercentPerStack * status.stacks / 100f;
        }
        return Math.max(0.15f, multiplier);
    }

    public static void update(GameWorld world, Unit unit, float delta) {
        for (StatusInstance status : new ArrayList<>(unit.statuses)) {
            status.durationRemaining -= delta;
            if (status.definition.visual != null) {
                status.definition.visual.run(world, unit, status, delta);
            }
            if (status.definition.onTick == null) {
                continue;
            }
            status.tickTimer += delta;
            while (status.tickTimer >= status.definition.tickInterval && !unit.isDead) {
                status.tickTimer -= status.definition.tickInterval;
                status.definition.onTick.run(world, unit, status);
            }
        }
        unit.statuses.removeIf(status -> status.durationRemaining <= 0f);
    }
}
