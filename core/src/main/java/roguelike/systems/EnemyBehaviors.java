package roguelike.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import roguelike.definitions.ContentRegistry;
import roguelike.definitions.Hooks;
import roguelike.model.DamageSpec;
import roguelike.model.GameWorld;
import roguelike.model.Projectile;
import roguelike.model.Team;
import roguelike.model.Unit;

public final class EnemyBehaviors {
    private static final Color TELEGRAPH_COLOR = new Color(1f, 0.2f, 0.2f, 1f);
    private static final Color HEAL_BEAM_COLOR = new Color(0.45f, 1f, 0.5f, 1f);
    private static final Color WEB_COLOR = new Color(0.85f, 0.92f, 0.8f, 1f);
    private static final Color ACID_COLOR = new Color(0.6f, 0.95f, 0.3f, 1f);
    private static final Color VOID_COLOR = new Color(0.55f, 0.4f, 1f, 1f);
    private static final float BEAM_LENGTH = 1400f;

    private EnemyBehaviors() {
    }

    public static Hooks.EnemyBehavior chase() {
        return (world, enemy, delta) -> enemy.moveDirection.set(Navigation.towardPlayer(world, enemy));
    }

    public static Hooks.EnemyBehavior erraticChase(float wobbleStrength) {
        return (world, enemy, delta) -> {
            Vector2 towardPlayer = Navigation.towardPlayer(world, enemy);
            float wobble = MathUtils.sin(world.time * 7f + enemy.hashCode() % 97) * wobbleStrength;
            enemy.moveDirection.set(towardPlayer).add(-towardPlayer.y * wobble, towardPlayer.x * wobble).nor();
        };
    }

    public static Hooks.EnemyBehavior rangedKite(float preferredDistance, float projectileSpeed, float secondsBetweenShots) {
        return (world, enemy, delta) -> {
            Vector2 towardPlayer = new Vector2(world.player().position).sub(enemy.position);
            float distance = towardPlayer.len();
            towardPlayer.nor();
            enemy.behaviorTimer += delta;
            if (enemy.behaviorTimer > 2.5f) {
                enemy.behaviorTimer = 0f;
                enemy.behaviorPhase = 1 - enemy.behaviorPhase;
            }
            boolean canSeePlayer = Navigation.hasLineOfSight(world, enemy.position, world.player().position);
            if (distance > preferredDistance + 60f || !canSeePlayer) {
                enemy.moveDirection.set(Navigation.towardPlayer(world, enemy));
            } else if (distance < preferredDistance - 60f) {
                enemy.moveDirection.set(towardPlayer).scl(-1f);
            } else {
                float strafeSide = enemy.behaviorPhase == 0 ? 1f : -1f;
                enemy.moveDirection.set(-towardPlayer.y * strafeSide, towardPlayer.x * strafeSide).scl(0.6f);
            }
            if (enemy.attackCooldownRemaining <= 0f && distance < preferredDistance + 250f && canSeePlayer) {
                enemy.attackCooldownRemaining = secondsBetweenShots;
                AnimationSystem.attackPose(enemy, towardPlayer.angleDeg());
                Projectile projectile = Attacks.fireProjectile(world, enemy, towardPlayer.angleDeg(), projectileSpeed, DamageSpec.enemy(enemy.attackDamage), enemy.color);
                projectile.radius = 7f;
                projectile.lifetimeRemaining = 3f;
            }
        };
    }

