package roguelike.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;

public final class UiCanvas {
    public static final float PIXEL = 2f;
    public static final float TEXT_SIZE = 2f;
    public static final float HEADING_SIZE = 4f;
    public static final float TITLE_SIZE = 8f;
    public static final float KEYCAP_WIDTH = 24f;
    public static final float KEYCAP_HEIGHT = 26f;
    public static final Color INK = new Color(0.07f, 0.055f, 0.094f, 1f);
    public static final Color PANEL_COLOR = new Color(0.16f, 0.13f, 0.21f, 1f);
    public static final Color PANEL_DARK_COLOR = new Color(0.1f, 0.08f, 0.135f, 1f);
    public static final Color HIGHLIGHTED_PANEL_COLOR = new Color(0.23f, 0.19f, 0.3f, 1f);
    public static final Color DISABLED_PANEL_COLOR = new Color(0.12f, 0.11f, 0.14f, 1f);
    public static final Color TEXT_COLOR = new Color(0.95f, 0.92f, 0.85f, 1f);
    public static final Color MUTED_TEXT_COLOR = new Color(0.62f, 0.58f, 0.7f, 1f);
    public static final Color ACCENT_COLOR = new Color(1f, 0.82f, 0.38f, 1f);
    public static final Color GOOD_COLOR = new Color(0.45f, 0.9f, 0.5f, 1f);
    public static final Color BAD_COLOR = new Color(1f, 0.4f, 0.4f, 1f);
    private static final Color SHADOW_COLOR = new Color(0f, 0f, 0f, 0.45f);
    private static final Color BUTTON_COLOR = new Color(0.29f, 0.235f, 0.38f, 1f);
    private static final Color BUTTON_HOVER_COLOR = new Color(0.38f, 0.31f, 0.5f, 1f);
    private static final Color BUTTON_LIP_COLOR = new Color(0.15f, 0.11f, 0.2f, 1f);
    private static final Color DISABLED_BUTTON_COLOR = new Color(0.17f, 0.16f, 0.2f, 1f);
    private static final Color DISABLED_ICON_COLOR = new Color(0.5f, 0.5f, 0.5f, 0.6f);
    private static final Color BAR_BACK_COLOR = new Color(0.06f, 0.045f, 0.08f, 1f);
    private static final Color BAR_TRAIL_COLOR = new Color(1f, 0.9f, 0.7f, 1f);
    private static final Color KEY_COLOR = new Color(0.86f, 0.82f, 0.9f, 1f);
    private static final Color KEY_LIP_COLOR = new Color(0.5f, 0.46f, 0.58f, 1f);
    private static final Color TOOLTIP_COLOR = new Color(0.09f, 0.07f, 0.12f, 0.97f);
    private static final Color VIGNETTE_COLOR = new Color(0f, 0f, 0f, 0.13f);
    private static final Color EMBER_HOT_COLOR = new Color(1f, 0.75f, 0.35f, 1f);
    private static final Color EMBER_COOL_COLOR = new Color(0.75f, 0.2f, 0.25f, 1f);
    private static final float BUTTON_LIP = PIXEL * 2f;
    private static final float TITLE_OUTLINE = PIXEL * 2f;
    private static final int TITLE_DEPTH = 2;
    private static final float FADE_SECONDS = 0.32f;
    private static final int FADE_STEPS = 4;
    private static final int EMBER_COUNT = 48;
    private static final int VIGNETTE_STEPS = 5;
    private static final float VIGNETTE_STEP_SIZE = 20f;

    private final SpriteBatch batch;
    private final BitmapFont font;
    private final Viewport viewport;
    private final GlyphLayout layout = new GlyphLayout();
    private final Color shadeColor = new Color();
    public final PixelArt art;
    public final Vector2 mouse = new Vector2();
    public boolean isClicked;
    public boolean isMouseDown;
    public float time;

    public UiCanvas(SpriteBatch batch, BitmapFont font, Viewport viewport, PixelArt art) {
        this.batch = batch;
        this.font = font;
        this.viewport = viewport;
        this.art = art;
    }

