package com.desugar.glucose.renderer

import android.graphics.Color
import com.desugar.glucose.assets.AssetManager
import com.desugar.glucose.camera.OrthographicCamera
import dev.romainguy.kotlin.math.*

object Renderer2D {
    private var _data: Renderer2DData? = null
    private val data: Renderer2DData get() = requireNotNull(_data) { "Renderer2D not initialized!" }

    fun init(assetManager: AssetManager) {
        val quadIndices = IntArray(MAX_INDICES)
        var offset = 0
        for (i in quadIndices.indices step 6) {
            quadIndices[i + 0] = offset + 0
            quadIndices[i + 1] = offset + 1
            quadIndices[i + 2] = offset + 2

            quadIndices[i + 3] = offset + 2
            quadIndices[i + 4] = offset + 3
            quadIndices[i + 5] = offset + 0

            offset += 4
        }

        val cameraData: CameraData = CameraData(Mat4.identity())
        val uniformBuffer: UniformBuffer = UniformBuffer.create(
            count = Mat4.identity().toFloatArray().size * Float.SIZE_BYTES, binding = 0
        )

        val quadVertexArray: VertexArray = VertexArray.create()
        val quadVertexBuffer: VertexBuffer =
            VertexBuffer.create(MAX_VERTICES * QuadVertex.NUMBER_OF_COMPONENTS).apply {
                layout = BufferLayout {
                    addElement("a_Position", ShaderDataType.Float3)
                    addElement("a_TexCoord", ShaderDataType.Float2)
                    addElement("a_Color", ShaderDataType.Float4)
                    addElement("a_TexIndex", ShaderDataType.Float)
                    addElement("a_TilingFactor", ShaderDataType.Float)
                    addElement("a_FlipTexture", ShaderDataType.Float)
                }
            }
        quadVertexArray.addVertexBuffer(quadVertexBuffer)
        quadVertexArray.indexBuffer = IndexBuffer.create(quadIndices)
        val quadShader: Shader = Shader.create(assetManager, "shader/Renderer2D_Quad.glsl")
        val quadIndexCount: Int = 0
        val quadVertexBufferBase: MutableList<QuadVertex> = ArrayList(MAX_VERTICES)

        val circleVertexArray: VertexArray = VertexArray.create()
        val circleVertexBuffer: VertexBuffer =
            VertexBuffer.create(MAX_VERTICES * CircleVertex.NUMBER_OF_COMPONENTS).apply {
                layout = BufferLayout {
                    addElement("a_WorldPosition", ShaderDataType.Float3)
                    addElement("a_LocalPosition", ShaderDataType.Float2)
                    addElement("a_Color", ShaderDataType.Float4)
                    addElement("a_Thickness", ShaderDataType.Float)
                    addElement("a_Fade", ShaderDataType.Float)
                }
            }
        circleVertexArray.addVertexBuffer(circleVertexBuffer)
        circleVertexArray.indexBuffer = IndexBuffer.create(quadIndices) // Use quadIndices
        val circleShader: Shader = Shader.create(assetManager, "shader/Renderer2D_Circle.glsl")
        val circleIndexCount: Int = 0
        val circleVertexBufferBase: MutableList<CircleVertex> = ArrayList(MAX_VERTICES)

        val defaultQuadVertexPositions: Array<Float4> = Array(4) {
            when (it) {
                0 -> Float4(-0.5f, -0.5f, 0.0f, 1.0f) // BL
                1 -> Float4(0.5f, -0.5f, 0.0f, 1.0f)  // BR
                2 -> Float4(0.5f, 0.5f, 0.0f, 1.0f)   // TR
                3 -> Float4(-0.5f, 0.5f, 0.0f, 1.0f)  // TL
                else -> error("Wrong QuadVertex index: $it, max 3")
            }
        }

        val stats: RenderStatistics = RenderStatistics()


        val whiteTexture: Texture2D = Texture2D.create(Texture.Specification())
        val textureSlots: Array<Texture2D?> = Array(MAX_TEXTURE_SLOTS) { null }
        val textureSlotIndex: Int = 1 // 0 = white texture slot

        _data = Renderer2DData(
            cameraData = cameraData,
            uniformBuffer = uniformBuffer,
            whiteTexture = whiteTexture,
            quadVertexArray = quadVertexArray,
            quadVertexBuffer = quadVertexBuffer,
            quadShader = quadShader,
            quadIndexCount = quadIndexCount,
            quadVertexBufferBase = quadVertexBufferBase,
            circleVertexArray = circleVertexArray,
            circleVertexBuffer = circleVertexBuffer,
            circleShader = circleShader,
            circleIndexCount = circleIndexCount,
            circleVertexBufferBase = circleVertexBufferBase,
            textureSlots = textureSlots,
            textureSlotIndex = textureSlotIndex,
            defaultQuadVertexPositions = defaultQuadVertexPositions,
            stats = stats
        )

        whiteTexture.setData(intArrayOf(Color.WHITE).toIntBuffer())
        quadShader.bind()

        val samplers = IntArray(MAX_TEXTURE_SLOTS) { index -> index }
        quadShader.setIntArray("u_Textures", *samplers)
        textureSlots.fill(null)
        textureSlots[WHITE_TEXTURE_SLOT_INDEX] = whiteTexture

    }

