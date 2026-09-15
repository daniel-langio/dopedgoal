package app.dopedgoal.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class BrickConstraintsTest {

    private fun paint(coverage: Float) =
        Paint(hue = 20f, saturation = 50f, lightness = 50f, coverage = coverage, pattern = PaintPattern.BANDED)

    private fun brick(
        material: Material = Material.CLAY,
        paint: Paint = paint(0.4f),
        artifact: Artifact = Artifact.NONE,
        wear: Wear = Wear(cracks = 0, chips = 0, kintsugi = false, stain = 0f),
    ) = Brick(
        taskId = "task-1",
        material = material,
        paint = paint,
        wear = wear,
        artifact = artifact,
        aspect = 1f,
        seed = 42L,
    )

    @Test
    fun `paint cannot exceed the common material cap`() {
        assertFailsWith<IllegalArgumentException> { paint(0.9f) }
    }

    @Test
    fun `precious material rejects paint above its own lower cap`() {
        assertFailsWith<IllegalArgumentException> {
            brick(material = Material.GOLD, paint = paint(0.5f))
        }
    }

    @Test
    fun `precious material accepts paint at its cap`() {
        val gold = brick(material = Material.GOLD, paint = paint(PRECIOUS_MAX_COVERAGE))
        assertEquals(Material.GOLD, gold.material)
    }

    @Test
    fun `artifact must be able to grow on its material`() {
        assertFailsWith<IllegalArgumentException> {
            brick(material = Material.DIAMOND, artifact = Artifact(ArtifactKind.MOSS, 2))
        }
    }

    @Test
    fun `artifact compatible with its material is accepted`() {
        val wood = brick(material = Material.WOOD, artifact = Artifact(ArtifactKind.MUSHROOM, 1))
        assertEquals(ArtifactKind.MUSHROOM, wood.artifact.kind)
    }

    @Test
    fun `artifact needs both a kind and a count`() {
        assertFailsWith<IllegalArgumentException> { Artifact(ArtifactKind.MOSS, 0) }
        assertFailsWith<IllegalArgumentException> { Artifact(null, 2) }
    }

    @Test
    fun `precious material neither cracks nor stains`() {
        assertFailsWith<IllegalArgumentException> {
            brick(
                material = Material.SILVER,
                paint = paint(0.1f),
                wear = Wear(cracks = 2, chips = 0, kintsugi = false, stain = 0f),
            )
        }
    }

    @Test
    fun `emblem stays on the brick face and stays legible`() {
        assertFailsWith<IllegalArgumentException> { Emblem("📚", EmblemTreatment.CARVED, atX = 1.4f) }
        assertFailsWith<IllegalArgumentException> { Emblem("📚", EmblemTreatment.CARVED, angle = 75f) }
        val ok = Emblem("📚", EmblemTreatment.GILDED, atX = 0.3f, atY = 0.7f, angle = -38f)
        assertEquals(EmblemTreatment.GILDED, ok.treatment)
    }

    @Test
    fun `every precious material is rarer than every common one`() {
        assertTrue(Material.precious.all { it.rarity < 0.5f })
        assertTrue(Material.common.all { it.rarity >= 0.5f })
        assertEquals(10, Material.entries.size)
        assertEquals(16, PaintPattern.entries.size)
        assertEquals(10, ArtifactKind.entries.size)
    }
}
