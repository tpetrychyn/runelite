package scene;

import models.DynamicObject;
import models.StaticObject;
import net.runelite.api.Constants;
import net.runelite.api.Entity;
import net.runelite.cache.ConfigType;
import net.runelite.cache.IndexType;
import net.runelite.cache.ObjectManager;
import net.runelite.cache.definitions.ModelDefinition;
import net.runelite.cache.definitions.ObjectDefinition;
import net.runelite.cache.definitions.OverlayDefinition;
import net.runelite.cache.definitions.UnderlayDefinition;
import net.runelite.cache.definitions.loaders.OverlayLoader;
import net.runelite.cache.definitions.loaders.UnderlayLoader;
import net.runelite.cache.fs.*;
import net.runelite.cache.item.ColorPalette;
import net.runelite.cache.item.RSTextureProvider;
import net.runelite.cache.region.LocationType;
import net.runelite.cache.region.Region;
import net.runelite.cache.region.RegionLoader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SceneRegionBuilder {

    private RegionLoader regionLoader;
    private ObjectManager objectManager;
    private RSTextureProvider rsTextureProvider;

    private static int[] colorPalette = new ColorPalette(0.7d, 0, 512).getColorPalette();

    private static final Map<Integer, UnderlayDefinition> underlays = new HashMap<>();
    private static final Map<Integer, OverlayDefinition> overlays = new HashMap<>();

    public SceneRegionBuilder(RSTextureProvider textureProvider) throws IOException {

        rsTextureProvider = textureProvider;
        regionLoader = new RegionLoader(StoreProvider.getStore());
        regionLoader.loadRegions();

        objectManager = new ObjectManager(StoreProvider.getStore());

        loadUnderlays(StoreProvider.getStore());
        loadOverlays(StoreProvider.getStore());
        objectManager.load();
    }

    // Loads a single region(rs size 64), not a scene(rs size 104)!

    // worldCoords to regionId
    // int regionId = (x >>> 6 << 8) | y >>> 6;
    public SceneRegion loadRegion(int regionId) {
        Region region = regionLoader.getRegion(regionId);
        if (region == null) {
            return null;
        }
        SceneRegion sceneRegion = new SceneRegion(region);
        int baseX = sceneRegion.getBaseX();
        int baseY = sceneRegion.getBaseY();

        // TODO: prevents overflow at edge of map
        boolean hasLeftRegion = regionLoader.findRegionForWorldCoordinates(baseX - 1, baseY) != null;
        boolean hasRightRegion = regionLoader.findRegionForWorldCoordinates(baseX + Region.X, baseY) != null;
        boolean hasUpRegion = regionLoader.findRegionForWorldCoordinates(baseX, baseY + Region.Y) != null;
        boolean hasDownRegion = regionLoader.findRegionForWorldCoordinates(baseX, baseY - 1) != null;

        int blend = 5;
        int len = Constants.REGION_SIZE * blend * 2;
        int[] hues = new int[len];
        int[] sats = new int[len];
        int[] light = new int[len];
        int[] mul = new int[len];
        int[] num = new int[len];

        // calculate tile colors
        int var9 = (int) Math.sqrt(5100.0D);
        int var10 = var9 * 768 >> 8;
        for (int z = 0; z < Constants.MAX_Z; z++) {
            for (int x = 0; x < Constants.REGION_SIZE + 1; x++) {
                for (int y = 0; y < Constants.REGION_SIZE + 1; y++) {
                    int worldX = baseX + x;
                    int worldY = baseY + y;

                    int xHeightDiff = getTileHeight(z, worldX + 1, worldY) - getTileHeight(z, worldX - 1, worldY);
                    int yHeightDiff = getTileHeight(z, worldX, worldY + 1) - getTileHeight(z, worldX, worldY - 1);
                    int diff = (int) Math.sqrt((double) (xHeightDiff * xHeightDiff + yHeightDiff * yHeightDiff + 65536));
                    int var16 = (xHeightDiff << 8) / diff;
                    int var17 = 65536 / diff;
                    int var18 = (yHeightDiff << 8) / diff;
                    int var19 = (var16 * -50 + var18 * -50 + var17 * -10) / var10 + 96;

                    int color = (getTileSettings(0, worldX - 1, worldY) >> 2) + (getTileSettings(0, worldX, worldY - 1) >> 2) + (getTileSettings(0, worldX + 1, worldY) >> 3) + (getTileSettings(0, worldX, worldY + 1) >> 3) + (getTileSettings(0, worldX, worldY) >> 1);
                    sceneRegion.getTileColors()[x][y] = var19 - color;
                }
            }

            for (int xi = -blend*2; xi < Constants.REGION_SIZE + blend*2; ++xi) {
                for (int yi = -blend; yi < Constants.REGION_SIZE + blend; ++yi) {
                    int xr = xi + 5;
                    if (xr >= -blend && xr < Constants.REGION_SIZE + blend) {
                        Region r = regionLoader.findRegionForWorldCoordinates(baseX + xr, baseY + yi);
                        if (r != null) {
                            int underlayId = r.getUnderlayId(z, convert(xr), convert(yi));
                            if (underlayId > 0) {
                                UnderlayDefinition underlay = findUnderlay(underlayId - 1);
                                hues[yi + blend] += underlay.getHue();
                                sats[yi + blend] += underlay.getSaturation();
                                light[yi + blend] += underlay.getLightness();
                                mul[yi + blend] += underlay.getHueMultiplier();
                                num[yi + blend]++;
                            }
                        }
                    }

                    int xl = xi - 5;
                    if (xl >= -blend && xl < Constants.REGION_SIZE + blend) {
                        Region r = regionLoader.findRegionForWorldCoordinates(baseX + xl, baseY + yi);
                        if (r != null) {
                            int underlayId = r.getUnderlayId(z, convert(xl), convert(yi));
                            if (underlayId > 0) {
                                UnderlayDefinition underlay = findUnderlay(underlayId - 1);
                                hues[yi + blend] -= underlay.getHue();
                                sats[yi + blend] -= underlay.getSaturation();
                                light[yi + blend] -= underlay.getLightness();
                                mul[yi + blend] -= underlay.getHueMultiplier();
                                num[yi + blend]--;
                            }
                        }
                    }
                }

                if (xi >= 0 && xi < Constants.REGION_SIZE) {
                    int runningHues = 0;
                    int runningSat = 0;
                    int runningLight = 0;
                    int runningMultiplier = 0;
                    int runningNumber = 0;

                    for (int yi = -blend * 2; yi < Constants.REGION_SIZE + blend * 2; ++yi) {
                        int yu = yi + 5;
                        if (yu >= -blend && yu < Constants.REGION_SIZE + blend) {
                            runningHues += hues[yu + blend];
                            runningSat += sats[yu + blend];
                            runningLight += light[yu + blend];
                            runningMultiplier += mul[yu + blend];
                            runningNumber += num[yu + blend];
                        }

                        int yd = yi - 5;
                        if (yd >= -blend && yd < Constants.REGION_SIZE + blend) {
                            runningHues -= hues[yd + blend];
                            runningSat -= sats[yd + blend];
                            runningLight -= light[yd + blend];
                            runningMultiplier -= mul[yd + blend];
                            runningNumber -= num[yd + blend];
                        }

                        if (yi >= 0 && yi < Constants.REGION_SIZE) {
                            Region r = regionLoader.findRegionForWorldCoordinates(baseX + xi, baseY + yi);
                            if (r == null) {
                                continue;
                            }

                            int underlayId = r.getUnderlayId(z, xi, yi) & 0xFF;
                            int overlayId = r.getOverlayId(z, xi, yi) & 0xFF;
                            if (underlayId <= 0 && overlayId <= 0) {
                                continue;
                            }

                            int swHeight = getTileHeight(z, baseX + xi, baseY + yi);
                            int seHeight = getTileHeight(z, baseX + xi + 1, baseY + yi);
                            int neHeight = getTileHeight(z, baseX + xi + 1, baseY + yi + 1);
                            int nwHeight = getTileHeight(z, baseX + xi, baseY + yi + 1);

                            int swColor = sceneRegion.getTileColors()[xi][yi];
                            int seColor = sceneRegion.getTileColors()[xi + 1][yi];
                            int neColor = sceneRegion.getTileColors()[xi + 1][yi + 1];
                            int nwColor = sceneRegion.getTileColors()[xi][yi + 1];
                            int rgb = -1;

                            int underlayHsl = -1;
                            if (underlayId > 0) {
                                int avgHue = runningHues * 256 / runningMultiplier;
                                int avgSat = runningSat / runningNumber;
                                int avgLight = runningLight / runningNumber;
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
                                sceneRegion.addTile(z, xi, yi, 0, 0, -1, swHeight, seHeight, neHeight, nwHeight, method4220(rgb, swColor), method4220(rgb, seColor), method4220(rgb, neColor), method4220(rgb, nwColor), 0, 0, 0, 0, underlayRgb, 0);
                            } else {
                                int overlayPath = r.getOverlayPath(z, xi, yi) + 1;
                                int overlayRotation = r.getOverlayRotation(z, xi, yi);

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
                                sceneRegion.addTile(z, xi, yi, overlayPath, overlayRotation, overlayTexture, swHeight, seHeight, neHeight, nwHeight, method4220(rgb, swColor), method4220(rgb, seColor), method4220(rgb, neColor), method4220(rgb, nwColor), adjustHSLListness0(overlayHsl, swColor), adjustHSLListness0(overlayHsl, seColor), adjustHSLListness0(overlayHsl, neColor), adjustHSLListness0(overlayHsl, nwColor), underlayRgb, overlayRgb);
                            }
                        }
                    }
                }
            }
        }

        sceneRegion.getLocations().forEach(loc -> {
            ObjectDefinition objectDefinition = objectManager.getObject(loc.getId());
            ModelDefinition modelDefinition = objectDefinition.getModel(loc.getType(), loc.getOrientation());

            int z = loc.getPosition().getZ();
            int x = loc.getPosition().getX();
            int y = loc.getPosition().getY();
//            int xSize = (x << 7) + (width << 6);
//            int ySize = (y << 7) + (length << 6);

            int swHeight = getTileHeight(z, x, y);
            int seHeight = getTileHeight(z, x + 1, y);
            int neHeight = getTileHeight(z, x + 1, y + 1);
            int nwHeight = getTileHeight(z, x, y + 1);

            int height = swHeight + seHeight + neHeight + nwHeight >> 2;

            if (loc.getType() == LocationType.FLOOR_DECORATION.getValue()) {
                if (modelDefinition != null) {
                    StaticObject model = new StaticObject(modelDefinition, objectDefinition.getAmbient() + 64, objectDefinition.getContrast() + 768, -50, -10, -50);

                    if (objectDefinition.getContouredGround() >= 0) {
//                               model = model.contourGround(mapLoader, xSize, height, ySize, true, objectDefinition.getContouredGround(), worldX, worldY);
                    }

                    sceneRegion.newFloorDecoration(z, loc.getPosition().getX() - baseX, loc.getPosition().getY() - baseY, height, model, 0, 0);
                }
            }

            if (loc.getType() == LocationType.INTERACTABLE_WALL_DECORATION.getValue()) {
                Entity entity;
                if (objectDefinition.getAnimationID() == -1 && modelDefinition != null) {
                    entity = new StaticObject(modelDefinition, objectDefinition.getAmbient() + 64, objectDefinition.getContrast() + 768, -50, -10, -50);
                } else if (objectDefinition.getAnimationID() > 0) {
                    entity = new DynamicObject(objectManager, loc.getId(), loc.getType(), loc.getOrientation(), height, loc.getPosition().getX() - baseX, loc.getPosition().getY() - baseY, objectDefinition.getAnimationID(), true, null);
                } else {
                    return;
                }

                int[] orientationTransform = {1, 2, 4, 8};
                sceneRegion.newWallDecoration(z, loc.getPosition().getX() - baseX, loc.getPosition().getY() - baseY, height, entity, null, orientationTransform[loc.getOrientation()], 0, 0, 0);
            }


// TODO: in progress

//                    if (loc.getType() == LocationType.INTERACTABLE.getValue()) {
//                        if (modelDefinition != null) {
//                            ModelImpl model = new ModelImpl(modelDefinition, objectDefinition.getAmbient() + 64, objectDefinition.getContrast() + 768, -50, -10, -50);
//                            scene.newWallDecoration(z, loc.getPosition().getX() - baseX, loc.getPosition().getY() - baseY, height, model, null, 0, 0, 0, 0);
//                        }
//                    }

//            if (loc.getType() == LocationType.INTERACTABLE.getValue()) {
//                if (modelDefinition != null) {
//                    ModelImpl model = new ModelImpl(modelDefinition, objectDefinition.getAmbient() + 64, objectDefinition.getContrast() + 768, -50, -10, -50);
//                    sceneRegion.newWallDecoration(z, loc.getPosition().getX() - baseX, loc.getPosition().getY() - baseY, height, model, null, 0, 0, 0, 0);
//                }
//            }
        });

        return sceneRegion;
    }

    public int getTileHeight(int z, int x, int y) {
        Region r = regionLoader.findRegionForWorldCoordinates(x, y);
        if (r == null) {
            return 0;
        }
        return r.getTileHeight(z, x % 64, y % 64);
    }

    public int getTileSettings(int z, int x, int y) {
        Region r = regionLoader.findRegionForWorldCoordinates(x, y);
        if (r == null) {
            return 0;
        }

        return r.getTileSetting(z, x % 64, y % 64);
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

    private static int convert(int d) {
        if (d >= 0) {
            return d % 64;
        } else {
            return 64 - -(d % 64) - 1;
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
