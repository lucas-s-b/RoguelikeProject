package roguelike.content;

import roguelike.content.perks.DefensivePerks;
import roguelike.content.perks.FamilyPerks;
import roguelike.content.perks.LegendaryPerks;
import roguelike.content.perks.OnHitPerks;
import roguelike.content.perks.OnKillPerks;
import roguelike.content.perks.SkillPerks;
import roguelike.content.perks.StatPerks;

public final class Content {
    private Content() {
    }

    public static void loadAll() {
        StatusContent.register();
        WeaponContent.register();
        SkillContent.register();
        EnemyContent.register();
        EnemyTraits.register();
        StageContent.register();
        OnHitPerks.register();
        OnKillPerks.register();
        SkillPerks.register();
        StatPerks.register();
        DefensivePerks.register();
        LegendaryPerks.register();
        FamilyPerks.register();
    }
}
