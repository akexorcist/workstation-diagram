# UI Redesign Implementation Guide

## Overview

This guide provides step-by-step instructions with complete code examples for implementing the UI redesign. Follow the phases in order for a smooth migration.

---

## Phase 1: Create New Components

### Step 1.1: Create HeaderCard.kt

**Location**: `viewer/src/commonMain/kotlin/dev/akexorcist/workstation/ui/components/HeaderCard.kt`

**Complete Code**:

```kotlin
package dev.akexorcist.workstation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HeaderCard(
    title: String,
    date: String,
    onHomeClick: () -> Unit,
    onGithubClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF3A3A3A)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Title
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            // Date
            Text(
                text = date,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFFB0B0B0)
            )
            
            // Icon Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onHomeClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = "Home",
                        tint = Color(0xFFE0E0E0),
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // GitHub icon (using a simple placeholder - replace with actual GitHub icon)
                IconButton(
                    onClick = onGithubClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    // TODO: Replace with actual GitHub icon
                    Icon(
                        Icons.Default.Home, // Placeholder
                        contentDescription = "GitHub",
                        tint = Color(0xFFE0E0E0),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Footer
            Text(
                text = "Powered by\nKotlin Multiplatform &\nCompose Multiplatform",
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF808080),
                lineHeight = 14.sp
            )
        }
    }
}
```

---

### Step 1.2: Create CollapsibleSection.kt

**Location**: `viewer/src/commonMain/kotlin/dev/akexorcist/workstation/ui/components/CollapsibleSection.kt`

**Complete Code**:

```kotlin
package dev.akexorcist.workstation.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CollapsibleSection(
    title: String,
    icon: ImageVector? = null,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color(0xFF333333),
                    RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                )
                .clickable { onExpandChange(!isExpanded) }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFFE0E0E0),
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
            
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp 
                             else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = Color(0xFFE0E0E0),
                modifier = Modifier.size(20.dp)
            )
        }
        
        // Content with animation
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(animationSpec = tween(200)) + 
                    expandVertically(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(200)) + 
                   shrinkVertically(animationSpec = tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color(0xFF2E2E2E),
                        RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                    )
                    .padding(12.dp)
            ) {
                content()
            }
        }
    }
}
```

---

### Step 1.3: Create InstructionLegend.kt

**Location**: `viewer/src/commonMain/kotlin/dev/akexorcist/workstation/ui/components/InstructionLegend.kt`

**Complete Code**:

```kotlin
package dev.akexorcist.workstation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class LegendItem(
    val label: String,
    val color: Color
)

@Composable
fun InstructionLegend(
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val legendItems = listOf(
        LegendItem("Computer", Color(0xFF2196F3)),
        LegendItem("Hub", Color(0xFF4CAF50)),
        LegendItem("Accessory", Color(0xFFFF9800)),
        LegendItem("Output Connector", Color(0xFF9C27B0)),
        LegendItem("Input Connector", Color(0xFFBA68C8))
    )
    
    CollapsibleSection(
        title = "Instruction",
        icon = Icons.Default.Info,
        isExpanded = isExpanded,
        onExpandChange = onExpandChange,
        modifier = modifier,
        content = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                legendItems.forEach { item ->
                    LegendItemRow(
                        label = item.label,
                        color = item.color
                    )
                }
            }
        }
    )
}

@Composable
private fun LegendItemRow(
    label: String,
    color: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Color indicator
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(4.dp))
        )
        
        // Label
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFFE0E0E0)
        )
    }
}
```

---

### Step 1.4: Create FloatingControlPanel.kt

**Location**: `viewer/src/commonMain/kotlin/dev/akexorcist/workstation/ui/components/FloatingControlPanel.kt`

**Complete Code**:

