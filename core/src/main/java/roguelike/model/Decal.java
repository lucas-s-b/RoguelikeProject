package roguelike.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public final class Decal {
    public final Vector2 position = new Vector2();
    public final Color color = new Color(Color.WHITE);
    public float width;
    public float height;
    public float rotation;
    public boolean isSoft;
    public float lifetimeRemaining;
    public float totalLifetime;
}
