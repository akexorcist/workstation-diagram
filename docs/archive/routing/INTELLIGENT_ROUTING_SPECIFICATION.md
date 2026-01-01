# Intelligent Connection Routing - Specification

## Overview

Implementation of an intelligent grid-based connection routing system using A* pathfinding algorithm to route connections around devices with optimal paths and minimal crossings.

---

## Requirements Summary

Based on user requirements clarification:

### Core Algorithms
- **Pathfinding**: A* algorithm with Manhattan distance heuristic
- **Multi-Connection**: Simultaneous routing with conflict resolution
- **Grid System**: 10x10 virtual units per grid cell
- **Layout**: Devices snap to grid positions

### Routing Rules
1. ✅ No connection passes through devices
2. ✅ 10 virtual unit clearance from devices
3. ✅ Paths follow grid system (orthogonal only)
4. ✅ Connections can cross each other
5. ✅ No two connections on same grid segment in same direction
6. ✅ Port connections extend from device edge (20 units minimum)
7. ✅ Shortest path with crossing penalty and turn penalty
8. ✅ Path simplification removes unnecessary turns
9. ✅ Ports are 1-to-1 only (one connection per port)
10. ✅ Hybrid calculation (recalculate on window resize)

### Failure Handling
- Cannot find path → Draw straight line in warning color (e.g., red/orange)
- Show visual indicator that routing failed

### Visual Feedback
- Optional debug mode (checkbox to enable)
- When enabled, show:
  - Grid overlay
  - Crossing points
  - Clearance zones
  - Failed routes highlighted

### Configuration
- All routing parameters configurable in code
- Easy to tune without recompiling UI

---

## Connection Path Travel Rules

### Path Structure
Every connection path consists of the following segments in order:

1. **Grid-Snapped Port** → Port position snapped to nearest grid cell center
   - Original port position calculated but not rendered
   - Path starts from grid-aligned position
2. **Port Extension** → Extend straight from snapped port (perpendicular to device edge)
   - Minimum 20 virtual units (snapped to grid)
   - Direction: LEFT/RIGHT for left/right ports, UP/DOWN for top/bottom ports
3. **Grid Routing** → Navigate on grid using orthogonal (horizontal/vertical) segments
   - Only 90-degree turns allowed
   - Follows A* pathfinding with cost optimization
   - **Strict occupancy enforcement** - blocked if same direction occupied
4. **Port Extension** → Extend straight to target port (grid-aligned)
5. **Grid-Snapped Port** → End at target grid-aligned position

### Travel Constraints

**Device Avoidance:**
- ❌ Connections NEVER pass through device boundaries
- ✅ Maintain minimum 10 virtual unit clearance from all devices
- ✅ Routing treats devices + clearance as blocked obstacles

**Grid Alignment:**
- ✅ All routing segments snap to 10×10 virtual unit grid
- ✅ Device sizes snap to grid boundaries for clean routing
- ✅ **Port positions snap to grid** (no misaligned starting points)
- ✅ **Ports snap outside device bounds** (TOP/LEFT ports push outward to avoid overlapping device)
- ✅ **Port extensions snap to grid** (clean transitions)
- ✅ Path follows only horizontal and vertical grid lines
- ❌ No diagonal movements allowed

**Direction Rules:**
- ✅ Connections can cross each other (perpendicular crossing OK)
- ❌ **Two connections CANNOT travel same direction in same grid cell** (ENFORCED)
  - Example: Both going NORTH through cell (0,0) → BLOCKED by A*
  - Example: One NORTH, one EAST through cell (0,0) → Allowed
- ✅ Opposite directions can share grid cells (NORTH + SOUTH = OK)
- ✅ A* algorithm strictly enforces occupancy - will find alternative route or fail
- ✅ **Both endpoints of each segment are marked as occupied** to prevent overlaps
- ✅ **Port extension segments included in occupancy tracking** (full path tracked)

**Path Optimization:**
- ✅ Prefer shorter paths (Manhattan distance)
- ✅ Minimize crossings with existing connections (crossing penalty = 5.0)
- ✅ Minimize turns for cleaner appearance (turn penalty = 2.0)
- ✅ Remove collinear intermediate points (path simplification)
- ✅ **Port extensions checked for conflicts before routing** (prevents overlaps at starts/ends)
- ✅ **Duplicate consecutive points filtered** (prevents zero-length segments with wrong directions)

### Example Path Flow

```
Monitor [HDMI-IN] (actual port)
     ↑
     • (snap to grid)
     |
     | (extension up, grid-aligned)
     |
     →→→→ (routing right on grid)
         |
         | (routing down on grid)
         |
         ↓ (extension down, grid-aligned)
         |
         • (snap to grid)
         ↑
    [HDMI-OUT] Laptop (actual port)
```

**Key Improvements:**
- Port positions snap to nearest grid cell center
- Extensions are calculated from snapped positions
- No misaligned micro-segments at port connections
- Clean, professional appearance throughout entire path

**Cost Breakdown for Above Path:**
- Distance: 50 cells × 1.0 = 50.0
- Turns: 2 turns × 2.0 = 4.0
- Crossings: 0 × 5.0 = 0.0
- **Total Cost: 54.0**

### Routing Order

Connections are routed sequentially in order of **shortest straight-line distance**:
1. Calculate Euclidean distance between source and target ports
2. Sort all connections by this distance (shortest first)
3. Route shorter connections first
4. Later connections navigate around already-routed paths

**Rationale:**
- Short connections get optimal, direct paths
- Long connections have more flexibility to route around obstacles
- Results in cleaner overall diagram layout

---

## Port Connection Rules

### 1-to-1 Port Mapping
Each port can only be used by **one connection**. This ensures:
- Clear, unambiguous connection topology
- Simpler validation and error detection
- More maintainable diagrams
- Realistic representation of physical hardware limitations

