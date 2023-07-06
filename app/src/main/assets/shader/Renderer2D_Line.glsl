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
layout(location=4)in vec4 a_StrokeColor;
layout(location=5)in float a_StrokeWidth;

struct VertexOutput
{
    vec2 LocalPosition;
    vec2 Start;
    vec2 End;
    vec4 StrokeColor;
    float StrokeWidth;
};

layout(location=0)out VertexOutput Output;

void main(){
    Output.LocalPosition=a_LocalPosition;
    Output.Start=a_Start;
    Output.End=a_End;
    Output.StrokeColor=a_StrokeColor;
    Output.StrokeWidth=a_StrokeWidth;
    gl_Position=u_ViewProjection*vec4(a_WorldPosition,1.);
}

#type fragment
#version 310 es
//#extension GL_ANDROID_extension_pack_es31a : require
precision mediump float;

struct VertexOutput
{
    vec2 LocalPosition;
    vec2 Start;
    vec2 End;
    vec4 StrokeColor;
    float StrokeWidth;
};

layout(location=0)in VertexOutput Input;
layout(location=0)out vec4 color;

float lineSDF(vec2 pos,vec2 a,vec2 b){
    vec2 pa=pos-a,ba=b-a;
    float h=clamp(dot(pa,ba)/dot(ba,ba),0.,1.);
    return length(pa-ba*h);
}

void main(){
    // Normalize the coordinates to keep aspect ratio into account
    vec2 uv=Input.LocalPosition;
    
    // Calculate the distance to the line segment
    float dist=lineSDF(uv,Input.Start,Input.End);
    
    // Normalize the distance value to the line width
    float lineDist=dist/Input.StrokeWidth;
    float aa = fwidth(dist);
    // Calculate the line edge
    float lineEdge=smoothstep(aa,-aa,1.-lineDist);
    
    // Output the color
    vec4 c = vec4(Input.StrokeColor.rgb,lineEdge);
    c.a *= Input.StrokeColor.a;
    color=c;
}