package com.desugar.glucose.renderer

import com.desugar.glucose.renderer.opengl.OpenGLFramebuffer

enum class FramebufferTextureFormat
{
    None,
    // Color
    RGBA8,
    RED_INTEGER,

    // Depth/stencil
    DEPTH24STENCIL8,
}

data class FramebufferTextureSpecification(
    val format: FramebufferTextureFormat = FramebufferTextureFormat.None
)

data class FramebufferAttachmentSpecification(
    val attachments: List<FramebufferTextureSpecification> = listOf(
        FramebufferTextureSpecification(format = FramebufferTextureFormat.RGBA8),
        FramebufferTextureSpecification(format = FramebufferTextureFormat.DEPTH24STENCIL8)
    )
)

data class FramebufferSpecification(
    var width: Int,
    var height: Int,
    val attachmentsSpec: FramebufferAttachmentSpecification,
    val downSampleFactor: Int = 1,
    val sampling: Int = 1, // Anti-Aliasing, multisample textures
    val swapChainTarget: Boolean = false
)

interface Framebuffer {
    val specification: FramebufferSpecification
    val colorAttachmentTexture: Texture2D

    fun invalidate()
    fun bind()
    fun unbind()
    fun resize(width: Int, height: Int)

    fun readPixel(attachmentIndex: Int, x: Int, y: Int): Int
    fun clearAttachment(attachmentIndex: Int, value: Int)
    fun getColorAttachmentRendererID(index: Int = 0): Int

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