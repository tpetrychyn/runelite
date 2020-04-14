package renderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class GpuIntBuffer
{
	private IntBuffer buffer = allocateDirect(65536);

	public GpuIntBuffer() {}

	public GpuIntBuffer(int size) {
		this.buffer = allocateDirect(size);
	}

	void put(int x, int y, int z)
	{
		buffer.put(x).put(y).put(z);
	}

	void put(int x, int y, int z, int c)
	{
		buffer.put(x).put(y).put(z).put(c);
	}

	void putAt(int offset, int x, int y, int z, int c) {
		buffer.put(offset, x).put(offset+1, y).put(offset+2, z).put(offset+3, c);
	}

	void flip()
	{
		buffer.flip();
	}

	public void clear()
	{
		buffer.clear();
	}

	void ensureCapacity(int size)
	{
		while (buffer.remaining() < size)
		{
			IntBuffer newB = allocateDirect(buffer.capacity() * 2);
			buffer.flip();
			newB.put(buffer);
			buffer = newB;
		}
	}

	IntBuffer getBuffer()
	{
		return buffer;
	}

	static IntBuffer allocateDirect(int size)
	{
		return ByteBuffer.allocateDirect(size * Integer.BYTES)
			.order(ByteOrder.nativeOrder())
			.asIntBuffer();
	}
}
