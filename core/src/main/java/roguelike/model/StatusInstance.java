package roguelike.model;

import roguelike.definitions.StatusDefinition;

public final class StatusInstance {
    public final StatusDefinition definition;
    public int stacks;
    public float durationRemaining;
    public float tickTimer;
    public Unit source;

    public StatusInstance(StatusDefinition definition) {
        this.definition = definition;
    }
}
