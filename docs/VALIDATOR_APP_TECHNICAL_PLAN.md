# Validator App - Technical Plan

## Overview

A standalone Kotlin Multiplatform validator application that validates `workstation.json` files with detailed error reporting, supporting both web and desktop platforms.

---

## Project Goals

### Primary Features
1. ✅ **File Chooser** - Select `workstation.json` files from filesystem
2. ✅ **Detailed Validation** - Comprehensive error/warning reporting with locations
3. ✅ **User-Friendly Display** - Clear, actionable error messages
4. ✅ **Multi-Platform** - Web (browser) and Desktop (JVM) support
5. ✅ **JSON Syntax Highlighting** - Show file content with error locations
6. ✅ **Export Reports** - Save validation results as text/JSON

### Success Criteria
- User can identify exact location of errors
- User can fix errors based on provided guidance
- Works offline (no server required)
- Fast validation (< 1 second for typical files)

---

## Code Sharing Analysis

### Shared Modules (commonMain)

#### ✅ **Can Be Directly Reused**

1. **Data Models** (`data/model/`)
   - `Device.kt` - All device labels and enums
   - `Connection.kt` - Connection models
   - `Layout.kt` - WorkstationLayout structure
   - **Status**: ✅ 100% reusable

2. **Validation Logic** (`data/validation/`)
   - `DataValidator.kt` - Core validation logic
   - **Status**: ⚠️ Needs enhancement for detailed error reporting
   - **Action**: Extend with location tracking

3. **Serialization** (`data/serialization/`)
   - `WorkstationLayoutSerializer.kt` - JSON parsing
   - **Status**: ✅ 100% reusable

#### ⚠️ **Needs Modification**

1. **Repository** (`data/repository/`)
   - `WorkstationRepository.kt` - File loading
   - **Status**: ⚠️ Platform-specific file I/O
   - **Action**: Extract common interface, platform-specific implementations

#### ❌ **Not Needed**

1. **UI Components** (`ui/components/`, `ui/sidebar/`, etc.)
   - DiagramCanvas, controls, panels
   - **Status**: ❌ Not needed (validator has different UI)

2. **Routing** (`routing/`)
   - Intelligent routing algorithm
   - **Status**: ❌ Not needed (validation only)

3. **ViewModel** (`presentation/`)
   - WorkstationViewModel
   - **Status**: ❌ Validator has different view model

---

## Project Structure

### Module Organization

```
workstation/
├── shared/                           # NEW: Shared library module
│   ├── commonMain/
│   │   ├── data/
│   │   │   ├── model/               # Moved from viewer app
│   │   │   ├── serialization/        # Moved from viewer app
│   │   │   └── validation/          # Enhanced from viewer app
│   │   └── repository/              # Common interfaces
│   ├── jvmMain/                     # JVM file I/O
│   └── jsMain/                      # JS file I/O
│
├── viewer/                          # RENAMED: Diagram viewer app (main app)
│   ├── commonMain/
│   │   ├── routing/                 # Routing stays in viewer
│   │   ├── ui/                      # UI stays in viewer
│   │   └── presentation/            # ViewModel stays in viewer
│   ├── jvmMain/
│   └── jsMain/
│
└── validator/                       # NEW: Validator app
    ├── commonMain/
    │   ├── ui/                      # Validator UI
    │   ├── presentation/            # Validator ViewModel
    │   └── domain/                  # Validation reporting logic
    ├── jvmMain/                     # Desktop entry point
    └── jsMain/                      # Web entry point
```

### Gradle Configuration

```kotlin
// settings.gradle.kts
rootProject.name = "workstation"

include(":shared")       // Shared library
include(":viewer")       // Diagram viewer app (main app)
include(":validator")    // Validator app
```

---

## Shared Module Specification

### Module: `shared`

**Purpose**: Common data models, serialization, and validation logic

#### Dependencies
```kotlin
// shared/build.gradle.kts
dependencies {
    commonMain {
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.kotlinx.coroutines.core)
    }
}
```

#### Exported APIs

**Data Models:**
```kotlin
// Public API
data class WorkstationLayout
data class Device
data class Port
data class Connection
enum class DeviceLabel
enum class PortType
// ... all model classes
```

