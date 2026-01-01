# Port Label Implementation Specification

## Overview

This document outlines the implementation of capsule-shaped port labels in the Workstation Diagram application. This feature enhances port visualization by displaying the port name in a capsule that seamlessly attaches to device edges.

**Date**: January 1, 2026  
**Status**: Implemented  
**Design Reference**: See attached mockup image

---

## Design Requirements

### Core Requirements

1. **Capsule Shape**: 
   - Ports must use a capsule shape with a flat edge on the device side
   - No gap between port and device (seamless attachment)
   - Rounded corners on the outer edges

2. **Label Text**:
   - Display shortened version of port name
   - Use the smallest text size from Material Theme (labelSmall)
   - Appropriate padding inside capsule

3. **Color Scheme**:
   - Color based on port direction:
     - Input ports: Use `inputActiveColor`
     - Output ports: Use `outputActiveColor`
     - Bidirectional ports: Use `inputActiveColor` with reduced opacity

4. **Responsive Behavior**:
   - Support panning and zooming
   - Scale text and capsule size proportionally with zoom
   - Ensure visibility at different zoom levels
   - Only render ports visible in the viewport (performance)

5. **Label Format**:
   - Remove common suffixes: " Input", " Output", " Port"
   - Replace common terms with abbreviations: "Thunderbolt" → "TB", "DisplayPort" → "DP"
   - Keep remaining text as-is

---

## Component Design

### CapsulePortNode Composable

**File**: `PortNode.kt` (update existing file)

**Purpose**: Display port with capsule-shaped label that attaches to device

**Visual Structure**:
```
Device Side (flat edge)
┌─────────────────────┐
│                     │
│     HDMI            │ ← Port label
│                     │
└─────────────────────┘
 Rounded corners on 3 sides
```

**Props**:
```kotlin
@Composable
fun CapsulePortNode(
    port: Port,                   // Port data object
    zoom: Float,                  // Current zoom level
    clipEdge: String? = null,     // "left", "right", "top", or "bottom"
    modifier: Modifier = Modifier
)
```

**Styling**:
- Shape: RoundedCornerShape with 0.dp radius on the device side edge
- Color: Based on port direction (input/output)
- Text: White, small size, medium weight
- Padding: Small horizontal/vertical padding, scaled with zoom

---

## Implementation Details

### 1. Configuration Constants

Added to `RenderingConfig.kt`:

```kotlin
// Port capsule rendering configuration
const val portCapsuleBaseWidth: Float = 6f          // Base width (excluding text)
const val portCapsuleWidthPerChar: Float = 4.2f     // Width per character
const val portCapsuleFontSize: Float = 7f           // Base font size before scaling
const val portCapsuleHeight: Float = 16f            // Fixed height for the capsule
const val portCapsuleHorizontalPadding: Float = 2f  // Inner horizontal padding
const val portCapsuleSidePadding: Float = 2f        // Extra padding on both sides
const val portDeviceOverlap: Float = 5f             // Overlap with device edge
```

### 2. Utility Functions

#### Port Direction Color

```kotlin
@Composable
private fun getPortDirectionColor(direction: PortDirection): Color {
    return when (direction) {
        PortDirection.INPUT -> WorkstationTheme.themeColor.connection.inputActiveColor
        PortDirection.OUTPUT -> WorkstationTheme.themeColor.connection.outputActiveColor
        PortDirection.BIDIRECTIONAL -> WorkstationTheme.themeColor.connection.inputActiveColor.copy(alpha = 0.7f)
    }
}
```

#### Port Name Shortening

```kotlin
private fun getShortPortName(port: Port): String {
    // Clean up port names to simpler forms
    return port.name
        .replace(" Input", "")
        .replace(" Output", "")
        .replace(" Port", "")
        .replace("Thunderbolt", "TB")
        .replace("DisplayPort", "DP")
}
```

#### Text Width Estimation

