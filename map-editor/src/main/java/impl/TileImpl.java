package impl;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;

@Getter
@Setter
public class TileImpl implements Tile {
    private int plane;
    private int x;
    private int y;
    private TilePaint tilePaint;
    private TileModel tileModel;

    public TileImpl(int z, int x, int y) {
        this.plane = z;
        this.x = x;
        this.y = y;
    }

    @Override
    public DecorativeObject getDecorativeObject() {
        return null;
    }

    @Override
    public GameObject[] getGameObjects() {
        return new GameObject[0];
    }

    @Override
    public TileItemPile getItemLayer() {
        return null;
    }

    @Override
    public GroundObject getGroundObject() {
        return null;
    }

    @Override
    public WallObject getWallObject() {
        return null;
    }

    @Override
    public Point getSceneLocation() {
        return new Point(x, y);
    }

    @Override
    public int getRenderLevel() {
        return plane;
    }

    @Override
    public boolean hasLineOfSightTo(Tile other) {
        return false;
    }

    @Override
    public List<TileItem> getGroundItems() {
        return null;
    }

    @Override
    public Tile getBridge() {
        return null;
    }

    @Override
    public long getHash() {
        return 0;
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public Point getCanvasLocation() {
        return null;
    }

    @Override
    public Point getCanvasLocation(int zOffset) {
        return null;
    }

    @Override
    public Polygon getCanvasTilePoly() {
        return null;
    }

    @Override
    public Point getCanvasTextLocation(Graphics2D graphics, String text, int zOffset) {
        return null;
    }

    @Override
    public Point getMinimapLocation() {
        return null;
    }

    @Nullable
    @Override
    public Shape getClickbox() {
        return null;
    }

    @Override
    public WorldPoint getWorldLocation() {
        return null;
    }

    @Override
    public LocalPoint getLocalLocation() {
        return null;
    }
}
