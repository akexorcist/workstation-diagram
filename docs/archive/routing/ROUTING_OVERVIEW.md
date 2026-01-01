# Intelligent Connection Routing - Overview

## Purpose

This document provides a high-level overview of the intelligent grid-based connection routing system for the Workstation Diagram application.

---

## What This Routing System Does

The routing system automatically calculates optimal paths for connections between device ports, ensuring:

- ✅ **No collision with devices** - Connections route around device obstacles
- ✅ **Port avoidance** - Connections route around all ports in the diagram
- ✅ **Proper clearance** - Maintains minimum distance from devices and ports
- ✅ **Grid-aligned paths** - All paths follow orthogonal grid system
- ✅ **Minimal crossings** - Prefers paths with fewer intersections
- ✅ **Smart conflict resolution** - Multiple connections can share space intelligently
- ✅ **Graceful failure** - Shows warning when optimal path impossible

---

## Core Algorithm

**A* Pathfinding** with the following characteristics:

- **Heuristic**: Manhattan distance (optimal for orthogonal grid)
- **Cost Function**: Base movement cost + crossing penalty
- **Grid-based**: Discrete grid cells for path planning
- **Multi-connection**: Routes all connections with conflict awareness

---

## Key Parameters

| Parameter | Default | Description |
|-----------|---------|-------------|
| Grid Cell Size | 10 units | Size of each routing grid cell |
| Device Clearance | 10 units | Minimum distance from devices |
| Port Clearance | 35 units | Minimum distance from other ports |
| Port Extension | 20 units | Straight extension from ports |
| Crossing Penalty | 5.0 | Cost multiplier for path crossings |

All parameters are configurable in `RoutingConfig.kt`.

---

## Routing Rules

### 1. **Enhanced Device and Port Avoidance**
No connection path passes through any device or port. Enlarged clearance zones are maintained around each device and port, with special handling for ports to ensure complete avoidance.

### 2. **Grid Alignment**
All paths follow a virtual grid system. Devices snap their size to align with grid boundaries.

### 3. **Orthogonal Paths Only**
Connections only move horizontally or vertically - no diagonal segments.

### 4. **Port Extensions**
Connections extend straight from ports (following existing behavior) before routing begins.

### 5. **Crossing Rules**
- Connections **can cross** each other
- Multiple connections **cannot** use the same grid cell in the same direction simultaneously
- Perpendicular crossings are allowed and preferred

### 6. **Failure Handling**
When no valid path exists:
- Draw straight line from source to target
- Use warning color (orange-red)
- Make line visually distinct (thicker)

---

## System Architecture

The routing system consists of 4 main components:

```
┌─────────────────────────────────────────────┐
│         ConnectionRouter                     │
│  (Orchestrates entire routing process)       │
└─────────────────┬───────────────────────────┘
                  │
         ┌────────┴────────┐
         │                 │
    ┌────▼─────┐    ┌─────▼──────┐
    │  Routing │    │   A* Path  │
    │   Grid   │◄───│   Finder   │
    └──────────┘    └────────────┘
         │
    ┌────▼─────┐
    │ Routing  │
    │ Config   │
    └──────────┘
```

### Component Responsibilities

1. **RoutingConfig** - Centralized configuration parameters
2. **RoutingGrid** - Grid system, obstacle tracking, occupancy management
3. **AStarPathfinder** - Path calculation using A* algorithm
4. **ConnectionRouter** - Coordinates routing for all connections

---

## Coordinate System Integration

The routing system works seamlessly with both coordinate systems:

- **Absolute Coordinates**: Converted to virtual space via `CoordinateTransformer`
- **Virtual Coordinates**: Used directly

**All routing calculations happen in virtual coordinate space.**

---

## Performance Characteristics

### Expected Performance
- **Grid Size**: Proportional to virtual canvas size ÷ grid cell size
- **Time Complexity**: O(n × m × log(m)) where:
  - n = number of connections
  - m = grid cells in path search
- **Practical Performance**: < 1 second for typical layouts (10-20 connections)

### Optimization Strategy
- ✅ Grid-based discretization (reduces search space)
- ✅ Manhattan heuristic (guides search efficiently)
- ✅ Early termination (stops at goal)
- ✅ Result caching (avoids recalculation)

---

## Cache Management

**Cache Invalidation Triggers:**
- Window resize (canvas size changes)
- Zoom level changes

**Not invalidated by:**
- Device movement (requires JSON file update)
- Connection changes (requires JSON file update)

Caching strategy assumes static layout data with dynamic viewport.

---

## Migration Strategy

### Rollout Plan
1. Keep existing `calculateOrthogonalPath()` as fallback
2. Add feature flag to toggle new routing system
3. Test thoroughly with flag enabled
4. Remove old algorithm once stable

### Rollback Capability
If issues arise, disable feature flag to revert to old routing instantly.

---

## Document Structure

This routing system is documented across multiple files:

- **ROUTING_OVERVIEW.md** (this file) - High-level concepts
- **ROUTING_DATA_STRUCTURES.md** - Grid system, data models
- **ROUTING_ALGORITHMS.md** - A* implementation, pathfinding logic
- **ROUTING_INTEGRATION.md** - How to integrate with existing code
- **ROUTING_ISSUES_RESOLVED.md** - Design review findings and resolutions

---

## Quick Start

### For Developers
1. Read this overview
2. Review `ROUTING_DATA_STRUCTURES.md` for data models
3. Study `ROUTING_ALGORITHMS.md` for algorithm details
4. Follow `ROUTING_INTEGRATION.md` for implementation

### For Configuration
Edit `RoutingConfig.kt` to adjust routing behavior:

```kotlin
RoutingConfig.apply {
    gridCellSize = 15f        // Coarser grid
    deviceClearance = 15f     // More space around devices
    portClearance = 35f       // Enhanced port avoidance
    turnPenalty = 10.0f       // Discourage turns near ports
    pathRepulsionFactor = 6f  // Stronger path repulsion
    minPathSpacing = 25f      // Wider spacing between paths
    crossingPenalty = 10f     // Avoid crossings more
    portExtension = 30f       // Longer port extensions
}
```

---

## Success Criteria

- ✅ No connection passes through any device
- ✅ Connections maintain proper clearance from devices
- ✅ All paths follow grid (orthogonal segments only)
- ✅ Connections can cross each other
- ✅ No two connections share same direction in same cell
- ✅ Failed routes show warning color
- ✅ All parameters easily configurable
- ✅ Routes recalculate on window resize
- ✅ Routing time < 5 seconds for 50 connections

---

## Next Steps

Ready to dive deeper? See:
- **Data Structures**: `ROUTING_DATA_STRUCTURES.md`
- **Algorithms**: `ROUTING_ALGORITHMS.md`
- **Integration Guide**: `ROUTING_INTEGRATION.md`
- **Design Review**: `ROUTING_ISSUES_RESOLVED.md`
