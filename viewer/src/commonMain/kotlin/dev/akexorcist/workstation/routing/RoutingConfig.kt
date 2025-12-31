package dev.akexorcist.workstation.routing

import androidx.compose.ui.graphics.Color

object RoutingConfig {
    var gridCellSize: Float = 10f
    var deviceSnapToGrid: Boolean = true
    var deviceClearance: Float = 10f
    var portExtension: Float = 30f
    var crossingPenalty: Float = 5f
    var gridMoveCost: Float = 1f
    var turnPenalty: Float = 1.5f
    var allowDiagonal: Boolean = false
    var simplifyPath: Boolean = true
    var maxPathfindingIterations: Int = 10000
    var failedRouteColor: Color = Color(0xFFFF5722)
    var failedRouteAlpha: Float = 0.8f
    var failedRouteWidthMultiplier: Float = 1.5f
    var enableCaching: Boolean = true
    var recalculateOnResize: Boolean = true
}
