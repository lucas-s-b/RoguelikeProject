# Content Guide

## Running the game
- Play: `gradlew.bat lwjgl3:run`
- Check that all content still works: `gradlew.bat core:test`

## Where things live
| Folder | What it holds |
|---|---|
| `content/` | All game content. This is where you add things. |
| `content/perks/` | Perks, grouped by theme. |
| `content/GameText.java` | Every menu and HUD text. |
| `definitions/` | The building blocks that content is made from. |
| `systems/` | The engine. You rarely need to touch it. |
| `systems/Attacks.java` | Ready-made attacks to use in your content. |
| `graphics/` | Everything that draws the game: units, effects, particles and post-processing. |
| `ui/` | The pixel art menus and HUD: font, icons, panels, buttons and bars. |
| `screens/` | The menu, dungeon, reward, shop and game over screens. |

Icons and font letters are drawn as text in `ui/PixelArt.java` and `ui/PixelFont.java`, one character per pixel.

## How a hit works
Every weapon swing, skill hit, projectile and status tick goes through `CombatSystem.dealHit`, in this order:
1. Bonus damage and damage percent from stats.
2. `modifyOutgoingHit` on the attacker's perks.
3. The crit roll.
4. Damage-taken statuses on the target, such as Marked.
5. `modifyIncomingHit` on the target's perks, then dodge and armor.
6. The damage is dealt.
7. `onHit` on the attacker's perks, if the hit allows on-hit effects.
8. `onDamaged` on the target's perks.
9. `onKill` on the attacker's perks, if the target died.

On-hit runs once per hit, so a skill that hits 10 times triggers on-hit 10 times. Perk damage uses `DamageSpec.perk(...)`, which does not trigger on-hit, so perks can't set each other off forever. Add `.withOnHit(true)` when you want perk damage to trigger on-hit too. Vengeful Spirit does this.

## Adding a perk
Open a file in `content/perks/` and add this inside `register()`:
```java
ContentRegistry.add(PerkDefinition.create("frost_touch")
        .name("Frost Touch")
        .description("Hits deal 4 bonus ice damage.")
        .rarity(Rarity.COMMON)
        .maxStacks(5)
        .onHit((world, hit, perk) ->
                Attacks.hitUnit(world, hit.attacker, hit.target, DamageSpec.perk(4f * perk.stacks, DamageType.ICE))));
```
The id must be unique. The perk shows up in rewards and the shop automatically.

A perk that only changes stats needs no code:
```java
ContentRegistry.add(PerkDefinition.create("thick_hide")
        .name("Thick Hide")
        .description("Gain 3 armor and 10 max HP.")
        .stat(Stat.ARMOR, 3f)
        .stat(Stat.MAX_HEALTH, 10f));
```

### Perk hooks
| Hook | When it runs | Parameters |
|---|---|---|
| `onHit` | After you damage something | world, hit, perk |
| `modifyOutgoingHit` | Before your hit deals damage. Change `hit.damage` or set `hit.isCritical`. | world, hit, perk |
| `modifyIncomingHit` | Before you take damage. Change `hit.damage` or set `hit.isDodged`. | world, hit, perk |
| `onDamaged` | After you take damage | world, hit, perk |
| `onKill` | After you kill something | world, killer, victim, perk |
| `modifySkillCast` | Before a skill fires. Change `cast.damage`, `cast.hitCount`, `cast.projectileCount` or `cast.areaMultiplier`. | world, cast, perk |
| `onSkillCast` | After a skill fires | world, cast, perk |
| `onWeaponAttack` | Before a weapon attack. Change `damage.damage`. | world, attacker, damage, perk |
| `onTick` | Every frame | world, owner, perk, delta |
| `onRoomStart`, `onRoomClear` | When a room starts or is cleared | world, owner, perk |
| `onAcquire` | When you pick up the perk | run, owner, perk |

`perk.stacks` is how many copies you own. `perk.counter`, `perk.timer` and `perk.isUsed` are free storage for effects like "every 5th hit" or "once per room".

## Adding a skill
Add this to `content/SkillContent.java`:
```java
ContentRegistry.add(SkillDefinition.create("ground_slam")
        .name("Ground Slam")
        .description("Slam the ground 3 times, damaging enemies around you.")
        .price(65)
        .cooldown(6f)
        .damage(12f, DamageType.PHYSICAL)
        .hitCount(3)
        .tags(SkillTag.AREA, SkillTag.MULTI_HIT)
        .color(0.8f, 0.7f, 0.5f)
        .action((world, cast) -> {
            for (int slam = 0; slam < cast.hitCount; slam++) {
                world.schedule(slam * 0.3f, () ->
                        Attacks.circleBlast(world, cast.caster, cast.caster.position, cast.area(100f), cast.damageSpec(), cast.definition.color));
            }
        }));
```
Use `cast.hitCount`, `cast.projectileCount` and `cast.area(...)` instead of fixed numbers. That way perks like Relentless, Split Shot and Expansive Mind work with your skill. Tags decide which perks affect a skill: `MULTI_HIT` gets extra hits, `PROJECTILE` gets extra projectiles, and `MOVEMENT` triggers Afterimage.

## Adding a weapon
Add this to `content/WeaponContent.java`:
```java
ContentRegistry.add(WeaponDefinition.create("battle_axe")
        .name("Battle Axe")
        .description("A huge, slow swing all around you.")
        .price(85)
        .cooldown(0.9f)
        .damage(22f, DamageType.PHYSICAL)
        .color(0.7f, 0.5f, 0.4f)
        .attack((world, attacker, damage, aimAngle) ->
                Attacks.meleeArc(world, attacker, aimAngle, 90f, 300f, damage, damage.damageType.color)));
```
Always pass along the `damage` you are given, so weapon level, Spellblade and other perks apply.

