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
import app.dopedgoal.core.Brick
import app.dopedgoal.core.Cohesion
import app.dopedgoal.core.mintBanner
import app.dopedgoal.core.mintBrick
import app.dopedgoal.core.mintWallStyle
import app.dopedgoal.ui.theme.DopedGoalTheme
import java.nio.ByteBuffer
import java.util.zip.CRC32
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Bitmap golden tests for [WallCanvas] and [BannerCanvas], following the exact
 * pattern in `BrickCanvasGoldenTest.kt`: no emulator is available in this
 * sandbox, so each hash is a documented `TODO(golden)` placeholder. Run once
 * in CI (or on any real device/emulator), read the actual hash off the
 * assertion failure, and replace the placeholder — see that file's class doc
 * for why this must never be faked.
 *
 * Fixture: goal "goal-learn-kotlin", the same wall style `MintTest.kt` already
 * locks down. Task ids `task-0` through `task-12` plus `task-48` were picked
 * by a throwaway local search (not committed) over sequential task ids to
 * cover, in one 14-brick wall: a precious material (`task-48`, gold), a
 * non-empty artifact (`task-3`, lichen), `wear.kintsugi == true` (`task-12`),
 * `aspect == 0.5f` (`task-5`) and `aspect == 1.5f` (`task-0`/`task-12`). Since
 * minting is deterministic, this fixture is stable forever without the search
 * needing to be re-run or shipped.
 */
class WallCanvasGoldenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun wallOfTwelvePlusBricksMatchesGoldenHashInLightTheme() {
        composeRule.setContent {
            DopedGoalTheme(darkTheme = false) {
                WallCanvas(
                    goalId = GOAL,
                    placedBricks = PLACED_BRICKS,
                    ghostCount = GHOST_COUNT,
                    modifier = Modifier.size(360.dp, 420.dp),
                )
            }
        }

        val hash = composeRule.onRoot().captureToImage().toPixelMap().crc32()

        // TODO(golden): no emulator available here. Run once in CI, then
        // replace LIGHT_WALL_HASH with the real value.
        assertEquals(LIGHT_WALL_HASH, hash)
    }

    @Test
    fun wallOfTwelvePlusBricksMatchesGoldenHashInDarkTheme() {
        composeRule.setContent {
            DopedGoalTheme(darkTheme = true) {
                WallCanvas(
                    goalId = GOAL,
                    placedBricks = PLACED_BRICKS,
                    ghostCount = GHOST_COUNT,
                    modifier = Modifier.size(360.dp, 420.dp),
                )
            }
        }

        val hash = composeRule.onRoot().captureToImage().toPixelMap().crc32()

        // TODO(golden): no emulator available here. Run once in CI, then
        // replace DARK_WALL_HASH with the real value.
        assertEquals(DARK_WALL_HASH, hash)
    }

    @Test
    fun bannerMatchesGoldenHash() {
        val banner = mintBanner(GOAL, "📚")

        composeRule.setContent {
            DopedGoalTheme {
                BannerCanvas(banner, modifier = Modifier.size(92.dp, 150.dp))
            }
        }

        val hash = composeRule.onRoot().captureToImage().toPixelMap().crc32()

        // TODO(golden): no emulator available here. Run once in CI, then
        // replace BANNER_HASH with the real value.
        assertEquals(BANNER_HASH, hash)
    }

    private companion object {
        const val GOAL = "goal-learn-kotlin"
        const val GHOST_COUNT = 3

        val STYLE = mintWallStyle(GOAL)

        // task-0..task-12 (13 bricks) plus task-48 (precious) = 14 bricks,
        // covering every property the acceptance checklist requires. See the
        // class doc for how these ids were picked.
        val PLACED_BRICKS: List<Brick> = ((0..12).map { "task-$it" } + "task-48")
            .map { taskId -> mintBrick(taskId, STYLE, Cohesion.DEFAULT, emblemChar = "📚") }

        // Placeholders, not real golden values yet — see the TODO on each test.
        const val LIGHT_WALL_HASH = 0L
        const val DARK_WALL_HASH = 0L
        const val BANNER_HASH = 0L
    }
}

/**
 * CRC32 over the captured bitmap's raw ARGB ints. Same helper as
 * `BrickCanvasGoldenTest.kt` — kept local to this file since Kotlin has no
 * androidTest-shared utility file for it yet.
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