    public static Hooks.EnemyBehavior charger(float chargeRange, float windupSeconds, float chargeSpeedMultiplier, float chargeSeconds) {
        return (world, enemy, delta) -> {
            Unit player = world.player();
            enemy.behaviorTimer -= delta;
            if (enemy.behaviorPhase == 0) {
                enemy.speedMultiplier = 1f;
                enemy.moveDirection.set(Navigation.towardPlayer(world, enemy));
                boolean canSeePlayer = Navigation.hasLineOfSight(world, enemy.position, player.position);
                if (enemy.behaviorTimer <= 0f && enemy.position.dst(player.position) < chargeRange && canSeePlayer) {
                    enemy.behaviorPhase = 1;
                    enemy.behaviorTimer = windupSeconds;
                    enemy.aimAngle = Targeting.angleBetween(enemy.position, player.position);
                    Vector2 telegraphEnd = Navigation.clipToWalls(world, enemy.position, new Vector2(chargeRange * 1.2f, 0f).setAngleDeg(enemy.aimAngle).add(enemy.position));
                    EffectSystem.telegraphLine(world, enemy.position, telegraphEnd, enemy.radius * 2f, TELEGRAPH_COLOR, windupSeconds);
                    enemy.windupTimeRemaining = windupSeconds;
                }
            } else if (enemy.behaviorPhase == 1) {
                enemy.moveDirection.setZero();
                if (enemy.behaviorTimer <= 0f) {
                    enemy.behaviorPhase = 2;
                    enemy.behaviorTimer = chargeSeconds;
                    enemy.speedMultiplier = chargeSpeedMultiplier;
                    enemy.moveDirection.set(1f, 0f).setAngleDeg(enemy.aimAngle);
                }
            } else if (enemy.behaviorTimer <= 0f) {
                enemy.behaviorPhase = 0;
                enemy.behaviorTimer = 2.5f;
                enemy.speedMultiplier = 1f;
            } else {
                EffectSystem.afterimage(world, enemy);
            }
        };
    }

    public static Hooks.EnemyBehavior bomber(float triggerDistance, float fuseSeconds, float explosionRadius) {
        return (world, enemy, delta) -> {
            Unit player = world.player();
            if (enemy.behaviorPhase == 0) {
                enemy.moveDirection.set(Navigation.towardPlayer(world, enemy));
                if (enemy.position.dst(player.position) < triggerDistance) {
                    enemy.behaviorPhase = 1;
                    enemy.behaviorTimer = fuseSeconds;
                    EffectSystem.telegraphCircle(world, enemy.position, explosionRadius, TELEGRAPH_COLOR, fuseSeconds);
                    enemy.windupTimeRemaining = fuseSeconds;
                }
                return;
            }
            enemy.moveDirection.setZero();
            enemy.behaviorTimer -= delta;
            if (enemy.behaviorTimer <= 0f) {
                Attacks.circleBlast(world, enemy, enemy.position, explosionRadius, DamageSpec.enemy(enemy.attackDamage).withKnockback(350f), enemy.color);
                CombatSystem.kill(world, enemy, null);
            }
        };
    }

    public static Hooks.EnemyBehavior bossWarden(String minionEnemyId) {
        return (world, boss, delta) -> {
            Unit player = world.player();
            boss.moveDirection.set(Navigation.towardPlayer(world, boss)).scl(0.5f);
            boss.behaviorTimer -= delta;
            if (boss.behaviorTimer > 0f) {
                return;
            }
            boolean isEnraged = boss.healthFraction() < 0.5f;
            boss.behaviorTimer = isEnraged ? 1.8f : 2.6f;
            int pattern = boss.behaviorPhase % 4;
            boss.behaviorPhase++;
            DamageSpec damage = DamageSpec.enemy(boss.attackDamage);
            if (pattern == 0) {
                int projectileCount = isEnraged ? 24 : 16;
                float angleOffset = MathUtils.random(360f);
                EffectSystem.shockwave(world, boss.position, boss.radius * 2.2f, boss.color, 0.35f);
                for (int index = 0; index < projectileCount; index++) {
                    Attacks.fireProjectile(world, boss, angleOffset + index * 360f / projectileCount, 220f, damage, boss.color).radius = 8f;
                }
            } else if (pattern == 1) {
                for (int volley = 0; volley < 3; volley++) {
                    world.schedule(volley * 0.3f, () -> {
                        if (!boss.isDead && !player.isDead) {
                            float angleToPlayer = Targeting.angleBetween(boss.position, player.position);
                            AnimationSystem.attackPose(boss, angleToPlayer);
                            Attacks.fireSpread(world, boss, angleToPlayer, 3, 30f, 340f, damage, boss.color);
                        }
                    });
                }
            } else if (pattern == 2) {
                for (int index = 0; index < 2; index++) {
                    EnemySystem.spawn(world, ContentRegistry.enemy(minionEnemyId), Attacks.randomPointAround(boss.position, 90f));
                }
            } else {
                Vector2 slamPoint = new Vector2(player.position);
                EffectSystem.telegraphCircle(world, slamPoint, 110f, TELEGRAPH_COLOR, 0.9f);
                boss.windupTimeRemaining = 0.9f;
                world.schedule(0.9f, () -> {
                    if (!boss.isDead) {
                        Attacks.circleBlast(world, boss, slamPoint, 110f, damage.scaled(1.5f), boss.color);
                    }
                });
            }
        };
    }

