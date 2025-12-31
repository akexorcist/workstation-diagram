package dev.akexorcist.workstation.presentation.config

import dev.akexorcist.workstation.data.model.Offset
import dev.akexorcist.workstation.data.model.Size

object StateManagementConfig {
    // Initial pan to center content in the visible area
    // Sidebar takes 300px on left, control panel takes 60px on top
    // To center, we need to shift right by half sidebar width (150px) and down by half control panel height (30px)
    val initialPan: Offset = Offset(150f, 30f)

    fun validateZoom(zoom: Float): Float {
        return zoom.coerceIn(InteractionConfig.minZoom, InteractionConfig.maxZoom)
    }

    fun validatePan(pan: Offset, canvasSize: Size): Offset {
        return pan
    }
}