# W2A: Resolved Implementation Decisions

Resolves all open questions from spec section 18, using Wave 1 findings.

---

## Resolution of Spec Section 18

---

### Screen Class

- **Resolved class name:** `view.world.generator.WorldViewGenerator`
- **Resolved sub-stage:** `view.world.generator.StageCapitol` (package-private inner class of the same package; not accessible from mod code without reflection)
- **How the sub-stage activates:** `WorldViewGenerator.set()` selects `StageCapitol` when `WORLD.GEN().playerX < 0` (no capital placed yet)
- **Source:** w1a-screen-class.md — Screen Class section
- **Confidence:** High

---

### Candidate Site Data Model

**Resources**
- Resource types are **data-driven**, not hardcoded. Enumerate at runtime via `RESOURCES.minables().all()` → `LIST<Minable>`
- Localized display name: `minable.resource.name` (`CharSequence`)
- Stable index: `minable.index` (`int`)
- Value per site (0.0–1.0): `4.0 * Σ(info.get(terrain).getD() * m.terrain(terrain))` for all `TERRAIN` in `TERRAINS.ALL()`
  - `info` is a `WorldTerrainInfo` initialized with `info.initCity(tx1, ty1)`
- Source: w1b-candidate-data.md — Resource Fields section
- **Confidence:** High

**Adjacency**
- River: `WORLD.WATER().RIVER.is(int tx, int ty)` — per-tile boolean
- Mountain: `WORLD.MOUNTAIN().coversTile(int tx, int ty)` — per-tile boolean
- Ocean: `WORLD.WATER().has.is(int tx, int ty) && !WORLD.WATER().RIVER.is(int tx, int ty)` — water that is not a river tile
- For the 3×3 footprint: scan the center tile `(tx1+1, ty1+1)` and the surrounding 5×5 ring for adjacency checks; at minimum scan the center tile
- Source: w1b-candidate-data.md — Adjacency Fields section
- **Confidence:** High (ocean check is approximation — see gap note below)

**Climate**
- Access: `WORLD.CLIMATE().getter.get(int cx, int cy)` — returns a `CLIMATE` object (`CLIMATE extends INFO`)
- Enumerate all climates: `CLIMATES.ALL()` at runtime — returns `LIST<CLIMATE>`; count = 3 in v70 (Cold, Temperate, Hot)
- Identity: `climate.index()` (`int`, 0-based stable index into `CLIMATES.ALL()`)
- Localized label: `climate.name` (`CharSequence`) — use this for all UI labels; do NOT hardcode strings
- Source: w1b-candidate-data.md — Climate Field section
- **Confidence:** High

---

### Marker Style and Draw Layer

- **Resolved layer:** `WORLD.OVERLAY().things` — public `EThings` instance on `WorldOverlays`
- **Resolved draw API:**
  ```java
  WORLD.OVERLAY().things.hover(int x1, int y1, int w, int h, COLOR color, boolean thick)
  ```
- **Resolved coordinate mapping:**
  ```java
  int worldX = tx1 * C.TILE_SIZE;
  int worldY = ty1 * C.TILE_SIZE;
  int worldW = WCentre.TILE_DIM * C.TILE_SIZE;  // 3 * C.TILE_SIZE
  int worldH = WCentre.TILE_DIM * C.TILE_SIZE;
  ```
  `EThings.render()` applies `data.offX1()` / `data.offY1()` scroll offsets internally — the mod passes world-pixel coordinates only.
- **Color recommendation:** `COLOR.GREEN200` for passing sites (`ColorImp(0, 255, 0)` — bright green visible against map terrain). `COLOR.GREEN100` exists but is `(0, 128, 0)` — too dark to read reliably over world tiles.
- **Critical behavior:** `EThings` clears its list at the end of every world render call. Markers **must** be re-added every frame in `SCRIPT_INSTANCE.render()`
- **Source:** w1c-draw-layer.md — Marker Draw Method and EThings API sections
- **Confidence:** High

---

### UI Integration Approach

- **Resolved approach:** Build a separate filter panel and draw it via raw `Renderer` calls in `SCRIPT_INSTANCE.render()`. Do NOT attempt to extend the existing `StageCapitol` panel directly.
- **Reason:** `StageCapitol` is package-private. Its `GPanel` and the `ToolConfig.addUI()` callback are anonymous inner classes — inaccessible without reflection. Reflection-based injection is fragile and explicitly marked medium-confidence in Wave 1 findings.
- **Resolved widget classes for MVP:**
  - Draw panel background using `Renderer` primitives
  - Text rendering using game font via `Renderer`
  - Mouse interaction handled in `SCRIPT_INSTANCE.hover(COORDINATE, boolean)` and `SCRIPT_INSTANCE.mouseClick(MButt)`
  - Checkboxes and threshold fields implemented as simple state + bounding-box hit detection
- **Layout anchor:**
  - Existing "Place Capital" panel: width=260, top-left approximately `(C.DIM().x()/2 - 130, 64)`
  - Filter panel: place to the **right** of the existing panel, same y=64 baseline, with a small gap
