package roguelike.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import roguelike.model.GameWorld;
import roguelike.model.Particle;

public final class ParticleRenderer {
    private static final Color drawColor = new Color();

    private ParticleRenderer() {
    }

    public static void drawSolid(GameWorld world, SpriteBatch batch, GameTextures textures) {
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.begin();
        for (Particle particle : world.particles) {
            if (particle.style == Particle.Style.SMOKE || particle.style == Particle.Style.SHARD) {
                drawParticle(batch, textures, particle);
            }
        }
        batch.end();
    }

    public static void drawGlowing(GameWorld world, SpriteBatch batch, GameTextures textures) {
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        batch.begin();
        for (Particle particle : world.particles) {
            if (particle.style != Particle.Style.SMOKE && particle.style != Particle.Style.SHARD) {
                drawParticle(batch, textures, particle);
            }
        }
        batch.end();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private static void drawParticle(SpriteBatch batch, GameTextures textures, Particle particle) {
        float progress = 1f - particle.lifetimeRemaining / particle.totalLifetime;
        float age = particle.totalLifetime - particle.lifetimeRemaining;
        float fadeIn = particle.fadeInSeconds > 0f ? Math.min(1f, age / particle.fadeInSeconds) : 1f;
        drawColor.set(particle.color).lerp(particle.endColor, progress);
        float alpha = drawColor.a * fadeIn;
        drawColor.a = 1f;
        float size = particle.size * MathUtils.lerp(1f, particle.endSizeScale, progress) * 2f;
        float x = particle.position.x;
        float y = particle.position.y;
        switch (particle.style) {
            case GLOW -> Draw.sprite(batch, textures.glow, x, y, size * 1.6f, size * 1.6f, 0f, drawColor, alpha * 0.85f);
            case SPARK -> {
                float length = size + particle.velocity.len() * 0.06f;
                Draw.sprite(batch, textures.glow, x, y, length * 1.6f, size, particle.velocity.angleDeg(), drawColor, alpha);
            }
            case SMOKE -> Draw.sprite(batch, textures.smoke, x, y, size, size, particle.rotation, drawColor, alpha);
            case SHARD -> Draw.sprite(batch, textures.diamond, x, y, size, size * 0.6f, particle.rotation, drawColor, alpha);
            case GHOST -> Draw.sprite(batch, textures.circle, x, y, size, size, 0f, drawColor, alpha);
        }
    }
}
