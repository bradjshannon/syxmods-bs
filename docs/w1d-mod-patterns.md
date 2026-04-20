# W1D Findings: Community Mod Patterns

## Source
Community mod template and guide: https://github.com/4rg0n/songs-of-syx-mod-example (master branch, 2 months old as of research date)

---

## Mod Entry Point Structure

The canonical v70 mod structure uses two classes:

### 1. `MainScript implements SCRIPT`
The primary entry point, discovered by the game via reflection. Must be `public` with a no-args constructor.

```java
package your.mod;

import lombok.NoArgsConstructor;
import script.SCRIPT;
import util.info.INFO;

@NoArgsConstructor
@SuppressWarnings("unused") // used by the game via reflection
public final class MainScript implements SCRIPT {

    private final INFO info = new INFO("Mod Name", "Description goes here");

    @Override public CharSequence name() { return info.name; }
    @Override public CharSequence desc() { return info.desc; }

    @Override
    public void initBeforeGameCreated() {
        // Runs before game object is constructed — use for early hooks
    }

    @Override
    public void initBeforeGameInited() {
        // Runs after GAME is created but before init phase
    }

    @Override
    public boolean isSelectable() { return SCRIPT.super.isSelectable(); }

    @Override
    public boolean forceInit() { return SCRIPT.super.forceInit(); }

    @Override
    public SCRIPT_INSTANCE createInstance() {
        return new InstanceScript();
    }
}
```

**Key points:**
- `@NoArgsConstructor` is Lombok — or just provide an explicit public no-args constructor
- `isSelectable()` default = true (mod appears in the mod selection UI)
- `forceInit()` default = false (must be manually activated); return `true` to auto-activate on all games
- `createInstance()` is called exactly once per game session

### 2. `InstanceScript implements SCRIPT.SCRIPT_INSTANCE`
The per-session instance. Created by `MainScript.createInstance()`.

```java
package your.mod;

import script.SCRIPT;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.*;
import util.gui.misc.GBox;
import view.keyboard.KEYS;

final class InstanceScript implements SCRIPT.SCRIPT_INSTANCE {

    @Override
    public void save(FilePutter file) { /* write save data */ }

    @Override
    public void load(FileGetter file) { /* read save data */ }

    @Override
    public void update(double deltaSeconds) { /* called ~60x/sec */ }

    @Override
    public void render(Renderer renderer, float deltaSeconds) { /* draw */ }

    @Override
    public void hoverTimer(double mouseTimer, GBox text) { /* tooltip */ }

    @Override
    public void keyPush(KEYS keys) { /* key press */ }

    @Override
    public void mouseClick(MButt button) { /* mouse click */ }

    @Override
    public void hover(COORDINATE mCoo, boolean mouseHasMoved) { /* mouse position */ }

    @Override
    public boolean handleBrokenSavedState() {
        return SCRIPT.SCRIPT_INSTANCE.super.handleBrokenSavedState();
    }
}
```

---

## Current Scaffold (`ExampleMod.java`)

The project's current `src/main/java/mod/example/ExampleMod.java` does **not** implement `SCRIPT`. It is a generic placeholder that must be replaced.

**Action required for Wave 3:** Delete `ExampleMod.java` and create two new files:
- `src/main/java/mod/syx/bs/CapitalFilterMod.java` (implements `SCRIPT`)
- `src/main/java/mod/syx/bs/CapitalFilterInstance.java` (implements `SCRIPT.SCRIPT_INSTANCE`)

Or any preferred package name — as long as the `SCRIPT` implementation is public and has a no-args constructor.

---

## Maven Build and Deployment

```
mvn validate   # register game JARs (one-time)
mvn package    # compile + build JAR to target/
mvn install    # package + copy to %APPDATA%\songsofsyx\mods\<mod-name>\
mvn clean      # remove target/ and mod from game mods folder
```

