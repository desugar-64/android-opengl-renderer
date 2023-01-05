package com.desugar.glucose.renderer.opengl

import android.opengl.GLES31
import com.desugar.glucose.renderer.ShaderDataType

val ShaderDataType.openGLBaseType: Int
    get() = when (this) {
        ShaderDataType.None -> error("Unconvertable shader data type: ${this.name}")
        ShaderDataType.Float -> GLES31.GL_FLOAT
        ShaderDataType.Float2 -> GLES31.GL_FLOAT
        ShaderDataType.Float3 -> GLES31.GL_FLOAT
        ShaderDataType.Float4 -> GLES31.GL_FLOAT
        ShaderDataType.Mat3 -> GLES31.GL_FLOAT
        ShaderDataType.Mat4 -> GLES31.GL_FLOAT
        ShaderDataType.Int -> GLES31.GL_INT
        ShaderDataType.Int2 -> GLES31.GL_INT
        ShaderDataType.Int3 -> GLES31.GL_INT
        ShaderDataType.Int4 -> GLES31.GL_INT
        ShaderDataType.Bool -> GLES31.GL_BOOL
    }