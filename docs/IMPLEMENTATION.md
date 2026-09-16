# Implementation plan

Written 2026-09-15. This document assumes no prior context: it is what a fresh
session needs to continue building Doped Goal.

---

## 1. Read this first

### The design source is not in this repository

The authoritative design material lives at **`/srv/download/goal-app-context`**:

| File | What it is |
| --- | --- |
| `CONTEXT.md` | Domain model, brick system, materials, animations, wall layout |
| `bricks.py` | Reference brick generator — `mint()` plus `BrickPainter` (~750 lines) |
| `banners.py` | Reference banner generator |
| `features.svg`, `paints.svg`, `materials.svg`, `banners.svg` | Rendered catalogs of every feature |
| `create-goal.html`, `wall-animated.html`, `brick-placing.html`, `artifacts-animated.html` | Interactive prototypes |

Read `CONTEXT.md` and the relevant part of `bricks.py` before touching the
renderer or the domain model. Nothing in this repository points at that path on
its own.

In this repository, [`ANDROID_DESIGN.md`](../ANDROID_DESIGN.md) is the design
direction — screens, tokens, accessibility rules, copy tone. It overrides the
HTML prototypes wherever they disagree, because the prototypes are web.

### The Python files are reference, not truth

**The Kotlin generator is authoritative.** The Python files show *how* a brick is
drawn, never *which* brick is drawn. CPython's Mersenne Twister has no JVM
equivalent, and byte-exact parity is incompatible with the thumbnail LOD
simplification `ANDROID_DESIGN.md` requires. Full reasoning in
[`docs/development/2026-09-15-porting-python-rng-determinism.md`](development/2026-09-15-porting-python-rng-determinism.md).

### `Rng` is a frozen contract

Read the KDoc on
[`core/src/main/kotlin/app/dopedgoal/core/Rng.kt`](../core/src/main/kotlin/app/dopedgoal/core/Rng.kt)
before changing anything that draws from it.

A brick is minted once and must look the same forever. Two consequences:

- **Draw order is part of the contract.** Inserting a draw shifts every value
  after it, changing bricks that already exist. Add new properties at the *end*
  of a sequence.
- **Each visual layer has its own salted stream** — `grain`, `paint`, `crack`,
  `chip`, `artifact`, via `brick.rng(salt)`. This is what makes the contract
  survivable: a *new* layer under a *new* salt shifts nothing.

### The unpinned half

`mint()` has 18 draw sites and its output is storable. `BrickPainter` has **97**
that are re-derived from the seed on every single draw and never persisted.

Golden tests currently cover `mint()` only. **The renderer's bitmap golden test
must land in the same PR as the renderer** — once bricks exist on user devices,
a renderer refactor that shifts a stream silently rewrites their walls, and
there is no repair.

---

## 2. Where the project is now

Merged and released: `v0.1.1` (scaffold + CI), `v0.2.0` (minting).

| Module | State |
| --- | --- |
| `:core` | Pure Kotlin/JVM. Domain model with constraints, plus deterministic minting. Tested. **Done for now.** |
| `:app` | Compose + Material 3 theme with the design tokens. Three bottom-nav destinations, all placeholder text. **`:core` is never called from the UI.** |

What exists in `:core`: `Material`, `PaintPattern`, `ArtifactKind`, `Paint`,
`Wear`, `Emblem`, `Artifact`, `Brick`, `WallStyle`, `Cohesion`, `Banner`,
`Cloth`, `Fray`, `Charge`, `Goal`, `Task`, `Rng`, `seedOf`, `mintWallStyle`,
`mintBrick`, `mintBanner`.

Invalid states are unconstructable: an artifact only on a material it can grow
on, coverage capped per material, precious materials neither crack nor stain,
fringe never on a swallowtail.

What does **not** exist: any renderer, any persistence, any real screen, any way
to create a goal.

---

## 3. PR 1 — Compose Canvas renderer

The largest and riskiest piece. Nothing else can be built first, because the
create-goal screen's live ghost wall needs it.

### Scope

New package `app/src/main/kotlin/app/dopedgoal/ui/wall/`:

