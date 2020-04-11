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
import impl.SceneImpl;
import impl.TileImpl;
import impl.WallDecoration;
import jogamp.nativewindow.SurfaceScaleUtils;
import layoutControllers.MainController;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.cache.SpriteManager;
import net.runelite.cache.definitions.TextureDefinition;
import net.runelite.cache.fs.Store;
import net.runelite.cache.item.RSTextureProvider;
import renderer.Camera;
import template.Template;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

@Slf4j
public class MapEditor implements GLEventListener, KeyListener {

    // This is the maximum number of triangles the compute shaders support
    private static final int MAX_TRIANGLE = 4096;
    private static final int SMALL_TRIANGLE_COUNT = 512;
    private static final int FLAG_SCENE_BUFFER = Integer.MIN_VALUE;
    static final int MAX_DISTANCE = 950;

    private MouseListener mouseListener;

    private SceneUploader sceneUploader = new SceneUploader();
    private SceneImpl scene = new SceneImpl();
    private TextureManager textureManager = new TextureManager();
    private RSTextureProvider textureProvider;
    private MapPreparer mapPreparer;

    private GL4 gl;

//    static final String LINUX_VERSION_HEADER =
//            "#version 420\n" +
//                    "#extension GL_ARB_compute_shader : require\n" +
//                    "#extension GL_ARB_shader_storage_buffer_object : require\n" +
//                    "#extension GL_ARB_explicit_attrib_location : require\n";
//    static final String WINDOWS_VERSION_HEADER = "#version 430\n";
//
//    static final Shader PROGRAM = new Shader()
//            .add(GL4.GL_VERTEX_SHADER, "gpu/vert.glsl")
//            .add(GL4.GL_GEOMETRY_SHADER, "gpu/geom.glsl")
//            .add(GL4.GL_FRAGMENT_SHADER, "gpu/frag.glsl");
//
//    static final Shader COMPUTE_PROGRAM = new Shader()
//            .add(GL4.GL_COMPUTE_SHADER, "gpu/comp.glsl");
//
//    static final Shader SMALL_COMPUTE_PROGRAM = new Shader()
//            .add(GL4.GL_COMPUTE_SHADER, "gpu/comp_small.glsl");
//
//    static final Shader UNORDERED_COMPUTE_PROGRAM = new Shader()
//            .add(GL4.GL_COMPUTE_SHADER, "gpu/comp_unordered.glsl");
//
//    static final Shader UI_PROGRAM = new Shader()
//            .add(GL4.GL_VERTEX_SHADER, "gpu/vertui.glsl")
//            .add(GL4.GL_FRAGMENT_SHADER, "gpu/fragui.glsl");

    private int glProgram;
    private int glComputeProgram;
    private int glSmallComputeProgram;
    private int glUnorderedComputeProgram;
    private int glUiProgram;

    private int vaoHandle;

    private int interfaceTexture;

    private int vaoUiHandle;
    private int vboUiHandle;

    private int fboSceneHandle;
    private int texSceneHandle;
    private int rboSceneHandle;

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

    private MainController mainController;

