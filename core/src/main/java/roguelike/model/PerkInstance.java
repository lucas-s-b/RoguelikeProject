package roguelike.model;

import roguelike.definitions.PerkDefinition;

public final class PerkInstance {
    public final PerkDefinition definition;
    public int stacks = 1;
    public int counter;
    public float timer;
    public boolean isUsed;

    public PerkInstance(PerkDefinition definition) {
        this.definition = definition;
    }
}
