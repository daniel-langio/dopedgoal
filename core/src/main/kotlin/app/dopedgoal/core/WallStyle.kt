package app.dopedgoal.core

/** Recommended default cohesion: bricks vary, but the wall still reads as one wall. */
const val DEFAULT_COHESION: Float = 0.6f

/**
 * The per-goal style anchor every brick on that wall leans toward.
 *
 * Cohesion λ ∈ [0,1] decides how far: λ=0 leaves bricks fully random, λ=1 makes
 * them near-identical. λ is applied *after* the precious-material roll, so rare
 * materials remain possible on any wall.
 */
data class WallStyle(
    val material: Material,
    val hue: Float,
    val saturation: Float,
    val coverage: Float,
    val pattern: PaintPattern,
) {
    init {
        require(!material.precious) { "a wall anchor must be a common material" }
        require(coverage in 0f..MAX_COVERAGE) { "coverage $coverage would hide the material" }
    }
}

@JvmInline
value class Cohesion(val value: Float) {
    init {
        require(value in 0f..1f) { "cohesion $value is outside [0,1]" }
    }

    companion object {
        val DEFAULT = Cohesion(DEFAULT_COHESION)
    }
}
