package roguelike.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import roguelike.RoguelikeGame;
import roguelike.content.GameText;
import roguelike.graphics.EffectRenderer;
import roguelike.graphics.PostProcessor;
import roguelike.model.GameWorld;
import roguelike.model.PerkFamily;
import roguelike.model.PerkInstance;
import roguelike.model.RunState;
import roguelike.model.SkillInstance;
import roguelike.model.Stat;
import roguelike.model.Unit;
import roguelike.systems.EffectSystem;
import roguelike.systems.PlayerControlSystem;
import roguelike.systems.RenderSystem;
import roguelike.systems.RoomSystem;
import roguelike.systems.SkillSystem;
import roguelike.systems.WeaponSystem;
import roguelike.systems.WorldUpdate;
import roguelike.ui.UiCanvas;

public final class DungeonScreen extends ScreenAdapter {
    private static final float MAXIMUM_FRAME_DELTA = 1f / 30f;
    private static final float SECONDS_BEFORE_REWARDS = 1.5f;
    private static final float SECONDS_BEFORE_GAME_OVER = 1.2f;
    private static final float HIT_STOP_TIME_SCALE = 0.1f;
    private static final float MAXIMUM_SHAKE_OFFSET = 16f;
    private static final float CENTER_X = RoguelikeGame.WORLD_WIDTH / 2f;
    private static final float HUD_HEIGHT = 96f;
    private static final float PANEL_Y = 10f;
    private static final float PANEL_HEIGHT = 78f;
    private static final float VITALS_X = 16f;
    private static final float VITALS_WIDTH = 300f;
    private static final float WEAPON_X = 328f;
    private static final float WEAPON_WIDTH = 208f;
    private static final float SKILLS_X = 548f;
    private static final float SKILL_WIDTH = 172f;
    private static final float SKILL_SPACING = 180f;
    private static final float LOW_HEALTH_FRACTION = 0.3f;
    private static final float HEARTBEAT_SECONDS = 0.8f;
    private static final float READY_FLASH_SECONDS = 0.35f;
    private static final float BANNER_DROP_SECONDS = 0.4f;
    private static final int PERK_ROWS_PER_COLUMN = 18;
    private static final float BUILD_ROW_HEIGHT = 26f;
    private static final Color HEALTH_COLOR = new Color(0.85f, 0.2f, 0.25f, 1f);
    private static final Color LOW_HEALTH_COLOR = new Color(1f, 0.5f, 0.45f, 1f);
    private static final Color SHIELD_COLOR = new Color(0.55f, 0.8f, 1f, 1f);
    private static final Color COOLDOWN_COLOR = new Color(0.03f, 0.02f, 0.05f, 0.72f);
    private static final Color STRIPE_COLOR = new Color(1f, 1f, 1f, 0.04f);
    private static final Color ROW_HOVER_COLOR = new Color(1f, 1f, 1f, 0.09f);

    private final RoguelikeGame game;
    private final RunState run;
    private final GameWorld world;
    private final Vector2 mouseWorldPosition = new Vector2();
    private final float[] ripples = new float[PostProcessor.MAXIMUM_RIPPLES * 4];
    private final float[] skillReadyFlash = new float[RunState.SKILL_SLOT_COUNT];
    private final boolean[] wasSkillCoolingDown = new boolean[RunState.SKILL_SLOT_COUNT];
    private final Color flashColor = new Color();
    private boolean isPaused;
    private float timeSincePlayerDied;
    private float time;
    private float displayedGold;

    public DungeonScreen(RoguelikeGame game, RunState run) {
        this.game = game;
        this.run = run;
        this.world = RoomSystem.createRoom(run);
        this.displayedGold = run.gold;
    }

    @Override
    public void hide() {
        Gdx.graphics.setCursor(game.art.arrowCursor);
    }

    @Override
    public void render(float delta) {
        time += delta;
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            isPaused = !isPaused;
        }
        Gdx.graphics.setCursor(isPaused ? game.art.arrowCursor : game.art.aimCursor);
        mouseWorldPosition.set(Gdx.input.getX(), Gdx.input.getY());
        game.viewport.unproject(mouseWorldPosition);
        if (!isPaused) {
            float frameDelta = Math.min(delta, MAXIMUM_FRAME_DELTA);
            boolean isHitStopped = world.hitStopRemaining > 0f;
            world.hitStopRemaining -= frameDelta;
            WorldUpdate.update(world, isHitStopped ? frameDelta * HIT_STOP_TIME_SCALE : frameDelta, mouseWorldPosition);
        }
        updateHudAnimations(delta);

