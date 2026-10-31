package roguelike.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;
import roguelike.RoguelikeGame;
import roguelike.content.GameText;
import roguelike.model.RunState;
import roguelike.ui.UiCanvas;

public final class GameOverScreen extends ScreenAdapter {
    private static final Color TITLE_COLOR = new Color(0.9f, 0.25f, 0.3f, 1f);
    private static final Color BACKDROP_TINT = new Color(1f, 0.55f, 0.55f, 1f);
    private static final float STAT_REVEAL_DELAY = 0.35f;
    private static final float STAT_REVEAL_INTERVAL = 0.2f;

    private final RoguelikeGame game;
    private final RunState run;
    private float time;

    public GameOverScreen(RoguelikeGame game, RunState run) {
        this.game = game;
        this.run = run;
    }

    @Override
    public void render(float delta) {
        time += delta;
        ScreenUtils.clear(UiCanvas.INK);
        game.viewport.apply();
        UiCanvas ui = game.ui;
        float centerX = RoguelikeGame.WORLD_WIDTH / 2f;
        ui.begin();
        ui.backdrop(BACKDROP_TINT);
        float titleHalfWidth = ui.textWidth(GameText.YOU_DIED, UiCanvas.TITLE_SIZE) / 2f;
        ui.title(GameText.YOU_DIED, centerX, 604f, UiCanvas.TITLE_SIZE, TITLE_COLOR);
        ui.iconCentered(ui.art.skull, centerX - titleHalfWidth - 70f, 576f, UiCanvas.HEADING_SIZE, Color.WHITE);
        ui.iconCentered(ui.art.skull, centerX + titleHalfWidth + 70f, 576f, UiCanvas.HEADING_SIZE, Color.WHITE);

        ui.frame(centerX - 230f, 300f, 460f, 172f, UiCanvas.PANEL_COLOR, null);
        TextureRegion[] statIcons = {ui.art.door, ui.art.skull, ui.art.star};
        String[] statLines = {
                GameText.roomsCleared(run.roomsCleared),
                GameText.enemiesDefeated(run.enemiesKilled),
                GameText.perksCollected(run.player.perks.size())
        };
        for (int index = 0; index < statLines.length; index++) {
            if (time < STAT_REVEAL_DELAY + index * STAT_REVEAL_INTERVAL) {
                continue;
            }
            float rowTop = 446f - index * 48f;
            ui.iconCentered(statIcons[index], centerX - 184f, rowTop - 7f, UiCanvas.TEXT_SIZE, Color.WHITE);
            ui.text(statLines[index], centerX - 150f, rowTop, UiCanvas.TEXT_SIZE, UiCanvas.TEXT_COLOR);
        }
        boolean isBackPressed = ui.button(centerX - 150f, 196f, 300f, 60f, GameText.BACK_TO_MENU, null, true);
        ui.fadeIn(time);
        ui.end();
        if (isBackPressed) {
            game.showMenu();
        }
    }
}
