package dev.akexorcist.workstation.routing

import dev.akexorcist.workstation.data.model.*
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.math.min
import kotlin.math.max

private data class PortPosition(
    val portId: String,
    val deviceId: String,
    val virtualPos: Pair<Float, Float>,
    val gridPos: GridPoint,
    val side: DeviceSide
)

private data class PortExtension(
    val portId: String,
    val startPos: GridPoint,
    val endPos: GridPoint,
    val direction: GridDirection,
    val length: Float
)

private data class DevicePairCorridor(
    val sourceDeviceId: String,
    val targetDeviceId: String,
    val centerPoint: GridPoint,
    val availableSpace: Int,
    val connections: MutableList<Connection>
)

private data class ConnectionWithDistance(
    val connection: Connection,
    val distance: Float,
    val sourcePortPos: GridPoint,
    val targetPortPos: GridPoint
)

class ConnectorPathRouter(private val config: RoutingConfig = RoutingConfig) {
    
    fun routeConnections(
        devices: List<Device>,
        connections: List<Connection>,
        virtualCanvasSize: Size
    ): List<RoutedConnection> {
        val grid = setupGrid(virtualCanvasSize)
        val deviceMap = devices.associateBy { it.id }
        val involvedPortIds = connections.flatMap { listOf(it.sourcePortId, it.targetPortId) }.toSet()
        
        markObstacles(devices, connections, involvedPortIds, grid)
        
        val portPositions = calculatePortPositions(devices, grid)
        val portExtensions = calculatePortExtensions(portPositions, connections, grid)
        
        val orderedConnections = orderConnections(connections, portPositions)
        
        val corridors = groupConnectionsByDevicePairs(orderedConnections, devices, grid)
        
        val routedPaths = mutableMapOf<String, List<GridPoint>>()
        val routedConnections = mutableListOf<RoutedConnection>()
        
        corridors.forEach { corridor ->
            corridor.connections.forEachIndexed { index, connection ->
                val sourcePort = portPositions[connection.sourcePortId]
                val targetPort = portPositions[connection.targetPortId]
                val sourceExtension = portExtensions[connection.sourcePortId]
                val targetExtension = portExtensions[connection.targetPortId]
                
                if (sourcePort == null || targetPort == null || 
                    sourceExtension == null || targetExtension == null) {
                    routedConnections.add(createFailedConnection(connection, sourcePort, targetPort))
                    return@forEachIndexed
                }
                
                val distributionWaypoint = if (index == 0) {
                    corridor.centerPoint
                } else {
                    calculateDistributionOffset(corridor, index, routedPaths, grid)
                }
                
                val pathResult = routeWithDistribution(
                    connection,
                    sourceExtension.endPos,
                    targetExtension.endPos,
                    distributionWaypoint,
                    routedPaths,
                    grid
                )
                
                if (pathResult.success && pathResult.waypoints.isNotEmpty()) {
                    var optimizedPath = pathResult.waypoints
                    optimizedPath = ensureOrthogonal(optimizedPath)
                    optimizedPath = removeReversedTurnsAtStart(optimizedPath, sourceExtension, targetExtension, grid)
                    optimizedPath = ensureOrthogonal(optimizedPath)
                    optimizedPath = removeZigzags(optimizedPath)
                    optimizedPath = ensureOrthogonal(optimizedPath)
                    optimizedPath = removeReversedTurns(optimizedPath, grid)
                    optimizedPath = ensureOrthogonal(optimizedPath)
                    optimizedPath = minimizeTurns(optimizedPath)
                    optimizedPath = ensureOrthogonal(optimizedPath)
                    optimizedPath = enforceSpacingPreservingCenter(
                        optimizedPath,
                        connection.id,
                        routedPaths,
                        corridor,
                        index,
                        grid
                    )
                    optimizedPath = ensureOrthogonal(optimizedPath)
                    optimizedPath = preventSegmentOverlap(optimizedPath, connection.id, routedPaths, grid)
                    optimizedPath = ensureOrthogonal(optimizedPath)
                    optimizedPath = validateOrthogonal(optimizedPath)
                    
                    routedPaths[connection.id] = optimizedPath
                    
                    val fullGridPath = buildList {
                        add(sourcePort.gridPos)
                        if (sourcePort.gridPos != sourceExtension.startPos) {
                            add(sourceExtension.startPos)
                        }
                        optimizedPath.forEach { point ->
                            if (isEmpty() || last() != point) {
                                add(point)
                            }
                        }
                        if (last() != targetExtension.endPos) {
                            add(targetExtension.endPos)
                        }
                        if (last() != targetPort.gridPos) {
                            add(targetPort.gridPos)
                        }
                    }
                    
                    val virtualWaypoints = buildList {
                        add(sourcePort.virtualPos)
                        add(grid.toVirtualPoint(sourceExtension.startPos))
                        optimizedPath.forEach { gridPoint ->
                            add(grid.toVirtualPoint(gridPoint))
                        }
                        add(grid.toVirtualPoint(targetExtension.endPos))
                        add(targetPort.virtualPos)
                    }
                    
                    val crossings = countCrossings(optimizedPath, routedPaths.values.filter { it.isNotEmpty() })
                    
                    routedConnections.add(
                        RoutedConnection(
                            connectionId = connection.id,
                            waypoints = fullGridPath,
                            virtualWaypoints = virtualWaypoints,
                            success = true,
                            crossings = crossings
                        )
                    )
                } else {
                    routedConnections.add(createFailedConnection(connection, sourcePort, targetPort))
                }
            }
        }
        
        return routedConnections
    }
    
    private fun setupGrid(virtualCanvasSize: Size): RoutingGrid {
        val gridWidth = (virtualCanvasSize.width / config.gridCellSize).toInt().coerceAtLeast(1)
        val gridHeight = (virtualCanvasSize.height / config.gridCellSize).toInt().coerceAtLeast(1)
        return RoutingGrid(gridWidth, gridHeight, config.gridCellSize)
    }
    
