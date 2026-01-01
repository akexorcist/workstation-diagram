# UI Redesign Specification

## Overview

This document specifies the complete UI redesign for the Workstation Diagram application, transforming from a simple sidebar + top control panel layout to a more sophisticated design with header card, instruction legend, collapsible sections, and floating controls.

**Date**: December 31, 2025  
**Status**: Ready for Implementation  
**Design Reference**: See attached mockup image

---

## Design Philosophy

### Current Problems
1. **Generic appearance** - Looks like a basic developer tool
2. **No branding** - Missing personal identity
3. **Flat hierarchy** - No visual organization of information
4. **Control panel takes too much space** - Horizontal bar at top
5. **No legend** - Users don't know what colors mean
6. **Search always visible** - Not needed for small device lists

### New Design Goals
1. **Professional branding** - Personal workstation showcase
2. **Better information hierarchy** - Grouped sections with collapsible headers
3. **Space efficiency** - Floating controls, collapsible sections
4. **User guidance** - Instruction legend with color codes
5. **Modern aesthetics** - Darker theme, card-based design, smooth shadows

---

## Color Palette

### Background Colors
```kotlin
object AppColors {
    // Main backgrounds
    val CanvasBackground = Color(0xFF1A1A1A)      // Very dark gray (diagram area)
    val SidebarBackground = Color(0xFF2A2A2A)     // Dark gray (left sidebar)
    
    // Card backgrounds
    val CardBackground = Color(0xFF3A3A3A)        // Medium dark gray (header, controls)
    val CardBackgroundHover = Color(0xFF4A4A4A)   // Lighter on hover
    val CardBackgroundSelected = Color(0xFF505050) // Selected state
    
    // Device category colors (from current implementation)
    val CategoryComputer = Color(0xFF2196F3)       // Blue
    val CategoryHub = Color(0xFF4CAF50)            // Green
    val CategoryAccessory = Color(0xFFFF9800)      // Orange
    
    // Connection direction colors (new for legend)
    val OutputConnector = Color(0xFF9C27B0)        // Purple
    val InputConnector = Color(0xFFBA68C8)         // Light Purple
    
    // Text colors
    val TextPrimary = Color(0xFFFFFFFF)            // White
    val TextSecondary = Color(0xFFB0B0B0)          // Light gray
    val TextTertiary = Color(0xFF808080)           // Medium gray
    
    // Interactive elements
    val IconTint = Color(0xFFE0E0E0)               // Light gray for icons
    val IconTintHover = Color(0xFFFFFFFF)          // White on hover
    val AccentPurple = Color(0xFFB794F4)           // Purple for switches
}
```

---

## Typography

### Font Weights & Sizes
```kotlin
object AppTypography {
    // Header Card
    val HeaderTitle = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
    )
    val HeaderSubtitle = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        color = Color(0xFFB0B0B0)
    )
    val HeaderFooter = TextStyle(
        fontSize = 10.sp,
        fontWeight = FontWeight.Normal,
        color = Color(0xFF808080),
        lineHeight = 14.sp
    )
    
    // Section Headers
    val SectionHeader = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = Color.White
    )
    
    // Legend Items
    val LegendItem = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        color = Color(0xFFE0E0E0)
    )
    
    // Device List
    val DeviceName = TextStyle(
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = Color.White
    )
    val DeviceModel = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal,
        color = Color(0xFFB0B0B0)
    )
    
    // Control Panel
    val ControlLabel = TextStyle(
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = Color.White
    )
    val ZoomValue = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        color = Color(0xFFE0E0E0)
    )
}
```

---

## Spacing & Dimensions

