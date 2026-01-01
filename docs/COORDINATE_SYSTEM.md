# Coordinate System Design

## Problem Statement

The current implementation uses absolute pixel coordinates for device positioning, which has several limitations:

### Current Issues
1. **Not responsive**: Fixed pixel positions don't adapt to different screen sizes
2. **Not scalable**: Doesn't work well on hi-res displays or when window is resized
3. **Canvas size dependency**: Tied to hardcoded `1920x1080` canvas size
4. **Platform limitation**: Same data file can't work across web, desktop, and mobile

### Example Current Data
```json
{
  "position": { "x": 100, "y": 200 },
  "size": { "width": 200, "height": 150 }
}
```

## Proposed Solution: Virtual Coordinate System

### Concept
Use **virtual units** on a fixed virtual canvas (e.g., 10000x10000) that gets scaled to the actual screen size. Think of it like:
- **Android**: density-independent pixels (dp)
- **iOS**: points
- **Web**: viewport units
- **Game engines**: world coordinates

### Benefits
1. ✅ **Resolution independent**: Works on any screen size
2. ✅ **Responsive**: Automatically scales to window resize
3. ✅ **Platform agnostic**: Same data works on web, desktop, mobile
4. ✅ **Designer friendly**: Use familiar coordinate system (like designing in Figma/Sketch)
5. ✅ **Maintains aspect ratio**: Virtual units preserve intended proportions
6. ✅ **Easy to reason about**: Natural units (100 units = small device, 1000 units = large spacing)

## Implementation Approaches

### Approach 1: Virtual Canvas System (Recommended)

**Data Format**:
```json
{
  "metadata": {
    "title": "Sample Workstation",
    "coordinateSystem": "virtual",  // NEW: Specify coordinate system
    "virtualCanvas": {
      "width": 10000,   // Virtual canvas size
      "height": 10000
    },
    "version": "2.0"
  },
  "devices": [
    {
      "id": "laptop-main",
      "position": {
        "x": 1000,     // 1000 virtual units from left
        "y": 2000      // 2000 virtual units from top
      },
      "size": {
        "width": 800,   // 800 virtual units wide
        "height": 600   // 600 virtual units tall
      }
    }
  ]
}
```

**Why 10000x10000 Virtual Canvas?**
- Large enough for fine-grained positioning
- Easy mental math (1000 units = 10% of canvas)
- Common in design tools
- No floating point precision issues

**Conversion at Runtime**:
```kotlin
fun virtualToScreen(
    virtualCoord: Float,
    virtualCanvasSize: Float,
    actualCanvasSize: Float,
    zoom: Float
): Float {
    // Scale from virtual space to actual screen space
    val scale = actualCanvasSize / virtualCanvasSize
    return virtualCoord * scale * zoom
}

// Example usage
val virtualCanvas = layout.metadata.virtualCanvas ?: Size(10000f, 10000f)
val scaleX = canvasWidth / virtualCanvas.width
val scaleY = canvasHeight / virtualCanvas.height

val screenX = device.position.x * scaleX * uiState.zoom + uiState.panOffset.x
val screenY = device.position.y * scaleY * uiState.zoom + uiState.panOffset.y
```

**Centering Calculation**:
```kotlin
// Calculate center of visible area (accounting for UI overlays)
val visibleWidth = windowWidth - SIDEBAR_WIDTH
val visibleHeight = windowHeight - CONTROL_PANEL_HEIGHT

// To center a device at (0.5, 0.5):
val centerX = (0.5 * canvasWidth * zoom) - (visibleWidth / 2) + (SIDEBAR_WIDTH / 2)
val centerY = (0.5 * canvasHeight * zoom) - (visibleHeight / 2) + (CONTROL_PANEL_HEIGHT / 2)
```

### Approach 2: Hybrid System (Backward Compatible)

Support both absolute pixels and virtual coordinates with automatic detection.

