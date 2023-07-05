package com.desugar.glucose.renderer.opengl

import android.opengl.GLES31
import android.util.Log
import com.desugar.glucose.renderer.RendererAPI
import com.desugar.glucose.renderer.VertexArray
import dev.romainguy.kotlin.math.Float4

class OpenGLRendererAPI : RendererAPI {

    override fun init() {
        // LOG
        Log.d(TAG, "vendor: " + GLES31.glGetString(GLES31.GL_VENDOR))
        Log.d(TAG, "renderer: " + GLES31.glGetString(GLES31.GL_RENDERER))
        Log.d(TAG, "version: " + GLES31.glGetString(GLES31.GL_VERSION))
        GLES31.glEnable(GLES31.GL_BLEND)
        GLES31.glBlendFunc(GLES31.GL_SRC_ALPHA, GLES31.GL_ONE_MINUS_SRC_ALPHA)
        GLES31.glEnable(GLES31.GL_DEPTH_TEST)
    }

    override fun setClearColor(color: Float4) {
        GLES31.glClearColor(color.r, color.g, color.b, color.a)
    }

    override fun clear() {
        GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT or GLES31.GL_DEPTH_BUFFER_BIT)
    }

    override fun drawIndexed(vertexArray: VertexArray, indexCount: Int) {
        vertexArray.bind()
        GLES31.glDrawElements(
            /* mode = */ GLES31.GL_TRIANGLES,
            /* count = */
            if (indexCount == 0) requireNotNull(vertexArray.indexBuffer).count else indexCount,
            /* type = */
            GLES31.GL_UNSIGNED_INT,
            /* offset = */
            0
        )
    }

    override fun setViewPort(x: Int, y: Int, width: Int, height: Int) {
        GLES31.glViewport(x, y, width, height)
    }

    override fun disableDepthTest() {
        GLES31.glDisable(GLES31.GL_DEPTH_TEST)
    }

    override fun drawLines(vertexArray: VertexArray, vertexCount: Int) {
        TODO("Not yet implemented")
    }

    override fun setLineWidth(width: Float) {
        TODO("Not yet implemented")
    }

    companion object {
        private const val TAG = "OpenGLRendererAPI"
    }
}