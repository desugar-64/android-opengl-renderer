package com.desugar.glucose

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.opengl.GLSurfaceView
import android.util.Log
import android.util.TypedValue
import android.view.DragEvent
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import com.desugar.glucose.events.*
import com.desugar.glucose.ext.activeButton
import com.desugar.glucose.ext.isFromMouse

@SuppressLint("ViewConstructor")
class GlucoseGLView(context: Context, val graphicsRoot: GraphicsRoot) : GLSurfaceView(context) {
    private val rendererCallback: Renderer = GlucoseGLRenderer(graphicsRoot = graphicsRoot)

    init {
        setEGLContextClientVersion(3)
        setRenderer(rendererCallback)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        Log.d(TAG, "onKeyDown: $keyCode, ${event.displayLabel}, down:${event.downTime}")
        graphicsRoot.onEvent(KeyPressedEvent(keyCode, event.repeatCount))
        requestRender()
        return true
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        Log.d(TAG, "onKeyUp: $keyCode, ${event.displayLabel}")
        graphicsRoot.onEvent(KeyReleasedEvent(keyCode))
        requestRender()
        return super.onKeyUp(keyCode, event)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        Log.d("dispatchTouchEvent", event.toString())

        if (event.isFromMouse) {
            val mouseMovedEvent = when (event.actionMasked) {
                MotionEvent.ACTION_MOVE -> MouseMovedEvent(event.x, event.y)
                else -> null
            }
            if (mouseMovedEvent != null) graphicsRoot.onEvent(mouseMovedEvent)
        } else {
            val x = event.x
            val y = event.y
            val pointerIndex = event.actionIndex
            val pointerId = event.getPointerId(pointerIndex)
            val touchEvent = when (event.action) {
                MotionEvent.ACTION_DOWN -> TouchPressedEvent(x, y, pointerIndex, pointerId)
                MotionEvent.ACTION_MOVE -> TouchMovedEvent(x, y, pointerIndex, pointerId)
                else -> TouchReleasedEvent(x, y, pointerIndex, pointerId)
            }
            graphicsRoot.onEvent(touchEvent)
            requestRender()
        }
        return true
    }

    override fun onDragEvent(event: DragEvent): Boolean {
        Log.d("onDragEvent", event.toString())
        return true
    }

    override fun onHoverEvent(event: MotionEvent): Boolean {
        Log.d("onHoverEvent", event.toString())
        return true
    }

    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
        if (event.isFromMouse) {
            val x = event.x
            val y = event.y
            val scrollX = event.getAxisValue(MotionEvent.AXIS_HSCROLL)
            val scrollY = event.getAxisValue(MotionEvent.AXIS_VSCROLL)
            val mouseEvent = when (event.actionMasked) {
                MotionEvent.ACTION_HOVER_MOVE -> MouseMovedEvent(x, y)
                MotionEvent.ACTION_SCROLL -> MouseScrolledEvent(scrollX, scrollY)
                MotionEvent.ACTION_BUTTON_RELEASE -> {
                    val button: MouseButton = requireNotNull(event.activeButton)
                    MouseButtonReleasedEvent(button)
                }

                MotionEvent.ACTION_BUTTON_PRESS -> {
                    val button: MouseButton = requireNotNull(event.activeButton)
                    MouseButtonPressedEvent(button)
                }

                else -> null
            }
            if (mouseEvent != null) {
                graphicsRoot.onEvent(mouseEvent)
            }

        }
        if (event.isFromSource(InputDevice.SOURCE_CLASS_POINTER)) {
            val action = when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> "down"
                MotionEvent.ACTION_UP -> "up"
                MotionEvent.ACTION_MOVE -> "move"
                MotionEvent.ACTION_HOVER_MOVE -> "hover move"
                MotionEvent.ACTION_SCROLL -> "scroll"
                MotionEvent.ACTION_BUTTON_RELEASE -> "release"
                MotionEvent.ACTION_BUTTON_PRESS -> "press"
                else -> "${event.actionMasked}"
            }
            val button = when (event.buttonState) {
                MotionEvent.BUTTON_PRIMARY -> "left"
                MotionEvent.BUTTON_SECONDARY -> "right"
                MotionEvent.BUTTON_TERTIARY -> "middle"
                else -> "${event.buttonState}"
            }
            val actionButton = when (event.actionButton) {
                MotionEvent.BUTTON_PRIMARY -> "left"
                MotionEvent.BUTTON_SECONDARY -> "right"
                MotionEvent.BUTTON_TERTIARY -> "middle"
                else -> "${event.actionButton}"
            }
            Log.d(TAG, "dispatchGenericMotionEvent: mouse event")
            Log.d(TAG, "                    action: $action")
            Log.d(TAG, "                    button: $button")
            Log.d(TAG, "             action button: $actionButton")
            Log.d(TAG, "                      info: $event")
            requestRender()
        }

        return super.dispatchGenericMotionEvent(event)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isFocusable = true
        isFocusableInTouchMode = true
        requestFocus()
    }

    override fun onDetachedFromWindow() {
        graphicsRoot.onDestroy()
        super.onDetachedFromWindow()
    }

    override fun onDrawForeground(canvas: Canvas) {
        super.onDrawForeground(canvas)
        return
        val size =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56f, resources.displayMetrics)
        val paint = Paint().apply {
            style = Paint.Style.FILL
            color = Color.YELLOW
            strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0.5f, resources.displayMetrics)
        }
        canvas.drawRect(0f, 0f, size, size, paint)
        canvas.drawCircle(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                100f,
                resources.displayMetrics
            ),
            height - TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                100f,
                resources.displayMetrics
            ),
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                4f,
                resources.displayMetrics
            ),
            paint
        )
        paint.color = Color.MAGENTA
        canvas.drawLine(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                160f,
                resources.displayMetrics
            ),
            height - TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                100f,
                resources.displayMetrics
            ),
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                260f,
                resources.displayMetrics
            ),
            height - TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                440f,
                resources.displayMetrics
            ),
            paint
        )
    }

    private companion object {
        private const val TAG = "GlucoseView"
    }
}