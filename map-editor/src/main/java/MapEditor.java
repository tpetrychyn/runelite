import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.Animator;
import config.AntiAliasingMode;
import impl.SceneImpl;
import jogamp.nativewindow.SurfaceScaleUtils;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.cache.SpriteManager;
import net.runelite.cache.definitions.TextureDefinition;
import net.runelite.cache.fs.Store;
import net.runelite.cache.item.RSTextureProvider;
import net.runelite.cache.region.Region;
import net.runelite.cache.region.RegionLoader;
import template.Template;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

@Slf4j
public class MapEditor implements GLEventListener, KeyListener {

    // This is the maximum number of triangles the compute shaders support
    private static final int MAX_TRIANGLE = 4096;
    private static final int SMALL_TRIANGLE_COUNT = 512;
    private static final int FLAG_SCENE_BUFFER = Integer.MIN_VALUE;
    static final int MAX_DISTANCE = 180;
    static final int MAX_FOG_DEPTH = 100;

    private static GLWindow window;
    private static Animator animator;

    private SceneUploader sceneUploader = new SceneUploader();
    private Scene scene = new SceneImpl();
    private TextureManager textureManager = new TextureManager();
    private RSTextureProvider textureProvider;

    private GL4 gl;

    static final String LINUX_VERSION_HEADER =
            "#version 420\n" +
                    "#extension GL_ARB_compute_shader : require\n" +
                    "#extension GL_ARB_shader_storage_buffer_object : require\n" +
                    "#extension GL_ARB_explicit_attrib_location : require\n";
    static final String WINDOWS_VERSION_HEADER = "#version 430\n";

    static final Shader PROGRAM = new Shader()
            .add(GL4.GL_VERTEX_SHADER, "vert.glsl")
            .add(GL4.GL_GEOMETRY_SHADER, "geom.glsl")
            .add(GL4.GL_FRAGMENT_SHADER, "frag.glsl");

    static final Shader COMPUTE_PROGRAM = new Shader()
            .add(GL4.GL_COMPUTE_SHADER, "comp.glsl");

    static final Shader SMALL_COMPUTE_PROGRAM = new Shader()
            .add(GL4.GL_COMPUTE_SHADER, "comp_small.glsl");

    static final Shader UNORDERED_COMPUTE_PROGRAM = new Shader()
            .add(GL4.GL_COMPUTE_SHADER, "comp_unordered.glsl");

    static final Shader UI_PROGRAM = new Shader()
            .add(GL4.GL_VERTEX_SHADER, "vertui.glsl")
            .add(GL4.GL_FRAGMENT_SHADER, "fragui.glsl");

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

    private int centerX = 422;
    private int centerY = 490;
    private int yaw = 2000;
    private int pitch = 250;

    private int cameraX = 2400;
    private int cameraY = 6500;
    private int cameraZ = -2500;

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

    public static void main(String[] args) {
        new MapEditor().setup();
    }

    private void setup() {
        try {
            File base = StoreLocation.LOCATION;
            Store store = new Store(base);
            store.load();
            SpriteManager sprites = new SpriteManager(store);
            net.runelite.cache.TextureManager textureManager = new net.runelite.cache.TextureManager(store);
            textureManager.load();
            textureProvider = new RSTextureProvider(textureManager, sprites);
            sprites.load();

            RegionLoader regionLoader = new RegionLoader(store);
            regionLoader.loadRegions();

            MapPreparer mapPreparer = new MapPreparer(store);
            mapPreparer.load();

            for (Region region : regionLoader.getRegions()) {
                if (region.getRegionID() != 10038 && region.getRegionID() != 9783) {
                    continue;
                }

                scene = new SceneImpl();
                mapPreparer.loadTiles(region, (SceneImpl) scene);
            }
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

        GLProfile glProfile = GLProfile.get(GLProfile.GL4);
        GLCapabilities glCaps = new GLCapabilities(glProfile);

        window = GLWindow.create(glCaps);

        window.setTitle("Hello Triangle (enhanced)");
        window.setSize(1024, 768);

        window.addGLEventListener(this);
        window.addKeyListener(this);

        window.setContextCreationFlags(GLContext.CTX_OPTION_DEBUG);
        window.setVisible(true);

        animator = new Animator(window);
        animator.start();

        window.addWindowListener(new WindowAdapter() {
            public void windowDestroyed(WindowEvent e) {
                animator.stop();
                System.exit(1);
            }
        });

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


    @Override
    public void display(GLAutoDrawable drawable) {
        handleKeys();

        gl = drawable.getGL().getGL4();

        for (int y = 0; y < Constants.SCENE_SIZE; ++y) {
            for (int x = 0; x < Constants.SCENE_SIZE; ++x) {
                Tile t = scene.getTiles()[0][x][y];
                if (t == null) {
                    continue;
                }
                if (t.getTilePaint() != null) {
                    drawScenePaint(t.getTilePaint(), t.getX(), t.getY());
                }
                if (t.getTileModel() != null) {
                    drawSceneModel(t.getTileModel(), t.getX(), t.getY());
                }
            }
        }

        final int viewportHeight = 600;
        final int viewportWidth = 800;

        // Clear scene
        int sky = 9493480;
        gl.glClearColor((sky >> 16 & 0xFF) / 255f, (sky >> 8 & 0xFF) / 255f, (sky & 0xFF) / 255f, 1f);
        gl.glClear(gl.GL_COLOR_BUFFER_BIT);

        createProjectionMatrix(0, 800, 600, 0, 0, Constants.SCENE_SIZE * Perspective.LOCAL_TILE_SIZE * 2);

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
                .put(yaw)
                .put(pitch)
                .put(centerX)
                .put(centerY)
                .put(300)
                .put(cameraX) //x
                .put(cameraZ) // z
                .put(cameraY); // y
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
            int renderViewportHeight = viewportHeight;
            int renderViewportWidth = viewportWidth;

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

            final int fogDepth = 2;
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
                return WINDOWS_VERSION_HEADER;
            }
            return null;
        });
        template.addInclude(MapEditor.class);

        glProgram = PROGRAM.compile(gl, template);
        glComputeProgram = COMPUTE_PROGRAM.compile(gl, template);
        glSmallComputeProgram = SMALL_COMPUTE_PROGRAM.compile(gl, template);
        glUnorderedComputeProgram = UNORDERED_COMPUTE_PROGRAM.compile(gl, template);
        glUiProgram = UI_PROGRAM.compile(gl, template);

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

    void handleKeys() {
        int speed = 1;
        if (keys[KeyEvent.VK_SHIFT]) {
            speed = 10;
        }

        if (keys[KeyEvent.VK_LEFT]) {
            cameraX += 2 * speed;
        }

        if (keys[KeyEvent.VK_RIGHT]) {
            cameraX -= 2 * speed;
        }

        if (keys[KeyEvent.VK_UP]) {
            cameraY += 2 * speed;
        }

        if (keys[KeyEvent.VK_DOWN]) {
            cameraY -= 2 * speed;
        }

        if (keys[KeyEvent.VK_P]) {
            yaw += 2 * speed;
        }

        if (keys[KeyEvent.VK_O]) {
            yaw -= 2 * speed;
        }

        if (keys[KeyEvent.VK_I]) {
            cameraZ -= 2 * speed;
        }

        if (keys[KeyEvent.VK_K]) {
            cameraZ += 2 * speed;
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        keys[e.getKeyCode()] = true;
    }

    boolean[] keys = new boolean[250];
    @Override
    public void keyReleased(KeyEvent e) {
        if(e.isAutoRepeat()){
            return;
        }

        keys[e.getKeyCode()] = false;
    }
}
