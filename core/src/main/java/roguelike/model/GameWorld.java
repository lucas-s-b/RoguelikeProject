package roguelike.model;

import com.badlogic.gdx.math.Rectangle;
import java.util.ArrayList;
import java.util.List;

public final class GameWorld {
    public final RunState run;
    public final Rectangle arena = new Rectangle(20f, 120f, 1240f, 560f);
    public final RoomLayout layout = new RoomLayout(31, 14, 20f, 120f);
    public final List<Unit> units = new ArrayList<>();
    public final List<Projectile> projectiles = new ArrayList<>();
    public final List<VisualEffect> effects = new ArrayList<>();
    public final List<FloatingText> floatingTexts = new ArrayList<>();
    public final List<Particle> particles = new ArrayList<>();
    public final List<Decal> decals = new ArrayList<>();
    public final List<TimedAction> timedActions = new ArrayList<>();
    private final List<Unit> unitsToAdd = new ArrayList<>();
    private final List<Projectile> projectilesToAdd = new ArrayList<>();
    private final List<TimedAction> timedActionsToAdd = new ArrayList<>();
    public float time;
    public int pendingSpawnCount;
    public boolean isRoomCleared;
    public float timeSinceRoomCleared;
    public float screenShake;
    public float hurtFlash;
    public float screenFlash;
    public float hitStopRemaining;

    public GameWorld(RunState run) {
        this.run = run;
    }

    public Unit player() {
        return run.player;
    }

    public void addUnit(Unit unit) {
        unitsToAdd.add(unit);
    }

    public void addProjectile(Projectile projectile) {
        projectilesToAdd.add(projectile);
    }

    public void schedule(float delaySeconds, Runnable action) {
        timedActionsToAdd.add(new TimedAction(delaySeconds, action));
    }

    public boolean hasPendingUnits() {
        return !unitsToAdd.isEmpty();
    }

    public void addPendingObjects() {
        units.addAll(unitsToAdd);
        unitsToAdd.clear();
        projectiles.addAll(projectilesToAdd);
        projectilesToAdd.clear();
        timedActions.addAll(timedActionsToAdd);
        timedActionsToAdd.clear();
    }
}
