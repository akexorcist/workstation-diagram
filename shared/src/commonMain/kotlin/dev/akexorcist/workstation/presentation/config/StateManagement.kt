package dev.akexorcist.workstation.presentation.config

import dev.akexorcist.workstation.data.model.Offset
import dev.akexorcist.workstation.data.model.Size

object StateManagementConfig {
    // Initial pan to center content in the visible area
    // Sidebar takes 300px on left, control panel takes 60px on top
    // To center, we need to shift right by half sidebar width (150px) and down by half control panel height (30px)
    val initialPan: Offset = Offset(150f, 30f)

    /**
     * Validates and constrains zoom level based on provided viewport config or fallback values.
     * 
     * @param zoom The desired zoom level
     * @param viewportConfig Optional viewport config from workstation.json with min/max zoom constraints
     * @return Validated zoom level constrained between minimum and maximum allowed values
     */
    fun validateZoom(zoom: Float, viewportConfig: dev.akexorcist.workstation.data.model.ViewportConfig? = null): Float {
        val minZoom = viewportConfig?.minZoom ?: InteractionConfig.minZoom
        val maxZoom = viewportConfig?.maxZoom ?: InteractionConfig.maxZoom
        return zoom.coerceIn(minZoom, maxZoom)
    }

    fun validatePan(pan: Offset): Offset {
        return pan
    }
}