        Camera camera = game.viewport.getCamera();
        float shakeAmount = world.screenShake * world.screenShake * MAXIMUM_SHAKE_OFFSET;
        float shakeX = shakeAmount * MathUtils.sin(world.time * 71f) * MathUtils.cos(world.time * 23f);
        float shakeY = shakeAmount * MathUtils.sin(world.time * 59f + 1.7f);
        camera.position.add(shakeX, shakeY, 0f);
        camera.update();
        game.postProcessor.beginScene(game.viewport);
        ScreenUtils.clear(0.03f, 0.025f, 0.045f, 1f);
        game.shapes.setProjectionMatrix(camera.combined);
        game.batch.setProjectionMatrix(camera.combined);
        RenderSystem.draw(world, game.shapes, game.batch, game.font, game.textures);
        EffectRenderer.collectRipples(world, ripples);
        game.postProcessor.endScene(game.viewport, world.screenShake, world.hurtFlash, world.screenFlash, world.time, ripples);
        camera.position.sub(shakeX, shakeY, 0f);
        camera.update();

        UiCanvas ui = game.ui;
        ui.begin();
        ui.rect(0f, 0f, RoguelikeGame.WORLD_WIDTH, HUD_HEIGHT, UiCanvas.PANEL_DARK_COLOR);
        ui.rect(0f, HUD_HEIGHT - UiCanvas.PIXEL, RoguelikeGame.WORLD_WIDTH, UiCanvas.PIXEL, UiCanvas.INK);
        drawVitals(ui);
        drawWeapon(ui);
        drawSkills(ui);
        drawBossBar(ui);
        drawRoomCleared(ui);
        if (Gdx.input.isKeyPressed(Input.Keys.TAB)) {
            drawBuildPanel(ui);
        }
        boolean isAbandonPressed = isPaused && drawPauseMenu(ui);
        ui.fadeIn(time);
        ui.end();

