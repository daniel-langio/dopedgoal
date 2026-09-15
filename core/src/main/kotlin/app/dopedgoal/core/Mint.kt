package app.dopedgoal.core

/**
 * Minting: turning an identifier into a brick, a banner or a wall anchor.
 *
 * Every draw order in this file is part of the frozen contract described on
 * [Rng]. Inserting a draw shifts every value after it, which would change
 * bricks that already exist. Add new properties at the *end* of a sequence, or
 * on their own salted stream.
 */

/**
 * Scales the precious-material roll. `Material.rarity` reads as a percentage
 * (silver 0.09), and the reference generator divides it by ten before rolling,
 * so silver lands at 0.9% and all four precious materials together at 2.2%.
 *
 * Note that `CONTEXT.md` tabulates the unscaled figures (9% / 6% / 5% / 2%,
 * 22% together). The reference generator's scaled numbers are used here,
 * because 22% precious would stop reading as a rare surprise — but this is the
 * one knob to turn if walls feel too plain.
 */
const val PRECIOUS_RARITY_SCALE: Float = 0.1f

/** How strongly cohesion pulls the paint pattern, as opposed to colour. */
private const val PATTERN_COHESION_BIAS: Float = 0.7f

private const val ARTIFACT_CHANCE: Float = 0.35f
private const val KINTSUGI_CHANCE: Float = 0.12f

private val ASPECTS = listOf(0.5f, 1.0f, 1.0f, 1.0f, 1.5f)
private val COMMON_CRACKS = listOf(0, 0, 1, 1, 2, 3)
private val COMMON_CHIPS = listOf(0, 1, 1, 2)
private val PRECIOUS_CHIPS = listOf(0, 1)
private val BANNER_HOLES = listOf(0, 0, 1, 2)

/**
 * The style anchor for one goal's wall, derived from the goal id. Always a
 * common material: precious materials are a per-brick surprise, never the
 * baseline a whole wall is built from.
 */
fun mintWallStyle(goalId: String): WallStyle {
    val rng = Rng(seedOf(goalId))
    return WallStyle(
        material = rng.choice(Material.common),
        hue = rng.uniform(0f, 360f),
        saturation = rng.uniform(35f, 85f),
        coverage = rng.uniform(0.25f, MAX_COVERAGE),
        pattern = rng.choice(PaintPattern.entries),
    )
}

/**
 * Mints the brick for one task. Called once, at task creation; the result is
 * stored and never recomputed from screen size, theme or completion date.
 *
 * The precious roll happens *before* cohesion is applied, so a rare material can
 * surface on any wall no matter how tame its style anchor.
 */
fun mintBrick(
    taskId: String,
    style: WallStyle,
    cohesion: Cohesion,
    emblemChar: String? = null,
): Brick {
    val seed = seedOf(taskId)
    val rng = Rng(seed)
    val lambda = cohesion.value

    val material = rollMaterial(rng, style, lambda)

    val hue = lerpHue(rng.uniform(0f, 360f), style.hue, lambda)
    val saturation = lerp(rng.uniform(20f, 90f), style.saturation, lambda)
    val coverage = lerp(rng.uniform(0.15f, MAX_COVERAGE), style.coverage, lambda)
    val pattern = if (rng.chance(lambda * PATTERN_COHESION_BIAS)) {
        style.pattern
    } else {
        rng.choice(PaintPattern.entries)
    }

    val paint = Paint(
        hue = hue,
        saturation = saturation,
        lightness = rng.uniform(38f, 62f),
        coverage = minOf(coverage, material.maxCoverage),
        pattern = pattern,
    )

    // Precious materials wear differently: they chip, but they do not crack,
    // stain or take kintsugi. The skipped draws mean a precious brick consumes a
    // shorter stream than a common one — deliberate, and matching the reference.
    val wear = Wear(
        cracks = if (material.precious) 0 else rng.choice(COMMON_CRACKS),
        chips = rng.choice(if (material.precious) PRECIOUS_CHIPS else COMMON_CHIPS),
        kintsugi = rng.chance(KINTSUGI_CHANCE) && !material.precious,
        stain = if (material.precious) 0f else rng.nextFloat(),
    )

    val artifact = if (rng.chance(ARTIFACT_CHANCE)) {
        Artifact(kind = rng.choice(material.artifacts), count = rng.intBetween(1, 3))
    } else {
        Artifact.NONE
    }

    val aspect = rng.choice(ASPECTS)

    return Brick(
        taskId = taskId,
        material = material,
        paint = paint,
        wear = wear,
        artifact = artifact,
        aspect = aspect,
        seed = seed,
        emblem = emblemChar?.let { mintEmblem(it, rng) },
    )
}

/**
 * Rolls the precious materials in declaration order against a single draw, then
 * falls back to the wall's anchor or a free common material depending on
 * cohesion.
 */
private fun rollMaterial(rng: Rng, style: WallStyle, lambda: Float): Material {
    val roll = rng.nextFloat()
    var threshold = 0f
    for (candidate in Material.precious) {
        threshold += candidate.rarity * PRECIOUS_RARITY_SCALE
        if (roll < threshold) return candidate
    }
    return if (rng.chance(lambda)) style.material else rng.choice(Material.common)
}

/** Scatters the emblem across the brick face, kept inside the legible angle. */
private fun mintEmblem(char: String, rng: Rng): Emblem = Emblem(
    char = char,
    treatment = rng.choice(EmblemTreatment.entries),
    scale = 1f,
    atX = rng.nextFloat(),
    atY = rng.nextFloat(),
    angle = rng.uniform(-38f, 38f),
)

/**
 * Mints a goal's banner. The fringe/swallowtail rule is applied by removing
 * fringe from the candidate list *before* choosing, so the invalid pairing is
 * never constructed and then rejected.
 */
fun mintBanner(goalId: String, charge: String): Banner {
    val seed = seedOf(goalId)
    val rng = Rng(seed)

    val silhouette = rng.choice(Silhouette.entries)
    val trims = Trim.entries.filter { !(it == Trim.FRINGE && silhouette == Silhouette.SWALLOWTAIL) }

    return Banner(
        goalId = goalId,
        silhouette = silhouette,
        division = rng.choice(Division.entries),
        cloth = Cloth(
            kind = rng.choice(ClothKind.entries),
            hue = rng.uniform(0f, 360f),
            saturation = rng.uniform(38f, 78f),
            lightness = rng.uniform(34f, 52f),
        ),
        trim = rng.choice(trims),
        fray = Fray(
            holes = rng.choice(BANNER_HOLES),
            hem = rng.nextFloat() * 0.6f,
            fade = rng.nextFloat() * 0.5f,
        ),
        charge = Charge(char = charge, scale = rng.uniform(0.85f, 1.25f), angle = rng.uniform(-8f, 8f)),
        seed = seed,
    )
}
