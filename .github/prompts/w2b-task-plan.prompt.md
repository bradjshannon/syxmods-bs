---
description: "Wave 2B — Create implementation task plan (runs after all W1 agents complete)"
mode: agent
---

You are a technical task planner. Wave 1 research agents have completed their findings. Your job is to produce a concrete, ordered implementation task breakdown for the Songs of Syx mod described in the spec.

---

## Your Task

Read the original spec and all four Wave 1 findings files. Produce an ordered implementation plan with discrete, independently-verifiable tasks. Each task should be small enough to implement and test in isolation.

---

## Sources to Read (all of them)

1. Original spec: `specs/syx_v70_settlement_candidate_filter_overlay_spec.md`
   - Read the full document.

2. Wave 1 findings:
   - `docs/w1a-screen-class.md` — screen class, hook points, regenerate button UI
   - `docs/w1b-candidate-data.md` — candidate site data model, resource fields, adjacency, climate
   - `docs/w1c-draw-layer.md` — draw layers, marker rendering API, renderer type
   - `docs/w1d-mod-patterns.md` — mod lifecycle, UI injection, render hook, event patterns

3. Current workspace structure:
   - `src/main/java/mod/example/ExampleMod.java` — starter class
   - `src/main/resources/mod-files/_Info.txt` — mod metadata
   - `pom.xml` — Maven build config

---

## Planning Constraints

- Tasks must be ordered by dependency (earlier tasks must not depend on later ones)
- Each task must have a clear, testable completion criterion
- No task should be so large that it cannot be completed in a single focused implementation session
- Respect the spec's hard requirements:
  - additive-only mod (no destructive replacement unless unavoidable)
  - UI controls on the settlement-selection screen (not the global settings panel)
  - candidate data cached after geography generation
  - markers shown only when at least one rule is active
  - status display shows `passing / total`
- Follow the internal separation recommended in spec section 14.4:
  - screen integration / panel rendering
  - candidate cache extraction
  - filter state model
  - filter evaluation
  - marker rendering

---

## Output

Write your output to: `docs/w2b-task-plan.md`

Structure the file as follows:

```
# W2B: Implementation Task Plan

## Package and Class Structure
Proposed Java package name, top-level class names, and their responsibilities.

## Ordered Task List

### Task 1: [Title]
- **Depends on**: (none, or previous task numbers)
- **What to implement**: [concrete description using actual class/field names from Wave 1 findings]
- **Files to create/modify**: 
- **Completion criterion**: [specific, verifiable]

### Task 2: [Title]
... (repeat for all tasks)

## Risks and Mitigations
List the highest-risk implementation steps and how to mitigate them, based on Wave 1 confidence levels.

## Build and Deploy Verification
Steps to verify the mod loads in-game after each major milestone.
```

Include at minimum these logical task groups (break into as many individual tasks as appropriate):
1. Delete `ExampleMod.java` and create replacement SCRIPT entry point classes (two new files: one implementing `script.SCRIPT`, one implementing `script.SCRIPT.SCRIPT_INSTANCE`); update `pom.xml` artifactId and mod name; update `_Info.txt`
2. Mod registration and lifecycle hook
3. Candidate data extraction and cache
4. Filter state model (Java classes, no UI yet)
5. Filter evaluation logic (pure logic, no UI yet)
6. UI panel — layout and widget scaffold
7. UI panel — resource rules (checkboxes + threshold inputs)
8. UI panel — adjacency rules (checkboxes)
9. UI panel — climate rules (checkboxes)
10. UI panel — status display (`X / Y pass`)
11. Panel integration into settlement screen
12. Marker rendering (draw passing candidates on map)
13. Wire filter state changes to re-evaluation and marker redraw
14. Wire geography regeneration to cache rebuild
15. End-to-end validation against spec section 17 checklist
```