    public static Hooks.EnemyBehavior turret(float secondsBetweenVolleys, int projectileCount, float spreadDegrees, float projectileSpeed, String statusId, int statusStacks) {
        return (world, enemy, delta) -> {
            Unit player = world.player();
            enemy.moveDirection.setZero();
            if (enemy.attackCooldownRemaining > 0f || !Navigation.hasLineOfSight(world, enemy.position, player.position)) {
                return;
            }
            enemy.attackCooldownRemaining = secondsBetweenVolleys;
            float angleToPlayer = Targeting.angleBetween(enemy.position, player.position);
            AnimationSystem.attackPose(enemy, angleToPlayer);
            for (Projectile spore : Attacks.fireSpread(world, enemy, angleToPlayer, projectileCount, spreadDegrees, projectileSpeed, DamageSpec.enemy(enemy.attackDamage), enemy.color)) {
                spore.radius = 7f;
                spore.lifetimeRemaining = 3f;
                spore.onImpact = (impactWorld, projectile, unitHit) -> StatusSystem.apply(unitHit, statusId, statusStacks, enemy);
            }
        };
    }

    public static Hooks.EnemyBehavior leaper(float leapRange, float windupSeconds, float leapSeconds, float landingRadius, float secondsBetweenLeaps) {
        return (world, enemy, delta) -> {
            Unit player = world.player();
            if (enemy.behaviorPhase == 1) {
                enemy.moveDirection.setZero();
                return;
            }
            enemy.moveDirection.set(Navigation.towardPlayer(world, enemy));
            boolean canSeePlayer = Navigation.hasLineOfSight(world, enemy.position, player.position);
            if (enemy.attackCooldownRemaining > 0f || enemy.position.dst(player.position) > leapRange || !canSeePlayer) {
                return;
            }
            enemy.behaviorPhase = 1;
            enemy.windupTimeRemaining = windupSeconds;
            Vector2 landingPoint = new Vector2(player.position);
            EffectSystem.telegraphCircle(world, landingPoint, landingRadius, TELEGRAPH_COLOR, windupSeconds + leapSeconds);
            world.schedule(windupSeconds, () -> {
                if (enemy.isDead) {
                    return;
                }
                Attacks.dash(enemy, Targeting.angleBetween(enemy.position, landingPoint), enemy.position.dst(landingPoint), leapSeconds);
                world.schedule(leapSeconds, () -> {
                    enemy.behaviorPhase = 0;
                    enemy.attackCooldownRemaining = secondsBetweenLeaps;
                    if (!enemy.isDead) {
                        Attacks.circleBlast(world, enemy, landingPoint, landingRadius, DamageSpec.enemy(enemy.attackDamage), enemy.color);
                    }
                });
            });
        };
    }

    public static Hooks.EnemyBehavior slowTurner(float turnDegreesPerSecond) {
        return (world, enemy, delta) -> {
            float angleToPlayer = Navigation.towardPlayer(world, enemy).angleDeg();
            float maximumTurn = turnDegreesPerSecond * delta;
            enemy.aimAngle += MathUtils.clamp(Targeting.angleDifference(enemy.aimAngle, angleToPlayer), -maximumTurn, maximumTurn);
            enemy.moveDirection.set(1f, 0f).setAngleDeg(enemy.aimAngle);
        };
    }

