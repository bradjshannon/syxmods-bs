# Agent Context — syxmods-bs

This file provides environment and project context for implementing agents.

---

## Game

| Field | Value |
|---|---|
| Game | Songs of Syx |
| Target version | **v70** (specifically v70.33) |
| Install path | `E:\SteamLibrary\steamapps\common\Songs of Syx` |
| User data / mods folder | `%APPDATA%\songsofsyx\mods` |
| Game JAR | `E:\SteamLibrary\steamapps\common\Songs of Syx\SongsOfSyx.jar` |
| Sources JAR | `E:\SteamLibrary\steamapps\common\Songs of Syx\info\SongsOfSyx-sources.jar` |
| Script guide | `E:\SteamLibrary\steamapps\common\Songs of Syx\info\scriptGuide.odt` |

---

## Toolchain

| Tool | Version | Path |
|---|---|---|
| JDK | Temurin 21.0.10 | `C:\dev\jdk21` |
| Maven | 3.9.14 | `C:\dev\apache-maven-3.9.14` |

Both are on the user `PATH` and `JAVA_HOME`/`M2_HOME` are set. If a new terminal session doesn't have them, prefix commands with:

```powershell
$env:JAVA_HOME = "C:\dev\jdk21"
$env:Path = "C:\dev\jdk21\bin;C:\dev\apache-maven-3.9.14\bin;$env:Path"
```

---

## Maven

The game JAR and sources JAR are already registered in the local Maven repo:

```
com.songsofsyx:songsofsyx:70.33        (JAR + sources)
```

Registered at: `~/.m2/repository/com/songsofsyx/songsofsyx/70.33/`

Running `mvn validate` re-registers them (idempotent). Only needed again if the game updates.

---

## Build Workflow

| Command | Effect |
|---|---|
| `mvn validate` | (Re-)register game JARs into local Maven repo |
| `mvn compile` | Compile mod sources |
| `mvn package` | Compile + produce shaded fat JAR in `target/` |
| `mvn install` | Package + deploy mod to `%APPDATA%\songsofsyx\mods\<mod name>\` |

---

## Project Structure

```
syxmods-bs/
├── pom.xml                                    # Maven build; game version properties here
├── AGENTS.md                                  # This file
├── specs/
│   └── syx_v70_settlement_candidate_filter_overlay_spec.md   # MVP spec
├── .vscode/
│   ├── launch.json                            # "Launch Game" / "Launch Game (Launcher)"
│   └── settings.json                          # Java home + Maven path for VS Code
└── src/main/
    ├── java/mod/example/ExampleMod.java       # Starter class — rename/replace
    └── resources/mod-files/
        ├── _Info.txt                          # Mod metadata (name, version, author)
        └── V70/                               # Version-specific assets go here
```

The current `pom.xml` scaffold uses `artifactId = example-mod`, `name = Example Mod`. **Update both** when naming the real mod. The mod name controls the deploy folder under `%APPDATA%\songsofsyx\mods\`.

---

## VS Code Launch Configs

`.vscode/launch.json` contains two configs:

- **Launch Game** — runs `init.Main`, working directory = game install path
- **Launch Game (Launcher)** — runs `init.MainLaunchLauncher`, same working dir

The `Extension Pack for Java` VS Code extension is required (`vscjava.vscode-java-pack`).

---

## Game Sources Reference

The sources JAR is the primary reference for game internals. It is available:

- at `E:\SteamLibrary\steamapps\common\Songs of Syx\info\SongsOfSyx-sources.jar`
- in the local Maven repo at `~/.m2/repository/com/songsofsyx/songsofsyx/70.33/songsofsyx-70.33-sources.jar`

VS Code will attach sources automatically via Maven when the game JAR is on the classpath.

The community mod example repo is also useful for integration patterns:
- https://github.com/4rg0n/songs-of-syx-mod-example

---

## Current Task

Implement the mod described in:

```
specs/syx_v70_settlement_candidate_filter_overlay_spec.md
```

Summary: an additive overlay on the settlement-selection / map review screen that:
- reads candidate site properties (resources, adjacency, climate) after geography generation
- lets the player configure filter rules via on-screen checkboxes and thresholds
- marks passing candidates on the map
- shows `passing / total` count

See the spec for full requirements, filter semantics, data model, and validation checklist.

---

## Key Open Questions for Implementing Agent

(from spec section 18 — to be resolved by examining game sources)

- Exact class responsible for the settlement-selection / map review screen
- Exact class(es) exposing candidate site data (resources, adjacency, climate)
- Available resource types on a candidate site in v70
- Hook point for geography regeneration completion
- Safest draw layer for candidate markers
- Whether the regenerate-geography UI box can be extended in-place or requires an adjacent panel
