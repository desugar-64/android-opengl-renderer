package com.desugar.glucose.renderer

import com.desugar.glucose.renderer.opengl.OpenGLFramebuffer

data class FramebufferSpecification(
    var width: Int,
    var height: Int,
    val samples: Int = 1,
    val swapChainTarget: Boolean = false
)

interface Framebuffer {
    val specification: FramebufferSpecification
    val colorAttachmentTexture: Texture2D

    fun invalidate()
    fun bind()
    fun unbind()
    fun resize(width: Int, height: Int)

    fun destroy()

    companion object {
        fun create(spec: FramebufferSpecification): Framebuffer {
            return when (Renderer.api) {
                RendererAPI.API.NONE -> error("Select rendering API.")
                RendererAPI.API.OPENGL -> OpenGLFramebuffer(spec)
            }
        }
    }
}