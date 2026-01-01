package dev.akexorcist.workstation.routing

import kotlin.math.min
import kotlin.math.max

data class PathResult(
    val waypoints: List<GridPoint>,
    val success: Boolean,
    val totalCost: Float,
    val crossings: Int
)

class AStarPathfinder(
    private val grid: RoutingGrid,
    private val densityTracker: PathDensityTracker? = null,
    private val config: RoutingConfig = RoutingConfig
) {
    private data class Node(
        val point: GridPoint,
        val gCost: Float,
        val hCost: Float,
        val parent: Node?,
        val direction: GridDirection?
    ) : Comparable<Node> {
        val fCost: Float get() = gCost + hCost
        
        override fun compareTo(other: Node): Int = fCost.compareTo(other.fCost)
    }
    
    fun findPath(
        start: GridPoint,
        end: GridPoint,
        connectionId: String,
        existingPaths: Map<String, List<GridPoint>>,
        waypointHints: List<GridPoint> = emptyList()
    ): PathResult {
        if (start == end) return PathResult(listOf(start), true, 0f, 0)
        
        // If waypoint hints provided, try routing through them first
        if (waypointHints.isNotEmpty()) {
            val fullPath = mutableListOf<GridPoint>()
            var currentStart = start
            var totalCost = 0f
            var success = true
            var totalCrossings = 0
            
            for (waypoint in waypointHints + listOf(end)) {
                val partialResult = findPathSegment(currentStart, waypoint, connectionId, existingPaths)
                if (!partialResult.success) {
                    success = false
                    break
                }
                
                fullPath.addAll(if (fullPath.isEmpty()) partialResult.waypoints else partialResult.waypoints.drop(1))
                totalCost += partialResult.totalCost
                totalCrossings += partialResult.crossings
                currentStart = waypoint
            }
            
            if (success) {
                return PathResult(fullPath, true, totalCost, totalCrossings)
            }
            // If failed with hints, fall through to direct routing
        }
        
        // Direct routing without hints
        return findPathSegment(start, end, connectionId, existingPaths)
    }
    
    private fun findPathSegment(
        start: GridPoint, 
        end: GridPoint, 
        connectionId: String,
        existingPaths: Map<String, List<GridPoint>>
    ): PathResult {
        // Use a sorted list as a priority queue for Kotlin Multiplatform compatibility
        val openSet = mutableListOf<Node>()
        val closedSet = mutableSetOf<GridPoint>()
        val gScores = mutableMapOf<GridPoint, Float>()
        val directionsFrom = mutableMapOf<GridPoint, GridDirection>()
        
        openSet.add(Node(start, 0f, heuristic(start, end), null, null))
        gScores[start] = 0f
        
        repeat(config.maxPathfindingIterations) {
            if (openSet.isEmpty()) return PathResult(emptyList(), false, Float.MAX_VALUE, 0)
            
            // Find node with lowest fCost
            val current = openSet.minByOrNull { it.fCost }!!
            openSet.remove(current)
            
            if (current.point == end) {
                return reconstructPath(current, existingPaths)
            }
            
            if (current.point in closedSet) return@repeat
            closedSet.add(current.point)
            
            grid.getNeighbors(current.point).forEach { (neighbor, direction) ->
                if (neighbor in closedSet || grid.isBlocked(neighbor)) return@forEach
                
                // Check if we can occupy this cell in the direction we want
                if (!grid.canOccupy(neighbor, connectionId, direction)) return@forEach
                
                // Calculate various cost factors
                val moveCost = config.gridMoveCost
                val crossingCost = calculateCrossingCost(current.point, neighbor, direction, existingPaths)
                val turnCost = if (current.direction != null && current.direction != direction) config.turnPenalty else 0f
                val repulsionCost = calculateRepulsionCost(neighbor, existingPaths)
                val densityCost = densityTracker?.getDensityCost(neighbor) ?: 0f
                val distributionCost = calculateDistributionCost(neighbor, start, end)
                
                val tentativeG = current.gCost + moveCost + crossingCost + turnCost + 
                                 repulsionCost + densityCost + distributionCost
                
                if (tentativeG < (gScores[neighbor] ?: Float.MAX_VALUE)) {
                    directionsFrom[neighbor] = direction
                    gScores[neighbor] = tentativeG
                    
                    // Check if node is already in open set
                    val existingNode = openSet.find { it.point == neighbor }
                    if (existingNode != null) {
                        openSet.remove(existingNode)
                    }
                    
                    openSet.add(Node(neighbor, tentativeG, heuristic(neighbor, end), current, direction))
                }
            }
        }
        
        // Failed to find path
        return PathResult(emptyList(), false, Float.MAX_VALUE, 0)
    }
    
    private fun heuristic(from: GridPoint, to: GridPoint): Float =
        from.manhattanDistanceTo(to).toFloat() * config.gridMoveCost
    
    private fun calculateCrossingCost(
        from: GridPoint,
        to: GridPoint,
        direction: GridDirection,
        existingPaths: Map<String, List<GridPoint>>
    ): Float {
        var cost = 0f
        val penalty = config.crossingPenalty
        
        existingPaths.values.forEach { path ->
            path.zipWithNext().forEach { (p1, p2) ->
                if (segmentsIntersect(from, to, p1, p2)) {
                    val existingDirection = GridDirection.fromPoints(p1, p2)
                    
                    // Higher penalty for parallel paths
                    if (!direction.isPerpendicular(existingDirection)) {
                        cost += penalty * 2.0f
                    } else {
                        cost += penalty
                    }
                    
                    // Extremely high penalty for same direction conflicts
                    if (direction == existingDirection) {
                        cost += penalty * 10.0f
                    }
                }
            }
        }
        
        return cost
    }
    
    private fun calculateRepulsionCost(
        point: GridPoint,
        existingPaths: Map<String, List<GridPoint>>
    ): Float {
        var totalCost = 0f
        val minDistance = config.minPathSpacing / config.gridCellSize
        
        existingPaths.values.forEach { path ->
            path.forEach { existingPoint ->
                val distance = point.euclideanDistanceTo(existingPoint)
                
                if (distance < minDistance) {
                    val isHorizontallyAligned = existingPoint.y == point.y
                    val isVerticallyAligned = existingPoint.x == point.x
                    val isAligned = isHorizontallyAligned || isVerticallyAligned
                    
                    // Stronger repulsion for aligned paths to prevent parallel routing
                    val factor = if (isAligned) config.pathRepulsionFactor * 2.5f else config.pathRepulsionFactor
                    
                    val repulsionStrength = (minDistance - distance) * factor
                    totalCost += repulsionStrength
                }
            }
        }
        
        return totalCost
    }
    
    private fun calculateDistributionCost(currentPoint: GridPoint, start: GridPoint, end: GridPoint): Float {
        // Calculate cost based on cell density from density tracker
        val corridorInfo = grid.findDeviceCorridor(start, end) ?: return 0f
        
        // We have identified a corridor - calculate how far this point is from center
        val (corridorTop, corridorBottom) = corridorInfo
        
        // Check if the point is actually between the corridor points
        val inCorridor = currentPoint.y in min(corridorTop.y, corridorBottom.y)..max(corridorTop.y, corridorBottom.y)
        if (!inCorridor) return 0f
        
        // Calculate horizontal distance from center of corridor
        val idealX = corridorTop.x
        val distanceFromIdeal = kotlin.math.abs(currentPoint.x - idealX)
        
        // Calculate normalized cost (higher distance = higher cost)
        return distanceFromIdeal * config.distributionFactor
    }
    
    private fun segmentsIntersect(a1: GridPoint, a2: GridPoint, b1: GridPoint, b2: GridPoint): Boolean {
        // Handle special case where segments are single points
        if (a1 == a2 || b1 == b2) return false
        
        val aIsHorizontal = a1.y == a2.y
        val aIsVertical = a1.x == a2.x
        val bIsHorizontal = b1.y == b2.y
        val bIsVertical = b1.x == b2.x
        
        return when {
            // Horizontal segment intersects vertical segment
            aIsHorizontal && bIsVertical -> {
                val aXRange = min(a1.x, a2.x)..max(a1.x, a2.x)
                val bYRange = min(b1.y, b2.y)..max(b1.y, b2.y)
                b1.x in aXRange && a1.y in bYRange
            }
            // Vertical segment intersects horizontal segment
            aIsVertical && bIsHorizontal -> {
                val aYRange = min(a1.y, a2.y)..max(a1.y, a2.y)
                val bXRange = min(b1.x, b2.x)..max(b1.x, b2.x)
                a1.x in bXRange && b1.y in aYRange
            }
            // Parallel segments don't intersect (at least not in grid-based routing)
            else -> false
        }
    }
    
    private fun reconstructPath(endNode: Node, existingPaths: Map<String, List<GridPoint>>): PathResult {
        // Reconstruct the raw path from A*
        val rawPath = generateSequence(endNode) { it.parent }
            .map { it.point }
            .toList()
            .reversed()
        
        // Process the path based on configuration
        val processedPath = processPath(rawPath)
        
        // Count crossings with existing paths
        val crossings = countCrossings(processedPath, existingPaths)
        
        // Update density tracker with this path
        densityTracker?.recordPath(processedPath)
        
        return PathResult(processedPath, true, endNode.gCost, crossings)
    }
    
    private fun processPath(path: List<GridPoint>): List<GridPoint> {
        // First ensure path has no duplicates
        val deduplicatedPath = path.fold(mutableListOf<GridPoint>()) { acc, point ->
            if (acc.isEmpty() || acc.last() != point) {
                acc.add(point)
            }
            acc
        }
        
        if (deduplicatedPath.size <= 2) return deduplicatedPath
        
        // Then simplify if configured
        val simplifiedPath = if (config.simplifyPath) {
            simplifyPath(deduplicatedPath)
        } else {
            deduplicatedPath
        }
        
        // Then remove zigzags if configured
        return if (config.removeZigzags) {
            removeZigzags(simplifiedPath)
        } else {
            simplifiedPath
        }
    }
    
    private fun simplifyPath(path: List<GridPoint>): List<GridPoint> {
        if (path.size <= 2) return path
        
        val simplified = mutableListOf<GridPoint>()
        simplified.add(path.first())
        
        var i = 0
        while (i < path.size - 1) {
            // Find longest collinear sequence
            var j = i + 1
            while (j < path.size - 1 && isCollinear(path[i], path[j], path[j + 1])) {
                j++
            }
            
            // Add the last point of the collinear sequence
            if (j >= path.size) j = path.size - 1
            simplified.add(path[j])
            i = j
        }
        
        // Make sure we have the last point
        if (simplified.last() != path.last()) {
            simplified.add(path.last())
        }
        
        return simplified
    }
    
    private fun isCollinear(p1: GridPoint, p2: GridPoint, p3: GridPoint): Boolean =
        (p1.x == p2.x && p2.x == p3.x) || (p1.y == p2.y && p2.y == p3.y)
    
    private fun removeZigzags(path: List<GridPoint>): List<GridPoint> {
        if (path.size < 4) return path
        
        // First ensure the path is fully orthogonal
        val orthogonal = mutableListOf<GridPoint>()
        orthogonal.add(path.first())
        
        for (i in 1 until path.size) {
            val prev = orthogonal.last()
            val current = path[i]
            
            // Add corner point if needed for diagonal movement
            if (prev.x != current.x && prev.y != current.y) {
                orthogonal.add(GridPoint(current.x, prev.y))
            }
            
            orthogonal.add(current)
        }
        
        // Then eliminate unnecessary zigzags
        val withoutZigzags = mutableListOf<GridPoint>()
        withoutZigzags.add(orthogonal.first())
        
        var i = 1
        while (i < orthogonal.size - 2) {
            val a = withoutZigzags.last()
            val b = orthogonal[i]
            val c = orthogonal[i + 1]
            val d = orthogonal[i + 2]
            
            // Check for "stairstep" patterns that can be simplified
            if (isZigzag(a, b, c, d)) {
                // Skip the middle points and add a direct corner
                withoutZigzags.add(getZigzagCorner(a, d))
                i += 3 // Skip all intermediate points
            } else {
                withoutZigzags.add(b)
                i++
            }
        }
        
        // Add remaining points
        while (i < orthogonal.size) {
            withoutZigzags.add(orthogonal[i])
            i++
        }
        
        return withoutZigzags
    }
    
    private fun isZigzag(a: GridPoint, b: GridPoint, c: GridPoint, d: GridPoint): Boolean {
        val isHorizontalFirst = a.y == b.y
        val isVerticalFirst = a.x == b.x
        val isHorizontalSecond = b.x == c.x
        val isVerticalSecond = b.y == c.y
        val isHorizontalThird = c.y == d.y
        val isVerticalThird = c.x == d.x
        
        return (isHorizontalFirst && isVerticalSecond && isHorizontalThird) ||
               (isVerticalFirst && isHorizontalSecond && isVerticalThird)
    }
    
    private fun getZigzagCorner(start: GridPoint, end: GridPoint): GridPoint =
        if (start.y == end.y) {
            GridPoint(end.x, start.y)
        } else {
            GridPoint(start.x, end.y)
        }
    
    private fun countCrossings(path: List<GridPoint>, existingPaths: Map<String, List<GridPoint>>): Int =
        path.zipWithNext().sumOf { (from, to) ->
            existingPaths.values.sumOf { waypoints ->
                waypoints.zipWithNext().count { (p1, p2) ->
                    segmentsIntersect(from, to, p1, p2)
                }
            }
        }
}
