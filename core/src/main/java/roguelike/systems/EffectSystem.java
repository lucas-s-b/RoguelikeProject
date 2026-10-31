package roguelike.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import roguelike.model.Decal;
import roguelike.model.FloatingText;
import roguelike.model.GameWorld;
import roguelike.model.Hit;
import roguelike.model.Particle;
import roguelike.model.Team;
import roguelike.model.Unit;
import roguelike.model.VisualEffect;

public final class EffectSystem {
    public static final Color GOLD_COLOR = new Color(1f, 0.85f, 0.25f, 1f);
    public static final Color HEAL_COLOR = new Color(0.4f, 1f, 0.45f, 1f);
    public static final Color CRIT_COLOR = new Color(1f, 0.9f, 0.2f, 1f);
    public static final Color PLAYER_DAMAGED_COLOR = new Color(1f, 0.3f, 0.3f, 1f);
    private static final float POP_SECONDS = 0.2f;
    private static final Color SMOKE_COLOR = new Color(0.09f, 0.08f, 0.1f, 1f);
    private static final Color MOTE_COLOR = new Color(1f, 0.86f, 0.62f, 0.3f);
    private static final int MAXIMUM_PARTICLES = 3000;
    private static final int MAXIMUM_DECALS = 160;
    private static final int SUMMON_PARTICLE_COUNT = 14;
    private static final float MAXIMUM_SCREEN_SHAKE = 1f;
    private static final float SCREEN_SHAKE_DECAY_PER_SECOND = 2.2f;
    private static final float FLOATING_TEXT_GRAVITY = 240f;
    private static final float AMBIENT_MOTES_PER_SECOND = 10f;
    private static final float DECAL_SECONDS = 9f;
    private static final Color derivedColor = new Color();

    private EffectSystem() {
    }

    public static VisualEffect circle(GameWorld world, Vector2 position, float radius, Color color, float duration) {
        return create(world, VisualEffect.Shape.CIRCLE, position, radius, color, duration);
    }

    public static VisualEffect ring(GameWorld world, Vector2 position, float radius, Color color, float duration) {
        return create(world, VisualEffect.Shape.RING, position, radius, color, duration);
    }

    public static VisualEffect shockwave(GameWorld world, Vector2 position, float radius, Color color, float duration) {
        VisualEffect effect = create(world, VisualEffect.Shape.RING, position, radius, color, duration);
        effect.growsOverTime = true;
        effect.thickness = Math.max(6f, radius * 0.18f);
        return effect;
    }

    public static VisualEffect arc(GameWorld world, Vector2 position, float angle, float arcDegrees, float radius, Color color, float duration) {
        VisualEffect effect = create(world, VisualEffect.Shape.ARC, position, radius, color, duration);
        effect.angle = angle;
        effect.arcDegrees = arcDegrees;
        return effect;
    }

    public static VisualEffect line(GameWorld world, Vector2 start, Vector2 end, float thickness, Color color, float duration) {
        VisualEffect effect = create(world, VisualEffect.Shape.LINE, start, 0f, color, duration);
        effect.endPosition.set(end);
        effect.thickness = thickness;
        return effect;
    }

    public static VisualEffect telegraphCircle(GameWorld world, Vector2 position, float radius, Color color, float duration) {
        VisualEffect effect = create(world, VisualEffect.Shape.CIRCLE, position, radius, color, duration);
        effect.isTelegraph = true;
        return effect;
    }

    public static VisualEffect telegraphLine(GameWorld world, Vector2 start, Vector2 end, float width, Color color, float duration) {
        VisualEffect effect = create(world, VisualEffect.Shape.LINE, start, 0f, color, duration);
        effect.endPosition.set(end);
        effect.thickness = width;
        effect.isTelegraph = true;
        return effect;
    }

    public static void summon(GameWorld world, Vector2 position, float radius, Color color, float duration) {
        VisualEffect effect = create(world, VisualEffect.Shape.RING, position, radius, color, duration);
        effect.isTelegraph = true;
        for (int index = 0; index < SUMMON_PARTICLE_COUNT; index++) {
            Vector2 start = new Vector2(radius * MathUtils.random(2.5f, 4.5f), 0f).setAngleDeg(MathUtils.random(360f)).add(position);
            Particle particle = createParticle(world, Particle.Style.SPARK, start, color, MathUtils.random(2f, 3.5f), duration);
            if (particle == null) {
                return;
            }
            particle.velocity.set(position).sub(start).scl(1f / duration);
            particle.drag = 0f;
            particle.endSizeScale = 1f;
            particle.color.a = 0f;
            particle.endColor.a = 1f;
        }
    }

