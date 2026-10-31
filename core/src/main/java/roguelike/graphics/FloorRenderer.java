package roguelike.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import roguelike.definitions.StageDefinition;
import roguelike.model.Decal;
import roguelike.model.FloorStyle;
import roguelike.model.GameWorld;
import roguelike.model.Projectile;
import roguelike.model.RoomLayout;
import roguelike.model.TileType;
import roguelike.model.Unit;
import roguelike.model.VisualEffect;
import roguelike.systems.RoomSystem;

public final class FloorRenderer {
    private static final Color WALL_SHADOW_COLOR = new Color(0f, 0f, 0f, 0.45f);
    private static final Color CLEAR_COLOR = new Color(0f, 0f, 0f, 0f);
    private static final Color PLAYER_LIGHT_COLOR = new Color(1f, 0.8f, 0.55f, 0.13f);
    private static final Color TORCH_CORE_COLOR = new Color(1f, 0.92f, 0.7f, 1f);
    private static final Color BLOCK_SHADOW_COLOR = new Color(0f, 0f, 0f, 0.35f);
    private static final Color MOSS_COLOR = new Color(0.24f, 0.46f, 0.22f, 1f);
    private static final Color STONE_PIT_COLOR = new Color(0.018f, 0.014f, 0.028f, 1f);
    private static final Color MOSS_PIT_COLOR = new Color(0.01f, 0.028f, 0.02f, 1f);
    private static final Color ABYSS_COLOR = new Color(0.012f, 0.012f, 0.045f, 1f);
    private static final Color LAVA_COLOR = new Color(0.8f, 0.22f, 0.04f, 1f);
    private static final Color LAVA_HOT_COLOR = new Color(1f, 0.62f, 0.12f, 1f);
    private static final Color LAVA_CRUST_COLOR = new Color(0.3f, 0.07f, 0.03f, 1f);
    private static final Color STAR_COLOR = new Color(0.85f, 0.9f, 1f, 1f);
    private static final Color NEBULA_COLOR = new Color(0.45f, 0.3f, 1f, 1f);
    private static final Color STONE_MIST_COLOR = new Color(0.5f, 0.4f, 0.8f, 1f);
    private static final Color MOSS_MIST_COLOR = new Color(0.35f, 0.9f, 0.55f, 1f);
    private static final float TILE_SIZE = RoomLayout.TILE_SIZE;
    private static final float TILE_GAP = 2f;
    private static final float TILE_BEVEL = 2f;
    private static final float WALL_THICKNESS = 14f;
    private static final float BLOCK_HEIGHT = 14f;
    private static final float BLOCK_SHADOW_OFFSET = 6f;
    private static final float PIT_DEPTH_BAND = 16f;
    private static final float TOP_WALL_SHADOW_DEPTH = 40f;
    private static final float SIDE_WALL_SHADOW_DEPTH = 22f;
    private static final float DECAL_FADE_SECONDS = 2f;
    private static final int TORCH_COUNT = 5;
    private static final Color drawColor = new Color();
    private static final Color edgeColor = new Color();

    private FloorRenderer() {
    }