### Standard Spacing
```kotlin
object AppDimensions {
    // Padding
    val PaddingXSmall = 4.dp
    val PaddingSmall = 8.dp
    val PaddingMedium = 12.dp
    val PaddingLarge = 16.dp
    val PaddingXLarge = 24.dp
    
    // Corner Radius
    val CornerRadiusSmall = 6.dp
    val CornerRadiusMedium = 8.dp
    val CornerRadiusLarge = 12.dp
    
    // Elevations
    val ElevationCard = 4.dp
    val ElevationFloating = 8.dp
    
    // Sizes
    val IconSize = 20.dp
    val IconButtonSize = 40.dp
    val ColorIndicatorSize = 12.dp
    val BorderAccentWidth = 4.dp
    
    // Sidebar
    val SidebarWidth = 280.dp
    val SidebarMaxDeviceListHeight = 500.dp
    
    // Control Panel
    val ControlPanelWidth = 240.dp
    val ControlPanelMinHeight = 140.dp
}
```

---

## Component Specifications

### 1. Header Card

**File**: `HeaderCard.kt`

**Purpose**: Display branding, navigation, and attribution

**Visual Structure**:
```
┌─────────────────────────────────────┐
│  Akexorcist's Workstation           │ ← Title (Bold, 18sp)
│  Dec 2025                            │ ← Date (Normal, 12sp)
│  [🏠] [GitHub]                       │ ← Icon buttons (20dp)
│                                      │
│  Powered by                          │ ← Footer (10sp, 2 lines)
│  Kotlin Multiplatform &              │
│  Compose Multiplatform               │
└─────────────────────────────────────┘
```

**Props**:
```kotlin
@Composable
fun HeaderCard(
    title: String,                    // "Akexorcist's Workstation"
    date: String,                     // "Dec 2025" (formatted from metadata)
    onHomeClick: () -> Unit,          // Navigate to akexorcist.dev
    onGithubClick: () -> Unit,        // Navigate to github.com/akexorcist
    modifier: Modifier = Modifier
)
```

**Styling**:
- Background: `Color(0xFF3A3A3A)`
- Padding: 16.dp all sides
- Corner radius: 12.dp
- Elevation: 4.dp shadow

**Layout Details**:
- Title at top, left-aligned
- Date below title, smaller, gray
- Icon buttons in horizontal row below date (8.dp spacing)
- Footer text at bottom, 2 lines, smallest size, gray

**Icons**:
- Home: Material Icons `Home` or custom home icon
- GitHub: Use GitHub logo (may need custom vector asset)

---

### 2. Collapsible Section

**File**: `CollapsibleSection.kt`

**Purpose**: Reusable collapsible container for Instruction and Device List

**Visual Structure**:
```
┌─────────────────────────────────────┐
│  ⓘ Section Title           [▲]     │ ← Header (clickable)
├─────────────────────────────────────┤
│  Content goes here...                │ ← Content (shown when expanded)
│  Multiple lines...                   │
└─────────────────────────────────────┘
```

**Props**:
```kotlin
@Composable
fun CollapsibleSection(
    title: String,                    // "Instruction" or "Device List"
    icon: ImageVector?,               // Optional leading icon
    isExpanded: Boolean,              // Expansion state
    onExpandChange: (Boolean) -> Unit, // Callback when toggled
    content: @Composable () -> Unit,  // Content to show when expanded
    modifier: Modifier = Modifier
)
```

**Header Styling**:
- Background: `Color(0xFF333333)`
- Padding: 12.dp horizontal, 10.dp vertical
- Clickable to toggle
- Icon on left (if provided)
- Title in middle (14.sp, Medium weight)
- Chevron on right (rotates 180° when expanded)

**Expand/Collapse Animation**:
- Use `AnimatedVisibility` for content
- Fade + expand vertically
- Duration: 200ms
- Easing: FastOutSlowIn

---

### 3. Instruction Legend

**File**: `InstructionLegend.kt`

**Purpose**: Show color legend for device categories and connection types

**Visual Structure**:
```
┌─────────────────────────────────────┐
│  ⓘ Instruction              [▲]    │ ← Collapsible header
├─────────────────────────────────────┤
│  ■ Computer                         │ ← Blue square (12×12dp)
│  ■ Hub                              │ ← Green square
│  ■ Accessory                        │ ← Orange square
│  ■ Output Connector                 │ ← Purple square
│  ■ Input Connector                  │ ← Light purple square
└─────────────────────────────────────┘
```

