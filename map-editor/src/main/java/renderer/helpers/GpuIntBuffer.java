package renderer.helpers;

import com.jogamp.opengl.util.GLBuffers;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class GpuIntBuffer
{
	private IntBuffer buffer = allocateDirect(65536);

	void put(int x, int y, int z)
	{
		buffer.put(x).put(y).put(z);
	}

	public void put(int x, int y, int z, int c)
	{
		buffer.put(x).put(y).put(z).put(c);
	}

	public void flip()
	{
		buffer.flip();
	}

	public void clear()
	{
		buffer.clear();
	}

	public void ensureCapacity(int size)
	{
		while (buffer.remaining() < size)
		{
			IntBuffer newB = allocateDirect(buffer.capacity() * 2);
			buffer.flip();
			newB.put(buffer);
			buffer = newB;
		}
	}

	public IntBuffer getBuffer()
	{
		return buffer;
	}

	public static IntBuffer allocateDirect(int size)
	{
		return GLBuffers.newDirectIntBuffer(size * GLBuffers.SIZEOF_INT);
	}
}
