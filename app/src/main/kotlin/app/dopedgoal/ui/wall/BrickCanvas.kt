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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import app.dopedgoal.core.ArtifactKind
import app.dopedgoal.core.BRICK_HEIGHT
import app.dopedgoal.core.Brick
import app.dopedgoal.core.EmblemTreatment
import app.dopedgoal.core.Grain
import app.dopedgoal.core.Material
import app.dopedgoal.core.Paint
import app.dopedgoal.core.PaintPattern
import app.dopedgoal.core.Rng
import app.dopedgoal.ui.theme.Dimens
import app.dopedgoal.ui.theme.wall
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Level of detail. Thumbnails (list rows, miniature walls) drop the expensive
 * per-layer texture in favour of a flat wash, but the emblem and border always
 * survive — those two carry completion state and identity, per
 * ANDROID_DESIGN.md's "must survive at 48dp" rule.
 */
enum class BrickLod { Full, Thumbnail }

/**
 * Draws one minted [brick]. Reads mortar colour from the theme so chips (cut
 * *into* the brick down to the wall behind it) match the surface it sits on;
 * everything else is delegated to [drawBrick], which is a pure function of its
 * arguments so it can be called from a plain `Canvas` with no other context.
 */
@Composable
fun BrickCanvas(brick: Brick, modifier: Modifier = Modifier, lod: BrickLod = BrickLod.Full) {
    val mortarColor = MaterialTheme.wall.mortar
    Canvas(modifier = modifier) {
        drawBrick(brick, mortarColor, lod)
    }
}

/**
 * Pure rendering of [brick] into this [DrawScope]. No composition-local reads,
 * no side effects: every pixel is a function of [brick], [mortarColor] and
 * [lod]. This must stay true because a brick's texture is re-derived from its
 * seed on every single draw rather than read from storage — see the KDoc on
 * `core.Rng`.
 *
 * Draw order mirrors `bricks.py`'s `BrickPainter.svg()` exactly: material base,
 * grain, paint, stain, cracks, artifact, emblem, (close clip), chips, border.
 */
fun DrawScope.drawBrick(brick: Brick, mortarColor: Color, lod: BrickLod = BrickLod.Full) {
    val w = brick.width
    val h = BRICK_HEIGHT
    withTransform({
        scale(size.width / w, size.height / h, pivot = Offset.Zero)
    }) {
        val corner = CornerRadius(Dimens.brickCorner.value)
        val clip = Path().apply { addRoundRect(RoundRect(Rect(Offset.Zero, Size(w, h)), corner)) }
        clipPath(clip) {
            drawMaterialBase(brick)
            if (lod == BrickLod.Full) {
                drawGrain(brick)
                drawPaint(brick)
                drawStain(brick)
                drawCracks(brick)
                drawArtifacts(brick)
            } else {
                drawFlatPaintWash(brick)
            }
            drawEmblem(brick, lod)
        }
        // Chips and the border are drawn after the clip closes, matching the
        // reference: a chip cuts down to the wall behind the brick, so it must
        // not be masked by the brick's own rounded-rect clip.
        if (lod == BrickLod.Full) drawChips(brick, mortarColor)
        drawBorder(brick)
    }
}

private fun DrawScope.drawMaterialBase(brick: Brick) {
    val material = brick.material
    val alpha = if (material.grain == Grain.GLASS) 0.38f else 1f
    drawRect(color = material.baseColor(alpha), size = Size(brick.width, BRICK_HEIGHT))
}

// ---------------------------------------------------------------------------
// Grain
// ---------------------------------------------------------------------------

