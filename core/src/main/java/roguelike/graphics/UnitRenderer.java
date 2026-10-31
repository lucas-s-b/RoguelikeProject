package roguelike.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import roguelike.model.BodyShape;
import roguelike.model.GameWorld;
import roguelike.model.StatusInstance;
import roguelike.model.Team;
import roguelike.model.Unit;
import roguelike.systems.AnimationSystem;
import roguelike.systems.Targeting;

public final class UnitRenderer {
    private static final Color SHADOW_COLOR = new Color(0f, 0f, 0f, 0.55f);
    private static final Color HEALTH_BACK_COLOR = new Color(0.12f, 0.03f, 0.04f, 0.9f);
    private static final Color HEALTH_FILL_COLOR = new Color(0.95f, 0.22f, 0.25f, 1f);
    private static final Color HEALTH_TRAIL_COLOR = new Color(1f, 0.88f, 0.7f, 1f);
    private static final Color HEALTH_SHINE_COLOR = new Color(1f, 0.6f, 0.6f, 1f);
    private static final Color BAR_OUTLINE_COLOR = new Color(0.07f, 0.055f, 0.094f, 0.95f);
    private static final Color SHIELD_COLOR = new Color(0.6f, 0.85f, 1f, 1f);
    private static final Color WINDUP_COLOR = new Color(1f, 0.35f, 0.25f, 1f);
    private static final Color BOSS_EYE_COLOR = new Color(1f, 0.85f, 0.3f, 1f);
    private static final Color ELITE_COLOR = new Color(1f, 0.8f, 0.3f, 1f);
    private static final Color FRONTAL_SHIELD_COLOR = new Color(0.75f, 0.85f, 1f, 1f);
    private static final float SPEED_FOR_FULL_STRETCH = 220f;
    private static final float PHASE_PER_HASH = MathUtils.PI2 / 1024f;
    private static final float BLINK_INTERVAL_SECONDS = 3.3f;
    private static final float BLINK_SECONDS = 0.12f;
    private static final float BAR_PIXEL = 2f;
    private static final float BAR_HEIGHT = 6f;
    private static final float PIP_SIZE = 8f;
    private static final float FLYING_HEIGHT = 12f;
    private static final float FLYING_BOB_AMOUNT = 5f;
    private static final float FLYING_BOB_SPEED = 4f;
    private static final Color bodyColor = new Color();
    private static final Color drawColor = new Color();

    private UnitRenderer() {
    }

