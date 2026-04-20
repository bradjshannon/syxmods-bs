# W2B: Implementation Task Plan

All class/method names reference v70 game sources confirmed in Wave 1. Follows spec section 14.4 separation of concerns.

---

## Package and Class Structure

**Package root:** `mod.capitalfilter`

| Class | Implements | Responsibility |
|---|---|---|
| `CapitalFilterScript` | `script.SCRIPT` | Mod entry point; discovered by game via reflection; creates instance |
| `CapitalFilterInstance` | `script.SCRIPT.SCRIPT_INSTANCE` | Per-session lifecycle: render, update, mouse, save/load |
| `CandidateSite` | — | Immutable data record: tile coords, resource percents, adjacency booleans, climate |
| `CandidateCache` | — | Scans world tiles; builds and holds `List<CandidateSite>`; owns rebuild trigger |
| `FilterState` | — | Mutable filter settings: resource rules, adjacency requires, climate allows |
| `ResourceRule` | — | Per-resource: enabled boolean + minPercent int |
| `FilterEvaluator` | — | Static utility: `passes(CandidateSite, FilterState)` |
| `FilterPanel` | — | Raw-renderer UI: draws panel, handles checkbox/threshold hit testing |
| `MarkerRenderer` | — | Calls `WORLD.OVERLAY().things.hover(...)` for each passing site each frame |

**Files to delete:** `src/main/java/mod/example/ExampleMod.java` — does not implement `script.SCRIPT`.

---

## Ordered Task List

---

### Task 1: Configure mod identity
- **Depends on:** none
- **What to implement:**
  1. Delete `src/main/java/mod/example/ExampleMod.java`
  2. In `pom.xml`: change `<groupId>` to `mod.capitalfilter`, `<artifactId>` to `capital-filter`, `<name>` to `Capital Filter`
  3. In `src/main/resources/mod-files/_Info.txt`: set `NAME: "Capital Filter"`, `VERSION: "0.1.0"`, `DESC: "Filter settlement candidates by resources, adjacency, and climate."`, `AUTHOR: "bradjshannon"`
- **Files to create/modify:** `pom.xml`, `_Info.txt`; delete `ExampleMod.java`
- **Completion criterion:** `mvn compile` succeeds with no source files in `mod/example/`

---

### Task 2: Mod entry point — `CapitalFilterScript`
- **Depends on:** Task 1
- **What to implement:**
  ```java
  package mod.capitalfilter;
  import script.SCRIPT;
  import util.info.INFO;

  public final class CapitalFilterScript implements SCRIPT {
      private final INFO info = new INFO("Capital Filter", "Filter settlement candidates.");
      public CapitalFilterScript() {}  // explicit no-args constructor; no Lombok
      @Override public CharSequence name() { return info.name; }
      @Override public CharSequence desc() { return info.desc; }
      @Override public boolean forceInit() { return true; }
      @Override public SCRIPT_INSTANCE createInstance() { return new CapitalFilterInstance(); }
  }
  ```
  `forceInit()` returns `true` so the mod activates automatically on all games without manual selection.
- **Files to create:** `src/main/java/mod/capitalfilter/CapitalFilterScript.java`
- **Completion criterion:** `mvn compile` succeeds

---

### Task 3: Mod instance skeleton — `CapitalFilterInstance`
- **Depends on:** Task 2
- **What to implement:** Full `SCRIPT_INSTANCE` implementation with all methods stubbed (no-ops). Include field stubs for `CandidateCache`, `FilterState`, `FilterPanel`, `MarkerRenderer` (all null for now). Add the `VIEW.current() instanceof WorldViewGenerator` guard to `render()` and `update()`.
- **Files to create:** `src/main/java/mod/capitalfilter/CapitalFilterInstance.java`
- **Completion criterion:** `mvn compile` succeeds; `forceInit=true` so mod appears active in a new game (verify at runtime later)

---

