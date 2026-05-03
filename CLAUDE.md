# Agent Rules — Workstation Diagram

These rules apply to every agent in this project automatically.

---

## Standard Operating Procedure (Every Agent, Every Time)

### On startup — non-negotiable
1. Read `.agents/PROJECT_CONFIG.md` → extract `PROJECT_ROOT`, `TECH_STACK_KEY`, `TEAM_KNOWLEDGE_ROOT`
2. Read `{TEAM_KNOWLEDGE_ROOT}/team_knowledge.md`
3. Read `{TEAM_KNOWLEDGE_ROOT}/{TECH_STACK_KEY}/` files relevant to your role
4. Read `{TEAM_KNOWLEDGE_ROOT}/cross-project/testing_principles.md` — mandatory for all roles
5. Read other `{TEAM_KNOWLEDGE_ROOT}/cross-project/` files if they exist
6. Read `.agents/output/shared_context.md` in full

### On completion — non-negotiable
1. Update `.agents/output/shared_context.md`:
   - Project State: mark your phase complete
   - Known Class Locations, Decisions Log, Known Issues as applicable
2. Write to `{TEAM_KNOWLEDGE_ROOT}/` when you found something reusable:
   - See `{TEAM_KNOWLEDGE_ROOT}/WRITING_GUIDE.md` for when and how

Full protocol: `.agents/SHARED_KNOWLEDGE_PROTOCOL.md`

---

## Agent Team

```
User → 00 Team Lead (quality gates + process monitor)
           │
           └─► 00 Orchestrator (mechanical sequencer)
                   │
                   ├─► 01 Architect → 01b Architecture Reviewer (↔ loop max 2x)
                   ├─► 02 Scaffold
                   ├─► 03 Shared Developer → 04 Feature Developer → 05 Platform Integrator
                   ├─► 06 Build Verifier ↔ 07 Fixer (loop, max 3x)
                   ├─► 11 Reviewer + 12 Test Agent + 08–10 QA (parallel, Checkpoint 8)
                   └─► UI Pass: 13 UI Designer → 14 UI Coder → 06 Build Verifier → 15 UI QA (Checkpoint 9)
```

All agent definitions: `.agents/`

---

## Project Constraints (Never Violate)

- All shared logic goes in `shared/` — never duplicated across `viewer/` and `editor/`
- No platform-specific code in `commonMain` — use `expect/actual` for platform abstractions
- All state management via `StateFlow` + `ViewModel` — no global mutable state
- Immutable state updates only — always use `.copy()`, never mutate state directly
- Virtual coordinate system for all canvas math — never use raw pixel values in business logic
- No hardcoded colors or sizes — always use `MaterialTheme.colorScheme` and `MaterialTheme.typography`
- Source sets: `commonMain` for shared, `jvmMain` for desktop-only, `wasmJsMain` for web-only
- Kotlin version: 2.3.21 | Compose Multiplatform: 1.10.3 | JVM Toolchain: 21
