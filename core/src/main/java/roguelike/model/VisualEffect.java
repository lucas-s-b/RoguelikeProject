package roguelike.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public final class VisualEffect {
    public enum Shape {
        CIRCLE,
        RING,
        ARC,
        LINE,
        LIGHTNING,
        POP
    }

    public Shape shape = Shape.CIRCLE;
    public final Vector2 position = new Vector2();
    public final Vector2 endPosition = new Vector2();
    public float radius;
    public float angle;
    public float arcDegrees;
    public float thickness = 3f;
    public final Color color = new Color(Color.WHITE);
    public float lifetimeRemaining;
    public float totalLifetime;
    public boolean growsOverTime;
    public boolean isTelegraph;
    public float sweepSide = 1f;
    public float seed;
    public BodyShape bodyShape = BodyShape.CIRCLE;
}