### Task 4: `CandidateSite` data record
- **Depends on:** Task 1
- **What to implement:**
  ```java
  package mod.capitalfilter;

  import init.type.CLIMATE;

  public final class CandidateSite {
      public final int tx1, ty1;           // top-left tile of 3×3 footprint
      public final double[] resourcePercents; // indexed by minable.index
      public final boolean adjacentRiver;
      public final boolean adjacentOcean;
      public final boolean adjacentMountain;
      public final CLIMATE climate;

      public CandidateSite(int tx1, int ty1, double[] resourcePercents,
                           boolean adjacentRiver, boolean adjacentOcean,
                           boolean adjacentMountain, CLIMATE climate) { ... }
  }
  ```
  `resourcePercents` length = `RESOURCES.minables().all().size()` at scan time.
- **Files to create:** `src/main/java/mod/capitalfilter/CandidateSite.java`
- **Completion criterion:** Compiles. No game API calls yet.

---

### Task 5: `FilterState` and `ResourceRule`
- **Depends on:** Task 4
- **What to implement:**
  ```java
  package mod.capitalfilter;

  public final class FilterState {
      public final ResourceRule[] resourceRules;  // length = RESOURCES.minables().all().size()
      public boolean requireRiver   = false;
      public boolean requireOcean   = false;
      public boolean requireMountain = false;
      public boolean[] allowedClimates;  // length = CLIMATES.ALL().size()

      // Initialised once RESOURCES and CLIMATES are loaded (call from CandidateCache or CapitalFilterInstance)
      public FilterState(int resourceCount, int climateCount) {
          resourceRules = new ResourceRule[resourceCount];
          for (int i = 0; i < resourceCount; i++) resourceRules[i] = new ResourceRule();
          allowedClimates = new boolean[climateCount];
      }

      public boolean hasAnyRuleEnabled() { ... }  // see spec section 15.2
  }

  public final class ResourceRule {
      public boolean enabled = false;
      public int minPercent = 25;  // default threshold
  }
  ```
- **Files to create:** `src/main/java/mod/capitalfilter/FilterState.java`, `src/main/java/mod/capitalfilter/ResourceRule.java`
- **Completion criterion:** Compiles. `hasAnyRuleEnabled()` returns correct results for edge cases (all false → false; one enabled → true).

---

### Task 6: `FilterEvaluator`
- **Depends on:** Tasks 4, 5
- **What to implement:** Pure static logic, no game API calls at evaluation time:
  ```java
  package mod.capitalfilter;

  public final class FilterEvaluator {
      public static boolean passes(CandidateSite site, FilterState filters) {
          if (!filters.hasAnyRuleEnabled()) return false;

          // Resource rules
          for (int i = 0; i < filters.resourceRules.length; i++) {
              ResourceRule rule = filters.resourceRules[i];
              if (rule.enabled && site.resourcePercents[i] < rule.minPercent / 100.0)
                  return false;
          }

          // Adjacency rules
          if (filters.requireRiver    && !site.adjacentRiver)    return false;
          if (filters.requireOcean    && !site.adjacentOcean)    return false;
          if (filters.requireMountain && !site.adjacentMountain) return false;

          // Climate rules
          boolean climateFilterActive = false;
          for (boolean b : filters.allowedClimates) if (b) { climateFilterActive = true; break; }
          if (climateFilterActive && !filters.allowedClimates[site.climate.index()])
              return false;

          return true;
      }
  }
  ```
- **Files to create:** `src/main/java/mod/capitalfilter/FilterEvaluator.java`
- **Completion criterion:** Compiles. Unit-testable: verify with two hardcoded `CandidateSite` values and a `FilterState`.

---

