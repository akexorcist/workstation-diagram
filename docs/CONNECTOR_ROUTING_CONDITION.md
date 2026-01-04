# Grid and Alignment Requirements
* Grid-based System: Routing uses a 10×10 virtual unit grid with A pathfinding
* Orthogonal Movement Only: Paths must follow strictly horizontal and vertical segments
* Grid Alignment: All path segments must snap to the grid for visual consistency
* No Diagonal Segments: All paths must be 100% orthogonal with proper corner points

# Obstacle Avoidance
* Device Avoidance: Paths must never pass through device boundaries
* Device Clearance: Must maintain minimum distance 10 units from devices
* Port Avoidance: Paths cannot pass through port areas not involved in the connection
* Port Clearance: Must maintain minimum distance 15 units from uninvolved ports

# Path Quality Rules
* Minimal Turns: Paths must use the fewest possible turns while respecting constraints
* No Zigzags: Back-and-forth zigzag patterns are prohibited
* Reversed Turn Avoidance: Paths must avoid immediate opposite-direction turns (e.g., right then left, up then down)
* Direction Consistency: Paths should maintain direction when possible
* Turn Minimization: Priority given to L-shaped paths when possible

# Path Spacing and Distribution
* Minimum Path Spacing: Parallel paths must be separated by at least 20 units
* Crossing Constraints: Connections can cross but never share segments in same direction
* Center-First Distribution: Path distribution must start from the center of the spacing between devices first, not from the edges
* Outward Distribution: After placing the center path, subsequent paths must distribute on either side of the center based on optimal across count
* Equal Distribution: Connections must distribute evenly across available space between devices, radiating outward from the center
* No Clustering: Paths should not cluster on one side of available space

# Port Handling
* Port Extensions: Paths extend straight from ports 70 units (RenderingConfig.portExtension) before routing starts
  * Applied to both source and target ports in auto-generated connections
  * Extension direction is perpendicular to port's device edge:
    * LEFT ports extend left (negative X)
    * RIGHT ports extend right (positive X)
    * TOP ports extend up (negative Y)
    * BOTTOM ports extend down (positive Y)
  * Routing algorithm uses extended points as start/end positions
  * Final path includes: port position → extended point → routing points → extended point → port position
* 1-to-1 Port Mapping: Each port used by only one connection
* Port Area Protection: Areas around ports and their extensions are protected

# Performance and Edge Cases
* Routing Order: Shorter connections are routed first
* Failed Route Handling: Shows warning visuals when optimal paths are impossible
* Grid Boundaries: Properly handles devices and ports at grid boundaries