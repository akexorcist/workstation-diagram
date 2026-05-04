# Shared Knowledge Protocol

> This protocol applies to EVERY agent in EVERY project, without exception.
> Agents do not need to be told — it is enforced by CLAUDE.md at the project root.

---

## Two Layers of Knowledge

### Layer 1 — Team Knowledge (permanent, cross-project)
**Location:** `{TEAM_KNOWLEDGE_ROOT}/`

Accumulated wisdom that grows smarter with every project, organized by tech stack.
Contains patterns, gotchas, fix playbooks, and process improvements discovered in practice.

**Read:** Before starting any work — read files relevant to your role from the stack folder declared in `PROJECT_CONFIG.md` (`TECH_STACK_KEY`), plus `cross-project/`.
**Write:** After completing work — when you discover something reusable and non-obvious. See `{TEAM_KNOWLEDGE_ROOT}/WRITING_GUIDE.md` for when and how to write.

### Layer 2 — Project Knowledge (current project only)
**Location:** `{PROJECT_ROOT}/.agents/output/shared_context.md`

Live memory for the current project: phase state, class locations, decisions, issues, build history.

**Read:** Before starting any work.
**Write:** After completing any work — always.

---

## Before Starting Work (Every Agent, Every Time)

1. Read `PROJECT_CONFIG.md` → get `PROJECT_ROOT` and `TECH_STACK_KEY`
2. Read `{TEAM_KNOWLEDGE_ROOT}/team_knowledge.md` → understand what knowledge exists
3. Read relevant stack files from `{TEAM_KNOWLEDGE_ROOT}/{TECH_STACK_KEY}/` based on your role:

| Agent Role | Files to Read |
|---|---|
| Scaffold | `build_gotchas.md`, `fix_playbook.md` |
| Shared / Feature Developer | `effective_patterns.md`, `lessons_learned.md` |
| Platform Integrator | `effective_patterns.md`, `build_gotchas.md` |
| Fixer | `fix_playbook.md`, `build_gotchas.md` |
| Reviewer | `effective_patterns.md` |
| QA Agents | `effective_patterns.md`, `lessons_learned.md` |

4. Read `{TEAM_KNOWLEDGE_ROOT}/cross-project/testing_principles.md` — applies to all roles, always
5. Read other `{TEAM_KNOWLEDGE_ROOT}/cross-project/` files if they exist
6. Read `{PROJECT_ROOT}/.agents/output/shared_context.md`

---

## After Completing Work (Every Agent, Every Time)

### Always update project knowledge (`shared_context.md`):
- **Project State** — mark your phase complete
- **Known Class Locations** — every new public type (class, interface, object, data class)
- **Decisions Log** — every choice not explicitly in the spec
- **Known Issues** — every non-obvious problem and its fix

### Write to team knowledge when you encounter:

| What you found | Where to write |
|---|---|
| Build/config problem with a non-obvious fix | `{TECH_STACK_KEY}/build_gotchas.md` |
| Coding pattern that worked particularly well | `{TECH_STACK_KEY}/effective_patterns.md` |
| Mistake that cost time and could recur | `{TECH_STACK_KEY}/lessons_learned.md` |
| Recurring compiler/runtime error with a clear fix | `{TECH_STACK_KEY}/fix_playbook.md` |
| Pipeline or agent process improvement | `cross-project/process_improvements.md` |

If the stack folder or file does not exist yet, create it — see `WRITING_GUIDE.md`.
After writing, update the Projects Contributed table in `Knowledge/team_knowledge.md`.

---

## Project Knowledge — `shared_context.md` Update Conventions

**Known Class Locations** entry format:
```
`ClassName` → `fully.qualified.package.ClassName` (module/src/commonMain/kotlin/.../ClassName.kt)
```

**Decisions Log** entry format:
```
[AgentName] Decision: <what was decided> | Reason: <why>
```

**Known Issues** entry format:
```
[AgentName] Issue: <what went wrong> | Fix: <what was done>
```

---

## Conflict Resolution

If `shared_context.md` contradicts what you see in the actual files:
1. Trust the actual files — the context may be stale
2. Correct the entry in `shared_context.md`
3. Add: `[AgentName] Correction: <what was wrong> | Actual: <what is true now>`
