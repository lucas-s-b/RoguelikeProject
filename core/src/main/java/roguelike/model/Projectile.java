package roguelike.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import java.util.HashSet;
import java.util.Set;
import roguelike.definitions.Hooks;

public final class Projectile {
    public Team team = Team.PLAYER;
    public Unit owner;
    public final Vector2 position = new Vector2();
    public final Vector2 velocity = new Vector2();
    public float radius = 6f;
    public float lifetimeRemaining = 2f;
    public float age;
    public DamageSpec damageSpec;
    public int pierceRemaining;
    public boolean isHoming;
    public float homingStrength = 5f;
    public final Color color = new Color(Color.WHITE);
    public final Set<Unit> unitsAlreadyHit = new HashSet<>();
    public Hooks.ProjectileImpact onImpact;
    public Hooks.ProjectileImpact onExpire;
    public Hooks.ProjectileUpdate onUpdate;
    public float timer;
    public boolean isDestroyed;
}