    public static Hooks.EnemyBehavior blinkCaster(float secondsBetweenBlinks, float blinkDistance, float castDelaySeconds, float fireballSpeed, String statusId) {
        return (world, enemy, delta) -> {
            Unit player = world.player();
            Vector2 towardPlayer = new Vector2(player.position).sub(enemy.position).nor();
            enemy.moveDirection.set(-towardPlayer.y, towardPlayer.x).scl(0.5f);
            if (enemy.attackCooldownRemaining > 0f) {
                return;
            }
            enemy.attackCooldownRemaining = secondsBetweenBlinks;
            EffectSystem.burst(world, enemy.position, enemy.color, 14, 180f, 4f, 0.35f);
            enemy.position.set(blinkDistance, 0f).setAngleDeg(MathUtils.random(360f)).add(player.position);
            EffectSystem.burst(world, enemy.position, enemy.color, 14, 180f, 4f, 0.35f);
            EffectSystem.shockwave(world, enemy.position, enemy.radius * 3f, enemy.color, 0.3f);
            enemy.windupTimeRemaining = castDelaySeconds;
            world.schedule(castDelaySeconds, () -> {
                if (enemy.isDead || player.isDead) {
                    return;
                }
                float angleToPlayer = Targeting.angleBetween(enemy.position, player.position);
                AnimationSystem.attackPose(enemy, angleToPlayer);
                Projectile fireball = Attacks.fireProjectile(world, enemy, angleToPlayer, fireballSpeed, DamageSpec.enemy(enemy.attackDamage), enemy.color);
                fireball.radius = 8f;
                fireball.lifetimeRemaining = 2.5f;
                fireball.onImpact = (impactWorld, projectile, unitHit) -> StatusSystem.apply(unitHit, statusId, 2, enemy);
            });
        };
    }

    public static Hooks.EnemyBehavior mortar(float secondsBetweenShells, float shellSeconds, float blastRadius) {
        return (world, enemy, delta) -> {
            Unit player = world.player();
            boolean isTooClose = enemy.position.dst(player.position) < 260f;
            Vector2 awayFromPlayer = new Vector2(enemy.position).sub(player.position).nor();
            enemy.moveDirection.set(isTooClose ? awayFromPlayer : Navigation.towardPlayer(world, enemy).scl(0.3f));
            if (enemy.attackCooldownRemaining > 0f) {
                return;
            }
            enemy.attackCooldownRemaining = secondsBetweenShells;
            Vector2 impactPoint = new Vector2(player.position).mulAdd(player.velocity, shellSeconds * 0.4f);
            float angleToImpact = Targeting.angleBetween(enemy.position, impactPoint);
            AnimationSystem.attackPose(enemy, angleToImpact);
            EffectSystem.muzzleFlash(world, enemy.position, angleToImpact, enemy.color);
            EffectSystem.telegraphCircle(world, impactPoint, blastRadius, TELEGRAPH_COLOR, shellSeconds);
            world.schedule(shellSeconds, () -> {
                if (!enemy.isDead) {
                    Attacks.circleBlast(world, enemy, impactPoint, blastRadius, DamageSpec.enemy(enemy.attackDamage), enemy.color);
                }
            });
        };
    }

    public static Hooks.EnemyBehavior orbCaster(float preferredDistance, float secondsBetweenOrbs, float orbSpeed) {
        return (world, enemy, delta) -> {
            Unit player = world.player();
            Vector2 towardPlayer = new Vector2(player.position).sub(enemy.position);
            float distance = towardPlayer.len();
            towardPlayer.nor();
            float wobble = MathUtils.sin(world.time * 3f + enemy.hashCode() % 97);
            boolean canSeePlayer = Navigation.hasLineOfSight(world, enemy.position, player.position);
            Vector2 approachDirection = distance > preferredDistance || !canSeePlayer ? Navigation.towardPlayer(world, enemy) : new Vector2(towardPlayer).scl(-0.7f);
            enemy.moveDirection.set(approachDirection).add(-towardPlayer.y * wobble, towardPlayer.x * wobble).nor();
            if (enemy.attackCooldownRemaining > 0f || !canSeePlayer) {
                return;
            }
            enemy.attackCooldownRemaining = secondsBetweenOrbs;
            Projectile orb = Attacks.fireProjectile(world, enemy, Targeting.angleBetween(enemy.position, player.position), orbSpeed, DamageSpec.enemy(enemy.attackDamage), enemy.color);
            orb.radius = 9f;
            orb.lifetimeRemaining = 4f;
            orb.isHoming = true;
            orb.homingStrength = 1.6f;
        };
    }

