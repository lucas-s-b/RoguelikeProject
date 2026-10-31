package roguelike.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

public final class Draw {
    private static final Color nearFirstColor = new Color();
    private static final Color farFirstColor = new Color();
    private static final Color nearSecondColor = new Color();
    private static final Color farSecondColor = new Color();

    private Draw() {
    }

    public static void sprite(SpriteBatch batch, Texture texture, float centerX, float centerY, float width, float height,
                              float rotationDegrees, Color color, float alpha) {
        batch.setColor(color.r, color.g, color.b, color.a * alpha);
        batch.draw(texture, centerX - width / 2f, centerY - height / 2f, width / 2f, height / 2f, width, height, 1f, 1f, rotationDegrees,
                0, 0, texture.getWidth(), texture.getHeight(), false, false);
    }

    public static void band(ShapeRenderer shapes, float x, float y, float innerRadius, float middleRadius, float outerRadius,
                            float startAngle, float sweepDegrees, Color color, float innerAlpha, float middleAlpha, float outerAlpha, float tailFade) {
        int segments = Math.max(6, Math.round(Math.abs(sweepDegrees) / 360f * 48f));
        for (int segment = 0; segment < segments; segment++) {
            float firstProgress = (float) segment / segments;
            float secondProgress = (float) (segment + 1) / segments;
            float firstAngle = startAngle + sweepDegrees * firstProgress;
            float secondAngle = startAngle + sweepDegrees * secondProgress;
            float firstTail = 1f - tailFade * (1f - firstProgress);
            float secondTail = 1f - tailFade * (1f - secondProgress);
            float firstCos = MathUtils.cosDeg(firstAngle);
            float firstSin = MathUtils.sinDeg(firstAngle);
            float secondCos = MathUtils.cosDeg(secondAngle);
            float secondSin = MathUtils.sinDeg(secondAngle);
            for (int row = 0; row < 2; row++) {
                float nearRadius = row == 0 ? innerRadius : middleRadius;
                float farRadius = row == 0 ? middleRadius : outerRadius;
                float nearAlpha = row == 0 ? innerAlpha : middleAlpha;
                float farAlpha = row == 0 ? middleAlpha : outerAlpha;
                nearFirstColor.set(color.r, color.g, color.b, nearAlpha * firstTail);
                farFirstColor.set(color.r, color.g, color.b, farAlpha * firstTail);
                nearSecondColor.set(color.r, color.g, color.b, nearAlpha * secondTail);
                farSecondColor.set(color.r, color.g, color.b, farAlpha * secondTail);
                float nearFirstX = x + firstCos * nearRadius;
                float nearFirstY = y + firstSin * nearRadius;
                float farFirstX = x + firstCos * farRadius;
                float farFirstY = y + firstSin * farRadius;
                float nearSecondX = x + secondCos * nearRadius;
                float nearSecondY = y + secondSin * nearRadius;
                float farSecondX = x + secondCos * farRadius;
                float farSecondY = y + secondSin * farRadius;
                shapes.triangle(nearFirstX, nearFirstY, farFirstX, farFirstY, farSecondX, farSecondY, nearFirstColor, farFirstColor, farSecondColor);
                shapes.triangle(nearFirstX, nearFirstY, farSecondX, farSecondY, nearSecondX, nearSecondY, nearFirstColor, farSecondColor, nearSecondColor);
            }
        }
    }

    public static void softLine(ShapeRenderer shapes, float startX, float startY, float endX, float endY, float width, Color color, float alpha) {
        float length = (float) Math.hypot(endX - startX, endY - startY);
        if (length < 0.001f) {
            return;
        }
        float sideX = -(endY - startY) / length * width / 2f;
        float sideY = (endX - startX) / length * width / 2f;
        nearFirstColor.set(color.r, color.g, color.b, alpha);
        farFirstColor.set(color.r, color.g, color.b, 0f);
        for (int direction = -1; direction <= 1; direction += 2) {
            float edgeStartX = startX + sideX * direction;
            float edgeStartY = startY + sideY * direction;
            float edgeEndX = endX + sideX * direction;
            float edgeEndY = endY + sideY * direction;
            shapes.triangle(startX, startY, endX, endY, edgeEndX, edgeEndY, nearFirstColor, nearFirstColor, farFirstColor);
            shapes.triangle(startX, startY, edgeEndX, edgeEndY, edgeStartX, edgeStartY, nearFirstColor, farFirstColor, farFirstColor);
        }
    }
}
