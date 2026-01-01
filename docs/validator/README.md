# Workstation JSON Validator Specifications

## Quick Start for AI Agents

This directory contains complete specifications for implementing a `workstation.json` validator that is compatible with the main Kotlin application.

---

## Documents

Read in this order:

1. **`01-VALIDATOR_OVERVIEW.md`** - Start here
   - Purpose and scope
   - Output format
   - Integration options
   - Language recommendations

2. **`02-DATA_MODEL_SPECIFICATION.md`** - JSON schema
   - Complete structure definition
   - Field types and requirements
   - Validation pseudo-code
   - Examples

3. **`03-ENUM_DEFINITIONS.md`** - Valid enum values
   - All enum types
   - Case-sensitive values
   - Usage examples

4. **`04-VALIDATION_RULES.md`** - Business logic
   - Structural validation
   - Referential integrity
   - Business rules (1-to-1 ports, etc.)
   - Routing compatibility

5. **`05-ERROR_CODES.md`** - Standard error codes
   - Error code definitions
   - Severity levels
   - Error messages

6. **`06-VALIDATION_EXAMPLES.md`** - Real examples
   - Valid files
   - Invalid files with expected errors
   - Edge cases

---

## Implementation Checklist

- [ ] Read all specification documents
- [ ] Implement JSON parsing
- [ ] Implement enum validation
- [ ] Implement structural validation
- [ ] Implement referential integrity checks
- [ ] Implement 1-to-1 port rule
- [ ] Implement routing compatibility checks
- [ ] Add error reporting with standard codes
- [ ] Create test suite with provided examples
- [ ] Verify output format matches specification

---

## Key Requirements

### MUST Implement

✅ JSON structure validation
✅ Required field checking  
✅ Enum value validation (case-sensitive)
✅ Device/Port reference validation
✅ 1-to-1 port usage rule
✅ No self-connections rule
✅ Unique ID validation
✅ Numeric range validation

### SHOULD Implement

⚠️ Grid alignment warnings
⚠️ Device overlap detection
⚠️ Clearance recommendations
⚠️ Port distribution suggestions

### MAY Implement

ℹ️ Performance optimizations
ℹ️ Auto-fix suggestions
ℹ️ Visual validation reports
ℹ️ JSON schema generation

---

## Output Format Example

```json
{
  "valid": false,
  "errors": [
    {
      "code": "ERR_DUPLICATE_PORT_USAGE",
      "message": "Port 'usb-c-1' on device 'laptop-main' is used by multiple connections",
      "location": {
        "device": "laptop-main",
        "port": "usb-c-1",
        "connections": ["conn-1", "conn-2"]
      },
      "severity": "ERROR"
    }
  ],
  "warnings": [],
  "info": [],
  "summary": {
    "totalDevices": 6,
    "totalConnections": 7,
    "totalPorts": 18,
    "errorCount": 1,
    "warningCount": 0,
    "infoCount": 0
  }
}
```

---

## Testing

Test files are provided in:
- `test-data/valid/` - Should pass validation
- `test-data/invalid/` - Should fail with specific errors  
- `test-data/warnings/` - Should pass with warnings

Reference implementation tests:
- See `src/commonMain/kotlin/dev/akexorcist/workstation/data/validation/WorkstationValidator.kt`

---

## Support

Questions or need clarification?
- Review the main application source code
- Check the intelligent routing specification: `docs/INTELLIGENT_ROUTING_SPECIFICATION.md`
- Refer to the complete validation rules document: `docs/WORKSTATION_JSON_VALIDATION_RULES.md`

---

## Version

- **Specification Version**: 1.0
- **Compatible with workstation.json version**: "1.0"
- **Last Updated**: 2025-12-31
