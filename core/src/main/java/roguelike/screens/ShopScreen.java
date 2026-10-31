package roguelike.screens;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import roguelike.RoguelikeGame;
import roguelike.content.GameText;
import roguelike.definitions.PerkDefinition;
import roguelike.definitions.SkillDefinition;
import roguelike.definitions.WeaponDefinition;
import roguelike.model.PerkFamily;
import roguelike.model.RunState;
import roguelike.model.SkillInstance;
import roguelike.systems.EffectSystem;
import roguelike.systems.PlayerControlSystem;
import roguelike.systems.ShopSystem;
import roguelike.ui.UiCanvas;

public final class ShopScreen extends ScreenAdapter {
    private static final float CARD_WIDTH = 296f;
    private static final float CARD_HEIGHT = 156f;
    private static final float CARD_SPACING = 312f;
    private static final float CARDS_LEFT = 32f;
    private static final float CARDS_WIDTH = 920f;
    private static final float WEAPON_ROW_Y = 478f;
    private static final float SKILL_ROW_Y = 284f;
    private static final float PERK_ROW_Y = 90f;
    private static final float HEADING_GAP = 22f;
    private static final float SIDE_X = 984f;
    private static final float SIDE_WIDTH = 264f;
    private static final float SLOT_HEIGHT = 92f;
    private static final float SLOT_SPACING = 100f;
    private static final float BUTTON_Y = 18f;
    private static final float BUTTON_HEIGHT = 56f;
    private static final Color EQUIPPED_TAG_COLOR = new Color(0.14f, 0.3f, 0.2f, 1f);

    private final RoguelikeGame game;
    private final RunState run;
    private final ShopSystem.Offers offers;
    private final Color disabledAccentColor = new Color();
    private float time;

    public ShopScreen(RoguelikeGame game, RunState run) {
        this.game = game;
        this.run = run;
        this.offers = ShopSystem.createOffers(run);
    }

