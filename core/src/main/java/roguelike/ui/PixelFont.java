package roguelike.ui;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public final class PixelFont {
    public static final int CAP_HEIGHT = 7;
    public static final int LINE_HEIGHT = 10;
    public static final char NON_BREAKING_SPACE = ' ';
    private static final int GLYPH_ROWS = 9;
    private static final int X_HEIGHT = 5;
    private static final int SPACE_ADVANCE = 4;
    private static final int LETTER_SPACING = 1;
    private static final int CELL_WIDTH = 7;
    private static final int CELL_HEIGHT = GLYPH_ROWS + 1;
    private static final int ATLAS_COLUMNS = 16;
    private static final int ATLAS_SIZE = 128;
    private static final String GLYPHS = """
            A .###.|#...#|#...#|#####|#...#|#...#|#...#
            B ####.|#...#|#...#|####.|#...#|#...#|####.
            C .###.|#...#|#....|#....|#....|#...#|.###.
            D ####.|#...#|#...#|#...#|#...#|#...#|####.
            E #####|#....|#....|####.|#....|#....|#####
            F #####|#....|#....|####.|#....|#....|#....
            G .###.|#...#|#....|#.###|#...#|#...#|.###.
            H #...#|#...#|#...#|#####|#...#|#...#|#...#
            I ###|.#.|.#.|.#.|.#.|.#.|###
            J ..##|...#|...#|...#|...#|#..#|.##.
            K #...#|#..#.|#.#..|##...|#.#..|#..#.|#...#
            L #...|#...|#...|#...|#...|#...|####
            M #...#|##.##|#.#.#|#.#.#|#...#|#...#|#...#
            N #...#|#...#|##..#|#.#.#|#..##|#...#|#...#
            O .###.|#...#|#...#|#...#|#...#|#...#|.###.
            P ####.|#...#|#...#|####.|#....|#....|#....
            Q .###.|#...#|#...#|#...#|#.#.#|#..#.|.##.#
            R ####.|#...#|#...#|####.|#.#..|#..#.|#...#
            S .###.|#...#|#....|.###.|....#|#...#|.###.
            T #####|..#..|..#..|..#..|..#..|..#..|..#..
            U #...#|#...#|#...#|#...#|#...#|#...#|.###.
            V #...#|#...#|#...#|#...#|#...#|.#.#.|..#..
            W #...#|#...#|#...#|#.#.#|#.#.#|##.##|#...#
            X #...#|#...#|.#.#.|..#..|.#.#.|#...#|#...#
            Y #...#|#...#|.#.#.|..#..|..#..|..#..|..#..
            Z #####|....#|...#.|..#..|.#...|#....|#####
            a ....|....|.##.|...#|.###|#..#|.###
            b #...|#...|###.|#..#|#..#|#..#|###.
            c ....|....|.###|#...|#...|#...|.###
            d ...#|...#|.###|#..#|#..#|#..#|.###
            e ....|....|.##.|#..#|####|#...|.###
            f .##|#..|###|#..|#..|#..|#..
            g ....|....|.###|#..#|#..#|#..#|.###|...#|.##.
            h #...|#...|###.|#..#|#..#|#..#|#..#
            i #|.|#|#|#|#|#
            j .#|..|.#|.#|.#|.#|.#|.#|#.
            k #...|#...|#..#|#.#.|##..|#.#.|#..#
            l #.|#.|#.|#.|#.|#.|.#
            m .....|.....|##.#.|#.#.#|#.#.#|#.#.#|#.#.#
            n ....|....|###.|#..#|#..#|#..#|#..#
            o ....|....|.##.|#..#|#..#|#..#|.##.
            p ....|....|###.|#..#|#..#|#..#|###.|#...|#...
            q ....|....|.###|#..#|#..#|#..#|.###|...#|...#
            r ...|...|#.#|##.|#..|#..|#..
            s ....|....|.###|#...|.##.|...#|###.
            t ...|.#.|###|.#.|.#.|.#.|..#
            u ....|....|#..#|#..#|#..#|#..#|.###
            v .....|.....|#...#|#...#|.#.#.|.#.#.|..#..
            w .....|.....|#...#|#...#|#.#.#|#.#.#|.#.#.
            x ....|....|#..#|#..#|.##.|#..#|#..#
            y ....|....|#..#|#..#|#..#|#..#|.###|...#|.##.
            z ....|....|####|...#|.##.|#...|####
            0 .##.|#..#|#..#|#..#|#..#|#..#|.##.
            1 .#.|##.|.#.|.#.|.#.|.#.|###
            2 .##.|#..#|...#|..#.|.#..|#...|####
            3 ###.|...#|...#|.##.|...#|...#|###.
            4 #..#|#..#|#..#|####|...#|...#|...#
            5 ####|#...|#...|###.|...#|...#|###.
            6 .##.|#...|#...|###.|#..#|#..#|.##.
            7 ####|...#|...#|..#.|.#..|.#..|.#..
            8 .##.|#..#|#..#|.##.|#..#|#..#|.##.
            9 .##.|#..#|#..#|.###|...#|...#|.##.
            ! #|#|#|#|#|.|#
            " #.#|#.#|...|...|...|...|...
            # .#.#.|.#.#.|#####|.#.#.|#####|.#.#.|.#.#.
            $ ..#..|.####|#.#..|.###.|..#.#|####.|..#..
            % ##..#|##..#|...#.|..#..|.#...|#..##|#..##
            & .##..|#..#.|#.#..|.#...|#.#.#|#..#.|.##.#
            ' #|#|.|.|.|.|.
            ( .#|#.|#.|#.|#.|#.|.#
            ) #.|.#|.#|.#|.#|.#|#.
            * ...|#.#|.#.|#.#|...|...|...
            + ...|...|.#.|###|.#.|...|...
            , ..|..|..|..|..|..|.#|#.
            - ...|...|...|###|...|...|...
            . .|.|.|.|.|.|#
            / ..#|..#|.#.|.#.|.#.|#..|#..
            : .|.|.|#|.|.|#
            ; ..|..|..|.#|..|..|.#|#.
            < ...|..#|.#.|#..|.#.|..#|...
            = ...|...|###|...|###|...|...
            > ...|#..|.#.|..#|.#.|#..|...
            ? .##.|#..#|...#|..#.|.#..|....|.#..
            @ .###.|#...#|#.###|#.#.#|#.##.|#....|.###.
            [ ##|#.|#.|#.|#.|#.|##
            \\ #..|#..|.#.|.#.|.#.|..#|..#
            ] ##|.#|.#|.#|.#|.#|##
            ^ .#.|#.#|...|...|...|...|...
            _ ....|....|....|....|....|....|....|####
            ` #.|.#|..|..|..|..|..
            { ..#|.#.|.#.|#..|.#.|.#.|..#
            | #|#|#|#|#|#|#
            } #..|.#.|.#.|..#|.#.|.#.|#..
            ~ ....|....|.#.#|#.#.|....|....|....
            """;

    private PixelFont() {
    }

    public static BitmapFont create() {
        String[] lines = GLYPHS.strip().split("\n");
        Pixmap pixmap = new Pixmap(ATLAS_SIZE, ATLAS_SIZE, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        BitmapFont.BitmapFontData data = new BitmapFont.BitmapFontData();
        for (int index = 0; index < lines.length; index++) {
            char character = lines[index].charAt(0);
            String[] rows = lines[index].substring(2).split("\\|");
            int width = rows[0].length();
            int cellX = (index % ATLAS_COLUMNS) * CELL_WIDTH;
            int cellY = (index / ATLAS_COLUMNS) * CELL_HEIGHT;
            for (int row = 0; row < rows.length; row++) {
                if (rows[row].length() != width) {
                    throw new IllegalStateException("Every row of the glyph " + character + " must have the same width.");
                }
                for (int column = 0; column < width; column++) {
                    if (rows[row].charAt(column) == '#') {
                        pixmap.drawPixel(cellX + column, cellY + row, 0xFFFFFFFF);
                    }
                }
            }
            BitmapFont.Glyph glyph = new BitmapFont.Glyph();
            glyph.id = character;
            glyph.srcX = cellX;
            glyph.srcY = cellY;
            glyph.width = width;
            glyph.height = GLYPH_ROWS;
            glyph.yoffset = -GLYPH_ROWS;
            glyph.xadvance = width + LETTER_SPACING;
            data.setGlyph(character, glyph);
        }
        for (char blank : new char[] {' ', NON_BREAKING_SPACE}) {
            BitmapFont.Glyph glyph = new BitmapFont.Glyph();
            glyph.id = blank;
            glyph.xadvance = SPACE_ADVANCE;
            data.setGlyph(blank, glyph);
        }
        data.missingGlyph = data.getGlyph('?');
        data.lineHeight = LINE_HEIGHT;
        data.capHeight = CAP_HEIGHT;
        data.xHeight = X_HEIGHT;
        data.descent = CAP_HEIGHT - GLYPH_ROWS;
        data.down = -LINE_HEIGHT;
        data.spaceXadvance = SPACE_ADVANCE;
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        pixmap.dispose();
        BitmapFont font = new BitmapFont(data, new TextureRegion(texture), true);
        font.setOwnsTexture(true);
        return font;
    }
}