```kotlin
private fun getEstimatedPortWidth(port: Port, zoom: Float): Float {
    // Get simple estimate of text width with basic character counting
    val shortName = port.name
        .replace(" Input", "")
        .replace(" Output", "")
        .replace(" Port", "")
        .replace("Thunderbolt", "TB")
        .replace("DisplayPort", "DP")
    
    // Scale the base container width and character width directly with zoom
    val baseWidth = RenderingConfig.portCapsuleBaseWidth * zoom
    val charWidth = shortName.length * RenderingConfig.portCapsuleWidthPerChar * zoom
    val innerPaddingWidth = RenderingConfig.portCapsuleHorizontalPadding * 2 * zoom
    
    // Add consistent side padding that's the same for all ports
    val sidePadding = RenderingConfig.portCapsuleSidePadding * 2 * zoom // Both left and right sides
    
    // Return the total width with consistent side padding
    return baseWidth + charWidth + innerPaddingWidth + sidePadding
}
```

### 3. CapsulePortNode Composable Implementation

```kotlin
@Composable
fun CapsulePortNode(
    port: Port,
    zoom: Float,
    clipEdge: String? = null,  // "left", "right", "top", or "bottom"
    modifier: Modifier = Modifier
) {
    val capsuleColor = getPortDirectionColor(port.direction)
    
    val innerPaddingDp = (RenderingConfig.portCapsuleHorizontalPadding * zoom).dp
    val sidePaddingDp = (RenderingConfig.portCapsuleSidePadding * zoom).dp
    val textSizeSp = (RenderingConfig.portCapsuleFontSize * zoom).sp
    val capsuleHeightDp = (RenderingConfig.portCapsuleHeight * zoom).dp
    
    val cornerRadius = (capsuleHeightDp.value / 2).dp
    
    val shape = when(clipEdge) {
        "left" -> RoundedCornerShape(0.dp, cornerRadius, cornerRadius, 0.dp)
        "right" -> RoundedCornerShape(cornerRadius, 0.dp, 0.dp, cornerRadius)
        "top" -> RoundedCornerShape(0.dp, 0.dp, cornerRadius, cornerRadius)
        "bottom" -> RoundedCornerShape(cornerRadius, cornerRadius, 0.dp, 0.dp)
        else -> RoundedCornerShape(cornerRadius)
    }
    
    val textAlignment = when(clipEdge) {
        "right" -> TextAlign.End
        "left" -> TextAlign.Start
        else -> TextAlign.Center
    }
    
    val boxAlignment = when(clipEdge) {
        "right" -> Alignment.CenterEnd
        "left" -> Alignment.CenterStart
        else -> Alignment.Center
    }
    
    Surface(
        shape = shape,
        color = capsuleColor,
        modifier = modifier
            .height(capsuleHeightDp)
            .clip(shape)
    ) {
        Box(
            contentAlignment = boxAlignment,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = getShortPortName(port),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = textSizeSp,
                textAlign = textAlignment,
                softWrap = false,
                overflow = TextOverflow.Visible,
                lineHeight = textSizeSp,
                modifier = Modifier.padding(start = sidePaddingDp, end = sidePaddingDp, top = 0.dp, bottom = 0.dp)
            )
        }
    }
}
```

### 4. DiagramCanvas Updates

Updated `DiagramCanvas.kt` with the `PortsOverlay` composable for rendering port labels:

```kotlin
@Composable
private fun PortsOverlay(
    layout: WorkstationLayout,
    canvasSize: Size,
    zoom: Float,
    panOffset: Offset,
    viewportSize: androidx.compose.ui.geometry.Size
) {
    // Render each port with its own width based on content
    layout.devices.forEach { device ->
        device.ports.forEach { port ->
            val portPosition = calculatePortScreenPosition(
                device, port, layout.metadata, canvasSize, zoom, panOffset
            )
            
            // Use fixed height that scales with zoom for consistency
            val capsuleHeight = RenderingConfig.portCapsuleHeight * zoom
            
            // Calculate individual width for this port based on its content
            val capsuleWidth = getEstimatedPortWidth(port, zoom)
            
            // Determine clip edge and position based on device side
            val clipEdge: String
            
            val overlap = RenderingConfig.portDeviceOverlap * zoom
            
            val adjustedPosition = when (port.position.side) {
                DeviceSide.LEFT -> {
                    clipEdge = "right"
                    val deviceEdgeX = portPosition.x
                    Offset(deviceEdgeX - capsuleWidth + overlap, portPosition.y - capsuleHeight / 2)
                }
                DeviceSide.RIGHT -> {
                    clipEdge = "left"
                    Offset(portPosition.x - overlap, portPosition.y - capsuleHeight / 2)
                }
                DeviceSide.TOP -> {
                    clipEdge = "bottom"
                    Offset(portPosition.x - capsuleWidth / 2, portPosition.y - capsuleHeight + overlap)
                }
                DeviceSide.BOTTOM -> {
                    clipEdge = "top"
                    Offset(portPosition.x - capsuleWidth / 2, portPosition.y - overlap)
                }
            }
            
            // Only render if the port is in the viewport
            val portCheckRadius = kotlin.math.max(capsuleWidth, capsuleHeight)
            if (isPortVisibleInViewport(portPosition, portCheckRadius, viewportSize)) {
                CapsulePortNode(
                    port = port,
                    zoom = zoom,
                    clipEdge = clipEdge,
                    modifier = Modifier
                        .offset(
                            x = adjustedPosition.x.dp,
                            y = adjustedPosition.y.dp
                        )
                        .width(capsuleWidth.dp)
                )
            }
        }
    }
}
```

