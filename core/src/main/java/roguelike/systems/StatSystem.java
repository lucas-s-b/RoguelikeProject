package roguelike.systems;

import java.util.Map;
import roguelike.model.PerkInstance;
import roguelike.model.Stat;
import roguelike.model.TimedBuff;
import roguelike.model.Unit;

public final class StatSystem {
    private StatSystem() {
    }

    public static void recalculate(Unit unit) {
        unit.stats.clear();
        unit.stats.putAll(unit.baseStats);
        for (PerkInstance perk : unit.perks) {
            for (Map.Entry<Stat, Float> entry : perk.definition.statsPerStack.entrySet()) {
                unit.stats.merge(entry.getKey(), entry.getValue() * perk.stacks, Float::sum);
            }
        }
        for (TimedBuff buff : unit.buffs) {
            unit.stats.merge(buff.stat, buff.amount, Float::sum);
        }
        float healthMultiplier = Math.max(0.1f, 1f + unit.stat(Stat.MAX_HEALTH_PERCENT) / 100f);
        unit.stats.put(Stat.MAX_HEALTH, unit.stat(Stat.MAX_HEALTH) * healthMultiplier);
        float speedMultiplier = Math.max(0.2f, 1f + unit.stat(Stat.MOVE_SPEED_PERCENT) / 100f);
        unit.stats.put(Stat.MOVE_SPEED, unit.stat(Stat.MOVE_SPEED) * speedMultiplier);
        unit.health = Math.min(unit.health, unit.maxHealth());
    }

    public static TimedBuff findBuff(Unit unit, String sourceId, Stat stat) {
        for (TimedBuff buff : unit.buffs) {
            if (buff.sourceId.equals(sourceId) && buff.stat == stat) {
                return buff;
            }
        }
        return null;
    }

    public static void setBuff(Unit unit, String sourceId, Stat stat, float amount, float durationSeconds) {
        TimedBuff existing = findBuff(unit, sourceId, stat);
        if (existing == null) {
            unit.buffs.add(new TimedBuff(sourceId, stat, amount, durationSeconds));
        } else {
            existing.amount = amount;
            existing.durationRemaining = Math.max(existing.durationRemaining, durationSeconds);
        }
        recalculate(unit);
    }

    public static void updateBuffs(Unit unit, float delta) {
        for (TimedBuff buff : unit.buffs) {
            buff.durationRemaining -= delta;
        }
        unit.buffs.removeIf(buff -> buff.durationRemaining <= 0f);
        recalculate(unit);
    }
}
