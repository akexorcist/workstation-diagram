# System Architecture

## Overview

The Workstation Diagram application follows a clean, layered architecture with clear separation of concerns. The architecture is designed for multiplatform support, maintainability, and testability.

---

## Layered Architecture

The application is organized into four distinct layers, each with specific responsibilities:

### UI Layer

**Purpose**: User interface components and visual presentation

**Responsibilities**:
- Compose UI components (screens, dialogs, sidebars, canvas)
- Visual rendering of devices, connections, and ports
- User interaction handling (clicks, drags, keyboard input)
- Theme management and styling
- Animation and visual effects

**Characteristics**:
- Declarative UI using Compose Multiplatform
- Stateless components that receive state from ViewModels
- Automatic recomposition when state changes
- Platform-agnostic UI code in commonMain

**Integration**: Collects state from ViewModels using `collectAsState()` and sends events to ViewModels through function calls.

### Presentation Layer

**Purpose**: Business logic, state management, and coordination

**Responsibilities**:
- ViewModels that manage application state
- State transformation and validation
- Business logic coordination
- Derived state computation for rendering optimization
- Event handling from UI layer

**Characteristics**:
- MVVM pattern implementation
- Reactive state using StateFlow
- Lifecycle-aware components
- Suspend functions for asynchronous operations
- State validation before updates

**Integration**: Receives data from Data Layer through Repository interfaces, manages state, and exposes state to UI Layer.

### Data Layer

**Purpose**: Data models, persistence, and validation

**Responsibilities**:
- Data models (devices, ports, connections, layout)
- JSON serialization and deserialization
- Data validation rules and validation logic
- Repository interfaces and implementations
- Data transformation and mapping

**Characteristics**:
- Immutable data classes
- Sealed classes for type-safe results
- Platform-agnostic data structures
- Validation before data acceptance
- Clear separation between models and business logic

**Integration**: Provides data access through Repository interfaces to Presentation Layer, handles serialization and validation.

### Platform Layer

**Purpose**: Platform-specific implementations and utilities

**Responsibilities**:
- Platform-specific file I/O
- Platform-specific system integrations
- Platform-specific UI adaptations (if needed)
- Resource loading mechanisms

**Characteristics**:
- Expect/actual pattern for platform abstractions
- Isolated platform-specific code
- Common interface definitions in commonMain
- Platform implementations in platform-specific source sets

**Integration**: Provides platform abstractions used by Data Layer and other layers through expect/actual declarations.

---

## MVVM Pattern

The application uses the Model-View-ViewModel (MVVM) pattern for separation of concerns:

### Model

**Representation**: Data models in the Data Layer

**Characteristics**:
- Immutable data classes
- Pure data structures without business logic
- Serialization support
- Validation rules

### View

**Representation**: Compose UI components in the UI Layer

**Characteristics**:
- Stateless components
- Receives state from ViewModels
- Sends events to ViewModels
- Declarative UI definition

### ViewModel

**Representation**: ViewModels in the Presentation Layer

**Characteristics**:
- Manages UI state using StateFlow
- Contains business logic
- Coordinates data access through repositories
- Validates state changes
- Computes derived state for rendering optimization
- Lifecycle-aware (automatic cleanup)

**State Management**:
- Primary state: UI state (loading, layout, selection, viewport)
- Derived state: Render-optimized state (pre-computed rendering data)
- State validation: All state changes validated before application

---

## Data Flow

The application follows unidirectional data flow:

### State Flow (ViewModel → UI)

1. ViewModel manages state using StateFlow
2. State changes trigger StateFlow emissions
3. UI components collect state using `collectAsState()`
4. Compose automatically recomposes when state changes
5. UI reflects current state

### Event Flow (UI → ViewModel)

1. User interactions trigger events in UI components
2. UI calls ViewModel functions with event data
3. ViewModel processes events and updates state
4. State changes propagate back to UI through StateFlow
5. UI updates reflect the new state

### Data Loading Flow

1. ViewModel initiates data loading through Repository
2. Repository loads and deserializes data
3. Repository validates data
4. Repository returns result (Success, PartialSuccess, or Error)
5. ViewModel processes result and updates state
6. UI reflects loading state, then displays data or error

---

## Multiplatform Strategy

The application uses Kotlin Multiplatform to share code across platforms:

### Source Set Organization

- **commonMain**: Platform-agnostic code shared across all platforms
  - Data models, business logic, UI components
  - No platform-specific dependencies
  - Expect declarations for platform abstractions

- **jvmMain**: Desktop-specific implementations
  - JVM-specific file I/O
  - Desktop UI adaptations (if needed)
  - Actual implementations of expect declarations

- **wasmJsMain**: Web-specific implementations
  - Web-specific file I/O
  - Web UI adaptations (if needed)
  - Actual implementations of expect declarations

### Platform Abstractions

**Expect/Actual Pattern**:
- Common interface defined in commonMain using `expect`
- Platform-specific implementations in platform source sets using `actual`
- Compiler ensures all platforms provide implementations
- Type-safe platform abstractions

**Benefits**:
- Single codebase for business logic
- Platform-specific code isolated
- Type-safe platform abstractions
- Easy to add new platforms

---

## Design Patterns

### Repository Pattern

**Purpose**: Abstracts data access from business logic

