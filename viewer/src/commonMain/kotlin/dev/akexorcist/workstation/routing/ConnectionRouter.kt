package dev.akexorcist.workstation.routing

import dev.akexorcist.workstation.data.model.*

data class RoutedConnection(
    val connectionId: String,
    val waypoints: List<GridPoint>,
    val virtualWaypoints: List<Pair<Float, Float>>,
    val success: Boolean,
    val crossings: Int
)

class ConnectionRouter(private val config: RoutingConfig = RoutingConfig) {
    private val router = ConnectorPathRouter(config)
    
    fun routeConnections(
        devices: List<Device>,
        connections: List<Connection>,
        virtualCanvasSize: Size
    ): List<RoutedConnection> {
        return router.routeConnections(devices, connections, virtualCanvasSize)
    }
}
