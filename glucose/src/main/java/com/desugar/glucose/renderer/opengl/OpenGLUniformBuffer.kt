package com.desugar.glucose.renderer.opengl

import android.opengl.GLES31
import com.desugar.glucose.renderer.UniformBuffer
import com.desugar.glucose.renderer.toFloatBuffer

class OpenGLUniformBuffer(
    count: Int, binding: Int
) : UniformBuffer {

    private var rendererId: Int = 0

    override val count: Int = count
    override val sizeBytes: Int
        get() = count * Float.SIZE_BYTES

    init {
        val ids = IntArray(1)
        GLES31.glGenBuffers(1, ids, 0)
        rendererId = ids[0]
        bind()
        GLES31.glBufferData(
            /* target = */ GLES31.GL_UNIFORM_BUFFER,
            /* size = */ sizeBytes,
            /* data = */ null,
            /* usage = */ GLES31.GL_DYNAMIC_DRAW
        )
        GLES31.glBindBufferBase(
            /* target = */ GLES31.GL_UNIFORM_BUFFER,
            /* index = */ binding,
            /* buffer = */ rendererId
        );
    }

    override fun setData(data: FloatArray) {
        bind()
        GLES31.glBufferSubData(
            /* target = */ GLES31.GL_UNIFORM_BUFFER,
            /* offset = */ 0,
            /* size = */ data.size * Float.SIZE_BYTES,
            /* data = */ data.toFloatBuffer()
        )
    }

    override fun bind() {
        GLES31.glBindBuffer(GLES31.GL_UNIFORM_BUFFER, rendererId)
    }

    override fun unbind() {
        GLES31.glBindBuffer(GLES31.GL_UNIFORM_BUFFER, 0)
    }

    override fun destroy() {
        GLES31.glDeleteBuffers(1, intArrayOf(rendererId), 0)
    }

}