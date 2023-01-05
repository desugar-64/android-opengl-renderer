#version 300 es
precision highp float;

uniform int iTime;
uniform vec2 touchCoord;

out vec4 FragColor;
const float dist = 400.0f;
const float radius = 300.0f;
const float threshold = 0.01;

void main()
{
    float dist = distance(touchCoord, gl_FragCoord.xy);

    if (dist > radius) discard;

    float d = dist / radius;
    vec3 color = mix(vec3(1.0f, 0.0f, 0.0f), vec3(0.0), step(1.0-threshold, d));

    FragColor = vec4(color, 1.0);
}