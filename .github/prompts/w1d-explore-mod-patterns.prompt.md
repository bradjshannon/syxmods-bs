---
description: "Wave 1D — Survey community mod example for mod lifecycle and UI extension patterns (context-isolated)"
mode: agent
---

You are researching mod integration patterns for Songs of Syx v70 to answer specific technical questions needed to implement a mod. You will fetch the community mod example repository from GitHub and examine its structure and code.

## CONTEXT ISOLATION

Do NOT read any file in the `docs/` folder. Do NOT reference or build on findings from any other agent. Use only the sources listed in this prompt.

---

## Your Task

Survey the community Songs of Syx mod example repository and extract patterns relevant to implementing a UI overlay mod. Answer the following questions with exact class names, interfaces, and code patterns:

1. How does a mod register itself with the game? What interface(s) or base class(es) must be implemented? What is the entry-point method or class?
2. How does a mod hook into the game's screen or UI lifecycle? Is there an interface for injecting UI panels?
3. Are there examples of a mod adding UI elements (panels, buttons, checkboxes) to an existing game screen? How is this done?
4. Is there a render hook interface a mod can implement to draw custom graphics during a specific screen's render pass?
5. How does the mod's Maven/build configuration declare the entry point (manifest, annotation, config file)?
6. Are there any patterns for listening to game events (map generated, screen opened, etc.)?
7. What game API classes does the example use most heavily — list the top imports and what they're used for?

---

## How to Access the Community Example

The community mod example is at: https://github.com/4rg0n/songs-of-syx-mod-example

Fetch the repository content. Key pages to fetch:
- `https://github.com/4rg0n/songs-of-syx-mod-example` (README and structure)
- `https://raw.githubusercontent.com/4rg0n/songs-of-syx-mod-example/main/src/main/java/com/example/mod/MyMod.java` (or equivalent main class — adjust path if needed)
- Browse the repository tree to find relevant source files

Also examine the workspace mod entry point at:
`src/main/java/mod/example/ExampleMod.java`

And the mod metadata:
`src/main/resources/mod-files/_Info.txt`

---

## Background Context

Read section 14 of:
`specs/syx_v70_settlement_candidate_filter_overlay_spec.md`

This section describes the implementation approach needed. Use it only to understand what patterns to look for.

---

## Output

Write your findings to: `docs/w1d-mod-patterns.md`

Structure the file exactly as follows:

```
# W1D Findings: Mod Lifecycle and UI Extension Patterns

## Mod Entry Point
- Interface or base class a mod must implement:
- Entry point method name and signature:
- How the game discovers the mod class (manifest key, annotation, etc.):

## UI Injection Patterns
- Interface(s) available for adding UI to existing screens:
- How a mod adds a panel or widget to an existing game screen:
- Code pattern / example (copy relevant snippet):

## Render Hook Pattern
- Interface(s) for custom drawing during a screen render pass:
- Method signature to implement:
- How to register the renderer with the game:

## Event / Lifecycle Hooks
- Any observable or callback for "map/geography generated":
- Any observable or callback for "settlement screen opened/closed":
- Pattern used (listener, observer, method override):

## Key Game API Imports
List the most commonly used game API classes in the example:
| Class | Package | Purpose |
|---|---|---|

## Build / Manifest Configuration
- How the entry point class is declared in pom.xml or manifest:
- Relevant snippet:

## Gaps and Unknowns
- Patterns not found in the example that may require source JAR research
```
