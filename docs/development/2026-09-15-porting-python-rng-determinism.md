# Porting `mint()` cannot reuse Python's RNG stream

- Date: 2026-09-15
- Status: resolved
- Area: brick renderer, domain model
- Related issue/PR: `feat/deterministic-minting`

## Symptoms

`bricks.py` mints every brick by drawing a fixed sequence of values from
`random.Random(seed)`, and a wall is reproducible only because that stream is
reproducible.

Kotlin has no equivalent. `kotlin.random.Random` is a different algorithm and
`java.util.Random` is a 48-bit LCG; neither reproduces CPython's Mersenne
Twister. The draws are not uniform in cost either — `uniform()` consumes exactly
one 53-bit float, but `choice()` routes through `_randbelow(n)`, which
rejection-samples (`k = n.bit_length(); r = getrandbits(k); while r >= n: ...`)
and so consumes a *variable* number of 32-bit words. Counting calls is not
enough; the rejection loop has to be replicated or the stream desynchronises and
every value after it shifts.

## Cause

Determinism is a hard product requirement: a brick is minted once at task
creation and must never change. The question was whether "deterministic" has to
mean *identical to the Python reference* or merely *stable on Android*.

An earlier revision of this note located the risk in `mint()`, and claimed that
persisting resolved brick properties would protect against a generator change.
That is only half right, and it is the less important half. Randomness is not
confined to minting:

| | draw sites | persisted? |
| --- | --- | --- |
| `mint()` | 18 | yes, once resolved properties are stored |
| `BrickPainter` | 97 | **no** — re-derived from the seed on every draw |

Every speckle position, crack vertex, splatter dot and chip outline is generated
at *render* time from `brick.rng(salt)`. Storage does nothing for those. What is
actually frozen forever is **the renderer's generator and its draw order**, not
`mint()`.

Reimplementing MT19937 is also less work than first assumed — roughly 150 lines
with published test vectors. The real cost of parity is not the generator, it is
that the Compose renderer's internal structure would be locked to the Python
painter's structure.

## Resolution

**The Kotlin generator is authoritative. The Python files are visual reference
for *how* a brick is drawn, not a source of *which* brick is drawn.**

What settled it: `ANDROID_DESIGN.md` already requires that list-thumbnail
rendering "simplify texture and artifacts to protect performance". Any such LOD
path makes fewer draws than the Python painter, so the streams diverge and
parity is lost the moment that documented requirement is implemented. Byte-exact
parity is not merely expensive, it is incompatible with the design.

Implemented in `:core`:

- `Rng` — SplitMix64, chosen because it is small enough to verify by eye, well
  distributed, and stable for every seed including zero. Verified against the
  published reference vectors for seed 0.
- `seedOf(text)` — first eight bytes of SHA-256, big endian. Stable across
  processes and platforms, unlike `String.hashCode`.
- `Rng.salted(salt)` — one stream per visual layer (`grain`, `paint`, `crack`,
  `chip`, `artifact`), carried over from the reference design. This is the
  property that makes the frozen contract survivable: a *new* visual layer can
  be added under a *new* salt without shifting any value an existing layer sees.
- `mintWallStyle`, `mintBrick`, `mintBanner` in `Mint.kt`, mirroring the
  reference's decision structure and ordering.

Rejected: reimplementing MT19937 and CPython's `_randbelow` semantics for exact
parity. It buys the ability to diff renderer bugs against the committed SVG
catalogs, which is a real but survivable loss, and costs a permanent constraint
on how the renderer may be written.

## Verification

`./gradlew :core:test`. The suite pins the contract from three directions:

- **Reference vectors** — `Rng(0)` reproduces the published SplitMix64 outputs,
  and `seedOf` is pinned to exact longs.
- **Golden values** — a fixed goal and task id assert exact material, paint,
  wear, artifact, aspect, emblem and banner values. These are what a user with
  that id already has on their wall; changing them changes existing bricks.
- **Invariants over large samples** — 20,000 mints across four cohesion values
  all construct successfully, so no combination violates the artifact/material
  or coverage rules. Precious materials land at 2.19% (silver 0.89%, gold 0.59%,
  glass 0.51%, diamond 0.21%) and stay in band at every cohesion, confirming the
  rare roll really does precede cohesion. λ=1 pulls every common brick onto the
  anchor; λ=0 does not converge. Fringe never lands on a swallowtail.

## Follow-up

- **The painter is not yet covered.** The golden tests pin `mint()`; the 97
  render-time draws have nothing holding them still, and that is the half that
  cannot be repaired after the fact. The Compose renderer needs a golden test
  that hashes a rendered bitmap for a fixed brick, added in the same change that
  introduces the renderer — not later.
- **Persist resolved brick properties, not just the seed.** Cheap insurance that
  permanently immunises the `mint()` half against any future generator change.
- **Rarity discrepancy.** `CONTEXT.md` tabulates silver 9% / gold 6% / glass 5% /
  diamond 2%, but the reference generator scales `rarity` by 1/10 before rolling,
  giving 0.9% / 0.6% / 0.5% / 0.2% and 2.2% precious overall. The generator's
  numbers are used, since 22% precious would stop reading as a rare surprise.
  Exposed as `PRECIOUS_RARITY_SCALE` if that judgement turns out to be wrong.
