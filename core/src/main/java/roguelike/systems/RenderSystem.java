package roguelike.systems;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import roguelike.graphics.EffectRenderer;
import roguelike.graphics.FloorRenderer;
import roguelike.graphics.GameTextures;
import roguelike.graphics.ParticleRenderer;
import roguelike.graphics.ProjectileRenderer;
import roguelike.graphics.UnitRenderer;
import roguelike.model.GameWorld;

public final class RenderSystem {
    private RenderSystem() {
    }

    public static void draw(GameWorld world, ShapeRenderer shapes, SpriteBatch batch, BitmapFont font, GameTextures textures) {
        FloorRenderer.draw(world, shapes, batch, textures);
        EffectRenderer.drawTelegraphs(world, shapes);
        UnitRenderer.drawBodies(world, batch, textures);
        ProjectileRenderer.drawCores(world, batch, textures);
        ParticleRenderer.drawSolid(world, batch, textures);
        UnitRenderer.drawBars(world, shapes);
        EffectRenderer.drawEffects(world, shapes, batch, textures);
        UnitRenderer.drawShields(world, shapes);
        ProjectileRenderer.drawGlows(world, batch, textures);
        ParticleRenderer.drawGlowing(world, batch, textures);
        UnitRenderer.drawGlows(world, batch, textures);
        EffectRenderer.drawFloatingTexts(world, batch, font);
    }
}
