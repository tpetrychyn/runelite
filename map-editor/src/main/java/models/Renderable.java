package models;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Model;
import renderer.SceneUploader;
import renderer.helpers.ModelBuffers;

@Getter
@Setter
public abstract class Renderable {
    private long tag;
    private int flags;
    private int orientation;

    private int sceneX;
    private int sceneY;

    private int x; // 3d world space position
    private int y;
    private int height;

    // opengl buffer offsets
    private int bufferOffset = -1;
    private int uvBufferOffset = -1;
    private int sceneBufferOffset = -1;
    private int bufferLen = -1;
    private int pickerType = -1;

    public abstract void draw(ModelBuffers modelBuffers, int sceneX, int sceneY);
    public abstract void drawDynamic(ModelBuffers modelBuffers, SceneUploader sceneUploader);
}