- `BrickCanvas.kt` — draws one `Brick` into a `DrawScope`
- `WallCanvas.kt` — running-bond layout of placed and ghost bricks
- `BannerCanvas.kt` — draws one `Banner`
- `Hsl.kt` — HSL→`Color`; materials and paint are specified in HSL, Compose is not

### Rules

Draw in this order, matching the reference: **material base → grain → paint →
degradation (cracks, chips, stains, kintsugi) → artifact → emblem → border.**

- Use the existing salts: `brick.rng("grain")`, `"paint"`, `"crack"`, `"chip"`,
  `"artifact"`. Do not invent new salts for existing layers.
- Common-material paint coverage ≤ 72%, precious ≤ 18%. Already enforced in the
  type; do not re-derive it in the renderer.
- **LOD:** at thumbnail size (≲64dp) simplify texture and drop artifacts. The
  emoji emblem and completion state must survive at 48dp.
- Wall layout: running bond, odd rows offset half a brick, bottom-up fill,
  occasional seeded dropped brick at a row edge for an organic top silhouette.
- Ghost bricks are dashed outlines, ≥3:1 non-text contrast against mortar.
- Mortar and ghost colours come from `MaterialTheme.wall` (`WallColors` in
  `ui/theme/Wall.kt`), deliberately outside the Material scheme.
- Brick corner radius 3–5dp, so bricks read as a different material from cards.

### Acceptance

- [ ] A bitmap golden test for a fixed brick — render to `ImageBitmap`, hash,
      assert. This belongs in `androidTest`; CI already runs instrumentation
      tests on an emulator. **Not optional, not deferrable.**
- [ ] Golden test for a wall of ≥12 bricks covering: a precious material, an
      artifact, kintsugi, an aspect-0.5 and an aspect-1.5 brick.
- [ ] Renders correctly in light and dark.
- [ ] No text drawn over patterned bricks.

Reference: `bricks.py` lines 223–698 (`BrickPainter`), `banners.py` line 100+
(`BannerPainter`), and the SVG catalogs for what each feature should look like.

---

## 4. PR 2 — Local persistence

First release is local-first: goals work with no account and no connection.

### Open decision, resolve at the start of the PR

**Room vs DataStore.** Recommendation: **Room**, for the query and migration
story. "No lost bricks" is a hard product rule, and a schema change that loses a
wall is unacceptable. DataStore has no migration testing story worth the name.

### Rules

- **Persist resolved brick properties, not just the seed.** This is the cheap
  insurance that permanently immunises minted bricks against any future
  generator change. Storing only a seed re-derives the brick on every read,
  which reintroduces exactly the risk `Rng`'s contract exists to contain.
- Store the completion *timestamp* per task. Wall fill order is completion
  order, not list order — `Goal.placedBricks` already sorts by it.
- Export the Room schema and commit it; add a migration test from day one.
- Repository exposes `Flow`; no persistence types leak into `:core` or the UI.

### Acceptance

- [ ] Create, read, update, delete goals and tasks
- [ ] A goal survives process death and app restart
- [ ] In-memory Room tests for the DAO
- [ ] A migration test exists, even with only one version

---

## 5. PR 3 — Create flow, goal detail, completion

This is the PR that makes the app usable. Split it if it grows past review size;
`5a` create flow, `5b` detail + completion is a clean seam.

### Create goal — two stages, never one long form

**Step 1, "What are you building?"** — goal name (autofocus), category chips
(Health, Learning, Career, Creative, Social, Personal), emoji picker row plus
"More emoji", live banner preview. Primary button: **Choose the bricks**.

**Step 2, "Choose the bricks"** — live ghost wall pinned below the top app bar,
label "Small, finishable steps work best.", one row per task (ordinal, one-line
field, delete with a 48dp target), **Add a brick** appends and focuses, cohesion
under a "Wall character" heading as `wild ◯────●──── tame` defaulting to 60%
(never the word "lambda"), footer CTA **Create goal · N bricks** disabled until
a name and ≥1 non-blank task exist.

Over 30 bricks, warn once — "This is a big wall. Want to split it into two
goals?" — and keep **Create anyway** available. Supporting ADHD users means
never taking control away.

### Goal detail

Banner plus wall ≈38% of the viewport on first view, then scrolls; **not**
permanently sticky. NEXT BRICK card 64dp; other task rows 52dp with `✓`/`○` and
an ordinal. Tapping an incomplete task opens a bottom sheet with a **Place
brick** action, which prevents accidental completion.

