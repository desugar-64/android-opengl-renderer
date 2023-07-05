package com.example.hellogl.renderer

import com.desugar.glucose.assets.AssetManager
import com.desugar.glucose.camera.OrthographicCameraController
import com.desugar.glucose.core.Timestep
import com.desugar.glucose.events.Event
import com.desugar.glucose.events.EventDispatcher
import com.desugar.glucose.events.WindowResizeEvent
import com.desugar.glucose.layers.Layer
import com.desugar.glucose.renderer.*
import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.Float4
import dev.romainguy.kotlin.math.scale
import dev.romainguy.kotlin.math.translation
import kotlin.properties.Delegates

class GraphicsSandbox(private val assetManager: AssetManager) : Layer("GraphicsSandbox") {
    private val shaderLibrary = ShaderLibrary()

    private var triangleVA: VertexArray by Delegates.notNull()
    private var squareVA: VertexArray by Delegates.notNull()

    private var cameraController: OrthographicCameraController = OrthographicCameraController(
        aspectRatio = 1.0f,
        height = 800,
        rotation = true
    )
    private var grassTexture: Texture2D by Delegates.notNull()

    override fun onAttach(surfaceWidth: Int, surfaceHeight: Int) {
        // Vertex Array
        triangleVA = VertexArray.create()

        // Vertex Buffer
        val triangleVertices = floatArrayOf(
            // x      y      z   r     g     b     a
            -0.5f, -0.5f, 0.0f, 0.8f, 0.2f, 0.8f, 1.0f,
            0.5f, -0.5f, 0.0f, 0.2f, 0.0f, 0.8f, 1.0f,
            0.0f, 0.5f, 0.0f, 0.8f, 0.8f, 0.2f, 1.0f,
        )
        val triangleVertexBuffer = VertexBuffer.create(triangleVertices).apply {
            layout = BufferLayout {
                addElement("a_Position", ShaderDataType.Float3)
                addElement("a_Color", ShaderDataType.Float4)
            }
        }
        triangleVA.addVertexBuffer(triangleVertexBuffer)

        // Index Buffer
        triangleVA.indexBuffer = IndexBuffer.create(intArrayOf(0, 1, 2))

        val squareVertices = floatArrayOf(
            // x      y      z   u     v
            -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, // bottom left
            0.5f, -0.5f, 0.0f, 1.0f, 0.0f, // top left
            0.5f, 0.5f, 0.0f, 1.0f, 1.0f, // top right
            -0.5f, 0.5f, 0.0f, 0.0f, 1.0f, // bottom right
        )
        squareVA = VertexArray.create()
        val squareVB = VertexBuffer.create(squareVertices).apply {
            layout = BufferLayout {
                addElement("a_Position", ShaderDataType.Float3)
                addElement("a_TextCoord", ShaderDataType.Float2)
            }
        }
        squareVA.addVertexBuffer(squareVB)
        squareVA.indexBuffer = IndexBuffer.create(intArrayOf(0, 1, 2, 2, 3, 0))

        shaderLibrary.load(assetManager, "shader/Triangle.glsl")
        shaderLibrary.load(assetManager, "shader/FlatColor.glsl")

        val textureShader = shaderLibrary.load(assetManager, "shader/Renderer2D_Quad.glsl")

        grassTexture = Texture2D.create("texture/texture_grass.png", assetManager)
        grassTexture.bind(1)
        textureShader.setIntArray("u_Textures", 0, 1/*texture_slot*/)
    }

    override fun onDetach() {
        super.onDetach()
        shaderLibrary.destroyAll()
    }

    override fun onUpdate(dt: Timestep) {
        cameraController.onUpdate(dt)

        RenderCommand.setClearColor(Float4(0.1f, 0.1f, 0.1f, 1.0f))
        RenderCommand.clear()
        Renderer.beginScene(cameraController.camera)
        val scale = scale(Float3(0.1f))
        val yellow = Float4(1.0f, 0.9f, 0.2f, 1.0f)
        val blueColor = Float4(0.2f, 0.3f, 0.8f, 1.0f)
        val flatColorShader = shaderLibrary["FlatColor"]
        for (i in 0 until 10) {
            for (j in 0 until 10) {
                val pos = Float3(j * 0.11f, i * 0.11f, 0.0f) - 0.5f
                val transform = translation(pos) * scale
                if (j % 2 == 0) {
                    flatColorShader.setFloat4("u_Color", yellow)
                } else {
                    flatColorShader.setFloat4("u_Color", blueColor)
                }
                Renderer.submit(flatColorShader, squareVA, transform)
            }
        }
        // Draw triangle
//        Renderer.submit(shaderLibrary["Triangle"], triangleVA)

        // Draw grass texture quad
        grassTexture.bind()
        Renderer.submit(shaderLibrary["Texture"], squareVA, scale(Float3(1.5f)))
        Renderer.endScene()
    }

    override fun onEvent(event: Event) {
        cameraController.onEvent(event)

        with(EventDispatcher(event)) {
            dispatch<WindowResizeEvent> { onResizeWindow(it) }
        }
    }

    private fun onResizeWindow(event: WindowResizeEvent): Boolean {
        Renderer.onWindowResize(event.width, event.height)
        return false
    }

}