# Songs of Syx v70 Mod Spec — Settlement Candidate Filter Overlay

## 1. Purpose

Create a **v70-only**, **additive-only** mod for **Songs of Syx** that helps the player identify viable city founding locations during the map review / settlement-selection flow.

The problem being solved:

- The player must currently mouse over many possible settlement locations and read tooltip values one by one.
- There is no built-in memory, marking, or filtering of good candidate sites.
- For most strategies, only a relatively small subset of sites are viable.
- The mod should reduce repetitive scanning by marking candidate sites that satisfy user-selected rules.

This mod is **not** intended to alter world generation, site scoring, tooltip content, settlement rules, or save data.

---

## 2. Scope

### In scope

- Additive overlay on the map review / settlement-selection screen
- Additive UI controls displayed on that screen
- Rule-based filtering of candidate settlement locations
- Marking of passing candidates on the map
- Display of `passing candidates / total candidates`
- Session-local filter state
- Candidate-property caching after map generation

### Out of scope

- Any changes to world generation
- Any changes to site values
- Any changes to founding rules
- Any tooltip changes
- Any bookmarking/pinning system in MVP
- Any persistent scoring/ranking logic beyond pass/fail filtering
- Any support for versions other than v70
- Any use of the global mod settings panel for MVP runtime controls

---

## 3. Hard Requirements

1. **Version target**
   - v70 only

2. **Mod behavior**
   - additive overlay and additive UI only
   - no destructive replacement of gameplay logic unless technically unavoidable for screen integration

3. **UI placement**
   - controls must appear **within the map review / settlement-selection screen**
   - controls must **not** rely on the global mod settings panel
   - preferred placement is:
     - inside the existing transparent UI box that contains the regenerate-geography button, or
     - immediately adjacent to that box

4. **Marker behavior**
   - passing candidates get a marker
   - failing candidates get no marker in MVP
   - no marker display unless **at least one rule** is enabled

5. **Tooltip behavior**
   - no tooltip changes in MVP

6. **Bookmarking**
   - no bookmarking in MVP

7. **Status display**
   - panel must show ratio:
     - `passing candidates / total candidates`

---

## 4. User Experience Summary

After geography is generated and the settlement review screen is available:

- The mod builds a structured list of all candidate settlement sites and their relevant properties.
- The user can enable rules using checkboxes in an on-screen filter panel.
- When one or more rules are enabled, the mod evaluates all candidate sites against the active rules.
- Candidate sites that pass are marked on the map.
- The panel shows the number of passing candidates relative to the total number of candidates.
- When zero rules are enabled, no candidate markers are shown.

---

## 5. MVP UI Specification

## 5.1 Placement

Preferred placement:

- integrated into the transparent UI box that currently contains the regenerate-geography button
- or immediately adjacent to that box if integration is visually or technically cleaner

The implementation agent should determine the least fragile placement based on actual v70 screen code.

## 5.2 Panel contents

The panel should contain:

1. Regenerate geography button (existing vanilla element, unchanged if possible)
2. Resource filter section
3. Adjacency filter section
4. Climate filter section
5. Status line showing:
   - `X / Y pass`

No advanced window system is required for MVP unless necessary for layout.

## 5.3 Resource rules

For each supported location resource:

- one checkbox to enable the rule
- one numeric field, stepper, or equivalent input for minimum `%` threshold

Semantics:

- unchecked = ignore this resource rule
- checked = candidate must satisfy:
  - `candidate.resourcePercent >= configuredThreshold`

Example row:

- `[x] Clay   [100]`

