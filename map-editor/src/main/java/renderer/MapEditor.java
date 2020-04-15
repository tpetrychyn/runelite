package renderer;

import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.Screen;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.javafx.NewtCanvasJFX;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.Animator;
import config.AntiAliasingMode;
import eventHandlers.MouseListener;
import layoutControllers.MainController;
import layoutControllers.MinimapController;
import lombok.extern.slf4j.Slf4j;
import models.TilePaintImpl;
import models.WallDecoration;
import net.runelite.api.*;
import net.runelite.cache.SpriteManager;
import net.runelite.cache.fs.Store;
import net.runelite.cache.fs.StoreProvider;
import net.runelite.cache.item.RSTextureProvider;
import scene.Scene;
import scene.SceneRegionBuilder;
import scene.SceneTile;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

@Slf4j
public class MapEditor implements GLEventListener, KeyListener {

    // This is the maximum number of triangles the compute shaders support
    private static final int MAX_TRIANGLE = 4096;
    private static final int SMALL_TRIANGLE_COUNT = 512;
    private static final int FLAG_SCENE_BUFFER = Integer.MIN_VALUE;
    static final int MAX_DISTANCE = 1000;

    private MouseListener mouseListener;

    private SceneUploader sceneUploader = new SceneUploader();
    private TextureManager textureManager = new TextureManager();
    private RSTextureProvider textureProvider;
    private Scene scene;

    private GL4 gl;

    private int glProgram;
    private int glComputeProgram;
    private int glSmallComputeProgram;
    private int glUnorderedComputeProgram;

    private int vaoHandle;

    private int interfaceTexture;

    private int fboSceneHandle;
    private int colorTexSceneHandle;
    private int rboSceneHandle;
    private int depthTexSceneHandle;

    // scene vertex buffer id
    private int bufferId;
    // scene uv buffer id
    private int uvBufferId;

    private int tmpBufferId; // temporary scene vertex buffer
    private int tmpUvBufferId; // temporary scene uv buffer
    private int tmpModelBufferId; // scene model buffer, large
    private int tmpModelBufferSmallId; // scene model buffer, small
    private int tmpModelBufferUnorderedId;
    private int tmpOutBufferId; // target vertex buffer for compute shaders
    private int tmpOutUvBufferId; // target uv buffer for compute shaders

    private int textureArrayId;

    private int uniformBufferId;
    private final IntBuffer uniformBuffer = GpuIntBuffer.allocateDirect(5 + 3 + 2048 * 4);
    private final float[] textureOffsets = new float[128];

    private GpuIntBuffer vertexBuffer;
    private GpuFloatBuffer uvBuffer;

    private GpuIntBuffer modelBufferUnordered;
    private GpuIntBuffer modelBufferSmall;
    private GpuIntBuffer modelBuffer;

    private int unorderedModels;

    /**
     * number of models in small buffer
     */
    private int smallModels;

    /**
     * number of models in large buffer
     */
    private int largeModels;

    /**
     * offset in the target buffer for model
     */
    private int targetBufferOffset;

    /**
     * offset into the temporary scene vertex buffer
     */
    private int tempOffset;

    /**
     * offset into the temporary scene uv buffer
     */
    private int tempUvOffset;

    private int lastViewportWidth;
    private int lastViewportHeight;
    private int lastCanvasWidth;
    private int lastCanvasHeight;
    private int lastStretchedCanvasWidth;
    private int lastStretchedCanvasHeight;
    private AntiAliasingMode lastAntiAliasingMode;


    private Camera camera = new Camera();

    // Uniforms
    private int uniUseFog;
    private int uniFogColor;
    private int uniFogDepth;
    private int uniDrawDistance;
    private int uniProjectionMatrix;
    private int uniBrightness;
    private int uniTex;
    private int uniTexSamplingMode;
    private int uniTexSourceDimensions;
    private int uniTexTargetDimensions;
    private int uniTextures;
    private int uniTextureOffsets;
    private int uniBlockSmall;
    private int uniBlockLarge;
    private int uniBlockMain;
    private int uniSmoothBanding;

    Animator animator;
    SceneRegionBuilder sceneRegionBuilder;
    private MinimapController minimapController;

    int canvasWidth = 1200;
    int canvasHeight = (int) (canvasWidth / 1.3);

