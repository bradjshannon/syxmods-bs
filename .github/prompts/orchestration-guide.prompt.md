---
description: "Orchestration guide — sequence of subagent calls to implement the settlement filter overlay mod"
---

# Orchestration Guide: Settlement Candidate Filter Overlay Mod

This document coordinates the full subagent sequence for implementing the mod described in `specs/syx_v70_settlement_candidate_filter_overlay_spec.md`.

---

## Execution Model

Agents are executed sequentially. Context isolation within Wave 1 is achieved by giving each agent a clean prompt referencing only original sources — Wave 1 agents do not read each other's output. This produces independent, uncorrelated research findings.

Wave 2 agents may run in either order (both read Wave 1 output but not each other). Wave 3 runs last and reads everything.

---

## Deliverables Map

| Agent | Reads | Writes |
|---|---|---|
| W1A | spec (§1–5), sources JAR | `docs/w1a-screen-class.md` |
| W1B | spec (§8, §14), sources JAR | `docs/w1b-candidate-data.md` |
| W1C | spec (§9, §14), sources JAR | `docs/w1c-draw-layer.md` |
| W1D | spec (§14), GitHub example repo | `docs/w1d-mod-patterns.md` |
| W2A | spec (full), all W1 docs | `docs/w2a-resolved-spec.md` |
| W2B | spec (full), all W1 docs, workspace | `docs/w2b-task-plan.md` |
| W3  | spec (full), all W1+W2 docs, workspace | source files + `docs/w3-implementation-summary.md` |

---

## Wave 1 — Research (context-isolated)

Run in sequence. Each agent uses only the original spec and game sources — not other agents' findings.

**Pause after Wave 1 to review all four findings files before proceeding.**

### Step 1: W1A — Screen Class
- Prompt file: `.github/prompts/w1a-explore-screen-class.prompt.md`
- Agent: `Explore` (thorough)
- Output: `docs/w1a-screen-class.md`

### Step 2: W1B — Candidate Data Model
- Prompt file: `.github/prompts/w1b-explore-candidate-data.prompt.md`
- Agent: `Explore` (thorough)
- Output: `docs/w1b-candidate-data.md`

### Step 3: W1C — Draw Layer System
- Prompt file: `.github/prompts/w1c-explore-draw-layer.prompt.md`
- Agent: `Explore` (thorough)
- Output: `docs/w1c-draw-layer.md`

### Step 4: W1D — Mod Lifecycle Patterns
- Prompt file: `.github/prompts/w1d-explore-mod-patterns.prompt.md`
- Agent: `Explore` (medium)
- Output: `docs/w1d-mod-patterns.md`

---

## Wave 1 Review Gate

Before proceeding to Wave 2:

1. Read all four docs/ findings files
2. Check each "Confidence and Gaps" section
3. If any critical item has low confidence, re-run that agent with a more targeted follow-up prompt
4. Resolve any obvious conflicts between findings

---

## Wave 2 — Synthesis (can run in either order)

### Step 5: W2A — Resolve Open Questions
- Prompt file: `.github/prompts/w2a-update-spec.prompt.md`
- Agent: `Specification`
- Output: `docs/w2a-resolved-spec.md`

### Step 6: W2B — Task Plan
- Prompt file: `.github/prompts/w2b-task-plan.prompt.md`
- Agent: `Task Planner Instructions`
- Output: `docs/w2b-task-plan.md`

---

## Wave 2 Review Gate

Before proceeding to Wave 3:

1. Read `docs/w2a-resolved-spec.md` — verify all section 18 items are resolved
2. Read `docs/w2b-task-plan.md` — verify task order is logical and completion criteria are testable
3. If items remain unresolved in W2A, either accept the risk or run a targeted W1 follow-up

---

## Wave 3 — Implementation

### Step 7: W3 — Full Implementation
- Prompt file: `.github/prompts/w3-implement.prompt.md`
- Agent: `Software Engineer Agent`
- Output: source files + `docs/w3-implementation-summary.md`

---

## Post-Implementation

1. Run `mvn compile` to verify clean build
2. Run `mvn install` to deploy to mods folder
3. Launch game and navigate to settlement-selection screen
4. Validate against spec section 17 checklist
5. Review `docs/w3-implementation-summary.md` for deviations or unverified items

---

## File Inventory After Full Execution

```
docs/
  w1a-screen-class.md          ← Wave 1 research
  w1b-candidate-data.md        ← Wave 1 research
  w1c-draw-layer.md            ← Wave 1 research
  w1d-mod-patterns.md          ← Wave 1 research
  w2a-resolved-spec.md         ← Wave 2 synthesis
  w2b-task-plan.md             ← Wave 2 task plan
  w3-implementation-summary.md ← Wave 3 summary

.github/prompts/
  orchestration-guide.prompt.md    ← this file
  w1a-explore-screen-class.prompt.md
  w1b-explore-candidate-data.prompt.md
  w1c-explore-draw-layer.prompt.md
  w1d-explore-mod-patterns.prompt.md
  w2a-update-spec.prompt.md
  w2b-task-plan.prompt.md
  w3-implement.prompt.md

src/main/java/mod/<modname>/      ← Wave 3 output
specs/
  syx_v70_settlement_candidate_filter_overlay_spec.md  ← original, unchanged
```
