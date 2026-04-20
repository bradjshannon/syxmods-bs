# W1B Findings: Candidate Site Data Model

## Candidate Site Class
- **No single pre-existing candidate site class** — candidate data is computed on demand from world terrain maps.
- The class used to compute site properties is `world.map.terrain.WorldTerrainInfo`
- **Source file path in JAR:** `world/map/terrain/WorldTerrainInfo.java`
- `WorldTerrainInfo` is a data-container, not a positioned entity. It must be initialized per tile.

The mod must construct its own candidate site list by scanning the map.

---

## Computing Site Properties

### Initialization
```java
WorldTerrainInfo info = new WorldTerrainInfo();
info.initCity(int tileX1, int tileY1);  // fills data for a 3x3 tile block
```
`initCity(x1, y1)` scans a `WCentre.TILE_DIM × WCentre.TILE_DIM` (3×3) tile block starting at `(x1, y1)` and accumulates terrain fractions and climate fractions, then normalizes by dividing by `3*3=9`.

---

## Resource Fields

Resources are **data-driven** — the specific resource types are loaded from game config files at runtime. They are NOT hardcoded enum constants. The mod must iterate `RESOURCES.minables().all()` at runtime.

### Resource types accessor
```java
RESOURCES.minables().all()  // returns LIST<Minable>
```

### Per-resource value computation
For each `Minable m` in `RESOURCES.minables().all()`:
```java
double resourceValue = 0;
for (TERRAIN te : TERRAINS.ALL()) {
    resourceValue += info.get(te).getD() * m.terrain(te);
}
double resourcePercent = 4.0 * resourceValue;  // scaled to 0.0–1.0 (or slightly above)
```

This is exactly what `UIWorldToolCapitolPlaceInfo.placeInfo()` computes and displays with `GFORMAT.perc(b.text(), 4.0*d)`.

### Minable fields of interest
```java
m.resource        // RESOURCE — has .name, .icon
m.index           // int — stable index within RESOURCES.minables().all()
m.onEverymap      // boolean — true if this resource appears on every generated map
```

### Unit
`resourcePercent` is a `double` in range ~0.0–1.0 (1.0 = 100%). Displayed as percentage with `GFORMAT.perc()`. For filter comparison: compare `resourcePercent >= threshold / 100.0` where threshold is an integer 0–100.

---

## Adjacency Fields

`WorldTerrainInfo` does **not** have explicit adjacency boolean fields. Adjacency must be checked directly from the world terrain maps using tile coordinates.

The center tile of a capitol slot is:
```java
int cx = tx1 + WCentre.TILE_DIM / 2;  // = tx1 + 1
int cy = ty1 + WCentre.TILE_DIM / 2;  // = ty1 + 1
```

### River adjacency
```java
boolean hasRiver = WORLD.WATER().RIVER.is(cx, cy);
// or check surrounding tiles:
boolean hasRiverNearby = WORLD.WATER().isRivery.is(cx, cy);
```
`WORLD.WATER().RIVER` is a `WorldWater.WATER` instance (the `River` inner class).
`WORLD.WATER().RIVER.is(int tx, int ty)` — returns true if tile is a river tile.
`WORLD.WATER().isRivery` is a `MAP_BOOLEAN` that checks for river in nearby area.

For the filter ("adjacent to river"), scanning the 3×3 tile block and any surrounding ring of tiles for any river tile is the most reliable approach.

### Ocean/Water adjacency
`WORLD.WATER().has` is a `MAP_BOOLEAN`:
```java
boolean hasWater = WORLD.WATER().has.is(cx, cy);
```
`WORLD.WATER().OCEAN` is a `WorldWater.OpenSet` (public field). Checking the center + surrounding tiles for `WORLD.WATER().has.is(t.x(), t.y())` and then distinguishing ocean from river requires additional logic. The `WorldTerrainInfo.add()` method calls `WORLD.WATER().add(this, tx, ty)` which adds water terrain fraction — if water fraction > 0 in the area, water is nearby.

For MVP, "adjacent to ocean" can be approximated by: any water tile in the 3×3 block or adjacent ring that is NOT a river (i.e., `has.is(t) && !RIVER.is(t)`).

### Mountain adjacency
```java
boolean hasMountain = WORLD.MOUNTAIN().coversTile(cx, cy);
// or:
boolean hasMountain = WORLD.MOUNTAIN().is(cx, cy);
// broader check:
boolean hasMountainNearby = WORLD.MOUNTAIN().haser.is(cx, cy);
```
`WorldMountain.coversTile(int tx, int ty)` — true if a mountain covers this tile.
`WorldMountain.is(int tx, int ty)` — true if tile is a mountain tile.
`WorldMountain.haser` is a `MAP_BOOLEAN`.

For the filter ("adjacent to mountain"), scanning the surrounding ring of tiles for `WORLD.MOUNTAIN().coversTile(t)` is recommended.

---

## Climate Field

### Field name and access
Climate at a tile is retrieved from:
```java
CLIMATE climate = WORLD.CLIMATE().getter.get(cx, cy);
```
Where `cx, cy` is the center tile of the capitol slot.