### Validation
The system validates that:
- No port is used as a source by multiple connections
- No port is used as a target by multiple connections
- Validation fails with clear error message if 1-to-1 rule is violated

### Data Model Example
```json
{
  "connections": [
    {
      "id": "conn-1",
      "sourceDeviceId": "laptop",
      "sourcePortId": "usb-c-1",     // ← Port used only once
      "targetDeviceId": "monitor",
      "targetPortId": "hdmi-in"       // ← Port used only once
    }
  ]
}
```

**Invalid Example** (would fail validation):
```json
{
  "connections": [
    {"id": "conn-1", "sourcePortId": "usb-c-1", ...},
    {"id": "conn-2", "sourcePortId": "usb-c-1", ...}  // ❌ Port reused
  ]
}
```

---

## Configuration Structure

```kotlin
// src/commonMain/kotlin/dev/akexorcist/workstation/routing/RoutingConfig.kt
object RoutingConfig {
    /**
     * Grid System Configuration
     */
    // Size of each grid cell in virtual units
    var gridCellSize: Float = 10f
    
    // Whether devices should snap to grid
    var deviceSnapToGrid: Boolean = true
    
    /**
     * Clearance Configuration
     */
    // Minimum clearance from devices (virtual units)
    var deviceClearance: Float = 10f
    
    // Minimum extension from port before first turn (virtual units)
    var portExtension: Float = 20f
    
    /**
     * Pathfinding Configuration
     */
    // Weight/cost for a crossing (higher = avoid crossings more)
    var crossingPenalty: Float = 5f
    
    // Weight for each grid unit traveled (base cost)
    var gridMoveCost: Float = 1f
    
    // Weight/cost for making a turn (higher = prefer straight paths)
    var turnPenalty: Float = 2f
    
    // Whether to allow diagonal moves (currently false - orthogonal only)
    var allowDiagonal: Boolean = false
    
    /**
     * Routing Behavior
     */
    // Simplify collinear points to reduce visual complexity
    var simplifyPath: Boolean = true
    
    // Maximum A* iterations before giving up (prevent infinite loops)
    var maxPathfindingIterations: Int = 10000
    
    /**
     * Visual Configuration
     */
    // Color for failed routes (when no path found)
    var failedRouteColor: androidx.compose.ui.graphics.Color = 
        androidx.compose.ui.graphics.Color(0xFFFF5722) // Orange-red
    
    // Alpha for failed routes
    var failedRouteAlpha: Float = 0.8f
    
    // Width multiplier for failed routes (makes them thicker to stand out)
    var failedRouteWidthMultiplier: Float = 1.5f
    
    /**
     * Debug Visualization
     */
    // Show debug visualization (controlled by UI checkbox)
    var showDebugVisualization: Boolean = false
    
    // Grid line color when debug mode enabled
    var debugGridColor: androidx.compose.ui.graphics.Color = 
        androidx.compose.ui.graphics.Color(0x30FFFFFF) // Semi-transparent white
    
    // Crossing point marker color
    var debugCrossingColor: androidx.compose.ui.graphics.Color = 
        androidx.compose.ui.graphics.Color(0xFFFFEB3B) // Yellow
    
    // Clearance zone color
    var debugClearanceColor: androidx.compose.ui.graphics.Color = 
        androidx.compose.ui.graphics.Color(0x20FF0000) // Semi-transparent red
    
    // Size of crossing point markers
    var debugCrossingMarkerSize: Float = 4f
    
    /**
     * Performance Configuration
     */
    // Enable path caching (user wants simple approach, but cache is still good)
    var enableCaching: Boolean = true
    
    // Recalculate on window resize
    var recalculateOnResize: Boolean = true
}
```

---

## Data Structures

### Grid System

