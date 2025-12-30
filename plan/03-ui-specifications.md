# UI Specifications & Rendering

## UI Layer - Compose Components

### Main Components Structure

```
compose/
├── WorkstationDiagramScreen.kt      # Main screen
├── components/
│   ├── DiagramCanvas.kt             # Main canvas container
│   ├── DeviceComponent.kt           # Individual device renderer
│   ├── ConnectionLine.kt            # Connection path renderer
│   ├── PortIndicator.kt            # Port visualization
│   ├── DeviceLabel.kt               # Device name/model display
│   └── ConnectionAnimation.kt       # Connection animation
├── sidebar/
│   ├── DeviceListSidebar.kt         # Left sidebar device list
│   └── DeviceListItem.kt            # Individual device list item
├── controls/
│   ├── ZoomControl.kt               # Zoom slider
│   ├── ThemeToggle.kt               # Dark/light theme toggle
│   ├── AnimationToggle.kt           # Connection animation toggle
│   └── ExportButton.kt              # Export functionality
├── details/
│   ├── DeviceDetailsPanel.kt        # Device specification panel
│   └── ConnectionDetailsPanel.kt    # Connection information panel
├── accessibility/
│   └── AccessibilitySupport.kt      # Accessibility utilities
└── components/
    └── Tooltip.kt                    # Tooltip component
```

## Rendering Pipeline

**File:** `compose/components/DiagramCanvas.kt`

### Rendering Order

1. **Canvas Setup:** Set up Compose Canvas with zoom/pan transformations
2. **Connection Rendering:** Draw connections first (background layer)
3. **Device Rendering:** Draw devices on top
4. **Port Rendering:** Draw port indicators
5. **Labels:** Draw device names/labels
6. **Interaction Layer:** Handle clicks, hovers, drags

### Key Techniques

- Use `Modifier.graphicsLayer` for transformations
- Use `Canvas` composable for custom drawing
- Use `Modifier.pointerInput` for interactions
- Implement smooth animations with `animateFloatAsState`

### Viewport Transformations

```kotlin
Modifier.graphicsLayer {
    scaleX = zoom
    scaleY = zoom
    translationX = panOffset.x
    translationY = panOffset.y
}
```

## Connection Rendering

**File:** `compose/components/ConnectionLine.kt`

### Features

- Render connections as Bezier curves or orthogonal paths
- Support animation (flowing effect along connection)
- Color coding by connection type/category
- Highlight on hover
- Show connection type label

### Connection Direction Visualization

1. **Arrow Heads:**
   - Arrow heads at target end (or both ends for bidirectional)
   - Size based on zoom level
   - Color matches connection line

2. **Line Styles:**
   - Solid lines for standard connections
   - Dashed lines for certain connection types
   - Different line styles for direction

3. **Color Gradient:**
   - Color gradient from source to target
   - Animated flow direction
   - Category-based base colors

### Multiple Connections Between Same Devices

**File:** `domain/layout/ConnectionBundler.kt`

- **Connection Bundling:** Group parallel connections
- **Offset Strategy:** Stagger connections with small offsets
- **Layer Assignment:** Use different z-layers
- **Visual Grouping:** Show as bundled cable with count indicator

### Connection Label Positioning

**File:** `domain/layout/LabelPositionCalculator.kt`

- Position labels at midpoint of connection path
- Avoid overlapping with devices or other connections
- Use background/outline for label readability
- Rotate label to follow connection angle
- Show on hover for less cluttered view

### Connection Line Styling

- Line thickness based on connection category/importance
- Dashed lines for certain connection types
- Opacity for less important connections
- Highlight on hover/selection

**Connection Category Colors:**
- DATA: Blue
- VIDEO: Purple/Pink
- AUDIO: Green
- POWER: Yellow/Orange
- NETWORK: Teal

## Device Rendering

**File:** `compose/components/DeviceComponent.kt`

### Visual Design

1. **Device Shapes:**
   - Rounded rectangles (primary shape)
   - Consider shape variations by type (monitors = wider, hubs = square)
   - Icon support (optional, for future)

2. **Color Coding:**
   - Category-based colors:
     - HUB: Green
     - PERIPHERAL: Orange/Yellow
     - CENTRAL_DEVICE: Blue
   - Type-based color variations
   - Support theme-aware colors

3. **Visual Hierarchy:**
   - Border thickness for importance
   - Shadow/elevation for depth
   - Highlight selected/hovered devices

**File:** `presentation/DeviceStyle.kt`

- Define device styling rules
- Map device types/categories to visual styles
- Support theme variations

### Device States

- **Normal:** Default appearance
- **Hovered:** Slight elevation, border highlight
- **Selected:** Strong border, background highlight
- **Connected:** Show connection indicators

