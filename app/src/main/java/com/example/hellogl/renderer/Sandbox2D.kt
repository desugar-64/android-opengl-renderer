package com.example.hellogl.renderer

import android.util.Log
import com.desugar.glucose.assets.AssetManager
import com.desugar.glucose.camera.OrthographicCameraController
import com.desugar.glucose.core.Timestep
import com.desugar.glucose.events.Event
import com.desugar.glucose.events.EventDispatcher
import com.desugar.glucose.events.WindowResizeEvent
import com.desugar.glucose.layers.Layer
import com.desugar.glucose.renderer.*
import dev.romainguy.kotlin.math.Float2
import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.Float4
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

    override fun onAttach(surfaceWidth: Int, surfaceHeight: Int) {
        viewportWidth = surfaceWidth
        viewportHeight = surfaceHeight

        viewportCameraController = OrthographicCameraController(
            aspectRatio = /*surfaceWidth / surfaceHeight.toFloat()*/ 1.0f,
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

        // virtual resolution
        val spec = FramebufferSpecification(
            width = surfaceWidth,
            height = surfaceHeight,
            samples = 1
        )
        offScreenFramebuffer = Framebuffer.create(spec)
        offScreenCameraController = OrthographicCameraController(
            aspectRatio = spec.width / spec.height.toFloat(),
            rotation = true
        )
        offScreenCameraController.zoomLevel = 4.0f
        offScreenCameraController.updateCameraProjection()
        offScreenCameraController.onViewportSizeUpdate(
            width = spec.width / spec.samples,
            height = spec.height / spec.samples
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
        Renderer2D.drawQuad(Float2(-1.0f, 1.0f), Float2(0.8f, 0.8f), Float4(0.8f, 0.2f, 0.3f, 1.0f))
        Renderer2D.drawQuad(
            Float2(-0.2f, 0.2f),
            Float2(0.5f, 0.75f),
            Float4(0.2f, 0.3f, 0.8f, 1.0f)
        )
        Renderer2D.drawQuad(
            Float3(0.0f, 0.0f, -0.1f),
            Float2(1f, 1f),
            grassTexture,
            tilingFactor = 1.0f
        )
//        Renderer2D.drawRotatedQuad(Float3(-0.0f, -0.5f, 0.1f), Float2(1f, 1f), -45f, grassTexture, tilingFactor = 1.0f)
        Renderer2D.drawQuad(
            Float3(-0.5f, -1.5f, 1.0f),
            Float2(0.8f, 0.8f),
            grassTexture,
            tilingFactor = 4.0f
        )
        Renderer2D.drawQuad(Float3(-0.5f, 0.5f, -0.6f), Float2(0.5f, 0.5f), stairsTexture)
        Renderer2D.drawQuad(Float3(0.5f, 0.5f, -0.8f), Float2(0.5f, 0.5f), barrelTexture)
        Renderer2D.endScene()

        // switch to default screen buffer and draw out offscreen texture
        offScreenFramebuffer.unbind()
        RenderCommand.setViewPort(0, 0, viewportWidth, viewportHeight)

        RenderCommand.setClearColor(Float4(0.1f, 0.1f, 0.1f, 1.0f))
        RenderCommand.clear()

        Renderer2D.beginScene(viewportCameraController.camera)
        RenderCommand.disableDepthTest()
        Renderer2D.drawQuad(
            Float3(-0.0f, 0.0f, -0.5f),
            Float2(2.0f),
            offScreenFramebuffer.colorAttachmentTexture
        )
//        Renderer2D.drawQuad(Float3(-0.0f, 0.0f, -0.5f), Float2(1.0f), Float4(1.0f, 0.0f, 1.0f, 1.0f))
        Renderer2D.endScene()
    }

    override fun onGuiRender() {
        super.onGuiRender()
        val stats = Renderer2D.renderStats()
        Log.d(TAG, "Render2D Stats:")
        Log.d(TAG, "    Draw Calls: ${stats.drawCalls}")
        Log.d(TAG, "         Quads: ${stats.quadCount}")
        Log.d(TAG, "      Vertices: ${stats.vertexCount}")
        Log.d(TAG, "       Indices: ${stats.indexCount}")
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
        offScreenFramebuffer.resize(event.width, event.height)
        return false
    }

}

val zeroPos = Float2(0.0f)