    fun shutdown() {
        data.quadShader.destroy()
        data.quadVertexArray.destroy()
        data.circleVertexArray.destroy()
        data.circleShader.destroy()
        data.textureSlots.fill(null)
        _data = null
    }

    fun beginScene(camera: OrthographicCamera) {
        data.cameraData.viewProjection = camera.viewProjectionMatrix
        data.uniformBuffer.setData(transpose(data.cameraData.viewProjection).toFloatArray())

        data.quadIndexCount = 0
        data.quadVertexBufferBase.clear()
        data.circleIndexCount = 0
        data.circleVertexBufferBase.clear()

        data.textureSlotIndex = 1
    }

    fun endScene() {
        data.quadVertexBuffer.setData(data.quadVertexBufferBase.toQuadVertexFloatArray())
        data.circleVertexBuffer.setData(data.circleVertexBufferBase.toCircleVertexFloatArray())
        flush()
    }

    private fun flush() {
        if (data.quadIndexCount > 0) {
            // Bind textures
            for (i in 0 until data.textureSlotIndex) {
                data.textureSlots[i]?.bind(slot = i)
            }
            data.quadShader.bind()
            RenderCommand.drawIndexed(data.quadVertexArray, data.quadIndexCount)
            data.stats.drawCalls++
        }
        if (data.circleIndexCount > 0) {
            data.circleShader.bind()
            RenderCommand.drawIndexed(data.circleVertexArray, data.circleIndexCount)
            data.stats.drawCalls++
        }
    }

    // primitives
    fun lerp(a: Float, b: Float, t: Float): Float {
        return a + (b - a) * t
    }

    fun drawCircle(
        transform: Mat4,
        color: Float4,
        thickness: Float,
        fade: Float
    ) {
        for (i in 0 until 4) {
            val circleVertex = CircleVertex(
                worldPosition = (transform * data.defaultQuadVertexPositions[i]).xyz,
                localPosition = (data.defaultQuadVertexPositions[i] * 2.0f).xy,
                color = color,
                thickness = thickness,
                fade = fade
            )
            data.circleVertexBufferBase.add(circleVertex)
        }
        data.circleIndexCount += 6
        data.stats.quadCount++
    }

    fun drawQuadPx(
        positionPixels: Float2,
        sizePixels: Float2,
        color: Float4,
        windowWidth: Float,
        windowHeight: Float,
        orthographicSize: Float
    ) {

        // Get the screen aspect ratio
        val screenAspect = windowWidth / windowHeight

        val sizeNDC = Float2(
            x = (sizePixels.x / windowWidth) * screenAspect,
            y = (sizePixels.y / windowHeight)
        )

        // Convert the position and size from screen space to NDC
        val positionNDC = Float3(
            x = lerp(-1f * screenAspect, 1f * screenAspect, (positionPixels.x / windowWidth)),
            y = lerp(-1f, 1f, (positionPixels.y / windowHeight)),
            z = 0f
        ) * orthographicSize * 2f


        // Generate the transformation matrix
        val transform = translation(positionNDC) *
                scale(Float3(sizeNDC * orthographicSize * 2f, 1.0f))

        // Submit the quad
        submitQuad(transform, color)
    }

    fun drawQuad(position: Float2, size: Float2, color: Float4) {
        drawQuad(Float3(position, 0.0f), size, color)
    }

    fun drawQuad(position: Float3, size: Float2, color: Float4) {

        if (data.quadIndexCount >= MAX_INDICES) {
            flushAndReset()
        }

        val transform = translation(position) * scale(Float3(size, 1.0f))
        submitQuad(transform, color)
    }

    fun drawQuad(
        position: Float2,
        size: Float2,
        texture: Texture2D,
        tintColor: Float4 = Float4(1.0f),
        tilingFactor: Float = 1.0f
    ) {
        drawQuad(Float3(position, 0.0f), size, texture, tintColor, tilingFactor)
    }

