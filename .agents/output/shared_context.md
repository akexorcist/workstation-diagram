# Shared Context — Workstation Diagram

> Live project memory. Every agent reads this before starting and updates it after completing.
> If this file contradicts what you see in the actual code, trust the code and correct this file.

---

## Project State

| Phase | Agent | Status | Date |
|---|---|---|---|
| Architecture | 01 Architect | — | — |
| Architecture Review | 01b Architecture Reviewer | — | — |
| Scaffold | 02 Scaffold | — | — |
| Shared Module | 03 Shared Developer | — | — |
| Feature Modules | 04 Feature Developer | — | — |
| Platform Integration | 05 Platform Integrator | — | — |
| Build Verification | 06 Build Verifier | — | — |
| Code Review | 11 Reviewer | — | — |
| Test Review | 12 Test Agent | — | — |
| QA Performance | 08 QA Performance | — | — |
| QA Quality | 09 QA Quality | — | — |
| QA Security | 10 QA Security | — | — |
| UI Design | 13 UI Designer | — | — |
| UI Coding | 14 UI Coder | — | — |
| UI QA | 15 UI QA | — | — |

**Overall project status:** Not started

---

## Build Verification History

| Iteration | Status | Errors | Output file |
|---|---|---|---|
| (none yet) | | | |

---

## Module Dependency Graph

```
(populated by Scaffold agent)
```

---

## Known Class Locations

> Format: `ClassName` → `fully.qualified.package.ClassName` (module/src/sourceSet/kotlin/.../ClassName.kt)

### :shared module
(populated by Shared Developer agent)

### :viewer module
(populated by Feature Developer agent)

### :editor module
(populated by Feature Developer agent)

---

## Decisions Log

> Format: [AgentName] Decision: <what was decided> | Reason: <why>

(none yet)

---

## Test Coverage

> Full coverage map: `/Users/akexorcist/Documents/Agent/Knowledge/kmp-desktop-web-compose/project_test_coverage_workstation_diagram.md`

| Test File | Module | Tests | Covers |
|---|---|---|---|
| `DataValidatorTest.kt` | `:shared` commonTest | 11 | All validation rules (incl. 2 bugs fixed) |
| `StateManagementConfigTest.kt` | `:shared` commonTest | 4 | Zoom clamping logic |
| `WorkstationLayoutSerializerTest.kt` | `:shared` commonTest | 4 | JSON parsing resilience + round-trip |
| `SimpleConnectionRouterTest.kt` | `:editor` commonTest | 3 | Auto-routing L-path and fallback |

**22 tests total — all passing as of 2026-05-04**

Largest gap: `WorkstationViewModel` synchronous state mutations (pan, hover, theme, reset) — testable but not yet written.

---

## Known Issues

> Format: [AgentName] Issue: <what went wrong> | Fix: <what was done>

[Test run 2026-05-04] Issue: `DataValidator.validateDevice()` used `Set.count { it == id } > 1` to detect duplicate device IDs and port IDs — this is always false on a `Set` so duplicates were silently ignored | Fix: replaced with `seenDeviceIds: MutableSet<String>` in `validateLayout` and `seenPortIds: MutableSet<String>` in `validateDevice`

---

## Notes

This project is a Kotlin Multiplatform application targeting JVM Desktop and WASM Web.
- `:shared` — data models, repository, validation, routing, shared UI components
- `:viewer` — read-only diagram viewer
- `:editor` — interactive diagram editor

Build commands:
- Compile all: `./gradlew build`
- Run all tests: `./gradlew allTests`
- Run desktop viewer: `./gradlew :viewer:jvmRun`
- Run desktop editor: `./gradlew :editor:jvmRun`
