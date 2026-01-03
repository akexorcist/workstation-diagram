# Export Routing Paths to JSON

This document explains how to export routing paths from runtime results back to the `workstation.json` file.

## Overview

The routing system generates paths at runtime using automatic pathfinding. The `PathExporter` utility allows you to extract these generated paths and add them as `routingPoints` to connections in the JSON file, enabling manual path adjustment.

## How It Works

### Path Structure

When a connection is routed, the `RoutedConnection` contains `virtualWaypoints` with this structure:

```
[0] = sourcePort.virtualPos          (port position)
[1] = sourceExtension.startPos       (port extension start)
[2..n-2] = optimizedPath points      (intermediate routing points) ← These are extracted
[n-1] = targetExtension.endPos       (port extension end)
[n] = targetPort.virtualPos          (port position)
```

The `PathExporter` extracts only the intermediate routing points (indices 2 to n-2), which are the actual path waypoints between the port extensions.

## Usage

### Method 1: Export Entire Layout

Export all routing paths from the current layout:

```kotlin
// In WorkstationViewModel or similar
val jsonString = viewModel.exportLayoutWithRoutingPoints(prettyPrint = true)
// jsonString contains the full JSON with routingPoints added to connections
```

### Method 2: Export Specific Connection

Get routing points for a single connection:

```kotlin
val routingPoints = viewModel.getRoutingPointsForConnection("conn-1")
// Returns List<Point>? with intermediate routing points
```

### Method 3: Direct PathExporter Usage

Use `PathExporter` directly:

```kotlin
import dev.akexorcist.workstation.routing.PathExporter

// Get layout and routed connections
val layout = uiState.layout
val routedConnections = uiState.routedConnectionMap

// Export to JSON
val jsonString = PathExporter.exportToPrettyJson(layout, routedConnections)

// Or get routing points for a specific connection
val routedConnection = routedConnections["conn-1"]
val routingPoints = routedConnection?.let { PathExporter.extractRoutingPoints(it) }
```

## Example Output

Before export (automatic routing):
```json
{
  "id": "conn-1",
  "sourceDeviceId": "office-laptop",
  "sourcePortId": "office-laptop-thunderbolt",
  "targetDeviceId": "usb-docking-station",
  "targetPortId": "dock-thunderbolt-in-1"
}
```

After export (with routingPoints):
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

## Implementation Details

### PathExporter Functions

1. **`extractRoutingPoints(routedConnection: RoutedConnection): List<Point>`**
   - Extracts intermediate routing points from a RoutedConnection
   - Returns empty list if path is too short (< 5 points)
   - Skips port positions and extension points

2. **`updateLayoutWithRoutingPoints(layout, routedConnections): WorkstationLayout`**
   - Updates all connections in layout with routing points
   - Only updates successfully routed connections
   - Skips connections with empty routing points

3. **`exportToJson(layout, routedConnections): String`**
   - Exports layout to compact JSON string

4. **`exportToPrettyJson(layout, routedConnections): String`**
   - Exports layout to pretty-printed JSON with indentation

### WorkstationViewModel Functions

1. **`exportLayoutWithRoutingPoints(prettyPrint: Boolean = true): String?`**
   - Exports current layout with all routing points
   - Returns null if layout or routing not available

2. **`getRoutingPointsForConnection(connectionId: String): List<Point>?`**
   - Gets routing points for a specific connection
   - Returns null if connection not found or routing failed

## Workflow

### Step 1: Run Application
1. Load the workstation layout
2. Let the routing system generate paths automatically
3. Verify paths look correct in the UI

### Step 2: Export Paths
```kotlin
// In browser console or debug tool
val json = viewModel.exportLayoutWithRoutingPoints()
console.log(json)
```

### Step 3: Update JSON File
1. Copy the exported JSON
2. Replace the connections section in `workstation.json`
3. Or merge routingPoints into existing connections

### Step 4: Reload
1. Reload the application
2. The router will detect `routingPoints` and use them instead of automatic routing
3. You can now manually adjust the points in JSON

## Notes

- **Empty Paths**: Connections with very short paths (< 5 waypoints) won't have routingPoints exported
- **Failed Routes**: Connections that failed to route won't have routingPoints added
- **Port Extensions**: Port extension points are not included in routingPoints (they're calculated automatically)
- **Coordinate System**: All points are in virtual coordinates (same as device positions)

## Debugging

To check if routing points were extracted correctly:

```kotlin
// Check routing points for a connection
val points = viewModel.getRoutingPointsForConnection("conn-1")
println("Routing points: ${points?.size} points")
points?.forEach { println("  (${it.x}, ${it.y})") }

// Check if connection has manual routing
val connection = layout.connections.find { it.id == "conn-1" }
println("Has routingPoints: ${connection?.routingPoints != null}")
println("Points count: ${connection?.routingPoints?.size ?: 0}")
```

## Future Enhancements

- **UI Export Button**: Add a button in the UI to export paths
- **Selective Export**: Export only specific connections
- **Path Validation**: Validate exported paths before saving
- **Diff View**: Show differences between automatic and manual paths
- **Path Editor**: Visual editor for adjusting paths directly in UI

