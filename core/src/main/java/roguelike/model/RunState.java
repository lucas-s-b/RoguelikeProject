package roguelike.model;

public final class RunState {
    public static final int SKILL_SLOT_COUNT = 4;

    public Unit player;
    public WeaponInstance weapon;
    public final SkillInstance[] skills = new SkillInstance[SKILL_SLOT_COUNT];
    public int gold;
    public int roomNumber = 1;
    public int roomsCleared;
    public int enemiesKilled;
}
