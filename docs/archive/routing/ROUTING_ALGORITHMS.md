# Intelligent Connection Routing - Algorithms

## Overview

This document details the algorithms used in the routing system, primarily the A* pathfinding algorithm with custom cost functions.

---

## A* Pathfinding Algorithm

### Why A*?

A* is optimal for grid-based pathfinding because:
- **Guaranteed shortest path** (with admissible heuristic)
- **Efficient exploration** (guided by heuristic)
- **Widely understood** (well-documented algorithm)
- **Flexible cost function** (can incorporate crossings, turns, etc.)

### Algorithm Overview

```
1. Initialize:
   - Open set = {start node}
   - Closed set = {}
   - gScore[start] = 0
   
2. While open set not empty:
   a. Get node with lowest fCost from open set
   b. If node == goal: reconstruct path and return
   c. Add node to closed set
   d. For each neighbor:
      - Skip if blocked or in closed set
      - Calculate tentative gScore
      - If better than previous: update and add to open set
      
3. If open set empty: no path exists
```

---

## Cost Function

### Base Movement Cost

```kotlin
baseCost = distance × gridMoveCost
```

Where:
- **distance**: Number of grid cells traveled
- **gridMoveCost**: Configurable weight (default: 1.0)

### Crossing Penalty

```kotlin
crossingCost = numberOfCrossings × crossingPenalty
```

Where:
- **numberOfCrossings**: How many existing paths this segment crosses
- **crossingPenalty**: Configurable weight (default: 5.0)

### Total Cost

```kotlin
totalCost = baseCost + crossingCost
```

**Effect:**
- Shorter paths preferred (lower base cost)
- Paths with fewer crossings preferred (lower crossing cost)
- Tunable balance via `crossingPenalty` configuration

---

## Heuristic Function

### Manhattan Distance

```kotlin
fun heuristic(from: GridPoint, to: GridPoint): Float {
    val dx = abs(from.x - to.x)
    val dy = abs(from.y - to.y)
    return (dx + dy).toFloat() × gridMoveCost
}
```

**Why Manhattan?**
- **Admissible**: Never overestimates actual cost
- **Optimal for orthogonal grids**: Exactly matches movement constraints
- **Fast to calculate**: Simple arithmetic

**Example:**
```
From (0, 0) to (10, 5)
Manhattan = |10-0| + |5-0| = 15
Actual path: minimum 15 moves
```

---

## Segment Intersection Detection

### Problem

Determine if two orthogonal line segments intersect.

### Algorithm

```kotlin
fun segmentsIntersect(
    a1: GridPoint, a2: GridPoint,  // Segment A
    b1: GridPoint, b2: GridPoint   // Segment B
): Boolean {
    // Case 1: Horizontal A, Vertical B
    if (a1.y == a2.y && b1.x == b2.x) {
        val aXMin = minOf(a1.x, a2.x)
        val aXMax = maxOf(a1.x, a2.x)
        val bYMin = minOf(b1.y, b2.y)
        val bYMax = maxOf(b1.y, b2.y)
        
        return b1.x in aXMin..aXMax && a1.y in bYMin..bYMax
    }
    
    // Case 2: Vertical A, Horizontal B
    if (a1.x == a2.x && b1.y == b2.y) {
        val aYMin = minOf(a1.y, a2.y)
        val aYMax = maxOf(a1.y, a2.y)
        val bXMin = minOf(b1.x, b2.x)
        val bXMax = maxOf(b1.x, b2.x)
        
        return a1.x in bXMin..bXMax && b1.y in aYMin..aYMax
    }
    
    // Case 3: Parallel segments (both H or both V)
    // Parallel orthogonal segments don't intersect at a point
    return false
}
```

**Visual Example:**

```
Horizontal A: (1,5)─────(7,5)
                    │
Vertical B:       (4,2)
                    │
                  (4,8)

Intersection at (4,5)? YES
- B.x=4 in [1,7] ✓
- A.y=5 in [2,8] ✓
```

---

## Crossing Cost Calculation

### Algorithm

