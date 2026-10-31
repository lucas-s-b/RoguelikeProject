package roguelike.definitions;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ContentRegistry {
    public static final Map<String, PerkDefinition> perks = new LinkedHashMap<>();
    public static final Map<String, SkillDefinition> skills = new LinkedHashMap<>();
    public static final Map<String, WeaponDefinition> weapons = new LinkedHashMap<>();
    public static final Map<String, StatusDefinition> statuses = new LinkedHashMap<>();
    public static final Map<String, EnemyDefinition> enemies = new LinkedHashMap<>();
    public static final Map<String, StageDefinition> stages = new LinkedHashMap<>();
    public static final List<PerkDefinition> eliteTraits = new ArrayList<>();

    private ContentRegistry() {
    }

    public static PerkDefinition add(PerkDefinition perk) {
        perks.put(perk.id, perk);
        return perk;
    }

    public static SkillDefinition add(SkillDefinition skill) {
        skills.put(skill.id, skill);
        return skill;
    }

    public static WeaponDefinition add(WeaponDefinition weapon) {
        weapons.put(weapon.id, weapon);
        return weapon;
    }

    public static StatusDefinition add(StatusDefinition status) {
        statuses.put(status.id, status);
        return status;
    }

    public static EnemyDefinition add(EnemyDefinition enemy) {
        enemies.put(enemy.id, enemy);
        return enemy;
    }

    public static StageDefinition add(StageDefinition stage) {
        stages.put(stage.id, stage);
        return stage;
    }

    public static PerkDefinition perk(String id) {
        return require(perks, id, "perk");
    }

    public static SkillDefinition skill(String id) {
        return require(skills, id, "skill");
    }

    public static WeaponDefinition weapon(String id) {
        return require(weapons, id, "weapon");
    }

    public static StatusDefinition status(String id) {
        return require(statuses, id, "status");
    }

    public static EnemyDefinition enemy(String id) {
        return require(enemies, id, "enemy");
    }

    private static <T> T require(Map<String, T> registry, String id, String kind) {
        T definition = registry.get(id);
        if (definition == null) {
            throw new IllegalArgumentException("No " + kind + " registered with id '" + id + "'. Known ids: " + registry.keySet());
        }
        return definition;
    }
}
