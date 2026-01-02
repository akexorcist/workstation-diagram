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
    
    // Caching mechanism
    private data class CacheKey(
        val canvasWidth: Int,
        val canvasHeight: Int,
        val connectionCount: Int
    )
    private var routingCache: Map<CacheKey, List<RoutedConnection>>? = null
    
    fun routeConnections(
        devices: List<Device>,
        connections: List<Connection>,
        virtualCanvasSize: Size
    ): List<RoutedConnection> {
        // Check cache if enabled
        if (config.enableCaching) {
            val cacheKey = CacheKey(
                canvasWidth = virtualCanvasSize.width.toInt(),
                canvasHeight = virtualCanvasSize.height.toInt(),
                connectionCount = connections.size
            )
            
            routingCache?.get(cacheKey)?.let { return it }
        }
        
        // Initialize grid system with correct dimensions
        val gridWidth = (virtualCanvasSize.width / config.gridCellSize).toInt().coerceAtLeast(1)
        val gridHeight = (virtualCanvasSize.height / config.gridCellSize).toInt().coerceAtLeast(1)
        val grid = RoutingGrid(gridWidth, gridHeight, config.gridCellSize)

        // Create density tracker for path distribution
        val densityTracker = PathDensityTracker(gridWidth, gridHeight, config)

        // Create device lookup map
        val deviceMap = devices.associateBy { it.id }
        
        // Mark all devices as obstacles in the grid
        devices.forEach { device ->
            // Snap device size to grid if configured
            val (width, height) = if (config.deviceSnapToGrid) {
                grid.snapSize(device.size.width, device.size.height)
            } else {
                device.size.width to device.size.height
            }

            // Mark as obstacle with configured clearance
            grid.markDeviceObstacle(device.position.x, device.position.y, width, height, config.deviceClearance)

            // Register in density tracker
            densityTracker.registerDevice(device, config.gridCellSize)
        }
        
        // Mark all ports as obstacles for port avoidance
        devices.forEach { device ->
            device.ports.forEach { port ->
                val portPosition = calculatePortPosition(device, port)
                grid.markPortObstacle(portPosition.first, portPosition.second, config.portClearance)
            }
        }
        
        // Track existing paths for conflict avoidance
        val existingPaths = mutableMapOf<String, List<GridPoint>>()

        // Route connections in order of distance (shortest first)
        val results = connections
            .sortedBy { calculateConnectionDistance(it, deviceMap) }
            .map { connection ->
                routeConnection(connection, deviceMap, grid, densityTracker, existingPaths).also { routed ->
                    // Record successful paths for future routing
                    if (routed.success) {
                        existingPaths[connection.id] = routed.waypoints
                        grid.occupyPath(connection.id, routed.waypoints)
                    }
                }
            }
        
        // Update cache
        if (config.enableCaching) {
            routingCache = mapOf(
                CacheKey(
                    canvasWidth = virtualCanvasSize.width.toInt(),
                    canvasHeight = virtualCanvasSize.height.toInt(),
                    connectionCount = connections.size
                ) to results
            )
        }
        
        return results
    }
    
    private fun routeConnection(
        connection: Connection,
        deviceMap: Map<String, Device>,
        grid: RoutingGrid,
        densityTracker: PathDensityTracker,
        existingPaths: Map<String, List<GridPoint>>
    ): RoutedConnection {
        // Get source and target devices/ports
        val sourceDevice = deviceMap[connection.sourceDeviceId]
        val targetDevice = deviceMap[connection.targetDeviceId]
        val sourcePort = sourceDevice?.ports?.find { it.id == connection.sourcePortId }
        val targetPort = targetDevice?.ports?.find { it.id == connection.targetPortId }
        
        // Handle missing devices/ports
        if (sourceDevice == null || targetDevice == null || sourcePort == null || targetPort == null) {
            return createFailedRoute(connection, grid)
        }
        
        // Calculate port positions
        val sourcePos = calculatePortPosition(sourceDevice, sourcePort)
        val targetPos = calculatePortPosition(targetDevice, targetPort)
        
        // Create a temporary grid that allows routing from source to target ports
        val tempGrid = createPortAccessibleGrid(grid, sourcePos, targetPos)
        
        // Get grid-aligned ports
        val sourceGrid = snapPortToGrid(sourcePos, sourceDevice, sourcePort, tempGrid)
        val targetGrid = snapPortToGrid(targetPos, targetDevice, targetPort, tempGrid)
        val sourceSnapped = grid.toVirtualPoint(sourceGrid)
        val targetSnapped = grid.toVirtualPoint(targetGrid)
        
        // Calculate port extensions
        val startPoint = calculateExtendedPortPoint(sourcePort, sourceSnapped, tempGrid)
        val endPoint = calculateExtendedPortPoint(targetPort, targetSnapped, tempGrid)
        
        val startGrid = tempGrid.toGridPoint(startPoint.first, startPoint.second)
        val endGrid = tempGrid.toGridPoint(endPoint.first, endPoint.second)
        
        // Check if port extensions are valid
        val sourceExtensionDir = GridDirection.fromPoints(sourceGrid, startGrid)
        val targetExtensionDir = GridDirection.fromPoints(targetGrid, endGrid)
        
        if (!tempGrid.canOccupy(sourceGrid, connection.id, sourceExtensionDir) ||
            !tempGrid.canOccupy(startGrid, connection.id, sourceExtensionDir) ||
            !tempGrid.canOccupy(targetGrid, connection.id, targetExtensionDir) ||
            !tempGrid.canOccupy(endGrid, connection.id, targetExtensionDir)) {
            return createFailedRoute(connection, grid, sourceSnapped, targetSnapped)
        }
        
        // Create temporary pathfinder for this specific connection
        val pathfinderForConnection = AStarPathfinder(tempGrid, densityTracker, config)
        
        // Generate waypoint hints to guide the path through balanced distribution
        val waypointHints = generateDistributionWaypoints(startGrid, endGrid, tempGrid, densityTracker)
        
        // Find path with A* algorithm
        val result = pathfinderForConnection.findPath(
            start = startGrid,
            end = endGrid,
            connectionId = connection.id,
            existingPaths = existingPaths,
            waypointHints = waypointHints
        )
        
        return if (result.success) {
            // Create full path including port positions and extensions
            val fullGridPath = buildUniquePointList { list ->
                list.add(sourceGrid)
                list.add(startGrid)
                list.addAll(result.waypoints)
                list.add(endGrid)
                list.add(targetGrid)
            }
            
            // Convert to virtual coordinates
            val virtualWaypoints = listOf(sourceSnapped) + 
                                   listOf(startPoint) +
                                   result.waypoints.map { tempGrid.toVirtualPoint(it) } +
                                   listOf(endPoint) +
                                   listOf(targetSnapped)
            
            RoutedConnection(connection.id, fullGridPath, virtualWaypoints, true, result.crossings)
        } else {
            createFailedRoute(connection, grid, sourceSnapped, targetSnapped)
        }
    }
    
    private fun createPortAccessibleGrid(
        originalGrid: RoutingGrid,
        sourcePos: Pair<Float, Float>, 
        targetPos: Pair<Float, Float>
    ): RoutingGrid {
        val tempGrid = RoutingGrid(originalGrid.width, originalGrid.height, originalGrid.cellSize)
        
        // Copy obstacles from original grid, but unblock ports
        for (x in 0 until originalGrid.width) {
            for (y in 0 until originalGrid.height) {
                val point = GridPoint(x, y)
                if (originalGrid.isBlocked(point)) {
                    // Check if this point is at or near source/target port
                    val sourceGridPoint = originalGrid.toGridPoint(sourcePos.first, sourcePos.second)
                    val targetGridPoint = originalGrid.toGridPoint(targetPos.first, targetPos.second)
                    
                    // Keep a small area around ports unblocked (radius 2 cells)
                    val isSourcePortPoint = point.manhattanDistanceTo(sourceGridPoint) <= 2
                    val isTargetPortPoint = point.manhattanDistanceTo(targetGridPoint) <= 2
                    
                    if (!isSourcePortPoint && !isTargetPortPoint) {
                        tempGrid.markDeviceObstacle(
                            x * originalGrid.cellSize, 
                            y * originalGrid.cellSize, 
                            originalGrid.cellSize, 
                            originalGrid.cellSize,
                            0f
                        )
                    }
                }
            }
        }
        
        return tempGrid
    }
    
    private fun generateDistributionWaypoints(
        start: GridPoint,
        end: GridPoint,
        grid: RoutingGrid,
        densityTracker: PathDensityTracker
    ): List<GridPoint> {
        // Get corridor center points if available
        val corridorPoints = grid.findDeviceCorridor(start, end)
        
        if (corridorPoints != null) {
            val (corridorTop, corridorBottom) = corridorPoints
            return listOf(
                // Only use the corridor points if they're not blocked
                if (!grid.isBlocked(corridorTop)) corridorTop else return emptyList(),
                if (!grid.isBlocked(corridorBottom)) corridorBottom else return listOf(corridorTop)
            )
        }
        
        // Alternatively, use device pathway center from density tracker
        val pathwayCenter = densityTracker.getDevicePathwayCenter(start, end)
        
        return if (pathwayCenter != null && !grid.isBlocked(pathwayCenter)) {
            listOf(pathwayCenter)
        } else {
            emptyList()
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

    private fun calculateConnectionDistance(
        connection: Connection, 
        deviceMap: Map<String, Device>
    ): Float {
        // Get devices and ports
        val sourceDevice = deviceMap[connection.sourceDeviceId] ?: return Float.MAX_VALUE
        val targetDevice = deviceMap[connection.targetDeviceId] ?: return Float.MAX_VALUE
        val sourcePort = sourceDevice.ports.find { it.id == connection.sourcePortId } ?: return Float.MAX_VALUE
        val targetPort = targetDevice.ports.find { it.id == connection.targetPortId } ?: return Float.MAX_VALUE

        // Calculate Euclidean distance between ports
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
        // Convert virtual coordinates to grid point
        var gridPoint = grid.toGridPoint(portPos.first, portPos.second)
        
        // Calculate device grid bounds
        val deviceGridLeft = (device.position.x / grid.cellSize).toInt()
        val deviceGridRight = ((device.position.x + device.size.width) / grid.cellSize).toInt()
        val deviceGridTop = (device.position.y / grid.cellSize).toInt()
        val deviceGridBottom = ((device.position.y + device.size.height) / grid.cellSize).toInt()
        
        // Adjust to ensure port is outside device bounds
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
            DeviceSide.BOTTOM -> {
                if (gridPoint.y <= deviceGridBottom) {
                    GridPoint(gridPoint.x, deviceGridBottom + 1)
                } else gridPoint
            }
            DeviceSide.RIGHT -> {
                if (gridPoint.x <= deviceGridRight) {
                    GridPoint(deviceGridRight + 1, gridPoint.y)
                } else gridPoint
            }
        }
        
        return gridPoint
    }

    private fun calculatePortPosition(device: Device, port: Port): Pair<Float, Float> {
        // Calculate port position based on device edge and port offset
        return when (port.position.side) {
            DeviceSide.TOP -> {
                val positionX = port.position.position.coerceIn(0f, device.size.width)
                device.position.x + positionX to device.position.y
            }
            DeviceSide.BOTTOM -> {
                val positionX = port.position.position.coerceIn(0f, device.size.width)
                device.position.x + positionX to device.position.y + device.size.height
            }
            DeviceSide.LEFT -> {
                val positionY = port.position.position.coerceIn(0f, device.size.height)
                device.position.x to device.position.y + positionY
            }
            DeviceSide.RIGHT -> {
                val positionY = port.position.position.coerceIn(0f, device.size.height)
                device.position.x + device.size.width to device.position.y + positionY
            }
        }
    }

    private fun calculateExtendedPortPoint(
        port: Port, 
        portPos: Pair<Float, Float>, 
        grid: RoutingGrid
    ): Pair<Float, Float> {
        // Get base extension length from config
        val baseExtension = config.portExtension
        
        // Adjust extension length based on device side and port position
        val extension = when (port.position.side) {
            DeviceSide.LEFT, DeviceSide.RIGHT -> {
                // Add slight variation for left/right ports based on vertical position
                val positionRatio = port.position.position / 150f
                val extensionVariation = baseExtension * (0.8f + positionRatio * 0.4f)
                extensionVariation.coerceAtLeast(baseExtension * 0.8f)
            }
            DeviceSide.TOP, DeviceSide.BOTTOM -> {
                // Add slight variation for top/bottom ports based on horizontal position
                val positionRatio = port.position.position / 150f
                val extensionVariation = baseExtension * (0.8f + positionRatio * 0.4f)
                extensionVariation.coerceAtLeast(baseExtension * 0.8f)
            }
        }
        
        // Calculate extended position based on port side
        val extendedPos = when (port.position.side) {
            DeviceSide.LEFT -> portPos.first - extension to portPos.second
            DeviceSide.RIGHT -> portPos.first + extension to portPos.second
            DeviceSide.TOP -> portPos.first to portPos.second - extension
            DeviceSide.BOTTOM -> portPos.first to portPos.second + extension
        }
        
        // Align extension to grid by preserving orthogonal movement
        val portGrid = grid.toGridPoint(portPos.first, portPos.second)
        val extendedGrid = grid.toGridPoint(extendedPos.first, extendedPos.second)
        
        val alignedGrid = when (port.position.side) {
            DeviceSide.LEFT, DeviceSide.RIGHT -> GridPoint(extendedGrid.x, portGrid.y)
            DeviceSide.TOP, DeviceSide.BOTTOM -> GridPoint(portGrid.x, extendedGrid.y)
        }
        
        return grid.toVirtualPoint(alignedGrid)
    }
    
    private fun buildUniquePointList(block: (MutableList<GridPoint>) -> Unit): List<GridPoint> {
        // Create a temporary list and apply the block to it
        val tempList = mutableListOf<GridPoint>()
        block(tempList)
        
        // Filter out consecutive duplicates
        val uniqueList = mutableListOf<GridPoint>()
        
        for (point in tempList) {
            if (uniqueList.isEmpty() || uniqueList.last() != point) {
                uniqueList.add(point)
            }
        }
        
        return uniqueList
    }
}
