package scene;

import lombok.Getter;
import net.runelite.api.Constants;

@Getter
public class Scene {
    // NxM grid of regions to display
    private SceneRegion[][] regions;
    private int radius;

    public boolean hasBeenUploaded;

    public Scene(SceneRegionBuilder sceneRegionBuilder, int centerRegionId, int radius) {
        this.radius = radius;
        this.regions = new SceneRegion[radius][radius];

        int regionId = centerRegionId;
        if (radius > 1) {
            regionId = centerRegionId - (256 * (radius - 2)) - ((radius - 2));
        }
        for (int x = 0; x < radius; x++) {
            for (int y = 0; y < radius; y++) {
                System.out.printf("Loading region %d\n", regionId);
                this.regions[x][y] = sceneRegionBuilder.loadRegion(regionId);
                regionId++;
            }
            regionId += 256 - (radius); // move 1 region to the right, reset to lowest y
        }
    }

    public SceneTile getTile(int z, int x, int y) {
        if (x < 0 || y < 0) {
            return null;
        }

        // figure which SceneRegion(n, m) the tile exists in
        int gridX = x / Constants.REGION_SIZE;
        int gridY = y / Constants.REGION_SIZE;
        if (gridX >= radius || gridY >= radius) {
            return null;
        }

        SceneRegion region = getRegion(gridX, gridY);
        if (region == null) {
            return null;
        }

        int regionX = x % Constants.REGION_SIZE;
        int regionY = y % Constants.REGION_SIZE;
        SceneTile tile = region.getTiles()[z][regionX][regionY];
        if (tile != null) {
            // offset the tile by adding it's scene offset to it's region offset - get scene position
            tile.setX(regionX + gridX * Constants.REGION_SIZE);
            tile.setY(regionY + gridY * Constants.REGION_SIZE);
        }

        return tile;
    }

    public SceneRegion getRegion(int gridX, int gridY) {
        if (gridX >= radius || gridY >= radius) {
            return null;
        }
        return regions[gridX][gridY];
    }

    public int calcTileColor(int z, int x, int y) {
        SceneTile tile = getTile(z, x, y);
        if (tile == null) {
            return 12345678;
        }

        int var9 = (int) Math.sqrt(5100.0D);
        int var10 = var9 * 768 >> 8;
        int xHeightDiff = getTileHeight(z, x + 1, y) - getTileHeight(z, x - 1, y);
        int yHeightDiff = getTileHeight(z, x, y + 1) - getTileHeight(z, x, y - 1);
        int diff = (int) Math.sqrt((double) (xHeightDiff * xHeightDiff + yHeightDiff * yHeightDiff + 65536));
        int var16 = (xHeightDiff << 8) / diff;
        int var17 = 65536 / diff;
        int var18 = (yHeightDiff << 8) / diff;
        int var19 = (var16 * -50 + var18 * -50 + var17 * -10) / var10 + 96;

        int color = (getTileSettings(0, x - 1, y) >> 2) + (getTileSettings(0, x, y - 1) >> 2) + (getTileSettings(0, x + 1, y) >> 3) + (getTileSettings(0, x, y + 1) >> 3) + (getTileSettings(0, x, y) >> 1);
        return var19 - color;
    }

    public int getTileHeight(int z, int x, int y) {
        SceneTile tile = getTile(z, x, y);
        if (tile == null) {
            return 0;
        }

        return tile.height != null ? tile.getHeight() : 0;
    }

    public int getTileSettings(int z, int x, int y) {
        SceneTile tile = getTile(z, x, y);
        if (tile == null) {
            return 0;
        }

        return tile.getSettings();
    }
}