    public void begin() {
        time += Gdx.graphics.getDeltaTime();
        mouse.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(mouse);
        isClicked = Gdx.input.justTouched();
        isMouseDown = Gdx.input.isTouched();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
    }

    public void end() {
        batch.end();
        batch.setColor(Color.WHITE);
        font.getData().setScale(1f);
        font.setColor(Color.WHITE);
    }

    public static float snap(float value) {
        return Math.round(value / PIXEL) * PIXEL;
    }

    public boolean isHovered(float x, float y, float width, float height) {
        return mouse.x >= x && mouse.x <= x + width && mouse.y >= y && mouse.y <= y + height;
    }

    public void rect(float x, float y, float width, float height, Color color) {
        batch.setColor(color);
        batch.draw(art.pixel, x, y, width, height);
    }

    public void frame(float x, float y, float width, float height, Color fill, Color border) {
        float inset = border == null ? PIXEL : PIXEL * 2f;
        rect(x + PIXEL, y - PIXEL * 2f, width - PIXEL * 2f, PIXEL * 2f, SHADOW_COLOR);
        rect(x + PIXEL, y, width - PIXEL * 2f, height, INK);
        rect(x, y + PIXEL, width, height - PIXEL * 2f, INK);
        if (border != null) {
            rect(x + PIXEL, y + PIXEL, width - PIXEL * 2f, height - PIXEL * 2f, border);
        }
        rect(x + inset, y + inset, width - inset * 2f, height - inset * 2f, fill);
        rect(x + inset, y + height - inset - PIXEL, width - inset * 2f, PIXEL, shadeColor.set(fill).lerp(Color.WHITE, 0.1f));
        rect(x + inset, y + inset, width - inset * 2f, PIXEL, shadeColor.set(fill).lerp(Color.BLACK, 0.3f));
    }

    public void corners(float x, float y, float width, float height, Color color) {
        float spread = PIXEL * 2f + (MathUtils.sin(time * 7f) > 0f ? PIXEL : 0f);
        float arm = PIXEL * 6f;
        float left = x - spread;
        float right = x + width + spread - PIXEL;
        float bottom = y - spread;
        float top = y + height + spread - PIXEL;
        rect(left, top, arm, PIXEL, color);
        rect(left, top - arm + PIXEL, PIXEL, arm, color);
        rect(right - arm + PIXEL, top, arm, PIXEL, color);
        rect(right, top - arm + PIXEL, PIXEL, arm, color);
        rect(left, bottom, arm, PIXEL, color);
        rect(left, bottom, PIXEL, arm, color);
        rect(right - arm + PIXEL, bottom, arm, PIXEL, color);
        rect(right, bottom, PIXEL, arm, color);
    }

    public void bar(float x, float y, float width, float height, float fraction, float trailingFraction, Color fillColor) {
        float innerWidth = width - PIXEL * 2f;
        float innerHeight = height - PIXEL * 2f;
        float fillWidth = (float) Math.floor(innerWidth * MathUtils.clamp(fraction, 0f, 1f) / PIXEL) * PIXEL;
        float trailingWidth = (float) Math.floor(innerWidth * MathUtils.clamp(trailingFraction, 0f, 1f) / PIXEL) * PIXEL;
        rect(x + PIXEL, y, innerWidth, height, INK);
        rect(x, y + PIXEL, width, innerHeight, INK);
        rect(x + PIXEL, y + PIXEL, innerWidth, innerHeight, BAR_BACK_COLOR);
        if (trailingWidth > fillWidth) {
            rect(x + PIXEL + fillWidth, y + PIXEL, trailingWidth - fillWidth, innerHeight, BAR_TRAIL_COLOR);
        }
        if (fillWidth > 0f) {
            rect(x + PIXEL, y + PIXEL, fillWidth, innerHeight, fillColor);
            rect(x + PIXEL, y + height - PIXEL * 2f, fillWidth, PIXEL, shadeColor.set(fillColor).lerp(Color.WHITE, 0.45f));
            rect(x + PIXEL, y + PIXEL, fillWidth, PIXEL, shadeColor.set(fillColor).lerp(Color.BLACK, 0.35f));
        }
    }