    private fun markObstacles(
        devices: List<Device>,
        connections: List<Connection>,
        involvedPortIds: Set<String>,
        grid: RoutingGrid
    ) {
        devices.forEach { device ->
            val (width, height) = if (config.deviceSnapToGrid) {
                grid.snapSize(device.size.width, device.size.height)
            } else {
                device.size.width to device.size.height
            }
            
            grid.markDeviceObstacle(
                device.position.x,
                device.position.y,
                width,
                height,
                config.deviceClearance
            )
            
            device.ports.forEach { port ->
                if (port.id !in involvedPortIds) {
                    val portVirtualPos = calculatePortPosition(device, port)
                    val portGridPos = grid.toGridPoint(portVirtualPos.first, portVirtualPos.second)
                    val adjustedVirtualPos = grid.toVirtualPoint(portGridPos)
                    
                    grid.markPortObstacle(
                        adjustedVirtualPos.first,
                        adjustedVirtualPos.second,
                        config.portClearance
                    )
                    
                    val extensionDirection = when (port.position.side) {
                        DeviceSide.LEFT -> GridDirection.WEST
                        DeviceSide.RIGHT -> GridDirection.EAST
                        DeviceSide.TOP -> GridDirection.NORTH
                        DeviceSide.BOTTOM -> GridDirection.SOUTH
                    }
                    
                    grid.markPortExtensionObstacle(
                        adjustedVirtualPos.first,
                        adjustedVirtualPos.second,
                        extensionDirection,
                        config.portExtension,
                        config.portClearance
                    )
                }
            }
        }
    }
    
    private fun calculatePortPositions(devices: List<Device>, grid: RoutingGrid): Map<String, PortPosition> {
        val portPositions = mutableMapOf<String, PortPosition>()
        
        devices.forEach { device ->
            device.ports.forEach { port ->
                val portVirtualPos = calculatePortPosition(device, port)
                val portGridPos = grid.toGridPoint(portVirtualPos.first, portVirtualPos.second)
                val adjustedGridPos = adjustPortGridPosition(portGridPos, device, port, grid)
                
                portPositions[port.id] = PortPosition(
                    portId = port.id,
                    deviceId = device.id,
                    virtualPos = portVirtualPos,
                    gridPos = adjustedGridPos,
                    side = port.position.side
                )
            }
        }
        
        return portPositions
    }
    
    private fun calculatePortExtensions(
        portPositions: Map<String, PortPosition>,
        connections: List<Connection>,
        grid: RoutingGrid
    ): Map<String, PortExtension> {
        val portExtensions = mutableMapOf<String, PortExtension>()
        
        connections.forEach { connection ->
            val sourcePort = portPositions[connection.sourcePortId]
            val targetPort = portPositions[connection.targetPortId]
            
            if (sourcePort != null && targetPort != null) {
                val sourceExtension = calculatePortExtension(sourcePort, grid)
                val targetExtension = calculatePortExtension(targetPort, grid)
                portExtensions[connection.sourcePortId] = sourceExtension
                portExtensions[connection.targetPortId] = targetExtension
            }
        }
        
        return portExtensions
    }
    
    private fun orderConnections(
        connections: List<Connection>,
        portPositions: Map<String, PortPosition>
    ): List<ConnectionWithDistance> {
        return connections.mapNotNull { connection ->
            val sourcePortPos = portPositions[connection.sourcePortId]?.gridPos
            val targetPortPos = portPositions[connection.targetPortId]?.gridPos
            
            if (sourcePortPos != null && targetPortPos != null) {
                val dx = targetPortPos.x - sourcePortPos.x
                val dy = targetPortPos.y - sourcePortPos.y
                val distance = sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                
                ConnectionWithDistance(connection, distance, sourcePortPos, targetPortPos)
            } else null
        }.sortedBy { it.distance }
    }
    
    private fun groupConnectionsByDevicePairs(
        orderedConnections: List<ConnectionWithDistance>,
        devices: List<Device>,
        grid: RoutingGrid
    ): List<DevicePairCorridor> {
        val deviceMap = devices.associateBy { it.id }
        val corridors = mutableMapOf<String, DevicePairCorridor>()
        
        orderedConnections.forEach { connWithDist ->
            val deviceIds = listOf(connWithDist.connection.sourceDeviceId, connWithDist.connection.targetDeviceId).sorted()
            val pairKey = "${deviceIds[0]}-${deviceIds[1]}"
            
            val existingCorridor = corridors[pairKey]
            
            if (existingCorridor != null) {
                existingCorridor.connections.add(connWithDist.connection)
            } else {
                val sourceDevice = deviceMap[connWithDist.connection.sourceDeviceId]
                val targetDevice = deviceMap[connWithDist.connection.targetDeviceId]
                
                if (sourceDevice != null && targetDevice != null) {
                    val centerPoint = calculateCorridorCenter(sourceDevice, targetDevice, grid)
                    val availableSpace = calculateAvailableSpace(sourceDevice, targetDevice, grid)
                    
                    val corridor = DevicePairCorridor(
                        sourceDeviceId = connWithDist.connection.sourceDeviceId,
                        targetDeviceId = connWithDist.connection.targetDeviceId,
                        centerPoint = centerPoint,
                        availableSpace = availableSpace,
                        connections = mutableListOf(connWithDist.connection)
                    )
                    
                    corridors[pairKey] = corridor
                }
            }
        }
        
        return corridors.values.toList()
    }
    
    private fun calculateCorridorCenter(sourceDevice: Device, targetDevice: Device, grid: RoutingGrid): GridPoint {
        val sourceLeft = (sourceDevice.position.x / grid.cellSize).toInt()
        val sourceRight = ((sourceDevice.position.x + sourceDevice.size.width) / grid.cellSize).toInt()
        val sourceTop = (sourceDevice.position.y / grid.cellSize).toInt()
        val sourceBottom = ((sourceDevice.position.y + sourceDevice.size.height) / grid.cellSize).toInt()
        
        val targetLeft = (targetDevice.position.x / grid.cellSize).toInt()
        val targetRight = ((targetDevice.position.x + targetDevice.size.width) / grid.cellSize).toInt()
        val targetTop = (targetDevice.position.y / grid.cellSize).toInt()
        val targetBottom = ((targetDevice.position.y + targetDevice.size.height) / grid.cellSize).toInt()
        
        val minX = min(sourceLeft, targetLeft)
        val maxX = max(sourceRight, targetRight)
        val minY = min(sourceTop, targetTop)
        val maxY = max(sourceBottom, targetBottom)
        
        val centerX = (minX + maxX) / 2
        val centerY = (minY + maxY) / 2
        
        return GridPoint(centerX.coerceIn(0, grid.width - 1), centerY.coerceIn(0, grid.height - 1))
    }
    
