---
description: "Wave 1A — Find settlement-selection screen class and UI hook points (context-isolated)"
mode: agent
---

You are researching the Songs of Syx v70 game source code to answer specific technical questions needed to implement a mod. You have access to the game sources JAR via terminal commands.

## CONTEXT ISOLATION

Do NOT read any file in the `docs/` folder. Do NOT reference or build on findings from any other agent. Use only the sources listed in this prompt.

---

## Your Task

Find the class(es) responsible for the settlement-selection / map review screen in Songs of Syx v70.

Answer the following questions with exact class names, method signatures, and source paths:

1. What is the fully-qualified class name of the screen or panel that handles the settlement-selection / map review UI?
2. What method(s) handle rendering or drawing on that screen? Provide full signatures.
3. What method or callback fires when geography regeneration completes?
4. Is there a UI box or panel class that wraps the regenerate-geography button? What is its class name and how is it structured in the source?
5. Does the screen class hold or have access to the list of candidate settlement sites? Through what field or method?
6. What is the parent class chain of the screen class (immediate parent and any relevant grandparents)?

---

## How to Access Game Sources

The game sources JAR is at:
`E:\SteamLibrary\steamapps\common\Songs of Syx\info\SongsOfSyx-sources.jar`

Use terminal commands to explore it. Suggested approach:

1. List all source files to find relevant class names:
   ```powershell
   jar tf "E:\SteamLibrary\steamapps\common\Songs of Syx\info\SongsOfSyx-sources.jar" | Select-String -Pattern "settlement|Settlement|MapReview|WorldCreat|candidate|Candidate|geography|Geography"
   ```

2. Extract the full source tree to a temp location so you can read files:
   ```powershell
   New-Item -ItemType Directory -Force "C:\temp\syx-src" | Out-Null
   Push-Location "C:\temp\syx-src"
   jar xf "E:\SteamLibrary\steamapps\common\Songs of Syx\info\SongsOfSyx-sources.jar"
   Pop-Location
   ```

3. Search extracted sources for relevant terms:
   ```powershell
   Get-ChildItem "C:\temp\syx-src" -Recurse -Filter "*.java" | Select-String -Pattern "regenerate|regen|settlement|candidate" | Select-Object -First 40
   ```

4. Read specific source files once you identify promising class names.

Be thorough. Cross-reference class names in source files that import or reference the screen class.

---

## Background Context

Read sections 1–5 of:
`specs/syx_v70_settlement_candidate_filter_overlay_spec.md`

Use this only to understand what the mod needs to hook into — do not use it as a substitute for actual source research.

---

## Output

Write your findings to: `docs/w1a-screen-class.md`

Structure the file exactly as follows:

```
# W1A Findings: Screen Class and UI Hook Points

## Screen Class
- Fully qualified class name:
- Source file path in JAR:
- Parent class (immediate):
- Parent class chain (up to top-level game class):

## Render / Draw Methods
List each relevant method signature, e.g.:
- `public void render(SPRITE_RENDERER r, float ds)`

## Geography Regeneration Hook
- How regeneration is triggered (class + method the button calls):
- What fires on completion (callback, method, or observable):
- Whether there is a clear post-generation event to hook into:

## Regenerate-Geography UI Box
- Class name wrapping the regenerate button (if any):
- How it is constructed or referenced:
- Whether it appears extensible without replacement:

## Candidate Site Access
- Whether the screen class holds candidate site data:
- Field name(s) or method(s) used:
- Type of the candidate site collection:

## Confidence and Gaps
- List what you found with high confidence
- List what remains uncertain or requires W1B findings to resolve
```
