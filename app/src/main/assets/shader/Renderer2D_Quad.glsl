#type vertex
#version 310 es
precision mediump float;

layout(std140,binding=0)uniform Camera
{
    mat4 u_ViewProjection;
};

layout(location=0)in vec4 a_Position;
layout(location=1)in vec2 a_TexCoord;
layout(location=2)in vec4 a_Color;
layout(location=3)in float a_TexIndex;
layout(location=4)in float a_TilingFactor;
layout(location=5)in float a_FlipTexture;

struct VertexOutput
{
    vec4 Color;
    vec2 TexCoord;
    float TexIndex;
    float TilingFactor;
    float FlipTexture;
};

layout(location=0)out VertexOutput Output;

void main(){
    Output.Color=a_Color;
    Output.TexCoord=a_TexCoord;
    Output.TexIndex=a_TexIndex;
    Output.TilingFactor=a_TilingFactor;
    Output.FlipTexture=a_FlipTexture;
    gl_Position=u_ViewProjection*a_Position;
}

#type fragment
#version 310 es
//#extension GL_ANDROID_extension_pack_es31a : require
precision mediump float;

struct VertexOutput
{
    vec4 Color;
    vec2 TexCoord;
    float TexIndex;
    float TilingFactor;
    float FlipTexture;
};

layout(location=0)in VertexOutput Input;
layout(location=0)out vec4 color;
layout(binding=0)uniform sampler2D u_Textures[8];

void main(){
    vec4 sum=vec4(1.);
    bool flipTexture=int(Input.FlipTexture)>0;
    vec2 texCoord=flipTexture?vec2(Input.TexCoord.x,1.-Input.TexCoord.y):Input.TexCoord;
    // pre OpenGL ES 3.1 arrays of samplers may only be indexed by a constant integral expression
    switch(int(Input.TexIndex)){
        case 0:
        sum=texture(u_Textures[0],texCoord*Input.TilingFactor);break;
        case 1:
        sum=texture(u_Textures[1],texCoord*Input.TilingFactor);break;
        case 2:
        sum=texture(u_Textures[2],texCoord*Input.TilingFactor);break;
        case 3:
        sum=texture(u_Textures[3],texCoord*Input.TilingFactor);break;
        case 4:
        sum=texture(u_Textures[4],texCoord*Input.TilingFactor);break;
        case 5:
        sum=texture(u_Textures[5],texCoord*Input.TilingFactor);break;
        case 6:
        sum=texture(u_Textures[6],texCoord*Input.TilingFactor);break;
        case 7:
        sum=texture(u_Textures[7],texCoord*Input.TilingFactor);break;
    }
    // OpenGL 3.1+
    //    color = texture(u_Textures[int(Input.TexIndex)], vec2(Input.TexCoord.x, 1.0 - Input.TexCoord.y)) * Input.Color;
    color=sum*Input.Color;
    //    color = vec4(Input.TilingFactor, Input.TilingFactor, Input.TilingFactor, 1.0);
}