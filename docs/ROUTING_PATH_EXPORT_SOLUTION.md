# Solution: Export Routing Paths from Runtime to JSON

## Problem

You want to extract the routing paths generated at runtime by the automatic pathfinding system and add them as `routingPoints` to the `workstation.json` file, so you can manually adjust them later.

## Solution

I've created a `PathExporter` utility that extracts intermediate routing points from `RoutedConnection` results and updates the `WorkstationLayout` with `routingPoints` in the connections.

## Files Created

### 1. `PathExporter.kt`
**Location**: `viewer/src/commonMain/kotlin/dev/akexorcist/workstation/routing/PathExporter.kt`

**Key Functions**:
- `extractRoutingPoints()` - Extracts intermediate routing points from a RoutedConnection
- `updateLayoutWithRoutingPoints()` - Updates layout connections with routing points
- `exportToJson()` - Exports layout to JSON string
- `exportToPrettyJson()` - Exports layout to pretty-printed JSON

### 2. `WorkstationViewModel` Extensions
**Location**: `viewer/src/commonMain/kotlin/dev/akexorcist/workstation/presentation/WorkstationViewModel.kt`

**New Functions**:
- `exportLayoutWithRoutingPoints()` - Exports current layout with all routing points
- `getRoutingPointsForConnection()` - Gets routing points for a specific connection

### 3. Documentation
- `docs/EXPORT_ROUTING_PATHS.md` - Complete usage guide
- `docs/ROUTING_PATH_EXPORT_SOLUTION.md` - This file

## How It Works

### Path Structure Understanding

When a connection is routed, `RoutedConnection.virtualWaypoints` contains:

```
[0] = sourcePort.virtualPos          ← Port position (skip)
[1] = sourceExtension.startPos        ← Extension start (skip)
[2..n-2] = optimizedPath points     ← Intermediate routing points (extract these)
[n-1] = targetExtension.endPos       ← Extension end (skip)
[n] = targetPort.virtualPos          ← Port position (skip)
```

The `PathExporter` extracts only indices 2 to n-2 (the intermediate routing points).

### Usage Example

```kotlin
// After routing is complete in your ViewModel
val layout = uiState.layout
val routedConnections = uiState.routedConnectionMap

// Export to JSON
val jsonString = PathExporter.exportToPrettyJson(layout, routedConnections)
println(jsonString)

// Or use ViewModel function
val jsonString = viewModel.exportLayoutWithRoutingPoints(prettyPrint = true)
```

### Output Format

**Before** (automatic routing):
```json
{
  "id": "conn-1",
  "sourceDeviceId": "office-laptop",
  "sourcePortId": "office-laptop-thunderbolt",
  "targetDeviceId": "usb-docking-station",
  "targetPortId": "dock-thunderbolt-in-1"
}
```

**After** (with routingPoints):
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

## Workflow

### Step 1: Run Application
1. Load `workstation.json` in the application
2. Let automatic routing generate paths
3. Verify paths look correct in the UI

### Step 2: Export Paths

**Option A: Using ViewModel (Recommended)**
```kotlin
// In browser console or debugger
val json = viewModel.exportLayoutWithRoutingPoints(true)
console.log(json)
```

**Option B: Direct PathExporter**
```kotlin
import dev.akexorcist.workstation.routing.PathExporter

val layout = uiState.layout
val routedConnections = uiState.routedConnectionMap
val json = PathExporter.exportToPrettyJson(layout, routedConnections)
```

### Step 3: Update JSON File
1. Copy the exported JSON
2. Replace the `connections` array in `workstation.json`
3. Or manually merge `routingPoints` into existing connections

### Step 4: Reload and Adjust
1. Reload the application
2. The router will detect `routingPoints` and use them (once manual routing is implemented)
3. You can now manually adjust points in JSON

## Key Features

✅ **Extracts Only Intermediate Points**: Skips port positions and extension points  
✅ **Handles Failed Routes**: Only exports successfully routed connections  
✅ **Pretty Printing**: Optional formatted JSON output  
✅ **Backward Compatible**: Doesn't break existing JSON structure  
✅ **Easy to Use**: Simple function calls from ViewModel  

## Integration with Manual Routing

Once you implement manual routing support (from `MANUAL_PATH_ROUTING_IMPLEMENTATION_PLAN.md`), the exported `routingPoints` will be automatically used instead of automatic routing.

## Testing

To test the export:

```kotlin
// Check if routing points were extracted
val points = viewModel.getRoutingPointsForConnection("conn-1")
println("Points: ${points?.size}")

// Export and verify JSON
val json = viewModel.exportLayoutWithRoutingPoints()
println(json)
```

## Next Steps

1. **Test Export**: Run the application and test `exportLayoutWithRoutingPoints()`
2. **Verify Output**: Check that exported JSON has correct `routingPoints` structure
3. **Update JSON**: Copy exported paths to `workstation.json`
4. **Implement Manual Routing**: Complete the manual routing implementation from the plan
5. **Test Round-Trip**: Verify that exported paths are correctly loaded and used

## Notes

- **Empty Paths**: Very short paths (< 5 waypoints) won't have routingPoints exported
- **Coordinate System**: All points are in virtual coordinates (same as device positions)
- **Port Extensions**: Extension points are calculated automatically, not included in routingPoints
- **Format**: Points are exported as `{ "x": Float, "y": Float }` matching the `Point` data class

