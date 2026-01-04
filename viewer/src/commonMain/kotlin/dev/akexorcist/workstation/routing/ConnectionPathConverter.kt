package dev.akexorcist.workstation.routing

import dev.akexorcist.workstation.data.model.*

/**
 * Converts Connection routingPoints from JSON to RoutedConnection format.
 * Uses the routingPoints directly from the JSON file without any automatic pathfinding.
 */
object ConnectionPathConverter {
    
    fun convertConnections(
        devices: List<Device>,
        connections: List<Connection>
    ): List<RoutedConnection> {
        val deviceMap = devices.associateBy { it.id }
        
        return connections.map { connection ->
            convertConnection(connection, deviceMap)
        }
    }
    
    private fun convertConnection(
        connection: Connection,
        deviceMap: Map<String, Device>
    ): RoutedConnection {
        val sourceDevice = deviceMap[connection.sourceDeviceId]
        val targetDevice = deviceMap[connection.targetDeviceId]
        
        // If devices or ports are missing, return failed connection
        if (sourceDevice == null || targetDevice == null) {
            return RoutedConnection(
                connectionId = connection.id,
                waypoints = emptyList(),
                virtualWaypoints = emptyList(),
                success = false,
                crossings = 0
            )
        }
        
        val sourcePort = sourceDevice.ports.find { it.id == connection.sourcePortId }
        val targetPort = targetDevice.ports.find { it.id == connection.targetPortId }
        
        if (sourcePort == null || targetPort == null) {
            return RoutedConnection(
                connectionId = connection.id,
                waypoints = emptyList(),
                virtualWaypoints = emptyList(),
                success = false,
                crossings = 0
            )
        }
        
        // Calculate port positions in virtual coordinates
        val sourcePortPosition = calculatePortVirtualPosition(sourceDevice, sourcePort)
        val targetPortPosition = calculatePortVirtualPosition(targetDevice, targetPort)
        
        // Build the full path: source port → routingPoints → target port
        val virtualWaypoints = mutableListOf<Pair<Float, Float>>()
        
        // Add source port position
        virtualWaypoints.add(Pair(sourcePortPosition.x, sourcePortPosition.y))
        
        // Add routing points from JSON (if available)
        val routingPoints = connection.routingPoints
        if (routingPoints != null && routingPoints.isNotEmpty()) {
            routingPoints.forEach { point ->
                virtualWaypoints.add(Pair(point.x, point.y))
            }
        }
        
        // Add target port position
        virtualWaypoints.add(Pair(targetPortPosition.x, targetPortPosition.y))
        
        // Success if routingPoints were provided, otherwise it's a fallback straight path
        val success = routingPoints != null && routingPoints.isNotEmpty()
        
        return RoutedConnection(
            connectionId = connection.id,
            waypoints = emptyList(), // Not used for rendering
            virtualWaypoints = virtualWaypoints,
            success = success,
            crossings = 0 // Not relevant for manual paths
        )
    }
    
    private fun calculatePortVirtualPosition(device: Device, port: Port): Point {
        return when (port.position.side) {
            DeviceSide.TOP -> {
                val positionX = port.position.position.coerceIn(0f, device.size.width)
                Point(
                    x = device.position.x + positionX,
                    y = device.position.y
                )
            }
            
            DeviceSide.BOTTOM -> {
                val positionX = port.position.position.coerceIn(0f, device.size.width)
                Point(
                    x = device.position.x + positionX,
                    y = device.position.y + device.size.height
                )
            }
            
            DeviceSide.LEFT -> {
                val positionY = port.position.position.coerceIn(0f, device.size.height)
                Point(
                    x = device.position.x,
                    y = device.position.y + positionY
                )
            }
            
            DeviceSide.RIGHT -> {
                val positionY = port.position.position.coerceIn(0f, device.size.height)
                Point(
                    x = device.position.x + device.size.width,
                    y = device.position.y + positionY
                )
            }
        }
    }
}