    public float textWidth(String text, float size) {
        font.getData().setScale(size);
        layout.setText(font, text);
        return layout.width;
    }

    public float textHeight(String text, float width, float size) {
        font.getData().setScale(size);
        layout.setText(font, text, Color.WHITE, width, Align.left, true);
        return layout.height;
    }

    public void text(String text, float x, float topY, float size, Color color) {
        drawText(text, x, topY, 0f, size, color, Align.left, false);
    }

    public void textCentered(String text, float centerX, float topY, float size, Color color) {
        drawText(text, snap(centerX - textWidth(text, size) / 2f), topY, 0f, size, color, Align.left, false);
    }

    public void textRight(String text, float rightX, float topY, float size, Color color) {
        drawText(text, rightX - textWidth(text, size), topY, 0f, size, color, Align.left, false);
    }

    public void textWrapped(String text, float x, float topY, float width, float size, Color color, int align) {
        drawText(text, x, topY, width, size, color, align, true);
    }

    public void title(String text, float centerX, float topY, float size, Color color) {
        float x = snap(centerX - textWidth(text, size) / 2f);
        font.setColor(INK);
        for (int offsetX = -1; offsetX <= 1; offsetX++) {
            for (int offsetY = -TITLE_DEPTH - 1; offsetY <= 1; offsetY++) {
                font.draw(batch, text, x + offsetX * TITLE_OUTLINE, topY + offsetY * TITLE_OUTLINE);
            }
        }
        font.setColor(shadeColor.set(color).lerp(INK, 0.55f));
        for (int depth = 1; depth <= TITLE_DEPTH; depth++) {
            font.draw(batch, text, x, topY - depth * TITLE_OUTLINE);
        }
        font.setColor(color);
        font.draw(batch, text, x, topY);
    }

    public void heading(String text, float x, float topY, float width, Color color) {
        drawText(text, x, topY, 0f, TEXT_SIZE, color, Align.left, false);
        float ruleX = x + textWidth(text, TEXT_SIZE) + PIXEL * 4f;
        float ruleY = snap(topY - PixelFont.CAP_HEIGHT * TEXT_SIZE / 2f);
        rect(ruleX, ruleY, x + width - ruleX, PIXEL, shadeColor.set(color).mul(1f, 1f, 1f, 0.4f));
        rect(ruleX, ruleY - PIXEL, x + width - ruleX, PIXEL, SHADOW_COLOR);
    }

    public void icon(TextureRegion region, float x, float y, float size, Color tint) {
        batch.setColor(tint);
        batch.draw(region, x, y, region.getRegionWidth() * size, region.getRegionHeight() * size);
    }

    public void iconCentered(TextureRegion region, float centerX, float centerY, float size, Color tint) {
        float width = region.getRegionWidth() * size;
        float height = region.getRegionHeight() * size;
        batch.setColor(tint);
        batch.draw(region, snap(centerX - width / 2f), snap(centerY - height / 2f), width, height);
    }

    public void keycap(float x, float y, String label) {
        rect(x + PIXEL, y, KEYCAP_WIDTH - PIXEL * 2f, KEYCAP_HEIGHT, INK);
        rect(x, y + PIXEL, KEYCAP_WIDTH, KEYCAP_HEIGHT - PIXEL * 2f, INK);
        rect(x + PIXEL, y + PIXEL, KEYCAP_WIDTH - PIXEL * 2f, BUTTON_LIP, KEY_LIP_COLOR);
        rect(x + PIXEL, y + PIXEL + BUTTON_LIP, KEYCAP_WIDTH - PIXEL * 2f, KEYCAP_HEIGHT - PIXEL * 2f - BUTTON_LIP, KEY_COLOR);
        float labelX = snap(x + (KEYCAP_WIDTH - textWidth(label, TEXT_SIZE)) / 2f);
        font.setColor(INK);
        font.draw(batch, label, labelX, y + KEYCAP_HEIGHT - PIXEL * 2f);
    }

