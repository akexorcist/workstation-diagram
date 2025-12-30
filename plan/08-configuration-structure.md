# Configuration Structure

This document defines the structure for all configurable values in the application.

## Configuration Architecture

### Configuration Layers

1. **Code Configuration:** Default values defined in code (configurable objects)
2. **Data Configuration:** Device-specific values in JSON data
3. **Runtime Configuration:** User preferences (zoom, pan, theme)

## Code Configuration Structure

### File: `presentation/config/RenderingConfig.kt`

```kotlin
object RenderingConfig {
    // Device Rendering
    val defaultDeviceBorderRadius: Float = 8f
    val defaultDeviceBorderThickness: Float = 2f
    
    // Port Indicators
    val portIndicatorBorderThickness: Float = 1f
    val portIndicatorSpacingFromEdge: Float = 4f
    val portIndicatorMinSpacing: Float = 8f
    val portIndicatorTextScaleFactor: Float = 1.2f // Multiplier for text-based sizing
    
    // Connection Lines
    val defaultConnectionLineThickness: Float = 2f
    val connectionLineThicknessByCategory: Map<ConnectionCategory, Float> = mapOf(
        ConnectionCategory.DATA to 2f,
        ConnectionCategory.VIDEO to 3f,
        ConnectionCategory.AUDIO to 2f,
        ConnectionCategory.POWER to 2.5f,
        ConnectionCategory.NETWORK to 2f
    )
    val arrowHeadWidth: Float = 12f
    val arrowHeadHeight: Float = 8f
    val connectionLabelPadding: Float = 4f
    
    // Text & Labels
    val baseFontSize: Float = 12f
    val deviceNameFontScale: Float = 1.17f // Relative to base
    val deviceModelFontScale: Float = 1.0f
    val portLabelFontScale: Float = 0.83f
    val connectionLabelFontScale: Float = 0.92f
    val fontSizeFromDimensionScale: Float = 0.06f // Font size = dimension * scale
    
    // Spacing
    val minDeviceSpacing: Float = 20f
    val sidebarWidth: Float = 300f
    val controlPanelHeight: Float = 60f
    val panelPadding: Float = 16f
    val deviceLabelSpacing: Float = 8f
}
```

### File: `domain/layout/AlgorithmConfig.kt`

```kotlin
object AlgorithmConfig {
    // Clearance Distances
    val minPathToDeviceDistance: Float = 10f
    val minParallelConnectionSpacing: Float = 5f
    val portConnectionPointRadius: Float = 3f
    
    // A* Pathfinding
    val pathfindingGridCellSize: Float = 10f
    val pathfindingMaxIterations: Int = 10000
    val pathfindingTimeoutMs: Long = 100
    
    // Bezier Curves
    val bezierControlPointOffset: Float = 20f
    val bezierSmoothnessFactor: Float = 0.5f
    val minCurveRadius: Float = 5f
    val maxCurveDeviation: Float = 50f
    
    // Layer Assignment
    val maxLayers: Int = 5
    val layerSpacing: Float = 8f
    
    // Crossing Detection
    val crossingDetectionTolerance: Float = 2f
    val countNearMissesAsCrossings: Boolean = false
}
```

### File: `presentation/config/InteractionConfig.kt`

```kotlin
object InteractionConfig {
    // Click Detection
    val clickToleranceRadius: Float = 5f
    val doubleClickTimeWindowMs: Long = 300
    val clickVsDragThreshold: Float = 5f
    
    // Zoom
    val zoomStepSize: Float = 0.1f
    val minZoom: Float = 0.1f
    val maxZoom: Float = 5.0f
    val zoomSensitivity: Float = 0.1f // Mouse wheel sensitivity
    
    // Pan
    val panSensitivity: Float = 1.0f
    val enableSmoothPanning: Boolean = true
    
    // Hover
    val hoverDetectionDelayMs: Long = 300
    val tooltipDelayMs: Long = 500
    val hoverAreaSize: Float = 10f
}
```

### File: `presentation/config/ViewportConfig.kt`

```kotlin
object ViewportConfig {
    val defaultZoom: Float = 1.0f
    val viewportCullingMargin: Float = 100f // Render elements this far outside viewport
    val defaultCanvasSize: Size = Size(1920f, 1080f)
    val minCanvasSize: Size = Size(800f, 600f)
    val maxCanvasSize: Size = Size(10000f, 10000f)
    
    // Coordinate System
    // Origin: Top-left corner (0, 0)
    // X-axis: Right = positive
    // Y-axis: Down = positive (standard screen coordinates)
    // Units: Pixels in world space
}
```

### File: `domain/util/SpatialIndexConfig.kt`