**Props**:
```kotlin
@Composable
fun InstructionLegend(
    isExpanded: Boolean,              // Controlled by parent
    onExpandChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
)
```

**Legend Items**:
```kotlin
data class LegendItem(
    val label: String,
    val color: Color
)

val legendItems = listOf(
    LegendItem("Computer", Color(0xFF2196F3)),
    LegendItem("Hub", Color(0xFF4CAF50)),
    LegendItem("Accessory", Color(0xFFFF9800)),
    LegendItem("Output Connector", Color(0xFF9C27B0)),
    LegendItem("Input Connector", Color(0xFFBA68C8))
)
```

**Item Styling**:
- Each row: 8.dp spacing between items
- Color square: 12×12.dp with 4.dp corner radius
- Label: 12.sp, 8.dp left margin from square
- Vertical spacing: 8.dp between rows
- Content padding: 12.dp all sides

---

### 4. Device List Sidebar

**File**: `DeviceListSidebar.kt` (major update)

**Purpose**: Show header, legend, and device list in organized layout

**Visual Structure**:
```
┌─────────────────────────────────────┐
│  [HeaderCard]                        │ ← Header with branding
├─────────────────────────────────────┤
│  [InstructionLegend]                 │ ← Collapsible legend
├─────────────────────────────────────┤
│  ☰ Device List (12)         [▲]    │ ← Collapsible device list
├─────────────────────────────────────┤
│  │ Office Laptop                    │ ← Device items
│  │ Macbook Pro                      │
│  ├─────────────────────────────────┤
│  │ Personal Laptop                  │
│  │ Macbook Pro                      │
│  └─────────────────────────────────┘
└─────────────────────────────────────┘
```

**Props**:
```kotlin
@Composable
fun DeviceListSidebar(
    uiState: WorkstationUiState,
    onDeviceClick: (String) -> Unit,
    onHomeClick: () -> Unit,
    onGithubClick: () -> Unit,
    isInstructionExpanded: Boolean,
    onInstructionExpandChange: (Boolean) -> Unit,
    isDeviceListExpanded: Boolean,
    onDeviceListExpandChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
)
```

**Layout**:
- Column with vertical scrolling
- Background: `Color(0xFF2A2A2A)`
- Width: 280.dp (fixed)
- Padding: 16.dp all sides
- Spacing between sections: 12.dp

**Changes from Current**:
- ✅ Add HeaderCard at top
- ✅ Add InstructionLegend below header
- ✅ Make device list collapsible
- ❌ Remove search field
- ✅ Update device items (simpler design)

---

### 5. Device List Item

**File**: `DeviceListSidebar.kt` (update existing `DeviceListItem`)

**Purpose**: Display individual device in list

**Visual Structure**:
```
┌─────────────────────────────────────┐
│▌ Office Laptop                      │ ← Left border accent (4dp)
│  Macbook Pro                         │ ← Model (gray, smaller)
└─────────────────────────────────────┘
```

**Props**:
```kotlin
@Composable
fun DeviceListItem(
    device: Device,
    isSelected: Boolean,
    isDarkTheme: Boolean,
    onClick: () -> Unit
)
```

**Styling**:
- Background: `Color(0xFF3A3A3A)` (normal), `Color(0xFF505050)` (selected)
- Corner radius: 8.dp
- Padding: 12.dp all sides
- Left border: 4.dp width, category color
- Spacing: 4.dp between name and model

**Changes from Current**:
- ❌ Remove colored square indicator (replaced with left border)
- ❌ Remove port count text
- ❌ Remove 3-dot menu button
- ✅ Add left colored border accent
- ✅ Simpler 2-line layout

**Border Colors by Category**:
```kotlin
val borderColor = when (device.category) {
    DeviceCategory.HUB -> Color(0xFF4CAF50)
    DeviceCategory.PERIPHERAL -> Color(0xFFFF9800)
    DeviceCategory.CENTRAL_DEVICE -> Color(0xFF2196F3)
}
```

