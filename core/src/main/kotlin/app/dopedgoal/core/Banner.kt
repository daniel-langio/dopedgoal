package app.dopedgoal.core

enum class Silhouette { SQUARE, POINTED, SWALLOWTAIL, ROUNDED, SCALLOPED }

enum class Division { PLAIN, PER_PALE, PER_FESS, QUARTERLY, CHEVRON, SALTIRE, BENDY }

enum class Cloth { LINEN, SILK, VELVET, CANVAS }

enum class Trim { NONE, BORDER, FRINGE, TASSELS }

/** Hem wear on a banner — patina, not a progress penalty. */
data class Fray(val holes: Int, val hemFade: Float) {
    init {
        require(holes >= 0) { "holes cannot be negative" }
        require(hemFade in 0f..1f) { "hemFade $hemFade is out of range" }
    }
}

/**
 * A goal's banner, minted from the goal id and reused unchanged on every surface.
 * The banner is identity, not per-screen decoration.
 */
data class Banner(
    val goalId: String,
    val silhouette: Silhouette,
    val division: Division,
    val cloth: Cloth,
    val trim: Trim,
    val fray: Fray,
    val charge: String,
    val seed: Long,
) {
    init {
        require(charge.isNotBlank()) { "a banner needs a charge" }
        require(!(trim == Trim.FRINGE && silhouette == Silhouette.SWALLOWTAIL)) {
            "fringe cannot hang from a swallowtail"
        }
    }
}
