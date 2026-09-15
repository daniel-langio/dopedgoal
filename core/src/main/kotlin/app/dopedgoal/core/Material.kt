package app.dopedgoal.core

/** Paint may never fully hide a common material. Mirrors `MAX_COVERAGE` in `bricks.py`. */
const val MAX_COVERAGE: Float = 0.72f

/** Precious materials keep far more of their surface bare. */
const val PRECIOUS_MAX_COVERAGE: Float = 0.18f

enum class Grain { PIT, SPECKLE, FIBER, VEIN, TILE, SHEEN, GLASS, FACET }

enum class ArtifactKind {
    MOSS, LICHEN, WEED, IVY, MUSHROOM, MAGGOT, SNAIL, COIN, FOSSIL, ENGRAVING,
}

/**
 * The ten brick materials. [rarity] is the roll weight used before cohesion is
 * applied, so a precious brick can surface on any wall.
 */
enum class Material(
    val hue: Float,
    val saturation: Float,
    val lightness: Float,
    val grain: Grain,
    val artifacts: Set<ArtifactKind>,
    val rarity: Float = 1.0f,
) {
    CONCRETE(30f, 7f, 60f, Grain.PIT, setOf(ArtifactKind.MOSS, ArtifactKind.LICHEN, ArtifactKind.WEED)),
    CLAY(14f, 44f, 50f, Grain.SPECKLE, setOf(ArtifactKind.MOSS, ArtifactKind.SNAIL, ArtifactKind.IVY, ArtifactKind.MAGGOT)),
    WOOD(28f, 36f, 40f, Grain.FIBER, setOf(ArtifactKind.MUSHROOM, ArtifactKind.MAGGOT, ArtifactKind.IVY)),
    MARBLE(220f, 9f, 84f, Grain.VEIN, setOf(ArtifactKind.ENGRAVING, ArtifactKind.LICHEN)),
    MOSAIC(198f, 24f, 55f, Grain.TILE, setOf(ArtifactKind.COIN, ArtifactKind.LICHEN)),
    SANDSTONE(38f, 38f, 66f, Grain.SPECKLE, setOf(ArtifactKind.FOSSIL, ArtifactKind.MOSS)),
    SILVER(212f, 10f, 74f, Grain.SHEEN, setOf(ArtifactKind.ENGRAVING, ArtifactKind.LICHEN), rarity = 0.09f),
    GOLD(44f, 70f, 56f, Grain.SHEEN, setOf(ArtifactKind.ENGRAVING, ArtifactKind.COIN), rarity = 0.06f),
    GLASS(184f, 34f, 80f, Grain.GLASS, setOf(ArtifactKind.ENGRAVING), rarity = 0.05f),
    DIAMOND(192f, 55f, 88f, Grain.FACET, setOf(ArtifactKind.ENGRAVING), rarity = 0.02f);

    val precious: Boolean get() = rarity < 0.5f

    val maxCoverage: Float get() = if (precious) PRECIOUS_MAX_COVERAGE else MAX_COVERAGE

    companion object {
        val common: List<Material> = entries.filter { !it.precious }
        val precious: List<Material> = entries.filter { it.precious }
    }
}
