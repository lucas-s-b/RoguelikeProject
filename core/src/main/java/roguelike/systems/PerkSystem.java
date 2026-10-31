package roguelike.systems;

import com.badlogic.gdx.math.MathUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import roguelike.definitions.ContentRegistry;
import roguelike.definitions.PerkDefinition;
import roguelike.model.PerkFamily;
import roguelike.model.PerkInstance;
import roguelike.model.Rarity;
import roguelike.model.RunState;
import roguelike.model.Unit;

public final class PerkSystem {
    public static final float FAMILY_OFFER_CHANCE = 0.85f;

    private PerkSystem() {
    }

    public static PerkInstance find(Unit unit, String perkId) {
        for (PerkInstance perk : unit.perks) {
            if (perk.definition.id.equals(perkId)) {
                return perk;
            }
        }
        return null;
    }

    public static int stacksOf(Unit unit, String perkId) {
        PerkInstance perk = find(unit, perkId);
        return perk == null ? 0 : perk.stacks;
    }

    public static int familyCount(Unit unit, PerkFamily family) {
        int count = 0;
        for (PerkInstance perk : unit.perks) {
            if (perk.definition.family == family) {
                count++;
            }
        }
        return count;
    }

    public static boolean canGain(Unit unit, PerkDefinition definition) {
        return stacksOf(unit, definition.id) < definition.maxStacks;
    }

    public static void addPerk(RunState run, Unit unit, PerkDefinition definition) {
        float previousMaxHealth = unit.maxHealth();
        PerkInstance perk = find(unit, definition.id);
        if (perk == null) {
            perk = new PerkInstance(definition);
            unit.perks.add(perk);
        } else {
            perk.stacks++;
        }
        StatSystem.recalculate(unit);
        float maxHealthGained = unit.maxHealth() - previousMaxHealth;
        if (maxHealthGained > 0f) {
            unit.health += maxHealthGained;
        }
        if (definition.onAcquire != null) {
            definition.onAcquire.run(run, unit, perk);
        }
    }

    public static List<PerkDefinition> rollChoices(Unit unit, int count, Rarity minimumRarity) {
        List<PerkDefinition> pool = new ArrayList<>();
        for (PerkDefinition definition : ContentRegistry.perks.values()) {
            if (canGain(unit, definition) && definition.rarity.ordinal() >= minimumRarity.ordinal()) {
                pool.add(definition);
            }
        }
        Set<PerkFamily> familiesOnOffer = new HashSet<>();
        for (PerkDefinition definition : pool) {
            familiesOnOffer.add(definition.family);
        }
        List<PerkFamily> familyTickets = new ArrayList<>();
        for (PerkInstance perk : unit.perks) {
            if (perk.definition.family != PerkFamily.GENERAL && familiesOnOffer.contains(perk.definition.family)) {
                familyTickets.add(perk.definition.family);
            }
        }
        List<PerkDefinition> choices = new ArrayList<>();
        if (!familyTickets.isEmpty() && MathUtils.random() < FAMILY_OFFER_CHANCE) {
            PerkFamily favoredFamily = familyTickets.get(MathUtils.random(familyTickets.size() - 1));
            List<PerkDefinition> familyPool = new ArrayList<>();
            for (PerkDefinition definition : pool) {
                if (definition.family == favoredFamily) {
                    familyPool.add(definition);
                }
            }
            PerkDefinition familyChoice = weightedPick(familyPool);
            pool.remove(familyChoice);
            choices.add(familyChoice);
        }
        while (choices.size() < count && !pool.isEmpty()) {
            PerkDefinition picked = weightedPick(pool);
            pool.remove(picked);
            choices.add(picked);
        }
        if (!choices.isEmpty()) {
            choices.add(MathUtils.random(choices.size() - 1), choices.remove(0));
        }
        return choices;
    }

    private static PerkDefinition weightedPick(List<PerkDefinition> pool) {
        int totalWeight = 0;
        for (PerkDefinition definition : pool) {
            totalWeight += definition.rarity.weight;
        }
        int roll = MathUtils.random(totalWeight - 1);
        for (PerkDefinition definition : pool) {
            roll -= definition.rarity.weight;
            if (roll < 0) {
                return definition;
            }
        }
        return pool.get(pool.size() - 1);
    }
}