    public static void lightning(GameWorld world, Vector2 start, Vector2 end, Color color, float thickness, float duration) {
        VisualEffect effect = create(world, VisualEffect.Shape.LIGHTNING, start, 0f, color, duration);
        effect.endPosition.set(end);
        effect.thickness = thickness;
        effect.seed = MathUtils.random(1000f);
        burst(world, end, color, 6, 160f, 4f, 0.25f);
    }

    public static void pop(GameWorld world, Unit unit) {
        VisualEffect effect = create(world, VisualEffect.Shape.POP, unit.position, unit.radius, unit.color, POP_SECONDS);
        effect.bodyShape = unit.bodyShape;
        effect.angle = unit.bodyAngle;
    }

    public static void explosion(GameWorld world, Vector2 position, float radius, Color color) {
        circle(world, position, radius, color, 0.25f);
        shockwave(world, position, radius, color, 0.35f);
        burst(world, position, color, Math.min(40, 8 + Math.round(radius / 6f)), radius * 3f, 5f, 0.4f);
        smoke(world, position, color, Math.min(12, 3 + Math.round(radius / 14f)), radius * 0.5f);
        scorch(world, position, radius * 0.75f, color);
        shake(world, Math.min(0.3f, radius / 600f));
    }

    public static void burst(GameWorld world, Vector2 position, Color color, int count, float speed, float size, float lifetime) {
        for (int index = 0; index < count; index++) {
            Particle particle = createParticle(world, Particle.Style.GLOW, position, color, size * MathUtils.random(0.6f, 1.3f), lifetime * MathUtils.random(0.6f, 1.2f));
            if (particle == null) {
                return;
            }
            particle.velocity.set(speed * MathUtils.random(0.3f, 1f), 0f).setAngleDeg(MathUtils.random(360f));
        }
    }

    public static void sparks(GameWorld world, Vector2 position, float angleDegrees, float spreadDegrees, Color color, int count, float speed) {
        for (int index = 0; index < count; index++) {
            Particle particle = createParticle(world, Particle.Style.SPARK, position, color, MathUtils.random(2.5f, 4f), MathUtils.random(0.15f, 0.35f));
            if (particle == null) {
                return;
            }
            float angle = angleDegrees + MathUtils.random(-spreadDegrees / 2f, spreadDegrees / 2f);
            particle.velocity.set(speed * MathUtils.random(0.5f, 1.2f), 0f).setAngleDeg(angle);
            particle.drag = 6f;
            particle.color.lerp(Color.WHITE, 0.5f);
        }
    }

    public static void shards(GameWorld world, Vector2 position, Color color, int count, float speed) {
        for (int index = 0; index < count; index++) {
            Particle particle = createParticle(world, Particle.Style.SHARD, position, color, MathUtils.random(2.5f, 5f), MathUtils.random(0.4f, 0.8f));
            if (particle == null) {
                return;
            }
            particle.velocity.set(speed * MathUtils.random(0.3f, 1f), 0f).setAngleDeg(MathUtils.random(360f));
            particle.rotation = MathUtils.random(360f);
            particle.spin = MathUtils.random(-720f, 720f);
            particle.drag = 5f;
            particle.endSizeScale = 0.7f;
            particle.endColor.set(color.r * 0.4f, color.g * 0.4f, color.b * 0.4f, 0f);
        }
    }

    public static void smoke(GameWorld world, Vector2 position, Color color, int count, float spreadRadius) {
        derivedColor.set(color).lerp(SMOKE_COLOR, 0.7f);
        derivedColor.a = 0.55f;
        for (int index = 0; index < count; index++) {
            Vector2 start = Attacks.randomPointAround(position, spreadRadius);
            Particle particle = createParticle(world, Particle.Style.SMOKE, start, derivedColor, MathUtils.random(10f, 18f), MathUtils.random(0.7f, 1.2f));
            if (particle == null) {
                return;
            }
            particle.velocity.set(start).sub(position).scl(MathUtils.random(1f, 2f)).add(0f, MathUtils.random(15f, 35f));
            particle.drag = 2.5f;
            particle.gravity = -20f;
            particle.endSizeScale = 2.2f;
            particle.rotation = MathUtils.random(360f);
            particle.spin = MathUtils.random(-60f, 60f);
            particle.fadeInSeconds = 0.08f;
        }
    }

