# Coordinate System

## Overview

The application supports two coordinate systems: **absolute** (legacy) and **virtual** (recommended). The virtual coordinate system provides resolution independence and cross-platform compatibility.

---

## Virtual Coordinate System

### Concept

Uses a fixed virtual canvas (typically 10000×10000 units) that automatically scales to any screen size. Similar to density-independent pixels in mobile development or viewport units in web development.

### Benefits

- **Resolution Independent**: Works on any screen size
- **Responsive**: Automatically adapts to window resizing
- **Platform Agnostic**: Same data works across web, desktop, and mobile
- **Aspect Ratio Preservation**: Maintains intended proportions
- **Designer Friendly**: Natural units similar to design tools

### Specification

Virtual coordinates are specified in layout metadata:
- `coordinateSystem`: Set to `"virtual"` to enable virtual coordinates
- `virtualCanvas`: Defines the virtual canvas size (width and height)

---

## Transformation Pipeline

Coordinates flow through three spaces:

### Data Space → World Space → Screen Space

1. **Data Space**: Raw coordinates from JSON (absolute or virtual units)
2. **World Space**: Normalized coordinates scaled to actual canvas size
3. **Screen Space**: Final rendered position with zoom and pan applied

### Transformation Steps

**For Virtual Coordinates**:
1. Scale from virtual canvas to actual canvas size
2. Apply zoom factor
3. Apply pan offset

**For Absolute Coordinates**:
1. Use coordinates directly (no scaling)
2. Apply zoom factor
3. Apply pan offset

---

## Grid Alignment

The coordinate system integrates with a routing grid:
- Grid cells align to virtual coordinate space
- Consistent routing behavior across screen sizes
- Port positions snap to grid for predictable routing

---

## Backward Compatibility

The system automatically detects the coordinate system:
- If `coordinateSystem` is `"virtual"` and `virtualCanvas` is provided → uses virtual coordinates
- Otherwise → uses absolute coordinates (legacy behavior)

Existing files with absolute coordinates continue to work without modification.

---

## Port Positioning

Ports use virtual units for positioning along device edges:
- Positions measured from device edges in virtual units
- Consistent behavior across different device sizes
- Automatic clamping to device bounds

---

## Related Documentation

- [VIEWPORT_SYSTEM.md](VIEWPORT_SYSTEM.md) - How zoom and pan interact with coordinates
- [ROUTING_SYSTEM.md](ROUTING_SYSTEM.md) - Grid-based routing with virtual coordinates
- [DATA_MODEL.md](DATA_MODEL.md) - Data structure for coordinate system specification
