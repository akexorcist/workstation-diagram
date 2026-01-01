# Intelligent Connection Routing - Data Structures

## Overview

This document details all data structures used in the routing system.

---

## Configuration

### RoutingConfig

Central configuration object for all routing parameters.

**Location**: `src/commonMain/kotlin/dev/akexorcist/workstation/routing/RoutingConfig.kt`

```kotlin
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
    
    // Whether to allow diagonal moves (currently false - orthogonal only)
    var allowDiagonal: Boolean = false
    
    /**
     * Routing Behavior
     */
    // Simplify collinear points (user wants false - keep all waypoints)
    var simplifyPath: Boolean = false
    
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
     * Performance Configuration
     */
    // Enable path caching
    var enableCaching: Boolean = true
    
    // Recalculate on window resize
    var recalculateOnResize: Boolean = true
}
```

---

## Grid System

### GridPoint

Represents a discrete point on the routing grid.

```kotlin
data class GridPoint(
    val x: Int,  // Grid column
    val y: Int   // Grid row
) {
    /**
     * Manhattan distance to another point
     * Used as A* heuristic
     */
    fun manhattanDistanceTo(other: GridPoint): Int {
        return kotlin.math.abs(x - other.x) + kotlin.math.abs(y - other.y)
    }
}
```

**Usage Example:**
```kotlin
val start = GridPoint(0, 0)
val end = GridPoint(10, 5)
val distance = start.manhattanDistanceTo(end)  // 15
```

---

### GridDirection

Enum representing direction of travel through a grid cell.

```kotlin
enum class GridDirection {
    NORTH,  // Moving up (decreasing Y)
    SOUTH,  // Moving down (increasing Y)
    EAST,   // Moving right (increasing X)
    WEST;   // Moving left (decreasing X)
    
    /**
     * Check if this direction is opposite to another
     * North ↔ South, East ↔ West
     */
    fun isOpposite(other: GridDirection): Boolean {
        return (this == NORTH && other == SOUTH) ||
               (this == SOUTH && other == NORTH) ||
               (this == EAST && other == WEST) ||
               (this == WEST && other == EAST)
    }
    
    /**
     * Check if this direction is perpendicular to another
     * North/South ⊥ East/West
     */
    fun isPerpendicular(other: GridDirection): Boolean {
        return (isVertical() && other.isHorizontal()) ||
               (isHorizontal() && other.isVertical())
    }
    
    fun isVertical(): Boolean = this == NORTH || this == SOUTH
    fun isHorizontal(): Boolean = this == EAST || this == WEST
}
```

**Usage Example:**
```kotlin
val dir1 = GridDirection.NORTH
val dir2 = GridDirection.SOUTH
val dir3 = GridDirection.EAST

dir1.isOpposite(dir2)      // true
dir1.isPerpendicular(dir3) // true
```

---

### GridCell

Tracks occupancy of a single grid cell by multiple connections.

```kotlin
data class GridCell(
    val x: Int,
    val y: Int
) {
    // Map of direction to set of connection IDs using that direction
    private val occupancy = mutableMapOf<GridDirection, MutableSet<String>>()
    
    /**
     * Check if a connection can occupy this cell in given direction
     * Rules:
     * - Empty cell: always allowed
     * - Same connection: always allowed (re-routing)
     * - Different direction already used: NOT allowed
     * - No conflicting direction: allowed
     */
    fun canOccupy(connectionId: String, direction: GridDirection): Boolean {
        val existing = occupancy[direction] ?: return true
        return existing.isEmpty() || connectionId in existing
    }
    
    /**
     * Mark this cell as occupied by a connection in given direction
     */
    fun occupy(connectionId: String, direction: GridDirection) {
        occupancy.getOrPut(direction) { mutableSetOf() }.add(connectionId)
    }
    
    /**
     * Release occupancy by a connection in given direction
     */
    fun release(connectionId: String, direction: GridDirection) {
        occupancy[direction]?.remove(connectionId)
    }
    
    /**
     * Get all current occupancy information
     */
    fun getOccupancy(): Map<GridDirection, Set<String>> = 
        occupancy.mapValues { it.value.toSet() }
}
```

**Key Behavior:**
- Multiple connections can use the same cell if traveling in different directions
- Only one connection can use a specific direction in a cell at once
- This prevents "parallel parking" conflicts

---

### RoutingGrid

The main grid structure managing obstacles and occupancy.

