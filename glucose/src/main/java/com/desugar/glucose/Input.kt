package com.desugar.glucose

import android.util.Log
import com.desugar.glucose.events.MouseButton

abstract class Input {

    abstract fun isKeyPressed(keyCode: Int): Boolean
    abstract fun isMouseButtonPressed(button: MouseButton): Boolean
    abstract fun getMouseX(): Float
    abstract fun getMouseY(): Float
    abstract fun getMouseScrollX(): Float
    abstract fun getMouseScrollY(): Float

    companion object {
        private var instance: Input? = null

        fun init(inputListener: KeyInputListener, mouseInputListener: MouseInputListener): Input {
            val ins: Input
            synchronized(Input) {
                ins = if (instance != null) {
                    requireNotNull(instance)
                } else {
                    AndroidInput(inputListener, mouseInputListener)
                }
            }
            instance = ins
            return ins
        }

        fun isKeyPressed(keyCode: Int): Boolean {
            val ins = instance
            requireNotNull(ins) { "Input not initialized" }
            return ins.isKeyPressed(keyCode)
        }

        fun isMouseButtonPressed(button: MouseButton): Boolean {
            val ins = instance
            requireNotNull(ins) { "Input not initialized" }
            return ins.isMouseButtonPressed(button)
        }

        fun getMouseX(): Float {
            val ins = instance
            requireNotNull(ins) { "Input not initialized" }
            return ins.getMouseX()
        }

        fun getMouseY(): Float {
            val ins = instance
            requireNotNull(ins) { "Input not initialized" }
            return ins.getMouseY()
        }

        fun getMouseScrollX(): Float {
            val ins = instance
            requireNotNull(ins) { "Input not initialized" }
            return ins.getMouseScrollX()
        }

        fun getMouseScrollY(): Float {
            val ins = instance
            requireNotNull(ins) { "Input not initialized" }
            return ins.getMouseScrollY()
        }

        fun deinit() {
            instance = null
        }
    }
}

interface KeyInputListener {
    fun onKeyPressed(keyCode: Int)
    fun onKeyReleased(keyCode: Int)
    fun isKeyPressed(keyCode: Int): Boolean
}

interface MouseInputListener {
    fun onButtonPressed(button: MouseButton)
    fun onButtonReleased(button: MouseButton)
    fun onMouseMoved(x: Float, y: Float)
    fun onMouseScrolled(deltaX: Float, deltaY: Float)
    fun isButtonPressed(mouseButton: MouseButton): Boolean
    val mouseX: Float
    val mouseY: Float
    val mouseScrollX: Float
    val mouseScrollY: Float
}

// TODO: Move this to a platform module
class KeyInputListenerImpl : KeyInputListener {
    private val pressedKeys = IntArray(MAX_PRESSED_KEYS) { KEYCODE_NONE }

    override fun onKeyPressed(keyCode: Int) {
        if (tryRegisterPressedKeyCode(keyCode).not()) {
            Log.w(TAG, "Unable to register $keyCode, reached limit of pressed keys")
        }
    }

    override fun onKeyReleased(keyCode: Int) {
        synchronized(KeyInputListenerImpl) {
            val slot = pressedKeys.indexOfFirst { it == keyCode }
            if (slot != -1) {
                pressedKeys[slot] = KEYCODE_NONE
            }
        }
    }

    override fun isKeyPressed(keyCode: Int): Boolean {
        return synchronized(KeyInputListenerImpl) { pressedKeys.contains(keyCode) }
    }

    private fun tryRegisterPressedKeyCode(keyCode: Int): Boolean {
        return synchronized(KeyInputListenerImpl) {
            val availableSlot = pressedKeys.indexOfFirst { it == KEYCODE_NONE || it == keyCode }
            if (availableSlot != -1) {
                pressedKeys[availableSlot] = keyCode
            }
            availableSlot != -1
        }
    }

    companion object {
        private const val TAG = "AndroidKeyInputListener"
        private const val MAX_PRESSED_KEYS = 5
        private const val KEYCODE_NONE = -1
    }
}

class MouseInputListenerImpl : MouseInputListener {
    private var _activeButton: MouseButton? = null
    private var _mouseX: Float = 0.0f
    private var _mouseY: Float = 0.0f
    private var _mouseScrollX: Float = 0.0f
    private var _mouseScrollY: Float = 0.0f

    override val mouseX: Float get() = _mouseX
    override val mouseY: Float get() = _mouseY
    override val mouseScrollX: Float get() = _mouseScrollX
    override val mouseScrollY: Float get() = _mouseScrollY

    override fun onButtonPressed(button: MouseButton) {
        _activeButton = button
    }

    override fun onButtonReleased(button: MouseButton) {
        _activeButton = null
    }

    override fun onMouseMoved(x: Float, y: Float) {
        _mouseX = x
        _mouseY = y
    }

    override fun onMouseScrolled(deltaX: Float, deltaY: Float) {
        _mouseScrollX = deltaX
        _mouseScrollY = deltaY
    }

    override fun isButtonPressed(mouseButton: MouseButton): Boolean {
        return _activeButton == mouseButton
    }
}

private class AndroidInput(
    private val keyInputListener: KeyInputListener,
    private val mouseInputListener: MouseInputListener
) : Input() {

    override fun isKeyPressed(keyCode: Int): Boolean {
        return keyInputListener.isKeyPressed(keyCode)
    }

    override fun isMouseButtonPressed(button: MouseButton): Boolean {
        return mouseInputListener.isButtonPressed(button)
    }

    override fun getMouseX(): Float {
        return mouseInputListener.mouseX
    }

    override fun getMouseY(): Float {
        return mouseInputListener.mouseY
    }

    override fun getMouseScrollX(): Float {
        return mouseInputListener.mouseScrollX
    }

    override fun getMouseScrollY(): Float {
        return mouseInputListener.mouseScrollY
    }
}