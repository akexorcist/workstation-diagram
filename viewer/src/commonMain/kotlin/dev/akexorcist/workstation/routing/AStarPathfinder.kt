package dev.akexorcist.workstation.routing

data class PathResult(
    val waypoints: List<GridPoint>,
    val success: Boolean,
    val totalCost: Float,
    val crossings: Int
)

class AStarPathfinder(
    private val grid: RoutingGrid,
    private val config: RoutingConfig = RoutingConfig
) {
    private data class Node(
        val point: GridPoint,
        val gCost: Float,
        val hCost: Float,
        val parent: Node?,
        val direction: GridDirection?
    ) {
        val fCost: Float get() = gCost + hCost
    }

    fun findPath(
        start: GridPoint,
        end: GridPoint,
        connectionId: String,
        existingPaths: Map<String, List<GridPoint>>
    ): PathResult {
        if (start == end) return PathResult(listOf(start), true, 0f, 0)

        val openSet = mutableListOf<Node>()
        val closedSet = mutableSetOf<GridPoint>()
        val gScores = mutableMapOf<GridPoint, Float>()

        openSet.add(Node(start, 0f, heuristic(start, end), null, null))
        gScores[start] = 0f

        repeat(config.maxPathfindingIterations) {
            val current = openSet.minByOrNull { it.fCost } ?: return PathResult(emptyList(), false, Float.MAX_VALUE, 0)
            openSet.remove(current)

            if (current.point == end) return reconstructPath(current, existingPaths)

            closedSet.add(current.point)

            grid.getNeighbors(current.point).forEach { (neighbor, direction) ->
                if (grid.isBlocked(neighbor) || neighbor in closedSet) return@forEach

                if (!grid.canOccupy(neighbor, connectionId, direction)) return@forEach

                val moveCost = config.gridMoveCost
                val crossingCost = calculateCrossingCost(current.point, neighbor, direction, existingPaths)
                val turnCost = if (current.direction != null && current.direction != direction) config.turnPenalty else 0f
                val repulsionCost = calculatePathRepulsionCost(neighbor, existingPaths)

                val tentativeG = current.gCost + moveCost + crossingCost + turnCost + repulsionCost

                if (tentativeG < (gScores[neighbor] ?: Float.MAX_VALUE)) {
                    gScores[neighbor] = tentativeG
                    openSet.add(Node(neighbor, tentativeG, heuristic(neighbor, end), current, direction))
                }
            }
        }

        return PathResult(emptyList(), false, Float.MAX_VALUE, 0)
    }

    private fun heuristic(from: GridPoint, to: GridPoint): Float =
        from.manhattanDistanceTo(to).toFloat() * config.gridMoveCost

    private fun calculateCrossingCost(
        from: GridPoint,
        to: GridPoint,
        direction: GridDirection,
        existingPaths: Map<String, List<GridPoint>>
    ): Float = existingPaths.values.sumOf { waypoints ->
        waypoints.zipWithNext().count { (p1, p2) -> segmentsIntersect(from, to, p1, p2) }
    }.toFloat() * config.crossingPenalty
    
    private fun calculatePathRepulsionCost(
        point: GridPoint,
        existingPaths: Map<String, List<GridPoint>>
    ): Float {
        var totalCost = 0f
        val minDistance = config.minPathSpacing / config.gridCellSize
        val parallelMinDistance = minDistance * 1.5f // Higher min distance for parallel paths
        
        existingPaths.values.forEach { path ->
            path.forEach { existingPoint ->
                val distance = point.manhattanDistanceTo(existingPoint)
                
                // Check if points are aligned (potential parallel paths)
                val isHorizontallyAligned = existingPoint.y == point.y
                val isVerticallyAligned = existingPoint.x == point.x
                val isAligned = isHorizontallyAligned || isVerticallyAligned
                
                // Use different thresholds for parallel vs non-parallel paths
                val effectiveMinDistance = if (isAligned) parallelMinDistance else minDistance
                
                if (distance < effectiveMinDistance) {
                    // Base repulsion factor
                    var factor = config.pathRepulsionFactor
                    
                    // Stronger repulsion for very close paths
                    if (distance <= effectiveMinDistance / 3) {
                        factor *= 2
                    }
                    
                    // Much stronger repulsion for parallel paths
                    if (isAligned) {
                        factor *= 3
                        
                        // Even stronger for vertical alignment (based on the screenshot)
                        if (isVerticallyAligned) {
                            factor *= 1.5f
                        }
                    }
                    
                    val repulsionStrength = (effectiveMinDistance - distance) * factor
                    totalCost += repulsionStrength
                }
            }
        }
        
        return totalCost
    }

    private fun segmentsIntersect(a1: GridPoint, a2: GridPoint, b1: GridPoint, b2: GridPoint): Boolean {
        val aIsHorizontal = a1.y == a2.y
        val aIsVertical = a1.x == a2.x
        val bIsHorizontal = b1.y == b2.y
        val bIsVertical = b1.x == b2.x

        return when {
            aIsHorizontal && bIsVertical -> {
                val aXRange = minOf(a1.x, a2.x)..maxOf(a1.x, a2.x)
                val bYRange = minOf(b1.y, b2.y)..maxOf(b1.y, b2.y)
                b1.x in aXRange && a1.y in bYRange
            }
            aIsVertical && bIsHorizontal -> {
                val aYRange = minOf(a1.y, a2.y)..maxOf(a1.y, a2.y)
                val bXRange = minOf(b1.x, b2.x)..maxOf(b1.x, b2.x)
                a1.x in bXRange && b1.y in aYRange
            }
            else -> false
        }
    }

    private fun reconstructPath(endNode: Node, existingPaths: Map<String, List<GridPoint>>): PathResult {
        val rawPath = generateSequence(endNode) { it.parent }
            .map { it.point }
            .toList()
            .reversed()

        // First simplify the path
        val simplifiedPath = if (config.simplifyPath) simplifyPath(rawPath) else rawPath
        
        // Then apply zigzag removal
        val finalPath = removeZigzags(simplifiedPath)

        val crossings = finalPath.zipWithNext().sumOf { (from, to) ->
            existingPaths.values.sumOf { waypoints ->
                waypoints.zipWithNext().count { (p1, p2) -> segmentsIntersect(from, to, p1, p2) }
            }
        }

        return PathResult(finalPath, true, endNode.gCost, crossings)
    }

    private fun simplifyPath(path: List<GridPoint>): List<GridPoint> {
        if (path.size <= 2) return path

        val simplified = mutableListOf<GridPoint>()
        simplified.add(path.first())
        
        var i = 0
        while (i < path.size - 1) {
            var j = i + 1
            while (j < path.size && isCollinear(path[i], path[j], path.getOrNull(j + 1))) {
                j++
            }
            
            if (j < path.size - 1) {
                val current = path[j]
                val next = path[j + 1]
                
                if (isUnnecessaryZigZag(path.getOrNull(i), current, next, path.getOrNull(minOf(j + 2, path.size - 1)))) {
                    j++
                }
            }
            
            simplified.add(path[j])
            i = j
        }
        
        return simplified
    }

    private fun isCollinear(p1: GridPoint, p2: GridPoint, p3: GridPoint?): Boolean {
        if (p3 == null) return false
        return (p1.x == p2.x && p2.x == p3.x) || (p1.y == p2.y && p2.y == p3.y)
    }
    
    private fun isUnnecessaryZigZag(prev: GridPoint?, current: GridPoint, next: GridPoint, nextNext: GridPoint?): Boolean {
        if (prev == null || nextNext == null) return false
        
        val isHorizontalFirst = prev.y == current.y
        val isVerticalFirst = prev.x == current.x
        val isHorizontalSecond = current.x == next.x
        val isVerticalSecond = current.y == next.y
        
        if ((isHorizontalFirst && isHorizontalSecond) || (isVerticalFirst && isVerticalSecond)) {
            return false
        }
        
        val isHorizontalThird = next.y == nextNext.y
        val isVerticalThird = next.x == nextNext.x
        
        return (isHorizontalFirst && isVerticalSecond && isHorizontalThird) ||
               (isVerticalFirst && isHorizontalSecond && isVerticalThird)
    }
    
    private fun removeZigzags(path: List<GridPoint>): List<GridPoint> {
        if (path.size < 4) return path
        
        // First pass: create a simplified orthogonal path
        val result = mutableListOf<GridPoint>()
        result.add(path.first())
        
        for (i in 1 until path.size) {
            val prev = result.last()
            val current = path[i]
            
            // Ensure orthogonal movement by adding corner points when needed
            if (prev.x != current.x && prev.y != current.y) {
                // We need a corner to maintain orthogonal paths
                result.add(GridPoint(current.x, prev.y))
            }
            
            result.add(current)
        }
        
        // Second pass: remove zigzags
        val withoutZigzags = mutableListOf<GridPoint>()
        withoutZigzags.add(result.first())
        
        var i = 1
        while (i < result.size - 2) {
            val a = withoutZigzags.last()
            val b = result[i]
            val c = result[i + 1]
            val d = result[i + 2]
            
            // Check for horizontal-vertical-horizontal zigzag
            if (a.y == b.y && b.x == c.x && c.y == d.y) {
                // Skip the middle points and add a direct corner
                withoutZigzags.add(GridPoint(d.x, a.y))
                i += 3 // Skip to after the zigzag
                continue
            }
            
            // Check for vertical-horizontal-vertical zigzag
            if (a.x == b.x && b.y == c.y && c.x == d.x) {
                // Skip the middle points and add a direct corner
                withoutZigzags.add(GridPoint(a.x, d.y))
                i += 3 // Skip to after the zigzag
                continue
            }
            
            withoutZigzags.add(b)
            i++
        }
        
        // Add remaining points
        while (i < result.size) {
            withoutZigzags.add(result[i])
            i++
        }
        
        // Third pass: simplify by removing unnecessary points on straight lines
        val simplified = mutableListOf<GridPoint>()
        simplified.add(withoutZigzags.first())
        
        for (i in 1 until withoutZigzags.size - 1) {
            val prev = simplified.last()
            val current = withoutZigzags[i]
            val next = withoutZigzags[i + 1]
            
            // Skip points that are on the same straight line
            if ((prev.x == current.x && current.x == next.x) ||
                (prev.y == current.y && current.y == next.y)) {
                continue
            }
            
            simplified.add(current)
        }
        
        simplified.add(withoutZigzags.last())
        return simplified
    }
}
