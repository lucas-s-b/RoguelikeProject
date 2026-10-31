package roguelike.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import roguelike.model.DamageType;
import roguelike.model.PerkFamily;

public final class PixelArt implements Disposable {
    public static final int WALL_TILE_SIZE = 32;
    private static final int ATLAS_SIZE = 128;
    private static final int CURSOR_SIZE = 32;
    private static final int CURSOR_SCALE = 2;
    private static final int AIM_CURSOR_HOTSPOT = 15;
    private static final Color BRICK_COLOR = new Color(0.11f, 0.09f, 0.15f, 1f);
    private static final Color MORTAR_COLOR = new Color(0.055f, 0.045f, 0.08f, 1f);

    private static final String HEART = """
            .kkk...kkk.
            kwprk.krrrk
            kprrrkrrrrk
            krrrrrrrrRk
            krrrrrrrrRk
            .krrrrrrRk.
            ..krrrrRk..
            ...krrRk...
            ....kRk....
            .....k.....
            """;
    private static final String COIN = """
            ..kkkkkk..
            .kllyyyyk.
            klyyyyyyYk
            klyyYYyyYk
            kyyyYYyyYk
            kyyyYYyyYk
            kyyyYYyyYk
            kYyyyyyyYk
            .kYYYYYYk.
            ..kkkkkk..
            """;
    private static final String SKULL = """
            ..kkkkkkk..
            .kwssssssk.
            kwsssssssgk
            kskkssskkgk
            kskkssskkgk
            kssssksssgk
            .ksssssssk.
            ..ksksksk..
            ..kkkkkkk..
            """;
    private static final String DOOR = """
            ...kkkk...
            ..knnnnk..
            .knNnnNnk.
            knnNnnNnnk
            knnNnnNnnk
            knnNnnNnnk
            knnNnnNnyk
            knnNnnNnnk
            knnNnnNnnk
            knnNnnNnnk
            kkkkkkkkkk
            """;
    private static final String STAR = """
            .....k.....
            ....kyk....
            ....kyk....
            kkkkylykkkk
            kyyyylyyyYk
            .kyyyyyyYk.
            ..kyyyyyk..
            ..kyyyyYk..
            .kyyYkYyYk.
            .kyYk.kYYk.
            .kkk...kkk.
            """;
    private static final String POTION = """
            ...kkk...
            ...knk...
            ..kkkkk..
            ...ksk...
            ..kswsk..
            .kprrrrk.
            kprrrrrRk
            krrrrrrRk
            krrrrrRRk
            .kRRRRRk.
            ..kkkkk..
            """;
    private static final String DICE = """
            .kkkkkkkkk.
            kwwwwwwwwsk
            kwwwwwwwwsk
            kwwkwwwkwsk
            kwwwwwwwwsk
            kwwwwkwwwsk
            kwwwwwwwwsk
            kwwkwwwkwsk
            kwwwwwwwwsk
            kssssssssgk
            .kkkkkkkkk.
            """;
    private static final String GEM = """
            ..kkkkk..
            .k33322k.
            k3332221k
            k2222111k
            .k22211k.
            ..k221k..
            ...k2k...
            ....k....
            """;
    private static final String SHIELD = """
            kkkkkkkkkk
            ksssssssgk
            ksbbbbbbgk
            ksbcbbbbgk
            ksbbbbbBgk
            ksbbbbbBgk
            .ksbbbBgk.
            .ksbbBBgk.
            ..ksbBgk..
            ...ksgk...
            ....kk....
            """;
    private static final String CHEVRON = """
            kk...
            k3k..
            k33k.
            k333k
            k33k.
            k3k..
            kk...
            """;
    private static final String SWORD = """
            .....k.....
            ....kwk....
            ...kwsgk...
            ...kwsgk...
            ...kwsgk...
            ...kwsgk...
            ...kwsgk...
            .kkkwsgkkk.
            kyyyyyyyyYk
            .kkkknkkkk.
            ....knk....
            ....kyk....
            ....kkk....
            """;
    private static final String SPARKLE = """
            .....k.....
            ....kuk....
            ....kmk....
            ...kmumk...
            .kkmmumMkk.
            kmmmuwuMMMk
            .kkmMuMMkk.
            ...kMuMk...
            ....kMk....
            ....kMk....
            .....k.....
            """;
    private static final String FLAME = """
            .....k.....
            ....kok....
            ....koOk...
            ...koyOk...
            ..koyyoOk..
            ..koylyOk..
            .koyllyoOk.
            .koyllyoOk.
            .koyylyoOk.
            ..kOoyoOk..
            ...kOOOk...
            ....kkk....
            """;
    private static final String CRYSTAL = """
            .....k.....
            ....kck....
            ...kcwbk...
            ...kcwbk...
            ..kccwbBk..
            ..kcwwbBk..
            ..kcwbbBk..
            ..kcwbBBk..
            ...kcbBk...
            ...kcbBk...
            ....kbk....
            .....k.....
            """;
    private static final String DROPLET = """
            ....k....
            ...kvk...
            ...kvk...
            ..kvvVk..
            .kevvvVk.
            .kevvvVk.
            kevvvvvVk
            kevvvvvVk
            kvvvvvVVk
            .kVVVVVk.
            ..kkkkk..
            """;
    private static final String BOLT = """
            ....kkkkk.
            ...klyyYk.
            ...klyYk..
            ..klyYk...
            ..klyykkkk
            .klyyyyyYk
            .kkkkyyYk.
            ....kyYk..
            ...kyYk...
            ...kyk....
            ..kYk.....
            ..kk......
            """;
    private static final String BLOOD = """
            ....k...k..
            ...krk.krk.
            ...krk.kRk.
            ..krrRk.k..
            .kprrrRk...
            .kprrrRk...
            kprrrrrRk..
            krrrrrrRk..
            krrrrrRRk..
            .kRRRRRk...
            ..kkkkk....
            """;
    private static final String ARROW_CURSOR = """
            k...........
            kk..........
            kwk.........
            kwwk........
            kwwwk.......
            kwwwwk......
            kwwwwwk.....
            kwwwwwwk....
            kwwwwwwwk...
            kwwwwwwwwk..
            kwwwwwkkkkk.
            kwwkwwk.....
            kwk.kwwk....
            kk..kwwk....
            .....kwwk...
            ......kk....
            """;
    private static final String AIM_CURSOR = """
            ......kkk......
            ......kwk......
            ......kwk......
            ......kwk......
            ......kkk......
            ...............
            kkkkk..k..kkkkk
            kwwwk.kwk.kwwwk
            kkkkk..k..kkkkk
            ...............
            ......kkk......
            ......kwk......
            ......kwk......
            ......kwk......
            ......kkk......
            """;

