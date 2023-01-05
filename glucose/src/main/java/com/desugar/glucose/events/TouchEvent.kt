package com.desugar.glucose.events

sealed class TouchEvent : Event() {
    abstract val pointerIndex: Int
    abstract val pointerId: Int
    override val categoryFlags: Int =
        EventCategory.EventCategoryTouch or EventCategory.EventCategoryInput
}

data class TouchPressedEvent(
    val x: Float,
    val y: Float,
    override val pointerIndex: Int,
    override val pointerId: Int
) : TouchEvent() {
    override val name: String = "TouchPressedEvent"
    override val type: EventType = EventType.TouchPressed
}

data class TouchMovedEvent(
    val x: Float,
    val y: Float,
    override val pointerIndex: Int,
    override val pointerId: Int
) : TouchEvent() {
    override val name: String = "TouchMovedEvent"
    override val type: EventType = EventType.TouchMoved
}

data class TouchReleasedEvent(
    val x: Float,
    val y: Float,
    override val pointerIndex: Int,
    override val pointerId: Int
) : TouchEvent() {
    override val name: String = "TouchReleasedEvent"
    override val type: EventType = EventType.TouchReleased
}