```kotlin
class RoutingGrid(
    val width: Int,      // Number of grid columns
    val height: Int,     // Number of grid rows
    val cellSize: Float  // Size of each cell in virtual units
) {
    // 2D array tracking if grid cell is blocked by device
    private val blocked = Array(width) { BooleanArray(height) { false } }
    
    // 2D array of grid cells for occupancy tracking
    private val cells = Array(width) { x -> 
        Array(height) { y -> 
            GridCell(x, y) 
        } 
    }
    
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
        val x2 = ((deviceX + deviceWidth + clearance) / cellSize).toInt()
            .coerceAtMost(width - 1)
        val y2 = ((deviceY + deviceHeight + clearance) / cellSize).toInt()
            .coerceAtMost(height - 1)
        
        // Mark all cells in this rectangle as blocked
        for (x in x1..x2) {
            for (y in y1..y2) {
                blocked[x][y] = true
            }
        }
    }
    
    /**
     * Check if grid cell is blocked by device
     */
    fun isBlocked(point: GridPoint): Boolean {
        if (point.x < 0 || point.x >= width || 
            point.y < 0 || point.y >= height) {
            return true  // Out of bounds = blocked
        }
        return blocked[point.x][point.y]
    }
    
    /**
     * Get grid cell at point
     */
    fun getCell(point: GridPoint): GridCell? {
        if (point.x < 0 || point.x >= width || 
            point.y < 0 || point.y >= height) {
            return null
        }
        return cells[point.x][point.y]
    }
    
    /**
     * Get neighbors for A* pathfinding
     * Returns list of (neighbor point, direction to reach it)
     */
    fun getNeighbors(point: GridPoint): List<Pair<GridPoint, GridDirection>> {
        val neighbors = mutableListOf<Pair<GridPoint, GridDirection>>()
        
        // North (up)
        if (point.y > 0) {
            neighbors.add(GridPoint(point.x, point.y - 1) to GridDirection.NORTH)
        }
        // South (down)
        if (point.y < height - 1) {
            neighbors.add(GridPoint(point.x, point.y + 1) to GridDirection.SOUTH)
        }
        // West (left)
        if (point.x > 0) {
            neighbors.add(GridPoint(point.x - 1, point.y) to GridDirection.WEST)
        }
        // East (right)
        if (point.x < width - 1) {
            neighbors.add(GridPoint(point.x + 1, point.y) to GridDirection.EAST)
        }
        
        return neighbors
    }
}
```

**Key Operations:**
- **Coordinate Conversion**: Virtual ↔ Grid coordinates
- **Obstacle Marking**: Blocks cells occupied by devices + clearance
- **Occupancy Tracking**: Per-cell, per-direction tracking
- **Neighbor Generation**: For A* pathfinding

---

## Pathfinding Structures

### AStarNode

Internal node used during A* pathfinding.

```kotlin
private data class AStarNode(
    val point: GridPoint,              // Current position
    val gCost: Float,                  // Cost from start to here
    val hCost: Float,                  // Heuristic cost to goal
    val parent: AStarNode?,            // Previous node in path
    val direction: GridDirection?      // Direction traveled to reach here
) : Comparable<AStarNode> {
    val fCost: Float get() = gCost + hCost  // Total estimated cost
    
    override fun compareTo(other: AStarNode): Int {
        return fCost.compareTo(other.fCost)
    }
}
```

**Why Comparable?**
Used in `PriorityQueue` to always explore lowest-cost nodes first.

**Cost Breakdown:**
- **gCost**: Actual cost accumulated from start
- **hCost**: Manhattan distance estimate to goal
- **fCost**: Total estimated path cost (g + h)

---

### PathResult

Result of A* pathfinding operation.

```kotlin
data class PathResult(
    val waypoints: List<GridPoint>,  // Ordered list of grid points in path
    val success: Boolean,            // Whether valid path was found
    val totalCost: Float,            // Total cost of path
    val crossings: Int               // Number of times path crosses other paths
)
```

**Success = false** indicates routing failure:
- No valid path exists
- Max iterations exceeded
- Start or end blocked

---

## Routing Results

### RoutedConnection

Final routing result for a single connection.

```kotlin
data class RoutedConnection(
    val connectionId: String,                           // Connection identifier
    val waypoints: List<GridPoint>,                     // Path in grid coordinates
    val virtualWaypoints: List<Pair<Float, Float>>,     // Path in virtual coordinates
    val success: Boolean,                               // Routing success/failure
    val crossings: Int                                  // Number of crossings
)
```

**Usage in Rendering:**
- Use `virtualWaypoints` for drawing connection lines
- Convert to screen coordinates using `CoordinateTransformer`
- Apply different styling based on `success` flag

---

## Data Structure Relationships

```
RoutingConfig
     │
     ├─► Used by ────► ConnectionRouter
     │                      │
     │                      ├─► Creates RoutingGrid
     │                      │        │
     │                      │        ├─► Contains GridCell[][]
     │                      │        └─► Tracks blocked[][]
     │                      │
     │                      └─► Creates AStarPathfinder
     └─► Used by ────►              │
                                    ├─► Uses GridPoint
                                    ├─► Uses GridDirection
                                    ├─► Creates AStarNode (internal)
                                    └─► Returns PathResult
                                             │
                                             └─► Converted to RoutedConnection
```

---

## Memory Considerations

### Grid Size Calculation

For a virtual canvas of **10,000 × 10,000** with **gridCellSize = 10**:
- Grid dimensions: 1,000 × 1,000 cells
- Total cells: 1,000,000
- Memory per cell: ~48 bytes (GridCell + boolean)
- Total memory: ~48 MB

### Optimization Opportunities

If memory becomes an issue:
1. Increase `gridCellSize` (coarser grid)
2. Use sparse data structure (only store non-empty cells)
3. Lazy-initialize cells on first access

---

## Thread Safety

**Current Design: Single-threaded**

- All routing calculations happen on main thread
- No concurrent access to grid structures
- Cache is read-only after calculation

**Future Consideration:**
If routing to background thread:
- Make grid structures thread-safe
- Use immutable results
- Consider concurrent pathfinding for multiple connections

---

## Next Steps

- See `ROUTING_ALGORITHMS.md` for pathfinding logic
- See `ROUTING_INTEGRATION.md` for usage examples
