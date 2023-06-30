#type vertex
#version 310 es
 precision mediump float;

layout (std140, binding = 0) uniform Camera
{
mat4 u_ViewProjection;
};

layout (location = 0) in vec3 a_Position;
layout (location = 1) in vec2 a_TexCoord;
layout (location = 2) in vec4 a_Color;
layout (location = 3) in float a_TexIndex;
layout (location = 4) in float a_TilingFactor;
layout (location = 5) in float a_FlipTexture;

out vec2 v_TexCoord;
out vec4 v_Color;
out float v_TexIndex;
out float v_TilingFactor;
out float v_FlipTexture;

void main() {
v_TexCoord = a_TexCoord;
v_Color = a_Color;
v_TexIndex = a_TexIndex;
v_TilingFactor = a_TilingFactor;
v_FlipTexture = a_FlipTexture;
gl_Position = u_ViewProjection * vec4(a_Position, 1.0);
}

#type fragment
    #version 310 es
//#extension GL_ANDROID_extension_pack_es31a : require
precision mediump float;

in vec4 v_Color;
in vec2 v_TexCoord;
in float v_TexIndex;
in float v_TilingFactor;
in float v_FlipTexture;

out vec4 color;

uniform sampler2D u_Textures[8];

void main() {
    vec4 sum = vec4(1.0);
    bool flipTexture = int(v_FlipTexture) > 0;
    vec2 texCoord = flipTexture ? vec2(v_TexCoord.x, 1.0 - v_TexCoord.y) : v_TexCoord;
    // pre OpenGL ES 3.1 arrays of samplers may only be indexed by a constant integral expression
    switch (int(v_TexIndex)) {
        case 0:
        sum = texture(u_Textures[0], texCoord * v_TilingFactor);
        break;
        case 1:
        sum = texture(u_Textures[1], texCoord * v_TilingFactor);
        break;
        case 2:
        sum = texture(u_Textures[2], texCoord * v_TilingFactor);
        break;
        case 3:
        sum = texture(u_Textures[3], texCoord * v_TilingFactor);
        break;
        case 4:
        sum = texture(u_Textures[4], texCoord * v_TilingFactor);
        break;
        case 5:
        sum = texture(u_Textures[5], texCoord * v_TilingFactor);
        break;
        case 6:
        sum = texture(u_Textures[6], texCoord * v_TilingFactor);
        break;
        case 7:
        sum = texture(u_Textures[7], texCoord * v_TilingFactor);
        break;
    }
    // OpenGL 3.1+
    //    color = texture(u_Textures[int(v_TexIndex)], vec2(v_TexCoord.x, 1.0 - v_TexCoord.y)) * v_Color;
    color = sum * v_Color;
    //    color = vec4(v_TilingFactor, v_TilingFactor, v_TilingFactor, 1.0);
}