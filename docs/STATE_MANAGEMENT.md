# State Management

## Overview

The application uses a reactive state management system based on StateFlow and the MVVM pattern. State flows unidirectionally from ViewModels to UI components, ensuring predictable state updates and automatic UI synchronization.

---

## StateFlow-Based Reactive State

### Core Concept

StateFlow provides a reactive state container that automatically notifies observers when state changes. The application uses StateFlow to manage all UI state, ensuring that UI components automatically update when state changes.

### State Observation

- **StateFlow**: Mutable state container that emits state changes
- **State Observation**: UI components observe state using `collectAsState()`
- **Automatic Updates**: Compose automatically recomposes when observed state changes
- **Efficient Propagation**: StateFlow uses conflation to efficiently propagate state changes

### State Sharing

StateFlow instances are shared using `stateIn()` with a coroutine scope, ensuring:
- State is shared efficiently across multiple observers
- State persists while there are active observers
- Automatic cleanup when no longer needed
- Lifecycle-aware state management

---

## Unidirectional Data Flow

### Flow Direction

**State Flow (ViewModel → UI)**:
1. ViewModel manages state using StateFlow
2. State changes trigger StateFlow emissions
3. UI components collect state using `collectAsState()`
4. Compose automatically recomposes when state changes
5. UI reflects current state

**Event Flow (UI → ViewModel)**:
1. User interactions trigger events in UI components
2. UI calls ViewModel functions with event data
3. ViewModel processes events and updates state
4. State changes propagate back to UI through StateFlow
5. UI updates reflect the new state

### Benefits

- **Predictable Updates**: State always flows in one direction
- **Easy Debugging**: Clear data flow makes issues easier to trace
- **Testability**: ViewModels can be tested independently of UI
- **Maintainability**: Clear separation of concerns

---

## ViewModel Lifecycle

### Lifecycle Awareness

ViewModels are lifecycle-aware components that:
- **Automatic Cleanup**: Resources cleaned up when ViewModel is cleared
- **State Preservation**: State persists across configuration changes
- **Coroutine Scope**: Built-in `viewModelScope` for coroutines
- **Lifecycle Integration**: Integrates with Compose lifecycle

### State Persistence

- State persists while ViewModel is active
- State is preserved across UI recompositions
- State survives configuration changes (orientation, theme)
- State is cleared when ViewModel is no longer needed

### Resource Management

- Coroutines launched in `viewModelScope` are automatically cancelled
- StateFlow subscriptions are managed automatically
- No manual cleanup required
- Memory-efficient state management

---

## State Structure

### Primary State

Primary state contains all UI-related state:
- **Layout State**: Current workstation layout data
- **Selection State**: Selected devices, connections, ports
- **Hover State**: Hovered elements for visual feedback
- **Viewport State**: Zoom level, pan offset, viewport size
- **UI State**: Theme, loading state, error messages, UI panel visibility
- **Interaction State**: Search queries, filters, animation toggles

### Derived State

Derived state is computed from primary state for rendering optimization:
- **Render Data**: Pre-computed rendering information for devices and connections
- **Visibility Data**: Viewport culling information
- **Coordinate Transformations**: Screen-space coordinates for rendering
- **Selection Highlighting**: Visual state for selected elements

### State Separation

Primary state and derived state are separated to:
- **Optimize Performance**: Avoid unnecessary recomputations
- **Prevent Recomposition**: Derived state changes don't trigger unnecessary recompositions
- **Clear Responsibilities**: Primary state for business logic, derived state for rendering

---

## State Validation

### Validation Before Updates

All state changes are validated before being applied:
- **Zoom Validation**: Zoom levels constrained to min/max values
- **Pan Validation**: Pan offsets validated (currently no constraints)
- **Selection Validation**: Selected elements validated against current layout
- **Data Validation**: Layout data validated before state updates

### Validation Sources

Validation can come from:
- **Configuration**: Viewport configuration from layout metadata
- **Default Values**: Fallback values when configuration unavailable
- **Business Rules**: Application-specific validation rules
- **State Constraints**: Constraints based on current state

