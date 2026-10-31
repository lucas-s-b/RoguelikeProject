package roguelike.systems;

import com.badlogic.gdx.math.MathUtils;
import java.util.Arrays;
import java.util.function.BiFunction;
import roguelike.definitions.StageDefinition;
import roguelike.model.RoomLayout;
import roguelike.model.TileType;

public final class LayoutGenerator {
    private static final int PLACEMENT_ATTEMPTS = 40;
    private static final int REGULAR_ROOM_PADDING = 2;
    private static final int BOSS_ROOM_PADDING = 3;
    private static final int BOSS_ROOM_WALL_LIMIT = 3;
    private static final int BOSS_ROOM_VOID_LIMIT = 2;
    private static final int SPAWN_CLEAR_HALF_WIDTH = 3;
    private static final int SPAWN_CLEAR_HALF_HEIGHT = 2;
    private static final int BOSS_CLEAR_ROWS = 5;

    private LayoutGenerator() {
    }

    public static void generate(RoomLayout layout, StageDefinition stage, boolean isBossRoom) {
        Arrays.fill(layout.tiles, TileType.FLOOR);
        int padding = isBossRoom ? BOSS_ROOM_PADDING : REGULAR_ROOM_PADDING;
        int wallCount = isBossRoom ? Math.min(BOSS_ROOM_WALL_LIMIT, stage.wallFeatures) : stage.wallFeatures;
        int voidCount = isBossRoom ? Math.min(BOSS_ROOM_VOID_LIMIT, stage.voidFeatures) : stage.voidFeatures;
        int centerColumn = layout.columns / 2;
        int centerRow = layout.rows / 2;
        for (int feature = 0; feature < wallCount + voidCount; feature++) {
            TileType type = feature < wallCount ? TileType.WALL : TileType.VOID;
            boolean[][] mask = featureMask(type);
            for (int attempt = 0; attempt < PLACEMENT_ATTEMPTS; attempt++) {
                int column = MathUtils.random(layout.columns - mask.length);
                int row = MathUtils.random(layout.rows - mask[0].length);
                if (canPlace(layout, mask, column, row, padding, isBossRoom)) {
                    for (int maskColumn = 0; maskColumn < mask.length; maskColumn++) {
                        for (int maskRow = 0; maskRow < mask[0].length; maskRow++) {
                            if (mask[maskColumn][maskRow]) {
                                layout.setTile(column + maskColumn, row + maskRow, type);
                            }
                        }
                    }
                    break;
                }
            }
        }
        int[] distances = new int[layout.tiles.length];
        Navigation.fillDistances(layout, distances, centerColumn, centerRow, false);
        for (int index = 0; index < layout.tiles.length; index++) {
            if (layout.tiles[index] == TileType.FLOOR && distances[index] == Navigation.UNREACHABLE) {
                layout.tiles[index] = TileType.WALL;
            }
        }
    }

    private static boolean[][] featureMask(TileType type) {
        BiFunction<Integer, Integer, boolean[][]> filled = (width, height) -> {
            boolean[][] mask = new boolean[width][height];
            for (boolean[] column : mask) {
                Arrays.fill(column, true);
            }
            return mask;
        };
        int style = MathUtils.random(3);
        if (type == TileType.VOID && MathUtils.random() < 0.2f) {
            int length = MathUtils.random(4, 6);
            return MathUtils.randomBoolean() ? filled.apply(length, 1) : filled.apply(1, length);
        }
        if (type == TileType.VOID) {
            int width = MathUtils.random(2, 5);
            int height = MathUtils.random(2, 4);
            boolean[][] mask = filled.apply(width, height);
            for (int corner = 0; corner < 4 && width >= 3 && height >= 3; corner++) {
                if (MathUtils.randomBoolean()) {
                    mask[corner % 2 == 0 ? 0 : width - 1][corner < 2 ? 0 : height - 1] = false;
                }
            }
            return mask;
        }
        if (style == 0) {
            int size = MathUtils.random(1, 2);
            return filled.apply(size, size);
        }
        if (style == 1) {
            int length = MathUtils.random(3, 5);
            return MathUtils.randomBoolean() ? filled.apply(length, 1) : filled.apply(1, length);
        }
        boolean[][] mask = filled.apply(3, 3);
        if (style == 2) {
            int cornerColumn = MathUtils.randomBoolean() ? 0 : 1;
            int cornerRow = MathUtils.randomBoolean() ? 0 : 1;
            for (int column = cornerColumn; column < cornerColumn + 2; column++) {
                for (int row = cornerRow; row < cornerRow + 2; row++) {
                    mask[column][row] = false;
                }
            }
            return mask;
        }
        mask[0][0] = false;
        mask[2][0] = false;
        mask[0][2] = false;
        mask[2][2] = false;
        return mask;
    }

    private static boolean canPlace(RoomLayout layout, boolean[][] mask, int column, int row, int padding, boolean isBossRoom) {
        int centerColumn = layout.columns / 2;
        int centerRow = layout.rows / 2;
        for (int maskColumn = 0; maskColumn < mask.length; maskColumn++) {
            for (int maskRow = 0; maskRow < mask[0].length; maskRow++) {
                if (!mask[maskColumn][maskRow]) {
                    continue;
                }
                int tileColumn = column + maskColumn;
                int tileRow = row + maskRow;
                boolean isNearPlayerSpawn = Math.abs(tileColumn - centerColumn) <= SPAWN_CLEAR_HALF_WIDTH && Math.abs(tileRow - centerRow) <= SPAWN_CLEAR_HALF_HEIGHT;
                boolean isNearBossSpawn = isBossRoom && Math.abs(tileColumn - centerColumn) <= SPAWN_CLEAR_HALF_WIDTH && tileRow >= layout.rows - BOSS_CLEAR_ROWS;
                if (isNearPlayerSpawn || isNearBossSpawn) {
                    return false;
                }
                for (int offsetColumn = -padding; offsetColumn <= padding; offsetColumn++) {
                    for (int offsetRow = -padding; offsetRow <= padding; offsetRow++) {
                        int checkColumn = tileColumn + offsetColumn;
                        int checkRow = tileRow + offsetRow;
                        if (!layout.isInside(checkColumn, checkRow) || layout.tileAt(checkColumn, checkRow) != TileType.FLOOR) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
}
