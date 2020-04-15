import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import glm.mat.Mat4x4;
import net.runelite.cache.SpriteManager;
import net.runelite.cache.fs.Store;
import net.runelite.cache.fs.StoreProvider;
import net.runelite.cache.item.RSTextureProvider;
import renderer.GpuFloatBuffer;
import renderer.GpuIntBuffer;
import renderer.SceneUploader;
import renderer.TextureManager;
import scene.Scene;
import scene.SceneRegionBuilder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static com.jogamp.opengl.GL2ES3.*;
import static glm.GlmKt.glm;
import static renderer.GLUtil.glGenBuffers;

/**
 * Created by GBarbieri on 16.03.2017.
 */
public class NewRenderer implements GLEventListener, KeyListener {

    private static GLWindow window;
    private static Animator animator;

    private interface Buffer {

        int VERTEX = 0;
        int ELEMENT = 1;
        int GLOBAL_MATRICES = 2;
        int MODEL_MATRIX = 3;
        int MAX = 4;
    }

    private IntBuffer bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX);

    private SceneUploader sceneUploader = new SceneUploader();
    private TextureManager textureManager = new TextureManager();
    private RSTextureProvider textureProvider;
    private Scene scene;

    public static void main(String[] args) {
        new NewRenderer().setup();
    }

    int vertices = 4;

    int vertex_size = 4; // X, Y, Z, Color

    private Program program;

    private int canvasWidth = 1024;
    private int canvasHeight = 768;

    private void setup() {
        try {
            Store store = StoreProvider.getStore();
            SpriteManager sprites = new SpriteManager(store);
            net.runelite.cache.TextureManager textureManager = new net.runelite.cache.TextureManager(store);

            textureManager.load();

            textureProvider = new RSTextureProvider(textureManager, sprites);
            sprites.load();

            scene = new Scene(new SceneRegionBuilder(textureProvider), 12854, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }


        GLProfile glProfile = GLProfile.get(GLProfile.GL4);
        GLCapabilities glCapabilities = new GLCapabilities(glProfile);

        window = GLWindow.create(glCapabilities);

        window.setTitle("Hello Triangle (enhanced)");
        window.setSize(canvasWidth, canvasHeight);

        window.setContextCreationFlags(GLContext.CTX_OPTION_DEBUG);
        window.setVisible(true);

        window.addGLEventListener(this);
        window.addKeyListener(this);

        animator = new Animator(window);
        animator.start();

        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowDestroyed(WindowEvent e) {
                animator.stop();
                System.exit(1);
            }
        });

    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL4 gl = drawable.getGL().getGL4();

        program = new Program(gl, "shaders", "vert", "frag");

        initBuffers(gl);
        gl.setSwapInterval(1);
        gl.glEnable(GL_DEPTH_TEST);
    }

    private GpuIntBuffer vertexBuffer = new GpuIntBuffer();
    private GpuFloatBuffer uvBuffer = new GpuFloatBuffer();

    private ByteBuffer globalMatricesPointer;

    int vbo_vertex_handle;
    int ibo_handle;
    int indices = 6;

    int vao_handle;
    private void initBuffers(GL4 gl) {
        // VERTEX BUFFER SETUP
//        FloatBuffer vertex_data = FloatBuffer.allocate(vertices * vertex_size);
//        vertex_data.put(new float[]{-0.5f, -0.5f, 0, 0});
//        vertex_data.put(new float[]{0.5f, -0.5f, 1, 0.7f});
//        vertex_data.put(new float[]{0.5f, 0.5f, 0, 0.2f});
//        vertex_data.put(new float[]{-0.5f, 0.5f, 1, 0.2f});
//        vertex_data.flip();


        vertexBuffer.clear();
        sceneUploader.upload(scene, vertexBuffer, uvBuffer);
        vertexBuffer.flip();

        // VAO BUFFER
        vao_handle = glGenBuffers(gl);
        gl.glBindVertexArray(vao_handle);

        IntBuffer vertexBuffer = this.vertexBuffer.getBuffer();
        vbo_vertex_handle = glGenBuffers(gl);
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo_vertex_handle);
        gl.glBufferData(gl.GL_ARRAY_BUFFER, vertexBuffer.limit() * GLBuffers.SIZEOF_INT, vertexBuffer, gl.GL_STATIC_DRAW);

        gl.glEnableVertexAttribArray(0);
        gl.glVertexAttribPointer(0, 3, gl.GL_UNSIGNED_INT, false, 4 * GLBuffers.SIZEOF_INT, 0);

        gl.glEnableVertexAttribArray(1);
        gl.glVertexAttribPointer(1, 1, gl.GL_UNSIGNED_INT, false, 4 * GLBuffers.SIZEOF_INT, 3 * GLBuffers.SIZEOF_INT);
        gl.glBindBuffer(GL_ARRAY_BUFFER, 0);

        // INDEX BUFFER