    public boolean button(float x, float y, float width, float height, String label, TextureRegion icon, boolean isEnabled) {
        boolean isHovered = isEnabled && isHovered(x, y, width, height);
        boolean isPressed = isHovered && isMouseDown;
        float boxHeight = isPressed ? height - BUTTON_LIP : height;
        float faceBottom = isPressed ? y + PIXEL : y + PIXEL + BUTTON_LIP;
        float faceHeight = height - PIXEL * 2f - BUTTON_LIP;
        Color faceColor = !isEnabled ? DISABLED_BUTTON_COLOR : isHovered ? BUTTON_HOVER_COLOR : BUTTON_COLOR;
        rect(x + PIXEL, y - PIXEL * 2f, width - PIXEL * 2f, PIXEL * 2f, SHADOW_COLOR);
        rect(x + PIXEL, y, width - PIXEL * 2f, boxHeight, INK);
        rect(x, y + PIXEL, width, boxHeight - PIXEL * 2f, INK);
        if (!isPressed) {
            rect(x + PIXEL, y + PIXEL, width - PIXEL * 2f, BUTTON_LIP, BUTTON_LIP_COLOR);
        }
        rect(x + PIXEL, faceBottom, width - PIXEL * 2f, faceHeight, faceColor);
        rect(x + PIXEL, faceBottom + faceHeight - PIXEL, width - PIXEL * 2f, PIXEL, shadeColor.set(faceColor).lerp(Color.WHITE, 0.2f));

        float iconWidth = icon == null ? 0f : icon.getRegionWidth() * TEXT_SIZE + PIXEL * 4f;
        float labelWidth = textWidth(label, TEXT_SIZE);
        float contentX = snap(x + (width - iconWidth - labelWidth) / 2f);
        float centerY = faceBottom + faceHeight / 2f;
        if (icon != null) {
            batch.setColor(isEnabled ? Color.WHITE : DISABLED_ICON_COLOR);
            batch.draw(icon, contentX, snap(centerY - icon.getRegionHeight() * TEXT_SIZE / 2f),
                    icon.getRegionWidth() * TEXT_SIZE, icon.getRegionHeight() * TEXT_SIZE);
        }
        Color labelColor = !isEnabled ? MUTED_TEXT_COLOR : isHovered ? ACCENT_COLOR : TEXT_COLOR;
        drawText(label, contentX + iconWidth, snap(centerY + PixelFont.CAP_HEIGHT * TEXT_SIZE / 2f), 0f, TEXT_SIZE, labelColor, Align.left, false);
        if (isHovered) {
            float chevronWidth = art.chevron.getRegionWidth() * TEXT_SIZE;
            float chevronHeight = art.chevron.getRegionHeight() * TEXT_SIZE;
            float chevronY = snap(centerY - chevronHeight / 2f);
            float nudge = MathUtils.sin(time * 9f) > 0f ? PIXEL : 0f;
            float leftX = contentX - PIXEL * 5f - chevronWidth - nudge;
            float rightX = contentX + iconWidth + labelWidth + PIXEL * 5f + nudge;
            batch.setColor(ACCENT_COLOR);
            batch.draw(art.chevron, leftX, chevronY, chevronWidth, chevronHeight);
            batch.draw(art.chevron, rightX + chevronWidth, chevronY, -chevronWidth, chevronHeight);
        }
        return isHovered && isClicked;
    }