```kotlin
// src/commonMain/kotlin/dev/akexorcist/workstation/routing/GridSystem.kt

/**
 * Represents a point on the routing grid
 */
data class GridPoint(
    val x: Int,  // Grid column
    val y: Int   // Grid row
) {
    // Manhattan distance to another point
    fun manhattanDistanceTo(other: GridPoint): Int {
        return kotlin.math.abs(x - other.x) + kotlin.math.abs(y - other.y)
    }
}

/**
 * Direction a connection travels through a grid segment
 */
enum class GridDirection {
    NORTH,  // Moving up (decreasing Y)
    SOUTH,  // Moving down (increasing Y)
    EAST,   // Moving right (increasing X)
    WEST;   // Moving left (decreasing X)
    
    fun isOpposite(other: GridDirection): Boolean {
        return (this == NORTH && other == SOUTH) ||
               (this == SOUTH && other == NORTH) ||
               (this == EAST && other == WEST) ||
               (this == WEST && other == EAST)
    }
    
    fun isPerpendicular(other: GridDirection): Boolean {
        return (isVertical() && other.isHorizontal()) ||
               (isHorizontal() && other.isVertical())
    }
    
    fun isVertical(): Boolean = this == NORTH || this == SOUTH
    fun isHorizontal(): Boolean = this == EAST || this == WEST
}

/**
 * Tracks which connections occupy a grid segment and in which directions
 */
data class GridSegment(
    val from: GridPoint,
    val to: GridPoint
) {
    // Map of connection ID to direction
    private val occupancy = mutableMapOf<String, GridDirection>()
    
    fun canOccupy(direction: GridDirection): Boolean {
        // Empty segment - always allowed
        if (occupancy.isEmpty()) return true
        
        // Check if any existing occupant conflicts
        return occupancy.values.none { existingDir ->
            // Conflict if same direction (both going north)
            existingDir == direction
        }
    }
    
    fun occupy(connectionId: String, direction: GridDirection) {
        occupancy[connectionId] = direction
    }
    
    fun release(connectionId: String) {
        occupancy.remove(connectionId)
    }
    
    fun getOccupancy(): Map<String, GridDirection> = occupancy.toMap()
}

/**
 * The routing grid with obstacle tracking
 */
class RoutingGrid(
    val width: Int,   // Number of grid columns
    val height: Int,  // Number of grid rows
    val cellSize: Float  // Size of each cell in virtual units
) {
    // 2D array tracking if grid cell is blocked by device
    private val blocked = Array(width) { BooleanArray(height) { false } }
    
    // Track occupied segments
    private val segments = mutableMapOf<String, GridSegment>()
    
    /**
     * Convert virtual coordinate to grid coordinate
     */
    fun toGridPoint(virtualX: Float, virtualY: Float): GridPoint {
        return GridPoint(
            x = (virtualX / cellSize).toInt(),
            y = (virtualY / cellSize).toInt()
        )
    }
    
    /**
     * Convert grid coordinate to virtual coordinate (center of cell)
     */
    fun toVirtualPoint(gridPoint: GridPoint): Pair<Float, Float> {
        return Pair(
            gridPoint.x * cellSize + cellSize / 2,
            gridPoint.y * cellSize + cellSize / 2
        )
    }
    
    /**
     * Mark grid cells occupied by a device (with clearance)
     */
    fun markDeviceObstacle(
        deviceX: Float,
        deviceY: Float,
        deviceWidth: Float,
        deviceHeight: Float,
        clearance: Float
    ) {
        // Calculate grid bounds with clearance
        val x1 = ((deviceX - clearance) / cellSize).toInt().coerceAtLeast(0)
        val y1 = ((deviceY - clearance) / cellSize).toInt().coerceAtLeast(0)
        val x2 = ((deviceX + deviceWidth + clearance) / cellSize).toInt().coerceAtMost(width - 1)
        val y2 = ((deviceY + deviceHeight + clearance) / cellSize).toInt().coerceAtMost(height - 1)
        
        // Mark all cells in this rectangle as blocked
        for (x in x1..x2) {
            for (y in y1..y2) {
                blocked[x][y] = true
            }
        }
    }
    
    /**
     * Check if grid cell is blocked
     */
    fun isBlocked(point: GridPoint): Boolean {
        if (point.x < 0 || point.x >= width || point.y < 0 || point.y >= height) {
            return true  // Out of bounds = blocked
        }
        return blocked[point.x][point.y]
    }
    
    /**
     * Get segment key for lookup
     */
    private fun getSegmentKey(from: GridPoint, to: GridPoint): String {
        // Normalize direction for consistent key
        val (p1, p2) = if (from.x < to.x || (from.x == to.x && from.y < to.y)) {
            from to to
        } else {
            to to from
        }
        return "${p1.x},${p1.y}-${p2.x},${p2.y}"
    }
    
    /**
     * Check if segment can be occupied in given direction
     */
    fun canOccupySegment(from: GridPoint, to: GridPoint, direction: GridDirection): Boolean {
        val key = getSegmentKey(from, to)
        val segment = segments.getOrPut(key) { GridSegment(from, to) }
        return segment.canOccupy(direction)
    }
    
    /**
     * Occupy a segment
     */
    fun occupySegment(connectionId: String, from: GridPoint, to: GridPoint, direction: GridDirection) {
        val key = getSegmentKey(from, to)
        val segment = segments.getOrPut(key) { GridSegment(from, to) }
        segment.occupy(connectionId, direction)
    }
    
    /**
     * Get neighbors for A* pathfinding
     */
    fun getNeighbors(point: GridPoint): List<Pair<GridPoint, GridDirection>> {
        val neighbors = mutableListOf<Pair<GridPoint, GridDirection>>()
        
        // North
        if (point.y > 0) {
            neighbors.add(GridPoint(point.x, point.y - 1) to GridDirection.NORTH)
        }
        // South
        if (point.y < height - 1) {
            neighbors.add(GridPoint(point.x, point.y + 1) to GridDirection.SOUTH)
        }
        // West
        if (point.x > 0) {
            neighbors.add(GridPoint(point.x - 1, point.y) to GridDirection.WEST)
        }
        // East
        if (point.x < width - 1) {
            neighbors.add(GridPoint(point.x + 1, point.y) to GridDirection.EAST)
        }
        
        return neighbors
    }
}
```

---

## A* Pathfinding Algorithm

### Turn Reduction Strategy

The pathfinding algorithm includes **turn penalty** to create cleaner, more aesthetic paths:

**Cost Function:**
```
totalCost = baseCost + crossingCost + turnCost + occupancyCost

where:
- baseCost = distance × gridMoveCost (1.0)
- crossingCost = crossings × crossingPenalty (5.0)
- turnCost = turns × turnPenalty (2.0)
- occupancyCost = conflicts × crossingPenalty × 10
```

**Turn Detection:**
- A turn occurs when direction changes (e.g., NORTH → EAST)
- Continuing straight has 0 turn cost
- Each direction change adds turnPenalty to path cost
- This naturally guides A* to prefer straight paths

**Path Simplification:**
After pathfinding completes, collinear points are removed:
```
Original:  A → B → C → D → E
           (all horizontal)
Simplified: A → E
           (removes intermediate points B, C, D)
```

This results in:
- Fewer waypoints (simpler data)
- Cleaner visual appearance
- Easier to understand connection flow
- More "professional" looking diagrams

### Algorithm Implementation

