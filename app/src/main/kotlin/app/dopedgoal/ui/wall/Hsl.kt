package app.dopedgoal.ui.wall

import androidx.compose.ui.graphics.Color
import app.dopedgoal.core.Material
import app.dopedgoal.core.Paint
import kotlin.math.abs

/**
 * Standard CSS/SVG HSL -> RGB conversion — the same math a browser applies to
 * `bricks.py`'s `hsl(h s% l%)` strings. [hue] is degrees and wraps mod 360;
 * [saturation]/[lightness] are 0-100 percentages.
 */
fun hsl(hue: Float, saturation: Float, lightness: Float, alpha: Float = 1f): Color {
    val h = ((hue % 360f) + 360f) % 360f
    val s = (saturation / 100f).coerceIn(0f, 1f)
    val l = (lightness / 100f).coerceIn(0f, 1f)

    val chroma = (1f - abs(2f * l - 1f)) * s
    val hPrime = h / 60f
    val x = chroma * (1f - abs(hPrime.mod(2f) - 1f))
    val (r1, g1, b1) = when {
        hPrime < 1f -> Triple(chroma, x, 0f)
        hPrime < 2f -> Triple(x, chroma, 0f)
        hPrime < 3f -> Triple(0f, chroma, x)
        hPrime < 4f -> Triple(0f, x, chroma)
        hPrime < 5f -> Triple(x, 0f, chroma)
        else -> Triple(chroma, 0f, x)
    }
    val m = l - chroma / 2f
    return Color(red = r1 + m, green = g1 + m, blue = b1 + m, alpha = alpha)
}

/** The material's base surface colour, per its own hue/saturation/lightness. */
fun Material.baseColor(alpha: Float = 1f): Color = hsl(hue, saturation, lightness, alpha)

/**
 * Same hue/saturation as [baseColor], lightness shifted by [delta] and clamped —
 * mirrors `Material.shade` in `bricks.py`.
 */
fun Material.shade(delta: Float): Color = hsl(hue, saturation, (lightness + delta).coerceIn(4f, 96f))

/** The paint layer's colour, per its own hue/saturation/lightness. */
fun Paint.toColor(alpha: Float = 1f): Color = hsl(hue, saturation, lightness, alpha)
