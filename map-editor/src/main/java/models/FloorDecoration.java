package models;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import renderer.SceneUploader;
import renderer.helpers.GpuIntBuffer;
import renderer.helpers.ModelBuffers;

import javax.annotation.Nullable;
import java.awt.*;
import java.nio.IntBuffer;

@Getter
@Setter
public class FloorDecoration extends Renderable {
    private int id;
    private int orientation;
    private Model model;
    private int x;
    private int y;
    private int height;
    private long tag;
    private int flags;

    public void draw(ModelBuffers modelBuffers, int sceneX, int sceneY) {
        int x = sceneX * Perspective.LOCAL_TILE_SIZE + Perspective.LOCAL_HALF_TILE_SIZE;
        int z = sceneY * Perspective.LOCAL_TILE_SIZE + Perspective.LOCAL_HALF_TILE_SIZE;

        int tc = Math.min(ModelBuffers.MAX_TRIANGLE, model.getTrianglesCount());
        int uvOffset = model.getUvBufferOffset();

        GpuIntBuffer b = modelBuffers.bufferForTriangles(tc);

        b.ensureCapacity(9);
        IntBuffer buffer = b.getBuffer();
        buffer.put(model.getBufferOffset());
        buffer.put(uvOffset);
        buffer.put(tc);
        buffer.put(modelBuffers.getTargetBufferOffset());
        buffer.put(ModelBuffers.FLAG_SCENE_BUFFER | (model.getRadius() << 12) | orientation);
        buffer.put(x).put(height).put(z);
        buffer.put(modelBuffers.calcPickerId(sceneX, sceneY, 2));

        modelBuffers.addTargetBufferOffset(tc * 3);
    }

    @Override
    public void drawDynamic(ModelBuffers modelBuffers, SceneUploader sceneUploader) {
        //TODO
    }

    public String toString() {
        return String.format("Floor Decoration: id %d, x %d, y %d, height %d", id, x, y, height);
    }
}
