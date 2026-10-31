package roguelike.systems;

import com.badlogic.gdx.math.MathUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import roguelike.definitions.ContentRegistry;
import roguelike.definitions.PerkDefinition;
import roguelike.definitions.SkillDefinition;
import roguelike.definitions.WeaponDefinition;
import roguelike.model.Rarity;
import roguelike.model.RunState;
import roguelike.model.SkillInstance;
import roguelike.model.WeaponInstance;

public final class ShopSystem {
    public static final int OFFERS_PER_CATEGORY = 3;
    public static final float HEAL_FRACTION = 0.35f;
    private static final int BASE_REROLL_COST = 15;
    private static final int REROLL_COST_INCREASE = 10;

    public static final class Offers {
        public final List<WeaponDefinition> weapons = new ArrayList<>();
        public final List<SkillDefinition> skills = new ArrayList<>();
        public final List<PerkDefinition> perks = new ArrayList<>();
        public int rerollCost = BASE_REROLL_COST;
        public boolean isHealBought;
    }

    private ShopSystem() {
    }

    public static Offers createOffers(RunState run) {
        Offers offers = new Offers();
        rollOffers(run, offers);
        return offers;
    }

    public static void rollOffers(RunState run, Offers offers) {
        List<WeaponDefinition> weaponPool = new ArrayList<>();
        for (WeaponDefinition weapon : ContentRegistry.weapons.values()) {
            if (weapon.isSoldInShop) {
                weaponPool.add(weapon);
            }
        }
        List<SkillDefinition> skillPool = new ArrayList<>(ContentRegistry.skills.values());
        Collections.shuffle(weaponPool);
        Collections.shuffle(skillPool);
        offers.weapons.clear();
        offers.weapons.addAll(weaponPool.subList(0, Math.min(OFFERS_PER_CATEGORY, weaponPool.size())));
        offers.skills.clear();
        offers.skills.addAll(skillPool.subList(0, Math.min(OFFERS_PER_CATEGORY, skillPool.size())));
        offers.perks.clear();
        offers.perks.addAll(PerkSystem.rollChoices(run.player, OFFERS_PER_CATEGORY, Rarity.COMMON));
    }

    public static int weaponPrice(RunState run, WeaponDefinition weapon) {
        boolean isOwned = run.weapon != null && run.weapon.definition == weapon;
        return isOwned ? Math.round(weapon.price * 0.6f * (run.weapon.level + 1)) : weapon.price;
    }

    public static int skillPrice(RunState run, SkillDefinition skill) {
        SkillInstance owned = ownedSkill(run, skill);
        return owned == null ? skill.price : Math.round(skill.price * 0.6f * (owned.level + 1));
    }

    public static int perkPrice(PerkDefinition perk) {
        return perk.rarity.price;
    }

    public static int healPrice(RunState run) {
        return 20 + run.roomsCleared * 4;
    }

    public static SkillInstance ownedSkill(RunState run, SkillDefinition skill) {
        for (SkillInstance owned : run.skills) {
            if (owned != null && owned.definition == skill) {
                return owned;
            }
        }
        return null;
    }

    public static int freeSkillSlot(RunState run) {
        for (int slot = 0; slot < run.skills.length; slot++) {
            if (run.skills[slot] == null) {
                return slot;
            }
        }
        return -1;
    }

    public static boolean canBuySkill(RunState run, SkillDefinition skill) {
        return ownedSkill(run, skill) != null || freeSkillSlot(run) >= 0;
    }

    public static boolean buyWeapon(RunState run, Offers offers, WeaponDefinition weapon) {
        int price = weaponPrice(run, weapon);
        if (run.gold < price) {
            return false;
        }
        run.gold -= price;
        if (run.weapon != null && run.weapon.definition == weapon) {
            run.weapon.level++;
        } else {
            run.weapon = new WeaponInstance(weapon);
        }
        offers.weapons.remove(weapon);
        return true;
    }

    public static boolean buySkill(RunState run, Offers offers, SkillDefinition skill) {
        int price = skillPrice(run, skill);
        if (run.gold < price || !canBuySkill(run, skill)) {
            return false;
        }
        run.gold -= price;
        SkillInstance owned = ownedSkill(run, skill);
        if (owned != null) {
            owned.level++;
        } else {
            run.skills[freeSkillSlot(run)] = new SkillInstance(skill);
        }
        offers.skills.remove(skill);
        return true;
    }

    public static boolean buyPerk(RunState run, Offers offers, PerkDefinition perk) {
        int price = perkPrice(perk);
        if (run.gold < price || !PerkSystem.canGain(run.player, perk)) {
            return false;
        }
        run.gold -= price;
        PerkSystem.addPerk(run, run.player, perk);
        offers.perks.remove(perk);
        return true;
    }

    public static boolean buyHeal(RunState run, Offers offers) {
        int price = healPrice(run);
        if (run.gold < price || offers.isHealBought || run.player.health >= run.player.maxHealth()) {
            return false;
        }
        run.gold -= price;
        run.player.health = Math.min(run.player.maxHealth(), run.player.health + run.player.maxHealth() * HEAL_FRACTION);
        offers.isHealBought = true;
        return true;
    }

    public static boolean reroll(RunState run, Offers offers) {
        if (run.gold < offers.rerollCost) {
            return false;
        }
        run.gold -= offers.rerollCost;
        offers.rerollCost += REROLL_COST_INCREASE;
        rollOffers(run, offers);
        return true;
    }

    public static void sellSkill(RunState run, int slot) {
        SkillInstance skill = run.skills[slot];
        if (skill == null) {
            return;
        }
        run.gold += MathUtils.round(skill.definition.price * 0.4f);
        run.skills[slot] = null;
    }
}