The list of resources is discovered at runtime by iterating `RESOURCES.minables().all()`. The checkbox label for each resource uses `minable.resource.name` (the game's own localized `CharSequence`). Do not hardcode resource names.

## 5.4 Adjacency rules

For each of the following booleans:

- ocean
- river
- mountain

Use **one checkbox per property**.

Semantics:

- unchecked = ignore this adjacency property
- checked = candidate **must have** this adjacency

Examples:

- `[x] River`
- `[ ] Ocean`
- `[x] Mountain`

Important:
- MVP does **not** support exclusion rules such as “must not be adjacent to ocean”
- A checkbox here means only “require”

## 5.5 Climate rules

Iterate `CLIMATES.ALL()` at runtime to produce one checkbox per climate. The checkbox label text is `climate.name` — the game's own localized `CharSequence`. Do not hardcode climate names.

Semantics:

- if one or more climate boxes are checked:
  - candidate climate must be one of the checked climates
- if no climate boxes are checked:
  - climate is ignored

Example (labels will reflect actual game localization, e.g. the three climates may appear as "Cold", "Temperate", "Hot"):

- `[x] Cold`
- `[x] Temperate`
- `[ ] Hot`

## 5.6 Rule activation rule

Markers must be shown **only when at least one rule is enabled**.

A rule counts as enabled if any of the following is true:

- any resource checkbox is checked
- any adjacency checkbox is checked
- any climate checkbox is checked

If zero rules are enabled:

- show no candidate markers
- status line may still display `0 / total` or may display total only, at implementer discretion
- recommended display: `0 / total pass`

---

## 6. Filter Semantics

## 6.1 Overall logic

A candidate site passes if all enabled rule groups pass.

Equivalent logic:

- all enabled resource rules must pass
- all checked adjacency rules must pass
- climate rule must pass if any climate boxes are checked

This is:

- **AND** across categories
- **AND** across enabled resource rules
- **AND** across checked adjacency requirements
- **OR** within the checked climate set

## 6.2 Formal behavior

A candidate passes iff:

1. For every resource rule with `enabled = true`:
   - `candidate.resourcePercent >= threshold`

2. For every adjacency checkbox that is checked:
   - corresponding candidate adjacency boolean must be `true`

3. For climate:
   - if no climate checkboxes are checked: climate passes automatically
   - otherwise candidate climate must match one of the checked climates

---

## 7. Candidate Cache Architecture

## 7.1 Required approach

After map generation, create a structured in-memory list of all candidate settlement sites and their relevant properties.

This cache is the basis for filtering and marker display.

The cache should be rebuilt when the geography / settlement candidate map changes.

It should **not** be rebuilt merely because the user changes filter settings.

## 7.2 Reason for this approach

This is preferred because:

- all candidates may need to be evaluated repeatedly as the user toggles rules
- the mod should not depend on repeated tooltip hover or hover-derived state
- filtering should remain responsive
- UI evaluation should be decoupled from rendering frequency

## 7.3 Cache lifecycle

### Build cache when:
- a new geography / candidate set is generated
- the regenerate-geography action completes
- any screen flow recreates the candidate site set

### Reuse cache when:
- any filter checkbox changes
- any resource threshold changes
- markers need redraw
- status count needs update

### Invalidate cache when:
- the underlying candidate site set changes
- the player exits the screen, if screen-local state is preferred

---

## 8. Candidate Data Model

The final implementation may use different actual types/names depending on game code, but conceptually each candidate entry should include:

```text
CandidateSite
- id or stable reference
- map coordinate / render coordinate
- resource percent values for each supported resource
- adjacentOcean : boolean
- adjacentRiver : boolean
- adjacentMountain : boolean
- climate : CLIMATE  // game object reference; identity via CLIMATES.ALL() index
```

A conceptual container:

```text
List<CandidateSite> candidateSites
```

## 8.1 Source of truth

The implementing agent should obtain these values from:

- the underlying candidate/site model, or
- the same evaluation path used by the vanilla settlement screen

The implementation should **not** treat the tooltip as the source of truth unless that is the only accessible practical path.

Goal:
- marker behavior should agree with the values shown by the vanilla screen

---

## 9. Marker Specification

## 9.1 MVP marker behavior

For candidate sites that pass all active rules:

- draw a marker at the site location

For candidate sites that fail:

- draw nothing in MVP

## 9.2 Marker constraints

MVP marker should be:

- simple
- visually distinct
- low clutter
- additive only
- not dependent on tooltip changes

Possible acceptable marker styles:

- small ring
- dot
- icon
- highlight halo

The implementation agent should choose a style that is readable and minimally intrusive.

## 9.3 Marker visibility condition

No markers should be drawn when zero rules are enabled.

---

## 10. Status Display

The panel must display:

- `passing / total`

Example:

- `12 / 184 pass`

Definitions:

- `total` = total number of candidate settlement sites in the current cached candidate list
- `passing` = count of candidate sites that satisfy all active rules

The count should update whenever:
- filter checkboxes change
- resource thresholds change
- the candidate cache is rebuilt

---

## 11. Session State

## 11.1 MVP requirement

Filter state should exist at runtime while the player is reviewing the current generated geography.

Recommended behavior:

- reroll/regenerate geography keeps current filter settings
- leaving the settlement-selection flow may discard filter settings
- no save-file persistence required for MVP

## 11.2 Optional non-MVP behavior

The implementing agent may optionally structure code so persistence can be added later, but MVP should not depend on it.

---

## 12. Non-Goals / Explicitly Unsupported MVP Features

The following are intentionally excluded from MVP:

- bookmark candidate sites
- add labels to candidate sites
- alter tooltips
- dim failing candidates
- rank candidates
- weighted scoring
- “must not have adjacency” rules
- filtering in screens other than the settlement-selection/map review screen
- compatibility guarantees for non-v70 versions

---

## 13. Performance Expectations

The candidate list should be small enough that full re-evaluation on filter changes is acceptable.

Recommended behavior:

- cache once per generated geography
- recompute pass/fail across cached candidates on each filter update
- avoid rebuilding underlying candidate-property extraction on every UI interaction
- avoid repeated heavy reflection or repeated tooltip-path execution if a cleaner direct data path exists

No hard numeric performance target is required for MVP, but the filter UI should feel immediate.

---

## 14. Implementation Guidance for Agentic Tools

The implementing agent is expected to determine final implementation details based on actual v70 code.

### 14.1 Determine actual integration point
Find the v70 screen/class responsible for:
- settlement-selection or map review UI
- rendering candidate sites
- handling geography regeneration
- exposing or computing site properties used by the vanilla tooltip

### 14.2 Prefer least-fragile additive hook
Choose the least fragile approach that achieves:
- extra UI controls on-screen
- marker rendering
- post-generation candidate caching

Possible implementation styles may include:
- injecting additional widgets into the existing screen
- patching/overriding the relevant screen class
- intercepting render/update flow in a minimally invasive way

### 14.3 Prefer vanilla-consistent data extraction
For each candidate site, derive:
- resource percentages
- adjacency booleans
- climate value

Use the same underlying data source/path the vanilla screen uses if practical.

### 14.4 Keep logic separated
Recommended internal separation:

- screen integration / panel rendering
- candidate cache extraction
- filter state model
- filter evaluation
- marker rendering

This separation is recommended even if the final code must live in a limited set of mod classes.

---

## 15. Suggested Internal Structures

These are conceptual only.

## 15.1 Filter state

```text
FilterState
- resourceRules: map<ResourceType, ResourceRule>
- requireOcean: boolean
- requireRiver: boolean
- requireMountain: boolean
- allowedClimates: boolean[]  // length = CLIMATES.ALL().size(); indexed by climate.index()
```

```text
ResourceRule
- enabled: boolean
- minPercent: integer
```

## 15.2 Derived helper flags

```text
hasAnyRuleEnabled =
    any(resourceRule.enabled)
    OR requireOcean
    OR requireRiver
    OR requireMountain
    OR any(allowedClimates)
```

Note:
- climate checkboxes count as enabled rules when checked

## 15.3 Candidate evaluation pseudocode

```text
function passes(candidate, filters):
    if not hasAnyRuleEnabled(filters):
        return false

    for each resourceRule in filters.resourceRules:
        if resourceRule.enabled:
            if candidate.resourcePercent[resourceRule.resource] < resourceRule.minPercent:
                return false

    if filters.requireOcean and not candidate.adjacentOcean:
        return false

    if filters.requireRiver and not candidate.adjacentRiver:
        return false

    if filters.requireMountain and not candidate.adjacentMountain:
        return false

    climateFilterActive = any(filters.allowedClimates)

    if climateFilterActive:
        if not filters.allowedClimates[candidate.climate.index()]:
            return false

    return true
```

---

## 16. UI Behavior Details

## 16.1 Resource threshold defaults
The implementation agent should choose practical defaults.

Recommended default behavior:
- resource checkbox unchecked
- threshold field populated with a sensible default numeric value, but ignored until checkbox is checked

## 16.2 Climate behavior with no boxes checked
If no climate boxes are checked:
- climate is ignored

## 16.3 Adjacency behavior with no boxes checked
If no adjacency boxes are checked:
- adjacency is ignored

## 16.4 Resource behavior with no boxes checked
If no resource boxes are checked:
- resource rules are ignored

## 16.5 Mixed rules
Any mix of:
- resource rules
- adjacency checkboxes
- climate checkboxes

must work together using the pass logic defined above.

---

## 17. Validation Checklist

The implementation should be considered successful if all of the following are true:

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

## 18. Open Implementation Decisions for Agent

The following should be resolved by the implementing agent based on actual code:

- exact class and method names in v70
- exact candidate-site resource list available in the screen/model
- exact marker style and draw layer
- exact UI widget classes and layout dimensions
- whether filter settings survive geography reroll only, or the entire screen lifetime
- safest hook point for geography regeneration completion
- whether the regenerate-geography box can be extended directly or whether an adjacent panel is cleaner
- whether candidate IDs or coordinates are the better stable render anchor

---

## 19. Final Intent

The core intent of this mod is simple:

- let the player specify a set of desirable location traits
- evaluate all candidate founding sites on the current generated map
- visually mark only the candidates that satisfy those rules
- avoid repetitive manual tooltip scanning

The implementation should remain as simple and additive as possible while achieving this.
