package roguelike.systems;

import roguelike.model.DamageSpec;
import roguelike.model.GameWorld;
import roguelike.model.PerkInstance;
import roguelike.model.Stat;
import roguelike.model.Unit;
import roguelike.model.WeaponInstance;

public final class WeaponSystem {
    public static final float DAMAGE_PER_LEVEL = 0.25f;

    private WeaponSystem() {
    }

    public static float damageAtLevel(WeaponInstance weapon) {
        return weapon.definition.damage * (1f + DAMAGE_PER_LEVEL * (weapon.level - 1));
    }

    public static float cooldownFor(Unit attacker, WeaponInstance weapon) {
        float attackSpeedPercent = Math.max(-50f, attacker.stat(Stat.ATTACK_SPEED_PERCENT));
        return weapon.definition.cooldown / (1f + attackSpeedPercent / 100f);
    }

    public static boolean tryAttack(GameWorld world, Unit attacker, WeaponInstance weapon, float aimAngle) {
        if (attacker.attackCooldownRemaining > 0f) {
            return false;
        }
        DamageSpec damage = DamageSpec.weapon(damageAtLevel(weapon), weapon.definition.damageType);
        for (PerkInstance perk : attacker.perks) {
            if (perk.definition.onWeaponAttack != null) {
                perk.definition.onWeaponAttack.run(world, attacker, damage, perk);
            }
        }
        weapon.attackCount++;
        AnimationSystem.attackPose(attacker, aimAngle);
        weapon.definition.attack.run(world, attacker, damage, aimAngle);
        attacker.attackCooldownRemaining = cooldownFor(attacker, weapon);
        return true;
    }
}