**Validation:**
```kotlin
sealed class ValidationResult {
    data class Success(val warnings: List<ValidationWarning> = emptyList())
    data class Failure(val errors: List<ValidationError>)
}

data class ValidationError(
    val code: String,
    val message: String,
    val severity: ErrorSeverity,
    val location: ErrorLocation,
    val suggestion: String? = null
)

data class ValidationWarning(
    val code: String,
    val message: String,
    val location: ErrorLocation,
    val suggestion: String? = null
)

data class ErrorLocation(
    val path: String,                    // JSON path: "devices[0].ports[1].offset"
    val line: Int? = null,               // Line number in file
    val column: Int? = null,             // Column number in file
    val deviceId: String? = null,        // Affected device
    val portId: String? = null,          // Affected port
    val connectionId: String? = null     // Affected connection
)

enum class ErrorSeverity {
    ERROR,      // Blocks loading
    WARNING,    // Loads but may have issues
    INFO        // Best practice suggestions
}

interface WorkstationValidator {
    fun validate(layout: WorkstationLayout): ValidationResult
    fun validateJson(jsonString: String): ValidationResult
}
```

**File Loading:**
```kotlin
interface FileReader {
    suspend fun readFile(path: String): Result<String>
    suspend fun readFileBytes(path: String): Result<ByteArray>
}

expect fun createFileReader(): FileReader
```

---

## Validator App Specification

### Module: `validator`

**Purpose**: Standalone validation application with UI

#### Dependencies
```kotlin
// validator/build.gradle.kts
dependencies {
    commonMain {
        implementation(project(":shared"))
        implementation(compose.runtime)
        implementation(compose.foundation)
        implementation(compose.ui)
        implementation(compose.material3)
        implementation(compose.materialIconsExtended)
        implementation(libs.androidx.lifecycle.viewmodel)
    }
}
```

#### UI Components

1. **File Chooser Screen**
   - Button: "Choose workstation.json"
   - Drag & drop support
   - Recent files list
   - Sample file loader

2. **Validation Results Screen**
   - Summary card (✅ Valid / ❌ Errors / ⚠️ Warnings)
   - Error list with severity badges
   - JSON preview with error highlighting
   - Fix suggestions panel

3. **Error Detail View**
   - Error code and message
   - Location (device/port/connection)
   - JSON path
   - Suggestion/solution
   - "Copy error" button

4. **Export Results**
   - Export as text
   - Export as JSON
   - Copy to clipboard

#### View Model

```kotlin
class ValidatorViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<ValidatorUiState>(ValidatorUiState.Empty)
    val uiState: StateFlow<ValidatorUiState> = _uiState.asStateFlow()
    
    fun loadFile(content: String)
    fun validateFile()
    fun exportResults(format: ExportFormat)
    fun clearResults()
}

sealed class ValidatorUiState {
    object Empty : ValidatorUiState()
    object Loading : ValidatorUiState()
    data class Loaded(
        val filename: String,
        val content: String,
        val result: ValidationResult
    ) : ValidatorUiState()
    data class Error(val message: String) : ValidatorUiState()
}
```

---

## Enhanced Validation Logic

### New Validation Rules

Extend `DataValidator` with:

1. **Structural Validation**
   ```kotlin
   - JSON syntax errors with line/column
   - Missing required fields
   - Invalid data types
   - Unknown enum values
   ```

2. **Business Rules**
   ```kotlin
   - 1-to-1 port usage
   - No self-connections
   - Unique IDs
   - Port offset ranges (0.0-1.0)
   ```

3. **Routing Compatibility**
   ```kotlin
   - Grid alignment checks (INFO level)
   - Device overlap detection (WARNING)
   - Clearance requirements (WARNING)
   - Canvas bounds validation (ERROR)
   ```

4. **Best Practices**
   ```kotlin
   - Port distribution suggestions (INFO)
   - Grid alignment recommendations (INFO)
   - Connection complexity warnings (INFO)
   ```

### Error Code System

```kotlin
object ErrorCodes {
    // Structural errors (ERR_xxx)
    const val ERR_INVALID_JSON = "ERR_INVALID_JSON"
    const val ERR_MISSING_FIELD = "ERR_MISSING_FIELD"
    const val ERR_INVALID_TYPE = "ERR_INVALID_TYPE"
    const val ERR_INVALID_ENUM = "ERR_INVALID_ENUM"
    
    // Reference errors (ERR_REF_xxx)
    const val ERR_REF_DEVICE_NOT_FOUND = "ERR_REF_DEVICE_NOT_FOUND"
    const val ERR_REF_PORT_NOT_FOUND = "ERR_REF_PORT_NOT_FOUND"
    
    // Business rule errors (ERR_RULE_xxx)
    const val ERR_RULE_DUPLICATE_ID = "ERR_RULE_DUPLICATE_ID"
    const val ERR_RULE_DUPLICATE_PORT_USAGE = "ERR_RULE_DUPLICATE_PORT_USAGE"
    const val ERR_RULE_SELF_CONNECTION = "ERR_RULE_SELF_CONNECTION"
    const val ERR_RULE_INVALID_RANGE = "ERR_RULE_INVALID_RANGE"
    
    // Warnings (WARN_xxx)
    const val WARN_DEVICE_OVERLAP = "WARN_DEVICE_OVERLAP"
    const val WARN_TIGHT_CLEARANCE = "WARN_TIGHT_CLEARANCE"
    const val WARN_PORT_DIRECTION = "WARN_PORT_DIRECTION"
    
    // Info (INFO_xxx)
    const val INFO_GRID_ALIGNMENT = "INFO_GRID_ALIGNMENT"
    const val INFO_PORT_DISTRIBUTION = "INFO_PORT_DISTRIBUTION"
}
```