    private fun calculateAvailableSpace(sourceDevice: Device, targetDevice: Device, grid: RoutingGrid): Int {
        val sourceLeft = (sourceDevice.position.x / grid.cellSize).toInt()
        val sourceRight = ((sourceDevice.position.x + sourceDevice.size.width) / grid.cellSize).toInt()
        val targetLeft = (targetDevice.position.x / grid.cellSize).toInt()
        val targetRight = ((targetDevice.position.x + targetDevice.size.width) / grid.cellSize).toInt()
        
        val minX = min(sourceLeft, targetLeft)
        val maxX = max(sourceRight, targetRight)
        
        return maxX - minX
    }
    
    private fun calculateDistributionOffset(
        corridor: DevicePairCorridor,
        index: Int,
        routedPaths: Map<String, List<GridPoint>>,
        grid: RoutingGrid
    ): GridPoint? {
        val minSpacingCells = (config.minPathSpacing / grid.cellSize).toInt().coerceAtLeast(1)
        val offsetIndex = (index - 1) / 2 + 1
        val isRightSide = (index - 1) % 2 == 0
        
        val offset = offsetIndex * minSpacingCells
        val offsetX = if (isRightSide) offset else -offset
        
        val centerX = corridor.centerPoint.x
        val newX = (centerX + offsetX).coerceIn(0, grid.width - 1)
        
        val centerY = corridor.centerPoint.y
        val newY = centerY.coerceIn(0, grid.height - 1)
        
        val candidatePoint = GridPoint(newX, newY)
        
        if (grid.isBlocked(candidatePoint)) {
            return null
        }
        
        return candidatePoint
    }
    
    private fun routeWithDistribution(
        connection: Connection,
        start: GridPoint,
        end: GridPoint,
        distributionWaypoint: GridPoint?,
        existingPaths: Map<String, List<GridPoint>>,
        grid: RoutingGrid
    ): PathResult {
        val pathfinder = AStarPathfinder(grid, null, config)
        
        val waypointHints = if (distributionWaypoint != null) {
            listOf(distributionWaypoint)
        } else {
            emptyList()
        }
        
        return pathfinder.findPath(start, end, connection.id, existingPaths, waypointHints)
    }
    
    private fun removeReversedTurnsAtStart(
        waypoints: List<GridPoint>,
        sourceExtension: PortExtension,
        targetExtension: PortExtension,
        grid: RoutingGrid
    ): List<GridPoint> {
        if (waypoints.size < 2) return waypoints
        
        val sourceExtDir = sourceExtension.direction
        val targetExtDir = targetExtension.direction
        
        if (waypoints.size >= 2) {
            val firstDir = GridDirection.fromPoints(waypoints[0], waypoints[1])
            
            if (firstDir == sourceExtDir.opposite()) {
                if (waypoints.size >= 3) {
                    val secondDir = GridDirection.fromPoints(waypoints[1], waypoints[2])
                    if (secondDir.isPerpendicular(sourceExtDir)) {
                        return listOf(waypoints[0]) + waypoints.drop(2)
                    }
                }
                
                val alternative = findStartAlternative(waypoints[0], sourceExtDir, grid)
                if (alternative != null) {
                    return listOf(waypoints[0], alternative) + waypoints.drop(1)
                }
            }
        }
        
        if (waypoints.size >= 2) {
            val lastDir = GridDirection.fromPoints(waypoints[waypoints.size - 2], waypoints[waypoints.size - 1])
            
            if (lastDir == targetExtDir.opposite()) {
                if (waypoints.size >= 3) {
                    val secondLastDir = GridDirection.fromPoints(waypoints[waypoints.size - 3], waypoints[waypoints.size - 2])
                    if (secondLastDir.isPerpendicular(targetExtDir)) {
                        return waypoints.dropLast(2) + listOf(waypoints.last())
                    }
                }
                
                val alternative = findStartAlternative(waypoints.last(), targetExtDir, grid)
                if (alternative != null) {
                    return waypoints.dropLast(1) + listOf(alternative, waypoints.last())
                }
            }
        }
        
        return validateOrthogonal(waypoints)
    }
    
    private fun findStartAlternative(
        start: GridPoint,
        extensionDir: GridDirection,
        grid: RoutingGrid
    ): GridPoint? {
        val perpDirs = when {
            extensionDir.isVertical() -> listOf(GridDirection.EAST, GridDirection.WEST)
            else -> listOf(GridDirection.NORTH, GridDirection.SOUTH)
        }
        
        for (dir in perpDirs) {
            val offset = when (dir) {
                GridDirection.NORTH -> GridPoint(0, -1)
                GridDirection.SOUTH -> GridPoint(0, 1)
                GridDirection.EAST -> GridPoint(1, 0)
                GridDirection.WEST -> GridPoint(-1, 0)
            }
            
            val candidate = GridPoint(
                (start.x + offset.x).coerceIn(0, grid.width - 1),
                (start.y + offset.y).coerceIn(0, grid.height - 1)
            )
            
            if (!grid.isBlocked(candidate)) {
                return candidate
            }
        }
        
        return null
    }
    
    private fun ensureOrthogonal(waypoints: List<GridPoint>): List<GridPoint> {
        if (waypoints.size < 2) return waypoints
        
        val result = mutableListOf<GridPoint>()
        result.add(waypoints.first())
        
        for (i in 1 until waypoints.size) {
            val prev = result.last()
            val current = waypoints[i]
            
            val dx = current.x - prev.x
            val dy = current.y - prev.y
            
            when {
                dx == 0 && dy == 0 -> continue
                dx != 0 && dy != 0 -> {
                    val corner1 = GridPoint(current.x, prev.y)
                    val corner2 = GridPoint(prev.x, current.y)
                    
                    val dist1 = abs(dx) + abs(prev.y - current.y)
                    val dist2 = abs(prev.x - current.x) + abs(dy)
                    
                    val corner = if (dist1 <= dist2) corner1 else corner2
                    
                    if (corner != prev) {
                        result.add(corner)
                    }
                    if (corner != current && current != result.lastOrNull()) {
                        result.add(current)
                    }
                }
                else -> {
                    if (current != result.last()) {
                        result.add(current)
                    }
                }
            }
        }
        
        val finalResult = result.distinct()
        return validateOrthogonal(finalResult)
    }
    
