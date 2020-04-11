package models;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.*;

@Getter
@Setter
public class SceneTile {
    private int plane;
    private int x;
    private int y;
    private TilePaintImpl tilePaint;
    private TileModelImpl tileModel;
    private FloorDecoration floorDecoration;
    private WallDecoration wallDecoration;

    SceneTile(int z, int x, int y) {
        this.plane = z;
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return String.format("x %d y %d rgb %d", x, y, tilePaint.getRBG());
    }
}
