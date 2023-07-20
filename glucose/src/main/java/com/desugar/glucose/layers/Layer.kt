package com.desugar.glucose.layers

import android.content.res.Resources
import android.util.DisplayMetrics
import androidx.annotation.CallSuper
import com.desugar.glucose.camera.OrthographicCameraController
import com.desugar.glucose.core.Timestep
import com.desugar.glucose.events.Event
import com.desugar.glucose.events.EventDispatcher
import com.desugar.glucose.events.WindowResizeEvent
import com.desugar.glucose.renderer.Renderer2D

abstract class Layer(
    private val debugName: String,
    private val displayMetrics: DisplayMetrics = Resources.getSystem().displayMetrics
) {

    var isVisible: Boolean = true
    private var renderScope: RenderScope? = null

    lateinit var cameraController: OrthographicCameraController

    @CallSuper
    open fun onAttach(surfaceWidth: Int, surfaceHeight: Int) {
        cameraController = OrthographicCameraController.createWorldUnitsController(
            viewportWidth = surfaceWidth,
            viewportHeight = surfaceHeight,
        )
        cameraController.disableMovement = true
        cameraController.zoomLevel = 1.0f
        cameraController.updateCameraProjection()
        renderScope = RenderScope(surfaceWidth, surfaceHeight, displayMetrics.density)
    }

    open fun onDetach() {}

    internal fun onUpdate(dt: Timestep) {
        Renderer2D.resetRenderStats()
        cameraController.onUpdate(dt)
        renderScope?.onUpdate(dt)
    }
    abstract fun RenderScope.onUpdate(dt: Timestep)

    @CallSuper
    open fun onEvent(event: Event) {
        EventDispatcher(event).dispatch<WindowResizeEvent> { resize ->
            onWindowResize(resize.width, resize.height)
            false
        }
    }

    private fun onWindowResize(width: Int, height: Int) {
        cameraController.onVisibleBoundsResize(width, height)
        renderScope?.let { scope ->
            if (scope.viewportWidth != width || scope.viewportHeight != height) {
                renderScope = RenderScope(width, height, displayMetrics.density)
            }
        }
    }

    open fun onGuiRender() {}

    override fun toString(): String {
        return "Layer(name=$debugName)"
    }

    protected companion object {
        const val TAG = "Layer"
    }
}