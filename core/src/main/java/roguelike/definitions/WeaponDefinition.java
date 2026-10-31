package roguelike.definitions;

import com.badlogic.gdx.graphics.Color;
import roguelike.model.DamageType;

public final class WeaponDefinition {
    public final String id;
    public String name = "";
    public String description = "";
    public int price = 50;
    public float cooldown = 0.5f;
    public float damage = 10f;
    public DamageType damageType = DamageType.PHYSICAL;
    public final Color color = new Color(Color.WHITE);
    public boolean isSoldInShop = true;
    public Hooks.WeaponAttack attack;

    private WeaponDefinition(String id) {
        this.id = id;
    }

    public static WeaponDefinition create(String id) {
        return new WeaponDefinition(id);
    }

    public WeaponDefinition name(String weaponName) {
        name = weaponName;
        return this;
    }

    public WeaponDefinition description(String weaponDescription) {
        description = weaponDescription;
        return this;
    }

    public WeaponDefinition price(int goldPrice) {
        price = goldPrice;
        return this;
    }

    public WeaponDefinition cooldown(float secondsBetweenAttacks) {
        cooldown = secondsBetweenAttacks;
        return this;
    }

    public WeaponDefinition damage(float baseDamage, DamageType type) {
        damage = baseDamage;
        damageType = type;
        return this;
    }

    public WeaponDefinition color(float red, float green, float blue) {
        color.set(red, green, blue, 1f);
        return this;
    }

    public WeaponDefinition notSoldInShop() {
        isSoldInShop = false;
        return this;
    }

    public WeaponDefinition attack(Hooks.WeaponAttack weaponAttack) {
        attack = weaponAttack;
        return this;
    }
}