    public NewtCanvasJFX LoadMap(MainController mainController, MinimapController minimapController) {
        try {
            Store store = StoreProvider.getStore();
            SpriteManager sprites = new SpriteManager(store);
            net.runelite.cache.TextureManager textureManager = new net.runelite.cache.TextureManager(store);

            textureManager.load();

            textureProvider = new RSTextureProvider(textureManager, sprites);
            sprites.load();

            sceneRegionBuilder = new SceneRegionBuilder(textureProvider);
            scene = new Scene(sceneRegionBuilder, 12854, 1);

            // center camera in scene
            camera.setCameraX(Perspective.LOCAL_TILE_SIZE * Constants.REGION_SIZE * scene.getRadius() / 2);
            camera.setCameraY(Perspective.LOCAL_TILE_SIZE * Constants.REGION_SIZE * scene.getRadius() / 2);
            camera.setCenterX(canvasWidth / 2);
            camera.setCenterY(canvasHeight / 2);

            this.minimapController = minimapController;
            minimapController.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }

        bufferId = uvBufferId = uniformBufferId = tmpBufferId = tmpUvBufferId = tmpModelBufferId = tmpModelBufferSmallId = tmpModelBufferUnorderedId = tmpOutBufferId = tmpOutUvBufferId  = -1;
        unorderedModels = smallModels = largeModels = 0;

        vertexBuffer = new GpuIntBuffer();
        uvBuffer = new GpuFloatBuffer();

        modelBufferUnordered = new GpuIntBuffer();
        modelBufferSmall = new GpuIntBuffer();
        modelBuffer = new GpuIntBuffer();

        GLProfile.initSingleton();

        com.jogamp.newt.Display jfxNewtDisplay = NewtFactory.createDisplay(null, false);
        Screen screen = NewtFactory.createScreen(jfxNewtDisplay, 0);
        GLProfile glProfile = GLProfile.get(GLProfile.GL4);
        GLCapabilities glCaps = new GLCapabilities(glProfile);
        glCaps.setDepthBits(24);

        GLWindow window = GLWindow.create(screen, glCaps);
        window.addGLEventListener(this);
        window.addKeyListener(this);
        mouseListener = new MouseListener();
        window.addMouseListener(mouseListener);

        NewtCanvasJFX glCanvas = new NewtCanvasJFX(window);
        glCanvas.setWidth(canvasWidth);
        glCanvas.setHeight(canvasHeight);

        animator = new Animator(window);
        animator.setUpdateFPSFrames(3, null);
        animator.start();

        mouseListener.getDragListeners().add(e -> {
            int speed = 2;//(int)(2 * animator.getLastFPSPeriod() / 10);
            if (mouseListener.getPreviousMouseX() < e.getX()) {
                camera.addYaw(-speed);
            } else if (mouseListener.getPreviousMouseX() > e.getX()) {
                camera.addYaw(speed);
            }

            if (mouseListener.getPreviousMouseY() < e.getY()) {
                camera.addPitch(1);
            } else if (mouseListener.getPreviousMouseY() > e.getY()) {
                camera.addPitch(-1);
            }
            return null;
        });

        mainController.setCamera(camera);
        mainController.setMouseListener(mouseListener);
        mainController.setAnimator(animator);

        lastViewportWidth = lastViewportHeight = lastCanvasWidth = lastCanvasHeight = -1;
        lastStretchedCanvasWidth = lastStretchedCanvasHeight = -1;
        lastAntiAliasingMode = null;

        textureArrayId = -1;

        return glCanvas;
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        try {
            gl = drawable.getGL().getGL4();

            gl.glEnable(gl.GL_DEPTH_TEST);
            gl.glDepthFunc(gl.GL_LEQUAL);
            gl.glDepthRangef(1, 0);
//            gl.glGetIntegerv(gl.GL_DEPTH_BITS, intBuf1);
//            System.out.printf("depth bits %s \n", intBuf1.get(0));

            initVao();
            initProgram();
            initInterfaceTexture();
            initUniformBuffer();
            initBuffers();

            uploadScene();
            drawTiles();
        } catch (ShaderException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {

    }

    void drawTiles() {
        targetBufferOffset = 0;
        smallModels = largeModels = unorderedModels = 0;
        for (int x = 0; x < scene.getRadius() * Constants.REGION_SIZE; x++) {
            for (int y = 0; y < scene.getRadius() * Constants.REGION_SIZE; y++) {
                SceneTile tile = scene.getTile(0, x, y);
                if (tile != null) {
                    this.drawTile(tile);
                }
            }
        }

        if (hoverTile != null) {
//            drawSceneTemporary(hoverTile.getTilePaint(), hoverTile.getX(), hoverTile.getY());
        }
    }

    void checkHover() {
        for (int x = 0; x < scene.getRadius() * Constants.REGION_SIZE; x++) {
            for (int y = 0; y < scene.getRadius() * Constants.REGION_SIZE; y++) {
                SceneTile tile = scene.getTile(0, x, y);
                if (tile != null) {
                    if (tile.getTilePaint() != null) {
                        int pitchSin = camera.getPitchSin();
                        int pitchCos = camera.getPitchCos();
                        int yawSin = camera.getYawSin();
                        int yawCos = camera.getYawCos();

                        int var9;
                        int var10 = var9 = (x << 7) - camera.getCameraX();
                        int var11;
                        int var12 = var11 = (y << 7) - camera.getCameraY();
                        int var13;
                        int var14 = var13 = var10 + Perspective.LOCAL_TILE_SIZE;
                        int var15;
                        int var16 = var15 = var12 + Perspective.LOCAL_TILE_SIZE;
                        int var17 = tile.getTilePaint().getSwHeight() - camera.getCameraZ();
                        int var18 = tile.getTilePaint().getSeHeight() - camera.getCameraZ();
                        int var19 = tile.getTilePaint().getNeHeight() - camera.getCameraZ();
                        int var20 = tile.getTilePaint().getNwHeight() - camera.getCameraZ();
                        int var21 = var10 * yawCos + yawSin * var12 >> 16;
                        var12 = var12 * yawCos - yawSin * var10 >> 16;
                        var10 = var21;
                        var21 = var17 * pitchCos - pitchSin * var12 >> 16;
                        var12 = pitchSin * var17 + var12 * pitchCos >> 16;
                        var17 = var21;
                        if (var12 >= 50) {
                            var21 = var14 * yawCos + yawSin * var11 >> 16;
                            var11 = var11 * yawCos - yawSin * var14 >> 16;
                            var14 = var21;
                            var21 = var18 * pitchCos - pitchSin * var11 >> 16;
                            var11 = pitchSin * var18 + var11 * pitchCos >> 16;
                            var18 = var21;
                            if (var11 >= 50) {
                                var21 = var13 * yawCos + yawSin * var16 >> 16;
                                var16 = var16 * yawCos - yawSin * var13 >> 16;
                                var13 = var21;
                                var21 = var19 * pitchCos - pitchSin * var16 >> 16;
                                var16 = pitchSin * var19 + var16 * pitchCos >> 16;
                                var19 = var21;
                                if (var16 >= 50) {
                                    var21 = var9 * yawCos + yawSin * var15 >> 16;
                                    var15 = var15 * yawCos - yawSin * var9 >> 16;
                                    var9 = var21;
                                    var21 = var20 * pitchCos - pitchSin * var15 >> 16;
                                    var15 = pitchSin * var20 + var15 * pitchCos >> 16;
                                    if (var15 >= 50) {

                                        int dy = var10 * camera.getScale() / var12 + camera.getCenterX();
                                        int dx = var17 * camera.getScale() / var12 + camera.getCenterY();
                                        int cy = var14 * camera.getScale() / var11 + camera.getCenterX();
                                        int cx = var18 * camera.getScale() / var11 + camera.getCenterY();
                                        int ay = var13 * camera.getScale() / var16 + camera.getCenterX();
                                        int ax = var19 * camera.getScale() / var16 + camera.getCenterY();
                                        int by = var9 * camera.getScale() / var15 + camera.getCenterX();
                                        int bx = var21 * camera.getScale() / var15 + camera.getCenterY();

                                        int mouseX2 = mouseListener.getMouseX();
                                        int mouseY2 = mouseListener.getMouseY();
                                        if (((ay - by) * (cx - bx) - (ax - bx) * (cy - by) > 0) && containsBounds(mouseX2, mouseY2, ax, bx, cx, ay, by, cy)) {
                                            hoverTile = tile;
                                        } else if (((dy - cy) * (bx - cx) - (dx - cx) * (by - cy) > 0) && containsBounds(mouseX2, mouseY2, dx, cx, bx, dy, cy, by)) {
                                            hoverTile = tile;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    void drawTile(SceneTile tile) {
        int x = tile.getX();
        int y = tile.getY();

//        if (tile.isNeedsUpdate()) {
//            updateTile(tile);
        if (tile.getTilePaint() != null) {
            drawScenePaint(tile.getTilePaint(), x, y);
        } else if (tile.getTileModel() != null) {
            drawSceneModel(tile.getTileModel(), x, y);
        }

        FloorDecoration f = tile.getFloorDecoration();
        if (f != null) {
            drawSceneRenderable(f.getModel(), 0, f.getHeight(), x, y);
        }

        WallDecoration w = tile.getWallDecoration();
        if (w != null) {
            drawSceneRenderable(w.getModelA(), w.getOrientationA(), w.getHeight(), x, y);
        }
//            tile.setNeedsUpdate(false);
//        }
    }

    SceneTile hoverTile;

    void updateTile(SceneTile tile) {
        // FIXME: use buffersubdata to only upload the part of the intbuffer that needs to change
        // uploading the entire buffer causes huge lag
//        GpuIntBuffer buffer = new GpuIntBuffer(6 * Integer.BYTES);
//        sceneUploader.modifyTilePaint(tile.getTilePaint(), vertexBuffer, uvBuffer);
//        IntBuffer vertexBuffer = this.vertexBuffer.getBuffer();
//
//        gl.glBindBuffer(gl.GL_ARRAY_BUFFER, bufferId);
//
////        gl.glBufferData(gl.GL_ARRAY_BUFFER, vertexBuffer.limit() * Integer.BYTES, null, gl.GL_DYNAMIC_DRAW);
//        gl.glBufferSubData(gl.GL_ARRAY_BUFFER, tile.getTilePaint().getBufferOffset() * Integer.BYTES, tile.getTilePaint().getBufferLen() * Integer.BYTES, vertexBuffer);
//
//        gl.glBindBuffer(gl.GL_ARRAY_BUFFER, 0);


//        sceneUploader.upload(tile, vertexBuffer, uvBuffer);
//        IntBuffer vertexBuffer = this.vertexBuffer.getBuffer();
//
//        gl.glBindBuffer(gl.GL_ARRAY_BUFFER, bufferId);
//        gl.glBufferData(gl.GL_ARRAY_BUFFER, vertexBuffer.limit() * Integer.BYTES, vertexBuffer, gl.GL_STATIC_COPY);
////
//        gl.glBufferSubData(gl.GL_ARRAY_BUFFER, 0, vertexBuffer.limit() * Integer.BYTES, vertexBuffer);
//        gl.glBindBuffer(gl.GL_ARRAY_BUFFER, 0);
    }


    boolean containsBounds(int i_0, int i_1, int i_2, int i_3, int i_4, int i_5, int i_6, int i_7) {
        if (i_1 < i_2 && i_1 < i_3 && i_1 < i_4) {
            return false;
        } else if (i_1 > i_2 && i_1 > i_3 && i_1 > i_4) {
            return false;
        } else if (i_0 < i_5 && i_0 < i_6 && i_0 < i_7) {
            return false;
        } else if (i_0 > i_5 && i_0 > i_6 && i_0 > i_7) {
            return false;
        } else {
            int i_8 = (i_1 - i_2) * (i_6 - i_5) - (i_0 - i_5) * (i_3 - i_2);
            int i_9 = (i_7 - i_6) * (i_1 - i_3) - (i_0 - i_6) * (i_4 - i_3);
            int i_10 = (i_5 - i_7) * (i_1 - i_4) - (i_2 - i_4) * (i_0 - i_7);
            return i_8 == 0 ? (i_9 != 0 ? (i_9 < 0 ? i_10 <= 0 : i_10 >= 0) : true) : (i_8 < 0 ? i_9 <= 0 && i_10 <= 0 : i_9 >= 0 && i_10 >= 0);
        }
    }

    private void drawSceneRenderable(Model model, int orientation, int height, int tileX, int tileY) {
        if (model != null && model.getSceneId() == sceneUploader.sceneId) {
//            model.calculateBoundsCylinder();
//            model.calculateExtreme(orientation);

            // draw in the middle of the tile otherwise camera clipping
            int x = tileX * Perspective.LOCAL_TILE_SIZE + Perspective.LOCAL_HALF_TILE_SIZE;
            int z = tileY * Perspective.LOCAL_TILE_SIZE + Perspective.LOCAL_HALF_TILE_SIZE;

            int tc = Math.min(MAX_TRIANGLE, model.getTrianglesCount());
            int uvOffset = model.getUvBufferOffset();

            GpuIntBuffer b = bufferForTriangles(tc);

            b.ensureCapacity(8);
            IntBuffer buffer = b.getBuffer();
            buffer.put(model.getBufferOffset());
            buffer.put(uvOffset);
            buffer.put(tc);
            buffer.put(targetBufferOffset);
            buffer.put(FLAG_SCENE_BUFFER | (model.getRadius() << 12) | orientation);
            buffer.put(x).put(height).put(z);

            targetBufferOffset += tc * 3;
        }
//        if (renderable instanceof Model && ((Model) renderable).getSceneId() == sceneUploader.sceneId)
//        {

//        }
    }

    private void drawScenePaint(TilePaint paint, int tileX, int tileY) {
        if (paint.getBufferLen() > 0) {
            int x = tileX * Perspective.LOCAL_TILE_SIZE;
            int y = 0;
            int z = tileY * Perspective.LOCAL_TILE_SIZE;

            GpuIntBuffer b = modelBufferUnordered;
            ++unorderedModels;

            b.ensureCapacity(8);
            IntBuffer buffer = b.getBuffer();
            buffer.put(paint.getBufferOffset());
            buffer.put(paint.getUvBufferOffset());
            buffer.put(2);
            buffer.put(targetBufferOffset);
            buffer.put(FLAG_SCENE_BUFFER);
            buffer.put(x).put(y).put(z);

            targetBufferOffset += 2 * 3;
        }
    }

    public void drawSceneModel(TileModel model, int tileX, int tileY) {
        if (model.getBufferLen() > 0) {
            int x = tileX * Perspective.LOCAL_TILE_SIZE;
            int y = 0;
            int z = tileY * Perspective.LOCAL_TILE_SIZE;

            GpuIntBuffer b = modelBufferUnordered;
            ++unorderedModels;

            b.ensureCapacity(8);
            IntBuffer buffer = b.getBuffer();
            buffer.put(model.getBufferOffset());
            buffer.put(model.getUvBufferOffset());
            buffer.put(model.getBufferLen() / 3);
            buffer.put(targetBufferOffset);
            buffer.put(FLAG_SCENE_BUFFER);
            buffer.put(x).put(y).put(z);

            targetBufferOffset += model.getBufferLen();
        }
    }

    void drawSceneTemporary(TilePaint tile, int tileX, int tileY) {
        int x = tileX * Perspective.LOCAL_TILE_SIZE;
        int y = 0;
        int z = tileY * Perspective.LOCAL_TILE_SIZE;
        int len = sceneUploader.upload(tile, vertexBuffer, uvBuffer);

        GpuIntBuffer b = bufferForTriangles(2);

        b.ensureCapacity(8);
        IntBuffer buffer = b.getBuffer();
        buffer.put(tempOffset);
        buffer.put(-1);
        buffer.put(len / 3);
        buffer.put(targetBufferOffset);
        buffer.put(0);
        buffer.put(x).put(y).put(z);

        tempOffset += len;
        targetBufferOffset += len;
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        if (drawable.getAnimator() != null) {
            handleKeys(drawable.getAnimator().getLastFPSPeriod());
            checkHover();
            handleClick();
        }

        if (canvasWidth > 0 && canvasHeight > 0 && (canvasWidth != lastViewportWidth || canvasHeight != lastViewportHeight)) {
            createProjectionMatrix(0, canvasWidth, canvasHeight, 0, 1, MAX_DISTANCE * Perspective.LOCAL_TILE_SIZE);
            lastViewportWidth = canvasWidth;
            lastViewportHeight = canvasHeight;
        }

        // Setup anti-aliasing
        final AntiAliasingMode antiAliasingMode = AntiAliasingMode.MSAA_4;
        final boolean aaEnabled = antiAliasingMode != AntiAliasingMode.DISABLED;

        if (aaEnabled) {
            gl.glEnable(gl.GL_MULTISAMPLE);

            final int stretchedCanvasWidth = canvasWidth;
            final int stretchedCanvasHeight = canvasHeight;

            // Re-create fbo
            if (lastStretchedCanvasWidth != stretchedCanvasWidth
                    || lastStretchedCanvasHeight != stretchedCanvasHeight
                    || lastAntiAliasingMode != antiAliasingMode) {
                final int maxSamples = GLUtil.glGetInteger(gl, gl.GL_MAX_SAMPLES);
                final int samples = Math.min(antiAliasingMode.getSamples(), maxSamples);

                initAAFbo(stretchedCanvasWidth, stretchedCanvasHeight, samples);

                lastStretchedCanvasWidth = stretchedCanvasWidth;
                lastStretchedCanvasHeight = stretchedCanvasHeight;
            }

            gl.glBindFramebuffer(gl.GL_DRAW_FRAMEBUFFER, fboSceneHandle);
        }

        lastAntiAliasingMode = antiAliasingMode;

        // Clear scene
        int sky = 9493480;
        gl.glClearColor((sky >> 16 & 0xFF) / 255f, (sky >> 8 & 0xFF) / 255f, (sky & 0xFF) / 255f, 1f);
        gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT);

        // Upload buffers
        vertexBuffer.flip();
        uvBuffer.flip();
        modelBuffer.flip();
        modelBufferSmall.flip();
        modelBufferUnordered.flip();

        IntBuffer vertexBuffer = this.vertexBuffer.getBuffer();
        FloatBuffer uvBuffer = this.uvBuffer.getBuffer();
        IntBuffer modelBuffer = this.modelBuffer.getBuffer();
        IntBuffer modelBufferSmall = this.modelBufferSmall.getBuffer();
        IntBuffer modelBufferUnordered = this.modelBufferUnordered.getBuffer();

        gl.glBindBuffer(gl.GL_ARRAY_BUFFER, tmpBufferId);
        gl.glBufferData(gl.GL_ARRAY_BUFFER, vertexBuffer.limit() * Integer.BYTES, vertexBuffer, gl.GL_DYNAMIC_DRAW);

        gl.glBindBuffer(gl.GL_ARRAY_BUFFER, tmpUvBufferId);
        gl.glBufferData(gl.GL_ARRAY_BUFFER, uvBuffer.limit() * Float.BYTES, uvBuffer, gl.GL_DYNAMIC_DRAW);

        gl.glBindBuffer(gl.GL_ARRAY_BUFFER, tmpModelBufferId);
        gl.glBufferData(gl.GL_ARRAY_BUFFER, modelBuffer.limit() * Integer.BYTES, modelBuffer, gl.GL_DYNAMIC_DRAW);

        gl.glBindBuffer(gl.GL_ARRAY_BUFFER, tmpModelBufferSmallId);
        gl.glBufferData(gl.GL_ARRAY_BUFFER, modelBufferSmall.limit() * Integer.BYTES, modelBufferSmall, gl.GL_DYNAMIC_DRAW);

        gl.glBindBuffer(gl.GL_ARRAY_BUFFER, tmpModelBufferUnorderedId);
        gl.glBufferData(gl.GL_ARRAY_BUFFER, modelBufferUnordered.limit() * Integer.BYTES, modelBufferUnordered, gl.GL_DYNAMIC_DRAW);

        gl.glBindBuffer(gl.GL_ARRAY_BUFFER, tmpOutBufferId);
        gl.glBufferData(gl.GL_ARRAY_BUFFER,
                targetBufferOffset * 16, // each vertex is an ivec4, which is 16 bytes
                null,
                gl.GL_STREAM_DRAW);

        gl.glBindBuffer(gl.GL_ARRAY_BUFFER, tmpOutUvBufferId);
        gl.glBufferData(gl.GL_ARRAY_BUFFER,
                targetBufferOffset * 16,
                null,
                gl.GL_STREAM_DRAW);

        // UBO
        gl.glBindBuffer(gl.GL_UNIFORM_BUFFER, uniformBufferId);
        uniformBuffer.clear();
        uniformBuffer
                .put(camera.getYaw())
                .put(camera.getPitch())
                .put(camera.getCenterX())
                .put(camera.getCenterY())
                .put(camera.getScale())
                .put(camera.getCameraX()) //x
                .put(camera.getCameraZ()) // z
                .put(camera.getCameraY()); // y
        uniformBuffer.flip();

        gl.glBufferSubData(gl.GL_UNIFORM_BUFFER, 0, uniformBuffer.limit() * Integer.BYTES, uniformBuffer);
        gl.glBindBuffer(gl.GL_UNIFORM_BUFFER, 0);

        // Draw 3d scene
        if (textureProvider != null && this.bufferId != -1) {
            gl.glUniformBlockBinding(glSmallComputeProgram, uniBlockSmall, 0);
            gl.glUniformBlockBinding(glComputeProgram, uniBlockLarge, 0);

            gl.glBindBufferBase(gl.GL_UNIFORM_BUFFER, 0, uniformBufferId);

            /*
             * Compute is split into two separate programs 'small' and 'large' to
             * save on GPU resources. Small will sort <= 512 faces, large will do <= 4096.
             */

            // unordered
            gl.glUseProgram(glUnorderedComputeProgram);

            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 0, tmpModelBufferUnorderedId);
            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 1, this.bufferId);
            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 2, tmpBufferId);
            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 3, tmpOutBufferId);
            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 4, tmpOutUvBufferId);
            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 5, this.uvBufferId);
            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 6, tmpUvBufferId);

            gl.glDispatchCompute(unorderedModels, 1, 1);

            // small
            gl.glUseProgram(glSmallComputeProgram);

            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 0, tmpModelBufferSmallId);
            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 1, this.bufferId);
            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 2, tmpBufferId);
            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 3, tmpOutBufferId); // vout[]
            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 4, tmpOutUvBufferId); //uvout[]
            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 5, this.uvBufferId);
            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 6, tmpUvBufferId);

            gl.glDispatchCompute(smallModels, 1, 1);

            // large
            gl.glUseProgram(glComputeProgram);

            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 0, tmpModelBufferId);
            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 1, this.bufferId);
            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 2, tmpBufferId);
            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 3, tmpOutBufferId);
            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 4, tmpOutUvBufferId);
            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 5, this.uvBufferId);
            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 6, tmpUvBufferId);

            gl.glDispatchCompute(largeModels, 1, 1);

            gl.glMemoryBarrier(gl.GL_SHADER_STORAGE_BARRIER_BIT);

            if (textureArrayId == -1) {
                // lazy init textures as they may not be loaded at plugin start.
                // this will return -1 and retry if not all textures are loaded yet, too.
                textureArrayId = textureManager.initTextureArray(textureProvider, gl);
            }

            gl.glUseProgram(glProgram);

            final int fogDepth = 0;
            gl.glUniform1i(uniUseFog, fogDepth > 0 ? 1 : 0);
            gl.glUniform4f(uniFogColor, (sky >> 16 & 0xFF) / 255f, (sky >> 8 & 0xFF) / 255f, (sky & 0xFF) / 255f, 1f);
            gl.glUniform1i(uniFogDepth, fogDepth);
            gl.glUniform1i(uniDrawDistance, MAX_DISTANCE * Perspective.LOCAL_TILE_SIZE);

            // Brightness happens to also be stored in the texture provider, so we use that
            gl.glUniform1f(uniBrightness, 0.7f);//(float) textureProvider.getBrightness());
            gl.glUniform1f(uniSmoothBanding, 1f);