```kotlin
// src/commonMain/kotlin/dev/akexorcist/workstation/routing/AStarPathfinder.kt

/**
 * A* node for pathfinding
 */
private data class AStarNode(
    val point: GridPoint,
    val gCost: Float,  // Cost from start
    val hCost: Float,  // Heuristic cost to end
    val parent: AStarNode?,
    val direction: GridDirection?  // Direction we came from
) : Comparable<AStarNode> {
    val fCost: Float get() = gCost + hCost
    
    override fun compareTo(other: AStarNode): Int {
        return fCost.compareTo(other.fCost)
    }
}

/**
 * A* pathfinding result
 */
data class PathResult(
    val waypoints: List<GridPoint>,
    val success: Boolean,
    val totalCost: Float,
    val crossings: Int
)

/**
 * A* pathfinder for grid-based routing
 */
class AStarPathfinder(
    private val grid: RoutingGrid,
    private val config: RoutingConfig
) {
    /**
     * Find path from start to end, considering existing routes
     */
    fun findPath(
        start: GridPoint,
        end: GridPoint,
        connectionId: String,
        existingPaths: Map<String, List<GridPoint>>
    ): PathResult {
        // Priority queue for A*
        val openSet = java.util.PriorityQueue<AStarNode>()
        val closedSet = mutableSetOf<GridPoint>()
        val gScores = mutableMapOf<GridPoint, Float>()
        
        // Start node
        val startNode = AStarNode(
            point = start,
            gCost = 0f,
            hCost = heuristic(start, end),
            parent = null,
            direction = null
        )
        
        openSet.add(startNode)
        gScores[start] = 0f
        
        var iterations = 0
        
        while (openSet.isNotEmpty() && iterations < config.maxPathfindingIterations) {
            iterations++
            
            val current = openSet.poll()
            
            // Found the goal!
            if (current.point == end) {
                return reconstructPath(current, existingPaths)
            }
            
            closedSet.add(current.point)
            
            // Explore neighbors
            for ((neighbor, direction) in grid.getNeighbors(current.point)) {
                // Skip if blocked by device
                if (grid.isBlocked(neighbor)) continue
                
                // Skip if already evaluated
                if (neighbor in closedSet) continue
                
                // Calculate cost to reach this neighbor
                val moveCost = config.gridMoveCost
                val crossingCost = calculateCrossingCost(
                    current.point,
                    neighbor,
                    direction,
                    existingPaths
                )
                
                val tentativeGCost = current.gCost + moveCost + crossingCost
                
                // Check if this is a better path
                val previousGCost = gScores[neighbor] ?: Float.MAX_VALUE
                if (tentativeGCost < previousGCost) {
                    gScores[neighbor] = tentativeGCost
                    
                    val neighborNode = AStarNode(
                        point = neighbor,
                        gCost = tentativeGCost,
                        hCost = heuristic(neighbor, end),
                        parent = current,
                        direction = direction
                    )
                    
                    openSet.add(neighborNode)
                }
            }
        }
        
        // No path found
        return PathResult(
            waypoints = emptyList(),
            success = false,
            totalCost = Float.MAX_VALUE,
            crossings = 0
        )
    }
    
    /**
     * Manhattan distance heuristic
     */
    private fun heuristic(from: GridPoint, to: GridPoint): Float {
        return from.manhattanDistanceTo(to).toFloat() * config.gridMoveCost
    }
    
    /**
     * Calculate cost of crossing existing paths
     */
    private fun calculateCrossingCost(
        from: GridPoint,
        to: GridPoint,
        direction: GridDirection,
        existingPaths: Map<String, List<GridPoint>>
    ): Float {
        var cost = 0f
        
        // Check if this segment crosses any existing paths
        for ((pathId, waypoints) in existingPaths) {
            for (i in 0 until waypoints.size - 1) {
                val pathFrom = waypoints[i]
                val pathTo = waypoints[i + 1]
                
                // Check if segments intersect
                if (segmentsIntersect(from, to, pathFrom, pathTo)) {
                    // Get direction of existing path segment
                    val existingDirection = getDirection(pathFrom, pathTo)
                    
                    // Check if directions allow sharing
                    if (!grid.canOccupySegment(from, to, direction)) {
                        // Cannot share - make this path very expensive
                        cost += config.crossingPenalty * 10
                    } else {
                        // Can cross - add crossing penalty
                        cost += config.crossingPenalty
                    }
                }
            }
        }
        
        return cost
    }
    
    /**
     * Check if two segments intersect
     */
    private fun segmentsIntersect(
        a1: GridPoint, a2: GridPoint,
        b1: GridPoint, b2: GridPoint
    ): Boolean {
        // Check if segments share any grid cells
        val aXRange = minOf(a1.x, a2.x)..maxOf(a1.x, a2.x)
        val aYRange = minOf(a1.y, a2.y)..maxOf(a1.y, a2.y)
        val bXRange = minOf(b1.x, b2.x)..maxOf(b1.x, b2.x)
        val bYRange = minOf(b1.y, b2.y)..maxOf(b1.y, b2.y)
        
        return aXRange.any { it in bXRange } && aYRange.any { it in bYRange }
    }
    
    /**
     * Get direction between two adjacent points
     */
    private fun getDirection(from: GridPoint, to: GridPoint): GridDirection {
        return when {
            to.x > from.x -> GridDirection.EAST
            to.x < from.x -> GridDirection.WEST
            to.y > from.y -> GridDirection.SOUTH
            to.y < from.y -> GridDirection.NORTH
            else -> GridDirection.EAST  // Shouldn't happen
        }
    }
    
    /**
     * Reconstruct path from A* result
     */
    private fun reconstructPath(
        endNode: AStarNode,
        existingPaths: Map<String, List<GridPoint>>
    ): PathResult {
        val path = mutableListOf<GridPoint>()
        var current: AStarNode? = endNode
        
        while (current != null) {
            path.add(0, current.point)
            current = current.parent
        }
        
        // Count crossings
        val crossings = countCrossings(path, existingPaths)
        
        return PathResult(
            waypoints = path,
            success = true,
            totalCost = endNode.gCost,
            crossings = crossings
        )
    }
    
    /**
     * Count how many times this path crosses existing paths
     */
    private fun countCrossings(
        path: List<GridPoint>,
        existingPaths: Map<String, List<GridPoint>>
    ): Int {
        var crossings = 0
        
        for (i in 0 until path.size - 1) {
            val from = path[i]
            val to = path[i + 1]
            
            for ((_, waypoints) in existingPaths) {
                for (j in 0 until waypoints.size - 1) {
                    if (segmentsIntersect(from, to, waypoints[j], waypoints[j + 1])) {
                        crossings++
                    }
                }
            }
        }
        
        return crossings
    }
}
```

