# Manual Path Routing Implementation Plan

## Overview

This document outlines the implementation plan for supporting manually defined routing paths in the workstation.json file. This allows users to manually adjust path routing when automatic routing doesn't produce desired results.

## Current State

### Data Model
- ✅ `Connection` model already has `routingPoints: List<Point>?` field
- ✅ `Point` has `x: Float, y: Float` (virtual coordinates)
- ❌ JSON connections don't include `routingPoints` field
- ❌ Router doesn't check for or use manual routing points

### Routing System
- Uses `ConnectorPathRouter` for automatic pathfinding
- Converts between virtual coordinates (Float) and grid coordinates (GridPoint)
- Applies port extensions to connect ports to routing paths
- Performs multiple optimization passes (orthogonal enforcement, zigzag removal, etc.)

## Implementation Plan

### Phase 1: JSON Schema Support

#### 1.1 Update JSON Structure
Add optional `routingPoints` array to connection objects in `workstation.json`:

```json
{
  "id": "conn-1",
  "sourceDeviceId": "office-laptop",
  "sourcePortId": "office-laptop-thunderbolt",
  "targetDeviceId": "usb-docking-station",
  "targetPortId": "dock-thunderbolt-in-1",
  "routingPoints": [
    { "x": 350.0, "y": 800.0 },
    { "x": 350.0, "y": 700.0 },
    { "x": 500.0, "y": 700.0 }
  ]
}
```

**Specifications:**
- `routingPoints` is optional (can be omitted or null)
- If present, must be a non-empty array of `Point` objects
- Points are in virtual coordinates (same coordinate system as device positions)
- Points represent waypoints between source and target ports
- First point should connect to source port extension end
- Last point should connect to target port extension end

#### 1.2 Validation Rules
- If `routingPoints` is provided, it must have at least 1 point
- Points must be within canvas bounds (0 to canvasSize.width/height)
- Points should form a reasonable path (not too far from source/target)

### Phase 2: Router Integration

#### 2.1 Detection Logic
Modify `ConnectorPathRouter.routeConnections()` to:
1. Check if `connection.routingPoints` exists and is non-empty
2. If manual points exist, use manual routing path
3. If manual points don't exist, use automatic routing (current behavior)

#### 2.2 Manual Path Processing
Create new function `processManualPath()` that:
1. Converts virtual coordinates to grid coordinates
2. Validates path points are reachable
3. Ensures orthogonal path (if needed)
4. Connects port extensions to manual path
5. Builds full path including port positions and extensions

**Key Considerations:**
- Port extensions are still calculated and used
- Manual path connects from `sourceExtension.endPos` to `targetExtension.endPos`
- Manual routing points are intermediate waypoints
- Path should be orthogonal (90-degree turns only)

#### 2.3 Path Construction Flow

For manual paths:
```
[sourcePort.gridPos] 
  → [sourceExtension.startPos] 
  → [sourceExtension.endPos] 
  → [manual routingPoints converted to grid] 
  → [targetExtension.endPos] 
  → [targetExtension.startPos] 
  → [targetPort.gridPos]
```

#### 2.4 Validation & Optimization

**Required:**
- Ensure path is orthogonal (add intermediate points if needed)
- Validate points are not blocked by obstacles
- Connect properly to port extensions

**Optional (can be configurable):**
- Remove redundant points (collinear points)
- Smooth path transitions
- Validate minimum spacing from other paths

### Phase 3: Implementation Details

#### 3.1 New Functions in ConnectorPathRouter

```kotlin
private fun hasManualRouting(connection: Connection): Boolean {
    return connection.routingPoints != null && connection.routingPoints.isNotEmpty()
}

private fun processManualPath(
    connection: Connection,
    sourceExtension: PortExtension,
    targetExtension: PortExtension,
    sourcePort: PortPosition,
    targetPort: PortPosition,
    grid: RoutingGrid
): RoutedConnection {
    // Convert virtual points to grid points
    // Ensure orthogonal path
    // Connect port extensions
    // Build full path
    // Return RoutedConnection
}

private fun convertManualPointsToGrid(
    routingPoints: List<Point>,
    grid: RoutingGrid
): List<GridPoint> {
    return routingPoints.map { point ->
        grid.toGridPoint(point.x, point.y)
    }
}

private fun ensureManualPathOrthogonal(
    waypoints: List<GridPoint>
): List<GridPoint> {
    // Similar to ensureOrthogonal but for manual paths
    // Add intermediate points to make path orthogonal
}
```

#### 3.2 Modified routeConnections() Flow