//            for (int id = 0; id < textures.length; ++id) {
//                TextureDefinition texture = textures[id];
//                if (texture == null) {
//                    continue;
//                }
//
//                textureProvider.load(id); // trips the texture load flag which lets textures animate
//
//                textureOffsets[id * 2] = texture.getU();
//                textureOffsets[id * 2 + 1] = texture.getV();
//            }

            // Bind uniforms
            gl.glUniformBlockBinding(glProgram, uniBlockMain, 0);
            gl.glUniform1i(uniTextures, 1); // texture sampler array is bound to texture1
            gl.glUniform2fv(uniTextureOffsets, 128, textureOffsets, 0);

            // We just allow the GL to do face culling. Note this requires the priority renderer
            // to have logic to disregard culled faces in the priority depth testing.
            gl.glEnable(gl.GL_CULL_FACE);
            gl.glCullFace(GL.GL_BACK);

            // Enable blending for alpha
            gl.glEnable(gl.GL_BLEND);
            gl.glBlendFunc(gl.GL_SRC_ALPHA, gl.GL_ONE_MINUS_SRC_ALPHA);

            // Draw output of compute shaders
            gl.glBindVertexArray(vaoHandle);

            gl.glEnableVertexAttribArray(0);
            gl.glBindBuffer(gl.GL_ARRAY_BUFFER, tmpOutBufferId);
            gl.glVertexAttribIPointer(0, 4, gl.GL_INT, 0, 0);

            gl.glEnableVertexAttribArray(1);
            gl.glBindBuffer(gl.GL_ARRAY_BUFFER, tmpOutUvBufferId);
            gl.glVertexAttribPointer(1, 4, gl.GL_FLOAT, false, 0, 0);

            gl.glDrawArrays(gl.GL_TRIANGLES, 0, targetBufferOffset);

            gl.glDisable(gl.GL_BLEND);
            gl.glDisable(gl.GL_CULL_FACE);

            gl.glUseProgram(0);
        }

        if (aaEnabled) {
            gl.glBindFramebuffer(gl.GL_READ_FRAMEBUFFER, fboSceneHandle);
            gl.glBindFramebuffer(gl.GL_DRAW_FRAMEBUFFER, 0);
            gl.glBlitFramebuffer(0, 0, lastStretchedCanvasWidth, lastStretchedCanvasHeight,
                    0, 0, lastStretchedCanvasWidth, lastStretchedCanvasHeight,
                    gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT, gl.GL_NEAREST);

            // Reset
            gl.glBindFramebuffer(gl.GL_READ_FRAMEBUFFER, 0);
        }

        vertexBuffer.clear();
        uvBuffer.clear();
        modelBuffer.clear();
        modelBufferSmall.clear();
        modelBufferUnordered.clear();