    public void LoadMap(MainController mainController) {
        try {
            File base = StoreLocation.LOCATION;
            Store store = new Store(base);
            store.load();
            SpriteManager sprites = new SpriteManager(store);
            net.runelite.cache.TextureManager textureManager = new net.runelite.cache.TextureManager(store);
            textureManager.load();
            textureProvider = new RSTextureProvider(textureManager, sprites);
            sprites.load();

            mapPreparer = new MapPreparer(store, textureProvider);
            mapPreparer.load();

            scene = new SceneImpl();
        } catch (IOException e) {
            e.printStackTrace();
        }

        bufferId = uvBufferId = uniformBufferId = tmpBufferId = tmpUvBufferId = tmpModelBufferId = tmpModelBufferSmallId = tmpModelBufferUnorderedId = tmpOutBufferId = tmpOutUvBufferId = -1;
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

        GLWindow window = GLWindow.create(screen, glCaps);
        window.addGLEventListener(this);
        window.addKeyListener(this);
        mouseListener = new MouseListener();

        mouseListener.getDragListeners().add(e -> {
            int speed = 2;
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
        window.addMouseListener(mouseListener);


//        window.setSize(800, 600);
//        window.setVisible(true);

        NewtCanvasJFX glCanvas = new NewtCanvasJFX(window);
        glCanvas.setWidth(800);
        glCanvas.setHeight(600);
        mainController.getGroup().getChildren().add(glCanvas);

        this.mainController = mainController;
        mainController.setCamera(camera);
        mainController.setMouseListener(mouseListener);

        Animator animator = new Animator(window);
        animator.start();

        lastViewportWidth = lastViewportHeight = lastCanvasWidth = lastCanvasHeight = -1;
        lastStretchedCanvasWidth = lastStretchedCanvasHeight = -1;
        lastAntiAliasingMode = null;

        textureArrayId = -1;
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        try {
            gl = drawable.getGL().getGL4();

            initVao();
            initProgram();
            initInterfaceTexture();
            initUniformBuffer();
            initBuffers();

            uploadScene();
        } catch (ShaderException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {

    }

    void drawTiles() {
        int cameraX = camera.getCameraX();
        int cameraY = camera.getCameraY();

        int screenCenterX = cameraX / Perspective.LOCAL_TILE_SIZE;
        int screenCenterY = cameraY / Perspective.LOCAL_TILE_SIZE;

        int cameraXTileMin = 0;
        int cameraYTileMin = 0;
        int cameraXTileMax = Constants.SCENE_SIZE;
        int cameraYTileMax = Constants.SCENE_SIZE;

        TileImpl[][] tiles = scene.getTiles()[0];
        TileImpl tile;
        for (int x = -MAX_DISTANCE; x <= 0; x++) {
            int xMin = x + screenCenterX;
            int xMax = screenCenterX - x;
            if (xMin >= cameraXTileMin || xMax < cameraXTileMax) {
                for (int y = -MAX_DISTANCE; y <= 0; y++) {
                    int yMin = y + screenCenterY;
                    int yMax = screenCenterY - y;
                    if (xMin >= cameraXTileMin) {
                        if (yMin >= cameraYTileMin) {
                            tile = tiles[xMin][yMin];
                            if (tile != null) {
                                this.drawTile(tile);
                            }
                        }

                        if (yMax < cameraYTileMax) {
                            tile = tiles[xMin][yMax];
                            if (tile != null) {
                                this.drawTile(tile);
                            }
                        }
                    }

                    if (xMax < cameraXTileMax) {
                        if (yMin >= cameraYTileMin) {
                            tile = tiles[xMax][yMin];
                            if (tile != null) {
                                this.drawTile(tile);
                            }
                        }

                        if (yMax < cameraYTileMax) {
                            tile = tiles[xMax][yMax];
                            if (tile != null) {
                                this.drawTile(tile);
                            }
                        }
                    }
                }
            }
        }
    }

    void drawTile(TileImpl tile) {
        int x = tile.getX();
        int y = tile.getY();

        int pitchSin = camera.getPitchSin();
        int pitchCos = camera.getPitchCos();
        int yawSin = camera.getYawSin();
        int yawCos = camera.getYawCos();

        if (tile.getTilePaint() != null) {
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

                            drawScenePaint(tile.getTilePaint(), tile.getX(), tile.getY());
                        }
                    }
                }
            }
        } else if (tile.getTileModel() != null) {
            drawSceneModel(tile.getTileModel(), x, y);
        }

        FloorDecoration f = tile.getFloorDecoration();
        if (f != null) {
            drawSceneRenderable(f.getModel(), 0, f.getHeight(), f.getX(), f.getY());
        }

        WallDecoration w = tile.getWallDecoration();
        if (w != null) {
            drawSceneRenderable(w.getModelA(), w.getOrientationA(), w.getHeight(), w.getX(), w.getY());
        }
    }

    void handleHover() {
        if (hoverTile == lastHoverTile) {
            return;
        }

        if (saveSw != 0) {
            lastHoverTile.getTilePaint().setSwColor(saveSw);
            lastHoverTile.getTilePaint().setSeColor(saveSe);
            lastHoverTile.getTilePaint().setNeColor(saveNe);
            lastHoverTile.getTilePaint().setNwColor(saveNw);
        }

        lastHoverTile = hoverTile;
        saveSw = hoverTile.getTilePaint().getSwColor();
        saveSe = hoverTile.getTilePaint().getSeColor();
        saveNe = hoverTile.getTilePaint().getNeColor();
        saveNw = hoverTile.getTilePaint().getNwColor();

        hoverTile.getTilePaint().setSwColor(5555);
        hoverTile.getTilePaint().setSeColor(5555);
        hoverTile.getTilePaint().setNeColor(5555);
        hoverTile.getTilePaint().setNwColor(5555);
        uploadScene();
    }

    TileImpl lastHoverTile;
    TileImpl hoverTile;
    int saveSw, saveSe, saveNe, saveNw;

    int windowWidth = 800;
    int windowHeight = 600;

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

    void moveScene() {
        if (lastX != xPos || lastY != yPos) {
            scene.clearTiles();
            mapPreparer.loadTiles(scene, xPos, yPos);
            mapPreparer.loadObjects(scene, xPos, yPos);
            uploadScene();

            lastX = xPos;
            lastY = yPos;
        }
    }