    fun drawQuad(
        position: Float3,
        size: Float2,
        texture: Texture2D,
        tintColor: Float4 = Float4(1.0f),
        tilingFactor: Float = 1.0f
    ) {
        if (data.quadIndexCount >= MAX_INDICES) {
            flushAndReset()
        }

        var textureIndex = findTextureSlotIndexFor(texture)
        if (textureIndex == -1) {
            textureIndex = data.textureSlotIndex
            data.textureSlots[textureIndex] = texture
            data.textureSlotIndex++
        } else {
            data.textureSlotIndex = textureIndex + 1
        }
        submitQuad(
            transform = translation(position) * scale(Float3(size, 1.0f)),
            color = tintColor,
            texIndex = textureIndex.toFloat(),
            tilingFactor = tilingFactor,
            flipTexture = if (texture.flipTexture) 1.0f else 0.0f
        )
    }

    fun drawQuad(
        position: Float2,
        size: Float2,
        subTexture: SubTexture2D,
        tintColor: Float4 = Float4(1.0f),
        tilingFactor: Float = 1.0f
    ) {
        drawQuad(Float3(position, 0.0f), size, subTexture, tintColor, tilingFactor)
    }

    fun drawQuad(
        position: Float3,
        size: Float2,
        subTexture: SubTexture2D,
        tintColor: Float4 = Float4(1.0f),
        tilingFactor: Float = 1.0f
    ) {
        if (data.quadIndexCount >= MAX_INDICES) {
            flushAndReset()
        }

        var textureIndex = findTextureSlotIndexFor(subTexture.texture)
        if (textureIndex == -1) {
            textureIndex = data.textureSlotIndex
            data.textureSlots[textureIndex] = subTexture.texture
            data.textureSlotIndex++
        } else {
            data.textureSlotIndex = textureIndex + 1
        }
        submitQuad(
            transform = translation(position) * scale(Float3(size, 1.0f)),
            color = tintColor,
            texIndex = textureIndex.toFloat(),
            textureCoords = subTexture.texCoords,
            tilingFactor = tilingFactor,
            flipTexture = if (subTexture.flipTexture) 1.0f else 0.0f
        )
    }

    fun drawRotatedQuad(position: Float2, size: Float2, rotation: Float, color: Float4) {
        drawRotatedQuad(Float3(position, 0.0f), size, rotation, color)
    }

    fun drawRotatedQuad(position: Float3, size: Float2, rotation: Float, color: Float4) {
        if (data.quadIndexCount >= MAX_INDICES) {
            flushAndReset()
        }

        submitQuad(
            transform = translation(position) * rotation(Z_AXIS, rotation) * scale(
                Float3(
                    size,
                    1.0f
                )
            ),
            color = color
        )
    }

    fun drawRotatedQuad(
        position: Float2,
        size: Float2,
        rotation: Float,
        texture: Texture2D,
        tilingFactor: Float = 1.0f,
        tintColor: Float4 = Float4(1.0f)
    ) {
        drawRotatedQuad(Float3(position, 0.0f), size, rotation, texture, tilingFactor, tintColor)
    }

    fun drawRotatedQuad(
        position: Float3,
        size: Float2,
        rotation: Float,
        texture: Texture2D,
        tilingFactor: Float = 1.0f,
        tintColor: Float4 = Float4(1.0f)
    ) {
        if (data.quadIndexCount >= MAX_INDICES) {
            flushAndReset()
        }

        var textureIndex = findTextureSlotIndexFor(texture)
        if (textureIndex == -1) {
            textureIndex = data.textureSlotIndex
            data.textureSlots[textureIndex] = texture
            data.textureSlotIndex++
        } else {
            data.textureSlotIndex = textureIndex + 1
        }


        submitQuad(
            transform = translation(position) * rotation(Z_AXIS, rotation) * scale(
                Float3(
                    size,
                    1.0f
                )
            ),
            color = tintColor,
            texIndex = textureIndex.toFloat(),
            tilingFactor = tilingFactor,
            flipTexture = if (texture.flipTexture) 1.0f else 0.0f
        )
    }

    fun drawRotatedQuad(
        position: Float2,
        size: Float2,
        rotation: Float,
        subTexture: SubTexture2D,
        tilingFactor: Float = 1.0f,
        tintColor: Float4 = Float4(1.0f)
    ) {
        drawRotatedQuad(Float3(position, 0.0f), size, rotation, subTexture, tilingFactor, tintColor)
    }

