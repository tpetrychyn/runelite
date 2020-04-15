import com.jogamp.common.GlueGenVersion;
import com.jogamp.common.util.VersionUtil;
import com.jogamp.nativewindow.NativeWindowVersion;
import com.jogamp.newt.NewtVersion;
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
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2ES2.GL_DEBUG_OUTPUT;
import static com.jogamp.opengl.GL2ES3.*;
import static renderer.GLUtil.glGenBuffers;

/**
 * Created by GBarbieri on 16.03.2017.
 */
public class HelloTriangleSimple implements GLEventListener, KeyListener {

    private static GLWindow window;
    private static Animator animator;

    private SceneUploader sceneUploader = new SceneUploader();
    private TextureManager textureManager = new TextureManager();
    private RSTextureProvider textureProvider;
    private Scene scene;

    public static void main(String[] args) {
        new HelloTriangleSimple().setup();
    }

    int vertices = 4;

    int vertex_size = 4; // X, Y, Z, Color

    private Program program;

    private void setup() {
//        try {
//            Store store = StoreProvider.getStore();
//            SpriteManager sprites = new SpriteManager(store);
//            net.runelite.cache.TextureManager textureManager = new net.runelite.cache.TextureManager(store);
//
//            textureManager.load();
//
//            textureProvider = new RSTextureProvider(textureManager, sprites);
//            sprites.load();
//
//            scene = new Scene(new SceneRegionBuilder(textureProvider), 12854, 1);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


        GLProfile glProfile = GLProfile.get(GLProfile.GL4);
        GLCapabilities glCapabilities = new GLCapabilities(glProfile);

        window = GLWindow.create(glCapabilities);

        window.setTitle("Hello Triangle (enhanced)");
        window.setSize(1024, 768);

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

    int vbo_vertex_handle;
    int ibo_handle;
    int indices = 6;

    int colorUniform;

    int vao_handle;
    private void initBuffers(GL4 gl) {
        // VERTEX BUFFER SETUP
        FloatBuffer vertex_data = FloatBuffer.allocate(vertices * vertex_size);
        vertex_data.put(new float[]{-0.5f, -0.5f, 0, 0});
        vertex_data.put(new float[]{0.5f, -0.5f, 1, 0.7f});
        vertex_data.put(new float[]{0.5f, 0.5f, 0, 0.2f});
        vertex_data.put(new float[]{-0.5f, 0.5f, 1, 0.2f});
        vertex_data.flip();

        // VAO BUFFER
        vao_handle = glGenBuffers(gl);
        gl.glBindVertexArray(vao_handle);

        vbo_vertex_handle = glGenBuffers(gl);
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo_vertex_handle);
        gl.glBufferData(GL_ARRAY_BUFFER, vertex_data.limit() * GLBuffers.SIZEOF_FLOAT, vertex_data, GL_STATIC_DRAW);

        gl.glEnableVertexAttribArray(0);
        gl.glVertexAttribPointer(0, 3, gl.GL_FLOAT, false, vertex_size * GLBuffers.SIZEOF_FLOAT, 0);

        gl.glEnableVertexAttribArray(1);
        gl.glVertexAttribPointer(1, 1, gl.GL_FLOAT, false, vertex_size * GLBuffers.SIZEOF_FLOAT, 3 * GLBuffers.SIZEOF_FLOAT);
        gl.glBindBuffer(GL_ARRAY_BUFFER, 0);

        // INDEX BUFFER
        IntBuffer index_data = IntBuffer.allocate(indices);
        index_data.put(new int[]{0, 1, 2, 2, 3, 1});
        index_data.flip();

        ibo_handle = glGenBuffers(gl);
        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo_handle);
        gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, index_data.limit() * GLBuffers.SIZEOF_INT, index_data, GL_STATIC_DRAW);
        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        // COLOR UNIFORM
        gl.glUseProgram(program.name);
        colorUniform = gl.glGetUniformLocation(program.name, "u_Color");
        assert(colorUniform != -1);
        gl.glUseProgram(0);

        gl.glBindVertexArray(0);
    }

    float r = 0f;

    @Override
    public void display(GLAutoDrawable drawable) {
        GL4 gl = drawable.getGL().getGL4();
        gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT);

        gl.glUseProgram(program.name);
        gl.glUniform4f(colorUniform, r, 0.5f, 0.5f, 1);

        gl.glBindVertexArray(vao_handle);
        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo_handle);
        gl.glDrawElements(gl.GL_TRIANGLES, indices, GL_UNSIGNED_INT, ibo_handle);

        r += 0.01;
        if (r > 1) {
            r = 0;
        }
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL4 gl = drawable.getGL().getGL4();

//        gl.glViewport(x, y, width, height);
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