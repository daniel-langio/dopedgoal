package app.dopedgoal.ui.wall

import android.graphics.Paint as AndroidPaint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import app.dopedgoal.core.Banner
import app.dopedgoal.core.Cloth
import app.dopedgoal.core.ClothKind
import app.dopedgoal.core.Division
import app.dopedgoal.core.Silhouette
import app.dopedgoal.core.Trim
import app.dopedgoal.ui.theme.wall
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

// Nominal banner geometry, matching banners.py's W/H constants. Not core
// constants: banners aren't laid out on the wall grid, only hung above it.
private const val W = 92f
private const val H = 150f

private val GOLD = Color(0xFFD8AC4A)

/**
 * Draws one goal's [banner]. Reads mortar colour from the theme so fray holes
 * and the field behind the silhouette cutout match the surface behind it;
 * everything else is delegated to [drawBanner], a pure function of its
 * arguments, matching the discipline `BrickCanvas`/`drawBrick` already keep.
 */
@Composable
fun BannerCanvas(banner: Banner, modifier: Modifier = Modifier) {
    val backdropColor = MaterialTheme.wall.mortar
    Canvas(modifier = modifier) {
        drawBanner(banner, backdropColor)
    }
}

/**
 * Pure rendering of [banner] into this [DrawScope]. [backdropColor] is what
 * shows through fray holes and would show behind the silhouette cutout —
 * `banners.py` hardcodes its dark `BACKDROP` constant there because it only
 * ever renders on a fixed dark backdrop; this renderer is theme-aware, so the
 * caller's mortar colour is threaded through instead.
 *
 * Draw order mirrors `BannerPainter.svg()` exactly: rod, field/weave/shading/
 * holes (clipped to the outline), trim, charge, final border stroke.
 */
fun DrawScope.drawBanner(banner: Banner, backdropColor: Color) {
    withTransform({
        scale(size.width / W, size.height / H, pivot = Offset.Zero)
    }) {
        val outline = outlinePath(banner.silhouette)
        drawRod()
        clipPath(outline) {
            drawField(banner)
            drawWeave(banner)
            drawShading(banner)
            drawHoles(banner, backdropColor)
        }
        drawTrim(banner, outline)
        drawCharge(banner)
        drawPath(outline, color = Color.Black.copy(alpha = 0.45f), style = Stroke(width = 1f))
    }
}

// ---------------------------------------------------------------------------
// Outline
// ---------------------------------------------------------------------------

/** One `Path` per [Silhouette] — mirrors `_outline()` in banners.py. */
private fun outlinePath(silhouette: Silhouette): Path = Path().apply {
    when (silhouette) {
        Silhouette.SQUARE -> {
            moveTo(0f, 0f)
            lineTo(W, 0f)
            lineTo(W, H)
            lineTo(0f, H)
            close()
        }
        Silhouette.POINTED -> {
            moveTo(0f, 0f)
            lineTo(W, 0f)
            lineTo(W, H * 0.72f)
            lineTo(W / 2f, H)
            lineTo(0f, H * 0.72f)
            close()
        }
        Silhouette.SWALLOWTAIL -> {
            moveTo(0f, 0f)
            lineTo(W, 0f)
            lineTo(W, H)
            lineTo(W * 0.72f, H * 0.86f)
            lineTo(W / 2f, H * 0.97f)
            lineTo(W * 0.28f, H * 0.86f)
            lineTo(0f, H)
            close()
        }
        Silhouette.ROUNDED -> {
            moveTo(0f, 0f)
            lineTo(W, 0f)
            lineTo(W, H * 0.78f)
            quadraticTo(W / 2f, H + 14f, 0f, H * 0.78f)
            close()
        }
        Silhouette.SCALLOPED -> {
            // The reference chains SVG arcs along the hem. Compose's arcTo
            // needs the same start/end tangent bookkeeping an SVG "a" command
            // gets for free, so each scallop is approximated with a quadratic
            // bezier bulging downward instead — visually equivalent.
            moveTo(0f, 0f)
            lineTo(W, 0f)
            val hemY = H * 0.88f
            lineTo(W, hemY)
            val scallops = 5
            val step = W / scallops
            val bulge = step / 2.4f
            var cx = W
            repeat(scallops) {
                val nx = cx - step
                quadraticTo(cx - step / 2f, hemY + bulge, nx, hemY)
                cx = nx
            }
            close()
        }
    }
}

// ---------------------------------------------------------------------------
// Rod
// ---------------------------------------------------------------------------

/**
 * The hanging rod above the banner — fixed brown/gold tones, not
 * theme-dependent. This is a physical rod, not a themed UI element, so it
 * mirrors banners.py's hardcoded hex values rather than reading from
 * `MaterialTheme.wall`.
 */
