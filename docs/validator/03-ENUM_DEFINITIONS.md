# Enum Definitions

## Overview

This document lists all valid enum values that the validator must recognize. These values are **case-sensitive** and must match exactly.

---

## DeviceType

Valid values for `device.type`:

```
LAPTOP
MONITOR
DOCKING_STATION
USB_HUB
GAMING_CONSOLE
CAMERA
AUDIO_DEVICE
STORAGE
```

### Usage Examples

```json
{ "type": "LAPTOP" }          // ✅ Valid
{ "type": "MONITOR" }         // ✅ Valid
{ "type": "laptop" }          // ❌ Invalid (lowercase)
{ "type": "Desktop" }         // ❌ Invalid (not in list)
```

---

## DeviceCategory

Valid values for `device.category`:

```
HUB
PERIPHERAL
CENTRAL_DEVICE
```

### Description

- **HUB**: Devices that connect multiple other devices (docking stations, USB hubs)
- **PERIPHERAL**: Accessory devices (monitors, storage, cameras)
- **CENTRAL_DEVICE**: Primary devices (laptops, desktops, gaming consoles)

### Usage Examples

```json
{ "category": "CENTRAL_DEVICE" }    // ✅ Valid
{ "category": "HUB" }               // ✅ Valid  
{ "category": "Central" }           // ❌ Invalid (wrong format)
```

---

## PortType

Valid values for `port.type`:

```
USB_C
USB_A_2_0
USB_A_3_0
USB_A_3_1
USB_A_3_2
HDMI
HDMI_2_1
DISPLAY_PORT
MINI_HDMI
MICRO_HDMI
ETHERNET
AUX
POWER
```

### Port Type Categories

**USB Ports:**
- `USB_C` - USB Type-C connector
- `USB_A_2_0` - USB Type-A USB 2.0
- `USB_A_3_0` - USB Type-A USB 3.0
- `USB_A_3_1` - USB Type-A USB 3.1
- `USB_A_3_2` - USB Type-A USB 3.2

**Video Ports:**
- `HDMI` - Standard HDMI
- `HDMI_2_1` - HDMI 2.1
- `DISPLAY_PORT` - DisplayPort
- `MINI_HDMI` - Mini HDMI connector
- `MICRO_HDMI` - Micro HDMI connector

**Other Ports:**
- `ETHERNET` - RJ45 network port
- `AUX` - 3.5mm audio jack
- `POWER` - Power connector

### Usage Examples

```json
{ "type": "USB_C" }           // ✅ Valid
{ "type": "HDMI" }            // ✅ Valid
{ "type": "usb-c" }           // ❌ Invalid (lowercase with hyphen)
{ "type": "USB_Type_C" }      // ❌ Invalid (not in list)
```

---

## PortDirection

Valid values for `port.direction`:

```
INPUT
OUTPUT
BIDIRECTIONAL
```

### Description

- **INPUT**: Port receives data/signal (e.g., monitor HDMI input)
- **OUTPUT**: Port sends data/signal (e.g., laptop HDMI output)
- **BIDIRECTIONAL**: Port can both send and receive (e.g., USB-C)

### Usage Examples

```json
{ "direction": "INPUT" }          // ✅ Valid
{ "direction": "BIDIRECTIONAL" }  // ✅ Valid
{ "direction": "input" }          // ❌ Invalid (lowercase)
{ "direction": "IN" }             // ❌ Invalid (abbreviation not allowed)
```

---

## DeviceSide

Valid values for `port.position.side`:

```
TOP
BOTTOM
LEFT
RIGHT
```

### Description

Indicates which side of the device the port is located on:
- **TOP**: Top edge of device
- **BOTTOM**: Bottom edge of device
- **LEFT**: Left edge of device
- **RIGHT**: Right edge of device

### Usage Examples