    public static void drawBodies(GameWorld world, SpriteBatch batch, GameTextures textures) {
        Unit player = world.player();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.begin();
        for (Unit unit : world.units) {
            if (unit.isDead) {
                continue;
            }
            float shadowScale = spawnScale(unit) * (1f - hopHeight(unit, stunColor(unit) != null) * 0.04f);
            Draw.sprite(batch, textures.glow, unit.position.x, unit.position.y - unit.radius * 0.75f,
                    unit.radius * 3.2f * shadowScale, unit.radius * 1.5f * shadowScale, 0f, SHADOW_COLOR, 1f);
        }
        for (Unit unit : world.units) {
            if (unit.isDead) {
                continue;
            }
            Color stunColor = stunColor(unit);
            boolean isStunned = stunColor != null;
            float phase = (unit.hashCode() & 1023) * PHASE_PER_HASH;
            float moveAmount = MathUtils.clamp(unit.smoothedVelocity.len() / SPEED_FOR_FULL_STRETCH, 0f, 1f);
            float breath = isStunned ? 0f : MathUtils.sin(unit.age * 3f + phase) * 0.035f;
            float poseAmount = unit.attackPoseTimeRemaining / AnimationSystem.ATTACK_POSE_SECONDS;
            poseAmount *= poseAmount;
            float hitSquash = Math.max(0f, unit.hitFlashTimeRemaining) * 2.5f;
            boolean isWindingUp = unit.windupTimeRemaining > 0f;
            float flashPeriod = 0.08f + 0.18f * Math.min(1f, unit.windupTimeRemaining);
            boolean isWindupFlash = isWindingUp && unit.windupTimeRemaining % flashPeriod < flashPeriod * 0.4f;
            boolean isWhite = unit.hitFlashTimeRemaining > 0f || isWindupFlash;
            boolean isBlinking = unit.team == Team.PLAYER && unit.invulnerableTimeRemaining > 0f && MathUtils.sin(world.time * 40f) > 0f;
            float alpha = isBlinking ? 0.4f : 1f;

            float stretchAngle = poseAmount > 0.01f ? unit.attackPoseAngle : unit.smoothedVelocity.angleDeg();
            float stretch = 1f + 0.14f * moveAmount * (1f - poseAmount) + 0.22f * poseAmount + hitSquash;
            float scale = spawnScale(unit) * (1f + breath) * (isWindingUp ? 1.1f : 1f);
            float radius = unit.radius * scale;
            float centerX = unit.position.x + MathUtils.cosDeg(unit.attackPoseAngle) * 6f * poseAmount;
            float centerY = unit.position.y + hopHeight(unit, isStunned) + MathUtils.sinDeg(unit.attackPoseAngle) * 6f * poseAmount;
            if (isWindingUp) {
                centerX += MathUtils.random(-1.5f, 1.5f);
                centerY += MathUtils.random(-1.5f, 1.5f);
            }
            BodyShape shape = unit.bodyShape;
            boolean isRound = shape == BodyShape.CIRCLE;
            float shapeStretch = isRound ? stretch : 1f + (stretch - 1f) * 0.5f;
            float bodyAngle = isRound ? stretchAngle : unit.bodyAngle + shape.angleOffset;
            float bodyWidth = radius * 2f * shape.visualScale * shapeStretch;
            float bodyHeight = radius * 2f * shape.visualScale / shapeStretch;
            float innerRadius = radius * shape.visualScale * shape.innerRadiusFactor;
            Texture bodyTexture = textures.bodyTexture(shape);

            bodyColor.set(unit.color);
            if (isStunned) {
                bodyColor.lerp(stunColor, 0.65f);
            }
            drawColor.set(bodyColor).mul(0.3f, 0.3f, 0.35f, 1f);
            Draw.sprite(batch, bodyTexture, centerX, centerY, bodyWidth + 5f * scale, bodyHeight + 5f * scale, bodyAngle, drawColor, alpha);
            Draw.sprite(batch, bodyTexture, centerX, centerY, bodyWidth, bodyHeight, bodyAngle, isWhite ? Color.WHITE : bodyColor, alpha);
            if (!isWhite) {
                drawColor.set(bodyColor).mul(0.7f, 0.7f, 0.75f, 1f);
                Draw.sprite(batch, textures.circle, centerX + innerRadius * 0.15f, centerY - innerRadius * 0.2f, innerRadius * 1.5f, innerRadius * 1.4f, 0f, drawColor, alpha * 0.35f);
                drawColor.set(bodyColor).lerp(Color.WHITE, 0.6f);
                Draw.sprite(batch, textures.circle, centerX - innerRadius * 0.32f, centerY + innerRadius * 0.34f, innerRadius * 0.6f, innerRadius * 0.5f, 0f, drawColor, alpha * 0.7f);
            }

            float lookAngle = unit.team == Team.PLAYER ? unit.aimAngle : Targeting.angleBetween(unit.position, player.position);
            float lookX = MathUtils.cosDeg(lookAngle);
            float lookY = MathUtils.sinDeg(lookAngle);
            boolean areEyesClosed = !isStunned && ((unit.age + phase) % BLINK_INTERVAL_SECONDS < BLINK_SECONDS || unit.hitFlashTimeRemaining > 0f);
            float eyeSpacing = innerRadius * 0.36f * (0.65f + 0.35f * Math.abs(lookY));
            float eyesX = centerX + lookX * innerRadius * 0.38f;
            float eyesY = centerY + lookY * innerRadius * 0.22f + innerRadius * 0.12f;
            float eyeWidth = innerRadius * 0.26f;
            float eyeHeight = innerRadius * (areEyesClosed ? 0.07f : 0.34f);
            Color eyeColor = unit.isBoss ? BOSS_EYE_COLOR : drawColor.set(bodyColor).mul(0.12f, 0.12f, 0.14f, 1f);
            for (int side = -1; side <= 1; side += 2) {
                float eyeX = eyesX + side * eyeSpacing;
                Draw.sprite(batch, textures.circle, eyeX, eyesY, eyeWidth, eyeHeight, 0f, eyeColor, alpha);
                if (!areEyesClosed && !unit.isBoss) {
                    float glintSize = eyeWidth * 0.4f;
                    float glintX = eyeX - eyeWidth * 0.15f + lookX * eyeWidth * 0.15f;
                    Draw.sprite(batch, textures.circle, glintX, eyesY + eyeHeight * 0.2f, glintSize, glintSize, 0f, Color.WHITE, alpha * 0.9f);
                }
            }

            if (isStunned) {
                float shellSize = radius * 2.5f;
                Draw.sprite(batch, textures.circle, centerX, centerY, shellSize, shellSize, 0f, stunColor, alpha * 0.3f);
            }
            if (unit.team == Team.PLAYER) {
                float aimDistance = radius + 10f + 5f * poseAmount;
                Draw.sprite(batch, textures.diamond, centerX + lookX * aimDistance, centerY + lookY * aimDistance, 11f, 7f, lookAngle, Color.WHITE, alpha);
            }
        }
        batch.end();
    }

