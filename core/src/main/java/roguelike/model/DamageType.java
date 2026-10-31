package roguelike.model;

import com.badlogic.gdx.graphics.Color;

public enum DamageType {
    PHYSICAL(new Color(0.95f, 0.95f, 0.95f, 1f)),
    MAGIC(new Color(0.78f, 0.5f, 1f, 1f)),
    FIRE(new Color(1f, 0.55f, 0.15f, 1f)),
    ICE(new Color(0.55f, 0.88f, 1f, 1f)),
    POISON(new Color(0.5f, 0.95f, 0.3f, 1f)),
    LIGHTNING(new Color(1f, 0.95f, 0.4f, 1f)),
    BLEED(new Color(0.9f, 0.15f, 0.2f, 1f));

    public final Color color;

    DamageType(Color color) {
        this.color = color;
    }
}
