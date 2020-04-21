package renderer;

import com.google.common.annotations.VisibleForTesting;
import com.jogamp.opengl.GL4;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import renderer.helpers.GLUtil;

import java.util.ArrayList;
import java.util.List;

import static renderer.helpers.GLUtil.glGetProgramInfoLog;

public class Shader
{
	public static final String LINUX_VERSION_HEADER =
			"#version 420\n" +
					"#extension GL_ARB_compute_shader : require\n" +
					"#extension GL_ARB_shader_storage_buffer_object : require\n" +
					"#extension GL_ARB_explicit_attrib_location : require\n";
	static final String WINDOWS_VERSION_HEADER = "#version 430\n";

	static final Shader PROGRAM = new Shader()
			.add(GL4.GL_VERTEX_SHADER, "/gpu/vert.glsl")
			.add(GL4.GL_GEOMETRY_SHADER, "/gpu/geom.glsl")
//			.add(GL4.GL_FRAGMENT_SHADER, "/gpu/picker.frag");
			.add(GL4.GL_FRAGMENT_SHADER, "/gpu/frag.glsl");

	static final Shader COMPUTE_PROGRAM = new Shader()
			.add(GL4.GL_COMPUTE_SHADER, "/gpu/comp.glsl");

	static final Shader SMALL_COMPUTE_PROGRAM = new Shader()
			.add(GL4.GL_COMPUTE_SHADER, "/gpu/comp_small.glsl");

	static final Shader UNORDERED_COMPUTE_PROGRAM = new Shader()
			.add(GL4.GL_COMPUTE_SHADER, "/gpu/comp_unordered.glsl");

	@VisibleForTesting
	final List<Unit> units = new ArrayList<>();

	@RequiredArgsConstructor
	@VisibleForTesting
	static class Unit
	{
		@Getter
		private final int type;

		@Getter
		private final String filename;
	}

	public Shader()
	{
	}

	public Shader add(int type, String name)
	{
		units.add(new Unit(type, name));
		return this;
	}

	public int compile(GL4 gl, Template template) throws ShaderException
	{
		int program = gl.glCreateProgram();
		int[] shaders = new int[units.size()];
		int i = 0;
		boolean ok = false;
		try
		{
			while (i < shaders.length)
			{
				Unit unit = units.get(i);
				int shader = gl.glCreateShader(unit.type);
				String source = template.load(unit.filename);
				gl.glShaderSource(shader, 1, new String[]{source}, null);
				gl.glCompileShader(shader);

				if (GLUtil.glGetShader(gl, shader, gl.GL_COMPILE_STATUS) != gl.GL_TRUE)
				{
					String err = GLUtil.glGetShaderInfoLog(gl, shader);
					gl.glDeleteShader(shader);
					throw new ShaderException(err);
				}
				gl.glAttachShader(program, shader);
				shaders[i++] = shader;
			}

			gl.glLinkProgram(program);

			if (GLUtil.glGetProgram(gl, program, gl.GL_LINK_STATUS) == gl.GL_FALSE)
			{
				String err = glGetProgramInfoLog(gl, program);
				throw new ShaderException(err);
			}

			gl.glValidateProgram(program);

			if (GLUtil.glGetProgram(gl, program, gl.GL_VALIDATE_STATUS) == gl.GL_FALSE)
			{
				String err = glGetProgramInfoLog(gl, program);
				throw new ShaderException(err);
			}

			ok = true;
		}
		finally
		{
			while (i > 0)
			{
				int shader = shaders[--i];
				gl.glDetachShader(program, shader);
				gl.glDeleteShader(shader);
			}

			if (!ok)
			{
				gl.glDeleteProgram(program);
			}
		}

		return program;
	}
}