---

## Router Coordinator

```kotlin
// src/commonMain/kotlin/dev/akexorcist/workstation/routing/ConnectionRouter.kt

/**
 * Routed connection result
 */
data class RoutedConnection(
    val connectionId: String,
    val waypoints: List<GridPoint>,
    val virtualWaypoints: List<Pair<Float, Float>>,  // Converted to virtual coordinates
    val success: Boolean,
    val crossings: Int
)

/**
 * Main connection router - coordinates routing for all connections
 */
class ConnectionRouter(
    private val config: RoutingConfig = RoutingConfig
) {
    /**
     * Route all connections simultaneously with conflict resolution
     */
    fun routeConnections(
        devices: List<Device>,
        connections: List<Connection>,
        virtualCanvasSize: Size
    ): List<RoutedConnection> {
        // Calculate grid dimensions
        val gridWidth = (virtualCanvasSize.width / config.gridCellSize).toInt()
        val gridHeight = (virtualCanvasSize.height / config.gridCellSize).toInt()
        
        // Create grid
        val grid = RoutingGrid(gridWidth, gridHeight, config.gridCellSize)
        
        // Mark device obstacles
        for (device in devices) {
            grid.markDeviceObstacle(
                deviceX = device.position.x,
                deviceY = device.position.y,
                deviceWidth = device.size.width,
                deviceHeight = device.size.height,
                clearance = config.deviceClearance
            )
        }
        
        // Create pathfinder
        val pathfinder = AStarPathfinder(grid, config)
        
        // Store routed connections
        val routedConnections = mutableListOf<RoutedConnection>()
        val existingPaths = mutableMapOf<String, List<GridPoint>>()
        
        // Route all connections (simultaneous with conflict resolution)
        for (connection in connections) {
            val sourceDevice = devices.find { it.id == connection.sourceDeviceId }
            val targetDevice = devices.find { it.id == connection.targetDeviceId }
            
            if (sourceDevice == null || targetDevice == null) continue
            
            val sourcePort = sourceDevice.ports.find { it.id == connection.sourcePortId }
            val targetPort = targetDevice.ports.find { it.id == connection.targetPortId }
            
            if (sourcePort == null || targetPort == null) continue
            
            // Calculate port positions in virtual space
            val sourcePos = calculatePortPosition(sourceDevice, sourcePort)
            val targetPos = calculatePortPosition(targetDevice, targetPort)
            
            // Calculate start/end points with port extension
            val startPoint = calculateExtendedPortPoint(
                sourceDevice, sourcePort, sourcePos, config.portExtension
            )
            val endPoint = calculateExtendedPortPoint(
                targetDevice, targetPort, targetPos, config.portExtension
            )
            
            // Convert to grid coordinates
            val startGrid = grid.toGridPoint(startPoint.first, startPoint.second)
            val endGrid = grid.toGridPoint(endPoint.first, endPoint.second)
            
            // Find path
            val result = pathfinder.findPath(startGrid, endGrid, connection.id, existingPaths)
            
            if (result.success) {
                // Store path for future conflict detection
                existingPaths[connection.id] = result.waypoints
                
                // Occupy grid segments
                for (i in 0 until result.waypoints.size - 1) {
                    val from = result.waypoints[i]
                    val to = result.waypoints[i + 1]
                    val direction = getSegmentDirection(from, to)
                    grid.occupySegment(connection.id, from, to, direction)
                }
                
                // Convert waypoints to virtual coordinates
                val virtualWaypoints = result.waypoints.map { grid.toVirtualPoint(it) }
                
                routedConnections.add(
                    RoutedConnection(
                        connectionId = connection.id,
                        waypoints = result.waypoints,
                        virtualWaypoints = virtualWaypoints,
                        success = true,
                        crossings = result.crossings
                    )
                )
            } else {
                // Failed to route - create straight line fallback
                routedConnections.add(
                    RoutedConnection(
                        connectionId = connection.id,
                        waypoints = listOf(startGrid, endGrid),
                        virtualWaypoints = listOf(
                            startPoint.first to startPoint.second,
                            endPoint.first to endPoint.second
                        ),
                        success = false,
                        crossings = 0
                    )
                )
            }
        }
        
        return routedConnections
    }
    
    // Helper methods...
    private fun calculatePortPosition(device: Device, port: Port): Pair<Float, Float> {
        // Implementation from existing code
        TODO()
    }
    
    private fun calculateExtendedPortPoint(
        device: Device,
        port: Port,
        portPos: Pair<Float, Float>,
        extension: Float
    ): Pair<Float, Float> {
        // Extend horizontally from device edge
        TODO()
    }
    
    private fun getSegmentDirection(from: GridPoint, to: GridPoint): GridDirection {
        return when {
            to.x > from.x -> GridDirection.EAST
            to.x < from.x -> GridDirection.WEST
            to.y > from.y -> GridDirection.SOUTH
            else -> GridDirection.NORTH
        }
    }
}
```

---

## Integration Points

### 1. DiagramCanvas Updates
- Replace `calculateOrthogonalPath` with router-generated waypoints
- Add debug visualization rendering
- Handle failed routes with warning color

### 2. ViewModel Integration
- Cache routed connections
- Trigger re-routing on layout changes
- Trigger re-routing on window resize

### 3. UI Controls
- Add debug mode checkbox to ControlPanel
- Show routing statistics (crossings, failed routes)

