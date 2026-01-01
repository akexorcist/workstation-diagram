# Connection Path Rendering Implementation

## Overview

This document details the implementation of connection path rendering in the Workstation Diagram application. The feature visualizes connections between device ports using gradient paths with dotted foreground, providing clear visual indication of data flow direction through color gradients.

**Date**: January 2, 2026  
**Status**: Implemented  
**Related Files**: DiagramCanvas.kt, RenderingConfig.kt, Color.kt

---

## Design Requirements

### Core Requirements

1. **Connection Path Structure**:
   - Background layer: Solid gradient line that represents the connection
   - Foreground layer: Evenly spaced dots along the path with gradient colors
   - Seamless corners at path bends

2. **Gradient Colors**:
   - Colors based on port directions (input/output)
   - Input ports: Purple (Indigo)
   - Output ports: Pink
   - Color flow represents data direction

3. **Visual States**:
   - Active: Full opacity colors (default for visible connections)
   - Inactive: Reduced opacity (when not selected or highlighted)
   - Hover: Medium opacity (when hovered but not selected)
   - Selected: Enhanced brightness

4. **Responsive Behavior**:
   - Support panning and zooming
   - Scale line width and dot size proportionally with zoom
   - Maintain correct gradient direction at all zoom levels
   - Gradient follows path even with multiple segments

---

## Component Design

### Connection Path Rendering

**File**: `DiagramCanvas.kt`

**Purpose**: Render connection paths between device ports with proper gradients and foreground dots.

**Visual Structure**:
```
┌───────────┐                        ┌────────────┐
│           │                        │            │
│ DEVICE A  ├────●────●────●────●────┤  DEVICE B  │
│           │                        │            │
└───────────┘                        └────────────┘
     │                                     │
     │                                     │
  OUTPUT                                 INPUT
   (Pink)          Gradient Path         (Purple)
```

**Implementation Components**:
- Path calculation
- Gradient background line rendering
- Dot foreground rendering
- Color selection based on port directions

---

## Implementation Details

### 1. Configuration Constants

Constants in `RenderingConfig.kt` control the connection rendering appearance:

```kotlin
// Connection rendering configuration
const val connectionWidth: Float = 4f              // Base width for connections
const val connectionBackgroundWidth: Float = 8f    // Width of the background line
const val connectionForegroundWidth: Float = 6f    // Width of the foreground elements
const val connectionDotLength: Float = 1.5f        // Size of the dots
const val connectionDotGap: Float = 8f             // Spacing between dots
```

### 2. Color Definitions

Connection colors are defined in `ContentColor` class within `Color.kt`:

```kotlin
connection = ConnectionLineComponent(
    // Input colors (purple)
    inputActiveColor = ThemeColor.DimIndigo500,
    inputInactiveColor = ThemeColor.DimIndigo500.copy(alpha = 0.25f),
    inputBackgroundActiveColor = ThemeColor.DimIndigo600.copy(alpha = 0.5f),
    inputBackgroundInactiveColor = ThemeColor.DimIndigo600.copy(alpha = 0.2f),
    
    // Output colors (pink)
    outputActiveColor = ThemeColor.DimPink500,
    outputInactiveColor = ThemeColor.DimPink500.copy(alpha = 0.25f),
    outputBackgroundActiveColor = ThemeColor.DimPink600.copy(alpha = 0.5f),
    outputBackgroundInactiveColor = ThemeColor.DimPink600.copy(alpha = 0.2f),
    
    // Spacing color
    spacingColor = ThemeColor.Gray900
)
```

### 3. Color Selection Based on Port Directions

The colors for gradients are chosen based on the source and target port directions:

