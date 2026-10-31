package roguelike.systems;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import roguelike.definitions.SkillDefinition;
import roguelike.model.GameWorld;
import roguelike.model.PerkInstance;
import roguelike.model.RunState;
import roguelike.model.SkillCast;
import roguelike.model.SkillInstance;
import roguelike.model.SkillTag;
import roguelike.model.Stat;
import roguelike.model.Unit;

public final class SkillSystem {
    public static final float DAMAGE_PER_LEVEL = 0.3f;
    private static final float MAXIMUM_COOLDOWN_REDUCTION = 40f;

    private SkillSystem() {
    }

    public static float cooldownFor(Unit caster, SkillDefinition definition) {
        float reductionPercent = MathUtils.clamp(caster.stat(Stat.COOLDOWN_REDUCTION_PERCENT), -100f, MAXIMUM_COOLDOWN_REDUCTION);
        return definition.cooldown * (1f - reductionPercent / 100f);
    }

    public static boolean tryCast(GameWorld world, Unit caster, SkillInstance skill, Vector2 aimPoint) {
        if (skill.cooldownRemaining > 0f) {
            return false;
        }
        cast(world, caster, skill.definition, skill.level, aimPoint, false);
        skill.cooldownRemaining = cooldownFor(caster, skill.definition);
        return true;
    }

    public static SkillCast cast(GameWorld world, Unit caster, SkillDefinition definition, int level, Vector2 aimPoint, boolean isEcho) {
        SkillCast cast = new SkillCast();
        cast.definition = definition;
        cast.caster = caster;
        cast.level = level;
        cast.isEcho = isEcho;
        cast.aimPoint.set(aimPoint);
        cast.aimAngle = Targeting.angleBetween(caster.position, aimPoint);
        cast.damage = definition.damage * (1f + DAMAGE_PER_LEVEL * (level - 1));
        cast.hitCount = definition.hitCount;
        cast.projectileCount = definition.projectileCount;
        if (cast.hasTag(SkillTag.MULTI_HIT)) {
            cast.hitCount += Math.round(caster.stat(Stat.EXTRA_SKILL_HITS));
        }
        if (cast.hasTag(SkillTag.PROJECTILE)) {
            cast.projectileCount += Math.round(caster.stat(Stat.EXTRA_PROJECTILES));
        }
        cast.areaMultiplier = Math.max(0.2f, 1f + caster.stat(Stat.AREA_PERCENT) / 100f);
        for (PerkInstance perk : caster.perks) {
            if (perk.definition.modifySkillCast != null) {
                perk.definition.modifySkillCast.run(world, cast, perk);
            }
        }
        AnimationSystem.attackPose(caster, cast.aimAngle);
        definition.action.run(world, cast);
        for (PerkInstance perk : caster.perks) {
            if (perk.definition.onSkillCast != null) {
                perk.definition.onSkillCast.run(world, cast, perk);
            }
        }
        return cast;
    }

    public static void recast(GameWorld world, SkillCast original, float delaySeconds) {
        Vector2 aimPoint = new Vector2(original.aimPoint);
        world.schedule(delaySeconds, () -> {
            if (!original.caster.isDead) {
                cast(world, original.caster, original.definition, original.level, aimPoint, true);
            }
        });
    }

    public static void updateCooldowns(RunState run, float delta) {
        for (SkillInstance skill : run.skills) {
            if (skill != null) {
                skill.cooldownRemaining = Math.max(0f, skill.cooldownRemaining - delta);
            }
        }
    }

    public static void reduceAllCooldowns(RunState run, float seconds) {
        updateCooldowns(run, seconds);
    }
}
