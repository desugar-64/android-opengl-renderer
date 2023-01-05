package com.example.hellogl.renderer

import com.desugar.glucose.GraphicsApp
import com.desugar.glucose.assets.AssetManager

class SandboxOpenGLApp(assetManager: AssetManager) : GraphicsApp(assetManager = assetManager) {
    init {
//        pushLayer(GraphicsSandbox(assetManager))
        pushLayer(Sandbox2D(assetManager))
    }
}