package scene;

import lombok.Getter;
import lombok.Setter;
import models.*;
import net.runelite.cache.definitions.MapDefinition;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SceneTile extends MapDefinition.Tile {
    private int plane;
    private int x;
    private int y;
    private TilePaintImpl tilePaint;
    private TileModelImpl tileModel;
    private FloorDecoration floorDecoration;
    private WallDecoration wallDecoration;
    private List<WallDecoration> boundaryObjects = new ArrayList<>();

    private SceneTile north;
    private SceneTile northEast;
    private SceneTile east;

    public SceneTile(int z, int x, int y) {
        this.plane = z;
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return String.format("x %d y %d rgb %d", x, y, tilePaint.getRgb());
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof SceneTile)) return false;

        SceneTile other = (SceneTile) o;

        return x == other.x && y == other.y && plane == other.plane;
    }
}
