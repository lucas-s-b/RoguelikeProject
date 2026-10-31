package roguelike;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import roguelike.content.Content;
import roguelike.content.SkillContent;
import roguelike.content.WeaponContent;
import roguelike.graphics.GameTextures;
import roguelike.graphics.PostProcessor;
import roguelike.model.RunState;
import roguelike.screens.DungeonScreen;
import roguelike.screens.GameOverScreen;
import roguelike.screens.MenuScreen;
import roguelike.screens.PerkRewardScreen;
import roguelike.screens.ShopScreen;
import roguelike.systems.RoomSystem;
import roguelike.systems.RunSystem;
import roguelike.ui.PixelArt;
import roguelike.ui.PixelFont;
import roguelike.ui.UiCanvas;

public final class RoguelikeGame extends Game {
    public static final float WORLD_WIDTH = 1280f;
    public static final float WORLD_HEIGHT = 720f;

    public SpriteBatch batch;
    public ShapeRenderer shapes;
    public BitmapFont font;
    public FitViewport viewport;
    public PixelArt art;
    public UiCanvas ui;
    public GameTextures textures;
    public PostProcessor postProcessor;

    @Override
    public void create() {
        Content.loadAll();
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        font = PixelFont.create();
        art = new PixelArt();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT);
        ui = new UiCanvas(batch, font, viewport, art);
        Gdx.graphics.setCursor(art.arrowCursor);
        textures = new GameTextures();
        postProcessor = new PostProcessor();
        setScreen(new MenuScreen(this));
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        super.resize(width, height);
    }

    @Override
    public void dispose() {
        super.dispose();
        batch.dispose();
        shapes.dispose();
        font.dispose();
        art.dispose();
        textures.dispose();
        postProcessor.dispose();
    }

    public void showMenu() {
        setScreen(new MenuScreen(this));
    }

    public void startRun() {
        RunState run = RunSystem.createRun(WeaponContent.RUSTY_SWORD, SkillContent.FLURRY);
        setScreen(new DungeonScreen(this, run));
    }

    public void showRoomRewards(RunState run) {
        setScreen(new PerkRewardScreen(this, run));
    }

    public void continueAfterRewards(RunState run) {
        if (RoomSystem.isShopAfterRoom(run.roomNumber)) {
            setScreen(new ShopScreen(this, run));
        } else {
            enterNextRoom(run);
        }
    }

    public void enterNextRoom(RunState run) {
        run.roomNumber++;
        setScreen(new DungeonScreen(this, run));
    }

    public void showGameOver(RunState run) {
        setScreen(new GameOverScreen(this, run));
    }
}
