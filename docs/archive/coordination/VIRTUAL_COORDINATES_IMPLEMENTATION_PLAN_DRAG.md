# Virtual Coordinates Implementation Plan

## Executive Summary

This document provides a detailed, reviewed plan for implementing virtual coordinate support in the Workstation Diagram application. The implementation is designed to be **backward compatible** and **low risk**.

## Current State Analysis

### Data Model (Current)
```kotlin
// src/commonMain/kotlin/dev/akexorcist/workstation/data/model/Layout.kt
data class LayoutMetadata(
    val title: String,
    val date: String,
    val canvasSize: Size,  // Currently: absolute pixels (1920x1080)
    val theme: ThemeConfig? = null,
    val version: String = "1.0"
)

// src/commonMain/kotlin/dev/akexorcist/workstation/data/model/Device.kt
data class Position(val x: Float, val y: Float)  // Currently: absolute pixels
data class Size(val width: Float, val height: Float)  // Currently: absolute pixels
```

### Current Coordinate Transform
```kotlin
// All coordinate transformations follow this pattern:
screenX = device.position.x * zoom + panOffset.x
screenY = device.position.y * zoom + panOffset.y
```

### Files Using Coordinates

1. **DiagramCanvas.kt** (4 locations)
   - Line ~51: Hover detection
   - Line ~81: Click detection  
   - Line ~220: Device rendering
   - Line ~300: Port position calculation

2. **WorkstationViewModel.kt** (3 locations)
   - Line ~171: World to screen transform
   - Line ~178: Visibility checking
   - Line ~235: Port position calculation

3. **DataValidator.kt** (2 locations)
   - Position validation
   - Size validation

## Proposed Changes

### 1. Data Model Changes (Backward Compatible)

```kotlin
// Add new optional fields to LayoutMetadata
@Serializable
data class LayoutMetadata(
    val title: String,
    val date: String,
    val canvasSize: Size,  // Keep for backward compatibility
    val theme: ThemeConfig? = null,
    val version: String = "1.0",
    // NEW FIELDS (optional for backward compatibility)
    val coordinateSystem: String? = null,  // "absolute" or "virtual"
    val virtualCanvas: Size? = null  // Only required if coordinateSystem == "virtual"
)
```

**Why This Is Safe:**
- ✅ Existing JSON files work unchanged (null defaults)
- ✅ No breaking changes to data model
- ✅ Serialization remains compatible

### 2. Add Coordinate Transformer

Create a new utility class to handle coordinate transformation:

```kotlin
// src/commonMain/kotlin/dev/akexorcist/workstation/utils/CoordinateTransformer.kt
object CoordinateTransformer {
    
    fun isVirtual Coordinates(metadata: LayoutMetadata): Boolean {
        return metadata.coordinateSystem == "virtual" && 
               metadata.virtualCanvas != null
    }
    
    /**
     * Transforms device position from data space to screen space
     * Handles both absolute and virtual coordinates
     */
    fun transformPosition(
        dataPosition: Position,
        metadata: LayoutMetadata,
        actualCanvasSize: Size,
        zoom: Float,
        panOffset: Offset
    ): androidx.compose.ui.geometry.Offset {
        val worldPosition = if (isVirtualCoordinates(metadata)) {
            // Virtual to world space
            val virtualCanvas = metadata.virtualCanvas!!
            Position(
                x = (dataPosition.x / virtualCanvas.width) * actualCanvasSize.width,
                y = (dataPosition.y / virtualCanvas.height) * actualCanvasSize.height
            )
        } else {
            // Already in world space (absolute pixels)
            dataPosition
        }
        
        // World to screen space (same for both systems)
        return androidx.compose.ui.geometry.Offset(
            x = worldPosition.x * zoom + panOffset.x,
            y = worldPosition.y * zoom + panOffset.y
        )
    }
    
    /**
     * Transforms device size from data space to screen space
     */
    fun transformSize(
        dataSize: Size,
        metadata: LayoutMetadata,
        actualCanvasSize: Size,
        zoom: Float
    ): androidx.compose.ui.geometry.Size {
        val worldSize = if (isVirtualCoordinates(metadata)) {
            // Virtual to world space
            val virtualCanvas = metadata.virtualCanvas!!
            Size(
                width = (dataSize.width / virtualCanvas.width) * actualCanvasSize.width,
                height = (dataSize.height / virtualCanvas.height) * actualCanvasSize.height
            )
        } else {
            // Already in world space
            dataSize
        }
        
        // World to screen space
        return androidx.compose.ui.geometry.Size(
            width = worldSize.width * zoom,
            height = worldSize.height * zoom
        )
    }
}
```

**Why This Is Safe:**
- ✅ Encapsulates all transformation logic in one place
- ✅ Easy to test independently
- ✅ No changes to existing coordinate logic (falls through to absolute)
- ✅ Clear, documented API

### 3. Update DiagramCanvas (Minimal Changes)

**Current Code:**
```kotlin
val screenPos = Offset(
    x = device.position.x * uiState.zoom + uiState.panOffset.x,
    y = device.position.y * uiState.zoom + uiState.panOffset.y
)
```

**New Code:**
```kotlin
val screenPos = CoordinateTransformer.transformPosition(
    dataPosition = device.position,
    metadata = layout.metadata,
    actualCanvasSize = Size(size.width, size.height),
    zoom = uiState.zoom,
    panOffset = uiState.panOffset
)
```

**Changes Required:**
- Replace 4 coordinate calculations in DiagramCanvas.kt
- Replace 3 coordinate calculations in WorkstationViewModel.kt
- Total: ~7 lines changed

