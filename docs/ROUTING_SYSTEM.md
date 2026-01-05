# Routing System

## Overview

The routing system generates paths for connections between device ports. It supports both automatic pathfinding and manual routing with user-defined waypoints.

---

## Grid-Based Pathfinding

### Grid System

Routing uses a grid-based system (typically 10×10 virtual units) for consistent pathfinding:
- All paths align to grid cells
- Orthogonal movement only (horizontal and vertical)
- No diagonal segments
- Visual consistency across all paths

### Pathfinding Algorithm

The system uses pathfinding algorithms to find optimal paths:
- Considers obstacles (devices, ports)
- Minimizes path length and turns
- Avoids collisions with other paths
- Optimizes for clean layout

---

## Obstacle Avoidance

### Device Avoidance

Paths must avoid device boundaries:
- Never pass through device boundaries
- Maintain minimum clearance (typically 10 units)
- Respect device positions and sizes

### Port Avoidance

Paths avoid port areas not involved in the connection:
- Maintain minimum clearance from uninvolved ports (typically 15 units)
- Protect port areas and their extensions
- Ensure clean routing around ports

---

## Path Quality

### Optimization Rules

Paths are optimized for quality:
- **Minimal Turns**: Fewest possible turns while respecting constraints
- **No Zigzags**: Back-and-forth patterns are prohibited
- **Direction Consistency**: Maintain direction when possible
- **L-Shaped Priority**: Prefer L-shaped paths when feasible

### Path Distribution

Multiple paths between devices are distributed evenly:
- Center-first distribution (start from center, expand outward)
- Equal spacing between parallel paths
- Minimum spacing (typically 20 units)
- No clustering on one side

---

## Port Extensions

### Extension System

Paths extend straight from ports before routing begins:
- Extension length: typically 70 units
- Direction: perpendicular to port's device edge
- Applied to both source and target ports
- Routing algorithm uses extended points as start/end positions

### Path Structure

Complete path includes:
1. Source port position
2. Source port extension
3. Routing waypoints (manual or automatic)
4. Target port extension
5. Target port position

---

## Manual vs Automatic Routing

### Automatic Routing

When routing points are not provided:
- System automatically generates path
- Uses pathfinding algorithm
- Avoids obstacles and optimizes layout
- Generated paths are saved immediately

### Manual Routing

When routing points are provided in JSON:
- Uses user-defined waypoints
- Validates path is reachable
- Ensures orthogonal path (90-degree turns)
- Connects properly to port extensions

### Priority

Manual routing takes precedence:
- If routing points exist, use manual routing
- Otherwise, use automatic routing
- Once routing points exist, they persist

---

## Path Optimization

### Post-Processing

Generated paths undergo optimization:
- Orthogonal enforcement (ensures 90-degree turns)
- Zigzag removal (eliminates back-and-forth patterns)
- Redundant point removal
- Path simplification

### Quality Assurance

Paths are validated for:
- Obstacle clearance
- Grid alignment
- Path connectivity
- Visual quality

---

## Related Documentation

- [COORDINATE_SYSTEM.md](COORDINATE_SYSTEM.md) - Grid alignment with coordinate system
- [RENDERING_SYSTEM.md](RENDERING_SYSTEM.md) - Path rendering
- [EDITING_SYSTEM.md](EDITING_SYSTEM.md) - Manual path editing