---

## Implementation Phases

### Phase 1: Foundation (Day 1-2)
- [ ] Create `RoutingConfig.kt`
- [ ] Create `GridSystem.kt` data structures
- [ ] Implement grid coordinate conversion
- [ ] Write unit tests for grid system

### Phase 2: A* Implementation (Day 2-3)
- [ ] Implement `AStarPathfinder.kt`
- [ ] Test pathfinding with simple cases
- [ ] Test with obstacles
- [ ] Benchmark performance

### Phase 3: Multi-Connection Routing (Day 3-4)
- [ ] Implement `ConnectionRouter.kt`
- [ ] Simultaneous routing logic
- [ ] Grid occupancy tracking
- [ ] Conflict resolution

### Phase 4: Integration (Day 4-5)
- [ ] Update DiagramCanvas to use router
- [ ] Integrate with ViewModel
- [ ] Handle window resize events
- [ ] Cache management

### Phase 5: Visual Debugging (Day 5)
- [ ] Implement debug visualization
- [ ] Add UI checkbox control
- [ ] Render grid overlay
- [ ] Show crossing points and clearances

### Phase 6: Testing & Polish (Day 6)
- [ ] Test with sample data
- [ ] Test with complex layouts
- [ ] Performance optimization
- [ ] Documentation

**Total: 6 days**

---

## Testing Strategy

### Unit Tests
- Grid coordinate conversion
- A* pathfinding (simple cases)
- Segment intersection detection
- Direction conflict detection

### Integration Tests
- Route 2 connections (no crossing)
- Route 2 connections (with crossing)
- Route connection around obstacle
- Failed routing fallback

### Performance Tests
- 50 connections on 1000x1000 grid
- Measure routing time
- Measure render performance

---

## Success Criteria

- ✅ No connection passes through any device
- ✅ Connections maintain 10 unit clearance from devices
- ✅ All paths follow grid (orthogonal segments only)
- ✅ Connections can cross each other
- ✅ No two connections share same grid segment in same direction
- ✅ Failed routes show warning color
- ✅ Debug mode visualizes routing
- ✅ All parameters configurable
- ✅ Routes recalculate on window resize
- ✅ Routing time < 5 seconds for 50 connections

---

## Configuration Example

```kotlin
// Example: Adjust for tighter/looser routing
RoutingConfig.apply {
    gridCellSize = 15f  // Larger cells = fewer grid points
    deviceClearance = 15f  // More clearance
    crossingPenalty = 10f  // Avoid crossings more aggressively
    portExtension = 30f  // Longer straight extension from ports
}
```

---

---

## Design Review - Issues & Resolutions

### Critical Issues Found & Resolved

#### 1. ✅ **Port Extension Direction** - RESOLVED
**Issue**: Ambiguity about port extension direction when user said "horizontal" but ports can be on any device side.

**Resolution**: Follow existing code behavior - always extend left or right from port regardless of port side. No changes to existing port extension logic required.

---

#### 2. ✅ **Device Grid Snapping** - RESOLVED
**Issue**: Unclear when and how devices snap to grid.