    public static void trail(GameWorld world, Vector2 position, Color color, float size, float lifetime) {
        Particle particle = createParticle(world, Particle.Style.GLOW, position, color, size, lifetime);
        if (particle != null) {
            particle.velocity.set(MathUtils.random(-15f, 15f), MathUtils.random(-15f, 15f));
        }
    }

    public static void afterimage(GameWorld world, Unit unit) {
        Particle particle = createParticle(world, Particle.Style.GHOST, unit.position, unit.color, unit.radius, 0.22f);
        if (particle != null) {
            particle.drag = 0f;
            particle.color.a = 0.45f;
            particle.endSizeScale = 0.85f;
        }
    }

    public static void muzzleFlash(GameWorld world, Vector2 position, float angleDegrees, Color color) {
        sparks(world, position, angleDegrees, 50f, color, 3, 260f);
        Particle flash = createParticle(world, Particle.Style.GLOW, position, Color.WHITE, 8f, 0.08f);
        if (flash != null) {
            flash.endSizeScale = 1.6f;
        }
    }

    public static void rising(GameWorld world, Vector2 position, Color color, int count, float spreadRadius) {
        for (int index = 0; index < count; index++) {
            Vector2 start = Attacks.randomPointAround(position, spreadRadius);
            Particle particle = createParticle(world, Particle.Style.GLOW, start, color, MathUtils.random(3f, 5f), MathUtils.random(0.5f, 0.9f));
            if (particle == null) {
                return;
            }
            particle.velocity.set(MathUtils.random(-10f, 10f), MathUtils.random(50f, 110f));
            particle.drag = 1.5f;
            particle.fadeInSeconds = 0.1f;
        }
    }

    public static void splat(GameWorld world, Vector2 position, Color color, float radius) {
        derivedColor.set(color).mul(0.45f, 0.45f, 0.45f, 1f);
        derivedColor.a = 0.5f;
        createDecal(world, position, derivedColor, radius * 2f, radius * MathUtils.random(1.4f, 1.9f), false);
        int dropletCount = MathUtils.random(4, 7);
        for (int index = 0; index < dropletCount; index++) {
            Vector2 dropletPosition = new Vector2(radius * MathUtils.random(1f, 2.2f), 0f).setAngleDeg(MathUtils.random(360f)).add(position);
            float dropletSize = radius * MathUtils.random(0.2f, 0.45f);
            createDecal(world, dropletPosition, derivedColor, dropletSize, dropletSize * MathUtils.random(0.7f, 1f), false);
        }
    }

    public static void scorch(GameWorld world, Vector2 position, float radius, Color color) {
        for (Decal decal : world.decals) {
            if (decal.isSoft && decal.position.dst(position) < radius * 0.4f) {
                decal.lifetimeRemaining = decal.totalLifetime;
                return;
            }
        }
        derivedColor.set(color).mul(0.15f, 0.15f, 0.15f, 1f);
        derivedColor.a = 0.6f;
        createDecal(world, position, derivedColor, radius * 2f, radius * 2f * MathUtils.random(0.8f, 1f), true);
    }

    public static void flames(GameWorld world, Unit unit, Color color, float particlesPerSecond, float delta) {
        int count = emissionCount(particlesPerSecond, delta);
        for (int index = 0; index < count; index++) {
            Vector2 start = Attacks.randomPointAround(unit.position, unit.radius * 0.7f);
            Particle particle = createParticle(world, Particle.Style.GLOW, start, color, MathUtils.random(3f, 6f), MathUtils.random(0.3f, 0.55f));
            if (particle == null) {
                return;
            }
            particle.color.lerp(Color.WHITE, 0.35f);
            particle.endColor.set(color.r, color.g * 0.3f, color.b * 0.2f, 0f);
            particle.velocity.set(MathUtils.random(-15f, 15f), MathUtils.random(30f, 70f));
            particle.gravity = -140f;
            particle.drag = 2f;
            particle.endSizeScale = 0.2f;
        }
    }

