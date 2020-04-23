package renderer;

import com.jogamp.common.nio.Buffers;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.Screen;
import com.jogamp.newt.javafx.NewtCanvasJFX;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.GLBuffers;
import layoutControllers.MainController;
import layoutControllers.MinimapController;
import lombok.extern.slf4j.Slf4j;
import models.*;
import models.DynamicObject;
import models.Renderable;
import net.runelite.api.*;
import net.runelite.cache.SpriteManager;
import net.runelite.cache.fs.Store;
import net.runelite.cache.fs.StoreProvider;
import net.runelite.cache.item.RSTextureProvider;
import org.joml.Matrix4f;
import renderer.helpers.*;
import scene.Scene;
import scene.SceneRegionBuilder;
import scene.SceneTile;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;

import static renderer.helpers.GLUtil.*;

@Slf4j
public class MapEditor implements GLEventListener {

    // This is the maximum number of triangles the compute shaders support
    private static final int MAX_TEMP_VERTICES = 65535;
    static final int MAX_DISTANCE = 1000;

    private InputHandler inputHandler;
    private final SceneUploader sceneUploader = new SceneUploader();
    private final TextureManager textureManager = new TextureManager();
    private RSTextureProvider textureProvider;
    private Scene scene;

    private GL4 gl;

    private int glProgram;
    private int glComputeProgram;
    private int glSmallComputeProgram;
    private int glUnorderedComputeProgram;

    private int fboMainRenderer;
    private int rboDepthMain;
    private int texColorMain;
    private int texPickerMain;
    private int[] pboIds = new int[2];
    private int pboIndex;

    private int vaoHandle;

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
    private int colorPickerBufferId; // target uv buffer for compute shaders

    private int textureArrayId;

    private int uniformBufferId;
    private final IntBuffer uniformBuffer = GpuIntBuffer.allocateDirect(5 + 3);
    private final float[] textureOffsets = new float[128];

    private ModelBuffers modelBuffers;

    private int lastViewportWidth;
    private int lastViewportHeight;
    private int lastCanvasWidth;
    private int lastCanvasHeight;
    private int lastStretchedCanvasWidth;
    private int lastStretchedCanvasHeight;
    private AntiAliasingMode lastAntiAliasingMode;

    private final Camera camera = new Camera();

    // Uniforms
    private int uniUseFog;
    private int uniFogColor;
    private int uniFogDepth;
    private int uniDrawDistance;
    private int uniProjectionMatrix;
    private int uniBrightness;
    private int uniTextures;
    private int uniTextureOffsets;
    private int uniBlockSmall;
    private int uniBlockLarge;
    private int uniBlockMain;
    private int uniSmoothBanding;
    private int uniHoverId;
    private int uniSelectedIds;

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
            scene = new Scene(sceneRegionBuilder, 12085, 1);

            // center camera in viewport
            camera.setCenterX(canvasWidth / 2);
            camera.setCenterY(canvasHeight / 2);

            this.minimapController = minimapController;
            minimapController.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }

        bufferId = uvBufferId = uniformBufferId = tmpBufferId = tmpUvBufferId = tmpModelBufferId = tmpModelBufferSmallId = tmpModelBufferUnorderedId = tmpOutBufferId = tmpOutUvBufferId = -1;
        colorPickerBufferId = -1;

        modelBuffers = new ModelBuffers();

        GLProfile.initSingleton();

        com.jogamp.newt.Display jfxNewtDisplay = NewtFactory.createDisplay(null, false);
        Screen screen = NewtFactory.createScreen(jfxNewtDisplay, 0);
        GLProfile glProfile = GLProfile.get(GLProfile.GL4);
        GLCapabilities glCaps = new GLCapabilities(glProfile);
        glCaps.setAlphaBits(8);

        GLWindow window = GLWindow.create(screen, glCaps);
        window.addGLEventListener(this);

        inputHandler = new InputHandler(camera, this);
        window.addKeyListener(inputHandler);
        window.addMouseListener(inputHandler);

        NewtCanvasJFX glCanvas = new NewtCanvasJFX(window);
        glCanvas.setWidth(canvasWidth);
        glCanvas.setHeight(canvasHeight);

        animator = new Animator(window);
        animator.setUpdateFPSFrames(3, null);
        animator.start();

        mainController.setCamera(camera);
        mainController.setInputHandler(inputHandler);
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
            gl.glDepthRangef(0, 1);
