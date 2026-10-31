package roguelike.model;

public enum BodyShape {
    CIRCLE(1f, 1f, 0f),
    TRIANGLE(1.3f, 0.5f, 0f),
    SQUARE(1.12f, 0.72f, 0f),
    DIAMOND(1.15f, 0.72f, 45f),
    PENTAGON(1.08f, 0.8f, 0f),
    HEXAGON(1.06f, 0.86f, 0f),
    STAR(1.3f, 0.42f, 0f);

    public final float visualScale;
    public final float innerRadiusFactor;
    public final float angleOffset;

    BodyShape(float visualScale, float innerRadiusFactor, float angleOffset) {
        this.visualScale = visualScale;
        this.innerRadiusFactor = innerRadiusFactor;
        this.angleOffset = angleOffset;
    }
}