---

## Platform-Specific Implementation

### JVM (Desktop)

**File Chooser:**
```kotlin
// jvmMain/kotlin/FileChooserJvm.kt
actual fun openFileChooser(): String? {
    return JFileChooser().apply {
        fileFilter = FileNameExtensionFilter("JSON files", "json")
    }.let { chooser ->
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            chooser.selectedFile.absolutePath
        } else null
    }
}
```

**File I/O:**
```kotlin
// jvmMain/kotlin/FileReaderJvm.kt
actual fun createFileReader(): FileReader = object : FileReader {
    override suspend fun readFile(path: String): Result<String> {
        return runCatching {
            File(path).readText()
        }
    }
}
```

### JS (Web)

**File Chooser:**
```kotlin
// jsMain/kotlin/FileChooserJs.kt
actual fun openFileChooser(): String? {
    // Use HTML5 file input
    val input = document.createElement("input") as HTMLInputElement
    input.type = "file"
    input.accept = ".json"
    input.click()
    // Return via callback/promise
}
```

**File I/O:**
```kotlin
// jsMain/kotlin/FileReaderJs.kt
actual fun createFileReader(): FileReader = object : FileReader {
    override suspend fun readFile(path: String): Result<String> {
        return suspendCoroutine { continuation ->
            val reader = FileReader()
            reader.onload = { event ->
                continuation.resume(Result.success(event.target.result as String))
            }
            reader.onerror = {
                continuation.resume(Result.failure(Exception("Failed to read file")))
            }
            reader.readAsText(Blob(arrayOf(path)))
        }
    }
}
```

---

## UI Mockup

### Main Screen Layout

```
┌────────────────────────────────────────────────────────┐
│  Workstation JSON Validator           [About] [Export] │
├────────────────────────────────────────────────────────┤
│                                                         │
│  ┌──────────────────────────────────────────────────┐  │
│  │  📁  Choose workstation.json file                │  │
│  │                                                   │  │
│  │  [  Select File  ]  or  Drop file here          │  │
│  │                                                   │  │
│  │  Recent files:                                   │  │
│  │  • workstation-2025-12-31.json                   │  │
│  │  • workstation-backup.json                       │  │
│  └──────────────────────────────────────────────────┘  │
│                                                         │
└────────────────────────────────────────────────────────┘
```

### Results Screen Layout

```
┌────────────────────────────────────────────────────────┐
│  workstation.json                      [New] [Export]  │
├────────────────────────────────────────────────────────┤
│  ┌──────────────────────────────────────────────────┐  │
│  │  ❌ Validation Failed                            │  │
│  │  • 3 Errors  • 2 Warnings  • 1 Info             │  │
│  └──────────────────────────────────────────────────┘  │
│                                                         │
│  ┌─ Errors ─────────────────────────────────────────┐  │
│  │  🔴 ERR_RULE_DUPLICATE_PORT_USAGE               │  │
│  │     Port 'usb-c-1' on device 'laptop-main' is   │  │
│  │     used by multiple connections                 │  │
│  │     Location: devices[0].ports[0]                │  │
│  │     Connections: conn-1, conn-2                  │  │
│  │     💡 Each port can only be used once           │  │
│  │     [Show in JSON] [Copy Error]                  │  │
│  │                                                   │  │
│  │  🔴 ERR_REF_DEVICE_NOT_FOUND                    │  │
│  │     Connection 'conn-5' references non-existent  │  │
│  │     device 'monitor-3'                           │  │
│  │     Location: connections[4].targetDeviceId      │  │
│  │     💡 Check device IDs match exactly            │  │
│  └──────────────────────────────────────────────────┘  │
│                                                         │
│  ┌─ Warnings ───────────────────────────────────────┐  │
│  │  ⚠️  WARN_DEVICE_OVERLAP                         │  │
│  │     Devices 'laptop' and 'monitor-1' may overlap │  │
│  │     💡 Ensure 10+ units clearance between devices│  │
│  └──────────────────────────────────────────────────┘  │
│                                                         │
│  ┌─ JSON Preview ──────────────────────────────────┐  │
│  │  {                                                │  │
│  │    "devices": [                                   │  │
│  │      {                                            │  │
│  │❌      "id": "laptop-main",    ← Error here      │  │
│  │        "ports": [                                 │  │
│  │❌        { "id": "usb-c-1" }   ← Port used twice │  │
│  └──────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────┘
```

