# Data Model Specification

## Complete JSON Schema

This document defines the exact structure of `workstation.json` that the validator must understand.

---

## Root Structure

```json
{
  "metadata": { ... },      // Required
  "devices": [ ... ],       // Required (can be empty array)
  "connections": [ ... ]    // Required (can be empty array)
}
```

---

## Metadata Object

### Structure

```json
{
  "metadata": {
    "title": "string",              // Required
    "date": "string",                // Required (ISO 8601 recommended)
    "canvasSize": {                  // Required
      "width": number,               // Required, > 0
      "height": number               // Required, > 0
    },
    "version": "string",             // Required (e.g., "1.0")
    "theme": {                       // Optional
      "isDark": boolean              // Optional, defaults to true
    }
  }
}
```

### Field Specifications

| Field | Type | Required | Validation | Default |
|-------|------|----------|------------|---------|
| `title` | string | ✅ Yes | Non-empty | - |
| `date` | string | ✅ Yes | Any string format | - |
| `canvasSize` | object | ✅ Yes | Must have width/height | - |
| `canvasSize.width` | number | ✅ Yes | > 0 | - |
| `canvasSize.height` | number | ✅ Yes | > 0 | - |
| `version` | string | ✅ Yes | Non-empty | - |
| `theme` | object | ❌ No | - | `{ "isDark": true }` |
| `theme.isDark` | boolean | ❌ No | - | `true` |

### Examples

**Minimal Valid:**
```json
{
  "metadata": {
    "title": "My Workstation",
    "date": "2025-12-31",
    "canvasSize": { "width": 1920, "height": 1080 },
    "version": "1.0"
  }
}
```

**Complete:**
```json
{
  "metadata": {
    "title": "Development Workstation",
    "date": "2025-12-31T10:30:00Z",
    "canvasSize": { "width": 1920, "height": 1080 },
    "version": "1.0",
    "theme": { "isDark": true }
  }
}
```

---

## Device Object

### Structure

```json
{
  "id": "string",                    // Required, unique
  "name": "string",                  // Required
  "model": "string",                 // Required
  "type": "DeviceType",              // Required (enum)
  "category": "DeviceCategory",      // Required (enum)
  "position": {                      // Required
    "x": number,                     // Required, >= 0
    "y": number                      // Required, >= 0
  },
  "size": {                          // Required
    "width": number,                 // Required, > 0
    "height": number                 // Required, > 0
  },
  "ports": [ ... ],                  // Required (array, can be empty)
  "specifications": {                // Optional
    "manufacturer": "string",        // Optional
    "modelNumber": "string",         // Optional
    "technicalSpecs": {              // Optional
      "key": "value"                 // Custom key-value pairs
    }
  }
}
```

### Field Specifications

| Field | Type | Required | Validation | Notes |
|-------|------|----------|------------|-------|
| `id` | string | ✅ Yes | Non-empty, unique across devices | kebab-case recommended |
| `name` | string | ✅ Yes | Non-empty | Display name |
| `model` | string | ✅ Yes | Non-empty | Product model |
| `type` | string | ✅ Yes | Must be valid DeviceType enum | See enum list |
| `category` | string | ✅ Yes | Must be valid DeviceCategory enum | See enum list |
| `position.x` | number | ✅ Yes | >= 0, within canvas bounds | Virtual units |
| `position.y` | number | ✅ Yes | >= 0, within canvas bounds | Virtual units |
| `size.width` | number | ✅ Yes | > 0 | Virtual units |
| `size.height` | number | ✅ Yes | > 0 | Virtual units |
| `ports` | array | ✅ Yes | Can be empty | Port objects |
| `specifications` | object | ❌ No | - | Metadata only |

### Example

```json
{
  "id": "laptop-main",
  "name": "Main Laptop",
  "model": "MacBook Pro",
  "type": "LAPTOP",
  "category": "HOST",
  "position": { "x": 100, "y": 200 },
  "size": { "width": 200, "height": 150 },
  "ports": [
    { ... }
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
```

---

## Port Object

### Structure

```json
{
  "id": "string",                    // Required, unique within device
  "name": "string",                  // Required
  "type": "PortType",                // Required (enum)
  "direction": "PortDirection",      // Required (enum)
  "position": {                      // Required
    "side": "DeviceSide",            // Required (enum)
    "offset": number                 // Required, 0.0-1.0
  }
}
```

### Field Specifications

