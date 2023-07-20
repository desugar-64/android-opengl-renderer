package com.example.hellogl.renderer

import com.desugar.glucose.GraphicsRoot
import com.desugar.glucose.assets.AssetManager

class SandboxOpenGLApp(assetManager: AssetManager) : GraphicsRoot(assetManager = assetManager) {
    init {
//        pushLayer(GraphicsSandbox(assetManager))
        val sandbox2D = Sandbox2D(assetManager)
        sandbox2D.isVisible = false
        val demoLayer = DemoLayer(assetManager)
        pushLayer(sandbox2D)
        pushLayer(demoLayer)
    }
}