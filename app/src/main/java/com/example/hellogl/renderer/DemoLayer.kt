package com.example.hellogl.renderer

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Handler
import android.os.Looper
import android.view.animation.LinearInterpolator
import com.desugar.glucose.assets.AssetManager
import com.desugar.glucose.camera.OrthographicCameraController
import com.desugar.glucose.core.Timestep
import com.desugar.glucose.layers.Layer
import com.desugar.glucose.layers.RenderScope
import com.desugar.glucose.renderer.RenderCommand
import com.desugar.glucose.renderer.Renderer2D
import dev.romainguy.kotlin.math.Float2
import dev.romainguy.kotlin.math.Float4

class DemoLayer(assetManager: AssetManager) : Layer("DemoLayer") {
    private val greenColor = Float4(0.0f, 1.0f, 0.0f, 1.0f)
    private val redColor = Float4(1.0f, 0.0f, 0.0f, 1.0f)
    private val blueColor = Float4(0.0f, 0.0f, 1.0f, 1.0f)

    private var cellHeight = 0
    private var cellWidth = 0

    private var rotationX: Float = 0.0f
    private var rotationY: Float = 0.0f
    private var rotationZ: Float = 0.0f

    override fun onAttach(surfaceWidth: Int, surfaceHeight: Int) {
        super.onAttach(surfaceWidth, surfaceHeight)

       Handler(Looper.getMainLooper()).post {
           val animator = ValueAnimator.ofFloat(0f, 360f)
           animator.duration = 4000L
           animator.repeatCount = ValueAnimator.INFINITE
           animator.repeatMode = ValueAnimator.RESTART
           animator.interpolator = LinearInterpolator()
           animator.addUpdateListener { value ->
               rotationX = value.animatedValue as Float
               rotationY = value.animatedValue as Float
               rotationZ = value.animatedValue as Float
           }
           animator.start()
       }

        cameraController =
            OrthographicCameraController.createPixelUnitsController(surfaceWidth, surfaceHeight)
        cellHeight = surfaceHeight / 4
        cellWidth = surfaceWidth / 3
    }

    override fun RenderScope.onUpdate(dt: Timestep) {
        RenderCommand.setClearColor(greenColor)
        RenderCommand.clear()


        scene2D(cameraController.camera) {
            // Grid
            drawGrid()

            drawCell0()
            drawCell1()
        }
    }

    context(RenderScope)
    private fun Renderer2D.drawGrid() {
        for (y in cellHeight until viewportHeight step cellHeight) {
            drawLine(
                start = Float2(0f, y.toFloat()),
                end = Float2(viewportWidth.toFloat(), y.toFloat()),
                color = blueColor,
                thickness = 1.dpToPx()
            )
            for (x in 0 until viewportWidth step cellWidth) {
                drawLine(
                    start = Float2(x.toFloat(), 0f),
                    end = Float2(x.toFloat(), viewportHeight.toFloat()),
                    color = blueColor,
                    thickness = 1.dpToPx()
                )
            }
        }
    }

    context(RenderScope)
    private fun Renderer2D.drawCell0() {
        val rectSize = 72.dpToPx()
        drawRotatedQuad(
            position = Float2(
                x = cellWidth / 2f,
                y = cellHeight / 2f
            ),
            size = Float2(rectSize),
            rotation = rotationX,
            color = redColor
        )
    }

    context(RenderScope)
    private fun Renderer2D.drawCell1() {
        val rectSize = 72.dpToPx()
        drawRotatedQuad(
            position = Float2(
                x = cellWidth / 2f + cellWidth,
                y = cellHeight / 2f
            ),
            size = Float2(rectSize),
            rotation = rotationX,
            color = blueColor
        )
    }
}