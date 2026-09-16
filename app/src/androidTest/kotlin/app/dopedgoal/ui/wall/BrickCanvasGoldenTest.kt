package app.dopedgoal.ui.wall

import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import app.dopedgoal.core.Cohesion
import app.dopedgoal.core.mintBrick
import app.dopedgoal.core.mintWallStyle
import app.dopedgoal.ui.theme.DopedGoalTheme
import java.nio.ByteBuffer
import java.util.zip.CRC32
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * A bitmap golden test for the renderer. Per docs/IMPLEMENTATION.md: "the
 * renderer's bitmap golden test must land in the same PR as the renderer" —
 * `BrickPainter`'s ~97 draw sites are re-derived from the seed on every draw
 * and never persisted, so a silent shift here silently rewrites walls that
 * already exist on a user's device once this ships.
 *
 * Fixture is the exact one already locked down in
 * core/src/test/kotlin/app/dopedgoal/core/MintTest.kt: goal "goal-learn-kotlin",
 * task "task-1", default cohesion, emblem "📚". That brick's domain properties
 * (material WOOD, paint pattern CRACKLE, wear.stain ~0.686, emblem GILDED, ...)
 * are already asserted there, so this test only has to prove the *pixels* for
 * that already-frozen brick stay put.
 */
class BrickCanvasGoldenTest {

    @get:Rule
    val composeRule = createComposeRule()

    // createComposeRule() rather than createAndroidComposeRule<MainActivity>():
    // it still hosts content inside a real, windowed test Activity (it just
    // doesn't require a caller-defined Activity subclass), so
    // drawContext.canvas.nativeCanvas text drawing (the emblem layer) has a
    // real hardware-accelerated Canvas underneath. No fallback was needed.

    @Test
    fun fullLodBrickMatchesGoldenHash() {
        val style = mintWallStyle(GOAL)
        val brick = mintBrick("task-1", style, Cohesion.DEFAULT, emblemChar = "📚")

        composeRule.setContent {
            DopedGoalTheme {
                BrickCanvas(brick, modifier = Modifier.size(120.dp, 52.dp), lod = BrickLod.Full)
            }
        }

        val hash = composeRule.onRoot().captureToImage().toPixelMap().crc32()

        assertEquals(FULL_LOD_HASH, hash)
    }

    @Test
    fun thumbnailLodBrickMatchesGoldenHash() {
        val style = mintWallStyle(GOAL)
        val brick = mintBrick("task-1", style, Cohesion.DEFAULT, emblemChar = "📚")

        composeRule.setContent {
            DopedGoalTheme {
                // 48dp x 20.8dp preserves the brick's aspect at thumbnail size.
                BrickCanvas(brick, modifier = Modifier.size(48.dp, 20.8.dp), lod = BrickLod.Thumbnail)
            }
        }

        val hash = composeRule.onRoot().captureToImage().toPixelMap().crc32()

        assertEquals(THUMBNAIL_LOD_HASH, hash)
    }

    private companion object {
        const val GOAL = "goal-learn-kotlin"

        const val FULL_LOD_HASH = 2715681868L
        const val THUMBNAIL_LOD_HASH = 4217217427L
    }
}

/**
 * CRC32 over the captured bitmap's raw ARGB ints. Simple, stable across JVM
 * runs, and sensitive to any pixel change anywhere in the image.
 */
private fun PixelMap.crc32(): Long {
    val buffer = ByteBuffer.allocate(4 * width * height)
    for (y in 0 until height) {
        for (x in 0 until width) {
            buffer.putInt(this[x, y].toArgb())
        }
    }
    val crc = CRC32()
    crc.update(buffer.array())
    return crc.value
}