**Data Format**:
```json
{
  "metadata": {
    "coordinateSystem": "absolute"  // or "virtual"
    // If "virtual", virtualCanvas is required
  }
}
```

**Detection Logic**:
```kotlin
enum class CoordinateSystem {
    ABSOLUTE,   // Legacy: direct pixels (1920x1080 canvas)
    VIRTUAL     // New: virtual units (10000x10000 canvas)
}

fun detectCoordinateSystem(metadata: Metadata): CoordinateSystem {
    return when {
        metadata.coordinateSystem == "virtual" && metadata.virtualCanvas != null -> 
            CoordinateSystem.VIRTUAL
        else -> 
            CoordinateSystem.ABSOLUTE
    }
}
```

### Approach 3: Adaptive Virtual Canvas

Automatically determine virtual canvas size from content bounds.

**Concept**: 
- Read device positions from file
- Calculate bounding box
- Use bounding box as virtual canvas
- Auto-scale to any screen

**Not Recommended**: Loses explicit control over layout proportions

## Recommended Implementation

### Phase 1: Add Normalized Support
1. Add `coordinateSystem` field to metadata
2. Support both systems (backward compatible)
3. Convert coordinates at load time

### Phase 2: Update Sample Data
1. Create new sample files with normalized coordinates
2. Keep legacy files for backward compatibility
3. Document migration guide

### Phase 3: Center Calculation Fix
1. Calculate actual visible canvas area (minus UI overlays)
2. Find bounding box of all devices
3. Calculate pan offset to center the bounding box

## Centering Algorithm

```kotlin
data class BoundingBox(
    val minX: Float,
    val minY: Float,
    val maxX: Float,
    val maxY: Float
) {
    val centerX: Float get() = (minX + maxX) / 2
    val centerY: Float get() = (minY + maxY) / 2
    val width: Float get() = maxX - minX
    val height: Float get() = maxY - minY
}

fun calculateDevicesBoundingBox(
    devices: List<Device>,
    zoom: Float
): BoundingBox {
    val minX = devices.minOf { it.position.x * zoom }
    val minY = devices.minOf { it.position.y * zoom }
    val maxX = devices.maxOf { (it.position.x + it.size.width) * zoom }
    val maxY = devices.maxOf { (it.position.y + it.size.height) * zoom }
    
    return BoundingBox(minX, minY, maxX, maxY)
}

fun calculateCenterPan(
    devicesBoundingBox: BoundingBox,
    visibleWidth: Float,
    visibleHeight: Float,
    sidebarWidth: Float = 300f,
    controlPanelHeight: Float = 60f
): Offset {
    // Calculate center of visible area (accounting for UI overlays)
    val visibleCenterX = (visibleWidth - sidebarWidth) / 2 + sidebarWidth
    val visibleCenterY = (visibleHeight - controlPanelHeight) / 2 + controlPanelHeight
    
    // Calculate pan offset to center devices bounding box
    val panX = visibleCenterX - devicesBoundingBox.centerX
    val panY = visibleCenterY - devicesBoundingBox.centerY
    
    return Offset(panX, panY)
}

fun resetToCenter() {
    val layout = uiState.layout ?: return
    val boundingBox = calculateDevicesBoundingBox(layout.devices, uiState.zoom)
    
    // Get actual window/canvas size
    val visibleWidth = canvasWidth
    val visibleHeight = canvasHeight
    
    val centerPan = calculateCenterPan(
        boundingBox,
        visibleWidth,
        visibleHeight
    )
    
    handlePanChange(centerPan)
}
```

## Migration Path

