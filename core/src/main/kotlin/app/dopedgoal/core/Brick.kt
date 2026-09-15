package app.dopedgoal.core

/** Nominal brick geometry, in the same units the SVG reference generator uses. */
const val BRICK_WIDTH: Float = 120f
const val BRICK_HEIGHT: Float = 52f

enum class PaintPattern {
    BANDED, DIPPED, HALF, SPLATTER, DRIP, WEATHERED, EDGE,
    STRIPES, DIAGONAL, CHECKER, ROLLER, SPRAY, FRAME, CORNER, TAG, CRACKLE,
}

enum class EmblemTreatment { STICKER, STAMPED, CARVED, GILDED }

/**
 * Paint laid over a material. [coverage] is bounded here so no constructed paint
 * can hide the material it sits on; the per-material cap is enforced by [Brick].
 */
data class Paint(
    val hue: Float,
    val saturation: Float,
    val lightness: Float,
    val coverage: Float,
    val pattern: PaintPattern,
) {
    init {
        require(coverage in 0f..MAX_COVERAGE) {
            "coverage $coverage would hide the material"
        }
    }
}

/** Patina, never punishment: cracks, chips, stains and gold-filled kintsugi. */
data class Wear(
    val cracks: Int,
    val chips: Int,
    val kintsugi: Boolean,
    val stain: Float,
) {
    init {
        require(cracks >= 0) { "cracks cannot be negative" }
        require(chips >= 0) { "chips cannot be negative" }
        require(stain in 0f..1f) { "stain $stain is out of range" }
    }
}

/** The user's emoji, placed on the brick face at mint time and never moved again. */
data class Emblem(
    val char: String,
    val treatment: EmblemTreatment,
    val scale: Float = 1f,
    val atX: Float = 0.5f,
    val atY: Float = 0.5f,
    val angle: Float = 0f,
) {
    init {
        require(char.isNotBlank()) { "an emblem needs a character" }
        require(atX in 0f..1f && atY in 0f..1f) { "emblem would sit outside the brick" }
        require(angle in -60f..60f) { "angle $angle is past legible" }
        require(scale > 0f) { "scale $scale is not visible" }
    }
}

/** An organic or found object on the brick surface. [count] of 0 means none. */
data class Artifact(val kind: ArtifactKind?, val count: Int) {
    init {
        require(count >= 0) { "count cannot be negative" }
        require((kind == null) == (count == 0)) {
            "an artifact needs both a kind and a count, or neither"
        }
    }

    companion object {
        val NONE = Artifact(kind = null, count = 0)
    }
}

/**
 * A minted brick. Minting happens once, at task creation, and the result is
 * stored — a brick is never recomputed from screen size, theme or completion date.
 *
 * The constructor rejects the two pairings the design forbids: an artifact that
 * cannot grow on its material, and paint thick enough to bury a precious surface.
 */
data class Brick(
    val taskId: String,
    val material: Material,
    val paint: Paint,
    val wear: Wear,
    val artifact: Artifact,
    val aspect: Float,
    val seed: Long,
    val emblem: Emblem? = null,
) {
    init {
        val kind = artifact.kind
        require(kind == null || kind in material.artifacts) {
            "$kind cannot grow on ${material.name.lowercase()}"
        }
        require(paint.coverage <= material.maxCoverage) {
            "${material.name.lowercase()} cannot be painted over at ${paint.coverage}"
        }
        require(aspect > 0f) { "aspect $aspect is not a brick" }
        if (material.precious) {
            require(wear.cracks == 0) { "${material.name.lowercase()} does not crack" }
            require(wear.stain == 0f) { "${material.name.lowercase()} does not stain" }
        }
    }

    val width: Float get() = BRICK_WIDTH * aspect

    /**
     * The stream for one layer of this brick's appearance — `grain`, `paint`,
     * `crack`, `chip`, `artifact`. Texture geometry is re-derived on every draw
     * rather than stored, so these streams are a frozen contract. See [Rng].
     */
    fun rng(salt: String): Rng = Rng(seed).salted(salt)
}
