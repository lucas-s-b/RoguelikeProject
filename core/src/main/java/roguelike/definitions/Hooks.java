package roguelike.definitions;

import roguelike.model.DamageSpec;
import roguelike.model.GameWorld;
import roguelike.model.Hit;
import roguelike.model.PerkInstance;
import roguelike.model.Projectile;
import roguelike.model.RunState;
import roguelike.model.SkillCast;
import roguelike.model.StatusInstance;
import roguelike.model.Unit;

public final class Hooks {
    private Hooks() {
    }

    public interface HitHook {
        void run(GameWorld world, Hit hit, PerkInstance perk);
    }

    public interface KillHook {
        void run(GameWorld world, Unit killer, Unit victim, PerkInstance perk);
    }

    public interface SkillCastHook {
        void run(GameWorld world, SkillCast cast, PerkInstance perk);
    }

    public interface WeaponAttackHook {
        void run(GameWorld world, Unit attacker, DamageSpec damage, PerkInstance perk);
    }

    public interface TickHook {
        void run(GameWorld world, Unit owner, PerkInstance perk, float delta);
    }

    public interface RoomHook {
        void run(GameWorld world, Unit owner, PerkInstance perk);
    }

    public interface AcquireHook {
        void run(RunState run, Unit owner, PerkInstance perk);
    }

    public interface WeaponAttack {
        void run(GameWorld world, Unit attacker, DamageSpec damage, float aimAngle);
    }

    public interface SkillAction {
        void run(GameWorld world, SkillCast cast);
    }

    public interface StatusTick {
        void run(GameWorld world, Unit unit, StatusInstance status);
    }

    public interface StatusVisual {
        void run(GameWorld world, Unit unit, StatusInstance status, float delta);
    }

    public interface EnemyBehavior {
        void run(GameWorld world, Unit enemy, float delta);
    }

    public interface ProjectileImpact {
        void run(GameWorld world, Projectile projectile, Unit unitHit);
    }

    public interface ProjectileUpdate {
        void run(GameWorld world, Projectile projectile, float delta);
    }
}