    fun drawRotatedQuad(
        position: Float3,
        size: Float2,
        rotation: Float,
        subTexture: SubTexture2D,
        tilingFactor: Float = 1.0f,
        tintColor: Float4 = Float4(1.0f)
    ) {
        if (data.quadIndexCount >= MAX_INDICES) {
            flushAndReset()
        }

        var textureIndex = findTextureSlotIndexFor(subTexture.texture)
        if (textureIndex == -1) {
            textureIndex = data.textureSlotIndex
            data.textureSlots[textureIndex] = subTexture.texture
            data.textureSlotIndex++
        } else {
            data.textureSlotIndex = textureIndex + 1
        }


        submitQuad(
            transform = translation(position) * rotation(Z_AXIS, rotation) * scale(
                Float3(
                    size,
                    1.0f
                )
            ),
            color = tintColor,
            texIndex = textureIndex.toFloat(),
            textureCoords = subTexture.texCoords,
            tilingFactor = tilingFactor,
            flipTexture = if (subTexture.flipTexture) 1.0f else 0.0f
        )
    }


    fun resetRenderStats() {
        data.stats.reset()
    }

    fun renderStats(): RenderStatistics {
        return data.stats
    }

    private fun flushAndReset() {
        endScene()

        data.quadIndexCount = 0
        data.quadVertexBufferBase.clear()
        data.textureSlotIndex = 1
        data.textureSlots[WHITE_TEXTURE_SLOT_INDEX] = data.whiteTexture

    }

    private fun findTextureSlotIndexFor(
        texture: Texture2D,
    ): Int {
        var textureIndex = -1
        for (i in 1..data.textureSlotIndex) {
            if (data.textureSlots[i] == texture) {
                textureIndex = i
            }
        }
        return textureIndex
    }


    private fun submitQuad(
        transform: Mat4,
        color: Float4,
        textureCoords: Array<Float2> = arrayOf(bottomLeft, bottomRight, topRight, topLeft), // CCW
        texIndex: Float = 0.0f, // 0 = white texture
        tilingFactor: Float = 1.0f,
        flipTexture: Float = 1.0f
    ) {

        for (i in 0 until 4) {
            data.quadVertexBufferBase += QuadVertex(
                position = (transform * data.defaultQuadVertexPositions[i]).xyz,
                texCoord = textureCoords[i],
                color = color,
                texIndex = texIndex,
                tilingFactor = tilingFactor,
                flipTexture = flipTexture
            )
        }

//        data.quadVertexBufferBase += QuadVertex(
//            position = (transform * data.quadVertexPositions[0]).xyz,
//            texCoord = bottomLeft,
//            color = color,
//            texIndex = texIndex,
//            tilingFactor = tilingFactor
//        )
//        data.quadVertexBufferBase += QuadVertex(
//            position = (transform * data.quadVertexPositions[1]).xyz,
//            texCoord = topLeft,
//            color = color,
//            texIndex = texIndex,
//            tilingFactor = tilingFactor
//        )
//        data.quadVertexBufferBase += QuadVertex(
//            position = (transform * data.quadVertexPositions[2]).xyz,
//            texCoord = topRight,
//            color = color,
//            texIndex = texIndex,
//            tilingFactor = tilingFactor
//        )
//        data.quadVertexBufferBase += QuadVertex(
//            position = (transform * data.quadVertexPositions[3]).xyz,
//            texCoord = bottomRight,
//            color = color,
//            texIndex = texIndex,
//            tilingFactor = tilingFactor
//        )

        data.quadIndexCount += 6
        data.stats.quadCount++
    }

}

private fun List<CircleVertex>.toCircleVertexFloatArray(): FloatArray {
    val verticesData = FloatArray(size * CircleVertex.NUMBER_OF_COMPONENTS)
    var lastVertexIndex = 0
    for (circleVertex in this) {
        // worldPosition
        verticesData[lastVertexIndex + 0] = circleVertex.worldPosition.x
        verticesData[lastVertexIndex + 1] = circleVertex.worldPosition.y
        verticesData[lastVertexIndex + 2] = circleVertex.worldPosition.z
        // localPosition
        verticesData[lastVertexIndex + 3] = circleVertex.localPosition.x
        verticesData[lastVertexIndex + 4] = circleVertex.localPosition.y
        // color
        verticesData[lastVertexIndex + 5] = circleVertex.color.r
        verticesData[lastVertexIndex + 6] = circleVertex.color.g
        verticesData[lastVertexIndex + 7] = circleVertex.color.b
        verticesData[lastVertexIndex + 8] = circleVertex.color.a

        // thickness
        verticesData[lastVertexIndex + 9] = circleVertex.thickness
        // fade
        verticesData[lastVertexIndex + 10] = circleVertex.fade

        lastVertexIndex += CircleVertex.NUMBER_OF_COMPONENTS
    }
    return verticesData
}


