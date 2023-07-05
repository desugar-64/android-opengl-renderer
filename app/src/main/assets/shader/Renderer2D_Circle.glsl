#type vertex
#version 310 es
 precision mediump float;

layout (std140, binding = 0) uniform Camera
{
mat4 u_ViewProjection;
};

layout (location = 0) in vec3 a_WorldPosition;
layout (location = 1) in vec2 a_LocalPosition;
layout (location = 2) in vec4 a_FillColor;
layout (location = 3) in vec4 a_StrokeColor;
layout (location = 4) in float a_StrokeWidth;
//layout (location = 5) in float a_FlipTexture;

struct VertexOutput
{
vec2 LocalPosition;
vec4 FillColor;
vec4 StrokeColor;
float StrokeWidth;
};

layout (location = 0) out VertexOutput Output;

void main() {
Output.LocalPosition = a_LocalPosition;
Output.FillColor = a_FillColor;
Output.StrokeColor = a_StrokeColor;
Output.StrokeWidth = a_StrokeWidth;
gl_Position = u_ViewProjection * vec4(a_WorldPosition, 1.0);
}

#type fragment
#version 310 es
//#extension GL_ANDROID_extension_pack_es31a : require
precision mediump float;

struct VertexOutput
{
vec2 LocalPosition;
vec4 FillColor;
vec4 StrokeColor;
float StrokeWidth;
};

layout (location = 0) in VertexOutput Input;
layout (location = 0) out vec4 color;
//layout (binding = 0) uniform sampler2D u_Textures[8];

float sdfCircle(vec2 localPosition, float thickness, float fade) {
float distance = 1.0 - length(localPosition);
float circle = smoothstep(0.0, fade, distance);
circle *= smoothstep(thickness + fade, thickness, distance);
return circle;
}

vec4 sdfCircle2(vec2 localPosition, vec4 fillColor, vec4 strokeColor, float strokeWidth) {
float radius = 1.0;

float dist = length(localPosition);
float aa = fwidth(dist);

// Create the SDF
float circleSDF = radius - dist;

// Calculate the smooth edges of the stroke
float strokeEdge = smoothstep(aa, - aa, circleSDF - strokeWidth);

// Calculate the smooth edges of the circle
float circleEdge = smoothstep(aa, - aa, circleSDF);

// Calculate the color of the pixel
vec4 c = mix(fillColor, strokeColor, strokeEdge);

// Apply the circle edge to the alpha
c.a *= 1.0 - circleEdge;
return c;
}

void main() {
vec4 circle = sdfCircle2(Input.LocalPosition, Input.FillColor, Input.StrokeColor, Input.StrokeWidth);
if (circle.a == 0.0)
discard;

// color = Input.Color;
// color.a *= circle;
color = circle;
}