## Port Visual Representation

**File:** `compose/components/PortIndicator.kt`

### Port Indicators

1. **Visual Style:**
   - Small circles/squares at port positions
   - Color coding by port type
   - Size based on zoom level
   - Show port type icon/symbol (optional)

2. **Port States:**
   - Connected (highlighted)
   - Available (dimmed)
   - Hovered (enlarged)
   - Selected (outlined)

3. **Port Labels:**
   - Show port name on hover
   - Optional: Always show for important ports
   - Position labels to avoid overlap

**File:** `domain/util/PortRenderer.kt`

- Calculate port positions in screen coordinates
- Handle port grouping/clustering
- Manage port label positioning

## Connection Animation

**File:** `compose/components/ConnectionAnimation.kt`

### Animation Types

1. **Flow Animation:**
   - Animated dots/particles moving along path
   - Speed based on connection type/category
   - Direction based on port directions
   - Pause at connection points

2. **Pulse Animation:**
   - Subtle pulse effect on active connections
   - Highlight data flow direction
   - Configurable animation speed

### Implementation

- Use `LaunchedEffect` with `infiniteRepeatable`
- Calculate positions along Bezier curves using `PathMeasure`
- Use `drawPath` with animated offset
- Support pause/resume animation
- Performance: Only animate visible connections

**File:** `domain/util/PathMeasure.kt`

- Utility to calculate points along Bezier paths
- Support for distance-based positioning
- Handle path length calculations

## Presentation Layer

### State Management

**File:** `presentation/WorkstationViewModel.kt`

```kotlin
class WorkstationViewModel {
    val uiState: StateFlow<WorkstationUiState>
    val diagramState: StateFlow<DiagramState>
    
    fun handleZoomChange(zoom: Float)
    fun handlePanChange(offset: Offset)
    fun handleDeviceClick(deviceId: String)
    fun handleConnectionClick(connectionId: String)
    fun toggleConnectionAnimation(enabled: Boolean)
    fun toggleTheme()
    fun searchDevices(query: String)
    fun selectDevice(deviceId: String)
    fun deselectDevice()
}
```

**File:** `presentation/WorkstationUiState.kt`

```kotlin
data class WorkstationUiState(
    val layout: WorkstationLayout,
    val selectedDeviceId: String?,
    val selectedConnectionId: String?,
    val zoom: Float,
    val panOffset: Offset,
    val showConnectionAnimation: Boolean,
    val isDarkTheme: Boolean,
    val connectionPaths: Map<String, ConnectionPath>, // pre-calculated paths
    val searchQuery: String = "",
    val filteredDeviceIds: Set<String> = emptySet()
)

data class DiagramState(
    val viewportBounds: Rect,
    val scale: Float,
    val deviceRenderData: List<DeviceRenderData>,
    val connectionRenderData: List<ConnectionRenderData>
)

data class DeviceRenderData(
    val device: Device,
    val screenPosition: Offset,
    val screenSize: Size,
    val isVisible: Boolean,
    val isSelected: Boolean,
    val isHovered: Boolean
)

data class ConnectionRenderData(
    val connection: Connection,
    val path: Path,
    val isVisible: Boolean,
    val isSelected: Boolean,
    val isHovered: Boolean
)
```

### Connection Path Calculation

**File:** `presentation/ConnectionPathCalculator.kt`

- Transforms domain layer connection data into renderable paths
- Applies zoom/pan transformations
- Handles animation state
- Caches calculated paths

## Sidebar Components

**File:** `compose/sidebar/DeviceListSidebar.kt`

### Features

1. **Device List:**
   - Display all devices in scrollable list
   - Show device name and model
   - Indicate device category/type
   - Highlight selected device

2. **Device Search/Filter:**
   - Search bar to filter devices by name/model
   - Filter by device type/category
   - Highlight matching devices in diagram
   - Scroll to device on selection

3. **Device Selection:**
   - Click device in list to highlight in diagram
   - Show connected devices when selecting
   - Highlight connection paths of selected device
   - Smooth scroll/zoom to selected device

**File:** `compose/sidebar/DeviceListItem.kt`

- Individual device list item component
- Show device icon/color
- Display device name and model
- Show connection count
- Handle click interactions

## Control Components

**File:** `compose/controls/ZoomControl.kt`

- Zoom slider (0.1x to 5.0x)
- Zoom in/out buttons
- Reset zoom button
- Display current zoom level

**File:** `compose/controls/ThemeToggle.kt`

- Toggle between dark/light theme
- Support system theme detection
- Persist theme preference

**File:** `compose/controls/AnimationToggle.kt`

- Toggle connection animation on/off
- Persist animation preference

