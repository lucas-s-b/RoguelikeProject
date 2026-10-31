package roguelike.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import roguelike.RoguelikeGame;
import roguelike.content.GameText;

public final class DesktopLauncher {
    private DesktopLauncher() {
    }

    public static void main(String[] arguments) {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle(GameText.GAME_TITLE);
        configuration.setWindowedMode((int) RoguelikeGame.WORLD_WIDTH, (int) RoguelikeGame.WORLD_HEIGHT);
        configuration.useVsync(true);
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
        new Lwjgl3Application(new RoguelikeGame(), configuration);
    }
}