```kotlin
package dev.akexorcist.workstation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FloatingControlPanel(
    zoom: Float,
    onZoomChange: (Float) -> Unit,
    connectionAnimationEnabled: Boolean,
    onConnectionAnimationToggle: (Boolean) -> Unit,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(240.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF3A3A3A)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Zoom Control
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "x${String.format("%.1f", zoom)} Zoom",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                
                Slider(
                    value = zoom,
                    onValueChange = onZoomChange,
                    valueRange = 0.1f..5.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFB794F4),
                        activeTrackColor = Color(0xFF9575CD),
                        inactiveTrackColor = Color(0xFF505050)
                    )
                )
            }
            
            Divider(color = Color(0xFF505050), thickness = 1.dp)
            
            // Connection Animation Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Connection Animation",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFFE0E0E0)
                )
                
                Switch(
                    checked = connectionAnimationEnabled,
                    onCheckedChange = onConnectionAnimationToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFB794F4),
                        checkedTrackColor = Color(0xFF9575CD),
                        uncheckedThumbColor = Color(0xFF808080),
                        uncheckedTrackColor = Color(0xFF505050)
                    )
                )
            }
            
            // Dark Theme Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dark Theme",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFFE0E0E0)
                )
                
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { onThemeToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFB794F4),
                        checkedTrackColor = Color(0xFF9575CD),
                        uncheckedThumbColor = Color(0xFF808080),
                        uncheckedTrackColor = Color(0xFF505050)
                    )
                )
            }
        }
    }
}
```

---

## Phase 2: Create Utility Functions

### Step 2.1: Create DateFormatter.kt

**Location**: `viewer/src/commonMain/kotlin/dev/akexorcist/workstation/utils/DateFormatter.kt`

**Complete Code**:

```kotlin
package dev.akexorcist.workstation.utils

fun formatDateToMonthYear(dateString: String): String {
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

---

### Step 2.2: Create/Update PlatformUtils.kt

**Location**: `viewer/src/commonMain/kotlin/dev/akexorcist/workstation/utils/PlatformUtils.kt`

**Common (commonMain)**:

```kotlin
package dev.akexorcist.workstation.utils

expect fun openUrl(url: String)
```

**JVM (jvmMain)**:

**Location**: `viewer/src/jvmMain/kotlin/dev/akexorcist/workstation/utils/PlatformUtils.kt`

```kotlin
package dev.akexorcist.workstation.utils

import java.awt.Desktop
import java.net.URI

actual fun openUrl(url: String) {
    try {
        if (Desktop.isDesktopSupported()) {
            val desktop = Desktop.getDesktop()
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(URI(url))
            }
        }
    } catch (e: Exception) {
        println("Error opening URL: $url - ${e.message}")
    }
}
```

**JS (jsMain)**:

**Location**: `viewer/src/jsMain/kotlin/dev/akexorcist/workstation/utils/PlatformUtils.kt`

```kotlin
package dev.akexorcist.workstation.utils

import kotlinx.browser.window

actual fun openUrl(url: String) {
    window.open(url, "_blank")
}
```

---

## Phase 3: Update State Management

### Step 3.1: Update WorkstationUiState.kt

**Add these fields**:

```kotlin
data class WorkstationUiState(
    // ... existing fields ...
    
    // Add these new fields
    val isInstructionExpanded: Boolean = true,
    val isDeviceListExpanded: Boolean = true,
    val connectionAnimationEnabled: Boolean = true
)
```

---

### Step 3.2: Update WorkstationViewModel.kt

**Add these methods**:

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

## Phase 4: Update DeviceListSidebar.kt

This is a major update. Here's the complete new version:

**Location**: `viewer/src/commonMain/kotlin/dev/akexorcist/workstation/ui/sidebar/DeviceListSidebar.kt`

```kotlin
package dev.akexorcist.workstation.ui.sidebar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.akexorcist.workstation.data.model.DeviceCategory
import dev.akexorcist.workstation.presentation.WorkstationUiState
import dev.akexorcist.workstation.ui.components.CollapsibleSection
import dev.akexorcist.workstation.ui.components.HeaderCard
import dev.akexorcist.workstation.ui.components.InstructionLegend
import dev.akexorcist.workstation.utils.formatDateToMonthYear
import dev.akexorcist.workstation.utils.openUrl