    private fun validateOrthogonal(waypoints: List<GridPoint>): List<GridPoint> {
        if (waypoints.size < 2) return waypoints
        
        val result = mutableListOf<GridPoint>()
        result.add(waypoints.first())
        
        for (i in 1 until waypoints.size) {
            val prev = result.last()
            val current = waypoints[i]
            
            val dx = current.x - prev.x
            val dy = current.y - prev.y
            
            if (dx == 0 && dy == 0) {
                continue
            } else if (dx != 0 && dy != 0) {
                val corner = if (abs(dx) > abs(dy)) {
                    GridPoint(current.x, prev.y)
                } else {
                    GridPoint(prev.x, current.y)
                }
                
                if (corner != prev) {
                    result.add(corner)
                }
                if (corner != current) {
                    result.add(current)
                }
            } else {
                result.add(current)
            }
        }
        
        return result.distinct()
    }
    
    private fun removeZigzags(waypoints: List<GridPoint>): List<GridPoint> {
        if (waypoints.size < 4) return waypoints
        
        val result = mutableListOf<GridPoint>()
        result.add(waypoints.first())
        
        var i = 1
        while (i < waypoints.size - 2) {
            val a = result.last()
            val b = waypoints[i]
            val c = waypoints[i + 1]
            val d = waypoints[i + 2]
            
            if (isZigzag(a, b, c, d)) {
                val corner = getZigzagCorner(a, d)
                result.add(corner)
                i += 3
            } else {
                result.add(b)
                i++
            }
        }
        
        while (i < waypoints.size) {
            result.add(waypoints[i])
            i++
        }
        
        return result
    }
    
    private fun isZigzag(a: GridPoint, b: GridPoint, c: GridPoint, d: GridPoint): Boolean {
        val dir1 = GridDirection.fromPoints(a, b)
        val dir2 = GridDirection.fromPoints(b, c)
        val dir3 = GridDirection.fromPoints(c, d)
        
        return dir1.isPerpendicular(dir2) && dir2.isPerpendicular(dir3) && 
               (dir1.isHorizontal() == dir3.isHorizontal())
    }
    
    private fun getZigzagCorner(start: GridPoint, end: GridPoint): GridPoint {
        return if (start.x == end.x) {
            GridPoint(start.x, end.y)
        } else if (start.y == end.y) {
            GridPoint(end.x, start.y)
        } else {
            GridPoint(end.x, start.y)
        }
    }
    
    private fun removeReversedTurns(waypoints: List<GridPoint>, grid: RoutingGrid): List<GridPoint> {
        if (waypoints.size < 3) return waypoints
        
        var currentPath = waypoints
        var iterations = 0
        val maxIterations = 10
        
        while (iterations < maxIterations) {
            val result = mutableListOf<GridPoint>()
            result.add(currentPath.first())
            
            var i = 1
            var changed = false
            
            while (i < currentPath.size - 1) {
                val prev = result.last()
                val current = currentPath[i]
                val next = currentPath[i + 1]
                
                val dir1 = GridDirection.fromPoints(prev, current)
                val dir2 = GridDirection.fromPoints(current, next)
                
                if (dir1 == dir2.opposite()) {
                    val alternative = findAlternativeForReverseTurn(prev, current, next, grid)
                    if (alternative != null && alternative != current) {
                        result.add(alternative)
                        i += 2
                        changed = true
                    } else {
                        result.add(current)
                        i++
                    }
                } else {
                    result.add(current)
                    i++
                }
            }
            
            if (result.last() != currentPath.last()) {
                result.add(currentPath.last())
            }
            
            if (!changed) break
            
            currentPath = result
            iterations++
        }
        
        return validateOrthogonal(currentPath)
    }
    
    private fun findAlternativeForReverseTurn(
        prev: GridPoint,
        current: GridPoint,
        next: GridPoint,
        grid: RoutingGrid
    ): GridPoint? {
        val dir1 = GridDirection.fromPoints(prev, current)
        val dir2 = GridDirection.fromPoints(current, next)
        
        if (dir1 != dir2.opposite()) return null
        
        val perpendicularDirs = when {
            dir1.isVertical() -> listOf(GridDirection.EAST, GridDirection.WEST)
            else -> listOf(GridDirection.NORTH, GridDirection.SOUTH)
        }
        
        val candidates = mutableListOf<GridPoint>()
        val searchRadius = 3
        
        for (radius in 1..searchRadius) {
            perpendicularDirs.forEach { perpDir ->
                val offset = when (perpDir) {
                    GridDirection.NORTH -> GridPoint(0, -radius)
                    GridDirection.SOUTH -> GridPoint(0, radius)
                    GridDirection.EAST -> GridPoint(radius, 0)
                    GridDirection.WEST -> GridPoint(-radius, 0)
                }
                
                val candidate1 = GridPoint(
                    (current.x + offset.x).coerceIn(0, grid.width - 1),
                    (current.y + offset.y).coerceIn(0, grid.height - 1)
                )
                val candidate2 = GridPoint(
                    (current.x - offset.x).coerceIn(0, grid.width - 1),
                    (current.y - offset.y).coerceIn(0, grid.height - 1)
                )
                
                listOf(candidate1, candidate2).forEach { candidate ->
                    if (candidate != current &&
                        !grid.isBlocked(candidate) &&
                        isValidTransition(prev, candidate, next)) {
                        candidates.add(candidate)
                    }
                }
            }
            
            if (candidates.isNotEmpty()) break
        }
        
        return candidates.minByOrNull {
            it.manhattanDistanceTo(prev) + it.manhattanDistanceTo(next)
        }
    }
    
    private fun isValidTransition(prev: GridPoint, current: GridPoint, next: GridPoint): Boolean {
        val dx1 = current.x - prev.x
        val dy1 = current.y - prev.y
        val dx2 = next.x - current.x
        val dy2 = next.y - current.y
        
        return (dx1 == 0 || dy1 == 0) && (dx2 == 0 || dy2 == 0)
    }
    
