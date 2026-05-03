# Project Configuration

> This is the only file that contains project-specific values.
> All agents read this file first to get the context they need.
> To reuse this agent team on a new project, copy the .agents/ folder and replace this file only.

---

## Identity

| Key | Value |
|---|---|
| APP_NAME | Workstation Diagram |
| BASE_PACKAGE | dev.akexorcist.workstation |
| PROJECT_ROOT | /Users/akexorcist/Documents/Workspace/Workspace-Multiplatform/workstation-diagram |
| SPEC_FILE | (set per-task — path to the feature spec or prompt file) |
| TECH_STACK_KEY | kmp-desktop-web-compose |
| TEAM_KNOWLEDGE_ROOT | /Users/akexorcist/Documents/Agent/Knowledge |

## Build Config

| Key | Value |
|---|---|
| KOTLIN_VERSION | 2.3.0 |
| COMPOSE_VERSION | 1.9.3 |
| JVM_TOOLCHAIN | 21 |
| GRADLE_WRAPPER | (see gradle/wrapper/gradle-wrapper.properties) |

## Module List

```
:shared    → Data models, repository, validation, routing structs, shared UI components, shared theme
:viewer    → Read-only viewer app (ViewModel, screen, components)
:editor    → Interactive editor app (ViewModel, screen, components, routing logic)
```

## Source Set Convention

| Source Set | Purpose |
|---|---|
| `commonMain` | All shared Kotlin/Compose code — runs on all targets |
| `jvmMain` | Desktop (JVM) only — window setup, file I/O, desktop-specific APIs |
| `wasmJsMain` | Web (WASM/JS) only — browser APIs, JS interop |
| `commonTest` | Platform-agnostic tests |

## Tech Stack

| Layer | Choice |
|---|---|
| Language | Kotlin Multiplatform |
| UI | Compose Multiplatform + Material3 |
| State | StateFlow + androidx.lifecycle.viewmodel (KMP) |
| Serialization | Kotlinx Serialization (JSON) |
| Async | Kotlinx Coroutines |
| DI | None — constructor injection only |
| Targets | JVM Desktop + WASM Web |

## Build Commands

| Command | Purpose |
|---|---|
| `./gradlew build` | Compile all targets (use for build verification) |
| `./gradlew allTests` | Run all tests across all targets |
| `./gradlew :viewer:jvmRun` | Run desktop viewer (interactive) |
| `./gradlew :editor:jvmRun` | Run desktop editor (interactive) |
| `./gradlew :viewer:wasmJsBrowserDevelopmentRun` | Run web viewer |

## Key Constraints

- All shared logic in `:shared` — no duplication between `:viewer` and `:editor`
- No platform-specific code in `commonMain` — use `expect/actual`
- Immutable state only — `.copy()` for all state mutations
- Virtual coordinate system for canvas math — never raw pixels in business logic
- No hardcoded colors or text sizes in composables