```kotlin
val (startBackgroundColor, endBackgroundColor) = when {
    // If source is OUTPUT and target is INPUT (normal flow)
    sourceDirection == PortDirection.OUTPUT && targetDirection == PortDirection.INPUT -> {
        // Use output color at start, input color at end
        val outputBg = when {
            isSelected -> connectionTheme.outputBackgroundActiveColor
            isHovered -> connectionTheme.outputBackgroundActiveColor.copy(alpha = 0.7f)
            else -> connectionTheme.outputBackgroundInactiveColor
        }
        val inputBg = when {
            isSelected -> connectionTheme.inputBackgroundActiveColor
            isHovered -> connectionTheme.inputBackgroundActiveColor.copy(alpha = 0.7f)
            else -> connectionTheme.inputBackgroundInactiveColor
        }
        Pair(inputBg, outputBg) // Swapped for correct gradient direction
    }
    // If source is INPUT and target is OUTPUT (reverse flow)
    sourceDirection == PortDirection.INPUT && targetDirection == PortDirection.OUTPUT -> {
        // Use input color at start, output color at end
        val inputBg = when {
            isSelected -> connectionTheme.inputBackgroundActiveColor
            isHovered -> connectionTheme.inputBackgroundActiveColor.copy(alpha = 0.7f)
            else -> connectionTheme.inputBackgroundInactiveColor
        }
        val outputBg = when {
            isSelected -> connectionTheme.outputBackgroundActiveColor
            isHovered -> connectionTheme.outputBackgroundActiveColor.copy(alpha = 0.7f)
            else -> connectionTheme.outputBackgroundInactiveColor
        }
        Pair(inputBg, outputBg)
    }
    // For other combinations (default case)
    else -> {
        // Default to input->output flow
        val inputBg = when {
            isSelected -> connectionTheme.inputBackgroundActiveColor
            isHovered -> connectionTheme.inputBackgroundActiveColor.copy(alpha = 0.7f)
            else -> connectionTheme.inputBackgroundInactiveColor
        }
        val outputBg = when {
            isSelected -> connectionTheme.outputBackgroundActiveColor
            isHovered -> connectionTheme.outputBackgroundActiveColor.copy(alpha = 0.7f)
            else -> connectionTheme.outputBackgroundInactiveColor
        }
        Pair(inputBg, outputBg)
    }
}

// Similar logic for foreground colors
```

### 4. Path Rendering Function

The `drawGradientConnectionPath` function handles the actual drawing of the connection:

```kotlin
private fun DrawScope.drawGradientConnectionPath(
    path: List<Offset>,
    startBackgroundColor: Color,
    endBackgroundColor: Color, 
    startForegroundColor: Color,
    endForegroundColor: Color,
    zoom: Float
) {
    if (path.size < 2) return
    
    val backgroundWidth = RenderingConfig.connectionBackgroundWidth * zoom
    val foregroundWidth = RenderingConfig.connectionForegroundWidth * zoom
    
    // Calculate total path length for gradient placement
    var totalPathLength = 0f
    val segmentLengths = mutableListOf<Float>()
    
    for (i in 0 until path.size - 1) {
        val length = Offset(
            path[i+1].x - path[i].x,
            path[i+1].y - path[i].y
        ).getDistance()
        segmentLengths.add(length)
        totalPathLength += length
    }
    
    // Create a Path object to draw the entire connection path at once
    val backgroundPath = Path()
    backgroundPath.moveTo(path.first().x, path.first().y)
    
    for (i in 1 until path.size) {
        backgroundPath.lineTo(path[i].x, path[i].y)
    }
    
    // Create a single gradient brush for the entire path
    val backgroundBrush = Brush.linearGradient(
        colors = listOf(startBackgroundColor, endBackgroundColor),
        start = path.first(),
        end = path.last()
    )
    
    // Draw the background line
    drawPath(
        path = backgroundPath,
        brush = backgroundBrush,
        style = Stroke(
            width = backgroundWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
    
    // Draw the foreground dots
    val dotRadius = RenderingConfig.connectionDotLength * zoom
    val dotGap = RenderingConfig.connectionDotGap * zoom
    val dotStep = dotRadius * 2 + dotGap
    
    val totalLength = calculatePathLength(path)
    val dotsCount = (totalLength / dotStep).toInt() + 1
    
    if (dotsCount >= 2) {
        val adjustedStep = totalLength / (dotsCount - 1)
        
        for (i in 0 until dotsCount) {
            val distance = i * adjustedStep
            if (distance > totalLength) break
            
            // Get position on the path for this dot
            val dotPosition = getPointAtDistance(path, distance)
            
            // Calculate color based on position along path
            val fraction = distance / totalLength
            val dotColor = lerp(
                startForegroundColor,
                endForegroundColor,
                fraction
            )
            
            // Draw the dot
            drawCircle(
                color = dotColor,
                radius = dotRadius,
                center = dotPosition
            )
        }
    }
}
```