### Task 7: `CandidateCache` — scan and build
- **Depends on:** Tasks 4, 6
- **What to implement:**
  ```java
  package mod.capitalfilter;
  // Imports: world.*, init.*, game.*, snake2d.*

  public final class CandidateCache {
      private List<CandidateSite> sites = new ArrayList<>();
      private int lastSeed = Integer.MIN_VALUE;
      private int resourceCount;

      public void init() {
          resourceCount = RESOURCES.minables().all().size();
      }

      public void rebuildIfNeeded() {
          int seed = WORLD.GEN().seed;
          if (seed == lastSeed) return;
          lastSeed = seed;
          rebuild();
      }

      private void rebuild() {
          sites.clear();
          WorldTerrainInfo info = new WorldTerrainInfo();
          int W = WORLD.TWIDTH(), H = WORLD.THEIGHT();
          int dim = WCentre.TILE_DIM;
          for (int ty1 = 0; ty1 + dim <= H; ty1++) {
              for (int tx1 = 0; tx1 + dim <= W; tx1++) {
                  if (WorldCentrePlacablity.terrain(tx1, ty1) != null) continue;
                  info.initCity(tx1, ty1);
                  sites.add(buildSite(tx1, ty1, info));
              }
          }
      }
  }
  ```
  `buildSite()` computes all `CandidateSite` fields:
  - Resource percents: `4.0 * Σ(info.get(te).getD() * m.terrain(te))` for all `te` in `TERRAINS.ALL()`
  - River: scan center tile `(tx1+1, ty1+1)` and orthogonal neighbors for `WORLD.WATER().RIVER.is(tx, ty)`
  - Ocean: same scan, `WORLD.WATER().has.is(tx, ty) && !WORLD.WATER().RIVER.is(tx, ty)`
  - Mountain: scan surrounding ring for `WORLD.MOUNTAIN().coversTile(tx, ty)`
  - Climate: `WORLD.CLIMATE().getter.get(tx1+1, ty1+1)`

  **Note:** If `WorldCentrePlacablity` is package-private (compile error), implement the validity check directly:
  ```java
  // Fallback validity check if WorldCentrePlacablity is inaccessible:
  private static boolean isValidCapitalSite(int tx1, int ty1) {
      // Must be land, inside bounds, and have a clear orthogonal neighbor
      // Approximate with WORLD.TERRAIN() and boundary checks
  }
  ```
- **Files to create:** `src/main/java/mod/capitalfilter/CandidateCache.java`
- **Completion criterion:** `mvn compile` succeeds. In `CapitalFilterInstance.update()`, call `cache.rebuildIfNeeded()` when in StageCapitol — after a log statement, confirm in game console that rebuild fires once per regen.

---

### Task 8: `MarkerRenderer`
- **Depends on:** Tasks 6, 7
- **What to implement:**
  ```java
  package mod.capitalfilter;
  // Imports: world.WORLD, init.constant.C, world.map.regions.centre.WCentre, snake2d.util.color.COLOR

  public final class MarkerRenderer {
      public void render(List<CandidateSite> sites, FilterState filters) {
          if (!filters.hasAnyRuleEnabled()) return;
          int w = WCentre.TILE_DIM * C.TILE_SIZE;
          int h = WCentre.TILE_DIM * C.TILE_SIZE;
          for (CandidateSite site : sites) {
              if (FilterEvaluator.passes(site, filters)) {
                  WORLD.OVERLAY().things.hover(
                      site.tx1 * C.TILE_SIZE,
                      site.ty1 * C.TILE_SIZE,
                      w, h, COLOR.GREEN200, true  // GREEN200=(0,255,0); GREEN100=(0,128,0) is too dark to see on map
                  );
              }
          }
      }
  }
  ```
  Called from `CapitalFilterInstance.render()` every frame (EThings clears each frame — re-adding is required).
- **Files to create:** `src/main/java/mod/capitalfilter/MarkerRenderer.java`
- **Completion criterion:** Compiles. In-game: with one adjacency rule enabled, green boxes appear over valid candidates.

---

### Task 9: `FilterPanel` — scaffold and position
- **Depends on:** Tasks 5
- **What to implement:** A panel class that knows its screen bounding box and can draw a background. Position it to the right of the "Place Capital" panel:
  ```java
  // Existing panel: centered at (C.DIM().x()/2 - 130, 64), width=260
  // Filter panel: place at (C.DIM().x()/2 + 130 + GAP, 64), width=~240
  int panelX = C.DIM().x() / 2 + 130 + 8;  // 8px gap
  int panelY = 64;
  int panelW = 240;
  ```
  For MVP, draw a filled dark rectangle as background using the `Renderer` API. Determine the exact `Renderer` draw-rect call at compile time (check `snake2d.Renderer` for available fill/rect methods).
- **Files to create:** `src/main/java/mod/capitalfilter/FilterPanel.java`
- **Completion criterion:** Compiles. A dark rectangle appears to the right of the vanilla panel during capital placement.