```kotlin
fun calculateCrossingCost(
    from: GridPoint,
    to: GridPoint,
    direction: GridDirection,
    existingPaths: Map<String, List<GridPoint>>
): Float {
    var cost = 0f
    
    // Check against all existing paths
    for ((pathId, waypoints) in existingPaths) {
        // Check each segment in existing path
        for (i in 0 until waypoints.size - 1) {
            val pathFrom = waypoints[i]
            val pathTo = waypoints[i + 1]
            
            // Do segments intersect?
            if (segmentsIntersect(from, to, pathFrom, pathTo)) {
                val existingDirection = getDirection(pathFrom, pathTo)
                val cell = grid.getCell(intersectionPoint)
                
                // Can we occupy this cell in our direction?
                if (cell?.canOccupy(currentConnectionId, direction) == false) {
                    // Cannot share - very high penalty
                    cost += crossingPenalty × 10
                } else {
                    // Can cross - normal penalty
                    cost += crossingPenalty
                }
            }
        }
    }
    
    return cost
}
```

**Penalty Levels:**
1. **No crossing**: 0 cost
2. **Perpendicular crossing**: 1 × crossingPenalty (allowed, discouraged)
3. **Same direction conflict**: 10 × crossingPenalty (strongly discouraged)

---

## Connection Routing Order

### Sorting Strategy

Route connections in order of **straight-line distance** (shortest first):

```kotlin
fun routeConnections(connections: List<Connection>): List<RoutedConnection> {
    // Sort by source-to-target distance
    val sorted = connections.sortedBy { connection ->
        val sourcePos = getPortPosition(connection.sourceDeviceId, connection.sourcePortId)
        val targetPos = getPortPosition(connection.targetDeviceId, connection.targetPortId)
        
        // Euclidean distance
        sqrt(
            (targetPos.x - sourcePos.x).pow(2) + 
            (targetPos.y - sourcePos.y).pow(2)
        )
    }
    
    // Route in order
    return sorted.map { routeConnection(it) }
}
```

**Rationale:**
- **Short connections** get optimal paths (routed first)
- **Long connections** can navigate around existing routes (more flexibility)
- **Overall layout** tends to be cleaner

---

## Path Reconstruction

### Algorithm

```kotlin
fun reconstructPath(endNode: AStarNode): PathResult {
    val path = mutableListOf<GridPoint>()
    var current: AStarNode? = endNode
    
    // Walk backwards from goal to start
    while (current != null) {
        path.add(0, current.point)  // Prepend
        current = current.parent
    }
    
    // Optional: simplify collinear points
    val simplified = if (config.simplifyPath) {
        simplifyCollinear(path)
    } else {
        path
    }
    
    return PathResult(
        waypoints = simplified,
        success = true,
        totalCost = endNode.gCost,
        crossings = countCrossings(simplified, existingPaths)
    )
}
```

### Path Simplification (Optional)

Remove intermediate collinear points:

```kotlin
fun simplifyCollinear(path: List<GridPoint>): List<GridPoint> {
    if (path.size <= 2) return path
    
    val simplified = mutableListOf(path[0])
    
    for (i in 1 until path.size - 1) {
        val prev = path[i - 1]
        val curr = path[i]
        val next = path[i + 1]
        
        // Check if curr is collinear with prev and next
        val isCollinear = 
            (prev.x == curr.x && curr.x == next.x) ||  // Vertical line
            (prev.y == curr.y && curr.y == next.y)     // Horizontal line
        
        if (!isCollinear) {
            simplified.add(curr)
        }
    }
    
    simplified.add(path.last())
    return simplified
}
```

**Note:** Current configuration has `simplifyPath = false` to keep all waypoints.

---

## Device Size Snapping

### Algorithm

Ensure device boundaries align with grid cells:

```kotlin
fun snapDeviceSize(device: Device, gridCellSize: Float): Size {
    return Size(
        width = ceil(device.size.width / gridCellSize) × gridCellSize,
        height = ceil(device.size.height / gridCellSize) × gridCellSize
    )
}
```

