package roguelike.model;

public final class Hit {
    public Unit attacker;
    public Unit target;
    public float damage;
    public DamageType damageType = DamageType.PHYSICAL;
    public HitSource source = HitSource.WEAPON;
    public SkillCast skillCast;
    public boolean canTriggerOnHit;
    public boolean canCrit;
    public boolean isCritical;
    public boolean isDodged;
    public float bonusDamagePercent;
    public float armorPenetration;
    public float knockback;

    public boolean isDirectAttack() {
        return source == HitSource.WEAPON || source == HitSource.SKILL;
    }
}
