package renderer.helpers;

import com.jogamp.opengl.GL4;

public class GLUtil
{
	private static final int ERR_LEN = 1024;

	private static final int[] buf = new int[1];

	public static int glGetInteger(GL4 gl, int pname)
	{
		gl.glGetIntegerv(pname, buf, 0);
		return buf[0];
	}

	public static int glGetShader(GL4 gl, int shader, int pname)
	{
		gl.glGetShaderiv(shader, pname, buf, 0);
		assert buf[0] > -1;
		return buf[0];
	}

	public static int glGetProgram(GL4 gl, int program, int pname)
	{
		gl.glGetProgramiv(program, pname, buf, 0);
		assert buf[0] > -1;
		return buf[0];
	}

	public static String glGetShaderInfoLog(GL4 gl, int shader)
	{
		byte[] err = new byte[ERR_LEN];
		gl.glGetShaderInfoLog(shader, ERR_LEN, buf, 0, err, 0);
		return new String(err, 0, buf[0]);
	}

	public static String glGetProgramInfoLog(GL4 gl, int program)
	{
		byte[] err = new byte[ERR_LEN];
		gl.glGetProgramInfoLog(program, ERR_LEN, buf, 0, err, 0);
		return new String(err, 0, buf[0]);
	}

	public static int glGenVertexArrays(GL4 gl)
	{
		gl.glGenVertexArrays(1, buf, 0);
		return buf[0];
	}

	public static void glDeleteVertexArrays(GL4 gl, int vertexArray)
	{
		buf[0] = vertexArray;
		gl.glDeleteVertexArrays(1, buf, 0);
	}

	public static int glGenBuffers(GL4 gl)
	{
		gl.glGenBuffers(1, buf, 0);
		return buf[0];
	}

	public static void glDeleteBuffer(GL4 gl, int buffer)
	{
		buf[0] = buffer;
		gl.glDeleteBuffers(1, buf, 0);
	}

	public static int glGenTexture(GL4 gl)
	{
		gl.glGenTextures(1, buf, 0);
		return buf[0];
	}

	public static void glDeleteTexture(GL4 gl, int texture)
	{
		buf[0] = texture;
		gl.glDeleteTextures(1, buf, 0);
	}

	public static int glGenFrameBuffer(GL4 gl)
	{
		gl.glGenFramebuffers(1, buf, 0);
		return buf[0];
	}

	public static void glDeleteFrameBuffer(GL4 gl, int frameBuffer)
	{
		buf[0] = frameBuffer;
		gl.glDeleteFramebuffers(1, buf, 0);
	}

	public static int glGenRenderbuffer(GL4 gl)
	{
		gl.glGenRenderbuffers(1, buf, 0);
		return buf[0];
	}

	public static void glDeleteRenderbuffers(GL4 gl, int renderBuffer)
	{
		buf[0] = renderBuffer;
		gl.glDeleteRenderbuffers(1, buf, 0);
	}
}