private fun DrawScope.drawRod() {
    val rodWidth = W + 22f
    drawRoundRect(
        color = Color(0xFF6B5540),
        topLeft = Offset(-11f, -9f),
        size = Size(rodWidth, 7f),
        cornerRadius = CornerRadius(3.5f),
    )
    drawRoundRect(
        color = Color(0xFF8B7256),
        topLeft = Offset(-11f, -9f),
        size = Size(rodWidth, 2.5f),
        cornerRadius = CornerRadius(1.2f),
    )
    drawCircle(color = GOLD, radius = 5f, center = Offset(-11f, -5.5f))
    drawCircle(color = GOLD, radius = 5f, center = Offset(W + 11f, -5.5f))
}

// ---------------------------------------------------------------------------
// Cloth colour helpers — mirror `Cloth.tone`/`Cloth.counter` in banners.py.
// ---------------------------------------------------------------------------

private fun Cloth.tone(delta: Float = 0f, alpha: Float = 1f): Color =
    hsl(hue, saturation, (lightness + delta).coerceIn(6f, 94f), alpha)

private fun Cloth.counter(delta: Float = 0f): Color =
    hsl((hue + 165f) % 360f, max(12f, saturation - 18f), min(92f, lightness + 22f + delta))

// ---------------------------------------------------------------------------
// Field
// ---------------------------------------------------------------------------