**Implementation**:
- Repository interface defines data access contract
- Repository implementation handles data loading, serialization, validation
- ViewModels depend on Repository interfaces, not implementations
- Enables testing with mock repositories

**Benefits**:
- Separation of data access from business logic
- Easy to swap data sources
- Testable with mock implementations

### Observer Pattern

**Purpose**: Enables reactive state updates

**Implementation**:
- StateFlow provides observable state
- UI components observe state changes
- Automatic UI updates when state changes
- Efficient state propagation with conflation

**Benefits**:
- Reactive UI updates
- Decoupled state management
- Efficient state propagation

### Strategy Pattern

**Purpose**: Encapsulates configurable algorithms

**Implementation**:
- Configuration classes define algorithm parameters
- Algorithms use configuration for behavior
- Easy to adjust behavior without code changes
- Centralized configuration management

**Benefits**:
- Configurable behavior
- Centralized configuration
- Easy to adjust algorithms

### Sealed Class Pattern

**Purpose**: Type-safe state and result representations

**Implementation**:
- Sealed classes for result types (Success, Error, PartialSuccess)
- Exhaustive when expressions ensure all cases handled
- Type-safe state transitions
- Compiler-enforced completeness

**Benefits**:
- Type safety
- Exhaustive handling
- Clear state representations

---

## Integration Points

### ViewModel and Repository

- ViewModels depend on Repository interfaces
- Repositories provide data access abstraction
- Dependency injection through constructor parameters
- Enables testing with mock repositories

### ViewModel and UI

- UI collects state from ViewModels using `collectAsState()`
- UI calls ViewModel functions for events
- ViewModels expose StateFlow for state observation
- Automatic recomposition when state changes

### UI and Platform

- UI uses platform abstractions through expect/actual
- Platform-specific code isolated in platform source sets
- Common UI code works across all platforms
- Platform adaptations handled transparently

### Data and Serialization

- Data models support serialization annotations
- Serialization handled by Repository implementations
- Validation occurs after deserialization
- Type-safe data structures

---

## Module Organization

### Shared Module

**Purpose**: Common code shared across applications

**Contains**:
- Data models and structures
- Serialization logic
- Validation rules
- Shared UI components
- Utilities and configuration
- Platform abstractions

**Used By**: Viewer and Editor modules

### Viewer Module

**Purpose**: Read-only visualization application

**Contains**:
- Viewer-specific ViewModels
- Viewer-specific UI components
- Routing path conversion
- Viewer-specific utilities

**Depends On**: Shared module

### Editor Module

**Purpose**: Interactive editing application

**Contains**:
- Editor-specific ViewModels
- Editor-specific UI components
- Editing logic and state management
- Export functionality

**Depends On**: Shared module

---

## Performance Considerations

### State Management

- **StateFlow Conflation**: Efficient state updates with automatic conflation
- **Derived State**: Pre-computed rendering data to avoid recalculation
- **Lazy Computation**: State computed only when needed
- **State Validation**: Validated before expensive operations

### Rendering

- **Viewport Culling**: Only renders visible elements
- **Coordinate Caching**: Minimizes coordinate transformations
- **Path Simplification**: Reduces unnecessary rendering
- **Lazy Composition**: Components composed only when visible

### Asynchronous Operations

- **Coroutine Dispatchers**: Appropriate dispatchers for different tasks
- **Suspend Functions**: Non-blocking asynchronous operations
- **Background Processing**: Heavy operations on background threads
- **Main Thread Updates**: UI updates on main thread

### Memory Management

- **ViewModel Lifecycle**: Automatic resource cleanup
- **StateFlow Sharing**: Efficient state sharing with `stateIn`
- **Immutable State**: Prevents accidental state mutations
- **Resource Cleanup**: Automatic cleanup when ViewModels cleared

---

## Best Practices

### State Management

- **Immutable State**: All state classes use `val` properties
- **State Validation**: All state changes validated before application
- **Derived State**: Computed in ViewModels, not UI layer
- **State Separation**: Separate UI state from render state

### Asynchronous Operations

- **Suspend Functions**: Async operations are suspend functions
- **LaunchedEffect**: Side effects triggered by LaunchedEffect
- **Coroutine Scope**: Use ViewModel's viewModelScope
- **Error Handling**: Comprehensive error handling with user feedback

### UI Composition

- **Stateless Components**: UI components receive state, don't manage it
- **Recomposition Optimization**: Minimize unnecessary recompositions
- **Lazy Composition**: Compose components only when needed
- **State Collection**: Use `collectAsState()` for state observation

### Code Organization

- **Separation of Concerns**: Clear boundaries between layers
- **Dependency Inversion**: High-level modules depend on abstractions
- **Platform Agnosticism**: Common code contains no platform dependencies
- **Type Safety**: Use sealed classes and type-safe patterns

---

## Related Documentation

- [PROJECT_OVERVIEW.md](PROJECT_OVERVIEW.md) - Project structure and purpose
- [STATE_MANAGEMENT.md](STATE_MANAGEMENT.md) - State management concepts
- [COORDINATE_SYSTEM.md](COORDINATE_SYSTEM.md) - Coordinate system design
- [VIEWPORT_SYSTEM.md](VIEWPORT_SYSTEM.md) - Viewport navigation
- [ROUTING_SYSTEM.md](ROUTING_SYSTEM.md) - Connection routing
- [RENDERING_SYSTEM.md](RENDERING_SYSTEM.md) - Visual rendering

