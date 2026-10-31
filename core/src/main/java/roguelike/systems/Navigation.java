package roguelike.systems;

import com.badlogic.gdx.math.Vector2;
import java.util.ArrayDeque;
import java.util.Arrays;
import roguelike.model.GameWorld;
import roguelike.model.RoomLayout;
import roguelike.model.TileType;
import roguelike.model.Unit;

public final class Navigation {
    public static final int UNREACHABLE = Integer.MAX_VALUE;
    private static final float SIGHT_STEP = 10f;
    private static final float PATH_WIDTH_FACTOR = 0.85f;
    private static final int[] NEIGHBOR_COLUMNS = {1, -1, 0, 0, 1, 1, -1, -1};
    private static final int[] NEIGHBOR_ROWS = {0, 0, 1, -1, 1, -1, 1, -1};

    private Navigation() {
    }

    public static void update(GameWorld world) {
        RoomLayout layout = world.layout;
        Unit player = world.player();
        int column = layout.columnOf(player.position.x);
        int row = layout.rowOf(player.position.y);
        fillDistances(layout, layout.groundDistances, column, row, false);
        fillDistances(layout, layout.airDistances, column, row, true);
    }

    public static void fillDistances(RoomLayout layout, int[] distances, int startColumn, int startRow, boolean canCrossVoid) {
        Arrays.fill(distances, UNREACHABLE);
        if (!layout.isInside(startColumn, startRow)) {
            return;
        }
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        int startIndex = startRow * layout.columns + startColumn;
        distances[startIndex] = 0;
        queue.add(startIndex);
        while (!queue.isEmpty()) {
            int index = queue.poll();
            int column = index % layout.columns;
            int row = index / layout.columns;
            for (int neighbor = 0; neighbor < 4; neighbor++) {
                int nextColumn = column + NEIGHBOR_COLUMNS[neighbor];
                int nextRow = row + NEIGHBOR_ROWS[neighbor];
                if (!layout.isInside(nextColumn, nextRow)) {
                    continue;
                }
                TileType tile = layout.tileAt(nextColumn, nextRow);
                int nextIndex = nextRow * layout.columns + nextColumn;
                boolean isOpen = tile == TileType.FLOOR || (canCrossVoid && tile == TileType.VOID);
                if (isOpen && distances[nextIndex] == UNREACHABLE) {
                    distances[nextIndex] = distances[index] + 1;
                    queue.add(nextIndex);
                }
            }
        }
    }

    public static Vector2 towardPlayer(GameWorld world, Unit unit) {
        RoomLayout layout = world.layout;
        Vector2 target = world.player().position;
        Vector2 direction = new Vector2(target).sub(unit.position);
        float length = direction.len();
        if (length < 0.001f) {
            return direction.setZero();
        }
        boolean canCrossVoid = unit.isFlying;
        float sideX = -direction.y / length * unit.radius * PATH_WIDTH_FACTOR;
        float sideY = direction.x / length * unit.radius * PATH_WIDTH_FACTOR;
        boolean isPathClear = true;
        for (int side = -1; side <= 1 && isPathClear; side++) {
            isPathClear = openFraction(layout, unit.position.x + sideX * side, unit.position.y + sideY * side,
                    target.x + sideX * side, target.y + sideY * side, canCrossVoid) >= 1f;
        }
        if (isPathClear) {
            return direction.scl(1f / length);
        }
        int[] distances = canCrossVoid ? layout.airDistances : layout.groundDistances;
        int column = layout.columnOf(unit.position.x);
        int row = layout.rowOf(unit.position.y);
        int bestDistance = layout.isInside(column, row) ? distances[row * layout.columns + column] : UNREACHABLE;
        int bestColumn = -1;
        int bestRow = -1;
        for (int neighbor = 0; neighbor < NEIGHBOR_COLUMNS.length; neighbor++) {
            int nextColumn = column + NEIGHBOR_COLUMNS[neighbor];
            int nextRow = row + NEIGHBOR_ROWS[neighbor];
            if (!layout.isInside(nextColumn, nextRow)) {
                continue;
            }
            TileType besideColumn = layout.tileAt(nextColumn, row);
            TileType besideRow = layout.tileAt(column, nextRow);
            boolean isCornerOpen = (besideColumn == TileType.FLOOR || (canCrossVoid && besideColumn == TileType.VOID))
                    && (besideRow == TileType.FLOOR || (canCrossVoid && besideRow == TileType.VOID));
            boolean isDiagonal = NEIGHBOR_COLUMNS[neighbor] != 0 && NEIGHBOR_ROWS[neighbor] != 0;
            int distance = distances[nextRow * layout.columns + nextColumn];
            if ((!isDiagonal || isCornerOpen) && distance < bestDistance) {
                bestDistance = distance;
                bestColumn = nextColumn;
                bestRow = nextRow;
            }
        }
        if (bestColumn < 0) {
            return direction.scl(1f / length);
        }
        return new Vector2(layout.centerX(bestColumn), layout.centerY(bestRow)).sub(unit.position).nor();
    }

    public static boolean hasLineOfSight(GameWorld world, Vector2 from, Vector2 to) {
        return openFraction(world.layout, from.x, from.y, to.x, to.y, true) >= 1f;
    }

    public static Vector2 clipToWalls(GameWorld world, Vector2 start, Vector2 end) {
        float fraction = openFraction(world.layout, start.x, start.y, end.x, end.y, true);
        return new Vector2(end).sub(start).scl(fraction).add(start);
    }

    public static Vector2 nearestOpenPosition(RoomLayout layout, Vector2 position, boolean canCrossVoid) {
        int column = layout.columnOf(position.x);
        int row = layout.rowOf(position.y);
        TileType tile = layout.tileAt(column, row);
        if (layout.isInside(column, row) && (tile == TileType.FLOOR || (canCrossVoid && tile == TileType.VOID))) {
            return new Vector2(position);
        }
        for (int ring = 1; ring < Math.max(layout.columns, layout.rows); ring++) {
            for (int offsetColumn = -ring; offsetColumn <= ring; offsetColumn++) {
                for (int offsetRow = -ring; offsetRow <= ring; offsetRow++) {
                    boolean isOnRing = Math.max(Math.abs(offsetColumn), Math.abs(offsetRow)) == ring;
                    if (isOnRing && layout.tileAt(column + offsetColumn, row + offsetRow) == TileType.FLOOR) {
                        return new Vector2(layout.centerX(column + offsetColumn), layout.centerY(row + offsetRow));
                    }
                }
            }
        }
        return new Vector2(position);
    }

    private static float openFraction(RoomLayout layout, float startX, float startY, float endX, float endY, boolean canCrossVoid) {
        float length = (float) Math.hypot(endX - startX, endY - startY);
        int steps = Math.max(1, (int) Math.ceil(length / SIGHT_STEP));
        for (int step = 1; step <= steps; step++) {
            float fraction = (float) step / steps;
            TileType tile = layout.typeAt(startX + (endX - startX) * fraction, startY + (endY - startY) * fraction);
            if (tile == TileType.WALL || (tile == TileType.VOID && !canCrossVoid)) {
                return (float) (step - 1) / steps;
            }
        }
        return 1f;
    }
}
