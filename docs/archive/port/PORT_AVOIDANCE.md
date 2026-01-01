# Intelligent Routing: Port Avoidance Feature

## Overview

This document details the implementation of the Port Avoidance feature in the intelligent routing system. This feature ensures that connection paths avoid overlapping with all ports in the diagram, not just the source and target ports of the connection.

**Date**: January 3, 2026  
**Status**: Implemented  
**Related Files**: GridSystem.kt, ConnectionRouter.kt, RoutingConfig.kt

---

## Feature Description

The Port Avoidance feature enhances the intelligent routing system by treating all ports in the diagram as obstacles, similar to how devices are treated. This ensures that connector paths never overlap or pass through ports, creating a cleaner and more readable diagram.

Key benefits:
- Prevents visual confusion when paths cross over ports
- Maintains clean separation between connections and ports
- Improves overall diagram readability
- Works consistently across all modules and device types

---

## Implementation Approach

### 1. Enhanced Port Obstacle Marking

Each port in the diagram is marked as an obstacle with an enlarged circular clearance zone:

```kotlin
fun markPortObstacle(portX: Float, portY: Float, clearance: Float) {
    val radius = clearance / cellSize
    val centerX = (portX / cellSize).toInt()
    val centerY = (portY / cellSize).toInt()

    // Add 3 to create a larger blocking zone
    val radiusInt = radius.toInt() + 3
    
    val x1 = (centerX - radiusInt).coerceAtLeast(0)
    val y1 = (centerY - radiusInt).coerceAtLeast(0)
    val x2 = (centerX + radiusInt).coerceAtMost(width - 1)
    val y2 = (centerY + radiusInt).coerceAtMost(height - 1)
    
    // For ports, use a larger effective radius with a buffer
    val effectiveRadius = radius * 1.2f
    
    for (x in x1..x2) {
        for (y in y1..y2) {
            val dx = x - centerX
            val dy = y - centerY
            val distance = kotlin.math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
            
            if (distance <= effectiveRadius) {
                blocked[x][y] = true
            }
        }
    }
}
```

This creates circular obstacle zones around each port, ensuring connections route around them.

### 2. Enhanced Temporary Grid with Port-Specific Clearance

For each connection, we create a temporary grid that:
1. Includes all device and port obstacles from the main grid
2. Applies larger clearance for ports on the same device side as the source/target
3. Explicitly unblocks the source and target ports of the current connection
4. Uses this temporary grid for pathfinding

```kotlin
// Create temporary grid for this connection that allows source and target ports to be connected
val tempGrid = RoutingGrid(grid.width, grid.height, grid.cellSize)

// Copy blocked cells from the original grid, except for source and target port positions
for (x in 0 until grid.width) {
    for (y in 0 until grid.height) {
        val point = GridPoint(x, y)
        if (grid.isBlocked(point)) {
            // Skip blocking if this is the source or target port
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
                    0f
                )
            }
        }
    }
}
```

### 3. Enhanced Configuration Parameters

Several configuration parameters now work together to improve port avoidance:

```kotlin
object RoutingConfig {
    // Other parameters...
    var portClearance: Float = 35f  // Increased minimum distance from ports
    var turnPenalty: Float = 10.0f  // Higher penalty to discourage turns near ports
    var minPathSpacing: Float = 25f  // Wider spacing between paths
    var pathRepulsionFactor: Float = 6f  // Stronger repulsion from existing paths
    // Other parameters...
}
```

---

## Usage

The port avoidance feature is automatically applied when routing connections. No additional action is required by the user, but the clearance radius can be adjusted as needed:

```kotlin
// Adjust port avoidance settings
RoutingConfig.portClearance = 35f  // Increased port clearance radius
RoutingConfig.turnPenalty = 10.0f  // Higher penalty for turns near ports
RoutingConfig.minPathSpacing = 25f  // Wider spacing between paths
RoutingConfig.pathRepulsionFactor = 6f  // Stronger path repulsion
```

---

## Technical Details

### Port Detection Algorithm

1. For each device in the diagram:
   - Retrieve all ports
   - Calculate the exact position of each port
   - Mark a circular area around each port as blocked

### Temporary Grid Mechanism

1. When routing a specific connection:
   - Create a temporary grid copying all obstacles from the main grid
   - Unblock cells near the source and target ports for the current connection
   - Use this temporary grid for A* pathfinding

### Performance Considerations

- Port obstacle marking has O(n × p) complexity where:
  - n = number of devices
  - p = average number of ports per device
- The clearance radius determines how many grid cells are checked per port
- Overall impact on performance is minimal compared to the pathfinding algorithm

---

## Edge Cases and Considerations

### 1. Densely Packed Ports

**Issue**: When ports are placed very close together, their blocked areas might overlap and create large no-go zones.

**Solution**:
- The algorithm still finds paths through available spaces
- In extreme cases, prioritizes going around the entire device

### 2. Source and Target Port Access

**Issue**: If all ports are treated as obstacles, the algorithm can't reach the source/target ports.

**Solution**:
- Special handling creates a temporary grid for each connection
- Explicitly unblocks the source and target ports of the current connection
- Ensures connections can still start/end at ports

### 3. Path Complexity

**Issue**: Avoiding all ports might create more complex paths with additional turns.

**Solution**:
- The A* algorithm still optimizes for minimal turns and path length
- May slightly increase path complexity but significantly improves diagram readability

---

## Success Criteria

- ✅ No connection path overlaps with any port in the diagram
- ✅ Connections can still reach their source and target ports
- ✅ Minimal impact on routing performance
- ✅ Clearance radius is configurable via RoutingConfig
- ✅ Works with existing path simplification algorithms

---

**Status**: Implemented and Tested  
**Impact**: High - Significant improvement in diagram readability