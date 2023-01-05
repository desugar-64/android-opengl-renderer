package com.desugar.glucose.renderer

import com.desugar.glucose.assets.AssetManager
import com.desugar.glucose.camera.OrthographicCamera
import dev.romainguy.kotlin.math.Mat4

object Renderer {
    val api get() = RendererAPI.api

    private var sceneData: SceneData = SceneData(Mat4.identity())

    fun init(assetManager: AssetManager) {
        RenderCommand.init()
        Renderer2D.init(assetManager)
    }

    fun onWindowResize(width: Int, height: Int) {
        RenderCommand.setViewPort(0, 0, width, height)
    }

    fun beginScene(orthographicCamera: OrthographicCamera) {
        sceneData.viewProjectionMatrix = orthographicCamera.viewProjectionMatrix
    }

    fun endScene() {

    }

    fun submit(shader: Shader, vertexArray: VertexArray, transform: Mat4 = Mat4.identity()) {
        shader.bind()
        shader.setMat4("u_ViewProjection", sceneData.viewProjectionMatrix)
        shader.setMat4("u_Transform", transform)
        vertexArray.bind()
        RenderCommand.drawIndexed(vertexArray)
    }

    fun shutDown() {
        Renderer2D.shutdown()
    }

}

data class SceneData(
    var viewProjectionMatrix: Mat4
)