Mod files go to: `%APPDATA%\songsofsyx\mods\<game.mod.name property in pom.xml>\`

The JAR is placed in: `<mod-folder>/V70/script/<name>.jar`

---

## Mod JAR Discovery

The game loads mod JARs from the `script/` subdirectory of the mod's version folder. `ScriptLoad` scans JARs for classes implementing `script.SCRIPT` via reflection:

```
<APPDATA>/songsofsyx/mods/<mod-name>/V70/script/  → JAR files scanned
```

The JAR must contain:
- `public class <YourClass> implements script.SCRIPT` — public, with no-args constructor
- The game calls `createInstance()` to get the `SCRIPT_INSTANCE`

No annotation or registration file is needed — pure reflection-based discovery.

---

## Lifecycle Hooks and Their Active Phases

| Method | When Called | Notes |
|---|---|---|
| `initBeforeGameCreated()` | Before `GAME` is constructed | No `GAME.*` or `WORLD.*` access yet |
| `initBeforeGameInited()` | After `GAME` created, before init | `GAME` exists; `WORLD` may not be ready |
| `createInstance()` | During `GAME` init | Returns `SCRIPT_INSTANCE` |
| `SCRIPT_INSTANCE.render(r, ds)` | Every render frame | Active during StageCapitol ✓ |
| `SCRIPT_INSTANCE.update(ds)` | ~60 Hz game loop | Active during StageCapitol ✓ |
| `SCRIPT_INSTANCE.hover(coo, moved)` | Every frame | Mouse coordinates in screen pixels |
| `SCRIPT_INSTANCE.mouseClick(button)` | On mouse click | `MButt.LEFT`, `MButt.RIGHT` |
| `SCRIPT_INSTANCE.hoverTimer(timer, gbox)` | When mouse hovers | Can add tooltip text |
| `SCRIPT_INSTANCE.save(file)` / `load(file)` | On game save/load | Persist filter state across sessions |

---

## Save/Load Pattern

Filter state (checked filters, threshold values) should be persisted via `save(FilePutter)` and `load(FileGetter)`:

```java
@Override
public void save(FilePutter file) {
    file.bool(filterState.riverRequired);
    file.i(filterState.resourceThreshold);
    // etc.
}

@Override
public void load(FileGetter file) {
    filterState.riverRequired = file.bool();
    filterState.resourceThreshold = file.i();
    // etc.
}
```

`FilePutter` and `FileGetter` are in `snake2d.util.file.*`.

---

## UI Addition Pattern (from how-to guide)

The community guide has a `doc/howto/add_ui_element.md`. The general pattern (from examination of mod SDK) for adding UI during an active view:

```java
// In createInstance() or a render-time check:
// Check if we're in the right view, then inject once
if (!uiInjected && VIEW.current() instanceof WorldViewGenerator) {
    // ... inject filter panel via reflection or direct method ...
    uiInjected = true;
}
```

For raw renderer-based UI (no injection):
```java
@Override
public void render(Renderer r, float ds) {
    if (!(VIEW.current() instanceof WorldViewGenerator)) return;
    // Draw panel background, text, checkboxes directly using Renderer
    // ...
}
```

---

## pom.xml Config Properties

Rename the mod by updating `pom.xml` properties:
```xml
<properties>
    <mod.name>Capital Filter</mod.name>   <!-- deployed folder name -->
    <game.version.major>70</game.version.major>
    <game.version.minor>33</game.version.minor>
    <!-- ... etc. -->
</properties>
```

The `_Info.txt` in `src/main/resources/mod-files/` also needs updating:
```
NAME: "Capital Filter"
VERSION: "1.0.0"
AUTHOR: "..."
```

---

## Confidence and Gaps

### High confidence
- `SCRIPT` + `SCRIPT_INSTANCE` interface structure is confirmed from both game sources and community template
- JAR discovery is via reflection scanning `script.SCRIPT` implementors
- `render()` and `update()` fire during StageCapitol
- `save()` / `load()` can persist filter state
- `ExampleMod.java` must be replaced (it does not implement `SCRIPT`)
- pom.xml properties control mod deployment folder name

### Uncertain / gaps
- Whether `initBeforeGameCreated()` runs once per JVM session or per game load — from context, it appears to run once when the GAME object is first constructed, not on every reload
- Whether `VIEW.current()` returns a `WorldViewGenerator` during the very first render frame (or if there's a null/transition frame) — should guard with null check
- Whether Lombok `@NoArgsConstructor` is on the classpath in this project's pom.xml — if not, provide explicit no-args constructor; check pom.xml dependencies
- The exact `game.mod.name` property name in pom.xml — the current scaffold has `example-mod`; check the actual pom.xml
