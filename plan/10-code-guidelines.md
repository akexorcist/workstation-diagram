# Code Guidelines

This document defines the coding standards and best practices for the Interactive Workstation Diagram project.

## Core Principles

### 1. Code Clarity Over Comments

**Rule:** No useless comments. Code should be self-documenting.

**Good:**
```kotlin
fun calculatePortPosition(device: Device, port: Port): Point {
    val deviceRect = device.bounds
    return when (port.position.side) {
        DeviceSide.TOP -> Point(deviceRect.left + offsetX, deviceRect.top)
        DeviceSide.BOTTOM -> Point(deviceRect.left + offsetX, deviceRect.bottom)
        DeviceSide.LEFT -> Point(deviceRect.left, deviceRect.top + offsetY)
        DeviceSide.RIGHT -> Point(deviceRect.right, deviceRect.top + offsetY)
    }
}
```

**Bad:**
```kotlin
// Calculate port position
fun calculatePortPosition(device: Device, port: Port): Point {
    // Get device rectangle
    val deviceRect = device.bounds
    // Return point based on side
    return when (port.position.side) {
        // Top side
        DeviceSide.TOP -> Point(deviceRect.left + offsetX, deviceRect.top)
        // ... more useless comments
    }
}
```

**When to Comment:**
- **Why, not what:** Explain reasoning, not obvious code
- **Complex algorithms:** Document non-obvious logic or mathematical formulas
- **Business decisions:** Explain why a specific approach was chosen
- **Workarounds:** Document temporary solutions or known limitations
- **API contracts:** Document expected behavior, edge cases, preconditions

**Good Comments:**
```kotlin
// Use Manhattan distance for A* heuristic because it's better suited for
// orthogonal routing (preferring horizontal/vertical segments) and is faster
// to calculate than Euclidean distance.
fun calculateHeuristic(from: Point, to: Point): Float {
    return abs(from.x - to.x) + abs(from.y - to.y)
}

// Ports at offset 0.0 or 1.0 are at device corners. Add small offset to
// prevent connection lines from overlapping with device corner edges.
val adjustedOffset = when {
    portOffset == 0f -> 0.01f
    portOffset == 1f -> 0.99f
    else -> portOffset
}
```

### 2. Concise Code

**Rule:** Think twice before writing. Write the minimum code that clearly expresses intent.

**Good:**
```kotlin
val visibleDevices = devices.filter { it.bounds.intersects(viewport) }
```

**Bad:**
```kotlin
val visibleDevices = mutableListOf<Device>()
for (device in devices) {
    if (device.bounds.intersects(viewport)) {
        visibleDevices.add(device)
    }
}
```

**Principles:**
- Prefer expressions over statements
- Use Kotlin standard library functions (filter, map, find, etc.)
- Avoid unnecessary intermediate variables
- Use extension functions for clarity
- Prefer data classes for simple data holders

**Good:**
```kotlin
fun findDeviceById(id: String): Device? = devices.find { it.id == id }

val hasConnections = device.ports.any { port -> 
    connections.any { it.sourcePortId == port.id || it.targetPortId == port.id }
}
```

**Bad:**
```kotlin
fun findDeviceById(id: String): Device? {
    for (device in devices) {
        if (device.id == id) {
            return device
        }
    }
    return null
}
```

### 3. Idiomatic Kotlin

**Rule:** Be a Kotlin expert. Use idiomatic Kotlin patterns. Avoid unusual code patterns unless justified.

#### Use Kotlin Features

**Good:**
```kotlin
// Data classes
data class Position(val x: Float, val y: Float)

// Sealed classes for results
sealed class LoadResult {
    data class Success(val layout: WorkstationLayout) : LoadResult()
    data class Error(val message: String) : LoadResult()
}

// Extension functions
fun Point.toOffset() = Offset(x, y)
fun Device.bounds(): Rect = Rect(position.x, position.y, position.x + size.width, position.y + size.height)

// Smart casts
if (result is LoadResult.Success) {
    val layout = result.layout // Smart cast, no need for explicit cast
}

// When expressions
val categoryColor = when (device.category) {
    DeviceCategory.HUB -> Color.Green
    DeviceCategory.PERIPHERAL -> Color.Orange
    DeviceCategory.CENTRAL_DEVICE -> Color.Blue
}

// Null safety
val port = device.ports.firstOrNull { it.id == portId } ?: return null
```

