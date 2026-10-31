package roguelike.systems;

import com.badlogic.gdx.math.MathUtils;
import roguelike.model.BodyShape;
import roguelike.model.GameWorld;
import roguelike.model.Unit;

public final class AnimationSystem {
    public static final float ATTACK_POSE_SECONDS = 0.14f;
    public static final float SPAWN_SECONDS = 0.35f;
    private static final float VELOCITY_SMOOTHING_PER_SECOND = 14f;
    private static final float HEALTH_TRAIL_CATCH_UP_PER_SECOND = 3f;
    private static final float POINTED_TURN_DEGREES_PER_SECOND = 540f;
    private static final float STAR_SPIN_DEGREES_PER_SECOND = 70f;
    private static final float LEAN_DEGREES_PER_SPEED = 0.06f;
    private static final float MAXIMUM_LEAN_DEGREES = 14f;

    private AnimationSystem() {
    }

    public static void attackPose(Unit unit, float angleDegrees) {
        unit.attackPoseTimeRemaining = ATTACK_POSE_SECONDS;
        unit.attackPoseAngle = angleDegrees;
    }

    public static void update(GameWorld world, float delta) {
        for (Unit unit : world.units) {
            unit.age += delta;
            unit.attackPoseTimeRemaining = Math.max(0f, unit.attackPoseTimeRemaining - delta);
            unit.windupTimeRemaining = Math.max(0f, unit.windupTimeRemaining - delta);
            unit.smoothedVelocity.lerp(unit.velocity, Math.min(1f, VELOCITY_SMOOTHING_PER_SECOND * delta));
            float healthFraction = MathUtils.clamp(unit.healthFraction(), 0f, 1f);
            if (healthFraction >= unit.trailingHealthFraction) {
                unit.trailingHealthFraction = healthFraction;
            } else {
                float gap = unit.trailingHealthFraction - healthFraction;
                unit.trailingHealthFraction -= gap * Math.min(1f, HEALTH_TRAIL_CATCH_UP_PER_SECOND * delta);
            }
            if (unit.bodyShape == BodyShape.TRIANGLE) {
                float facingAngle = unit.attackPoseTimeRemaining > 0f ? unit.attackPoseAngle : Targeting.angleBetween(unit.position, world.player().position);
                float maximumTurn = POINTED_TURN_DEGREES_PER_SECOND * delta;
                unit.bodyAngle += MathUtils.clamp(Targeting.angleDifference(unit.bodyAngle, facingAngle), -maximumTurn, maximumTurn);
            } else if (unit.bodyShape == BodyShape.STAR) {
                unit.bodyAngle = (unit.bodyAngle + STAR_SPIN_DEGREES_PER_SECOND * delta) % 360f;
            } else if (unit.bodyShape != BodyShape.CIRCLE) {
                float lean = MathUtils.clamp(-unit.smoothedVelocity.x * LEAN_DEGREES_PER_SPEED, -MAXIMUM_LEAN_DEGREES, MAXIMUM_LEAN_DEGREES);
                unit.bodyAngle = MathUtils.lerp(unit.bodyAngle, lean, Math.min(1f, 10f * delta));
            }
        }
    }
}
