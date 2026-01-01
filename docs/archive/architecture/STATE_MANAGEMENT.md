# State Management Guide

## Overview

This document provides a comprehensive guide to state management in the Workstation Diagram application. It covers the state management architecture, patterns, and best practices used throughout the project.

## Table of Contents

- [State Management Architecture](#state-management-architecture)
- [ViewModel Implementation](#viewmodel-implementation)
- [StateFlow Usage](#stateflow-usage)
- [State Models](#state-models)
- [State Updates](#state-updates)
- [State Collection in Compose](#state-collection-in-compose)
- [Lifecycle Management](#lifecycle-management)
- [Best Practices](#best-practices)

---

## State Management Architecture

### Architecture Diagram

```
┌────────────────────���───────��────────────────────────────────┐
│                      UI Layer (Compose)                       │
│                                                               │
│  @Composable                                                  │
│  fun WorkstationScreen(viewModel: WorkstationViewModel) {   │
│      val uiState by viewModel.uiState.collectAsState()        │
│      // UI automatically updates when uiState changes         │
│  }                                                            │
└──────────────────────────┬──────────────────────────────────┘
                           │ collectAsState()
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                 Presentation Layer                            │
│                                                               │
│  class WorkstationViewModel : ViewModel {                    │
│      private val _uiState = MutableStateFlow(                │
│          WorkstationUiState(isLoading = true)                 │
│      )                                                        │
│      val uiState: StateFlow<WorkstationUiState> =            │
│          _uiState.asStateFlow()                               │
│                                                               │
│      fun handleZoomChange(zoom: Float) {                     │
│          _uiState.value = _uiState.value.copy(zoom = zoom)    │
│      }                                                        │
│  }                                                            │
└─────────────────────────────────────────────────────────────┘
```

### Key Components

1. **ViewModel**: Holds and manages state
2. **StateFlow**: Reactive state container
3. **Compose UI**: Collects and reacts to state changes

---

## ViewModel Implementation

### Lifecycle ViewModel

The project uses JetBrains' Lifecycle ViewModel for Kotlin Multiplatform:

```kotlin
package dev.akexorcist.workstation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkstationViewModel(
    private val repository: WorkstationRepository = WorkstationRepositoryImpl()
) : ViewModel() {
    // State management
    private val _uiState = MutableStateFlow(WorkstationUiState(isLoading = true))
    val uiState: StateFlow<WorkstationUiState> = _uiState.asStateFlow()

    private val _diagramState = MutableStateFlow(DiagramState())
    val diagramState: StateFlow<DiagramState> = _diagramState.asStateFlow()

    // Suspend functions for async operations
    suspend fun loadLayout() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        when (val result = repository.loadLayout()) {
            is LoadResult.Success -> {
                _uiState.value = _uiState.value.copy(
                    layout = result.layout,
                    isLoading = false
                )
                updateDiagramState()
            }
            is LoadResult.PartialSuccess -> {
                _uiState.value = _uiState.value.copy(
                    layout = result.layout,
                    isLoading = false,
                    errorMessage = "Loaded with warnings: ${result.errors.joinToString(", ")}"
                )
                updateDiagramState()
            }
            is LoadResult.Error -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.message
                )
            }
        }
    }

    // State update functions
    fun handleZoomChange(zoom: Float) {
        val validatedZoom = StateManagementConfig.validateZoom(zoom)
        _uiState.value = _uiState.value.copy(zoom = validatedZoom)
        updateDiagramState()
    }

    fun handlePanChange(offset: Offset) {
        val canvasSize = _uiState.value.layout?.metadata?.canvasSize ?: ViewportConfig.defaultCanvasSize
        val validatedPan = StateManagementConfig.validatePan(offset, canvasSize)
        _uiState.value = _uiState.value.copy(panOffset = validatedPan)
        updateDiagramState()
    }

    fun handleDeviceClick(deviceId: String) {
        _uiState.value = _uiState.value.copy(
            selectedDeviceId = deviceId,
            selectedConnectionId = null
        )
        updateDiagramState()
    }

    fun handleConnectionClick(connectionId: String) {
        _uiState.value = _uiState.value.copy(
            selectedConnectionId = connectionId,
            selectedDeviceId = null
        )
        updateDiagramState()
    }

    fun deselectAll() {
        _uiState.value = _uiState.value.copy(
            selectedDeviceId = null,
            selectedConnectionId = null
        )
        updateDiagramState()
    }

    fun toggleTheme() {
        _uiState.value = _uiState.value.copy(isDarkTheme = !_uiState.value.isDarkTheme)
    }

    fun searchDevices(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)

        val layout = _uiState.value.layout ?: return
        val filteredIds = if (query.isBlank()) {
            emptySet()
        } else {
            layout.devices
                .filter { device ->
                    device.name.contains(query, ignoreCase = true) ||
                    device.model.contains(query, ignoreCase = true)
                }
                .map { it.id }
                .toSet()
        }

        _uiState.value = _uiState.value.copy(filteredDeviceIds = filteredIds)
        updateDiagramState()
    }

    fun resetZoom() {
        _uiState.value = _uiState.value.copy(zoom = ViewportConfig.defaultZoom)
        updateDiagramState()
    }

    fun resetPan() {
        _uiState.value = _uiState.value.copy(panOffset = StateManagementConfig.initialPan)
        updateDiagramState()
    }

    private fun updateDiagramState() {
        val layout = _uiState.value.layout ?: return
        val zoom = _uiState.value.zoom
        val pan = _uiState.value.panOffset

        val deviceRenderData = layout.devices.map { device ->
            val screenPosition = worldToScreen(device.position, zoom, pan)
            val screenSize = Size(device.size.width * zoom, device.size.height * zoom)
            val isVisible = isDeviceVisible(device, zoom, pan)
            val isSelected = device.id == _uiState.value.selectedDeviceId

            DeviceRenderData(
                device = device,
                screenPosition = screenPosition,
                screenSize = screenSize,
                isVisible = isVisible,
                isSelected = isSelected,
                isHovered = false
            )
        }

        val connectionRenderData = layout.connections.map { connection ->
            val sourceDevice = layout.devices.find { it.id == connection.sourceDeviceId }
            val targetDevice = layout.devices.find { it.id == connection.targetDeviceId }

            val path = if (sourceDevice != null && targetDevice != null) {
                calculateStraightPath(connection, sourceDevice, targetDevice, zoom, pan)
            } else {
                emptyList()
            }

            val isVisible = isConnectionVisible(path, zoom, pan)
            val isSelected = connection.id == _uiState.value.selectedConnectionId

            ConnectionRenderData(
                connection = connection,
                path = path,
                isVisible = isVisible,
                isSelected = isSelected,
                isHovered = false
            )
        }

        _diagramState.value = _diagramState.value.copy(
            scale = zoom,
            deviceRenderData = deviceRenderData,
            connectionRenderData = connectionRenderData
        )
    }

    // Helper functions for coordinate transformations
    private fun worldToScreen(position: Position, zoom: Float, pan: Offset): Offset {
        return Offset(
            x = position.x * zoom + pan.x,
            y = position.y * zoom + pan.y
        )
    }

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

    private fun isConnectionVisible(path: List<Point>, zoom: Float, pan: Offset): Boolean {
        if (path.isEmpty()) return false

        val screenPoints = path.map { point ->
            worldToScreen(Position(point.x, point.y), zoom, pan)
        }

        val minX = screenPoints.minOf { it.x } - ViewportConfig.viewportCullingMargin
        val maxX = screenPoints.maxOf { it.x } + ViewportConfig.viewportCullingMargin
        val minY = screenPoints.minOf { it.y } - ViewportConfig.viewportCullingMargin
        val maxY = screenPoints.maxOf { it.y } + ViewportConfig.viewportCullingMargin

        val pathRect = Rect(minX, minY, maxX, maxY)
        val viewportRect = Rect(0f, 0f, 1920f, 1080f)

        return pathRect.intersects(viewportRect)
    }

    private fun calculateStraightPath(
        connection: Connection,
        sourceDevice: Device,
        targetDevice: Device,
        zoom: Float,
        pan: Offset
    ): List<Point> {
        val sourcePort = sourceDevice.ports.find { it.id == connection.sourcePortId }
        val targetPort = targetDevice.ports.find { it.id == connection.targetPortId }

        if (sourcePort == null || targetPort == null) return emptyList()

        val sourcePosition = calculatePortWorldPosition(sourceDevice, sourcePort)
        val targetPosition = calculatePortWorldPosition(targetDevice, targetPort)

        return listOf(sourcePosition, targetPosition)
    }

    private fun calculatePortWorldPosition(device: Device, port: Port): Point {
        val deviceRect = Rect(
            device.position.x,
            device.position.y,
            device.position.x + device.size.width,
            device.position.y + device.size.height
        )

        val offset = when {
            port.position.offset == 0f -> 0.01f
            port.position.offset == 1f -> 0.99f
            else -> port.position.offset
        }

        return when (port.position.side) {
            DeviceSide.TOP -> Point(
                deviceRect.left + (deviceRect.width * offset),
                deviceRect.top
            )
            DeviceSide.BOTTOM -> Point(
                deviceRect.left + (deviceRect.width * offset),
                deviceRect.bottom
            )
            DeviceSide.LEFT -> Point(
                deviceRect.left,
                deviceRect.top + (deviceRect.height * offset)
            )
            DeviceSide.RIGHT -> Point(
                deviceRect.right,
                deviceRect.top + (deviceRect.height * offset)
            )
        }
    }
}
```

### Why Use Lifecycle ViewModel?

1. **Lifecycle Awareness**: Automatically cleared when no longer needed
2. **Coroutine Scope**: Built-in `viewModelScope` for coroutines
3. **State Preservation**: Survives configuration changes
4. **Cross-Platform**: Works on JVM, JS, and other platforms
5. **Testability**: Easy to test with mock dependencies

---

## StateFlow Usage

### MutableStateFlow vs StateFlow

```kotlin
// MutableStateFlow - for internal state updates
private val _uiState = MutableStateFlow(WorkstationUiState(isLoading = true))

// StateFlow - for external read-only access
val uiState: StateFlow<WorkstationUiState> = _uiState.asStateFlow()
```

### StateFlow Characteristics

| Characteristic | Description |
|----------------|-------------|
| **Thread-Safe** | Safe to update from any coroutine |
| **Conflation** | Rapid updates are conflated to latest value |
| **Always Active** | Emits current value to new subscribers |
| **Backpressure** | Handles slow consumers automatically |

### StateFlow vs LiveData

| Feature | StateFlow | LiveData |
|---------|-----------|----------|
| **Coroutines** | Native support | Requires adapters |
| **Multiplatform** | Yes | No (Android only) |
| **Always Active** | Yes | No (needs observer) |
| **Operators** | Rich flow operators | Limited |

---

## State Models

### WorkstationUiState

```kotlin
data class WorkstationUiState(
    val isLoading: Boolean = false,
    val layout: WorkstationLayout? = null,
    val errorMessage: String? = null,
    val zoom: Float = 1.0f,
    val panOffset: Offset = Offset.Zero,
    val selectedDeviceId: String? = null,
    val selectedConnectionId: String? = null,
    val searchQuery: String = "",
    val filteredDeviceIds: Set<String> = emptySet(),
    val isDarkTheme: Boolean = true
)
```

**State Fields**:

| Field | Type | Purpose |
|-------|------|---------|
| `isLoading` | Boolean | Loading state indicator |
| `layout` | WorkstationLayout? | Current workstation layout |
| `errorMessage` | String? | Error message to display |
| `zoom` | Float | Current zoom level |
| `panOffset` | Offset | Current pan offset |
| `selectedDeviceId` | String? | Currently selected device |
| `selectedConnectionId` | String? | Currently selected connection |
| `searchQuery` | String | Current search query |
| `filteredDeviceIds` | Set<String> | IDs of filtered devices |
| `isDarkTheme` | Boolean | Theme preference |

### DiagramState

```kotlin
data class DiagramState(
    val scale: Float = 1.0f,
    val deviceRenderData: List<DeviceRenderData> = emptyList(),
    val connectionRenderData: List<ConnectionRenderData> = emptyList()
)
```

**State Fields**:

| Field | Type | Purpose |
|-------|------|---------|
| `scale` | Float | Current scale for rendering |
| `deviceRenderData` | List<DeviceRenderData> | Pre-computed device render data |
| `connectionRenderData` | List<ConnectionRenderData> | Pre-computed connection render data |

### DeviceRenderData

```kotlin
data class DeviceRenderData(
    val device: Device,
    val screenPosition: Offset,
    val screenSize: Size,
    val isVisible: Boolean,
    val isSelected: Boolean,
    val isHovered: Boolean
)
```

### ConnectionRenderData

```kotlin
data class ConnectionRenderData(
    val connection: Connection,
    val path: List<Point>,
    val isVisible: Boolean,
    val isSelected: Boolean,
    val isHovered: Boolean
)
```

---

## State Updates

### Immutable State Pattern

All state updates use immutable data classes with the `copy` function:

```kotlin
// ❌ Bad: Mutable state
data class WorkstationUiState(
    var isLoading: Boolean = false
)

fun updateLoading() {
    _uiState.value.isLoading = true  // Direct mutation
}

// ✅ Good: Immutable state
data class WorkstationUiState(
    val isLoading: Boolean = false
)

fun updateLoading() {
    _uiState.value = _uiState.value.copy(isLoading = true)  // Immutable update
}
```

### State Update Examples

#### Simple Update

```kotlin
fun toggleTheme() {
    _uiState.value = _uiState.value.copy(isDarkTheme = !_uiState.value.isDarkTheme)
}
```

#### Update with Validation

```kotlin
fun handleZoomChange(zoom: Float) {
    val validatedZoom = StateManagementConfig.validateZoom(zoom)
    _uiState.value = _uiState.value.copy(zoom = validatedZoom)
    updateDiagramState()
}
```

#### Update with Computation

```kotlin
fun searchDevices(query: String) {
    _uiState.value = _uiState.value.copy(searchQuery = query)

    val layout = _uiState.value.layout ?: return
    val filteredIds = if (query.isBlank()) {
        emptySet()
    } else {
        layout.devices
            .filter { device ->
                device.name.contains(query, ignoreCase = true) ||
                device.model.contains(query, ignoreCase = true)
            }
            .map { it.id }
            .toSet()
    }

    _uiState.value = _uiState.value.copy(filteredDeviceIds = filteredIds)
    updateDiagramState()
}
```

#### Multiple Field Update

```kotlin
fun deselectAll() {
    _uiState.value = _uiState.value.copy(
        selectedDeviceId = null,
        selectedConnectionId = null
    )
    updateDiagramState()
}
```

---

## State Collection in Compose

### collectAsState()

The primary way to collect StateFlow in Compose:

```kotlin
@Composable
fun WorkstationTheme(
    viewModel: WorkstationViewModel,
    content: @Composable () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadLayout()
    }

    val colorScheme = if (uiState.isDarkTheme) {
        darkColorScheme()
    } else {
        lightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
```

### LaunchedEffect for Side Effects

```kotlin
@Composable
fun WorkstationScreen(viewModel: WorkstationViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    // Load data when composition starts
    LaunchedEffect(Unit) {
        viewModel.loadLayout()
    }

    // React to specific state changes
    LaunchedEffect(uiState.layout) {
        if (uiState.layout != null) {
            // Layout loaded, perform action
        }
    }
}
```

### rememberCoroutineScope for User Actions

```kotlin
@Composable
fun ReloadButton(viewModel: WorkstationViewModel) {
    val coroutineScope = rememberCoroutineScope()

    Button(
        onClick = {
            coroutineScope.launch {
                viewModel.loadLayout()
            }
        }
    ) {
        Text("Reload")
    }
}
```

---

## Lifecycle Management

### ViewModel Lifecycle

```kotlin
class WorkstationViewModel(
    private val repository: WorkstationRepository = WorkstationRepositoryImpl()
) : ViewModel() {

    // ViewModel is created when needed
    init {
        // Initialization code
    }

    // ViewModel is cleared when no longer needed
    override fun onCleared() {
        // Cleanup resources
        super.onCleared()
    }
}
```

### viewModelScope

The ViewModel provides a `viewModelScope` that is automatically cancelled when the ViewModel is cleared:

```kotlin
class WorkstationViewModel : ViewModel() {
    fun performLongRunningTask() {
        viewModelScope.launch {
            // This coroutine is automatically cancelled
            // when the ViewModel is cleared
        }
    }
}
```

### Compose Lifecycle

```kotlin
@Composable
fun WorkstationScreen(viewModel: WorkstationViewModel) {
    // LaunchedEffect is cancelled when composable leaves composition
    LaunchedEffect(Unit) {
        viewModel.loadLayout()
    }

    // rememberCoroutineScope is cancelled when composable leaves composition
    val coroutineScope = rememberCoroutineScope()

    Button(
        onClick = {
            coroutineScope.launch {
                viewModel.loadLayout()
            }
        }
    ) {
        Text("Reload")
    }
}
```

---

## Best Practices

### 1. Keep State Immutable

```kotlin
// ✅ Good: Immutable state
data class WorkstationUiState(
    val isLoading: Boolean = false
)

// ❌ Bad: Mutable state
data class WorkstationUiState(
    var isLoading: Boolean = false
)
```

### 2. Use Suspend Functions for Async Operations

```kotlin
// ✅ Good: Suspend function
suspend fun loadLayout() {
    val result = repository.loadLayout()
    // Handle result
}

// ❌ Bad: Manual coroutine scope
private val viewModelScope = CoroutineScope(Dispatchers.Main)

fun loadLayout() {
    viewModelScope.launch {
        val result = repository.loadLayout()
        // Handle result
    }
}
```

### 3. Separate UI State from Render State

```kotlin
// UI State - What to display
private val _uiState = MutableStateFlow(WorkstationUiState())
val uiState: StateFlow<WorkstationUiState> = _uiState.asStateFlow()

// Render State - How to display
private val _diagramState = MutableStateFlow(DiagramState())
val diagramState: StateFlow<DiagramState> = _diagramState.asStateFlow()
```

### 4. Validate State Updates

```kotlin
fun handleZoomChange(zoom: Float) {
    val validatedZoom = StateManagementConfig.validateZoom(zoom)
    _uiState.value = _uiState.value.copy(zoom = validatedZoom)
}
```

### 5. Use LaunchedEffect for One-Time Operations

```kotlin
@Composable
fun WorkstationScreen(viewModel: WorkstationViewModel) {
    // ✅ Good: Load once when composition starts
    LaunchedEffect(Unit) {
        viewModel.loadLayout()
    }

    // ❌ Bad: Load on every recomposition
    SideEffect {
        viewModel.loadLayout()
    }
}
```

### 6. Use Derived State for Computed Values

```kotlin
// ✅ Good: Compute in ViewModel
private fun updateDiagramState() {
    val deviceRenderData = layout.devices.map { /* ... */ }
    _diagramState.value = _diagramState.value.copy(
        deviceRenderData = deviceRenderData
    )
}

// ❌ Bad: Compute in UI
@Composable
fun WorkstationScreen(viewModel: WorkstationViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val deviceRenderData = uiState.layout?.devices?.map { /* ... */ } ?: emptyList()
    // Recomputes on every recomposition
}
```

### 7. Handle Errors Gracefully

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

### 8. Use Sealed Classes for State Representations

```kotlin
// ✅ Good: Sealed class for load results
sealed class LoadResult {
    data class Success(val layout: WorkstationLayout) : LoadResult()
    data class PartialSuccess(val layout: WorkstationLayout, val errors: List<String>) : LoadResult()
    data class Error(val message: String, val cause: Throwable? = null) : LoadResult()
}

// ❌ Bad: Multiple nullable fields
data class LoadResult(
    val layout: WorkstationLayout? = null,
    val errors: List<String>? = null,
    val errorMessage: String? = null
)
```

---

## Summary

State management in the Workstation Diagram application follows these key principles:

1. **ViewModel**: Centralized state management with lifecycle awareness
2. **StateFlow**: Reactive state container with automatic updates
3. **Immutable State**: All state updates use immutable data classes
4. **Suspend Functions**: Async operations without manual coroutine scopes
5. **Compose Integration**: Seamless state collection with `collectAsState()`
6. **Validation**: State updates are validated before application
7. **Error Handling**: Graceful error handling with user feedback
8. **Performance**: Optimized rendering with viewport culling and derived state

This approach provides a robust, maintainable, and performant state management solution for the multiplatform application.