**Bad:**
```kotlin
// Avoid Java-style patterns
class Position {
    private var x: Float = 0f
    private var y: Float = 0f
    
    fun getX(): Float = x
    fun setX(value: Float) { x = value }
}

// Avoid unnecessary null checks when Kotlin handles it
if (device != null) {
    if (device.ports != null) {
        // ...
    }
}

// Avoid explicit casts when smart casts work
if (result is LoadResult.Success) {
    val layout = result as LoadResult.Success // Unnecessary cast
}
```

#### Avoid Unusual Patterns

**If you must use an unusual pattern, document why:**

```kotlin
// Using @JvmField to avoid getter/setter overhead in hot path
// This is necessary because this class is accessed millions of times
// during pathfinding calculations.
@JvmField
var x: Float = 0f
```

#### Kotlin Best Practices

1. **Immutability First:**
   ```kotlin
   // Prefer val over var
   val devices: List<Device> = loadDevices()
   
   // Use immutable collections
   val connections: List<Connection> = layout.connections
   ```

2. **Use Scope Functions Appropriately:**
   ```kotlin
   // apply: object configuration
   val device = Device(...).apply {
       // configure device
   }
   
   // let: null checks and transformations
   device?.let { processDevice(it) }
   
   // with: non-extension context
   with(algorithmConfig) {
       // use config properties
   }
   
   // run: object initialization and computation
   val result = run {
       // complex computation
   }
   ```

3. **Prefer Expression Bodies:**
   ```kotlin
   fun isVisible(device: Device, viewport: Rect): Boolean = 
       device.bounds.intersects(viewport)
   ```

4. **Use Default Arguments:**
   ```kotlin
   fun createDevice(
       id: String,
       name: String,
       size: Size = DataDefaults.defaultDeviceSize,
       position: Position = DataDefaults.defaultDevicePosition
   ): Device
   ```

5. **Use Named Arguments for Clarity:**
   ```kotlin
   val point = Point(
       x = device.position.x + offset,
       y = device.position.y
   )
   ```

6. **Prefer Sealed Classes Over Enums for Complex Types:**
   ```kotlin
   sealed class ValidationError {
       data class DuplicateId(val id: String) : ValidationError()
       data class InvalidPosition(val deviceName: String) : ValidationError()
       object MissingRequiredField : ValidationError()
   }
   ```

### 4. Testability

**Rule:** Code must be written with testing in mind.

#### Principles

1. **Pure Functions:**
   - Prefer pure functions (no side effects)
   - Same input = same output
   - Easy to test

   ```kotlin
   // Good: Pure function
   fun calculateDistance(p1: Point, p2: Point): Float {
       val dx = p2.x - p1.x
       val dy = p2.y - p1.y
       return sqrt(dx * dx + dy * dy)
   }
   
   // Bad: Side effects, hard to test
   fun calculateDistance() {
       val p1 = getPointFromGlobalState()
       val p2 = getPointFromGlobalState()
       updateGlobalState(/* ... */)
   }
   ```

2. **Dependency Injection:**
   - Pass dependencies as parameters
   - Avoid global state
   - Use interfaces for testability

   ```kotlin
   // Good: Dependencies injected
   class ConnectionRouter(
       private val pathPlanner: PathPlanner,
       private val collisionDetector: CollisionDetector
   ) {
       fun routeConnection(connection: Connection): Path {
           // Use injected dependencies
       }
   }
   
   // Bad: Hard dependencies
   class ConnectionRouter {
       private val pathPlanner = PathPlanner() // Hard to mock
       fun routeConnection(connection: Connection): Path {
           // ...
       }
   }
   ```

