# Workstation Diagram - Documentation

## Overview

Workstation Diagram is a Kotlin Multiplatform application for visualizing and editing workstation hardware configurations. It provides interactive diagrams showing how devices (laptops, monitors, hubs, peripherals) are connected via ports and cables.

This documentation focuses on understanding the project's architecture, design concepts, and how systems work together. For implementation details, refer to the code itself.

---

## Quick Start

**New to the project?** Start here:
1. [PROJECT_OVERVIEW.md](PROJECT_OVERVIEW.md) - Understand the project structure and purpose
2. [ARCHITECTURE.md](ARCHITECTURE.md) - Learn the system architecture and design patterns
3. [COORDINATE_SYSTEM.md](COORDINATE_SYSTEM.md) - Understand the coordinate system (essential for understanding rendering)

---

## Documentation Structure

### Core Documentation

- **[PROJECT_OVERVIEW.md](PROJECT_OVERVIEW.md)** - High-level project understanding
  - Project purpose and goals
  - Module structure (shared/viewer/editor)
  - Key features overview
  - Technology stack summary

### Architecture & Design

- **[ARCHITECTURE.md](ARCHITECTURE.md)** - System architecture and design patterns
  - Layered architecture (UI, Presentation, Data, Platform)
  - MVVM pattern explanation
  - Multiplatform strategy
  - Design patterns used
  - Data flow concepts
  - Integration points

- **[STATE_MANAGEMENT.md](STATE_MANAGEMENT.md)** - State management concepts
  - StateFlow-based reactive state
  - Unidirectional data flow
  - ViewModel lifecycle
  - State validation and constraints
  - Derived state computation

### Core Systems

- **[COORDINATE_SYSTEM.md](COORDINATE_SYSTEM.md)** - Coordinate system design
  - Virtual vs absolute coordinate systems
  - Transformation pipeline (Data → World → Screen)
  - Zoom and pan integration
  - Grid alignment concepts
  - Resolution independence

- **[VIEWPORT_SYSTEM.md](VIEWPORT_SYSTEM.md)** - Viewport navigation and interaction
  - Zoom mechanics (point-based zooming)
  - Pan operations
  - Centering algorithms
  - Viewport culling for performance
  - Coordinate transformation during navigation

- **[ROUTING_SYSTEM.md](ROUTING_SYSTEM.md)** - Connection routing architecture
  - Grid-based pathfinding
  - Obstacle avoidance
  - Manual vs automatic routing
  - Path optimization strategies
  - Port extension system
  - Path distribution algorithms

- **[RENDERING_SYSTEM.md](RENDERING_SYSTEM.md)** - Visual rendering concepts
  - Rendering layers (devices, connections, ports)
  - Connection path rendering (Bezier curves, gradients)
  - Port rendering (capsule labels)
  - Animation system
  - Performance optimizations (culling, caching)

### Data & Validation

- **[DATA_MODEL.md](DATA_MODEL.md)** - Data structure concepts
  - WorkstationLayout structure
  - Device, Port, Connection relationships
  - Metadata and configuration
  - Coordinate system specification
  - Data relationships and constraints

- **[VALIDATION.md](VALIDATION.md)** - Validation rules and concepts
  - Validation levels (error, warning, info)
  - Business rules (1-to-1 port usage, etc.)
  - Structural validation
  - Routing compatibility checks
  - Best practices validation

### Editor-Specific

- **[EDITING_SYSTEM.md](EDITING_SYSTEM.md)** - Editing capabilities
  - Port position editing
  - Connection path editing
  - Routing point management
  - Auto-generation behavior
  - Export functionality

---

## Project Structure

The project consists of three main modules:

- **shared** - Common code shared across all applications (data models, serialization, validation, utilities)
- **viewer** - Read-only visualization application (Desktop JVM and Web WASM/JS)
- **editor** - Interactive editing application (Desktop JVM and Web WASM/JS)

---

## Key Concepts

### Coordinate System
The application uses a virtual coordinate system (10000×10000 canvas) that automatically scales to any screen size, providing resolution independence and cross-platform compatibility.

### Viewport Navigation
Point-based zooming keeps the viewport center fixed during zoom operations. Pan operations use accumulated deltas for smooth interaction.

### Connection Routing
Grid-based A* pathfinding with obstacle avoidance. Supports both manual routing (user-defined waypoints) and automatic routing (algorithm-generated paths).

### Rendering
Multi-layer rendering system with Bezier curve corners, gradient coloring, and animated flow effects for connections. Ports are rendered as capsule-shaped labels.

---

## Technology Stack

- **Language**: Kotlin
- **UI Framework**: Compose Multiplatform
- **Serialization**: Kotlinx Serialization (JSON)
- **Concurrency**: Kotlinx Coroutines
- **State Management**: StateFlow, Lifecycle ViewModel
- **Platforms**: JVM (desktop), WASM/JS (web)

---

## Documentation Principles

This documentation focuses on:
- **Concepts**: How systems work conceptually
- **Design decisions**: Why things are designed a certain way
- **Architecture patterns**: High-level patterns and their purposes
- **Data flow**: How data moves through the system
- **Relationships**: How different systems interact

This documentation does not include:
- Code examples (code is self-documenting)
- Implementation details (refer to code)
- API references (refer to code)
- Step-by-step guides (focus on understanding, not doing)

---

## Related Documentation

For validator application documentation, see the [validator/](validator/) directory.
