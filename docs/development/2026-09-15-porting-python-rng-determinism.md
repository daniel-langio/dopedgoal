# Porting `mint()` cannot reuse Python's RNG stream

- Date: 2026-09-15
- Status: investigating
- Area: brick renderer, domain model
- Related issue/PR: initial Android scaffold

## Symptoms

Not a failure yet — a blocker identified while scoping the scaffold, which is
why `:core` currently ships the brick *types* and their constraints but no
`mint()` function.

`bricks.py` mints every brick by drawing a fixed sequence of values from
`random.Random(seed)`: the rare-material roll, then hue, saturation, coverage,
pattern, wear, artifact and aspect, in that exact order. A goal's wall is
reproducible only because that stream is reproducible.

Kotlin has no equivalent of `random.Random`. `kotlin.random.Random` is a
different generator entirely, and `java.util.Random` is a 48-bit LCG. Neither
reproduces CPython's Mersenne Twister, and `random.uniform`/`choice`/`randint`
each consume a generator-specific number of words per call.

## Cause

Determinism in this product is a hard requirement, not an optimisation: a brick
is minted once at task creation and must never change. Any port therefore has to
decide whether "deterministic" means *identical to the Python reference* or
merely *stable on Android*.

## Resolution

Undecided. Two candidates:

1. **Reimplement MT19937 plus CPython's `uniform`/`choice`/`randint` draw order
   in Kotlin.** Bricks match the Python reference and the SVG catalogs exactly,
   so `features.svg` and friends stay valid fixtures. Costs a non-trivial amount
   of exactly-right code, and locks the draw order forever.
2. **Define the Kotlin generator as authoritative** and treat the Python files
   purely as visual reference. Cheaper and freer, but the reference renders stop
   being comparable output and the two implementations drift.

Option 2 looks right for a local-first first release, since no minted brick has
shipped to a user yet and nothing needs to survive a cross-implementation
comparison. It should be decided before the first real goal is persisted, not
after — the choice becomes irreversible the moment a user owns a wall.

## Verification

None yet. Whichever option wins needs a golden-file test: a fixed goal id and
cohesion producing a byte-stable list of minted bricks, so a future refactor
cannot quietly change anyone's existing wall.

## Follow-up

Decide before implementing `mint()`. The persisted schema should store the
minted brick's resolved properties, not just its seed, so a generator change
cannot retroactively alter walls that already exist regardless of which option
is chosen.
