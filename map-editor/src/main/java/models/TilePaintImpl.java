package models;

import com.google.inject.Inject;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Constants;
import net.runelite.api.Perspective;
import net.runelite.cache.definitions.UnderlayDefinition;
import net.runelite.cache.region.Region;
import renderer.SceneUploader;
import renderer.helpers.GpuFloatBuffer;
import renderer.helpers.GpuIntBuffer;
import renderer.helpers.ModelBuffers;
import scene.Scene;
import scene.SceneTile;

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

    private UnderlayDefinition underlayDefinition;

    public TilePaintImpl(int swColor, int seColor, int neColor, int nwColor, int texture, int rgb, boolean isFlat, UnderlayDefinition underlayDefinition) {
        this.isFlat = true;
        this.swColor = swColor;
        this.seColor = seColor;
        this.neColor = neColor;
        this.nwColor = nwColor;
        this.texture = texture;
        this.rgb = rgb;
        this.isFlat = isFlat;
        this.underlayDefinition = underlayDefinition;
    }

    public void calcBlendColor(Scene scene) {
        int baseX = sceneX;
        int baseY = sceneY;
        int blend = 5;
        int[] hues = new int[blend * 2];
        int[] sats = new int[blend * 2];
        int[] light = new int[blend * 2];
        int[] mul = new int[blend * 2];
        int[] num = new int[blend * 2];
        for (int xi = -blend * 2; xi < blend * 2; ++xi) {
            for (int yi = -blend; yi < blend; ++yi) {
                int xr = xi + 5;
                if (xr >= -blend && xr < blend) {
                    SceneTile tile = scene.getTile(0, baseX + xr, baseY + yi);
                    if (tile != null && tile.getTilePaint() != null) {
                        UnderlayDefinition underlay = tile.getTilePaint().underlayDefinition;
                        hues[yi + blend] += underlay.getHue();
                        sats[yi + blend] += underlay.getSaturation();
                        light[yi + blend] += underlay.getLightness();
                        mul[yi + blend] += underlay.getHueMultiplier();
                        num[yi + blend]++;
                    }
                }

                int xl = xi - 5;
                if (xl >= -blend && xl < blend) {
                    SceneTile tile = scene.getTile(0, baseX + xl, baseY + yi);
                    if (tile != null && tile.getTilePaint() != null) {
                        UnderlayDefinition underlay = tile.getTilePaint().underlayDefinition;
                        hues[yi + blend] += underlay.getHue();
                        sats[yi + blend] += underlay.getSaturation();
                        light[yi + blend] += underlay.getLightness();
                        mul[yi + blend] += underlay.getHueMultiplier();
                        num[yi + blend]++;
                    }
                }
            }

            if (xi >= 0) {
                int runningHues = 0;
                int runningSat = 0;
                int runningLight = 0;
                int runningMultiplier = 0;
                int runningNumber = 0;

                for (int yi = -blend * 2; yi < blend * 2; ++yi) {
                    int yu = yi + 5;
                    if (yu >= -blend && yu < blend) {
                        runningHues += hues[yu + blend];
                        runningSat += sats[yu + blend];
                        runningLight += light[yu + blend];
                        runningMultiplier += mul[yu + blend];
                        runningNumber += num[yu + blend];
                    }

                    int yd = yi - 5;
                    if (yd >= -blend && yd < blend) {
                        runningHues -= hues[yd + blend];
                        runningSat -= sats[yd + blend];
                        runningLight -= light[yd + blend];
                        runningMultiplier -= mul[yd + blend];
                        runningNumber -= num[yd + blend];
                    }

                    if (yi >= 0) {
                        if (xi == sceneX && yi == sceneY) {
                            int swColor = scene.getRegionFromSceneCoord(xi, yi).getTileColors()[xi][yi];
                            int seColor = scene.getRegionFromSceneCoord(xi + 1, yi).getTileColors()[xi + 1][yi];
                            int neColor = scene.getRegionFromSceneCoord(xi + 1, yi + 1).getTileColors()[xi + 1][yi + 1];
                            int nwColor = scene.getRegionFromSceneCoord(xi, yi + 1).getTileColors()[xi][yi + 1];

                            if (runningMultiplier == 0 || runningNumber == 0) {
                                continue;
                            }
                            int avgHue = runningHues * 256 / runningMultiplier;
                            int avgSat = runningSat / runningNumber;
                            int avgLight = runningLight / runningNumber;
                            rgb = hslToRgb(avgHue, avgSat, avgLight);

//                        if (avgLight < 0) {
//                            avgLight = 0;
//                        } else if (avgLight > 255) {
//                            avgLight = 255;
//                        }
//
//                        int underlayHsl = hslToRgb(avgHue, avgSat, avgLight);

                            //int swColor, int seColor, int neColor, int nwColor
                            this.swColor = method4220(rgb, swColor);
                            this.seColor = method4220(rgb, seColor);
                            this.neColor = method4220(rgb, neColor);
                            this.nwColor = method4220(rgb, nwColor);
                        }
                    }
                }
            }
        }
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
        buffer.put(modelBuffers.calcPickerId(sceneX, sceneY, 30));
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
        buffer.put(modelBuffers.calcPickerId(sceneX, sceneY, 30));
        buffer.put(-1).put(-1).put(-1).put(-1);

        setSceneBufferOffset(modelBuffers.getTargetBufferOffset());
        modelBuffers.addTargetBufferOffset(2 * 3);
    }

    public void drawDynamic(ModelBuffers modelBuffers, SceneUploader sceneUploader) {
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
        buffer.put(modelBuffers.calcPickerId(sceneX, sceneY, 30));
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

    int hslToRgb(int var0, int var1, int var2) {
        if (var2 > 179) {
            var1 /= 2;
        }

        if (var2 > 192) {
            var1 /= 2;
        }

        if (var2 > 217) {
            var1 /= 2;
        }

        if (var2 > 243) {
            var1 /= 2;
        }

        int var3 = (var1 / 32 << 7) + (var0 / 4 << 10) + var2 / 2;
        return var3;
    }

    static int method4220(int var0, int var1) {
        if (var0 == -1) {
            return 12345678;
        } else {
            var1 = (var0 & 127) * var1 / 128;
            if (var1 < 2) {
                var1 = 2;
            } else if (var1 > 126) {
                var1 = 126;
            }

            return (var0 & 65408) + var1;
        }
    }
}