private fun DrawScope.drawGrain(brick: Brick) {
    val material = brick.material
    val rng = brick.rng("grain")
    val w = brick.width
    val h = BRICK_HEIGHT
    when (material.grain) {
        Grain.PIT -> repeat(90) { drawFleck(rng, w, h, material) }
        Grain.SPECKLE -> repeat(55) { drawFleck(rng, w, h, material) }
        Grain.FIBER -> repeat(14) {
            val y = rng.uniform(0f, h)
            val bow = rng.uniform(-4f, 4f)
            val path = Path().apply {
                moveTo(0f, y)
                quadraticTo(w / 2f, y + bow, w, y)
            }
            drawPath(
                path,
                color = material.shade(-9f).copy(alpha = 0.55f),
                style = Stroke(width = rng.uniform(0.6f, 1.8f)),
            )
        }
        Grain.VEIN -> repeat(4) {
            val x0 = rng.uniform(0f, w)
            val y0 = rng.uniform(0f, h)
            val controlDx = rng.uniform(-30f, 30f)
            val controlDy = rng.uniform(-14f, 14f)
            val endDx = rng.uniform(-60f, 60f)
            val endDy = rng.uniform(-20f, 20f)
            val path = Path().apply {
                moveTo(x0, y0)
                quadraticTo(x0 + controlDx, y0 + controlDy, x0 + endDx, y0 + endDy)
            }
            drawPath(
                path,
                color = material.shade(-22f).copy(alpha = 0.6f),
                style = Stroke(width = rng.uniform(0.5f, 1.3f)),
            )
        }
        Grain.TILE -> {
            val step = 11f
            var x = 0f
            while (x < w) {
                var y = 0f
                while (y < h) {
                    drawRect(
                        color = material.shade(rng.uniform(-10f, 10f)).copy(alpha = 0.85f),
                        topLeft = Offset(x + 0.7f, y + 0.7f),
                        size = Size(step - 1.4f, step - 1.4f),
                    )
                    y += step
                }
                x += step
            }
        }
        Grain.SHEEN -> {
            val bands = 9
            for (i in 0 until bands) {
                val t = i / (bands - 1f)
                val delta = 26f * sin(t * PI.toFloat() * 1.6f) - 12f
                drawRect(
                    color = material.shade(delta),
                    topLeft = Offset(0f, t * h),
                    size = Size(w, h / bands + 1f),
                )
            }
            val highlight = Path().apply {
                moveTo(w * 0.1f, h)
                lineTo(w * 0.3f, 0f)
                lineTo(w * 0.42f, 0f)
                lineTo(w * 0.22f, h)
                close()
            }
            drawPath(highlight, color = Color.White.copy(alpha = 0.28f))
        }
        Grain.GLASS -> {
            repeat(3) {
                val x = rng.uniform(0f, w)
                val path = Path().apply {
                    moveTo(x, h)
                    lineTo(x + 18f, 0f)
                    lineTo(x + 26f, 0f)
                    lineTo(x + 8f, h)
                    close()
                }
                drawPath(path, color = Color.White.copy(alpha = 0.16f))
            }
            drawRect(
                color = Color.White.copy(alpha = 0.3f),
                topLeft = Offset(2f, 2f),
                size = Size(w - 4f, h - 4f),
                style = Stroke(width = 1f),
            )
        }
        Grain.FACET -> {
            val points = (0 until 7).map { Offset(rng.uniform(0f, w), rng.uniform(0f, h)) } +
                listOf(Offset(0f, 0f), Offset(w, 0f), Offset(0f, h), Offset(w, h))
            for (i in 0 until points.size - 2) {
                val a = points[i]
                val c = points[i + 1]
                val d = points[i + 2]
                val path = Path().apply {
                    moveTo(a.x, a.y)
                    lineTo(c.x, c.y)
                    lineTo(d.x, d.y)
                    close()
                }
                drawPath(path, color = material.shade(rng.uniform(-16f, 12f)).copy(alpha = 0.9f))
                drawPath(path, color = Color.White.copy(alpha = 0.35f), style = Stroke(width = 0.5f))
            }
        }
    }
}

private fun DrawScope.drawFleck(rng: Rng, w: Float, h: Float, material: Material) {
    val x = rng.uniform(0f, w)
    val y = rng.uniform(0f, h)
    val r = rng.uniform(0.4f, 1.6f)
    drawCircle(color = material.shade(-7f).copy(alpha = 0.5f), radius = r, center = Offset(x, y))
}

// ---------------------------------------------------------------------------
// Paint
// ---------------------------------------------------------------------------

