package com.example.hellogl.renderer

import com.desugar.glucose.assets.AssetManager
import com.desugar.glucose.camera.OrthographicCameraController
import com.desugar.glucose.core.Timestep
import com.desugar.glucose.events.Event
import com.desugar.glucose.events.EventDispatcher
import com.desugar.glucose.events.WindowResizeEvent
import com.desugar.glucose.layers.Layer
import com.desugar.glucose.layers.RenderScope
import com.desugar.glucose.renderer.Framebuffer
import com.desugar.glucose.renderer.FramebufferAttachmentSpecification
import com.desugar.glucose.renderer.FramebufferSpecification
import com.desugar.glucose.renderer.RenderCommand
import com.desugar.glucose.renderer.Renderer
import com.desugar.glucose.renderer.Renderer2D
import com.desugar.glucose.renderer.SubTexture2D
import com.desugar.glucose.renderer.Texture2D
import dev.romainguy.kotlin.math.Float2
import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.Float4
import kotlin.properties.Delegates
import kotlin.random.Random

class Sandbox2D(
    private val assetManager: AssetManager
) : Layer("Sandbox2D") {
    private var offScreenCameraController: OrthographicCameraController by Delegates.notNull()
    private var offScreenFramebuffer: Framebuffer by Delegates.notNull()

    private var grassTexture: Texture2D by Delegates.notNull()
    private var stairsTexture: SubTexture2D by Delegates.notNull()
    private var barrelTexture: SubTexture2D by Delegates.notNull()

    private var treeTexture: SubTexture2D by Delegates.notNull()

    private var rotation: Float = 0.0f
    private val random = Random.Default
    private val lines: MutableList<Float4> = mutableListOf()

    override fun onAttach(surfaceWidth: Int, surfaceHeight: Int) {
        super.onAttach(surfaceWidth, surfaceHeight)

        var startPoint = Float2(random.nextFloat() * surfaceWidth, random.nextFloat() * surfaceHeight)
        repeat(2) {
            val endPoint =
                Float2(random.nextFloat() * surfaceWidth, random.nextFloat() * surfaceHeight)
            lines.add(Float4(startPoint.x, startPoint.y, endPoint.x, endPoint.y))
            startPoint = endPoint
       }

        grassTexture = Texture2D.create("texture/texture_grass.png", assetManager)
        val spriteSheetTexture = Texture2D.create("texture/RPGpack_sheet.png", assetManager)
        stairsTexture = SubTexture2D.createFromCoords(
            texture = spriteSheetTexture,
            coords = Float2(7.0f, 6.0f),
            cellSize = Float2(64.0f)
        )
        barrelTexture = SubTexture2D.createFromCoords(
            texture = spriteSheetTexture,
            coords = Float2(8.0f, 2.0f),
            cellSize = Float2(64.0f)
        )
        treeTexture = SubTexture2D.createFromCoords(
            texture = spriteSheetTexture,
            coords = Float2(2.0f, 1.0f),
            cellSize = Float2(64.0f),
            spriteSize = Float2(1.0f, 2.0f)
        )

        // offscreen buffer config
        val spec = FramebufferSpecification(
            width = surfaceWidth,
            height = surfaceHeight,
            attachmentsSpec = FramebufferAttachmentSpecification(),
        )
        offScreenFramebuffer = Framebuffer.create(spec)
        offScreenCameraController = OrthographicCameraController.createPixelUnitsController(
            viewportWidth = spec.width,
            viewportHeight = spec.height
        )
        offScreenCameraController.zoomLevel = 1.0f
        offScreenCameraController.onVisibleBoundsResize(
            width = spec.width / spec.downSampleFactor,
            height = spec.height / spec.downSampleFactor
        )
    }

    override fun RenderScope.onUpdate(dt: Timestep) {
        offScreenCameraController.onUpdate(dt)

        // draw into our offscreen buffer, also set drawing viewport to framebuffer size
        drawIntoFrameBuffer(offScreenFramebuffer) {
            scene2D(offScreenCameraController.camera) { renderOffscreenScene(dt) }
        }
        // if no framebuffer specified the default window framebuffer used
        scene2D(cameraController.camera) {
            renderMainScene(offScreenFramebuffer.colorAttachmentTexture)
        }
    }

    context(RenderScope)
    private fun Renderer2D.renderMainScene(colorAttachmentTexture: Texture2D) {
        RenderCommand.setClearColor(Float4(0.1f, 0.1f, 0.1f, 1.0f))
        RenderCommand.clear()
        RenderCommand.disableDepthTest()

        drawQuad(
            Float3(0.0f, 0.0f, -0.5f),
            Float2(viewportWidth.toFloat(), viewportHeight.toFloat()),
            colorAttachmentTexture
        )

        drawQuad(
            Float2(-0.2f, 0.2f),
            Float2(0.5f, 0.75f),
            Float4(0.2f, 0.3f, 0.8f, 1.0f)
        )
        val orthographicSize = cameraController.orthographicSize
        val grassSize = Float2(
            x = 0.5f * orthographicSize,
            y = 0.5f * orthographicSize
        )

        // Top Center
        val greenQuadSize = Float2(0.3f, 0.3f) * orthographicSize
        drawQuad(
            position = Float2(
                x = 0f,
                y = orthographicSize - greenQuadSize.y / 2
            ),
            size = greenQuadSize,
            color = Float4(0f, 1f, 0f, 1.0f)
        )

        // Center
        drawQuad(
            position = Float2(
                x = 0f,
                y = 0f
            ),
            size = greenQuadSize / 4f,
            color = Float4(0f, 1f, 1f, 1.0f)
        )
        // Bottom Right
        drawQuad(
            position = Float2(
                x = orthographicSize * cameraController.aspectRatio - grassSize.x / 2,
                y = -orthographicSize + grassSize.x / 2
            ),
            size = grassSize,
            texture = grassTexture,
            tilingFactor = 1.0f
        )

    }

    context(RenderScope)
    private fun Renderer2D.renderOffscreenScene(dt: Timestep) {
        RenderCommand.setClearColor(Float4(0.3f, 0.3f, 0.3f, 1.0f))
        RenderCommand.clear()
        drawQuad(
            Float2(200.0f, 200.0f) * density,
            Float2(100.8f, 100.8f) * density,
            Float4(0.8f, 0.2f, 0.3f, 1.0f)
        )
        drawQuad(
            Float2(0.0f, 0.0f),
            Float2(100.8f, 100.8f),
            Float4(0.8f, 0.2f, 0.3f, 1.0f)
        )
        drawQuad(
            position = Float3(300.0f, 300.0f, 0.0f),
            size = Float2(300f, 300f),
            texture = grassTexture,
            tilingFactor = 4.0f
        )
        drawQuad(Float3(-0.5f, 0.5f, -0.6f), Float2(0.5f, 0.5f), stairsTexture)
        drawQuad(Float3(0.5f, 0.5f, -0.8f), Float2(0.5f, 0.5f), barrelTexture)

        val sizePixels =
            Float2(56f, 56f) * density * offScreenCameraController.zoomLevel
        // top right
        drawQuad(
            position = Float2(
                viewportWidth.toFloat(),
                viewportHeight.toFloat()
            ) - sizePixels / 2f,
            size = sizePixels,
            color = Float4(1f, 0f, 1f, 1f)
        )
        val circleSize = sizePixels * 2f
        repeat(5) { index ->
            drawCircle(
                position = Float2(
                    x = viewportWidth / 2f,
                    y = viewportHeight.toFloat() / 2f + (index * density * 24)
                ) - Float2(x = circleSize.x, y = circleSize.y),
                size = circleSize * ((index.toFloat() / 5f) * 1.0f),
                fillColor = Float4(1.0f, 0.0f, 0.0f, 1.0f),
                strokeColor = Float4(0.0f, 1.0f, 0.0f, 1.0f),
                strokeWidth = 0.1f
            )
        }


        val thickness = 0.5f * density

        drawAntiAliasedLine(
            start = Float2(100f) * density,
            end = Float2(200f, 440f) * density,
            color = Float4(1.0f, 0.0f, 1.0f, 1.0f),
            thickness = thickness
        )

        // Draw lines
        lines.forEach {
            val startPoint = Float2(it.x, it.y)
            drawLine(
                start = startPoint,
                end = Float2(it.z, it.w),
                color = Float4(1.0f, 1.0f, 1.0f, 1.0f),
                thickness = thickness
            )
            drawCircle(
                position = startPoint,
                size = Float2(10.0f * density),
                fillColor = Float4(0.0f, 1.0f, 0.0f, 1.0f),
                strokeColor = Float4(0.0f, 1.0f, 0.0f, 0.0f),
                strokeWidth = 0.1f
            )
        }

        rotation += (30f * dt.seconds)
        drawRotatedQuad(
            position = Float2(
                viewportWidth.toFloat(),
                viewportHeight.toFloat()
            ) - sizePixels / 2f,
            size = sizePixels * 0.7f,
            color = Float4(0.1f, 0f, 1f, 1f),
            rotation = rotation % 360
        )

        // center
        drawQuad(
            position = Float2(
                x = viewportWidth / 2f,
                y = viewportHeight / 2f
            ),
            size = sizePixels,
            color = Float4(1f, 0f, 0f, 1.0f)
        )
    }

    override fun onGuiRender() {
        super.onGuiRender()
        val stats = Renderer2D.renderStats()
//        Log.d(TAG, "Render2D Stats:")
//        Log.d(TAG, "    Draw Calls: ${stats.drawCalls}")
//        Log.d(TAG, "         Quads: ${stats.quadCount}")
//        Log.d(TAG, "      Vertices: ${stats.vertexCount}")
//        Log.d(TAG, "       Indices: ${stats.indexCount}")
    }

    override fun onEvent(event: Event) {
        super.onEvent(event)
        offScreenCameraController.onEvent(event)

        with(EventDispatcher(event)) {
            dispatch<WindowResizeEvent> { onResizeWindow(it) }
        }
    }

    private fun onResizeWindow(event: WindowResizeEvent): Boolean {
        Renderer.onWindowResize(event.width, event.height)
//        viewportCameraController.onVisibleBoundsResize(event.width, event.height)
//        offScreenFramebuffer.resize(event.width, event.height)
        return false
    }

}

val zeroPos = Float2(0.0f)