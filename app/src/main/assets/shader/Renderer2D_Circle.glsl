#type vertex
#version 310 es
 precision mediump float;

layout (std140, binding = 0) uniform Camera
{
mat4 u_ViewProjection;
};

layout (location = 0) in vec3 a_WorldPosition;
layout (location = 1) in vec2 a_LocalPosition;
layout (location = 2) in vec4 a_Color;
layout (location = 3) in float a_Thickness;
layout (location = 4) in float a_Fade;
//layout (location = 5) in float a_FlipTexture;

struct VertexOutput
{
vec2 LocalPosition;
vec4 Color;
float Thickness;
float Fade;
//    float FlipTexture;
};

layout (location = 0) out VertexOutput Output;

void main() {
Output.LocalPosition = a_LocalPosition;
Output.Color = a_Color;
Output.Thickness = a_Thickness;
Output.Fade = a_Fade;
gl_Position = u_ViewProjection * vec4(a_WorldPosition, 1.0);
}

#type fragment
#version 310 es
//#extension GL_ANDROID_extension_pack_es31a : require
precision mediump float;

struct VertexOutput
{
vec2 LocalPosition;
vec4 Color;
float Thickness;
float Fade;
//    float FlipTexture;
};

layout (location = 0) in VertexOutput Input;
layout (location = 0) out vec4 color;
//layout (binding = 0) uniform sampler2D u_Textures[8];

float sdfCircle(vec2 localPosition, float thickness, float fade) {
float distance = 1.0 - length(localPosition);
float circle = smoothstep(0.0, Input.Fade, distance);
circle *= smoothstep(thickness + fade, thickness, distance);
return circle;
}

void main() {
float circle = sdfCircle(Input.LocalPosition, Input.Thickness, Input.Fade);
if (circle == 0.0)
discard;

color = Input.Color;
color.a *= circle;
//        color = vec4(circle, circle, circle, 1.0);
}