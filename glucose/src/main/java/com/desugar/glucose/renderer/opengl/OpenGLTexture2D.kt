package com.desugar.glucose.renderer.opengl

import android.opengl.GLES31
import com.desugar.glucose.assets.AssetManager
import com.desugar.glucose.renderer.Texture2D
import java.nio.Buffer

class OpenGLTexture2D : Texture2D {
    override var width: Int = 0
    override var height: Int = 0
    override var rendererId: Int = 0
        private set
    override var flipTexture: Boolean = true

    private var pixelDataFormat: Int = GLES31.GL_RGBA
    private var channelsCount: Int = 4 // r,g,b,a

    constructor(path: String, assetManager: AssetManager) {
        prepareGLTexture()
        loadAssetTexture(path, assetManager)
    }

    constructor(width: Int, height: Int) {
        prepareGLTexture()
        this.width = width
        this.height = height

        GLES31.glTexParameteri(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_WRAP_S, GLES31.GL_REPEAT)
        GLES31.glTexParameteri(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_WRAP_T, GLES31.GL_REPEAT)
        GLES31.glTexParameteri(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_MIN_FILTER, GLES31.GL_LINEAR)
        GLES31.glTexParameteri(
            GLES31.GL_TEXTURE_2D,
            GLES31.GL_TEXTURE_MAG_FILTER,
            GLES31.GL_NEAREST
        )

        GLES31.glTexStorage2D(
            GLES31.GL_TEXTURE_2D,
            1,
            getInternalGLFormat(channelsCount),
            width,
            height
        )
    }

    constructor(texture: Int, width: Int, height: Int, flipTexture: Boolean) {
        rendererId = texture
        this.width = width
        this.height = height
        this.flipTexture = flipTexture
    }

    private fun prepareGLTexture() {
        val ids = IntArray(1)
//        GLES31.glPixelStorei(GLES31.GL_UNPACK_ALIGNMENT, 1);
        GLES31.glGenTextures(1, ids, 0)
        rendererId = ids[0]
        GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, rendererId)
    }

    private fun loadAssetTexture(path: String, assetManager: AssetManager) {
        val pixelMap = assetManager.loadPictureAsset(path)
        width = pixelMap.width
        height = pixelMap.height

        pixelDataFormat = getPixelDataFormat(pixelMap.channels)
        channelsCount = pixelMap.channels
        GLES31.glTexStorage2D(
            GLES31.GL_TEXTURE_2D,
            1,
            getInternalGLFormat(pixelMap.channels),
            width,
            height
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
            GLES31.GL_TEXTURE_2D,
            0,
            0,
            0,
            width,
            height,
            pixelDataFormat,
            getType(channelsCount),
            data
        )
    }

    private fun getInternalGLFormat(channels: Int): Int {
        return when (channels) {
            3 -> GLES31.GL_RGB8
            4 -> GLES31.GL_RGBA8
            else -> error("Unsupported channel count: $channels")
        }
    }

    private fun getPixelDataFormat(channels: Int): Int {
        return when (channels) {
            3 -> GLES31.GL_RGB
            4 -> GLES31.GL_RGBA
            else -> error("Unsupported channel count: $channels")
        }
    }

    private fun getType(channels: Int): Int {
        return when (channels) {
            3 -> GLES31.GL_UNSIGNED_SHORT_5_6_5
            4 -> GLES31.GL_UNSIGNED_BYTE
            else -> error("Unsupported channel count: $channels")
        }
    }

    override fun bind(slot: Int) {
        GLES31.glActiveTexture(GLES31.GL_TEXTURE0 + slot)
        GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, rendererId)
    }

    override fun destroy() {
        GLES31.glDeleteTextures(1, intArrayOf(rendererId), 0)
    }
}