```json
{ "side": "TOP" }         // ✅ Valid
{ "side": "LEFT" }        // ✅ Valid
{ "side": "top" }         // ❌ Invalid (lowercase)
{ "side": "NORTH" }       // ❌ Invalid (not in list)
```

---

## ConnectionCategory

Valid values for `connection.connectionType.category`:

```
DATA
VIDEO
AUDIO
POWER
NETWORK
```

### Description

- **DATA**: Data transfer connections (USB, Thunderbolt)
- **VIDEO**: Video signal connections (HDMI, DisplayPort)
- **AUDIO**: Audio connections (aux cables)
- **POWER**: Power delivery connections
- **NETWORK**: Network connections (Ethernet)

### Usage Examples

```json
{ "category": "DATA" }        // ✅ Valid
{ "category": "VIDEO" }       // ✅ Valid
{ "category": "data" }        // ❌ Invalid (lowercase)
{ "category": "USB" }         // ❌ Invalid (not a category)
```

---

## Validation Rules

### Case Sensitivity

ALL enum values are **CASE-SENSITIVE**. The validator MUST:
- ✅ Accept exact matches only (e.g., `"USB_C"`)
- ❌ Reject case variations (e.g., `"usb_c"`, `"Usb_C"`)
- ❌ Reject abbreviations or alternatives

### Unknown Values

If an unknown enum value is encountered:
```json
{
  "valid": false,
  "errors": [{
    "code": "ERR_INVALID_ENUM",
    "message": "Invalid PortType 'usb-c'. Must be one of: USB_C, USB_A_2_0, ...",
    "location": {
      "device": "laptop-main",
      "port": "port-1",
      "field": "type"
    },
    "severity": "ERROR"
  }]
}
```

---

## Enum Validation Pseudo-Code

```python
VALID_DEVICE_TYPES = {
    "LAPTOP", "MONITOR", "DOCKING_STATION", "USB_HUB",
    "GAMING_CONSOLE", "CAMERA", "AUDIO_DEVICE", "STORAGE"
}

VALID_DEVICE_CATEGORIES = {
    "HUB", "PERIPHERAL", "CENTRAL_DEVICE"
}

VALID_PORT_TYPES = {
    "USB_C", "USB_A_2_0", "USB_A_3_0", "USB_A_3_1", "USB_A_3_2",
    "HDMI", "HDMI_2_1", "DISPLAY_PORT", "MINI_HDMI", "MICRO_HDMI",
    "ETHERNET", "AUX", "POWER"
}

VALID_PORT_DIRECTIONS = {
    "INPUT", "OUTPUT", "BIDIRECTIONAL"
}

VALID_DEVICE_SIDES = {
    "TOP", "BOTTOM", "LEFT", "RIGHT"
}

VALID_CONNECTION_CATEGORIES = {
    "DATA", "VIDEO", "AUDIO", "POWER", "NETWORK"
}

def validate_enum(value, valid_set, enum_name):
    if value not in valid_set:
        raise ValidationError(
            f"Invalid {enum_name}: '{value}'. Must be one of: {', '.join(sorted(valid_set))}"
        )
```

---

## Future Enum Extensions

If new enum values are added in future versions:
- The validator should be updated to support them
- Older validators may reject newer enum values (expected behavior)
- Consider using version checking to allow forward compatibility

---

## Summary Table

| Enum | Values | Used In | Case Sensitive |
|------|--------|---------|----------------|
| DeviceType | 8 values | `device.type` | ✅ Yes |
| DeviceCategory | 3 values | `device.category` | ✅ Yes |
| PortType | 13 values | `port.type` | ✅ Yes |
| PortDirection | 3 values | `port.direction` | ✅ Yes |
| DeviceSide | 4 values | `port.position.side` | ✅ Yes |
| ConnectionCategory | 5 values | `connection.connectionType.category` | ✅ Yes |

---

## Next: Validation Rules

See `04-VALIDATION_RULES.md` for complete validation logic.
