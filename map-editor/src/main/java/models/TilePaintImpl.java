package models;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Perspective;
import net.runelite.api.TilePaint;
import org.apache.commons.lang3.NotImplementedException;
import renderer.SceneUploader;
import renderer.helpers.GpuFloatBuffer;
import renderer.helpers.GpuIntBuffer;
import renderer.helpers.ModelBuffers;
import renderer.helpers.PickerType;

import java.nio.IntBuffer;

import static renderer.helpers.ModelBuffers.FLAG_SCENE_BUFFER;

@Getter
@Setter
public class TilePaintImpl extends Renderable {
    private boolean isFlat;
    private int swHeight;
    private int seHeight;
    private int neHeight;
    private int nwHeight;

    private int swColor;
    private int seColor;
    private int neColor;
    private int nwColor;
    private int texture;
    private int rgb;

    public TilePaintImpl(int swColor, int seColor, int neColor, int nwColor, int texture, int rgb, boolean isFlat) {
        this.isFlat = true;
        this.swColor = swColor;
        this.seColor = seColor;
        this.neColor = neColor;
        this.nwColor = nwColor;
        this.texture = texture;
        this.rgb = rgb;
        this.isFlat = isFlat;
    }

    public TilePaintImpl(TilePaintImpl orig) {
        swColor = orig.swColor;
        seColor = orig.seColor;
        neColor = orig.neColor;
        nwColor = orig.nwColor;
        swHeight = orig.swHeight;
        seHeight = orig.seHeight;
        neHeight = orig.neHeight;
        nwHeight = orig.nwHeight;
        texture = orig.texture;
        rgb = orig.rgb;
        isFlat = orig.isFlat;
        sceneX = orig.sceneX;
        sceneY = orig.sceneY;
    }

    public void setSolidColor(int color) {
        swColor = color;
        seColor = color;
        neColor = color;
        nwColor = color;
    }

    public void update(GL4 gl, int bufferId, ModelBuffers modelBuffers, SceneUploader sceneUploader, int sceneX, int sceneY) {
        GpuIntBuffer modifyBuffer = new GpuIntBuffer();
        GpuFloatBuffer modifyUvBuffer = new GpuFloatBuffer();
        sceneUploader.upload(this, modifyBuffer, modifyUvBuffer);
        modifyBuffer.clear();

        // modify the persistent vertex buffer on the GPU
        gl.glBindBuffer(gl.GL_ARRAY_BUFFER, bufferId);
//         tile.getTilePaint().getBufferOffset() * GLBuffers.SIZEOF_INT * 4 ---- offset is 6 writes, 4 ints each
        gl.glBufferSubData(gl.GL_ARRAY_BUFFER, getBufferOffset() * GLBuffers.SIZEOF_INT * 4, modifyBuffer.getBuffer().limit(), modifyBuffer.getBuffer());
        gl.glBindBuffer(gl.GL_ARRAY_BUFFER, 0);

        int x = sceneX * Perspective.LOCAL_TILE_SIZE;
        int y = 0;
        int z = sceneY * Perspective.LOCAL_TILE_SIZE;

        GpuIntBuffer b = modelBuffers.getModelBufferUnordered();
        modelBuffers.incUnorderedModels();

        b.ensureCapacity(9);
        IntBuffer buffer = b.getBuffer();
        buffer.put(getBufferOffset());
        buffer.put(getUvBufferOffset());
        buffer.put(2);
        buffer.put(getSceneBufferOffset());
        buffer.put(FLAG_SCENE_BUFFER);
        buffer.put(x).put(y).put(z);
        buffer.put(modelBuffers.calcPickerId(sceneX, sceneY, 0));
    }

    public void draw(ModelBuffers modelBuffers, int sceneX, int sceneY) {
        int x = sceneX * Perspective.LOCAL_TILE_SIZE;
        int y = 0;
        int z = sceneY * Perspective.LOCAL_TILE_SIZE;

        GpuIntBuffer b = modelBuffers.getModelBufferUnordered();
        modelBuffers.incUnorderedModels();

        b.ensureCapacity(13);
        IntBuffer buffer = b.getBuffer();
        buffer.put(getBufferOffset());
        buffer.put(getUvBufferOffset());
        buffer.put(2);
        buffer.put(modelBuffers.getTargetBufferOffset());
        buffer.put(FLAG_SCENE_BUFFER);
        buffer.put(x).put(y).put(z);
        buffer.put(modelBuffers.calcPickerId(sceneX, sceneY, 0));
        buffer.put(-1).put(-1).put(-1).put(-1);

        setSceneBufferOffset(modelBuffers.getTargetBufferOffset());
        modelBuffers.addTargetBufferOffset(2 * 3);
    }

    public void drawDynamic(ModelBuffers modelBuffers, SceneUploader sceneUploader, PickerType pickerType) {
        int x = getSceneX() * Perspective.LOCAL_TILE_SIZE;
        int y = 0;
        int z = getSceneY() * Perspective.LOCAL_TILE_SIZE;

        // cpu vertexBuffer is cleared every frame, temp tiles can write to it from 0
        int len = sceneUploader.upload(this, modelBuffers.getVertexBuffer(), modelBuffers.getUvBuffer());

        GpuIntBuffer b = modelBuffers.getModelBufferUnordered();
        modelBuffers.incUnorderedModels();

        b.ensureCapacity(13);
        IntBuffer buffer = b.getBuffer();
        buffer.put(modelBuffers.getTempOffset());
        buffer.put(-1);
        buffer.put(2);
        buffer.put(modelBuffers.getTargetBufferOffset() + modelBuffers.getTempOffset());
        buffer.put(0);
        buffer.put(x).put(y).put(z);
        buffer.put(pickerType.getValue());
        buffer.put(-1).put(-1).put(-1).put(-1);

        modelBuffers.addTempOffset(len);
    }

    @Override
    public boolean equals(Object v) {
        if (v instanceof TilePaintImpl) {
            return ((TilePaintImpl) v).sceneX == sceneX && ((TilePaintImpl) v).sceneY == sceneY && ((TilePaintImpl) v).rgb == rgb;
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + sceneX;
        hash = 17 * hash + sceneY;
        hash = 17 * hash + rgb;
        hash = 17 * hash + texture;
        return hash;
    }
}