---

## Implementation Phases

### Phase 1: Project Restructuring (Week 1)

**Tasks:**
1. Create `shared` module
   - Set up build.gradle.kts
   - Configure multiplatform targets
2. Move shared code to `shared`
   - data/model → shared/commonMain/data/model
   - data/serialization → shared/commonMain/data/serialization
   - data/validation → shared/commonMain/data/validation
3. Rename `app` directory
   - Move current src → app/src
   - Update app/build.gradle.kts
4. Update app to depend on `shared`
   - Add `implementation(project(":shared"))`
   - Fix imports
5. Verify app still works

**Estimated Time**: 3-4 hours

---

### Phase 2: Enhanced Validation (Week 1-2)

**Tasks:**
1. Extend `ValidationResult` classes
   - Add `ValidationError` with location
   - Add `ValidationWarning` with suggestions
   - Add `ErrorLocation` with path tracking
2. Enhance `DataValidator`
   - Add JSON syntax validation
   - Add line/column tracking
   - Add error code system
   - Add suggestion generation
3. Implement grid alignment checks
4. Implement device overlap detection
5. Add comprehensive error messages
6. Write unit tests for validators

**Estimated Time**: 8-10 hours

---

### Phase 3: Validator App UI (Week 2)

**Tasks:**
1. Create `validator` module
   - Set up build.gradle.kts
   - Configure desktop/web targets
2. Create ValidatorViewModel
   - State management
   - File loading logic
   - Export functionality
3. Create UI components
   - FileChooserScreen
   - ValidationResultsScreen
   - ErrorDetailView
   - JSONPreviewPane
4. Implement platform-specific file I/O
   - JVM: JFileChooser
   - JS: HTML5 file input
5. Add error highlighting in JSON preview

**Estimated Time**: 12-15 hours

---

### Phase 4: Polish & Testing (Week 3)

**Tasks:**
1. Add drag & drop file support
2. Add recent files list
3. Implement export functionality
   - Export as text
   - Export as JSON
   - Copy to clipboard
4. Add keyboard shortcuts
5. Improve error messages
6. Add sample files
7. Create comprehensive test suite
8. Write user documentation

**Estimated Time**: 8-10 hours

---

## Testing Strategy

### Unit Tests
- Validation logic (all error codes)
- Data model serialization
- Error location tracking
- Suggestion generation

### Integration Tests
- File loading (JVM/JS)
- Complete validation flow
- Export functionality

### Manual Testing
- File chooser on desktop
- File input on web
- Error display clarity
- JSON highlighting accuracy

---

## Performance Considerations

### Optimization Targets
- Validation: < 100ms for typical files
- File loading: < 500ms
- UI rendering: < 16ms per frame

### Strategies
- Lazy error evaluation
- Virtual scrolling for large error lists
- Incremental JSON parsing
- Memoized validation results

---

## Documentation

### User Documentation
1. How to use validator
2. Understanding error messages
3. How to fix common errors
4. FAQ

### Developer Documentation
1. Project structure
2. Adding new validation rules
3. Error code conventions
4. Contributing guide

---

## Success Metrics

### Functional
- ✅ All validation rules documented in specs are implemented
- ✅ Error messages clearly identify problem and solution
- ✅ Works on both web and desktop
- ✅ Handles files up to 10MB

### Non-Functional
- ✅ Validation completes in < 1 second
- ✅ 95%+ test coverage
- ✅ No crashes on invalid JSON
- ✅ Accessible UI (keyboard navigation, screen readers)

---

## Future Enhancements

### Phase 5+ (Optional)
- Real-time validation as you type
- Visual diff between file versions
- Auto-fix for common errors
- Batch validation of multiple files
- CI/CD integration (CLI mode)
- VS Code extension
- JSON schema generation

---

## Summary

**Total Estimated Time**: 31-39 hours (4-5 working days)

**Key Benefits:**
- ✅ Reuses 60%+ of existing code via `shared` module
- ✅ Clear separation of concerns
- ✅ Maintains main app functionality
- ✅ Provides valuable standalone tool
- ✅ Easy to extend with new validation rules
- ✅ Multi-platform support out of the box
