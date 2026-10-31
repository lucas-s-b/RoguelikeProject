package roguelike.content.perks;

import com.badlogic.gdx.math.Vector2;
import roguelike.definitions.ContentRegistry;
import roguelike.definitions.PerkDefinition;
import roguelike.model.DamageSpec;
import roguelike.model.DamageType;
import roguelike.model.HitSource;
import roguelike.model.PerkFamily;
import roguelike.model.Rarity;
import roguelike.model.Stat;
import roguelike.model.Unit;
import roguelike.systems.Attacks;
import roguelike.systems.EffectSystem;
import roguelike.systems.Targeting;

public final class LegendaryPerks {
    private LegendaryPerks() {
    }

    public static void register() {
        ContentRegistry.add(PerkDefinition.create("twin_souls")
                .name("Twin Souls")
                .description("All your on-hit effects trigger twice.")
                .rarity(Rarity.LEGENDARY)
                .family(PerkFamily.GENERAL)
                .maxStacks(1)
                .stat(Stat.ON_HIT_REPEATS, 1f));

        ContentRegistry.add(PerkDefinition.create("storm_caller")
                .name("Storm Caller")
                .description("Every 2 seconds, lightning strikes a random enemy for 20 damage and applies on-hit effects.")
                .rarity(Rarity.LEGENDARY)
                .family(PerkFamily.LIGHTNING)
                .maxStacks(1)
                .onTick((world, owner, perk, delta) -> {
                    perk.timer += delta;
                    if (perk.timer < 2f) {
                        return;
                    }
                    perk.timer = 0f;
                    Unit target = Targeting.randomEnemy(world, owner.team);
                    if (target == null) {
                        return;
                    }
                    Vector2 skyPoint = new Vector2(target.position).add(0f, 300f);
                    EffectSystem.lightning(world, skyPoint, target.position, DamageType.LIGHTNING.color, 5f, 0.25f);
                    Attacks.hitUnit(world, owner, target, DamageSpec.perk(20f, DamageType.LIGHTNING).withOnHit(true));
                }));

        ContentRegistry.add(PerkDefinition.create("inferno")
                .name("Inferno")
                .description("Your Burn deals double damage.")
                .rarity(Rarity.LEGENDARY)
                .family(PerkFamily.FIRE)
                .maxStacks(1)
                .modifyOutgoingHit((world, hit, perk) -> {
                    if (hit.source == HitSource.STATUS && hit.damageType == DamageType.FIRE) {
                        hit.damage *= 2f;
                    }
                }));

        ContentRegistry.add(PerkDefinition.create("plague_bearer")
                .name("Plague Bearer")
                .description("Your Poison deals double damage.")
                .rarity(Rarity.LEGENDARY)
                .family(PerkFamily.POISON)
                .maxStacks(1)
                .modifyOutgoingHit((world, hit, perk) -> {
                    if (hit.source == HitSource.STATUS && hit.damageType == DamageType.POISON) {
                        hit.damage *= 2f;
                    }
                }));

        ContentRegistry.add(PerkDefinition.create("exsanguinate")
                .name("Exsanguinate")
                .description("Your Bleed deals double damage.")
                .rarity(Rarity.LEGENDARY)
                .family(PerkFamily.BLEED)
                .maxStacks(1)
                .modifyOutgoingHit((world, hit, perk) -> {
                    if (hit.source == HitSource.STATUS && hit.damageType == DamageType.BLEED) {
                        hit.damage *= 2f;
                    }
                }));

        ContentRegistry.add(PerkDefinition.create("thousand_cuts")
                .name("Thousand Cuts")
                .description("Every hit increases your damage by 0.2% until the room ends.")
                .rarity(Rarity.LEGENDARY)
                .family(PerkFamily.SLAYER)
                .maxStacks(1)
                .onHit((world, hit, perk) -> perk.counter = Math.min(500, perk.counter + 1))
                .modifyOutgoingHit((world, hit, perk) -> hit.bonusDamagePercent += perk.counter * 0.2f)
                .onRoomStart((world, owner, perk) -> perk.counter = 0));
    }
}
