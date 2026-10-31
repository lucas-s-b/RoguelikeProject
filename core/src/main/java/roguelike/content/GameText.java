package roguelike.content;

import roguelike.model.PerkFamily;
import roguelike.model.Rarity;
import roguelike.model.Stat;
import roguelike.systems.ShopSystem;
import roguelike.ui.PixelFont;

public final class GameText {
    public static final String GAME_TITLE = "Roguelike Prototype";
    public static final String MENU_SUBTITLE = "Clear rooms, collect perks, build something broken.";
    public static final String START_RUN = "Start run";
    public static final String QUIT = "Quit";
    public static final String CONTROLS = "WASD to move. Hold left mouse to attack. Q, E, R, F to use skills. Hold Tab to view your build. Esc to pause.";
    public static final String EMPTY_SLOT = "Empty";
    public static final String ROOM_CLEARED = "Room cleared";
    public static final String YOUR_BUILD = "Your build";
    public static final String PERKS = "Perks";
    public static final String STATS = "Stats";
    public static final String NO_PERKS_YET = "No perks yet";
    public static final String PAUSED = "Paused";
    public static final String RESUME = "Resume";
    public static final String ABANDON_RUN = "Abandon run";
    public static final String CHOOSE_PERK = "Choose a perk";
    public static final String CHOOSE_RARE_PERK = "Boss defeated! Choose a rare perk";
    public static final String CONTINUE = "Continue";
    public static final String SHOP = "Shop";
    public static final String WEAPONS = "Weapons";
    public static final String SKILLS = "Skills";
    public static final String YOUR_SKILLS = "Your skills";
    public static final String EQUIPPED = "Equipped";
    public static final String NO_FREE_SLOT = "No free slot";
    public static final String SOLD_OUT = "Sold out";
    public static final String NEXT_ROOM = "Next room";
    public static final String YOU_DIED = "You died";
    public static final String BACK_TO_MENU = "Back to menu";
    public static final String FAMILIES = "Families";

    private GameText() {
    }

    public static String room(int stageNumber, int roomInStage, boolean isBossRoom) {
        return isBossRoom ? "Stage " + stageNumber + ", Boss" : "Stage " + stageNumber + ", Room " + roomInStage;
    }

    public static String stage(int stageNumber) {
        return "Stage " + stageNumber;
    }

    public static String familyName(PerkFamily family) {
        return switch (family) {
            case GENERAL -> null;
            case FIRE -> "Fire";
            case POISON -> "Poison";
            case BLEED -> "Bleed";
            case FROST -> "Frost";
            case LIGHTNING -> "Lightning";
            case CRITICAL -> "Critical";
            case SKILL -> "Skill";
            case SLAYER -> "Slayer";
            case DEFENSE -> "Defense";
            case GOLD -> "Gold";
        };
    }

    public static String gold(int amount) {
        return "Gold: " + amount;
    }

    public static String health(float current, float maximum) {
        return "HP " + (int) Math.ceil(current) + " / " + Math.round(maximum);
    }

    public static String withLevel(String name, int level) {
        return name + " Lv" + PixelFont.NON_BREAKING_SPACE + level;
    }

    public static String roomClearedGold(int amount) {
        return "Room cleared. +" + amount + " gold";
    }

    public static String owned(int stacks, int maxStacks) {
        return "Owned: " + stacks + " / " + maxStacks;
    }

    public static String price(int amount) {
        return amount + " gold";
    }

    public static String upgradeTo(int level) {
        return "Upgrade to Lv " + level;
    }

    public static String sell(int amount) {
        return "Sell (+" + amount + " gold)";
    }

    public static String heal(int price) {
        return "Heal " + Math.round(ShopSystem.HEAL_FRACTION * 100f) + "% HP (" + price + " gold)";
    }

    public static String reroll(int price) {
        return "Reroll (" + price + " gold)";
    }

    public static String roomsCleared(int amount) {
        return "Rooms cleared: " + amount;
    }

    public static String enemiesDefeated(int amount) {
        return "Enemies defeated: " + amount;
    }

    public static String perksCollected(int amount) {
        return "Perks collected: " + amount;
    }

    public static String rarityName(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> "Common";
            case RARE -> "Rare";
            case EPIC -> "Epic";
            case LEGENDARY -> "Legendary";
        };
    }

    public static String statName(Stat stat) {
        return switch (stat) {
            case MAX_HEALTH -> "Max health";
            case HEALTH_REGEN -> "Health regen";
            case ARMOR -> "Armor";
            case DODGE_CHANCE -> "Dodge chance";
            case MOVE_SPEED -> "Move speed";
            case FLAT_DAMAGE -> "Bonus damage";
            case DAMAGE_PERCENT -> "Damage";
            case SKILL_DAMAGE_PERCENT -> "Skill damage";
            case ATTACK_SPEED_PERCENT -> "Attack speed";
            case CRIT_CHANCE -> "Crit chance";
            case CRIT_DAMAGE_PERCENT -> "Crit damage";
            case COOLDOWN_REDUCTION_PERCENT -> "Cooldown reduction";
            case AREA_PERCENT -> "Area size";
            case EXTRA_SKILL_HITS -> "Extra skill hits";
            case EXTRA_PROJECTILES -> "Extra projectiles";
            case ON_HIT_REPEATS -> "On-hit repeats";
            case LIFE_ON_HIT -> "Life per hit";
            case GOLD_PERCENT -> "Gold gain";
            case MAX_HEALTH_PERCENT, MOVE_SPEED_PERCENT, EXTRA_CHAIN_BOUNCES -> null;
        };
    }
}
