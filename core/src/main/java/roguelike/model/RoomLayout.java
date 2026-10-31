package roguelike.model;

import java.util.Arrays;

public final class RoomLayout {
    public static final float TILE_SIZE = 40f;

    public final int columns;
    public final int rows;
    public final float originX;
    public final float originY;
    public final TileType[] tiles;
    public final int[] groundDistances;
    public final int[] airDistances;

    public RoomLayout(int columns, int rows, float originX, float originY) {
        this.columns = columns;
        this.rows = rows;
        this.originX = originX;
        this.originY = originY;
        this.tiles = new TileType[columns * rows];
        this.groundDistances = new int[columns * rows];
        this.airDistances = new int[columns * rows];
        Arrays.fill(tiles, TileType.FLOOR);
    }

    public boolean isInside(int column, int row) {
        return column >= 0 && column < columns && row >= 0 && row < rows;
    }

    public TileType tileAt(int column, int row) {
        return isInside(column, row) ? tiles[row * columns + column] : TileType.WALL;
    }

    public void setTile(int column, int row, TileType type) {
        tiles[row * columns + column] = type;
    }

    public TileType typeAt(float x, float y) {
        return tileAt(columnOf(x), rowOf(y));
    }

    public int columnOf(float x) {
        return (int) Math.floor((x - originX) / TILE_SIZE);
    }

    public int rowOf(float y) {
        return (int) Math.floor((y - originY) / TILE_SIZE);
    }

    public float centerX(int column) {
        return originX + (column + 0.5f) * TILE_SIZE;
    }

    public float centerY(int row) {
        return originY + (row + 0.5f) * TILE_SIZE;
    }
}