---

### Task 10: `FilterPanel` — resource rule rows
- **Depends on:** Tasks 5, 9
- **What to implement:** For each `Minable m` in `RESOURCES.minables().all()`:
  - Draw a checkbox rect (unfilled = unchecked, filled = checked)
  - Draw `m.resource.name` label text next to checkbox
  - Draw a threshold number input (display current `minPercent`; +/− buttons or text)
  - Track bounding boxes for click hit-testing in `onMouseClick(int screenX, int screenY)`
- **Files to modify:** `FilterPanel.java`
- **Completion criterion:** Resource checkboxes render and toggle correctly on click. Threshold value updates on +/− click.

---

### Task 11: `FilterPanel` — adjacency rule rows
- **Depends on:** Tasks 5, 9
- **What to implement:** Three checkbox rows labeled `WORLD.WATER().RIVER` (use a hardcoded label "River" here — there is no `INFO` on these), `Ocean`, `Mountain`. Toggle `filters.requireRiver`, `requireOcean`, `requireMountain`.

  Note: River/ocean/mountain do not have game-localized names via an `INFO` object. Use English literals: "River", "Ocean", "Mountain". These are geographical terms, not game-data labels, so hardcoding is acceptable here.
- **Files to modify:** `FilterPanel.java`
- **Completion criterion:** Adjacency checkboxes render and toggle.

---

### Task 12: `FilterPanel` — climate rule rows
- **Depends on:** Tasks 5, 9
- **What to implement:** Iterate `CLIMATES.ALL()`:
  ```java
  for (int i = 0; i < CLIMATES.ALL().size(); i++) {
      CLIMATE c = CLIMATES.ALL().get(i);
      // draw checkbox
      // draw c.name as label
      // on click: filters.allowedClimates[i] = !filters.allowedClimates[i]
  }
  ```
  Labels are fully localized via `c.name`.
- **Files to modify:** `FilterPanel.java`
- **Completion criterion:** Climate checkboxes render with game-localized names and toggle correctly.

---

### Task 13: `FilterPanel` — status line
- **Depends on:** Tasks 6, 7, 9
- **What to implement:** Count passing and total sites each frame:
  ```java
  int passing = 0, total = cache.getSites().size();
  if (filters.hasAnyRuleEnabled()) {
      for (CandidateSite site : cache.getSites()) {
          if (FilterEvaluator.passes(site, filters)) passing++;
      }
  }
  // Render: "12 / 347 pass"
  ```
  Draw the status string at the bottom of the filter panel.
- **Files to modify:** `FilterPanel.java`, `CandidateCache.java` (add `getSites()` accessor)
- **Completion criterion:** Status line shows correct counts. When zero rules are enabled, displays `0 / N pass` (or `N total`).

---

### Task 14: Wire everything into `CapitalFilterInstance`
- **Depends on:** Tasks 3, 7, 8, 9–13
- **What to implement:** Full wiring in `CapitalFilterInstance`:
  ```java
  // Fields
  private CandidateCache cache;
  private FilterState filters;
  private FilterPanel panel;
  private MarkerRenderer markers;
  private boolean initialized = false;

  // In update(double ds):
  if (!(VIEW.current() instanceof WorldViewGenerator)) return;
  if (WORLD.GEN().playerX >= 0) return;  // capital already placed
  if (!initialized) { initComponents(); initialized = true; }
  cache.rebuildIfNeeded();

  // In render(Renderer r, float ds):
  if (!(VIEW.current() instanceof WorldViewGenerator)) return;
  if (WORLD.GEN().playerX >= 0) return;
  markers.render(cache.getSites(), filters);
  panel.render(r, cache, filters);

  // In hover(COORDINATE mCoo, boolean moved):
  if (panel != null) panel.onMouseHover(mCoo.x(), mCoo.y());

  // In mouseClick(MButt button):
  if (panel != null && button == MButt.LEFT) panel.onMouseClick(VIEW.mouse().x(), VIEW.mouse().y());
  ```
  `initComponents()` creates `CandidateCache`, calls `cache.init()` and `cache.rebuildIfNeeded()`, creates `FilterState` with correct array sizes, creates `FilterPanel` and `MarkerRenderer`.
