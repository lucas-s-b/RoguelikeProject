package roguelike.content;

import com.badlogic.gdx.math.Vector2;
import java.util.List;
import roguelike.definitions.ContentRegistry;
import roguelike.definitions.WeaponDefinition;
import roguelike.model.DamageType;
import roguelike.model.Projectile;
import roguelike.model.Stat;
import roguelike.model.Unit;
import roguelike.systems.Attacks;
import roguelike.systems.CombatSystem;
import roguelike.systems.EffectSystem;
import roguelike.systems.StatusSystem;
import roguelike.systems.Targeting;

public final class WeaponContent {
    public static final String RUSTY_SWORD = "rusty_sword";
    public static final String TWIN_DAGGERS = "twin_daggers";
    public static final String HUNTING_BOW = "hunting_bow";
    public static final String APPRENTICE_STAFF = "apprentice_staff";
    public static final String WAR_HAMMER = "war_hammer";
    public static final String THROWING_AXES = "throwing_axes";
    public static final String LONG_SPEAR = "long_spear";
    public static final String STORM_CALLER = "storm_caller";
    public static final String MOON_GLAIVE = "moon_glaive";
    public static final String CINDER_GAUNTLETS = "cinder_gauntlets";
    public static final String GLACIER_CROSSBOW = "glacier_crossbow";
    public static final String REAPER_SCYTHE = "reaper_scythe";
    public static final String SUNBEAM_ROD = "sunbeam_rod";
    public static final String BLOOD_CLEAVER = "blood_cleaver";
    public static final String GRAVITY_ORB = "gravity_orb";
    public static final String VENOM_WHIP = "venom_whip";
    public static final String RICOCHET_DISC = "ricochet_disc";
    private static final float GLAIVE_OUTWARD_SECONDS = 0.4f;
    private static final float GRAVITY_PULSE_INTERVAL = 0.3f;

    private WeaponContent() {
    }

