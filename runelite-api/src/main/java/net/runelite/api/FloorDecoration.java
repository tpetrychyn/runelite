package net.runelite.api;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

import javax.annotation.Nullable;
import java.awt.*;

@Getter
@Setter
public class FloorDecoration implements GroundObject, Renderable {
    private Entity entity;
    private Model model;
    private Renderable renderable;
    private int x;
    private int y;
    private int height;
    private long tag;
    private int flags;

    @Override
    public Shape getConvexHull() {
        return null;
    }

    @Override
    public Node getNext() {
        return null;
    }

    @Override
    public Node getPrevious() {
        return null;
    }

    @Override
    public long getHash() {
        return 0;
    }

    @Override
    public int getPlane() {
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

    @Override
    public int getModelHeight() {
        return 0;
    }

    @Override
    public void setModelHeight(int modelHeight) { }

    @Override
    public void draw(int orientation, int pitchSin, int pitchCos, int yawSin, int yawCos, int x, int y, int z, long hash) {

    }
}
