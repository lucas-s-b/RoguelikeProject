package roguelike.content;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import roguelike.definitions.ContentRegistry;
import roguelike.definitions.Hooks;
import roguelike.definitions.SkillDefinition;
import roguelike.model.DamageSpec;
import roguelike.model.DamageType;
import roguelike.model.Projectile;
import roguelike.model.SkillTag;
import roguelike.model.Unit;
import roguelike.systems.Attacks;
import roguelike.systems.EffectSystem;
import roguelike.systems.StatusSystem;
import roguelike.systems.Targeting;

public final class SkillContent {
    public static final String FLURRY = "flurry";
    public static final String FIREBALL = "fireball";
    public static final String DASH_STRIKE = "dash_strike";
    public static final String FROST_NOVA = "frost_nova";
    public static final String BLADE_FAN = "blade_fan";
    public static final String WHIRLWIND = "whirlwind";
    public static final String CHAIN_LIGHTNING = "chain_lightning";
    public static final String METEOR = "meteor";
    public static final String POISON_CLOUD = "poison_cloud";
    public static final String BARRIER = "barrier";
    public static final String ARROW_RAIN = "arrow_rain";

    private SkillContent() {
    }

    public static void register() {
        ContentRegistry.add(SkillDefinition.create(FLURRY)
                .name("Flurry")
                .description("Swing 10 times in quick succession. Every swing applies on-hit effects.")
                .price(60)
                .cooldown(7f)
                .damage(6f, DamageType.PHYSICAL)
                .hitCount(10)
                .tags(SkillTag.MELEE, SkillTag.MULTI_HIT)
                .color(0.9f, 0.9f, 1f)
                .action((world, cast) -> {
                    DamageSpec damage = cast.damageSpec();
                    for (int swing = 0; swing < cast.hitCount; swing++) {
                        float angleJitter = MathUtils.random(-20f, 20f);
                        world.schedule(swing * 0.06f, () -> {
                            if (!cast.caster.isDead) {
                                Attacks.meleeArc(world, cast.caster, cast.caster.aimAngle + angleJitter, cast.area(85f), 100f, damage, cast.definition.color);
                            }
                        });
                    }
                }));

        ContentRegistry.add(SkillDefinition.create(FIREBALL)
                .name("Fireball")
                .description("Launch a fireball that explodes, burning everything nearby.")
                .price(70)
                .cooldown(5f)
                .damage(25f, DamageType.FIRE)
                .tags(SkillTag.PROJECTILE, SkillTag.AREA)
                .color(1f, 0.5f, 0.15f)
                .action((world, cast) -> {
                    DamageSpec explosionDamage = cast.damageSpec();
                    Hooks.ProjectileImpact explode = (impactWorld, projectile, unitHit) -> {
                        for (Unit target : Attacks.circleBlast(impactWorld, cast.caster, projectile.position, cast.area(80f), explosionDamage, cast.definition.color)) {
                            StatusSystem.apply(target, StatusContent.BURN, 2, cast.caster);
                        }
                    };
                    float spread = 15f * (cast.projectileCount - 1);
                    for (Projectile fireball : Attacks.fireSpread(world, cast.caster, cast.aimAngle, cast.projectileCount, spread, 480f, cast.damageSpec().scaled(0.4f), cast.definition.color)) {
                        fireball.radius = 10f;
                        fireball.lifetimeRemaining = 1.2f;
                        fireball.onImpact = explode;
                        fireball.onExpire = explode;
                    }
                }));

        ContentRegistry.add(SkillDefinition.create(DASH_STRIKE)
                .name("Dash Strike")
                .description("Dash toward the cursor, striking every enemy you pass through. You can't be hit while dashing.")
                .price(55)
                .cooldown(4f)
                .damage(20f, DamageType.PHYSICAL)
                .tags(SkillTag.MOVEMENT, SkillTag.MELEE)
                .color(0.6f, 0.9f, 1f)
                .action((world, cast) -> {
                    float dashDistance = 230f;
                    Vector2 start = new Vector2(cast.caster.position);
                    Vector2 end = new Vector2(dashDistance, 0f).setAngleDeg(cast.aimAngle).add(start);
                    Attacks.lineStrike(world, cast.caster, start, end, cast.area(44f), cast.damageSpec(), cast.definition.color);
                    Attacks.dash(cast.caster, cast.aimAngle, dashDistance, 0.15f);
                }));

        ContentRegistry.add(SkillDefinition.create(FROST_NOVA)
                .name("Frost Nova")
                .description("Blast the area around you with ice, chilling enemies.")
                .price(65)
                .cooldown(8f)
                .damage(15f, DamageType.ICE)
                .tags(SkillTag.AREA)
                .color(0.55f, 0.88f, 1f)
                .action((world, cast) -> {
                    for (Unit target : Attacks.circleBlast(world, cast.caster, cast.caster.position, cast.area(150f), cast.damageSpec(), cast.definition.color)) {
                        StatusSystem.apply(target, StatusContent.CHILL, 3, cast.caster);
                    }
                }));

        ContentRegistry.add(SkillDefinition.create(BLADE_FAN)
                .name("Blade Fan")
                .description("Throw 7 knives in a wide fan.")
                .price(60)
                .cooldown(5f)
                .damage(9f, DamageType.PHYSICAL)
                .projectileCount(7)
                .tags(SkillTag.PROJECTILE)
                .color(0.85f, 0.85f, 0.9f)
                .action((world, cast) -> {
                    for (Projectile knife : Attacks.fireSpread(world, cast.caster, cast.aimAngle, cast.projectileCount, 70f, 560f, cast.damageSpec(), cast.definition.color)) {
                        knife.radius = 5f;
                        knife.lifetimeRemaining = 0.8f;
                    }
                }));

        ContentRegistry.add(SkillDefinition.create(WHIRLWIND)
                .name("Whirlwind")
                .description("Spin for 2 seconds, hitting all nearby enemies 8 times.")
                .price(80)
                .cooldown(9f)
                .damage(7f, DamageType.PHYSICAL)
                .hitCount(8)
                .tags(SkillTag.MELEE, SkillTag.AREA, SkillTag.MULTI_HIT)
                .color(0.8f, 0.8f, 0.95f)
                .action((world, cast) -> {
                    DamageSpec damage = cast.damageSpec();
                    for (int spin = 0; spin < cast.hitCount; spin++) {
                        world.schedule(spin * 0.25f, () -> {
                            if (!cast.caster.isDead) {
                                Attacks.circleBlast(world, cast.caster, cast.caster.position, cast.area(95f), damage, cast.definition.color);
                            }
                        });
                    }
                }));

        ContentRegistry.add(SkillDefinition.create(CHAIN_LIGHTNING)
                .name("Chain Lightning")
                .description("Strike the enemy nearest the cursor with lightning that jumps to 5 more enemies.")
                .price(75)
                .cooldown(5f)
                .damage(14f, DamageType.LIGHTNING)
                .hitCount(5)
                .tags(SkillTag.CHAIN, SkillTag.MULTI_HIT)
                .color(1f, 0.95f, 0.4f)
                .action((world, cast) -> {
                    Unit firstTarget = Targeting.nearestEnemy(world, cast.caster.team, cast.aimPoint, 250f, null);
                    if (firstTarget == null) {
                        firstTarget = Targeting.nearestEnemy(world, cast.caster.team, cast.caster.position, 450f, null);
                    }
                    if (firstTarget != null) {
                        Attacks.chainLightning(world, cast.caster, firstTarget, cast.hitCount, 220f, cast.damageSpec());
                    }
                }));

        ContentRegistry.add(SkillDefinition.create(METEOR)
                .name("Meteor")
                .description("Call down a meteor at the cursor after a short delay. Huge damage in a large area.")
                .price(90)
                .cooldown(12f)
                .damage(60f, DamageType.FIRE)
                .tags(SkillTag.AREA)
                .color(1f, 0.4f, 0.1f)
                .action((world, cast) -> {
                    Vector2 impactPoint = new Vector2(cast.aimPoint);
                    float radius = cast.area(130f);
                    EffectSystem.telegraphCircle(world, impactPoint, radius, cast.definition.color, 0.9f);
                    world.schedule(0.9f, () -> {
                        for (Unit target : Attacks.circleBlast(world, cast.caster, impactPoint, radius, cast.damageSpec(), cast.definition.color)) {
                            StatusSystem.apply(target, StatusContent.BURN, 3, cast.caster);
                        }
                    });
                }));

        ContentRegistry.add(SkillDefinition.create(POISON_CLOUD)
                .name("Poison Cloud")
                .description("Create a toxic cloud at the cursor that poisons enemies inside it 8 times.")
                .price(70)
                .cooldown(10f)
                .damage(4f, DamageType.POISON)
                .hitCount(8)
                .tags(SkillTag.AREA, SkillTag.MULTI_HIT)
                .color(0.45f, 0.9f, 0.3f)
                .action((world, cast) -> {
                    Vector2 cloudCenter = new Vector2(cast.aimPoint);
                    float radius = cast.area(110f);
                    DamageSpec damage = cast.damageSpec();
                    for (int pulse = 0; pulse < cast.hitCount; pulse++) {
                        world.schedule(pulse * 0.5f, () -> {
                            for (Unit target : Attacks.circleBlast(world, cast.caster, cloudCenter, radius, damage, cast.definition.color)) {
                                StatusSystem.apply(target, StatusContent.POISON, 1, cast.caster);
                            }
                        });
                    }
                }));

        ContentRegistry.add(SkillDefinition.create(BARRIER)
                .name("Barrier")
                .description("Gain a shield that absorbs 25 damage. Stronger with each level.")
                .price(50)
                .cooldown(12f)
                .damage(25f, DamageType.PHYSICAL)
                .tags(SkillTag.DEFENSIVE)
                .color(0.6f, 0.85f, 1f)
                .action((world, cast) -> {
                    cast.caster.shield += cast.damage;
                    EffectSystem.ring(world, cast.caster.position, cast.caster.radius + 14f, cast.definition.color, 0.5f);
                }));

        ContentRegistry.add(SkillDefinition.create(ARROW_RAIN)
                .name("Arrow Rain")
                .description("Rain 12 arrows down around the cursor over 1.5 seconds.")
                .price(75)
                .cooldown(8f)
                .damage(8f, DamageType.PHYSICAL)
                .hitCount(12)
                .tags(SkillTag.AREA, SkillTag.MULTI_HIT)
                .color(0.75f, 0.6f, 0.4f)
                .action((world, cast) -> {
                    Vector2 rainCenter = new Vector2(cast.aimPoint);
                    float rainRadius = cast.area(90f);
                    DamageSpec damage = cast.damageSpec();
                    for (int arrow = 0; arrow < cast.hitCount; arrow++) {
                        world.schedule(arrow * 0.125f, () ->
                                Attacks.circleBlast(world, cast.caster, Attacks.randomPointAround(rainCenter, rainRadius), 28f, damage, cast.definition.color));
                    }
                }));
    }
}
