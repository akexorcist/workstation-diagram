# Intelligent Connection Routing - Summary

## Core Concepts

- **Grid-based Pathfinding**: Uses A* algorithm with Manhattan distance heuristic on a 10×10 virtual unit grid
- **Device Avoidance**: Routes connections around device obstacles and port areas with configurable clearance
- **Port Avoidance**: Prevents connections from overlapping with port locations and their extension paths
- **Orthogonal Paths**: All connections follow grid-aligned horizontal/vertical segments only
- **Crossing Management**: Minimizes connection crossings with configurable penalties
- **Turn Minimization**: Prioritizes paths with fewest possible turns for a clean visual layout
- **Zigzag Avoidance**: Actively prevents back-and-forth path patterns for direct, professional appearance
- **Path Simplification**: Removes unnecessary turns to create cleaner diagrams
- **Equal Distribution**: Spreads connections evenly across available space between devices
- **Failure Handling**: Shows warning visuals when optimal paths are impossible

## Current Features

- **Enhanced Port Avoidance**: Treats all ports as obstacles with circular clearance zones and protected port extension paths
- **Connection Ordering**: Routes shorter connections first for optimal layout
- **Conflict Resolution**: Manages grid occupancy to prevent path overlaps
- **Grid Alignment**: Devices snap to grid boundaries for cleaner routing
- **Performance Optimization**: Efficient A* implementation with caching
- **Port Extensions**: Connections extend straight from ports before routing begins
- **Path Distribution**: Evenly distributes connections across available space
- **Turn Optimization**: Actively minimizes the number of turns in each path
- **Advanced Zigzag Prevention**: Multi-level detection and elimination of zigzag patterns for clean paths
- **Pattern Recognition**: Identifies and resolves complex path patterns including extended zigzags
- **Port Area Protection**: Prevents routing through areas surrounding ports and their extension paths for ports not involved in the connection
- **Coordinate System Integration**: Works in virtual coordinate space for consistent behavior

## Routing Rules

- **No Collision**: Connections never pass through device boundaries or port areas
- **Port Area Avoidance**: Routes cannot pass through areas surrounding ports or their extension paths for ports not involved in the connection
- **Clearance Maintenance**: Maintains minimum distance from devices and ports
- **Orthogonal Movement**: Only horizontal and vertical segments allowed
- **1-to-1 Port Mapping**: Each port used by only one connection
- **Crossing Constraints**: Connections can cross but not share segments in same direction
- **Grid Alignment**: All routing segments snap to grid for visual consistency
- **Equal Distribution**: Connections distribute evenly across available space between devices rather than clustering
- **Minimal Turns**: Paths use the fewest possible turns while respecting other constraints
- **Direction Consistency**: Paths maintain consistent direction when possible to avoid zigzags
- **No Zigzags**: Back-and-forth zigzag patterns are prohibited and automatically removed

## Configuration Parameters

- **Grid Cell Size**: Controls granularity of routing grid (default: 10 units)
- **Device Clearance**: Minimum distance from devices (default: 10 units)
- **Port Clearance**: Minimum distance from ports (default: 15 units)
- **Port Extension**: Straight extension from ports (default: 70 units)
- **Crossing Penalty**: Cost multiplier for path crossings (default: 5 units)
- **Turn Penalty**: Cost for making turns (default: 8 units)
- **Progressive Turn Factor**: Multiplier for increasing turn penalties with each additional turn (default: 1.2)
- **Zigzag Penalty**: Extra cost multiplier for zigzag patterns (default: 3)
- **Direction Change Limit**: Soft maximum for direction changes before severe penalties (default: 4)
- **Path Spacing**: Minimum distance between parallel paths (default: 20 units)
- **Path Repulsion**: Factor controlling how strongly paths repel each other (default: 4)
- **Path Density Penalty**: Cost multiplier for routing through congested areas (default: 3)
- **Distribution Factor**: Controls how strongly paths are encouraged to spread out (default: 6)
- **Direction Bias Factor**: Controls strength of preferred direction guidance (default: 0.95)

## System Architecture

- **ConnectionRouter**: Main coordinator orchestrating the routing process
- **RoutingGrid**: Grid system tracking obstacles and path occupancy
- **AStarPathfinder**: Path calculation using A* algorithm
- **PathDensityTracker**: Monitors congestion and path distribution across the grid
- **RoutingConfig**: Centralized configuration parameters
- **GridPoint/GridDirection**: Core data structures for grid representation

## Performance Characteristics

- **Routing Time**: < 1 second for typical layouts (10-20 connections)
- **Memory Usage**: Scales with virtual canvas size ÷ grid cell size
- **Optimization Strategy**: Grid-based discretization, Manhattan heuristic, early termination
- **Cache Management**: Invalidation on window resize and zoom level changes

## Visual Handling

- **Failed Routes**: Straight line with warning color (orange-red)
- **Path Rendering**: Connects through grid-aligned waypoints
- **Path Simplification**: Optional removal of collinear points

## Edge Cases

- **Dense Port Layouts**: Algorithm navigates around port clusters
- **Source/Target Access**: Special handling ensures connections can reach ports
- **Path Complexity**: A* optimization balances turns and crossings
- **Grid Boundaries**: Handles devices and ports at grid boundaries

## Path Distribution System

- **Density Tracking**: Monitors how many paths traverse each grid region
- **Cost Adjustment**: Dynamically increases A* cost for grid cells with high path density
- **Spatial Awareness**: Calculates "center line" between devices to reference for distribution
- **Balanced Spreading**: Encourages connections to distribute evenly across available space
- **Adaptive Behavior**: Still respects other routing constraints while optimizing distribution
- **Visual Balance**: Prevents paths from clustering on one side of the layout
- **Configuration Control**: Distribution strength can be tuned using RoutingConfig parameters

## Turn Minimization System

- **Direction Change Tracking**: Counts and penalizes each direction change in potential paths
- **Progressive Penalties**: Increases turn cost penalty with each additional turn in a path
- **Preferred Direction Bias**: Identifies and maintains consistent routing direction
- **Post-Process Optimization**: Applies multi-pass optimization to eliminate unnecessary turns
- **Corner Point Selection**: Intelligently chooses optimal corners for diagonal connections
- **Port Extension Adjustment**: Customizes port extension length to enable straighter paths
- **Direction-Aware Heuristic**: Modifies A* heuristic to favor paths that maintain direction

## Zigzag Avoidance System

- **Path Pattern Analysis**: Analyzes paths for zigzag patterns at multiple stages of routing
- **Preventive Penalties**: Applies severe cost penalties to discourage zigzag formation during pathfinding
- **Multi-level Detection**: Identifies both simple and complex zigzag patterns
- **Standard Zigzag Removal**: Eliminates basic back-and-forth patterns (Horizontal-Vertical-Horizontal)
- **Complex Pattern Elimination**: Detects and corrects extended zigzags and staircase patterns
- **Iterative Processing**: Applies multiple passes of zigzag elimination until no further improvements
- **Optimal Corner Calculation**: Replaces zigzags with direct L-shaped corners for cleaner appearance
- **Direction Change Minimization**: Ensures minimal direction changes when resolving zigzags
- **Path Consistency**: Maintains path integrity while removing unnecessary back-and-forth segments

## Integration Notes

- **Coordinate Conversion**: All routing in virtual space via coordinate transformer
- **Feature Toggle**: Rollback capability with toggle between old and new routing
- **Window Resize**: Automatic recalculation on canvas size changes