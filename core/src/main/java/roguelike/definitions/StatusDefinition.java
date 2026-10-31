package roguelike.definitions;

import com.badlogic.gdx.graphics.Color;

public final class StatusDefinition {
    public final String id;
    public String name = "";
    public final Color color = new Color(Color.WHITE);
    public int maxStacks = 10;
    public float duration = 3f;
    public float tickInterval = 0.5f;
    public float slowPercentPerStack;
    public float damageTakenPercentPerStack;
    public boolean isStun;
    public Hooks.StatusTick onTick;
    public Hooks.StatusVisual visual;

    private StatusDefinition(String id) {
        this.id = id;
    }

    public static StatusDefinition create(String id) {
        return new StatusDefinition(id);
    }

    public StatusDefinition name(String statusName) {
        name = statusName;
        return this;
    }

    public StatusDefinition color(float red, float green, float blue) {
        color.set(red, green, blue, 1f);
        return this;
    }

    public StatusDefinition maxStacks(int stackLimit) {
        maxStacks = stackLimit;
        return this;
    }

    public StatusDefinition duration(float durationSeconds) {
        duration = durationSeconds;
        return this;
    }

    public StatusDefinition tickInterval(float secondsBetweenTicks) {
        tickInterval = secondsBetweenTicks;
        return this;
    }

    public StatusDefinition slowPercentPerStack(float percent) {
        slowPercentPerStack = percent;
        return this;
    }

    public StatusDefinition damageTakenPercentPerStack(float percent) {
        damageTakenPercentPerStack = percent;
        return this;
    }

    public StatusDefinition stuns() {
        isStun = true;
        return this;
    }

    public StatusDefinition onTick(Hooks.StatusTick tick) {
        onTick = tick;
        return this;
    }

    public StatusDefinition visual(Hooks.StatusVisual statusVisual) {
        visual = statusVisual;
        return this;
    }
}
