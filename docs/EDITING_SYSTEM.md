# Editing System

## Overview

The editor module extends the viewer with interactive editing capabilities for modifying workstation diagrams. Users can edit port positions, connection paths, and export modified diagrams.

---

## Port Position Editing

### Interactive Editing

Ports can be repositioned along device edges:
- Drag ports to adjust position
- Position constrained to device edge
- Grid snapping for alignment
- Real-time visual feedback

### Position Constraints

Port positions are constrained:
- Must stay on device edge (horizontal or vertical constraint)
- Clamped to device bounds
- Grid alignment when grid enabled
- Position updates immediately affect connected paths

### Connection Synchronization

When port positions change:
- Connected routing points update automatically
- First/last routing points adjust to new port position
- Paths maintain connectivity
- Changes reflected immediately

---

## Connection Path Editing

### Routing Point Management

Users can manipulate routing waypoints:
- Select individual routing points
- Drag routing points to new positions
- Add or remove routing points
- Visual handles for interaction

### Line Segment Editing

Connection line segments can be edited:
- Select line segments
- Drag segments to adjust path
- Maintains orthogonal constraints
- Updates routing points accordingly

### Manual Routing

Manual routing points take precedence:
- User-defined waypoints stored in JSON
- Overrides automatic routing
- Persists across sessions
- Can be edited or removed

---

## Auto-Generation

### Automatic Routing

When connections lack routing points:
- System automatically generates paths
- Uses pathfinding algorithm
- Avoids obstacles and optimizes layout
- Generated paths saved immediately

### One-Time Generation

Auto-routing happens once:
- Generated paths become manual routing points
- Never regenerated automatically
- Can be manually edited afterward
- Persists in connection data

---

## Export Functionality

### JSON Export

Edited diagrams can be exported:
- Complete diagram data to JSON
- All routing points included (manual and auto-generated)
- Device positions and port positions preserved
- Maintains data integrity

### Export Process

Export includes:
- All device data with updated positions
- All port data with updated positions
- All connection data with routing points
- Metadata and configuration

---

## Editing State Management

### Selection State

Editor tracks selected elements:
- Selected routing points
- Selected line segments
- Selected ports
- Selected connections

### Drag State

During editing operations:
- Tracks dragging state
- Maintains original positions
- Updates positions in real-time
- Commits changes on drag end

### Hover State

Visual feedback during editing:
- Hovered routing points
- Hovered line segments
- Hovered ports
- Clear indication of editable elements

---

## Grid Integration

### Grid Snapping

Editing operations snap to grid:
- Port positions snap to grid
- Routing points snap to grid
- Grid size from configuration
- Optional grid alignment

---

## Related Documentation

- [ROUTING_SYSTEM.md](ROUTING_SYSTEM.md) - Routing system used by editor
- [COORDINATE_SYSTEM.md](COORDINATE_SYSTEM.md) - Coordinate system for editing
- [DATA_MODEL.md](DATA_MODEL.md) - Data structure for editing operations

