package com.example.hellogl.renderer

import com.desugar.glucose.GraphicsRoot
import com.desugar.glucose.assets.AssetManager

class SandboxOpenGLApp(assetManager: AssetManager) : GraphicsRoot(assetManager = assetManager) {
    init {
//        pushLayer(GraphicsSandbox(assetManager))
        pushLayer(Sandbox2D(assetManager))
    }
}