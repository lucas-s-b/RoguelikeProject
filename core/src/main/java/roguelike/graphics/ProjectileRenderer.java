package roguelike.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import roguelike.model.GameWorld;
import roguelike.model.Projectile;
import roguelike.model.Team;

public final class ProjectileRenderer {
    private static final Color drawColor = new Color();

    private ProjectileRenderer() {
    }

    public static void drawCores(GameWorld world, SpriteBatch batch, GameTextures textures) {
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.begin();
        for (Projectile projectile : world.projectiles) {
            float x = projectile.position.x;
            float y = projectile.position.y;
            float angle = projectile.velocity.angleDeg();
            float radius = projectile.radius * (1f + 0.12f * MathUtils.sin(projectile.age * 30f));
            if (projectile.team == Team.ENEMY) {
                Draw.sprite(batch, textures.circle, x, y, radius * 3.1f, radius * 2.4f, angle, projectile.color, 1f);
            }
            drawColor.set(projectile.color).lerp(Color.WHITE, 0.55f);
            Draw.sprite(batch, textures.circle, x, y, radius * 2.6f, radius * 1.7f, angle, drawColor, 1f);
        }
        batch.end();
    }

    public static void drawGlows(GameWorld world, SpriteBatch batch, GameTextures textures) {
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        batch.begin();
        for (Projectile projectile : world.projectiles) {
            float x = projectile.position.x;
            float y = projectile.position.y;
            float angle = projectile.velocity.angleDeg();
            float streakLength = projectile.radius * 4f + projectile.velocity.len() * 0.05f;
            float streakX = x - MathUtils.cosDeg(angle) * streakLength * 0.3f;
            float streakY = y - MathUtils.sinDeg(angle) * streakLength * 0.3f;
            Draw.sprite(batch, textures.glow, streakX, streakY, streakLength, projectile.radius * 3f, angle, projectile.color, 0.55f);
            float glowSize = projectile.radius * 6f;
            Draw.sprite(batch, textures.glow, x, y, glowSize, glowSize, 0f, projectile.color, 0.9f);
        }
        batch.end();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }
}
