package roguelike.graphics;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import roguelike.model.BodyShape;

public final class GameTextures implements Disposable {
    private static final int TEXTURE_SIZE = 128;
    private static final float PIXELS_PER_UNIT = TEXTURE_SIZE / 2f;
    private static final float SHAPE_RADIUS = 0.92f;
    private static final float CORNER_ROUNDNESS = 0.1f;
    private static final float STAR_INNER_RATIO = 0.48f;

    private interface AlphaShape {
        float alphaAt(float x, float y);
    }

    public final Texture glow;
    public final Texture circle;
    public final Texture diamond;
    public final Texture smoke;
    public final Texture triangle;
    public final Texture square;
    public final Texture pentagon;
    public final Texture hexagon;
    public final Texture star;

    public GameTextures() {
        glow = createTexture((x, y) -> {
            float falloff = Math.max(0f, 1f - (float) Math.hypot(x, y));
            return falloff * falloff;
        });
        circle = createTexture((x, y) -> ((1f - (float) Math.hypot(x, y)) * PIXELS_PER_UNIT - 1f) * 0.75f);
        diamond = createTexture((x, y) -> ((1f - Math.abs(x) - Math.abs(y)) * PIXELS_PER_UNIT - 1f) * 0.75f);
        smoke = createTexture((x, y) -> {
            float falloff = Math.max(0f, 1f - (float) Math.hypot(x, y));
            float noise = 0.5f + 0.25f * MathUtils.sin(x * 9f + MathUtils.sin(y * 6f) * 2f) + 0.25f * MathUtils.cos(y * 8f + MathUtils.cos(x * 5f) * 2f);
            return (float) Math.pow(falloff, 1.4f) * (0.55f + 0.45f * noise);
        });
        float[] triangleCorners = polygonCorners(3, 1f, 0f);
        float[] squareCorners = polygonCorners(4, 1f, 45f);
        float[] pentagonCorners = polygonCorners(5, 1f, 90f);
        float[] hexagonCorners = polygonCorners(6, 1f, 90f);
        float[] starCorners = polygonCorners(10, STAR_INNER_RATIO, 90f);
        triangle = createTexture((x, y) -> polygonAlpha(x, y, triangleCorners));
        square = createTexture((x, y) -> polygonAlpha(x, y, squareCorners));
        pentagon = createTexture((x, y) -> polygonAlpha(x, y, pentagonCorners));
        hexagon = createTexture((x, y) -> polygonAlpha(x, y, hexagonCorners));
        star = createTexture((x, y) -> polygonAlpha(x, y, starCorners));
    }

    public Texture bodyTexture(BodyShape shape) {
        return switch (shape) {
            case CIRCLE -> circle;
            case TRIANGLE -> triangle;
            case SQUARE, DIAMOND -> square;
            case PENTAGON -> pentagon;
            case HEXAGON -> hexagon;
            case STAR -> star;
        };
    }

    @Override
    public void dispose() {
        glow.dispose();
        circle.dispose();
        diamond.dispose();
        smoke.dispose();
        triangle.dispose();
        square.dispose();
        pentagon.dispose();
        hexagon.dispose();
        star.dispose();
    }

    private static float[] polygonCorners(int cornerCount, float alternateRadiusRatio, float firstCornerDegrees) {
        float[] corners = new float[cornerCount * 2];
        for (int corner = 0; corner < cornerCount; corner++) {
            double angle = Math.toRadians(firstCornerDegrees + corner * 360f / cornerCount);
            float radius = (corner % 2 == 0 ? 1f : alternateRadiusRatio) * (SHAPE_RADIUS - CORNER_ROUNDNESS);
            corners[corner * 2] = (float) Math.cos(angle) * radius;
            corners[corner * 2 + 1] = (float) Math.sin(angle) * radius;
        }
        return corners;
    }

    private static float polygonAlpha(float x, float y, float[] corners) {
        float nearestDistance = Float.MAX_VALUE;
        boolean isInside = false;
        int cornerCount = corners.length / 2;
        for (int corner = 0, previous = cornerCount - 1; corner < cornerCount; previous = corner++) {
            float startX = corners[previous * 2];
            float startY = corners[previous * 2 + 1];
            float edgeX = corners[corner * 2] - startX;
            float edgeY = corners[corner * 2 + 1] - startY;
            float along = MathUtils.clamp(((x - startX) * edgeX + (y - startY) * edgeY) / (edgeX * edgeX + edgeY * edgeY), 0f, 1f);
            nearestDistance = Math.min(nearestDistance, (float) Math.hypot(x - startX - edgeX * along, y - startY - edgeY * along));
            boolean crossesRow = (startY > y) != (startY + edgeY > y);
            if (crossesRow && x < startX + (y - startY) / edgeY * edgeX) {
                isInside = !isInside;
            }
        }
        float signedDistance = isInside ? -nearestDistance : nearestDistance;
        return ((CORNER_ROUNDNESS - signedDistance) * PIXELS_PER_UNIT - 1f) * 0.75f;
    }

    private static Texture createTexture(AlphaShape shape) {
        Pixmap pixmap = new Pixmap(TEXTURE_SIZE, TEXTURE_SIZE, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        for (int pixelY = 0; pixelY < TEXTURE_SIZE; pixelY++) {
            for (int pixelX = 0; pixelX < TEXTURE_SIZE; pixelX++) {
                float x = (pixelX + 0.5f) / PIXELS_PER_UNIT - 1f;
                float y = 1f - (pixelY + 0.5f) / PIXELS_PER_UNIT;
                float alpha = MathUtils.clamp(shape.alphaAt(x, y), 0f, 1f);
                pixmap.drawPixel(pixelX, pixelY, 0xFFFFFF00 | Math.round(alpha * 255f));
            }
        }
        Texture texture = new Texture(pixmap, true);
        texture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }
}
