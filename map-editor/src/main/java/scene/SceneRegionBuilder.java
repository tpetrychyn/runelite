package scene;

import models.SceneRegion;
import net.runelite.api.Constants;
import net.runelite.cache.ObjectManager;
import net.runelite.cache.fs.StoreProvider;
import net.runelite.cache.item.RSTextureProvider;
import net.runelite.cache.region.Region;
import net.runelite.cache.region.RegionLoader;

import java.io.IOException;

public class SceneRegionBuilder {

    private RegionLoader regionLoader;
    private ObjectManager objectManager;
    private RSTextureProvider rsTextureProvider;

    public SceneRegionBuilder() throws IOException {
//        rsTextureProvider = textureProvider;
        regionLoader = new RegionLoader(StoreProvider.getStore());
        regionLoader.loadRegions();

        objectManager = new ObjectManager(StoreProvider.getStore());
    }

    // 1. go through and load terrain
    // 2. loop and set tile heights

    // worldCoords to regionId
    // int regionId = (x >>> 6 << 8) | y >>> 6;
    public SceneRegion loadTiles(int regionId) {
        SceneRegion sceneRegion = new SceneRegion(regionLoader.getRegion(regionId));
        int baseX = sceneRegion.getBaseX();
        int baseY = sceneRegion.getBaseY();

        int len = Constants.SCENE_SIZE;
        int[] hues = new int[len];
        int[] sats = new int[len];
        int[] light = new int[len];
        int[] mul = new int[len];
        int[] num = new int[len];

        int var9 = (int) Math.sqrt(5100.0D);
        int var10 = var9 * 768 >> 8;

        for (int z = 0; z < Constants.MAX_Z; z++) {
            for (int y = 1; y < Constants.SCENE_SIZE - 1; y++) {
                for (int x = 1; x < Constants.SCENE_SIZE - 1; x++) {

                }
            }
        }

        return sceneRegion;
    }
}
