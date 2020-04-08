package impl;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.TilePaint;

@Getter
@Setter
public class TilePaintImpl implements TilePaint {
    private int bufferOffset;
    private int uvBufferOffset;
    private int bufferLen;

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

    @Override
    public int getRBG() {
        return rgb;
    }

    @Override
    public int getBufferOffset() {
        return bufferOffset;
    }

    @Override
    public void setBufferOffset(int bufferOffset) {
        this.bufferOffset = bufferOffset;
    }

    @Override
    public int getUvBufferOffset() {
        return uvBufferOffset;
    }

    @Override
    public void setUvBufferOffset(int bufferOffset) {
        this.uvBufferOffset = bufferOffset;
    }

    @Override
    public int getBufferLen() {
        return bufferLen;
    }

    @Override
    public void setBufferLen(int bufferLen) {
        this.bufferLen = bufferLen;
    }
}
