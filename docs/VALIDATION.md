# Validation

## Overview

The validation system ensures data integrity and proper functionality by checking layout data against business rules and structural requirements.

---

## Validation Levels

### Success

Layout passes all validation checks and is ready for use.

### Error

Layout has errors that prevent proper functionality:
- Missing required references
- Invalid data structures
- Business rule violations
- Data integrity issues

---

## Structural Validation

### Device Validation

- **Unique IDs**: Device IDs must be unique across all devices
- **Valid Position**: Position must be within canvas bounds
- **Valid Size**: Size must be positive (width and height > 0)
- **Port IDs**: Port IDs must be unique within each device
- **Port Position**: Port positions must be non-negative

### Connection Validation

- **Unique IDs**: Connection IDs must be unique
- **Device References**: Source and target devices must exist
- **Port References**: Source and target ports must exist on their devices
- **Self-Connection**: Source and target devices must be different
- **Port Direction**: Port direction combinations must be valid

---

## Business Rules

### 1-to-1 Port Usage

**Rule**: Each port can be used by at most one connection.

**Validation**: Checks that no port is referenced by multiple connections. This ensures clear connection topology and prevents routing conflicts.

### Port Direction Compatibility

**Valid Combinations**:
- OUTPUT → INPUT
- OUTPUT → BIDIRECTIONAL
- BIDIRECTIONAL → INPUT
- BIDIRECTIONAL → BIDIRECTIONAL

**Invalid Combinations**:
- INPUT → INPUT (both are inputs)
- OUTPUT → OUTPUT (both are outputs)

---

## Reference Validation

### Device References

Connections must reference existing devices:
- Source device must exist
- Target device must exist
- References validated during data loading

### Port References

Connections must reference existing ports:
- Source port must exist on source device
- Target port must exist on target device
- Port IDs validated against device port lists

---

## Data Integrity

### Coordinate Validation

- Positions must be within canvas bounds
- Sizes must be positive
- Coordinates validated against canvas size

### Port Position Validation

- Port positions must be non-negative
- Positions clamped to device bounds at runtime
- Validation ensures basic constraints

---

## Validation Process

### Loading Time

Validation occurs when layout data is loaded:
1. Parse JSON data
2. Validate structure
3. Validate references
4. Validate business rules
5. Return result (Success or Error)

### Error Reporting

Validation errors are reported with:
- Error message describing the issue
- Context about what failed
- Information to help fix the issue

---

## Related Documentation

- [DATA_MODEL.md](DATA_MODEL.md) - Data structure being validated
- [ROUTING_SYSTEM.md](ROUTING_SYSTEM.md) - Routing compatibility validation

