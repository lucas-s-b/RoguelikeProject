package roguelike.content;

import roguelike.definitions.ContentRegistry;
import roguelike.definitions.StageDefinition;
import roguelike.model.FloorStyle;

public final class StageContent {
    public static final String CRYPT = "crypt";
    public static final String GROTTO = "grotto";
    public static final String FORGE = "forge";
    public static final String SANCTUM = "sanctum";

    private StageContent() {
    }

    public static void register() {
        ContentRegistry.add(StageDefinition.create(CRYPT)
                .name("The Sunken Crypt")
                .enemies(EnemyContent.SLIME, EnemyContent.BAT, EnemyContent.SKELETON_ARCHER, EnemyContent.BRUTE, EnemyContent.BOMBER)
                .boss(EnemyContent.THE_WARDEN)
                .floorStyle(FloorStyle.STONE)
                .obstacles(6, 2)
                .floor(0.085f, 0.075f, 0.105f)
                .tiles(0.12f, 0.105f, 0.145f)
                .walls(0.16f, 0.13f, 0.2f)
                .wallEdges(0.5f, 0.42f, 0.62f)
                .torches(1f, 0.6f, 0.3f));

        ContentRegistry.add(StageDefinition.create(GROTTO)
                .name("The Fungal Grotto")
                .enemies(EnemyContent.BAT, EnemyContent.SKELETON_ARCHER, EnemyContent.BOMBER, EnemyContent.SPORELING, EnemyContent.OOZE, EnemyContent.LEAPER)
                .boss(EnemyContent.BROODMOTHER)
                .floorStyle(FloorStyle.MOSS)
                .obstacles(4, 4)
                .health(2.2f)
                .damage(1.35f)
                .armor(1f)
                .eliteChance(0.08f)
                .floor(0.055f, 0.085f, 0.07f)
                .tiles(0.08f, 0.125f, 0.095f)
                .walls(0.1f, 0.165f, 0.125f)
                .wallEdges(0.35f, 0.62f, 0.45f)
                .torches(0.5f, 1f, 0.6f));

        ContentRegistry.add(StageDefinition.create(FORGE)
                .name("The Ember Forge")
                .enemies(EnemyContent.BRUTE, EnemyContent.BOMBER, EnemyContent.LEAPER, EnemyContent.SHIELDBEARER, EnemyContent.FIRE_IMP, EnemyContent.MORTAR)
                .boss(EnemyContent.FORGE_COLOSSUS)
                .floorStyle(FloorStyle.METAL)
                .obstacles(5, 4)
                .health(3.6f)
                .damage(1.75f)
                .armor(2f)
                .eliteChance(0.15f)
                .floor(0.1f, 0.06f, 0.05f)
                .tiles(0.15f, 0.085f, 0.065f)
                .walls(0.21f, 0.1f, 0.075f)
                .wallEdges(0.72f, 0.38f, 0.2f)
                .torches(1f, 0.45f, 0.15f));

        ContentRegistry.add(StageDefinition.create(SANCTUM)
                .name("The Void Sanctum")
                .enemies(EnemyContent.SKELETON_ARCHER, EnemyContent.OOZE, EnemyContent.SHIELDBEARER, EnemyContent.MORTAR,
                        EnemyContent.WISP, EnemyContent.VOID_PRIEST, EnemyContent.WATCHER)
                .boss(EnemyContent.HOLLOW_KING)
                .floorStyle(FloorStyle.ASTRAL)
                .obstacles(3, 6)
                .health(6.2f)
                .damage(2f)
                .armor(3f)
                .eliteChance(0.22f)
                .floor(0.045f, 0.055f, 0.1f)
                .tiles(0.07f, 0.085f, 0.15f)
                .walls(0.095f, 0.11f, 0.21f)
                .wallEdges(0.42f, 0.52f, 0.92f)
                .torches(0.55f, 0.65f, 1f));
    }
}
