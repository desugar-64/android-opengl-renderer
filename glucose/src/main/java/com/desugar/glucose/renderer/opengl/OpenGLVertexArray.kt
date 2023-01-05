package com.desugar.glucose.renderer.opengl

import android.opengl.GLES31
import com.desugar.glucose.renderer.IndexBuffer
import com.desugar.glucose.renderer.VertexArray
import com.desugar.glucose.renderer.VertexBuffer

class OpenGLVertexArray : VertexArray {
    private var rendererId: Int = 0

    private val _vertexBuffers: MutableList<VertexBuffer> = ArrayList()
    val vertexBuffers: List<VertexBuffer> get() = _vertexBuffers

    override var indexBuffer: IndexBuffer? = null
        set(value) {
            field = value
            GLES31.glBindVertexArray(rendererId)
            value?.bind()
        }

    init {
        val ids = IntArray(1)
        GLES31.glGenVertexArrays(1, ids, 0)
        rendererId = ids[0]
    }

    override fun bind() {
        GLES31.glBindVertexArray(rendererId)
    }

    override fun unbind() {
        GLES31.glBindVertexArray(0)
    }

    override fun destroy() {
        GLES31.glDeleteVertexArrays(1, intArrayOf(rendererId), 0)
    }

    override fun addVertexBuffer(vertexBuffer: VertexBuffer) {
        GLES31.glBindVertexArray(rendererId)
        vertexBuffer.bind()
        vertexBuffer.layout?.let { bufferLayout ->
            bufferLayout.forEachIndexed { index, element ->
                GLES31.glEnableVertexAttribArray(index)
                GLES31.glVertexAttribPointer(
                    index,
                    element.type.components,
                    element.type.openGLBaseType,
                    element.normalized,
                    bufferLayout.stride,
                    element.offset
                )
            }
        }

        _vertexBuffers.add(vertexBuffer)
    }

}