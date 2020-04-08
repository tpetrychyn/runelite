import impl.SceneImpl;
import impl.TilePaintImpl;
import impl.TileImpl;
import net.runelite.api.Constants;
import net.runelite.api.Scene;
import net.runelite.cache.*;
import net.runelite.cache.TextureManager;
import net.runelite.cache.definitions.OverlayDefinition;
import net.runelite.cache.definitions.SpriteDefinition;
import net.runelite.cache.definitions.UnderlayDefinition;
import net.runelite.cache.definitions.loaders.OverlayLoader;
import net.runelite.cache.definitions.loaders.SpriteLoader;
import net.runelite.cache.definitions.loaders.UnderlayLoader;
import net.runelite.cache.fs.*;
import net.runelite.cache.item.ColorPalette;
import net.runelite.cache.item.RSTextureProvider;
import net.runelite.cache.region.Region;
import net.runelite.cache.region.RegionLoader;
import net.runelite.cache.util.Djb2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MapPreparer {

    private static final Logger logger = LoggerFactory.getLogger(MapPreparer.class);

    private static final int MAP_SCALE = 4; // this squared is the number of pixels per map square
    private static final int MAPICON_MAX_WIDTH = 5; // scale minimap icons down to this size so they fit..
    private static final int MAPICON_MAX_HEIGHT = 6;
    private static final int BLEND = 5; // number of surrounding tiles for ground blending

    private static int[] colorPalette = new ColorPalette(0.9d, 0, 512).getColorPalette();

    private static int[][] TILE_SHAPE_2D = new int[][]{{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, {1, 0, 0, 0, 1, 1, 0, 0, 1, 1, 1, 0, 1, 1, 1, 1}, {1, 1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0}, {0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 1}, {0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, {1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1}, {1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 0}, {1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 0, 0, 1, 1}, {1, 1, 1, 1, 1, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 1, 1}};
    private static int[][] TILE_ROTATION_2D = new int[][]{{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15}, {12, 8, 4, 0, 13, 9, 5, 1, 14, 10, 6, 2, 15, 11, 7, 3}, {15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0}, {3, 7, 11, 15, 2, 6, 10, 14, 1, 5, 9, 13, 0, 4, 8, 12}};

    private final int wallColor = (238 + (int) (Math.random() * 20.0D) - 10 << 16) + (238 + (int) (Math.random() * 20.0D) - 10 << 8) + (238 + (int) (Math.random() * 20.0D) - 10);
    private final int doorColor = 238 + (int) (Math.random() * 20.0D) - 10 << 16;

    private final Store store;

    private final Map<Integer, UnderlayDefinition> underlays = new HashMap<>();
    private final Map<Integer, OverlayDefinition> overlays = new HashMap<>();
    private final Map<Integer, Image> scaledMapIcons = new HashMap<>();

    private RegionLoader regionLoader;
    private final AreaManager areas;
    private final SpriteManager sprites;
    private RSTextureProvider rsTextureProvider;
    private final ObjectManager objectManager;

    public MapPreparer(Store store) {
        this.store = store;
        this.areas = new AreaManager(store);
        this.sprites = new SpriteManager(store);
        objectManager = new ObjectManager(store);
    }

    public void load() throws IOException {
        loadUnderlays(store);
        loadOverlays(store);
        objectManager.load();

        TextureManager textureManager = new TextureManager(store);
        textureManager.load();
        rsTextureProvider = new RSTextureProvider(textureManager, sprites);

        loadRegions(store);
        areas.load();
        sprites.load();
        loadSprites();
    }

    public void loadTiles(Region region, SceneImpl scene) {
        int baseX = region.getBaseX();
        int baseY = region.getBaseY();

        int len = 104;
        int[] hues = new int[len];
        int[] sats = new int[len];
        int[] light = new int[len];
        int[] mul = new int[len];
        int[] num = new int[len];

        int[][] tileColors = new int[105][105];

        int var9 = (int) Math.sqrt(5100.0D);
        int var10 = var9 * 768 >> 8;

        for (int z = 0; z < Constants.MAX_Z; z++) {

            for (int y = 1; y < 103; y++) {
                for (int x = 1; x < 103; x++) {
                    Region r = regionLoader.findRegionForWorldCoordinates(baseX + x, baseY + y);

                    int xHeightDiff = r.getTileHeight(0, convert(x + 1), convert(y)) - r.getTileHeight(0, convert(x - 1), convert(y));
                    int yHeightDiff = r.getTileHeight(0, convert(x), convert(y + 1)) - r.getTileHeight(0, convert(x), convert(y - 1));
                    int diff = (int) Math.sqrt((double) xHeightDiff * xHeightDiff + yHeightDiff * yHeightDiff + 65536);
                    int var16 = (xHeightDiff << 8) / diff;
                    int var17 = 65536 / diff;
                    int var18 = (yHeightDiff << 8) / diff;
                    int var19 = (var16 * -50 + var18 * -50 + var17 * -10) / var10 + 96;

                    int wSetting = r.getTileSetting(0, convert(x - 1), convert(y));
                    int sSetting = r.getTileSetting(0, convert(x), convert(y - 1));
                    int eSetting = r.getTileSetting(0, convert(x + 1), convert(y));
                    int nSetting = r.getTileSetting(0, convert(x), convert(y + 1));
                    int mySetting = r.getTileSetting(0, convert(x), convert(y));

                    int var20 = (wSetting >> 2 + sSetting >> 2 + eSetting >> 3 + nSetting >> 3 + mySetting >> 1);
                    tileColors[x][y] = var19 - var20;
                }
            }

            for (int xi = -5; xi < 109; ++xi) {
                for (int yi = 0; yi < 104; ++yi) {
                    int xr = xi + 5;
                    if (xr >= 0 && xr < 104) {
                        Region r = regionLoader.findRegionForWorldCoordinates(baseX + xr, baseY + yi);
                        if (r != null) {
                            int underlayId = r.getUnderlayId(z, convert(xr), convert(yi));
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
                    if (xl >= 0 && xl < 104) {
                        Region r = regionLoader.findRegionForWorldCoordinates(baseX + xl, baseY + yi);
                        if (r != null) {
                            int underlayId = r.getUnderlayId(z, convert(xl), convert(yi));
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

                if (xi >= 1 && xi < 103) {
                    int runningHues = 0;
                    int runningSat = 0;
                    int runningLight = 0;
                    int runningMultiplier = 0;
                    int runningNumber = 0;

                    for (int yi = -5; yi < 109; ++yi) {
                        int yu = yi + 5;
                        if (yu >= 0 && yu < 104) {
                            runningHues += hues[yu];
                            runningSat += sats[yu];
                            runningLight += light[yu];
                            runningMultiplier += mul[yu];
                            runningNumber += num[yu];
                        }

                        int yd = yi - 5;
                        if (yd >= 0 && yd < 104) {
                            runningHues -= hues[yd];
                            runningSat -= sats[yd];
                            runningLight -= light[yd];
                            runningMultiplier -= mul[yd];
                            runningNumber -= num[yd];
                        }

                        if (yi >= 1 && yi < 103) {
                            Region r = regionLoader.findRegionForWorldCoordinates(baseX + xi, baseY + yi);
                            if (r != null) {
                                int underlayId = r.getUnderlayId(z, convert(xi), convert(yi));
                                int overlayId = r.getOverlayId(z, convert(xi), convert(yi));

                                if (underlayId > 0 || overlayId > 0) {
                                    int swHeight = r.getTileHeight(z, convert(xi), convert(yi));
                                    int seHeight = r.getTileHeight(z, convert(xi + 1), convert(yi));
                                    int neHeight = r.getTileHeight(z, convert(xi + 1), convert(yi + 1));
                                    int nwHeight = r.getTileHeight(z, convert(xi), convert(yi + 1));

                                    int swColor = tileColors[xi][yi];
                                    int seColor = tileColors[xi + 1][yi];
                                    int neColor = tileColors[xi + 1][yi + 1];
                                    int nwColor = tileColors[xi][yi + 1];
                                    int rgb = -1;
                                    int underlayHsl = -1;

                                    if (underlayId > 0) {
                                        int avgHue = runningHues * 256 / runningMultiplier;
                                        int avgSat = runningSat / runningNumber;
                                        int avgLight = runningLight / runningNumber;
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
                                        int overlayPath = r.getOverlayPath(z, convert(xi), convert(yi)) + 1;
                                        int overlayRotation = r.getOverlayRotation(z, convert(xi), convert(yi));

                                        OverlayDefinition overlayDefinition = findOverlay(overlayId - 1);
                                        int overlayTexture = overlayDefinition.getTexture();
                                        int overlayHsl;

                                        int field550 = (int) (Math.random() * 17.0D) - 8;
                                        int field548 = (int) (Math.random() * 33.0D) - 16;

                                        if (overlayTexture >= 0) {
                                            rgb = rsTextureProvider.getAverageTextureRGB(overlayTexture);
                                            overlayHsl = -1;
                                        } else if (overlayDefinition.getRgbColor() == 0xFF_00FF) {
                                            overlayHsl = -2;
                                            overlayTexture = -1;
                                            rgb = -2;
                                        } else {
                                            overlayHsl = hslToRgb(overlayDefinition.getHue(), overlayDefinition.getSaturation(), overlayDefinition.getLightness());

                                            int hue = overlayDefinition.getHue() + field550 & 255;
                                            int lightness = overlayDefinition.getLightness() + field548;
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
                                            int hue = overlayDefinition.getOtherHue() + field550 & 255;
                                            int lightness = overlayDefinition.getOtherLightness() + field548;
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

        int z = 1;
        int var3 = 2;
        int var4 = 4;
        int var11 = 0;
        int var12 = 0;

        int[][][] renderFlags = new int[4][105][105];

        for (int renderLevel = 0; renderLevel < 4; ++renderLevel) {
            if (renderLevel > 0) {
                z <<= 3;
                var3 <<= 3;
                var4 <<= 3;
            }

            for (int zi = 0; zi <= renderLevel; ++zi) {
                for (int yi = 0; yi <= 104; ++yi) {
                    for (int xi = 0; xi <= 104; ++xi) {
                        short var46;
                        if ((renderFlags[zi][xi][yi] & z) != 0) {
                            var9 = yi;
                            var10 = yi;
                            var11 = zi;

                            for (var12 = zi; var9 > 0 && (renderFlags[zi][xi][var9 - 1] & z) != 0; --var9) {
                            }

                            while (var10 < 104 && (renderFlags[zi][xi][var10 + 1] & z) != 0) {
                                ++var10;
                            }

                            label465:
                            while (var11 > 0) {
                                for (int var13 = var9; var13 <= var10; ++var13) {
                                    if ((renderFlags[var11 - 1][xi][var13] & z) == 0) {
                                        break label465;
                                    }
                                }

                                --var11;
                            }

                            label454:
                            while (var12 < renderLevel) {
                                for (int var13 = var9; var13 <= var10; ++var13) {
                                    if ((renderFlags[var12 + 1][xi][var13] & z) == 0) {
                                        break label454;
                                    }
                                }

                                ++var12;
                            }

                            int var13 = (var10 - var9 + 1) * (var12 + 1 - var11);
                            if (var13 >= 8) {
                                var46 = 240;
                                Region r = regionLoader.findRegionForWorldCoordinates(baseX + xi, baseY + yi);

                                int var15 = r.getTileHeight(var12, convert(xi), convert(var9)) - var46;
                                int var16 = r.getTileHeight(var11, convert(xi), convert(var9));
                                scene.addOccluder(renderLevel, 1, xi * 128, xi * 128, var9 * 128, var10 * 128 + 128, var15, var16);

                                for (int var17 = var11; var17 <= var12; ++var17) {
                                    for (int var18 = var9; var18 <= var10; ++var18) {
                                        int[] var10000 = renderFlags[var17][xi];
                                        var10000[var18] &= ~z;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static int convert(int d) {
        if (d >= 0) {
            return d % 64;
        } else {
            return 64 - -(d % 64) - 1;
        }
    }

    private void loadRegions(Store store) throws IOException {
        regionLoader = new RegionLoader(store);
        regionLoader.loadRegions();
        regionLoader.calculateBounds();

        logger.info("North most region: {}", regionLoader.getLowestY().getBaseY());
        logger.info("South most region: {}", regionLoader.getHighestY().getBaseY());
        logger.info("West most region:  {}", regionLoader.getLowestX().getBaseX());
        logger.info("East most region:  {}", regionLoader.getHighestX().getBaseX());
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