    public static Hooks.EnemyBehavior healer(float preferredDistance, float secondsBetweenHeals, float healRadius, float healFraction) {
        return (world, enemy, delta) -> {
            Unit player = world.player();
            Vector2 towardPlayer = new Vector2(player.position).sub(enemy.position);
            float distance = towardPlayer.len();
            enemy.moveDirection.set(distance < preferredDistance ? towardPlayer.nor().scl(-1f) : Navigation.towardPlayer(world, enemy).scl(0.4f));
            if (enemy.attackCooldownRemaining > 0f) {
                return;
            }
            enemy.attackCooldownRemaining = secondsBetweenHeals;
            enemy.windupTimeRemaining = 0.3f;
            EffectSystem.ring(world, enemy.position, healRadius, HEAL_BEAM_COLOR, 0.5f);
            for (Unit ally : Targeting.enemiesInRadius(world, Team.PLAYER, enemy.position, healRadius)) {
                if (ally != enemy && ally.health < ally.maxHealth()) {
                    EffectSystem.line(world, enemy.position, ally.position, 4f, HEAL_BEAM_COLOR, 0.35f);
                    CombatSystem.heal(world, ally, ally.maxHealth() * healFraction);
                }
            }
        };
    }

    public static Hooks.EnemyBehavior sniper(float preferredDistance, float secondsBetweenShots, float aimSeconds, float beamWidth) {
        return (world, enemy, delta) -> {
            Unit player = world.player();
            if (enemy.behaviorPhase == 1) {
                enemy.moveDirection.setZero();
                return;
            }
            Vector2 towardPlayer = new Vector2(player.position).sub(enemy.position);
            float distance = towardPlayer.len();
            boolean canSeePlayer = Navigation.hasLineOfSight(world, enemy.position, player.position);
            boolean isTooClose = distance < preferredDistance && canSeePlayer;
            enemy.moveDirection.set(isTooClose ? new Vector2(towardPlayer).nor().scl(-0.8f) : Navigation.towardPlayer(world, enemy).scl(0.5f));
            if (enemy.attackCooldownRemaining > 0f || !canSeePlayer) {
                return;
            }
            enemy.attackCooldownRemaining = secondsBetweenShots;
            enemy.behaviorPhase = 1;
            enemy.windupTimeRemaining = aimSeconds;
            float angleToPlayer = towardPlayer.angleDeg();
            AnimationSystem.attackPose(enemy, angleToPlayer);
            Vector2 beamStart = new Vector2(enemy.position);
            Vector2 beamEnd = Navigation.clipToWalls(world, beamStart, new Vector2(BEAM_LENGTH, 0f).setAngleDeg(angleToPlayer).add(beamStart));
            EffectSystem.telegraphLine(world, beamStart, beamEnd, beamWidth, TELEGRAPH_COLOR, aimSeconds);
            world.schedule(aimSeconds, () -> {
                enemy.behaviorPhase = 0;
                if (!enemy.isDead) {
                    Attacks.lineStrike(world, enemy, beamStart, beamEnd, beamWidth, DamageSpec.enemy(enemy.attackDamage), enemy.color);
                }
            });
        };
    }

