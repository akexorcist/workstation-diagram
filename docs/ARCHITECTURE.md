# Architecture Documentation

## System Architecture Overview

The Workstation Diagram application follows a clean, layered architecture designed for Kotlin Multiplatform (KMP) development. This document provides an in-depth look at the architectural decisions, patterns, and design principles used throughout the project.

## Table of Contents

- [Architectural Principles](#architectural-principles)
- [Layered Architecture](#layered-architecture)
- [Multiplatform Strategy](#multiplatform-strategy)
- [Design Patterns](#design-patterns)
- [Data Flow](#data-flow)
- [Component Communication](#component-communication)
- [Error Handling](#error-handling)
- [Performance Considerations](#performance-considerations)

---

## Architectural Principles

### 1. Separation of Concerns

Each layer has a distinct responsibility:

- **UI Layer**: Presentation and user interaction
- **Presentation Layer**: State management and business logic
- **Data Layer**: Data access, validation, and transformation
- **Platform Layer**: Platform-specific implementations

### 2. Platform Agnosticism

The common code contains no platform-specific dependencies. All platform-specific code is isolated using expect/actual declarations.

### 3. Reactive Programming

The application uses reactive streams (StateFlow) for state management, ensuring UI updates are automatic and consistent.

### 4. Dependency Inversion

High-level modules depend on abstractions (interfaces) rather than concrete implementations, enabling testability and flexibility.

### 5. Single Source of Truth

State is managed centrally in the ViewModel, with a single source of truth for the UI.

---

## Layered Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      UI Layer (Compose)                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Main.kt    │  │  Theme.kt    │  │  Screens.kt  │      │
│  │  (Entry Pt)  │  │  (Theming)   │  │  (UI Comps)  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└──────────────────────────┬──────────────────────────────────┘
                           │ StateFlow
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                 Presentation Layer                            │
│  ┌──────────────────────────────────────────────────────┐   │
│  │           WorkstationViewModel                        │   │
│  │  • State Management (StateFlow)                      │   │
│  │  • Business Logic                                    │   │
│  │  • User Action Handlers                              │   │
│  │  • Lifecycle Management (ViewModel)                  │   │
│  └──────────────────────────────────────────────────────┘   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Config    │  │   State      │  │   Render     │      │
│  │  Classes    │  │   Models     │  │   Data       │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└──────────────────────────┬──────────────────────────────────┘
                           │ Repository Interface
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                      Data Layer                               │
│  ┌──────────────────────────────────────────────────────┐   │
│  │         WorkstationRepository (Interface)             │   │
│  └──────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │       WorkstationRepositoryImpl                       │   │
│  │  • Data Loading                                       │   │
│  │  • JSON Parsing                                       │   │
│  │  • Validation Delegation                               │   │
│  └──────────────────────────────────────────────────────┘   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Models     │  │ Serialization│  │  Validation  │      │
│  └──────────────┘  └──────────────┘  └─────��────────┘      │
└──────────────────────────┬──────────────────────────────────┘
                           │ Platform Utilities
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                 Platform Layer                                │
│  ┌──────────────────┐      ┌──────────────────┐             │
│  │   JVM Utils     │      │    JS Utils      │             │
│  │  • File I/O      │      │  • Date/Time     │             │
│  │  • Date/Time     │      │  • Resource I/O  │             │
│  └──────────────────┘      └──────────────────┘             │
└─────────────────────────────────────────────────────────────┘
```

### Layer Responsibilities

#### UI Layer

- **Responsibility**: Render UI and handle user interactions
- **Technologies**: Compose Multiplatform
- **Key Components**:
  - Entry points (`Main.kt`)
  - Theme configuration
  - Screen composables
  - Interactive components

#### Presentation Layer

- **Responsibility**: Manage state and business logic
- **Technologies**: ViewModel, StateFlow, Coroutines
- **Key Components**:
  - `WorkstationViewModel`
  - State models (`WorkstationUiState`, `DiagramState`)
  - Configuration classes
  - Render data models

#### Data Layer

- **Responsibility**: Data access, transformation, and validation
- **Technologies**: Repository Pattern, Kotlinx Serialization
- **Key Components**:
  - Repository interface and implementation
  - Data models (serializable)
  - Serialization logic
  - Validation logic

#### Platform Layer

- **Responsibility**: Platform-specific implementations
- **Technologies**: Expect/Actual declarations
- **Key Components**:
  - Platform utilities
  - File I/O
  - Date/time handling

---

## Multiplatform Strategy

### Source Set Organization

```
src/
├── commonMain/              # Shared across all platforms
│   ├── kotlin/
│   │   └── dev/akexorcist/workstation/
│   │       ├── data/       # Data models, repository
│   │       ├── presentation/ # ViewModel, state
│   │       ├── ui/         # Compose UI components
│   │       └── utils/      # Platform utility declarations
│   └── resources/
├── jvmMain/                 # Desktop-specific
│   └── kotlin/
│       └── dev/akexorcist/workstation/
│           ├── Main.kt     # Desktop entry point
│           └── utils/      # JVM utility implementations
└── jsMain/                  # Web-specific
    └── kotlin/
        └── dev/akexorcist/workstation/
            ├── Main.kt     # Web entry point
            └── utils/      # JS utility implementations
```

### Expect/Actual Pattern

Platform-specific code is isolated using Kotlin's expect/actual mechanism:

#### Declaration (commonMain)

```kotlin
// src/commonMain/kotlin/dev/akexorcist/workstation/utils/PlatformUtils.kt
package dev.akexorcist.workstation.utils

expect fun getCurrentDate(): String
expect fun readResourceFile(path: String): String
```

#### Implementation (jvmMain)

```kotlin
// src/jvmMain/kotlin/dev/akexorcist/workstation/utils/PlatformUtils.kt
package dev.akexorcist.workstation.utils

import java.time.LocalDate

actual fun getCurrentDate(): String {
    return LocalDate.now().toString()
}

actual fun readResourceFile(path: String): String {
    val inputStream = Thread.currentThread()
        .contextClassLoader
        .getResourceAsStream(path)
        ?: throw IllegalStateException("Resource file not found: $path")
    return inputStream.bufferedReader().use { it.readText() }
}
```

#### Implementation (jsMain)

```kotlin
// src/jsMain/kotlin/dev/akexorcist/workstation/utils/PlatformUtils.kt
package dev.akexorcist.workstation.utils

import kotlin.js.Date

actual fun getCurrentDate(): String {
    val date = Date()
    return date.toISOString().split("T")[0]
}

actual fun readResourceFile(path: String): String {
    throw NotImplementedError("Async resource loading not implemented for JS target")
}
```

### Platform-Specific Dependencies

#### JVM Dependencies

```kotlin
val jvmMain by getting {
    dependencies {
        implementation(compose.desktop.currentOs)
    }
}
```

#### JavaScript Dependencies

```kotlin
val jsMain by getting {
    dependencies {
        implementation(compose.html.core)
    }
}
```

---

## Design Patterns

### 1. Repository Pattern

**Purpose**: Abstract data access and provide a clean interface for the presentation layer.

```kotlin
interface WorkstationRepository {
    suspend fun loadLayout(): LoadResult
    suspend fun loadLayoutFromJson(jsonString: String): LoadResult
    fun validateLayout(layout: WorkstationLayout): ValidationResult
}

class WorkstationRepositoryImpl : WorkstationRepository {
    // Implementation details...
}
```

**Benefits**:
- Separates data access logic from business logic
- Enables easy testing with mock implementations
- Provides a single point of control for data operations

### 2. MVVM (Model-View-ViewModel)

**Purpose**: Separate UI from business logic and state management.

```kotlin
// Model
data class WorkstationUiState(
    val isLoading: Boolean = false,
    val layout: WorkstationLayout? = null,
    // ...
)

// ViewModel
class WorkstationViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WorkstationUiState())
    val uiState: StateFlow<WorkstationUiState> = _uiState.asStateFlow()
}

// View (Compose)
@Composable
fun WorkstationScreen(viewModel: WorkstationViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    // UI implementation
}
```

**Benefits**:
- Clear separation of concerns
- Testable business logic
- Reactive UI updates

### 3. Observer Pattern (StateFlow)

**Purpose**: Enable reactive state updates across the application.

```kotlin
class WorkstationViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WorkstationUiState())
    val uiState: StateFlow<WorkstationUiState> = _uiState.asStateFlow()
}

@Composable
fun WorkstationScreen(viewModel: WorkstationViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    // Automatically recomposes when uiState changes
}
```

**Benefits**:
- Automatic UI updates
- Thread-safe state management
- Backpressure handling

### 4. Sealed Class Pattern

**Purpose**: Represent restricted class hierarchies for type-safe state handling.

```kotlin
sealed class LoadResult {
    data class Success(val layout: WorkstationLayout) : LoadResult()
    data class PartialSuccess(val layout: WorkstationLayout, val errors: List<String>) : LoadResult()
    data class Error(val message: String, val cause: Throwable? = null) : LoadResult()
}

when (val result = repository.loadLayout()) {
    is LoadResult.Success -> { /* handle success */ }
    is LoadResult.PartialSuccess -> { /* handle partial success */ }
    is LoadResult.Error -> { /* handle error */ }
}
```

**Benefits**:
- Type-safe exhaustive handling
- Compiler-checked when expressions
- Clear representation of states

### 5. Strategy Pattern

**Purpose**: Define a family of algorithms and make them interchangeable.

```kotlin
interface ViewportConfig {
    companion object {
        val defaultCanvasSize = Size(1920f, 1080f)
        val defaultZoom = 1.0f
        const val viewportCullingMargin = 100f
    }
}

object StateManagementConfig {
    fun validateZoom(zoom: Float): Float {
        return zoom.coerceIn(0.1f, 5.0f)
    }

    fun validatePan(offset: Offset, canvasSize: Size): Offset {
        // Validation logic
    }
}
```

**Benefits**:
- Encapsulated algorithms
- Easy to extend with new strategies
- Centralized configuration

---

## Data Flow

### Loading Data Flow

```
User Action (App Start)
    │
    ▼
┌─────────────────────────────────────────┐
│  UI Layer (LaunchedEffect)              │
│  LaunchedEffect(Unit) {                │
│      viewModel.loadLayout()            │
│  }                                      │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│  Presentation Layer                     │
│  WorkstationViewModel                   │
│  suspend fun loadLayout() {             │
│      _uiState.value = copy(isLoading)   │
│      val result = repository.load()     │
│      _uiState.value = update(result)   │
│  }                                      │
└──────────────┬──────────────────────────┘
               │
               ▼
┌──────────────���────────���─────────────────┐
│  Data Layer                             │
│  WorkstationRepository                  │
│  suspend fun loadLayout(): LoadResult { │
│      val json = readFile()              │
│      return parseJson(json)             │
│  }                                      │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│  Platform Layer                        │
│  PlatformUtils                          │
│  fun readResourceFile(path): String {   │
│      // Platform-specific I/O          │
│  }                                      │
└──────────────┬──────────────────────────┘
               │
               ▼
         Data Loaded
               │
               ▼
┌─────────────────────────────────────────┐
│  StateFlow Update                       │
│  _uiState.value = newState              │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│  UI Layer (Automatic Recomposition)     │
│  val uiState by viewModel.uiState        │
│      .collectAsState()                   │
│  // UI recomposes with new state         │
└─────────────────────────────────────────┘
```

### User Interaction Flow

```
User Action (e.g., Zoom)
    │
    ▼
┌─────────────────────────────────────────┐
│  UI Layer (Compose)                     │
│  Slider(onValueChange = {               │
│      viewModel.handleZoomChange(it)     │
│  })                                     ���
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│  Presentation Layer                     │
│  WorkstationViewModel                   │
│  fun handleZoomChange(zoom: Float) {    │
│      val validated = validateZoom(zoom) │
│      _uiState.value = copy(zoom)         │
│      updateDiagramState()               │
│  }                                      │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│  StateFlow Update                       │
│  _uiState.value = newState              │
│  _diagramState.value = newState         │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│  UI Layer (Automatic Recomposition)     │
│  // UI recomposes with new zoom level    │
└─────────────────────────────────────────┘
```

---

## Component Communication

### ViewModel to UI Communication

**Mechanism**: StateFlow

```kotlin
// ViewModel
class WorkstationViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WorkstationUiState())
    val uiState: StateFlow<WorkstationUiState> = _uiState.asStateFlow()
}

// UI
@Composable
fun WorkstationScreen(viewModel: WorkstationViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    // UI automatically updates when uiState changes
}
```

### UI to ViewModel Communication

**Mechanism**: Direct function calls

```kotlin
// UI
@Composable
fun ZoomControl(viewModel: WorkstationViewModel) {
    Slider(
        value = uiState.zoom,
        onValueChange = { viewModel.handleZoomChange(it) }
    )
}

// ViewModel
class WorkstationViewModel : ViewModel() {
    fun handleZoomChange(zoom: Float) {
        // Handle zoom change
    }
}
```

### ViewModel to Repository Communication

**Mechanism**: Suspend function calls

```kotlin
// ViewModel
class WorkstationViewModel : ViewModel() {
    suspend fun loadLayout() {
        val result = repository.loadLayout()
        // Handle result
    }
}

// Repository
interface WorkstationRepository {
    suspend fun loadLayout(): LoadResult
}
```

### Repository to Platform Layer Communication

**Mechanism**: Expect/actual function calls

```kotlin
// Repository
class WorkstationRepositoryImpl : WorkstationRepository {
    private fun readFile(): String {
        return readResourceFile("/data/workstation.json")
    }
}

// Platform (common)
expect fun readResourceFile(path: String): String

// Platform (JVM)
actual fun readResourceFile(path: String): String {
    // JVM-specific implementation
}
```

---

## Error Handling

### Error Handling Strategy

The application uses a multi-layered error handling approach:

#### 1. Repository Layer

```kotlin
sealed class LoadResult {
    data class Success(val layout: WorkstationLayout) : LoadResult()
    data class PartialSuccess(val layout: WorkstationLayout, val errors: List<String>) : LoadResult()
    data class Error(val message: String, val cause: Throwable? = null) : LoadResult()
}
```

#### 2. ViewModel Layer

```kotlin
suspend fun loadLayout() {
    try {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        when (val result = repository.loadLayout()) {
            is LoadResult.Success -> {
                _uiState.value = _uiState.value.copy(
                    layout = result.layout,
                    isLoading = false
                )
            }
            is LoadResult.Error -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.message
                )
            }
        }
    } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = "Unexpected error: ${e.message}"
        )
    }
}
```

#### 3. UI Layer

```kotlin
@Composable
fun WorkstationScreen(viewModel: WorkstationViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.errorMessage != null) {
        ErrorDialog(
            message = uiState.errorMessage,
            onDismiss = { viewModel.clearError() }
        )
    }
}
```

### Error Types

| Error Type | Layer | Handling |
|-------------|-------|----------|
| File Not Found | Repository | Returns `LoadResult.Error` |
| Parse Error | Repository | Returns `LoadResult.Error` |
| Validation Error | Repository | Returns `LoadResult.PartialSuccess` |
| Network Error | Repository | Returns `LoadResult.Error` |
| Unexpected Error | ViewModel | Catches and displays generic error |

---

## Performance Considerations

### 1. State Management

**Optimization**: Use StateFlow for efficient state updates

```kotlin
// StateFlow only emits when value actually changes
private val _uiState = MutableStateFlow(WorkstationUiState())
val uiState: StateFlow<WorkstationUiState> = _uiState.asStateFlow()
```

**Benefits**:
- Conflation of rapid updates
- Thread-safe
- Backpressure support

### 2. Viewport Culling

**Optimization**: Only render visible elements

```kotlin
private fun isDeviceVisible(device: Device, zoom: Float, pan: Offset): Boolean {
    val screenPosition = worldToScreen(device.position, zoom, pan)
    val screenSize = Size(device.size.width * zoom, device.size.height * zoom)

    val deviceRect = Rect(
        left = screenPosition.x,
        top = screenPosition.y,
        right = screenPosition.x + screenSize.width,
        bottom = screenPosition.y + screenSize.height
    )

    val viewportRect = Rect(
        left = -ViewportConfig.viewportCullingMargin,
        top = -ViewportConfig.viewportCullingMargin,
        right = 1920f + ViewportConfig.viewportCullingMargin,
        bottom = 1080f + ViewportConfig.viewportCullingMargin
    )

    return deviceRect.intersects(viewportRect)
}
```

**Benefits**:
- Reduced rendering overhead
- Better performance with large diagrams
- Smoother zooming and panning

### 3. Lazy Computation

**Optimization**: Compute render data only when needed

```kotlin
private fun updateDiagramState() {
    val layout = _uiState.value.layout ?: return
    // Only compute if layout exists
    // ...
}
```

**Benefits**:
- Avoids unnecessary computations
- Reduces memory usage
- Improves responsiveness

### 4. Coroutine Dispatchers

**Optimization**: Use appropriate dispatchers for different tasks

```kotlin
// CPU-intensive work
withContext(Dispatchers.Default) {
    // Computation
}

// I/O operations
withContext(Dispatchers.IO) {
    // File/network operations
}
```

**Benefits**:
- Optimal thread utilization
- Prevents blocking UI thread
- Better responsiveness

### 5. Memory Management

**Optimization**: Use ViewModel lifecycle for automatic cleanup

```kotlin
class WorkstationViewModel : ViewModel() {
    // Automatically cleared when ViewModel is destroyed
    private val _uiState = MutableStateFlow(WorkstationUiState())

    override fun onCleared() {
        // Cleanup if needed
        super.onCleared()
    }
}
```

**Benefits**:
- Automatic resource cleanup
- Prevents memory leaks
- Proper lifecycle management

---

## Summary

The Workstation Diagram application architecture is designed with the following key principles:

1. **Clean Separation**: Each layer has a clear, single responsibility
2. **Platform Agnosticism**: Common code is truly platform-independent
3. **Reactive Programming**: State changes automatically propagate to UI
4. **Testability**: Dependencies are inverted for easy testing
5. **Performance**: Optimizations for rendering and state management
6. **Maintainability**: Clear patterns and conventions throughout

This architecture provides a solid foundation for future development and ensures the application can scale across multiple platforms while maintaining code quality and performance.