# Workstation JSON Validation Rules

## Overview

This document defines the validation rules for `workstation.json` files to ensure data integrity and proper intelligent routing functionality.

---

## Metadata Rules

### Canvas Size
- ✅ **MUST** have `canvasSize` object with `width` and `height`
- ✅ `width` and `height` **MUST** be positive numbers > 0
- ✅ **RECOMMENDED**: Use multiples of 10 for grid alignment (e.g., 1920, 1080)

### Version
- ✅ **MUST** have `version` field
- ✅ Version **MUST** be a string (e.g., "1.0")

### Theme
- ⚠️ **OPTIONAL**: `theme` object with `isDark` boolean
- ✅ If provided, `isDark` **MUST** be boolean

---

## Device Rules

### Required Fields
- ✅ **MUST** have unique `id` (string)
- ✅ **MUST** have `name` (string, non-empty)
- ✅ **MUST** have `type` (valid DeviceType enum)
- ✅ **MUST** have `category` (valid DeviceCategory enum)
- ✅ **MUST** have `position` object with `x` and `y`
- ✅ **MUST** have `size` object with `width` and `height`
- ✅ **MUST** have `ports` array (can be empty)

### Device ID
- ✅ **MUST** be unique across all devices
- ✅ **MUST** be non-empty string
- ✅ **RECOMMENDED**: Use kebab-case (e.g., `laptop-main`, `monitor-1`)
- ❌ **MUST NOT** contain spaces or special characters except hyphen

### Device Position
- ✅ `x` and `y` **MUST** be non-negative numbers (>= 0)
- ✅ **RECOMMENDED**: Use multiples of 10 for grid alignment
- ✅ Devices **SHOULD** fit within canvas bounds: `x + width <= canvasWidth`, `y + height <= canvasHeight`

### Device Size
- ✅ `width` and `height` **MUST** be positive numbers > 0
- ✅ **RECOMMENDED**: Use multiples of 10 for grid alignment
- ✅ **RECOMMENDED**: Minimum size 50×50 for usability
- ⚠️ **WARNING**: Very small devices (< 50×50) may have port positioning issues

---

## Port Rules

### Required Fields
- ✅ **MUST** have unique `id` within the device
- ✅ **MUST** have `name` (string, non-empty)
- ✅ **MUST** have `type` (valid PortType enum)
- ✅ **MUST** have `direction` (INPUT, OUTPUT, or BIDIRECTIONAL)
- ✅ **MUST** have `position` object with `side` and `offset`

### Port ID
- ✅ **MUST** be unique within the same device
- ✅ **CAN** be reused across different devices
- ✅ **RECOMMENDED**: Use kebab-case (e.g., `usb-c-1`, `hdmi-in`)

### Port Position - Side
- ✅ **MUST** be one of: `TOP`, `BOTTOM`, `LEFT`, `RIGHT`
- ✅ Ports on same device **CAN** use the same side
- ✅ **RECOMMENDED**: Distribute ports across multiple sides to avoid overlap

### Port Position
- ✅ **MUST** be a non-negative number in virtual units
- ✅ For LEFT and RIGHT sides, position is measured from the top edge of the device
- ✅ For TOP and BOTTOM sides, position is measured from the left edge of the device
- ✅ **POSITION CLAMPING**: Values outside device bounds are automatically adjusted:
  - Positions less than 0 are set to 0
  - Positions exceeding device size (height for LEFT/RIGHT, width for TOP/BOTTOM) use the maximum value
- ✅ **RECOMMENDED**: Position ports at device midpoints for common layout patterns
- ✅ **RECOMMENDED**: Space ports evenly along the device edge

### Port Distribution Best Practices
- ✅ **AVOID**: Clustering many ports on one side (> 4 ports)
- ✅ **RECOMMENDED**: Distribute across TOP/BOTTOM/LEFT/RIGHT
- ✅ **RECOMMENDED**: Ports should face their target devices when possible

---

## Connection Rules

### Required Fields
- ✅ **MUST** have unique `id`
- ✅ **MUST** have `sourceDeviceId` (references existing device)
- ✅ **MUST** have `sourcePortId` (references existing port on source device)
- ✅ **MUST** have `targetDeviceId` (references existing device)
- ✅ **MUST** have `targetPortId` (references existing port on target device)

