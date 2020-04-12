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
                this.regions[x][y] = sceneRegionBuilder.loadRegion(regionId);
                System.out.printf("Loaded region %d\n", regionId);
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

        int regionX = x % Constants.REGION_SIZE;
        int regionY = y % Constants.REGION_SIZE;
        SceneTile tile = regions[gridX][gridY].getTiles()[z][regionX][regionY];
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
}
