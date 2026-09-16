package app.dopedgoal.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Wall surface colors, kept outside the Material scheme: mortar and ghost slots
 * are material, not UI, so they must not drift with dynamic color.
 */
data class WallColors(
    val mortar: Color,
    val ghost: Color,
    val gold: Color,
)

val LocalWallColors = staticCompositionLocalOf {
    WallColors(mortar = MortarLight, ghost = QuietInk, gold = Gold)
}

/**
 * Spacing is an 8dp system. [gutter] is the standard horizontal margin; compact
 * rows use [gutterCompact]. Brick corners stay far tighter than card corners so
 * bricks read as a different material from the UI containing them.
 */
object Dimens {
    val spacingXSmall: Dp = 4.dp
    val spacingSmall: Dp = 8.dp
    val spacingMedium: Dp = 16.dp
    val spacingLarge: Dp = 24.dp
    val spacingXLarge: Dp = 32.dp

    val gutter: Dp = 20.dp
    val gutterCompact: Dp = 16.dp

    val cardCorner: Dp = 16.dp
    val brickCorner: Dp = 4.dp
    val minTouchTarget: Dp = 48.dp

    val nextBrickCard: Dp = 64.dp
    val taskRow: Dp = 52.dp
    val brickThumb: Dp = 56.dp

    val miniBannerWidth: Dp = 40.dp
    val miniBannerHeight: Dp = 65.dp
    val miniWallWidth: Dp = 120.dp
    val miniWallHeight: Dp = 65.dp
}

val MaterialTheme.wall: WallColors
    @Composable @ReadOnlyComposable
    get() = LocalWallColors.current
