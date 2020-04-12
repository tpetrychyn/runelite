package scene;

import lombok.Getter;
import lombok.Setter;
import models.TileModelImpl;
import models.TilePaintImpl;
import models.WallDecoration;
import net.runelite.api.*;
import net.runelite.cache.definitions.MapDefinition;

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