/** A jittered-outline path around a rect perimeter — `_rough` in `bricks.py`. */
private fun roughPath(x: Float, y: Float, w: Float, h: Float, rng: Rng, jitter: Float = 2.2f): Path {
    val cols = (w / 9f).toInt().coerceAtLeast(2)
    val rows = (h / 9f).toInt().coerceAtLeast(2)
    val points = ArrayList<Offset>(2 * cols + 2 * rows)
    for (i in 0..cols) points += Offset(x + w * i / cols, y + rng.uniform(-jitter, jitter))
    for (i in 1..rows) points += Offset(x + w + rng.uniform(-jitter, jitter), y + h * i / rows)
    for (i in 1..cols) points += Offset(x + w - w * i / cols, y + h + rng.uniform(-jitter, jitter))
    for (i in 1 until rows) points += Offset(x + rng.uniform(-jitter, jitter), y + h - h * i / rows)
    return Path().apply {
        moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) lineTo(points[i].x, points[i].y)
        close()
    }
}

/** A scatter of small flecks in [material]'s shade range — `_flakes` in `bricks.py`. */
private fun DrawScope.drawFlakes(x: Float, y: Float, w: Float, h: Float, rng: Rng, density: Float, material: Material) {
    val count = (w * h * density / 100f).toInt()
    repeat(count) {
        val fx = rng.uniform(x, x + w)
        val fy = rng.uniform(y, y + h)
        val r = rng.uniform(0.8f, 3.2f)
        drawCircle(
            color = material.shade(rng.uniform(-6f, 6f)).copy(alpha = rng.uniform(0.55f, 0.95f)),
            radius = r,
            center = Offset(fx, fy),
        )
    }
}

/** Wavy paint-coloured highlight strokes — `_brushwork` in `bricks.py`. */
private fun DrawScope.drawBrushwork(x: Float, y: Float, w: Float, h: Float, rng: Rng, paint: Paint) {
    repeat(rng.intBetween(2, 5)) {
        val sy = rng.uniform(y, y + h)
        val bow = rng.uniform(-2.5f, 2.5f)
        val path = Path().apply {
            moveTo(x, sy)
            quadraticTo(x + w / 2f, sy + bow, x + w, sy)
        }
        val lightness = min(88f, paint.lightness + rng.uniform(6f, 18f))
        drawPath(
            path,
            color = hsl(paint.hue, paint.saturation, lightness, 0.35f),
            style = Stroke(width = rng.uniform(1f, 3f)),
        )
    }
}

/** The shared tail of banded/dipped: a rough-edged wash plus flakes and brushwork. */
private fun DrawScope.drawWashRegion(x: Float, y: Float, w: Float, h: Float, rng: Rng, paint: Paint, material: Material) {
    drawPath(roughPath(x, y, w, h, rng), color = paint.toColor(0.92f))
    drawFlakes(x, y, w, h, rng, 1.2f, material)
    drawBrushwork(x, y, w, h, rng, paint)
}