- **Files to modify:** `CapitalFilterInstance.java`
- **Completion criterion:** `mvn compile` succeeds. End-to-end smoke test in-game: filter panel appears, checkboxes toggle, markers appear/disappear.

---

### Task 15: Wire geography regeneration → cache rebuild
- **Depends on:** Task 14
- **What to implement:** The seed-polling in `update()` (already part of `CandidateCache.rebuildIfNeeded()` in Task 7) handles this automatically. Verify that after clicking the vanilla "Regenerate" button:
  1. `WORLD.GEN().seed` changes
  2. `CandidateCache.rebuildIfNeeded()` detects the change on the next `update()` tick
  3. `cache.getSites()` is replaced with new candidates
  4. Markers update on the next `render()` call
  5. Filter state (checkboxes, thresholds) is preserved across the regen
- **Files to modify:** none (verification task)
- **Completion criterion:** After clicking "Regenerate" in-game, markers refresh with new candidates; user's filter settings are unchanged.

---

### Task 16: End-to-end validation — spec section 17 checklist
- **Depends on:** Task 15
- **What to verify (all must pass before declaring done):**
  1. ☐ Mod operates on v70 — loads without crash
  2. ☐ UI controls appear on settlement-selection screen during capital placement
  3. ☐ Controls are NOT in a global mod settings panel
  4. ☐ Candidate data is cached (only rebuilt on seed change, not on every filter toggle)
  5. ☐ Toggling a checkbox immediately updates which sites are marked
  6. ☐ Passing candidates show green outline markers
  7. ☐ No markers when zero rules are enabled
  8. ☐ Vanilla tooltip content is unchanged
  9. ☐ Status line shows `passing / total`
  10. ☐ Regenerating geography triggers cache rebuild; filter settings preserved
  11. ☐ No vanilla game behavior is broken (can still place capital, regen still works)
- **Files to create:** none (checklist is in `docs/w3-implementation-summary.md` per Wave 3 instructions)
- **Completion criterion:** All 11 checklist items pass.

---

## Risks and Mitigations

| Risk | Likelihood | Mitigation |
|---|---|---|
| `WorldCentrePlacablity` is package-private | Medium | Attempt direct use first; if compile error, replicate validity logic using `WORLD.TERRAIN()`, `WTRAV.isGoodLandTile()`, boundary checks |
| `Renderer` text/rect draw API unknown | Medium | Examine `snake2d.Renderer` source at compile time; fall back to `SPRITES` sprite-based drawing if primitive rect is unavailable |
| Candidate scan is slow on large maps | Low | Profile at ~256×128 default map; if >500ms, add stride-3 scan optimization or background thread (not recommended for MVP) |
| `WORLD.GEN().seed` is 0 initially | Low | Initialize `lastSeed` to `Integer.MIN_VALUE` to force first build; guard `rebuild()` with `WORLD.THEIGHT() > 0` |
| `VIEW.current()` null on first frame | Low | `instanceof` check handles null cleanly (null is not an instance of anything) |
| `COLOR.GREEN100` constant name incorrect | Low | Check `snake2d.util.color.COLOR` static fields at compile time; substitute nearest green if name differs |
| `WCentre.TILE_DIM` not public | Low | If inaccessible, hardcode 3 with a comment citing source |

---

## Build and Deploy Verification

After each major task group:

```powershell
$env:JAVA_HOME = "C:\dev\jdk21"
$env:Path = "C:\dev\jdk21\bin;C:\dev\apache-maven-3.9.14\bin;$env:Path"

# Compile only (fast iteration)
mvn compile

# Full build + deploy to game mods folder
mvn install
```

Game mods folder after install:
```
%APPDATA%\songsofsyx\mods\Capital Filter\V70\script\capital-filter-0.1.0.jar
%APPDATA%\songsofsyx\mods\Capital Filter\_Info.txt
```

In-game: launch the game, start a new game (or load one with `forceInit=true`), enter world generation. The "Capital Filter" panel should appear to the right of the "Place Capital" panel.
