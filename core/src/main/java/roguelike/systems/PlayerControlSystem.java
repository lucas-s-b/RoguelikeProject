package roguelike.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import roguelike.model.GameWorld;
import roguelike.model.SkillInstance;
import roguelike.model.Unit;

public final class PlayerControlSystem {
    public static final int[] SKILL_KEYS = {Input.Keys.Q, Input.Keys.E, Input.Keys.R, Input.Keys.F};

    private PlayerControlSystem() {
    }

    public static void update(GameWorld world, Vector2 mouseWorldPosition) {
        Unit player = world.player();
        if (player.isDead) {
            return;
        }
        float horizontal = 0f;
        float vertical = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            horizontal -= 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            horizontal += 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            vertical -= 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            vertical += 1f;
        }
        player.moveDirection.set(horizontal, vertical).nor();
        player.aimAngle = Targeting.angleBetween(player.position, mouseWorldPosition);
        if (StatusSystem.isStunned(player)) {
            return;
        }
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && world.run.weapon != null) {
            WeaponSystem.tryAttack(world, player, world.run.weapon, player.aimAngle);
        }
        for (int slot = 0; slot < world.run.skills.length; slot++) {
            SkillInstance skill = world.run.skills[slot];
            if (skill != null && Gdx.input.isKeyPressed(SKILL_KEYS[slot])) {
                SkillSystem.tryCast(world, player, skill, mouseWorldPosition);
            }
        }
    }
}