    @Override
    public void display(GLAutoDrawable drawable) {
        handleKeys();
        moveScene();
//        handleHover();
        handleClick();
        drawTiles();

        gl = drawable.getGL().getGL4();

        if (windowWidth > 0 && windowHeight > 0 && (windowWidth != lastViewportWidth || windowHeight != lastViewportHeight)) {
            createProjectionMatrix(0, windowWidth, windowHeight, 0, 0, Constants.SCENE_SIZE * Perspective.LOCAL_TILE_SIZE);
            lastViewportWidth = windowWidth;
            lastViewportHeight = windowHeight;
        }

        // Clear scene
        int sky = 9493480;
        gl.glClearColor((sky >> 16 & 0xFF) / 255f, (sky >> 8 & 0xFF) / 255f, (sky & 0xFF) / 255f, 1f);
        gl.glClear(gl.GL_COLOR_BUFFER_BIT);

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
            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 3, tmpOutBufferId);
            gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 4, tmpOutUvBufferId);
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

            final TextureDefinition[] textures = textureProvider.getTextureDefinitions();
            int renderHeightOff = 0;
            int renderWidthOff = 0;
            int renderCanvasHeight = 800;
//            int renderViewportHeight = viewportHeight;
//            int renderViewportWidth = viewportWidth;

//            if (client.isStretchedEnabled()) {
//                Dimension dim = client.getStretchedDimensions();
//                renderCanvasHeight = dim.height;
//
//                double scaleFactorY = dim.getHeight() / canvasHeight;
//                double scaleFactorX = dim.getWidth() / canvasWidth;
//
//                // Pad the viewport a little because having ints for our viewport dimensions can introduce off-by-one errors.
//                final int padding = 1;
//
//                // Ceil the sizes because even if the size is 599.1 we want to treat it as size 600 (i.e. render to the x=599 pixel).
//                renderViewportHeight = (int) Math.ceil(scaleFactorY * (renderViewportHeight)) + padding * 2;
//                renderViewportWidth = (int) Math.ceil(scaleFactorX * (renderViewportWidth)) + padding * 2;
//
//                // Floor the offsets because even if the offset is 4.9, we want to render to the x=4 pixel anyway.
//                renderHeightOff = (int) Math.floor(scaleFactorY * (renderHeightOff)) - padding;
//                renderWidthOff = (int) Math.floor(scaleFactorX * (renderWidthOff)) - padding;
//            }

//            glDpiAwareViewport(renderWidthOff, renderCanvasHeight - renderViewportHeight - renderHeightOff, renderViewportWidth, renderViewportHeight);

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

        vertexBuffer.clear();
        uvBuffer.clear();
        modelBuffer.clear();
        modelBufferSmall.clear();
        modelBufferUnordered.clear();

        targetBufferOffset = 0;
        smallModels = largeModels = unorderedModels = 0;
        tempOffset = 0;
        tempUvOffset = 0;

        // Texture on UI
