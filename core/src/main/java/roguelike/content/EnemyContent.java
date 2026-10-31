package roguelike.content;

import roguelike.definitions.ContentRegistry;
import roguelike.definitions.EnemyDefinition;
import roguelike.model.BodyShape;
import roguelike.systems.EnemyBehaviors;

public final class EnemyContent {
    public static final String SLIME = "slime";
    public static final String BAT = "bat";
    public static final String SKELETON_ARCHER = "skeleton_archer";
    public static final String BRUTE = "brute";
    public static final String BOMBER = "bomber";
    public static final String THE_WARDEN = "the_warden";
    public static final String SPORELING = "sporeling";
    public static final String OOZE = "ooze";
    public static final String SMALL_OOZE = "small_ooze";
    public static final String LEAPER = "leaper";
    public static final String BROODMOTHER = "broodmother";
    public static final String SHIELDBEARER = "shieldbearer";
    public static final String FIRE_IMP = "fire_imp";
    public static final String MORTAR = "mortar";
    public static final String FORGE_COLOSSUS = "forge_colossus";
    public static final String WISP = "wisp";
    public static final String VOID_PRIEST = "void_priest";
    public static final String WATCHER = "watcher";
    public static final String HOLLOW_KING = "hollow_king";

    private EnemyContent() {
    }

