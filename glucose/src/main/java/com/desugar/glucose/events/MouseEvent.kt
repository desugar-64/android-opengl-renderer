package com.desugar.glucose.events

import android.view.MotionEvent
import com.desugar.glucose.events.Event.EventCategory.*
import com.desugar.glucose.events.Event.EventType.*

enum class MouseButton(val id: Int) {
    LEFT(MotionEvent.BUTTON_PRIMARY), RIGHT(MotionEvent.BUTTON_SECONDARY), MIDDLE(MotionEvent.BUTTON_TERTIARY)
}

sealed class MouseEvent : Event() {
    override val categoryFlags: Int = EventCategoryMouse or EventCategoryInput
}

data class MouseMovedEvent(val mouseX: Float, val mouseY: Float) : MouseEvent() {
    override val name: String = "MouseMovedEvent"
    override val type: EventType = MouseMoved
}

data class MouseScrolledEvent(val offsetX: Float, val offsetY: Float) : MouseEvent() {
    override val name: String = "MouseScrolledEvent"
    override val type: EventType = MouseScrolled
}

sealed class MouseButtonEvent : MouseEvent() {
    abstract val button: MouseButton
    override val categoryFlags: Int =
        EventCategoryMouse or EventCategoryMouseButton or EventCategoryInput
}

data class MouseButtonPressedEvent(override val button: MouseButton) : MouseButtonEvent() {
    override val name: String = "MouseButtonPressedEvent"
    override val type: EventType = MouseButtonPressed
}

data class MouseButtonReleasedEvent(override val button: MouseButton) : MouseButtonEvent() {
    override val name: String = "MouseButtonReleasedEvent"
    override val type: EventType = MouseButtonReleased
}