//        drawUi(canvasHeight, canvasWidth);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
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
        gl.glBufferData(gl.GL_ARRAY_BUFFER, vertexBuffer.limit() * Integer.BYTES, vertexBuffer, gl.GL_STATIC_COPY);

        gl.glBindBuffer(gl.GL_ARRAY_BUFFER, uvBufferId);
        gl.glBufferData(gl.GL_ARRAY_BUFFER, uvBuffer.limit() * Float.BYTES, uvBuffer, gl.GL_STATIC_COPY);

        gl.glBindBuffer(gl.GL_ARRAY_BUFFER, 0);

        vertexBuffer.clear();
        uvBuffer.clear();
    }

    private void initVao() {
        // Create VAO
        vaoHandle = GLUtil.glGenVertexArrays(gl);

        // Create UI VAO
        vaoUiHandle = GLUtil.glGenVertexArrays(gl);
        // Create UI buffer
        vboUiHandle = GLUtil.glGenBuffers(gl);
        gl.glBindVertexArray(vaoUiHandle);

        FloatBuffer vboUiBuf = GpuFloatBuffer.allocateDirect(5 * 4);
        vboUiBuf.put(new float[]{
                // positions     // texture coords
                1f, 1f, 0.0f, 1.0f, 0f, // top right
                1f, -1f, 0.0f, 1.0f, 1f, // bottom right
                -1f, -1f, 0.0f, 0.0f, 1f, // bottom left
                -1f, 1f, 0.0f, 0.0f, 0f  // top left
        });
        vboUiBuf.rewind();
        gl.glBindBuffer(gl.GL_ARRAY_BUFFER, vboUiHandle);
        gl.glBufferData(gl.GL_ARRAY_BUFFER, vboUiBuf.capacity() * Float.BYTES, vboUiBuf, gl.GL_STATIC_DRAW);

        // position attribute
        gl.glVertexAttribPointer(0, 3, gl.GL_FLOAT, false, 5 * Float.BYTES, 0);
        gl.glEnableVertexAttribArray(0);

        // texture coord attribute
        gl.glVertexAttribPointer(1, 2, gl.GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
        gl.glEnableVertexAttribArray(1);

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
        glUiProgram = Shader.UI_PROGRAM.compile(gl, template);

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

        uniTex = gl.glGetUniformLocation(glUiProgram, "tex");
        uniTexSamplingMode = gl.glGetUniformLocation(glUiProgram, "samplingMode");
        uniTexTargetDimensions = gl.glGetUniformLocation(glUiProgram, "targetDimensions");
        uniTexSourceDimensions = gl.glGetUniformLocation(glUiProgram, "sourceDimensions");
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
        float tz = -((far + near) / (far - near));

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

    private int getScaledValue(final double scale, final int value) {
        return SurfaceScaleUtils.scale(value, (float) scale);
    }

    private void glDpiAwareViewport(final int x, final int y, final int width, final int height) {
//        final AffineTransform t = ((Graphics2D) canvas.getGraphics()).getTransform();
        gl.glViewport(
                getScaledValue(1, x),
                getScaledValue(1, y),
                getScaledValue(1, width),
                getScaledValue(1, height));
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

    private void drawSceneRenderable(Model model, int orientation, int height, int x, int y) {
        if (model != null && model.getSceneId() == sceneUploader.sceneId) {
//            model.calculateBoundsCylinder();
//            model.calculateExtreme(orientation);

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
            buffer.put(x).put(height).put(y);

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

    // edgeville
//    int lastX, xPos = 3086;
//    int lastY, yPos = 3491;

    int lastX, xPos = 2520;
    int lastY, yPos = 3490;

    void handleKeys() {
        double xVec = -(double)camera.getYawSin() / 65535;
        double yVec = (double)camera.getYawCos() / 65535;
        double zVec = (double)camera.getPitchSin() / 65535;

        int speed = 1;
        if (keys[KeyEvent.VK_SHIFT]) {
            speed = 2;
        }

        if (keys[KeyEvent.VK_W]) {
            if (camera.addX((int)(10 * xVec * speed))) {
                xPos += Constants.SCENE_SIZE;
            }
            if (camera.addY((int)(10 * yVec * speed))) {
                yPos += Constants.SCENE_SIZE;
            }

            camera.addZ((int)(10 * zVec * speed));
        }

        if (keys[KeyEvent.VK_S]) {
            if (camera.addX(-(int)(10 * xVec * speed))) {
                xPos += Constants.SCENE_SIZE;
            }
            if (camera.addY(-(int)(10 * yVec * speed))) {
                yPos += Constants.SCENE_SIZE;
            }

            camera.addZ(-(int)(10 * zVec * speed));
        }

        if (keys[KeyEvent.VK_A]) {
            if (camera.addY((int)(10 * xVec * speed))) {
                xPos += Constants.SCENE_SIZE;
            }
            if (camera.addX(-(int)(10 * yVec * speed))) {
                yPos += Constants.SCENE_SIZE;
            }
        }

        if (keys[KeyEvent.VK_D]) {
            if (camera.addY(-(int)(10 * xVec * speed))) {
                xPos += Constants.SCENE_SIZE;
            }
            if (camera.addX((int)(10 * yVec * speed))) {
                yPos += Constants.SCENE_SIZE;
            }
        }

        if (keys[KeyEvent.VK_SPACE]) {
            camera.addZ(-10*speed);
        }

        if (keys[KeyEvent.VK_X]) {
            camera.addZ(10*speed);
        }

        if (keys[KeyEvent.VK_O]) {
            camera.addYaw(2 * speed);
        }

        if (keys[KeyEvent.VK_P]) {
            camera.addYaw(-2 * speed);
        }

        if (keys[KeyEvent.VK_I]) {
            camera.addZ(-2 * speed);
        }

        if (keys[KeyEvent.VK_K]) {
            camera.addZ(2 * speed);
        }
    }

    void handleClick() {
        if (hoverTile == null) {
            return;
        }
        if (mouseListener.isMouseClicked()) {
            System.out.printf("clicked tile worldx %s worldY %s %s \n", xPos + hoverTile.getX() - 52, yPos + hoverTile.getY() - 52, hoverTile);
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
