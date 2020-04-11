package impl;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;

@Getter
@Setter
public class TileImpl {
    private int plane;
    private int x;
    private int y;
    private TilePaintImpl tilePaint;
    private TileModelImpl tileModel;
    private FloorDecoration floorDecoration;
    private WallDecoration wallDecoration;

    TileImpl(int z, int x, int y) {
        this.plane = z;
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return String.format("x %d y %d rgb %d", x, y, tilePaint.getRBG());
    }
}
