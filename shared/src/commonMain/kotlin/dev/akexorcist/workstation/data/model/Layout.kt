package dev.akexorcist.workstation.data.model

import kotlinx.serialization.Serializable

@Serializable
data class WorkstationLayout(
    val devices: List<Device>,
    val connections: List<Connection>,
    val metadata: LayoutMetadata
)

@Serializable
data class LayoutMetadata(
    val title: String,
    val date: String,
    val canvasSize: Size,
    val theme: ThemeConfig? = null,
    val version: String = "1.0",
    // Virtual coordinate system support (backward compatible)
    val coordinateSystem: String? = null,  // "absolute" or "virtual"
    val virtualCanvas: Size? = null,  // Required when coordinateSystem == "virtual"
    val viewport: ViewportConfig? = null,
    val grid: GridConfig? = null
)

@Serializable
data class ThemeConfig(
    val isDark: Boolean = true
)

@Serializable
data class ViewportConfig(
    val defaultZoom: Float = 1.0f,
    val cullingMargin: Float = 100f,
    val minZoom: Float = 0.1f,
    val maxZoom: Float = 3.0f
)

@Serializable
data class GridConfig(
    val enabled: Boolean = true,
    val size: Float = 20f,
    val majorLineInterval: Int = 5
)