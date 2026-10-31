package roguelike.definitions;

import com.badlogic.gdx.graphics.Color;
import java.util.ArrayList;
import java.util.List;
import roguelike.model.BodyShape;

public final class EnemyDefinition {
    public final String id;
    public String name = "";
    public float maxHealth = 20f;
    public float moveSpeed = 100f;
    public float radius = 14f;
    public final Color color = new Color(Color.RED);
    public float contactDamage = 8f;
    public float attackDamage = 8f;
    public float armor;
    public int goldReward = 2;
    public float spawnCost = 1f;
    public int minimumRoom = 1;
    public boolean isBoss;
    public boolean isSummonOnly;
    public boolean isFlying;
    public float frontalShieldDegrees;
    public BodyShape shape = BodyShape.CIRCLE;
    public Hooks.EnemyBehavior behavior;
    public final List<PerkDefinition> traits = new ArrayList<>();

    private EnemyDefinition(String id) {
        this.id = id;
    }

    public static EnemyDefinition create(String id) {
        return new EnemyDefinition(id);
    }

    public EnemyDefinition name(String enemyName) {
        name = enemyName;
        return this;
    }

    public EnemyDefinition health(float baseHealth) {
        maxHealth = baseHealth;
        return this;
    }

    public EnemyDefinition moveSpeed(float pixelsPerSecond) {
        moveSpeed = pixelsPerSecond;
        return this;
    }

    public EnemyDefinition radius(float bodyRadius) {
        radius = bodyRadius;
        return this;
    }

    public EnemyDefinition color(float red, float green, float blue) {
        color.set(red, green, blue, 1f);
        return this;
    }

    public EnemyDefinition contactDamage(float damageOnTouch) {
        contactDamage = damageOnTouch;
        return this;
    }

    public EnemyDefinition attackDamage(float damagePerAttack) {
        attackDamage = damagePerAttack;
        return this;
    }

    public EnemyDefinition armor(float flatDamageReduction) {
        armor = flatDamageReduction;
        return this;
    }

    public EnemyDefinition trait(PerkDefinition trait) {
        traits.add(trait);
        return this;
    }

    public EnemyDefinition goldReward(int goldOnKill) {
        goldReward = goldOnKill;
        return this;
    }

    public EnemyDefinition spawnCost(float budgetCost) {
        spawnCost = budgetCost;
        return this;
    }

    public EnemyDefinition minimumRoom(int firstRoom) {
        minimumRoom = firstRoom;
        return this;
    }

    public EnemyDefinition shape(BodyShape bodyShape) {
        shape = bodyShape;
        return this;
    }

    public EnemyDefinition flying() {
        isFlying = true;
        return this;
    }

    public EnemyDefinition summonOnly() {
        isSummonOnly = true;
        return this;
    }

    public EnemyDefinition frontalShield(float arcDegrees) {
        frontalShieldDegrees = arcDegrees;
        return this;
    }

    public EnemyDefinition boss() {
        isBoss = true;
        return this;
    }

    public EnemyDefinition behavior(Hooks.EnemyBehavior enemyBehavior) {
        behavior = enemyBehavior;
        return this;
    }
}
