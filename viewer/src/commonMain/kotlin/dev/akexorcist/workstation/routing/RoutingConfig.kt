package dev.akexorcist.workstation.routing

object RoutingConfig {
    // Grid System
    var gridCellSize: Float = 10f
    var deviceSnapToGrid: Boolean = true

    // Clearance and Extension
    var deviceClearance: Float = 10f
    var portClearance: Float = 15f
    var portExtension: Float = 70f
    var minPathSpacing: Float = 20f

    // Pathfinding Costs
    var gridMoveCost: Float = 1f
    var crossingPenalty: Float = 10f
    var turnPenalty: Float = 15f
    var pathRepulsionFactor: Float = 4f
    var pathDensityPenalty: Float = 3f
    var distributionFactor: Float = 6f
    var portProximityPenalty: Float = 20f
    
    // Path Quality
    var simplifyPath: Boolean = true
    var removeZigzags: Boolean = true

    // Performance and Limits
    var maxPathfindingIterations: Int = 20000
    var enableCaching: Boolean = true

    // Failed Route Visualization
    var failedRouteAlpha: Float = 0.8f
    var failedRouteWidthMultiplier: Float = 1.5f
}
