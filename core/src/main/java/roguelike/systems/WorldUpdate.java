package roguelike.systems;

import com.badlogic.gdx.math.Vector2;
import java.util.List;
import roguelike.model.GameWorld;
import roguelike.model.PerkInstance;
import roguelike.model.Stat;
import roguelike.model.Team;
import roguelike.model.TimedAction;
import roguelike.model.Unit;

public final class WorldUpdate {
    private WorldUpdate() {
    }

    public static void update(GameWorld world, float delta, Vector2 mouseWorldPosition) {
        world.time += delta;
        world.addPendingObjects();

        List<TimedAction> timedActions = world.timedActions;
        for (TimedAction timedAction : timedActions) {
            timedAction.delayRemaining -= delta;
            if (timedAction.delayRemaining <= 0f) {
                timedAction.action.run();
            }
        }
        timedActions.removeIf(timedAction -> timedAction.delayRemaining <= 0f);

        PlayerControlSystem.update(world, mouseWorldPosition);
        SkillSystem.updateCooldowns(world.run, delta);
        Navigation.update(world);

        for (Unit unit : world.units) {
            if (unit.isDead) {
                continue;
            }
            StatSystem.updateBuffs(unit, delta);
            StatusSystem.update(world, unit, delta);
            unit.attackCooldownRemaining -= delta;
            unit.contactCooldownRemaining -= delta;
            unit.invulnerableTimeRemaining -= delta;
            unit.hitFlashTimeRemaining -= delta;
            float regeneration = unit.stat(Stat.HEALTH_REGEN);
            if (regeneration > 0f) {
                unit.health = Math.min(unit.maxHealth(), unit.health + regeneration * delta);
            }
            for (PerkInstance perk : unit.perks) {
                if (perk.definition.onTick != null) {
                    perk.definition.onTick.run(world, unit, perk, delta);
                }
            }
            if (unit.team == Team.ENEMY && !unit.isDead) {
                EnemySystem.update(world, unit, delta);
            }
        }

        MovementSystem.update(world, delta);
        AnimationSystem.update(world, delta);
        ProjectileSystem.update(world, delta);
        EffectSystem.update(world, delta);
        world.units.removeIf(unit -> unit.isDead && unit != world.player());
        RoomSystem.update(world, delta);
    }
}
