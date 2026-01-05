# Viewport System

## Overview

The viewport system provides navigation capabilities for exploring the diagram: zoom, pan, and automatic centering. All operations work seamlessly with both virtual and absolute coordinate systems.

---

## Zoom

### Point-Based Zooming

Zoom operates towards a specific point (typically the viewport center), keeping that point fixed during zoom operations. This provides intuitive zoom behavior where the content under the cursor or center stays in place.

### Zoom Constraints

Zoom levels are constrained between minimum and maximum values:
- Configurable via layout metadata
- Default values when not specified
- Validated before application

### Zoom Integration

Zoom is applied after coordinate transformation:
1. Coordinates transformed to world space
2. Zoom factor applied
3. Pan offset applied for final screen position

---

## Pan

### Pan Operations

Panning moves the viewport to explore different areas of the diagram:
- Drag gestures for smooth panning
- Accumulated delta tracking for responsive interaction
- Works at any zoom level
- Coordinate system agnostic

### Pan State Management

- ViewModel maintains pan offset as source of truth
- Pan offset captured at drag start
- Deltas accumulated during drag
- Complete position sent to ViewModel (not incremental)

---

## Centering

### Automatic Centering

The system can automatically center the viewport on diagram content:
- Calculates bounding box of all devices
- Centers bounding box in viewport
- Accounts for current zoom level
- Adjusts pan offset to achieve centering

### Centering Algorithm

1. Calculate device bounding box
2. Find center of bounding box
3. Calculate viewport center
4. Compute pan offset to align centers
5. Apply pan offset with zoom consideration

---

## Viewport Culling

### Performance Optimization

Only elements visible in the current viewport are rendered:
- Calculates viewport bounds
- Checks element visibility
- Skips rendering for off-screen elements
- Significant performance improvement for large diagrams

### Culling Calculation

Elements are culled based on:
- Viewport bounds (position and size)
- Element bounds (position and size)
- Zoom level consideration
- Margin for smooth scrolling

---

## Coordinate Transformation

All viewport operations work through the coordinate transformation system:
- Pan operations transform coordinates correctly
- Zoom applies after coordinate transformation
- Works with both virtual and absolute coordinates
- Maintains coordinate system integrity

---

## Related Documentation

- [COORDINATE_SYSTEM.md](COORDINATE_SYSTEM.md) - Coordinate transformation pipeline
- [STATE_MANAGEMENT.md](STATE_MANAGEMENT.md) - Viewport state management
- [RENDERING_SYSTEM.md](RENDERING_SYSTEM.md) - Viewport culling in rendering

