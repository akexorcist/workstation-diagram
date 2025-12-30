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

                val tentativeG = current.gCost + moveCost + crossingCost + turnCost

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

        val path = if (config.simplifyPath) simplifyPath(rawPath) else rawPath

        val crossings = path.zipWithNext().sumOf { (from, to) ->
            existingPaths.values.sumOf { waypoints ->
                waypoints.zipWithNext().count { (p1, p2) -> segmentsIntersect(from, to, p1, p2) }
            }
        }

        return PathResult(path, true, endNode.gCost, crossings)
    }

    private fun simplifyPath(path: List<GridPoint>): List<GridPoint> {
        if (path.size <= 2) return path

        val simplified = mutableListOf(path.first())
        var i = 0
        while (i < path.size - 1) {
            var j = i + 1
            while (j < path.size && isCollinear(path[i], path[j], path.getOrNull(j + 1))) {
                j++
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
}
