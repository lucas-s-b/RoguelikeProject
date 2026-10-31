package roguelike.model;

import roguelike.definitions.SkillDefinition;

public final class SkillInstance {
    public final SkillDefinition definition;
    public int level = 1;
    public float cooldownRemaining;

    public SkillInstance(SkillDefinition definition) {
        this.definition = definition;
    }
}
