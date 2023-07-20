package com.desugar.glucose.layers

import com.desugar.glucose.camera.OrthographicCamera
import com.desugar.glucose.renderer.Framebuffer
import com.desugar.glucose.renderer.RenderCommand
import com.desugar.glucose.renderer.Renderer2D

class RenderScope(
    val viewportWidth: Int,
    val viewportHeight: Int,
    val density: Float
) {
    val renderer2D = Renderer2D

    inline fun drawIntoFrameBuffer(framebuffer: Framebuffer, actions: () -> Unit) {
        framebuffer.bind()
        actions()
        framebuffer.unbind()
        RenderCommand.setViewPort(0, 0, viewportWidth, viewportHeight)
    }

    inline fun scene2D(orthographicCamera: OrthographicCamera, drawCommands: Renderer2D.() -> Unit) {
        renderer2D.beginScene(orthographicCamera)
        renderer2D.drawCommands()
        renderer2D.endScene()
    }

    fun Int.dpToPx() = this * density
    fun Float.dpToPx() = this * density
}