@Composable
fun DeviceListSidebar(
    uiState: WorkstationUiState,
    onDeviceClick: (String) -> Unit,
    isInstructionExpanded: Boolean,
    onInstructionExpandChange: (Boolean) -> Unit,
    isDeviceListExpanded: Boolean,
    onDeviceListExpandChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(Color(0xFF2A2A2A))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header Card
        val displayDate = uiState.layout?.metadata?.date?.let { formatDateToMonthYear(it) } ?: ""
        HeaderCard(
            title = "Akexorcist's Workstation",
            date = displayDate,
            onHomeClick = { openUrl("https://akexorcist.dev/") },
            onGithubClick = { openUrl("https://github.com/akexorcist") }
        )
        
        // Instruction Legend
        InstructionLegend(
            isExpanded = isInstructionExpanded,
            onExpandChange = onInstructionExpandChange
        )
        
        // Device List
        uiState.layout?.let { layout ->
            CollapsibleSection(
                title = "Device List (${layout.devices.size})",
                icon = Icons.Default.List,
                isExpanded = isDeviceListExpanded,
                onExpandChange = onDeviceListExpandChange,
                content = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        layout.devices.forEach { device ->
                            DeviceListItem(
                                device = device,
                                isSelected = device.id == uiState.selectedDeviceId,
                                onClick = { onDeviceClick(device.id) }
                            )
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun DeviceListItem(
    device: dev.akexorcist.workstation.data.model.Device,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        Color(0xFF505050)
    } else {
        Color(0xFF3A3A3A)
    }
    
    val borderColor = when (device.category) {
        DeviceCategory.HUB -> Color(0xFF4CAF50)
        DeviceCategory.PERIPHERAL -> Color(0xFFFF9800)
        DeviceCategory.CENTRAL_DEVICE -> Color(0xFF2196F3)
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 4.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = device.model,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFFB0B0B0)
                )
            }
        }
    }
}
```

---

## Phase 5: Update WorkstationDiagramScreen.kt

**Key Changes**:
1. Remove top control panel
2. Add floating control panel
3. Update sidebar props
4. Darken canvas background

**Updated Layout Section**:

```kotlin
// Inside WorkstationDiagramScreen composable:

