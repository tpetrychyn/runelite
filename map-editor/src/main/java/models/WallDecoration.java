package models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Entity;
import net.runelite.api.Perspective;
import renderer.SceneUploader;
import renderer.helpers.GpuIntBuffer;
import renderer.helpers.ModelBuffers;

import java.nio.IntBuffer;

import static renderer.helpers.ModelBuffers.MAX_TRIANGLE;

@Getter
@Setter
@AllArgsConstructor
public class WallDecoration extends Renderable {
    private long tag;
    private int flags;
    private int x;
    private int y;
    private int height;
    private Entity entityA;
    private Entity entityB;
    private int orientationA;
    private int orientationB;

    public void draw(ModelBuffers modelBuffers, int sceneX, int sceneY) {
        int x = sceneX * Perspective.LOCAL_TILE_SIZE + Perspective.LOCAL_HALF_TILE_SIZE;
        int z = sceneY * Perspective.LOCAL_TILE_SIZE + Perspective.LOCAL_HALF_TILE_SIZE;

        int tc = Math.min(MAX_TRIANGLE, entityA.getModel().getTrianglesCount());
        int uvOffset = entityA.getModel().getUvBufferOffset();

        GpuIntBuffer b = modelBuffers.bufferForTriangles(tc);

        b.ensureCapacity(9);
        IntBuffer buffer = b.getBuffer();
        buffer.put(entityA.getModel().getBufferOffset());
        buffer.put(uvOffset);
        buffer.put(tc);
        buffer.put(modelBuffers.getTargetBufferOffset());
        buffer.put(ModelBuffers.FLAG_SCENE_BUFFER | (entityA.getModel().getRadius() << 12) | orientationA);
        buffer.put(x).put(height).put(z);
        buffer.put(modelBuffers.calcPickerId(sceneX, sceneY, 3));

        modelBuffers.addTargetBufferOffset(tc * 3);
    }

    public void drawDynamic(ModelBuffers modelBuffers, SceneUploader sceneUploader) {
        int x = getSceneX() * Perspective.LOCAL_TILE_SIZE + Perspective.LOCAL_HALF_TILE_SIZE;
        int y = getSceneY() * Perspective.LOCAL_TILE_SIZE + Perspective.LOCAL_HALF_TILE_SIZE;
        int faces = Math.min(MAX_TRIANGLE, entityA.getModel().getTrianglesCount());
        modelBuffers.getVertexBuffer().ensureCapacity(12 * faces);
        modelBuffers.getUvBuffer().ensureCapacity(12 * faces);
        int len = 0;
        for (int i = 0; i < faces; ++i) {
            len += sceneUploader.pushFace(entityA.getModel(), i, modelBuffers.getVertexBuffer(), modelBuffers.getUvBuffer());
        }
        GpuIntBuffer b = modelBuffers.bufferForTriangles(faces);

        b.ensureCapacity(9);
        IntBuffer buffer = b.getBuffer();
        buffer.put(modelBuffers.getTempOffset());
        buffer.put(-1);
        buffer.put(len / 3);
        buffer.put(modelBuffers.getTargetBufferOffset() + modelBuffers.getTempOffset());
        buffer.put((entityA.getModel().getRadius() << 12) | getOrientationA());
        buffer.put(x).put(height).put(y);
        buffer.put(-1);//modelBuffers.calcPickerId(getSceneX(), getSceneY(), 3));

        modelBuffers.addTempOffset(len);
    }
}
