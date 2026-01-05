# Workstation Diagram - Project Overview

## Purpose

Workstation Diagram is a Kotlin Multiplatform application for visualizing and editing workstation hardware configurations. It provides interactive diagrams showing how devices (laptops, monitors, hubs, peripherals) are connected via ports and cables.

The application supports both read-only viewing and interactive editing of workstation configurations, with automatic pathfinding for connection routing and a resolution-independent coordinate system for cross-platform compatibility.

---

## Project Structure

The project consists of three main modules, each serving a distinct purpose:

### Shared Module

**Purpose**: Common code shared across all applications

**Responsibilities**:
- Data models and structures (devices, ports, connections, layout metadata)
- JSON serialization and deserialization
- Data validation rules and validation logic
- Shared UI components (device rendering, port rendering, canvas components)
- Coordinate transformation utilities
- Configuration classes (rendering, viewport, state management)
- Platform-agnostic utilities

**Design Principle**: Contains only platform-independent code that can be used by both viewer and editor applications.

### Viewer Module

**Purpose**: Read-only visualization application

**Responsibilities**:
- Display workstation diagrams from JSON data
- Viewport navigation (zoom, pan, centering)
- Device interaction (selection, hover, details)
- Connection visualization with routing paths
- UI controls and sidebars (device list, legend, header)
- Theme management

**Target Platforms**: Desktop (JVM) and Web (WASM/JS)

**Design Principle**: Focuses on visualization and exploration without modification capabilities.

### Editor Module

**Purpose**: Interactive editing application

**Responsibilities**:
- All viewer capabilities (inherits visualization features)
- Port position editing on device edges
- Connection path editing (manual routing point manipulation)
- Auto-routing generation for connections without routing points
- Export functionality to JSON format
- Editing state management (selection, dragging, hover states)

**Target Platforms**: Desktop (JVM) and Web (WASM/JS)

**Design Principle**: Extends viewer functionality with full editing capabilities while maintaining the same visualization foundation.

---

## Key Features

### Viewer Application Features

#### Core Visualization
- Renders devices with visual representations based on device categories
- Displays connections between devices with intelligent routing paths
- Supports both manual routing (from JSON) and automatic path calculation
- Responsive rendering that adapts to different screen sizes

#### Viewport Navigation
- Point-based zooming (zooms towards viewport center for intuitive interaction)
- Smooth panning with drag gestures
- Automatic centering on diagram content
- Keyboard shortcuts for navigation
- Zoom level management with configurable limits

#### Device Interaction
- Device selection and highlighting
- Hover states for visual feedback
- Device details display
- Device list sidebar with search and filtering
- Visual distinction between device categories

#### User Interface
- Collapsible UI panels for focused viewing
- Theme switching (light/dark mode)
- Connection animation toggle
- Instruction legend for device categories and connection types
- Header information (title, date, external links)

### Editor Application Features

#### Editing Capabilities
- **Port Position Editing**: Interactive adjustment of port positions along device edges
- **Connection Path Editing**: Manual manipulation of routing waypoints
- **Routing Point Management**: Select, drag, and modify individual routing points
- **Line Segment Editing**: Direct editing of connection line segments
- **Device Selection**: Select devices for editing operations

#### Auto-Generation
- **Automatic Routing**: Generates routing paths for connections missing routing information
- **One-time Generation**: Auto-routed paths are saved immediately and persist
- **Path Optimization**: Generated paths avoid device obstacles and minimize crossings
- **Smart Detection**: Only generates paths when routing points are absent

**Auto-Generation Behavior**: When a connection lacks routing points, the system automatically generates an optimized path. Once generated (or manually defined), routing points persist and can be manually edited. The system never regenerates paths for connections that already have routing points.

#### Export Functionality
- Exports complete diagram data to JSON format
- Preserves all routing points (both manual and auto-generated)
- Maintains device positions, port positions, and connection paths
- Ensures data integrity and backward compatibility

---

## Key Concepts

### Data Model

The application uses a JSON-based data format with a hierarchical structure:

- **Devices**: Represent hardware components with positions, sizes, and ports
  - Ports are positioned along device edges (top, bottom, left, right)
  - Ports have directions (input, output, bidirectional)
  - Devices belong to categories (laptop, monitor, hub, peripheral)

- **Connections**: Link device ports together
  - Reference source and target devices and ports
  - May include optional routing points for manual path definition
  - Support automatic path generation when routing points are absent

- **Metadata**: Contains canvas configuration, viewport settings, and coordinate system specification

### Coordinate System