    public static Hooks.EnemyBehavior bossBroodmother(String minionEnemyId, String webStatusId) {
        return (world, boss, delta) -> {
            Unit player = world.player();
            boss.moveDirection.set(Navigation.towardPlayer(world, boss)).scl(0.6f);
            boss.behaviorTimer -= delta;
            if (boss.behaviorTimer > 0f) {
                return;
            }
            boolean isEnraged = boss.healthFraction() < 0.5f;
            boss.behaviorTimer = isEnraged ? 1.8f : 2.6f;
            int pattern = boss.behaviorPhase % 4;
            boss.behaviorPhase++;
            DamageSpec damage = DamageSpec.enemy(boss.attackDamage);
            if (pattern == 0) {
                float angleToPlayer = Targeting.angleBetween(boss.position, player.position);
                AnimationSystem.attackPose(boss, angleToPlayer);
                for (Projectile web : Attacks.fireSpread(world, boss, angleToPlayer, isEnraged ? 11 : 7, 80f, 200f, damage, WEB_COLOR)) {
                    web.radius = 9f;
                    web.lifetimeRemaining = 3.5f;
                    web.onImpact = (impactWorld, projectile, unitHit) -> StatusSystem.apply(unitHit, webStatusId, 3, boss);
                }
            } else if (pattern == 1) {
                Vector2 landingPoint = new Vector2(player.position);
                EffectSystem.telegraphCircle(world, landingPoint, 130f, TELEGRAPH_COLOR, 1.1f);
                boss.windupTimeRemaining = 0.7f;
                world.schedule(0.7f, () -> {
                    if (boss.isDead) {
                        return;
                    }
                    Attacks.dash(boss, Targeting.angleBetween(boss.position, landingPoint), boss.position.dst(landingPoint), 0.4f);
                    world.schedule(0.4f, () -> {
                        if (!boss.isDead) {
                            Attacks.circleBlast(world, boss, landingPoint, 130f, damage.scaled(1.6f), boss.color);
                            EffectSystem.shake(world, 0.5f);
                        }
                    });
                });
            } else if (pattern == 2) {
                for (int index = 0; index < (isEnraged ? 4 : 3); index++) {
                    EnemySystem.spawn(world, ContentRegistry.enemy(minionEnemyId), Attacks.randomPointAround(boss.position, 100f));
                }
            } else {
                for (int index = 0; index < (isEnraged ? 8 : 5); index++) {
                    Vector2 impactPoint = Attacks.randomPointAround(player.position, 220f);
                    float delay = 1f + index * 0.12f;
                    EffectSystem.telegraphCircle(world, impactPoint, 60f, TELEGRAPH_COLOR, delay);
                    world.schedule(delay, () -> {
                        if (!boss.isDead) {
                            Attacks.circleBlast(world, boss, impactPoint, 60f, damage, ACID_COLOR);
                        }
                    });
                }
            }
        };
    }

    public static Hooks.EnemyBehavior bossColossus(String minionEnemyId, String burnStatusId) {
        return (world, boss, delta) -> {
            Unit player = world.player();
            boss.moveDirection.set(Navigation.towardPlayer(world, boss)).scl(0.45f);
            boss.behaviorTimer -= delta;
            if (boss.behaviorTimer > 0f) {
                return;
            }
            boolean isEnraged = boss.healthFraction() < 0.5f;
            boss.behaviorTimer = isEnraged ? 2f : 2.8f;
            int pattern = boss.behaviorPhase % 4;
            boss.behaviorPhase++;
            DamageSpec damage = DamageSpec.enemy(boss.attackDamage);
            if (pattern == 0) {
                int projectileCount = isEnraged ? 28 : 20;
                for (int wave = 0; wave < 2; wave++) {
                    float waveOffset = wave * 180f / projectileCount;
                    world.schedule(wave * 0.45f, () -> {
                        if (boss.isDead) {
                            return;
                        }
                        EffectSystem.shockwave(world, boss.position, boss.radius * 2.4f, boss.color, 0.35f);
                        for (int index = 0; index < projectileCount; index++) {
                            Projectile flame = Attacks.fireProjectile(world, boss, waveOffset + index * 360f / projectileCount, 210f, damage, boss.color);
                            flame.radius = 8f;
                            flame.onImpact = (impactWorld, projectile, unitHit) -> StatusSystem.apply(unitHit, burnStatusId, 1, boss);
                        }
                    });
                }
            } else if (pattern == 1) {
                int beamCount = isEnraged ? 5 : 3;
                float angleToPlayer = Targeting.angleBetween(boss.position, player.position);
                boss.windupTimeRemaining = 1.1f;
                for (int beam = 0; beam < beamCount; beam++) {
                    Vector2 beamStart = new Vector2(boss.position);
                    Vector2 beamEnd = Navigation.clipToWalls(world, beamStart, new Vector2(BEAM_LENGTH, 0f).setAngleDeg(angleToPlayer + (beam - (beamCount - 1) / 2f) * 28f).add(beamStart));
                    EffectSystem.telegraphLine(world, beamStart, beamEnd, 36f, TELEGRAPH_COLOR, 1.1f);
                    world.schedule(1.1f, () -> {
                        if (!boss.isDead) {
                            Attacks.lineStrike(world, boss, beamStart, beamEnd, 36f, damage.scaled(1.4f), boss.color);
                        }
                    });
                }
            } else if (pattern == 2) {
                for (int shell = 0; shell < (isEnraged ? 9 : 6); shell++) {
                    Vector2 impactPoint = Attacks.randomPointAround(player.position, 240f);
                    float delay = 1.1f + shell * 0.1f;
                    EffectSystem.telegraphCircle(world, impactPoint, 80f, TELEGRAPH_COLOR, delay);
                    world.schedule(delay, () -> {
                        if (!boss.isDead) {
                            Attacks.circleBlast(world, boss, impactPoint, 80f, damage, boss.color);
                        }
                    });
                }
            } else {
                for (int index = 0; index < (isEnraged ? 3 : 2); index++) {
                    EnemySystem.spawn(world, ContentRegistry.enemy(minionEnemyId), Attacks.randomPointAround(boss.position, 110f));
                }
            }
        };
    }