/** The cloth background, split by [Division] — mirrors `_field()`. */
private fun DrawScope.drawField(banner: Banner) {
    val cloth = banner.cloth
    val base = cloth.tone()
    val counter = cloth.counter()
    drawRect(color = base, topLeft = Offset(-4f, -4f), size = Size(W + 8f, H + 20f))

    when (banner.division) {
        Division.PLAIN -> Unit
        Division.PER_PALE -> drawRect(
            color = counter,
            topLeft = Offset(W / 2f, -4f),
            size = Size(W / 2f + 4f, H + 20f),
        )
        Division.PER_FESS -> drawRect(
            color = counter,
            topLeft = Offset(-4f, H / 2f),
            size = Size(W + 8f, H / 2f + 20f),
        )
        Division.QUARTERLY -> {
            drawRect(color = counter, topLeft = Offset(W / 2f, -4f), size = Size(W / 2f + 4f, H / 2f + 4f))
            drawRect(color = counter, topLeft = Offset(-4f, H / 2f), size = Size(W / 2f + 4f, H / 2f + 20f))
        }
        Division.CHEVRON -> {
            val path = Path().apply {
                moveTo(-4f, H * 0.62f)
                lineTo(W / 2f, H * 0.3f)
                lineTo(W + 4f, H * 0.62f)
                lineTo(W + 4f, H * 0.82f)
                lineTo(W / 2f, H * 0.5f)
                lineTo(-4f, H * 0.82f)
                close()
            }
            drawPath(path, color = counter)
        }
        Division.SALTIRE -> {
            val first = Path().apply {
                moveTo(-4f, 4f)
                lineTo(14f, -4f)
                lineTo(W + 4f, H)
                lineTo(W - 14f, H + 8f)
                close()
            }
            val second = Path().apply {
                moveTo(W + 4f, 4f)
                lineTo(W - 14f, -4f)
                lineTo(-4f, H)
                lineTo(14f, H + 8f)
                close()
            }
            drawPath(first, color = counter)
            drawPath(second, color = counter)
        }
        Division.BENDY -> {
            for (i in -2..8) {
                val x = i * 22f
                val path = Path().apply {
                    moveTo(x, -4f)
                    lineTo(x + 11f, -4f)
                    lineTo(x + 11f - 40f, H + 20f)
                    lineTo(x - 40f, H + 20f)
                    close()
                }
                drawPath(path, color = counter)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Weave
// ---------------------------------------------------------------------------

/** A texture hint per [ClothKind], drawn from `banner.rng("weave")`. */
private fun DrawScope.drawWeave(banner: Banner) {
    val rng = banner.rng("weave")
    when (banner.cloth.kind) {
        ClothKind.LINEN -> {
            var x = 0f
            while (x < W) {
                drawRect(color = Color.Black.copy(alpha = 0.05f), topLeft = Offset(x, -4f), size = Size(1f, H + 20f))
                x += 4f
            }
            var y = 0f
            while (y < H) {
                drawRect(color = Color.White.copy(alpha = 0.05f), topLeft = Offset(-4f, y), size = Size(W + 8f, 1f))
                y += 4f
            }
        }
        ClothKind.CANVAS -> repeat(220) {
            drawCircle(
                color = Color.Black.copy(alpha = 0.07f),
                radius = rng.uniform(0.4f, 1.3f),
                center = Offset(rng.uniform(0f, W), rng.uniform(0f, H)),
            )
        }
        ClothKind.VELVET -> {
            val bands = 9
            for (i in 0 until bands) {
                val t = i / (bands - 1f)
                val opacity = (0.10f * abs(sin(t * 3.1f))).coerceIn(0f, 1f)
                drawRect(
                    color = Color.Black.copy(alpha = opacity),
                    topLeft = Offset(t * W, -4f),
                    size = Size(W / bands + 1f, H + 20f),
                )
            }
        }
        ClothKind.SILK -> {
            // banners.py's unmatched/`else` branch: a soft diagonal sheen.
            val bands = 7
            for (i in 0 until bands) {
                val t = i / (bands - 1f)
                val opacity = (0.16f * max(0f, cos((t - 0.35f) * 3.4f))).coerceIn(0f, 1f)
                drawRect(
                    color = Color.White.copy(alpha = opacity),
                    topLeft = Offset(t * W, -4f),
                    size = Size(W / bands + 1f, H + 20f),
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Shading
// ---------------------------------------------------------------------------

/** Vertical fold-shadow bands, a top shadow, and an optional sun-fade wash. */
private fun DrawScope.drawShading(banner: Banner) {
    for (t in FOLD_POSITIONS) {
        drawRect(
            color = Color.Black.copy(alpha = 0.08f),
            topLeft = Offset(t * W, -4f),
            size = Size(W * 0.07f, H + 20f),
        )
    }
    drawRect(color = Color.Black.copy(alpha = 0.18f), topLeft = Offset(-4f, -4f), size = Size(W + 8f, 14f))

    val fade = banner.fray.fade
    if (fade > 0.05f) {
        drawRect(
            color = Color(0xFFE8E2D8).copy(alpha = fade * 0.28f),
            topLeft = Offset(-4f, H * 0.45f),
            size = Size(W + 8f, H * 0.6f),
        )
    }
}

private val FOLD_POSITIONS = listOf(0.18f, 0.44f, 0.7f)

// ---------------------------------------------------------------------------
// Holes
// ---------------------------------------------------------------------------

/** [banner]'s fray holes, drawn from `banner.rng("holes")`. */
private fun DrawScope.drawHoles(banner: Banner, backdropColor: Color) {
    val rng = banner.rng("holes")
    repeat(banner.fray.holes) {
        val x = rng.uniform(12f, W - 12f)
        val y = rng.uniform(H * 0.3f, H * 0.9f)
        val r = rng.uniform(3f, 7f)
        drawCircle(color = backdropColor, radius = r, center = Offset(x, y))
        drawCircle(
            color = Color.Black.copy(alpha = 0.3f),
            radius = r + 1.2f,
            center = Offset(x, y),
            style = Stroke(width = 1.5f),
        )
    }
}

// ---------------------------------------------------------------------------
// Trim
// ---------------------------------------------------------------------------

/** Per [Trim] — mirrors `_trim()`. Drawn unclipped, on top of the field. */
private fun DrawScope.drawTrim(banner: Banner, outline: Path) {
    when (banner.trim) {
        Trim.NONE -> Unit
        Trim.BORDER -> drawPath(outline, color = GOLD.copy(alpha = 0.9f), style = Stroke(width = 3.5f))
        Trim.FRINGE -> {
            // Hem y depends on silhouette: scalloped/rounded hems sit higher
            // than the nominal bottom edge.
            val hem = H * when (banner.silhouette) {
                Silhouette.SCALLOPED -> 0.88f
                Silhouette.ROUNDED -> 0.78f
                else -> 1.0f
            }
            val rng = banner.rng("fringe")
            for (i in 0 until 16) {
                val x = 3f + i * (W - 6f) / 15f
                val length = rng.uniform(6f, 12f)
                drawRoundRect(
                    color = GOLD,
                    topLeft = Offset(x, hem - 2f),
                    size = Size(2f, length),
                    cornerRadius = CornerRadius(1f),
                )
            }
        }
        Trim.TASSELS -> {
            val y0 = H * 0.99f
            for (x in listOf(6f, W - 6f)) {
                drawLine(color = GOLD, start = Offset(x, y0), end = Offset(x, y0 + 10f), strokeWidth = 1.5f)
                drawCircle(color = GOLD, radius = 4f, center = Offset(x, y0 + 13f))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Charge
// ---------------------------------------------------------------------------

/**
 * The goal's emoji, large and centred, rotated per [Banner.charge]. Uses the
 * same native-canvas technique as `BrickCanvas.kt`'s `drawEmblem`: a soft
 * drop-shadow copy behind the glyph approximates `_charge()`'s
 * `feDropShadow`.
 */
private fun DrawScope.drawCharge(banner: Banner) {
    val charge = banner.charge
    val size = 40f * charge.scale
    val x = W / 2f
    val y = H * 0.42f

    val nativeCanvas = drawContext.canvas.nativeCanvas
    val textPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
        textAlign = AndroidPaint.Align.CENTER
        textSize = size
    }
    val metrics = textPaint.fontMetrics
    val baselineY = y - (metrics.ascent + metrics.descent) / 2f

    fun drawGlyph(dy: Float, colorFilter: android.graphics.ColorFilter?) {
        textPaint.colorFilter = colorFilter
        nativeCanvas.save()
        nativeCanvas.rotate(charge.angle, x, y)
        nativeCanvas.drawText(charge.char, x, baselineY + dy, textPaint)
        nativeCanvas.restore()
    }

    drawGlyph(
        dy = 2f,
        colorFilter = PorterDuffColorFilter(Color.Black.copy(alpha = 0.5f).toArgb(), PorterDuff.Mode.SRC_IN),
    )
    drawGlyph(dy = 0f, colorFilter = null)
}