3. **Separate Business Logic from Side Effects:**
   ```kotlin
   // Good: Logic separated
   fun validateDevice(device: Device): ValidationResult {
       // Pure validation logic
       return when {
           device.id.isEmpty() -> ValidationResult.Error("Empty ID")
           device.size.width <= 0 -> ValidationResult.Error("Invalid width")
           else -> ValidationResult.Success
       }
   }
   
   // Side effects in separate function
   suspend fun saveDevice(device: Device) {
       // I/O operations
   }
   ```

4. **Avoid Static/Global State:**
   ```kotlin
   // Good: State passed as parameter
   fun renderDevice(device: Device, viewport: Viewport, theme: Theme)
   
   // Bad: Global state
   object GlobalState {
       var viewport: Viewport? = null
   }
   fun renderDevice(device: Device) {
       val viewport = GlobalState.viewport // Hard to test
   }
   ```

5. **Make Functions Small and Focused:**
   ```kotlin
   // Good: Small, focused, testable
   fun calculatePortWorldPosition(device: Device, port: Port): Point {
       val deviceRect = device.bounds()
       return when (port.position.side) {
           DeviceSide.TOP -> calculateTopPosition(deviceRect, port.position.offset)
           DeviceSide.BOTTOM -> calculateBottomPosition(deviceRect, port.position.offset)
           DeviceSide.LEFT -> calculateLeftPosition(deviceRect, port.position.offset)
           DeviceSide.RIGHT -> calculateRightPosition(deviceRect, port.position.offset)
       }
   }
   
   private fun calculateTopPosition(rect: Rect, offset: Float): Point =
       Point(rect.left + (rect.width * offset), rect.top)
   ```

6. **Use Result Types for Error Handling:**
   ```kotlin
   // Good: Explicit error handling, testable
   fun parseDevice(json: String): Result<Device> {
       return try {
           Result.success(Json.decodeFromString<Device>(json))
       } catch (e: Exception) {
           Result.failure(e)
       }
   }
   ```

## Code Organization

### File Structure

1. **One Class/Interface per File:**
   - File name matches class name
   - Exception: Small related data classes can be in same file

2. **Package Organization:**
   ```
   data/
     model/          # Data models
     repository/     # Data access
     validation/     # Validation logic
     serialization/  # Serialization
   
   domain/
     layout/         # Layout algorithms
     util/           # Utilities
   
   presentation/
     config/         # Configuration
     # ViewModels, State, etc.
   ```

3. **File Ordering:**
   ```kotlin
   // 1. Package declaration
   package data.model
   
   // 2. Imports (alphabetical, grouped)
   import kotlinx.serialization.Serializable
   
   // 3. Type aliases (if any)
   typealias DeviceId = String
   
   // 4. Data classes / Classes
   data class Device(...)
   
   // 5. Enums
   enum class DeviceType { ... }
   
   // 6. Extension functions (if any)
   fun Device.bounds(): Rect { ... }
   ```

### Naming Conventions

1. **Classes:** PascalCase
   ```kotlin
   class ConnectionRouter
   data class WorkstationLayout
   ```

2. **Functions:** camelCase
   ```kotlin
   fun calculatePath()
   fun isDeviceVisible()
   ```

3. **Variables:** camelCase
   ```kotlin
   val deviceList: List<Device>
   var currentZoom: Float
   ```

4. **Constants:** UPPER_SNAKE_CASE
   ```kotlin
   const val MAX_ZOOM = 5.0f
   const val DEFAULT_CANVAS_WIDTH = 1920f
   ```

5. **Private Properties:** camelCase with underscore prefix (if needed for clarity)
   ```kotlin
   private val _uiState = MutableStateFlow(...)
   val uiState: StateFlow<UiState> = _uiState.asStateFlow()
   ```

6. **Boolean Properties/Functions:** is/has/should prefix
   ```kotlin
   val isVisible: Boolean
   fun hasConnections(): Boolean
   fun shouldRender(): Boolean
   ```

### Function Design

1. **Keep Functions Small:**
   - Single responsibility
   - Maximum 20-30 lines (guideline, not strict)
   - Extract complex logic into separate functions

