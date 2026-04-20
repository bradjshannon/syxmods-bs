# W1C Findings: Draw Layer System

## Marker Draw Method — Recommended Approach

**Use `WORLD.OVERLAY().things.hover(...)` to draw candidate site markers.**

This is the correct approach because:
- `WORLD.OVERLAY()` returns a `WorldOverlays` instance (fully public)
- `WorldOverlays.things` is `public final EThings`
- `EThings.hover(int x1, int y1, int w, int h, COLOR color, boolean thick)` is public
- The world renderer automatically calls `things.render(Renderer r, ShadowBatch s, RenderData data)` each frame during `GAME.world().render()`, using the correct scroll offsets and zoom

### Call site in the mod
```java
// In SCRIPT_INSTANCE.render(Renderer r, float ds):
if (!(VIEW.current() instanceof WorldViewGenerator)) return;  // guard
if (WORLD.GEN().playerX >= 0) return;  // capital already placed

for (CandidateSite site : passingCandidates) {
    int worldX = site.tx1 * C.TILE_SIZE;
    int worldY = site.ty1 * C.TILE_SIZE;
    int worldW = WCentre.TILE_DIM * C.TILE_SIZE;
    int worldH = WCentre.TILE_DIM * C.TILE_SIZE;
    WORLD.OVERLAY().things.hover(worldX, worldY, worldW, worldH, PASS_COLOR, true);
}
```

---

## Coordinate System

### World pixel coordinates
```java
int worldPixelX = tileX * C.TILE_SIZE;
int worldPixelY = tileY * C.TILE_SIZE;
```
`C.TILE_SIZE` is the pixel size of one world tile (compile-time constant, typically 16). This is the coordinate system used by `EThings` and all world overlay classes.

### Coordinate transform (USED INTERNALLY by EThings — not needed by mod)
The `EThings.render()` method applies the scroll offset internally:
```java
// Inside EThings.render():
SPRITES.cons().BIG.outline.renderBox(r, e.x1() - data.offX1(), e.y1() - data.offY1(), e.width(), e.height());
```
Where `data.offX1()` and `data.offY1()` are the world scroll offsets provided by `RenderData`. **The mod does NOT need to compute screen coordinates.** World tile coordinates `× C.TILE_SIZE` are sufficient.

### GameWindow access (for reference — NOT needed for overlay approach)
If the mod ever needs raw screen coordinates (e.g., for drawing outside the world viewport), the formula would be:
```java
int screenX = ((tileX * C.TILE_SIZE - window.pixels().x1()) >> window.zoomout()) + window.view().x1();
int screenY = ((tileY * C.TILE_SIZE - window.pixels().y1()) >> window.zoomout()) + window.view().y1();
```
Accessing `window` from a mod requires reflection since it's a package-private field of `WorldViewGenerator`.

---

## EThings API

```java
// WorldOverlays.things — the primary rectangle overlay accumulator
public final class EThings {

    // Add a world-pixel rectangle for this frame only
    public void add(int x1, int y1, int w, int h, COLOR color, boolean thick);

    // Same as add()
    public void hover(int x1, int y1, int w, int h, COLOR color, boolean thick);

    // Convenience overload using a RECTANGLE with margin
    public void hover(RECTANGLE body, COLOR color, boolean thick, int margin);

    // (Called by world renderer — not by mod code)
    public void render(Renderer r, ShadowBatch s, RenderData data);

    // (Called by world renderer — clears all pending items each frame)
    void clear();
}
```

**Critical:** `EThings` clears its list (`ai = 0`) at the end of every `render()` call. The mod MUST add markers every frame (i.e., every call to `SCRIPT_INSTANCE.render()`).

---

## Render Execution Order

Each frame during `StageCapitol`:
1. `GAME.script().callback.render(r, ds)` — fires all `SCRIPT_INSTANCE.render()` calls
2. `VIEW.current().render(r, ds, false)` — `WorldViewGenerator.render()` fires
3. Inside `WorldViewGenerator.render()`: `GAME.world().render(r, ds, zoomout, pixels, viewX, viewY)`
4. Inside `GAME.world().render()`: tile layers, then overlays (including `WORLD.OVERLAY().things.render()`)

