package com.example.hellogl.renderer

import android.content.res.Resources
import android.util.Log
import com.desugar.glucose.assets.AssetManager
import com.desugar.glucose.camera.OrthographicCameraController
import com.desugar.glucose.core.Timestep
import com.desugar.glucose.events.Event
import com.desugar.glucose.events.EventDispatcher
import com.desugar.glucose.events.WindowResizeEvent
import com.desugar.glucose.layers.Layer
import com.desugar.glucose.renderer.Framebuffer
import com.desugar.glucose.renderer.FramebufferAttachmentSpecification
import com.desugar.glucose.renderer.FramebufferSpecification
import com.desugar.glucose.renderer.FramebufferTextureFormat
import com.desugar.glucose.renderer.FramebufferTextureSpecification
import com.desugar.glucose.renderer.RenderCommand
import com.desugar.glucose.renderer.Renderer
import com.desugar.glucose.renderer.Renderer2D
import com.desugar.glucose.renderer.SubTexture2D
import com.desugar.glucose.renderer.Texture2D
import dev.romainguy.kotlin.math.Float2
import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.Float4
import dev.romainguy.kotlin.math.scale
import dev.romainguy.kotlin.math.translation
import kotlin.properties.Delegates


class Sandbox2D(
    private val assetManager: AssetManager
) : Layer("Sandbox2D") {
    private var offScreenCameraController: OrthographicCameraController by Delegates.notNull()
    private var viewportCameraController: OrthographicCameraController by Delegates.notNull()

    private var grassTexture: Texture2D by Delegates.notNull()
    private var stairsTexture: SubTexture2D by Delegates.notNull()
    private var barrelTexture: SubTexture2D by Delegates.notNull()
    private var treeTexture: SubTexture2D by Delegates.notNull()

    private var offScreenFramebuffer: Framebuffer by Delegates.notNull()

    private var viewportWidth: Int = 0
    private var viewportHeight: Int = 0
    private var rotation: Float = 0.0f

    override fun onAttach(surfaceWidth: Int, surfaceHeight: Int) {
        viewportWidth = surfaceWidth
        viewportHeight = surfaceHeight

        viewportCameraController = OrthographicCameraController(
            aspectRatio = surfaceWidth / surfaceHeight.toFloat(),
            height = surfaceHeight,
            rotation = true
        )
        viewportCameraController.disableMovement = true
        viewportCameraController.zoomLevel = 1.0f
        viewportCameraController.updateCameraProjection()


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
            attachmentsSpec = FramebufferAttachmentSpecification(
                listOf(
                    FramebufferTextureSpecification(format = FramebufferTextureFormat.RGBA8),
                    FramebufferTextureSpecification(format = FramebufferTextureFormat.DEPTH24STENCIL8)
                )
            ),
        )
        offScreenFramebuffer = Framebuffer.create(spec)
        offScreenCameraController = OrthographicCameraController(
            width = spec.width,
            height = spec.height
        )
        offScreenCameraController.zoomLevel = 1.0f
        offScreenCameraController.onVisibleBoundsResize(
            width = spec.width / spec.downSampleFactor,
            height = spec.height / spec.downSampleFactor
        )
    }

    override fun onUpdate(dt: Timestep) {
        offScreenCameraController.onUpdate(dt)
        viewportCameraController.onUpdate(dt)

        Renderer2D.resetRenderStats()

        // draw into our offscreen buffer
        offScreenFramebuffer.bind()
        RenderCommand.setClearColor(Float4(0.3f, 0.3f, 0.3f, 1.0f))
        RenderCommand.clear()
        Renderer2D.beginScene(offScreenCameraController.camera)
        Renderer2D.drawQuad(
            Float2(200.0f, 200.0f),
            Float2(100.8f, 100.8f),
            Float4(0.8f, 0.2f, 0.3f, 1.0f)
        )
        Renderer2D.drawQuad(
            Float2(0.0f, 0.0f),
            Float2(100.8f, 100.8f),
            Float4(0.8f, 0.2f, 0.3f, 1.0f)
        )
//        Renderer2D.drawRotatedQuad(Float3(-0.0f, -0.5f, 0.1f), Float2(1f, 1f), -45f, grassTexture, tilingFactor = 1.0f)
        Renderer2D.drawQuad(
            position = Float3(300.0f, 300.0f, 0.0f),
            size = Float2(300f, 300f),
            texture = grassTexture,
            tilingFactor = 4.0f
        )
        Renderer2D.drawQuad(Float3(-0.5f, 0.5f, -0.6f), Float2(0.5f, 0.5f), stairsTexture)
        Renderer2D.drawQuad(Float3(0.5f, 0.5f, -0.8f), Float2(0.5f, 0.5f), barrelTexture)

        val density = Resources.getSystem().displayMetrics.density
        val sizePixels = Float2(56f, 56f) * density * offScreenCameraController.zoomLevel
        // top right
        Renderer2D.drawQuad(
            position = Float2(viewportWidth.toFloat(), viewportHeight.toFloat()) - sizePixels / 2f,
            size = sizePixels,
            color = Float4(1f, 0f, 1f, 1f)
        )
        val circleSize = sizePixels * 2f
        repeat(5) { index ->
            val transform =
                translation(
                    Float3(
                        x = viewportWidth / 2f,
                        y = viewportHeight.toFloat() - circleSize.y,
                        z = 0.0f
                    ) - Float3(x = circleSize.x, y = circleSize.y * index)
                ) * scale(Float3(circleSize))

            Renderer2D.drawCircle(
                transform = transform,
                color = Float4(0.7f, 0.7f, index.toFloat() / 5, 1.0f),
                thickness = (1.0f - (index.toFloat() / 5)).coerceAtLeast(0.1f),
                fade = 0.005f + (index / 7f)
            )
        }

        rotation += (30f * dt.seconds)
        Renderer2D.drawRotatedQuad(
            position = Float2(viewportWidth.toFloat(), viewportHeight.toFloat()) - sizePixels / 2f,
            size = sizePixels * 0.7f,
            color = Float4(0.1f, 0f, 1f, 1f),
            rotation = rotation % 360
        )

        // center
        Renderer2D.drawQuad(
            position = Float2(x = viewportWidth / 2f, y = viewportHeight / 2f),
            size = sizePixels,
            color = Float4(1f, 0f, 0f, 1.0f)
        )

        Renderer2D.endScene()

        // switch to default screen buffer and draw out offscreen texture
        offScreenFramebuffer.unbind()
        RenderCommand.setViewPort(0, 0, viewportWidth, viewportHeight)

        RenderCommand.setClearColor(Float4(0.1f, 0.1f, 0.1f, 1.0f))
        RenderCommand.clear()

        Renderer2D.beginScene(viewportCameraController.camera)
        RenderCommand.disableDepthTest()

        // framebuffer texture
        Renderer2D.drawQuad(
            Float3(-0.0f, 0.0f, -0.5f),
            Float2(viewportWidth.toFloat(), viewportHeight.toFloat()),
            offScreenFramebuffer.colorAttachmentTexture
        )

        Renderer2D.drawQuad(
            Float2(-0.2f, 0.2f),
            Float2(0.5f, 0.75f),
            Float4(0.2f, 0.3f, 0.8f, 1.0f)
        )
        val orthographicSize = viewportCameraController.orthographicSize
        val grassSize = Float2(
            x = 0.5f * orthographicSize,
            y = 0.5f * orthographicSize
        )

        // Top Center
        val greenQuadSize = Float2(0.3f, 0.3f) * orthographicSize
        Renderer2D.drawQuad(
            position = Float2(
                x = 0f,
                y = orthographicSize - greenQuadSize.y / 2
            ),
            size = greenQuadSize,
            color = Float4(0f, 1f, 0f, 1.0f)
        )

        // Center
        Renderer2D.drawQuad(
            position = Float2(
                x = 0f,
                y = 0f
            ),
            size = greenQuadSize / 4f,
            color = Float4(0f, 1f, 1f, 1.0f)
        )
        // Bottom Right
        Renderer2D.drawQuad(
            position = Float2(
                x = orthographicSize * viewportCameraController.aspectRatio - grassSize.x / 2,
                y = -orthographicSize + grassSize.x / 2
            ),
            size = grassSize,
            texture = grassTexture,
            tilingFactor = 1.0f
        )

//        Renderer2D.drawQuadPx(
//            positionPixels = Float2(viewportWidth.toFloat() / 2f, -100f) - sizePixels / 2f,
//            sizePixels = sizePixels,
//            color = Float4(0.5f, 0.2f, 1f, 1f),
//            windowWidth = viewportWidth.toFloat(),
//            windowHeight = viewportHeight.toFloat(),
//            orthographicSize = orthographicSize
//        )
//
//        Renderer2D.drawQuadPx(
//            positionPixels = Float2(0f, viewportHeight / 2f),
//            sizePixels = sizePixels,
//            color = Float4(0.3f, 0.6f, 0.4f, 1f),
//            windowWidth = viewportWidth.toFloat(),
//            windowHeight = viewportHeight.toFloat(),
//            orthographicSize = orthographicSize
//        )

        Renderer2D.endScene()
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
        offScreenCameraController.onEvent(event)

        with(EventDispatcher(event)) {
            dispatch<WindowResizeEvent> { onResizeWindow(it) }
        }
    }

    private fun onResizeWindow(event: WindowResizeEvent): Boolean {
        viewportWidth = event.width
        viewportHeight = event.height
        Renderer.onWindowResize(event.width, event.height)
//        viewportCameraController.onVisibleBoundsResize(event.width, event.height)
//        offScreenFramebuffer.resize(event.width, event.height)
        return false
    }

}

val zeroPos = Float2(0.0f)