2. **Prefer Expression Bodies:**
   ```kotlin
   fun isEmpty(): Boolean = items.isEmpty()
   ```

3. **Use Meaningful Names:**
   ```kotlin
   // Good
   fun findDeviceById(id: String): Device?
   fun calculateConnectionPath(connection: Connection): Path
   
   // Bad
   fun get(id: String): Device?
   fun calc(c: Connection): Path
   ```

4. **Limit Parameters:**
   - Maximum 3-4 parameters
   - Use data classes for multiple related parameters
   ```kotlin
   // Good
   data class RenderConfig(val zoom: Float, val pan: Offset, val theme: Theme)
   fun render(config: RenderConfig)
   
   // Bad
   fun render(zoom: Float, panX: Float, panY: Float, theme: Theme, ...)
   ```

## Error Handling

### Use Result Types

```kotlin
// Good: Explicit error handling
fun loadLayout(): Result<WorkstationLayout> {
    return try {
        val json = readFile()
        val layout = Json.decodeFromString<WorkstationLayout>(json)
        Result.success(layout)
    } catch (e: FileNotFoundException) {
        Result.failure(LoadError.FileNotFound(e.message ?: "Unknown"))
    } catch (e: SerializationException) {
        Result.failure(LoadError.ParseError(e.message ?: "Unknown"))
    }
}
```

### Use Sealed Classes for Errors

```kotlin
sealed class ValidationError {
    data class DuplicateId(val id: String) : ValidationError()
    data class InvalidPosition(val deviceName: String, val position: Position) : ValidationError()
    data class MissingDevice(val deviceId: String) : ValidationError()
    object EmptyLayout : ValidationError()
}
```

## Performance Considerations

### Lazy Evaluation

```kotlin
// Good: Lazy computation
val expensiveCalculation: String by lazy {
    // Expensive operation
    computeExpensiveValue()
}

// Good: Sequence for large collections
val filtered = devices.asSequence()
    .filter { it.isVisible }
    .map { it.toRenderData() }
    .toList()
```

### Avoid Unnecessary Allocations

```kotlin
// Good: Reuse objects
val rect = Rect() // Reuse in loop
devices.forEach { device ->
    rect.set(device.position.x, device.position.y, ...)
    // Use rect
}

// Bad: Create new objects in hot path
devices.forEach { device ->
    val rect = Rect(device.position.x, ...) // New allocation each iteration
}
```

## Testing Considerations

### Make Code Testable

1. **Avoid Private Functions That Need Testing:**
   - If logic is complex enough to need testing, make it internal or public
   - Use `internal` visibility for test access

2. **Use Dependency Injection:**
   ```kotlin
   // Good: Testable
   class PathPlanner(
       private val gridSize: Float = AlgorithmConfig.pathfindingGridCellSize
   ) {
       fun planPath(...): Path
   }
   
   // In test
   val planner = PathPlanner(gridSize = 5f) // Can inject test value
   ```

3. **Avoid Hard Dependencies:**
   ```kotlin
   // Good: Can be mocked
   interface DeviceRepository {
       suspend fun loadDevices(): List<Device>
   }
   
   class WorkstationViewModel(
       private val repository: DeviceRepository
   )
   ```

4. **Use Pure Functions:**
   - Pure functions are easiest to test
   - No side effects
   - Deterministic

## Kotlin-Specific Guidelines

### Use Kotlin Standard Library

```kotlin
// Good: Use Kotlin stdlib
val device = devices.firstOrNull { it.id == id }
val hasConnections = connections.any { it.sourceDeviceId == deviceId }
val deviceIds = devices.map { it.id }.toSet()

// Bad: Java-style
var device: Device? = null
for (d in devices) {
    if (d.id == id) {
        device = d
        break
    }
}
```

### Prefer Data Classes

```kotlin
// Good: Data class
data class Position(val x: Float, val y: Float)

// Bad: Regular class with manual equals/hashCode
class Position(val x: Float, val y: Float) {
    override fun equals(other: Any?): Boolean { ... }
    override fun hashCode(): Int { ... }
}
```

