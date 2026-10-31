package roguelike.content.perks;

import com.badlogic.gdx.math.MathUtils;
import roguelike.content.StatusContent;
import roguelike.definitions.ContentRegistry;
import roguelike.definitions.PerkDefinition;
import roguelike.model.DamageSpec;
import roguelike.model.DamageType;
import roguelike.model.HitSource;
import roguelike.model.PerkFamily;
import roguelike.model.Rarity;
import roguelike.systems.Attacks;
import roguelike.systems.CombatSystem;
import roguelike.systems.EffectSystem;
import roguelike.systems.StatusSystem;

public final class DefensivePerks {
    private DefensivePerks() {
    }

    public static void register() {
        ContentRegistry.add(PerkDefinition.create("thorns")
                .name("Thorns")
                .description("When an enemy hits you, deal 10 damage back to it.")
                .rarity(Rarity.COMMON)
                .family(PerkFamily.DEFENSE)
                .maxStacks(3)
                .onDamaged((world, hit, perk) -> {
                    if (hit.attacker != null && hit.attacker != hit.target && hit.source != HitSource.STATUS) {
                        Attacks.hitUnit(world, hit.target, hit.attacker, DamageSpec.perk(10f * perk.stacks, DamageType.PHYSICAL));
                    }
                }));

        ContentRegistry.add(PerkDefinition.create("second_wind")
                .name("Second Wind")
                .description("Once per room, survive a lethal hit with 1 HP and heal 30% of your max HP.")
                .rarity(Rarity.EPIC)
                .family(PerkFamily.DEFENSE)
                .maxStacks(1)
                .onDamaged((world, hit, perk) -> {
                    if (hit.target.health <= 0f && !perk.isUsed) {
                        perk.isUsed = true;
                        hit.target.health = 1f + hit.target.maxHealth() * 0.3f;
                        EffectSystem.ring(world, hit.target.position, 60f, EffectSystem.HEAL_COLOR, 0.6f);
                        EffectSystem.text(world, hit.target.position, perk.definition.name, EffectSystem.HEAL_COLOR, 1.1f);
                    }
                })
                .onRoomStart((world, owner, perk) -> perk.isUsed = false));

        ContentRegistry.add(PerkDefinition.create("berserker")
                .name("Berserker")
                .description("Deal up to 50% more damage the lower your HP is.")
                .rarity(Rarity.RARE)
                .family(PerkFamily.BLEED)
                .maxStacks(2)
                .modifyOutgoingHit((world, hit, perk) -> {
                    float missingHealthFraction = 1f - MathUtils.clamp(hit.attacker.healthFraction(), 0f, 1f);
                    hit.bonusDamagePercent += 50f * perk.stacks * missingHealthFraction;
                }));

        ContentRegistry.add(PerkDefinition.create("executioner")
                .name("Executioner")
                .description("Deal 60% more damage to enemies below 30% HP.")
                .rarity(Rarity.RARE)
                .family(PerkFamily.CRITICAL)
                .maxStacks(2)
                .modifyOutgoingHit((world, hit, perk) -> {
                    if (hit.target.healthFraction() < 0.3f) {
                        hit.bonusDamagePercent += 60f * perk.stacks;
                    }
                }));

        ContentRegistry.add(PerkDefinition.create("opportunist")
                .name("Opportunist")
                .description("Deal 15% more damage to enemies with any status effect.")
                .rarity(Rarity.COMMON)
                .family(PerkFamily.GENERAL)
                .maxStacks(3)
                .modifyOutgoingHit((world, hit, perk) -> {
                    if (!hit.target.statuses.isEmpty()) {
                        hit.bonusDamagePercent += 15f * perk.stacks;
                    }
                }));

        ContentRegistry.add(PerkDefinition.create("giant_slayer")
                .name("Giant Slayer")
                .description("Deal 25% more damage to bosses.")
                .rarity(Rarity.COMMON)
                .family(PerkFamily.GENERAL)
                .maxStacks(3)
                .modifyOutgoingHit((world, hit, perk) -> {
                    if (hit.target.isBoss) {
                        hit.bonusDamagePercent += 25f * perk.stacks;
                    }
                }));

        ContentRegistry.add(PerkDefinition.create("steady_aim")
                .name("Steady Aim")
                .description("Stand still for 1 second to make your hits deal 30% more damage until you move.")
                .rarity(Rarity.RARE)
                .family(PerkFamily.CRITICAL)
                .maxStacks(2)
                .onTick((world, owner, perk, delta) -> {
                    boolean isStandingStill = owner.moveDirection.isZero() && owner.dashTimeRemaining <= 0f;
                    perk.timer = isStandingStill ? perk.timer + delta : 0f;
                })
                .modifyOutgoingHit((world, hit, perk) -> {
                    if (perk.timer >= 1f) {
                        hit.bonusDamagePercent += 30f * perk.stacks;
                    }
                }));

        ContentRegistry.add(PerkDefinition.create("battle_ready")
                .name("Battle Ready")
                .description("Start every room with a 15 HP shield.")
                .rarity(Rarity.COMMON)
                .family(PerkFamily.DEFENSE)
                .maxStacks(3)
                .onRoomStart((world, owner, perk) -> owner.shield = Math.max(owner.shield, 15f * perk.stacks)));

        ContentRegistry.add(PerkDefinition.create("interest")
                .name("Interest")
                .description("When you clear a room, gain 10% of your gold (up to 20).")
                .rarity(Rarity.RARE)
                .family(PerkFamily.GOLD)
                .maxStacks(1)
                .onRoomClear((world, owner, perk) -> world.run.gold += Math.min(20, Math.round(world.run.gold * 0.1f))));

        ContentRegistry.add(PerkDefinition.create("meditation")
                .name("Meditation")
                .description("When you clear a room, heal 10% of your max HP.")
                .rarity(Rarity.COMMON)
                .family(PerkFamily.DEFENSE)
                .maxStacks(3)
                .onRoomClear((world, owner, perk) -> CombatSystem.heal(world, owner, owner.maxHealth() * 0.1f * perk.stacks)));

        ContentRegistry.add(PerkDefinition.create("cold_blood")
                .name("Cold Blood")
                .description("Gain 30% crit chance against Chilled or Frozen enemies.")
                .rarity(Rarity.RARE)
                .family(PerkFamily.FROST)
                .maxStacks(2)
                .modifyOutgoingHit((world, hit, perk) -> {
                    boolean isTargetCold = StatusSystem.has(hit.target, StatusContent.CHILL) || StatusSystem.has(hit.target, StatusContent.FROZEN);
                    if (isTargetCold && hit.canCrit && MathUtils.random(100f) < 30f * perk.stacks) {
                        hit.isCritical = true;
                    }
                }));
    }
}
