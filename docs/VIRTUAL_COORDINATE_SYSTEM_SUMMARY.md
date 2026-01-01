# Virtual Coordinate System - Summary

## Core Concepts

- **Resolution Independence**: Uses virtual units on a fixed virtual canvas (10000×10000) that scales to any screen size
- **Coordinate Transformation**: Automatic conversion between virtual and screen coordinates with zoom/pan support
- **Backward Compatibility**: Supports both absolute pixel coordinates (legacy) and virtual coordinates
- **Responsive Design**: Layouts automatically adapt to window resizing and different display resolutions
- **Platform Agnosticism**: Same coordinate system works across web, desktop, and mobile platforms
- **Grid Alignment**: Integrates with routing grid system for consistent routing behavior
- **Port Positioning**: Ports use virtual units for positioning along device edges with predictable behavior
- **Aspect Ratio Preservation**: Maintains intended proportions across different screen sizes

## Current Features

- **Automatic Detection**: System detects coordinate system from metadata fields
- **Seamless Transformation**: CoordinateTransformer handles all coordinate conversions
- **Dynamic Scaling**: Content scales proportionally as window resizes
- **Cross-Platform Support**: Works identically across all supported platforms
- **Zoom Integration**: Virtual coordinates work correctly with zoom functionality
- **Pan Integration**: Coordinate system preserves pan behavior across screen sizes
- **Validation System**: Built-in validation for coordinates within virtual canvas bounds
- **Optimized Performance**: Minimal calculations with no performance impact

## Coordinate System Rules

- **Virtual Canvas**: Uses 10000×10000 virtual canvas by default
- **Origin Location**: Top-left origin (0,0) with Y-down orientation
- **Unit Conversion**: Virtual units scale proportionally to actual canvas size
- **Coordinate Flow**: Data Space → World Space → Screen Space transformation chain
- **Metadata Specification**: Uses "coordinateSystem" and "virtualCanvas" fields in layout metadata
- **Device Positioning**: All device coordinates expressed in virtual units
- **Port Placement**: Ports positioned in virtual units from edges
- **Device Sizing**: Device dimensions specified in virtual units

## Configuration Parameters

- **Default Virtual Canvas**: 10000×10000 units (configurable)
- **Coordinate System Type**: "absolute" (legacy) or "virtual" (new)
- **Conversion Scale**: Calculated as actualCanvasSize / virtualCanvasSize
- **Zoom Factor**: Applied after virtual-to-world conversion
- **Pan Offset**: Applied after zoom to position in viewport

## System Architecture

- **CoordinateTransformer**: Core utility that handles all coordinate transformations
- **LayoutMetadata**: Contains coordinate system specification and canvas size
- **RoutingGrid**: Integrates with virtual coordinates for connection routing
- **StateManagementConfig**: Manages viewport and pan calculations
- **DiagramCanvas**: Renders elements using transformed coordinates
- **WorkstationViewModel**: Maintains coordinate state and transformations

## Visual Handling

- **Device Rendering**: Positions and sizes scaled according to coordinate system
- **Connection Paths**: All paths transformed to screen space for rendering
- **Viewport Culling**: Only renders elements visible in the current viewport
- **Grid Alignment**: Elements snap to grid in virtual coordinate space
- **Port Positioning**: Ports align consistently along device edges

## Integration Notes

- **Data Format**: JSON configuration specifies coordinate system type
- **Window Resize**: Automatic recalculation of scale factors on resize
- **Drag/Pan**: All panning operations work in transformed coordinate space
- **Zoom Behavior**: Zoom applies after coordinate transformation
- **Connection Routing**: Grid-based routing adapts to virtual coordinate system

## Migration Support

- **Backward Compatibility**: Legacy files with absolute coordinates still work
- **Automatic Detection**: System detects coordinate system from metadata
- **Migration Utility**: Tools available to convert absolute to virtual coordinates
- **Validation**: Validates coordinates are within virtual canvas bounds

## Performance Considerations

- **Efficient Transformation**: Simple math operations with minimal overhead
- **No Runtime Impact**: Negligible performance difference from absolute coordinates
- **Caching Strategy**: Transforms calculated only when needed
- **Viewport Culling**: Only transforms and renders visible elements

## Design Advantages

- **Designer Friendly**: Natural units similar to design tools like Figma/Sketch
- **Easier Mental Model**: Predictable units (100 units = small device, 1000 units = large spacing)
- **Cross-Platform Consistency**: Same layout works on all screen sizes and platforms
- **Future Proof**: Designs remain valid as display technology evolves