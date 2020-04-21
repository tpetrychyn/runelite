package scene;

import lombok.Getter;
import lombok.Setter;
import models.TileModelImpl;
import models.TilePaintImpl;
import models.WallDecoration;
import net.runelite.api.Constants;
import net.runelite.api.Entity;
import models.FloorDecoration;
import net.runelite.api.Perspective;
import net.runelite.cache.region.Location;
import net.runelite.cache.region.Region;

import java.util.List;

@Getter
@Setter
public class SceneRegion {
    private final int regionId;
    private final int baseX;
    private final int baseY;

    private SceneTile[][][] tiles = new SceneTile[Region.Z][Region.X][Region.Y];
    private int[][] tileColors = new int[Region.X+1][Region.Y+1];

    private int[][][] tileHeights;
    private byte[][][] tileSettings;
    private byte[][][] overlayIds;
    private byte[][][] overlayPaths;
    private byte[][][] overlayRotations;
    private byte[][][] underlayIds;

    private List<Location> locations;

    public SceneRegion(Region region) {
        this.regionId = region.getRegionID();
        this.baseX = region.getBaseX();
        this.baseY = region.getBaseY();

        this.tileHeights = region.getTileHeights();
        this.tileSettings = region.getTileSettings();
        this.overlayIds = region.getOverlayIds();
        this.overlayPaths = region.getOverlayPaths();
        this.overlayRotations = region.getOverlayRotations();
        this.underlayIds = region.getUnderlayIds();
        this.locations = region.getLocations();
    }

    public void clearTiles() {
        this.tiles = new SceneTile[Constants.MAX_Z][Constants.SCENE_SIZE][Constants.SCENE_SIZE];
    }

    public void addTile(int z, int x, int y, int overlayPath, int overlayRotation, int overlayTexture, int swHeight, int seHeight, int neHeight, int nwHeight, int swColor, int seColor, int neColor, int nwColor, int var15, int var16, int var17, int var18, int rgb, int overlayRgb) {
        if (overlayPath == 0) {
            for (int iz = z; iz >= 0; --iz) {
                if (this.tiles[iz][x][y] == null) {
                    this.tiles[iz][x][y] = new SceneTile(iz, x, y);
                }
            }

            this.tiles[z][x][y].setTilePaint(new TilePaintImpl(swColor, seColor, neColor, nwColor, -1, rgb, false));
            this.tiles[z][x][y].getTilePaint().setSwHeight(swHeight);
            this.tiles[z][x][y].getTilePaint().setSeHeight(seHeight);
            this.tiles[z][x][y].getTilePaint().setNeHeight(neHeight);
            this.tiles[z][x][y].getTilePaint().setNwHeight(nwHeight);
        } else if (overlayPath != 1) {
            for (int iz = z; iz >= 0; --iz) {
                if (this.tiles[iz][x][y] == null) {
                    this.tiles[iz][x][y] = new SceneTile(iz, x, y);
                }
            }

            this.tiles[z][x][y].setTileModel(new TileModelImpl(overlayPath, overlayRotation, overlayTexture, x, y, swHeight, seHeight, neHeight, nwHeight, swColor, seColor, neColor, nwColor, var15, var16, var17, var18, rgb, overlayRgb));
        } else {
            for (int iz = z; iz >= 0; --iz) {
                if (this.tiles[iz][x][y] == null) {
                    this.tiles[iz][x][y] = new SceneTile(iz, x, y);
                }
            }

            this.tiles[z][x][y].setTilePaint(new TilePaintImpl(var15, var16, var17, var18, overlayTexture, overlayRgb, seHeight == swHeight && swHeight == neHeight && nwHeight == swHeight));
            this.tiles[z][x][y].getTilePaint().setSwHeight(swHeight);
            this.tiles[z][x][y].getTilePaint().setSeHeight(seHeight);
            this.tiles[z][x][y].getTilePaint().setNeHeight(neHeight);
            this.tiles[z][x][y].getTilePaint().setNwHeight(nwHeight);
        }


        if (x > 0) {
            SceneTile west = this.tiles[z][x-1][y] == null ? new SceneTile(z, x-1, y) : this.tiles[z][x-1][y];
            west.setEast(this.tiles[z][x][y]);
            this.tiles[z][x-1][y] = west;
        }

        if (x > 0 && y > 0) {
            SceneTile southWest = this.tiles[z][x-1][y-1] == null ? new SceneTile(z, x-1, y-1) : this.tiles[z][x-1][y-1];
            southWest.setNorthEast(this.tiles[z][x][y]);
            this.tiles[z][x-1][y-1] = southWest;
        }

        if (y > 0) {
            SceneTile south = this.tiles[z][x][y-1] == null ? new SceneTile(z, x, y-1) : this.tiles[z][x][y-1];
            south.setNorth(this.tiles[z][x][y]);
            this.tiles[z][x][y-1] = south;
        }
    }

    public void newFloorDecoration(int z, int x, int y, int height, Entity entity, long tag, int flags) {
        for (int iz = z; iz >= 0; --iz) {
            if (this.tiles[iz][x][y] == null) {
                this.tiles[iz][x][y] = new SceneTile(iz, x, y);
            }
        }

        FloorDecoration floorDecoration = new FloorDecoration();
        floorDecoration.setModel(entity.getModel());
        floorDecoration.setX(x * Perspective.LOCAL_TILE_SIZE + Constants.REGION_SIZE);
        floorDecoration.setY(y * Perspective.LOCAL_TILE_SIZE + Constants.REGION_SIZE);
        floorDecoration.setHeight(height);
        floorDecoration.setTag(tag);
        floorDecoration.setFlags(flags);

        this.tiles[z][x][y].setFloorDecoration(floorDecoration);
    }

    public void newWallDecoration(int z, int x, int y, int height, Entity entityA, Entity entityB, int orientationA, int orientationB, long tag, int flags) {
        for (int iz = z; iz >= 0; --iz) {
            if (this.tiles[iz][x][y] == null) {
                this.tiles[iz][x][y] = new SceneTile(iz, x, y);
            }
        }

        WallDecoration wallDecoration = new WallDecoration(tag,
                flags,
                x * Perspective.LOCAL_TILE_SIZE + Constants.REGION_SIZE,
                y * Perspective.LOCAL_TILE_SIZE + Constants.REGION_SIZE,
                height,
                entityA,
                entityB,
                orientationA,
                orientationB);

        this.tiles[z][x][y].setWallDecoration(wallDecoration);
    }
}