    public static Hooks.EnemyBehavior bossHollowKing(String minionEnemyId) {
        return (world, boss, delta) -> {
            Unit player = world.player();
            boss.moveDirection.set(Navigation.towardPlayer(world, boss)).scl(0.4f);
            boss.behaviorTimer -= delta;
            if (boss.behaviorTimer > 0f) {
                return;
            }
            boolean isEnraged = boss.healthFraction() < 0.5f;
            boss.behaviorTimer = isEnraged ? 2.2f : 3f;
            int pattern = boss.behaviorPhase % 4;
            boss.behaviorPhase++;
            DamageSpec damage = DamageSpec.enemy(boss.attackDamage);
            if (pattern == 0) {
                int armCount = isEnraged ? 4 : 3;
                float startAngle = MathUtils.random(360f);
                for (int shot = 0; shot < 18; shot++) {
                    float shotAngle = startAngle + shot * 14f;
                    world.schedule(shot * 0.09f, () -> {
                        if (boss.isDead) {
                            return;
                        }
                        for (int arm = 0; arm < armCount; arm++) {
                            Attacks.fireProjectile(world, boss, shotAngle + arm * 360f / armCount, 230f, damage, VOID_COLOR).radius = 7f;
                        }
                    });
                }
            } else if (pattern == 1) {
                EffectSystem.burst(world, boss.position, VOID_COLOR, 30, 260f, 6f, 0.5f);
                Vector2 destination = new Vector2();
                for (int attempt = 0; attempt < 12; attempt++) {
                    destination.set(
                            MathUtils.random(world.arena.x + 100f, world.arena.x + world.arena.width - 100f),
                            MathUtils.random(world.arena.y + 100f, world.arena.y + world.arena.height - 100f));
                    if (destination.dst(player.position) > 360f) {
                        break;
                    }
                }
                boss.position.set(Navigation.nearestOpenPosition(world.layout, destination, boss.isFlying));
                EffectSystem.shockwave(world, boss.position, boss.radius * 3f, VOID_COLOR, 0.4f);
                int projectileCount = isEnraged ? 24 : 16;
                world.schedule(0.35f, () -> {
                    if (boss.isDead) {
                        return;
                    }
                    for (int index = 0; index < projectileCount; index++) {
                        Attacks.fireProjectile(world, boss, index * 360f / projectileCount, 250f, damage, VOID_COLOR).radius = 8f;
                    }
                });
            } else if (pattern == 2) {
                float novaRadius = isEnraged ? 260f : 220f;
                Vector2 novaCenter = new Vector2(boss.position);
                boss.windupTimeRemaining = 1.4f;
                EffectSystem.telegraphCircle(world, novaCenter, novaRadius, TELEGRAPH_COLOR, 1.4f);
                world.schedule(1.4f, () -> {
                    if (!boss.isDead) {
                        Attacks.circleBlast(world, boss, novaCenter, novaRadius, damage.scaled(2f), VOID_COLOR);
                        EffectSystem.shake(world, 0.6f);
                    }
                });
            } else {
                for (int index = 0; index < (isEnraged ? 3 : 2); index++) {
                    EnemySystem.spawn(world, ContentRegistry.enemy(minionEnemyId), Attacks.randomPointAround(boss.position, 120f));
                }
            }
        };
    }
}