- **Source:** w1a-screen-class.md — Regenerate-Geography UI Box + Extensibility sections; w1c-draw-layer.md — UI Panel Draw Strategy section
- **Confidence:** Medium (raw renderer approach is confirmed feasible; exact Renderer text/rect API needs verification at compile time)

---

### Hook Point for Geography Regeneration

- **Resolved hook:** There is **no explicit post-regeneration callback** exposed to mod code.
- **Resolved strategy:** Poll `WORLD.GEN().seed` in `SCRIPT_INSTANCE.update(double ds)`
  ```java
  // In update():
  int currentSeed = WORLD.GEN().seed;
  if (currentSeed != cachedSeed) {
      cachedSeed = currentSeed;
      rebuildCandidateCache();
  }
  ```
- **Also:** On first entry to `StageCapitol` (detect via `VIEW.current() instanceof WorldViewGenerator && WORLD.GEN().playerX < 0`), trigger an initial cache build if cache is empty.
- **Filter state** (user's checkbox/threshold settings) is **preserved** across geography rerolls — only the candidate cache is invalidated, not the filter preferences.
- **Source:** w1a-screen-class.md — Geography Regeneration Hook section; w1d-mod-patterns.md — Lifecycle Hooks table
- **Confidence:** High (seed-polling is reliable; `WORLD.GEN().seed` is a public `int` field set on each regen)

---

### Render Hook Entry Point

- **Resolved interface:** `script.SCRIPT.SCRIPT_INSTANCE`
- **Resolved render method:** `render(Renderer r, float ds)`
- **Call site in engine:**
  ```java
  // In ScriptEngine.callback (called from VIEW.render() every frame):
  GAME.script().callback.render(r, ds);
  ```
  This fires **before** `WorldViewGenerator.render()` in the same frame, which means markers added to `EThings` in `SCRIPT_INSTANCE.render()` will be drawn by the subsequent world render.
- **Guard required:** Check `VIEW.current() instanceof WorldViewGenerator` and `WORLD.GEN().playerX < 0` at the top of `render()` to scope all work to the capital placement phase.
- **Source:** w1c-draw-layer.md — Render Execution Order; w1d-mod-patterns.md — Lifecycle Hooks
- **Confidence:** High

---

### Site Identity / Render Anchor

- **Resolved:** Use tile coordinates `(tx1, ty1)` — the top-left corner of the 3×3 capitol footprint
- **Type:** two `int` fields
- **Stability:** stable for the lifetime of a generated map; invalidated only when `WORLD.GEN().seed` changes
- **Validity check at scan time:** `WorldCentrePlacablity.terrain(tx1, ty1) == null` — returns `null` for valid sites
- **Scan stride:** iterate every tile (stride 1), not stride 3 — the game allows placement at any valid offset, not just aligned 3×3 blocks
- **Source:** w1b-candidate-data.md — Candidate Site Collection; w1a-screen-class.md — Candidate Site Access
- **Confidence:** High

---

## Additional Resolutions (from Wave 1 Confidence/Gaps)

### Filter State Lifetime
- Filter settings (checkbox states, threshold values) **survive geography rerolls** — preserve across `rebuildCandidateCache()` calls
- Filter settings are **not persisted across game sessions** in MVP (no `save()`/`load()` implementation required for MVP; can be added in a follow-up)
- Reset filter state only if the user explicitly clears it, not on map regen

### Resource Filter: Threshold Comparison Unit
- `resourcePercent` stored as `double` in range 0.0–1.0
- Threshold UI shows integer 0–100
- Comparison: `candidateResourcePercent >= (thresholdInt / 100.0)`

### UI panel — Does `VIEW.current()` need null-guard?
- Yes: guard with `VIEW.current() instanceof WorldViewGenerator` — handles null and wrong-phase cleanly in a single check

### Lombok dependency
- The current `pom.xml` does **not** include Lombok. Do not use `@NoArgsConstructor` — provide explicit public no-args constructors.

### Ocean Adjacency — known gap
- `WORLD.WATER().OCEAN` is a `WorldWater.OpenSet` type; its public API is not fully confirmed
- Safe approximation: a tile is "ocean-adjacent" if `WORLD.WATER().has.is(tx, ty)` is true and `WORLD.WATER().RIVER.is(tx, ty)` is false
- Scan center tile and immediate orthogonal neighbors (up/down/left/right) of the 3×3 block

### `WorldTerrainInfo` availability
- `WorldTerrainInfo` is in package `world.map.terrain` — accessible from mod code (public class)
- `WorldCentrePlacablity` is in package `world.map.regions.centre` — must verify access modifier at compile time; if package-private, use reflection or compute validity differently

---

## Conflicts and Flags

| Item | Conflict | Recommendation |
|---|---|---|
| UI injection vs. raw renderer | Reflection-based `GuiSection` injection works but is fragile across patches | Use raw Renderer for MVP; defer widget injection to a post-MVP iteration |
| `WorldCentrePlacablity` access | Class may be package-private | Attempt direct call first; if compile error, use `WORLD.MOUNTAIN/WATER/TERRAIN` primitives to replicate validity check manually |
| Candidate scan performance | Scanning every tile with `WorldTerrainInfo.initCity()` may be slow on large maps | Scan only for validity first, then build `WorldTerrainInfo` only for valid sites; cache aggressively |
