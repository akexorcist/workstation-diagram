# Workstation Diagram - Project Overview

## Purpose

Workstation Diagram is a Kotlin Multiplatform application for visualizing and editing workstation hardware configurations. It allows users to create interactive diagrams showing how devices (laptops, monitors, hubs, peripherals) are connected via ports and cables.

---

## Project Structure

The project consists of three main modules:

### 1. **shared** Module
- **Purpose**: Common code shared across all applications
- **Contains**: Data models, serialization, validation, UI components, utilities
- **Used by**: Both viewer and editor modules

### 2. **viewer** Module
- **Purpose**: Read-only visualization application
- **Features**: Display workstation diagrams, viewport navigation (zoom/pan), device information
- **Targets**: Desktop (JVM) and Web (WASM/JS)

### 3. **editor** Module
- **Purpose**: Interactive editing application
- **Features**: All viewer features plus editing capabilities
- **Current Work**: Port position editing, connection line segment position editing
- **Auto-generation**: Automatically generates connections when not available in JSON
- **Targets**: Desktop (JVM) and Web (WASM/JS)

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
  - Auto-generated if routing points not provided

- **Metadata**: Canvas configuration, viewport settings, grid settings

### Coordinate System

- Uses virtual coordinate system for resolution independence
- Devices and connections positioned in virtual space
- Automatically scales to screen size
- Supports zoom and pan operations

### Routing System

Connections between devices can be:
- **Manual Routing**: User-defined waypoints stored in JSON
- **Auto Routing**: Automatically generated paths when manual routing not available
- Routes avoid device obstacles and optimize pathfinding

### State Management

- Uses MVVM (Model-View-ViewModel) architecture
- Reactive state with StateFlow
- ViewModels manage UI state and business logic
- State persists across viewport operations

---

## Editor App Features

The editor application extends the viewer with editing capabilities:

### Current Capabilities
- Visualize workstation diagrams (same as viewer)
- Viewport navigation (zoom, pan)
- Device and connection selection
- Theme switching (light/dark)

### In Development
- **Port Position Editing**: Allow users to adjust port positions on devices
- **Connection Path Editing**: Allow users to manually edit connection routing points
- **Auto-connection Generation**: Automatically generate connections when missing from JSON

### Auto-Generation Behavior

When a connection in the JSON lacks routing points:
1. System detects missing routing information
2. Auto-routing algorithm generates path
3. Path avoids device obstacles
4. Path optimizes for minimal crossings and clean layout

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
- Editor state: `editor/src/commonMain/kotlin/.../presentation/EditorViewModel.kt`
- JSON structure: `editor/src/commonMain/resources/data/workstation.json`
- Documentation: `docs/` directory

---

## Related Documentation

For more detailed information:
- [README.md](README.md) - Technical documentation and setup
- [ARCHITECTURE_SUMMARY.md](ARCHITECTURE_SUMMARY.md) - Architecture patterns
- [COORDINATE_SYSTEM.md](COORDINATE_SYSTEM.md) - Coordinate system details
- [MANUAL_PATH_ROUTING_IMPLEMENTATION_PLAN.md](MANUAL_PATH_ROUTING_IMPLEMENTATION_PLAN.md) - Routing implementation
- [WORKSTATION_JSON_VALIDATION_RULES.md](WORKSTATION_JSON_VALIDATION_RULES.md) - JSON validation rules

