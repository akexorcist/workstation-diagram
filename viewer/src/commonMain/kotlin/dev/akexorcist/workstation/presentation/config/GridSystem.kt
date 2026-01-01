package dev.akexorcist.workstation.presentation.config

import dev.akexorcist.workstation.data.model.Position
import kotlin.math.roundToInt

class GridSystem(private val configManager: ViewportConfigManager) {
    val gridSize: Float get() = configManager.gridSize
    val majorLineInterval: Int get() = configManager.gridMajorLineInterval
    val isEnabled: Boolean get() = configManager.gridEnabled

    fun snapToGrid(position: Position): Position {
        if (!isEnabled) return position
        
        val gridSize = gridSize
        val snappedX = (position.x / gridSize).roundToInt() * gridSize
        val snappedY = (position.y / gridSize).roundToInt() * gridSize
        return Position(snappedX, snappedY)
    }
    
    fun isMajorGridLine(index: Int): Boolean {
        return index % majorLineInterval == 0
    }
    
    fun calculateGridLines(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float
    ): Pair<List<Float>, List<Float>> {
        if (!isEnabled) return Pair(emptyList(), emptyList())
        
        val gridSize = gridSize
        val startGridX = (startX / gridSize).toInt()
        val endGridX = (endX / gridSize).toInt() + 1
        val startGridY = (startY / gridSize).toInt()
        val endGridY = (endY / gridSize).toInt() + 1
        
        val verticalLines = (startGridX..endGridX).map { it * gridSize }
        val horizontalLines = (startGridY..endGridY).map { it * gridSize }
        
        return Pair(verticalLines, horizontalLines)
    }
}