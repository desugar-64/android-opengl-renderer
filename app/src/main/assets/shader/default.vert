#version 300 es
precision mediump float;
layout (location = 0) in vec3 vPosition;
// Imports the camera matrix from the main function
uniform mat4 camMatrix;
// Imports the model matrix from the main function
uniform mat4 model;

void main()
{
    // calculates current position
    vec3 crntPos = vec3(model * vec4(vPosition, 1.0f));
    // Outputs the positions/coordinates of all vertices
    gl_Position = vec4(vPosition, 1.0f);
}