Box(
    modifier = Modifier
        .fillMaxSize()
        .focusRequester(focusRequester)
        .focusable()
        .onKeyEvent { /* ... existing key events ... */ }
        .onGloballyPositioned { /* ... existing size tracking ... */ }
) {
    // Layer 1: Canvas (full screen, behind everything)
    when {
        uiState.isLoading -> {
            LoadingState(
                message = "Loading workstation data...",
                isDarkTheme = uiState.isDarkTheme
            )
        }
        uiState.errorMessage != null -> {
            ErrorState(
                message = uiState.errorMessage ?: "Unknown error",
                onRetry = {
                    coroutineScope.launch {
                        viewModel.loadLayout()
                    }
                },
                isDarkTheme = uiState.isDarkTheme
            )
        }
        else -> {
            DiagramCanvas(
                uiState = uiState,
                onDeviceClick = viewModel::handleDeviceClick,
                onConnectionClick = viewModel::handleConnectionClick,
                onPanChange = viewModel::handlePanChange,
                onHoverDevice = { deviceId, isHovered -> viewModel.handleDeviceHover(deviceId, isHovered) },
                onHoverConnection = { connectionId, isHovered -> viewModel.handleConnectionHover(connectionId, isHovered) },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
    
    // Layer 2: UI Overlay (sidebar only)
    Row(modifier = Modifier.fillMaxSize()) {
        DeviceListSidebar(
            uiState = uiState,
            onDeviceClick = viewModel::handleDeviceClick,
            isInstructionExpanded = uiState.isInstructionExpanded,
            onInstructionExpandChange = { viewModel.toggleInstructionExpanded() },
            isDeviceListExpanded = uiState.isDeviceListExpanded,
            onDeviceListExpandChange = { viewModel.toggleDeviceListExpanded() },
            modifier = Modifier.width(280.dp)
        )
        
        Spacer(modifier = Modifier.weight(1f))
    }
    
    // Layer 3: Floating Control Panel (top-right)
    Box(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(16.dp)
    ) {
        FloatingControlPanel(
            zoom = uiState.zoom,
            onZoomChange = { newZoom ->
                val viewportCenterX = (canvasSize.width - 300f) / 2f + 300f
                val viewportCenterY = (canvasSize.height - 60f) / 2f + 60f
                val centerPoint = dev.akexorcist.workstation.data.model.Offset(viewportCenterX, viewportCenterY)
                val layoutCanvasSize = uiState.layout?.metadata?.canvasSize 
                    ?: dev.akexorcist.workstation.data.model.Size(canvasSize.width, canvasSize.height)
                viewModel.handleZoomChangeAtPoint(newZoom, centerPoint, layoutCanvasSize)
            },
            connectionAnimationEnabled = uiState.connectionAnimationEnabled,
            onConnectionAnimationToggle = { viewModel.toggleConnectionAnimation() },
            isDarkTheme = uiState.isDarkTheme,
            onThemeToggle = viewModel::toggleTheme
        )
    }
    
    // Device and Connection detail panels (existing code remains)
    // ...
}
```

---

## Phase 6: Update Canvas Background

**In DiagramCanvas.kt**, update the background color:

```kotlin
// Line ~68
Box(
    modifier = modifier
        .fillMaxSize()
        .background(if (uiState.isDarkTheme) Color(0xFF1A1A1A) else Color(0xFFE0E0E0))
        // ... rest of modifiers
)
```

---

## Testing Checklist

### Component Testing
- [ ] HeaderCard displays correctly
- [ ] Home icon opens akexorcist.dev
- [ ] GitHub icon opens github.com/akexorcist
- [ ] Date formats correctly (Dec 2025)
- [ ] Instruction legend expands/collapses
- [ ] Device list expands/collapses
- [ ] Device items show correct colors
- [ ] Floating panel appears at top-right
- [ ] Zoom slider works
- [ ] Connection animation toggle changes state
- [ ] Dark theme toggle works

### Layout Testing
- [ ] Sidebar width is correct (280dp)
- [ ] Floating panel doesn't overlap devices
- [ ] Canvas background is darker
- [ ] Scrolling works in sidebar
- [ ] Device list scrolls independently

### Interaction Testing
- [ ] Clicking device selects it
- [ ] Selected device highlights
- [ ] Collapsing sections saves space
- [ ] URL opening works on target platform
- [ ] All keyboard shortcuts still work

---

## Troubleshooting

### Issue: URLs don't open
**Solution**: Check platform-specific implementation of `openUrl()`. Ensure Desktop API is available on JVM.

### Issue: GitHub icon not showing
**Solution**: Add GitHub vector asset or use a placeholder icon temporarily.

### Issue: Date not formatting
**Solution**: Verify metadata date format in JSON is "YYYY-MM-DD".

### Issue: Floating panel overlaps canvas
**Solution**: Increase padding or adjust positioning in `Box(Alignment.TopEnd)`.

### Issue: Sidebar sections don't collapse
**Solution**: Verify state is being passed correctly from ViewModel to UI.

---

## Next Steps

After implementation:
1. Test on all target platforms (JVM Desktop, JS Web)
2. Verify performance with large device lists
3. Test on different screen sizes
4. Add GitHub icon asset
5. Consider adding connection animation logic (currently UI only)

---

**Ready to implement? Switch to Write mode and follow this guide step by step!**
