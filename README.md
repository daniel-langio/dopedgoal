# Doped Goal

A goal is a wall. A task is a brick. Finishing a task places its brick, and
finishing every task fills the wall.

Built for ADHD users: progress is additive, nothing decays, no streak can break,
and no brick is ever lost.

## Documentation

- [`ANDROID_DESIGN.md`](ANDROID_DESIGN.md) — design direction, screens, tokens, accessibility rules
- [`CONTRIBUTING.md`](CONTRIBUTING.md) — commit conventions, CI expectations, development notes
- [`docs/development/`](docs/development/README.md) — bugs and struggles encountered while building

## Modules

| Module | What it is |
| --- | --- |
| `:core` | Pure Kotlin/JVM domain. Goal, Task, Brick, WallStyle, Banner and the rules that make invalid bricks unconstructable. No Android dependency, so it unit-tests on the JVM. |
| `:app` | Android app — Jetpack Compose, Material 3 components styled with the design tokens. Holds the wall renderer. |

## Build

Requires JDK 21 and an Android SDK with platform 37.

```bash
./gradlew assembleDebug   # build
./gradlew test            # unit tests (JVM)
./gradlew lint            # Android lint, warnings are errors
./gradlew connectedDebugAndroidTest   # instrumentation tests, needs a device
```

Point the build at your SDK with a `local.properties` containing
`sdk.dir=/path/to/Android/Sdk`. That file is not committed.

## Status

Scaffold. The domain types and their constraints are in place and tested; the
three top-level destinations exist as placeholders. Brick minting, the Compose
Canvas renderer and the real screens are next — see
[the first-build scope](ANDROID_DESIGN.md#recommended-first-build-scope).

The visual reference material (`bricks.py`, `banners.py`, the HTML prototypes
and SVG catalogs) lives outside this repository. It is reference for the
renderer, not a UI kit to embed.