### Validation Benefits

- **Data Integrity**: Ensures state is always valid
- **User Experience**: Prevents invalid operations
- **Error Prevention**: Catches errors before they affect UI
- **Consistent Behavior**: Predictable state transitions

---

## State Updates

### Immutable State Updates

State is updated using immutable copy operations:
- **Copy Operations**: State classes use `copy()` for updates
- **Immutable Properties**: All state properties are `val`
- **Predictable Updates**: Immutable updates prevent accidental mutations
- **Thread Safety**: Immutable state is inherently thread-safe

### State Update Pattern

1. Read current state
2. Create new state using `copy()` with updated values
3. Validate new state
4. Update StateFlow with new state
5. StateFlow emits update to observers
6. UI automatically recomposes

### Batch Updates

Multiple state properties can be updated in a single `copy()` operation:
- **Atomic Updates**: All properties updated together
- **Single Emission**: StateFlow emits once for multiple changes
- **Efficient Updates**: Reduces number of recompositions

---

## Derived State Computation

### Purpose

Derived state is computed from primary state to optimize rendering:
- **Pre-computation**: Expensive calculations done once
- **Rendering Optimization**: Ready-to-use rendering data
- **Performance**: Avoids recalculating during rendering
- **Separation**: Keeps rendering logic separate from business logic

### Computation Trigger

Derived state is recomputed when:
- Primary state changes (layout, zoom, pan, selection)
- Viewport changes (size, position)
- Selection changes (devices, connections)
- Any state that affects rendering changes

### Computation Location

Derived state is computed in ViewModels:
- **ViewModel Responsibility**: ViewModels compute derived state
- **UI Layer Separation**: UI layer doesn't compute derived state
- **Performance**: Computation happens once, not during rendering
- **Testability**: Derived state computation can be tested independently

---

## State Management Patterns

### Single Source of Truth

- **ViewModel as Source**: ViewModel is the single source of truth for state
- **No Duplicate State**: State is not duplicated in UI components
- **Consistent State**: All components see the same state
- **Predictable Updates**: State updates happen in one place

### State Observation

- **Reactive Observation**: UI components observe state reactively
- **Automatic Updates**: UI updates automatically when state changes
- **Efficient Observation**: Only observes needed state
- **Lifecycle-Aware**: Observation respects component lifecycle

### Event Handling

- **Function Calls**: UI calls ViewModel functions for events
- **Event Processing**: ViewModel processes events and updates state
- **No Direct State Access**: UI doesn't directly modify state
- **Clear Event Flow**: Events flow clearly from UI to ViewModel

---

## State Management Best Practices

### State Design

- **Immutable State**: All state classes use immutable properties
- **Clear Structure**: State structure clearly represents UI needs
- **Minimal State**: Only store necessary state
- **Type Safety**: Use sealed classes for type-safe state

### State Updates

- **Validated Updates**: Always validate state before updates
- **Atomic Updates**: Update related properties together
- **Immutable Copies**: Use `copy()` for state updates
- **Clear Intent**: State update functions have clear names

### Performance

- **Derived State**: Compute expensive data in ViewModels
- **Lazy Computation**: Compute only when needed
- **State Separation**: Separate primary and derived state
- **Efficient Observation**: Observe only needed state

### Testing

- **Testable ViewModels**: ViewModels can be tested independently
- **Mock State**: Easy to create mock state for testing
- **State Validation**: Test state validation logic
- **State Transitions**: Test state transition logic

---

## Related Documentation

- [ARCHITECTURE.md](ARCHITECTURE.md) - System architecture and MVVM pattern
- [PROJECT_OVERVIEW.md](PROJECT_OVERVIEW.md) - Project structure and purpose
- [VIEWPORT_SYSTEM.md](VIEWPORT_SYSTEM.md) - Viewport state management
- [COORDINATE_SYSTEM.md](COORDINATE_SYSTEM.md) - Coordinate transformation in state

