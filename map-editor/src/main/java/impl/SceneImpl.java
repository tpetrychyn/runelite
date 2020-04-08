package impl;

import net.runelite.api.Constants;
import net.runelite.api.Occluder;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.coords.WorldPoint;

public class SceneImpl implements Scene {
    private int drawDistance = 90;
    private Occluder[][] planeOccluders;
    private int[] planeOccluderCounts;
    private Tile[][][] tiles;

    public SceneImpl() {
        tiles = new Tile[Constants.MAX_Z][Constants.SCENE_SIZE][Constants.SCENE_SIZE];
        planeOccluders = new Occluder[4][500];
        planeOccluderCounts = new int[4];
    }

    @Override
    public Tile[][][] getTiles() {
        return tiles;
    }

    public void addTile(int z, int x, int y, int overlayPath, int overlayRotation, int overlayTexture, int swHeight, int seHeight, int neHeight, int nwHeight, int swColor, int seColor, int neColor, int nwColor, int var15, int var16, int var17, int var18, int rgb, int overlayRgb) {
        if (overlayPath == 0) {
            for (int iz = z; iz >= 0; --iz) {
                if (this.tiles[iz][x][y] == null) {
                    this.tiles[iz][x][y] = new TileImpl(iz, x, y);
                }
            }

            ((TileImpl)this.tiles[z][x][y]).setTilePaint(new TilePaintImpl(swColor, seColor, neColor, nwColor, -1, rgb, false));
            ((TilePaintImpl)this.tiles[z][x][y].getTilePaint()).setSwHeight(swHeight);
            ((TilePaintImpl)this.tiles[z][x][y].getTilePaint()).setSeHeight(seHeight);
            ((TilePaintImpl)this.tiles[z][x][y].getTilePaint()).setNeHeight(neHeight);
            ((TilePaintImpl)this.tiles[z][x][y].getTilePaint()).setNwHeight(nwHeight);
        } else if (overlayPath != 1) {
            for (int iz = z; iz >= 0; --iz) {
                if (this.tiles[iz][x][y] == null) {
                    this.tiles[iz][x][y] = new TileImpl(iz, x, y);
                }
            }

            ((TileImpl)this.tiles[z][x][y]).setTileModel(new TileModelImpl(overlayPath, overlayRotation, overlayTexture, x, y, swHeight, seHeight, neHeight, nwHeight, swColor, seColor, neColor, nwColor, var15, var16, var17, var18, rgb, overlayRgb));
        } else {
            for (int iz = z; iz >= 0; --iz) {
                if (this.tiles[iz][x][y] == null) {
                    this.tiles[iz][x][y] = new TileImpl(iz, x, y);
                }
            }

            ((TileImpl)this.tiles[z][x][y]).setTilePaint(new TilePaintImpl(var15, var16, var17, var18, overlayTexture, overlayRgb, seHeight == swHeight && swHeight == neHeight && nwHeight == swHeight));
            ((TilePaintImpl)this.tiles[z][x][y].getTilePaint()).setSwHeight(swHeight);
            ((TilePaintImpl)this.tiles[z][x][y].getTilePaint()).setSeHeight(seHeight);
            ((TilePaintImpl)this.tiles[z][x][y].getTilePaint()).setNeHeight(neHeight);
            ((TilePaintImpl)this.tiles[z][x][y].getTilePaint()).setNwHeight(nwHeight);
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

    @Override
    public void addItem(int id, int quantity, WorldPoint point) {

    }

    @Override
    public void removeItem(int id, int quantity, WorldPoint point) {

    }

    @Override
    public int getDrawDistance() {
        return drawDistance;
    }

    @Override
    public void setDrawDistance(int drawDistance) {
        this.drawDistance = drawDistance;
    }
}