private fun DrawScope.drawPaint(brick: Brick) {
    val paint = brick.paint
    if (paint.coverage < 0.03f) return
    val rng = brick.rng("paint")
    val w = brick.width
    val h = BRICK_HEIGHT
    val c = paint.coverage
    val material = brick.material

    when (paint.pattern) {
        PaintPattern.BANDED -> {
            val bh = max(6f, h * c)
            val y = rng.uniform(0f, max(0.1f, h - bh))
            drawWashRegion(0f, y, w, bh, rng, paint, material)
        }
        PaintPattern.DIPPED -> {
            val bw = max(8f, w * c)
            val x = if (rng.chance(0.5f)) 0f else w - bw
            drawWashRegion(x, 0f, bw, h, rng, paint, material)
        }
        PaintPattern.HALF -> {
            val cut = w * c * 1.5f
            val skew = rng.uniform(18f, 46f)
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(cut, 0f)
                lineTo(max(0f, cut - skew), h)
                lineTo(0f, h)
                close()
            }
            drawPath(path, color = paint.toColor(0.9f))
            drawFlakes(0f, 0f, cut, h, rng, 1.3f, material)
            drawBrushwork(0f, 0f, cut, h, rng, paint)
        }
        PaintPattern.SPLATTER -> {
            val target = c * w * h
            var area = 0f
            while (area < target) {
                val bx = rng.uniform(0f, w)
                val by = rng.uniform(0f, h)
                val r = rng.uniform(2.5f, 9f)
                drawPath(roughPath(bx - r, by - r, r * 2f, r * 2f, rng, r * 0.35f), color = paint.toColor(0.88f))
                repeat(rng.intBetween(0, 3)) {
                    drawCircle(
                        color = paint.toColor(0.8f),
                        radius = rng.uniform(0.6f, 1.8f),
                        center = Offset(bx + rng.uniform(-r * 2.2f, r * 2.2f), by + rng.uniform(-r * 2.2f, r * 2.2f)),
                    )
                }
                area += PI.toFloat() * r * r
            }
        }
        PaintPattern.DRIP -> {
            val top = max(5f, h * c * 0.55f)
            drawPath(roughPath(0f, -2f, w, top + 2f, rng, 1.6f), color = paint.toColor(0.92f))
            repeat(rng.intBetween(3, 8)) {
                val x = rng.uniform(2f, w - 8f)
                val dh = rng.uniform(5f, h - top)
                val dw = rng.uniform(2.2f, 5.5f)
                val path = Path().apply {
                    moveTo(x, top - 1f)
                    lineTo(x + dw, top - 1f)
                    lineTo(x + dw * 0.8f, top + dh)
                    quadraticTo(x + dw / 2f, top + dh + dw, x + dw * 0.2f, top + dh)
                    close()
                }
                drawPath(path, color = paint.toColor(0.9f))
            }
            drawFlakes(0f, 0f, w, top, rng, 1.1f, material)
        }
        PaintPattern.EDGE -> {
            val t = max(3f, min(h * 0.4f, h * c * 1.1f))
            drawPath(roughPath(-2f, -2f, w + 4f, t, rng, 1.8f), color = paint.toColor(0.9f))
            drawPath(roughPath(-2f, h - t, w + 4f, t + 2f, rng, 1.8f), color = paint.toColor(0.9f))
            drawFlakes(0f, 0f, w, t, rng, 1.4f, material)
            drawFlakes(0f, h - t, w, t, rng, 1.4f, material)
        }
        PaintPattern.STRIPES -> {
            val n = rng.intBetween(3, 6)
            val bh = max(2.5f, h * c / n)
            val gap = (h - bh * n) / (n + 1)
            for (i in 0 until n) {
                val y = gap + i * (bh + gap)
                drawPath(roughPath(-2f, y, w + 4f, bh, rng, 1.2f), color = paint.toColor(0.92f))
                drawFlakes(0f, y, w, bh, rng, 1.0f, material)
            }
        }
        PaintPattern.DIAGONAL -> {
            val n = rng.intBetween(3, 5)
            val bw = max(4f, w * c / n)
            val skew = h * rng.uniform(0.4f, 0.9f)
            val spacing = (w + skew) / n
            for (i in 0..n) {
                val x0 = -skew + i * spacing + rng.uniform(-2f, 2f)
                val path = Path().apply {
                    moveTo(x0, -2f)
                    lineTo(x0 + bw, -2f)
                    lineTo(x0 + bw + skew, h + 2f)
                    lineTo(x0 + skew, h + 2f)
                    close()
                }
                drawPath(path, color = paint.toColor(0.9f))
            }
            drawFlakes(0f, 0f, w, h, rng, 0.9f * c, material)
        }
        PaintPattern.CHECKER -> {
            val cellSize = rng.choice(listOf(9f, 12f, 16f))
            var gy = 0f
            while (gy < h) {
                var gx = 0f
                while (gx < w) {
                    if (rng.chance(c)) {
                        drawPath(
                            roughPath(gx, gy, cellSize - 1f, cellSize - 1f, rng, 1.1f),
                            color = paint.toColor(0.9f),
                        )
                    }
                    gx += cellSize
                }
                gy += cellSize
            }
        }
        PaintPattern.ROLLER -> {
            val n = rng.intBetween(2, 3)
            val bh = max(5f, h * c / n)
            repeat(n) {
                val y = rng.uniform(0f, max(0.1f, h - bh))
                val x0 = rng.uniform(-6f, 6f)
                val length = w * rng.uniform(0.7f, 1.15f)
                drawPath(roughPath(x0, y, length, bh, rng, 1.4f), color = paint.toColor(0.9f))
                repeat(rng.intBetween(4, 9)) {
                    val sy = rng.uniform(y, y + bh)
                    drawRect(
                        color = material.shade(0f).copy(alpha = 0.7f),
                        topLeft = Offset(rng.uniform(x0, x0 + length * 0.8f), sy),
                        size = Size(rng.uniform(4f, 18f), rng.uniform(0.8f, 2f)),
                    )
                }
            }
        }
        PaintPattern.SPRAY -> {
            val cx = rng.uniform(w * 0.25f, w * 0.75f)
            val cy = rng.uniform(h * 0.3f, h * 0.7f)
            val spread = w * rng.uniform(0.28f, 0.5f)
            repeat((320 * c).toInt()) {
                val angle = rng.uniform(0f, (2.0 * PI).toFloat())
                val d = abs(rng.gaussian(0f, spread * 0.6f))
                val x = cx + cos(angle) * d
                val y = cy + sin(angle) * d * 0.6f
                drawCircle(color = paint.toColor(0.75f), radius = rng.uniform(0.5f, 2.4f), center = Offset(x, y))
            }
        }
        PaintPattern.FRAME -> {
            val t = max(3f, min(h * 0.3f, h * c * 0.9f))
            drawPath(roughPath(-2f, -2f, w + 4f, t, rng, 1.5f), color = paint.toColor(0.9f))
            drawPath(roughPath(-2f, h - t, w + 4f, t + 2f, rng, 1.5f), color = paint.toColor(0.9f))
            drawPath(roughPath(-2f, 0f, t, h, rng, 1.5f), color = paint.toColor(0.9f))
            drawPath(roughPath(w - t, 0f, t + 2f, h, rng, 1.5f), color = paint.toColor(0.9f))
            drawFlakes(0f, 0f, w, h, rng, 0.7f * c, material)
        }
        PaintPattern.CORNER -> {
            val leg = sqrt(2f * c * w * h)
            val lx = min(leg, w)
            val ly = min(leg * h / w, h)
            val cx = if (rng.chance(0.5f)) 0f else w
            val cy = if (rng.chance(0.5f)) 0f else h
            val sx = if (cx == 0f) 1f else -1f
            val sy = if (cy == 0f) 1f else -1f
            val path = Path().apply {
                moveTo(cx, cy)
                lineTo(cx + sx * lx, cy)
                lineTo(cx, cy + sy * ly)
                close()
            }
            drawPath(path, color = paint.toColor(0.9f))
            drawFlakes(min(cx, cx + sx * lx), min(cy, cy + sy * ly), lx, ly, rng, 1.1f, material)
        }
        PaintPattern.TAG -> {
            val width = 2f + c * 12f
            val startX = w * 0.12f
            val startY = h * 0.7f
            var curX = startX
            var curY = startY
            val path = Path().apply { moveTo(startX, startY) }
            repeat(rng.intBetween(3, 5)) {
                val controlDx = rng.uniform(6f, 16f)
                val controlDy = rng.uniform(-26f, -8f)
                val endDx = rng.uniform(12f, 24f)
                val endDy = rng.uniform(-4f, 12f)
                path.quadraticTo(curX + controlDx, curY + controlDy, curX + endDx, curY + endDy)
                curX += endDx
                curY += endDy
            }
            drawPath(
                path,
                color = paint.toColor(0.92f),
                style = Stroke(width = width, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }
        PaintPattern.CRACKLE -> {
            drawPath(roughPath(-2f, -2f, w + 4f, h + 4f, rng, 2.2f), color = paint.toColor(0.88f))
            val segments = (14 * (1.05f - c)).toInt() + 4
            repeat(segments) {
                var x = rng.uniform(0f, w)
                var y = rng.uniform(0f, h)
                val path = Path().apply { moveTo(x, y) }
                repeat(rng.intBetween(2, 4)) {
                    x += rng.uniform(-16f, 16f)
                    y += rng.uniform(-12f, 12f)
                    path.lineTo(x, y)
                }
                drawPath(
                    path,
                    color = material.shade(-4f).copy(alpha = 0.85f),
                    style = Stroke(width = rng.uniform(1.2f, 3.2f)),
                )
            }
            drawFlakes(0f, 0f, w, h, rng, 1.6f * (1f - c), material)
        }
        else -> {
            // WEATHERED, and any pattern not special-cased above, share the
            // reference's generic textured-wash fallback (bricks.py's `else`).
            drawPath(roughPath(-2f, -2f, w + 4f, h + 4f, rng, 2.6f), color = paint.toColor(0.85f))
            drawFlakes(0f, 0f, w, h, rng, 2.4f * (1f - c), material)
            drawBrushwork(0f, 0f, w, h, rng, paint)
        }
    }
}

/** Thumbnail LOD: one flat wash sized by coverage, no per-pattern texture. */
private fun DrawScope.drawFlatPaintWash(brick: Brick) {
    val paint = brick.paint
    if (paint.coverage < 0.03f) return
    val w = brick.width
    val h = BRICK_HEIGHT
    val washHeight = h * paint.coverage
    drawRect(color = paint.toColor(), topLeft = Offset(0f, h - washHeight), size = Size(w, washHeight))
}

// ---------------------------------------------------------------------------
// Stain, cracks, chips
// ---------------------------------------------------------------------------

private fun DrawScope.drawStain(brick: Brick) {
    val stain = brick.wear.stain
    if (stain < 0.55f) return
    val w = brick.width
    val h = BRICK_HEIGHT
    drawRect(
        color = Color(0xFF140E08).copy(alpha = (stain - 0.55f) * 0.5f),
        topLeft = Offset(0f, h * 0.55f),
        size = Size(w, h * 0.45f),
    )
}

private fun DrawScope.drawCracks(brick: Brick) {
    val wear = brick.wear
    if (wear.cracks == 0) return
    val rng = brick.rng("crack")
    val w = brick.width
    val h = BRICK_HEIGHT
    val color = if (wear.kintsugi) Color(0xFFD8AC4A) else Color.Black.copy(alpha = 0.55f)
    val strokeWidth = if (wear.kintsugi) 1.6f else 1.1f
    repeat(wear.cracks) {
        var x = rng.uniform(0f, w)
        var y = if (rng.chance(0.5f)) 0f else h
        val path = Path().apply { moveTo(x, y) }
        repeat(rng.intBetween(3, 6)) {
            x += rng.uniform(-14f, 14f)
            y += rng.uniform(6f, 14f) * (if (y < h / 2f) 1f else -1f)
            path.lineTo(x, y)
        }
        drawPath(path, color = color, style = Stroke(width = strokeWidth))
    }
}

private fun DrawScope.drawChips(brick: Brick, mortarColor: Color) {
    val wear = brick.wear
    if (wear.chips == 0) return
    val rng = brick.rng("chip")
    val w = brick.width
    val h = BRICK_HEIGHT
    val corners = listOf(Offset(0f, 0f), Offset(w, 0f), Offset(0f, h), Offset(w, h))
    for (corner in rng.sampleWithoutReplacement(corners, wear.chips)) {
        val s = rng.uniform(4f, 9f)
        val sx = if (corner.x == 0f) 1f else -1f
        val sy = if (corner.y == 0f) 1f else -1f
        val path = Path().apply {
            moveTo(corner.x, corner.y + sy * s)
            lineTo(corner.x + sx * s, corner.y)
            lineTo(corner.x, corner.y)
            close()
        }
        drawPath(path, color = mortarColor)
    }
}

// ---------------------------------------------------------------------------
// Artifact
// ---------------------------------------------------------------------------

private fun DrawScope.drawArtifacts(brick: Brick) {
    val artifact = brick.artifact
    val kind = artifact.kind ?: return
    val rng = brick.rng("artifact")
    val w = brick.width
    val h = BRICK_HEIGHT
    repeat(artifact.count) {
        val x = rng.uniform(8f, w - 8f)
        val y = h - rng.uniform(2f, 10f)
        drawMotif(kind, x, y, rng)
    }
}

private fun DrawScope.drawMotif(kind: ArtifactKind, x: Float, y: Float, rng: Rng) {
    when (kind) {
        ArtifactKind.MOSS -> repeat(6) {
            drawCircle(
                color = hsl(96f, 45f, 34f, 0.9f),
                radius = rng.uniform(1.5f, 3.5f),
                center = Offset(x + rng.uniform(-5f, 5f), y + rng.uniform(-3f, 3f)),
            )
        }
        ArtifactKind.LICHEN -> repeat(4) {
            drawCircle(
                color = hsl(70f, 22f, 72f, 0.75f),
                radius = rng.uniform(2f, 4f),
                center = Offset(x + rng.uniform(-6f, 6f), y + rng.uniform(-6f, 6f)),
            )
        }
        ArtifactKind.WEED -> repeat(3) {
            val path = Path().apply {
                moveTo(x, y)
                quadraticTo(x + rng.uniform(-4f, 4f), y - 6f, x + rng.uniform(-6f, 6f), y - 11f)
            }
            drawPath(path, color = hsl(104f, 40f, 38f), style = Stroke(width = 1.2f))
        }
        ArtifactKind.IVY -> {
            val stem = Path().apply {
                moveTo(x, y)
                quadraticTo(x + 8f, y - 8f, x + 14f, y - 16f)
            }
            drawPath(stem, color = hsl(110f, 30f, 28f), style = Stroke(width = 1f))
            for (i in 0 until 4) {
                val cx = x + i * 4f
                val cy = y - i * 4f
                rotate(rng.uniform(-40f, 40f), pivot = Offset(cx, cy)) {
                    drawOval(color = hsl(120f, 38f, 32f), topLeft = Offset(cx - 3f, cy - 2f), size = Size(6f, 4f))
                }
            }
        }
        ArtifactKind.MUSHROOM -> {
            drawRect(color = Color(0xFFE8DCC6), topLeft = Offset(x - 1f, y - 6f), size = Size(2f, 6f))
            drawArc(
                color = hsl(18f, 55f, 46f),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(x - 5f, y - 10f),
                size = Size(10f, 8f),
            )
        }
        ArtifactKind.MAGGOT -> repeat(2) {
            drawOval(
                color = Color(0xFFEFE6D2).copy(alpha = 0.95f),
                topLeft = Offset(x + it * 5f - 3f, y - 1.6f),
                size = Size(6f, 3.2f),
            )
        }
        ArtifactKind.SNAIL -> {
            drawCircle(color = hsl(32f, 45f, 55f), radius = 4f, center = Offset(x, y - 3f))
            drawCircle(
                color = hsl(32f, 45f, 35f),
                radius = 2f,
                center = Offset(x, y - 3f),
                style = Stroke(width = 1f),
            )
        }
        ArtifactKind.COIN -> {
            drawCircle(color = Color(0xFFD8AC4A), radius = 4f, center = Offset(x, y - 4f))
            drawCircle(
                color = Color(0xFFA37F2C),
                radius = 4f,
                center = Offset(x, y - 4f),
                style = Stroke(width = 1f),
            )
            drawCircle(
                color = Color(0xFFFFF8E0).copy(alpha = 0.3f),
                radius = 1f,
                center = Offset(x + 1.6f, y - 6.4f),
            )
        }
        ArtifactKind.FOSSIL -> {
            drawArc(
                color = hsl(30f, 25f, 40f),
                startAngle = -60f,
                sweepAngle = 300f,
                useCenter = false,
                topLeft = Offset(x - 4f, y - 8f),
                size = Size(8f, 8f),
                style = Stroke(width = 1.2f),
            )
            drawArc(
                color = hsl(30f, 25f, 40f),
                startAngle = 120f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(x - 2.5f, y - 5f),
                size = Size(5f, 5f),
                style = Stroke(width = 1.2f),
            )
        }
        // The default/fallback motif in bricks.py — a simple engraved cross-hatch.
        ArtifactKind.ENGRAVING -> {
            drawLine(color = hsl(220f, 8f, 45f), start = Offset(x - 4f, y), end = Offset(x + 4f, y), strokeWidth = 1f)
            drawLine(color = hsl(220f, 8f, 45f), start = Offset(x, y - 4f), end = Offset(x, y + 4f), strokeWidth = 1f)
        }
    }
}

// ---------------------------------------------------------------------------
// Emblem
// ---------------------------------------------------------------------------

private fun DrawScope.drawEmblem(brick: Brick, lod: BrickLod) {
    val emblem = brick.emblem ?: return
    val w = brick.width
    val h = BRICK_HEIGHT
    // Mirrors bricks.py's `_emblem`: margin keeps the glyph off the very edge,
    // atX/atY are normalized into the remaining interior.
    val size = 26f * emblem.scale
    val margin = size * 0.62f
    val x = margin + emblem.atX * max(0f, w - 2 * margin)
    val y = margin * 0.85f + emblem.atY * max(0f, h - 1.7f * margin)

    val nativeCanvas = drawContext.canvas.nativeCanvas
    val textPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
        textAlign = AndroidPaint.Align.CENTER
        textSize = size
    }

    fun drawGlyph(dy: Float = 0f, colorFilter: android.graphics.ColorFilter? = null, alpha: Int = 255) {
        textPaint.colorFilter = colorFilter
        textPaint.alpha = alpha
        val metrics = textPaint.fontMetrics
        val baselineY = y - (metrics.ascent + metrics.descent) / 2f + dy
        rotate(emblem.angle, Offset(x, y)) {
            nativeCanvas.drawText(emblem.char, x, baselineY, textPaint)
        }
    }

    if (lod == BrickLod.Thumbnail) {
        // Thumbnails keep only the natural-colour glyph: identity and
        // completion state must survive at 48dp, the treatment nuance can go.
        drawGlyph()
        return
    }

    when (emblem.treatment) {
        EmblemTreatment.STICKER -> {
            // Approximates the reference's SVG drop-shadow filter: a soft dark
            // copy offset down, then the natural glyph on top.
            drawGlyph(
                dy = 1.5f,
                colorFilter = PorterDuffColorFilter(Color.Black.copy(alpha = 0.45f).toArgb(), PorterDuff.Mode.SRC_IN),
            )
            drawGlyph()
        }
        EmblemTreatment.STAMPED -> {
            val silhouette = if (brick.paint.coverage > 0.05f) brick.paint.toColor() else brick.material.shade(-26f)
            drawGlyph(
                colorFilter = PorterDuffColorFilter(silhouette.toArgb(), PorterDuff.Mode.SRC_IN),
                alpha = (0.82f * 255).toInt(),
            )
        }
        EmblemTreatment.CARVED -> {
            drawGlyph(
                dy = -1.2f,
                colorFilter = PorterDuffColorFilter(brick.material.shade(14f).toArgb(), PorterDuff.Mode.SRC_IN),
                alpha = (0.8f * 255).toInt(),
            )
            drawGlyph(
                colorFilter = PorterDuffColorFilter(brick.material.shade(-24f).toArgb(), PorterDuff.Mode.SRC_IN),
                alpha = (0.9f * 255).toInt(),
            )
        }
        EmblemTreatment.GILDED -> {
            drawGlyph(
                dy = 1.4f,
                colorFilter = PorterDuffColorFilter(Color(0xFF6B4F14).toArgb(), PorterDuff.Mode.SRC_IN),
                alpha = (0.9f * 255).toInt(),
            )
            drawGlyph(colorFilter = PorterDuffColorFilter(Color(0xFFD8AC4A).toArgb(), PorterDuff.Mode.SRC_IN))
        }
    }
}