### Type and enum constants
`CLIMATE` is a class in `init.type.CLIMATE`. Climate instances are not a Java enum but are accessed as static singletons:
```java
CLIMATES.COLD()   // cold climate
CLIMATES.TEMP()   // temperate climate (note: method name is TEMP, not TEMPERATE)
CLIMATES.HOT()    // hot climate (NOTE: spec says "warm" but game calls it "HOT")
CLIMATES.ALL()    // LIST<CLIMATE> — all climates in order
```

**Note:** The spec previously used "warm" as the third climate label. The actual game constant is `CLIMATES.HOT()`. The spec has been updated to use `climate.name` (game-localized `CharSequence`) for all climate labels — hardcoded names are not used.

### Climate identity comparison
```java
CLIMATE siteClimate = WORLD.CLIMATE().getter.get(cx, cy);
if (siteClimate == CLIMATES.COLD())    // is cold
if (siteClimate == CLIMATES.TEMP())   // is temperate
if (siteClimate == CLIMATES.HOT())    // is hot/warm
// or by index:
siteClimate.index()                    // 0-based index into CLIMATES.ALL()
```

---

## Site Identity / Coordinate

### Coordinate type
A candidate site is identified by its **tile-space top-left corner** `(tx1, ty1)` where `tx1` and `ty1` are tile indices in `[0, WORLD.TWIDTH())` and `[0, WORLD.THEIGHT())`.

The capitol footprint is a `WCentre.TILE_DIM × WCentre.TILE_DIM = 3×3` tile block starting at `(tx1, ty1)`.

### Rendering anchor
To convert to world-pixel coordinates (for marker drawing):
```java
int worldX = tx1 * C.TILE_SIZE;
int worldY = ty1 * C.TILE_SIZE;
int worldW = WCentre.TILE_DIM * C.TILE_SIZE;  // 3 * C.TILE_SIZE
int worldH = WCentre.TILE_DIM * C.TILE_SIZE;
```
`C.TILE_SIZE` is the pixel size of one world tile (typically 16).

**Recommendation:** Use tile coordinates `(tx1, ty1)` as the primary site identity in the mod's cache. They are stable across filter changes (only change on map regeneration).

---

## Candidate Site Collection

There is **no pre-built collection** of valid candidate sites. The mod must build it.

### Scanning approach
```java
List<CandidateSite> sites = new ArrayList<>();
for (int ty1 = 0; ty1 + WCentre.TILE_DIM < WORLD.THEIGHT(); ty1++) {
    for (int tx1 = 0; tx1 + WCentre.TILE_DIM < WORLD.TWIDTH(); tx1++) {
        if (WorldCentrePlacablity.terrain(tx1, ty1) == null) {
            // valid candidate site — compute and cache properties
            sites.add(buildCandidateSite(tx1, ty1));
        }
    }
}
```

Note: This scans every tile, not just stride-3 tiles. To scan only non-overlapping slots, use `ty1 += WCentre.TILE_DIM` and `tx1 += WCentre.TILE_DIM` increments. However, the vanilla tool allows placement at any valid tile offset, not just stride-3 positions. Scanning every tile gives the most complete candidate set but will be slower. For MVP, stride-3 scanning is a reasonable approximation.

### Cardinality
Typical map size: `WORLD.TWIDTH()` × `WORLD.THEIGHT()`. Default world size is approximately 256×128 tiles. Valid candidates could number in the hundreds to low thousands depending on terrain distribution.

---

## Confidence and Gaps

### High confidence
- `WorldTerrainInfo.initCity(x1, y1)` is the correct method to compute site properties
- `RESOURCES.minables().all()` provides all resource types at runtime (data-driven, not hardcoded)
- Resource percentage formula: `4.0 * sum(info.get(te).getD() * m.terrain(te))` for all terrains
- `WORLD.CLIMATE().getter.get(cx, cy)` returns the climate at a tile
- Three climate constants: `CLIMATES.COLD()`, `CLIMATES.TEMP()`, `CLIMATES.HOT()` — NOT "warm"
- `WORLD.WATER().RIVER.is(tx, ty)` detects river tiles
- `WORLD.MOUNTAIN().coversTile(tx, ty)` or `haser.is(tx, ty)` detects mountain tiles
- `WorldCentrePlacablity.terrain(tx1, ty1) == null` is the validity check
- `WCentre.TILE_DIM = 3`, `C.TILE_SIZE` = pixels per tile

### Uncertain / gaps
- Exact behavior of `WORLD.WATER().OCEAN` — the `OpenSet` type and how to use it for ocean adjacency detection; needs further source reading
- Whether `WorldTerrainInfo.add()` scanning counts ocean separately from river in the terrain fractions — may need to check `WorldWater.add(info, tx, ty)` implementation
- Performance of scanning all tiles on map generation (call `initCity` for each valid candidate) — likely acceptable for default map size
- Whether `WORLD.TWIDTH()` / `WORLD.THEIGHT()` are available during `initBeforeGameCreated()` or only after terrain gen
