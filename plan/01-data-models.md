# Data Models & Validation

## Data Models

### Device Model

**File:** `data/model/Device.kt`

```kotlin
data class Device(
    val id: String,
    val name: String,
    val model: String,
    val type: DeviceType,
    val category: DeviceCategory,
    val position: Position, // x, y coordinates
    val size: Size, // width, height
    val ports: List<Port>,
    val specifications: DeviceSpecifications
)

enum class DeviceType {
    LAPTOP, MONITOR, DOCKING_STATION, USB_HUB, 
    GAMING_CONSOLE, CAMERA, AUDIO_DEVICE, STORAGE, ...
}

enum class DeviceCategory {
    HUB, PERIPHERAL, CENTRAL_DEVICE
}

data class Port(
    val id: String,
    val name: String,
    val type: PortType,
    val direction: PortDirection, // INPUT, OUTPUT, BIDIRECTIONAL
    val position: PortPosition // relative to device (top, bottom, left, right, specific offset)
)

enum class PortType {
    USB_C, USB_A_2_0, USB_A_3_0, USB_A_3_1, USB_A_3_2,
    HDMI, HDMI_2_1, DISPLAY_PORT, MINI_HDMI, MICRO_HDMI,
    ETHERNET, AUX, POWER, ...
}

enum class PortDirection {
    INPUT, OUTPUT, BIDIRECTIONAL
}

data class PortPosition(
    val side: DeviceSide, // TOP, BOTTOM, LEFT, RIGHT
    val offset: Float // 0.0 to 1.0 along the side
)

enum class DeviceSide {
    TOP, BOTTOM, LEFT, RIGHT
}

data class DeviceSpecifications(
    val manufacturer: String?,
    val modelNumber: String?,
    val technicalSpecs: Map<String, String> // key-value pairs
)

data class Position(
    val x: Float,
    val y: Float
)

data class Size(
    val width: Float,
    val height: Float
)
```

### Connection Model

**File:** `data/model/Connection.kt`

```kotlin
data class Connection(
    val id: String,
    val sourceDeviceId: String,
    val sourcePortId: String,
    val targetDeviceId: String,
    val targetPortId: String,
    val connectionType: ConnectionType,
    val cableSpecification: CableSpecification?,
    val routingPoints: List<Point>? // null = auto-calculate, populated = manual override
)

data class ConnectionType(
    val name: String, // "USB-C 3.2 Gen 2", "HDMI 2.1", etc.
    val category: ConnectionCategory
)

enum class ConnectionCategory {
    DATA, VIDEO, AUDIO, POWER, NETWORK
}

data class CableSpecification(
    val length: String?,
    val brand: String?,
    val notes: String?
)

data class Point(
    val x: Float,
    val y: Float
)
```

### Layout Data

**File:** `data/model/Layout.kt`

```kotlin
data class WorkstationLayout(
    val devices: List<Device>,
    val connections: List<Connection>,
    val metadata: LayoutMetadata
)

data class LayoutMetadata(
    val title: String,
    val date: String,
    val canvasSize: Size,
    val theme: ThemeConfig?,
    val version: String = "1.0" // Data format version
)

data class ThemeConfig(
    val isDark: Boolean = true
)
```

## JSON Schema

**File:** `data/serialization/WorkstationLayoutSerializer.kt`

### Example JSON Structure

```json
{
  "metadata": {
    "title": "Akexorcist's Workstation",
    "date": "Feb 2025",
    "canvasSize": { "width": 1920, "height": 1080 },
    "version": "1.0",
    "theme": {
      "isDark": true
    }
  },
  "devices": [
    {
      "id": "laptop-office",
      "name": "Office Laptop",
      "model": "MacBook Pro",
      "type": "LAPTOP",
      "category": "CENTRAL_DEVICE",
      "position": { "x": 100, "y": 200 },
      "size": { "width": 200, "height": 150 },
      "ports": [
        {
          "id": "usb-c-1",
          "name": "USB-C Port 1",
          "type": "USB_C",
          "direction": "BIDIRECTIONAL",
          "position": { "side": "LEFT", "offset": 0.3 }
        }
      ],
      "specifications": {
        "manufacturer": "Apple",
        "modelNumber": "MBP-2023",
        "technicalSpecs": {
          "processor": "M2 Pro",
          "memory": "16GB"
        }
      }
    }
  ],
  "connections": [
    {
      "id": "conn-1",
      "sourceDeviceId": "laptop-office",
      "sourcePortId": "usb-c-1",
      "targetDeviceId": "docking-station",
      "targetPortId": "usb-c-input",
      "connectionType": {
        "name": "USB-C 3.2 Gen 2",
        "category": "DATA"
      },
      "cableSpecification": {
        "length": "1m",
        "brand": "Anker"
      }
    }
  ]
}
```

