# Minecraft End Expansion 1.20.x-1

Forge mod for Minecraft `1.20.1` focused on a custom End phantom experience.

This project adds a dedicated `End Phantom` entity with custom spawning, combat behavior, taming, pet AI, riding support, dye customization, and custom visuals.

## Features

- Custom `End Phantom` entity instead of reusing vanilla phantom instances
- End-only spawn system that prefers true void space near players
- Stronger End phantom combat profile
- Custom targeting rules for geared players
- `Void Morsel` item for taming and healing
- `Phantom Harness` item for mounting and riding
- Tamed phantom follow, orbit, defend, ambient sound, and leash behavior
- Dye support for recoloring owned phantoms
- Custom body, eye glow, and item textures

## Gameplay

### End Phantom

End phantoms are custom phantoms that spawn in The End and are meant to feel like void predators rather than normal overworld phantoms.

Behavior highlights:

- Spawn over real void rather than directly on islands
- Can appear near islands and bridges if valid void space is nearby
- More likely to pressure better-equipped players
- Use a custom texture and custom glow eyes

### Taming

Use a `Void Morsel` on an End phantom to tame it.

While a player is holding a `Void Morsel`, wild End phantoms will move in close without attacking, which makes taming possible in survival.

Tamed phantoms:

- follow their owner
- orbit the owner while the owner is standing still
- defend the owner
- attack targets hit by the owner
- can be healed with more `Void Morsels`
- can be leashed by their owner
- can be named with a name tag

### Riding

Use a `Phantom Harness` on your tamed phantom, then empty-hand right-click it to mount.

Mounted phantoms:

- stop normal follow/orbit behavior while ridden
- use player direction and movement input for flight
- descend while crouching

### Dyeing

Owned tamed End phantoms can be recolored with vanilla dyes.

The body tint and eye glow both react to the stored dye color.

## Items

### Void Morsel

Used for:

- taming End phantoms
- healing your own tamed phantom
- luring wild End phantoms into a calm, non-hostile approach state

Recipe:

- `chorus_fruit`
- `phantom_membrane`
- `ender_pearl`

Command:

```mcfunction
/give @p endexpansionmod:void_morsel
```

### Phantom Harness

Used for:

- enabling riding on your tamed End phantom

Recipe pattern:

```text
M M
SPS
 M
```

- `M = phantom_membrane`
- `S = string`
- `P = ender_pearl`

Command:

```mcfunction
/give @p endexpansionmod:phantom_harness
```

## Creative Tab

Custom items are placed in:

- `Tools & Utilities`

## Development

### Requirements

- Java 17 or newer
- Minecraft Forge `47.0.19`
- Minecraft `1.20.1`

### Run the dev client

```powershell
$env:JAVA_HOME='C:\Users\Ortiz\.jdks\corretto-19.0.2'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat runClient
```

### Run the dev server

```powershell
$env:JAVA_HOME='C:\Users\Ortiz\.jdks\corretto-19.0.2'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat runServer
```

### Compile

```powershell
$env:JAVA_HOME='C:\Users\Ortiz\.jdks\corretto-19.0.2'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat compileJava
```

## Project Status

The current version is centered on the custom End phantom system and its supporting items, rendering, taming, and riding mechanics.
