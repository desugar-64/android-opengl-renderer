package com.desugar.glucose.camera

import android.opengl.Matrix
import com.desugar.glucose.Input
import com.desugar.glucose.core.Timestep
import com.desugar.glucose.events.*
import dev.romainguy.kotlin.math.Float2
import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.radians
import kotlin.math.cos
import kotlin.math.sin

class OrthographicCameraController {
    var camera: OrthographicCamera
        private set
    var aspectRatio: Float = 1.0f
        private set

    var zoomLevel: Float = 1.0f
    var rotation: Boolean = false
        private set

    var disableMovement: Boolean = false

    private var cameraRotation: Float = 0.0f

    var cameraPosition: Float3 = Float3(Float2(0.0f), -1.0f)
    private var cameraTranslationSpeed: Float = 1.0f

    private var cameraRotationSpeed: Float = 180.0f

    private var touchX: Float = 0.0f
    private var touchY: Float = 0.0f
    private var mouseX: Float = 0.0f
    private var mouseY: Float = 0.0f
    private var viewPortWidth: Int = 0
    private var viewPortHeight: Int = 0
    private var pixelCoordinates: Boolean = false
    val orthographicSize: Float
        get() =  viewPortHeight / 2f / zoomLevel

    constructor(aspectRatio: Float, height: Int, rotation: Boolean) {
        this.aspectRatio = aspectRatio
        this.rotation = rotation
        this.viewPortHeight = height

        camera = OrthographicCamera(
            left = -aspectRatio * orthographicSize,
            right = aspectRatio * orthographicSize,
            bottom = -orthographicSize,
            top = orthographicSize
        )
        onVisibleBoundsResize(width = 0, height = height)
    }

    constructor(width: Int, height: Int) {
        disableMovement = true
        pixelCoordinates = true
        camera = OrthographicCamera(
            left = 0f,
            right = width.toFloat(),
            bottom = 0f,
            top =  height.toFloat()
        )
        onVisibleBoundsResize(width, height)
    }

    fun onUpdate(dt: Timestep) {
        if (disableMovement) {
            return
        }
        if (Input.isKeyPressed(android.view.KeyEvent.KEYCODE_A)) {
            cameraPosition.x -= cos(radians(cameraRotation)) * cameraTranslationSpeed * dt.seconds
            cameraPosition.y -= sin(radians(cameraRotation)) * cameraTranslationSpeed * dt.seconds
//            cameraPosition -= Float3(x = cameraTranslationSpeed * dt.seconds, y = 0.0f, z = 0.0f)
        } else if (Input.isKeyPressed(android.view.KeyEvent.KEYCODE_D)) {
            cameraPosition.x += cos(radians(cameraRotation)) * cameraTranslationSpeed * dt.seconds
            cameraPosition.y += sin(radians(cameraRotation)) * cameraTranslationSpeed * dt.seconds
//            cameraPosition += Float3(x = cameraTranslationSpeed * dt.seconds, y = 0.0f, z = 0.0f)
        }

        if (Input.isKeyPressed(android.view.KeyEvent.KEYCODE_W)) {
            cameraPosition.x += -sin(radians(cameraRotation)) * cameraTranslationSpeed * dt.seconds
            cameraPosition.y += cos(radians(cameraRotation)) * cameraTranslationSpeed * dt.seconds
//            cameraPosition += Float3(x = 0.0f, y = cameraTranslationSpeed * dt.seconds, z = 0.0f)
        } else if (Input.isKeyPressed(android.view.KeyEvent.KEYCODE_S)) {
            cameraPosition.x -= -sin(radians(cameraRotation)) * cameraTranslationSpeed * dt.seconds
            cameraPosition.y -= cos(radians(cameraRotation)) * cameraTranslationSpeed * dt.seconds
//            cameraPosition -= Float3(x = 0.0f, y = cameraTranslationSpeed * dt.seconds, z = 0.0f)
        }



        if (Input.isMouseButtonPressed(MouseButton.LEFT)) {
            val mouseX = Input.getMouseX()
            val mouseY = Input.getMouseY()
            onMouseDrag(mouseX, mouseY)
        } else {
            mouseX = Input.getMouseX()
            mouseY = Input.getMouseY()
        }

        if (rotation) {
            if (Input.isKeyPressed(android.view.KeyEvent.KEYCODE_Q)) {
                cameraRotation -= cameraRotationSpeed * dt.seconds
            } else if (Input.isKeyPressed(android.view.KeyEvent.KEYCODE_E)) {
                cameraRotation += cameraRotationSpeed * dt.seconds
            }

            if (cameraRotation > 180.0f) {
                cameraRotation -= 360.0f
            } else if (cameraRotation <= -180.0f) {
                cameraRotation += 360.0f
            }
            camera.rotation = cameraRotation
        }

        camera.position = cameraPosition
    }

    fun onEvent(event: Event): Unit = with(EventDispatcher(event)) {
        if (!disableMovement) {
            dispatch<MouseScrolledEvent> { onMouseScrolled(it) }
            dispatch<TouchPressedEvent> { onTouchDown(it) }
            dispatch<TouchMovedEvent> { onTouchMove(it) }
        }
    }

    fun onVisibleBoundsResize(width: Int, height: Int) {
        viewPortWidth = width
        viewPortHeight = height
        if (width != 0 && height != 0) {
            aspectRatio = width / height.toFloat()
        }
        updateCameraProjection()
    }

    fun updateCameraProjection() {
        if (pixelCoordinates) {
            camera.setProjection(
                left = 0f,
                right = viewPortWidth.toFloat(),
                bottom = 0f,
                top =  viewPortHeight.toFloat()
            )
        } else {
            camera.setProjection(
                left = -aspectRatio * orthographicSize,
                right = aspectRatio * orthographicSize,
                bottom = -orthographicSize,
                top = orthographicSize
            )
        }
    }

    private fun onMouseScrolled(event: MouseScrolledEvent): Boolean {
        zoomLevel -= event.offsetY * 0.25f
        zoomLevel = zoomLevel.coerceAtLeast(0.25f)
        updateCameraProjection()
        return false
    }

    private fun onTouchDown(event: TouchPressedEvent): Boolean {
        touchX = event.x
        touchY = event.y
        return false
    }

    private fun onTouchMove(event: TouchMovedEvent): Boolean {
        val dx = event.x - touchX
        val dy = event.y - touchY
        val glX = dx / viewPortWidth
        val glY = dy / viewPortHeight
        val ratio = viewPortWidth / viewPortHeight.toFloat()

        cameraPosition += Float3((glX * ratio) * 1.5f, (glY / ratio) * 1.5f, 0.0f)
        updateCameraProjection()

        touchX = event.x
        touchY = event.y
        return false
    }

    private fun onMouseDrag(x: Float, y: Float): Boolean {
        val dx = x - mouseX
        val dy = y - mouseY
        val glX = dx / viewPortWidth
        val glY = dy / viewPortHeight
        val ratio = viewPortWidth / viewPortHeight.toFloat()

        cameraPosition += Float3((-glX * ratio) * 1.5f, (glY / ratio) * 1.5f, 0.0f)
        mouseX = x
        mouseY = y
        updateCameraProjection()
        return true
    }

}