| Field | Type | Required | Validation | Notes |
|-------|------|----------|------------|-------|
| `id` | string | ✅ Yes | Unique within device | kebab-case recommended |
| `name` | string | ✅ Yes | Non-empty | Display name |
| `type` | string | ✅ Yes | Must be valid PortType enum | See enum list |
| `direction` | string | ✅ Yes | INPUT, OUTPUT, or BIDIRECTIONAL | - |
| `position.side` | string | ✅ Yes | TOP, BOTTOM, LEFT, or RIGHT | - |
| `position.offset` | number | ✅ Yes | 0.0 <= value <= 1.0 | Percentage along edge |

### Offset Interpretation

- `0.0` = Start of edge (left for TOP/BOTTOM, top for LEFT/RIGHT)
- `0.5` = Middle of edge
- `1.0` = End of edge (right for TOP/BOTTOM, bottom for LEFT/RIGHT)

### Example

```json
{
  "id": "usb-c-1",
  "name": "USB-C Thunderbolt 1",
  "type": "USB_C",
  "direction": "BIDIRECTIONAL",
  "position": {
    "side": "LEFT",
    "offset": 0.3
  }
}
```

---

## Connection Object

### Structure

```json
{
  "id": "string",                    // Required, unique
  "sourceDeviceId": "string",        // Required (device reference)
  "sourcePortId": "string",          // Required (port reference)
  "targetDeviceId": "string",        // Required (device reference)
  "targetPortId": "string",          // Required (port reference)
  "connectionType": {                // Required
    "name": "string",                // Required
    "category": "ConnectionCategory" // Required (enum)
  },
  "cableSpecification": {            // Optional
    "length": "string",              // Optional
    "brand": "string"                // Optional
  }
}
```

### Field Specifications

| Field | Type | Required | Validation | Notes |
|-------|------|----------|------------|-------|
| `id` | string | ✅ Yes | Unique across connections | kebab-case recommended |
| `sourceDeviceId` | string | ✅ Yes | Must reference existing device ID | - |
| `sourcePortId` | string | ✅ Yes | Must exist on source device | - |
| `targetDeviceId` | string | ✅ Yes | Must reference existing device ID | ≠ sourceDeviceId |
| `targetPortId` | string | ✅ Yes | Must exist on target device | - |
| `connectionType.name` | string | ✅ Yes | Non-empty | Display name |
| `connectionType.category` | string | ✅ Yes | Must be valid ConnectionCategory | See enum list |
| `cableSpecification` | object | ❌ No | - | Metadata only |

### Example

```json
{
  "id": "conn-1",
  "sourceDeviceId": "laptop-main",
  "sourcePortId": "usb-c-1",
  "targetDeviceId": "docking-station",
  "targetPortId": "usb-c-host",
  "connectionType": {
    "name": "USB-C 3.2 Gen 2",
    "category": "DATA"
  },
  "cableSpecification": {
    "length": "1m",
    "brand": "Anker"
  }
}
```

---

## Type Definitions

### Numeric Types

- **integer**: Whole number (e.g., `42`, `-10`)
- **float/number**: Decimal number (e.g., `42.5`, `0.3`, `-10.2`)

### String Types

- **string**: Text value, must be non-empty where required
- Recommended format: kebab-case for IDs

### Boolean Types

- **boolean**: `true` or `false`

### Array Types

- **array**: Ordered list of values
- Can be empty (`[]`) unless otherwise specified

### Object Types

- **object**: Key-value pairs enclosed in `{}`
- Required fields must be present

---

## Validation Pseudo-Code

```python
def validate_workstation_json(data):
    # Check root structure
    assert "metadata" in data, "Missing metadata"
    assert "devices" in data, "Missing devices"
    assert "connections" in data, "Missing connections"
    
    # Validate metadata
    validate_metadata(data["metadata"])
    
    # Validate devices
    device_ids = set()
    for device in data["devices"]:
        validate_device(device)
        assert device["id"] not in device_ids, f"Duplicate device ID: {device['id']}"
        device_ids.add(device["id"])
    
    # Validate connections
    connection_ids = set()
    port_usage = {}  # Track which ports are used
    for connection in data["connections"]:
        validate_connection(connection, device_ids)
        assert connection["id"] not in connection_ids, f"Duplicate connection ID"
        
        # Check 1-to-1 port rule
        source_key = f"{connection['sourceDeviceId']}.{connection['sourcePortId']}"
        target_key = f"{connection['targetDeviceId']}.{connection['targetPortId']}"
        
        assert source_key not in port_usage, f"Port {source_key} used multiple times"
        assert target_key not in port_usage, f"Port {target_key} used multiple times"
        
        port_usage[source_key] = connection["id"]
        port_usage[target_key] = connection["id"]
    
    return ValidationResult(valid=True)
```

---

## Complete Example

See `workstation.json` in the project root for a complete, valid example file.

---

## Next: Enum Definitions

See `03-ENUM_DEFINITIONS.md` for complete enum value lists.
