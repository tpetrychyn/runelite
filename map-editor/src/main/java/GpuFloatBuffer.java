import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class GpuFloatBuffer
{
	private FloatBuffer buffer = allocateDirect(65536);

	void put(float texture, float u, float v, float pad)
	{
		buffer.put(texture).put(u).put(v).put(pad);
	}

	void flip()
	{
		buffer.flip();
	}

	void clear()
	{
		buffer.clear();
	}

	void ensureCapacity(int size)
	{
		while (buffer.remaining() < size)
		{
			FloatBuffer newB = allocateDirect(buffer.capacity() * 2);
			buffer.flip();
			newB.put(buffer);
			buffer = newB;
		}
	}

	FloatBuffer getBuffer()
	{
		return buffer;
	}

	static FloatBuffer allocateDirect(int size)
	{
		return ByteBuffer.allocateDirect(size * Float.BYTES)
			.order(ByteOrder.nativeOrder())
			.asFloatBuffer();
	}
}
