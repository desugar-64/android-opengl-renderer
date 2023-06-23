package com.desugar.glucose.renderer.opengl

import android.opengl.GLES31
import com.desugar.glucose.renderer.*

//<editor-fold desc="Vertex Buffer">
class OpenGLVertexBuffer : VertexBuffer {

    override val count: Int
    override val sizeBytes: Int
    override var layout: BufferLayout? = null
    private var rendererId: Int = 0
    private var isDestroyed: Boolean = false

    constructor(count: Int) {
        this.count = count
        this.sizeBytes = this.count * Float.SIZE_BYTES
        createVertexBuffer(null, GLES31.GL_DYNAMIC_DRAW)
    }

    constructor(vertices: FloatArray) {
        this.count = vertices.size
        this.sizeBytes = count * Float.SIZE_BYTES
        createVertexBuffer(vertices, GLES31.GL_STATIC_DRAW)
    }

    private fun createVertexBuffer(vertices: FloatArray?, usage: Int) {
        val ids = IntArray(1)
        GLES31.glGenBuffers(1, ids, 0)
        rendererId = ids[0]
        GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, rendererId)
        GLES31.glBufferData(
            /* target = */ GLES31.GL_ARRAY_BUFFER,
            /* size = */ sizeBytes,
            /* data = */ vertices?.toFloatBuffer(),
            /* usage = */ usage
        )
    }

    override fun bind() {
        if (isDestroyed) {
            error("Can't bind. The buffer has been destroyed.")
        }
        GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, rendererId)
    }

    override fun unbind() {
        if (!isDestroyed) {
            GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, 0)
        }
    }

    override fun setData(data: FloatArray) {
        bind()
        val sizeBytes = data.size * Float.SIZE_BYTES
        GLES31.glBufferSubData(GLES31.GL_ARRAY_BUFFER, 0, sizeBytes, data.toFloatBuffer())
    }

    override fun destroy() {
        unbind()
        GLES31.glDeleteBuffers(1, intArrayOf(rendererId), 0)
        isDestroyed = true
    }
}
//</editor-fold>

//<editor-fold desc="Index Buffer">
class OpenGLIndexBuffer(indices: IntArray) : IndexBuffer {
    override val count: Int = indices.size
    override val sizeBytes: Int = count * Int.SIZE_BYTES
    private var rendererId: Int = 0
    private var isDestroyed: Boolean = false

    init {
        val ids = IntArray(1)
        GLES31.glGenBuffers(1, ids, 0)
        rendererId = ids[0]
        GLES31.glBindBuffer(GLES31.GL_ELEMENT_ARRAY_BUFFER, rendererId)

        GLES31.glBufferData(
            /* target = */ GLES31.GL_ELEMENT_ARRAY_BUFFER,
            /* size = */ sizeBytes,
            /* data = */ indices.toIntBuffer(),
            /* usage = */ GLES31.GL_STATIC_DRAW
        )
    }

    override fun bind() {
        if (isDestroyed) {
            error("Can't bind. The buffer has been destroyed.")
        }
        GLES31.glBindBuffer(GLES31.GL_ELEMENT_ARRAY_BUFFER, rendererId)
    }

    override fun unbind() {
        if (!isDestroyed) {
            GLES31.glBindBuffer(GLES31.GL_ELEMENT_ARRAY_BUFFER, 0)
        }
    }

    override fun destroy() {
        unbind()
        GLES31.glDeleteBuffers(1, intArrayOf(rendererId), 0)
        isDestroyed = true
    }
}
//</editor-fold>