package dev.akexorcist.workstation.routing

/**
 * Minimal GridPoint definition for backward compatibility.
 * Not used for rendering - only virtualWaypoints are used.
 */
data class GridPoint(val x: Int, val y: Int)

data class RoutedConnection(
    val connectionId: String,
    val waypoints: List<GridPoint>,
    val virtualWaypoints: List<Pair<Float, Float>>,
    val success: Boolean,
    val crossings: Int
)

