package com.desugar.glucose.renderer

import com.desugar.glucose.assets.AssetManager
import com.desugar.glucose.renderer.opengl.OpenGLShader
import dev.romainguy.kotlin.math.*
import org.intellij.lang.annotations.Language

interface Shader {
    val name: String
    fun bind()
    fun unbind()

    fun setInt(name: String, value: Int)
    fun setIntArray(name: String, vararg values: Int)
    fun setFloat(name: String, value: Float)
    fun setFloat2(name: String, value: Float2)
    fun setFloat3(name: String, value: Float3)
    fun setFloat4(name: String, value: Float4)
    fun setMat3(name: String, value: Mat3)
    fun setMat4(name: String, value: Mat4)

    fun destroy()

    companion object {
        private const val TAG = "Shader"

        fun create(assetManager: AssetManager, shaderFilePath: String): Shader {
            return when (Renderer.api) {
                RendererAPI.API.NONE -> error("Select rendering API")
                RendererAPI.API.OPENGL -> OpenGLShader(assetManager, shaderFilePath)
            }
        }

        fun create(
            name: String,
            @Language("GLSL") vertexSrc: String,
            @Language("GLSL") fragmentSrc: String
        ): Shader {
            return when (Renderer.api) {
                RendererAPI.API.NONE -> error("Select rendering API")
                RendererAPI.API.OPENGL -> OpenGLShader(name, vertexSrc, fragmentSrc)
            }
        }
    }
}

class ShaderLibrary {
    private val shaders: MutableMap<String, Shader> = mutableMapOf()

    fun add(shader: Shader, name: String = "") {
        val shaderName = name.takeIf { it.isNotEmpty() } ?: shader.name
        require(shaders[shaderName] == null) { "Shader $shaderName already exists!" }
        shaders[shaderName] = shader
    }

    fun load(assetManager: AssetManager, filePath: String, name: String = ""): Shader {
        val shader = Shader.create(assetManager, filePath)
        add(shader, name)
        return shader
    }

    operator fun get(name: String): Shader {
        return requireNotNull(shaders[name]) { "Shader $name not found!" }
    }

    fun destroyAll() {
        shaders.forEach { (_, shader) ->
            shader.destroy()
        }
        shaders.clear()
    }
}