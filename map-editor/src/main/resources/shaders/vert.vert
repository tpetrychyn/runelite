#version 450

#include semantic.glsl
#include hsl_to_rgb.glsl

//// Incoming vertex position, Model Space.
layout (location = POSITION) in vec4 position;
//
// Incoming vertex color.
layout (location = COLOR) in int color;

// Projection and view matrices.
layout (binding = TRANSFORM0) uniform Transform0
{
    mat4 proj;
    mat4 view;
};

// model matrix
layout (binding = TRANSFORM1) uniform Transform1
{
    mat4 model;
};

out vec4 fragColor;

void main() {
    int hsl = color & 0xffff;
    float a = float(color >> 24 & 0xff) / 255.f;
    vec3 rgb = hslToRgb(hsl);

    // Normally gl_Position is in Clip Space and we calculate it by multiplying together all the matrices
    gl_Position = proj * (view * (position));

    fragColor = vec4(rgb, 1.f - a);
}