package roguelike.model;

import roguelike.definitions.WeaponDefinition;

public final class WeaponInstance {
    public final WeaponDefinition definition;
    public int level = 1;
    public int attackCount;

    public WeaponInstance(WeaponDefinition definition) {
        this.definition = definition;
    }
}
