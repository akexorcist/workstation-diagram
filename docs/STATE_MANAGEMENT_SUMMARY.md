# State Management - Summary

## Core Concepts

- **MVVM Architecture**: Implements Model-View-ViewModel pattern for clear separation of concerns
- **Immutable State**: Uses immutable data classes with copy() for predictable updates
- **Reactive UI**: Leverages StateFlow for reactive UI updates with automatic recomposition
- **Derived State**: Maintains separate UI state and render state for optimized performance
- **Lifecycle Management**: Automatic cleanup of resources through ViewModel lifecycle
- **Coordinate Transformation**: Handles virtual-to-screen coordinate conversion for rendering
- **Validation Layer**: All state updates are validated before application
- **Error Handling**: Comprehensive error handling with user feedback

## Current Features

- **StateFlow Implementation**: Reactive state container with automatic UI updates
- **Two-Tier State System**: Separates UI state (what to display) from render state (how to display)
- **Viewport Management**: Handles zoom, pan, and content centering
- **Selection State**: Tracks selected devices and connections
- **Search Functionality**: Filters devices based on search queries
- **Theme Management**: Controls light/dark theme toggling
- **Visibility Culling**: Only renders elements visible in the current viewport
- **Hover State**: Tracks mouse hover for interactive elements
- **Animation Control**: Toggles connection animations
- **Error State**: Captures and displays load errors and warnings
- **UI Panel Management**: Controls panel expanded/collapsed states

## State Management Rules

- **No Direct Mutations**: All state changes must use immutable copy operations
- **Validated Updates**: State values are validated before updating state
- **Unidirectional Data Flow**: State flows from ViewModel to UI, events flow from UI to ViewModel
- **State Separation**: UI state remains separate from render state
- **Coordinate System Integration**: All positioning goes through coordinate transformation
- **View Independence**: ViewModels don't directly reference views
- **Suspended Operations**: All asynchronous operations use suspend functions
- **Automatic Recalculation**: Render state updates automatically when UI state changes

## Configuration Parameters

- **Initial Pan Offset**: Default `Offset(150f, 30f)` to account for UI elements
- **Zoom Constraints**: Min zoom `0.5f`, max zoom `3.5f`
- **Default Zoom**: `1.0f` for initial rendering
- **Viewport Culling Margin**: `100f` units beyond viewport for smoother scrolling
- **Default Canvas Size**: `Size(1920f, 1080f)` when not specified in layout

## System Architecture

- **WorkstationViewModel**: Central state coordinator that manages all state
- **WorkstationUiState**: Contains what to display (layout, selection, theme, etc.)
- **DiagramState**: Contains how to display (render data, transformations)
- **StateManagementConfig**: Manages viewport state validation
- **ViewportConfig**: Controls viewport rendering parameters
- **InteractionConfig**: Manages zoom limits and interaction constraints

## State Models

- **WorkstationUiState**: Tracks UI state (layout, selection, theme, etc.)
- **DiagramState**: Contains pre-computed render data for performance
- **DeviceRenderData**: Screen-space information for device rendering
- **ConnectionRenderData**: Screen-space information for connection rendering
- **Rect**: Utility for viewport intersection testing

## State Updates

- **Direct Updates**: Simple state properties use direct copy operations
- **Validated Updates**: Properties with constraints use validation functions
- **Computed Updates**: Properties that depend on calculations use helper functions
- **Related Field Updates**: Multiple fields may update together (e.g., selection)
- **Render State Updates**: UI state changes trigger render state recalculation

## Compose Integration

- **collectAsState()**: Collects StateFlow values for reactive UI updates
- **LaunchedEffect**: Handles side effects and one-time operations
- **rememberCoroutineScope**: Manages coroutines for UI event handling
- **SideEffect**: Provides a way to perform side effects safely
- **Key-based Recomposition**: Optimizes recomposition based on changed state

## Best Practices

- **Keep State Immutable**: All state classes use `val` properties
- **Use Suspend Functions**: Asynchronous operations are implemented as suspend functions
- **Separate UI from Render State**: Prevents unnecessary UI recompositions
- **Validate State Updates**: All state changes go through validation
- **Use LaunchedEffect for Side Effects**: Prevents operations on every recomposition
- **Compute Derived State in ViewModel**: Prevents calculations in the UI layer
- **Handle Errors Gracefully**: All error states are captured and displayed
- **Use Sealed Classes for Results**: Ensures exhaustive handling of possible states