    public static void register() {
        ContentRegistry.add(EnemyDefinition.create(SLIME)
                .name("Slime")
                .health(28f)
                .moveSpeed(85f)
                .radius(14f)
                .color(0.4f, 0.85f, 0.35f)
                .contactDamage(8f)
                .goldReward(2)
                .spawnCost(1f)
                .behavior(EnemyBehaviors.chase()));

        ContentRegistry.add(EnemyDefinition.create(BAT)
                .name("Bat")
                .health(14f)
                .moveSpeed(170f)
                .radius(10f)
                .color(0.6f, 0.4f, 0.75f)
                .flying()
                .shape(BodyShape.TRIANGLE)
                .contactDamage(5f)
                .goldReward(1)
                .spawnCost(0.6f)
                .behavior(EnemyBehaviors.erraticChase(0.8f)));

        ContentRegistry.add(EnemyDefinition.create(SKELETON_ARCHER)
                .name("Skeleton Archer")
                .health(22f)
                .moveSpeed(90f)
                .radius(13f)
                .color(0.9f, 0.88f, 0.75f)
                .shape(BodyShape.DIAMOND)
                .contactDamage(4f)
                .attackDamage(9f)
                .goldReward(3)
                .spawnCost(1.5f)
                .minimumRoom(2)
                .behavior(EnemyBehaviors.rangedKite(300f, 300f, 1.8f)));

        ContentRegistry.add(EnemyDefinition.create(BRUTE)
                .name("Brute")
                .health(90f)
                .moveSpeed(70f)
                .radius(22f)
                .color(0.7f, 0.35f, 0.25f)
                .shape(BodyShape.SQUARE)
                .contactDamage(16f)
                .goldReward(6)
                .spawnCost(3f)
                .minimumRoom(3)
                .behavior(EnemyBehaviors.charger(320f, 0.6f, 4.5f, 0.45f)));

        ContentRegistry.add(EnemyDefinition.create(BOMBER)
                .name("Bomber")
                .health(20f)
                .moveSpeed(130f)
                .radius(12f)
                .color(1f, 0.55f, 0.2f)
                .shape(BodyShape.STAR)
                .contactDamage(0f)
                .attackDamage(22f)
                .goldReward(3)
                .spawnCost(1.5f)
                .minimumRoom(2)
                .behavior(EnemyBehaviors.bomber(55f, 0.8f, 85f)));

        ContentRegistry.add(EnemyDefinition.create(THE_WARDEN)
                .name("The Warden")
                .health(900f)
                .moveSpeed(70f)
                .radius(38f)
                .color(0.8f, 0.15f, 0.25f)
                .shape(BodyShape.HEXAGON)
                .contactDamage(20f)
                .attackDamage(12f)
                .goldReward(60)
                .minimumRoom(5)
                .boss()
                .behavior(EnemyBehaviors.bossWarden(SLIME)));

        ContentRegistry.add(EnemyDefinition.create(SPORELING)
                .name("Sporeling")
                .health(24f)
                .moveSpeed(0f)
                .radius(15f)
                .color(0.8f, 0.5f, 0.75f)
                .shape(BodyShape.PENTAGON)
                .contactDamage(4f)
                .attackDamage(7f)
                .goldReward(3)
                .spawnCost(1.5f)
                .behavior(EnemyBehaviors.turret(2.4f, 3, 30f, 200f, StatusContent.POISON, 2)));

        ContentRegistry.add(EnemyDefinition.create(OOZE)
                .name("Ooze")
                .health(34f)
                .moveSpeed(75f)
                .radius(18f)
                .color(0.3f, 0.8f, 0.65f)
                .contactDamage(9f)
                .goldReward(2)
                .spawnCost(1.6f)
                .trait(EnemyTraits.splitsInto(SMALL_OOZE, 2))
                .behavior(EnemyBehaviors.chase()));

        ContentRegistry.add(EnemyDefinition.create(SMALL_OOZE)
                .name("Oozeling")
                .health(12f)
                .moveSpeed(120f)
                .radius(10f)
                .color(0.45f, 0.9f, 0.75f)
                .contactDamage(5f)
                .goldReward(1)
                .summonOnly()
                .behavior(EnemyBehaviors.erraticChase(0.5f)));

        ContentRegistry.add(EnemyDefinition.create(LEAPER)
                .name("Leaper")
                .health(26f)
                .moveSpeed(90f)
                .radius(13f)
                .color(0.6f, 0.8f, 0.25f)
                .shape(BodyShape.TRIANGLE)
                .contactDamage(6f)
                .attackDamage(12f)
                .goldReward(3)
                .spawnCost(1.5f)
                .behavior(EnemyBehaviors.leaper(260f, 0.55f, 0.3f, 55f, 2.2f)));

        ContentRegistry.add(EnemyDefinition.create(BROODMOTHER)
                .name("The Broodmother")
                .health(1000f)
                .moveSpeed(85f)
                .radius(40f)
                .color(0.45f, 0.7f, 0.3f)
                .shape(BodyShape.STAR)
                .contactDamage(18f)
                .attackDamage(11f)
                .goldReward(80)
                .boss()
                .behavior(EnemyBehaviors.bossBroodmother(LEAPER, StatusContent.CHILL)));

        ContentRegistry.add(EnemyDefinition.create(SHIELDBEARER)
                .name("Shieldbearer")
                .health(60f)
                .moveSpeed(65f)
                .radius(20f)
                .color(0.62f, 0.64f, 0.72f)
                .shape(BodyShape.SQUARE)
                .armor(3f)
                .frontalShield(120f)
                .contactDamage(14f)
                .goldReward(5)
                .spawnCost(2.5f)
                .behavior(EnemyBehaviors.slowTurner(110f)));

        ContentRegistry.add(EnemyDefinition.create(FIRE_IMP)
                .name("Fire Imp")
                .health(20f)
                .moveSpeed(90f)
                .radius(11f)
                .color(1f, 0.45f, 0.2f)
                .flying()
                .shape(BodyShape.TRIANGLE)
                .contactDamage(4f)
                .attackDamage(10f)
                .goldReward(3)
                .spawnCost(1.4f)
                .behavior(EnemyBehaviors.blinkCaster(3.2f, 230f, 0.5f, 320f, StatusContent.BURN)));

        ContentRegistry.add(EnemyDefinition.create(MORTAR)
                .name("Mortar Golem")
                .health(42f)
                .moveSpeed(45f)
                .radius(16f)
                .color(0.55f, 0.42f, 0.35f)
                .shape(BodyShape.HEXAGON)
                .armor(2f)
                .contactDamage(6f)
                .attackDamage(16f)
                .goldReward(4)
                .spawnCost(2f)
                .behavior(EnemyBehaviors.mortar(3.2f, 1.2f, 75f)));

        ContentRegistry.add(EnemyDefinition.create(FORGE_COLOSSUS)
                .name("The Forge Colossus")
                .health(1100f)
                .moveSpeed(55f)
                .radius(46f)
                .color(0.9f, 0.42f, 0.15f)
                .shape(BodyShape.SQUARE)
                .armor(4f)
                .contactDamage(22f)
                .attackDamage(14f)
                .goldReward(100)
                .boss()
                .behavior(EnemyBehaviors.bossColossus(FIRE_IMP, StatusContent.BURN)));

        ContentRegistry.add(EnemyDefinition.create(WISP)
                .name("Wisp")
                .health(22f)
                .moveSpeed(125f)
                .radius(10f)
                .color(0.55f, 0.85f, 1f)
                .flying()
                .shape(BodyShape.DIAMOND)
                .contactDamage(5f)
                .attackDamage(9f)
                .goldReward(3)
                .spawnCost(1.3f)
                .behavior(EnemyBehaviors.orbCaster(280f, 2.6f, 150f)));

        ContentRegistry.add(EnemyDefinition.create(VOID_PRIEST)
                .name("Void Priest")
                .health(38f)
                .moveSpeed(95f)
                .radius(14f)
                .color(0.72f, 0.5f, 1f)
                .shape(BodyShape.PENTAGON)
                .contactDamage(4f)
                .goldReward(5)
                .spawnCost(2.2f)
                .behavior(EnemyBehaviors.healer(330f, 3f, 260f, 0.15f)));

        ContentRegistry.add(EnemyDefinition.create(WATCHER)
                .name("Watcher")
                .health(32f)
                .moveSpeed(60f)
                .radius(14f)
                .color(0.92f, 0.9f, 1f)
                .flying()
                .contactDamage(4f)
                .attackDamage(18f)
                .goldReward(4)
                .spawnCost(2f)
                .behavior(EnemyBehaviors.sniper(420f, 4f, 0.9f, 26f)));

        ContentRegistry.add(EnemyDefinition.create(HOLLOW_KING)
                .name("The Hollow King")
                .health(1000f)
                .moveSpeed(70f)
                .radius(42f)
                .color(0.42f, 0.32f, 0.85f)
                .flying()
                .shape(BodyShape.DIAMOND)
                .armor(3f)
                .contactDamage(22f)
                .attackDamage(12f)
                .goldReward(120)
                .boss()
                .behavior(EnemyBehaviors.bossHollowKing(WISP)));
    }
}
