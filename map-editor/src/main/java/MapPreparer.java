import impl.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.*;
import net.runelite.cache.*;
import net.runelite.cache.TextureManager;
import net.runelite.cache.definitions.*;
import net.runelite.cache.definitions.ObjectDefinition;
import net.runelite.cache.definitions.loaders.MapLoader;
import net.runelite.cache.definitions.loaders.OverlayLoader;
import net.runelite.cache.definitions.loaders.SpriteLoader;
import net.runelite.cache.definitions.loaders.UnderlayLoader;
import net.runelite.cache.fs.*;
import net.runelite.cache.item.ColorPalette;
import net.runelite.cache.item.RSTextureProvider;
import net.runelite.cache.region.Location;
import net.runelite.cache.region.LocationType;
import net.runelite.cache.region.Region;
import net.runelite.cache.region.RegionLoader;
import net.runelite.cache.util.Djb2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapPreparer {

    private static final int MAPICON_MAX_WIDTH = 5; // scale minimap icons down to this size so they fit..
    private static final int MAPICON_MAX_HEIGHT = 6;

    private static int[] colorPalette = new ColorPalette(0.7d, 0, 512).getColorPalette();

    private static int[][] TILE_SHAPE_2D = new int[][]{{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, {1, 0, 0, 0, 1, 1, 0, 0, 1, 1, 1, 0, 1, 1, 1, 1}, {1, 1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0}, {0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 1}, {0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, {1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1}, {1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 0}, {1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 0, 0, 1, 1}, {1, 1, 1, 1, 1, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 1, 1}};
    private static int[][] TILE_ROTATION_2D = new int[][]{{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15}, {12, 8, 4, 0, 13, 9, 5, 1, 14, 10, 6, 2, 15, 11, 7, 3}, {15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0}, {3, 7, 11, 15, 2, 6, 10, 14, 1, 5, 9, 13, 0, 4, 8, 12}};

    private final int wallColor = (238 + (int) (Math.random() * 20.0D) - 10 << 16) + (238 + (int) (Math.random() * 20.0D) - 10 << 8) + (238 + (int) (Math.random() * 20.0D) - 10);
    private final int doorColor = 238 + (int) (Math.random() * 20.0D) - 10 << 16;

    private final Store store;

    private final Map<Integer, UnderlayDefinition> underlays = new HashMap<>();
    private final Map<Integer, OverlayDefinition> overlays = new HashMap<>();
    private final Map<Integer, Image> scaledMapIcons = new HashMap<>();

    private MapLoader mapLoader;
    private RegionLoader regionLoader;
    private ObjectManager objectManager;
    private RSTextureProvider rsTextureProvider;

    public MapPreparer(Store store, RSTextureProvider textureProvider) {
        this.store = store;
        this.rsTextureProvider = textureProvider;
        mapLoader = new MapLoader(store);
        regionLoader = new RegionLoader(store);
        objectManager = new ObjectManager(store);
    }

    public void load() throws IOException {
        loadUnderlays(store);
        loadOverlays(store);
        loadSprites();
        objectManager.load();
    }

    public void loadTiles(SceneImpl scene, int midX, int midY) {
        int baseX = midX - Constants.SCENE_SIZE/2; // half the scene size to paint 52 in all directions
        int baseY = midY - Constants.SCENE_SIZE/2;

        int len = Constants.SCENE_SIZE;
        int[] hues = new int[len];
        int[] sats = new int[len];
        int[] light = new int[len];
        int[] mul = new int[len];
        int[] num = new int[len];

        int[][] tileColors = new int[Constants.SCENE_SIZE+1][Constants.SCENE_SIZE+1];

        int var9 = (int) Math.sqrt(5100.0D);
        int var10 = var9 * 768 >> 8;

        for (int z = 0; z < Constants.MAX_Z; z++) {
            for (int y = 1; y < Constants.SCENE_SIZE-1; y++) {
                for (int x = 1; x < Constants.SCENE_SIZE-1; x++) {
                    int worldX = baseX + x;
                    int worldY = baseY + y;


                    int xHeightDiff = mapLoader.getWorldTile(z, worldX + 1, worldY).height - mapLoader.getWorldTile(z, worldX - 1, worldY).height;
                    int yHeightDiff = mapLoader.getWorldTile(z, worldX, worldY + 1).height - mapLoader.getWorldTile(z, worldX, worldY - 1).height;
                    int diff = (int) Math.sqrt((double) xHeightDiff * xHeightDiff + yHeightDiff * yHeightDiff + 65536);
                    int var16 = (xHeightDiff << 8) / diff;
                    int var17 = 65536 / diff;
                    int var18 = (yHeightDiff << 8) / diff;
                    int var19 = (var16 * -50 + var18 * -50 + var17 * -10) / var10 + 96;

                    int wSetting = mapLoader.getWorldTile(0, worldX - 1, worldY).settings;
                    int sSetting = mapLoader.getWorldTile(0, worldX, worldY - 1).settings;
                    int eSetting = mapLoader.getWorldTile(0, worldX + 1, worldY).settings;
                    int nSetting = mapLoader.getWorldTile(0, worldX, worldY + 1).settings;
                    int mySetting = mapLoader.getWorldTile(0, worldX, worldY).settings;

                    int var20 = (wSetting >> 2 + sSetting >> 2 + eSetting >> 3 + nSetting >> 3 + mySetting >> 1);
                    tileColors[x][y] = var19 - var20;
                }
            }

            for (int xi = -5; xi < Constants.SCENE_SIZE+5; ++xi) {
                for (int yi = 0; yi < Constants.SCENE_SIZE; ++yi) {
                    int xr = xi + 5;
                    if (xr >= 0 && xr < Constants.SCENE_SIZE) {
                        MapDefinition.Tile tile = mapLoader.getWorldTile(z, baseX + xr, baseY + yi);
                        if (tile != null) {
                            int underlayId = tile.underlayId;
                            if (underlayId > 0) {
                                UnderlayDefinition underlay = findUnderlay(underlayId - 1);
                                hues[yi] += underlay.getHue();
                                sats[yi] += underlay.getSaturation();
                                light[yi] += underlay.getLightness();
                                mul[yi] += underlay.getHueMultiplier();
                                num[yi]++;
                            }
                        }
                    }

                    int xl = xi - 5;
                    if (xl >= 0 && xl < Constants.SCENE_SIZE) {
                        MapDefinition.Tile tile = mapLoader.getWorldTile(z, baseX + xl, baseY + yi);
                        if (tile != null) {
                            int underlayId = tile.underlayId;
                            if (underlayId > 0) {
                                UnderlayDefinition underlay = findUnderlay(underlayId - 1);
                                hues[yi] -= underlay.getHue();
                                sats[yi] -= underlay.getSaturation();
                                light[yi] -= underlay.getLightness();
                                mul[yi] -= underlay.getHueMultiplier();
                                num[yi]--;
                            }
                        }
                    }
                }

                if (xi >= 1 && xi < Constants.SCENE_SIZE-1) {
                    int runningHues = 0;
                    int runningSat = 0;
                    int runningLight = 0;
                    int runningMultiplier = 0;
                    int runningNumber = 0;

                    for (int yi = -5; yi < Constants.SCENE_SIZE+5; ++yi) {
                        int yu = yi + 5;
                        if (yu >= 0 && yu < Constants.SCENE_SIZE) {
                            runningHues += hues[yu];
                            runningSat += sats[yu];
                            runningLight += light[yu];
                            runningMultiplier += mul[yu];
                            runningNumber += num[yu];
                        }

                        int yd = yi - 5;
                        if (yd >= 0 && yd < Constants.SCENE_SIZE) {
                            runningHues -= hues[yd];
                            runningSat -= sats[yd];
                            runningLight -= light[yd];
                            runningMultiplier -= mul[yd];
                            runningNumber -= num[yd];
                        }

                        if (yi >= 1 && yi < Constants.SCENE_SIZE-1) {
                            MapDefinition.Tile tile = mapLoader.getWorldTile(z, baseX + xi, baseY + yi);
                            if (tile != null) {
                                int underlayId = tile.getUnderlayId() & 0xFF;
                                int overlayId = tile.getOverlayId() & 0xFF;

                                if (underlayId > 0 || overlayId > 0) {
                                    int swHeight = mapLoader.getWorldTile(z, baseX + xi, baseY + yi).height;
                                    int seHeight = mapLoader.getWorldTile(z, baseX + xi + 1, baseY + yi).height;
                                    int neHeight = mapLoader.getWorldTile(z, baseX + xi + 1, baseY + yi + 1).height;
                                    int nwHeight = mapLoader.getWorldTile(z, baseX + xi, baseY + yi + 1).height;

                                    int swColor = tileColors[xi][yi];
                                    int seColor = tileColors[xi + 1][yi];
                                    int neColor = tileColors[xi + 1][yi + 1];
                                    int nwColor = tileColors[xi][yi + 1];
                                    int rgb = -1;
                                    int underlayHsl = -1;

                                    if (underlayId > 0) {
                                        int avgHue = runningHues * 256 / Math.max(1, runningMultiplier);
                                        int avgSat = runningSat / Math.max(1, runningNumber);
                                        int avgLight = runningLight / Math.max(1, runningNumber);
                                        // randomness is added to avgHue here

                                        rgb = hslToRgb(avgHue, avgSat, avgLight);

                                        if (avgLight < 0) {
                                            avgLight = 0;
                                        } else if (avgLight > 255) {
                                            avgLight = 255;
                                        }

                                        underlayHsl = hslToRgb(avgHue, avgSat, avgLight);
                                    }

                                    int underlayRgb = 0;
                                    if (underlayHsl != -1) {
                                        int var0 = method4220(underlayHsl, 96);
                                        underlayRgb = colorPalette[var0];
                                    }

                                    if (overlayId == 0) {
                                        scene.addTile(z, xi, yi, 0, 0, -1, swHeight, seHeight, neHeight, nwHeight, method4220(rgb, swColor), method4220(rgb, seColor), method4220(rgb, neColor), method4220(rgb, nwColor), 0, 0, 0, 0, underlayRgb, 0);
                                    } else {
                                        int overlayPath = tile.getOverlayPath() + 1;
                                        int overlayRotation = tile.getOverlayRotation();

                                        OverlayDefinition overlayDefinition = findOverlay(overlayId - 1);
                                        int overlayTexture = overlayDefinition.getTexture();
                                        int overlayHsl;

                                        if (overlayTexture >= 0) {
                                            rgb = rsTextureProvider.getAverageTextureRGB(overlayTexture);
                                            overlayHsl = -1;
                                        } else if (overlayDefinition.getRgbColor() == 0xFF_00FF) {
                                            overlayHsl = -2;
                                            overlayTexture = -1;
                                            rgb = -2;
                                        } else {
                                            overlayHsl = hslToRgb(overlayDefinition.getHue(), overlayDefinition.getSaturation(), overlayDefinition.getLightness());

                                            int hue = overlayDefinition.getHue() & 255;
                                            int lightness = overlayDefinition.getLightness();
                                            if (lightness < 0) {
                                                lightness = 0;
                                            } else if (lightness > 255) {
                                                lightness = 255;
                                            }

                                            rgb = hslToRgb(hue, overlayDefinition.getSaturation(), lightness);
                                        }

                                        int overlayRgb = 0;
                                        if (rgb != -2) {
                                            int var0 = adjustHSLListness0(rgb, 96);
                                            overlayRgb = colorPalette[var0];
                                        }

                                        if (overlayDefinition.getSecondaryRgbColor() != -1) {
                                            int hue = overlayDefinition.getOtherHue() & 255;
                                            int lightness = overlayDefinition.getOtherLightness();
                                            if (lightness < 0) {
                                                lightness = 0;
                                            } else if (lightness > 255) {
                                                lightness = 255;
                                            }

                                            rgb = hslToRgb(hue, overlayDefinition.getOtherSaturation(), lightness);
                                            int var0 = adjustHSLListness0(rgb, 96);
                                            overlayRgb = colorPalette[var0];
                                        }
                                        scene.addTile(z, xi, yi, overlayPath, overlayRotation, overlayTexture, swHeight, seHeight, neHeight, nwHeight, method4220(rgb, swColor), method4220(rgb, seColor), method4220(rgb, neColor), method4220(rgb, nwColor), adjustHSLListness0(overlayHsl, swColor), adjustHSLListness0(overlayHsl, seColor), adjustHSLListness0(overlayHsl, neColor), adjustHSLListness0(overlayHsl, nwColor), underlayRgb, overlayRgb);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void loadObjects(SceneImpl scene, int midX, int midY) {
        int baseX = midX - Constants.SCENE_SIZE/2; // half the scene size to paint 52 in all directions
        int baseY = midY - Constants.SCENE_SIZE/2;

        for (int y = 1; y < Constants.SCENE_SIZE-1; y++) {
            for (int x = 1; x < Constants.SCENE_SIZE - 1; x++) {
                int worldX = baseX + x;
                int worldY = baseY + y;

                Region r = regionLoader.loadRegionFromWorldCoordinates(worldX, worldY);
                if (r == null) {
                    continue;
                }

                List<Location> locations = r.getLocationsAt(0, worldX, worldY);
                if (locations == null || locations.size() == 0) {
                    continue;
                }

                int finalX = x;
                int finalY = y;
                locations.forEach(loc -> {
                    ObjectDefinition objectDefinition = objectManager.getObject(loc.getId());
                    ModelDefinition modelDefinition = objectDefinition.getModel(loc.getType(), loc.getOrientation());

//                    int width;
//                    int length;
//                    if (loc.getOrientation() != 1 && loc.getOrientation() != 3) {
//                        width = objectDefinition.getSizeX();
//                        length = objectDefinition.getSizeY();
//                    } else {
//                        width = objectDefinition.getSizeY();
//                        length = objectDefinition.getSizeX();
//                    }

                    int z = loc.getPosition().getZ();
//                    int xSize = (finalX << 7) + (width << 6);
//                    int ySize = (finalY << 7) + (length << 6);

                    int swHeight = mapLoader.getWorldTile(z, baseX + finalX, baseY + finalY).height;
                    int seHeight = mapLoader.getWorldTile(z, baseX + finalX +1, baseY + finalY).height;
                    int neHeight = mapLoader.getWorldTile(z, baseX + finalX +1, baseY + finalY + 1).height;
                    int nwHeight = mapLoader.getWorldTile(z, baseX + finalX, baseY + finalY +1).height;

                    int height = swHeight + seHeight + neHeight + nwHeight >> 2;

                    if (loc.getType() == LocationType.FLOOR_DECORATION.getValue()) {
                        if (modelDefinition != null) {
                            ModelImpl model = new ModelImpl(modelDefinition, objectDefinition.getAmbient() + 64, objectDefinition.getContrast() + 768, -50, -10, -50);

                            if (objectDefinition.getContouredGround() >= 0) {
//                               model = model.contourGround(mapLoader, xSize, height, ySize, true, objectDefinition.getContouredGround(), worldX, worldY);
                            }

                            scene.newFloorDecoration(z, loc.getPosition().getX() - baseX, loc.getPosition().getY() - baseY, height, model, 0, 0);
                        }
                    }
                    if (loc.getType() == LocationType.INTERACTABLE_WALL_DECORATION.getValue()) {
                        if (modelDefinition != null) {
                            ModelImpl model = new ModelImpl(modelDefinition, objectDefinition.getAmbient() + 64, objectDefinition.getContrast() + 768, -50, -10, -50);
                            int[] orientationTransform = {1, 2, 4, 8};
                            scene.newWallDecoration(z, loc.getPosition().getX() - baseX, loc.getPosition().getY() - baseY, height, model, null, orientationTransform[loc.getOrientation()], 0, 0, 0);
                        }
                    }

                    if (loc.getType() == LocationType.INTERACTABLE.getValue()) {
                        if (modelDefinition != null) {
                            ModelImpl model = new ModelImpl(modelDefinition, objectDefinition.getAmbient() + 64, objectDefinition.getContrast() + 768, -50, -10, -50);
                            scene.newWallDecoration(z, loc.getPosition().getX() - baseX, loc.getPosition().getY() - baseY, height, model, null, 0, 0, 0, 0);
                        }
                    }

                    if (loc.getType() == LocationType.DIAGONAL_INTERACTABLE.getValue()) {
                        if (modelDefinition != null) {
                            ModelImpl model = new ModelImpl(modelDefinition, objectDefinition.getAmbient() + 64, objectDefinition.getContrast() + 768, -50, -10, -50);
                            scene.newWallDecoration(z, loc.getPosition().getX() - baseX, loc.getPosition().getY() - baseY, height, model, null, 0, 0, 0, 0);
                        }
                    }
                });
            }
        }
    }

    private void loadUnderlays(Store store) throws IOException {
        Storage storage = store.getStorage();
        Index index = store.getIndex(IndexType.CONFIGS);
        Archive archive = index.getArchive(ConfigType.UNDERLAY.getId());

        byte[] archiveData = storage.loadArchive(archive);
        ArchiveFiles files = archive.getFiles(archiveData);

        for (FSFile file : files.getFiles()) {
            UnderlayLoader loader = new UnderlayLoader();
            UnderlayDefinition underlay = loader.load(file.getFileId(), file.getContents());

            underlays.put(underlay.getId(), underlay);
        }
    }

    private UnderlayDefinition findUnderlay(int id) {
        return underlays.get(id);
    }

    private void loadOverlays(Store store) throws IOException {
        Storage storage = store.getStorage();
        Index index = store.getIndex(IndexType.CONFIGS);
        Archive archive = index.getArchive(ConfigType.OVERLAY.getId());

        byte[] archiveData = storage.loadArchive(archive);
        ArchiveFiles files = archive.getFiles(archiveData);

        for (FSFile file : files.getFiles()) {
            OverlayLoader loader = new OverlayLoader();
            OverlayDefinition overlay = loader.load(file.getFileId(), file.getContents());

            overlays.put(overlay.getId(), overlay);
        }
    }

    private OverlayDefinition findOverlay(int id) {
        return overlays.get(id);
    }

    private void loadSprites() throws IOException {
        Storage storage = store.getStorage();
        Index index = store.getIndex(IndexType.SPRITES);
        final int mapsceneHash = Djb2.hash("mapscene");

        for (Archive a : index.getArchives()) {
            byte[] contents = a.decompress(storage.loadArchive(a));

            SpriteLoader loader = new SpriteLoader();
            SpriteDefinition[] sprites = loader.load(a.getArchiveId(), contents);

            for (SpriteDefinition sprite : sprites) {
                if (sprite.getHeight() <= 0 || sprite.getWidth() <= 0) {
                    continue;
                }

                if (a.getNameHash() == mapsceneHash) {
                    BufferedImage spriteImage = new BufferedImage(sprite.getWidth(), sprite.getHeight(), BufferedImage.TYPE_INT_ARGB);
                    spriteImage.setRGB(0, 0, sprite.getWidth(), sprite.getHeight(), sprite.getPixels(), 0, sprite.getWidth());

                    // scale image down so it fits
                    Image scaledImage = spriteImage.getScaledInstance(MAPICON_MAX_WIDTH, MAPICON_MAX_HEIGHT, 0);

                    assert scaledMapIcons.containsKey(sprite.getFrame()) == false;
                    scaledMapIcons.put(sprite.getFrame(), scaledImage);
                }
            }
        }
    }

    static int method4220(int var0, int var1) {
        if (var0 == -1) {
            return 12345678;
        } else {
            var1 = (var0 & 127) * var1 / 128;
            if (var1 < 2) {
                var1 = 2;
            } else if (var1 > 126) {
                var1 = 126;
            }

            return (var0 & 65408) + var1;
        }
    }

    static final int adjustHSLListness0(int var0, int var1) {
        if (var0 == -2) {
            return 12345678;
        } else if (var0 == -1) {
            if (var1 < 2) {
                var1 = 2;
            } else if (var1 > 126) {
                var1 = 126;
            }

            return var1;
        } else {
            var1 = (var0 & 127) * var1 / 128;
            if (var1 < 2) {
                var1 = 2;
            } else if (var1 > 126) {
                var1 = 126;
            }

            return (var0 & 65408) + var1;
        }
    }

    int hslToRgb(int var0, int var1, int var2) {
        if (var2 > 179) {
            var1 /= 2;
        }

        if (var2 > 192) {
            var1 /= 2;
        }

        if (var2 > 217) {
            var1 /= 2;
        }

        if (var2 > 243) {
            var1 /= 2;
        }

        int var3 = (var1 / 32 << 7) + (var0 / 4 << 10) + var2 / 2;
        return var3;
    }
}

