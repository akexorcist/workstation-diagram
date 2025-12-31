package dev.akexorcist.workstation.routing

import dev.akexorcist.workstation.data.model.*
import kotlin.math.sqrt

data class RoutedConnection(
    val connectionId: String,
    val waypoints: List<GridPoint>,
    val virtualWaypoints: List<Pair<Float, Float>>,
    val success: Boolean,
    val crossings: Int
)

class ConnectionRouter(private val config: RoutingConfig = RoutingConfig) {

    fun routeConnections(
        devices: List<Device>,
        connections: List<Connection>,
        virtualCanvasSize: Size
    ): List<RoutedConnection> {
        val gridWidth = (virtualCanvasSize.width / config.gridCellSize).toInt().coerceAtLeast(1)
        val gridHeight = (virtualCanvasSize.height / config.gridCellSize).toInt().coerceAtLeast(1)
        val grid = RoutingGrid(gridWidth, gridHeight, config.gridCellSize)
        val deviceMap = devices.associateBy { it.id }

        // Mark devices as obstacles
        devices.forEach { device ->
            val (width, height) = if (config.deviceSnapToGrid) {
                grid.snapSize(device.size.width, device.size.height)
            } else {
                device.size.width to device.size.height
            }
            grid.markDeviceObstacle(device.position.x, device.position.y, width, height, config.deviceClearance)
        }
        
        // Mark all ports as obstacles for more intelligent routing
        devices.forEach { device ->
            device.ports.forEach { port ->
                val portPosition = calculatePortPosition(device, port)
                grid.markPortObstacle(portPosition.first, portPosition.second, config.portClearance)
            }
        }

        val pathfinder = AStarPathfinder(grid, config)
        val existingPaths = mutableMapOf<String, List<GridPoint>>()

        return connections
            .sortedBy { calculateConnectionDistance(it, deviceMap) }
            .map { connection ->
                routeConnection(connection, deviceMap, grid, pathfinder, existingPaths).also { routed ->
                    if (routed.success) {
                        existingPaths[connection.id] = routed.waypoints
                        grid.occupyPath(connection.id, routed.waypoints)
                    }
                }
            }
    }

