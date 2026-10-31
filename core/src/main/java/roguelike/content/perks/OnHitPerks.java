package roguelike.content.perks;

import com.badlogic.gdx.math.MathUtils;
import roguelike.content.StatusContent;
import roguelike.definitions.ContentRegistry;
import roguelike.definitions.PerkDefinition;
import roguelike.model.DamageSpec;
import roguelike.model.DamageType;
import roguelike.model.PerkFamily;
import roguelike.model.Projectile;
import roguelike.model.Rarity;
import roguelike.systems.Attacks;
import roguelike.systems.CombatSystem;
import roguelike.systems.EffectSystem;
import roguelike.systems.StatusSystem;

public final class OnHitPerks {
    private OnHitPerks() {
    }

    public static void register() {
        ContentRegistry.add(PerkDefinition.create("ember_touch")
                .name("Ember Touch")
                .description("Hits on Burning enemies deal 20% more damage.")
                .rarity(Rarity.COMMON)
                .family(PerkFamily.FIRE)
                .maxStacks(3)
                .modifyOutgoingHit((world, hit, perk) -> {
                    if (StatusSystem.has(hit.target, StatusContent.BURN)) {
                        hit.bonusDamagePercent += 20f * perk.stacks;
                    }
                }));

        ContentRegistry.add(PerkDefinition.create("kindling")
                .name("Kindling")
                .description("Hits apply 1 Burn.")
                .rarity(Rarity.COMMON)
                .family(PerkFamily.FIRE)
                .maxStacks(3)
                .onHit((world, hit, perk) -> StatusSystem.apply(hit.target, StatusContent.BURN, perk.stacks, hit.attacker)));

        ContentRegistry.add(PerkDefinition.create("venom_coating")
                .name("Venom Coating")
                .description("Hits apply 1 Poison.")
                .rarity(Rarity.COMMON)
                .family(PerkFamily.POISON)
                .maxStacks(3)
                .onHit((world, hit, perk) -> StatusSystem.apply(hit.target, StatusContent.POISON, perk.stacks, hit.attacker)));

        ContentRegistry.add(PerkDefinition.create("serrated_edge")
                .name("Serrated Edge")
                .description("Hits apply 1 Bleed.")
                .rarity(Rarity.COMMON)
                .family(PerkFamily.BLEED)
                .maxStacks(3)
                .onHit((world, hit, perk) -> StatusSystem.apply(hit.target, StatusContent.BLEED, perk.stacks, hit.attacker)));

        ContentRegistry.add(PerkDefinition.create("frostbite")
                .name("Frostbite")
                .description("Hits have a 20% chance to apply Chill.")
                .rarity(Rarity.COMMON)
                .family(PerkFamily.FROST)
                .maxStacks(3)
                .onHit((world, hit, perk) -> {
                    if (MathUtils.random(100f) < 20f * perk.stacks) {
                        StatusSystem.apply(hit.target, StatusContent.CHILL, 1, hit.attacker);
                    }
                }));

        ContentRegistry.add(PerkDefinition.create("static_charge")
                .name("Static Charge")
                .description("Every 5th hit releases chain lightning that jumps between 3 enemies for 8 damage.")
                .rarity(Rarity.RARE)
                .family(PerkFamily.LIGHTNING)
                .maxStacks(3)
                .onHit((world, hit, perk) -> {
                    perk.counter++;
                    if (perk.counter >= 5) {
                        perk.counter = 0;
                        Attacks.chainLightning(world, hit.attacker, hit.target, 2, 200f, DamageSpec.perk(8f * perk.stacks, DamageType.LIGHTNING));
                    }
                }));

        ContentRegistry.add(PerkDefinition.create("vampiric_touch")
                .name("Vampiric Touch")
                .description("Heal 1 HP every 3rd hit.")
                .rarity(Rarity.RARE)
                .family(PerkFamily.DEFENSE)
                .maxStacks(3)
                .onHit((world, hit, perk) -> {
                    perk.counter++;
                    if (perk.counter >= 3) {
                        perk.counter = 0;
                        CombatSystem.heal(world, hit.attacker, perk.stacks);
                    }
                }));

        ContentRegistry.add(PerkDefinition.create("thunderclap")
                .name("Thunderclap")
                .description("Hits have a 10% chance to cause a small explosion that deals 12 damage.")
                .rarity(Rarity.RARE)
                .family(PerkFamily.GENERAL)
                .maxStacks(2)
                .onHit((world, hit, perk) -> {
                    if (MathUtils.random(100f) < 10f * perk.stacks) {
                        Attacks.circleBlast(world, hit.attacker, hit.target.position, 60f, DamageSpec.perk(12f, DamageType.PHYSICAL), DamageType.FIRE.color);
                    }
                }));

        ContentRegistry.add(PerkDefinition.create("echoing_strikes")
                .name("Echoing Strikes")
                .description("Hits have a 20% chance to strike again for 50% damage.")
                .rarity(Rarity.EPIC)
                .family(PerkFamily.CRITICAL)
                .maxStacks(2)
                .onHit((world, hit, perk) -> {
                    if (MathUtils.random(100f) < 20f * perk.stacks) {
                        Attacks.hitUnit(world, hit.attacker, hit.target, DamageSpec.perk(hit.damage * 0.5f, hit.damageType));
                    }
                }));

        ContentRegistry.add(PerkDefinition.create("hunters_mark")
                .name("Hunter's Mark")
                .description("Hits apply Marked. Marked enemies take 8% more damage per stack.")
                .rarity(Rarity.COMMON)
                .family(PerkFamily.CRITICAL)
                .maxStacks(3)
                .onHit((world, hit, perk) -> StatusSystem.apply(hit.target, StatusContent.MARKED, perk.stacks, hit.attacker)));

        ContentRegistry.add(PerkDefinition.create("gold_leaf")
                .name("Gold Leaf")
                .description("Every 15th hit grants 1 gold.")
                .rarity(Rarity.COMMON)
                .family(PerkFamily.GOLD)
                .maxStacks(3)
                .onHit((world, hit, perk) -> {
                    perk.counter++;
                    if (perk.counter >= 15) {
                        perk.counter = 0;
                        world.run.gold += perk.stacks;
                        EffectSystem.text(world, hit.target.position, "+" + perk.stacks, EffectSystem.GOLD_COLOR, 0.9f);
                    }
                }));

        ContentRegistry.add(PerkDefinition.create("splinter")
                .name("Splinter")
                .description("Hits have a 15% chance to launch 2 shards from the target that deal 6 damage.")
                .rarity(Rarity.RARE)
                .family(PerkFamily.GENERAL)
                .maxStacks(3)
                .onHit((world, hit, perk) -> {
                    if (MathUtils.random(100f) >= 15f * perk.stacks) {
                        return;
                    }
                    for (int shardIndex = 0; shardIndex < 2; shardIndex++) {
                        Projectile shard = Attacks.fireProjectile(world, hit.attacker, MathUtils.random(360f), 420f, DamageSpec.perk(6f, DamageType.PHYSICAL), DamageType.PHYSICAL.color);
                        shard.position.set(hit.target.position);
                        shard.unitsAlreadyHit.add(hit.target);
                        shard.radius = 4f;
                        shard.lifetimeRemaining = 0.6f;
                    }
                }));

        ContentRegistry.add(PerkDefinition.create("shatter")
                .name("Shatter")
                .description("Hitting a Chilled enemy has a 15% chance to Freeze it for 1 second.")
                .rarity(Rarity.RARE)
                .family(PerkFamily.FROST)
                .maxStacks(2)
                .onHit((world, hit, perk) -> {
                    if (StatusSystem.has(hit.target, StatusContent.CHILL) && MathUtils.random(100f) < 15f * perk.stacks) {
                        StatusSystem.apply(hit.target, StatusContent.FROZEN, 1, hit.attacker);
                    }
                }));

        ContentRegistry.add(PerkDefinition.create("hemorrhage")
                .name("Hemorrhage")
                .description("Hitting an enemy with 5 Bleed consumes it to deal 30 damage.")
                .rarity(Rarity.RARE)
                .family(PerkFamily.BLEED)
                .maxStacks(2)
                .onHit((world, hit, perk) -> {
                    if (StatusSystem.stacksOf(hit.target, StatusContent.BLEED) >= 5) {
                        StatusSystem.remove(hit.target, StatusContent.BLEED);
                        Attacks.hitUnit(world, hit.attacker, hit.target, DamageSpec.perk(30f * perk.stacks, DamageType.BLEED));
                    }
                }));
    }
}
