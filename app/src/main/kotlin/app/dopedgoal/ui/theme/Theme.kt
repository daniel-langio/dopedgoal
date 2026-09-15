package app.dopedgoal.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val LightScheme = lightColorScheme(
    primary = Terracotta,
    onPrimary = Paper,
    secondary = Moss,
    onSecondary = Paper,
    tertiary = Gold,
    onTertiary = Ink,
    background = Canvas,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = MortarLight,
    onSurfaceVariant = QuietInk,
    outline = QuietInk,
    outlineVariant = MortarLight,
    error = Danger,
    onError = Paper,
)

private val DarkScheme = darkColorScheme(
    primary = Terracotta,
    onPrimary = InkDark,
    secondary = Moss,
    onSecondary = InkDark,
    tertiary = Gold,
    onTertiary = CanvasDark,
    background = CanvasDark,
    onBackground = InkDark,
    surface = PaperDark,
    onSurface = InkDark,
    surfaceVariant = MortarDark,
    onSurfaceVariant = QuietInkDark,
    outline = QuietInkDark,
    outlineVariant = MortarDark,
    error = Danger,
    onError = InkDark,
)

/**
 * Material 3 supplies the accessibility and interaction base; the palette is
 * ours. Dynamic color is deliberately not used — the warm paper surface is part
 * of the product's identity, and mortar must stay a fixed value against bricks.
 */
@Composable
fun DopedGoalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val wallColors = if (darkTheme) {
        WallColors(mortar = MortarDark, ghost = QuietInkDark, gold = Gold)
    } else {
        WallColors(mortar = MortarLight, ghost = QuietInk, gold = Gold)
    }

    CompositionLocalProvider(LocalWallColors provides wallColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkScheme else LightScheme,
            typography = DopedGoalTypography,
            content = content,
        )
    }
}
