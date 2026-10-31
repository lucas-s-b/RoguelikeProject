package roguelike.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import roguelike.model.GameWorld;
import roguelike.model.Hit;
import roguelike.model.HitSource;
import roguelike.model.PerkInstance;
import roguelike.model.Stat;
import roguelike.model.StatusInstance;
import roguelike.model.Team;
import roguelike.model.Unit;

public final class CombatSystem {
    private static final int MAXIMUM_ON_HIT_DEPTH = 6;
    private static final float PLAYER_INVULNERABILITY_AFTER_HIT = 0.4f;
    private static final float BASE_CRIT_MULTIPLIER = 1.5f;
    private static final float FRONTAL_SHIELD_DAMAGE_MULTIPLIER = 0.2f;
    private static int currentHitDepth;

    private CombatSystem() {
    }

    public static void dealHit(GameWorld world, Hit hit) {
        Unit attacker = hit.attacker;
        Unit target = hit.target;
        if (target.isDead || target.invulnerableTimeRemaining > 0f) {
            return;
        }
        currentHitDepth++;
        try {
            if (attacker != null) {
                if (hit.isDirectAttack()) {
                    hit.damage += attacker.stat(Stat.FLAT_DAMAGE);
                }
                hit.bonusDamagePercent += attacker.stat(Stat.DAMAGE_PERCENT);
                if (hit.source == HitSource.SKILL) {
                    hit.bonusDamagePercent += attacker.stat(Stat.SKILL_DAMAGE_PERCENT);
                }
                for (PerkInstance perk : attacker.perks) {
                    if (perk.definition.modifyOutgoingHit != null) {
                        perk.definition.modifyOutgoingHit.run(world, hit, perk);
                    }
                }
                hit.damage *= Math.max(0f, 1f + hit.bonusDamagePercent / 100f);
                if (hit.canCrit && (hit.isCritical || MathUtils.random(100f) < attacker.stat(Stat.CRIT_CHANCE))) {
                    hit.isCritical = true;
                    hit.damage *= BASE_CRIT_MULTIPLIER + attacker.stat(Stat.CRIT_DAMAGE_PERCENT) / 100f;
                }
            }
            for (StatusInstance status : target.statuses) {
                hit.damage *= 1f + status.definition.damageTakenPercentPerStack * status.stacks / 100f;
            }
            for (PerkInstance perk : target.perks) {
                if (perk.definition.modifyIncomingHit != null) {
                    perk.definition.modifyIncomingHit.run(world, hit, perk);
                }
            }
            float shieldArc = target.enemyDefinition == null ? 0f : target.enemyDefinition.frontalShieldDegrees;
            if (shieldArc > 0f && attacker != null && hit.source != HitSource.STATUS) {
                float angleToAttacker = Targeting.angleBetween(target.position, attacker.position);
                if (Math.abs(Targeting.angleDifference(target.aimAngle, angleToAttacker)) < shieldArc / 2f) {
                    hit.damage *= FRONTAL_SHIELD_DAMAGE_MULTIPLIER;
                    EffectSystem.sparks(world, target.position, angleToAttacker, 50f, Color.WHITE, 5, 260f);
                }
            }
            if (hit.source != HitSource.STATUS && MathUtils.random(100f) < target.stat(Stat.DODGE_CHANCE)) {
                hit.isDodged = true;
            }
            if (hit.isDodged) {
                EffectSystem.shockwave(world, target.position, target.radius + 14f, Color.WHITE, 0.3f);
                return;
            }
            if (hit.source != HitSource.STATUS) {
                hit.damage = Math.max(1f, hit.damage - Math.max(0f, target.stat(Stat.ARMOR) - hit.armorPenetration));
            }
            float absorbedByShield = Math.min(target.shield, hit.damage);
            target.shield -= absorbedByShield;
            target.health -= hit.damage - absorbedByShield;
            target.hitFlashTimeRemaining = 0.08f;
            EffectSystem.damageNumber(world, hit);
            float impactAngle = attacker == null ? MathUtils.random(360f) : Targeting.angleBetween(attacker.position, target.position);
            EffectSystem.sparks(world, target.position, impactAngle, 70f, hit.damageType.color, hit.isCritical ? 9 : 4, hit.isCritical ? 420f : 280f);
            if (hit.isCritical) {
                EffectSystem.shockwave(world, target.position, target.radius + 18f, EffectSystem.CRIT_COLOR, 0.22f);
                EffectSystem.shake(world, 0.12f);
            }
            if (target.team == Team.PLAYER && hit.source == HitSource.STATUS) {
                world.hurtFlash = Math.max(world.hurtFlash, 0.35f);
            } else if (target.team == Team.PLAYER) {
                EffectSystem.shake(world, 0.35f);
                world.hurtFlash = 1f;
                world.hitStopRemaining = Math.max(world.hitStopRemaining, 0.06f);
            }
            if (hit.knockback > 0f && attacker != null && !target.isBoss) {
                Vector2 pushDirection = new Vector2(target.position).sub(attacker.position).nor();
                target.knockbackVelocity.mulAdd(pushDirection, hit.knockback);
            }
            if (target.team == Team.PLAYER && hit.source != HitSource.STATUS) {
                target.invulnerableTimeRemaining = PLAYER_INVULNERABILITY_AFTER_HIT;
            }
            if (attacker != null && hit.canTriggerOnHit && currentHitDepth <= MAXIMUM_ON_HIT_DEPTH) {
                int triggerCount = 1 + Math.round(attacker.stat(Stat.ON_HIT_REPEATS));
                for (int trigger = 0; trigger < triggerCount; trigger++) {
                    for (PerkInstance perk : attacker.perks) {
                        if (perk.definition.onHit != null) {
                            perk.definition.onHit.run(world, hit, perk);
                        }
                    }
                }
                float lifeOnHit = attacker.stat(Stat.LIFE_ON_HIT);
                if (lifeOnHit > 0f) {
                    heal(world, attacker, lifeOnHit);
                }
            }
            for (PerkInstance perk : target.perks) {
                if (perk.definition.onDamaged != null) {
                    perk.definition.onDamaged.run(world, hit, perk);
                }
            }
            if (target.health <= 0f && !target.isDead) {
                kill(world, target, attacker);
            }
        } finally {
            currentHitDepth--;
        }
    }

