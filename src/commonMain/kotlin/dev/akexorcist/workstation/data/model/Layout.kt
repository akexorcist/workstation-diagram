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
    val virtualCanvas: Size? = null  // Required when coordinateSystem == "virtual"
)

@Serializable
data class ThemeConfig(
    val isDark: Boolean = true
)