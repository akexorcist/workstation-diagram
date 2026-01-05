# Rendering System

## Overview

The rendering system displays devices, connections, and ports with multi-layer rendering, smooth curves, and animated effects. All rendering adapts to zoom levels and uses viewport culling for performance.

---

## Rendering Layers

### Device Rendering

Devices are rendered as rectangles with:
- Visual representation based on device category
- Selection and hover highlighting
- Position and size from coordinate transformation
- Viewport culling for off-screen devices

### Connection Rendering

Connections use dual-layer rendering:

**Background Layer**:
- Wider solid path providing visual foundation
- Gradient coloring from source to target
- Rounded corners with Bezier curves

**Foreground Layer**:
- Narrower animated path
- Flowing oval shapes showing data flow direction
- Color-coded by connection type (input/output)

### Port Rendering

Ports are rendered as capsule-shaped labels:
- Attach seamlessly to device edges
- Color-coded by direction (input/output/bidirectional)
- Text automatically shortened for readability
- Scale proportionally with zoom

---

## Bezier Curve Corners

### Smooth Corners

Connection paths use Bezier curves for smooth corners:
- Orthogonal paths converted to smooth curves
- Quadratic Bezier curves at corners
- Adaptive corner radius based on segment length
- Consistent appearance across all paths

### Path Construction

Paths start as orthogonal (90-degree) segments:
- Converted to smooth curves during rendering
- Corner radius adapts to path geometry
- Maintains visual consistency

---

## Animation System

### Flow Animation

Connections feature animated flow effects:
- Oval shapes flow along the path
- Infinite animation loop
- Direction indicates data flow
- Normalized animation for consistent speed at all zoom levels

### Animation Control

Animation can be toggled:
- Enabled/disabled by user preference
- Performance consideration for large diagrams
- Smooth transitions when toggling

---

## Performance Optimizations

### Viewport Culling

Only visible elements are rendered:
- Calculates viewport bounds
- Checks element visibility
- Skips rendering for off-screen elements
- Significant performance improvement

### Rendering Efficiency

- Path objects created once per connection
- Segment lengths cached
- Coordinate transformations minimized
- Minimal object allocation during rendering

---

## Zoom Adaptation

All rendering aspects scale with zoom:
- Path widths scale proportionally
- Corner radii scale appropriately
- Animation spacing adapts
- Text sizes scale for readability
- Maintains visual consistency at all zoom levels

---

## Visual States

### Selection State

Selected elements have enhanced appearance:
- Brighter colors
- Increased visibility
- Visual feedback for user actions

### Hover State

Hovered elements provide visual feedback:
- Highlighted appearance
- Smooth opacity transitions
- Clear indication of interactive elements

### Connection States

Connections have distinct visual states:
- Input connections: specific color scheme
- Output connections: specific color scheme
- Failed routes: warning visual treatment

---

## Related Documentation

- [VIEWPORT_SYSTEM.md](VIEWPORT_SYSTEM.md) - Viewport culling and zoom
- [ROUTING_SYSTEM.md](ROUTING_SYSTEM.md) - Path generation for rendering
- [COORDINATE_SYSTEM.md](COORDINATE_SYSTEM.md) - Coordinate transformation for rendering

