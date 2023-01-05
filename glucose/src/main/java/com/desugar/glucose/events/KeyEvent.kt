package com.desugar.glucose.events

import com.desugar.glucose.events.Event.EventCategory.EventCategoryInput
import com.desugar.glucose.events.Event.EventCategory.EventCategoryKeyboard
import com.desugar.glucose.events.Event.EventType.KeyPressed
import com.desugar.glucose.events.Event.EventType.KeyReleased

sealed class KeyEvent : Event() {
    abstract val keyCode: Int
    override val categoryFlags: Int = EventCategoryKeyboard or EventCategoryInput
}

data class KeyPressedEvent(override val keyCode: Int, val repeatCount: Int) : KeyEvent() {
    override val name: String = "KeyPressedEvent"
    override val type: EventType = KeyPressed

}

data class KeyReleasedEvent(override val keyCode: Int) : KeyEvent() {
    override val name: String = "KeyReleasedEvent"
    override val type: EventType = KeyReleased
}