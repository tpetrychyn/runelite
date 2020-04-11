package models;

import net.runelite.api.*;

public class SceneImpl{
    private Occluder[][] planeOccluders;
    private int[] planeOccluderCounts;
    private SceneTile[][][] tiles;

    public SceneImpl() {
        tiles = new SceneTile[Constants.MAX_Z][Constants.SCENE_SIZE][Constants.SCENE_SIZE];
        planeOccluders = new Occluder[4][500];
        planeOccluderCounts = new int[4];
    }

    public SceneTile[][][] getTiles() {
        return tiles;
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
    }

    public void addOccluder(int plane, int type, int minX, int maxX, int minZ, int maxZ, int minY, int maxY) {
        int minTileX = minX / 128;
        int maxTileX = maxX / 128;
        int minTileY = minZ / 128;
        int maxTileY = maxZ / 128;
        planeOccluders[plane][planeOccluderCounts[plane]] = new Occluder(minTileX, maxTileX, minTileY, maxTileY, type, minX, maxX, minZ, maxZ, minY, maxY);
        planeOccluderCounts[plane]++;
    }

    public void newFloorDecoration(int z, int x, int y, int height, Entity entity, long tag, int flags) {
        for (int iz = z; iz >= 0; --iz) {
            if (this.tiles[iz][x][y] == null) {
                this.tiles[iz][x][y] = new SceneTile(iz, x, y);
            }
        }

        FloorDecoration floorDecoration = new FloorDecoration();
        floorDecoration.setEntity(entity);
        floorDecoration.setModel(entity.getModel());
        floorDecoration.setRenderable(floorDecoration);
        floorDecoration.setX(x * Perspective.LOCAL_TILE_SIZE + Constants.REGION_SIZE);
        floorDecoration.setY(y * Perspective.LOCAL_TILE_SIZE + Constants.REGION_SIZE);
        floorDecoration.setHeight(height);
        floorDecoration.setTag(tag);
        floorDecoration.setFlags(flags);

        this.tiles[z][x][y].setFloorDecoration(floorDecoration);
    }

    public void newWallDecoration(int z, int x, int y, int height, ModelImpl modelA, ModelImpl modelB, int orientationA, int orientationB, long tag, int flags) {
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
                modelA,
                modelB,
                orientationA,
                orientationB);

        this.tiles[z][x][y].setWallDecoration(wallDecoration);
    }
}