    private fun routeConnection(
        connection: Connection,
        deviceMap: Map<String, Device>,
        grid: RoutingGrid,
        pathfinder: AStarPathfinder,
        existingPaths: Map<String, List<GridPoint>>
    ): RoutedConnection {
        val sourceDevice = deviceMap[connection.sourceDeviceId]
        val targetDevice = deviceMap[connection.targetDeviceId]
        val sourcePort = sourceDevice?.ports?.find { it.id == connection.sourcePortId }
        val targetPort = targetDevice?.ports?.find { it.id == connection.targetPortId }

        if (sourceDevice == null || targetDevice == null || sourcePort == null || targetPort == null) {
            return createFailedRoute(connection, grid)
        }
        
        // Unblock the source and target ports that are being connected
        val sourcePos = calculatePortPosition(sourceDevice, sourcePort)
        val targetPos = calculatePortPosition(targetDevice, targetPort)
        
        // Create temporary grid for this connection that allows source and target ports to be connected
        val tempGrid = RoutingGrid(grid.width, grid.height, grid.cellSize)
        
        // Copy the blocked cells from the original grid to the temporary grid
        for (x in 0 until grid.width) {
            for (y in 0 until grid.height) {
                val point = GridPoint(x, y)
                if (grid.isBlocked(point)) {
                    // Mark as blocked in the temp grid except for source and target port positions
                    val sourceGridPoint = grid.toGridPoint(sourcePos.first, sourcePos.second)
                    val targetGridPoint = grid.toGridPoint(targetPos.first, targetPos.second)
                    
                    val isSourcePortPoint = point.manhattanDistanceTo(sourceGridPoint) <= 2
                    val isTargetPortPoint = point.manhattanDistanceTo(targetGridPoint) <= 2
                    
                    if (!isSourcePortPoint && !isTargetPortPoint) {
                        tempGrid.markDeviceObstacle(
                            x * grid.cellSize, 
                            y * grid.cellSize, 
                            grid.cellSize, 
                            grid.cellSize,
                            0f  // No additional clearance since it's already in the original grid
                        )
                    }
                }
            }
        }

        val sourceGrid = snapPortToGrid(sourcePos, sourceDevice, sourcePort, tempGrid)
        val targetGrid = snapPortToGrid(targetPos, targetDevice, targetPort, tempGrid)
        val sourceSnapped = grid.toVirtualPoint(sourceGrid)
        val targetSnapped = grid.toVirtualPoint(targetGrid)
        
        val adjustedSourcePort = if (true) { // Apply to all sides, not just left/right
            val portIndex = sourceDevice.ports.filter { it.position.side == sourcePort.position.side }
                                             .sortedBy { it.position.position }
                                             .indexOf(sourcePort)
            val positionVariation = portIndex * config.minPathSpacing * 1.5f   
            Port(
                id = sourcePort.id,
                name = sourcePort.name,
                type = sourcePort.type,
                direction = sourcePort.direction,
                position = PortPosition(
                    side = sourcePort.position.side,
                    position = sourcePort.position.position + positionVariation
                )
            )
        } else sourcePort
        
        val adjustedTargetPort = if (true) { // Apply to all sides, not just left/right
            val portIndex = targetDevice.ports.filter { it.position.side == targetPort.position.side }
                                             .sortedBy { it.position.position }
                                             .indexOf(targetPort)
            val positionVariation = portIndex * config.minPathSpacing * 1.5f
            Port(
                id = targetPort.id,
                name = targetPort.name,
                type = targetPort.type,
                direction = targetPort.direction,
                position = PortPosition(
                    side = targetPort.position.side,
                    position = targetPort.position.position + positionVariation
                )
            )
        } else targetPort
        
        val startPoint = calculateExtendedPortPoint(adjustedSourcePort, sourceSnapped, grid)
        val endPoint = calculateExtendedPortPoint(adjustedTargetPort, targetSnapped, grid)

        val startGrid = grid.toGridPoint(startPoint.first, startPoint.second)
        val endGrid = grid.toGridPoint(endPoint.first, endPoint.second)

        val sourceExtensionDir = GridDirection.fromPoints(sourceGrid, startGrid)
        val targetExtensionDir = GridDirection.fromPoints(targetGrid, endGrid)

        if (!tempGrid.canOccupy(sourceGrid, connection.id, sourceExtensionDir) ||
            !tempGrid.canOccupy(startGrid, connection.id, sourceExtensionDir) ||
            !tempGrid.canOccupy(targetGrid, connection.id, targetExtensionDir) ||
            !tempGrid.canOccupy(endGrid, connection.id, targetExtensionDir)) {
            return createFailedRoute(connection, grid, sourceSnapped, targetSnapped)
        }

        // Use the temporary grid with the pathfinder for this specific connection
        val pathfinderForConnection = AStarPathfinder(tempGrid, config)
        val result = pathfinderForConnection.findPath(startGrid, endGrid, connection.id, existingPaths)

        return if (result.success) {
            var lastPoint: GridPoint? = null
            val fullGridPath = buildList {
                fun addUnique(point: GridPoint) {
                    if (point != lastPoint) {
                        add(point)
                        lastPoint = point
                    }
                }
                addUnique(sourceGrid)
                addUnique(startGrid)
                result.waypoints.forEach { addUnique(it) }
                addUnique(endGrid)
                addUnique(targetGrid)
            }
            
            val virtualWaypoints = buildList {
                add(sourceSnapped)
                add(startPoint)
                addAll(result.waypoints.map { grid.toVirtualPoint(it) })
                add(endPoint)
                add(targetSnapped)
            }
            RoutedConnection(connection.id, fullGridPath, virtualWaypoints, true, result.crossings)
        } else {
            createFailedRoute(connection, grid, sourceSnapped, targetSnapped)
        }
    }

    private fun createFailedRoute(
        connection: Connection,
        grid: RoutingGrid,
        sourcePos: Pair<Float, Float> = 0f to 0f,
        targetPos: Pair<Float, Float> = 0f to 0f
    ): RoutedConnection = RoutedConnection(
        connectionId = connection.id,
        waypoints = listOf(
            grid.toGridPoint(sourcePos.first, sourcePos.second),
            grid.toGridPoint(targetPos.first, targetPos.second)
        ),
        virtualWaypoints = listOf(sourcePos, targetPos),
        success = false,
        crossings = 0
    )

    private fun calculateConnectionDistance(connection: Connection, deviceMap: Map<String, Device>): Float {
        val sourceDevice = deviceMap[connection.sourceDeviceId] ?: return Float.MAX_VALUE
        val targetDevice = deviceMap[connection.targetDeviceId] ?: return Float.MAX_VALUE
        val sourcePort = sourceDevice.ports.find { it.id == connection.sourcePortId } ?: return Float.MAX_VALUE
        val targetPort = targetDevice.ports.find { it.id == connection.targetPortId } ?: return Float.MAX_VALUE

        val sourcePos = calculatePortPosition(sourceDevice, sourcePort)
        val targetPos = calculatePortPosition(targetDevice, targetPort)
        val dx = targetPos.first - sourcePos.first
        val dy = targetPos.second - sourcePos.second
        return sqrt(dx * dx + dy * dy)
    }