// ---------------------------------------------------------------------------
// Border
// ---------------------------------------------------------------------------

private fun DrawScope.drawBorder(brick: Brick) {
    val material = brick.material
    val w = brick.width
    val h = BRICK_HEIGHT
    val rim = when {
        material == Material.GOLD -> Color(0xFFD8AC4A)
        material.precious -> Color(0xFFCFE6EE)
        else -> Color.Black.copy(alpha = 0.35f)
    }
    val strokeWidth = if (material.precious) 1.4f else 1f
    drawRoundRect(
        color = rim,
        size = Size(w, h),
        cornerRadius = CornerRadius(Dimens.brickCorner.value),
        style = Stroke(width = strokeWidth),
    )
}

// ---------------------------------------------------------------------------
// Rng helpers local to the renderer — Rng itself is not modified, these are
// small additions consumed only from this UI layer.
// ---------------------------------------------------------------------------

/**
 * A Gaussian-ish deviate via Box-Muller, built only from [Rng.nextFloat]. Used
 * for the spray paint pattern, which needs a normal-ish scatter around a
 * centre point and has no equivalent on [Rng] itself.
 */
private fun Rng.gaussian(mean: Float, stdDev: Float): Float {
    val u1 = nextFloat().coerceAtLeast(1e-7f)
    val u2 = nextFloat()
    val z0 = sqrt(-2f * ln(u1)) * cos(2f * PI.toFloat() * u2)
    return mean + z0 * stdDev
}

/** A deterministic without-replacement pick of [count] items, via repeated [Rng.nextInt]. */
private fun <T> Rng.sampleWithoutReplacement(items: List<T>, count: Int): List<T> {
    val pool = items.toMutableList()
    val picked = ArrayList<T>(count.coerceAtMost(pool.size))
    repeat(count.coerceAtMost(pool.size)) {
        picked += pool.removeAt(nextInt(pool.size))
    }
    return picked
}
