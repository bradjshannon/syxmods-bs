---
description: "Wave 1C — Find draw layer system for safe additive overlay markers (context-isolated)"
mode: agent
---

You are researching the Songs of Syx v70 game source code to answer specific technical questions needed to implement a mod. You have access to the game sources JAR via terminal commands.

## CONTEXT ISOLATION

Do NOT read any file in the `docs/` folder. Do NOT reference or build on findings from any other agent. Use only the sources listed in this prompt.

---

## Your Task

Find the rendering / draw layer system in Songs of Syx v70 and determine the safest way to draw additive overlay markers on the map during the settlement-selection screen.

Answer the following questions with exact class names, method signatures, and constants:

1. What draw layer system does the game use? What are the available layers and their names/constants?
2. Which layer is safest for drawing an additive overlay marker that sits above the map but below game UI chrome (tooltips, menus)?
3. What is the API for drawing a simple shape (circle, ring, dot, icon) at a map coordinate? What class and method handles this?
4. What coordinate system is used for marker placement — world tile coordinates, screen pixels, or a normalized space? How do you convert a candidate site's position into render coordinates?
5. What is the render loop entry point for custom drawing in mod code? Is there a `render(SPRITE_RENDERER r, float ds)` or equivalent that a mod can inject into?
6. Is there a `SPRITE_RENDERER` or equivalent type that custom render code receives? What drawing primitives does it expose?
7. Are there examples in the source of additive rendering on top of the world map (fog of war, overlays, highlights)?

---

## How to Access Game Sources

The game sources JAR is at:
`E:\SteamLibrary\steamapps\common\Songs of Syx\info\SongsOfSyx-sources.jar`

Use terminal commands to explore it. Suggested approach:

1. List files related to rendering and drawing:
   ```powershell
   jar tf "E:\SteamLibrary\steamapps\common\Songs of Syx\info\SongsOfSyx-sources.jar" | Select-String -Pattern "render|Render|sprite|Sprite|draw|Draw|layer|Layer|overlay|Overlay"
   ```

2. Extract the full source tree to a temp location:
   ```powershell
   New-Item -ItemType Directory -Force "C:\temp\syx-src" | Out-Null
   Push-Location "C:\temp\syx-src"
   jar xf "E:\SteamLibrary\steamapps\common\Songs of Syx\info\SongsOfSyx-sources.jar"
   Pop-Location
   ```

3. Search for rendering primitives and layer constants:
   ```powershell
   Get-ChildItem "C:\temp\syx-src" -Recurse -Filter "*.java" | Select-String -Pattern "SPRITE_RENDERER|SpriteRenderer|drawCircle|drawRect|drawIcon|LAYER" | Select-Object -First 40
   ```

4. Look at how existing UI overlays or highlights are rendered in the world-view code:
   ```powershell
   Get-ChildItem "C:\temp\syx-src" -Recurse -Filter "*.java" | Select-String -Pattern "highlight|Highlight|tileOverlay|fog|Fog" | Select-Object -First 30
   ```

5. Read relevant source files once identified.

---

## Background Context

Read sections 9 and 14 of:
`specs/syx_v70_settlement_candidate_filter_overlay_spec.md`

These sections describe the marker behavior the mod needs. Use them only to understand the requirement — actual source code is the authority.

---

## Output

Write your findings to: `docs/w1c-draw-layer.md`

Structure the file exactly as follows:

```
# W1C Findings: Draw Layer System and Marker Rendering

## Draw Layer System
- How layers are defined (class, enum, or constants):
- List of available layers with names and typical use:
- Recommended layer for an additive candidate-site marker:
- Reason for recommendation:

## Marker Drawing API
- Class and method for drawing a simple shape at a world coordinate:
- Method signature:
- Coordinate system used (tile coords, world coords, screen pixels):
- How to convert a candidate site position to render coordinates:

## Render Loop Entry Point
- Class/interface a mod implements to hook into the render loop:
- Method signature to override:
- When it fires relative to the base game draw calls:

## SPRITE_RENDERER / Renderer Type
- Fully qualified class name of the renderer passed to draw methods:
- Key drawing primitives available (list method signatures):

## Existing Overlay Examples
- Class(es) in the base game that render something on top of the world map:
- How they register with the render system:

## Confidence and Gaps
- List what you found with high confidence
- List what remains uncertain
```
