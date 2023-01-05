package com.desugar.glucose.ext

import android.view.InputDevice
import android.view.MotionEvent
import com.desugar.glucose.events.MouseButton

val MotionEvent.isFromMouse: Boolean
    get() {
        val mouseDevice = isFromSource(InputDevice.SOURCE_CLASS_POINTER)
                && isFromSource(InputDevice.SOURCE_MOUSE)
        val toolMouse = getToolType(actionIndex) == MotionEvent.TOOL_TYPE_MOUSE
        return mouseDevice || toolMouse
    }

val MotionEvent.activeButton: MouseButton?
    get() {
        val button = when (buttonState) {
            MotionEvent.BUTTON_PRIMARY -> MouseButton.LEFT
            MotionEvent.BUTTON_SECONDARY -> MouseButton.RIGHT
            MotionEvent.BUTTON_TERTIARY -> MouseButton.MIDDLE
            else -> null
        }
        val actionButton = when (actionButton) {
            MotionEvent.BUTTON_PRIMARY -> MouseButton.LEFT
            MotionEvent.BUTTON_SECONDARY -> MouseButton.RIGHT
            MotionEvent.BUTTON_TERTIARY -> MouseButton.MIDDLE
            else -> null
        }
        return (button ?: actionButton)
    }