    @Override
    public void render(float delta) {
        time += delta;
        ScreenUtils.clear(UiCanvas.INK);
        game.viewport.apply();
        UiCanvas ui = game.ui;
        ui.begin();
        ui.backdrop(Color.WHITE);
        ui.title(GameText.SHOP, CARDS_LEFT + 4f + ui.textWidth(GameText.SHOP, UiCanvas.HEADING_SIZE) / 2f, 710f, UiCanvas.HEADING_SIZE, UiCanvas.ACCENT_COLOR);
        String goldText = GameText.gold(run.gold);
        float goldRight = SIDE_X + SIDE_WIDTH;
        ui.textRight(goldText, goldRight, 700f, UiCanvas.TEXT_SIZE, EffectSystem.GOLD_COLOR);
        ui.icon(ui.art.coin, goldRight - ui.textWidth(goldText, UiCanvas.TEXT_SIZE) - 28f, 683f, UiCanvas.TEXT_SIZE, Color.WHITE);

        ui.heading(GameText.WEAPONS, CARDS_LEFT, WEAPON_ROW_Y + CARD_HEIGHT + HEADING_GAP, CARDS_WIDTH, UiCanvas.MUTED_TEXT_COLOR);
        WeaponDefinition weaponToBuy = null;
        for (int index = 0; index < offers.weapons.size(); index++) {
            WeaponDefinition weapon = offers.weapons.get(index);
            boolean isEquipped = run.weapon.definition == weapon;
            int price = ShopSystem.weaponPrice(run, weapon);
            String statusLabel = isEquipped ? GameText.upgradeTo(run.weapon.level + 1) : null;
            float cardX = CARDS_LEFT + index * CARD_SPACING;
            if (drawOfferCard(ui, cardX, WEAPON_ROW_Y, weapon.name, weapon.description, weapon.color, ui.art.emblem(weapon.damageType), Color.WHITE, price, statusLabel, run.gold >= price)) {
                weaponToBuy = weapon;
            }
            if (isEquipped) {
                float tagWidth = ui.textWidth(GameText.EQUIPPED, UiCanvas.TEXT_SIZE) + 20f;
                float tagX = cardX + CARD_WIDTH - tagWidth - 12f;
                float tagY = WEAPON_ROW_Y + CARD_HEIGHT - 12f;
                ui.frame(tagX, tagY, tagWidth, 26f, EQUIPPED_TAG_COLOR, null);
                ui.text(GameText.EQUIPPED, tagX + 10f, tagY + 20f, UiCanvas.TEXT_SIZE, UiCanvas.GOOD_COLOR);
            }
        }
        if (offers.weapons.isEmpty()) {
            ui.text(GameText.SOLD_OUT, CARDS_LEFT, WEAPON_ROW_Y + CARD_HEIGHT / 2f + 8f, UiCanvas.TEXT_SIZE, UiCanvas.MUTED_TEXT_COLOR);
        }

        ui.heading(GameText.SKILLS, CARDS_LEFT, SKILL_ROW_Y + CARD_HEIGHT + HEADING_GAP, CARDS_WIDTH, UiCanvas.MUTED_TEXT_COLOR);
        SkillDefinition skillToBuy = null;
        for (int index = 0; index < offers.skills.size(); index++) {
            SkillDefinition skill = offers.skills.get(index);
            SkillInstance owned = ShopSystem.ownedSkill(run, skill);
            boolean hasSlot = ShopSystem.canBuySkill(run, skill);
            int price = ShopSystem.skillPrice(run, skill);
            String statusLabel = owned != null ? GameText.upgradeTo(owned.level + 1) : hasSlot ? null : GameText.NO_FREE_SLOT;
            float cardX = CARDS_LEFT + index * CARD_SPACING;
            if (drawOfferCard(ui, cardX, SKILL_ROW_Y, skill.name, skill.description, skill.color, ui.art.emblem(skill.damageType), Color.WHITE, price, statusLabel, run.gold >= price && hasSlot)) {
                skillToBuy = skill;
            }
        }
        if (offers.skills.isEmpty()) {
            ui.text(GameText.SOLD_OUT, CARDS_LEFT, SKILL_ROW_Y + CARD_HEIGHT / 2f + 8f, UiCanvas.TEXT_SIZE, UiCanvas.MUTED_TEXT_COLOR);
        }

        ui.heading(GameText.PERKS, CARDS_LEFT, PERK_ROW_Y + CARD_HEIGHT + HEADING_GAP, CARDS_WIDTH, UiCanvas.MUTED_TEXT_COLOR);
        PerkDefinition perkToBuy = null;
        for (int index = 0; index < offers.perks.size(); index++) {
            PerkDefinition perk = offers.perks.get(index);
            int price = ShopSystem.perkPrice(perk);
            float cardX = CARDS_LEFT + index * CARD_SPACING;
            if (drawOfferCard(ui, cardX, PERK_ROW_Y, perk.name, perk.description, perk.rarity.color, ui.art.familyIcon(perk.family),
                    perk.family == PerkFamily.GENERAL ? perk.rarity.color : Color.WHITE, price, null, run.gold >= price)) {
                perkToBuy = perk;
            }
        }
        if (offers.perks.isEmpty()) {
            ui.text(GameText.SOLD_OUT, CARDS_LEFT, PERK_ROW_Y + CARD_HEIGHT / 2f + 8f, UiCanvas.TEXT_SIZE, UiCanvas.MUTED_TEXT_COLOR);
        }

        float slotsTop = WEAPON_ROW_Y + CARD_HEIGHT;
        ui.heading(GameText.YOUR_SKILLS, SIDE_X, slotsTop + HEADING_GAP, SIDE_WIDTH, UiCanvas.MUTED_TEXT_COLOR);
        int slotToSell = -1;
        for (int slot = 0; slot < run.skills.length; slot++) {
            SkillInstance skill = run.skills[slot];
            float slotY = slotsTop - SLOT_HEIGHT - slot * SLOT_SPACING;
            ui.frame(SIDE_X, slotY, SIDE_WIDTH, SLOT_HEIGHT, UiCanvas.PANEL_COLOR, null);
            ui.keycap(SIDE_X + 10f, slotY + SLOT_HEIGHT - 36f, Input.Keys.toString(PlayerControlSystem.SKILL_KEYS[slot]));
            if (skill == null) {
                ui.text(GameText.EMPTY_SLOT, SIDE_X + 44f, slotY + SLOT_HEIGHT - 16f, UiCanvas.TEXT_SIZE, UiCanvas.MUTED_TEXT_COLOR);
                continue;
            }
            ui.text(GameText.withLevel(skill.definition.name, skill.level), SIDE_X + 44f, slotY + SLOT_HEIGHT - 16f, UiCanvas.TEXT_SIZE, skill.definition.color);
            int sellValue = MathUtils.round(skill.definition.price * 0.4f);
            if (ui.button(SIDE_X + 10f, slotY + 10f, SIDE_WIDTH - 20f, 36f, GameText.sell(sellValue), ui.art.coin, true)) {
                slotToSell = slot;
            }
        }

        int healPrice = ShopSystem.healPrice(run);
        boolean canHeal = !offers.isHealBought && run.gold >= healPrice && run.player.health < run.player.maxHealth();
        boolean isHealPressed = ui.button(CARDS_LEFT, BUTTON_Y, 320f, BUTTON_HEIGHT, GameText.heal(healPrice), ui.art.potion, canHeal);
        boolean isRerollPressed = ui.button(CARDS_LEFT + 336f, BUTTON_Y, 280f, BUTTON_HEIGHT, GameText.reroll(offers.rerollCost), ui.art.dice, run.gold >= offers.rerollCost);
        boolean isNextRoomPressed = ui.button(SIDE_X, BUTTON_Y, SIDE_WIDTH, BUTTON_HEIGHT, GameText.NEXT_ROOM, ui.art.door, true);
        ui.fadeIn(time);
        ui.end();

        if (weaponToBuy != null) {
            ShopSystem.buyWeapon(run, offers, weaponToBuy);
        } else if (skillToBuy != null) {
            ShopSystem.buySkill(run, offers, skillToBuy);
        } else if (perkToBuy != null) {
            ShopSystem.buyPerk(run, offers, perkToBuy);
        } else if (slotToSell >= 0) {
            ShopSystem.sellSkill(run, slotToSell);
        } else if (isHealPressed) {
            ShopSystem.buyHeal(run, offers);
        } else if (isRerollPressed) {
            ShopSystem.reroll(run, offers);
        } else if (isNextRoomPressed) {
            game.enterNextRoom(run);
        }
    }