    private fun snapPortToGrid(
        portPos: Pair<Float, Float>,
        device: Device,
        port: Port,
        grid: RoutingGrid
    ): GridPoint {
        var gridPoint = grid.toGridPoint(portPos.first, portPos.second)
        
        // Calculate device grid bounds
        val deviceGridLeft = (device.position.x / grid.cellSize).toInt()
        val deviceGridRight = ((device.position.x + device.size.width) / grid.cellSize).toInt()
        val deviceGridTop = (device.position.y / grid.cellSize).toInt()
        val deviceGridBottom = ((device.position.y + device.size.height) / grid.cellSize).toInt()
        
        // Adjust grid position to ensure port stays outside device bounds
        gridPoint = when (port.position.side) {
            DeviceSide.TOP -> {
                if (gridPoint.y >= deviceGridTop) {
                    GridPoint(gridPoint.x, deviceGridTop - 1)
                } else gridPoint
            }
            DeviceSide.LEFT -> {
                if (gridPoint.x >= deviceGridLeft) {
                    GridPoint(deviceGridLeft - 1, gridPoint.y)
                } else gridPoint
            }
            DeviceSide.BOTTOM, DeviceSide.RIGHT -> gridPoint
        }
        
        return gridPoint
    }

    private fun calculatePortPosition(device: Device, port: Port): Pair<Float, Float> {
        return when (port.position.side) {
            DeviceSide.TOP -> {
                val positionX = when {
                    port.position.position < 0 -> 0f
                    port.position.position > device.size.width -> device.size.width
                    else -> port.position.position
                }
                device.position.x + positionX to device.position.y
            }
            DeviceSide.BOTTOM -> {
                val positionX = when {
                    port.position.position < 0 -> 0f
                    port.position.position > device.size.width -> device.size.width
                    else -> port.position.position
                }
                device.position.x + positionX to device.position.y + device.size.height
            }
            DeviceSide.LEFT -> {
                val positionY = when {
                    port.position.position < 0 -> 0f
                    port.position.position > device.size.height -> device.size.height
                    else -> port.position.position
                }
                device.position.x to device.position.y + positionY
            }
            DeviceSide.RIGHT -> {
                val positionY = when {
                    port.position.position < 0 -> 0f
                    port.position.position > device.size.height -> device.size.height
                    else -> port.position.position
                }
                device.position.x + device.size.width to device.position.y + positionY
            }
        }
    }

    private fun calculateExtendedPortPoint(
        port: Port, 
        portPos: Pair<Float, Float>, 
        grid: RoutingGrid
    ): Pair<Float, Float> {
        val baseExtension = config.portExtension
        
        // Calculate extension with variation based on port position
        val extension = when (port.position.side) {
            DeviceSide.LEFT, DeviceSide.RIGHT -> {
                val positionRatio = port.position.position / 150f
                val extensionVariation = baseExtension * (0.8f + positionRatio * 0.8f)
                extensionVariation.coerceAtLeast(baseExtension)
            }
            else -> baseExtension
        }
        
        // Calculate extended position
        val extendedPos = when (port.position.side) {
            DeviceSide.LEFT -> portPos.first - extension to portPos.second
            DeviceSide.RIGHT -> portPos.first + extension to portPos.second
            DeviceSide.TOP -> portPos.first to portPos.second - extension
            DeviceSide.BOTTOM -> portPos.first to portPos.second + extension
        }
        
        // Convert to grid coordinates and back to ensure alignment
        val portGrid = grid.toGridPoint(portPos.first, portPos.second)
        val extendedGrid = grid.toGridPoint(extendedPos.first, extendedPos.second)
        
        // Force orthogonal alignment by keeping one coordinate from the port
        val alignedGrid = when (port.position.side) {
            DeviceSide.LEFT, DeviceSide.RIGHT -> GridPoint(extendedGrid.x, portGrid.y)
            DeviceSide.TOP, DeviceSide.BOTTOM -> GridPoint(portGrid.x, extendedGrid.y)
        }
        
        return grid.toVirtualPoint(alignedGrid)
    }
}
