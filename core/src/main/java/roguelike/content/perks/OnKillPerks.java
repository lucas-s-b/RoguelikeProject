package roguelike.content.perks;

import com.badlogic.gdx.math.MathUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import roguelike.content.StatusContent;
import roguelike.definitions.ContentRegistry;
import roguelike.definitions.PerkDefinition;
import roguelike.model.DamageSpec;
import roguelike.model.DamageType;
import roguelike.model.PerkFamily;
import roguelike.model.Projectile;
import roguelike.model.Rarity;
import roguelike.model.Stat;
import roguelike.model.StatusInstance;
import roguelike.model.TimedBuff;
import roguelike.model.Unit;
import roguelike.systems.Attacks;
import roguelike.systems.CombatSystem;
import roguelike.systems.SkillSystem;
import roguelike.systems.StatSystem;
import roguelike.systems.StatusSystem;
import roguelike.systems.Targeting;

public final class OnKillPerks {
    private static final List<String> SPREADING_STATUSES = List.of(StatusContent.BURN, StatusContent.POISON, StatusContent.BLEED);

    private OnKillPerks() {
    }

    public static void register() {
        ContentRegistry.add(PerkDefinition.create("corpse_explosion")
                .name("Corpse Explosion")
                .description("Enemies you kill explode, dealing 25% of their max HP to nearby enemies.")
                .rarity(Rarity.EPIC)
                .family(PerkFamily.SLAYER)
                .maxStacks(2)
                .onKill((world, killer, victim, perk) -> {
                    float explosionDamage = victim.maxHealth() * 0.25f * perk.stacks;
                    Attacks.circleBlast(world, killer, victim.position, 80f, DamageSpec.perk(explosionDamage, DamageType.PHYSICAL), victim.color);
                }));

        ContentRegistry.add(PerkDefinition.create("soul_harvest")
                .name("Soul Harvest")
                .description("Kills heal 2 HP.")
                .rarity(Rarity.COMMON)
                .family(PerkFamily.SLAYER)
                .maxStacks(3)
                .onKill((world, killer, victim, perk) -> CombatSystem.heal(world, killer, 2f * perk.stacks)));

        ContentRegistry.add(PerkDefinition.create("bloodlust")
                .name("Bloodlust")
                .description("Kills grant 10% attack speed for 3 seconds, stacking up to 50%.")
                .rarity(Rarity.RARE)
                .family(PerkFamily.SLAYER)
                .maxStacks(2)
                .onKill((world, killer, victim, perk) -> {
                    TimedBuff current = StatSystem.findBuff(killer, "bloodlust", Stat.ATTACK_SPEED_PERCENT);
                    float currentAmount = current == null ? 0f : current.amount;
                    float newAmount = Math.min(50f, currentAmount + 10f * perk.stacks);
                    StatSystem.setBuff(killer, "bloodlust", Stat.ATTACK_SPEED_PERCENT, newAmount, 3f);
                }));

        ContentRegistry.add(PerkDefinition.create("greed")
                .name("Greed")
                .description("Enemies drop 1 extra gold.")
                .rarity(Rarity.COMMON)
                .family(PerkFamily.GOLD)
                .maxStacks(5)
                .onKill((world, killer, victim, perk) -> world.run.gold += perk.stacks));

        ContentRegistry.add(PerkDefinition.create("contagion")
                .name("Contagion")
                .description("When an enemy dies, its Burn, Poison and Bleed spread to the 2 nearest enemies.")
                .rarity(Rarity.EPIC)
                .family(PerkFamily.POISON)
                .maxStacks(2)
                .onKill((world, killer, victim, perk) -> {
                    List<StatusInstance> statusesToSpread = new ArrayList<>();
                    for (StatusInstance status : victim.statuses) {
                        if (SPREADING_STATUSES.contains(status.definition.id)) {
                            statusesToSpread.add(status);
                        }
                    }
                    if (statusesToSpread.isEmpty()) {
                        return;
                    }
                    Set<Unit> alreadyInfected = new HashSet<>();
                    for (int spreadIndex = 0; spreadIndex < 2 * perk.stacks; spreadIndex++) {
                        Unit nearest = Targeting.nearestEnemy(world, killer.team, victim.position, 220f, alreadyInfected);
                        if (nearest == null) {
                            return;
                        }
                        alreadyInfected.add(nearest);
                        for (StatusInstance status : statusesToSpread) {
                            StatusSystem.apply(nearest, status.definition.id, status.stacks, killer);
                        }
                    }
                }));

        ContentRegistry.add(PerkDefinition.create("vengeful_spirit")
                .name("Vengeful Spirit")
                .description("Kills release a homing spirit that deals 10 magic damage and applies on-hit effects.")
                .rarity(Rarity.RARE)
                .family(PerkFamily.SLAYER)
                .maxStacks(2)
                .onKill((world, killer, victim, perk) -> {
                    for (int spiritIndex = 0; spiritIndex < perk.stacks; spiritIndex++) {
                        DamageSpec spiritDamage = DamageSpec.perk(10f, DamageType.MAGIC).withOnHit(true);
                        Projectile spirit = Attacks.fireProjectile(world, killer, MathUtils.random(360f), 300f, spiritDamage, DamageType.MAGIC.color);
                        spirit.position.set(victim.position);
                        spirit.isHoming = true;
                        spirit.lifetimeRemaining = 2.5f;
                    }
                }));

        ContentRegistry.add(PerkDefinition.create("adrenaline")
                .name("Adrenaline")
                .description("Kills reduce your skill cooldowns by 0.3 seconds.")
                .rarity(Rarity.COMMON)
                .family(PerkFamily.SKILL)
                .maxStacks(3)
                .onKill((world, killer, victim, perk) -> SkillSystem.reduceAllCooldowns(world.run, 0.3f * perk.stacks)));
    }
}