//        targetBufferOffset = 0;
//        smallModels = largeModels = unorderedModels = 0;
        tempOffset = 0;
        tempUvOffset = 0;

//        minimapController.drawCanvas(scene);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        drawable.getGL().getGL4().glViewport(x, y, width, height);
    }

    private void uploadScene() {
        vertexBuffer.clear();
        uvBuffer.clear();

        sceneUploader.upload(scene, vertexBuffer, uvBuffer);

        vertexBuffer.flip();
        uvBuffer.flip();

        IntBuffer vertexBuffer = this.vertexBuffer.getBuffer();
        FloatBuffer uvBuffer = this.uvBuffer.getBuffer();

        gl.glBindBuffer(gl.GL_ARRAY_BUFFER, bufferId);
        gl.glBufferData(gl.GL_ARRAY_BUFFER, vertexBuffer.limit() * Integer.BYTES, vertexBuffer, gl.GL_DYNAMIC_DRAW);

        gl.glBindBuffer(gl.GL_ARRAY_BUFFER, uvBufferId);
        gl.glBufferData(gl.GL_ARRAY_BUFFER, uvBuffer.limit() * Float.BYTES, uvBuffer, gl.GL_STATIC_COPY);

        gl.glBindBuffer(gl.GL_ARRAY_BUFFER, 0);

        vertexBuffer.clear();
        uvBuffer.clear();
    }

    private void initVao() {
        // Create VAO
        vaoHandle = GLUtil.glGenVertexArrays(gl);
        // unbind VBO
        gl.glBindBuffer(gl.GL_ARRAY_BUFFER, 0);
    }

    private void initProgram() throws ShaderException {
        Template template = new Template();
        template.add(key ->
        {
            if ("version_header".equals(key)) {
                return Shader.WINDOWS_VERSION_HEADER;
            }
            return null;
        });
        template.addInclude(Shader.class);

        glProgram = Shader.PROGRAM.compile(gl, template);
        glComputeProgram = Shader.COMPUTE_PROGRAM.compile(gl, template);
        glSmallComputeProgram = Shader.SMALL_COMPUTE_PROGRAM.compile(gl, template);
        glUnorderedComputeProgram = Shader.UNORDERED_COMPUTE_PROGRAM.compile(gl, template);

        initUniforms();
    }

    private void initUniforms() {
        uniProjectionMatrix = gl.glGetUniformLocation(glProgram, "projectionMatrix");
        uniBrightness = gl.glGetUniformLocation(glProgram, "brightness");
        uniSmoothBanding = gl.glGetUniformLocation(glProgram, "smoothBanding");
        uniUseFog = gl.glGetUniformLocation(glProgram, "useFog");
        uniFogColor = gl.glGetUniformLocation(glProgram, "fogColor");
        uniFogDepth = gl.glGetUniformLocation(glProgram, "fogDepth");
        uniDrawDistance = gl.glGetUniformLocation(glProgram, "drawDistance");

        uniTextures = gl.glGetUniformLocation(glProgram, "textures");
        uniTextureOffsets = gl.glGetUniformLocation(glProgram, "textureOffsets");

        uniBlockSmall = gl.glGetUniformBlockIndex(glSmallComputeProgram, "uniforms");
        uniBlockLarge = gl.glGetUniformBlockIndex(glComputeProgram, "uniforms");
        uniBlockMain = gl.glGetUniformBlockIndex(glProgram, "uniforms");
    }

    private void initInterfaceTexture() {
        interfaceTexture = GLUtil.glGenTexture(gl);
        gl.glBindTexture(gl.GL_TEXTURE_2D, interfaceTexture);
        gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_S, gl.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_T, gl.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MIN_FILTER, gl.GL_LINEAR);
        gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MAG_FILTER, gl.GL_LINEAR);
        gl.glBindTexture(gl.GL_TEXTURE_2D, 0);
    }

    private void initUniformBuffer() {
        uniformBufferId = GLUtil.glGenBuffers(gl);
        gl.glBindBuffer(gl.GL_UNIFORM_BUFFER, uniformBufferId);
        uniformBuffer.clear();
        uniformBuffer.put(new int[8]);
        final int[] pad = new int[2];
        for (int i = 0; i < 2048; i++) {
            uniformBuffer.put(Perspective.SINE[i]);
            uniformBuffer.put(Perspective.COSINE[i]);
            uniformBuffer.put(pad);
        }
        uniformBuffer.flip();

        gl.glBufferData(gl.GL_UNIFORM_BUFFER, uniformBuffer.limit() * Integer.BYTES, uniformBuffer, gl.GL_DYNAMIC_DRAW);
        gl.glBindBuffer(gl.GL_UNIFORM_BUFFER, 0);
    }

    private void initBuffers() {
        bufferId = GLUtil.glGenBuffers(gl);
        uvBufferId = GLUtil.glGenBuffers(gl);
        tmpBufferId = GLUtil.glGenBuffers(gl);
        tmpUvBufferId = GLUtil.glGenBuffers(gl);
        tmpModelBufferId = GLUtil.glGenBuffers(gl);
        tmpModelBufferSmallId = GLUtil.glGenBuffers(gl);
        tmpModelBufferUnorderedId = GLUtil.glGenBuffers(gl);
        tmpOutBufferId = GLUtil.glGenBuffers(gl);
        tmpOutUvBufferId = GLUtil.glGenBuffers(gl);
    }

    private void createProjectionMatrix(float left, float right, float bottom, float top, float near, float far) {
        // create a standard orthographic projection
        float tx = -((right + left) / (right - left));
        float ty = -((top + bottom) / (top - bottom));
        float tz = ((far + near) / (far - near));

        gl.glUseProgram(glProgram);

        float[] matrix = new float[]{
                2 / (right - left), 0, 0, 0,
                0, 2 / (top - bottom), 0, 0,
                0, 0, -2 / (far - near), 0,
                tx, ty, tz, 1
        };
        gl.glUniformMatrix4fv(uniProjectionMatrix, 1, false, matrix, 0);

        gl.glUseProgram(0);
    }

    private void initAAFbo(int width, int height, int aaSamples) {
        // Create and bind the FBO
        fboSceneHandle = GLUtil.glGenFrameBuffer(gl);
        gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, fboSceneHandle);

        // Create color render buffer
        rboSceneHandle = GLUtil.glGenRenderbuffer(gl);
        gl.glBindRenderbuffer(gl.GL_RENDERBUFFER, rboSceneHandle);
        gl.glRenderbufferStorageMultisample(gl.GL_RENDERBUFFER, aaSamples, gl.GL_RGBA, width, height);
        gl.glFramebufferRenderbuffer(gl.GL_FRAMEBUFFER, gl.GL_COLOR_ATTACHMENT0, gl.GL_RENDERBUFFER, rboSceneHandle);

        // Create color texture
        colorTexSceneHandle = GLUtil.glGenTexture(gl);
        gl.glBindTexture(gl.GL_TEXTURE_2D_MULTISAMPLE, colorTexSceneHandle);
        gl.glTexImage2DMultisample(gl.GL_TEXTURE_2D_MULTISAMPLE, aaSamples, gl.GL_RGBA, width, height, true);

        // Create depth texture
        depthTexSceneHandle = GLUtil.glGenTexture(gl);
        gl.glBindTexture(gl.GL_TEXTURE_2D_MULTISAMPLE, depthTexSceneHandle);
        gl.glTexImage2DMultisample(gl.GL_TEXTURE_2D_MULTISAMPLE, aaSamples, gl.GL_DEPTH_COMPONENT, width, height, true);

        // Bind color tex
        gl.glFramebufferTexture2D(gl.GL_FRAMEBUFFER, gl.GL_COLOR_ATTACHMENT0, gl.GL_TEXTURE_2D_MULTISAMPLE, colorTexSceneHandle, 0);

        // bind depth tex
        gl.glFramebufferTexture2D(gl.GL_FRAMEBUFFER, gl.GL_DEPTH_ATTACHMENT, gl.GL_TEXTURE_2D_MULTISAMPLE, depthTexSceneHandle, 0);

        // Reset
        gl.glBindTexture(gl.GL_TEXTURE_2D_MULTISAMPLE, 0);
        gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, 0);
        gl.glBindRenderbuffer(gl.GL_RENDERBUFFER, 0);
    }

    private GpuIntBuffer bufferForTriangles(int triangles) {
        if (triangles < SMALL_TRIANGLE_COUNT) {
            ++smallModels;
            return modelBufferSmall;
        } else {
            ++largeModels;
            return modelBuffer;
        }
    }

    void handleKeys(double dt) {
        double xVec = -(double) camera.getYawSin() / 65535;
        double yVec = (double) camera.getYawCos() / 65535;
        double zVec = (double) camera.getPitchSin() / 65535;

        int speed = 1;
        if (keys[KeyEvent.VK_SHIFT]) {
            speed = 4;
        }

        if (keys[KeyEvent.VK_W]) {
            camera.addX((int) (dt * xVec * speed));
            camera.addY((int) (dt * yVec * speed));
            camera.addZ((int) (dt * zVec * speed));
        }

        if (keys[KeyEvent.VK_S]) {
            camera.addX(-(int) (dt * xVec * speed));
            camera.addY(-(int) (dt * yVec * speed));
            camera.addZ(-(int) (dt * zVec * speed));
        }

        if (keys[KeyEvent.VK_A]) {
            // X uses yVec because we want to move perpendicular
            camera.addX(-(int) (dt * yVec * speed));
            camera.addY((int) (dt * xVec * speed));
        }

        if (keys[KeyEvent.VK_D]) {
            camera.addX((int) (dt * yVec * speed));
            camera.addY(-(int) (dt * xVec * speed));
        }

        if (keys[KeyEvent.VK_SPACE]) {
            camera.addZ(-(int) dt * speed);
        }

        if (keys[KeyEvent.VK_X]) {
            camera.addZ((int) dt * speed);
        }

        if (keys[KeyEvent.VK_K]) {
            scene = new Scene(sceneRegionBuilder, 10039, 6);
            uploadScene();
            drawTiles();
        }

        if (keys[KeyEvent.VK_R]) {
            uploadScene();
        }
    }

    void handleClick() {
        if (hoverTile == null) {
            return;
        }
        if (mouseListener.isMouseClicked()) {
            int x = hoverTile.getX();
            int y = hoverTile.getY();
            SceneTile north = scene.getTile(0, x, y + 1);
            SceneTile east = scene.getTile(0, x + 1, y);
            SceneTile northEast = scene.getTile(0, x + 1, y + 1);

            SceneTile tile = scene.getTile(0, x, y);
            int newHeight = tile.getTilePaint().getNeHeight() + 10;

            if (north != null && north.getTilePaint() != null) {
                TilePaintImpl tp = north.getTilePaint();
                tp.setSeHeight(newHeight);
                north.setHeight(newHeight);
                north.setNeedsUpdate(true);
            }
            if (east != null && east.getTilePaint() != null) {
                TilePaintImpl tp = east.getTilePaint();
                tp.setNwHeight(newHeight);
                east.setHeight(newHeight);
                east.setNeedsUpdate(true);
            }

            if (northEast != null && northEast.getTilePaint() != null) {
                TilePaintImpl tp = northEast.getTilePaint();
                tp.setSwHeight(newHeight);
                northEast.setHeight(newHeight);
                northEast.setNeedsUpdate(true);
            }

            tile.setHeight(newHeight);
            tile.getTilePaint().setNeHeight(newHeight);
            tile.getTilePaint().setNeColor(scene.calcTileColor(0, x, y));
            tile.setNeedsUpdate(true);

            System.out.printf("clicked tile %d %d\n", x, y);
            mouseListener.setMouseClicked(false);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        keys[e.getKeyCode()] = true;
    }

    boolean[] keys = new boolean[250];

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.isAutoRepeat()) {
            return;
        }

        keys[e.getKeyCode()] = false;
    }
}