The application uses a virtual coordinate system for resolution independence:

- Fixed virtual canvas (typically 10000×10000 units)
- Automatic scaling to any screen size
- Maintains aspect ratios across different displays
- Supports both virtual and absolute coordinate modes (backward compatible)
- Grid alignment for consistent routing behavior

See [COORDINATE_SYSTEM.md](COORDINATE_SYSTEM.md) for detailed information.

### Routing System

Connections between devices use intelligent routing:

- **Manual Routing**: User-defined waypoints stored in JSON data
- **Automatic Routing**: Algorithm-generated paths when manual routing is unavailable
- **Obstacle Avoidance**: Paths avoid device boundaries and other obstacles
- **Path Optimization**: Minimizes turns, crossings, and path length
- **Grid-Based**: Uses grid alignment for consistent pathfinding

See [ROUTING_SYSTEM.md](ROUTING_SYSTEM.md) for detailed information.

### State Management

The application uses MVVM (Model-View-ViewModel) architecture:

- **Reactive State**: StateFlow-based state management for automatic UI updates
- **Unidirectional Data Flow**: State flows from ViewModel to UI, events flow from UI to ViewModel
- **Lifecycle Awareness**: Automatic resource cleanup and state preservation
- **State Validation**: All state changes validated before application
- **Derived State**: Render-optimized state computed from primary state

See [STATE_MANAGEMENT.md](STATE_MANAGEMENT.md) for detailed information.

---

## Architecture Patterns

### Multiplatform Strategy

The project uses Kotlin Multiplatform to share code across platforms:

- **Common Code**: Platform-agnostic code in `commonMain` source sets
- **Platform-Specific Code**: Isolated in `jvmMain` (desktop) and `wasmJsMain` (web)
- **Expect/Actual Pattern**: Platform abstractions for platform-specific implementations
- **Single Codebase**: Shared business logic, data models, and UI components

### Layered Architecture

The application follows a clean layered architecture:

- **UI Layer**: Compose UI components, screens, and themes
- **Presentation Layer**: ViewModels, state management, and business logic
- **Data Layer**: Models, repositories, serialization, and validation
- **Platform Layer**: Platform-specific utilities and implementations

### Design Patterns

The application employs several design patterns:

- **Repository Pattern**: Abstracts data access for the presentation layer
- **MVVM Pattern**: Separates UI from business logic and state management
- **Observer Pattern**: Enables reactive state updates via StateFlow
- **Strategy Pattern**: Configurable algorithms for routing and rendering
- **Factory Pattern**: Object creation without specifying concrete types

See [ARCHITECTURE.md](ARCHITECTURE.md) for detailed information.

---

## Technology Stack

- **Language**: Kotlin
- **UI Framework**: Compose Multiplatform
- **Serialization**: Kotlinx Serialization (JSON)
- **Concurrency**: Kotlinx Coroutines
- **State Management**: StateFlow, Lifecycle ViewModel
- **Platforms**: JVM (desktop), WASM/JS (web)

---

## Data Format

The application uses a JSON-based data format (`workstation.json`) that is:

- **Human-readable**: Easy to read and edit manually
- **Extensible**: Supports optional fields for future enhancements
- **Backward Compatible**: Works with existing data files
- **Validation-Friendly**: Structured for comprehensive validation
- **Versioned**: Includes version information for migration support

The format includes:
- Device definitions with positions, sizes, and ports
- Connection definitions linking device ports
- Optional routing points for manual path control
- Metadata for canvas and viewport configuration

See [DATA_MODEL.md](DATA_MODEL.md) for detailed information about the data structure.

---

## Related Documentation

- [README.md](README.md) - Documentation navigation hub
- [ARCHITECTURE.md](ARCHITECTURE.md) - System architecture and design patterns
- [STATE_MANAGEMENT.md](STATE_MANAGEMENT.md) - State management concepts
- [COORDINATE_SYSTEM.md](COORDINATE_SYSTEM.md) - Coordinate system design
- [VIEWPORT_SYSTEM.md](VIEWPORT_SYSTEM.md) - Viewport navigation
- [ROUTING_SYSTEM.md](ROUTING_SYSTEM.md) - Connection routing
- [RENDERING_SYSTEM.md](RENDERING_SYSTEM.md) - Visual rendering
- [DATA_MODEL.md](DATA_MODEL.md) - Data structure concepts
- [VALIDATION.md](VALIDATION.md) - Validation concepts
- [EDITING_SYSTEM.md](EDITING_SYSTEM.md) - Editor capabilities