    private boolean drawOfferCard(UiCanvas ui, float x, float y, String name, String description, Color accent,
                                  TextureRegion icon, Color iconTint, int price, String statusLabel, boolean isEnabled) {
        boolean isHovered = isEnabled && ui.isHovered(x, y, CARD_WIDTH, CARD_HEIGHT);
        Color fill = isHovered ? UiCanvas.HIGHLIGHTED_PANEL_COLOR : isEnabled ? UiCanvas.PANEL_COLOR : UiCanvas.DISABLED_PANEL_COLOR;
        ui.frame(x, y, CARD_WIDTH, CARD_HEIGHT, fill, isEnabled ? accent : disabledAccentColor.set(accent).lerp(UiCanvas.INK, 0.6f));
        if (isHovered) {
            ui.corners(x, y, CARD_WIDTH, CARD_HEIGHT, UiCanvas.ACCENT_COLOR);
        }
        float top = y + CARD_HEIGHT;
        ui.iconCentered(icon, x + 26f, top - 26f, UiCanvas.TEXT_SIZE, iconTint);
        ui.text(name, x + 48f, top - 18f, UiCanvas.TEXT_SIZE, accent);
        ui.textWrapped(description, x + 14f, top - 48f, CARD_WIDTH - 28f, UiCanvas.TEXT_SIZE, isEnabled ? UiCanvas.TEXT_COLOR : UiCanvas.MUTED_TEXT_COLOR, Align.left);
        ui.icon(ui.art.coin, x + 14f, y + 10f, UiCanvas.TEXT_SIZE, Color.WHITE);
        ui.text(GameText.price(price), x + 42f, y + 27f, UiCanvas.TEXT_SIZE, run.gold >= price ? EffectSystem.GOLD_COLOR : UiCanvas.BAD_COLOR);
        if (statusLabel != null) {
            ui.textRight(statusLabel, x + CARD_WIDTH - 14f, y + 27f, UiCanvas.TEXT_SIZE, UiCanvas.MUTED_TEXT_COLOR);
        }
        return isHovered && ui.isClicked;
    }
}