---

### 6. Floating Control Panel

**File**: `FloatingControlPanel.kt`

**Purpose**: Zoom and theme controls in floating card at top-right

**Visual Structure**:
```
┌────────────────────────────┐
│ x1 Zoom                     │ ← Zoom label + value
│ [━━━━━━━━━━━━━━━━━━━━━━]   │ ← Slider
│                             │
│ Connection Animation  [●]   │ ← Label + Switch
│ Dark Theme           [●]   │ ← Label + Switch
└────────────────────────────┘
```

**Props**:
```kotlin
@Composable
fun FloatingControlPanel(
    zoom: Float,                      // Current zoom level
    onZoomChange: (Float) -> Unit,
    connectionAnimationEnabled: Boolean,
    onConnectionAnimationToggle: (Boolean) -> Unit,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    modifier: Modifier = Modifier
)
```

**Styling**:
- Background: `Color(0xFF3A3A3A)`
- Width: 240.dp (fixed)
- Padding: 16.dp all sides
- Corner radius: 12.dp
- Elevation: 8.dp shadow (floating)
- Positioned: 16.dp from top-right corner

**Layout**:
- Zoom label at top
- Slider below (full width)
- 16.dp spacing
- Connection Animation row (label + switch)
- 8.dp spacing
- Dark Theme row (label + switch)

**Switch Styling**:
- Thumb color: `Color(0xFFB794F4)` (purple) when checked
- Track color: `Color(0xFF505050)` unchecked, `Color(0xFF9575CD)` checked
- Size: Standard Material3 Switch

---

## Layout Changes

### Current Layout
```
┌─────────────────────────────────────────────────────┐
│  [Control Panel - Horizontal Bar]                    │ ← Top bar (60dp height)
├────────┬────────────────────────────────────────────┤
│        │                                             │
│ Device │          Diagram Canvas                     │
│  List  │                                             │
│        │                                             │
└────────┴────────────────────────────────────────────┘
```

### New Layout
```
┌─────────────────────────────────────────────────────┐
│                                    [Floating Panel]  │ ← Top-right (floating)
│        │                                             │
│ Header │          Diagram Canvas                     │
│ Legend │         (darker background)                 │
│ Device │                                             │
│  List  │                                             │
│        │                                             │
└────────┴────────────────────────────────────────────┘
```

**Key Changes**:
1. **Remove** horizontal control panel from top
2. **Add** floating control panel at top-right
3. **Update** sidebar with new sections
4. **Darken** canvas background

---

## State Management Updates

### WorkstationUiState.kt

**Add New Fields**:
```kotlin
data class WorkstationUiState(
    // ... existing fields ...
    
    // New fields for UI redesign
    val isInstructionExpanded: Boolean = true,
    val isDeviceListExpanded: Boolean = true,
    val connectionAnimationEnabled: Boolean = true
)
```

### WorkstationViewModel.kt

**Add New Methods**:
```kotlin
fun toggleInstructionExpanded() {
    _uiState.value = _uiState.value.copy(
        isInstructionExpanded = !_uiState.value.isInstructionExpanded
    )
}

fun toggleDeviceListExpanded() {
    _uiState.value = _uiState.value.copy(
        isDeviceListExpanded = !_uiState.value.isDeviceListExpanded
    )
}

fun toggleConnectionAnimation() {
    _uiState.value = _uiState.value.copy(
        connectionAnimationEnabled = !_uiState.value.connectionAnimationEnabled
    )
}
```

---

## Platform-Specific Features

### External URL Opening

**File**: `utils/PlatformUtils.kt`

**Purpose**: Open external URLs in default browser

**Common API**:
```kotlin
// commonMain
expect fun openUrl(url: String)
```

