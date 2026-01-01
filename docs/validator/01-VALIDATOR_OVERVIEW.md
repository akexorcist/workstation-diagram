# Workstation JSON Validator - Overview

## Purpose

This validator ensures that `workstation.json` files are structurally correct and compatible with the intelligent routing system. It can be implemented in any language/platform and must produce results that are compatible with the main Kotlin application.

---

## Validation Scope

### What This Validator Checks

1. **Structural Validity**
   - Valid JSON syntax
   - Required fields present
   - Correct data types
   - Valid enum values

2. **Referential Integrity**
   - Device IDs referenced by connections exist
   - Port IDs referenced by connections exist on correct devices
   - No dangling references

3. **Business Rules**
   - 1-to-1 port connections (no port used twice)
   - No self-connections (device to itself)
   - Unique IDs within scope
   - Valid ranges for numeric values

4. **Routing Compatibility**
   - Grid alignment recommendations
   - Device clearance requirements
   - Port positioning constraints
   - Canvas bounds checking

### What This Validator Does NOT Check

- Visual aesthetics or layout quality
- Actual routing success (handled by routing engine)
- Performance or optimization
- Theme or styling correctness

---

## Validation Levels

### ERROR (Must Fix)
- Blocks rendering entirely
- JSON cannot be loaded
- **Application Behavior**: Fails to load, shows error message
- **Examples**: Missing required field, invalid port reference, duplicate port usage

### WARNING (Should Fix)
- Layout loads but may have issues
- Routing might fail for specific connections
- **Application Behavior**: Loads but shows warnings, some routes may fail
- **Examples**: Devices overlap, poor grid alignment, tight clearances

### INFO (Recommended)
- Best practice suggestions
- Optimization opportunities
- **Application Behavior**: Works fine, informational only
- **Examples**: Could distribute ports better, consider grid alignment

---

## Output Format

The validator should output structured results that can be:
1. Displayed to users (human-readable)
2. Parsed by other tools (machine-readable)
3. Integrated into CI/CD pipelines

### Recommended Output Structure

```json
{
  "valid": true,
  "errors": [],
  "warnings": [
    {
      "code": "WARN_DEVICE_OVERLAP",
      "message": "Devices 'laptop-main' and 'monitor-1' may overlap",
      "location": {
        "devices": ["laptop-main", "monitor-1"]
      },
      "severity": "WARNING"
    }
  ],
  "info": [
    {
      "code": "INFO_GRID_ALIGNMENT",
      "message": "Device 'usb-hub' position not aligned to grid (350.5, 550)",
      "location": {
        "device": "usb-hub",
        "field": "position"
      },
      "severity": "INFO"
    }
  ],
  "summary": {
    "totalDevices": 6,
    "totalConnections": 7,
    "totalPorts": 18,
    "errorCount": 0,
    "warningCount": 1,
    "infoCount": 1
  }
}
```

---

## Integration Points

### Standalone Validator
- Command-line tool: `./validator workstation.json`
- Web service: `POST /validate` with JSON body
- Library: Import and call `validate(json)`

### CI/CD Integration
- Pre-commit hook
- GitHub Actions workflow
- GitLab CI pipeline
- Build step in deployment

### IDE Integration
- Real-time validation in JSON editors
- Lint errors/warnings display
- Auto-complete suggestions

---

## Implementation Languages

The validator can be implemented in any language. Common choices:

### JavaScript/TypeScript
- ✅ Easy JSON handling
- ✅ npm package distribution
- ✅ Browser and Node.js support
- 📦 Example: `@workstation/validator`

### Python
- ✅ Simple validation logic
- ✅ PyPI distribution
- ✅ CLI tool with argparse
- 📦 Example: `workstation-validator`

### Kotlin/Java
- ✅ Same language as main app
- ✅ Can share data models
- ✅ JVM ecosystem
- 📦 Example: `workstation-validator-jvm`

### Go
- ✅ Fast execution
- ✅ Single binary distribution
- ✅ No runtime dependencies
- 📦 Example: `workstation-validator-go`

### Rust
- ✅ Extremely fast
- ✅ Strong type safety
- ✅ WASM compilation support
- 📦 Example: `workstation-validator-rs`

