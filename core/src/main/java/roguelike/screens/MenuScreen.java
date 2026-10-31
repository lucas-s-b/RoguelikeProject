package roguelike.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import roguelike.RoguelikeGame;
import roguelike.content.GameText;
import roguelike.model.DamageType;
import roguelike.ui.UiCanvas;

public final class MenuScreen extends ScreenAdapter {
    private static final float EMBLEM_SPACING = 72f;

    private final RoguelikeGame game;
    private float time;

    public MenuScreen(RoguelikeGame game) {
        this.game = game;
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
        float titleBob = UiCanvas.snap(MathUtils.sin(time * 2f) * 4f);
        ui.title(GameText.GAME_TITLE, centerX, 604f + titleBob, UiCanvas.TITLE_SIZE, UiCanvas.ACCENT_COLOR);
        DamageType[] damageTypes = DamageType.values();
        for (int index = 0; index < damageTypes.length; index++) {
            float emblemX = centerX + (index - (damageTypes.length - 1) / 2f) * EMBLEM_SPACING;
            float emblemBob = UiCanvas.snap(MathUtils.sin(time * 3f - index * 0.8f) * 3f);
            ui.iconCentered(ui.art.emblem(damageTypes[index]), emblemX, 488f + emblemBob, UiCanvas.HEADING_SIZE, Color.WHITE);
        }
        ui.textCentered(GameText.MENU_SUBTITLE, centerX, 442f, UiCanvas.TEXT_SIZE, UiCanvas.MUTED_TEXT_COLOR);
        boolean isStartPressed = ui.button(centerX - 150f, 320f, 300f, 60f, GameText.START_RUN, null, true);
        boolean isQuitPressed = ui.button(centerX - 150f, 240f, 300f, 60f, GameText.QUIT, null, true);
        ui.frame(centerX - 430f, 64f, 860f, 84f, UiCanvas.PANEL_DARK_COLOR, null);
        ui.textWrapped(GameText.CONTROLS, centerX - 410f, 126f, 820f, UiCanvas.TEXT_SIZE, UiCanvas.MUTED_TEXT_COLOR, Align.center);
        ui.fadeIn(time);
        ui.end();
        if (isStartPressed) {
            game.startRun();
        } else if (isQuitPressed) {
            Gdx.app.exit();
        }
    }
}
