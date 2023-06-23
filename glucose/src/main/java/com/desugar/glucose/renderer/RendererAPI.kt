package com.desugar.glucose.renderer

import dev.romainguy.kotlin.math.Float4

interface RendererAPI {
    enum class API { NONE, OPENGL }

    fun init()
    fun setClearColor(color: Float4)
    fun clear()
    fun drawIndexed(vertexArray: VertexArray, indexCount: Int = 0)
    fun setViewPort(x: Int, y: Int, width: Int, height: Int)
    fun disableDepthTest()
    fun drawLines(vertexArray: VertexArray, vertexCount: Int)
    fun setLineWidth(width: Float)

    companion object {
        internal val api: API = API.OPENGL
    }
}