    public static void drawBars(GameWorld world, ShapeRenderer shapes) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (Unit unit : world.units) {
            boolean isHurt = unit.trailingHealthFraction < 0.999f;
            if (unit.isDead || unit.team == Team.PLAYER || (!isHurt && unit.statuses.isEmpty())) {
                continue;
            }
            float barWidth = Math.round(Math.max(26f, unit.radius * 2f) / BAR_PIXEL) * BAR_PIXEL;
            float barX = Math.round(unit.position.x - barWidth / 2f);
            float barY = Math.round(unit.position.y + unit.radius + 9f + (unit.isFlying ? hopHeight(unit, false) : 0f));
            float fillWidth = Math.round(barWidth * MathUtils.clamp(unit.healthFraction(), 0f, 1f) / BAR_PIXEL) * BAR_PIXEL;
            float trailWidth = Math.round(barWidth * MathUtils.clamp(unit.trailingHealthFraction, 0f, 1f) / BAR_PIXEL) * BAR_PIXEL;
            shapes.setColor(BAR_OUTLINE_COLOR);
            shapes.rect(barX - BAR_PIXEL, barY, barWidth + BAR_PIXEL * 2f, BAR_HEIGHT);
            shapes.rect(barX, barY - BAR_PIXEL, barWidth, BAR_HEIGHT + BAR_PIXEL * 2f);
            shapes.setColor(HEALTH_BACK_COLOR);
            shapes.rect(barX, barY, barWidth, BAR_HEIGHT);
            shapes.setColor(HEALTH_TRAIL_COLOR);
            shapes.rect(barX, barY, trailWidth, BAR_HEIGHT);
            shapes.setColor(unit.hitFlashTimeRemaining > 0f ? Color.WHITE : HEALTH_FILL_COLOR);
            shapes.rect(barX, barY, fillWidth, BAR_HEIGHT);
            shapes.setColor(HEALTH_SHINE_COLOR);
            shapes.rect(barX, barY + BAR_HEIGHT - BAR_PIXEL, fillWidth, BAR_PIXEL);
            float pipX = barX - BAR_PIXEL;
            float pipY = barY + BAR_HEIGHT + BAR_PIXEL * 2f;
            for (StatusInstance status : unit.statuses) {
                shapes.setColor(BAR_OUTLINE_COLOR);
                shapes.rect(pipX, pipY, PIP_SIZE, PIP_SIZE);
                shapes.setColor(status.definition.color);
                shapes.rect(pipX + BAR_PIXEL, pipY + BAR_PIXEL, PIP_SIZE - BAR_PIXEL * 2f, PIP_SIZE - BAR_PIXEL * 2f);
                pipX += PIP_SIZE + BAR_PIXEL;
            }
        }
        shapes.end();
    }

    public static void drawShields(GameWorld world, ShapeRenderer shapes) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (Unit unit : world.units) {
            if (unit.isDead) {
                continue;
            }
            float shieldArc = unit.enemyDefinition == null ? 0f : unit.enemyDefinition.frontalShieldDegrees;
            if (shieldArc > 0f) {
                Draw.band(shapes, unit.position.x, unit.position.y + (unit.isFlying ? hopHeight(unit, false) : 0f), unit.radius + 3f, unit.radius + 7f, unit.radius + 11f,
                        unit.aimAngle - shieldArc / 2f, shieldArc, FRONTAL_SHIELD_COLOR, 0.2f, 0.9f, 0.2f, 0f);
            }
            if (unit.isElite) {
                float elitePulse = 0.6f + 0.4f * MathUtils.sin(world.time * 4f + unit.radius);
                Draw.band(shapes, unit.position.x, unit.position.y + (unit.isFlying ? hopHeight(unit, false) : 0f), unit.radius + 1f, unit.radius + 4f, unit.radius + 9f, 0f, 360f, ELITE_COLOR, 0f, 0.7f * elitePulse, 0f, 0f);
            }
            if (unit.shield <= 0f) {
                continue;
            }
            float x = unit.position.x;
            float y = unit.position.y + (unit.isFlying ? hopHeight(unit, false) : 0f);
            float radius = unit.radius;
            float pulse = 0.7f + 0.3f * MathUtils.sin(world.time * 6f);
            Draw.band(shapes, x, y, radius + 2f, radius + 5f, radius + 8f, 0f, 360f, SHIELD_COLOR, 0f, 0.8f * pulse, 0f, 0f);
            Draw.band(shapes, x, y, radius + 3f, radius + 5f, radius + 7f, world.time * 160f, 80f, Color.WHITE, 0f, 0.7f, 0f, 1f);
        }
        shapes.end();
    }

    public static void drawGlows(GameWorld world, SpriteBatch batch, GameTextures textures) {
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        batch.begin();
        for (Unit unit : world.units) {
            if (unit.isDead) {
                continue;
            }
            float x = unit.position.x;
            float y = unit.position.y + (unit.isFlying ? hopHeight(unit, false) : 0f);
            float pulse = 0.75f + 0.25f * MathUtils.sin(world.time * 5f + unit.radius);
            if (unit.isBoss) {
                float auraSize = unit.radius * 5f * pulse;
                Draw.sprite(batch, textures.glow, x, y, auraSize, auraSize, 0f, unit.color, 0.35f);
            }
            if (unit.isElite) {
                float eliteGlowSize = unit.radius * 4f * pulse;
                Draw.sprite(batch, textures.glow, x, y, eliteGlowSize, eliteGlowSize, 0f, ELITE_COLOR, 0.3f);
            }
            if (!unit.statuses.isEmpty()) {
                float auraSize = unit.radius * 3.4f;
                Draw.sprite(batch, textures.glow, x, y, auraSize, auraSize, 0f, unit.statuses.get(0).definition.color, 0.22f * pulse);
            }
            if (unit.team == Team.PLAYER) {
                float haloSize = unit.radius * 4.5f;
                Draw.sprite(batch, textures.glow, x, y, haloSize, haloSize, 0f, unit.color, 0.22f);
                float aimDistance = unit.radius + 10f;
                float aimX = x + MathUtils.cosDeg(unit.aimAngle) * aimDistance;
                float aimY = y + MathUtils.sinDeg(unit.aimAngle) * aimDistance;
                Draw.sprite(batch, textures.glow, aimX, aimY, 26f, 26f, 0f, Color.WHITE, 0.5f);
            }
            if (unit.hitFlashTimeRemaining > 0f) {
                float flashSize = unit.radius * 4f;
                Draw.sprite(batch, textures.glow, x, y, flashSize, flashSize, 0f, Color.WHITE, 0.5f);
            }
            if (unit.windupTimeRemaining > 0f) {
                float urgency = 1f - Math.min(1f, unit.windupTimeRemaining);
                float windupSize = unit.radius * (4f + 2f * urgency);
                Draw.sprite(batch, textures.glow, x, y, windupSize, windupSize, 0f, WINDUP_COLOR, 0.25f + 0.35f * urgency);
            }
            float spawnFlash = 1f - unit.age / AnimationSystem.SPAWN_SECONDS;
            if (spawnFlash > 0f) {
                float flashSize = unit.radius * 6f;
                Draw.sprite(batch, textures.glow, x, y, flashSize, flashSize, 0f, unit.color, spawnFlash * 0.8f);
            }
            Color stunColor = stunColor(unit);
            if (stunColor != null) {
                for (int star = 0; star < 3; star++) {
                    float starAngle = world.time * 240f + star * 120f;
                    float starX = x + MathUtils.cosDeg(starAngle) * unit.radius * 0.9f;
                    float starY = y + unit.radius + 6f + MathUtils.sinDeg(starAngle) * unit.radius * 0.3f;
                    Draw.sprite(batch, textures.glow, starX, starY, 10f, 10f, 0f, stunColor, 0.9f);
                }
            }
        }
        batch.end();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private static float spawnScale(Unit unit) {
        return Interpolation.swingOut.apply(Math.min(1f, unit.age / AnimationSystem.SPAWN_SECONDS));
    }

    private static float hopHeight(Unit unit, boolean isStunned) {
        float phase = (unit.hashCode() & 1023) * PHASE_PER_HASH;
        if (unit.isFlying) {
            return FLYING_HEIGHT + MathUtils.sin(unit.age * FLYING_BOB_SPEED + phase) * FLYING_BOB_AMOUNT;
        }
        if (isStunned) {
            return 0f;
        }
        float moveAmount = MathUtils.clamp(unit.smoothedVelocity.len() / SPEED_FOR_FULL_STRETCH, 0f, 1f);
        return Math.abs(MathUtils.sin(unit.age * 11f + phase)) * 3.5f * moveAmount;
    }

    private static Color stunColor(Unit unit) {
        for (StatusInstance status : unit.statuses) {
            if (status.definition.isStun) {
                return status.definition.color;
            }
        }
        return null;
    }
}
