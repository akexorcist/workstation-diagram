package dev.akexorcist.workstation.presentation.config

import dev.akexorcist.workstation.data.model.Position
import dev.akexorcist.workstation.data.model.Size
import kotlinx.coroutines.flow.StateFlow

class VirtualUnitSystem(private val configManager: ViewportConfigManager) {
    private val canvasSize: StateFlow<Size> = configManager.canvasSize
    
    fun convertVirtualToAbsolute(position: Position): Position {
        val canvas = canvasSize.value
        val x = position.x
        val y = position.y
        return Position(x, y)
    }
    
    fun convertAbsoluteToVirtual(position: Position): Position {
        val canvas = canvasSize.value
        val x = position.x
        val y = position.y
        return Position(x, y)
    }
    
    fun convertVirtualToAbsoluteSize(size: Size): Size {
        val canvas = canvasSize.value
        val width = size.width
        val height = size.height
        return Size(width, height)
    }
    
    fun convertAbsoluteToVirtualSize(size: Size): Size {
        val canvas = canvasSize.value
        val width = size.width
        val height = size.height
        return Size(width, height)
    }
    
    fun isUsingVirtualUnits(): Boolean {
        return false
    }
}