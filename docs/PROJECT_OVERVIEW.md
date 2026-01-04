# Workstation Diagram - Project Overview

## Purpose

Workstation Diagram is a Kotlin Multiplatform application for visualizing and editing workstation hardware configurations. It allows users to view and create interactive diagrams showing how devices (laptops, monitors, hubs, peripherals) are connected via ports and cables.

---

## Project Structure

The project consists of three main modules:

### 1. **shared** Module
- **Purpose**: Common code shared across all applications
- **Contains**: Data models, serialization, validation, UI components, utilities
- **Used by**: Both viewer and editor modules

### 2. **viewer** Module
- **Purpose**: Read-only visualization application
- **Targets**: Desktop (JVM) and Web (WASM/JS)

### 3. **editor** Module
- **Purpose**: Interactive editing application
- **Targets**: Desktop (JVM) and Web (WASM/JS)

---

## Viewer Application

### Purpose

Read-only visualization application for displaying workstation diagrams. Users can view and explore hardware configurations without modifying them.

### Features

#### Core Visualization
- Display workstation diagrams from JSON data
- Render devices (laptops, monitors, hubs, peripherals) with visual representations
- Render connections between devices with routing paths
- Auto-routing display for connections (uses routing points from JSON or calculated paths)

#### Viewport Navigation
- Zoom in/out (mouse wheel, keyboard shortcuts, control panel)
- Pan (mouse drag, arrow keys)
- Zoom to specific points (maintains point position during zoom)
- Center viewport on all devices (auto-centering on load)
- Reset zoom to default value
- Keyboard shortcuts:
  - Ctrl/Cmd + Plus/Equals: Zoom in
  - Ctrl/Cmd + Minus: Zoom out
  - Arrow keys: Pan viewport

#### Device Interaction
- Device selection (click to select)
- Device hover states (visual feedback on hover)
- Device details dialog (shows device information when selected)
- Device list sidebar (navigable list of all devices with filtering support)
- Device search/filter (state support for filtering devices)

#### UI Features
- Instruction legend (color-coded device categories and connector types)
- Header card (diagram title, date, external links)
- Collapsible sections (instruction legend, device list)
- UI panel visibility toggle (hide/show sidebar for focused viewing)
- Theme switching (light/dark mode)
- Connection animation toggle (enable/disable animated connections)

### Data Handling
- Loads `workstation.json` files
- Read-only access (no save/export functionality)
- Displays routing points from JSON
- Calculates straight paths for connections without routing points

---

## Editor Application

### Purpose

Interactive editing application for creating and modifying workstation diagrams. Extends viewer capabilities with full editing functionality.

### Features

#### All Viewer Features
- All visualization capabilities from the viewer
- All viewport navigation features
- Theme switching
- Connection animation toggle

#### Editing Capabilities
- **Port Position Editing**: Adjust port positions on device edges interactively
- **Connection Path Editing**: Manually edit connection routing points/waypoints
- **Routing Point Management**: Select, drag, and modify routing points
- **Line Segment Editing**: Select and edit connection line segments
- **Device Selection**: Select devices for editing operations

#### Auto-Generation
- **Auto-routing**: Automatically generates routing points for connections when missing from JSON
- **One-time Generation**: Auto-routed paths are saved and persist in the connection data
- **Path Optimization**: Generated paths avoid device obstacles and optimize layout

**Auto-Generation Behavior**:
When a connection in the JSON lacks routing points:
1. System detects missing routing information
2. Auto-routing algorithm generates path (one-time operation)
3. Path avoids device obstacles
4. Path optimizes for minimal crossings and clean layout
5. Auto-generated routing points are immediately saved to the Connection object

**Important**: Once a connection has routing points (whether auto-generated or manually defined), it will never be auto-routed again. The routing points persist and can be manually edited by the user. All routing points (auto-generated or manual) are included when exporting to JSON.

#### Export Functionality
- Export edited diagrams to JSON format
- Saves all routing points (manual and auto-generated)
- Preserves device positions, port positions, and connection paths

#### Editing State Management
- Selected routing points
- Dragging routing points
- Hovered routing points
- Selected line segments
- Selected ports
- Connection selection for editing

### Data Handling
- Loads `workstation.json` files
- Reads and writes JSON data
- Auto-generates routing points when missing
- Syncs port positions with connections
- Exports complete diagram data

---

## Key Concepts

### Data Model

The application uses a JSON-based data format (`workstation.json`) with the following structure:

- **Devices**: Hardware components (laptops, monitors, hubs, peripherals)
  - Each device has a position, size, and list of ports
  - Ports have positions on device edges (TOP, BOTTOM, LEFT, RIGHT)
  - Ports have directions (INPUT, OUTPUT, BIDIRECTIONAL)

