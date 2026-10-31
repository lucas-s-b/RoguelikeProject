package roguelike.definitions;

import com.badlogic.gdx.graphics.Color;
import java.util.Arrays;
import java.util.EnumSet;
import roguelike.model.DamageType;
import roguelike.model.SkillTag;

public final class SkillDefinition {
    public final String id;
    public String name = "";
    public String description = "";
    public int price = 60;
    public float cooldown = 6f;
    public float damage = 10f;
    public int hitCount = 1;
    public int projectileCount = 1;
    public DamageType damageType = DamageType.PHYSICAL;
    public final EnumSet<SkillTag> tags = EnumSet.noneOf(SkillTag.class);
    public final Color color = new Color(Color.WHITE);
    public Hooks.SkillAction action;

    private SkillDefinition(String id) {
        this.id = id;
    }

    public static SkillDefinition create(String id) {
        return new SkillDefinition(id);
    }

    public SkillDefinition name(String skillName) {
        name = skillName;
        return this;
    }

    public SkillDefinition description(String skillDescription) {
        description = skillDescription;
        return this;
    }

    public SkillDefinition price(int goldPrice) {
        price = goldPrice;
        return this;
    }

    public SkillDefinition cooldown(float cooldownSeconds) {
        cooldown = cooldownSeconds;
        return this;
    }

    public SkillDefinition damage(float baseDamage, DamageType type) {
        damage = baseDamage;
        damageType = type;
        return this;
    }

    public SkillDefinition hitCount(int baseHitCount) {
        hitCount = baseHitCount;
        return this;
    }

    public SkillDefinition projectileCount(int baseProjectileCount) {
        projectileCount = baseProjectileCount;
        return this;
    }

    public SkillDefinition tags(SkillTag... skillTags) {
        tags.addAll(Arrays.asList(skillTags));
        return this;
    }

    public SkillDefinition color(float red, float green, float blue) {
        color.set(red, green, blue, 1f);
        return this;
    }

    public SkillDefinition action(Hooks.SkillAction skillAction) {
        action = skillAction;
        return this;
    }
}