    private fun minimizeTurns(waypoints: List<GridPoint>): List<GridPoint> {
        if (waypoints.size <= 2) return waypoints
        
        var simplified = waypoints
        var changed = true
        var iterations = 0
        val maxIterations = 10
        
        while (changed && iterations < maxIterations) {
            val previousSize = simplified.size
            simplified = removeCollinearPoints(simplified)
            simplified = validateOrthogonal(simplified)
            simplified = removeUnnecessaryCorners(simplified)
            simplified = validateOrthogonal(simplified)
            simplified = removeLongUnnecessaryPatterns(simplified)
            simplified = validateOrthogonal(simplified)
            simplified = tryDirectPaths(simplified)
            simplified = validateOrthogonal(simplified)
            changed = simplified.size < previousSize
            iterations++
        }
        
        return validateOrthogonal(simplified)
    }
    
    private fun removeCollinearPoints(waypoints: List<GridPoint>): List<GridPoint> {
        if (waypoints.size <= 2) return waypoints
        
        val result = mutableListOf<GridPoint>()
        result.add(waypoints.first())
        
        var i = 1
        while (i < waypoints.size - 1) {
            val prev = result.last()
            val current = waypoints[i]
            val next = waypoints[i + 1]
            
            val dir1 = GridDirection.fromPoints(prev, current)
            val dir2 = GridDirection.fromPoints(current, next)
            
            if (dir1 == dir2) {
                i++
            } else {
                result.add(current)
                i++
            }
        }
        
        result.add(waypoints.last())
        return result
    }
    
    private fun removeUnnecessaryCorners(waypoints: List<GridPoint>): List<GridPoint> {
        if (waypoints.size < 4) return waypoints
        
        var result = waypoints.toMutableList()
        var changed = true
        var iterations = 0
        val maxIterations = 5
        
        while (changed && iterations < maxIterations) {
            changed = false
            val newResult = mutableListOf<GridPoint>()
            newResult.add(result.first())
            
            var i = 1
            while (i < result.size - 2) {
                val prev = newResult.last()
                val current = result[i]
                val next = result[i + 1]
                val afterNext = result[i + 2]
                
                val dir1 = GridDirection.fromPoints(prev, current)
                val dir2 = GridDirection.fromPoints(current, next)
                val dir3 = GridDirection.fromPoints(next, afterNext)
                
                if (dir1 == dir3 && dir1.isPerpendicular(dir2)) {
                    if (isDirectPath(prev, afterNext)) {
                        newResult.add(afterNext)
                        i += 3
                        changed = true
                    } else {
                        newResult.add(current)
                        i++
                    }
                } else if (i < result.size - 3) {
                    val afterAfterNext = result[i + 3]
                    val dir4 = GridDirection.fromPoints(afterNext, afterAfterNext)
                    
                    if (dir1 == dir4 && dir1.isPerpendicular(dir2) && dir2 == dir3.opposite()) {
                        if (isDirectPath(prev, afterAfterNext)) {
                            newResult.add(afterAfterNext)
                            i += 4
                            changed = true
                        } else {
                            newResult.add(current)
                            i++
                        }
                    } else {
                        newResult.add(current)
                        i++
                    }
                } else {
                    newResult.add(current)
                    i++
                }
            }
            
            while (i < result.size) {
                newResult.add(result[i])
                i++
            }
            
            result = newResult
            iterations++
        }
        
        return result
    }
    
    private fun tryDirectPaths(waypoints: List<GridPoint>): List<GridPoint> {
        if (waypoints.size <= 2) return waypoints
        
        val result = mutableListOf<GridPoint>()
        result.add(waypoints.first())
        
        var i = 1
        while (i < waypoints.size) {
            val start = result.last()
            var bestSkip = i
            var bestEnd = waypoints[i]
            
            var j = i
            while (j < waypoints.size) {
                val candidate = waypoints[j]
                if (isDirectPath(start, candidate)) {
                    bestSkip = j
                    bestEnd = candidate
                    j++
                } else {
                    val nextIdx = j + 1
                    if (nextIdx < waypoints.size) {
                        val nextCandidate = waypoints[nextIdx]
                        if (isDirectPath(start, nextCandidate)) {
                            bestSkip = nextIdx
                            bestEnd = nextCandidate
                            j = nextIdx + 1
                        } else {
                            break
                        }
                    } else {
                        break
                    }
                }
            }
            
            if (bestSkip > i) {
                result.add(bestEnd)
                i = bestSkip + 1
            } else {
                result.add(waypoints[i])
                i++
            }
        }
        
        return validateOrthogonal(result)
    }
    
    private fun removeLongUnnecessaryPatterns(waypoints: List<GridPoint>): List<GridPoint> {
        if (waypoints.size < 5) return waypoints
        
        val result = mutableListOf<GridPoint>()
        result.add(waypoints.first())
        
        var i = 1
        while (i < waypoints.size - 3) {
            val start = result.last()
            var foundPattern = false
            var skipTo = i + 1
            
            for (j in (i + 3) until waypoints.size) {
                val candidate = waypoints[j]
                if (isDirectPath(start, candidate)) {
                    val pattern = waypoints.subList(i, j + 1)
                    if (hasUnnecessaryDetour(pattern)) {
                        result.add(candidate)
                        skipTo = j + 1
                        foundPattern = true
                        break
                    }
                }
            }
            
            if (!foundPattern) {
                result.add(waypoints[i])
            }
            i = skipTo
        }
        
        while (i < waypoints.size) {
            result.add(waypoints[i])
            i++
        }
        
        return validateOrthogonal(result)
    }
    
    private fun hasUnnecessaryDetour(pattern: List<GridPoint>): Boolean {
        if (pattern.size < 4) return false
        
        val startDir = GridDirection.fromPoints(pattern[0], pattern[1])
        val endDir = GridDirection.fromPoints(pattern[pattern.size - 2], pattern[pattern.size - 1])
        
        if (startDir != endDir) return false
        
        var directionChanges = 0
        for (i in 1 until pattern.size - 1) {
            val dir1 = GridDirection.fromPoints(pattern[i - 1], pattern[i])
            val dir2 = GridDirection.fromPoints(pattern[i], pattern[i + 1])
            if (dir1 != dir2) {
                directionChanges++
            }
        }
        
        return directionChanges >= 2
    }
    
