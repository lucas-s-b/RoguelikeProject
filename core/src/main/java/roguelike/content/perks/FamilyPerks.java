package roguelike.content.perks;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import java.util.List;
import roguelike.content.StatusContent;
import roguelike.definitions.ContentRegistry;
import roguelike.definitions.PerkDefinition;
import roguelike.model.DamageSpec;
import roguelike.model.DamageType;
import roguelike.model.HitSource;
import roguelike.model.PerkFamily;
import roguelike.model.Rarity;
import roguelike.model.Stat;
import roguelike.model.Team;
import roguelike.model.Unit;
import roguelike.systems.Attacks;
import roguelike.systems.EffectSystem;
import roguelike.systems.PerkSystem;
import roguelike.systems.StatSystem;
import roguelike.systems.StatusSystem;
import roguelike.systems.Targeting;

public final class FamilyPerks {
    private FamilyPerks() {
    }

    public static void register() {
        ContentRegistry.add(PerkDefinition.create("combustion")
                .name("Combustion")
                .description("Burning enemies explode when they die, dealing 10 fire damage for each Burn stack to enemies nearby.")
                .rarity(Rarity.RARE)
                .family(PerkFamily.FIRE)
                .maxStacks(2)
                .onKill((world, killer, victim, perk) -> {
                    int burnStacks = StatusSystem.stacksOf(victim, StatusContent.BURN);
                    if (burnStacks > 0) {
                        Attacks.circleBlast(world, killer, victim.position, 90f, DamageSpec.perk(10f * burnStacks * perk.stacks, DamageType.FIRE), DamageType.FIRE.color);
                    }
                }));

        ContentRegistry.add(PerkDefinition.create("wildfire")
                .name("Wildfire")
                .description("Every second, each enemy with 3 or more Burn spreads half of it to the nearest enemy.")
                .rarity(Rarity.RARE)
                .family(PerkFamily.FIRE)
                .maxStacks(1)
                .onTick((world, owner, perk, delta) -> {
                    perk.timer += delta;
                    if (perk.timer < 1f) {
                        return;
                    }
                    perk.timer = 0f;
                    for (Unit burning : Targeting.enemiesOf(world, owner.team)) {
                        int burnStacks = StatusSystem.stacksOf(burning, StatusContent.BURN);
                        Unit nearest = burnStacks < 3 ? null : Targeting.nearestEnemy(world, owner.team, burning.position, 160f, List.of(burning));
                        if (nearest != null) {
                            StatusSystem.apply(nearest, StatusContent.BURN, burnStacks / 2, owner);
                            EffectSystem.line(world, burning.position, nearest.position, 3f, DamageType.FIRE.color, 0.2f);
                        }
                    }
                }));

        ContentRegistry.add(PerkDefinition.create("pyromaniac")
                .name("Pyromaniac")
                .description("Burn deals 12% more damage for each Fire perk you own.")
                .rarity(Rarity.RARE)
                .family(PerkFamily.FIRE)
                .maxStacks(1)
                .modifyOutgoingHit((world, hit, perk) -> {
                    if (hit.source == HitSource.STATUS && hit.damageType == DamageType.FIRE) {
                        hit.damage *= 1f + 0.12f * PerkSystem.familyCount(hit.attacker, PerkFamily.FIRE);
                    }
                }));

        ContentRegistry.add(PerkDefinition.create("virulence")
                .name("Virulence")
                .description("Poison deals 15% more damage for each Poison perk you own.")
                .rarity(Rarity.RARE)
                .family(PerkFamily.POISON)
                .maxStacks(1)
                .modifyOutgoingHit((world, hit, perk) -> {
                    if (hit.source == HitSource.STATUS && hit.damageType == DamageType.POISON) {
                        hit.damage *= 1f + 0.15f * PerkSystem.familyCount(hit.attacker, PerkFamily.POISON);
                    }
                }));

        ContentRegistry.add(PerkDefinition.create("neurotoxin")
                .name("Neurotoxin")
                .description("Poisoned enemies deal 25% less damage to you.")
                .rarity(Rarity.RARE)
                .family(PerkFamily.POISON)
                .maxStacks(1)
                .modifyIncomingHit((world, hit, perk) -> {
                    if (hit.attacker != null && StatusSystem.has(hit.attacker, StatusContent.POISON)) {
                        hit.damage *= 0.75f;
                    }
                }));

        ContentRegistry.add(PerkDefinition.create("corrosion")
                .name("Corrosion")
                .description("Hits ignore 1 armor for every 3 Poison on the target.")
                .rarity(Rarity.RARE)
                .family(PerkFamily.POISON)
                .maxStacks(2)
                .modifyOutgoingHit((world, hit, perk) ->
                        hit.armorPenetration += StatusSystem.stacksOf(hit.target, StatusContent.POISON) / 3 * perk.stacks));

        ContentRegistry.add(PerkDefinition.create("lacerate")
                .name("Lacerate")
                .description("Critical hits apply 2 Bleed.")
                .rarity(Rarity.RARE)
                .family(PerkFamily.BLEED)
                .maxStacks(2)
                .onHit((world, hit, perk) -> {
                    if (hit.isCritical) {
                        StatusSystem.apply(hit.target, StatusContent.BLEED, 2 * perk.stacks, hit.attacker);
                    }
                }));

        ContentRegistry.add(PerkDefinition.create("bloodletting")
                .name("Bloodletting")
                .description("Bleed deals 20% more damage for each Bleed perk you own.")
                .rarity(Rarity.RARE)
                .family(PerkFamily.BLEED)
                .maxStacks(1)
                .modifyOutgoingHit((world, hit, perk) -> {
                    if (hit.source == HitSource.STATUS && hit.damageType == DamageType.BLEED) {
                        hit.damage *= 1f + 0.2f * PerkSystem.familyCount(hit.attacker, PerkFamily.BLEED);
                    }
                }));

        ContentRegistry.add(PerkDefinition.create("permafrost")
                .name("Permafrost")
                .description("Frozen enemies take 50% more damage from you.")
                .rarity(Rarity.RARE)
                .family(PerkFamily.FROST)
                .maxStacks(2)
                .modifyOutgoingHit((world, hit, perk) -> {
                    if (StatusSystem.has(hit.target, StatusContent.FROZEN)) {
                        hit.damage *= 1f + 0.5f * perk.stacks;
                    }
                }));

        ContentRegistry.add(PerkDefinition.create("numbing_cold")
                .name("Numbing Cold")
                .description("Chilled enemies deal 15% less damage to you.")
                .rarity(Rarity.COMMON)
                .family(PerkFamily.FROST)
                .maxStacks(2)
                .modifyIncomingHit((world, hit, perk) -> {
                    if (hit.attacker != null && StatusSystem.has(hit.attacker, StatusContent.CHILL)) {
                        hit.damage *= 1f - 0.15f * perk.stacks;
                    }
                }));

        ContentRegistry.add(PerkDefinition.create("hypothermia")
                .name("Hypothermia")
                .description("Chilled and Frozen enemies take 8% more damage from you for each Frost perk you own.")
                .rarity(Rarity.RARE)
                .family(PerkFamily.FROST)
                .maxStacks(1)
                .modifyOutgoingHit((world, hit, perk) -> {
                    boolean isCold = StatusSystem.has(hit.target, StatusContent.CHILL) || StatusSystem.has(hit.target, StatusContent.FROZEN);
                    if (isCold) {
                        hit.damage *= 1f + 0.08f * PerkSystem.familyCount(hit.attacker, PerkFamily.FROST);
                    }
                }));

        ContentRegistry.add(PerkDefinition.create("conductor")
                .name("Conductor")
                .description("Chain lightning jumps to 1 more enemy.")
                .rarity(Rarity.COMMON)
                .family(PerkFamily.LIGHTNING)
                .maxStacks(3)
                .stat(Stat.EXTRA_CHAIN_BOUNCES, 1f));

        ContentRegistry.add(PerkDefinition.create("overload")
                .name("Overload")
                .description("Lightning hits have a 15% chance to stun for a moment.")
                .rarity(Rarity.RARE)
                .family(PerkFamily.LIGHTNING)
                .maxStacks(2)
                .onHit((world, hit, perk) -> {
                    if (hit.damageType == DamageType.LIGHTNING && MathUtils.random(100f) < 15f * perk.stacks) {
                        StatusSystem.apply(hit.target, StatusContent.SHOCKED, 1, hit.attacker);
                    }
                }));

        ContentRegistry.add(PerkDefinition.create("closed_circuit")
                .name("Closed Circuit")
                .description("Chain lightning can now bounce on the same target.")
                .rarity(Rarity.RARE)
                .family(PerkFamily.LIGHTNING)
                .maxStacks(1)
                .stat(Stat.CHAIN_ITSELF, 1));

        ContentRegistry.add(PerkDefinition.create("superconductor")
                .name("Superconductor")
                .description("Lightning deals 15% more damage for each Lightning perk you own.")
                .rarity(Rarity.RARE)
                .family(PerkFamily.LIGHTNING)
                .maxStacks(1)
                .modifyOutgoingHit((world, hit, perk) -> {
                    if (hit.damageType == DamageType.LIGHTNING) {
                        hit.damage *= 1f + 0.15f * PerkSystem.familyCount(hit.attacker, PerkFamily.LIGHTNING);
                    }
                }));

        ContentRegistry.add(PerkDefinition.create("static_field")
                .name("Static Field")
                .description("Every 2 seconds, lightning strikes the nearest enemy for 15 damage for each Lightning perk you own.")
                .rarity(Rarity.RARE)
                .family(PerkFamily.LIGHTNING)
                .maxStacks(2)
                .onTick((world, owner, perk, delta) -> {
                    perk.timer += delta;
                    if (perk.timer < 2f) {
                        return;
                    }
                    perk.timer = 0f;
                    Unit target = Targeting.nearestEnemy(world, owner.team, owner.position, 500f, null);
                    if (target == null) {
                        return;
                    }
                    EffectSystem.lightning(world, new Vector2(target.position).add(0f, 300f), target.position, DamageType.LIGHTNING.color, 4f, 0.22f);
                    float damage = 15f * PerkSystem.familyCount(owner, PerkFamily.LIGHTNING) * perk.stacks;
                    Attacks.hitUnit(world, owner, target, DamageSpec.perk(damage, DamageType.LIGHTNING).withOnHit(true));
                }));

        ContentRegistry.add(PerkDefinition.create("precision")
                .name("Precision")
                .description("Gain 5% crit chance for each Critical perk you own.")
                .rarity(Rarity.RARE)
                .family(PerkFamily.CRITICAL)
                .maxStacks(1)
                .onTick((world, owner, perk, delta) ->
                        StatSystem.setBuff(owner, "precision", Stat.CRIT_CHANCE, 5f * PerkSystem.familyCount(owner, PerkFamily.CRITICAL), 0.5f)));

        ContentRegistry.add(PerkDefinition.create("arcane_mastery")
                .name("Arcane Mastery")
                .description("Skills deal 10% more damage for each Skill perk you own.")
                .rarity(Rarity.EPIC)
                .family(PerkFamily.SKILL)
                .maxStacks(1)
                .modifySkillCast((world, cast, perk) -> cast.damage *= 1f + 0.1f * PerkSystem.familyCount(cast.caster, PerkFamily.SKILL)));

        ContentRegistry.add(PerkDefinition.create("death_toll")
                .name("Death Toll")
                .description("Kills strike the nearest enemy for 20% of the victim's max HP for each Slayer perk you own.")
                .rarity(Rarity.RARE)
                .family(PerkFamily.SLAYER)
                .maxStacks(1)
                .onKill((world, killer, victim, perk) -> {
                    Unit nearest = Targeting.nearestEnemy(world, killer.team, victim.position, 260f, List.of(victim));
                    if (nearest == null) {
                        return;
                    }
                    EffectSystem.line(world, victim.position, nearest.position, 4f, victim.color, 0.25f);
                    float damage = victim.maxHealth() * 0.2f * PerkSystem.familyCount(killer, PerkFamily.SLAYER);
                    Attacks.hitUnit(world, killer, nearest, DamageSpec.perk(damage, DamageType.PHYSICAL));
                }));

        ContentRegistry.add(PerkDefinition.create("retribution")
                .name("Retribution")
                .description("When an enemy hits you, deal 6 times your armor as damage to enemies around you.")
                .rarity(Rarity.RARE)
                .family(PerkFamily.DEFENSE)
                .maxStacks(2)
                .onDamaged((world, hit, perk) -> {
                    if (hit.attacker != null && hit.attacker.team == Team.ENEMY && hit.source != HitSource.STATUS) {
                        float damage = 6f * hit.target.stat(Stat.ARMOR) * perk.stacks;
                        Attacks.circleBlast(world, hit.target, hit.target.position, 120f, DamageSpec.perk(damage, DamageType.PHYSICAL), EffectSystem.GOLD_COLOR);
                    }
                }));

        ContentRegistry.add(PerkDefinition.create("golden_touch")
                .name("Golden Touch")
                .description("Deal 1% more damage for every 8 gold you carry, up to 60%.")
                .rarity(Rarity.EPIC)
                .family(PerkFamily.GOLD)
                .maxStacks(1)
                .modifyOutgoingHit((world, hit, perk) -> hit.bonusDamagePercent += Math.min(60f, world.run.gold / 8f)));
    }
}