**Why This Is Safe:**
- ✅ Drop-in replacement for existing code
- ✅ Existing behavior unchanged when coordinateSystem is null
- ✅ No complex refactoring needed

### 4. Update ViewModel (Minimal Changes)

Same pattern as DiagramCanvas - replace direct calculations with transformer calls.

### 5. Validation Updates

```kotlin
// DataValidator.kt - Add validation for virtual coordinates
fun validateVirtualCoordinates(metadata: LayoutMetadata, devices: List<Device>): List<String> {
    val errors = mutableListOf<String>()
    
    if (metadata.coordinateSystem == "virtual") {
        if (metadata.virtualCanvas == null) {
            errors.add("virtualCanvas is required when coordinateSystem is 'virtual'")
        }
        
        // Validate positions are within virtual canvas bounds
        metadata.virtualCanvas?.let { virtualCanvas ->
            devices.forEach { device ->
                if (device.position.x < 0 || device.position.x > virtualCanvas.width) {
                    errors.add("Device ${device.id} x position ${device.position.x} outside virtual canvas bounds")
                }
                if (device.position.y < 0 || device.position.y > virtualCanvas.height) {
                    errors.add("Device ${device.id} y position ${device.position.y} outside virtual canvas bounds")
                }
            }
        }
    }
    
    return errors
}
```

**Why This Is Safe:**
- ✅ Only validates when virtual coordinates are used
- ✅ Catches configuration errors early
- ✅ Existing validation unchanged

## Implementation Steps

### Step 1: Add CoordinateTransformer (No Risk)
1. Create new file `CoordinateTransformer.kt`
2. Implement transformation functions
3. Write unit tests
4. **No existing code is modified**

### Step 2: Update Data Model (Backward Compatible)
1. Add optional fields to `LayoutMetadata`
2. Test serialization/deserialization
3. Verify existing JSON files still load
4. **No breaking changes**

### Step 3: Update DiagramCanvas (Low Risk)
1. Replace coordinate calculations one at a time
2. Test after each change
3. Verify absolute coordinates still work
4. **Fallback to existing behavior if anything breaks**

### Step 4: Update ViewModel (Low Risk)
1. Same process as DiagramCanvas
2. Test after each change
3. **Independent from UI changes**

### Step 5: Add Validation (Low Risk)
1. Add virtual coordinate validation
2. Test with both coordinate systems
3. **Doesn't affect existing validation**

### Step 6: Test & Verify
1. Test with existing absolute coordinate files
2. Test with new virtual coordinate files
3. Test window resizing
4. Test different screen sizes

## Risk Assessment

### Risks & Mitigations

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|---------|------------|
| Breaking existing files | Low | High | Backward compatible design with optional fields |
| Incorrect coordinate transform | Medium | Medium | Comprehensive unit tests, fallback to absolute |
| Performance impact | Low | Low | Simple math operations, no loops |
| Serialization issues | Low | Medium | Test with existing files first |
| Pan/zoom breaks | Medium | High | Test thoroughly, transformer is separate from pan/zoom |

### Testing Strategy

**Unit Tests:**
```kotlin
class CoordinateTransformerTest {
    @Test
    fun `absolute coordinates unchanged`() {
        val metadata = LayoutMetadata(
            coordinateSystem = null,  // or "absolute"
            virtualCanvas = null
        )
        val result = CoordinateTransformer.transformPosition(...)
        // Verify result matches current behavior
    }
    
    @Test
    fun `virtual coordinates scale correctly`() {
        val metadata = LayoutMetadata(
            coordinateSystem = "virtual",
            virtualCanvas = Size(10000f, 10000f)
        )
        // Verify 5000,5000 -> center of canvas
    }
}
```

**Integration Tests:**
- Load existing `workstation.json` (absolute)
- Load new `simple-workstation-virtual.json` (virtual)
- Verify both render correctly
- Verify window resize scales correctly

## Rollback Plan

If issues occur:
1. **Immediate**: Revert files in reverse order
2. **Step 3-5 Rollback**: Remove transformer calls, restore direct calculations
3. **Step 2 Rollback**: Remove optional fields (doesn't break existing code)
4. **Step 1 Rollback**: Delete CoordinateTransformer.kt
5. **Git**: All changes in separate commits for easy revert

## Success Criteria

- ✅ Existing JSON files load and render identically
- ✅ New virtual coordinate files load and render correctly
- ✅ Window resize scales virtual coordinates proportionally
- ✅ Pan and zoom work correctly with both systems
- ✅ All unit tests pass
- ✅ No performance degradation
- ✅ No breaking changes to API

## Code Review Checklist

Before implementation:
- [ ] Data model changes are backward compatible
- [ ] CoordinateTransformer has unit tests
- [ ] Fallback to absolute coordinates is correct
- [ ] All coordinate usage points identified
- [ ] Validation logic is correct
- [ ] Documentation is clear

After implementation:
- [ ] Existing files still work
- [ ] New files work correctly
- [ ] Pan/zoom still works
- [ ] Reset button centers correctly
- [ ] Window resize works
- [ ] All tests pass

## Timeline Estimate

- Step 1 (CoordinateTransformer): 30 minutes
- Step 2 (Data Model): 15 minutes
- Step 3 (DiagramCanvas): 30 minutes
- Step 4 (ViewModel): 20 minutes
- Step 5 (Validation): 20 minutes
- Step 6 (Testing): 30 minutes
- **Total: ~2.5 hours**

## Decision

**Recommendation: PROCEED with implementation**

**Rationale:**
- Design is backward compatible
- Changes are isolated and incremental
- Clear rollback plan exists
- Testing strategy is comprehensive
- Low risk of breaking existing functionality

**Next Action:**
Wait for approval before implementation.
