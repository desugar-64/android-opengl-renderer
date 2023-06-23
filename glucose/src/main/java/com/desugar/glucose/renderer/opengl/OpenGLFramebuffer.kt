package com.desugar.glucose.renderer.opengl

import android.R.bool
import android.opengl.GLES20.GL_FRAMEBUFFER
import android.opengl.GLES31
import android.util.Log
import com.desugar.glucose.renderer.Framebuffer
import com.desugar.glucose.renderer.FramebufferSpecification
import com.desugar.glucose.renderer.FramebufferTextureFormat
import com.desugar.glucose.renderer.FramebufferTextureSpecification
import com.desugar.glucose.renderer.Texture
import com.desugar.glucose.renderer.Texture2D
import com.desugar.glucose.renderer.toIntBuffer


class OpenGLFramebuffer(
    spec: FramebufferSpecification
) : Framebuffer {
    override val specification: FramebufferSpecification = spec
    override val colorAttachmentTexture: Texture2D
        get() = _colorAttachmentTexture

    private var rendererId: Int = 0
    private var depthAttachment: Int = 0
    private val colorAttachmentSpecifications: MutableList<FramebufferTextureSpecification> =
        mutableListOf()
    private var depthAttachmentSpecification: FramebufferTextureSpecification =
        FramebufferTextureSpecification()
    private var colorAttachments: IntArray = IntArray(0)
    private var _colorAttachmentTexture: OpenGLTexture2D =
        OpenGLTexture2D(0, Texture.Specification(flipTexture = true))

    private val sampledWidth get() = specification.width / specification.downSampleFactor
    private val sampledHeight get() = specification.height / specification.downSampleFactor

    init {
        val samples = specification.sampling
        require(samples > 0) { "FramebufferSpecification.samples must be > 0" }
        require(samples == 1 || samples % 2 == 0) { "FramebufferSpecification.samples must be an even number or 1" }

        specification.attachmentsSpec.attachments.forEach { attachmentSpec ->
            if (!isDepthFormat(attachmentSpec.format)) {
                colorAttachmentSpecifications.add(attachmentSpec)
            } else {
                depthAttachmentSpecification = attachmentSpec
            }
        }
        invalidate()
    }

    override fun invalidate() {
        if (rendererId != 0) {
            destroy()
            colorAttachments = IntArray(0)
            depthAttachment = 0
        }

        val id = IntArray(1)
        GLES31.glGenFramebuffers(1, id, 0)
        rendererId = id[0]

        GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, rendererId)

        val multisample: Boolean = specification.sampling > 1
        if (colorAttachmentSpecifications.isNotEmpty()) {
            colorAttachments = createTextures(multisample, colorAttachmentSpecifications.size)
            colorAttachments.forEachIndexed { index, handle ->
                bindTexture(multisample, handle)
                when (colorAttachmentSpecifications[index].format) {
                    FramebufferTextureFormat.RGBA8 -> attachColorTexture(
                        id = handle,
                        samples = specification.sampling,
                        internalFormat = GLES31.GL_RGBA8,
                        format = GLES31.GL_RGBA,
                        width = sampledWidth,
                        height = sampledHeight,
                        index = index
                    )

                    FramebufferTextureFormat.RED_INTEGER -> attachColorTexture(
                        id = handle,
                        samples = specification.sampling,
                        internalFormat = GLES31.GL_R32I,
                        format = GLES31.GL_RED_INTEGER,
                        width = sampledWidth,
                        height = sampledHeight,
                        index = index
                    )

                    else -> { /* no-op */ }
                }
            }
        }
        if (depthAttachmentSpecification.format != FramebufferTextureFormat.None) {
            depthAttachment = createTextures(multisample, 1).first()
            bindTexture(multisample, depthAttachment)
            if (depthAttachmentSpecification.format == FramebufferTextureFormat.DEPTH24STENCIL8) {
                attachDepthTexture(
                    id = depthAttachment,
                    samples = specification.sampling,
                    format = GLES31.GL_DEPTH24_STENCIL8,
                    attachmentType = GLES31.GL_DEPTH_STENCIL_ATTACHMENT,
                    width = sampledWidth,
                    height = sampledHeight
                )
            }
        }

        if (colorAttachments.isNotEmpty()) {
            val buffers: IntArray = IntArray(colorAttachments.size) {
                GLES31.GL_COLOR_ATTACHMENT0 + it
            }
            GLES31.glDrawBuffers(colorAttachments.size, buffers, 0);
        } else {
            // Only depth-pass
            GLES31.glDrawBuffers(0, intArrayOf(), 0)
        }

        require(GLES31.glCheckFramebufferStatus(GLES31.GL_FRAMEBUFFER) == GLES31.GL_FRAMEBUFFER_COMPLETE) {
            "Framebuffer is incomplete!"
        }

        if (_colorAttachmentTexture.rendererId != colorAttachments[0]) {
            _colorAttachmentTexture = OpenGLTexture2D(
                texture = colorAttachments[0],
                specification = Texture.Specification(
                    width = sampledWidth, height = sampledHeight, flipTexture = false
                )
            )
        }


        // switchback to default framebuffer
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
        if (width == 0 || height == 0 || width > MAX_FRAMEBUFFER_SIZE || height > MAX_FRAMEBUFFER_SIZE) {
            Log.w(TAG, "Attempt to resize framebuffer to $width, $height failed")
            return
        }
        if (specification.width != width || specification.height != height) {
            specification.width = width
            specification.height = height

            invalidate()
        }
    }

    override fun readPixel(attachmentIndex: Int, x: Int, y: Int): Int {
        require(attachmentIndex <= colorAttachments.lastIndex)
        GLES31.glReadBuffer(GLES31.GL_COLOR_ATTACHMENT0 + attachmentIndex)
        val data = IntArray(1).toIntBuffer()
        GLES31.glReadPixels(x, y, 1, 1, GLES31.GL_RED_INTEGER, GLES31.GL_INT, data)
        data.rewind()
        return data.get()
    }

    override fun clearAttachment(attachmentIndex: Int, value: Int) {
        require(attachmentIndex <= colorAttachments.lastIndex)
        val attachmentHandle = getColorAttachmentRendererID(attachmentIndex)
        val spec = colorAttachmentSpecifications[attachmentIndex]
        val type = when (spec.format) {
            FramebufferTextureFormat.None -> 0
            FramebufferTextureFormat.RGBA8 -> GLES31.GL_UNSIGNED_BYTE
            FramebufferTextureFormat.RED_INTEGER -> GLES31.GL_R32I
            FramebufferTextureFormat.DEPTH24STENCIL8 -> GLES31.GL_UNSIGNED_INT_24_8
        }
        val components = when (spec.format) {
            FramebufferTextureFormat.None -> 0
            FramebufferTextureFormat.RGBA8 -> 4
            FramebufferTextureFormat.RED_INTEGER -> 1
            FramebufferTextureFormat.DEPTH24STENCIL8 -> 1
        }
        val emptyPixels =
            IntArray(sampledWidth * sampledHeight * components) { value }.toIntBuffer()
        bindTexture(specification.sampling > 1, attachmentHandle)
        GLES31.glTexSubImage2D(
            /* target = */ textureTarget(specification.sampling > 1),
            /* level = */ 0,
            /* xoffset = */ 0,
            /* yoffset = */ 0,
            /* width = */ sampledWidth,
            /* height = */ sampledHeight,
            /* format = */ fbTextureFormatToGL(spec.format),
            /* type = */ type,
            /* pixels = */ emptyPixels
        )
    }

    private fun clearFramebufferColorAttachment(attachmentIndex: Int, clearValue: FloatArray) {
        val drawBuffers = IntArray(1) { GLES31.GL_COLOR_ATTACHMENT0 + attachmentIndex }

        GLES31.glDrawBuffers(1, drawBuffers, 0)
        GLES31.glClearBufferfv(GLES31.GL_COLOR, 0, clearValue, 0)
        GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, 0)
    }

    override fun getColorAttachmentRendererID(index: Int): Int {
        require(index <= colorAttachments.lastIndex)
        return colorAttachments[index]
    }

    override fun destroy() {
        unbind()
        val framebufferHandle = intArrayOf(rendererId)
        GLES31.glDeleteFramebuffers(1, framebufferHandle, 0)
        GLES31.glDeleteTextures(1, intArrayOf(depthAttachment), 0)
        GLES31.glDeleteTextures(1, colorAttachments, 0)
        GLES31.glDeleteFramebuffers(1, framebufferHandle, 0)
    }

    private companion object {
        private const val TAG = "OpenGLFramebuffer"
        const val MAX_FRAMEBUFFER_SIZE = 8192

        fun textureTarget(multisampled: Boolean): Int {
            return if (multisampled) GLES31.GL_TEXTURE_2D_MULTISAMPLE else GLES31.GL_TEXTURE_2D
        }

        fun createTextures(multisampled: Boolean, count: Int): IntArray {
            val ids = IntArray(count)
            GLES31.glGenTextures(count, ids, 0)
            return ids
        }

        fun bindTexture(multisampled: Boolean, id: Int) {
            GLES31.glBindTexture(textureTarget(multisampled), id)
        }

        fun attachColorTexture(
            id: Int,
            samples: Int,
            internalFormat: Int,
            format: Int,
            width: Int,
            height: Int,
            index: Int
        ) {
            val multisampled: Boolean = samples > 1
            if (multisampled) {
                GLES31.glTexStorage2DMultisample(
                    /* target = */ GLES31.GL_TEXTURE_2D_MULTISAMPLE,
                    /* samples = */ samples,
                    /* internalformat = */ internalFormat,
                    /* width = */ width,
                    /* height = */ height,
                    /* fixedsamplelocations = */ false
                )
            } else {
                GLES31.glTexImage2D(
                    /* target = */ GLES31.GL_TEXTURE_2D,
                    /* level = */ 0,
                    /* internalformat = */ internalFormat,
                    /* width = */ width,
                    /* height = */ height,
                    /* border = */ 0,
                    /* format = */ format,
                    /* type = */ GLES31.GL_UNSIGNED_BYTE,
                    /* pixels = */ null
                )
                GLES31.glTexParameteri(
                    /* target = */ GLES31.GL_TEXTURE_2D,
                    /* pname = */ GLES31.GL_TEXTURE_MIN_FILTER,
                    /* param = */ GLES31.GL_LINEAR
                )
                GLES31.glTexParameteri(
                    /* target = */ GLES31.GL_TEXTURE_2D,
                    /* pname = */ GLES31.GL_TEXTURE_MAG_FILTER,
                    /* param = */ GLES31.GL_LINEAR
                )
                GLES31.glTexParameteri(
                    /* target = */ GLES31.GL_TEXTURE_2D,
                    /* pname = */ GLES31.GL_TEXTURE_WRAP_R,
                    /* param = */ GLES31.GL_CLAMP_TO_EDGE
                )
                GLES31.glTexParameteri(
                    /* target = */ GLES31.GL_TEXTURE_2D,
                    /* pname = */ GLES31.GL_TEXTURE_WRAP_S,
                    /* param = */ GLES31.GL_CLAMP_TO_EDGE
                )
                GLES31.glTexParameteri(
                    /* target = */ GLES31.GL_TEXTURE_2D,
                    /* pname = */ GLES31.GL_TEXTURE_WRAP_T,
                    /* param = */ GLES31.GL_CLAMP_TO_EDGE
                )
            }
            GLES31.glFramebufferTexture2D(
                /* target = */ GLES31.GL_FRAMEBUFFER,
                /* attachment = */ GLES31.GL_COLOR_ATTACHMENT0 + index,
                /* textarget = */ textureTarget(multisampled),
                /* texture = */ id,
                /* level = */ 0
            )
        }

        fun attachDepthTexture(
            id: Int,
            samples: Int,
            format: Int,
            attachmentType: Int,
            width: Int,
            height: Int
        ) {
            val multisampled: Boolean = samples > 1
            if (multisampled) {
                GLES31.glTexStorage2DMultisample(
                    /* target = */ GLES31.GL_TEXTURE_2D_MULTISAMPLE,
                    /* samples = */ samples,
                    /* internalformat = */ format,
                    /* width = */ width,
                    /* height = */ height,
                    /* fixedsamplelocations = */ false
                )
            } else {
                GLES31.glTexStorage2D(GLES31.GL_TEXTURE_2D, 1, format, width, height)
                GLES31.glTexParameteri(
                    /* target = */ GLES31.GL_TEXTURE_2D,
                    /* pname = */ GLES31.GL_TEXTURE_MIN_FILTER,
                    /* param = */ GLES31.GL_LINEAR
                )
                GLES31.glTexParameteri(
                    /* target = */ GLES31.GL_TEXTURE_2D,
                    /* pname = */ GLES31.GL_TEXTURE_MAG_FILTER,
                    /* param = */ GLES31.GL_LINEAR
                )
                GLES31.glTexParameteri(
                    /* target = */ GLES31.GL_TEXTURE_2D,
                    /* pname = */ GLES31.GL_TEXTURE_WRAP_R,
                    /* param = */ GLES31.GL_CLAMP_TO_EDGE
                )
                GLES31.glTexParameteri(
                    /* target = */ GLES31.GL_TEXTURE_2D,
                    /* pname = */ GLES31.GL_TEXTURE_WRAP_S,
                    /* param = */ GLES31.GL_CLAMP_TO_EDGE
                )
                GLES31.glTexParameteri(
                    /* target = */ GLES31.GL_TEXTURE_2D,
                    /* pname = */ GLES31.GL_TEXTURE_WRAP_T,
                    /* param = */ GLES31.GL_CLAMP_TO_EDGE
                )
            }
            GLES31.glFramebufferTexture2D(
                /* target = */ GL_FRAMEBUFFER,
                /* attachment = */ attachmentType,
                /* textarget = */ textureTarget(multisampled),
                /* texture = */ id,
                /* level = */ 0
            )
        }

        fun isDepthFormat(format: FramebufferTextureFormat): Boolean {
            return when (format) {
                FramebufferTextureFormat.DEPTH24STENCIL8 -> true
                else -> false
            }
        }

        fun fbTextureFormatToGL(format: FramebufferTextureFormat): Int {
            return when (format) {
                FramebufferTextureFormat.RGBA8 -> GLES31.GL_RGBA8
                FramebufferTextureFormat.RED_INTEGER -> GLES31.GL_RED_INTEGER
                FramebufferTextureFormat.DEPTH24STENCIL8 -> GLES31.GL_DEPTH24_STENCIL8
                FramebufferTextureFormat.None -> 0
            }
        }

    }
}