```kotlin
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
        
        // NEW: Check for manual routing
        if (hasManualRouting(connection)) {
            val routedConnection = processManualPath(
                connection,
                sourceExtension,
                targetExtension,
                sourcePort,
                targetPort,
                grid
            )
            routedConnections.add(routedConnection)
            routedPaths[connection.id] = routedConnection.waypoints
            return@forEachIndexed
        }
        
        // Existing automatic routing logic...
    }
}
```

### Phase 4: Edge Cases & Error Handling

#### 4.1 Invalid Manual Paths
- Empty routingPoints array → Fall back to automatic routing
- Points outside canvas bounds → Clamp to bounds or fall back
- Path blocked by obstacles → Fall back to automatic routing (with warning)
- Path doesn't connect properly → Add connecting segments

#### 4.2 Partial Manual Paths
- If only some connections have manual paths, others use automatic routing
- Manual paths still participate in spacing calculations with automatic paths

#### 4.3 Port Extension Connection
- Manual path must connect to port extension ends
- If first/last manual point is far from extension end, add connecting segment
- Ensure orthogonal connection

### Phase 5: User Experience

#### 5.1 JSON Editing
- Users can manually add `routingPoints` to connections
- Points are in virtual coordinates (same as device positions)
- Easy to copy coordinates from device positions or other paths

#### 5.2 Visualization
- Manual paths are rendered the same as automatic paths
- No visual distinction (unless needed for debugging)

#### 5.3 Fallback Behavior
- If manual path fails validation, fall back to automatic routing
- Log warning for debugging purposes

## Implementation Steps

### Step 1: Add Manual Path Detection
- [ ] Add `hasManualRouting()` function
- [ ] Modify `routeConnections()` to check for manual paths
- [ ] Add early return for manual paths (temporary stub)

### Step 2: Implement Manual Path Processing
- [ ] Add `convertManualPointsToGrid()` function
- [ ] Add `ensureManualPathOrthogonal()` function
- [ ] Add `processManualPath()` function
- [ ] Connect port extensions properly

### Step 3: Path Construction
- [ ] Build full path with port positions
- [ ] Build virtual waypoints for rendering
- [ ] Calculate crossings count
- [ ] Return proper `RoutedConnection`

### Step 4: Validation & Error Handling
- [ ] Validate points are within bounds
- [ ] Check for blocked paths
- [ ] Add fallback to automatic routing
- [ ] Add logging for debugging

### Step 5: Testing
- [ ] Test with manual paths in JSON
- [ ] Test with mixed manual/automatic paths
- [ ] Test edge cases (invalid points, blocked paths)
- [ ] Test port extension connections

### Step 6: Documentation
- [ ] Update JSON schema documentation
- [ ] Add examples to workstation.json
- [ ] Document coordinate system and best practices

## Example JSON Structure

```json
{
  "connections": [
    {
      "id": "conn-1",
      "sourceDeviceId": "office-laptop",
      "sourcePortId": "office-laptop-thunderbolt",
      "targetDeviceId": "usb-docking-station",
      "targetPortId": "dock-thunderbolt-in-1",
      "routingPoints": [
        { "x": 350.0, "y": 800.0 },
        { "x": 350.0, "y": 700.0 },
        { "x": 500.0, "y": 700.0 }
      ]
    },
    {
      "id": "conn-2",
      "sourceDeviceId": "personal-laptop",
      "sourcePortId": "personal-laptop-thunderbolt",
      "targetDeviceId": "usb-docking-station",
      "targetPortId": "dock-thunderbolt-in-2"
      // No routingPoints - uses automatic routing
    }
  ]
}
```

## Coordinate System Notes

- **Virtual Coordinates**: Used in JSON and UI rendering
  - Device positions: `{ x: 100, y: 800 }`
  - Manual routing points: `{ x: 350.0, y: 700.0 }`
  
- **Grid Coordinates**: Used internally for routing
  - Converted from virtual: `grid.toGridPoint(virtualX, virtualY)`
  - Cell-based: `GridPoint(x: Int, y: Int)`
  - Cell size determined by `RoutingGrid.cellSize`

- **Conversion**: 
  - Virtual → Grid: `(virtualX / cellSize).toInt()`
  - Grid → Virtual: `gridPoint.x * cellSize + cellSize / 2f`

## Benefits

1. **Flexibility**: Users can fine-tune paths that automatic routing struggles with
2. **Backward Compatible**: Existing JSON files work without changes
3. **Gradual Adoption**: Can add manual paths incrementally
4. **Fallback Safety**: Invalid manual paths fall back to automatic routing
5. **Same Rendering**: Manual paths use same rendering pipeline

## Future Enhancements

1. **Path Editor UI**: Visual editor for adjusting paths
2. **Path Validation**: Real-time validation feedback
3. **Path Optimization**: Optional optimization of manual paths
4. **Path Templates**: Common routing patterns
5. **Export/Import**: Export manual paths for reuse

