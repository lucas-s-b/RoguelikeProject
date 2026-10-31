package roguelike.systems;

import roguelike.definitions.ContentRegistry;
import roguelike.model.RunState;
import roguelike.model.SkillInstance;
import roguelike.model.Stat;
import roguelike.model.Team;
import roguelike.model.Unit;
import roguelike.model.WeaponInstance;

public final class RunSystem {
    public static final int STARTING_GOLD = 30;

    private RunSystem() {
    }

    public static RunState createRun(String startingWeaponId, String startingSkillId) {
        RunState run = new RunState();
        Unit player = new Unit();
        player.team = Team.PLAYER;
        player.radius = 15f;
        player.color.set(0.35f, 0.75f, 1f, 1f);
        player.baseStats.put(Stat.MAX_HEALTH, 100f);
        player.baseStats.put(Stat.MOVE_SPEED, 240f);
        player.baseStats.put(Stat.CRIT_CHANCE, 5f);
        StatSystem.recalculate(player);
        player.health = player.maxHealth();
        run.player = player;
        run.weapon = new WeaponInstance(ContentRegistry.weapon(startingWeaponId));
        if (startingSkillId != null) {
            run.skills[0] = new SkillInstance(ContentRegistry.skill(startingSkillId));
        }
        run.gold = STARTING_GOLD;
        return run;
    }
}
