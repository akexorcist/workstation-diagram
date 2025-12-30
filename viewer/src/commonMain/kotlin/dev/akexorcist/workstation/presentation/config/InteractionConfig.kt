package dev.akexorcist.workstation.presentation.config

object InteractionConfig {
    val clickToleranceRadius: Float = 5f
    val doubleClickTimeWindowMs: Long = 300
    val clickVsDragThreshold: Float = 5f

    val zoomStepSize: Float = 0.1f
    val minZoom: Float = 0.1f
    val maxZoom: Float = 5.0f
    val zoomSensitivity: Float = 0.1f

    val panSensitivity: Float = 1.0f
    val enableSmoothPanning: Boolean = true

    val hoverDetectionDelayMs: Long = 300
    val tooltipDelayMs: Long = 500
    val hoverAreaSize: Float = 10f
}