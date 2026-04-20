---
description: "Wave 1B — Find candidate site data model (resources, adjacency, climate) (context-isolated)"
mode: agent
---

You are researching the Songs of Syx v70 game source code to answer specific technical questions needed to implement a mod. You have access to the game sources JAR via terminal commands.

## CONTEXT ISOLATION

Do NOT read any file in the `docs/` folder. Do NOT reference or build on findings from any other agent. Use only the sources listed in this prompt.

---

## Your Task

Find the data model for candidate settlement sites in Songs of Syx v70.

Answer the following questions with exact class names, field names, method signatures, and enum values:

1. What class or classes represent a candidate settlement site (a location the player can choose to found a city)?
2. What resource types are exposed on a candidate site in v70? List all resource fields with their exact Java types and names.
3. How are resource values expressed — as a percentage (0–100), a raw float, or some other unit?
4. What adjacency properties are available? Specifically: are `adjacentOcean`, `adjacentRiver`, `adjacentMountain` (or equivalent) fields present? What are their exact names and types?
5. What climate property is available? What enum or type represents climate values (cold, temperate, warm or equivalent)? List all valid enum constants.
6. How is a candidate site identified — by coordinate, by index, by some ID type? What is the type?
7. What class or collection holds the full list of candidate sites for a given generated geography?

---

## How to Access Game Sources

The game sources JAR is at:
`E:\SteamLibrary\steamapps\common\Songs of Syx\info\SongsOfSyx-sources.jar`

Use terminal commands to explore it. Suggested approach:

1. List files related to settlement candidates:
   ```powershell
   jar tf "E:\SteamLibrary\steamapps\common\Songs of Syx\info\SongsOfSyx-sources.jar" | Select-String -Pattern "candidate|Candidate|site|Site|settlement|Settlement|resource|Resource|region|Region|climate|Climate"
   ```

2. Extract the full source tree to a temp location:
   ```powershell
   New-Item -ItemType Directory -Force "C:\temp\syx-src" | Out-Null
   Push-Location "C:\temp\syx-src"
   jar xf "E:\SteamLibrary\steamapps\common\Songs of Syx\info\SongsOfSyx-sources.jar"
   Pop-Location
   ```

3. Search for climate and resource enums:
   ```powershell
   Get-ChildItem "C:\temp\syx-src" -Recurse -Filter "*.java" | Select-String -Pattern "enum.*[Cc]limate|COLD|TEMPERATE|WARM|CLIMATE" | Select-Object -First 30
   Get-ChildItem "C:\temp\syx-src" -Recurse -Filter "*.java" | Select-String -Pattern "adjacent|ocean|river|mountain" -CaseSensitive:$false | Select-Object -First 40
   ```

4. Read source files for candidate/site classes once identified.
5. Follow references to resource types from within candidate site classes.

Be thorough. Check tooltip-adjacent code paths since the tooltip is known to display these values — follow what it reads from.

---

## Background Context

Read sections 8 and 14 of:
`specs/syx_v70_settlement_candidate_filter_overlay_spec.md`

These sections describe the conceptual data model the mod needs. Use them only to understand what to look for — the actual source code is the authority.

---

## Output

Write your findings to: `docs/w1b-candidate-data.md`

Structure the file exactly as follows:

```
# W1B Findings: Candidate Site Data Model

## Candidate Site Class
- Fully qualified class name:
- Source file path in JAR:
- Parent class (if relevant):

## Resource Fields
For each resource: name, Java type, unit (percent / float / int), range if known
| Field Name | Java Type | Unit | Notes |
|---|---|---|---|

## Adjacency Fields
- adjacentOcean equivalent: (field name, type)
- adjacentRiver equivalent: (field name, type)
- adjacentMountain equivalent: (field name, type)
- Any other adjacency fields present:

## Climate Field
- Field name:
- Type / enum class name:
- Enum constants (list all):

## Site Identity / Coordinate
- How a candidate site is identified (type, field):
- How a screen-space or map-space coordinate is obtained from it:

## Candidate Site Collection
- Class or field that holds the full list of sites:
- Where it lives (class name + field name):
- Size/cardinality typical for a generated map (if inferable):

## Confidence and Gaps
- List what you found with high confidence
- List what remains uncertain or requires cross-reference with W1A findings
```
