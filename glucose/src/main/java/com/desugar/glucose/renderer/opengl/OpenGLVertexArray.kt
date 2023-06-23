package com.desugar.glucose.renderer.opengl

import android.opengl.GLES31
import com.desugar.glucose.renderer.IndexBuffer
import com.desugar.glucose.renderer.ShaderDataType
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
                when (element.type) {
                    ShaderDataType.None -> TODO()
                    ShaderDataType.Float,
                    ShaderDataType.Float2,
                    ShaderDataType.Float3,
                    ShaderDataType.Float4 -> {
                        GLES31.glEnableVertexAttribArray(index)
                        GLES31.glVertexAttribPointer(
                            /* indx = */ index,
                            /* size = */ element.type.components,
                            /* type = */ element.type.openGLBaseType,
                            /* normalized = */ element.normalized,
                            /* stride = */ bufferLayout.stride,
                            /* offset = */ element.offset
                        )
                    }

                    ShaderDataType.Int,
                    ShaderDataType.Int2,
                    ShaderDataType.Int3,
                    ShaderDataType.Int4,
                    ShaderDataType.Bool -> {
                        GLES31.glEnableVertexAttribArray(index)
                        GLES31.glVertexAttribIPointer(
                            /* index = */ index,
                            /* size = */ element.type.components,
                            /* type = */ element.type.openGLBaseType,
                            /* stride = */ bufferLayout.stride,
                            /* offset = */ element.offset
                        )
                    }

                    ShaderDataType.Mat3,
                    ShaderDataType.Mat4 -> {
                        val count = element.type.components
                        for (i in 0 until count) {
                            GLES31.glEnableVertexAttribArray(index)
                            GLES31.glVertexAttribPointer(
                                /* indx = */ index,
                                /* size = */ count,
                                /* type = */ element.type.openGLBaseType,
                                /* normalized = */ element.normalized,
                                /* stride = */ bufferLayout.stride,
                                /* offset = */ (element.offset + Float.SIZE_BYTES * count * i)
                            )
                            GLES31.glVertexAttribDivisor(index, 1)
                        }
                    }
                }

            }
        }

        _vertexBuffers.add(vertexBuffer)
    }

}