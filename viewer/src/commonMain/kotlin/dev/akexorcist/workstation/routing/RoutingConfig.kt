package dev.akexorcist.workstation.routing

object RoutingConfig {
    var gridCellSize: Float = 10f
    var deviceSnapToGrid: Boolean = true
    var deviceClearance: Float = 10f
    var portClearance: Float = 15f
    var portExtension: Float = 70f
    var crossingPenalty: Float = 5f
    var gridMoveCost: Float = 1f
    var turnPenalty: Float = 8.0f
    var simplifyPath: Boolean = true
    var maxPathfindingIterations: Int = 10000
    var failedRouteAlpha: Float = 0.8f
    var failedRouteWidthMultiplier: Float = 1.5f
    var minPathSpacing: Float = 20f
    var pathRepulsionFactor: Float = 4f
}
