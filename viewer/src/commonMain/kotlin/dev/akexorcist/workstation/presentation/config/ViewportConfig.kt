package dev.akexorcist.workstation.presentation.config

import dev.akexorcist.workstation.data.model.Size

object ViewportConfig {
    const val defaultZoom: Float = 1.0f
    const val viewportCullingMargin: Float = 100f
    val defaultCanvasSize: Size = Size(1920f, 1080f)
}
