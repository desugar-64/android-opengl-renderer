package com.desugar.glucose.renderer.opengl

import android.opengl.GLES31
import com.desugar.glucose.assets.AssetManager
import com.desugar.glucose.renderer.Texture
import com.desugar.glucose.renderer.Texture2D
import java.nio.Buffer

class OpenGLTexture2D : Texture2D {
    override var width: Int = 0
    override var height: Int = 0
    override var rendererId: Int = 0
        private set
    override var flipTexture: Boolean = true
    override lateinit var specification: Texture.Specification

    private var _isLoaded: Boolean = false

    constructor(path: String, assetManager: AssetManager) {
        prepareGLTexture()
        loadAssetTexture(path, assetManager)
    }

    constructor(specification: Texture.Specification) {
        prepareGLTexture()
        this.width = specification.width
        this.height = specification.height
        this.flipTexture = specification.flipTexture
        this.specification = specification

        GLES31.glTexParameteri(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_WRAP_S, GLES31.GL_REPEAT)
        GLES31.glTexParameteri(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_WRAP_T, GLES31.GL_REPEAT)
        GLES31.glTexParameteri(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_MIN_FILTER, GLES31.GL_LINEAR)
        GLES31.glTexParameteri(
            GLES31.GL_TEXTURE_2D,
            GLES31.GL_TEXTURE_MAG_FILTER,
            GLES31.GL_NEAREST
        )

        GLES31.glTexStorage2D(
            /* target = */ GLES31.GL_TEXTURE_2D,
            /* levels = */ 1,
            /* internalformat = */ specification.format.toGlInternalFormat(),
            /* width = */ width,
            /* height = */ height
        )
    }

    constructor(texture: Int, specification: Texture.Specification) {
        this.rendererId = texture
        this.width = specification.width
        this.height = specification.height
        this.flipTexture = specification.flipTexture
    }

    private fun prepareGLTexture() {
        val ids = IntArray(1)
        GLES31.glGenTextures(1, ids, 0)
        rendererId = ids[0]
        GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, rendererId)
    }

    private fun loadAssetTexture(path: String, assetManager: AssetManager) {
        val pixelMap = assetManager.loadPictureAsset(path)
        width = pixelMap.width
        height = pixelMap.height

        specification = Texture.Specification(width, height)

        GLES31.glTexStorage2D(
            /* target = */ GLES31.GL_TEXTURE_2D,
            /* levels = */ 1,
            /* internalformat = */ specification.format.toGlInternalFormat(),
            /* width = */ width,
            /* height = */ height
        )
        GLES31.glTexParameteri(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_WRAP_S, GLES31.GL_REPEAT)
        GLES31.glTexParameteri(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_WRAP_T, GLES31.GL_REPEAT)
        GLES31.glTexParameteri(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_MIN_FILTER, GLES31.GL_LINEAR)
        GLES31.glTexParameteri(
            GLES31.GL_TEXTURE_2D,
            GLES31.GL_TEXTURE_MAG_FILTER,
            GLES31.GL_NEAREST
        )
        setData(pixelMap.pixelData)
    }

    override fun setData(data: Buffer) {
        GLES31.glTexSubImage2D(
            /* target = */ GLES31.GL_TEXTURE_2D,
            /* level = */ 0,
            /* xoffset = */ 0,
            /* yoffset = */ 0,
            /* width = */ width,
            /* height = */ height,
            /* format = */ specification.format.toGlImageFormat(),
            /* type = */ specification.format.getDataType(),
            /* pixels = */ data
        )
        _isLoaded = true
    }

    override fun bind(slot: Int) {
        GLES31.glActiveTexture(GLES31.GL_TEXTURE0 + slot)
        GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, rendererId)
    }

    override fun destroy() {
        GLES31.glDeleteTextures(1, intArrayOf(rendererId), 0)
    }

    override fun isLoaded(): Boolean {
        return _isLoaded
    }
}

private fun Texture.ImageFormat.toGlInternalFormat(): Int {
    return when (this) {
        Texture.ImageFormat.None -> 0
        Texture.ImageFormat.R8 -> GLES31.GL_R8
        Texture.ImageFormat.RGB8 -> GLES31.GL_RGB8
        Texture.ImageFormat.RGBA8 -> GLES31.GL_RGBA8
        Texture.ImageFormat.RGBA32F -> GLES31.GL_RGBA32F
    }
}

private fun Texture.ImageFormat.getDataType(): Int {
    // Use a when expression to return the corresponding OpenGL type constant
    return when (this) {
        Texture.ImageFormat.None -> 0 // No type
        Texture.ImageFormat.R8 -> GLES31.GL_UNSIGNED_BYTE
        Texture.ImageFormat.RGB8 -> GLES31.GL_UNSIGNED_BYTE
        Texture.ImageFormat.RGBA8 -> GLES31.GL_UNSIGNED_BYTE
        Texture.ImageFormat.RGBA32F -> GLES31.GL_FLOAT
    }
}

private fun Texture.ImageFormat.toGlImageFormat(): Int {
    return when (this) {
        Texture.ImageFormat.None -> 0
        Texture.ImageFormat.R8 -> GLES31.GL_RED
        Texture.ImageFormat.RGB8 -> GLES31.GL_RGB
        Texture.ImageFormat.RGBA8 -> GLES31.GL_RGBA
        Texture.ImageFormat.RGBA32F -> GLES31.GL_RGBA
    }
}