### Connection ID
- ✅ **MUST** be unique across all connections
- ✅ **RECOMMENDED**: Use kebab-case with prefix (e.g., `conn-1`, `conn-hdmi-laptop-monitor`)

### Device and Port References
- ✅ `sourceDeviceId` **MUST** reference an existing device ID
- ✅ `targetDeviceId` **MUST** reference an existing device ID
- ✅ `sourcePortId` **MUST** exist on the source device
- ✅ `targetPortId` **MUST** exist on the target device
- ❌ **MUST NOT** connect a device to itself (source device ≠ target device)

### Port Usage (1-to-1 Rule)
- ✅ Each port **MUST** be used by at most ONE connection
- ❌ **INVALID**: Two connections using the same port
- ✅ A device **CAN** have multiple connections (using different ports)

**Example - Valid:**
```json
{
  "connections": [
    { 
      "id": "conn-1", 
      "sourceDeviceId": "laptop",
      "sourcePortId": "usb-c-1",
      "targetDeviceId": "hub",
      "targetPortId": "usb-in-1" 
    },
    { 
      "id": "conn-2", 
      "sourceDeviceId": "laptop",
      "sourcePortId": "usb-c-2",
      "targetDeviceId": "hub", 
      "targetPortId": "usb-in-2"
    }
  ]
}
```

**Example - Invalid:**
```json
{
  "connections": [
    { 
      "id": "conn-1", 
      "sourceDeviceId": "laptop",
      "sourcePortId": "usb-c-1",
      "targetDeviceId": "hub",
      "targetPortId": "usb-in-1" 
    },
    { 
      "id": "conn-2", 
      "sourceDeviceId": "laptop",
      "sourcePortId": "usb-c-1",  // ❌ Same port used twice!
      "targetDeviceId": "monitor", 
      "targetPortId": "usb-in-1"
    }
  ]
}
```

### Port Direction Compatibility
- ✅ OUTPUT → INPUT (valid)
- ✅ OUTPUT → BIDIRECTIONAL (valid)
- ✅ BIDIRECTIONAL → INPUT (valid)
- ✅ BIDIRECTIONAL → BIDIRECTIONAL (valid)
- ⚠️ INPUT → INPUT (technically allowed but unusual)
- ⚠️ OUTPUT → OUTPUT (technically allowed but unusual)

### Connection Type

---

## Intelligent Routing Constraints

### Grid Alignment (Recommended)
- ✅ Device positions **SHOULD** be multiples of 10 (grid cell size)
- ✅ Device sizes **SHOULD** be multiples of 10
- ✅ Canvas size **SHOULD** be multiples of 10

**Why?** This ensures clean grid-aligned routing without fractional positioning.

### Clearance Requirements
- ✅ Devices **SHOULD** have at least 10 units of space between them
- ✅ Devices **SHOULD NOT** overlap with each other
- ⚠️ **WARNING**: Overlapping devices will block routing paths

### Port Extension Space
- ✅ Ports **SHOULD** have at least 20 units of clear space extending from device edge
- ⚠️ Ports too close to canvas edge may have limited routing options

### Connection Complexity
- ✅ **RECOMMENDED**: Keep connection paths as short as possible
- ✅ **RECOMMENDED**: Avoid devices positioned between connected endpoints
- ✅ **AVOID**: Excessive connection crossings (impacts visual clarity)

---

## Validation Checklist

Use this checklist when creating or modifying `workstation.json`:

### Structure Validation
- [ ] Valid JSON syntax
- [ ] Has `metadata`, `devices`, and `connections` top-level keys
- [ ] All required fields present

### Device Validation
- [ ] All device IDs are unique
- [ ] All devices have valid positions (>= 0)
- [ ] All devices have valid sizes (> 0)
- [ ] Devices fit within canvas bounds
- [ ] Device sizes are multiples of 10 (recommended)

### Port Validation
- [ ] All port IDs are unique within each device
- [ ] All port offsets are between 0.0 and 1.0
- [ ] Port sides are valid (TOP/BOTTOM/LEFT/RIGHT)
- [ ] Ports are distributed across multiple sides (recommended)