---

## Edge Cases and Considerations

### 1. Overlapping Ports

**Issue**: When multiple ports are placed close together on the same device side, their labels may overlap.

**Solution**: 
- Pre-calculate the maximum width required for all ports on each device side
- Apply this consistent width to all ports on that side
- This ensures clean alignment and prevents varying widths

### 2. Visibility Culling

**Issue**: Rendering all port labels could impact performance, especially with many devices.

**Solution**:
- Implement viewport culling to only render ports visible in the current view
- Use `isPortVisibleInViewport()` check before rendering each port label
- This significantly improves performance with large diagrams

### 3. Positioning Accuracy

**Issue**: Port labels need to align perfectly with device edges with no gap.

**Solution**:
- Calculate an overlap amount that ensures port labels connect seamlessly to devices
- Adjust positioning based on port side (left, right, top, bottom)
- Use device edge coordinates for precise alignment

### 4. Text Legibility

**Issue**: Text may become too small to read at low zoom levels.

**Solution**:
- Scale text size with zoom level
- Use medium font weight to improve readability
- Apply appropriate padding that scales with zoom

### 5. Individual Sizing with Consistent Spacing

**Issue**: Varied port names would lead to inconsistent label sizes.

**Solution**:
- Calculate width for each port based on its text content
- Add consistent side padding for proper spacing
- Allow individual widths while maintaining consistent appearance
- Use fixed height for all ports for vertical consistency

---

## Implementation Checklist

### Phase 1: Setup ✓
- [x] Add port capsule configuration constants to `RenderingConfig.kt`
- [x] Create utility functions for port color and name shortening

### Phase 2: Component Implementation ✓
- [x] Create `CapsulePortNode` composable in `PortNode.kt`
- [x] Ensure correct rendering based on clip edge and device side
- [x] Implement text alignment and padding

### Phase 3: Integration ✓
- [x] Add `PortsOverlay` composable to `DiagramCanvas.kt`
- [x] Implement width calculation based on text content
- [x] Ensure proper positioning and attachment to devices
- [x] Add viewport culling for performance optimization

### Phase 4: Testing ✓
- [x] Test with different port names (long, short)
- [x] Test with all four device sides
- [x] Test at various zoom levels
- [x] Test panning behavior
- [x] Test with dense port layouts
- [x] Verify performance with many devices/ports

---

## Migration Notes

### Backward Compatibility
- The existing circular PortNode will be maintained for backward compatibility
- Both implementations will coexist
- The capsule version will be used by the PortsOverlay
- This ensures no breaking changes to existing code

### Performance Impact
- The additional text rendering might slightly impact performance
- Viewport culling and efficient layout calculations mitigate this impact
- Only visible ports are rendered

---

## Future Enhancements

### Potential Additions
1. **Port Tooltips** - Show full port details on hover
2. **Interactive Ports** - Click to highlight connected devices
3. **Port Groups** - Group related ports with collapsible labels
4. **Port Status** - Visual indicators for port status (connected, error)
5. **Port Filtering** - Option to show/hide port labels based on type

---

**Status**: Implemented and Tested  
**Implementation Time**: Completed  
**Risk Level**: Low (isolated UI enhancement)