# Data Model

## Overview

The application uses a JSON-based data format (`workstation.json`) with a hierarchical structure representing devices, ports, connections, and configuration.

---

## WorkstationLayout

The root data structure containing:
- **devices**: List of devices in the diagram
- **connections**: List of connections between device ports
- **metadata**: Configuration and layout information

---

## Device

Represents a hardware component (laptop, monitor, hub, peripheral).

### Properties

- **id**: Unique identifier
- **title**: Display name
- **label**: Device label/type
- **category**: Device category (HUB, DEVICE, HOST)
- **position**: Position in coordinate space
- **size**: Width and height
- **ports**: List of ports on the device
- **specifications**: Optional device specifications
- **url**: Optional external link

### Ports

Each device has ports for connections:
- **id**: Unique identifier within device
- **name**: Port name
- **direction**: INPUT, OUTPUT, or BIDIRECTIONAL
- **position**: Position along device edge (side and offset)

---

## Connection

Represents a link between two device ports.

### Properties

- **id**: Unique identifier
- **sourceDeviceId**: Source device reference
- **sourcePortId**: Source port reference
- **targetDeviceId**: Target device reference
- **targetPortId**: Target port reference
- **routingPoints**: Optional manual routing waypoints

### Routing Points

Optional user-defined waypoints for manual routing:
- Points in virtual coordinates
- Intermediate waypoints between ports
- Used when automatic routing is not desired

---

## LayoutMetadata

Configuration and layout information.

### Properties

- **title**: Diagram title
- **date**: Creation/modification date
- **canvasSize**: Canvas dimensions
- **theme**: Optional theme configuration
- **version**: Format version
- **coordinateSystem**: "absolute" or "virtual"
- **virtualCanvas**: Virtual canvas size (when using virtual coordinates)
- **viewport**: Viewport configuration (zoom limits, defaults)
- **grid**: Grid configuration (size, visibility)

---

## Data Relationships

### Device-Port Relationship

- One device has many ports
- Ports belong to one device
- Port IDs are unique within a device

### Connection Relationships

- Connections reference two devices (source and target)
- Connections reference two ports (source and target)
- One port can be used by at most one connection (1-to-1 rule)
- Connections may have routing points for manual routing

### Coordinate System

- Devices use position and size in coordinate space
- Ports use position relative to device edges
- Connections use routing points in coordinate space
- Coordinate system specified in metadata

---

## Data Constraints

### Unique Identifiers

- Device IDs must be unique across all devices
- Port IDs must be unique within each device
- Connection IDs must be unique across all connections

### Port Usage

- Each port can be used by at most one connection
- Prevents multiple connections from the same port
- Ensures clear connection topology

### References

- Connection device/port references must exist
- Source and target devices must be different
- References validated during data loading

---

## Related Documentation

- [COORDINATE_SYSTEM.md](COORDINATE_SYSTEM.md) - Coordinate system in data model
- [VALIDATION.md](VALIDATION.md) - Data validation rules
- [ROUTING_SYSTEM.md](ROUTING_SYSTEM.md) - Routing points in connections

