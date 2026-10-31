package roguelike.definitions;

import java.util.EnumMap;
import roguelike.model.PerkFamily;
import roguelike.model.Rarity;
import roguelike.model.Stat;

public final class PerkDefinition {
    public final String id;
    public String name = "";
    public String description = "";
    public Rarity rarity = Rarity.COMMON;
    public PerkFamily family = PerkFamily.GENERAL;
    public int maxStacks = 5;
    public final EnumMap<Stat, Float> statsPerStack = new EnumMap<>(Stat.class);
    public Hooks.HitHook modifyOutgoingHit;
    public Hooks.HitHook modifyIncomingHit;
    public Hooks.HitHook onHit;
    public Hooks.HitHook onDamaged;
    public Hooks.KillHook onKill;
    public Hooks.KillHook onDeath;
    public Hooks.SkillCastHook modifySkillCast;
    public Hooks.SkillCastHook onSkillCast;
    public Hooks.WeaponAttackHook onWeaponAttack;
    public Hooks.TickHook onTick;
    public Hooks.RoomHook onRoomStart;
    public Hooks.RoomHook onRoomClear;
    public Hooks.AcquireHook onAcquire;

    private PerkDefinition(String id) {
        this.id = id;
    }

    public static PerkDefinition create(String id) {
        return new PerkDefinition(id);
    }

    public PerkDefinition name(String perkName) {
        name = perkName;
        return this;
    }

    public PerkDefinition description(String perkDescription) {
        description = perkDescription;
        return this;
    }

    public PerkDefinition rarity(Rarity perkRarity) {
        rarity = perkRarity;
        return this;
    }

    public PerkDefinition family(PerkFamily perkFamily) {
        family = perkFamily;
        return this;
    }

    public PerkDefinition maxStacks(int stackLimit) {
        maxStacks = stackLimit;
        return this;
    }

    public PerkDefinition stat(Stat stat, float amountPerStack) {
        statsPerStack.merge(stat, amountPerStack, Float::sum);
        return this;
    }

    public PerkDefinition modifyOutgoingHit(Hooks.HitHook hook) {
        modifyOutgoingHit = hook;
        return this;
    }

    public PerkDefinition modifyIncomingHit(Hooks.HitHook hook) {
        modifyIncomingHit = hook;
        return this;
    }

    public PerkDefinition onHit(Hooks.HitHook hook) {
        onHit = hook;
        return this;
    }

    public PerkDefinition onDamaged(Hooks.HitHook hook) {
        onDamaged = hook;
        return this;
    }

    public PerkDefinition onKill(Hooks.KillHook hook) {
        onKill = hook;
        return this;
    }

    public PerkDefinition onDeath(Hooks.KillHook hook) {
        onDeath = hook;
        return this;
    }

    public PerkDefinition modifySkillCast(Hooks.SkillCastHook hook) {
        modifySkillCast = hook;
        return this;
    }

    public PerkDefinition onSkillCast(Hooks.SkillCastHook hook) {
        onSkillCast = hook;
        return this;
    }

    public PerkDefinition onWeaponAttack(Hooks.WeaponAttackHook hook) {
        onWeaponAttack = hook;
        return this;
    }

    public PerkDefinition onTick(Hooks.TickHook hook) {
        onTick = hook;
        return this;
    }

    public PerkDefinition onRoomStart(Hooks.RoomHook hook) {
        onRoomStart = hook;
        return this;
    }

    public PerkDefinition onRoomClear(Hooks.RoomHook hook) {
        onRoomClear = hook;
        return this;
    }

    public PerkDefinition onAcquire(Hooks.AcquireHook hook) {
        onAcquire = hook;
        return this;
    }
}
