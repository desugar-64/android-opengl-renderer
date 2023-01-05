package com.desugar.glucose.renderer.opengl

import com.desugar.glucose.renderer.GraphicsContext

class OpenGLContext : GraphicsContext() {
    override fun init() {
    }

    override fun swapBuffers() {
        // no-op
    }

    companion object {
        private const val TAG = "OpenGLContext"
    }
}