//            gl.glGetIntegerv(gl.GL_DEPTH_BITS, intBuf1);
//            System.out.printf("depth bits %s \n", intBuf1.get(0));

            initVao();
            initProgram();
            initUniformBuffer();
            initBuffers();
            initPickerBuffer();

            // disable vsync
//            gl.setSwapInterval(0);
        } catch (ShaderException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        shutdownBuffers();
        shutdownProgram();
        shutdownVao();
        shutdownAAFbo();
    }

    private void shutdownProgram() {
        gl.glDeleteProgram(glProgram);
        glProgram = -1;

        gl.glDeleteProgram(glComputeProgram);
        glComputeProgram = -1;

        gl.glDeleteProgram(glSmallComputeProgram);
        glSmallComputeProgram = -1;

        gl.glDeleteProgram(glUnorderedComputeProgram);
        glUnorderedComputeProgram = -1;
    }

    private void shutdownVao() {
        glDeleteVertexArrays(gl, vaoHandle);
        vaoHandle = -1;
    }

    private void shutdownAAFbo() {
        if (colorTexSceneHandle != -1) {
            glDeleteTexture(gl, colorTexSceneHandle);
            colorTexSceneHandle = -1;
        }

        if (depthTexSceneHandle != -1) {
            glDeleteTexture(gl, depthTexSceneHandle);
            depthTexSceneHandle = -1;
        }

        if (fboSceneHandle != -1) {
            glDeleteFrameBuffer(gl, fboSceneHandle);
            fboSceneHandle = -1;
        }

        if (rboSceneHandle != -1) {
            glDeleteRenderbuffers(gl, rboSceneHandle);
            rboSceneHandle = -1;
        }
    }

    void drawTiles() {
        modelBuffers.clear();
        modelBuffers.setTargetBufferOffset(0);
        for (int x = 0; x < scene.getRadius() * Constants.REGION_SIZE; x++) {
            for (int y = 0; y < scene.getRadius() * Constants.REGION_SIZE; y++) {
                SceneTile tile = scene.getTile(0, x, y);
                if (tile != null) {
                    this.drawTile(tile);
                }
            }
        }

        // allocate enough size in the outputBuffer for the static verts + the dynamic verts -- each vertex is an ivec4, 4 ints
        gl.glBindBuffer(gl.GL_ARRAY_BUFFER, tmpOutBufferId);
        gl.glBufferData(gl.GL_ARRAY_BUFFER, (modelBuffers.getTargetBufferOffset() + MAX_TEMP_VERTICES) * GLBuffers.SIZEOF_INT * 4, null, gl.GL_DYNAMIC_DRAW);

        gl.glBindBuffer(gl.GL_ARRAY_BUFFER, tmpOutUvBufferId);
        gl.glBufferData(gl.GL_ARRAY_BUFFER, (modelBuffers.getTargetBufferOffset() + MAX_TEMP_VERTICES) * GLBuffers.SIZEOF_FLOAT * 4, null, gl.GL_DYNAMIC_DRAW);

        gl.glBindBuffer(gl.GL_ARRAY_BUFFER, colorPickerBufferId);
        gl.glBufferData(gl.GL_ARRAY_BUFFER, (modelBuffers.getTargetBufferOffset() + MAX_TEMP_VERTICES) * GLBuffers.SIZEOF_INT, null, gl.GL_DYNAMIC_DRAW);
    }

    void handleHover() {
        String debugText = "";
        int mouseX = inputHandler.getMouseX();
        int mouseY = inputHandler.getMouseY();

        pboIndex = (pboIndex + 1) % 2;
        int nextIndex = (pboIndex + 1) % 2;

        // Read from pixel buffer object async to get pixels without blocking render
        gl.glBindFramebuffer(gl.GL_READ_FRAMEBUFFER, fboMainRenderer);
        gl.glBindBuffer(gl.GL_PIXEL_PACK_BUFFER, pboIds[pboIndex]);
        gl.glReadBuffer(gl.GL_COLOR_ATTACHMENT1);
        gl.glReadPixels(mouseX, canvasHeight - mouseY, 1, 1, gl.GL_RED_INTEGER, gl.GL_INT, 0);

        gl.glBindBuffer(gl.GL_PIXEL_PACK_BUFFER, pboIds[nextIndex]);
        ByteBuffer srcBuf = gl.glMapBuffer(gl.GL_PIXEL_PACK_BUFFER, gl.GL_READ_ONLY);

        byte[] pboBytes = new byte[4];
        srcBuf.get(pboBytes);
        int pickerId = (pboBytes[3] & 0xFF) << 24 | (pboBytes[2] & 0xFF) << 16 | (pboBytes[1] & 0xFF) << 8 | (pboBytes[0] & 0xFF);
        gl.glReadBuffer(gl.GL_COLOR_ATTACHMENT0);
        gl.glUnmapBuffer(gl.GL_PIXEL_PACK_BUFFER);
        gl.glBindBuffer(gl.GL_PIXEL_PACK_BUFFER, 0);
        gl.glBindFramebuffer(gl.GL_READ_FRAMEBUFFER, 0);

        if (pickerId == -1) {
            return;
        }

        //int colorPickerId = ((x & 0xFFF) << 20) | ((y & 0xFFF) << 4) | objectId & 0xF;
        int x = (pickerId >> 20) & 0xFFF;
        int y = (pickerId >> 4) & 0xFFF;
        int type = pickerId & 0xF;

        debugText += String.format("id %d\n x %d y %d objId %d \n", pickerId, x, y, type);

//        SceneTile t = scene.getTile(0, x, y);
//        if (t != null) {
//            if (t.getTilePaint() == null) {
//                return;
//            }
        hoverId = pickerId;
//        }
        camera.setDebugText(debugText);
    }

    void drawTile(SceneTile tile) {
        int x = tile.getX();
        int y = tile.getY();

        if (tile.getTilePaint() != null) {
            tile.getTilePaint().setSceneX(x);
            tile.getTilePaint().setSceneY(y);
            tile.getTilePaint().draw(modelBuffers, x, y);
        }

        if (tile.getTileModel() != null) {
            tile.getTileModel().draw(modelBuffers, x, y);
        }

        FloorDecoration f = tile.getFloorDecoration();
        if (f != null) {
            f.draw(modelBuffers, x, y);
        }

        WallDecoration w = tile.getWallDecoration();
        if (w != null) {
            if (w.getEntityA() instanceof StaticObject) {
                w.draw(modelBuffers, x, y);
            } else if (w.getEntityA() instanceof DynamicObject) {
                w.setSceneX(x);
                w.setSceneY(y);
                dynamicDecorations.add(w);
            }
        }
    }

    private final List<Renderable> dynamicDecorations = new ArrayList<>();

    private void drawDynamic() {
        for (Renderable r : dynamicDecorations) {
            r.drawDynamic(modelBuffers, sceneUploader, PickerType.PICKABLE);
        }
    }

    // FIXME: I probably need an SSBO because I want to select more than 1024 (max uniforms for my gpu?)
    private SizedIntegerList selectedIds = new SizedIntegerList(255);
    private int hoverId;

    @Override
    public void display(GLAutoDrawable drawable) {
        if (sceneChangeRequested) {
            uploadScene();
        }

        handleHover();
        handleClick();

        drawDynamic();

        if (canvasWidth > 0 && canvasHeight > 0 && (canvasWidth != lastViewportWidth || canvasHeight != lastViewportHeight)) {
            createProjectionMatrix(0, canvasWidth, canvasHeight, 0, 1, MAX_DISTANCE * Perspective.LOCAL_TILE_SIZE);
            lastViewportWidth = canvasWidth;
            lastViewportHeight = canvasHeight;
        }

        // base FBO to enable picking
        gl.glBindFramebuffer(gl.GL_DRAW_FRAMEBUFFER, fboMainRenderer);

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
                final int maxSamples = glGetInteger(gl, gl.GL_MAX_SAMPLES);
                final int samples = Math.min(antiAliasingMode.getSamples(), maxSamples);

                initAAFbo(stretchedCanvasWidth, stretchedCanvasHeight, samples);

                lastStretchedCanvasWidth = stretchedCanvasWidth;
                lastStretchedCanvasHeight = stretchedCanvasHeight;
            }

            gl.glBindFramebuffer(gl.GL_DRAW_FRAMEBUFFER, fboSceneHandle);
        }
        lastAntiAliasingMode = antiAliasingMode;

        IntBuffer i = GLBuffers.newDirectIntBuffer(new int[]{gl.GL_COLOR_ATTACHMENT0, gl.GL_COLOR_ATTACHMENT1});
        gl.glDrawBuffers(2, i);

        // Clear scene
        int sky = 9493480;
        gl.glClearColor((sky >> 16 & 0xFF) / 255f, (sky >> 8 & 0xFF) / 255f, (sky & 0xFF) / 255f, 1f);
        gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT);

        modelBuffers.flip();
        modelBuffers.flipVertUv();

        IntBuffer vertexBuffer = modelBuffers.getVertexBuffer().getBuffer();
        FloatBuffer uvBuffer = modelBuffers.getUvBuffer().getBuffer();
        IntBuffer modelBuffer = modelBuffers.getModelBuffer().getBuffer();
        IntBuffer modelBufferSmall = modelBuffers.getModelBufferSmall().getBuffer();
        IntBuffer modelBufferUnordered = modelBuffers.getModelBufferUnordered().getBuffer();

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
            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 7, colorPickerBufferId);

            gl.glDispatchCompute(modelBuffers.getUnorderedModels(), 1, 1);

            // small
            gl.glUseProgram(glSmallComputeProgram);

            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 0, tmpModelBufferSmallId);
            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 1, this.bufferId);
            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 2, tmpBufferId);
            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 3, tmpOutBufferId); // vout[]
            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 4, tmpOutUvBufferId); //uvout[]
            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 5, this.uvBufferId);
            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 6, tmpUvBufferId);
            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 7, colorPickerBufferId);

            gl.glDispatchCompute(modelBuffers.getSmallModels(), 1, 1);

            // large
            gl.glUseProgram(glComputeProgram);

            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 0, tmpModelBufferId);
            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 1, this.bufferId);
            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 2, tmpBufferId);
            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 3, tmpOutBufferId);
            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 4, tmpOutUvBufferId);
            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 5, this.uvBufferId);
            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 6, tmpUvBufferId);
            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 7, colorPickerBufferId);

            gl.glDispatchCompute(modelBuffers.getLargeModels(), 1, 1);

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

            gl.glUniform1i(uniHoverId, hoverId);
            gl.glUniform1iv(uniSelectedIds, 255, selectedIds.toIntArray(), 0);

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

            gl.glEnableVertexAttribArray(2);
            gl.glBindBuffer(gl.GL_ARRAY_BUFFER, colorPickerBufferId);
            gl.glVertexAttribIPointer(2, 1, gl.GL_INT, 0, 0);

            gl.glDrawArrays(gl.GL_TRIANGLES, 0, modelBuffers.getTargetBufferOffset() + modelBuffers.getTempOffset());

            gl.glDisable(gl.GL_BLEND);
            gl.glDisable(gl.GL_CULL_FACE);

            gl.glUseProgram(0);
        }

        if (aaEnabled) {
            gl.glBindFramebuffer(gl.GL_READ_FRAMEBUFFER, fboSceneHandle);
            gl.glBindFramebuffer(gl.GL_DRAW_FRAMEBUFFER, fboMainRenderer);
            gl.glBlitFramebuffer(0, 0, lastStretchedCanvasWidth, lastStretchedCanvasHeight,
                    0, 0, lastStretchedCanvasWidth, lastStretchedCanvasHeight,
                    gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT, gl.GL_NEAREST);

            gl.glReadBuffer(gl.GL_COLOR_ATTACHMENT1);
            gl.glDrawBuffer(gl.GL_COLOR_ATTACHMENT1);
            gl.glBlitFramebuffer(0, 0, lastStretchedCanvasWidth, lastStretchedCanvasHeight,
                    0, 0, lastStretchedCanvasWidth, lastStretchedCanvasHeight,
                    gl.GL_COLOR_BUFFER_BIT, gl.GL_NEAREST);
            gl.glDisable(gl.GL_BLEND);

            // Reset
            gl.glReadBuffer(gl.GL_COLOR_ATTACHMENT0);
            gl.glDrawBuffer(gl.GL_COLOR_ATTACHMENT0);
        }

        gl.glBindFramebuffer(gl.GL_READ_FRAMEBUFFER, fboMainRenderer);
        gl.glBindFramebuffer(gl.GL_DRAW_FRAMEBUFFER, 0);
        gl.glBlitFramebuffer(0, 0, canvasWidth, canvasHeight,
                0, 0, canvasWidth, canvasHeight,
                gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT, gl.GL_NEAREST);

        gl.glBindFramebuffer(gl.GL_READ_FRAMEBUFFER, 0);

        modelBuffers.clearVertUv();
        modelBuffers.clear();