- **Connections**: Links between device ports
  - References source and target devices/ports
  - Optional routing points for manual path definition
  - Auto-generated if routing points not provided (editor only)

- **Metadata**: Canvas configuration, viewport settings, grid settings

### Coordinate System

- Uses virtual coordinate system for resolution independence
- Devices and connections positioned in virtual space
- Automatically scales to screen size
- Supports zoom and pan operations

### Routing System

Connections between devices can be:
- **Manual Routing**: User-defined waypoints stored in JSON
- **Auto Routing**: Automatically generated paths when manual routing not available (editor only)
- Routes avoid device obstacles and optimize pathfinding

### State Management

- Uses MVVM (Model-View-ViewModel) architecture
- Reactive state with StateFlow
- ViewModels manage UI state and business logic
- State persists across viewport operations

---

## Architecture Patterns

### Multiplatform Strategy
- Common code in `commonMain`
- Platform-specific code in `jvmMain` (desktop) and `wasmJsMain` (web)
- Uses expect/actual pattern for platform abstractions

### Layered Architecture
- **UI Layer**: Compose UI components
- **Presentation Layer**: ViewModels and state management
- **Data Layer**: Models, repositories, serialization
- **Platform Layer**: Platform-specific utilities

### Design Patterns
- **Repository Pattern**: Abstracts data access
- **MVVM Pattern**: Separates UI from business logic
- **Observer Pattern**: Reactive state updates
- **Strategy Pattern**: Configurable algorithms (routing, rendering)

---

## Technology Stack

- **Language**: Kotlin
- **UI Framework**: Compose Multiplatform
- **Serialization**: Kotlinx Serialization (JSON)
- **Concurrency**: Kotlinx Coroutines
- **State Management**: StateFlow, Lifecycle ViewModel
- **Platforms**: JVM (desktop), WASM/JS (web)

---

## Development Workflow

### Build System
- Gradle with Kotlin DSL
- Multi-platform build configuration
- Shared dependency versions via `libs.versions.toml`

### Code Organization
- Modules organized by feature/application
- Shared code in `shared` module
- Application-specific code in respective modules
- Documentation in `docs/` directory

### Key Principles
- Keep common code platform-agnostic
- Use expect/actual for platform-specific implementations
- Maintain separation of concerns (UI, presentation, data)
- Follow Kotlin best practices
- Keep documentation up-to-date

---

## Current Development Focus

The editor module is currently being enhanced with:
1. **Port Position Editing**: Interactive editing of port positions on device edges
2. **Connection Path Editing**: Manual editing of connection routing waypoints
3. **Auto-routing Enhancement**: Improved automatic connection path generation

These features allow users to:
- Fine-tune port positions for better layout
- Manually adjust connection paths when auto-routing isn't ideal
- Have connections automatically generated when JSON lacks routing data

---

## Data Format

The application reads/writes `workstation.json` files with:
- Device definitions with positions, sizes, ports
- Connection definitions linking device ports
- Optional routing points for manual path control
- Metadata for canvas and viewport configuration

The JSON format is designed to be:
- Human-readable and editable
- Extensible (optional fields)
- Backward compatible
- Validation-friendly

---

## For AI Agents

When working on this project:

1. **Understand the Structure**: Know which module you're working in (shared/viewer/editor)
2. **Respect Boundaries**: Don't duplicate code that should be in `shared`
3. **Follow Patterns**: Match existing architecture patterns
4. **Check Documentation**: Review relevant docs before implementing
5. **Consider Multiplatform**: Ensure code works on both JVM and WASM/JS targets
6. **Maintain Compatibility**: Changes should work with existing JSON format
7. **Update Documentation**: Keep docs current when making changes

Key files to understand:
- Data models: `shared/src/commonMain/kotlin/.../data/model/`
- Viewer state: `viewer/src/commonMain/kotlin/.../presentation/WorkstationViewModel.kt`
- Editor state: `editor/src/commonMain/kotlin/.../presentation/EditorViewModel.kt`
- JSON structure: `viewer/src/commonMain/resources/data/workstation.json` or `editor/src/commonMain/resources/data/workstation.json`
- Documentation: `docs/` directory

---

## Related Documentation

For more detailed information:
- [README.md](README.md) - Technical documentation and setup
- [ARCHITECTURE_SUMMARY.md](ARCHITECTURE_SUMMARY.md) - Architecture patterns
- [COORDINATE_SYSTEM.md](COORDINATE_SYSTEM.md) - Coordinate system details
- [MANUAL_PATH_ROUTING_IMPLEMENTATION_PLAN.md](MANUAL_PATH_ROUTING_IMPLEMENTATION_PLAN.md) - Routing implementation
- [WORKSTATION_JSON_VALIDATION_RULES.md](WORKSTATION_JSON_VALIDATION_RULES.md) - JSON validation rules
