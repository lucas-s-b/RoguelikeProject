package roguelike.model;

public final class TimedAction {
    public float delayRemaining;
    public final Runnable action;

    public TimedAction(float delayRemaining, Runnable action) {
        this.delayRemaining = delayRemaining;
        this.action = action;
    }
}
