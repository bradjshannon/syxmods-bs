# W1A Findings: Screen Class and UI Hook Points

## Screen Class
- **Fully qualified class name:** `view.world.generator.WorldViewGenerator`
- **Source file path in JAR:** `view/world/generator/WorldViewGenerator.java`
- **Parent class (immediate):** `VIEW.ViewSubSimple`
- **Parent class chain:** `WorldViewGenerator → VIEW.ViewSubSimple → VIEW` (inner class hierarchy)

The settlement-selection stage is a **package-private inner class** `StageCapitol` within `view.world.generator`. It is instantiated by `WorldViewGenerator.set()` when `WORLD.GEN().playerX < 0` (no capital placed yet).

### Stage Lifecycle
`WorldViewGenerator.set()` selects the active stage in order:
1. `StagePickRace` — race not yet selected
2. `StageVisuals` — visual profile not yet created
3. `StagePickTitles` — titles not yet selected
4. `StageTerrain` — terrain not yet generated
5. **`StageCapitol`** — capital not yet placed (`playerX < 0`) ← **this is the target**
6. `StageFinish` — capital placed, ready to start

---

## Render / Draw Methods

`WorldViewGenerator` overrides:
```java
protected void render(Renderer r, float ds, boolean hide)
```
Implementation:
```java
window.crop(uiManager.viewPort());
GAME.world().render(r, ds, window.zoomout(), window.pixels(), window.view().x1(), window.view().y1());
```

`VIEW.ViewSubSimple` abstract method signature:
```java
protected abstract void render(Renderer r, float ds, boolean hide)
```

`VIEW` (top-level) render loop calls each frame:
```java
GAME.script().callback.render(r, ds)   // ← SCRIPT_INSTANCE render hook fires here
current.render(r, ds, false)            // ← WorldViewGenerator render fires here
```

**Critical finding:** `GAME.script().callback.render(r, ds)` is called every frame from `VIEW.render()`. This fires during `StageCapitol` because the `GAME` object is created before `VIEW` (and thus before `WorldViewGenerator` is shown). The `SCRIPT_INSTANCE.render()` hook IS active during settlement placement.

---

## Geography Regeneration Hook

### How regeneration is triggered
The regenerate-geography button in `StageCapitol` calls:
```java
WORLD.GEN().seed = RND.rInt(Integer.MAX_VALUE);
WORLD.TERRAIN().saver().generate(WorldViewGenerator.loadPrint);
WORLD.LANDMARKS().saver().generate(WorldViewGenerator.loadPrint);
WorldViewGenerator.loadPrint.exe();
MINIMAP().repaint();
```

There is also a static method `StageCapitol.generate()` which performs the full post-placement generation sequence:
```java
static void generate()  // called after capitol tile is chosen
static void regenerate()  // regenerates and preserves playerX/playerY
```

### What fires on completion
`WorldViewGenerator.loadPrint` is a static `ACTION` that calls `SPRITES.loader().print(...)`. There is **no explicit post-completion callback**. Generation happens synchronously and `loadPrint.exe()` is called at each stage checkpoint.

The `generate()` method in `StageCapitol` ends with `WORLD.RD().saver().generate(...)` — after this returns, generation is complete.

### Hook strategy for the mod
The mod cannot intercept the button click directly without replacing `StageCapitol`. The practical approach is:
1. In `SCRIPT_INSTANCE.update(double ds)`, compare a cached world seed/state to `WORLD.GEN().seed` each tick. If changed, rebuild the candidate cache.
2. Alternatively, cache the terrain generation version and detect it in `render()` before drawing markers.

---

## Regenerate-Geography UI Box

### Structure
`StageCapitol` creates:
```java
GuiSection butts = new GuiSection();
// ... adds buttons to butts ...
GPanel p = new GPanel(260, butts.body().height()).setButt();
p.setTitle(¤¤name);  // "Place Capital"
p.inner().set(butts);
butts.add(p);
butts.moveLastToBack();
butts.body().moveY1(64).centerX(C.DIM());
```

The panel is a `util.gui.panel.GPanel` (width=260) containing a `snake2d.util.gui.GuiSection` of buttons. It is positioned at y=64, horizontally centered.

### How it is added to the screen
Via `ToolConfig`:
```java
ToolConfig fixed = new ToolConfig() {
    @Override
    public void addUI(LISTE<RENDEROBJ> uis) {
        uis.add(butts);
    }
};
stages.tools.place(t, fixed);  // stages = WorldViewGenerator
```

### Extensibility
`StageCapitol` is **package-private** (`class StageCapitol`) — it cannot be subclassed or directly accessed from mod code. The `ToolConfig.addUI()` callback is also an anonymous inner class.

**Mod injection options:**
- Use reflection to access `WorldViewGenerator.tools` and intercept the `ToolConfig` or add additional UI elements after `set()` is called
- Create a secondary `GuiSection` that is separately registered with `WorldViewGenerator`'s uiManager via reflection
- The cleanest approach: detect `VIEW.current() instanceof WorldViewGenerator` in `SCRIPT_INSTANCE.render()` and draw a custom panel using the raw `Renderer`

---

## Candidate Site Access

- The screen class does NOT hold a pre-computed list of candidate sites.
- `UIWorldToolCapitolPlaceInfo` (package `view.world.generator.tools`) computes site properties on-the-fly using `WorldTerrainInfo.initCity(tx1, ty1)` when the player hovers a location.
- There is no existing enumerated list of valid candidate locations.
- **The mod must scan all map tiles**, calling `WorldCentrePlacablity.terrain(tx1, ty1)` to test validity, and `WorldTerrainInfo.initCity(tx1, ty1)` to extract properties.
- Valid tile stride: `WCentre.TILE_DIM = 3` tiles per capitol slot. Scan in steps of 3.

---

## Confidence and Gaps

### High confidence
- `WorldViewGenerator` is the correct screen class
- `StageCapitol` is the correct sub-stage (package-private)
- `SCRIPT_INSTANCE.render(Renderer r, float ds)` fires during `StageCapitol`
- `SCRIPT_INSTANCE.update(double ds)` fires every game tick during `StageCapitol`
- The regenerate button triggers `WORLD.TERRAIN().saver().generate(...)` synchronously
- `WCentre.TILE_DIM = 3` is the tile footprint of a capitol slot
- `WorldCentrePlacablity.terrain(tx1, ty1)` returns `null` for valid sites
- The GPanel is width=260, positioned at y=64, centered horizontally

### Uncertain / needs W1B + W1D cross-reference
- Whether `uiManager` on `WorldViewGenerator` / `ViewSubSimple` has a public API for adding additional panels — need to check visibility
- Whether reflection-based UI injection is stable across minor v70 patches
- The exact screen-space coordinate formula for mapping tile positions to render pixels (needs W1C for coordinate transform details)
- Whether `WORLD.GEN().seed` changes on every regeneration (suitable as cache invalidation key)