private fun List<QuadVertex>.toQuadVertexFloatArray(): FloatArray {
    val verticesData = FloatArray(size * QuadVertex.NUMBER_OF_COMPONENTS)
    var lastVertexIndex = 0
    for (quad in this) {
        // position
        verticesData[lastVertexIndex + 0] = quad.position.x
        verticesData[lastVertexIndex + 1] = quad.position.y
        verticesData[lastVertexIndex + 2] = quad.position.z
        // texture coordinate
        verticesData[lastVertexIndex + 3] = quad.texCoord.x
        verticesData[lastVertexIndex + 4] = quad.texCoord.y
        // color
        verticesData[lastVertexIndex + 5] = quad.color.r
        verticesData[lastVertexIndex + 6] = quad.color.g
        verticesData[lastVertexIndex + 7] = quad.color.b
        verticesData[lastVertexIndex + 8] = quad.color.a
        // texIndex
        verticesData[lastVertexIndex + 9] = quad.texIndex
        // a_TilingFactor
        verticesData[lastVertexIndex + 10] = quad.tilingFactor
        // a_FlipTexture
        verticesData[lastVertexIndex + 11] = quad.flipTexture

        lastVertexIndex += QuadVertex.NUMBER_OF_COMPONENTS
    }
    return verticesData
}

private const val MAX_QUADS = 100000
private const val MAX_VERTICES = MAX_QUADS * 4
private const val MAX_INDICES = MAX_QUADS * 6
private const val MAX_TEXTURE_SLOTS = 8 // TODO: query from actual HW

private data class QuadVertex(
    val position: Float3,
    val texCoord: Float2,
    val color: Float4,
    val texIndex: Float,
    val tilingFactor: Float,
    val flipTexture: Float
) {
    companion object {
        // must be equal to properties components count
        const val NUMBER_OF_COMPONENTS =
            /*x,y,z*/ 3 +
                /*u,v*/ 2 +
                /*r,g,b,a*/ 4 +
                /* texIdx */ 1 +
                /* tiling */ 1 +
                /* flipTexture */ 1
    }
}

private data class CircleVertex(
    val worldPosition: Float3,
    val localPosition: Float2,
    val color: Float4,
    val thickness: Float,
    val fade: Float
) {
    companion object {
        const val NUMBER_OF_COMPONENTS =
            /* worldPosition */ 3 +
                /* localPosition */ 2 +
                /* color */ 4 +
                /* thickness */ 1 +
                /* fade */ 1

    }
}

data class CameraData(var viewProjection: Mat4)

@Suppress("ArrayInDataClass")
private data class Renderer2DData(
    val cameraData: CameraData,
    val uniformBuffer: UniformBuffer,
    var whiteTexture: Texture2D,

    var quadVertexArray: VertexArray,
    var quadVertexBuffer: VertexBuffer,
    var quadShader: Shader,
    var quadIndexCount: Int = 0,
    val quadVertexBufferBase: MutableList<QuadVertex> = ArrayList(MAX_VERTICES),

    var circleVertexArray: VertexArray,
    var circleVertexBuffer: VertexBuffer,
    var circleShader: Shader,
    var circleIndexCount: Int = 0,
    val circleVertexBufferBase: MutableList<CircleVertex> = ArrayList(MAX_VERTICES),

    val textureSlots: Array<Texture2D?> = Array(MAX_TEXTURE_SLOTS) { null },
    var textureSlotIndex: Int = 1, // 0 = white texture slot
    val defaultQuadVertexPositions: Array<Float4> = Array(4) { Float4(0.0f) },
    val stats: RenderStatistics = RenderStatistics()
)

data class RenderStatistics(
    var drawCalls: Int = 0,
    var quadCount: Int = 0
) {
    val vertexCount: Int get() = quadCount * 4
    val indexCount: Int get() = quadCount * 6

    fun reset() {
        drawCalls = 0
        quadCount = 0
    }
}

private val bottomLeft = Float2(0.0f, 0.0f)
private val bottomRight = Float2(1.0f, 0.0f)
private val topRight = Float2(1.0f, 1.0f)
private val topLeft = Float2(0.0f, 1.0f)

private val whiteColor = Float4(1.0f)
private val Z_AXIS = Float3(0.0f, 0.0f, 1.0f)

private const val WHITE_TEXTURE_SLOT_INDEX = 0