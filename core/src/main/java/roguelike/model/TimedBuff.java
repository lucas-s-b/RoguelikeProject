package roguelike.model;

public final class TimedBuff {
    public final String sourceId;
    public final Stat stat;
    public float amount;
    public float durationRemaining;

    public TimedBuff(String sourceId, Stat stat, float amount, float durationRemaining) {
        this.sourceId = sourceId;
        this.stat = stat;
        this.amount = amount;
        this.durationRemaining = durationRemaining;
    }
}