    public static void draw(GameWorld world, ShapeRenderer shapes, SpriteBatch batch, GameTextures textures) {
        Rectangle arena = world.arena;
        RoomLayout layout = world.layout;
        StageDefinition stage = RoomSystem.stageFor(world.run.roomNumber);
        float arenaRight = arena.x + arena.width;
        float arenaTop = arena.y + arena.height;
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(stage.wallColor);
        shapes.rect(arena.x - WALL_THICKNESS, arena.y - WALL_THICKNESS, arena.width + WALL_THICKNESS * 2f, arena.height + WALL_THICKNESS * 2f);
        boolean hasGlowingSeams = stage.floorStyle == FloorStyle.ASTRAL;
        shapes.setColor(hasGlowingSeams ? drawColor.set(stage.wallEdgeColor).mul(0.3f, 0.3f, 0.36f, 1f) : stage.floorColor);
        shapes.rect(arena.x, arena.y, arena.width, arena.height);
        for (int row = 0; row < layout.rows; row++) {
            for (int column = 0; column < layout.columns; column++) {
                int tileHash = ((column * 73856093) ^ (row * 19349663) ^ (world.run.roomNumber * 83492791)) * 0x9E3779B1;
                tileHash = (tileHash ^ (tileHash >>> 16)) & 0x7FFFFFFF;
                float x = layout.originX + column * TILE_SIZE;
                float y = layout.originY + row * TILE_SIZE;
                TileType tile = layout.tileAt(column, row);
                if (tile == TileType.FLOOR) {
                    drawFloorTile(shapes, stage, x, y, tileHash);
                } else if (tile == TileType.VOID) {
                    drawVoidTile(shapes, stage, layout, column, row, tileHash, world.time);
                }
            }
        }
        shapes.rect(arena.x, arenaTop - TOP_WALL_SHADOW_DEPTH, arena.width, TOP_WALL_SHADOW_DEPTH, CLEAR_COLOR, CLEAR_COLOR, WALL_SHADOW_COLOR, WALL_SHADOW_COLOR);
        shapes.rect(arena.x, arena.y, arena.width, SIDE_WALL_SHADOW_DEPTH, WALL_SHADOW_COLOR, WALL_SHADOW_COLOR, CLEAR_COLOR, CLEAR_COLOR);
        shapes.rect(arena.x, arena.y, SIDE_WALL_SHADOW_DEPTH, arena.height, WALL_SHADOW_COLOR, CLEAR_COLOR, CLEAR_COLOR, WALL_SHADOW_COLOR);
        shapes.rect(arenaRight - SIDE_WALL_SHADOW_DEPTH, arena.y, SIDE_WALL_SHADOW_DEPTH, arena.height, CLEAR_COLOR, WALL_SHADOW_COLOR, WALL_SHADOW_COLOR, CLEAR_COLOR);
        drawWallBlocks(shapes, stage, layout);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(stage.wallEdgeColor);
        shapes.rect(arena.x, arena.y, arena.width, arena.height);
        shapes.rect(arena.x - WALL_THICKNESS, arena.y - WALL_THICKNESS, arena.width + WALL_THICKNESS * 2f, arena.height + WALL_THICKNESS * 2f);
        shapes.end();

        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.begin();
        for (Decal decal : world.decals) {
            if (layout.typeAt(decal.position.x, decal.position.y) != TileType.FLOOR) {
                continue;
            }
            float fade = Math.min(1f, decal.lifetimeRemaining / DECAL_FADE_SECONDS);
            Draw.sprite(batch, decal.isSoft ? textures.glow : textures.circle, decal.position.x, decal.position.y,
                    decal.width, decal.height, decal.rotation, decal.color, fade);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        boolean isLava = stage.floorStyle == FloorStyle.METAL;
        Color mistColor = switch (stage.floorStyle) {
            case STONE -> STONE_MIST_COLOR;
            case MOSS -> MOSS_MIST_COLOR;
            case METAL -> LAVA_HOT_COLOR;
            case ASTRAL -> NEBULA_COLOR;
        };
        for (int row = 0; row < layout.rows; row++) {
            for (int column = 0; column < layout.columns; column++) {
                if (layout.tileAt(column, row) != TileType.VOID) {
                    continue;
                }
                float flicker = 0.8f + 0.2f * MathUtils.sin(world.time * 3f + column * 1.3f + row * 2.1f);
                Draw.sprite(batch, textures.glow, layout.centerX(column), layout.centerY(row), 120f, 120f, 0f,
                        mistColor, (isLava ? 0.11f : 0.07f) * flicker);
            }
        }
        for (int torch = 0; torch < TORCH_COUNT; torch++) {
            float torchX = arena.x + arena.width * (torch + 0.5f) / TORCH_COUNT;
            float torchY = arenaTop + WALL_THICKNESS * 0.3f;
            float flicker = 0.85f + 0.1f * MathUtils.sin(world.time * 13f + torch * 1.7f) + 0.05f * MathUtils.sin(world.time * 31f + torch * 3.1f);
            Draw.sprite(batch, textures.glow, torchX, torchY - 50f, 320f * flicker, 230f * flicker, 0f, stage.torchColor, 0.09f);
            Draw.sprite(batch, textures.glow, torchX, torchY + 1f, 22f * flicker, 28f * flicker, 0f, stage.torchColor, 0.8f);
            Draw.sprite(batch, textures.glow, torchX, torchY, 9f, 12f * flicker, 0f, TORCH_CORE_COLOR, 1f);
        }
        Unit player = world.player();
        if (!player.isDead) {
            Draw.sprite(batch, textures.glow, player.position.x, player.position.y, 360f, 360f, 0f, PLAYER_LIGHT_COLOR, 1f);
        }
        for (Projectile projectile : world.projectiles) {
            float lightSize = projectile.radius * 16f;
            Draw.sprite(batch, textures.glow, projectile.position.x, projectile.position.y, lightSize, lightSize, 0f, projectile.color, 0.16f);
        }
        for (VisualEffect effect : world.effects) {
            float fade = effect.lifetimeRemaining / effect.totalLifetime;
            boolean isBlast = effect.shape == VisualEffect.Shape.CIRCLE && !effect.isTelegraph;
            if (isBlast || effect.growsOverTime) {
                float lightSize = effect.radius * 3f;
                Draw.sprite(batch, textures.glow, effect.position.x, effect.position.y, lightSize, lightSize, 0f, effect.color, 0.12f * fade);
            } else if (effect.shape == VisualEffect.Shape.LIGHTNING) {
                float lightSize = effect.position.dst(effect.endPosition) * 1.2f + 80f;
                float centerX = (effect.position.x + effect.endPosition.x) / 2f;
                float centerY = (effect.position.y + effect.endPosition.y) / 2f;
                Draw.sprite(batch, textures.glow, centerX, centerY, lightSize, lightSize, 0f, effect.color, 0.18f * fade);
            }
        }
        batch.end();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private static void drawFloorTile(ShapeRenderer shapes, StageDefinition stage, float x, float y, int tileHash) {
        float brightness = 0.82f + (tileHash % 100) / 100f * 0.3f;
        switch (stage.floorStyle) {
            case STONE -> {
                float tileLeft = x + TILE_GAP / 2f;
                float tileBottom = y + TILE_GAP / 2f;
                float tileSize = TILE_SIZE - TILE_GAP;
                shapes.setColor(drawColor.set(stage.tileColor).mul(brightness, brightness, brightness, 1f));
                shapes.rect(tileLeft, tileBottom, tileSize, tileSize);
                shapes.setColor(drawColor.set(stage.tileColor).mul(brightness * 1.3f, brightness * 1.3f, brightness * 1.35f, 1f));
                shapes.rect(tileLeft, tileBottom + tileSize - TILE_BEVEL, tileSize, TILE_BEVEL);
                shapes.setColor(drawColor.set(stage.tileColor).mul(brightness * 0.65f, brightness * 0.65f, brightness * 0.7f, 1f));
                shapes.rect(tileLeft, tileBottom, tileSize, TILE_BEVEL);
            }
            case MOSS -> {
                float half = TILE_SIZE / 2f;
                for (int stone = 0; stone < 4; stone++) {
                    float stoneShade = 0.78f + ((tileHash >> (stone * 3)) % 8) / 8f * 0.4f;
                    float stoneX = x + (stone % 2) * half + 1f;
                    float stoneY = y + (stone / 2) * half + 1f;
                    shapes.setColor(drawColor.set(stage.tileColor).mul(stoneShade, stoneShade, stoneShade, 1f));
                    shapes.rect(stoneX, stoneY, half - 2f, half - 2f);
                    shapes.setColor(drawColor.set(stage.tileColor).mul(stoneShade * 1.3f, stoneShade * 1.35f, stoneShade * 1.3f, 1f));
                    shapes.rect(stoneX, stoneY + half - 4f, half - 2f, TILE_BEVEL);
                }
                if (tileHash % 4 == 0) {
                    float mossX = x + 4f + (tileHash / 7) % 22;
                    float mossY = y + 4f + (tileHash / 11) % 22;
                    shapes.setColor(drawColor.set(MOSS_COLOR).lerp(stage.tileColor, 0.35f));
                    shapes.rect(mossX, mossY, 10f, 5f);
                    shapes.rect(mossX + 3f, mossY - 3f, 5f, 10f);
                }
            }
            case METAL -> {
                shapes.setColor(drawColor.set(stage.tileColor).mul(brightness, brightness, brightness, 1f));
                shapes.rect(x + 1f, y + 1f, TILE_SIZE - 2f, TILE_SIZE - 2f);
                shapes.setColor(drawColor.set(stage.tileColor).mul(brightness * 1.35f, brightness * 1.3f, brightness * 1.25f, 1f));
                shapes.rect(x + 1f, y + TILE_SIZE - 3f, TILE_SIZE - 2f, TILE_BEVEL);
                shapes.rect(x + 4f, y + 4f, 3f, 3f);
                shapes.rect(x + TILE_SIZE - 7f, y + 4f, 3f, 3f);
                shapes.rect(x + 4f, y + TILE_SIZE - 7f, 3f, 3f);
                shapes.rect(x + TILE_SIZE - 7f, y + TILE_SIZE - 7f, 3f, 3f);
                if (tileHash % 7 == 0) {
                    shapes.setColor(drawColor.set(stage.tileColor).mul(0.4f, 0.4f, 0.4f, 1f));
                    for (int slot = 0; slot < 3; slot++) {
                        shapes.rect(x + 10f, y + 12f + slot * 7f, TILE_SIZE - 20f, 3f);
                    }
                }
            }
            case ASTRAL -> {
                shapes.setColor(drawColor.set(stage.tileColor).mul(brightness, brightness, brightness, 1f));
                shapes.rect(x + TILE_GAP, y + TILE_GAP, TILE_SIZE - TILE_GAP * 2f, TILE_SIZE - TILE_GAP * 2f);
                shapes.setColor(drawColor.set(stage.tileColor).mul(brightness * 1.3f, brightness * 1.3f, brightness * 1.4f, 1f));
                shapes.rect(x + TILE_GAP, y + TILE_SIZE - TILE_GAP - TILE_BEVEL, TILE_SIZE - TILE_GAP * 2f, TILE_BEVEL);
                if (tileHash % 6 == 0) {
                    shapes.setColor(drawColor.set(stage.wallEdgeColor).mul(1f, 1f, 1f, 0.35f));
                    shapes.rect(x + 14f, y + 14f, 6f, 6f, 12f, 12f, 1f, 1f, 45f);
                }
            }
        }
    }

    private static void drawVoidTile(ShapeRenderer shapes, StageDefinition stage, RoomLayout layout, int column, int row, int tileHash, float time) {
        float x = layout.originX + column * TILE_SIZE;
        float y = layout.originY + row * TILE_SIZE;
        boolean isLava = stage.floorStyle == FloorStyle.METAL;
        Color baseColor = switch (stage.floorStyle) {
            case STONE -> STONE_PIT_COLOR;
            case MOSS -> MOSS_PIT_COLOR;
            case METAL -> LAVA_COLOR;
            case ASTRAL -> ABYSS_COLOR;
        };
        shapes.setColor(baseColor);
        shapes.rect(x, y, TILE_SIZE, TILE_SIZE);
        if (isLava) {
            for (int blob = 0; blob < 2; blob++) {
                float phase = (tileHash >> (blob * 4)) % 100 * 0.07f;
                float blobX = x + 10f + ((tileHash >> (blob * 5 + 2)) % 20) + MathUtils.sin(time * 1.3f + phase) * 4f;
                float blobY = y + 10f + ((tileHash >> (blob * 5 + 7)) % 20) + MathUtils.cos(time * 1.1f + phase) * 4f;
                shapes.setColor(drawColor.set(LAVA_HOT_COLOR).mul(1f, 1f, 1f, 0.55f + 0.35f * MathUtils.sin(time * 2f + phase)));
                shapes.circle(blobX, blobY, 6f + blob * 2f, 12);
            }
            if (tileHash % 4 == 0) {
                shapes.setColor(LAVA_CRUST_COLOR);
                shapes.rect(x + 6f + (tileHash / 13) % 18, y + 8f + (tileHash / 17) % 18, 12f, 6f);
            }
        } else if (stage.floorStyle == FloorStyle.ASTRAL) {
            for (int star = 0; star < 3; star++) {
                float twinkle = Math.max(0f, MathUtils.sin(time * 2f + (tileHash >> star) % 50));
                shapes.setColor(drawColor.set(STAR_COLOR).mul(1f, 1f, 1f, 0.25f + 0.75f * twinkle));
                shapes.rect(x + 4f + (tileHash >> (star * 4)) % 32, y + 4f + (tileHash >> (star * 4 + 2)) % 32, 2f, 2f);
            }
        }
        if (layout.tileAt(column, row + 1) != TileType.VOID) {
            edgeColor.set(isLava ? LAVA_CRUST_COLOR : drawColor.set(stage.tileColor).mul(0.95f, 0.95f, 1f, 1f));
            float bandBottom = y + TILE_SIZE - PIT_DEPTH_BAND;
            shapes.rect(x, bandBottom, TILE_SIZE, PIT_DEPTH_BAND, baseColor, baseColor, edgeColor, edgeColor);
        }
        if (layout.tileAt(column, row - 1) != TileType.VOID) {
            shapes.setColor(drawColor.set(stage.tileColor).mul(1.4f, 1.4f, 1.45f, 1f));
            shapes.rect(x, y, TILE_SIZE, TILE_BEVEL);
        }
        shapes.setColor(drawColor.set(baseColor).mul(0.6f, 0.6f, 0.6f, 1f));
        if (layout.tileAt(column - 1, row) != TileType.VOID) {
            shapes.rect(x, y, TILE_BEVEL, TILE_SIZE);
        }
        if (layout.tileAt(column + 1, row) != TileType.VOID) {
            shapes.rect(x + TILE_SIZE - TILE_BEVEL, y, TILE_BEVEL, TILE_SIZE);
        }
    }

    private static void drawWallBlocks(ShapeRenderer shapes, StageDefinition stage, RoomLayout layout) {
        shapes.setColor(BLOCK_SHADOW_COLOR);
        for (int row = 0; row < layout.rows; row++) {
            for (int column = 0; column < layout.columns; column++) {
                if (layout.tileAt(column, row) == TileType.WALL) {
                    shapes.rect(layout.originX + column * TILE_SIZE + BLOCK_SHADOW_OFFSET, layout.originY + row * TILE_SIZE - BLOCK_SHADOW_OFFSET, TILE_SIZE, TILE_SIZE);
                }
            }
        }
        for (int row = 0; row < layout.rows; row++) {
            for (int column = 0; column < layout.columns; column++) {
                if (layout.tileAt(column, row) != TileType.WALL) {
                    continue;
                }
                float x = layout.originX + column * TILE_SIZE;
                float y = layout.originY + row * TILE_SIZE;
                boolean hasFrontFace = layout.tileAt(column, row - 1) != TileType.WALL;
                float topBottom = hasFrontFace ? y + BLOCK_HEIGHT : y;
                if (hasFrontFace) {
                    shapes.setColor(drawColor.set(stage.wallColor).mul(0.62f, 0.62f, 0.66f, 1f));
                    shapes.rect(x, y, TILE_SIZE, BLOCK_HEIGHT);
                    shapes.setColor(drawColor.set(stage.wallColor).mul(0.38f, 0.38f, 0.42f, 1f));
                    shapes.rect(x, y, TILE_SIZE, TILE_BEVEL);
                    if (stage.floorStyle == FloorStyle.STONE || stage.floorStyle == FloorStyle.METAL) {
                        shapes.rect(x + 13f + (row % 2) * 7f, y + TILE_BEVEL, TILE_BEVEL, BLOCK_HEIGHT - TILE_BEVEL);
                    }
                    if (stage.floorStyle == FloorStyle.ASTRAL) {
                        shapes.setColor(drawColor.set(stage.wallEdgeColor).mul(1f, 1f, 1f, 0.8f));
                        shapes.rect(x, y + BLOCK_HEIGHT - TILE_BEVEL, TILE_SIZE, TILE_BEVEL);
                    }
                }
                float topHeight = y + TILE_SIZE - topBottom;
                shapes.setColor(drawColor.set(stage.wallColor).mul(1.25f, 1.25f, 1.28f, 1f));
                shapes.rect(x, topBottom, TILE_SIZE, topHeight);
                shapes.setColor(drawColor.set(stage.wallEdgeColor).mul(0.85f, 0.85f, 0.85f, 1f));
                if (layout.tileAt(column, row + 1) != TileType.WALL) {
                    shapes.rect(x, y + TILE_SIZE - TILE_BEVEL, TILE_SIZE, TILE_BEVEL);
                }
                if (layout.tileAt(column - 1, row) != TileType.WALL) {
                    shapes.rect(x, topBottom, TILE_BEVEL, topHeight);
                }
                shapes.setColor(drawColor.set(stage.wallColor).mul(0.75f, 0.75f, 0.8f, 1f));
                if (layout.tileAt(column + 1, row) != TileType.WALL) {
                    shapes.rect(x + TILE_SIZE - TILE_BEVEL, topBottom, TILE_BEVEL, topHeight);
                }
                switch (stage.floorStyle) {
                    case STONE -> {
                        shapes.rect(x + TILE_BEVEL, topBottom + topHeight / 2f, TILE_SIZE - TILE_BEVEL * 2f, TILE_BEVEL);
                        shapes.rect(x + 12f + (row % 2) * 14f, topBottom + topHeight / 2f, TILE_BEVEL, topHeight / 2f - TILE_BEVEL);
                    }
                    case MOSS -> {
                        shapes.setColor(MOSS_COLOR);
                        shapes.rect(x + 4f + (column * 7 + row * 3) % 16, y + TILE_SIZE - 7f, 14f, 4f);
                        shapes.rect(x + 20f - (column * 5 + row * 11) % 12, topBottom + 4f, 9f, 3f);
                    }
                    case METAL -> {
                        shapes.setColor(drawColor.set(stage.wallColor).mul(1.6f, 1.55f, 1.5f, 1f));
                        shapes.rect(x + 5f, topBottom + 4f, 3f, 3f);
                        shapes.rect(x + TILE_SIZE - 8f, topBottom + 4f, 3f, 3f);
                        shapes.rect(x + 5f, y + TILE_SIZE - 8f, 3f, 3f);
                        shapes.rect(x + TILE_SIZE - 8f, y + TILE_SIZE - 8f, 3f, 3f);
                    }
                    case ASTRAL -> {
                        shapes.setColor(drawColor.set(stage.wallEdgeColor).mul(1f, 1f, 1f, 0.55f));
                        float centerY = topBottom + topHeight / 2f;
                        shapes.rect(x + 14f, centerY - 6f, 6f, 6f, 12f, 12f, 1f, 1f, 45f);
                    }
                }
            }
        }
    }
}