**File:** `compose/controls/ExportButton.kt`

- Export button in controls panel
- Format selection dialog (PNG, SVG, PDF)
- Progress indicator for large exports

## Detail Panels

**File:** `compose/details/DeviceDetailsPanel.kt`

- Display device specifications
- Show device ports and connections
- Display technical specs
- Show connected devices

**File:** `compose/details/ConnectionDetailsPanel.kt`

- Display connection information
- Show connection type and specifications
- Display cable information
- Show source and target devices

## Tooltips

**File:** `compose/components/Tooltip.kt`

- Show device/connection details on hover
- Position tooltips to avoid viewport edges
- Support rich content (specifications, connection info)
- Dismiss on mouse leave or after timeout

## UI States

**File:** `compose/WorkstationDiagramScreen.kt`

### Loading States

- Show loading indicator while data loads
- Progressive rendering (devices first, then connections)
- Skeleton screens during initialization

### Error States

- Display error message for load failures
- Retry button
- Fallback empty state

### Empty States

- Message when no devices/connections
- Help text for first-time users

## Accessibility

**File:** `compose/accessibility/AccessibilitySupport.kt`

### Features

1. **Screen Reader Support:**
   - Semantic descriptions for devices
   - Announce connection information
   - Navigate devices via keyboard
   - Read device specifications

2. **Keyboard Navigation:**
   - Tab through devices in logical order
   - Arrow keys to navigate diagram
   - Enter/Space to select device
   - Escape to deselect

3. **High Contrast Mode:**
   - Support system high contrast settings
   - Ensure sufficient color contrast ratios
   - Alternative visual indicators (patterns, shapes)

4. **Focus Indicators:**
   - Clear focus rings on interactive elements
   - Focus management for keyboard users

### Implementation

- Use `Modifier.semantics` for screen reader descriptions
- Implement `Modifier.onKeyEvent` for keyboard navigation
- Test with screen readers (VoiceOver, NVDA, JAWS)

## Keyboard Shortcuts

**File:** `presentation/KeyboardShortcutHandler.kt`

### Shortcuts

- `+` / `-`: Zoom in/out
- `0`: Reset zoom
- `F`: Fit to viewport
- `Arrow Keys`: Pan diagram
- `Space + Drag`: Pan (alternative)
- `Ctrl/Cmd + F`: Focus search
- `Esc`: Deselect/close panels
- `Tab`: Navigate devices
- `Enter`: Select device

### Implementation

- Use `Modifier.onKeyEvent` in Compose
- Handle platform-specific modifiers (Ctrl vs Cmd)
- Show shortcut hints in UI (tooltips, help panel)

## Theme Support

**File:** `presentation/Theme.kt`

### Color Schemes

- Define color schemes for dark/light themes
- Device category colors (hubs, peripherals)
- Connection type colors
- Support system theme detection

### Theme Colors

**Dark Theme:**
- Background: Dark gray/black
- Device HUB: Green (#4CAF50)
- Device PERIPHERAL: Orange (#FF9800)
- Device CENTRAL: Blue (#2196F3)
- Connection DATA: Blue (#64B5F6)
- Connection VIDEO: Purple (#BA68C8)
- Connection AUDIO: Green (#81C784)
- Connection POWER: Yellow (#FFD54F)
- Connection NETWORK: Teal (#4DB6AC)

**Light Theme:**
- Background: Light gray/white
- Adjusted colors for light background
- Higher contrast ratios

## Responsive Layout

**File:** `compose/WorkstationDiagramScreen.kt`

### Responsive Strategies

1. **Sidebar:**
   - Collapsible on small screens
   - Overlay mode on mobile
   - Adjustable width

2. **Controls:**
   - Compact mode for small screens
   - Group related controls
   - Hide less important controls

3. **Diagram:**
   - Maintain aspect ratio
   - Scale to fit available space
   - Adjust device/connection sizes for readability

### Implementation

- Use `WindowSizeClass` or custom breakpoints
- Conditional rendering based on screen size
- Adaptive layouts with `Row`/`Column` arrangements

## Performance Optimization

### Rendering Optimizations

1. **Viewport Culling:**
   - Only render visible elements
   - Use spatial index for fast lookup
   - Render devices in viewport + margin

2. **Caching:**
   - Cache rendered paths
   - Cache calculated positions
   - Use `remember` for expensive computations

3. **Lazy Rendering:**
   - Use `LaunchedEffect` for expensive calculations
   - Progressive rendering
   - Level-of-detail (LOD) at low zoom

### Layout Calculation Optimizations

- Pre-calculate connection paths on data load
- Cache layout calculations
- Use coroutines for async processing
- Batch updates to reduce recompositions

