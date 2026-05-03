package dev.akexorcist.workstation.editor.utils

import dev.akexorcist.workstation.data.model.GridConfig
import kotlin.math.round

object GridUtils {
    fun snapToGrid(value: Float, gridSize: Float): Float {
        return round(value / gridSize) * gridSize
    }

    fun snapToGrid(value: Float, gridConfig: GridConfig?): Float {
        if (gridConfig?.enabled == false) return value
        return snapToGrid(value, gridConfig?.size ?: 20f)
    }
}
