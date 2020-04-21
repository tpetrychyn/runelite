package models;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Perspective;
import net.runelite.api.TilePaint;
import org.apache.commons.lang3.NotImplementedException;
import renderer.SceneUploader;
import renderer.helpers.GpuIntBuffer;
import renderer.helpers.ModelBuffers;

import java.nio.IntBuffer;

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

    public void draw(ModelBuffers modelBuffers, int sceneX, int sceneY) {
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
        buffer.put(modelBuffers.getTargetBufferOffset());
        buffer.put(ModelBuffers.FLAG_SCENE_BUFFER);
        buffer.put(x).put(y).put(z);
        buffer.put(modelBuffers.calcPickerId(sceneX, sceneY, 0));

        setSceneBufferOffset(modelBuffers.getTargetBufferOffset());
        modelBuffers.addTargetBufferOffset(2 * 3);
    }

    @Override
    public void drawDynamic(ModelBuffers modelBuffers, SceneUploader sceneUploader) {
        throw new NotImplementedException("tile paints do not have dynamic draws");
    }
}