//        minimapController.drawCanvas(scene);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        gl = drawable.getGL().getGL4();
        gl.glViewport(x, y, width, height);
    }

    private boolean sceneChangeRequested = true;

    public void changeScene(Scene scene) {
        this.scene = scene;
        sceneChangeRequested = true;
    }

    private void uploadScene() {
        dynamicDecorations.clear();
        sceneChangeRequested = false;
        modelBuffers.clearVertUv();

        sceneUploader.upload(scene, modelBuffers.getVertexBuffer(), modelBuffers.getUvBuffer());

        modelBuffers.flipVertUv();

        IntBuffer vertexBuffer = modelBuffers.getVertexBuffer().getBuffer();
        FloatBuffer uvBuffer = modelBuffers.getUvBuffer().getBuffer();

        gl.glBindBuffer(gl.GL_ARRAY_BUFFER, bufferId);
        gl.glBufferData(gl.GL_ARRAY_BUFFER, vertexBuffer.limit() * GLBuffers.SIZEOF_INT, vertexBuffer, gl.GL_STATIC_COPY);

        gl.glBindBuffer(gl.GL_ARRAY_BUFFER, uvBufferId);
        gl.glBufferData(gl.GL_ARRAY_BUFFER, uvBuffer.limit() * GLBuffers.SIZEOF_FLOAT, uvBuffer, gl.GL_STATIC_COPY);

        gl.glBindBuffer(gl.GL_ARRAY_BUFFER, 0);

        vertexBuffer.clear();
        uvBuffer.clear();
//        modelBuffers.clearVertUv();

        drawTiles();
    }

    private void initVao() {
        // Create VAO
        vaoHandle = glGenVertexArrays(gl);
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

        uniHoverId = gl.glGetUniformLocation(glProgram, "hoverId");
        uniSelectedIds = gl.glGetUniformLocation(glProgram, "selectedIds");
    }

    private void initUniformBuffer() {
        uniformBufferId = glGenBuffers(gl);
        gl.glBindBuffer(gl.GL_UNIFORM_BUFFER, uniformBufferId);
        uniformBuffer.clear();
        uniformBuffer.put(new int[8]);
        uniformBuffer.flip();

        gl.glBufferData(gl.GL_UNIFORM_BUFFER, uniformBuffer.limit() * Integer.BYTES, uniformBuffer, gl.GL_DYNAMIC_DRAW);
        gl.glBindBuffer(gl.GL_UNIFORM_BUFFER, 0);
    }

    private void initPickerBuffer() {
        fboMainRenderer = GLUtil.glGenFrameBuffer(gl);
        gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, fboMainRenderer);

        // create depth buffer
        rboDepthMain = GLUtil.glGenRenderbuffer(gl);
        gl.glBindRenderbuffer(gl.GL_RENDERBUFFER, rboDepthMain);
        gl.glRenderbufferStorage(gl.GL_RENDERBUFFER, gl.GL_DEPTH_COMPONENT, canvasWidth, canvasHeight);
        gl.glFramebufferRenderbuffer(gl.GL_FRAMEBUFFER, gl.GL_DEPTH_ATTACHMENT, gl.GL_RENDERBUFFER, rboDepthMain);

        // Create color texture
        texColorMain = GLUtil.glGenTexture(gl);
        gl.glBindTexture(gl.GL_TEXTURE_2D, texColorMain);
        gl.glTexImage2D(gl.GL_TEXTURE_2D, 0, gl.GL_RGBA, canvasWidth, canvasHeight, 0, gl.GL_RGBA, gl.GL_FLOAT, null);
        gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MIN_FILTER, gl.GL_NEAREST);
        gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MAG_FILTER, gl.GL_NEAREST);
        gl.glFramebufferTexture2D(gl.GL_FRAMEBUFFER, gl.GL_COLOR_ATTACHMENT0, gl.GL_TEXTURE_2D, texColorMain, 0);

        texPickerMain = GLUtil.glGenTexture(gl);
        gl.glBindTexture(gl.GL_TEXTURE_2D, texPickerMain);
        gl.glTexImage2D(gl.GL_TEXTURE_2D, 0, gl.GL_R32I, canvasWidth, canvasHeight, 0, gl.GL_RED_INTEGER, gl.GL_INT, null);
        gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MIN_FILTER, gl.GL_NEAREST);
        gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MAG_FILTER, gl.GL_NEAREST);
        gl.glFramebufferTexture2D(gl.GL_FRAMEBUFFER, gl.GL_COLOR_ATTACHMENT1, gl.GL_TEXTURE_2D, texPickerMain, 0);

        // init pbo
        gl.glGenBuffers(2, pboIds, 0);
        gl.glBindBuffer(gl.GL_PIXEL_PACK_BUFFER, pboIds[0]);
        gl.glBufferData(gl.GL_PIXEL_PACK_BUFFER, canvasWidth * canvasHeight * GLBuffers.SIZEOF_INT, null, gl.GL_STREAM_READ);
        gl.glBindBuffer(gl.GL_PIXEL_PACK_BUFFER, pboIds[1]);
        gl.glBufferData(gl.GL_PIXEL_PACK_BUFFER, canvasWidth * canvasHeight * GLBuffers.SIZEOF_INT, null, gl.GL_STREAM_READ);
        gl.glBindBuffer(gl.GL_PIXEL_PACK_BUFFER, 0);

        int status = gl.glCheckFramebufferStatus(gl.GL_FRAMEBUFFER);
        if (status != gl.GL_FRAMEBUFFER_COMPLETE) {
            System.out.println("bad picker fbo");
        }
        gl.glBindTexture(gl.GL_TEXTURE_2D, 0);
        gl.glBindRenderbuffer(gl.GL_RENDERBUFFER, 0);
        gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, 0);
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
        colorPickerBufferId = GLUtil.glGenBuffers(gl);
    }

    private void createProjectionMatrix(float left, float right, float bottom, float top, float near, float far) {
        gl.glUseProgram(glProgram);

        FloatBuffer fb = Buffers.newDirectFloatBuffer(16);
        new Matrix4f()
                .setOrtho(left, right, bottom, top, near, far)
                .get(fb);
        gl.glUniformMatrix4fv(uniProjectionMatrix, 1, false, fb);

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
        // Bind color tex
        gl.glFramebufferTexture2D(gl.GL_FRAMEBUFFER, gl.GL_COLOR_ATTACHMENT0, gl.GL_TEXTURE_2D_MULTISAMPLE, colorTexSceneHandle, 0);

        // Create depth texture
        depthTexSceneHandle = GLUtil.glGenTexture(gl);
        gl.glBindTexture(gl.GL_TEXTURE_2D_MULTISAMPLE, depthTexSceneHandle);
        gl.glTexImage2DMultisample(gl.GL_TEXTURE_2D_MULTISAMPLE, aaSamples, gl.GL_DEPTH_COMPONENT, width, height, true);
        // bind depth tex
        gl.glFramebufferTexture2D(gl.GL_FRAMEBUFFER, gl.GL_DEPTH_ATTACHMENT, gl.GL_TEXTURE_2D_MULTISAMPLE, depthTexSceneHandle, 0);

        // Create picker texture
        int texPickerHandle = GLUtil.glGenTexture(gl);
        gl.glBindTexture(gl.GL_TEXTURE_2D_MULTISAMPLE, texPickerHandle);
        gl.glTexImage2DMultisample(gl.GL_TEXTURE_2D_MULTISAMPLE, aaSamples, gl.GL_R32I, width, height, true);
        // Bind color tex
        gl.glFramebufferTexture2D(gl.GL_FRAMEBUFFER, gl.GL_COLOR_ATTACHMENT1, gl.GL_TEXTURE_2D_MULTISAMPLE, texPickerHandle, 0);

        int status = gl.glCheckFramebufferStatus(gl.GL_FRAMEBUFFER);
        if (status != gl.GL_FRAMEBUFFER_COMPLETE) {
            System.out.println("bad picker fbo");
        }

        // Reset
        gl.glBindTexture(gl.GL_TEXTURE_2D_MULTISAMPLE, 0);
        gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, 0);
        gl.glBindRenderbuffer(gl.GL_RENDERBUFFER, 0);
    }

    private void shutdownBuffers() {
        if (bufferId != -1) {
            glDeleteBuffer(gl, bufferId);
            bufferId = -1;
        }

        if (uvBufferId != -1) {
            glDeleteBuffer(gl, uvBufferId);
            uvBufferId = -1;
        }

        if (tmpBufferId != -1) {
            glDeleteBuffer(gl, tmpBufferId);
            tmpBufferId = -1;
        }

        if (tmpUvBufferId != -1) {
            glDeleteBuffer(gl, tmpUvBufferId);
            tmpUvBufferId = -1;
        }

        if (tmpModelBufferId != -1) {
            glDeleteBuffer(gl, tmpModelBufferId);
            tmpModelBufferId = -1;
        }

        if (tmpModelBufferSmallId != -1) {
            glDeleteBuffer(gl, tmpModelBufferSmallId);
            tmpModelBufferSmallId = -1;
        }

        if (tmpModelBufferUnorderedId != -1) {
            glDeleteBuffer(gl, tmpModelBufferUnorderedId);
            tmpModelBufferUnorderedId = -1;
        }

        if (tmpOutBufferId != -1) {
            glDeleteBuffer(gl, tmpOutBufferId);
            tmpOutBufferId = -1;
        }

        if (tmpOutUvBufferId != -1) {
            glDeleteBuffer(gl, tmpOutUvBufferId);
            tmpOutUvBufferId = -1;
        }
    }

    int hoverStartX = -1;
    int hoverStartY = -1;

    void handleClick() {
        if (hoverId == -1) {
            return;
        }
        if (inputHandler.isMouseClicked()) {
            selectedIds.clear();
            selectedIds.add(hoverId);

            inputHandler.mouseClicked = false;
        }


        if (inputHandler.leftMousePressed) {
            int x = (hoverId >> 20) & 0xFFF;
            int y = (hoverId >> 4) & 0xFFF;
            hoverStartX = x;
            hoverStartY = y;
            inputHandler.leftMousePressed = false;
        }

        if (inputHandler.isLeftMouseDown()) {
            int hoverX = (hoverId >> 20) & 0xFFF;
            int hoverY = (hoverId >> 4) & 0xFFF;
            selectedIds.clear();
            for (int x = hoverStartX; x <= hoverX; x++) {
                for (int y = hoverStartY; y >= hoverY; y--) {
                    SceneTile t = scene.getTile(0, x, y);
                    if (t != null && t.getTilePaint() != null) {
                        for (int i = 0; i < 4; i++) {
                            int colorPickerId = ((x & 0xFFF) << 20) | ((y & 0xFFF) << 4) | i & 0xF;
                            selectedIds.add(colorPickerId);
                        }
//                        int colorPickerId = ((x & 0xFFF) << 20) | ((y & 0xFFF) << 4) | 0 & 0xF;
//                        selectedIds.add(colorPickerId);
                    }
                }
            }
        }
    }
}
