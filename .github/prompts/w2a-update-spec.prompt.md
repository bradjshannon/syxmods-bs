---
description: "Wave 2A — Update spec with Wave 1 research findings (runs after all W1 agents complete)"
mode: agent
---

You are a specification writer. Wave 1 research agents have completed their findings. Your job is to resolve the open implementation questions in the spec using those findings, and produce a concrete resolved addendum.

---

## Your Task

Read the original spec and all four Wave 1 findings files. Produce a resolved addendum that replaces the ambiguous open questions in spec section 18 with concrete, actionable answers backed by the research.

Do NOT rewrite the entire spec. Write only the addendum document.

---

## Sources to Read (all of them)

1. Original spec: `specs/syx_v70_settlement_candidate_filter_overlay_spec.md`
   - Focus especially on sections 5, 8, 9, 14, and 18.

2. Wave 1 findings:
   - `docs/w1a-screen-class.md` — screen class, hook points, regenerate button UI
   - `docs/w1b-candidate-data.md` — candidate site data model, resource fields, adjacency, climate
   - `docs/w1c-draw-layer.md` — draw layers, marker rendering API, renderer type
   - `docs/w1d-mod-patterns.md` — mod lifecycle, UI injection, render hook, event patterns

---

## What to Resolve

For each item in spec section 18 ("Open Implementation Decisions for Agent"), provide a concrete resolution backed by the Wave 1 findings:

- exact class and method names in v70
- exact candidate-site resource list
- exact marker style and draw layer
- exact UI widget classes and layout dimensions (or best available)
- whether filter settings survive geography reroll vs. full screen lifetime
- safest hook point for geography regeneration completion
- whether the regenerate-geography box can be extended directly or requires adjacent panel
- whether candidate IDs or coordinates are the better stable render anchor

Also resolve the additional open questions from Wave 1 Confidence/Gaps sections.

Where Wave 1 findings conflict or are uncertain, flag the conflict explicitly and recommend the safer option.

---

## Output

Write your output to: `docs/w2a-resolved-spec.md`

Structure the file as follows:

```
# W2A: Resolved Implementation Decisions

## Resolution of Spec Section 18

### Screen Class
- Resolved class name:
- Source: [cite w1a finding]
- Confidence: high / medium / low

### Candidate Site Data Model
- Resolved resource fields (name, type, unit):
- Resolved adjacency fields:
- Resolved climate access pattern (how to enumerate climates and read display names at runtime):
- Source: [cite w1b finding]
- Confidence: high / medium / low

### Marker Style and Draw Layer
- Resolved layer:
- Resolved drawing API (class + method):
- Resolved coordinate mapping:
- Source: [cite w1c finding]
- Confidence: high / medium / low

### UI Integration Approach
- Resolved approach (extend existing box / adjacent panel):
- Resolved widget classes to use:
- Source: [cite w1a + w1d findings]
- Confidence: high / medium / low

### Hook Point for Geography Regeneration
- Resolved hook (class + method/callback):
- Source: [cite w1a + w1d findings]
- Confidence: high / medium / low

### Render Hook Entry Point
- Resolved interface + method to implement:
- Source: [cite w1c + w1d findings]
- Confidence: high / medium / low

### Site Identity / Render Anchor
- Resolved: coordinate vs. ID, and type:
- Source: [cite w1b finding]
- Confidence: high / medium / low

### Filter State Lifetime
- Resolved: survives reroll only / full screen lifetime:
- Reasoning:

## Unresolved Items
List anything that could not be resolved from Wave 1 findings, and what additional investigation is needed.

## Conflicts Between Wave 1 Findings
List any contradictions between w1a/w1b/w1c/w1d and how they were adjudicated.
```