**Example:**
```
Grid cell size: 10 units
Original device: 47 × 63
Snapped device:  50 × 70

Calculation:
width:  ceil(47/10) × 10 = 5 × 10 = 50
height: ceil(63/10) × 10 = 7 × 10 = 70
```

**Why?**
- Ensures device edges align with grid boundaries
- Simplifies clearance calculations
- Improves path quality (cleaner routing)

---

## Port Extension Calculation

### Algorithm

Extend connection straight from port before routing begins:

```kotlin
fun calculateExtendedPortPoint(
    device: Device,
    port: Port,
    portPos: Pair<Float, Float>,
    extension: Float
): Pair<Float, Float> {
    // Follow existing port extension behavior
    // Always extend left or right (horizontal)
    
    val direction = when (port.side) {
        PortSide.LEFT -> -1f    // Extend left
        PortSide.RIGHT -> 1f    // Extend right
        PortSide.TOP -> 1f      // Extend right (following existing code)
        PortSide.BOTTOM -> 1f   // Extend right (following existing code)
    }
    
    return Pair(
        portPos.first + (extension × direction),
        portPos.second
    )
}
```

**Follows Existing Behavior:**
- Extension direction matches current implementation
- No changes to existing port logic
- Routing begins from extended point

---

## Failed Route Handling

### Algorithm

When A* cannot find valid path:

```kotlin
fun handleFailedRoute(
    connectionId: String,
    startPoint: Pair<Float, Float>,
    endPoint: Pair<Float, Float>
): RoutedConnection {
    return RoutedConnection(
        connectionId = connectionId,
        waypoints = listOf(
            grid.toGridPoint(startPoint.first, startPoint.second),
            grid.toGridPoint(endPoint.first, endPoint.second)
        ),
        virtualWaypoints = listOf(startPoint, endPoint),
        success = false,  // Mark as failed
        crossings = 0
    )
}
```

**Rendering:**
- Draw straight line from start to end
- Use `failedRouteColor` (orange-red)
- Apply `failedRouteWidthMultiplier` (1.5×)
- Visually distinct from successful routes

---

## Performance Optimizations

### 1. Early Termination

```kotlin
while (openSet.isNotEmpty() && iterations < maxIterations) {
    val current = openSet.poll()
    
    if (current.point == goal) {
        return reconstructPath(current)  // Stop immediately
    }
    // ...
}
```

### 2. Closed Set Pruning

```kotlin
for (neighbor in getNeighbors(current)) {
    if (neighbor in closedSet) continue  // Skip already evaluated
    // ...
}
```

### 3. Priority Queue

```kotlin
val openSet = PriorityQueue<AStarNode>()  // Always explores lowest cost first
```

### 4. Heuristic Guidance

Manhattan distance guides search toward goal, avoiding unnecessary exploration.

---

## Time Complexity Analysis

### Single Path

**Best Case**: O(d) where d = straight-line distance
- Direct path exists with no obstacles

**Average Case**: O(d × log(m)) where m = grid cells explored
- Priority queue operations dominate

**Worst Case**: O(n × log(n)) where n = total grid cells
- Must explore entire grid (no path exists)

### All Connections

For c connections:
- **Sequential routing**: O(c × average_path_complexity)
- **Typical**: c=20, average_path=100 cells → ~2000 operations
- **Fast**: < 100ms on modern hardware

---

## Space Complexity

- **Grid**: O(width × height) - fixed
- **Open set**: O(m) where m = cells in search frontier
- **Closed set**: O(m) where m = explored cells
- **Path**: O(d) where d = path length

**Total**: O(width × height + m) - grid dominates

---

## Algorithm Correctness

### Guarantees

1. **Completeness**: If path exists, A* will find it
2. **Optimality**: With admissible heuristic, finds shortest path
3. **Termination**: Max iterations prevents infinite loops

### Edge Cases Handled

- ✅ Start == End (returns empty path)
- ✅ Start or End blocked (returns failure)
- ✅ No path exists (returns failure after exhaustive search)
- ✅ Multiple paths (returns optimal)

---

## Next Steps

- See `ROUTING_INTEGRATION.md` for implementation guide
- See `ROUTING_DATA_STRUCTURES.md` for data model details