### 5. Path Position Calculation

Two helper functions calculate positions along the path:

```kotlin
// Calculate total length of a multi-segment path
private fun calculatePathLength(path: List<Offset>): Float {
    var length = 0f
    for (i in 0 until path.size - 1) {
        val start = path[i]
        val end = path[i + 1]
        length += kotlin.math.sqrt(
            (end.x - start.x) * (end.x - start.x) + 
            (end.y - start.y) * (end.y - start.y)
        )
    }
    return length
}

// Find a point at a specific distance along a path
private fun getPointAtDistance(path: List<Offset>, distance: Float): Offset {
    if (distance <= 0f) return path.first()
    
    var distanceSoFar = 0f
    
    for (i in 0 until path.size - 1) {
        val start = path[i]
        val end = path[i + 1]
        
        val segmentLength = kotlin.math.sqrt(
            (end.x - start.x) * (end.x - start.x) + 
            (end.y - start.y) * (end.y - start.y)
        )
        
        if (distanceSoFar + segmentLength >= distance) {
            // This segment contains our point
            val remainingDistance = distance - distanceSoFar
            val ratio = remainingDistance / segmentLength
            
            return Offset(
                x = start.x + (end.x - start.x) * ratio,
                y = start.y + (end.y - start.y) * ratio
            )
        }
        
        distanceSoFar += segmentLength
    }
    
    // If we get here, return the last point
    return path.last()
}
```

---

## Edge Cases and Considerations

### 1. Multi-segment Paths

**Issue**: Paths with multiple segments need continuous gradients that flow properly across corners.

**Solution**: 
- Use a single Path object for the entire connection
- Apply a linear gradient from start to end points
- Use StrokeJoin.Round for smooth corners without overlapping circles

### 2. Dot Distribution

**Issue**: Dots need to be distributed evenly regardless of path complexity.

**Solution**:
- Calculate total path length
- Distribute dots evenly based on total length rather than per segment
- Use `getPointAtDistance()` to place dots at correct positions along the full path

### 3. Direction-Aware Gradients

**Issue**: Gradients must match port types at each end (input=purple, output=pink).

**Solution**:
- Check port directions of both source and target
- Create gradient in proper direction based on data flow
- Ensure colors at endpoints match the ports they connect to

### 4. Zoom Scaling

**Issue**: Connection rendering needs to scale properly with zoom.

**Solution**:
- Scale all dimensions (line width, dot size, spacing) with zoom factor
- Maintain consistent relative proportions at all zoom levels

### 5. Path Corners

**Issue**: Sharp corners between path segments can look unnatural.

**Solution**:
- Use StrokeJoin.Round to create smooth transitions at corners
- Single path object prevents gaps or overlaps at joints

---

## Performance Considerations

### Optimizations

1. **Path Batching**:
   - Draw the entire path at once rather than segment by segment
   - Reduces draw calls and improves performance

2. **Viewport Culling**:
   - Only render connections that are visible in the current viewport
   - Greatly improves performance with many connections

3. **Adaptive Dot Density**:
   - Dots are spaced based on a fixed distance, not a fixed count
   - Ensures reasonable visual density regardless of path length

4. **Efficient Path Calculations**:
   - Calculate path dimensions once and reuse
   - Precompute values where possible

---

## Future Enhancements

1. **Connection Animation**:
   - Animate dots flowing along the path to indicate data transfer
   - Add directional indicators for clearer flow representation

2. **Custom Path Routing**:
   - Allow users to customize path routing with control points
   - Intelligent routing to avoid overlapping with ports in all modules
   - Improve automatic path finding to avoid overlaps

3. **Connection Types**:
   - Visual distinction for different connection types (data, power, etc.)
   - Different patterns or textures based on connection category

4. **Interactive Connections**:
   - Highlight connected devices when hovering over a connection
   - Show connection details on click

5. **Connection Status**:
   - Visual indicators for connection status (active, error, high load)
   - Pulse or glow effects for active data transfers

---

**Status**: Implemented and Tested  
**Implementation**: Complete