---

## Compatibility Requirements

### Data Model Compatibility

The validator **MUST** understand the same JSON structure as the Kotlin application:

```
workstation.json
├── metadata (object)
│   ├── title (string)
│   ├── date (string)
│   ├── canvasSize (object)
│   │   ├── width (number)
│   │   └── height (number)
│   ├── version (string)
│   └── theme (object, optional)
│       └── isDark (boolean)
├── devices (array)
│   └── [device objects]
└── connections (array)
    └── [connection objects]
```

See `02-DATA_MODEL_SPECIFICATION.md` for complete schema.

### Enum Compatibility

The validator **MUST** recognize the same enum values:

- **DeviceType**: LAPTOP, MONITOR, DOCKING_STATION, USB_HUB, STORAGE, etc.
- **DeviceCategory**: CENTRAL_DEVICE, PERIPHERAL, HUB, etc.
- **PortType**: USB_C, USB_A_3_0, HDMI, DISPLAY_PORT, etc.
- **PortDirection**: INPUT, OUTPUT, BIDIRECTIONAL
- **DeviceSide**: TOP, BOTTOM, LEFT, RIGHT

See `03-ENUM_DEFINITIONS.md` for complete lists.

### Validation Rule Compatibility

The validator **MUST** apply the same rules documented in:
- `04-VALIDATION_RULES.md` - All validation rules
- `05-ERROR_CODES.md` - Standard error/warning codes

---

## Testing Requirements

### Test Coverage

The validator implementation should include tests for:

1. **Valid Files**
   - Minimal valid example
   - Complete realistic example
   - Edge cases (empty ports, single device, etc.)

2. **Invalid Files**
   - Missing required fields
   - Invalid types
   - Invalid enum values
   - Referential integrity violations
   - Business rule violations

3. **Warning Conditions**
   - Overlapping devices
   - Poor grid alignment
   - Tight clearances

### Test Data

Sample test files are provided in:
- `test-data/valid/` - Files that should pass
- `test-data/invalid/` - Files that should fail with specific errors
- `test-data/warnings/` - Files that should warn

---

## Versioning

### Validator Version

The validator should report its own version:
```json
{
  "validator": {
    "name": "workstation-validator-ts",
    "version": "1.0.0",
    "schemaVersion": "1.0"
  }
}
```

### Schema Version

The validator should check the `version` field in `workstation.json`:
- **"1.0"**: Current version (documented here)
- **Future versions**: May have different rules

---

## Performance Expectations

### Validation Speed

- Files < 100 devices: < 100ms
- Files < 1000 devices: < 1 second
- Files > 1000 devices: < 10 seconds

### Memory Usage

- Should handle files up to 10MB
- Streaming validation for very large files

---

## Error Handling

### JSON Parse Errors

```json
{
  "valid": false,
  "errors": [{
    "code": "ERR_INVALID_JSON",
    "message": "JSON syntax error at line 42: Unexpected token }",
    "severity": "ERROR"
  }]
}
```

### Schema Violations

```json
{
  "valid": false,
  "errors": [{
    "code": "ERR_MISSING_FIELD",
    "message": "Required field 'metadata.canvasSize' is missing",
    "location": {
      "path": "metadata"
    },
    "severity": "ERROR"
  }]
}
```

---

## Next Steps

To implement the validator:

1. **Read Data Model** → `02-DATA_MODEL_SPECIFICATION.md`
2. **Read Enum Definitions** → `03-ENUM_DEFINITIONS.md`
3. **Read Validation Rules** → `04-VALIDATION_RULES.md`
4. **Read Error Codes** → `05-ERROR_CODES.md`
5. **Review Examples** → `06-VALIDATION_EXAMPLES.md`
6. **Use Test Data** → `test-data/` directory

---

## Support

For questions or clarifications:
- Review the main application code in `src/commonMain/kotlin/dev/akexorcist/workstation/`
- Check validation logic in `WorkstationValidator.kt`
- Refer to intelligent routing spec in `docs/INTELLIGENT_ROUTING_SPECIFICATION.md`