### Use Extension Functions

```kotlin
// Good: Extension function
fun Device.bounds(): Rect = Rect(
    position.x,
    position.y,
    position.x + size.width,
    position.y + size.height
)

// Usage
val bounds = device.bounds()
```

### Use Smart Casts

```kotlin
// Good: Smart cast
if (result is LoadResult.Success) {
    val layout = result.layout // No explicit cast needed
    processLayout(layout)
}

// Bad: Explicit cast
if (result is LoadResult.Success) {
    val layout = result as LoadResult.Success
    processLayout(layout.layout)
}
```

### Use When Expressions

```kotlin
// Good: When expression
val color = when (device.category) {
    DeviceCategory.HUB -> Color.Green
    DeviceCategory.PERIPHERAL -> Color.Orange
    DeviceCategory.CENTRAL_DEVICE -> Color.Blue
}

// Bad: If-else chain
val color = if (device.category == DeviceCategory.HUB) {
    Color.Green
} else if (device.category == DeviceCategory.PERIPHERAL) {
    Color.Orange
} else {
    Color.Blue
}
```

## Code Review Checklist

Before submitting code, verify:

- [ ] No useless comments (only meaningful comments explaining why)
- [ ] Code is concise and clear
- [ ] Uses idiomatic Kotlin patterns
- [ ] Functions are small and focused
- [ ] Dependencies are injected (testable)
- [ ] Error handling is explicit (Result types, sealed classes)
- [ ] No hard-coded values (use configuration)
- [ ] Meaningful names for all identifiers
- [ ] Pure functions where possible
- [ ] No unnecessary allocations in hot paths
- [ ] Code is organized in appropriate packages/files

## Examples

### Good Example

```kotlin
// Domain layer - Pure business logic
fun calculateConnectionPath(
    connection: Connection,
    devices: Map<String, Device>,
    obstacles: List<Rect>
): Result<Path> {
    val sourceDevice = devices[connection.sourceDeviceId] 
        ?: return Result.failure(PathError.MissingDevice(connection.sourceDeviceId))
    
    val targetDevice = devices[connection.targetDeviceId]
        ?: return Result.failure(PathError.MissingDevice(connection.targetDeviceId))
    
    val sourcePort = sourceDevice.ports.find { it.id == connection.sourcePortId }
        ?: return Result.failure(PathError.MissingPort(connection.sourcePortId))
    
    val targetPort = targetDevice.ports.find { it.id == connection.targetPortId }
        ?: return Result.failure(PathError.MissingPort(connection.targetPortId))
    
    val startPoint = calculatePortWorldPosition(sourceDevice, sourcePort)
    val endPoint = calculatePortWorldPosition(targetDevice, targetPort)
    
    return if (hasDirectPath(startPoint, endPoint, obstacles)) {
        Result.success(createDirectPath(startPoint, endPoint))
    } else {
        planPathWithObstacles(startPoint, endPoint, obstacles)
    }
}

// Extension function for clarity
private fun Device.portById(id: String): Port? = ports.find { it.id == id }
```

### Bad Example

```kotlin
// Too many comments, unclear structure, not testable
class ConnectionRouter {
    // Global state - bad
    var devices: List<Device> = emptyList()
    
    // Too many responsibilities
    fun route(connection: Connection): Path {
        // Find source device
        var sourceDevice: Device? = null
        for (device in devices) {
            if (device.id == connection.sourceDeviceId) {
                sourceDevice = device
                break
            }
        }
        
        // Check if found
        if (sourceDevice == null) {
            // Error
            return Path() // Empty path - unclear error handling
        }
        
        // ... more Java-style code
    }
}
```

## Summary

1. **Self-documenting code** - No useless comments
2. **Concise** - Think twice, write once
3. **Idiomatic Kotlin** - Use Kotlin features properly
4. **Testable** - Pure functions, dependency injection, explicit errors
5. **Organized** - Clear structure, meaningful names
6. **Performant** - Avoid unnecessary allocations, use lazy evaluation

Follow these guidelines to write maintainable, testable, and idiomatic Kotlin code.