### For Existing Files
```kotlin
object CoordinateConverter {
    fun convertAbsoluteToNormalized(
        absolutePos: Float,
        canvasSize: Float
    ): Float {
        return absolutePos / canvasSize
    }
    
    fun migrateWorkstation(
        workstation: Workstation,
        canvasSize: Size
    ): Workstation {
        return workstation.copy(
            metadata = workstation.metadata.copy(
                coordinateSystem = "normalized"
            ),
            devices = workstation.devices.map { device ->
                device.copy(
                    position = Position(
                        x = convertAbsoluteToNormalized(device.position.x, canvasSize.width),
                        y = convertAbsoluteToNormalized(device.position.y, canvasSize.height)
                    ),
                    size = Size(
                        width = convertAbsoluteToNormalized(device.size.width, canvasSize.width),
                        height = convertAbsoluteToNormalized(device.size.height, canvasSize.height)
                    )
                )
            }
        )
    }
}
```

## Example Comparison

### Before (Absolute - 1920x1080 canvas)
```json
{
  "metadata": {
    "canvasSize": { "width": 1920, "height": 1080 }
  },
  "devices": [{
    "position": { "x": 100, "y": 200 },
    "size": { "width": 200, "height": 150 }
  }]
}
```
- **On 1080p display**: Works as designed
- **On 4K display**: Device appears tiny in corner
- **On mobile (800x600)**: Device might be off-screen
- **After resize**: Layout breaks completely

### After (Virtual - 10000x10000 canvas)
```json
{
  "metadata": {
    "coordinateSystem": "virtual",
    "virtualCanvas": { "width": 10000, "height": 10000 }
  },
  "devices": [{
    "position": { "x": 1000, "y": 2000 },    // Same relative position
    "size": { "width": 2000, "height": 1500 } // Same relative size
  }]
}
```
- **On any display**: Device maintains relative position (10% from left, 20% from top)
- **On 4K (3840x2160)**: Scales beautifully, device is 384x324 pixels
- **On mobile (800x600)**: Scales down, device is 80x90 pixels
- **After resize**: Layout scales proportionally - perfect!
- **Platform agnostic**: Works everywhere

### Real-World Example

**Laptop at center of 10000x10000 virtual canvas:**
```json
{
  "position": { "x": 4000, "y": 4500 },  // Near center
  "size": { "width": 2000, "height": 1500 }
}
```

**Scales to different screens:**
- **1920x1080 screen**: `768x162` pixels at `(768, 486)`
- **3840x2160 screen**: `1536x324` pixels at `(1536, 972)`  
- **1280x720 screen**: `512x108` pixels at `(512, 324)`
- **All proportional!**

## Configuration Update

Update `StateManagement.kt`:
```kotlin
object StateManagementConfig {
    // Remove hardcoded initial pan
    // Calculate dynamically based on content
    
    fun calculateInitialPan(
        layout: WorkstationLayout,
        canvasSize: Size,
        zoom: Float
    ): Offset {
        val boundingBox = calculateDevicesBoundingBox(layout.devices, zoom)
        return calculateCenterPan(
            boundingBox,
            canvasSize.width,
            canvasSize.height
        )
    }
}
```

## Implementation Priority

1. **High Priority**: Fix center calculation to properly center devices
2. **Medium Priority**: Add normalized coordinate support
3. **Low Priority**: Migrate existing sample data

## Next Steps

1. Implement dynamic center calculation
2. Add coordinate system detection
3. Create normalized sample file
4. Document coordinate system in SAMPLE_DATA_GUIDE.md
5. Add migration utility for converting files

## Port Positioning in Virtual Units

Ports are positioned along device edges using absolute virtual units:

```json
"ports": [
  {
    "id": "hdmi-out",
    "name": "HDMI Output",
    "type": "HDMI",
    "direction": "OUTPUT",
    "position": {
      "side": "RIGHT",
      "position": 75  // 75 virtual units from the top of the device
    }
  }
]
```

- For LEFT/RIGHT sides, position is measured in virtual units from the top edge
- For TOP/BOTTOM sides, position is measured in virtual units from the left edge
- Positions outside device bounds are automatically clamped:
  - If position < 0: Uses 0
  - If position > device height (for LEFT/RIGHT) or width (for TOP/BOTTOM): Uses maximum value
- This system makes port placement more predictable and consistent across different device sizes
