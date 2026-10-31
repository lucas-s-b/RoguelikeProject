package roguelike.content.perks;

import roguelike.definitions.ContentRegistry;
import roguelike.definitions.PerkDefinition;
import roguelike.model.DamageSpec;
import roguelike.model.DamageType;
import roguelike.model.PerkFamily;
import roguelike.model.Rarity;
import roguelike.model.SkillTag;
import roguelike.model.Stat;
import roguelike.systems.Attacks;
import roguelike.systems.SkillSystem;

public final class SkillPerks {
    private SkillPerks() {
    }

    public static void register() {
        ContentRegistry.add(PerkDefinition.create("relentless")
                .name("Relentless")
                .description("Multi-hit skills hit 2 more times.")
                .rarity(Rarity.RARE)
                .family(PerkFamily.SKILL)
                .maxStacks(2)
                .stat(Stat.EXTRA_SKILL_HITS, 2f));

        ContentRegistry.add(PerkDefinition.create("split_shot")
                .name("Split Shot")
                .description("Projectile weapons and skills fire 1 extra projectile.")
                .rarity(Rarity.RARE)
                .family(PerkFamily.SKILL)
                .maxStacks(2)
                .stat(Stat.EXTRA_PROJECTILES, 1f));

        ContentRegistry.add(PerkDefinition.create("expansive_mind")
                .name("Expansive Mind")
                .description("Skills cover 15% more area.")
                .rarity(Rarity.COMMON)
                .family(PerkFamily.SKILL)
                .maxStacks(3)
                .stat(Stat.AREA_PERCENT, 15f));

        ContentRegistry.add(PerkDefinition.create("quick_thinking")
                .name("Quick Thinking")
                .description("Skill cooldowns are 8% shorter.")
                .rarity(Rarity.COMMON)
                .family(PerkFamily.SKILL)
                .maxStacks(5)
                .stat(Stat.COOLDOWN_REDUCTION_PERCENT, 8f));

        ContentRegistry.add(PerkDefinition.create("echo_cast")
                .name("Echo Cast")
                .description("Every 3rd skill you cast is cast again for free.")
                .rarity(Rarity.LEGENDARY)
                .family(PerkFamily.SKILL)
                .maxStacks(1)
                .onSkillCast((world, cast, perk) -> {
                    if (cast.isEcho) {
                        return;
                    }
                    perk.counter++;
                    if (perk.counter >= 3) {
                        perk.counter = 0;
                        SkillSystem.recast(world, cast, 0.25f);
                    }
                }));

        ContentRegistry.add(PerkDefinition.create("spellblade")
                .name("Spellblade")
                .description("After you cast a skill, your next 3 weapon attacks deal 50% more damage.")
                .rarity(Rarity.RARE)
                .family(PerkFamily.SKILL)
                .maxStacks(3)
                .onSkillCast((world, cast, perk) -> perk.counter = 3)
                .onWeaponAttack((world, attacker, damage, perk) -> {
                    if (perk.counter > 0) {
                        perk.counter--;
                        damage.damage *= 1f + 0.5f * perk.stacks;
                    }
                }));

        ContentRegistry.add(PerkDefinition.create("overcharge")
                .name("Overcharge")
                .description("Skills deal 35% more damage, but cooldowns are 10% longer.")
                .rarity(Rarity.RARE)
                .family(PerkFamily.SKILL)
                .maxStacks(2)
                .stat(Stat.SKILL_DAMAGE_PERCENT, 35f)
                .stat(Stat.COOLDOWN_REDUCTION_PERCENT, -10f));

        ContentRegistry.add(PerkDefinition.create("arcane_surge")
                .name("Arcane Surge")
                .description("Casting a skill releases a nova that deals 12 magic damage around you.")
                .rarity(Rarity.RARE)
                .family(PerkFamily.SKILL)
                .maxStacks(3)
                .onSkillCast((world, cast, perk) ->
                        Attacks.circleBlast(world, cast.caster, cast.caster.position, 120f, DamageSpec.perk(12f * perk.stacks, DamageType.MAGIC), DamageType.MAGIC.color)));

        ContentRegistry.add(PerkDefinition.create("afterimage")
                .name("Afterimage")
                .description("Movement skills leave a burst at your starting point that deals 15 damage.")
                .rarity(Rarity.COMMON)
                .family(PerkFamily.SKILL)
                .maxStacks(3)
                .onSkillCast((world, cast, perk) -> {
                    if (cast.hasTag(SkillTag.MOVEMENT)) {
                        Attacks.circleBlast(world, cast.caster, cast.caster.position, 70f, DamageSpec.perk(15f * perk.stacks, DamageType.PHYSICAL), cast.definition.color);
                    }
                }));

        ContentRegistry.add(PerkDefinition.create("focused_mind")
                .name("Focused Mind")
                .description("Single-hit skills deal 60% more damage.")
                .rarity(Rarity.EPIC)
                .family(PerkFamily.SKILL)
                .maxStacks(1)
                .modifySkillCast((world, cast, perk) -> {
                    if (!cast.hasTag(SkillTag.MULTI_HIT) && !cast.hasTag(SkillTag.DEFENSIVE)) {
                        cast.damage *= 1.6f;
                    }
                }));

        ContentRegistry.add(PerkDefinition.create("blood_ritual")
                .name("Blood Ritual")
                .description("Skills cost 3 HP to cast but deal 40% more damage.")
                .rarity(Rarity.EPIC)
                .family(PerkFamily.BLEED)
                .maxStacks(2)
                .modifySkillCast((world, cast, perk) -> cast.damage *= 1f + 0.4f * perk.stacks)
                .onSkillCast((world, cast, perk) -> {
                    if (!cast.isEcho) {
                        cast.caster.health = Math.max(1f, cast.caster.health - 3f * perk.stacks);
                    }
                }));
    }
}
