package models;

import lombok.Getter;
import lombok.Setter;
import net.runelite.cache.region.Region;

@Getter
@Setter
public class SceneRegion extends Region {
    private SceneTile[][][] sceneTiles;
    private int[][] tileColors;

    public SceneRegion(Region region) {
        super(region.getRegionID());
    }


}
