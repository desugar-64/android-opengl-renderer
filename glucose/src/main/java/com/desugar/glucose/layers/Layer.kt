package com.desugar.glucose.layers

import com.desugar.glucose.core.Timestep
import com.desugar.glucose.events.Event

abstract class Layer(
    protected val debugName: String
) {
    open fun onAttach(surfaceWidth: Int, surfaceHeight: Int) {}
    open fun onDetach() {}
    open fun onUpdate(dt: Timestep) {}
    open fun onEvent(event: Event) {}
    open fun onGuiRender() {}

    override fun toString(): String {
        return "Layer(name=$debugName)"
    }

    protected companion object {
        const val TAG = "Layer"
    }
}