package com.desugar.glucose.renderer

import com.desugar.glucose.renderer.opengl.OpenGLIndexBuffer
import com.desugar.glucose.renderer.opengl.OpenGLUniformBuffer
import com.desugar.glucose.renderer.opengl.OpenGLVertexBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

interface GfxBuffer {
    val count: Int
    val sizeBytes: Int
    fun bind()
    fun unbind()
    fun destroy()
}

enum class ShaderDataType(val components: kotlin.Int) {
    None(0),
    Float(1),
    Float2(2),
    Float3(3),
    Float4(4),
    Mat3(3 * 3),
    Mat4(4 * 4),
    Int(1),
    Int2(2),
    Int3(3),
    Int4(4),
    Bool(1);
}

data class BufferElement(
    val name: String,
    val type: ShaderDataType,
    val sizeBytes: Int,
    var offset: Int = 0,
    val normalized: Boolean = false
)

class BufferLayout(
    val elements: List<BufferElement>
) : Iterable<BufferElement> by elements {
    var stride: Int = 0
        private set

    constructor(action: MutableList<BufferElement>.() -> Unit) : this(buildList(action))

    init {
        calculateOffsetAndStride()
    }

    private fun calculateOffsetAndStride() {
        var offset: Int = 0
        elements.forEach { element ->
            element.offset = offset
            offset += element.sizeBytes
            stride += element.sizeBytes
        }
    }

}

interface VertexBuffer : GfxBuffer {

    var layout: BufferLayout?

    fun setData(data: FloatArray)

    companion object {
        fun create(count: Int): VertexBuffer {
            return when (Renderer.api) {
                RendererAPI.API.NONE -> error("Select renderer API")
                RendererAPI.API.OPENGL -> OpenGLVertexBuffer(count)
            }
        }

        fun create(vertices: FloatArray): VertexBuffer {
            return when (Renderer.api) {
                RendererAPI.API.NONE -> error("Select renderer API")
                RendererAPI.API.OPENGL -> OpenGLVertexBuffer(vertices)
            }
        }
    }
}

interface IndexBuffer : GfxBuffer {

    companion object {
        fun create(indices: IntArray): IndexBuffer {
            return when (Renderer.api) {
                RendererAPI.API.NONE -> error("Select renderer API")
                RendererAPI.API.OPENGL -> OpenGLIndexBuffer(indices)
            }
        }
    }
}

interface UniformBuffer : GfxBuffer {
    fun setData(data: FloatArray)

    companion object {
        fun create(count: Int, binding: Int): UniformBuffer {
            return when (Renderer.api) {
                RendererAPI.API.NONE -> error("Select renderer API")
                RendererAPI.API.OPENGL -> OpenGLUniformBuffer(count, binding)
            }
        }
    }
}

fun FloatArray.toFloatBuffer(): FloatBuffer = ByteBuffer
    .allocateDirect(size * Float.SIZE_BYTES)
    .order(ByteOrder.nativeOrder())
    .asFloatBuffer()
    .put(this)
    .position(0) as FloatBuffer

fun IntArray.toIntBuffer(): IntBuffer = ByteBuffer
    .allocateDirect(size * Int.SIZE_BYTES)
    .order(ByteOrder.nativeOrder())
    .asIntBuffer()
    .put(this)
    .position(0) as IntBuffer

fun MutableList<BufferElement>.addElement(
    name: String,
    type: ShaderDataType,
    normalized: Boolean = false
) {
    val element = BufferElement(
        name = name,
        type = type,
        sizeBytes = type.sizeBytes,
        normalized = normalized
    )
    add(element)
}

val ShaderDataType.sizeBytes: Int
    get() = when (this) {
        ShaderDataType.Float -> Float.SIZE_BYTES
        ShaderDataType.Float2 -> Float.SIZE_BYTES * 2
        ShaderDataType.Float3 -> Float.SIZE_BYTES * 3
        ShaderDataType.Float4 -> Float.SIZE_BYTES * 4
        ShaderDataType.Mat3 -> Float.SIZE_BYTES * 3 * 3
        ShaderDataType.Mat4 -> Float.SIZE_BYTES * 4 * 4
        ShaderDataType.Int -> Int.SIZE_BYTES
        ShaderDataType.Int2 -> Int.SIZE_BYTES * 2
        ShaderDataType.Int3 -> Int.SIZE_BYTES * 3
        ShaderDataType.Int4 -> Int.SIZE_BYTES * 4
        ShaderDataType.Bool -> 1
        ShaderDataType.None -> 0
    }