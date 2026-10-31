package roguelike.content.perks;

import roguelike.definitions.ContentRegistry;
import roguelike.definitions.PerkDefinition;
import roguelike.model.PerkFamily;
import roguelike.model.Rarity;
import roguelike.model.Stat;

public final class StatPerks {
    private StatPerks() {
    }

    public static void register() {
        ContentRegistry.add(PerkDefinition.create("iron_skin")
                .name("Iron Skin")
                .description("Gain 2 armor. Armor reduces the damage of every hit.")
                .rarity(Rarity.COMMON)
                .family(PerkFamily.DEFENSE)
                .maxStacks(5)
                .stat(Stat.ARMOR, 2f));

        ContentRegistry.add(PerkDefinition.create("vitality")
                .name("Vitality")
                .description("Gain 20 max HP.")
                .rarity(Rarity.COMMON)
                .family(PerkFamily.DEFENSE)
                .maxStacks(5)
                .stat(Stat.MAX_HEALTH, 20f));

        ContentRegistry.add(PerkDefinition.create("swift_feet")
                .name("Swift Feet")
                .description("Move 10% faster.")
                .rarity(Rarity.COMMON)
                .family(PerkFamily.GENERAL)
                .maxStacks(3)
                .stat(Stat.MOVE_SPEED_PERCENT, 10f));

        ContentRegistry.add(PerkDefinition.create("sharpened_steel")
                .name("Sharpened Steel")
                .description("Weapon and skill hits deal 2 more damage.")
                .rarity(Rarity.COMMON)
                .family(PerkFamily.GENERAL)
                .maxStacks(3)
                .stat(Stat.FLAT_DAMAGE, 2f));

        ContentRegistry.add(PerkDefinition.create("frenzy")
                .name("Frenzy")
                .description("Attack 12% faster.")
                .rarity(Rarity.COMMON)
                .family(PerkFamily.GENERAL)
                .maxStacks(3)
                .stat(Stat.ATTACK_SPEED_PERCENT, 12f));

        ContentRegistry.add(PerkDefinition.create("keen_eye")
                .name("Keen Eye")
                .description("Gain 6% crit chance.")
                .rarity(Rarity.COMMON)
                .family(PerkFamily.CRITICAL)
                .maxStacks(4)
                .stat(Stat.CRIT_CHANCE, 6f));

        ContentRegistry.add(PerkDefinition.create("brutality")
                .name("Brutality")
                .description("Critical hits deal 40% more damage.")
                .rarity(Rarity.RARE)
                .family(PerkFamily.CRITICAL)
                .maxStacks(3)
                .stat(Stat.CRIT_DAMAGE_PERCENT, 40f));

        ContentRegistry.add(PerkDefinition.create("glass_cannon")
                .name("Glass Cannon")
                .description("Deal 50% more damage, but lose 30% of your max HP.")
                .rarity(Rarity.EPIC)
                .family(PerkFamily.GENERAL)
                .maxStacks(1)
                .stat(Stat.DAMAGE_PERCENT, 50f)
                .stat(Stat.MAX_HEALTH_PERCENT, -30f));

        ContentRegistry.add(PerkDefinition.create("lucky_coin")
                .name("Lucky Coin")
                .description("Enemies drop 20% more gold.")
                .rarity(Rarity.COMMON)
                .family(PerkFamily.GOLD)
                .maxStacks(3)
                .stat(Stat.GOLD_PERCENT, 20f));

        ContentRegistry.add(PerkDefinition.create("regeneration")
                .name("Regeneration")
                .description("Regenerate 1 HP per second.")
                .rarity(Rarity.COMMON)
                .family(PerkFamily.DEFENSE)
                .maxStacks(3)
                .stat(Stat.HEALTH_REGEN, 1f));

        ContentRegistry.add(PerkDefinition.create("evasion")
                .name("Evasion")
                .description("Gain a 7% chance to dodge enemy hits.")
                .rarity(Rarity.COMMON)
                .family(PerkFamily.DEFENSE)
                .maxStacks(3)
                .stat(Stat.DODGE_CHANCE, 7f));
    }
}
