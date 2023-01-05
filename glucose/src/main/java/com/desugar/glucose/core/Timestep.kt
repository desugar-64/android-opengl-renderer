package com.desugar.glucose.core

@JvmInline
value class Timestep(
    private val timeMs: Float = 0.0f
) {
    val seconds: Float get() = timeMs * 0.001f
    val milliseconds: Float get() = timeMs
}