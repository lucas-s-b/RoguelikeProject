package roguelike.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import java.util.Arrays;
import roguelike.model.FloatingText;
import roguelike.model.GameWorld;
import roguelike.model.VisualEffect;

public final class EffectRenderer {
    private static final Color TEXT_OUTLINE_COLOR = new Color(0.07f, 0.055f, 0.094f, 0.85f);
    private static final float TEXT_PIXELS_PER_SCALE = 2.2f;
    private static final float FLOATING_TEXT_WIDTH = 160f;
    private static final float SLASH_BODY_THICKNESS = 0.42f;
    private static final float SLASH_EDGE_THICKNESS = 0.1f;
    private static final float LIGHTNING_SEGMENT_LENGTH = 18f;
    private static final float LIGHTNING_JITTER = 11f;
    private static final float LIGHTNING_FLICKER_SECONDS = 0.035f;
    private static final float MINIMUM_RIPPLE_RADIUS = 70f;
    private static final Color drawColor = new Color();
    private static final Color transparentColor = new Color();
    private static final Color firstEdgeColor = new Color();
    private static final Color secondEdgeColor = new Color();

    private EffectRenderer() {
    }

    public static void drawTelegraphs(GameWorld world, ShapeRenderer shapes) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (VisualEffect effect : world.effects) {
            if (!effect.isTelegraph) {
                continue;
            }
            float progress = 1f - effect.lifetimeRemaining / effect.totalLifetime;
            float urgency = progress > 0.75f && MathUtils.sin(world.time * 45f) > 0f ? 1.5f : 1f;
            float x = effect.position.x;
            float y = effect.position.y;
            float radius = effect.radius;
            if (effect.shape == VisualEffect.Shape.CIRCLE) {
                float fillRadius = radius * progress;
                Draw.band(shapes, x, y, 0f, radius * 0.9f, radius, 0f, 360f, effect.color, 0.05f, 0.1f, 0.25f, 0f);
                Draw.band(shapes, x, y, 0f, fillRadius * 0.85f, fillRadius, 0f, 360f, effect.color, 0.1f * urgency, 0.18f * urgency, 0.35f * urgency, 0f);
                Draw.band(shapes, x, y, radius - 3f, radius - 1.5f, radius, 0f, 360f, effect.color, 0f, 0.8f * urgency, 0.3f, 0f);
            } else if (effect.shape == VisualEffect.Shape.LINE) {
                float fillEndX = MathUtils.lerp(x, effect.endPosition.x, progress);
                float fillEndY = MathUtils.lerp(y, effect.endPosition.y, progress);
                Draw.softLine(shapes, x, y, effect.endPosition.x, effect.endPosition.y, effect.thickness, effect.color, 0.2f);
                Draw.softLine(shapes, x, y, fillEndX, fillEndY, effect.thickness * 0.8f, effect.color, 0.4f * urgency);
            } else if (effect.shape == VisualEffect.Shape.RING) {
                float ringRadius = radius * (1.7f - 0.7f * Interpolation.pow2Out.apply(progress));
                for (int dash = 0; dash < 6; dash++) {
                    float dashAngle = world.time * 150f + dash * 60f;
                    Draw.band(shapes, x, y, ringRadius - 3f, ringRadius - 1.5f, ringRadius, dashAngle, 34f, effect.color, 0f, 0.85f, 0.25f, 0f);
                }
                Draw.band(shapes, x, y, 0f, radius * progress * 0.6f, radius * progress, 0f, 360f, effect.color, 0.35f * progress, 0.2f * progress, 0f, 0f);
            }
        }
        shapes.end();
    }

    public static void drawEffects(GameWorld world, ShapeRenderer shapes, SpriteBatch batch, GameTextures textures) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (VisualEffect effect : world.effects) {
            if (effect.isTelegraph) {
                continue;
            }
            float fade = effect.lifetimeRemaining / effect.totalLifetime;
            float progress = 1f - fade;
            float x = effect.position.x;
            float y = effect.position.y;
            if (effect.shape == VisualEffect.Shape.CIRCLE) {
                float blastRadius = effect.radius * (0.6f + 0.4f * Interpolation.pow3Out.apply(progress));
                float coreFade = Math.max(0f, 1f - progress * 2.5f);
                Draw.band(shapes, x, y, 0f, blastRadius * 0.85f, blastRadius, 0f, 360f, effect.color, 0.08f * fade, 0.35f * fade, 0f, 0f);
                if (coreFade > 0f) {
                    Draw.band(shapes, x, y, 0f, blastRadius * 0.35f, blastRadius * 0.7f, 0f, 360f, Color.WHITE, 0.55f * coreFade, 0.25f * coreFade, 0f, 0f);
                }
            } else if (effect.shape == VisualEffect.Shape.ARC) {
                drawSlash(shapes, effect, SLASH_BODY_THICKNESS, effect.color, 0.6f);
            }
        }
        shapes.end();

        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (VisualEffect effect : world.effects) {
            if (effect.isTelegraph) {
                continue;
            }
            float fade = effect.lifetimeRemaining / effect.totalLifetime;
            float x = effect.position.x;
            float y = effect.position.y;
            if (effect.shape == VisualEffect.Shape.RING) {
                float radius = currentRingRadius(effect);
                float halfThickness = effect.thickness * (effect.growsOverTime ? fade : 1f) + 1.5f;
                Draw.band(shapes, x, y, Math.max(0f, radius - halfThickness), radius, radius + halfThickness, 0f, 360f, effect.color, 0f, fade * 0.8f, 0f, 0f);
            } else if (effect.shape == VisualEffect.Shape.ARC) {
                drawSlash(shapes, effect, SLASH_EDGE_THICKNESS, Color.WHITE, 0.85f);
            } else if (effect.shape == VisualEffect.Shape.LINE) {
                float flicker = 1f + 0.12f * MathUtils.sin(world.time * 70f);
                Draw.softLine(shapes, x, y, effect.endPosition.x, effect.endPosition.y, effect.thickness * 2.4f * flicker, effect.color, fade * 0.5f);
                Draw.softLine(shapes, x, y, effect.endPosition.x, effect.endPosition.y, Math.max(2f, effect.thickness * 0.45f), Color.WHITE, fade * 0.9f);
            } else if (effect.shape == VisualEffect.Shape.LIGHTNING) {
                int flickerFrame = (int) (world.time / LIGHTNING_FLICKER_SECONDS) % 1000;
                drawJaggedLine(shapes, x, y, effect.endPosition.x, effect.endPosition.y, effect.seed + flickerFrame * 7.31f, effect.thickness, effect.color, fade);
            }
        }
        shapes.end();

        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.begin();
        for (VisualEffect effect : world.effects) {
            if (effect.shape != VisualEffect.Shape.POP) {
                continue;
            }
            float progress = 1f - effect.lifetimeRemaining / effect.totalLifetime;
            float diameter = effect.radius * 2f * effect.bodyShape.visualScale * (1f + 0.6f * Interpolation.pow2Out.apply(progress));
            float angle = effect.angle + effect.bodyShape.angleOffset;
            drawColor.set(effect.color).lerp(Color.WHITE, Math.min(1f, progress * 3f));
            Draw.sprite(batch, textures.bodyTexture(effect.bodyShape), effect.position.x, effect.position.y, diameter, diameter, angle, drawColor, 1f - progress);
        }
        batch.end();
    }

    public static void drawFloatingTexts(GameWorld world, SpriteBatch batch, BitmapFont font) {
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.begin();
        for (FloatingText floatingText : world.floatingTexts) {
            float age = floatingText.totalLifetime - floatingText.lifetimeRemaining;
            float punch = Math.max(0f, 1f - age / 0.15f);
            float fade = Math.min(1f, floatingText.lifetimeRemaining / (floatingText.totalLifetime * 0.4f));
            float pixelSize = Math.max(1f, Math.round(floatingText.scale * TEXT_PIXELS_PER_SCALE * (1f + 0.7f * punch * punch)));
            float textX = floatingText.position.x - FLOATING_TEXT_WIDTH / 2f;
            float textY = floatingText.position.y;
            font.getData().setScale(pixelSize);
            font.setColor(drawColor.set(TEXT_OUTLINE_COLOR).mul(1f, 1f, 1f, fade));
            font.draw(batch, floatingText.text, textX - pixelSize, textY, FLOATING_TEXT_WIDTH, Align.center, false);
            font.draw(batch, floatingText.text, textX + pixelSize, textY, FLOATING_TEXT_WIDTH, Align.center, false);
            font.draw(batch, floatingText.text, textX, textY - pixelSize, FLOATING_TEXT_WIDTH, Align.center, false);
            font.draw(batch, floatingText.text, textX, textY + pixelSize, FLOATING_TEXT_WIDTH, Align.center, false);
            font.setColor(drawColor.set(floatingText.color).mul(1f, 1f, 1f, fade));
            font.draw(batch, floatingText.text, textX, textY, FLOATING_TEXT_WIDTH, Align.center, false);
        }
        font.getData().setScale(1f);
        font.setColor(Color.WHITE);
        batch.end();
        batch.setColor(Color.WHITE);
    }

    public static void collectRipples(GameWorld world, float[] ripples) {
        Arrays.fill(ripples, 0f);
        int offset = 0;
        for (int index = world.effects.size() - 1; index >= 0 && offset < ripples.length; index--) {
            VisualEffect effect = world.effects.get(index);
            if (!effect.growsOverTime || effect.radius < MINIMUM_RIPPLE_RADIUS) {
                continue;
            }
            ripples[offset] = effect.position.x;
            ripples[offset + 1] = effect.position.y;
            ripples[offset + 2] = currentRingRadius(effect);
            ripples[offset + 3] = effect.lifetimeRemaining / effect.totalLifetime * Math.min(1f, effect.radius / 200f);
            offset += 4;
        }
    }

    private static float currentRingRadius(VisualEffect effect) {
        float progress = 1f - effect.lifetimeRemaining / effect.totalLifetime;
        if (effect.growsOverTime) {
            return effect.radius * Interpolation.pow2Out.apply(0.25f, 1f, progress);
        }
        return effect.radius * Interpolation.swingOut.apply(Math.min(1f, progress * 4f));
    }

    private static void drawSlash(ShapeRenderer shapes, VisualEffect effect, float thicknessFraction, Color color, float alpha) {
        float progress = 1f - effect.lifetimeRemaining / effect.totalLifetime;
        float headProgress = Interpolation.pow3Out.apply(Math.min(1f, progress / 0.55f));
        float tailProgress = Interpolation.pow2In.apply(progress);
        float startAngle = effect.angle - effect.arcDegrees / 2f * effect.sweepSide;
        float sweepDegrees = effect.arcDegrees * effect.sweepSide;
        float outerRadius = effect.radius + 3f;
        float maximumThickness = effect.radius * thicknessFraction;
        int segments = Math.max(6, Math.round(effect.arcDegrees / 8f));
        float centerX = effect.position.x;
        float centerY = effect.position.y;
        transparentColor.set(color.r, color.g, color.b, 0f);
        for (int segment = 0; segment < segments; segment++) {
            float firstPosition = (float) segment / segments;
            float secondPosition = (float) (segment + 1) / segments;
            float firstAngle = startAngle + sweepDegrees * MathUtils.lerp(tailProgress, headProgress, firstPosition);
            float secondAngle = startAngle + sweepDegrees * MathUtils.lerp(tailProgress, headProgress, secondPosition);
            float firstHeadTaper = Math.min(1f, (1f - firstPosition) * 6f + 0.2f);
            float secondHeadTaper = Math.min(1f, (1f - secondPosition) * 6f + 0.2f);
            float firstInnerRadius = outerRadius - maximumThickness * (float) Math.sqrt(firstPosition) * firstHeadTaper;
            float secondInnerRadius = outerRadius - maximumThickness * (float) Math.sqrt(secondPosition) * secondHeadTaper;
            float firstCos = MathUtils.cosDeg(firstAngle);
            float firstSin = MathUtils.sinDeg(firstAngle);
            float secondCos = MathUtils.cosDeg(secondAngle);
            float secondSin = MathUtils.sinDeg(secondAngle);
            firstEdgeColor.set(color.r, color.g, color.b, alpha * firstPosition);
            secondEdgeColor.set(color.r, color.g, color.b, alpha * secondPosition);
            float firstInnerX = centerX + firstCos * firstInnerRadius;
            float firstInnerY = centerY + firstSin * firstInnerRadius;
            float firstOuterX = centerX + firstCos * outerRadius;
            float firstOuterY = centerY + firstSin * outerRadius;
            float secondInnerX = centerX + secondCos * secondInnerRadius;
            float secondInnerY = centerY + secondSin * secondInnerRadius;
            float secondOuterX = centerX + secondCos * outerRadius;
            float secondOuterY = centerY + secondSin * outerRadius;
            shapes.triangle(firstInnerX, firstInnerY, firstOuterX, firstOuterY, secondOuterX, secondOuterY, transparentColor, firstEdgeColor, secondEdgeColor);
            shapes.triangle(firstInnerX, firstInnerY, secondOuterX, secondOuterY, secondInnerX, secondInnerY, transparentColor, secondEdgeColor, transparentColor);
        }
    }

    private static void drawJaggedLine(ShapeRenderer shapes, float startX, float startY, float endX, float endY,
                                       float seed, float thickness, Color color, float alpha) {
        float length = (float) Math.hypot(endX - startX, endY - startY);
        if (length < 1f) {
            return;
        }
        float perpendicularX = -(endY - startY) / length;
        float perpendicularY = (endX - startX) / length;
        int segmentCount = Math.max(2, Math.round(length / LIGHTNING_SEGMENT_LENGTH));
        float previousX = startX;
        float previousY = startY;
        for (int segment = 1; segment <= segmentCount; segment++) {
            float progress = (float) segment / segmentCount;
            double noise = Math.sin(seed * 12.9898 + segment * 78.233) * 43758.5453;
            float jitter = segment == segmentCount ? 0f : (float) (noise - Math.floor(noise) - 0.5) * 2f * LIGHTNING_JITTER;
            float pointX = MathUtils.lerp(startX, endX, progress) + perpendicularX * jitter;
            float pointY = MathUtils.lerp(startY, endY, progress) + perpendicularY * jitter;
            Draw.softLine(shapes, previousX, previousY, pointX, pointY, thickness * 2.6f, color, alpha * 0.5f);
            Draw.softLine(shapes, previousX, previousY, pointX, pointY, Math.max(1.5f, thickness * 0.5f), Color.WHITE, alpha * 0.95f);
            previousX = pointX;
            previousY = pointY;
        }
    }
}
