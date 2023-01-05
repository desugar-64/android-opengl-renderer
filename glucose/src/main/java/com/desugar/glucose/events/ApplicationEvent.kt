package com.desugar.glucose.events

import com.desugar.glucose.events.Event.EventCategory.EventCategoryApplication
import com.desugar.glucose.events.Event.EventType.*

sealed class ApplicationEvent : Event() {
    override val categoryFlags: Int = EventCategoryApplication.bit
}

data class WindowResizeEvent(val width: Int, val height: Int) : ApplicationEvent() {
    override val name: String = "WindowResizeEvent"
    override val type: EventType = WindowResize
}

object WindowCloseEvent : ApplicationEvent() {
    override val name: String = "WindowCloseEvent"
    override val type: EventType = WindowClose
}

data class WindowFocusEvent(val isFocused: Boolean) : ApplicationEvent() {
    override val name: String = "WindowFocusEvent"
    override val type: EventType = if (isFocused) WindowFocus else WindowLostFocus
}

data class WindowMovedEvent(val x: Int, val y: Int) : ApplicationEvent() {
    override val name: String = "WindowMovedEvent"
    override val type: EventType = WindowMoved
}

object AppTickEvent : ApplicationEvent() {
    override val name: String = "AppTickEvent"
    override val type: EventType = AppTick
}

object AppUpdateEvent : ApplicationEvent() {
    override val name: String = "AppUpdateEvent"
    override val type: EventType = AppUpdate
}

object AppRenderEvent : ApplicationEvent() {
    override val name: String = "AppRenderEvent"
    override val type: EventType = AppRender
}