    public static void register() {
        ContentRegistry.add(WeaponDefinition.create(RUSTY_SWORD)
                .name("Rusty Sword")
                .description("A wide slash in front of you.")
                .price(40)
                .cooldown(0.45f)
                .damage(12f, DamageType.PHYSICAL)
                .color(0.8f, 0.75f, 0.7f)
                .attack((world, attacker, damage, aimAngle) ->
                        Attacks.meleeArc(world, attacker, aimAngle, 75f, 110f, damage, damage.damageType.color)));

        ContentRegistry.add(WeaponDefinition.create(TWIN_DAGGERS)
                .name("Twin Daggers")
                .description("Very fast, short stabs. Great with on-hit perks.")
                .price(60)
                .cooldown(0.18f)
                .damage(6f, DamageType.PHYSICAL)
                .color(0.75f, 0.9f, 0.85f)
                .attack((world, attacker, damage, aimAngle) ->
                        Attacks.meleeArc(world, attacker, aimAngle, 55f, 70f, damage, damage.damageType.color)));

        ContentRegistry.add(WeaponDefinition.create(HUNTING_BOW)
                .name("Hunting Bow")
                .description("Fires an arrow that pierces one enemy.")
                .price(70)
                .cooldown(0.55f)
                .damage(14f, DamageType.PHYSICAL)
                .color(0.7f, 0.55f, 0.35f)
                .attack((world, attacker, damage, aimAngle) -> {
                    int arrowCount = 1 + Math.round(attacker.stat(Stat.EXTRA_PROJECTILES));
                    for (Projectile arrow : Attacks.fireSpread(world, attacker, aimAngle, arrowCount, 12f * (arrowCount - 1), 650f, damage, damage.damageType.color)) {
                        arrow.pierceRemaining = 1;
                        arrow.radius = 5f;
                        arrow.lifetimeRemaining = 1.2f;
                    }
                }));

        ContentRegistry.add(WeaponDefinition.create(APPRENTICE_STAFF)
                .name("Apprentice Staff")
                .description("Fires a homing magic bolt.")
                .price(70)
                .cooldown(0.5f)
                .damage(10f, DamageType.MAGIC)
                .color(0.7f, 0.45f, 1f)
                .attack((world, attacker, damage, aimAngle) -> {
                    int boltCount = 1 + Math.round(attacker.stat(Stat.EXTRA_PROJECTILES));
                    for (Projectile bolt : Attacks.fireSpread(world, attacker, aimAngle, boltCount, 20f * (boltCount - 1), 450f, damage, damage.damageType.color)) {
                        bolt.isHoming = true;
                        bolt.radius = 7f;
                        bolt.lifetimeRemaining = 1.6f;
                    }
                }));

        ContentRegistry.add(WeaponDefinition.create(WAR_HAMMER)
                .name("War Hammer")
                .description("A slow, heavy smash that knocks enemies back.")
                .price(80)
                .cooldown(1f)
                .damage(30f, DamageType.PHYSICAL)
                .color(0.6f, 0.6f, 0.65f)
                .attack((world, attacker, damage, aimAngle) -> {
                    Vector2 impactPoint = new Vector2(55f, 0f).setAngleDeg(aimAngle).add(attacker.position);
                    Attacks.circleBlast(world, attacker, impactPoint, 70f, damage.withKnockback(380f), damage.damageType.color);
                }));

        ContentRegistry.add(WeaponDefinition.create(THROWING_AXES)
                .name("Throwing Axes")
                .description("Throws three axes in a spread.")
                .price(75)
                .cooldown(0.6f)
                .damage(8f, DamageType.PHYSICAL)
                .color(0.85f, 0.6f, 0.4f)
                .attack((world, attacker, damage, aimAngle) -> {
                    int axeCount = 3 + Math.round(attacker.stat(Stat.EXTRA_PROJECTILES));
                    for (Projectile axe : Attacks.fireSpread(world, attacker, aimAngle, axeCount, 30f + 6f * (axeCount - 3), 520f, damage, damage.damageType.color)) {
                        axe.radius = 7f;
                        axe.lifetimeRemaining = 0.9f;
                    }
                }));

        ContentRegistry.add(WeaponDefinition.create(LONG_SPEAR)
                .name("Long Spear")
                .description("A long thrust that hits every enemy in a line.")
                .price(65)
                .cooldown(0.6f)
                .damage(16f, DamageType.PHYSICAL)
                .color(0.75f, 0.7f, 0.55f)
                .attack((world, attacker, damage, aimAngle) -> {
                    Vector2 tip = new Vector2(160f, 0f).setAngleDeg(aimAngle).add(attacker.position);
                    Attacks.lineStrike(world, attacker, attacker.position, tip, 22f, damage, damage.damageType.color);
                }));

        ContentRegistry.add(WeaponDefinition.create(STORM_CALLER)
                .name("Storm Caller")
                .description("Fires a bolt that chains lightning to 3 nearby enemies on impact.")
                .price(90)
                .cooldown(0.6f)
                .damage(9f, DamageType.LIGHTNING)
                .color(1f, 0.95f, 0.45f)
                .attack((world, attacker, damage, aimAngle) -> {
                    int boltCount = 1 + Math.round(attacker.stat(Stat.EXTRA_PROJECTILES));
                    for (Projectile bolt : Attacks.fireSpread(world, attacker, aimAngle, boltCount, 16f * (boltCount - 1), 560f, damage, damage.damageType.color)) {
                        bolt.radius = 6f;
                        bolt.lifetimeRemaining = 1.2f;
                        bolt.onImpact = (impactWorld, projectile, unitHit) -> {
                            Unit firstJump = Targeting.nearestEnemy(impactWorld, projectile.team, unitHit.position, 200f, projectile.unitsAlreadyHit);
                            if (firstJump != null) {
                                Attacks.chainLightning(impactWorld, projectile.owner, unitHit.position, firstJump, 2, 200f, damage.scaled(0.6f), projectile.unitsAlreadyHit);
                            }
                        };
                    }
                }));

        ContentRegistry.add(WeaponDefinition.create(MOON_GLAIVE)
                .name("Moon Glaive")
                .description("Throws a spinning blade that flies back to you, hitting enemies both ways.")
                .price(85)
                .cooldown(0.7f)
                .damage(12f, DamageType.MAGIC)
                .color(0.75f, 0.85f, 1f)
                .attack((world, attacker, damage, aimAngle) -> {
                    int glaiveCount = 1 + Math.round(attacker.stat(Stat.EXTRA_PROJECTILES));
                    for (Projectile glaive : Attacks.fireSpread(world, attacker, aimAngle, glaiveCount, 25f * (glaiveCount - 1), 620f, damage, damage.damageType.color)) {
                        glaive.radius = 10f;
                        glaive.pierceRemaining = 999;
                        glaive.lifetimeRemaining = 2.5f;
                        glaive.onUpdate = (updateWorld, projectile, delta) -> {
                            float previousTimer = projectile.timer;
                            projectile.timer += delta;
                            if (projectile.timer < GLAIVE_OUTWARD_SECONDS) {
                                return;
                            }
                            if (previousTimer < GLAIVE_OUTWARD_SECONDS) {
                                projectile.unitsAlreadyHit.clear();
                                EffectSystem.shockwave(updateWorld, projectile.position, 30f, projectile.color, 0.25f);
                            }
                            Vector2 towardOwner = new Vector2(projectile.owner.position).sub(projectile.position);
                            if (towardOwner.len() < projectile.owner.radius + projectile.radius) {
                                projectile.isDestroyed = true;
                                return;
                            }
                            projectile.velocity.set(towardOwner.nor().scl(projectile.velocity.len()));
                        };
                    }
                }));

        ContentRegistry.add(WeaponDefinition.create(CINDER_GAUNTLETS)
                .name("Cinder Gauntlets")
                .description("Rapid punches. Every third punch explodes and burns enemies.")
                .price(90)
                .cooldown(0.22f)
                .damage(6f, DamageType.FIRE)
                .color(1f, 0.5f, 0.2f)
                .attack((world, attacker, damage, aimAngle) -> {
                    Attacks.meleeArc(world, attacker, aimAngle, 62f, 60f, damage, damage.damageType.color);
                    if (world.run.weapon.attackCount % 3 != 0) {
                        return;
                    }
                    Vector2 explosionCenter = new Vector2(55f, 0f).setAngleDeg(aimAngle).add(attacker.position);
                    for (Unit burned : Attacks.circleBlast(world, attacker, explosionCenter, 75f, damage.scaled(1.5f), damage.damageType.color)) {
                        StatusSystem.apply(burned, StatusContent.BURN, 2, attacker);
                    }
                }));

        ContentRegistry.add(WeaponDefinition.create(GLACIER_CROSSBOW)
                .name("Glacier Crossbow")
                .description("Fires a heavy bolt that shatters into 6 ice shards that chill enemies.")
                .price(95)
                .cooldown(0.8f)
                .damage(16f, DamageType.ICE)
                .color(0.6f, 0.9f, 1f)
                .attack((world, attacker, damage, aimAngle) -> {
                    int boltCount = 1 + Math.round(attacker.stat(Stat.EXTRA_PROJECTILES));
                    for (Projectile bolt : Attacks.fireSpread(world, attacker, aimAngle, boltCount, 14f * (boltCount - 1), 600f, damage, damage.damageType.color)) {
                        bolt.radius = 8f;
                        bolt.lifetimeRemaining = 1.2f;
                        bolt.onImpact = (impactWorld, projectile, unitHit) -> {
                            StatusSystem.apply(unitHit, StatusContent.CHILL, 1, projectile.owner);
                            EffectSystem.burst(impactWorld, projectile.position, projectile.color, 14, 260f, 5f, 0.4f);
                            float shardAngle = projectile.velocity.angleDeg();
                            for (Projectile shard : Attacks.fireSpread(impactWorld, projectile.owner, shardAngle, 6, 300f, 420f, damage.scaled(0.4f), projectile.color)) {
                                shard.position.set(projectile.position);
                                shard.radius = 4f;
                                shard.lifetimeRemaining = 0.45f;
                                shard.unitsAlreadyHit.add(unitHit);
                                shard.onImpact = (shardWorld, shardProjectile, shardTarget) -> StatusSystem.apply(shardTarget, StatusContent.CHILL, 1, shardProjectile.owner);
                            }
                        };
                    }
                }));

        ContentRegistry.add(WeaponDefinition.create(REAPER_SCYTHE)
                .name("Reaper Scythe")
                .description("A wide sweep that pulls enemies toward you and heals 1 HP per enemy hit.")
                .price(100)
                .cooldown(0.75f)
                .damage(15f, DamageType.MAGIC)
                .color(0.55f, 0.35f, 0.75f)
                .attack((world, attacker, damage, aimAngle) -> {
                    List<Unit> reaped = Attacks.meleeArc(world, attacker, aimAngle, 125f, 200f, damage, damage.damageType.color);
                    for (Unit target : reaped) {
                        if (!target.isBoss) {
                            target.knockbackVelocity.mulAdd(new Vector2(attacker.position).sub(target.position).nor(), 320f);
                        }
                        EffectSystem.trail(world, target.position, damage.damageType.color, 14f, 0.4f);
                    }
                    CombatSystem.heal(world, attacker, reaped.size());
                }));

        ContentRegistry.add(WeaponDefinition.create(SUNBEAM_ROD)
                .name("Sunbeam Rod")
                .description("Fires a long beam of light that pierces every enemy and burns them.")
                .price(95)
                .cooldown(0.7f)
                .damage(11f, DamageType.FIRE)
                .color(1f, 0.85f, 0.4f)
                .attack((world, attacker, damage, aimAngle) -> {
                    Vector2 beamEnd = new Vector2(420f, 0f).setAngleDeg(aimAngle).add(attacker.position);
                    for (Unit burned : Attacks.lineStrike(world, attacker, attacker.position, beamEnd, 18f, damage, damage.damageType.color)) {
                        StatusSystem.apply(burned, StatusContent.BURN, 1, attacker);
                    }
                    EffectSystem.sparks(world, attacker.position, aimAngle, 40f, damage.damageType.color, 8, 360f);
                }));

        ContentRegistry.add(WeaponDefinition.create(BLOOD_CLEAVER)
                .name("Blood Cleaver")
                .description("A heavy cleave that deals up to double damage the lower your health is.")
                .price(85)
                .cooldown(0.8f)
                .damage(20f, DamageType.BLEED)
                .color(0.85f, 0.15f, 0.2f)
                .attack((world, attacker, damage, aimAngle) -> {
                    float missingHealthFraction = 1f - Math.max(0f, attacker.healthFraction());
                    List<Unit> cleaved = Attacks.meleeArc(world, attacker, aimAngle, 100f, 140f, damage.scaled(1f + missingHealthFraction), damage.damageType.color);
                    for (Unit target : cleaved) {
                        EffectSystem.burst(world, target.position, damage.damageType.color, 6 + Math.round(missingHealthFraction * 14f), 220f, 4f, 0.5f);
                    }
                }));

        ContentRegistry.add(WeaponDefinition.create(GRAVITY_ORB)
                .name("Gravity Orb")
                .description("Launches a slow orb that passes through enemies and pulses damage around it.")
                .price(110)
                .cooldown(1.1f)
                .damage(6f, DamageType.MAGIC)
                .color(0.6f, 0.4f, 1f)
                .attack((world, attacker, damage, aimAngle) -> {
                    int orbCount = 1 + Math.round(attacker.stat(Stat.EXTRA_PROJECTILES));
                    for (Projectile orb : Attacks.fireSpread(world, attacker, aimAngle, orbCount, 30f * (orbCount - 1), 150f, damage, damage.damageType.color)) {
                        orb.radius = 14f;
                        orb.pierceRemaining = 999;
                        orb.lifetimeRemaining = 2.6f;
                        orb.onUpdate = (updateWorld, projectile, delta) -> {
                            projectile.timer += delta;
                            if (projectile.timer < GRAVITY_PULSE_INTERVAL) {
                                return;
                            }
                            projectile.timer -= GRAVITY_PULSE_INTERVAL;
                            for (Unit target : Targeting.enemiesInRadius(updateWorld, projectile.team, projectile.position, 85f)) {
                                Attacks.hitUnit(updateWorld, projectile.owner, target, damage);
                            }
                            EffectSystem.shockwave(updateWorld, projectile.position, 85f, projectile.color, 0.3f);
                        };
                    }
                }));

        ContentRegistry.add(WeaponDefinition.create(VENOM_WHIP)
                .name("Venom Whip")
                .description("A long lash that poisons every enemy it hits.")
                .price(75)
                .cooldown(0.5f)
                .damage(9f, DamageType.POISON)
                .color(0.5f, 0.95f, 0.35f)
                .attack((world, attacker, damage, aimAngle) -> {
                    for (Unit poisoned : Attacks.meleeArc(world, attacker, aimAngle, 190f, 35f, damage, damage.damageType.color)) {
                        StatusSystem.apply(poisoned, StatusContent.POISON, 1, attacker);
                    }
                    Vector2 whipTip = new Vector2(190f, 0f).setAngleDeg(aimAngle).add(attacker.position);
                    EffectSystem.lightning(world, attacker.position, whipTip, damage.damageType.color, 4f, 0.15f);
                }));

        ContentRegistry.add(WeaponDefinition.create(RICOCHET_DISC)
                .name("Ricochet Disc")
                .description("Throws a disc that bounces between up to 4 enemies.")
                .price(80)
                .cooldown(0.55f)
                .damage(10f, DamageType.PHYSICAL)
                .color(0.85f, 0.9f, 0.95f)
                .attack((world, attacker, damage, aimAngle) -> {
                    int discCount = 1 + Math.round(attacker.stat(Stat.EXTRA_PROJECTILES));
                    for (Projectile disc : Attacks.fireSpread(world, attacker, aimAngle, discCount, 20f * (discCount - 1), 600f, damage, damage.damageType.color)) {
                        disc.radius = 7f;
                        disc.pierceRemaining = 3;
                        disc.lifetimeRemaining = 1f;
                        disc.onImpact = (impactWorld, projectile, unitHit) -> {
                            Unit nextTarget = Targeting.nearestEnemy(impactWorld, projectile.team, projectile.position, 320f, projectile.unitsAlreadyHit);
                            if (nextTarget == null) {
                                projectile.pierceRemaining = 0;
                                return;
                            }
                            float speed = projectile.velocity.len();
                            projectile.velocity.set(nextTarget.position).sub(projectile.position).nor().scl(speed);
                            projectile.lifetimeRemaining = 0.8f;
                            EffectSystem.sparks(impactWorld, projectile.position, projectile.velocity.angleDeg(), 60f, projectile.color, 5, 300f);
                        };
                    }
                }));
    }
}