```kotlin
object SpatialIndexConfig {
    // Grid-based spatial index
    val gridCellSize: Float = 50f // 50px x 50px cells
    val useQuadtree: Boolean = false // Use grid for simplicity
    
    // If switching to Quadtree later:
    val quadtreeMaxDepth: Int = 8
    val quadtreeMaxItemsPerNode: Int = 10
}
```

## Data Configuration (JSON)

### Device Dimensions in Data

Devices specify their dimensions in the JSON data:

```json
{
  "id": "laptop-office",
  "name": "Office Laptop",
  "size": {
    "width": 200,
    "height": 150
  }
}
```

- Each device can have custom width and height
- Dimensions are in world space units (pixels or dp equivalent)
- If not specified, use default from `ViewportConfig.defaultDeviceSize`

## Runtime Configuration (User Preferences)

### File: `presentation/config/UserPreferences.kt`

```kotlin
@Serializable
data class UserPreferences(
    val zoom: Float = 1.0f,
    val panOffset: Offset = Offset.Zero,
    val isDarkTheme: Boolean = true,
    val showConnectionAnimation: Boolean = false, // Not implemented yet
    val sidebarCollapsed: Boolean = false,
    val version: String = "1.0" // For migration
) {
    companion object {
        val DEFAULT = UserPreferences()
    }
}
```

### File: `data/model/Defaults.kt`

```kotlin
object DataDefaults {
    // Device Defaults
    val defaultDeviceSize = Size(200f, 150f)
    val defaultDevicePosition = Position(0f, 0f)
    val defaultDeviceCategory = DeviceCategory.PERIPHERAL
    
    // Port Defaults
    val defaultPortPosition = PortPosition(DeviceSide.LEFT, 0.5f)
    val defaultPortDirection = PortDirection.BIDIRECTIONAL
    val defaultPortType = PortType.USB_C
    
    // Connection Defaults
    val defaultConnectionCategory = ConnectionCategory.DATA
    fun defaultConnectionType() = ConnectionType("Unknown", defaultConnectionCategory)
    
    // Layout Defaults
    val defaultCanvasSize = Size(1920f, 1080f)
    val defaultTheme = ThemeConfig(isDark = true)
    const val defaultVersion = "1.0"
    const val defaultTitle = "Workstation Diagram"
    fun defaultDate() = java.time.LocalDate.now().toString() // "YYYY-MM-DD"
}
```

### File: `presentation/config/StateManagement.kt`

```kotlin
object StateManagementConfig {
    // State Persistence
    val saveStateDebounceMs: Long = 500 // Save 500ms after last change
    val preferencesFileName = "user_preferences.json"
    
    // Initial Viewport
    val initialZoom: Float = 1.0f
    val initialPan: Offset = Offset.Zero
    val useFitToContent: Boolean = true // Fit all devices in viewport on load
    
    // State Validation
    fun validateZoom(zoom: Float): Float {
        return zoom.coerceIn(InteractionConfig.minZoom, InteractionConfig.maxZoom)
    }
    
    fun validatePan(pan: Offset, canvasSize: Size): Offset {
        // Allow unlimited panning, no validation needed
        return pan
    }
}
```

## Configuration Usage Pattern

### Accessing Configuration

```kotlin
// In rendering code
val lineThickness = RenderingConfig.connectionLineThicknessByCategory[connection.category]
    ?: RenderingConfig.defaultConnectionLineThickness

// In algorithm code
val clearance = AlgorithmConfig.minPathToDeviceDistance

// In interaction code
val zoom = InteractionConfig.maxZoom.coerceAtMost(newZoom)
```

### Calculating Derived Values

```kotlin
// Font size based on device dimension
fun calculateDeviceNameFontSize(deviceSize: Size): Float {
    val baseSize = deviceSize.width * RenderingConfig.fontSizeFromDimensionScale
    return baseSize * RenderingConfig.deviceNameFontScale
}

// Port indicator size based on text
fun calculatePortIndicatorSize(portName: String, baseFontSize: Float): Float {
    val textWidth = estimateTextWidth(portName, baseFontSize)
    return textWidth * RenderingConfig.portIndicatorTextScaleFactor
}
```

## Configuration File Structure

```
commonMain/kotlin/
├── presentation/
│   └── config/
│       ├── RenderingConfig.kt      # UI rendering configuration
│       ├── InteractionConfig.kt    # User interaction configuration
│       ├── ViewportConfig.kt       # Viewport and canvas configuration
│       ├── PerformanceConfig.kt     # Performance targets and limits
│       ├── ErrorMessages.kt        # Error and user feedback messages
│       ├── StateManagement.kt       # State management configuration
│       └── UserPreferences.kt      # User preference data class
│
├── data/
│   └── model/
│       └── Defaults.kt              # Data model default values
│
└── domain/
    ├── layout/
    │   └── AlgorithmConfig.kt       # Algorithm parameters
    └── util/
        └── SpatialIndexConfig.kt   # Spatial index configuration
```