//        IntBuffer index_data = IntBuffer.allocate(indices);
//        index_data.put(new int[]{0, 1, 2, 2, 3, 1});
//        index_data.flip();
//
//        ibo_handle = glGenBuffers(gl);
//        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo_handle);
//        gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, index_data.limit() * GLBuffers.SIZEOF_INT, index_data, GL_STATIC_DRAW);
//        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        // PROJECTION MATRIX UNIFORM
        gl.glCreateBuffers(Buffer.MAX, bufferName);
        IntBuffer uniformBufferOffset = GLBuffers.newDirectIntBuffer(1);
        gl.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset);
        int globalBlockSize = glm.max(Mat4x4.SIZE * 2, uniformBufferOffset.get(0));

        gl.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.GLOBAL_MATRICES));
        gl.glBufferStorage(GL_UNIFORM_BUFFER, globalBlockSize, null, gl.GL_MAP_WRITE_BIT | gl.GL_MAP_PERSISTENT_BIT | gl.GL_MAP_COHERENT_BIT);
        gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        // map the transform buffers and keep them mapped
        globalMatricesPointer = gl.glMapNamedBufferRange(
                bufferName.get(Buffer.GLOBAL_MATRICES),
                0,
                Mat4x4.SIZE * 2,
                gl.GL_MAP_WRITE_BIT | gl.GL_MAP_PERSISTENT_BIT | gl.GL_MAP_COHERENT_BIT | gl.GL_MAP_INVALIDATE_BUFFER_BIT); // flags

        gl.glBindVertexArray(0);

        vertexBuffer.clear();
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL4 gl = drawable.getGL().getGL4();
//        vertexBuffer.flip();
        // view matrix
        {
            Mat4x4 view = new Mat4x4();
            view.to(globalMatricesPointer, Mat4x4.SIZE);
        }

        gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT);

        gl.glUseProgram(program.name);

        gl.glBindBufferBase(
                GL_UNIFORM_BUFFER,
                Semantic.Uniform.TRANSFORM0,
                bufferName.get(Buffer.GLOBAL_MATRICES));

        gl.glBindVertexArray(vao_handle);
        gl.glDrawArrays(vbo_vertex_handle, 0, vertexBuffer.getBuffer().limit() / 4);

        vertexBuffer.clear();


//        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo_handle);
//        gl.glDrawElements(gl.GL_TRIANGLES, indices, GL_UNSIGNED_INT, ibo_handle);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL4 gl = drawable.getGL().getGL4();

        // ortho matrix
        glm.ortho(0, canvasWidth, canvasHeight, 0, -1, 1).to(globalMatricesPointer);

        gl.glViewport(x, y, width, height);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        GL4 gl = drawable.getGL().getGL4();

        gl.glDeleteProgram(program.name);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            new Thread(() -> {
                window.destroy();
            }).start();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    private class Program {

        public int name = 0;

        public Program(GL4 gl, String root, String vertex, String fragment) {

            ShaderCode vertShader = ShaderCode.create(gl, GL_VERTEX_SHADER, this.getClass(), root, null, vertex,
                    "vert", null, true);
            ShaderCode fragShader = ShaderCode.create(gl, GL_FRAGMENT_SHADER, this.getClass(), root, null, fragment,
                    "frag", null, true);

            ShaderProgram shaderProgram = new ShaderProgram();

            shaderProgram.add(vertShader);
            shaderProgram.add(fragShader);

            shaderProgram.init(gl);

            name = shaderProgram.program();

            shaderProgram.link(gl, System.err);
        }
    }
}