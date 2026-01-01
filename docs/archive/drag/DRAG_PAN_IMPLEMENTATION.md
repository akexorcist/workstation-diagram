# Drag/Pan Implementation Guide

## Overview

This document explains the clean, architecture-compliant implementation of drag/pan functionality in the Workstation Diagram application.

## Architecture Compliance

The implementation follows the established architecture patterns documented in `STATE_MANAGEMENT.md` and `ARCHITECTURE.md`:

1. **ViewModel as Single Source of Truth**: `uiState.panOffset` is the authoritative pan position
2. **Direct Function Calls**: UI calls `onPanChange()` to update ViewModel
3. **StateFlow for State**: ViewModel emits state via StateFlow
4. **No Local State Duplication**: Canvas doesn't maintain its own copy of pan offset

## The Problem

During rapid drag gestures, sending individual drag deltas to the ViewModel can cause issues:
- Each `onPanChange(uiState.panOffset + delta)` reads from `uiState.panOffset`
- State updates are asynchronous
- Rapid events may read stale `uiState.panOffset` values
- Result: jumpy, incorrect pan behavior

## The Solution

### Key Insight
**Capture the pan offset at drag START, then accumulate deltas relative to that base**

This ensures:
- We never read from potentially-stale `uiState` during active drag
- Each drag session has a stable reference point
- Final position = drag start position + accumulated movement
- Reset button works correctly (next drag captures the new reset position)

### Implementation

```kotlin
@Composable
fun DiagramCanvas(
    uiState: WorkstationUiState,
    onPanChange: (Offset) -> Unit,
    // ... other parameters
) {
    // Track the pan offset at drag start (stable reference point)
    val dragStartPan = remember { mutableStateOf(uiState.panOffset) }
    val accumulatedDrag = remember { mutableStateOf(Offset.Zero) }

    Canvas(
        modifier = modifier
            .pointerInput(uiState.panOffset) { // Re-create gesture detector when panOffset changes externally
                detectDragGestures(
                    onDragStart = {
                        // Capture current pan as base for this drag session
                        dragStartPan.value = uiState.panOffset
                        accumulatedDrag.value = Offset.Zero
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        
                        // Accumulate this frame's delta
                        accumulatedDrag.value = Offset(
                            x = accumulatedDrag.value.x + dragAmount.x,
                            y = accumulatedDrag.value.y + dragAmount.y
                        )
                        
                        // Send absolute position to ViewModel
                        // (base position + all accumulated movement)
                        onPanChange(
                            Offset(
                                x = dragStartPan.value.x + accumulatedDrag.value.x,
                                y = dragStartPan.value.y + accumulatedDrag.value.y
                            )
                        )
                    },
                    onDragEnd = {
                        // Clear for next drag session
                        accumulatedDrag.value = Offset.Zero
                    },
                    onDragCancel = {
                        // Clear for next drag session
                        accumulatedDrag.value = Offset.Zero
                    }
                )
            }
    ) {
        // Render using uiState.panOffset (single source of truth)
        // ...
    }
}
```

## How It Works

### Scenario 1: Normal Drag

```
1. User starts drag at position (0, 0)
   - dragStartPan = (0, 0)
   - accumulatedDrag = (0, 0)

2. User drags 10px right
   - accumulatedDrag = (10, 0)
   - onPanChange(0 + 10, 0 + 0) = (10, 0)
   - ViewModel updates uiState.panOffset = (10, 0)

3. User drags another 5px right
   - accumulatedDrag = (15, 0)
   - onPanChange(0 + 15, 0 + 0) = (15, 0)
   - ViewModel updates uiState.panOffset = (15, 0)

4. User releases
   - accumulatedDrag resets to (0, 0)
   - Final position: (15, 0) ✓
```

### Scenario 2: Reset Button Mid-Session

```
1. Current position is (100, 50)
   - uiState.panOffset = (100, 50)

2. User clicks Reset
   - ViewModel sets uiState.panOffset = (150, 30)

3. User starts new drag
   - pointerInput key changes (uiState.panOffset changed)
   - Gesture detector is recreated
   - dragStartPan captures NEW position = (150, 30)
   - accumulatedDrag = (0, 0)

4. User drags 20px right
   - accumulatedDrag = (20, 0)
   - onPanChange(150 + 20, 30 + 0) = (170, 30)
   - Works correctly! ✓
```

