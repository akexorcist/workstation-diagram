# Port Rendering System - Summary

## Core Concepts

- **Capsule-Shaped Labels**: Ports display names in capsules that attach seamlessly to device edges
- **Color Coded Direction**: Input/output/bidirectional ports have distinct colors for quick identification
- **Responsive Design**: Port labels scale appropriately with zoom levels
- **Performance Optimized**: Only ports visible in the viewport are rendered
- **Text Simplification**: Port names are automatically shortened for readability
- **Edge Awareness**: Capsules adapt shape based on which device edge they're attached to
- **Grid Alignment**: Port positions snap to a virtual grid for consistent routing
- **Seamless Device Integration**: Capsules have flat edges where they connect to devices

## Current Features

- **Capsule UI Design**: Modern capsule-shaped port labels with rounded outer corners
- **Automatic Name Shortening**: Removes common suffixes and abbreviates technical terms
- **Direction-Based Coloring**: Visual differentiation of port types through color
- **Responsive Scaling**: Labels scale proportionally with zoom level
- **Viewport Culling**: Only renders ports visible in the current view
- **Device Side Adaptation**: Adjusts shape and positioning based on port location (top/bottom/left/right)
- **Text Alignment**: Automatic alignment based on port side (centered, left, right)
- **Dynamic Sizing**: Width calculation based on content length for optimal display
- **Seamless Attachment**: Port labels overlap slightly with device edges for continuity
- **Consistent Typography**: Standardized text styling for improved readability

## Rendering Rules

- **No Gap**: No visible gap between ports and device edges
- **Device Side Clipping**: Flat edge on the device side, rounded on other sides
- **Content-Based Width**: Each port capsule's width is determined by its name length
- **Fixed Height**: Consistent height for all port capsules for visual uniformity
- **Directional Colors**: Color scheme based on port direction for quick identification
- **Text Overflow Handling**: Long names are abbreviated and not truncated
- **Zoom Proportionality**: All dimensions scale proportionally with zoom level
- **Alignment Consistency**: Text alignment follows the edge attachment direction
- **Visibility Culling**: Only visible ports are rendered for performance
- **Edge-Side Awareness**: Labels adjust position and shape based on device side

## Configuration Parameters

- **Base Width**: Minimum capsule width excluding text (default: 6 units)
- **Width Per Character**: Additional width per character (default: 4.2 units)
- **Font Size**: Base font size before scaling (default: 7 units)
- **Capsule Height**: Fixed height for port capsules (default: 16 units)
- **Horizontal Padding**: Inner padding between text and capsule edges (default: 2 units)
- **Side Padding**: Extra padding on both sides of text (default: 2 units)
- **Device Overlap**: Amount of overlap with device edge (default: 5 units)

## System Architecture

- **CapsulePortNode**: Core composable for rendering individual port labels
- **PortsOverlay**: Composable for rendering all port labels in the diagram
- **RenderingConfig**: Central configuration parameters for port rendering
- **PortNode**: Contains utility functions for port name formatting and color selection
- **DiagramCanvas**: Integration point for the port rendering system

## Performance Characteristics

- **Rendering Efficiency**: Only visible ports rendered through viewport culling
- **Scaling Performance**: Maintains responsiveness at all zoom levels
- **Memory Optimization**: No caching of port labels, generated on-demand
- **Text Measurement**: Simple character-count estimation for width calculation

## Visual Handling

- **Clipped Corners**: Rounded corners only on the non-device sides
- **Text Formatting**: Bold, white text for maximum readability
- **Color Coding**: Input ports (blue), output ports (green), bidirectional (semi-transparent)
- **Text Abbreviation**: Common terms like "Thunderbolt" → "TB", "DisplayPort" → "DP"
- **Attachment Visualization**: Slight overlap creates seamless connection to devices

## Edge Cases

- **Dense Port Layouts**: Handled through individual width calculation
- **Very Long Port Names**: Automatic abbreviation and shortening
- **Mixed Port Types**: Distinct colors maintain clarity
- **Varying Zoom Levels**: Consistent scaling maintains readability
- **Viewport Boundaries**: Culling prevents rendering off-screen ports

## Text Processing System

- **Suffix Removal**: Removes redundant " Input", " Output", " Port" suffixes
- **Term Abbreviation**: Common port types replaced with standard abbreviations
- **Whitespace Handling**: Maintains proper spacing after abbreviation
- **Name Consistency**: Preserves identifying portions of port names
- **Technical Term Recognition**: Special handling for technical port types

## Migration Notes

- **Backward Compatibility**: Maintains support for legacy circular port visualization
- **Feature Toggle**: Can disable capsule rendering if needed
- **Performance Impact**: Minimal due to viewport culling and efficient rendering