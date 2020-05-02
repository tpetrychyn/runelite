package models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Entity;
import net.runelite.api.Model;
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
        Model model = entityA.getModel();
        if (model == null) {
            return;
        }

        model.calculateBoundsCylinder();
        model.calculateExtreme(orientationA);

        int x = sceneX * Perspective.LOCAL_TILE_SIZE + Perspective.LOCAL_HALF_TILE_SIZE;
        int z = sceneY * Perspective.LOCAL_TILE_SIZE + Perspective.LOCAL_HALF_TILE_SIZE;

        int tc = Math.min(MAX_TRIANGLE, model.getTrianglesCount());

        GpuIntBuffer b = modelBuffers.bufferForTriangles(tc);

        b.ensureCapacity(13);
        IntBuffer buffer = b.getBuffer();
        buffer.put(model.getBufferOffset());
        buffer.put(model.getUvBufferOffset());
        buffer.put(tc);
        buffer.put(modelBuffers.getTargetBufferOffset());
        buffer.put(ModelBuffers.FLAG_SCENE_BUFFER | (model.getRadius() << 12) | orientationA);
        buffer.put(x).put(height).put(z);
        buffer.put(modelBuffers.calcPickerId(sceneX, sceneY, 3));
        buffer.put(-1).put(-1).put(-1).put(-1);

        modelBuffers.addTargetBufferOffset(tc * 3);
    }

    public void drawDynamic(ModelBuffers modelBuffers, SceneUploader sceneUploader) {
        Model model = entityA.getModel();
        if (model == null) {
            return;
        }

        int x = getSceneX() * Perspective.LOCAL_TILE_SIZE + Perspective.LOCAL_HALF_TILE_SIZE;
        int y = getSceneY() * Perspective.LOCAL_TILE_SIZE + Perspective.LOCAL_HALF_TILE_SIZE;
        int faces = Math.min(MAX_TRIANGLE, model.getTrianglesCount());
        modelBuffers.getVertexBuffer().ensureCapacity(12 * faces);
        modelBuffers.getUvBuffer().ensureCapacity(12 * faces);
        int len = 0;
        for (int i = 0; i < faces; ++i) {
            len += sceneUploader.pushFace(model, i, modelBuffers.getVertexBuffer(), modelBuffers.getUvBuffer());
        }
        GpuIntBuffer b = modelBuffers.bufferForTriangles(faces);

        b.ensureCapacity(9);
        IntBuffer buffer = b.getBuffer();
        buffer.put(modelBuffers.getTempOffset());
        buffer.put(-1);
        buffer.put(len / 3);
        buffer.put(modelBuffers.getTargetBufferOffset() + modelBuffers.getTempOffset());
        buffer.put((model.getRadius() << 12) | getOrientationA());
        buffer.put(x).put(height).put(y);
        buffer.put(-2);
        buffer.put(-1).put(-1).put(-1).put(-1);

        modelBuffers.addTempOffset(len);
//        DynamicObject dyn = (DynamicObject) entityA;
//        int idx = 0;
//        int animOffset = 0;
//        int totalFramesCountUsed = 0;
//        for (int i=dyn.getSequenceDefinition().frameIds.length - dyn.getSequenceDefinition().frameCount;i<dyn.getSequenceDefinition().frameIds.length;i++) {
//            totalFramesCountUsed += dyn.getSequenceDefinition().frameLengths[i];
//        }
//        for (int i=dyn.getSequenceDefinition().frameIds.length - dyn.getSequenceDefinition().frameCount;i<dyn.getSequenceDefinition().frameIds.length;i++) {
//            Model model = dyn.getModel(i);
//            if (model == null) {
//                continue;
//            }
//            int x = sceneX * Perspective.LOCAL_TILE_SIZE + Perspective.LOCAL_HALF_TILE_SIZE;
//            int z = sceneY * Perspective.LOCAL_TILE_SIZE + Perspective.LOCAL_HALF_TILE_SIZE;
//
//            int tc = Math.min(MAX_TRIANGLE, model.getTrianglesCount());
//            int uvOffset = model.getUvBufferOffset();
//
//            GpuIntBuffer b = modelBuffers.bufferForTriangles(tc);
//
//            b.ensureCapacity(13);
//            IntBuffer buffer = b.getBuffer();
//            buffer.put(model.getBufferOffset());
//            buffer.put(uvOffset);
//            buffer.put(tc);
//            buffer.put(modelBuffers.getTargetBufferOffset());
//            buffer.put(ModelBuffers.FLAG_SCENE_BUFFER | (model.getRadius() << 12) | orientationA);
//            buffer.put(x).put(height).put(z);
//            buffer.put(modelBuffers.calcPickerId(sceneX, sceneY, 3));
//            buffer.put(idx).put(dyn.getSequenceDefinition().frameLengths[i]).put(animOffset).put(totalFramesCountUsed);
//
//            idx++;
//            animOffset += dyn.getSequenceDefinition().frameLengths[i];
//            modelBuffers.addTargetBufferOffset(tc * 3);
//        }
    }

    public void addDynamic(ModelBuffers modelBuffers, SceneUploader sceneUploader) {
        Model model = entityA.getModel();
        if (model == null) {
            return;
        }

        int x = getSceneX() * Perspective.LOCAL_TILE_SIZE + Perspective.LOCAL_HALF_TILE_SIZE;
        int y = getSceneY() * Perspective.LOCAL_TILE_SIZE + Perspective.LOCAL_HALF_TILE_SIZE;
        int faces = Math.min(MAX_TRIANGLE, model.getTrianglesCount());
        modelBuffers.getVertexBuffer().ensureCapacity(12 * faces);
        modelBuffers.getUvBuffer().ensureCapacity(12 * faces);
        int len = 0;
        for (int i = 0; i < faces; ++i) {
            len += sceneUploader.pushFace(model, i, modelBuffers.getVertexBuffer(), modelBuffers.getUvBuffer());
        }
        GpuIntBuffer b = modelBuffers.bufferForTriangles(faces);

        b.ensureCapacity(9);
        IntBuffer buffer = b.getBuffer();
        buffer.put(modelBuffers.getTempOffset());
        buffer.put(-1);
        buffer.put(len / 3);
        buffer.put(modelBuffers.getTargetBufferOffset() + modelBuffers.getTempOffset());
        buffer.put((model.getRadius() << 12) | getOrientationA());
        buffer.put(x).put(height).put(y);
        buffer.put(modelBuffers.calcPickerId(sceneX, sceneY, 3));
        buffer.put(-1).put(-1).put(-1).put(-1);

        modelBuffers.addTempOffset(len);
    }
}
