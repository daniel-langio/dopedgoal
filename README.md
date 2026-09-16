# Doped Goal

A goal is a wall. A task is a brick. Finishing a task places its brick, and
finishing every task fills the wall.

Built for ADHD users: progress is additive, nothing decays, no streak can break,
and no brick is ever lost.

## Documentation

- [`ANDROID_DESIGN.md`](ANDROID_DESIGN.md) — design direction, screens, tokens, accessibility rules
- [`CONTRIBUTING.md`](CONTRIBUTING.md) — commit conventions, CI expectations, development notes
- [`docs/IMPLEMENTATION.md`](docs/IMPLEMENTATION.md) — what to build next, and the constraints that govern it
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

## Releases

Every push to `main` builds a release APK through
[`build-apk.yml`](.github/workflows/build-apk.yml) and attaches it to a GitHub
release. Nothing about the version is maintained by hand.

| | Source | Why |
| --- | --- | --- |
| `versionCode` | `git rev-list --count HEAD` | Auto-increments per commit and is strictly monotonic. Android refuses to install an APK whose `versionCode` is not greater than the installed one, so every build is sideloadable over the last. |
| `versionName` | `version.properties`, bumped by commit type | `feat:` bumps the minor, `!:` or a `BREAKING CHANGE:` footer bumps the major, anything else bumps the patch. |

The bump is persisted only after the APK builds, as
`chore(release): vX.Y.Z [skip ci]`, then tagged and released. The `[skip ci]`
marker is load-bearing: without it that push would re-trigger the workflow and
bump forever.

Because the release bot pushes to `main`, your next local push can be rejected
as behind. Rebase onto it — the bot's commit touches `version.properties` only.

### Signing

The workflow signs with a real keystore when these repository secrets exist, and
otherwise falls back to the debug key, which is installable for testing but not
distributable:

| Secret | Contents |
| --- | --- |
| `RELEASE_KEYSTORE_BASE64` | the keystore, base64-encoded |
| `RELEASE_KEYSTORE_PASSWORD` | keystore password |
| `RELEASE_KEY_ALIAS` | key alias |
| `RELEASE_KEY_PASSWORD` | key password |

```bash
keytool -genkey -v -keystore release.jks -keyalg RSA -keysize 2048 \
  -validity 10000 -alias dopedgoal
base64 -w0 release.jks   # paste into RELEASE_KEYSTORE_BASE64
```

Keep the keystore itself out of the repository. Losing it means never being able
to update an already-installed app.

## Status

Pre-MVP. The domain types, their constraints and deterministic minting are in
place and tested; the three top-level destinations exist as placeholders. The
Compose Canvas renderer, local persistence and the real screens are next — see
[the first-build scope](ANDROID_DESIGN.md#recommended-first-build-scope).

Minting is deterministic from a frozen generator contract — read the note on
[`Rng`](core/src/main/kotlin/app/dopedgoal/core/Rng.kt) before changing anything
it touches.

The visual reference material (`bricks.py`, `banners.py`, the HTML prototypes
and SVG catalogs) lives outside this repository. It is reference for the
renderer, not a UI kit to embed.
