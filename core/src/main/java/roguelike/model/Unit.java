package roguelike.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import roguelike.definitions.EnemyDefinition;

public final class Unit {
    public Team team = Team.ENEMY;
    public final Vector2 position = new Vector2();
    public final Vector2 moveDirection = new Vector2();
    public final Vector2 knockbackVelocity = new Vector2();
    public final Vector2 dashVelocity = new Vector2();
    public final Vector2 velocity = new Vector2();
    public final Vector2 smoothedVelocity = new Vector2();
    public float dashTimeRemaining;
    public float age;
    public float attackPoseTimeRemaining;
    public float attackPoseAngle;
    public float windupTimeRemaining;
    public float swingSide = 1f;
    public float trailingHealthFraction = 1f;
    public BodyShape bodyShape = BodyShape.CIRCLE;
    public float bodyAngle;
    public float radius = 16f;
    public float health;
    public float shield;
    public final Color color = new Color(Color.WHITE);
    public boolean isDead;
    public boolean isBoss;
    public boolean isElite;
    public boolean isFlying;
    public final Vector2 lastSafePosition = new Vector2();
    public float aimAngle;
    public float speedMultiplier = 1f;
    public float attackCooldownRemaining;
    public float contactCooldownRemaining;
    public float invulnerableTimeRemaining;
    public float hitFlashTimeRemaining;
    public float behaviorTimer;
    public int behaviorPhase;
    public float contactDamage;
    public float attackDamage;
    public int goldReward;
    public EnemyDefinition enemyDefinition;
    public final EnumMap<Stat, Float> baseStats = new EnumMap<>(Stat.class);
    public final EnumMap<Stat, Float> stats = new EnumMap<>(Stat.class);
    public final List<PerkInstance> perks = new ArrayList<>();
    public final List<StatusInstance> statuses = new ArrayList<>();
    public final List<TimedBuff> buffs = new ArrayList<>();

    public float stat(Stat stat) {
        Float value = stats.get(stat);
        return value == null ? 0f : value;
    }

    public float maxHealth() {
        return Math.max(1f, stat(Stat.MAX_HEALTH));
    }

    public float healthFraction() {
        return health / maxHealth();
    }
}
