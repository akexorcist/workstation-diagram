package dev.akexorcist.workstation.routing

import dev.akexorcist.workstation.data.model.*

/**
 * Converts Connection routingPoints from JSON to RoutedConnection format.
 * Uses the routingPoints directly from the JSON file without any automatic pathfinding.
 *
 * When multiple connections share the same port, a common convergence/divergence
 * waypoint is injected adjacent to that port so the lines overlap for the shared segment.
 */
object ConnectionPathConverter {

    private const val SHARED_PORT_CONVERGENCE_DISTANCE = 40f

    fun convertConnections(
        devices: List<Device>,
        connections: List<Connection>
    ): List<RoutedConnection> {
        val deviceMap = devices.associateBy { it.id }
        val routedConnections = connections.map { convertConnection(it, deviceMap) }.toMutableList()
        applySharedPortConvergence(connections, deviceMap, routedConnections)
        return routedConnections
    }

    private fun convertConnection(
        connection: Connection,
        deviceMap: Map<String, Device>
    ): RoutedConnection {
        val sourceDevice = deviceMap[connection.sourceDeviceId]
        val targetDevice = deviceMap[connection.targetDeviceId]

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

        val sourcePortPosition = calculatePortVirtualPosition(sourceDevice, sourcePort)
        val targetPortPosition = calculatePortVirtualPosition(targetDevice, targetPort)

        val virtualWaypoints = mutableListOf<Pair<Float, Float>>()
        virtualWaypoints.add(Pair(sourcePortPosition.x, sourcePortPosition.y))

        val routingPoints = connection.routingPoints
        if (routingPoints != null && routingPoints.isNotEmpty()) {
            routingPoints.forEach { point ->
                virtualWaypoints.add(Pair(point.x, point.y))
            }
        }

        virtualWaypoints.add(Pair(targetPortPosition.x, targetPortPosition.y))

        val success = routingPoints != null && routingPoints.isNotEmpty()

        return RoutedConnection(
            connectionId = connection.id,
            waypoints = emptyList(),
            virtualWaypoints = virtualWaypoints,
            success = success,
            crossings = 0
        )
    }

    private fun applySharedPortConvergence(
        connections: List<Connection>,
        deviceMap: Map<String, Device>,
        routedConnections: MutableList<RoutedConnection>
    ) {
        val indexById = connections.mapIndexed { i, c -> c.id to i }.toMap()

        connections.groupBy { "${it.targetDeviceId}:${it.targetPortId}" }
            .filter { it.value.size > 1 }
            .forEach { (_, group) ->
                val first = group.first()
                val device = deviceMap[first.targetDeviceId] ?: return@forEach
                val port = device.ports.find { it.id == first.targetPortId } ?: return@forEach
                val convergence = portSharedWaypoint(calculatePortVirtualPosition(device, port), port.position.side)
                group.forEach { connection ->
                    val i = indexById[connection.id] ?: return@forEach
                    routedConnections[i] = injectBeforeLast(routedConnections[i], convergence)
                }
            }

        connections.groupBy { "${it.sourceDeviceId}:${it.sourcePortId}" }
            .filter { it.value.size > 1 }
            .forEach { (_, group) ->
                val first = group.first()
                val device = deviceMap[first.sourceDeviceId] ?: return@forEach
                val port = device.ports.find { it.id == first.sourcePortId } ?: return@forEach
                val divergence = portSharedWaypoint(calculatePortVirtualPosition(device, port), port.position.side)
                group.forEach { connection ->
                    val i = indexById[connection.id] ?: return@forEach
                    routedConnections[i] = injectAfterFirst(routedConnections[i], divergence)
                }
            }
    }

    private fun portSharedWaypoint(portPosition: Point, side: DeviceSide): Point {
        val d = SHARED_PORT_CONVERGENCE_DISTANCE
        return when (side) {
            DeviceSide.LEFT -> Point(portPosition.x - d, portPosition.y)
            DeviceSide.RIGHT -> Point(portPosition.x + d, portPosition.y)
            DeviceSide.TOP -> Point(portPosition.x, portPosition.y - d)
            DeviceSide.BOTTOM -> Point(portPosition.x, portPosition.y + d)
        }
    }

    private fun injectBeforeLast(routed: RoutedConnection, point: Point): RoutedConnection {
        val waypoints = routed.virtualWaypoints
        if (waypoints.size < 2) return routed
        val updated = waypoints.toMutableList().also { it.add(it.size - 1, Pair(point.x, point.y)) }
        return routed.copy(virtualWaypoints = updated)
    }

    private fun injectAfterFirst(routed: RoutedConnection, point: Point): RoutedConnection {
        val waypoints = routed.virtualWaypoints
        if (waypoints.size < 2) return routed
        val updated = waypoints.toMutableList().also { it.add(1, Pair(point.x, point.y)) }
        return routed.copy(virtualWaypoints = updated)
    }

    private fun calculatePortVirtualPosition(device: Device, port: Port): Point {
        return when (port.position.side) {
            DeviceSide.TOP -> {
                val positionX = port.position.position.coerceIn(0f, device.size.width)
                Point(x = device.position.x + positionX, y = device.position.y)
            }
            DeviceSide.BOTTOM -> {
                val positionX = port.position.position.coerceIn(0f, device.size.width)
                Point(x = device.position.x + positionX, y = device.position.y + device.size.height)
            }
            DeviceSide.LEFT -> {
                val positionY = port.position.position.coerceIn(0f, device.size.height)
                Point(x = device.position.x, y = device.position.y + positionY)
            }
            DeviceSide.RIGHT -> {
                val positionY = port.position.position.coerceIn(0f, device.size.height)
                Point(x = device.position.x + device.size.width, y = device.position.y + positionY)
            }
        }
    }
}