**Resolution**: 
- Snap device size (not position) to align with grid
- Device top-left position remains as-is from JSON
- Formula: `snappedSize = ceil(deviceSize / gridSize) * gridSize`
- Snap at routing calculation time (don't modify original data)
- Ensures device boundaries align with grid cells

---

#### 3. ✅ **Grid Occupancy Conflict Logic** - RESOLVED
**Issue**: Original spec tracked occupancy per segment, but should track per grid cell.

**Resolution**: Track occupancy **per grid cell** with correct conflict logic:
```kotlin
// Two connections conflict on a grid CELL if:
// - Both pass through the same cell
// - In the same direction (both North, both South, etc.)
// - NOT perpendicular (North & East = OK to share)
// - NOT opposite (North & South = OK to share)
```

**Implementation updated** in `GridSystem.kt` data structures.

---

#### 4. ✅ **Port Position Calculation** - RESOLVED
**Issue**: `calculatePortPosition()` and `calculateExtendedPortPoint()` marked as `TODO()`.

**Resolution**: Implement these by copying and adapting existing logic from `calculatePortScreenPosition` in current codebase, converting to work with virtual coordinates instead of screen coordinates.

---

#### 5. ✅ **A* Priority Queue Platform Compatibility** - RESOLVED
**Issue**: Using `java.util.PriorityQueue` which is JVM-specific.

**Resolution**: Keep `java.util.PriorityQueue` for now since:
- Application currently targets JVM only
- When targeting other platforms (JS/Native) in future, will implement platform-specific priority queue or use Kotlin multiplatform library
- Low priority issue - doesn't block current implementation

---

#### 6. ✅ **Crossing Cost Calculation Performance** - ACCEPTED
**Issue**: O(n²) complexity when checking all existing paths for every neighbor evaluation.

**Resolution**: Accept current approach because:
- Actual device and connection counts are small (not performance-critical)
- Typical layouts have 10-20 connections, not hundreds
- Premature optimization avoided
- Can optimize later if profiling shows issues

---

#### 7. ✅ **Segment Intersection Detection** - RESOLVED
**Issue**: `segmentsIntersect()` only checks bounding box overlap, not actual intersection.

**Resolution**: Implement correct orthogonal segment intersection:
```kotlin
fun segmentsIntersect(a1: GridPoint, a2: GridPoint, b1: GridPoint, b2: GridPoint): Boolean {
    // For orthogonal segments only:
    
    // Horizontal A, Vertical B
    if (a1.y == a2.y && b1.x == b2.x) {
        return b1.x in minOf(a1.x, a2.x)..maxOf(a1.x, a2.x) &&
               a1.y in minOf(b1.y, b2.y)..maxOf(b1.y, b2.y)
    }
    
    // Vertical A, Horizontal B
    if (a1.x == a2.x && b1.y == b2.y) {
        return a1.x in minOf(b1.x, b2.x)..maxOf(b1.x, b2.x) &&
               b1.y in minOf(a1.y, a2.y)..maxOf(a1.y, a2.y)
    }
    
    // Parallel segments don't intersect
    return false
}
```

---

#### 8. ✅ **Window Resize Trigger** - RESOLVED
**Issue**: Spec says "recalculate on window resize" but no implementation shown.

**Resolution**: Use existing `canvasSize` state in `WorkstationDiagramScreen`:
```kotlin
LaunchedEffect(canvasSize) {
    // Trigger re-routing when canvas size changes
    viewModel.recalculateRouting()
}
```

---

#### 9. ✅ **Grid Occupancy Data Structure** - RESOLVED
**Issue**: Need better structure for tracking cell occupancy.

**Resolution**: Implement per-cell tracking:
```kotlin
data class GridCell(val x: Int, val y: Int) {
    // Map of direction to set of connection IDs using that direction
    private val occupancy = mutableMapOf<GridDirection, MutableSet<String>>()
    
    fun canOccupy(connectionId: String, direction: GridDirection): Boolean {
        val existing = occupancy[direction] ?: return true
        return existing.isEmpty() || connectionId in existing
    }
    
    fun occupy(connectionId: String, direction: GridDirection) {
        occupancy.getOrPut(direction) { mutableSetOf() }.add(connectionId)
    }
}
```

---

#### 10. ✅ **Routing Order** - RESOLVED
**Issue**: Sequential routing means later connections get worse paths.

**Resolution**: Use better approach for routing order:
- Sort connections by shortest path first (straight-line distance)
- Shorter connections route first, have priority for optimal paths
- Longer connections route later, can navigate around existing routes
- This naturally produces better overall layouts

---

#### 11. ✅ **Cache Management** - RESOLVED
**Issue**: Caching mentioned but no invalidation strategy.

**Resolution**: Simple cache invalidation strategy:
- Invalidate cache **only** on window resize
- Invalidate cache on pan/zoom (recalculate paths for new viewport)
- **NOT** invalidated on device move or connection add/remove (those require JSON file changes handled externally)
- Cache key: Hash of canvas size + zoom level

---

#### 12. ✅ **Debug Visualization** - REMOVED
**Issue**: Debug features described but integration unclear.

**Resolution**: **Remove all debug visualization from specification**:
- No grid overlay rendering
- No crossing point visualization
- No clearance zone display
- Remove from `RoutingConfig.kt`
- Remove from integration requirements
- Simplifies implementation significantly

---

#### 13. ✅ **Failed Route Straight Line** - RESOLVED
**Issue**: Unclear how to render failed routes.

**Resolution**: Follow recommended approach:
- Draw straight line from port to port (including port extensions)
- Use distinct warning color (orange-red: `0xFFFF5722`)
- Make line thicker than normal (1.5x width multiplier)
- Visual distinction clear to user

---

#### 14. ✅ **Edge Case Handling** - RESOLVED
**Issue**: Missing edge case scenarios.

**Resolution**: Edge cases handled in JSON file validation before rendering:
- Device at grid boundary → JSON ensures valid positions
- Port extension off-grid → JSON ensures adequate margins
- Start and end same cell → JSON ensures minimum device spacing
- Device smaller than clearance → JSON ensures minimum device sizes
- Overlapping devices → JSON validation prevents this

**Application assumes valid, pre-validated JSON input.**

---

#### 15. ✅ **Coordinate System Compatibility** - RESOLVED
**Issue**: Ensure routing works with both absolute and virtual coordinate systems.

**Resolution**: Always route in virtual space:
- If absolute coordinates: Convert to virtual first using `CoordinateTransformer`
- Routing algorithm only works in virtual coordinate space
- Consistent behavior regardless of input coordinate system
- Integrates seamlessly with existing virtual coordinate support

---

#### 16. ✅ **Existing Orthogonal Path Replacement** - RESOLVED
**Issue**: Current code has `calculateOrthogonalPath()` which will be replaced.

**Resolution**: Follow recommended migration strategy:
- Keep old algorithm initially as fallback for testing
- Add feature flag to toggle between old and new routing
- Test new routing thoroughly with flag enabled
- Once stable, remove old algorithm completely
- Smooth migration path with rollback capability

---

### Updated Configuration

After resolving all issues, remove debug configuration:

```kotlin
// REMOVED from RoutingConfig:
// - showDebugVisualization
// - debugGridColor
// - debugCrossingColor
// - debugClearanceColor
// - debugCrossingMarkerSize

// All debug-related code removed from specification
```

---

### Updated Data Structures

#### Revised Grid Cell Structure

```kotlin
// Better occupancy tracking per cell
data class GridCell(
    val x: Int,
    val y: Int
) {
    private val occupancy = mutableMapOf<GridDirection, MutableSet<String>>()
    
    fun canOccupy(connectionId: String, direction: GridDirection): Boolean {
        val existing = occupancy[direction] ?: return true
        return existing.isEmpty() || connectionId in existing
    }
    
    fun occupy(connectionId: String, direction: GridDirection) {
        occupancy.getOrPut(direction) { mutableSetOf() }.add(connectionId)
    }
    
    fun release(connectionId: String, direction: GridDirection) {
        occupancy[direction]?.remove(connectionId)
    }
}

class RoutingGrid(
    val width: Int,
    val height: Int,
    val cellSize: Float
) {
    private val cells = Array(width) { x -> 
        Array(height) { y -> 
            GridCell(x, y) 
        } 
    }
    
    fun getCell(point: GridPoint): GridCell? {
        if (point.x < 0 || point.x >= width || point.y < 0 || point.y >= height) {
            return null
        }
        return cells[point.x][point.y]
    }
}
```

---

### Updated Integration Requirements

#### Remove Debug Features
- ~~Add debug mode checkbox to ControlPanel~~
- ~~Render grid overlay~~
- ~~Show crossing points~~
- ~~Show clearance zones~~

#### Add Cache Management
```kotlin
// In ViewModel
private var routingCache: Map<String, List<RoutedConnection>>? = null
private var lastCanvasSize: Size? = null
private var lastZoomLevel: Float? = null

fun recalculateRouting() {
    val currentKey = "${canvasSize.width}x${canvasSize.height}@${zoom}"
    if (routingCache == null || canvasSize != lastCanvasSize || zoom != lastZoomLevel) {
        // Invalidate cache and recalculate
        routingCache = router.routeConnections(devices, connections, virtualCanvasSize)
        lastCanvasSize = canvasSize
        lastZoomLevel = zoom
    }
}
```

#### Add Window Resize Handler
```kotlin
// In WorkstationDiagramScreen
LaunchedEffect(canvasSize) {
    if (canvasSize.width > 0 && canvasSize.height > 0) {
        viewModel.recalculateRouting()
    }
}
```

---

### Updated Implementation Phases

**Phase 5 REMOVED** (Debug visualization no longer needed)

**Revised Timeline:**

### Phase 1: Foundation (Day 1-2)
- [ ] Create `RoutingConfig.kt` (without debug fields)
- [ ] Create `GridSystem.kt` with revised cell structure
- [ ] Implement grid coordinate conversion
- [ ] Implement device size snapping to grid
- [ ] Write unit tests for grid system

### Phase 2: A* Implementation (Day 2-3)
- [ ] Implement `AStarPathfinder.kt`
- [ ] Implement correct `segmentsIntersect()` logic
- [ ] Test pathfinding with simple cases
- [ ] Test with obstacles
- [ ] Benchmark performance

### Phase 3: Multi-Connection Routing (Day 3-4)
- [ ] Implement `ConnectionRouter.kt`
- [ ] Implement connection sorting by length
- [ ] Grid cell occupancy tracking
- [ ] Conflict resolution logic
- [ ] Port position calculation implementation

### Phase 4: Integration (Day 4-5)
- [ ] Update DiagramCanvas to use router
- [ ] Add feature flag for old vs new routing
- [ ] Integrate with ViewModel
- [ ] Implement window resize handler
- [ ] Implement cache management
- [ ] Failed route rendering with warning color

### Phase 5: Testing & Polish (Day 5)
- [ ] Test with sample data
- [ ] Test with complex layouts
- [ ] Test coordinate system compatibility
- [ ] Performance testing
- [ ] Remove old routing algorithm
- [ ] Documentation

**Total: 5 days** (reduced from 6)

---

## Implementation Notes & Lessons Learned

### Critical Bug Fixes Applied

**1. Duplicate Grid Points in Path (FIXED)**
- **Problem**: Path construction created duplicate consecutive points (e.g., `sourceGrid` and `startGrid` could be same point)
- **Symptom**: Zero-length segments with incorrect direction assignment (defaulted to EAST)
- **Impact**: Occupancy tracking marked wrong directions, allowing overlapping connections
- **Solution**: Filter duplicate consecutive points before occupancy tracking
```kotlin
var lastPoint: GridPoint? = null
fun addUnique(point: GridPoint) {
    if (point != lastPoint) {
        add(point)
        lastPoint = point
    }
}
```

**2. Port Positioning Constraints**
- **Problem**: Multiple ports on same device side with close offsets (e.g., LEFT 0.3 and LEFT 0.7)
- **Symptom**: Port extensions naturally overlap in same direction
- **Solution**: Distribute ports across different device sides to avoid geometric conflicts
- **Best Practice**: Spread high-traffic connections across TOP/BOTTOM/LEFT/RIGHT sides

**3. Grid Snapping Algorithm Mismatch (FIXED)**
- **Problem**: DiagramCanvas used `round()` while ConnectionRouter used `toInt()` for grid snapping
- **Symptom**: Port dots and connection endpoints snapped to different grid cells
- **Impact**: Visual misalignment - connections didn't connect to port dots
- **Solution**: Use consistent `toInt()` (truncation) algorithm in both systems
```kotlin
// Both systems now use:
val gridX = (virtualX / gridCellSize).toInt()
val snappedX = gridX * gridCellSize + cellSize / 2f
```

**4. Port Snapping Inside Device Bounds (FIXED)**
- **Problem**: TOP/LEFT ports snapped to grid cells inside device area
- **Symptom**: Port dots rendered on top of device rectangles instead of outside
- **Impact**: Visual confusion - ports appeared to be inside devices
- **Solution**: Check device bounds and push TOP/LEFT ports outward only
- **Note**: BOTTOM/RIGHT ports naturally snap outside due to grid truncation, no adjustment needed
```kotlin
when (port.position.side) {
    DeviceSide.TOP -> if (gridY >= deviceGridTop) GridPoint(gridX, deviceGridTop - 1)
    DeviceSide.LEFT -> if (gridX >= deviceGridLeft) GridPoint(deviceGridLeft - 1, gridY)
    DeviceSide.BOTTOM, DeviceSide.RIGHT -> gridPoint  // No adjustment
}
```

### Port Layout Guidelines

For optimal routing without overlaps:
1. **Distribute ports across multiple sides** - Avoid clustering on one side
2. **Space port offsets** - Use 0.2, 0.5, 0.8 rather than 0.3, 0.4, 0.5
3. **Consider connection destinations** - Ports should face toward their targets when possible
4. **Test with realistic layouts** - Run routing with actual connection patterns

---

## Next Steps

✅ **Implementation Complete**
- Core routing system implemented and tested
- Occupancy tracking working correctly
- Path deduplication prevents overlap bugs
- 1-to-1 port validation enforced
