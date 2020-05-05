package scene;

import com.google.inject.Inject;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import lombok.Getter;
import net.runelite.api.Constants;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

@Getter
public class Scene {
    private final SceneRegionBuilder sceneRegionBuilder;
    // NxM grid of regions to display
    private SceneRegion[][] regions;
    private int radius;

    private final List<ActionListener> sceneChangeListeners = new ArrayList<>();
    private final ObjectProperty<SceneTile> hoveredEntity = new SimpleObjectProperty<>(null);
    private final ObjectProperty<Object> selectedEntity = new SimpleObjectProperty<>(null);

    public Scene(SceneRegionBuilder sceneRegionBuilder) {
        this.sceneRegionBuilder = sceneRegionBuilder;
    }

    public void Load(int centerRegionId, int radius) {
        this.radius = radius;
        this.regions = new SceneRegion[radius][radius];

        int regionId = centerRegionId;
        if (radius > 1) {
            regionId = centerRegionId - (256 * (radius - 2)) - ((radius - 2));
        }
        for (int x = 0; x < radius; x++) {
            for (int y = 0; y < radius; y++) {
                System.out.printf("Loading region %d\n", regionId);
                this.regions[x][y] = sceneRegionBuilder.loadRegion(regionId, true);
                regionId++;
            }
            regionId += 256 - (radius); // move 1 region to the right, reset to lowest y
        }

        this.sceneChangeListeners.forEach(it -> it.actionPerformed(null));
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

    public SceneRegion getRegionFromSceneCoord(int x, int y) {
        if (x < 0 || y < 0) {
            return null;
        }

        // figure which SceneRegion(n, m) the tile exists in
        int gridX = x / Constants.REGION_SIZE;
        int gridY = y / Constants.REGION_SIZE;
        if (gridX >= radius || gridY >= radius) {
            return null;
        }

        return getRegion(gridX, gridY);
    }

    public byte getTileSettings(int x, int y) {
        SceneRegion r = getRegionFromSceneCoord(x, y);
        return r.getTileSettings()[0][x][y];
    }
}