**JVM Implementation**:
```kotlin
// jvmMain
import java.awt.Desktop
import java.net.URI

actual fun openUrl(url: String) {
    if (Desktop.isDesktopSupported()) {
        Desktop.getDesktop().browse(URI(url))
    }
}
```

**JS Implementation**:
```kotlin
// jsMain
import kotlinx.browser.window

actual fun openUrl(url: String) {
    window.open(url, "_blank")
}
```

**Usage**:
```kotlin
IconButton(onClick = { openUrl("https://akexorcist.dev/") }) {
    Icon(Icons.Default.Home, contentDescription = "Home")
}
```

---

## Date Formatting

### DateFormatter.kt

**File**: `utils/DateFormatter.kt`

**Purpose**: Format ISO date to display format

**Function**:
```kotlin
fun formatDateToMonthYear(dateString: String): String {
    // Input: "2025-12-31"
    // Output: "Dec 2025"
    
    val parts = dateString.split("-")
    if (parts.size != 3) return dateString
    
    val year = parts[0]
    val monthNumber = parts[1]
    
    val monthName = when (monthNumber) {
        "01" -> "Jan"
        "02" -> "Feb"
        "03" -> "Mar"
        "04" -> "Apr"
        "05" -> "May"
        "06" -> "Jun"
        "07" -> "Jul"
        "08" -> "Aug"
        "09" -> "Sep"
        "10" -> "Oct"
        "11" -> "Nov"
        "12" -> "Dec"
        else -> return dateString
    }
    
    return "$monthName $year"
}
```

**Usage**:
```kotlin
val displayDate = formatDateToMonthYear(uiState.layout?.metadata?.date ?: "")
// "2025-12-31" → "Dec 2025"
```

---

## Implementation Checklist

### Phase 1: New Components ✓
- [ ] Create `HeaderCard.kt`
- [ ] Create `CollapsibleSection.kt`
- [ ] Create `InstructionLegend.kt`
- [ ] Create `FloatingControlPanel.kt`

### Phase 2: Update Existing ✓
- [ ] Update `DeviceListSidebar.kt`
- [ ] Update `DeviceListItem` composable
- [ ] Update `WorkstationUiState.kt`
- [ ] Update `WorkstationViewModel.kt`

### Phase 3: Utilities ✓
- [ ] Create `DateFormatter.kt`
- [ ] Create/Update `PlatformUtils.kt` for URL opening

### Phase 4: Layout ✓
- [ ] Update `WorkstationDiagramScreen.kt`
- [ ] Remove top control panel
- [ ] Add floating control panel
- [ ] Update canvas background color

### Phase 5: Testing ✓
- [ ] Test HeaderCard with URLs
- [ ] Test collapsible sections
- [ ] Test device list scrolling
- [ ] Test floating panel positioning
- [ ] Test dark theme toggle
- [ ] Test connection animation toggle (UI only)
- [ ] Test on different screen sizes

---

## Migration Notes

### Breaking Changes
- None - this is purely a UI redesign
- All existing functionality preserved
- Data models unchanged

### Backward Compatibility
- Old layout can be restored by reverting UI files
- No changes to ViewModel logic (except new toggles)
- No changes to data layer

### Rollback Plan
If issues occur:
1. Revert `WorkstationDiagramScreen.kt`
2. Revert `DeviceListSidebar.kt`
3. Delete new component files
4. Revert state additions

All changes can be isolated to UI layer.

---

## Future Enhancements

### Possible Additions
1. **Collapsible Floating Panel** - Save screen space
2. **Device Search** - Add back as collapsible section
3. **Filter by Category** - Quick filters in instruction legend
4. **Keyboard Shortcuts** - Show in instruction legend
5. **Theme Presets** - Multiple color schemes
6. **Export Options** - Screenshot, PDF in control panel
7. **Device Details** - Expand item to show ports inline
8. **Drag to Reorder** - Reorder devices in list

---

**Status**: Ready for implementation in Write mode
**Estimated Time**: 3.5-4 hours for complete implementation
**Risk Level**: Low (isolated UI changes only)
