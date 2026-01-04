# Workstation Diagram - Technical Documentation

## Overview

The Workstation Diagram is a Kotlin Multiplatform project that provides an interactive visualization tool for workstation hardware configurations. Built with Compose Multiplatform, it supports desktop (JVM) and web (JavaScript) targets from a single codebase.

## Quick Start

**For AI Agents and New Developers**: Start with [PROJECT_OVERVIEW.md](PROJECT_OVERVIEW.md) for a high-level understanding of the project structure, key concepts, and development focus before diving into implementation details.

## Table of Contents

- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Technology Stack](#technology-stack)
- [Build Configuration](#build-configuration)
- [Platform-Specific Implementations](#platform-specific-implementations)
- [State Management](#state-management)
- [Coordinate System](#coordinate-system) ⭐ NEW
- [Data Models](#data-models)
- [Repository Pattern](#repository-pattern)
- [ViewModel Implementation](#viewmodel-implementation)
- [UI Components](#ui-components)
- [Build and Run](#build-and-run)
- [Troubleshooting](#troubleshooting)

---

## Architecture

### Multiplatform Architecture

The project follows Kotlin Multiplatform (KMP) architecture with three main source sets:

- **commonMain**: Shared code across all platforms
- **jvmMain**: Desktop-specific implementations
- **jsMain**: Web-specific implementations

### Layered Architecture

```
┌─────────────────────────────────────┐
│         UI Layer (Compose)           │
├─────────────────────────────────────┤
│      Presentation Layer              │
│  (ViewModel, State Management)      │
├─────────────────────────────────────┤
│         Data Layer                  │
│  (Repository, Models, Validation)    │
├─────────────────────────────────────┤
│    Platform-Specific Utilities      │
│  (File I/O, Date, etc.)             │
└─────────────────────────────────────┘
```

---

## Project Structure

```
workstation/
├── build.gradle.kts                 # Gradle build configuration
├── gradle/
│   ├── libs.versions.toml          # Dependency versions
│   └── wrapper/                     # Gradle wrapper files
├── gradle.properties               # Gradle properties
├── gradlew                          # Gradle wrapper script (Unix)
├── gradlew.bat                      # Gradle wrapper script (Windows)
├── settings.gradle.kts              # Gradle settings
├── docs/                            # Technical documentation
│   ├── README.md                   # This file
│   ├── ARCHITECTURE.md             # Architecture details
│   ├── STATE_MANAGEMENT.md         # State management guide
│   └── PLATFORM_SUPPORT.md          # Platform-specific implementations
├── plan/                            # Project planning documents
└── src/
    ├── commonMain/                 # Shared code
    │   └── kotlin/
    │       └── dev/akexorcist/workstation/
    │           ├── data/
    │           │   ├── model/      # Data models
    │           │   ├── repository/ # Repository implementations
    │           │   ├── serialization/ # JSON serialization
    │           │   └── validation/ # Data validation
    │           ├── presentation/
    │           │   ├── WorkstationViewModel.kt
    │           │   └── config/     # Configuration classes
    │           ├── ui/             # Compose UI components
    │           └── utils/          # Platform utilities
    ├── jvmMain/                    # Desktop-specific code
    │   └── kotlin/
    │       └── dev/akexorcist/workstation/
    │           ├── Main.kt         # Desktop entry point
    │           └── utils/          # JVM utilities
    └── jsMain/                      # Web-specific code
        └── kotlin/
            └── dev/akexorcist/workstation/
                ├── Main.kt         # Web entry point
                └── utils/          # JS utilities
```

---

## Technology Stack

### Core Technologies

| Technology | Version | Purpose |
|-------------|---------|---------|
| Kotlin | 2.3.0 | Primary language |
| Compose Multiplatform | 1.9.3 | UI framework |
| Kotlinx Coroutines | 1.10.2 | Asynchronous programming |
| Kotlinx Serialization | 1.9.0 | JSON serialization |

### Lifecycle & ViewModel

| Technology | Version | Purpose |
|-------------|---------|---------|
| Lifecycle ViewModel | 2.8.0 | Lifecycle-aware ViewModels |
| ViewModel Compose | 2.8.0 | Compose integration for ViewModels |

### Platform-Specific

| Platform | Technology | Purpose |
|----------|-----------|---------|
| JVM | Compose Desktop | Desktop UI rendering |
| JS | Compose Web (HTML) | Web UI rendering |

---

## Build Configuration

### Gradle Configuration

The project uses Gradle with Kotlin DSL for build configuration.

#### Key Build Settings

```kotlin
kotlin {
    jvmToolchain(17)  // Java 17 for JVM target

    jvm()             // Desktop target
    js {
        browser()     // Web browser target
        binaries.executable()
    }
}
```

#### Dependency Management

Dependencies are managed through `gradle/libs.versions.toml`:

```toml
[versions]
kotlin = "2.3.0"
compose = "1.9.3"
kotlinx-coroutines = "1.10.2"
lifecycle-viewmodel = "2.8.0"
```

#### Memory Configuration

To handle large compilation tasks, memory settings are configured in `gradle.properties`:

```properties
org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=512m
kotlin.daemon.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=512m
```

---

## Platform-Specific Implementations

### Expect/Actual Pattern

Platform-specific code is implemented using Kotlin's expect/actual declarations:

#### Common Declaration

```kotlin
// commonMain/kotlin/dev/akexorcist/workstation/utils/PlatformUtils.kt
expect fun getCurrentDate(): String
expect fun readResourceFile(path: String): String
```

#### JVM Implementation

```kotlin
// jvmMain/kotlin/dev/akexorcist/workstation/utils/PlatformUtils.kt
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

#### JavaScript Implementation

```kotlin
// jsMain/kotlin/dev/akexorcist/workstation/utils/PlatformUtils.kt
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

## State Management

### ViewModel with Lifecycle

The project uses JetBrains' Lifecycle ViewModel for KMP, providing:

- **Lifecycle awareness**: Automatic cleanup when ViewModel is cleared
- **Coroutine scope management**: Built-in `viewModelScope` for coroutines
- **State preservation**: Survives configuration changes
- **Cross-platform support**: Works on JVM, JS, and other platforms

### StateFlow for Reactive State

State is managed using `StateFlow`:

```kotlin
class WorkstationViewModel(
    private val repository: WorkstationRepository = WorkstationRepositoryImpl()
) : ViewModel() {
    private val _uiState = MutableStateFlow(WorkstationUiState(isLoading = true))
    val uiState: StateFlow<WorkstationUiState> = _uiState.asStateFlow()

    private val _diagramState = MutableStateFlow(DiagramState())
    val diagramState: StateFlow<DiagramState> = _diagramState.asStateFlow()
}
```

### State Collection in Compose

State is collected in Compose using `collectAsState()`:

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

    // ... UI implementation
}
```

---

## Coordinate System

### Overview

The application supports two coordinate systems for maximum flexibility and cross-platform compatibility:

1. **Absolute Coordinates** (Legacy): Direct pixel values tied to a specific canvas size
2. **Virtual Coordinates** (Recommended): Resolution-independent units on a virtual canvas

### Virtual Coordinate System

Virtual coordinates use a fixed virtual canvas (e.g., 10000x10000) that automatically scales to any screen size.

**Example**:
```json
{
  "metadata": {
    "coordinateSystem": "virtual",
    "virtualCanvas": { "width": 10000, "height": 10000 }
  },
  "devices": [{
    "position": { "x": 1000, "y": 2000 },
    "size": { "width": 2000, "height": 1500 }
  }]
}
```

**Benefits**:
- ✅ Resolution independent - works on any screen size
- ✅ Responsive - automatically adapts to window resize
- ✅ Platform agnostic - same data works on web, desktop, mobile
- ✅ Maintains aspect ratios across devices

### Coordinate Transformation

The `CoordinateTransformer` utility handles transformation between coordinate systems:

```kotlin
// Transforms position from data space to screen space
val screenPos = CoordinateTransformer.transformPosition(
    dataPosition = device.position,
    metadata = layout.metadata,
    actualCanvasSize = canvasSize,
    zoom = zoom,
    panOffset = panOffset
)
```

**Transformation Flow**:
```
Data Space (JSON) → World Space (normalized) → Screen Space (rendered)
```

### Backward Compatibility

Existing files with absolute coordinates work unchanged. The system automatically detects the coordinate system:

```kotlin
fun isVirtualCoordinates(metadata: LayoutMetadata): Boolean {
    return metadata.coordinateSystem == "virtual" && 
           metadata.virtualCanvas != null
}
```

### Zoom Behavior

Zoom operates towards the **viewport center**, keeping the center point fixed during zoom operations for a natural, intuitive experience.

**Documentation**:
- [VIRTUAL_COORDINATE_SYSTEM_SUMMARY.md](VIRTUAL_COORDINATE_SYSTEM_SUMMARY.md) - Summary of the virtual coordinate system
- [PHASE_3_ENHANCEMENTS.md](PHASE_3_ENHANCEMENTS.md) - Implementation details
- [Archive: COORDINATE_SYSTEM.md](archive/coordination/COORDINATE_SYSTEM.md) - Original design and architecture

---

## Data Models

### Core Data Models

#### WorkstationLayout

```kotlin
@Serializable
data class WorkstationLayout(
    val metadata: LayoutMetadata,
    val devices: List<Device>,
    val connections: List<Connection>
)
```

#### Device

```kotlin
@Serializable
data class Device(
    val id: String,
    val name: String,
    val model: String,
    val category: DeviceCategory,
    val position: Position,
    val size: Size,
    val ports: List<Port>
)
```

#### Connection

```kotlin
@Serializable
data class Connection(
    val id: String,
    val sourceDeviceId: String,
    val sourcePortId: String,
    val targetDeviceId: String,
    val targetPortId: String,
    val category: ConnectionCategory,
    val type: ConnectionType
)
```

### State Models

#### WorkstationUiState

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

#### DiagramState

```kotlin
data class DiagramState(
    val scale: Float = 1.0f,
    val deviceRenderData: List<DeviceRenderData> = emptyList(),
    val connectionRenderData: List<ConnectionRenderData> = emptyList()
)
```

---

## Repository Pattern

### Repository Interface

```kotlin
interface WorkstationRepository {
    suspend fun loadLayout(): LoadResult
    suspend fun loadLayoutFromJson(jsonString: String): LoadResult
    fun validateLayout(layout: WorkstationLayout): ValidationResult
}
```

### Repository Implementation

```kotlin
class WorkstationRepositoryImpl : WorkstationRepository {
    override suspend fun loadLayout(): LoadResult = withContext(Dispatchers.Default) {
        try {
            val jsonString = readFile()
            loadLayoutFromJson(jsonString)
        } catch (e: Exception) {
            LoadResult.Error("Failed to load workstation data: ${e.message}", e)
        }
    }

    override suspend fun loadLayoutFromJson(jsonString: String): LoadResult {
        // Implementation details...
    }

    override fun validateLayout(layout: WorkstationLayout): ValidationResult {
        return DataValidator.validateLayout(layout)
    }

    private fun readFile(): String {
        val resourcePath = "/data/workstation.json"
        return readResourceFile(resourcePath)
    }
}
```

### LoadResult Sealed Class

```kotlin
sealed class LoadResult {
    data class Success(val layout: WorkstationLayout) : LoadResult()
    data class PartialSuccess(val layout: WorkstationLayout, val errors: List<String>) : LoadResult()
    data class Error(val message: String, val cause: Throwable? = null) : LoadResult()
}
```

---

## ViewModel Implementation

### ViewModel with Lifecycle

The `WorkstationViewModel` extends `ViewModel` from the lifecycle library:

```kotlin
class WorkstationViewModel(
    private val repository: WorkstationRepository = WorkstationRepositoryImpl()
) : ViewModel() {
    // State management
    private val _uiState = MutableStateFlow(WorkstationUiState(isLoading = true))
    val uiState: StateFlow<WorkstationUiState> = _uiState.asStateFlow()

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
            // ... other cases
        }
    }

    // State update functions
    fun handleZoomChange(zoom: Float) {
        val validatedZoom = StateManagementConfig.validateZoom(zoom)
        _uiState.value = _uiState.value.copy(zoom = validatedZoom)
        updateDiagramState()
    }

    // ... more functions
}
```

### Coroutine Management

The ViewModel uses suspend functions instead of manual coroutine scopes:

```kotlin
// ❌ Old approach (manual scope)
private val viewModelScope = CoroutineScope(Dispatchers.Main)

fun loadLayout() {
    viewModelScope.launch {
        // ...
    }
}

// ✅ New approach (suspend functions)
suspend fun loadLayout() {
    // ...
}
```

### UI Integration

Coroutines are launched in the UI layer using Compose's `LaunchedEffect`:

```kotlin
@Composable
fun WorkstationTheme(
    viewModel: WorkstationViewModel,
    content: @Composable () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Automatically load when composition starts
    LaunchedEffect(Unit) {
        viewModel.loadLayout()
    }

    // ... UI implementation
}
```

---

## UI Components

### Desktop Entry Point (JVM)

```kotlin
fun main() = application {
    val viewModel = remember { WorkstationViewModel() }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Workstation Diagram",
        state = WindowState(width = 1280.dp, height = 720.dp)
    ) {
        WorkstationTheme(viewModel = viewModel) {
            WorkstationDiagramScreen(viewModel = viewModel)
        }
    }
}
```

### Web Entry Point (JS)

```kotlin
fun main() {
    renderComposable(rootElementId = "root") {
        val viewModel = remember { WorkstationViewModel() }

        WorkstationTheme(viewModel = viewModel) {
            WorkstationDiagramScreen(viewModel = viewModel)
        }
    }
}
```

### Theme Component

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

---

## Build and Run

### Prerequisites

- **JDK 17 or higher** (for JVM target)
- **Node.js and Yarn** (for JavaScript target)
- **Gradle 8.5+** (included via wrapper)

### Build Commands

#### Build All Targets

```bash
./gradlew build
```

#### Build JVM Target Only

```bash
./gradlew :compileKotlinJvm
```

#### Build JavaScript Target Only

```bash
./gradlew :compileKotlinJs
```

### Run Commands

#### Run Desktop Application

```bash
./gradlew :run
```

#### Build JavaScript Executable

```bash
./gradlew :jsBrowserDevelopmentRun
```

### Clean Build

```bash
./gradlew clean build
```

---

## Troubleshooting

### Common Issues

#### 1. Java Toolchain Not Found

**Error:**
```
No matching toolchains found for requested specification: {languageVersion=21}
```

**Solution:**
The project is configured to use Java 17. Ensure Java 17+ is installed:

```bash
java -version
/usr/libexec/java_home -V
```

#### 2. Out of Memory During Build

**Error:**
```
java.lang.OutOfMemoryError: Java heap space
```

**Solution:**
Memory is already configured in `gradle.properties`. If issues persist, increase memory:

```properties
org.gradle.jvmargs=-Xmx6g -XX:MaxMetaspaceSize=1g
kotlin.daemon.jvmargs=-Xmx6g -XX:MaxMetaspaceSize=1g
```

#### 3. Main Dispatcher Missing

**Error:**
```
Module with the Main dispatcher is missing
```

**Solution:**
This was resolved by using suspend functions in ViewModel instead of manual coroutine scopes. The UI layer uses `LaunchedEffect` which automatically provides the correct dispatcher.

#### 4. Desktop Compose Imports Not Found

**Error:**
```
Unresolved reference 'desktop'
```

**Solution:**
Ensure the desktop dependency is added to `jvmMain`:

```kotlin
val jvmMain by getting {
    dependencies {
        implementation(compose.desktop.currentOs)
    }
}
```

#### 5. JavaScript Build Warnings

**Warning:**
```
DeprecationWarning: `url.parse()` behavior is not standardized
```

**Solution:**
These are warnings from npm packages and can be safely ignored. They don't affect functionality.

### Debug Mode

For detailed build information:

```bash
./gradlew build --info
```

For stack traces:

```bash
./gradlew build --stacktrace
```

---

## Additional Documentation

- [PROJECT_OVERVIEW.md](PROJECT_OVERVIEW.md) - **Start here**: High-level project overview for AI agents and developers
- [ARCHITECTURE_SUMMARY.md](ARCHITECTURE_SUMMARY.md) - Architecture patterns and design principles
- [COORDINATE_SYSTEM.md](COORDINATE_SYSTEM.md) - Coordinate system implementation
- [MANUAL_PATH_ROUTING_IMPLEMENTATION_PLAN.md](MANUAL_PATH_ROUTING_IMPLEMENTATION_PLAN.md) - Routing implementation details
- [WORKSTATION_JSON_VALIDATION_RULES.md](WORKSTATION_JSON_VALIDATION_RULES.md) - JSON validation rules

---

## Contributing

When contributing to this project:

1. Follow the existing code structure
2. Use expect/actual for platform-specific code
3. Keep commonMain truly platform-agnostic
4. Add documentation for new features
5. Test on both JVM and JS targets

---

## License

[Add your license information here]