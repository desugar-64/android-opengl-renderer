package com.desugar.glucose.renderer

import dev.romainguy.kotlin.math.Float2

class SubTexture2D(
    val texture: Texture2D
) {
    val texCoords: Array<Float2> = Array(4) { index ->
        when (index) {
            0 -> Float2(0.0f, 0.0f)
            1 -> Float2(1.0f, 0.0f)
            2 -> Float2(1.0f, 1.0f)
            3 -> Float2(0.0f, 1.0f)
            else -> error("Texture coordinates index overflow")
        }
    }

    val flipTexture: Boolean get() = texture.flipTexture

    constructor(texture: Texture2D, min: Float2, max: Float2) : this(texture) {
        texCoords[0] = min
        texCoords[1] = Float2(max.x, min.y)
        texCoords[2] = Float2(max.x, max.y)
        texCoords[3] = Float2(min.x, max.y)
    }

    companion object {
        fun createFromCoords(
            texture: Texture2D,
            coords: Float2,
            cellSize: Float2,
            spriteSize: Float2 = Float2(1.0f)
        ): SubTexture2D {
            val x = coords.x
            val y = coords.y
            val sheetWidth = texture.width
            val sheetHeight = texture.height
            val cellWidth = cellSize.x
            val cellHeight = cellSize.y

            val min = Float2(
                x = x * cellWidth / sheetWidth,
                y = y * cellHeight / sheetHeight
            ) // bottom left
            val max = Float2(
                x = (x + spriteSize.x) * cellWidth / sheetWidth,
                y = (y + spriteSize.y) * cellHeight / sheetHeight
            ) // top right

            return SubTexture2D(texture = texture, min = min, max = max)
        }
    }
}