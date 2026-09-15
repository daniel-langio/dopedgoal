package app.dopedgoal.core

enum class Silhouette { SQUARE, POINTED, SWALLOWTAIL, ROUNDED, SCALLOPED }

enum class Division { PLAIN, PER_PALE, PER_FESS, QUARTERLY, CHEVRON, SALTIRE, BENDY }

enum class ClothKind { LINEN, SILK, VELVET, CANVAS }

enum class Trim { NONE, BORDER, FRINGE, TASSELS }

/** The banner's fabric, carrying its own colour rather than borrowing the wall's. */
data class Cloth(
    val kind: ClothKind,
    val hue: Float,
    val saturation: Float,
    val lightness: Float,
) {
    init {
        require(hue in 0f..360f) { "hue $hue is off the wheel" }
        require(saturation in 0f..100f) { "saturation $saturation is out of range" }
        require(lightness in 0f..100f) { "lightness $lightness is out of range" }
    }
}

/** Hem wear on a banner — patina, not a progress penalty. */
data class Fray(val holes: Int, val hem: Float, val fade: Float) {
    init {
        require(holes >= 0) { "holes cannot be negative" }
        require(hem in 0f..1f) { "hem $hem must be a fraction" }
        require(fade in 0f..1f) { "fade $fade must be a fraction" }
    }
}

/** The goal's emoji, worn at banner scale. */
data class Charge(val char: String, val scale: Float = 1f, val angle: Float = 0f) {
    init {
        require(char.isNotBlank()) { "a charge needs a character" }
        require(scale > 0f) { "scale $scale is not visible" }
    }
}

/**
 * A goal's banner, minted from the goal id and reused unchanged on every
 * surface. The banner is identity, not per-screen decoration.
 */
data class Banner(
    val goalId: String,
    val silhouette: Silhouette,
    val division: Division,
    val cloth: Cloth,
    val trim: Trim,
    val fray: Fray,
    val charge: Charge,
    val seed: Long,
) {
    init {
        require(!(trim == Trim.FRINGE && silhouette == Silhouette.SWALLOWTAIL)) {
            "fringe cannot follow a swallowtail hem"
        }
    }

    /** The stream for one layer of this banner's appearance. See [Rng.salted]. */
    fun rng(salt: String): Rng = Rng(seed).salted(salt)
}
