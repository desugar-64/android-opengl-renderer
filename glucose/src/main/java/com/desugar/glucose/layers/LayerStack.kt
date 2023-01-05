package com.desugar.glucose.layers

class LayerStack {
    //    private var layerInsertIndex: Int = 0
    private val _layers: ArrayDeque<Layer> = ArrayDeque()

    val layers: List<Layer> get() = _layers

    fun pushLayer(layer: Layer) {
        _layers.add(layer)
//        layerInsertIndex++
    }

    fun pushOverlay(overlay: Layer) {
        _layers.addLast(overlay)
    }

    fun popLayer(layer: Layer) {
        if (_layers.remove(layer)) {
            layer.onDetach()
        }
    }

    fun popOverlay(overlay: Layer) {
        popLayer(overlay)
    }
}