    public static void bubbles(GameWorld world, Unit unit, Color color, float particlesPerSecond, float delta) {
        int count = emissionCount(particlesPerSecond, delta);
        for (int index = 0; index < count; index++) {
            Vector2 start = Attacks.randomPointAround(unit.position, unit.radius * 0.9f);
            Particle particle = createParticle(world, Particle.Style.GLOW, start, color, MathUtils.random(2f, 4f), MathUtils.random(0.6f, 1f));
            if (particle == null) {
                return;
            }
            particle.velocity.set(MathUtils.random(-10f, 10f), MathUtils.random(15f, 35f));
            particle.gravity = -15f;
            particle.drag = 1f;
            particle.endSizeScale = 1.5f;
            particle.fadeInSeconds = 0.15f;
        }
    }

    public static void drips(GameWorld world, Unit unit, Color color, float particlesPerSecond, float delta) {
        int count = emissionCount(particlesPerSecond, delta);
        for (int index = 0; index < count; index++) {
            Vector2 start = Attacks.randomPointAround(unit.position, unit.radius * 0.8f);
            Particle particle = createParticle(world, Particle.Style.GLOW, start, color, MathUtils.random(2f, 3.5f), MathUtils.random(0.3f, 0.45f));
            if (particle == null) {
                return;
            }
            particle.velocity.set(MathUtils.random(-10f, 10f), MathUtils.random(0f, 20f));
            particle.gravity = 300f;
            particle.drag = 0.5f;
            particle.endSizeScale = 0.7f;
            if (MathUtils.randomBoolean(0.35f)) {
                derivedColor.set(color).mul(0.45f, 0.45f, 0.45f, 1f);
                derivedColor.a = 0.5f;
                float dropletSize = MathUtils.random(3f, 5f);
                createDecal(world, new Vector2(start.x, start.y - 20f), derivedColor, dropletSize, dropletSize * 0.8f, false);
            }
        }
    }

    public static void frost(GameWorld world, Unit unit, Color color, float particlesPerSecond, float delta) {
        int count = emissionCount(particlesPerSecond, delta);
        for (int index = 0; index < count; index++) {
            Vector2 start = Attacks.randomPointAround(unit.position, unit.radius * 1.1f);
            Particle particle = createParticle(world, Particle.Style.SHARD, start, color, MathUtils.random(1.5f, 3f), MathUtils.random(0.6f, 1f));
            if (particle == null) {
                return;
            }
            particle.color.lerp(Color.WHITE, 0.4f);
            particle.velocity.set(MathUtils.random(-12f, 12f), MathUtils.random(-25f, -5f));
            particle.gravity = 20f;
            particle.drag = 1f;
            particle.rotation = MathUtils.random(360f);
            particle.spin = MathUtils.random(-300f, 300f);
            particle.endSizeScale = 0.5f;
            particle.fadeInSeconds = 0.1f;
        }
    }

    public static void sparkles(GameWorld world, Unit unit, Color color, float particlesPerSecond, float delta) {
        int count = emissionCount(particlesPerSecond, delta);
        for (int index = 0; index < count; index++) {
            Vector2 start = Attacks.randomPointAround(unit.position, unit.radius * 1.2f);
            Particle particle = createParticle(world, Particle.Style.GLOW, start, color, MathUtils.random(1.5f, 3f), MathUtils.random(0.5f, 0.8f));
            if (particle == null) {
                return;
            }
            particle.velocity.set(MathUtils.random(-5f, 5f), MathUtils.random(20f, 40f));
            particle.drag = 1f;
            particle.endSizeScale = 0.3f;
            particle.fadeInSeconds = 0.2f;
        }
    }

    public static void shake(GameWorld world, float amount) {
        world.screenShake = Math.min(MAXIMUM_SCREEN_SHAKE, world.screenShake + amount);
    }

    public static void text(GameWorld world, Vector2 position, String text, Color color, float scale) {
        FloatingText floatingText = new FloatingText();
        floatingText.text = text;
        floatingText.position.set(position.x + MathUtils.random(-8f, 8f), position.y + 18f);
        floatingText.velocity.set(MathUtils.random(-35f, 35f), MathUtils.random(110f, 140f) * scale);
        floatingText.color.set(color);
        floatingText.scale = scale;
        floatingText.lifetimeRemaining = 0.75f;
        floatingText.totalLifetime = 0.75f;
        world.floatingTexts.add(floatingText);
    }

    public static void damageNumber(GameWorld world, Hit hit) {
        String amount = String.valueOf(Math.max(1, Math.round(hit.damage)));
        if (hit.target.team == Team.PLAYER) {
            text(world, hit.target.position, amount, PLAYER_DAMAGED_COLOR, 1.1f);
        } else if (hit.isCritical) {
            text(world, hit.target.position, amount, CRIT_COLOR, 1.35f);
        } else {
            text(world, hit.target.position, amount, hit.damageType.color, 0.9f);
        }
    }

