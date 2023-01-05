package com.desugar.glucose.events

abstract class Event {
    enum class EventType {
        None,
        WindowClose, WindowResize, WindowFocus, WindowLostFocus, WindowMoved,
        AppTick, AppUpdate, AppRender,
        KeyPressed, KeyReleased,
        TouchPressed, TouchMoved, TouchReleased,
        MouseMoved, MouseButtonPressed, MouseButtonReleased, MouseScrolled
    }

    enum class EventCategory(internal val bit: Int) {
        None(0),
        EventCategoryApplication(bit(1)),
        EventCategoryInput(bit(2)),
        EventCategoryKeyboard(bit(3)),
        EventCategoryTouch(bit(4)),
        EventCategoryMouse(bit(5)),
        EventCategoryMouseButton(bit(6));
    }

    var isHandled: Boolean = false

    abstract val name: String
    abstract val type: EventType
    abstract val categoryFlags: Int

    fun isInCategory(category: EventCategory): Boolean {
        return categoryFlags and category != 0
    }

    override fun toString(): String {
        return "Event(name='$name', isHandled=$isHandled, type=$type, categoryFlags=$categoryFlags)"
    }

    private infix fun Int.and(category: EventCategory): Int {
        return this and category.bit
    }

    infix fun Int.or(category: EventCategory): Int {
        return this or category.bit
    }

    infix fun EventCategory.or(other: EventCategory): Int {
        return this.bit or other.bit
    }
}

@JvmInline
value class EventDispatcher(
    val event: Event
) {
    inline fun <reified E : Event> dispatch(func: (E) -> Boolean): Boolean {
        return if (event is E) {
            event.isHandled = func.invoke(event)
            true
        } else {
            false
        }
    }
}

fun bit(x: Int): Int {
    return 1 shl x
}