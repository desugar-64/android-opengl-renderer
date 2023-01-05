package com.desugar.glucose

import android.opengl.GLES31
import android.opengl.GLSurfaceView
import android.util.Log
import com.desugar.glucose.events.WindowResizeEvent
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GlucoseGLRenderer(
    private val graphicsApp: GraphicsApp
) : GLSurfaceView.Renderer {


    override fun onSurfaceCreated(gl: GL10, config: EGLConfig?) {
        Log.d(TAG, "onSurfaceCreated: ")
        val viewport = IntArray(4)
        GLES31.glGetIntegerv(GLES31.GL_VIEWPORT, viewport, 0)
        val (_, _, width, height) = viewport
        graphicsApp.onCreate(width, height)
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        Log.d(TAG, "onSurfaceChanged: ")
        graphicsApp.onEvent(WindowResizeEvent(width, height))
    }

    // The render loop
    override fun onDrawFrame(gl: GL10) {
        Log.d(TAG, "onDrawFrame: ")
        graphicsApp.run()
    }

    private companion object {
        private const val TAG = "GlucoseRenderer"
    }
}