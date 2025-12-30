package dev.akexorcist.workstation.presentation.config

import dev.akexorcist.workstation.data.model.ConnectionCategory

object RenderingConfig {
    val defaultDeviceBorderRadius: Float = 8f
    val defaultDeviceBorderThickness: Float = 2f

    val portIndicatorBorderThickness: Float = 1f
    val portIndicatorSpacingFromEdge: Float = 4f
    val portIndicatorMinSpacing: Float = 8f
    val portIndicatorTextScaleFactor: Float = 1.2f

    val defaultConnectionLineThickness: Float = 2f
    val connectionLineThicknessByCategory: Map<ConnectionCategory, Float> = mapOf(
        ConnectionCategory.DATA to 2f,
        ConnectionCategory.VIDEO to 3f,
        ConnectionCategory.AUDIO to 2f,
        ConnectionCategory.POWER to 2.5f,
        ConnectionCategory.NETWORK to 2f
    )
    val arrowHeadWidth: Float = 12f
    val arrowHeadHeight: Float = 8f
    val connectionLabelPadding: Float = 4f

    val baseFontSize: Float = 12f
    val deviceNameFontScale: Float = 1.17f
    val deviceModelFontScale: Float = 1.0f
    val portLabelFontScale: Float = 0.83f
    val connectionLabelFontScale: Float = 0.92f
    val fontSizeFromDimensionScale: Float = 0.06f

    val minDeviceSpacing: Float = 20f
    val sidebarWidth: Float = 300f
    val controlPanelHeight: Float = 60f
    val panelPadding: Float = 16f
    val deviceLabelSpacing: Float = 8f
}