    public static void kill(GameWorld world, Unit victim, Unit killer) {
        if (victim.isDead) {
            return;
        }
        victim.isDead = true;
        victim.health = 0f;
        EffectSystem.pop(world, victim);
        EffectSystem.shockwave(world, victim.position, victim.radius * 3f, victim.color, 0.4f);
        EffectSystem.burst(world, victim.position, victim.color, victim.isBoss ? 90 : 18, victim.isBoss ? 520f : 300f, 6f, 0.55f);
        EffectSystem.shards(world, victim.position, victim.color, victim.isBoss ? 40 : 8, victim.isBoss ? 480f : 260f);
        EffectSystem.splat(world, victim.position, victim.color, victim.radius * (victim.isBoss ? 1.6f : 1.1f));
        EffectSystem.shake(world, victim.isBoss ? 0.9f : 0.12f);
        if (victim.isBoss) {
            world.screenFlash = 1f;
            world.hitStopRemaining = 0.25f;
        }
        if (victim.team == Team.ENEMY) {
            float goldMultiplier = 1f + world.player().stat(Stat.GOLD_PERCENT) / 100f;
            int goldEarned = Math.round(victim.goldReward * goldMultiplier);
            world.run.gold += goldEarned;
            world.run.enemiesKilled++;
            if (goldEarned > 0) {
                EffectSystem.text(world, victim.position, "+" + goldEarned, EffectSystem.GOLD_COLOR, 0.9f);
            }
        }
        for (PerkInstance perk : victim.perks) {
            if (perk.definition.onDeath != null) {
                perk.definition.onDeath.run(world, killer, victim, perk);
            }
        }
        if (killer == null) {
            return;
        }
        for (PerkInstance perk : killer.perks) {
            if (perk.definition.onKill != null) {
                perk.definition.onKill.run(world, killer, victim, perk);
            }
        }
    }

    public static void heal(GameWorld world, Unit unit, float amount) {
        if (unit.isDead || amount <= 0f) {
            return;
        }
        float healed = Math.min(amount, unit.maxHealth() - unit.health);
        unit.health += healed;
        if (healed >= 2f) {
            EffectSystem.rising(world, unit.position, EffectSystem.HEAL_COLOR, 5, unit.radius);
            EffectSystem.text(world, unit.position, "+" + Math.round(healed), EffectSystem.HEAL_COLOR, 0.9f);
        }
    }
}
