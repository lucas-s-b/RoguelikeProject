package roguelike.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public final class Particle {
    public enum Style {
        GLOW,
        SPARK,
        SMOKE,
        SHARD,
        GHOST
    }

    public Style style = Style.GLOW;
    public final Vector2 position = new Vector2();
    public final Vector2 velocity = new Vector2();
    public final Color color = new Color(Color.WHITE);
    public final Color endColor = new Color(1f, 1f, 1f, 0f);
    public float size = 6f;
    public float endSizeScale = 0.4f;
    public float drag = 4f;
    public float gravity;
    public float rotation;
    public float spin;
    public float fadeInSeconds;
    public float lifetimeRemaining;
    public float totalLifetime;
}
