package roguelike.model;

import com.badlogic.gdx.math.Vector2;
import roguelike.definitions.SkillDefinition;

public final class SkillCast {
    public SkillDefinition definition;
    public Unit caster;
    public int level = 1;
    public final Vector2 aimPoint = new Vector2();
    public float aimAngle;
    public float damage;
    public int hitCount;
    public int projectileCount;
    public float areaMultiplier = 1f;
    public boolean isEcho;

    public boolean hasTag(SkillTag tag) {
        return definition.tags.contains(tag);
    }

    public float area(float baseRadius) {
        return baseRadius * areaMultiplier;
    }

    public DamageSpec damageSpec() {
        return DamageSpec.skill(this);
    }
}
