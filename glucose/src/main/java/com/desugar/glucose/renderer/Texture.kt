package com.desugar.glucose.renderer

import com.desugar.glucose.assets.AssetManager
import com.desugar.glucose.renderer.opengl.OpenGLTexture2D
import java.nio.Buffer

interface Texture {
    enum class ImageFormat {
        None,
        R8,
        RGB8,
        RGBA8,
        RGBA32F
    }

    data class Specification(
        val width: Int = 1,
        val height: Int = 1,
        val format: ImageFormat = ImageFormat.RGBA8,
        val generateMips: Boolean = true,
        val flipTexture: Boolean = false
    )


    val rendererId: Int
    val width: Int
    val height: Int
    val flipTexture: Boolean
    val specification: Specification

    fun bind(slot: Int = 0)
    fun setData(data: Buffer)
    fun isLoaded(): Boolean
    fun destroy()
}

abstract class Texture2D : Texture {

    companion object {
        fun create(specification: Texture.Specification): Texture2D {
            return when (Renderer.api) {
                RendererAPI.API.NONE -> error("Select render API")
                RendererAPI.API.OPENGL -> OpenGLTexture2D(specification)
            }
        }

        fun create(path: String, assetManager: AssetManager): Texture2D {
            return when (Renderer.api) {
                RendererAPI.API.NONE -> error("Select render API")
                RendererAPI.API.OPENGL -> OpenGLTexture2D(path, assetManager)
            }
        }

        fun create(texture: Int, specification: Texture.Specification): Texture2D {
            return when (Renderer.api) {
                RendererAPI.API.NONE -> error("Select render API")
                RendererAPI.API.OPENGL -> OpenGLTexture2D(texture, specification)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Texture2D
        return rendererId == other.rendererId
    }

    override fun hashCode(): Int {
        return rendererId.hashCode()
    }
}