### Completion

**The next open wall slot is assigned at completion time**, not creation time,
so build order reflects what the user actually did. Complete on tap, with a
5-second **Undo** snackbar. Snackbar copy: "Brick placed · 5 of 12".

Placement animation may ship simplified in this PR (the full sequence is a
follow-up), but it must be skippable, and `prefers-reduced-motion` must give a
150ms cross-fade with no shake, splatter or idle motion.

### Today screen

Next-brick card using the real minted brick at 56dp, then in-progress goal cards
with banner thumbnail, `placed / total` and a miniature wall. Empty state: one
ghost brick and "Your first brick is waiting." Never a blank checklist.

### Acceptance

- [ ] A goal can be created, a task completed, and the brick appears on the wall
- [ ] State survives restart
- [ ] Undo restores the unplaced state
- [ ] TalkBack announces placement and completion via live regions
- [ ] Dynamic type to 200% without silently truncating task labels
- [ ] Status is never conveyed by colour alone — pair with `✓`/`○` and text

---

## 6. Standing constraints

From `ANDROID_DESIGN.md`, applying to every PR:

- Progress is additive. **No streaks, decay, red warning states, or "behind"
  language.** Never use overdue, failure or loss framing in copy.
- Gold is for rare material and completion only, never for emphasis.
- All touch targets ≥48×48dp.
- Respect system dark theme; brick art stays physically rich rather than
  inverted.
- A brick is deterministic from its minted seed — never recomputed from screen
  size, current theme, or completion date.
- Use the same banner seed on every surface for a goal. The banner is identity.

---

## 7. Build, CI and release

```bash
./gradlew assembleDebug   # build
./gradlew test            # JVM unit tests
./gradlew lint            # warnings are errors
./gradlew connectedDebugAndroidTest   # needs a device or emulator
```

`local.properties` needs `sdk.dir=/path/to/Android/Sdk` and is not committed.

CI runs build, unit tests, lint and emulator instrumentation tests on every PR.
Prefer pushing and reading CI over a full local run.

**Every push to `main` cuts a release.** `versionCode` is the commit count;
`versionName` is bumped from the merge commit's conventional-commit type
(`feat:` minor, `!:` or a `BREAKING CHANGE:` footer major, else patch). So PR
titles directly set the released version number.

### Traps already paid for

Each of these cost time once. All are written up in
[`docs/development/`](development/README.md).

- **Never put a bracketed skip-ci token in a commit message**, not even while
  describing one. GitHub matches it anywhere in the message and silently
  schedules no run at all — no failure, no annotation. Write `skip-ci` unbracketed.
- **AGP 9 rejects the `kotlin-android` plugin.** Kotlin support is built in.
  `:app` applies only `com.android.application` and the Compose plugin.
- **`ObsoleteSdkInt` is disabled** for `:app`. Lint says `mipmap-anydpi-v26` is
  redundant at `minSdk 26`; following that advice makes the resource merger
  silently discard the launcher icons and the build fails at resource linking.
- **Lint version-currency checks are `informational`**, because they reach the
  network and an upstream release alone would fail an unchanged build.

---

## 8. Open items

| Item | Status |
| --- | --- |
| **Release signing** | No keystore configured, so published APKs are debug-signed. They install, but a later properly-signed build cannot upgrade over them — users must uninstall first. Configure before sharing outside the team; setup is in the README. |
| **Room vs DataStore** | Decide at the start of PR 2. Recommendation: Room. |
| **Precious rarity** | `CONTEXT.md` tabulates silver 9% / gold 6% / glass 5% / diamond 2%. The reference generator scales by 1/10, giving 2.2% precious overall, which is what `PRECIOUS_RARITY_SCALE` implements. If walls feel too plain, that constant is the knob. |
| **Emblem treatment choice** | Currently uniform over sticker / stamped / carved / gilded. No product rule was specified — a material-aware rule may read better (gilded on precious, carved on stone). |
| **Placement animation** | Full 3.6s sequence deferred. Timings are in `CONTEXT.md` and `ANDROID_DESIGN.md`; `brick-placing.html` is the reference. |