        if (isAbandonPressed) {
            game.showGameOver(run);
        } else if (world.player().isDead) {
            timeSincePlayerDied += delta;
            if (timeSincePlayerDied > SECONDS_BEFORE_GAME_OVER) {
                game.showGameOver(run);
            }
        } else if (world.isRoomCleared && world.timeSinceRoomCleared > SECONDS_BEFORE_REWARDS) {
            game.showRoomRewards(run);
        }
    }

    private void updateHudAnimations(float delta) {
        displayedGold += (run.gold - displayedGold) * Math.min(1f, delta * 8f);
        if (Math.abs(run.gold - displayedGold) < 0.5f) {
            displayedGold = run.gold;
        }
        for (int slot = 0; slot < run.skills.length; slot++) {
            boolean isCoolingDown = run.skills[slot] != null && run.skills[slot].cooldownRemaining > 0f;
            if (wasSkillCoolingDown[slot] && !isCoolingDown) {
                skillReadyFlash[slot] = READY_FLASH_SECONDS;
            }
            skillReadyFlash[slot] = Math.max(0f, skillReadyFlash[slot] - delta);
            wasSkillCoolingDown[slot] = isCoolingDown;
        }
    }

    private void drawVitals(UiCanvas ui) {
        Unit player = world.player();
        float barX = VITALS_X + 40f;
        float barWidth = VITALS_WIDTH - 52f;
        boolean isLowHealth = player.healthFraction() < LOW_HEALTH_FRACTION && !player.isDead;
        boolean isHeartbeat = isLowHealth && time % HEARTBEAT_SECONDS < 0.12f;
        ui.frame(VITALS_X, PANEL_Y, VITALS_WIDTH, PANEL_HEIGHT, UiCanvas.PANEL_COLOR, null);
        ui.icon(ui.art.heart, VITALS_X + 10f, PANEL_Y + 37f + (isHeartbeat ? UiCanvas.PIXEL : 0f), UiCanvas.TEXT_SIZE, Color.WHITE);
        Color healthColor = player.hitFlashTimeRemaining > 0f ? Color.WHITE : isHeartbeat ? LOW_HEALTH_COLOR : HEALTH_COLOR;
        ui.bar(barX, PANEL_Y + 34f, barWidth, 26f, player.healthFraction(), player.trailingHealthFraction, healthColor);
        if (player.shield > 0f) {
            float shieldFraction = player.shield / player.maxHealth();
            ui.bar(barX, PANEL_Y + 58f, barWidth, 10f, shieldFraction, shieldFraction, SHIELD_COLOR);
        }
        ui.textCentered(GameText.health(Math.max(0f, player.health), player.maxHealth()), barX + barWidth / 2f, PANEL_Y + 54f, UiCanvas.TEXT_SIZE, UiCanvas.TEXT_COLOR);

        boolean isCounting = run.gold - displayedGold >= 0.5f;
        float coinHop = isCounting && MathUtils.sin(time * 40f) > 0f ? UiCanvas.PIXEL : 0f;
        ui.icon(ui.art.coin, VITALS_X + 10f, PANEL_Y + 8f + coinHop, UiCanvas.TEXT_SIZE, Color.WHITE);
        ui.text(GameText.gold(Math.round(displayedGold)), barX, PANEL_Y + 26f, UiCanvas.TEXT_SIZE, EffectSystem.GOLD_COLOR);
        String roomLabel = GameText.room(RoomSystem.stageNumber(run.roomNumber), RoomSystem.roomInStage(run.roomNumber), RoomSystem.isBossRoom(run.roomNumber));
        ui.textRight(roomLabel, barX + barWidth, PANEL_Y + 26f, UiCanvas.TEXT_SIZE, UiCanvas.MUTED_TEXT_COLOR);
    }

    private void drawWeapon(UiCanvas ui) {
        Unit player = world.player();
        ui.frame(WEAPON_X, PANEL_Y, WEAPON_WIDTH, PANEL_HEIGHT, UiCanvas.PANEL_COLOR, null);
        ui.textWrapped(GameText.withLevel(run.weapon.definition.name, run.weapon.level), WEAPON_X + 12f, PANEL_Y + 68f,
                WEAPON_WIDTH - 24f, UiCanvas.TEXT_SIZE, run.weapon.definition.color, Align.left);
        ui.iconCentered(ui.art.emblem(run.weapon.definition.damageType), WEAPON_X + 24f, PANEL_Y + 22f, UiCanvas.TEXT_SIZE, Color.WHITE);
        float weaponCooldown = WeaponSystem.cooldownFor(player, run.weapon);
        float readyFraction = 1f - Math.max(0f, player.attackCooldownRemaining) / weaponCooldown;
        ui.bar(WEAPON_X + 44f, PANEL_Y + 16f, WEAPON_WIDTH - 56f, 12f, readyFraction, readyFraction, UiCanvas.GOOD_COLOR);
    }

    private void drawSkills(UiCanvas ui) {
        Unit player = world.player();
        for (int slot = 0; slot < run.skills.length; slot++) {
            SkillInstance skill = run.skills[slot];
            float slotX = SKILLS_X + slot * SKILL_SPACING;
            float flash = skillReadyFlash[slot] / READY_FLASH_SECONDS;
            ui.frame(slotX, PANEL_Y, SKILL_WIDTH, PANEL_HEIGHT, UiCanvas.PANEL_COLOR, flash > 0f ? flashColor.set(1f, 1f, 1f, flash) : null);
            ui.keycap(slotX + 10f, PANEL_Y + 46f, Input.Keys.toString(PlayerControlSystem.SKILL_KEYS[slot]));
            if (skill == null) {
                ui.text(GameText.EMPTY_SLOT, slotX + 10f, PANEL_Y + 42f, UiCanvas.TEXT_SIZE, UiCanvas.MUTED_TEXT_COLOR);
                continue;
            }
            ui.iconCentered(ui.art.emblem(skill.definition.damageType), slotX + 54f, PANEL_Y + 59f, UiCanvas.TEXT_SIZE, Color.WHITE);
            ui.textWrapped(GameText.withLevel(skill.definition.name, skill.level), slotX + 10f, PANEL_Y + 42f,
                    SKILL_WIDTH - 20f, UiCanvas.TEXT_SIZE, skill.definition.color, Align.left);
            if (skill.cooldownRemaining > 0f) {
                float cooldownFraction = Math.min(1f, skill.cooldownRemaining / SkillSystem.cooldownFor(player, skill.definition));
                float coveredHeight = UiCanvas.snap((PANEL_HEIGHT - UiCanvas.PIXEL * 2f) * cooldownFraction);
                ui.rect(slotX + UiCanvas.PIXEL, PANEL_Y + PANEL_HEIGHT - UiCanvas.PIXEL - coveredHeight, SKILL_WIDTH - UiCanvas.PIXEL * 2f, coveredHeight, COOLDOWN_COLOR);
                ui.textRight(String.format("%.1f", skill.cooldownRemaining), slotX + SKILL_WIDTH - 10f, PANEL_Y + 72f, UiCanvas.HEADING_SIZE, UiCanvas.TEXT_COLOR);
            }
        }
    }

    private void drawBossBar(UiCanvas ui) {
        for (Unit unit : world.units) {
            if (!unit.isBoss || unit.isDead) {
                continue;
            }
            float plaqueX = CENTER_X - 310f;
            ui.frame(plaqueX, 636f, 620f, 60f, UiCanvas.PANEL_COLOR, null);
            ui.icon(ui.art.skull, plaqueX + 12f, 645f, UiCanvas.TEXT_SIZE, Color.WHITE);
            ui.textCentered(unit.enemyDefinition.name, CENTER_X, 688f, UiCanvas.TEXT_SIZE, UiCanvas.TEXT_COLOR);
            ui.bar(plaqueX + 46f, 644f, 562f, 20f, unit.healthFraction(), unit.trailingHealthFraction, unit.color);
        }
    }

    private void drawRoomCleared(UiCanvas ui) {
        if (!world.isRoomCleared) {
            return;
        }
        float progress = Math.min(1f, world.timeSinceRoomCleared / BANNER_DROP_SECONDS);
        float bannerY = 400f + UiCanvas.snap((1f - Interpolation.swingOut.apply(progress)) * 160f);
        ui.frame(CENTER_X - 210f, bannerY, 420f, 76f, UiCanvas.PANEL_COLOR, UiCanvas.GOOD_COLOR);
        ui.iconCentered(ui.art.star, CENTER_X - 176f, bannerY + 38f, UiCanvas.TEXT_SIZE, Color.WHITE);
        ui.iconCentered(ui.art.star, CENTER_X + 176f, bannerY + 38f, UiCanvas.TEXT_SIZE, Color.WHITE);
        ui.title(GameText.ROOM_CLEARED, CENTER_X, bannerY + 54f, UiCanvas.HEADING_SIZE, UiCanvas.GOOD_COLOR);
    }

    private void drawBuildPanel(UiCanvas ui) {
        Unit player = world.player();
        ui.dim(0.5f);
        ui.frame(120f, 112f, 1040f, 592f, UiCanvas.PANEL_COLOR, null);
        ui.title(GameText.YOUR_BUILD, CENTER_X, 690f, UiCanvas.HEADING_SIZE, UiCanvas.TEXT_COLOR);
        List<PerkFamily> ownedFamilies = new ArrayList<>();
        for (PerkFamily family : PerkFamily.values()) {
            if (family != PerkFamily.GENERAL && PerkSystem.familyCount(player, family) > 0) {
                ownedFamilies.add(family);
            }
        }
        float perksTop = 632f;
        if (!ownedFamilies.isEmpty()) {
            ui.heading(GameText.FAMILIES, BUILD_LEFT, perksTop, BUILD_SECTION_WIDTH, UiCanvas.MUTED_TEXT_COLOR);
            float chipX = BUILD_LEFT;
            float chipTop = perksTop - 28f;
            for (PerkFamily family : ownedFamilies) {
                String chipText = GameText.familyName(family) + " " + PerkSystem.familyCount(player, family);
                float chipWidth = 26f + ui.textWidth(chipText, UiCanvas.TEXT_SIZE);
                if (chipX + chipWidth > BUILD_LEFT + BUILD_SECTION_WIDTH) {
                    chipX = BUILD_LEFT;
                    chipTop -= BUILD_ROW_HEIGHT;
                }
                ui.iconCentered(ui.art.familyIcon(family), chipX + 10f, chipTop - 7f, UiCanvas.TEXT_SIZE, Color.WHITE);
                ui.text(chipText, chipX + 26f, chipTop, UiCanvas.TEXT_SIZE, UiCanvas.TEXT_COLOR);
                chipX += chipWidth + 20f;
            }
            perksTop = chipTop - BUILD_ROW_HEIGHT - 12f;
        }
        ui.heading(GameText.PERKS, BUILD_LEFT, perksTop, 600f, UiCanvas.MUTED_TEXT_COLOR);
        if (player.perks.isEmpty()) {
            ui.text(GameText.NO_PERKS_YET, BUILD_LEFT, perksTop - 28f, UiCanvas.TEXT_SIZE, UiCanvas.MUTED_TEXT_COLOR);
        }
        PerkInstance hoveredPerk = null;
        for (int index = 0; index < player.perks.size(); index++) {
            PerkInstance perk = player.perks.get(index);
            float columnX = BUILD_LEFT + (index / PERK_ROWS_PER_COLUMN) * 308f;
            float rowTop = perksTop - 28f - (index % PERK_ROWS_PER_COLUMN) * BUILD_ROW_HEIGHT;
            if (ui.isHovered(columnX - 6f, rowTop - 21f, 296f, BUILD_ROW_HEIGHT)) {
                hoveredPerk = perk;
                ui.rect(columnX - 6f, rowTop - 21f, 296f, BUILD_ROW_HEIGHT, ROW_HOVER_COLOR);
            }
            PerkFamily family = perk.definition.family;
            Color iconTint = family == PerkFamily.GENERAL ? perk.definition.rarity.color : Color.WHITE;
            ui.iconCentered(ui.art.familyIcon(family), columnX + 10f, rowTop - 7f, UiCanvas.TEXT_SIZE, iconTint);
            ui.text(perk.definition.name, columnX + 26f, rowTop, UiCanvas.TEXT_SIZE, perk.definition.rarity.color);
            ui.textRight(perk.stacks + " / " + perk.definition.maxStacks, columnX + 284f, rowTop, UiCanvas.TEXT_SIZE, UiCanvas.MUTED_TEXT_COLOR);
        }

        ui.heading(GameText.STATS, 788f, 632f, 344f, UiCanvas.MUTED_TEXT_COLOR);
        float rowTop = 604f;
        int row = 0;
        for (Stat stat : Stat.values()) {
            String statName = GameText.statName(stat);
            if (statName == null) {
                continue;
            }
            if (row % 2 == 0) {
                ui.rect(782f, rowTop - 21f, 356f, BUILD_ROW_HEIGHT, STRIPE_COLOR);
            }
            ui.text(statName, 794f, rowTop, UiCanvas.TEXT_SIZE, UiCanvas.TEXT_COLOR);
            ui.textRight(formatStat(stat, player.stat(stat)), 1126f, rowTop, UiCanvas.TEXT_SIZE, UiCanvas.TEXT_COLOR);
            rowTop -= BUILD_ROW_HEIGHT;
            row++;
        }
        if (hoveredPerk != null) {
            ui.tooltip(hoveredPerk.definition.description, 320f);
        }
    }

    private boolean drawPauseMenu(UiCanvas ui) {
        ui.dim(0.55f);
        ui.frame(CENTER_X - 180f, 252f, 360f, 268f, UiCanvas.PANEL_COLOR, null);
        ui.title(GameText.PAUSED, CENTER_X, 494f, UiCanvas.HEADING_SIZE, UiCanvas.TEXT_COLOR);
        if (ui.button(CENTER_X - 130f, 368f, 260f, 56f, GameText.RESUME, null, true)) {
            isPaused = false;
        }
        return ui.button(CENTER_X - 130f, 288f, 260f, 56f, GameText.ABANDON_RUN, null, true);
    }

    private static String formatStat(Stat stat, float value) {
        return switch (stat) {
            case DODGE_CHANCE, DAMAGE_PERCENT, SKILL_DAMAGE_PERCENT, ATTACK_SPEED_PERCENT, CRIT_CHANCE,
                    CRIT_DAMAGE_PERCENT, COOLDOWN_REDUCTION_PERCENT, AREA_PERCENT, GOLD_PERCENT -> Math.round(value) + "%";
            case HEALTH_REGEN, LIFE_ON_HIT -> String.format("%.1f", value);
            default -> String.valueOf(Math.round(value));
        };
    }
}
