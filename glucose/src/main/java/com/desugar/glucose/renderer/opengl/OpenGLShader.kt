package com.desugar.glucose.renderer.opengl

import android.opengl.GLES31
import android.util.Log
import com.desugar.glucose.assets.AssetManager
import com.desugar.glucose.renderer.Shader
import com.desugar.glucose.util.parseVertexFragmentShaders
import dev.romainguy.kotlin.math.*
import java.io.File

class OpenGLShader : Shader {
    private var _name: String = ""
    override val name: String get() = _name
    private var rendererId: Int = 0

    constructor(name: String, vertexSrc: String, fragmentSrc: String) {
        _name = name
        compile(vertexSrc, fragmentSrc)
    }

    constructor(assetManager: AssetManager, filePath: String) {
        val file = File(filePath)
        _name = file.nameWithoutExtension
        val shaderSource = assetManager.loadTextAsset(filePath)
        val (vertexSrc, fragmentSrc) = parseVertexFragmentShaders(shaderSource)
        compile(vertexSrc, fragmentSrc)
    }

    override fun bind() {
        GLES31.glUseProgram(rendererId)
    }

    override fun unbind() {
        GLES31.glUseProgram(0)
    }

    override fun setInt(name: String, value: Int) {
        uploadUniformInt(name, value)
    }

    override fun setIntArray(name: String, vararg values: Int) {
        uploadUniformIntArray(name, *values)
    }

    override fun setFloat(name: String, value: Float) {
        uploadUniformFloat(name, value)
    }

    override fun setFloat2(name: String, value: Float2) {
        uploadUniformFloat2(name, value)
    }

    override fun setFloat3(name: String, value: Float3) {
        uploadUniformFloat3(name, value)
    }

    override fun setFloat4(name: String, value: Float4) {
        uploadUniformFloat4(name, value)
    }

    override fun setMat3(name: String, value: Mat3) {
        uploadUniformMat3(name, value)
    }

    override fun setMat4(name: String, value: Mat4) {
        uploadUniformMat4(name, value)
    }

    override fun uploadUniformInt(name: String, value: Int) {
        val location = GLES31.glGetUniformLocation(rendererId, name)
        GLES31.glUniform1i(location, value)
    }

    override fun uploadUniformIntArray(name: String, vararg values: Int) {
        val location = GLES31.glGetUniformLocation(rendererId, name)
        GLES31.glUniform1iv(location, values.size, values, 0)
    }

    override fun uploadUniformFloat(name: String, value: Float) {
        val location = GLES31.glGetUniformLocation(rendererId, name)
        GLES31.glUniform1f(location, value)
    }

    override fun uploadUniformFloat2(name: String, value: Float2) {
        val location = GLES31.glGetUniformLocation(rendererId, name)
        GLES31.glUniform2f(location, value.x, value.y)
    }

    override fun uploadUniformFloat3(name: String, value: Float3) {
        val location = GLES31.glGetUniformLocation(rendererId, name)
        GLES31.glUniform3f(location, value.x, value.y, value.z)
    }

    override fun uploadUniformFloat4(name: String, value: Float4) {
        val location = GLES31.glGetUniformLocation(rendererId, name)
        GLES31.glUniform4f(location, value.x, value.y, value.z, value.w)
    }

    override fun uploadUniformMat3(name: String, value: Mat3) {
        val location = GLES31.glGetUniformLocation(rendererId, name)
        GLES31.glUniformMatrix3fv(location, 1, true, value.toFloatArray(), 0)
    }

    override fun uploadUniformMat4(name: String, value: Mat4) {
        val location = GLES31.glGetUniformLocation(rendererId, name)
        GLES31.glUniformMatrix4fv(location, 1, true, value.toFloatArray(), 0)
    }

    override fun destroy() {
        unbind()
        GLES31.glDetachShader(rendererId, GLES31.GL_VERTEX_SHADER)
        GLES31.glDetachShader(rendererId, GLES31.GL_FRAGMENT_SHADER)
        GLES31.glDeleteProgram(rendererId)
    }

    private fun compile(vertexSrc: String, fragmentSrc: String) {
        val vertexShader = GLES31.glCreateShader(GLES31.GL_VERTEX_SHADER)
        GLES31.glShaderSource(vertexShader, vertexSrc)
        GLES31.glCompileShader(vertexShader)

        val status = IntArray(1)
        GLES31.glGetShaderiv(vertexShader, GLES31.GL_COMPILE_STATUS, status, 0)
        if (status[0] == GLES31.GL_FALSE) {
            val errorMessage = GLES31.glGetShaderInfoLog(vertexShader)
            GLES31.glDeleteShader(vertexShader)
            error(errorMessage)
        }

        val fragmentShader = GLES31.glCreateShader(GLES31.GL_FRAGMENT_SHADER)
        GLES31.glShaderSource(fragmentShader, fragmentSrc)
        GLES31.glCompileShader(fragmentShader)

        GLES31.glGetShaderiv(fragmentShader, GLES31.GL_COMPILE_STATUS, status, 0)
        if (status[0] == GLES31.GL_FALSE) {
            val errorMessage = GLES31.glGetShaderInfoLog(fragmentShader)
            GLES31.glDeleteShader(fragmentShader)
            Log.e(TAG, errorMessage)
            error(errorMessage)
        }

        rendererId = GLES31.glCreateProgram()
        val program = rendererId
        GLES31.glAttachShader(program, vertexShader)
        GLES31.glAttachShader(program, fragmentShader)

        GLES31.glLinkProgram(program)

        GLES31.glGetProgramiv(program, GLES31.GL_LINK_STATUS, status, 0)
        if (status[0] == GLES31.GL_FALSE) {
            val errorMessage = GLES31.glGetProgramInfoLog(program)
            GLES31.glDeleteProgram(program)
            Log.e(TAG, errorMessage)
            error(errorMessage)
        }

        GLES31.glDetachShader(program, vertexShader)
        GLES31.glDetachShader(program, fragmentShader)
    }

    private companion object {
        private const val TAG = "OpenGLShader"
    }
}