    private fun isDirectPath(from: GridPoint, to: GridPoint): Boolean {
        val dx = to.x - from.x
        val dy = to.y - from.y
        return dx == 0 || dy == 0
    }
    
    private fun enforceSpacingPreservingCenter(
        waypoints: List<GridPoint>,
        connectionId: String,
        existingPaths: Map<String, List<GridPoint>>,
        corridor: DevicePairCorridor,
        index: Int,
        grid: RoutingGrid
    ): List<GridPoint> {
        if (waypoints.size < 2) return waypoints
        
        val minSpacingCells = (config.minPathSpacing / grid.cellSize).toInt().coerceAtLeast(1)
        var adjustedPath = waypoints
        var iterations = 0
        val maxIterations = 30
        
        while (iterations < maxIterations) {
            val newPath = adjustedPath.toMutableList()
            var changed = false
            
            for (i in 0 until newPath.size - 1) {
                val current = newPath[i]
                val next = newPath[i + 1]
                
                val dx = next.x - current.x
                val dy = next.y - current.y
                
                if (dx == 0 && dy == 0) continue
                if (dx != 0 && dy != 0) {
                    val corner = if (abs(dx) > abs(dy)) {
                        GridPoint(next.x, current.y)
                    } else {
                        GridPoint(current.x, next.y)
                    }
                    newPath[i + 1] = corner
                    continue
                }
                
                val direction = GridDirection.fromPoints(current, next)
                val segmentPoints = getSegmentPoints(current, next)
                
                for ((otherId, otherPath) in existingPaths) {
                    if (otherId == connectionId || otherPath.isEmpty()) continue
                    
                    for (j in 0 until otherPath.size - 1) {
                        val otherCurrent = otherPath[j]
                        val otherNext = otherPath[j + 1]
                        val otherDirection = GridDirection.fromPoints(otherCurrent, otherNext)
                        
                        if (direction != otherDirection) continue
                        
                        val otherSegmentPoints = getSegmentPoints(otherCurrent, otherNext)
                        
                        val hasOverlap = segmentPoints.any { point ->
                            otherSegmentPoints.contains(point)
                        }
                        
                        if (hasOverlap && index != 0) {
                            val offset = calculateSpacingOffsetForOverlap(
                                current, next, otherCurrent, otherNext, direction, minSpacingCells, corridor, index
                            )
                            
                            if (offset != null) {
                                val adjustedCurrent = GridPoint(
                                    (current.x + offset.x).coerceIn(0, grid.width - 1),
                                    (current.y + offset.y).coerceIn(0, grid.height - 1)
                                )
                                val adjustedNext = GridPoint(
                                    (next.x + offset.x).coerceIn(0, grid.width - 1),
                                    (next.y + offset.y).coerceIn(0, grid.height - 1)
                                )
                                
                                if (adjustedCurrent != current || adjustedNext != next) {
                                    newPath[i] = adjustedCurrent
                                    newPath[i + 1] = adjustedNext
                                    changed = true
                                }
                            }
                        } else {
                            val minDistance = calculateMinDistanceBetweenSegments(
                                segmentPoints, otherSegmentPoints, direction
                            )
                            
                            if (minDistance < minSpacingCells) {
                                val offset = calculateSpacingOffset(
                                    current, next, otherCurrent, otherNext, direction, minSpacingCells
                                )
                                
                                if (offset != null) {
                                    val adjustedCurrent = GridPoint(
                                        (current.x + offset.x).coerceIn(0, grid.width - 1),
                                        (current.y + offset.y).coerceIn(0, grid.height - 1)
                                    )
                                    val adjustedNext = GridPoint(
                                        (next.x + offset.x).coerceIn(0, grid.width - 1),
                                        (next.y + offset.y).coerceIn(0, grid.height - 1)
                                    )
                                    
                                    if (adjustedCurrent != current || adjustedNext != next) {
                                        newPath[i] = adjustedCurrent
                                        newPath[i + 1] = adjustedNext
                                        changed = true
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            if (!changed) break
            
            adjustedPath = ensureOrthogonal(newPath)
            adjustedPath = validateOrthogonal(adjustedPath)
            iterations++
        }
        
        return validateOrthogonal(adjustedPath)
    }
    
    private fun calculateMinDistanceBetweenSegments(
        segment1: List<GridPoint>,
        segment2: List<GridPoint>,
        direction: GridDirection
    ): Int {
        return segment1.minOfOrNull { p1 ->
            segment2.minOfOrNull { p2 ->
                when {
                    direction.isHorizontal() -> abs(p1.y - p2.y)
                    else -> abs(p1.x - p2.x)
                }
            } ?: Int.MAX_VALUE
        } ?: Int.MAX_VALUE
    }
    
    private fun calculateSpacingOffsetForOverlap(
        p1: GridPoint, p2: GridPoint,
        q1: GridPoint, q2: GridPoint,
        direction: GridDirection,
        minSpacing: Int,
        corridor: DevicePairCorridor,
        index: Int
    ): GridPoint? {
        if (index == 0) return null
        
        return when {
            direction.isHorizontal() -> {
                val pY = p1.y
                val qY = q1.y
                val centerY = corridor.centerPoint.y
                
                val offsetY = if (pY < centerY) {
                    -(minSpacing + 1)
                } else {
                    (minSpacing + 1)
                }
                GridPoint(0, offsetY)
            }
            else -> {
                val pX = p1.x
                val qX = q1.x
                val centerX = corridor.centerPoint.x
                
                val offsetX = if (pX < centerX) {
                    -(minSpacing + 1)
                } else {
                    (minSpacing + 1)
                }
                GridPoint(offsetX, 0)
            }
        }
    }
    
    private fun preventSegmentOverlap(
        waypoints: List<GridPoint>,
        connectionId: String,
        existingPaths: Map<String, List<GridPoint>>,
        grid: RoutingGrid
    ): List<GridPoint> {
        if (waypoints.size < 2) return waypoints
        
        val result = mutableListOf<GridPoint>()
        result.add(waypoints.first())
        
        for (i in 1 until waypoints.size) {
            val prev = result.last()
            val current = waypoints[i]
            
            val segmentPoints = getSegmentPoints(prev, current)
            var hasOverlap = false
            
            for ((otherId, otherPath) in existingPaths) {
                if (otherId == connectionId || otherPath.isEmpty()) continue
                
                for (j in 0 until otherPath.size - 1) {
                    val otherSegmentPoints = getSegmentPoints(otherPath[j], otherPath[j + 1])
                    val direction = GridDirection.fromPoints(prev, current)
                    val otherDirection = GridDirection.fromPoints(otherPath[j], otherPath[j + 1])
                    
                    if (direction == otherDirection) {
                        val overlap = segmentPoints.any { point ->
                            otherSegmentPoints.contains(point)
                        }
                        
                        if (overlap) {
                            hasOverlap = true
                            break
                        }
                    }
                }
                
                if (hasOverlap) break
            }
            
            if (!hasOverlap) {
                result.add(current)
            } else {
                val adjusted = adjustSegmentToAvoidOverlap(prev, current, connectionId, existingPaths, grid)
                if (adjusted != null) {
                    result.add(adjusted)
                } else {
                    result.add(current)
                }
            }
        }
        
        val validated = validateOrthogonal(result)
        return validated
    }
    
    private fun adjustSegmentToAvoidOverlap(
        from: GridPoint,
        to: GridPoint,
        connectionId: String,
        existingPaths: Map<String, List<GridPoint>>,
        grid: RoutingGrid
    ): GridPoint? {
        val direction = GridDirection.fromPoints(from, to)
        val minSpacingCells = (config.minPathSpacing / grid.cellSize).toInt().coerceAtLeast(1)
        
        val perpOffset = when {
            direction.isHorizontal() -> GridPoint(0, minSpacingCells)
            else -> GridPoint(minSpacingCells, 0)
        }
        
        val candidate1 = GridPoint(
            (to.x + perpOffset.x).coerceIn(0, grid.width - 1),
            (to.y + perpOffset.y).coerceIn(0, grid.height - 1)
        )
        val candidate2 = GridPoint(
            (to.x - perpOffset.x).coerceIn(0, grid.width - 1),
            (to.y - perpOffset.y).coerceIn(0, grid.height - 1)
        )
        
        for (candidate in listOf(candidate1, candidate2)) {
            if (!grid.isBlocked(candidate)) {
                val testSegment = getSegmentPoints(from, candidate)
                var overlaps = false
                
                for ((otherId, otherPath) in existingPaths) {
                    if (otherId == connectionId) continue
                    for (j in 0 until otherPath.size - 1) {
                        val otherSegment = getSegmentPoints(otherPath[j], otherPath[j + 1])
                        val otherDir = GridDirection.fromPoints(otherPath[j], otherPath[j + 1])
                        
                        if (direction == otherDir && testSegment.any { otherSegment.contains(it) }) {
                            overlaps = true
                            break
                        }
                    }
                    if (overlaps) break
                }
                
                if (!overlaps) {
                    return candidate
                }
            }
        }
        
        return null
    }
    
    private fun getSegmentPoints(from: GridPoint, to: GridPoint): List<GridPoint> {
        val points = mutableListOf<GridPoint>()
        val dx = to.x - from.x
        val dy = to.y - from.y
        
        when {
            dx == 0 && dy == 0 -> {
                points.add(from)
            }
            dx == 0 -> {
                val step = if (dy > 0) 1 else -1
                var y = from.y
                while (y != to.y) {
                    points.add(GridPoint(from.x, y))
                    y += step
                }
                points.add(to)
            }
            dy == 0 -> {
                val step = if (dx > 0) 1 else -1
                var x = from.x
                while (x != to.x) {
                    points.add(GridPoint(x, from.y))
                    x += step
                }
                points.add(to)
            }
            else -> {
                points.add(from)
                points.add(to)
            }
        }
        
        return points
    }
    
    private fun calculateParallelDistance(
        p1: GridPoint, p2: GridPoint,
        q1: GridPoint, q2: GridPoint,
        direction: GridDirection
    ): Int {
        return when {
            direction.isHorizontal() -> {
                if (p1.y == q1.y) {
                    val pMinX = min(p1.x, p2.x)
                    val pMaxX = max(p1.x, p2.x)
                    val qMinX = min(q1.x, q2.x)
                    val qMaxX = max(q1.x, q2.x)
                    
                    if (pMaxX < qMinX) qMinX - pMaxX
                    else if (qMaxX < pMinX) pMinX - qMaxX
                    else 0
                } else {
                    abs(p1.y - q1.y)
                }
            }
            else -> {
                if (p1.x == q1.x) {
                    val pMinY = min(p1.y, p2.y)
                    val pMaxY = max(p1.y, p2.y)
                    val qMinY = min(q1.y, q2.y)
                    val qMaxY = max(q1.y, q2.y)
                    
                    if (pMaxY < qMinY) qMinY - pMaxY
                    else if (qMaxY < pMinY) pMinY - qMaxY
                    else 0
                } else {
                    abs(p1.x - q1.x)
                }
            }
        }
    }
    
    private fun calculateSpacingOffset(
        p1: GridPoint, p2: GridPoint,
        q1: GridPoint, q2: GridPoint,
        direction: GridDirection,
        minSpacing: Int
    ): GridPoint? {
        return when {
            direction.isHorizontal() -> {
                val pY = p1.y
                val qY = q1.y
                val distance = abs(pY - qY)
                
                if (distance < minSpacing) {
                    val offsetY = if (pY < qY) {
                        -(minSpacing - distance)
                    } else {
                        (minSpacing - distance)
                    }
                    GridPoint(0, offsetY)
                } else null
            }
            else -> {
                val pX = p1.x
                val qX = q1.x
                val distance = abs(pX - qX)
                
                if (distance < minSpacing) {
                    val offsetX = if (pX < qX) {
                        -(minSpacing - distance)
                    } else {
                        (minSpacing - distance)
                    }
                    GridPoint(offsetX, 0)
                } else null
            }
        }
    }
    
    private fun countCrossings(
        path: List<GridPoint>,
        otherPaths: List<List<GridPoint>>
    ): Int {
        if (path.size < 2) return 0
        
        var crossings = 0
        path.zipWithNext().forEach { (from, to) ->
            otherPaths.forEach { otherPath ->
                if (otherPath.size >= 2) {
                    otherPath.zipWithNext().forEach { (otherFrom, otherTo) ->
                        if (segmentsCross(from, to, otherFrom, otherTo)) {
                            crossings++
                        }
                    }
                }
            }
        }
        return crossings
    }
    
    private fun segmentsCross(
        a1: GridPoint, a2: GridPoint,
        b1: GridPoint, b2: GridPoint
    ): Boolean {
        if (a1 == a2 || b1 == b2) return false
        
        val aIsHorizontal = a1.y == a2.y
        val aIsVertical = a1.x == a2.x
        val bIsHorizontal = b1.y == b2.y
        val bIsVertical = b1.x == b2.x
        
        return when {
            aIsHorizontal && bIsVertical -> {
                val aXRange = min(a1.x, a2.x)..max(a1.x, a2.x)
                val bYRange = min(b1.y, b2.y)..max(b1.y, b2.y)
                b1.x in aXRange && a1.y in bYRange
            }
            aIsVertical && bIsHorizontal -> {
                val aYRange = min(a1.y, a2.y)..max(a1.y, a2.y)
                val bXRange = min(b1.x, b2.x)..max(b1.x, b2.x)
                a1.x in bXRange && b1.y in aYRange
            }
            else -> false
        }
    }
    
    private fun createFailedConnection(
        connection: Connection,
        sourcePort: PortPosition?,
        targetPort: PortPosition?
    ): RoutedConnection {
        val virtualWaypoints = buildList {
            if (sourcePort != null) add(sourcePort.virtualPos)
            if (targetPort != null) add(targetPort.virtualPos)
        }
        
        return RoutedConnection(
            connectionId = connection.id,
            waypoints = emptyList(),
            virtualWaypoints = virtualWaypoints,
            success = false,
            crossings = 0
        )
    }
    
    private fun calculatePortPosition(device: Device, port: Port): Pair<Float, Float> {
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
    
    private fun adjustPortGridPosition(
        portGrid: GridPoint,
        device: Device,
        port: Port,
        grid: RoutingGrid
    ): GridPoint {
        val deviceGridLeft = (device.position.x / grid.cellSize).toInt()
        val deviceGridRight = ((device.position.x + device.size.width) / grid.cellSize).toInt()
        val deviceGridTop = (device.position.y / grid.cellSize).toInt()
        val deviceGridBottom = ((device.position.y + device.size.height) / grid.cellSize).toInt()
        
        return when (port.position.side) {
            DeviceSide.TOP -> {
                if (portGrid.y >= deviceGridTop) {
                    GridPoint(portGrid.x, deviceGridTop - 1)
                } else portGrid
            }
            DeviceSide.LEFT -> {
                if (portGrid.x >= deviceGridLeft) {
                    GridPoint(deviceGridLeft - 1, portGrid.y)
                } else portGrid
            }
            DeviceSide.BOTTOM -> {
                if (portGrid.y <= deviceGridBottom) {
                    GridPoint(portGrid.x, deviceGridBottom + 1)
                } else portGrid
            }
            DeviceSide.RIGHT -> {
                if (portGrid.x <= deviceGridRight) {
                    GridPoint(deviceGridRight + 1, portGrid.y)
                } else portGrid
            }
        }
    }
    
    private fun calculatePortExtension(
        portPosition: PortPosition,
        grid: RoutingGrid
    ): PortExtension {
        val extensionLength = config.portExtension
        val portGrid = portPosition.gridPos
        
        val extensionDirection = when (portPosition.side) {
            DeviceSide.LEFT -> GridDirection.WEST
            DeviceSide.RIGHT -> GridDirection.EAST
            DeviceSide.TOP -> GridDirection.NORTH
            DeviceSide.BOTTOM -> GridDirection.SOUTH
        }
        
        val extensionCells = (extensionLength / grid.cellSize).toInt().coerceAtLeast(1)
        
        var endGrid = when (extensionDirection) {
            GridDirection.NORTH -> GridPoint(portGrid.x, portGrid.y - extensionCells)
            GridDirection.SOUTH -> GridPoint(portGrid.x, portGrid.y + extensionCells)
            GridDirection.EAST -> GridPoint(portGrid.x + extensionCells, portGrid.y)
            GridDirection.WEST -> GridPoint(portGrid.x - extensionCells, portGrid.y)
        }
        
        endGrid = GridPoint(
            endGrid.x.coerceIn(0, grid.width - 1),
            endGrid.y.coerceIn(0, grid.height - 1)
        )
        
        val validatedEndGrid = validateExtensionPath(portGrid, endGrid, extensionDirection, grid)
        
        val actualLength = when (extensionDirection) {
            GridDirection.NORTH, GridDirection.SOUTH -> {
                abs(validatedEndGrid.y - portGrid.y) * grid.cellSize
            }
            GridDirection.EAST, GridDirection.WEST -> {
                abs(validatedEndGrid.x - portGrid.x) * grid.cellSize
            }
        }
        
        return PortExtension(
            portId = portPosition.portId,
            startPos = portGrid,
            endPos = validatedEndGrid,
            direction = extensionDirection,
            length = actualLength
        )
    }
    
    private fun validateExtensionPath(
        start: GridPoint,
        targetEnd: GridPoint,
        direction: GridDirection,
        grid: RoutingGrid
    ): GridPoint {
        var current = start
        var lastValid = start
        
        when (direction) {
            GridDirection.NORTH -> {
                while (current.y > targetEnd.y) {
                    val next = GridPoint(current.x, current.y - 1)
                    if (next.y < 0 || grid.isBlocked(next)) break
                    lastValid = next
                    current = next
                }
            }
            GridDirection.SOUTH -> {
                while (current.y < targetEnd.y) {
                    val next = GridPoint(current.x, current.y + 1)
                    if (next.y >= grid.height || grid.isBlocked(next)) break
                    lastValid = next
                    current = next
                }
            }
            GridDirection.EAST -> {
                while (current.x < targetEnd.x) {
                    val next = GridPoint(current.x + 1, current.y)
                    if (next.x >= grid.width || grid.isBlocked(next)) break
                    lastValid = next
                    current = next
                }
            }
            GridDirection.WEST -> {
                while (current.x > targetEnd.x) {
                    val next = GridPoint(current.x - 1, current.y)
                    if (next.x < 0 || grid.isBlocked(next)) break
                    lastValid = next
                    current = next
                }
            }
        }
        
        return lastValid
    }
}

