package roguelike.model;

public final class DamageSpec {
    public float damage;
    public DamageType damageType = DamageType.PHYSICAL;
    public HitSource source = HitSource.WEAPON;
    public SkillCast skillCast;
    public boolean canTriggerOnHit = true;
    public boolean canCrit = true;
    public float knockback;

    public static DamageSpec weapon(float damage, DamageType damageType) {
        DamageSpec spec = new DamageSpec();
        spec.damage = damage;
        spec.damageType = damageType;
        return spec;
    }

    public static DamageSpec skill(SkillCast skillCast) {
        DamageSpec spec = new DamageSpec();
        spec.damage = skillCast.damage;
        spec.damageType = skillCast.definition.damageType;
        spec.source = HitSource.SKILL;
        spec.skillCast = skillCast;
        return spec;
    }

    public static DamageSpec perk(float damage, DamageType damageType) {
        DamageSpec spec = new DamageSpec();
        spec.damage = damage;
        spec.damageType = damageType;
        spec.source = HitSource.PERK;
        spec.canTriggerOnHit = false;
        spec.canCrit = false;
        return spec;
    }

    public static DamageSpec status(float damage, DamageType damageType) {
        DamageSpec spec = perk(damage, damageType);
        spec.source = HitSource.STATUS;
        return spec;
    }

    public static DamageSpec enemy(float damage) {
        DamageSpec spec = new DamageSpec();
        spec.damage = damage;
        spec.source = HitSource.ENEMY;
        spec.canTriggerOnHit = false;
        spec.canCrit = false;
        return spec;
    }

    public DamageSpec withKnockback(float knockbackAmount) {
        knockback = knockbackAmount;
        return this;
    }

    public DamageSpec withOnHit(boolean triggersOnHit) {
        canTriggerOnHit = triggersOnHit;
        return this;
    }

    public DamageSpec withCrit(boolean allowsCrit) {
        canCrit = allowsCrit;
        return this;
    }

    public DamageSpec scaled(float multiplier) {
        DamageSpec copy = new DamageSpec();
        copy.damage = damage * multiplier;
        copy.damageType = damageType;
        copy.source = source;
        copy.skillCast = skillCast;
        copy.canTriggerOnHit = canTriggerOnHit;
        copy.canCrit = canCrit;
        copy.knockback = knockback;
        return copy;
    }

    public Hit toHit(Unit attacker, Unit target) {
        Hit hit = new Hit();
        hit.attacker = attacker;
        hit.target = target;
        hit.damage = damage;
        hit.damageType = damageType;
        hit.source = source;
        hit.skillCast = skillCast;
        hit.canTriggerOnHit = canTriggerOnHit;
        hit.canCrit = canCrit;
        hit.knockback = knockback;
        return hit;
    }
}
