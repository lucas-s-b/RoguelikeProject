package roguelike.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public final class FloatingText {
    public String text = "";
    public final Vector2 position = new Vector2();
    public final Vector2 velocity = new Vector2();
    public final Color color = new Color(Color.WHITE);
    public float scale = 1f;
    public float lifetimeRemaining;
    public float totalLifetime;
}
