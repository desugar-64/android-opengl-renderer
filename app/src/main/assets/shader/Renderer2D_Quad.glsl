#type vertex
#version 310 es
precision mediump float;

layout(std140,binding=0)uniform Camera
{
    mat4 u_ViewProjection;
};

layout(location=0)in vec4 a_Position;
layout(location=1)in vec2 a_LocalPosition;
layout(location=2)in vec2 a_TexCoord;
layout(location=3)in vec4 a_Color;
layout(location=4)in float a_TexIndex;
layout(location=5)in float a_TilingFactor;
layout(location=6)in float a_FlipTexture;
layout(location=7)in float a_AspectRatio;
layout(location=8)in float a_StrokeWidth;
layout(location=9)in vec4 a_StrokeColor;
layout(location=10)in vec4 a_CornerRadius;

struct VertexOutput
{
    vec2 LocalPosition;
    vec4 Color;
    vec2 TexCoord;
    float TexIndex;
    float TilingFactor;
    float FlipTexture;
    vec4 CornersRadius;
    vec4 StrokeColor;
    float AspectRatio;
    float StrokeWidth;
};

layout(location=0)out VertexOutput Output;

void main(){
    vec4 pos = u_ViewProjection*a_Position;
    Output.LocalPosition=a_LocalPosition;
    Output.Color=a_Color;
    Output.TexCoord=a_TexCoord;
    Output.TexIndex=a_TexIndex;
    Output.TilingFactor=a_TilingFactor;
    Output.FlipTexture=a_FlipTexture;
    Output.CornersRadius=a_CornerRadius;
    Output.AspectRatio=a_AspectRatio;
    Output.StrokeWidth=a_StrokeWidth;
    Output.StrokeColor=a_StrokeColor;
    gl_Position=pos;
}

#type fragment
#version 310 es
//#extension GL_OES_standard_derivatives : enable
//#extension GL_ANDROID_extension_pack_es31a : require
precision mediump float;

struct VertexOutput
{
    vec2 LocalPosition;
    vec4 Color;
    vec2 TexCoord;
    float TexIndex;
    float TilingFactor;
    float FlipTexture;
    vec4 CornersRadius;
    vec4 StrokeColor;
    float AspectRatio;
    float StrokeWidth;
};

layout(location=0)in VertexOutput Input;
layout(location=0)out vec4 color;
layout(binding=0)uniform sampler2D u_Textures[8];

// b.x = width
// b.y = height
// r.x = roundness top-right  
// r.y = roundness boottom-right
// r.z = roundness top-left
// r.w = roundness bottom-left
float sdRoundBox(in vec2 p,in vec2 b,in vec4 r)
{
    r.xy=(p.x>0.)?r.xy:r.zw;
    r.x=(p.y>0.)?r.x:r.y;
    vec2 q=abs(p)-b+r.x;
    return min(max(q.x,q.y),0.)+length(max(q,0.))-r.x;
}

void main()
{
    vec4 baseColor=vec4(1.);
    bool flipTexture=int(Input.FlipTexture)>0;
    
    vec2 texCoord=flipTexture?vec2(Input.TexCoord.x,1.-Input.TexCoord.y):Input.TexCoord;
    // pre OpenGL ES 3.1 arrays of samplers may only be indexed by a constant integral expression
    switch(int(Input.TexIndex)){
        case 0:
        baseColor=texture(u_Textures[0],texCoord*Input.TilingFactor);break;
        case 1:
        baseColor=texture(u_Textures[1],texCoord*Input.TilingFactor);break;
        case 2:
        baseColor=texture(u_Textures[2],texCoord*Input.TilingFactor);break;
        case 3:
        baseColor=texture(u_Textures[3],texCoord*Input.TilingFactor);break;
        case 4:
        baseColor=texture(u_Textures[4],texCoord*Input.TilingFactor);break;
        case 5:
        baseColor=texture(u_Textures[5],texCoord*Input.TilingFactor);break;
        case 6:
        baseColor=texture(u_Textures[6],texCoord*Input.TilingFactor);break;
        case 7:
        baseColor=texture(u_Textures[7],texCoord*Input.TilingFactor);break;
    }
    
    vec2 p=Input.LocalPosition;
    float ratio=Input.AspectRatio;
    p.x*=ratio;
    float border=Input.StrokeWidth;
    vec2 size=vec2(1.*ratio,1.);
    vec4 cornerRadius=Input.CornersRadius;
    
    float d=sdRoundBox(p,size,cornerRadius)+border;

    vec4 bg=baseColor*Input.Color;
    vec4 borderCol=Input.StrokeColor;

    vec4 col=bg;
    float aa=fwidth(d);
    
    float boxEdge=1.-smoothstep(-aa,aa,d);
    float strokeEdge=1.-smoothstep(-aa,aa,d-border);
    
    vec4 strokeColor=mix(bg,borderCol,strokeEdge);
    //color.a *= boxEdge;
    vec4 finalColor=mix(strokeColor,col,boxEdge);
    strokeColor.a *= strokeEdge;
    finalColor.a *= strokeEdge;
    color=finalColor;
    //color=bg;
    //color*=sum*Input.Color;
    
    // OpenGL 3.1+
    //    color = texture(u_Textures[int(Input.TexIndex)], vec2(Input.TexCoord.x, 1.0 - Input.TexCoord.y)) * Input.Color;
    //    color=sum*Input.Color;
    //    color = vec4(Input.TilingFactor, Input.TilingFactor, Input.TilingFactor, 1.0);
}