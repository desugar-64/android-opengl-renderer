#type vertex
#version 300 es
precision mediump float;

layout (location = 0) in vec3 a_Position;
layout (location = 1) in vec4 a_Color;

uniform mat4 u_ViewProjection;
uniform mat4 u_Transform;
out vec4 v_Color;

void main() {
    v_Color = a_Color;
    gl_Position = u_ViewProjection * u_Transform * vec4(a_Position, 1.0);
}

    #type fragment
    #version 300 es
precision mediump float;

in vec4 v_Color;
out vec4 color;

void main() {
    color = v_Color;
}