    public static void update(GameWorld world, float delta) {
        for (VisualEffect effect : world.effects) {
            effect.lifetimeRemaining -= delta;
        }
        world.effects.removeIf(effect -> effect.lifetimeRemaining <= 0f);
        for (FloatingText floatingText : world.floatingTexts) {
            floatingText.lifetimeRemaining -= delta;
            floatingText.velocity.y -= FLOATING_TEXT_GRAVITY * delta;
            floatingText.position.mulAdd(floatingText.velocity, delta);
        }
        world.floatingTexts.removeIf(floatingText -> floatingText.lifetimeRemaining <= 0f);
        for (Particle particle : world.particles) {
            particle.lifetimeRemaining -= delta;
            particle.velocity.y -= particle.gravity * delta;
            particle.position.mulAdd(particle.velocity, delta);
            particle.velocity.scl(Math.max(0f, 1f - particle.drag * delta));
            particle.rotation += particle.spin * delta;
        }
        world.particles.removeIf(particle -> particle.lifetimeRemaining <= 0f);
        for (Decal decal : world.decals) {
            decal.lifetimeRemaining -= delta;
        }
        world.decals.removeIf(decal -> decal.lifetimeRemaining <= 0f);
        int moteCount = emissionCount(AMBIENT_MOTES_PER_SECOND, delta);
        for (int index = 0; index < moteCount; index++) {
            Vector2 start = new Vector2(
                    MathUtils.random(world.arena.x, world.arena.x + world.arena.width),
                    MathUtils.random(world.arena.y, world.arena.y + world.arena.height));
            Particle mote = createParticle(world, Particle.Style.GLOW, start, MOTE_COLOR, MathUtils.random(1.2f, 2.4f), MathUtils.random(3.5f, 6f));
            if (mote == null) {
                break;
            }
            mote.velocity.set(MathUtils.random(-8f, 8f), MathUtils.random(3f, 12f));
            mote.drag = 0f;
            mote.endSizeScale = 1f;
            mote.fadeInSeconds = 1.5f;
        }
        world.screenShake = Math.max(0f, world.screenShake - SCREEN_SHAKE_DECAY_PER_SECOND * delta);
        world.hurtFlash = Math.max(0f, world.hurtFlash - 2.5f * delta);
        world.screenFlash = Math.max(0f, world.screenFlash - 3f * delta);
    }

    private static VisualEffect create(GameWorld world, VisualEffect.Shape shape, Vector2 position, float radius, Color color, float duration) {
        VisualEffect effect = new VisualEffect();
        effect.shape = shape;
        effect.position.set(position);
        effect.radius = radius;
        effect.color.set(color);
        effect.lifetimeRemaining = duration;
        effect.totalLifetime = duration;
        world.effects.add(effect);
        return effect;
    }

    private static Particle createParticle(GameWorld world, Particle.Style style, Vector2 position, Color color, float size, float lifetime) {
        if (world.particles.size() >= MAXIMUM_PARTICLES) {
            return null;
        }
        Particle particle = new Particle();
        particle.style = style;
        particle.position.set(position);
        particle.color.set(color);
        particle.endColor.set(color.r, color.g, color.b, 0f);
        particle.size = size;
        particle.lifetimeRemaining = lifetime;
        particle.totalLifetime = lifetime;
        world.particles.add(particle);
        return particle;
    }

    private static void createDecal(GameWorld world, Vector2 position, Color color, float width, float height, boolean isSoft) {
        if (world.decals.size() >= MAXIMUM_DECALS) {
            world.decals.remove(0);
        }
        Decal decal = new Decal();
        decal.position.set(position);
        decal.color.set(color);
        decal.width = width;
        decal.height = height;
        decal.rotation = MathUtils.random(360f);
        decal.isSoft = isSoft;
        decal.lifetimeRemaining = DECAL_SECONDS * MathUtils.random(0.8f, 1.2f);
        decal.totalLifetime = decal.lifetimeRemaining;
        world.decals.add(decal);
    }

    private static int emissionCount(float particlesPerSecond, float delta) {
        float expectedCount = particlesPerSecond * delta;
        int wholeCount = (int) expectedCount;
        return MathUtils.random() < expectedCount - wholeCount ? wholeCount + 1 : wholeCount;
    }
}
