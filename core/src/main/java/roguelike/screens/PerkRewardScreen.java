package roguelike.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import java.util.List;
import roguelike.RoguelikeGame;
import roguelike.content.GameText;
import roguelike.definitions.PerkDefinition;
import roguelike.model.PerkFamily;
import roguelike.model.Rarity;
import roguelike.model.RunState;
import roguelike.systems.EffectSystem;
import roguelike.systems.PerkSystem;
import roguelike.systems.RoomSystem;
import roguelike.ui.UiCanvas;

public final class PerkRewardScreen extends ScreenAdapter {
    private static final int CHOICE_COUNT = 3;
    private static final float CARD_WIDTH = 340f;
    private static final float CARD_HEIGHT = 350f;
    private static final float CARD_GAP = 36f;
    private static final float CARD_Y = 150f;
    private static final float CARD_LIFT = 8f;
    private static final float BAND_HEIGHT = 34f;
    private static final float ENTRANCE_SECONDS = 0.45f;
    private static final float ENTRANCE_STAGGER = 0.12f;
    private static final float ENTRANCE_DISTANCE = 560f;
    private static final float INPUT_DELAY_SECONDS = 0.6f;

    private final RoguelikeGame game;
    private final RunState run;
    private final boolean wasBossRoom;
    private final List<PerkDefinition> choices;
    private final Color bandColor = new Color();
    private float time;

    public PerkRewardScreen(RoguelikeGame game, RunState run) {
        this.game = game;
        this.run = run;
        this.wasBossRoom = RoomSystem.isBossRoom(run.roomNumber);
        this.choices = PerkSystem.rollChoices(run.player, CHOICE_COUNT, wasBossRoom ? Rarity.RARE : Rarity.COMMON);
    }

    @Override
    public void render(float delta) {
        time += delta;
        ScreenUtils.clear(UiCanvas.INK);
        game.viewport.apply();
        UiCanvas ui = game.ui;
        float centerX = RoguelikeGame.WORLD_WIDTH / 2f;
        ui.begin();
        ui.backdrop(Color.WHITE);
        ui.title(wasBossRoom ? GameText.CHOOSE_RARE_PERK : GameText.CHOOSE_PERK, centerX, 676f, UiCanvas.HEADING_SIZE, UiCanvas.TEXT_COLOR);
        String goldText = GameText.roomClearedGold(RoomSystem.roomClearGold(run.roomNumber));
        float goldGroupX = UiCanvas.snap(centerX - (ui.textWidth(goldText, UiCanvas.TEXT_SIZE) + 28f) / 2f);
        ui.icon(ui.art.coin, goldGroupX, 596f, UiCanvas.TEXT_SIZE, Color.WHITE);
        ui.text(goldText, goldGroupX + 28f, 613f, UiCanvas.TEXT_SIZE, EffectSystem.GOLD_COLOR);

        PerkDefinition picked = null;
        float totalWidth = choices.size() * CARD_WIDTH + (choices.size() - 1) * CARD_GAP;
        float startX = UiCanvas.snap(centerX - totalWidth / 2f);
        for (int index = 0; index < choices.size(); index++) {
            PerkDefinition perk = choices.get(index);
            Color rarityColor = perk.rarity.color;
            float cardX = startX + index * (CARD_WIDTH + CARD_GAP);
            float cardCenterX = cardX + CARD_WIDTH / 2f;
            float entrance = Interpolation.pow3Out.apply(MathUtils.clamp((time - index * ENTRANCE_STAGGER) / ENTRANCE_SECONDS, 0f, 1f));
            boolean isHovered = time > INPUT_DELAY_SECONDS && ui.isHovered(cardX, CARD_Y, CARD_WIDTH, CARD_HEIGHT);
            float cardY = CARD_Y - UiCanvas.snap((1f - entrance) * ENTRANCE_DISTANCE) + (isHovered ? CARD_LIFT : 0f);
            float cardTop = cardY + CARD_HEIGHT;

            ui.frame(cardX, cardY, CARD_WIDTH, CARD_HEIGHT, isHovered ? UiCanvas.HIGHLIGHTED_PANEL_COLOR : UiCanvas.PANEL_COLOR, rarityColor);
            if (isHovered) {
                ui.corners(cardX, cardY, CARD_WIDTH, CARD_HEIGHT, rarityColor);
            }
            ui.rect(cardX + 4f, cardTop - 4f - BAND_HEIGHT, CARD_WIDTH - 8f, BAND_HEIGHT, bandColor.set(rarityColor).lerp(UiCanvas.INK, 0.55f));
            ui.rect(cardX + 4f, cardTop - 6f - BAND_HEIGHT, CARD_WIDTH - 8f, 2f, UiCanvas.INK);
            ui.textCentered(GameText.rarityName(perk.rarity), cardCenterX, cardTop - 14f, UiCanvas.TEXT_SIZE, UiCanvas.TEXT_COLOR);
            Color iconTint = perk.family == PerkFamily.GENERAL ? rarityColor : Color.WHITE;
            ui.iconCentered(ui.art.familyIcon(perk.family), cardCenterX, cardTop - 84f, UiCanvas.HEADING_SIZE, iconTint);

            float nameTop = cardTop - 124f;
            float textWidth = CARD_WIDTH - 40f;
            ui.textWrapped(perk.name, cardX + 20f, nameTop, textWidth, UiCanvas.HEADING_SIZE, rarityColor, Align.center);
            float descriptionTop = nameTop - ui.textHeight(perk.name, textWidth, UiCanvas.HEADING_SIZE) - 26f;
            ui.textWrapped(perk.description, cardX + 24f, descriptionTop, CARD_WIDTH - 48f, UiCanvas.TEXT_SIZE, UiCanvas.TEXT_COLOR, Align.center);
            int owned = PerkSystem.stacksOf(run.player, perk.id);
            ui.textCentered(GameText.owned(owned, perk.maxStacks), cardCenterX, cardY + 34f, UiCanvas.TEXT_SIZE, UiCanvas.MUTED_TEXT_COLOR);
            if (isHovered && ui.isClicked) {
                picked = perk;
            }
        }

        boolean isContinuePressed = choices.isEmpty() && ui.button(centerX - 130f, 300f, 260f, 60f, GameText.CONTINUE, null, true);
        ui.fadeIn(time);
        ui.end();

        if (picked != null) {
            PerkSystem.addPerk(run, run.player, picked);
            game.continueAfterRewards(run);
        } else if (isContinuePressed) {
            game.continueAfterRewards(run);
        }
    }
}
