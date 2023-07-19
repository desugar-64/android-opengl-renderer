package com.desugar.glucose

object DisplayUtil {
    const val HAIRLINE = 1.0f

    fun isHairlineThin(valuePx: Float): Boolean {
        return valuePx <= HAIRLINE
    }

    fun clampToHairline(valuePx: Float): Float {
        return if (isHairlineThin(valuePx)) HAIRLINE else valuePx
    }

}