### Connection Validation
- [ ] All connection IDs are unique
- [ ] All referenced device IDs exist
- [ ] All referenced port IDs exist on correct devices
- [ ] Each port is used by at most one connection (1-to-1 rule)
- [ ] No self-connections (device to itself)

### Routing Validation
- [ ] Devices don't overlap
- [ ] Adequate clearance between devices (>= 10 units)
- [ ] Port extensions have clear space (>= 20 units)
- [ ] Grid alignment followed (positions/sizes multiples of 10)

---

## Common Validation Errors

### ❌ Duplicate Port Usage
```json
// ERROR: Port "usb-c-1" used twice
{
  "connections": [
    { "id": "conn-1", "sourceDeviceId": "laptop", "sourcePortId": "usb-c-1", ... },
    { "id": "conn-2", "sourceDeviceId": "laptop", "sourcePortId": "usb-c-1", ... }
  ]
}
```

**Fix:** Use different ports or remove duplicate connection.

### ❌ Invalid Device Reference
```json
// ERROR: Device "monitor-3" doesn't exist
{
  "connections": [
    { "id": "conn-1", "sourceDeviceId": "laptop", "targetDeviceId": "monitor-3", ... }
  ]
}
```

**Fix:** Ensure device ID matches exactly (case-sensitive).

### ❌ Invalid Port Reference
```json
// ERROR: Port "dp-out" doesn't exist on laptop
{
  "connections": [
    { "id": "conn-1", "sourceDeviceId": "laptop", "sourcePortId": "dp-out", ... }
  ]
}
```

**Fix:** Check port ID exists on the specified device.

### ❌ Port Offset Out of Range
```json
// ERROR: Offset must be between 0.0 and 1.0
{
  "ports": [
    { "id": "usb-1", "position": { "side": "LEFT", "offset": 1.5 } }
  ]
}
```

**Fix:** Use values between 0.0 and 1.0 (e.g., 0.5).

### ❌ Overlapping Devices
```json
// ERROR: Devices overlap
{
  "devices": [
    { "id": "device-1", "position": { "x": 100, "y": 100 }, "size": { "width": 200, "height": 150 } },
    { "id": "device-2", "position": { "x": 150, "y": 120 }, "size": { "width": 200, "height": 150 } }
  ]
}
```

**Fix:** Adjust positions to avoid overlap with adequate clearance.

---

## Validation Implementation

The validation is implemented in:
```
src/commonMain/kotlin/dev/akexorcist/workstation/data/validation/WorkstationValidator.kt
```

### Validation Methods

- `validateLayout(layout: WorkstationLayout): ValidationResult`
- `validateDevices(devices: List<Device>): ValidationResult`
- `validateConnections(connections: List<Connection>, devices: List<Device>): ValidationResult`
- `validatePortUsage(layout: WorkstationLayout): ValidationResult`

### Usage Example

```kotlin
val layout = loadWorkstationLayout("workstation.json")
val result = WorkstationValidator.validateLayout(layout)

when (result) {
    is ValidationResult.Success -> println("Valid!")
    is ValidationResult.Error -> println("Errors: ${result.errors}")
}
```

---

## Best Practices Summary

✅ **DO:**
- Use unique IDs throughout
- Follow grid alignment (multiples of 10)
- Distribute ports across device sides
- Maintain adequate clearance between devices
- Follow 1-to-1 port connection rule
- Use descriptive names for devices and ports

❌ **DON'T:**
- Reuse port IDs within same device
- Connect same port to multiple connections
- Overlap devices
- Use port offsets outside 0.0-1.0 range
- Create self-connections
- Cluster too many ports on one side

---

## Tools & Resources

### JSON Validation
- Use a JSON validator before loading
- Schema validation (future enhancement)
- Linter integration (future enhancement)

### Visual Validation
- Load in application and check for routing failures
- Look for red/orange warning colors indicating failed routes
- Check for overlapping connections or devices

### Debug Mode
- Enable debug grid overlay to verify alignment
- Check clearance zones visualization
- Review crossing points and failed routes

---

## Version History

- **v1.0** - Initial validation rules
  - Basic structure validation
  - 1-to-1 port connection rule
  - Grid alignment recommendations
  - Device bounds checking for ports