## Why This Follows the Architecture

### ✅ Complies With

1. **Single Source of Truth** (STATE_MANAGEMENT.md, line 55-58)
   - ViewModel holds the authoritative `uiState.panOffset`
   - Canvas renders using `uiState.panOffset`, not local state

2. **Direct Function Calls** (ARCHITECTURE.md, line 505-524)
   - UI calls `onPanChange()` directly to update ViewModel
   - Just like `onValueChange` for Slider

3. **No Local State Duplication** (Best Practices)
   - `dragStartPan` and `accumulatedDrag` are **transient gesture state**, not duplicated app state
   - They exist only during active drag, like a Slider's thumb position

4. **Reactive Updates** (STATE_MANAGEMENT.md, line 32-35)
   - ViewModel updates StateFlow
   - UI automatically recomposes with new pan offset

### ❌ Does NOT Violate

1. **No Two-Way Binding**
   - We don't use `LaunchedEffect` to sync local state back to ViewModel
   - Gesture state is one-time-use, not synchronized

2. **No Stale Reads**
   - We capture pan offset once at drag start
   - We never read `uiState.panOffset` during active drag

## Key Differences from Previous Attempts

### ❌ Bad: Reading uiState during drag
```kotlin
detectDragGestures { change, dragAmount ->
    // BAD: Reads potentially stale uiState.panOffset
    onPanChange(
        Offset(
            x = uiState.panOffset.x + dragAmount.x,
            y = uiState.panOffset.y + dragAmount.y
        )
    )
}
```
**Problem**: `uiState.panOffset` may not have updated yet from previous frame

### ❌ Bad: Local state with LaunchedEffect sync
```kotlin
var localPan by remember { mutableStateOf(uiState.panOffset) }

LaunchedEffect(uiState.panOffset) {
    localPan = uiState.panOffset // Two-way sync anti-pattern
}

detectDragGestures { change, dragAmount ->
    localPan = localPan + dragAmount
}
```
**Problem**: Duplicates app state, creates sync issues

### ✅ Good: Capture at drag start, accumulate, send absolute
```kotlin
val dragStartPan = remember { mutableStateOf(uiState.panOffset) }
val accumulatedDrag = remember { mutableStateOf(Offset.Zero) }

detectDragGestures(
    onDragStart = {
        dragStartPan.value = uiState.panOffset  // Capture once
        accumulatedDrag.value = Offset.Zero
    },
    onDrag = { change, dragAmount ->
        accumulatedDrag.value += dragAmount  // Accumulate
        onPanChange(dragStartPan.value + accumulatedDrag.value)  // Send absolute
    }
)
```
**Why it works**: Stable reference point, no stale reads, clean state flow

## Testing Scenarios

### Test 1: Smooth Dragging
- ✅ Drag should follow mouse 1:1
- ✅ No jumping or lag
- ✅ Works at any zoom level

### Test 2: Reset During Drag
- ✅ Click reset button
- ✅ Start dragging immediately
- ✅ Should drag from reset position, not jump back

### Test 3: Multiple Drag Sessions
- ✅ Drag, release
- ✅ Drag again from same position
- ✅ Each session should be smooth and independent

### Test 4: Fast Dragging
- ✅ Drag very quickly
- ✅ Should accumulate correctly
- ✅ No lost frames or position errors

## Performance Considerations

1. **Minimal State Updates**
   - Only updates ViewModel on actual drag events
   - No updates when not dragging

2. **Efficient Recomposition**
   - Canvas recomposes only when uiState changes
   - Gesture state changes don't trigger recomposition

3. **No Memory Leaks**
   - Gesture state is scoped to composable
   - Cleared on drag end/cancel

## Related Documentation

- `STATE_MANAGEMENT.md` - Overall state management patterns
- `ARCHITECTURE.md` - Component communication patterns
- `PHASE_3_COMPLETION.md` - Interactive features implementation