## Data Validation

**File:** `data/validation/DataValidator.kt`

### Validation Rules

#### 1. Device Validation

- Device IDs must be unique across all devices
- Position must be within canvas bounds (0 to canvasSize.width/height)
- Size must be positive (width > 0, height > 0)
- Port IDs must be unique within each device
- Port positions must be valid (offset between 0.0 and 1.0)
- Device type must be a valid enum value
- Device category must be a valid enum value

#### 2. Connection Validation

- Source and target device IDs must exist in devices list
- Source and target port IDs must exist on respective devices
- Port direction compatibility:
  - INPUT port cannot be source
  - OUTPUT port cannot be target
  - BIDIRECTIONAL ports can be either source or target
- No self-connections (device cannot connect to itself)
- Connection IDs must be unique across all connections
- Connection type must have valid category

#### 3. Data Integrity

- All referenced device/port IDs in connections must exist
- Check for orphaned connections (connections referencing non-existent devices/ports)
- Validate JSON schema structure
- Metadata must contain required fields (title, date, canvasSize)
- Canvas size must be positive

### Error Handling Strategy

**File:** `data/repository/WorkstationRepository.kt`

- Use `Result<T>` or sealed classes for error handling
- Provide descriptive error messages
- Log validation errors
- Graceful degradation (skip invalid entries with warnings)

```kotlin
sealed class LoadResult {
    data class Success(val layout: WorkstationLayout) : LoadResult()
    data class PartialSuccess(val layout: WorkstationLayout, val errors: List<String>) : LoadResult()
    data class Error(val message: String, val cause: Throwable?) : LoadResult()
}
```

**Error Types:**
- File not found
- JSON parse errors
- Validation errors
- Missing required fields
- Invalid references

## Data Format Versioning

**File:** `data/serialization/DataVersion.kt`

### Versioning Strategy

1. **Version Field:**
   - Add `version` field to JSON metadata
   - Current version: `1.0`
   - Increment on breaking changes

2. **Migration:**
   - Support reading older versions
   - Migrate data to current format
   - Provide migration utilities

3. **Backward Compatibility:**
   - Support missing optional fields
   - Default values for new fields
   - Deprecation warnings for old fields

**File:** `data/serialization/DataMigrator.kt`

- Migrate data between versions
- Handle missing/extra fields
- Validate after migration

### Version History

- **1.0** (Current): Initial version
  - Basic device and connection models
  - Port position support
  - Connection routing points (optional)

## Error Recovery & Resilience

**File:** `data/repository/ErrorRecovery.kt`

### Recovery Strategies

1. **Partial Data Loading:**
   - Load valid devices even if some are invalid
   - Skip invalid connections
   - Log errors for user review

2. **Fallback Values:**
   - Default device sizes if missing
   - Default port positions if invalid
   - Default colors if theme missing
   - Default canvas size if not specified

3. **Data Repair:**
   - Auto-fix common issues (negative positions, out-of-bounds)
   - Suggest corrections for invalid connections
   - Validate and repair on load

## Serialization Implementation

**File:** `data/serialization/WorkstationLayoutSerializer.kt`

### Requirements

- Use Kotlinx Serialization
- Support JSON format
- Handle nullable fields
- Support default values
- Custom serializers for enums if needed
- Handle version migration during deserialization

### Example Implementation

```kotlin
@Serializable
data class WorkstationLayout(
    val devices: List<Device>,
    val connections: List<Connection>,
    val metadata: LayoutMetadata
) {
    companion object {
        fun fromJson(json: String): Result<WorkstationLayout> {
            return try {
                val layout = Json.decodeFromString<WorkstationLayout>(json)
                // Validate after deserialization
                DataValidator.validate(layout).fold(
                    onSuccess = { Result.success(layout) },
                    onFailure = { Result.failure(it) }
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
```

## Repository Pattern

**File:** `data/repository/WorkstationRepository.kt`

### Responsibilities

- Load JSON data from resources or file system
- Parse JSON into domain models
- Validate data integrity
- Handle errors gracefully
- Provide data to presentation layer
- Support data migration

### Interface

```kotlin
interface WorkstationRepository {
    suspend fun loadLayout(): LoadResult
    suspend fun loadLayoutFromPath(path: String): LoadResult
    fun validateLayout(layout: WorkstationLayout): ValidationResult
}
```

