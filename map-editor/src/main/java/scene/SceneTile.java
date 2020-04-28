package scene;

import lombok.Getter;
import lombok.Setter;
import models.FloorDecoration;
import models.TileModelImpl;
import models.TilePaintImpl;
import models.WallDecoration;
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

    SceneTile(int z, int x, int y) {
        this.plane = z;
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return String.format("x %d y %d rgb %d", x, y, tilePaint.getRgb());
    }
}
