package com.desugar.glucose.renderer

import com.desugar.glucose.renderer.opengl.OpenGLRendererAPI
import dev.romainguy.kotlin.math.Float4

object RenderCommand {
    private val rendererAPI: RendererAPI = OpenGLRendererAPI()

    fun init() {
        rendererAPI.init()
    }

    fun setClearColor(color: Float4) {
        rendererAPI.setClearColor(color)
    }

    fun clear() {
        rendererAPI.clear()
    }

    fun drawIndexed(vertexArray: VertexArray, indexCount: Int = 0) {
        rendererAPI.drawIndexed(vertexArray, indexCount)
    }

    fun setViewPort(x: Int, y: Int, width: Int, height: Int) {
        rendererAPI.setViewPort(x, y, width, height)
    }

    fun disableDepthTest() {
        rendererAPI.disableDepthTest()
    }
}