`world.run.weapon.attackCount` counts your attacks, for effects like every third attack.

## Adding an enemy
Add this to `content/EnemyContent.java`:
```java
ContentRegistry.add(EnemyDefinition.create("ghost")
        .name("Ghost")
        .health(18f)
        .moveSpeed(120f)
        .color(0.8f, 0.85f, 1f)
        .contactDamage(6f)
        .goldReward(2)
        .spawnCost(1f)
        .minimumRoom(3)
        .behavior(EnemyBehaviors.erraticChase(1.2f)));
```
`spawnCost` is how much of the room's spawn budget the enemy uses. `minimumRoom` is the first room it can appear in. Ready-made behaviors live in `systems/EnemyBehaviors.java`: `chase`, `erraticChase`, `rangedKite`, `charger`, `bomber` and `bossWarden`. Add `.boss()` to make an enemy appear in boss rooms.

In a custom behavior, set `enemy.windupTimeRemaining` before a big attack to make the enemy shake and flash, and call `AnimationSystem.attackPose(enemy, angle)` to make it lunge when it attacks.

## Adding a status effect
Add this to `content/StatusContent.java`:
```java
ContentRegistry.add(StatusDefinition.create("shock")
        .name("Shock")
        .color(1f, 1f, 0.4f)
        .maxStacks(5)
        .duration(2f)
        .tickInterval(1f)
        .onTick((world, unit, status) ->
                Attacks.hitUnit(world, status.source, unit, DamageSpec.status(3f * status.stacks, DamageType.LIGHTNING))));
```
Apply it from anywhere with `StatusSystem.apply(target, "shock", 1, attacker)`. These options need no code: `slowPercentPerStack`, `damageTakenPercentPerStack` and `stuns()`.

Add `.visual((world, unit, status, delta) -> ...)` to show particles while the status is active, for example `EffectSystem.flames`, `bubbles`, `drips`, `frost` or `sparkles`.

## The attack toolbox
Everything here is in `systems/Attacks.java`:
| Function | What it does |
|---|---|
| `hitUnit` | Hits one unit. |
| `meleeArc` | Hits everything in a cone in front of the attacker. |
| `circleBlast` | Hits everything in a circle. |
| `lineStrike` | Hits everything along a line. |
| `chainLightning` | Hits a target, then jumps to nearby enemies. |
| `fireProjectile` | Fires one projectile and returns it, so you can change `pierceRemaining`, `isHoming`, `onImpact`, `onUpdate` and more. |
| `fireSpread` | Fires several projectiles in a fan. |
| `dash` | Moves a unit quickly in one direction. |

`onUpdate` runs every frame while the projectile flies. `projectile.timer` is free storage for it.

`meleeArc`, `circleBlast`, `lineStrike` and `chainLightning` return the units they hit, so you can apply statuses to them afterward.

`EffectSystem.burst`, `sparks`, `shockwave`, `lightning` and `shake` add particles and screen shake to your content.

`EffectSystem.explosion` plays a full blast with smoke and a scorch mark. `telegraphCircle` and `telegraphLine` warn the player before an attack lands.

Other useful helpers:
- `world.schedule(seconds, () -> ...)` runs code later.
- `Targeting.nearestEnemy` and `Targeting.randomEnemy` find targets.
- `CombatSystem.heal` heals a unit.
- `EffectSystem` draws circles, rings and lines.

## Damage specs
| Spec | Triggers on-hit | Can crit | Used for |
|---|---|---|---|
| `DamageSpec.weapon(damage, type)` | Yes | Yes | Weapon attacks |
| `cast.damageSpec()` | Yes | Yes | Skill hits |
| `DamageSpec.perk(damage, type)` | No | No | Extra damage from perks |
| `DamageSpec.status(damage, type)` | No | No | Status ticks |
| `DamageSpec.enemy(damage)` | No | No | Enemy attacks |

You can change any of them with `.withOnHit(...)`, `.withCrit(...)`, `.withKnockback(...)` or `.scaled(multiplier)`.

## Balancing knobs
| What | Where |
|---|---|
| Enemy health and damage growth per room | `EnemySystem.HEALTH_SCALING_PER_ROOM`, `DAMAGE_SCALING_PER_ROOM` |
| How often bosses and shops appear | `RoomSystem.BOSS_ROOM_INTERVAL`, `SHOP_ROOM_INTERVAL` |
| How many enemies spawn | `RoomSystem.BASE_SPAWN_BUDGET`, `SPAWN_BUDGET_PER_ROOM` |
| Starting gold and starting stats | `RunSystem` |
| Starting weapon and skill | `RoguelikeGame.startRun` |
| Perk rarity weights and prices | `model/Rarity.java` |
| Damage gained per weapon or skill level | `WeaponSystem.DAMAGE_PER_LEVEL`, `SkillSystem.DAMAGE_PER_LEVEL` |
| Bloom, vignette and color grading | `graphics/PostProcessor.java` |
| Screen shake strength | `DungeonScreen.MAXIMUM_SHAKE_OFFSET` |
| Shockwave screen distortion | `PostProcessor.RIPPLE_STRENGTH` |
| UI colors and text sizes | `ui/UiCanvas.java` |

## LibGDX basics used here
- `RoguelikeGame` is the LibGDX `Game`. It creates the shared drawing tools and switches between screens.
- Each screen's `render(delta)` runs every frame. `delta` is the time since the last frame, in seconds.
- `ShapeRenderer` draws circles and rectangles. `SpriteBatch` with `BitmapFont` draws text.
- `FitViewport` keeps the game at 1280 by 720 and scales it to fit the window.
- `Gdx.input` reads the keyboard and mouse.
