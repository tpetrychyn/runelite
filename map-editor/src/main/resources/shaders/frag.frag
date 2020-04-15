
#version 450

#include semantic.glsl

// Incoming interpolated (between vertices) color.
//layout (location = BLOCK) in Block
//{
//    vec3 interpolatedColor;
//};

uniform vec4 u_Color;

in vec4 fragColor;

// Outgoing final color.
layout (location = FRAG_COLOR) out vec4 color;


void main()
{
    // We simply pad the interpolatedColor
//    outputColor = vec4(interpolatedColor, 1);
    color = vec4(gl_FragCoord.z, gl_FragCoord.z, gl_FragCoord.z, 1);//u_Color * fragColor;
}