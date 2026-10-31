package roguelike.definitions;

import com.badlogic.gdx.graphics.Color;
import java.util.ArrayList;
import java.util.List;
import roguelike.model.FloorStyle;

public final class StageDefinition {
    public final String id;
    public String name = "";
    public final List<String> enemyIds = new ArrayList<>();
    public String bossId;
    public float healthMultiplier = 1f;
    public float damageMultiplier = 1f;
    public float enemyArmor;
    public float eliteChance;
    public FloorStyle floorStyle = FloorStyle.STONE;
    public int wallFeatures = 5;
    public int voidFeatures = 2;
    public final Color floorColor = new Color(0.085f, 0.075f, 0.105f, 1f);
    public final Color tileColor = new Color(0.12f, 0.105f, 0.145f, 1f);
    public final Color wallColor = new Color(0.16f, 0.13f, 0.2f, 1f);
    public final Color wallEdgeColor = new Color(0.5f, 0.42f, 0.62f, 1f);
    public final Color torchColor = new Color(1f, 0.6f, 0.3f, 1f);

    private StageDefinition(String id) {
        this.id = id;
    }

    public static StageDefinition create(String id) {
        return new StageDefinition(id);
    }

    public StageDefinition name(String stageName) {
        name = stageName;
        return this;
    }

    public StageDefinition enemies(String... ids) {
        enemyIds.addAll(List.of(ids));
        return this;
    }

    public StageDefinition boss(String enemyId) {
        bossId = enemyId;
        return this;
    }

    public StageDefinition health(float multiplier) {
        healthMultiplier = multiplier;
        return this;
    }

    public StageDefinition damage(float multiplier) {
        damageMultiplier = multiplier;
        return this;
    }

    public StageDefinition armor(float armorForEveryEnemy) {
        enemyArmor = armorForEveryEnemy;
        return this;
    }

    public StageDefinition eliteChance(float chance) {
        eliteChance = chance;
        return this;
    }

    public StageDefinition floorStyle(FloorStyle style) {
        floorStyle = style;
        return this;
    }

    public StageDefinition obstacles(int wallFeatureCount, int voidFeatureCount) {
        wallFeatures = wallFeatureCount;
        voidFeatures = voidFeatureCount;
        return this;
    }

    public StageDefinition floor(float red, float green, float blue) {
        floorColor.set(red, green, blue, 1f);
        return this;
    }

    public StageDefinition tiles(float red, float green, float blue) {
        tileColor.set(red, green, blue, 1f);
        return this;
    }

    public StageDefinition walls(float red, float green, float blue) {
        wallColor.set(red, green, blue, 1f);
        return this;
    }

    public StageDefinition wallEdges(float red, float green, float blue) {
        wallEdgeColor.set(red, green, blue, 1f);
        return this;
    }

    public StageDefinition torches(float red, float green, float blue) {
        torchColor.set(red, green, blue, 1f);
        return this;
    }
}
