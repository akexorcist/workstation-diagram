package dev.akexorcist.workstation.presentation.config

import dev.akexorcist.workstation.data.model.Size

object ViewportConfig {
    val defaultZoom: Float = 1.0f
    val viewportCullingMargin: Float = 100f
    val defaultCanvasSize: Size = Size(1920f, 1080f)
    val minCanvasSize: Size = Size(800f, 600f)
    val maxCanvasSize: Size = Size(10000f, 10000f)
}