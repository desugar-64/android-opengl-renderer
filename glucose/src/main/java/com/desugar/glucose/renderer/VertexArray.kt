package com.desugar.glucose.renderer

import com.desugar.glucose.renderer.opengl.OpenGLVertexArray


interface VertexArray {

    fun bind()
    fun unbind()
    fun destroy()

    fun addVertexBuffer(vertexBuffer: VertexBuffer)
    var indexBuffer: IndexBuffer?

    companion object {
        fun create(): VertexArray {
            return when (RendererAPI.api) {
                RendererAPI.API.NONE -> error("Select renderer API")
                RendererAPI.API.OPENGL -> OpenGLVertexArray()
            }
        }
    }
}