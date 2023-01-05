package com.desugar.glucose.renderer.opengl

import android.opengl.GLES31
import com.desugar.glucose.renderer.Framebuffer
import com.desugar.glucose.renderer.FramebufferSpecification
import com.desugar.glucose.renderer.Texture2D

class OpenGLFramebuffer(
    spec: FramebufferSpecification
) : Framebuffer {
    override val specification: FramebufferSpecification = spec
    override val colorAttachmentTexture: Texture2D
        get() = _colorAttachmentTexture

    private var rendererId: Int = 0
    private var depthAttachment: Int = 0
    private var colorAttachmentRendererId: Int = 0
    private var _colorAttachmentTexture: OpenGLTexture2D = OpenGLTexture2D(0, 0, 0, true)

    private val sampledWidth get() = specification.width / specification.samples
    private val sampledHeight get() = specification.height / specification.samples

    init {
        val samples = specification.samples
        require(samples > 0) { "FramebufferSpecification.samples must be > 0" }
        require(samples == 1 || samples % 2 == 0) { "FramebufferSpecification.samples must be an even number or 1" }

        invalidate()
    }

    override fun invalidate() {
        if (rendererId != 0) {
            destroy()
        }

        val id = IntArray(1)
        GLES31.glGenFramebuffers(1, id, 0)
        rendererId = id[0]

        GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, rendererId)

        // color attachment
        GLES31.glGenTextures(1, id, 0)
        colorAttachmentRendererId = id[0]
        GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, colorAttachmentRendererId)
        GLES31.glTexImage2D(
            GLES31.GL_TEXTURE_2D,
            0,
            GLES31.GL_RGBA8,
            sampledWidth,
            sampledHeight,
            0,
            GLES31.GL_RGBA,
            GLES31.GL_UNSIGNED_BYTE,
            null
        )

        GLES31.glTexParameteri(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_MIN_FILTER, GLES31.GL_LINEAR)
        GLES31.glTexParameteri(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_MAG_FILTER, GLES31.GL_LINEAR)
        GLES31.glFramebufferTexture2D(
            GLES31.GL_FRAMEBUFFER,
            GLES31.GL_COLOR_ATTACHMENT0,
            GLES31.GL_TEXTURE_2D,
            colorAttachmentRendererId,
            0
        )

        if (_colorAttachmentTexture.rendererId != colorAttachmentRendererId) {
            _colorAttachmentTexture =
                OpenGLTexture2D(colorAttachmentRendererId, sampledWidth, sampledHeight, false)
        }

        // depth attachment
        //depth renderbuffer
//        GLES31.glGenRenderbuffers(1, id, 0);
//        depthAttachment = id[0]
//        GLES31.glBindRenderbuffer(GLES31.GL_RENDERBUFFER, depthAttachment);
//        GLES31.glRenderbufferStorage(GLES31.GL_RENDERBUFFER, GLES31.GL_DEPTH_COMPONENT16, specification.width, specification.width);
        GLES31.glFramebufferRenderbuffer(
            GLES31.GL_FRAMEBUFFER,
            GLES31.GL_DEPTH_ATTACHMENT,
            GLES31.GL_RENDERBUFFER,
            depthAttachment
        )

        GLES31.glGenTextures(1, id, 0)
        depthAttachment = id[0]
        GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, depthAttachment)
//        GLES31.glTexStorage2D(
//            GLES31.GL_TEXTURE_2D,
//            1,
//            GLES31.GL_RGBA8,
//            specification.width,
//            specification.height,
//        )
        GLES31.glTexImage2D(
            GLES31.GL_TEXTURE_2D,
            0,
            GLES31.GL_DEPTH24_STENCIL8,
            sampledWidth,
            sampledHeight,
            0,
            GLES31.GL_DEPTH_STENCIL,
            GLES31.GL_UNSIGNED_INT_24_8,
            null
        )
        GLES31.glFramebufferTexture2D(
            GLES31.GL_FRAMEBUFFER,
            GLES31.GL_DEPTH_STENCIL_ATTACHMENT,
            GLES31.GL_TEXTURE_2D,
            depthAttachment,
            0
        )

        require(GLES31.glCheckFramebufferStatus(GLES31.GL_FRAMEBUFFER) == GLES31.GL_FRAMEBUFFER_COMPLETE) {
            "Framebuffer is incomplete!"
        }

        GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, 0)
    }

    override fun bind() {
        GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, rendererId)
        GLES31.glViewport(0, 0, sampledWidth, sampledHeight)
    }

    override fun unbind() {
        GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, 0)
    }

    override fun resize(width: Int, height: Int) {
        specification.width = width
        specification.height = height
        invalidate()
    }

    override fun destroy() {
        unbind()
        val textures = intArrayOf(colorAttachmentRendererId, depthAttachment)
        GLES31.glDeleteTextures(textures.size, textures, 0)
        GLES31.glDeleteFramebuffers(1, intArrayOf(rendererId), 0)
    }
}