package com.desugar.glucose

import android.os.SystemClock
import com.desugar.glucose.assets.AssetManager
import com.desugar.glucose.core.Timestep
import com.desugar.glucose.events.*
import com.desugar.glucose.layers.Layer
import com.desugar.glucose.layers.LayerStack
import com.desugar.glucose.renderer.Renderer

open class GraphicsRoot(
    private val layerStack: LayerStack = LayerStack(),
    private val assetManager: AssetManager
) {

    private val keyInputListener = KeyInputListenerImpl()
    private val mouseInputListener = MouseInputListenerImpl()
    private var lastFrameTime: Long = 0L

    fun onCreate(width: Int, height: Int) {
        Renderer.init(assetManager)
        Input.init(keyInputListener, mouseInputListener)
        layerStack.layers.forEach { layer -> layer.onAttach(width, height) }
    }

    fun run() {
        val time = SystemClock.elapsedRealtime() // Platform.getTime()
        val dt = time - lastFrameTime
        lastFrameTime = time
        layerStack.layers.forEach { layer ->
            if (layer.isVisible) {
                layer.onUpdate(Timestep(dt.toFloat()))
                layer.onGuiRender()
            }
        }
    }

    fun pushLayer(layer: Layer) {
        layerStack.pushLayer(layer)
    }

    fun pushOverlay(overlay: Layer) {
        layerStack.pushOverlay(overlay)
    }

    fun onEvent(event: Event) {
        val dispatcher = EventDispatcher(event)
        dispatcher.dispatch<KeyEvent> { keyEvent ->
            when (keyEvent) {
                is KeyPressedEvent -> keyInputListener.onKeyPressed(keyEvent.keyCode)
                is KeyReleasedEvent -> keyInputListener.onKeyReleased(keyEvent.keyCode)
            }
            false
        }
        dispatcher.dispatch<MouseEvent> { mouseEvent ->
            when (mouseEvent) {
                is MouseButtonPressedEvent -> mouseInputListener.onButtonPressed(mouseEvent.button)
                is MouseButtonReleasedEvent -> mouseInputListener.onButtonReleased(mouseEvent.button)
                is MouseMovedEvent -> mouseInputListener.onMouseMoved(
                    mouseEvent.mouseX,
                    mouseEvent.mouseY
                )

                is MouseScrolledEvent -> mouseInputListener.onMouseScrolled(
                    mouseEvent.offsetX,
                    mouseEvent.offsetY
                )
            }
            false
        }

        for (idx in layerStack.layers.lastIndex downTo 0) {
            layerStack.layers[idx].onEvent(event)
            if (event.isHandled) {
                break
            }
        }
    }

    fun onDestroy() {
        Renderer.shutDown()
        layerStack.layers.forEach { it.onDetach() }
        Input.deinit()
    }
}