## Default Values Summary

### Rendering Defaults
- Device border radius: 8dp
- Device border thickness: 2dp
- Default connection line thickness: 2dp
- Base font size: 12sp
- Sidebar width: 300dp
- Control panel height: 60dp

### Algorithm Defaults
- Min path to device distance: 10px
- Min parallel connection spacing: 5px
- Pathfinding grid cell size: 10px
- Max layers: 5
- Layer spacing: 8px
- Bezier control point offset: 20px
- Bezier smoothness factor: 0.5
- Min curve radius: 5px
- Max curve deviation: 50px

### Interaction Defaults
- Min zoom: 0.1x
- Max zoom: 5.0x
- Default zoom: 1.0x
- Click tolerance: 5px
- Hover delay: 300ms

### Performance Defaults
- Target FPS: 60
- Min acceptable FPS: 30
- Max devices: 100
- Max connections: 200
- Load time target: < 1 second

### Coordinate System
- Origin: Top-left (0, 0)
- X-axis: Right = positive
- Y-axis: Down = positive
- Units: Pixels

### Spatial Index
- Type: Grid-based
- Cell size: 50px x 50px

### File: `presentation/config/PerformanceConfig.kt`

```kotlin
object PerformanceConfig {
    // Frame Rate
    val targetFPS: Int = 60
    val minAcceptableFPS: Int = 30
    val frameTimeBudgetMs: Float = 16.67f // For 60 FPS
    
    // Rendering Performance
    val maxDevicesForSmoothRendering: Int = 100
    val maxConnectionsForSmoothRendering: Int = 200
    val viewportCullingTargetMs: Float = 1f
    
    // Algorithm Performance
    val pathCalculationTimeoutMs: Long = 100
    val maxPathfindingIterations: Int = 10000
    val acceptableComplexity: String = "O(n²) for n < 100"
    
    // Memory
    val maxMemoryUsageMB: Int = 500
    val maxPathCacheSizeMB: Int = 50
    val maxSpatialIndexSizeMB: Int = 10
    
    // Load Time
    val initialLoadTimeTargetMs: Long = 1000
    val dataProcessingTimeTargetMs: Long = 500
}
```

### File: `presentation/config/ErrorMessages.kt`

```kotlin
object ErrorMessages {
    // Data Loading
    const val FILE_NOT_FOUND = "Unable to load workstation data. File not found: {path}"
    const val JSON_PARSE_ERROR = "Invalid data format. Please check your JSON file. Error: {error}"
    const val VALIDATION_ERROR = "Data validation failed: {error}"
    const val NETWORK_ERROR = "Unable to load data. Please check your internet connection."
    
    // Device Validation
    const val DUPLICATE_DEVICE_ID = "Duplicate device ID: {id}"
    const val INVALID_DEVICE_POSITION = "Device '{name}' position is outside canvas bounds"
    const val INVALID_DEVICE_SIZE = "Device '{name}' has invalid size (must be positive)"
    const val INVALID_PORT_POSITION = "Device '{name}' port '{portId}' has invalid position offset"
    
    // Connection Validation
    const val MISSING_DEVICE = "Connection '{id}' references non-existent device: {deviceId}"
    const val MISSING_PORT = "Connection '{id}' references non-existent port: {portId}"
    const val INVALID_PORT_DIRECTION = "Connection '{id}' has invalid port direction combination"
    const val SELF_CONNECTION = "Connection '{id}' cannot connect device to itself"
    
    // Port Validation
    const val DUPLICATE_PORT_ID = "Device '{deviceId}' has duplicate port ID: {portId}"
    const val INVALID_PORT_OFFSET = "Port '{portId}' offset must be between 0.0 and 1.0"
    
    // Rendering
    const val PATH_CALCULATION_FAILURE = "Unable to calculate connection path. Using straight line."
    const val RENDERING_ERROR = "Rendering error occurred. Some elements may not display correctly."
    const val PERFORMANCE_WARNING = "Large diagram detected. Rendering may be slow."
    
    // User Feedback
    const val LOAD_SUCCESS = "Workstation diagram loaded successfully"
    const val LOAD_WARNING = "Some data issues were found but diagram loaded with warnings"
    const val LOADING_INFO = "Loading workstation data..."
    const val PROCESSING_INFO = "Processing diagram layout..."
}
```

## Future Configuration Extensions

### Theme Configuration
- Structure prepared for future theme customization
- Color palette defined but extensible
- Theme switching mechanism ready

### Animation Configuration (Future)
- When animations are added, create `AnimationConfig.kt`
- Define animation durations, easing, etc.

### Export Configuration (Future)
- When export is added, create `ExportConfig.kt`
- Define export resolutions, formats, etc.

