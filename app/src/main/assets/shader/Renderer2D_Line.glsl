#type vertex
#version 310 es
precision mediump float;

layout(std140,binding=0)uniform Camera
{
    mat4 u_ViewProjection;
};

layout(location=0)in vec3 a_WorldPosition;
layout(location=1)in vec2 a_LocalPosition;
layout(location=2)in vec2 a_Start;
layout(location=3)in vec2 a_End;
layout(location=4)in vec4 a_Color;
layout(location=5)in float a_Thickness;

struct VertexOutput
{
    vec2 LocalPosition;
    vec2 P0;
    vec2 P1;
    vec4 Color;
    float Thickness;
};

layout(location=0)out VertexOutput Output;

void main(){
    Output.LocalPosition=a_LocalPosition;
    Output.P0=a_Start;
    Output.P1=a_End;
    Output.Color=a_Color;
    Output.Thickness=a_Thickness;
    gl_Position=u_ViewProjection*vec4(a_WorldPosition,1.);
}

#type fragment
#version 310 es
//#extension GL_ANDROID_extension_pack_es31a : require
precision mediump float;
const float gamma = 2.2;
struct VertexOutput
{
    vec2 LocalPosition;
    vec2 P0;
    vec2 P1;
    vec4 Color;
    float Thickness;
};

layout(location=0)in VertexOutput Input;
layout(location=0)out vec4 color;

float udSegment(in vec2 p,in vec2 a,in vec2 b)
{
    vec2 ba=b-a;
    vec2 pa=p-a;
    float h=clamp(dot(pa,ba)/dot(ba,ba),0.,1.);
    return length(pa-h*ba);
}

void main()
{
    vec2 uv=Input.LocalPosition;
    float dist=udSegment(uv,Input.P0,Input.P1);
    float aa=fwidth(dist);
    float lineEdge=smoothstep(-aa,aa,1.-dist);
    vec4 col=Input.Color;
    col.a*=lineEdge;
    
    color=vec4(pow(col.rgb, vec3(1.0 / gamma)), col.a);
}