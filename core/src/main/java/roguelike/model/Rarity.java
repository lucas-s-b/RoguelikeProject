package roguelike.model;

import com.badlogic.gdx.graphics.Color;

public enum Rarity {
    COMMON(60, 45, new Color(0.82f, 0.82f, 0.82f, 1f)),
    RARE(28, 80, new Color(0.35f, 0.65f, 1f, 1f)),
    EPIC(10, 130, new Color(0.75f, 0.4f, 1f, 1f)),
    LEGENDARY(3, 210, new Color(1f, 0.72f, 0.2f, 1f));

    public final int weight;
    public final int price;
    public final Color color;

    Rarity(int weight, int price, Color color) {
        this.weight = weight;
        this.price = price;
        this.color = color;
    }
}
