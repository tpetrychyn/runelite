package models;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Model;
import renderer.SceneUploader;
import renderer.helpers.ModelBuffers;
import renderer.helpers.PickerType;

@Getter
@Setter
public abstract class Renderable {
    protected long tag;
    protected int flags;
    protected int orientation;

    protected int sceneX;
    protected int sceneY;

    private int x; // 3d world space position
    private int y;
    private int height;

    // opengl buffer offsets
    protected int bufferOffset = -1;
    protected int uvBufferOffset = -1;
    protected int sceneBufferOffset = -1;
    protected int bufferLen = -1;
    protected int pickerType = -1;

    public abstract void draw(ModelBuffers modelBuffers, int sceneX, int sceneY);
    public abstract void drawDynamic(ModelBuffers modelBuffers, SceneUploader sceneUploader, PickerType pickerType);
}