**Conclusion:** The mod should call `WORLD.OVERLAY().things.hover()` in step 1 (`SCRIPT_INSTANCE.render()`). The markers will be drawn in step 4 with correct scroll/zoom offsets. This is the designed pattern (identical to how `EThings.hover(WEntity e)` works in the game's own code).

---

## COLOR Constants

`snake2d.util.color.COLOR` provides static color bindings:
```java
COLOR.GREEN100         // bright green — suitable for "passing" candidates
COLOR.RED100           // red — could be used for debugging "failing" candidates
COLOR.WHITE100         // white
COLOR.YELLOW100        // yellow
```

For a custom blended color at reduced opacity:
```java
// Example: 70% green
init.sprite.SPRITES   // texture-based colors
util.colors.GCOLOR    // game-specific color palette (GCOLOR.MAP(), GCOLOR.UI(), etc.)
snake2d.util.color.OPACITY  // opacity control: OPACITY.set(color, alpha)
```

**Recommendation:** Use `COLOR.GREEN100` for passing sites and `COLOR.WHITE100` or skip drawing failing sites entirely (showing only passing sites reduces visual clutter).

---

## UI Panel Draw Strategy

The filter panel (checkboxes, thresholds, count display) has two options:

### Option A: Raw Renderer in SCRIPT_INSTANCE.render() (simpler, no injection needed)
Draw the panel directly in `SCRIPT_INSTANCE.render()` using `Renderer` drawing primitives. Handle mouse interactions in `SCRIPT_INSTANCE.hover()` and `SCRIPT_INSTANCE.mouseClick()`.

- Pros: No reflection; fully self-contained
- Cons: More manual UI code; text rendering requires font metrics

The Renderer is `snake2d.Renderer`. Screen space in `SCRIPT_INSTANCE.render()` uses absolute screen pixel coordinates (0,0 = top-left). Panel position can be hardcoded to match the `StageCapitol` UI box position (e.g., same y=64 offset, placed to the right of the existing panel).

### Option B: Inject GuiSection into WorldViewGenerator.uiManager (cleaner, but requires reflection)
Create a `util.gui.misc.gui.GuiSection` or `GPanel` and inject it into `WorldViewGenerator`'s `uiManager` using reflection after the view is set. Then the game handles mouse events and rendering automatically.

Access path:
```java
WorldViewGenerator wvg = (WorldViewGenerator) VIEW.current();
// Reflection to access package-private field "uiManager"
Field f = WorldViewGenerator.class.getDeclaredField("uiManager");
f.setAccessible(true);
UIManager mgr = (UIManager) f.get(wvg);
mgr.add(filterPanel);  // or equivalent
```

- Pros: UI is handled by game's widget system (hover, click, drag automatically work)
- Cons: Fragile across game updates; field name may differ

**Recommendation for MVP:** Use Option A (raw renderer). Option B can be pursued if the rendering complexity is too high.

---

## SPRITE_RENDERER / Shadow Batch Note

The `EThings.render()` draws outlines using `SPRITES.cons().BIG.outline.renderBox(r, ...)`. This renders a sprite-based box outline (not a primitive). The `thick` parameter currently applies to both branches the same way (both draw the same outline — the implementation looks like it may be a work-in-progress).

For the mod, `thick = true` draws a bolder/more visible outline. Use `thick = true` for best visibility of candidate markers.

---

## Is Overlay Visible During StageCapitol?

Yes. Looking at `EThings` behavior:
- It is a simple per-frame accumulator (not a mode-gated overlay like `minerals` or `climate`)
- `EThings.render()` is called unconditionally as part of the world render pipeline
- The capital placement indicator (`PlacerOverlay`) uses the same overlay system during `StageCapitol`, confirming that overlays work in this phase

---

## Confidence and Gaps

### High confidence
- `WORLD.OVERLAY().things` is `public final EThings` — accessible from mod code
- `EThings.hover(int, int, int, int, COLOR, boolean)` is public
- Coordinate system: world pixels = tile × C.TILE_SIZE
- `EThings` clears every frame — items must be re-added in every `SCRIPT_INSTANCE.render()` call
- Render order: SCRIPT_INSTANCE.render() fires before WorldViewGenerator.render()
- Overlays are active during StageCapitol

### Uncertain / gaps
- Whether `GAME.world().render()` calls `things.render()` or `things.clear()` during StageCapitol — need to confirm that the world render pipeline includes EThings during world gen (not just during regular gameplay view); the PlacerOverlay evidence strongly suggests yes
- Whether `COLOR.GREEN100`, `COLOR.RED100` etc. are the correct static constant names — these follow the pattern seen in `GCOLOR.java` but the exact API should be verified when implementing
- Panel positioning: The existing `StageCapitol` panel is at x=centered, y=64 with width 260. Placing the filter panel to its right (or below) requires knowing the actual resolved x1 position of the centered panel, which is `C.DIM().x()/2 - 130`. The screen width `C.DIM().x()` is the display width.
