package app.dopedgoal.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MintTest {

    private val style = mintWallStyle(GOAL)

    /**
     * The golden test. These are not arbitrary numbers — they are what a user
     * with this goal id already has on their wall. A change here is a change to
     * bricks that already exist, so it must be deliberate, never incidental to a
     * refactor.
     */
    @Test
    fun `a known goal mints a known wall style`() {
        assertEquals(Material.WOOD, style.material)
        assertEquals(244.45029f, style.hue, TOLERANCE)
        assertEquals(45.513676f, style.saturation, TOLERANCE)
        assertEquals(0.49741f, style.coverage, TOLERANCE)
        assertEquals(PaintPattern.SPLATTER, style.pattern)
    }

    @Test
    fun `a known task mints a known brick`() {
        val brick = mintBrick("task-1", style, Cohesion.DEFAULT, emblemChar = "📚")

        assertEquals(Material.WOOD, brick.material)
        assertEquals(8861574740880364223L, brick.seed)
        assertEquals(1.0f, brick.aspect)

        assertEquals(199.4172f, brick.paint.hue, TOLERANCE)
        assertEquals(43.36836f, brick.paint.saturation, TOLERANCE)
        assertEquals(59.16693f, brick.paint.lightness, TOLERANCE)
        assertEquals(0.4259346f, brick.paint.coverage, TOLERANCE)
        assertEquals(PaintPattern.CRACKLE, brick.paint.pattern)

        assertEquals(3, brick.wear.cracks)
        assertEquals(0, brick.wear.chips)
        assertEquals(false, brick.wear.kintsugi)
        assertEquals(0.6861671f, brick.wear.stain, TOLERANCE)

        assertEquals(Artifact.NONE, brick.artifact)

        val emblem = assertNotNull(brick.emblem)
        assertEquals(EmblemTreatment.GILDED, emblem.treatment)
        assertEquals(0.043555263f, emblem.atX, TOLERANCE)
        assertEquals(7.680134f, emblem.angle, TOLERANCE)
    }

    @Test
    fun `a known goal mints a known banner`() {
        val banner = mintBanner(GOAL, "📚")
        assertEquals(Silhouette.POINTED, banner.silhouette)
        assertEquals(Division.QUARTERLY, banner.division)
        assertEquals(ClothKind.CANVAS, banner.cloth.kind)
        assertEquals(Trim.FRINGE, banner.trim)
        assertEquals(2, banner.fray.holes)
        assertEquals(1.2422893f, banner.charge.scale, TOLERANCE)
        assertEquals(-3005254734021552841L, banner.seed)
    }

    @Test
    fun `minting is idempotent`() {
        repeat(50) { i ->
            assertEquals(
                mintBrick("task-$i", style, Cohesion.DEFAULT, "📚"),
                mintBrick("task-$i", style, Cohesion.DEFAULT, "📚"),
            )
        }
        assertEquals(mintWallStyle(GOAL), mintWallStyle(GOAL))
        assertEquals(mintBanner(GOAL, "📚"), mintBanner(GOAL, "📚"))
    }

    @Test
    fun `every minted brick across many seeds is constructable`() {
        // Brick's constructor rejects incompatible artifacts and over-coverage,
        // so minting a large sample without throwing is the assertion.
        for (lambda in listOf(0f, 0.3f, 0.6f, 1f)) {
            val cohesion = Cohesion(lambda)
            repeat(5_000) { i ->
                val brick = mintBrick("t-$lambda-$i", style, cohesion, "📚")
                assertTrue(brick.paint.coverage <= brick.material.maxCoverage)
                brick.artifact.kind?.let { assertTrue(it in brick.material.artifacts) }
            }
        }
    }

    @Test
    fun `precious materials stay rare and can appear at any cohesion`() {
        // Scaled rarities: silver 0.9%, gold 0.6%, glass 0.5%, diamond 0.2%.
        for (lambda in listOf(0f, 0.6f, 1f)) {
            val cohesion = Cohesion(lambda)
            val sample = 40_000
            val precious = (0 until sample).count {
                mintBrick("p-$lambda-$it", style, cohesion).material.precious
            }
            val rate = precious.toDouble() / sample
            assertTrue(
                rate in 0.015..0.030,
                "precious rate $rate at cohesion $lambda is outside the expected 2.2% band",
            )
        }
    }

    @Test
    fun `full cohesion pulls common bricks onto the wall style`() {
        val tame = (0 until 2_000)
            .map { mintBrick("tame-$it", style, Cohesion(1f)) }
            .filter { !it.material.precious }
        assertTrue(tame.all { it.material == style.material }, "λ=1 left a common brick off-anchor")

        val wild = (0 until 2_000)
            .map { mintBrick("wild-$it", style, Cohesion(0f)) }
            .filter { !it.material.precious }
        assertTrue(
            wild.map { it.material }.toSet().size > 1,
            "λ=0 should not converge on one material",
        )
    }

    @Test
    fun `a wall anchor is never a precious material`() {
        repeat(2_000) { assertTrue(!mintWallStyle("anchor-$it").material.precious) }
    }

    @Test
    fun `fringe never lands on a swallowtail`() {
        var swallowtails = 0
        repeat(5_000) { i ->
            val banner = mintBanner("banner-$i", "📚")
            if (banner.silhouette == Silhouette.SWALLOWTAIL) {
                swallowtails++
                assertTrue(banner.trim != Trim.FRINGE)
            }
        }
        assertTrue(swallowtails > 0, "no swallowtails were minted, so nothing was exercised")
    }

    private companion object {
        const val GOAL = "goal-learn-kotlin"
        const val TOLERANCE = 1e-4f
    }
}
