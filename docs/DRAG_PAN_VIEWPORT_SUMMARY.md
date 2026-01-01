# Drag/Pan Viewport - Summary

## Core Concepts

- **Architecture-Compliant Implementation**: Follows established patterns from STATE_MANAGEMENT.md and ARCHITECTURE.md
- **ViewModel as Source of Truth**: `uiState.panOffset` is the authoritative pan position
- **Gesture-Based Interaction**: Uses Compose's detectDragGestures for intuitive interaction
- **Accumulated Delta Tracking**: Captures pan offset at drag start and accumulates deltas
- **Stable Reference Point**: Uses drag start position to prevent stale UI state reads
- **Coordinate Transformation**: All pan operations work through CoordinateTransformer
- **Virtual Coordinate System**: Panning works in both absolute and virtual coordinate modes

## Current Features

- **Smooth Panning**: Drag follows pointer 1:1 without jumping or lag
- **Zoom Integration**: Works correctly at any zoom level
- **Reset to Center**: Ability to reset pan to center the diagram
- **Performance Optimized**: Minimal state updates only during actual drag
- **Cross-Platform Support**: Works on all platforms (Desktop, Web, Mobile)
- **Viewport Culling**: Only renders visible elements for performance
- **Grid Alignment**: Ensures precise alignment with grid system
- **Consistent Pan Behavior**: Consistent behavior across various device densities
- **State Persistence**: Pan position maintained across app sessions

## Implementation Pattern

- **Drag Start Capture**: Captures current pan offset at the start of each drag
- **Delta Accumulation**: Accumulates movement deltas during active drag
- **No Stale Reads**: Never reads potentially outdated uiState during drag
- **Absolute Position Updates**: Sends complete position to ViewModel, not incremental
- **Clean State Flow**: One-way data flow without two-way binding
- **Responsive Updates**: UI automatically recomposes with new pan offset

## Configuration Parameters

- **Initial Pan Offset**: Default starting position when layout is loaded
- **Pan Bounds**: Optional constraints to prevent panning too far from content
- **Pan Reset Position**: Position to reset to when user clicks reset button
- **Drag Sensitivity**: Controls response to user drag gestures

## System Architecture

- **DiagramCanvas**: Main component that handles drag gestures and rendering
- **CoordinateTransformer**: Transforms between coordinate systems (virtual/absolute)
- **WorkstationViewModel**: Manages and validates pan state
- **RoutingConfig**: Integrates with routing system for connection rendering

## Visual Handling

- **Device Rendering**: Devices move with pan offset
- **Connection Rendering**: Connections move with pan offset
- **Viewport Culling**: Only renders elements visible in the current viewport
- **Grid Alignment**: Maintains alignment with underlying grid system

## Performance Considerations

- **Minimal State Updates**: Only updates ViewModel on actual drag events
- **Efficient Recomposition**: Canvas recomposes only when uiState changes
- **Gesture State Management**: Gesture state changes don't trigger recomposition
- **Viewport Culling**: Only renders elements visible in the current viewport

## Integration Notes

- **Coordinate Conversion**: All panning in virtual space via coordinate transformer
- **Window Resize**: Adjusts pan offset on window size changes
- **Multi-Device Support**: Works across different screen sizes and densities
- **Input Method Integration**: Works with mouse, touch, and stylus input