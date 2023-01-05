package com.desugar.glucose.renderer

import com.desugar.glucose.assets.AssetManager
import com.desugar.glucose.renderer.opengl.OpenGLTexture2D
import java.nio.Buffer

interface Texture {
    val rendererId: Int
    val width: Int
    val height: Int
    val flipTexture: Boolean

    fun bind(slot: Int = 0)
    fun setData(data: Buffer)
    fun destroy()
}

abstract class Texture2D : Texture {

    companion object {
        fun create(width: Int, height: Int): Texture2D {
            return when (Renderer.api) {
                RendererAPI.API.NONE -> error("Select render API")
                RendererAPI.API.OPENGL -> OpenGLTexture2D(width, height)
            }
        }

        fun create(path: String, assetManager: AssetManager): Texture2D {
            return when (Renderer.api) {
                RendererAPI.API.NONE -> error("Select render API")
                RendererAPI.API.OPENGL -> OpenGLTexture2D(path, assetManager)
            }
        }

        fun create(texture: Int, width: Int, height: Int, flipTexture: Boolean): Texture2D {
            return when (Renderer.api) {
                RendererAPI.API.NONE -> error("Select render API")
                RendererAPI.API.OPENGL -> OpenGLTexture2D(texture, width, height, flipTexture)
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