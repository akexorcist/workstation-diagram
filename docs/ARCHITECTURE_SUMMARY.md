# Application Architecture - Summary

## Core Concepts

- **Layered Architecture**: Clean separation into UI, Presentation, Data, and Platform layers
- **MVVM Pattern**: Model-View-ViewModel architecture for separation of concerns
- **Kotlin Multiplatform**: Shared codebase with platform-specific implementations
- **Dependency Inversion**: High-level modules depend on abstractions, not implementations
- **Reactive Programming**: StateFlow-based reactive UI updates
- **Immutable State**: Immutable data classes with copy() for predictable updates
- **Repository Pattern**: Abstracted data access with clean interfaces
- **Compose UI**: Modern declarative UI with automatic recomposition
- **Platform Agnosticism**: Common code contains no platform-specific dependencies

## Current Features

- **Multiplatform Support**: Targets JVM (desktop) and JS (web) platforms
- **Declarative UI**: Compose Multiplatform for consistent UI across platforms
- **State Management**: Comprehensive reactive state handling with StateFlow
- **Coroutines**: Asynchronous operations with suspend functions
- **Viewport Management**: Zoom, pan, and content centering
- **Selection System**: Device and connection selection with highlighting
- **Theme Management**: Light/dark theme toggling
- **Lifecycle Awareness**: Automatic resource cleanup
- **Error Handling**: Comprehensive error handling with user feedback
- **Serialization**: Kotlinx serialization for data parsing
- **Platform Abstractions**: Expect/actual pattern for platform-specific code
- **Viewport Culling**: Performance optimization for rendering

## Architecture Layers

- **UI Layer**: Compose UI components, screens, and themes
- **Presentation Layer**: ViewModels, state management, and business logic
- **Data Layer**: Repositories, data models, and serialization
- **Platform Layer**: Platform-specific utilities and implementations

## Design Patterns

- **Repository Pattern**: Abstracts data access for presentation layer
- **MVVM Pattern**: Separates UI from business logic and state management
- **Observer Pattern**: Enables reactive state updates via StateFlow
- **Sealed Class Pattern**: Type-safe state representations
- **Strategy Pattern**: Encapsulated algorithms with centralized configuration
- **Factory Pattern**: Object creation without specifying concrete types
- **Adapter Pattern**: Platform-specific implementations behind common interfaces
- **Command Pattern**: Encapsulates requests as objects

## Data Flow

- **Unidirectional Data Flow**: State flows from ViewModel to UI, events flow from UI to ViewModel
- **State Propagation**: StateFlow emits updates automatically to UI
- **Suspend Functions**: Async operations use coroutines with suspend functions
- **Validation Layer**: State updates go through validation before application
- **Derived State**: Render data computed in ViewModel to optimize performance

## Integration Points

- **Common Code**: Shared across all platforms in commonMain
- **Platform-Specific**: Isolated in platformMain source sets
- **Expect/Actual**: Platform-specific implementations via expect/actual declarations
- **View Integration**: Compose UI integrates with StateFlow via collectAsState()
- **Repository Integration**: ViewModels use repositories via interfaces
- **Plugin System**: Support for extensibility via modular components

## System Architecture

- **WorkstationViewModel**: Central state coordinator for the application
- **WorkstationUiState**: Primary UI state container
- **DiagramState**: Render-optimized state for diagram display
- **WorkstationRepository**: Data access abstraction
- **StateManagementConfig**: Configuration for state validation
- **ViewportConfig**: Viewport rendering parameters
- **RoutingConfig**: Connection routing configuration

## Performance Considerations

- **StateFlow Optimization**: Efficient state updates with conflation
- **Viewport Culling**: Only renders visible elements for better performance
- **Lazy Computation**: Computes render data only when needed
- **Coroutine Dispatchers**: Appropriate dispatchers for different tasks
- **Automatic Resource Cleanup**: ViewModel lifecycle for memory management
- **Coordinate Caching**: Minimizes coordinate transformations
- **Path Simplification**: Reduces unnecessary rendering

## Best Practices

- **Immutable State**: All state classes use val properties
- **Suspend Functions**: Async operations are suspend functions
- **Separation of UI/Render State**: Prevents unnecessary recompositions
- **Validated State Updates**: All state changes validated before applying
- **LaunchedEffect for Side Effects**: Prevents operations on every recomposition
- **Derived State in ViewModel**: Prevents calculations in UI layer
- **Graceful Error Handling**: All errors captured and displayed
- **Sealed Classes for Results**: Ensures exhaustive handling of states