    public void tooltip(String text, float maximumWidth) {
        float textWidth = Math.min(maximumWidth, textWidth(text, TEXT_SIZE));
        float boxWidth = textWidth + PIXEL * 10f;
        float boxHeight = textHeight(text, textWidth, TEXT_SIZE) + PIXEL * 12f;
        float boxX = snap(Math.min(mouse.x + 20f, viewport.getWorldWidth() - boxWidth - 8f));
        float boxY = snap(Math.max(mouse.y - 20f - boxHeight, 8f));
        rect(boxX + PIXEL, boxY - PIXEL * 2f, boxWidth - PIXEL * 2f, PIXEL * 2f, SHADOW_COLOR);
        rect(boxX + PIXEL, boxY, boxWidth - PIXEL * 2f, boxHeight, INK);
        rect(boxX, boxY + PIXEL, boxWidth, boxHeight - PIXEL * 2f, INK);
        rect(boxX + PIXEL, boxY + PIXEL, boxWidth - PIXEL * 2f, boxHeight - PIXEL * 2f, TOOLTIP_COLOR);
        drawText(text, boxX + PIXEL * 5f, boxY + boxHeight - PIXEL * 5f, textWidth, TEXT_SIZE, TEXT_COLOR, Align.left, true);
    }

    public void backdrop(Color tint) {
        float width = viewport.getWorldWidth();
        float height = viewport.getWorldHeight();
        float tileSize = PixelArt.WALL_TILE_SIZE * PIXEL;
        float scrollX = snap(time * 6f) / tileSize;
        float scrollY = snap(time * 3f) / tileSize;
        batch.setColor(tint);
        batch.draw(art.wall, 0f, 0f, width, height, scrollX, scrollY + height / tileSize, scrollX + width / tileSize, scrollY);
        for (int ember = 0; ember < EMBER_COUNT; ember++) {
            float seed = ember * 7.31f;
            float speed = 16f + hashFraction(seed * 3.7f) * 34f;
            float travel = (time * speed + hashFraction(seed * 1.9f) * height) % (height + 40f);
            float life = travel / (height + 40f);
            float emberX = hashFraction(seed * 5.3f) * width + MathUtils.sin(time * 1.4f + seed) * 14f;
            float size = ember % 4 == 0 ? PIXEL * 2f : PIXEL;
            shadeColor.set(EMBER_HOT_COLOR).lerp(EMBER_COOL_COLOR, life);
            shadeColor.a = (1f - life) * 0.9f;
            rect(snap(emberX), snap(travel - 20f), size, size, shadeColor);
        }
        for (int step = 1; step <= VIGNETTE_STEPS; step++) {
            float depth = step * VIGNETTE_STEP_SIZE;
            rect(0f, 0f, width, depth, VIGNETTE_COLOR);
            rect(0f, height - depth, width, depth, VIGNETTE_COLOR);
            rect(0f, depth, depth, height - depth * 2f, VIGNETTE_COLOR);
            rect(width - depth, depth, depth, height - depth * 2f, VIGNETTE_COLOR);
        }
    }

    public void dim(float alpha) {
        rect(0f, 0f, viewport.getWorldWidth(), viewport.getWorldHeight(), shadeColor.set(INK.r, INK.g, INK.b, alpha));
    }

    public void fadeIn(float screenTime) {
        if (screenTime >= FADE_SECONDS) {
            return;
        }
        float steppedProgress = (float) Math.floor(screenTime / FADE_SECONDS * FADE_STEPS) / FADE_STEPS;
        rect(0f, 0f, viewport.getWorldWidth(), viewport.getWorldHeight(), shadeColor.set(INK.r, INK.g, INK.b, 1f - steppedProgress));
    }

    private void drawText(String text, float x, float topY, float width, float size, Color color, int align, boolean wrap) {
        font.getData().setScale(size);
        font.setColor(INK.r, INK.g, INK.b, color.a * 0.85f);
        font.draw(batch, text, x, topY - size, width, align, wrap);
        font.setColor(color);
        font.draw(batch, text, x, topY, width, align, wrap);
    }

    private static float hashFraction(float seed) {
        double hashed = Math.sin(seed) * 43758.5453;
        return (float) (hashed - Math.floor(hashed));
    }
}