    public final Texture atlas;
    public final Texture wall;
    public final TextureRegion pixel;
    public final TextureRegion heart;
    public final TextureRegion coin;
    public final TextureRegion skull;
    public final TextureRegion door;
    public final TextureRegion star;
    public final TextureRegion potion;
    public final TextureRegion dice;
    public final TextureRegion gem;
    public final TextureRegion chevron;
    public final TextureRegion sword;
    public final TextureRegion sparkle;
    public final TextureRegion flame;
    public final TextureRegion crystal;
    public final TextureRegion droplet;
    public final TextureRegion bolt;
    public final TextureRegion blood;
    public final TextureRegion shield;
    public final Cursor arrowCursor;
    public final Cursor aimCursor;

    public PixelArt() {
        String[] arts = {HEART, COIN, SKULL, DOOR, STAR, POTION, DICE, GEM, CHEVRON, SWORD, SPARKLE, FLAME, CRYSTAL, DROPLET, BOLT, BLOOD, SHIELD};
        int[][] bounds = new int[arts.length][];
        Pixmap pixmap = new Pixmap(ATLAS_SIZE, ATLAS_SIZE, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(Color.WHITE);
        pixmap.fillRectangle(0, 0, 3, 3);
        int cursorX = 4;
        int cursorY = 0;
        int rowHeight = 0;
        for (int index = 0; index < arts.length; index++) {
            Pixmap image = paintPixmap(arts[index], 1, 0);
            if (cursorX + image.getWidth() >= ATLAS_SIZE) {
                cursorX = 0;
                cursorY += rowHeight + 1;
                rowHeight = 0;
            }
            pixmap.drawPixmap(image, cursorX, cursorY);
            bounds[index] = new int[] {cursorX, cursorY, image.getWidth(), image.getHeight()};
            cursorX += image.getWidth() + 1;
            rowHeight = Math.max(rowHeight, image.getHeight());
            image.dispose();
        }
        atlas = new Texture(pixmap);
        atlas.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        pixmap.dispose();
        TextureRegion[] regions = new TextureRegion[arts.length];
        for (int index = 0; index < arts.length; index++) {
            regions[index] = new TextureRegion(atlas, bounds[index][0], bounds[index][1], bounds[index][2], bounds[index][3]);
        }
        pixel = new TextureRegion(atlas, 1, 1, 1, 1);
        heart = regions[0];
        coin = regions[1];
        skull = regions[2];
        door = regions[3];
        star = regions[4];
        potion = regions[5];
        dice = regions[6];
        gem = regions[7];
        chevron = regions[8];
        sword = regions[9];
        sparkle = regions[10];
        flame = regions[11];
        crystal = regions[12];
        droplet = regions[13];
        bolt = regions[14];
        blood = regions[15];
        shield = regions[16];
        wall = createWallTexture();
        Pixmap arrowImage = paintPixmap(ARROW_CURSOR, CURSOR_SCALE, CURSOR_SIZE);
        arrowCursor = Gdx.graphics.newCursor(arrowImage, 0, 0);
        arrowImage.dispose();
        Pixmap aimImage = paintPixmap(AIM_CURSOR, CURSOR_SCALE, CURSOR_SIZE);
        aimCursor = Gdx.graphics.newCursor(aimImage, AIM_CURSOR_HOTSPOT, AIM_CURSOR_HOTSPOT);
        aimImage.dispose();
    }

    public TextureRegion emblem(DamageType damageType) {
        return switch (damageType) {
            case PHYSICAL -> sword;
            case MAGIC -> sparkle;
            case FIRE -> flame;
            case ICE -> crystal;
            case POISON -> droplet;
            case LIGHTNING -> bolt;
            case BLEED -> blood;
        };
    }

    public TextureRegion familyIcon(PerkFamily family) {
        return switch (family) {
            case GENERAL -> gem;
            case FIRE -> flame;
            case POISON -> droplet;
            case BLEED -> blood;
            case FROST -> crystal;
            case LIGHTNING -> bolt;
            case CRITICAL -> star;
            case SKILL -> sparkle;
            case SLAYER -> skull;
            case DEFENSE -> shield;
            case GOLD -> coin;
        };
    }

    @Override
    public void dispose() {
        atlas.dispose();
        wall.dispose();
        arrowCursor.dispose();
        aimCursor.dispose();
    }

    private static Texture createWallTexture() {
        Pixmap pixmap = new Pixmap(WALL_TILE_SIZE, WALL_TILE_SIZE, Pixmap.Format.RGBA8888);
        Color pixelColor = new Color();
        for (int pixelY = 0; pixelY < WALL_TILE_SIZE; pixelY++) {
            int brickRow = pixelY / 8;
            int brickY = pixelY % 8;
            for (int pixelX = 0; pixelX < WALL_TILE_SIZE; pixelX++) {
                int shiftedX = (pixelX + (brickRow % 2) * 8) % WALL_TILE_SIZE;
                int brickX = shiftedX % 16;
                if (brickX == 15 || brickY == 7) {
                    pixmap.drawPixel(pixelX, pixelY, Color.rgba8888(MORTAR_COLOR));
                    continue;
                }
                int brickNumber = brickRow * 2 + shiftedX / 16;
                float brickShade = 0.86f + ((brickNumber * 37 + 11) % 7) * 0.05f;
                float edgeShade = brickY == 0 ? 1.25f : brickY == 6 ? 0.8f : 1f;
                float speckle = (pixelX * 31 + pixelY * 17 + brickNumber * 5) % 19 == 0 ? 0.75f : 1f;
                float shade = brickShade * edgeShade * speckle;
                pixelColor.set(BRICK_COLOR).mul(shade, shade, shade, 1f);
                pixmap.drawPixel(pixelX, pixelY, Color.rgba8888(pixelColor));
            }
        }
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        pixmap.dispose();
        return texture;
    }

    private static Pixmap paintPixmap(String art, int scale, int minimumSize) {
        String[] rows = art.strip().split("\n");
        int width = rows[0].length();
        Pixmap pixmap = new Pixmap(Math.max(minimumSize, width * scale), Math.max(minimumSize, rows.length * scale), Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        for (int row = 0; row < rows.length; row++) {
            if (rows[row].length() != width) {
                throw new IllegalStateException("Every row of a pixel art image must have the same width.");
            }
            for (int column = 0; column < width; column++) {
                char key = rows[row].charAt(column);
                int color = switch (key) {
                    case '.' -> 0;
                    case 'k' -> 0x120E18FF;
                    case 'w' -> 0xFFFFFFFF;
                    case 'r' -> 0xE0394AFF;
                    case 'R' -> 0x8F1D36FF;
                    case 'p' -> 0xFF9AA2FF;
                    case 'y' -> 0xFFCC3DFF;
                    case 'Y' -> 0xC27A12FF;
                    case 'l' -> 0xFFF3A0FF;
                    case 'o' -> 0xFF8A26FF;
                    case 'O' -> 0xB84A10FF;
                    case 'b' -> 0x5AA8FFFF;
                    case 'B' -> 0x2A4F9EFF;
                    case 'c' -> 0xB8E8FFFF;
                    case 's' -> 0xE4E2ECFF;
                    case 'g' -> 0x9C98ADFF;
                    case 'n' -> 0x9A6A3AFF;
                    case 'N' -> 0x5E3A20FF;
                    case 'v' -> 0x7BE05AFF;
                    case 'V' -> 0x2F8A3AFF;
                    case 'e' -> 0xC8FF9EFF;
                    case 'm' -> 0xB77DFFFF;
                    case 'M' -> 0x6A3AA8FF;
                    case 'u' -> 0xE6CCFFFF;
                    case '1' -> 0x737373FF;
                    case '2' -> 0xBFBFBFFF;
                    case '3' -> 0xFFFFFFFF;
                    default -> throw new IllegalStateException("Unknown pixel art color key: " + key);
                };
                if (color != 0) {
                    pixmap.setColor(color);
                    pixmap.fillRectangle(column * scale, row * scale, scale, scale);
                }
            }
        }
        return pixmap;
    }
}
