---
description: "Wave 3 — Implement the settlement candidate filter overlay mod (runs after W2 is complete)"
mode: agent
---

You are a senior software engineer implementing a Songs of Syx v70 mod. All prerequisite research and planning is complete. Your job is to implement the full mod as specified, using the concrete technical findings from the research waves.

---

## Your Task

Implement the settlement candidate filter overlay mod described in the spec, using the resolved implementation decisions and task plan from Wave 2.

---

## Sources to Read (all of them, in this order)

1. **Original spec** (full): `specs/syx_v70_settlement_candidate_filter_overlay_spec.md`
2. **Resolved decisions**: `docs/w2a-resolved-spec.md`
3. **Task plan**: `docs/w2b-task-plan.md`
4. **Wave 1 findings** (for concrete API details):
   - `docs/w1a-screen-class.md`
   - `docs/w1b-candidate-data.md`
   - `docs/w1c-draw-layer.md`
   - `docs/w1d-mod-patterns.md`
5. **Current workspace**:
   - `src/main/java/mod/example/ExampleMod.java` — **delete this file**; it does not implement `script.SCRIPT` and cannot be used as the mod entry point. Replace it with two new classes in your chosen package.
   - `src/main/resources/mod-files/_Info.txt`
   - `pom.xml`
   - `AGENTS.md` — build environment, toolchain, deploy commands

---

## Implementation Requirements

Follow the task plan from `docs/w2b-task-plan.md` in order.

### Hard constraints (from spec):
- Additive-only mod — no destructive replacement of game logic unless technically unavoidable and documented
- UI controls must appear on the settlement-selection / map review screen
- Controls must NOT rely on the global mod settings panel
- Candidate data must be cached after geography generation, not re-extracted on every filter change
- Markers appear only when at least one rule is enabled
- Status panel shows `passing / total`
- No tooltip changes

### Code organization (from spec section 14.4):
Keep these concerns in separate classes:
- Screen integration / panel rendering
- Candidate cache extraction
- Filter state model
- Filter evaluation
- Marker rendering

### Build environment:
- JDK 21 at `C:\dev\jdk21`
- Maven 3.9.14 at `C:\dev\apache-maven-3.9.14`
- Game JAR registered in local Maven repo as `com.songsofsyx:songsofsyx:70.33`
- Build commands:
  - `mvn compile` — compile only
  - `mvn package` — produce fat JAR
  - `mvn install` — package + deploy to `%APPDATA%\songsofsyx\mods\`

Set these env vars if needed in a new terminal session:
```powershell
$env:JAVA_HOME = "C:\dev\jdk21"
$env:Path = "C:\dev\jdk21\bin;C:\dev\apache-maven-3.9.14\bin;$env:Path"
```

---

## Implementation Process

1. Read all source documents listed above before writing any code
2. Follow the ordered task list from `docs/w2b-task-plan.md`
3. After completing each task group, run `mvn compile` to catch errors early
4. After full implementation, run `mvn package` to verify the build
5. Fix any compile errors before proceeding to the next task
6. After all code is written and compiling, verify against the validation checklist in spec section 17

---

## Validation Checklist (spec section 17)

Before declaring done, confirm all of the following:

1. Mod operates on v70
2. UI controls appear on the settlement-selection / map review screen
3. Controls are not placed solely in the global mod settings panel
4. Candidate site data is cached after geography generation
5. Toggling rules reevaluates against cached candidates
6. Passing candidates receive markers
7. No markers appear when zero rules are enabled
8. Tooltip content remains unchanged
9. Panel displays `passing / total`
10. Regenerating geography rebuilds the candidate cache
11. Existing game behavior is otherwise unchanged

---

## Output

- All Java source files created or modified in `src/main/java/`
- Updated `src/main/resources/mod-files/_Info.txt` (mod name, version, author)
- Updated `pom.xml` (artifactId, mod name)
- The mod must compile cleanly with `mvn compile`
- Write a brief summary of what was implemented to: `docs/w3-implementation-summary.md`
  - List all created/modified files
  - Note any spec section 17 items that could not be fully verified at compile time
  - Note any deviations from the task plan and why
