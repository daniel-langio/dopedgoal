package app.dopedgoal.ui.wall

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import app.dopedgoal.core.BRICK_HEIGHT
import app.dopedgoal.core.BRICK_WIDTH
import app.dopedgoal.core.Brick
import app.dopedgoal.core.Rng
import app.dopedgoal.core.seedOf
import app.dopedgoal.ui.theme.Dimens
import app.dopedgoal.ui.theme.wall

/** Fixed gap between adjacent bricks, in nominal units — matches `bricks.py`'s `Wall.render`. */
private const val GAP = 4f

/**
 * Roughly one row in several ends its trailing edge one ghost brick short
 * instead of packing flush to the column edge — enough to read as an organic,
 * hand-built silhouette at the top of the wall without looking sparse or
 * broken. Only ever applies to ghost slots; see [layoutWall].
 */
private const val DROP_PROBABILITY = 0.2f

/**
 * Running-bond layout of [placedBricks] (already in build order — oldest
 * first, per `Goal.placedBricks`) plus [ghostCount] empty slots for
 * incomplete tasks.
 *
 * Reads mortar/ghost colours from the theme, same pattern as `BrickCanvas`;
 * everything else is delegated to [drawWall], a pure function of its
 * arguments.
 */
@Composable
fun WallCanvas(
    goalId: String,
    placedBricks: List<Brick>,
    ghostCount: Int,
    modifier: Modifier = Modifier,
    columns: Int = 5,
    lod: BrickLod = BrickLod.Full,
) {
    val mortarColor = MaterialTheme.wall.mortar
    val ghostColor = MaterialTheme.wall.ghost
    Canvas(modifier = modifier) {
        drawWall(goalId, placedBricks, ghostCount, mortarColor, ghostColor, columns, lod)
    }
}

/**
 * Pure rendering of the wall into this [DrawScope]. No composition-local
 * reads: every pixel is a function of the arguments, matching the discipline
 * `drawBrick` keeps — the dropped-brick silhouette must be stable across
 * recompositions of the same wall, not re-rolled on every draw.
 */
fun DrawScope.drawWall(
    goalId: String,
    placedBricks: List<Brick>,
    ghostCount: Int,
    mortarColor: Color,
    ghostColor: Color,
    columns: Int,
    lod: BrickLod = BrickLod.Full,
) {
    val placements = layoutWall(placedBricks, ghostCount, columns, goalId)
    if (placements.isEmpty()) return

    val nominalWidth = BRICK_WIDTH * columns
    val nominalHeight = placements.maxOf { it.y } + BRICK_HEIGHT

    withTransform({
        scale(size.width / nominalWidth, size.height / nominalHeight, pivot = Offset.Zero)
    }) {
        for (placement in placements) {
            // Row 0 (the oldest bricks) renders at the bottom; later rows
            // stack upward, mirroring CONTEXT.md's "bricks fill bottom-up".
            val flippedY = nominalHeight - BRICK_HEIGHT - placement.y
            translate(left = placement.x, top = flippedY) {
                if (placement.brick != null) {
                    drawBrick(placement.brick, mortarColor, lod)
                } else {
                    drawGhostBrick(placement.width, ghostColor)
                }
            }
        }
    }
}

/** One laid-out slot: a real brick, or (when [brick] is null) a ghost of [width]. */
private data class WallPlacement(val x: Float, val y: Float, val width: Float, val brick: Brick?)

/**
 * Ports `Wall.render`'s row-wrapping loop from bricks.py, with two overrides
 * from ANDROID_DESIGN.md/CONTEXT.md that the Python reference doesn't itself
 * implement (it is layout reference only, not the final word on wall rules):
 * odd rows are offset by half a brick width for a true running bond, and a
 * ghost row occasionally drops its trailing slot for an organic top edge.
 *
 * Row 0 holds the first (oldest) bricks at `y = 0`; the caller flips the
 * vertical axis so row 0 renders at the bottom of the canvas and later rows
 * stack upward.
 */
private fun layoutWall(
    placedBricks: List<Brick>,
    ghostCount: Int,
    columns: Int,
    goalId: String,
): List<WallPlacement> {
    val rowWidth = BRICK_WIDTH * columns
    val placements = ArrayList<WallPlacement>(placedBricks.size + ghostCount)

    var x = 0f
    var y = 0f
    var row = 0
    var columnInRow = 0

    fun startNewRow() {
        row += 1
        y += BRICK_HEIGHT + GAP
        x = if (row % 2 == 1) BRICK_WIDTH / 2f else 0f
        columnInRow = 0
    }

    for (brick in placedBricks) {
        if (x + brick.width > rowWidth) startNewRow()
        placements += WallPlacement(x, y, brick.width, brick)
        x += brick.width + GAP
        columnInRow += 1
    }

    // A dedicated, wall-level salt — deliberately not `brick.rng(...)`, which
    // is per-task and would tie an organic-silhouette decision to a brick
    // that hasn't been minted yet. Derived from the goal id so it's a pure,
    // stable function of the wall being rendered and unique per goal (unlike
    // keying off `WallStyle`, whose fields two different goals could share).
    val dropRng = Rng(seedOf("wall-drop:$goalId"))
    val ghostWidth = BRICK_WIDTH
    repeat(ghostCount) {
        val wouldOverflow = x + ghostWidth > rowWidth
        val isRowTrailingSlot = columnInRow >= columns - 1
        val dropTrailingGhost = !wouldOverflow && isRowTrailingSlot && dropRng.chance(DROP_PROBABILITY)
        if (wouldOverflow || dropTrailingGhost) startNewRow()
        placements += WallPlacement(x, y, ghostWidth, null)
        x += ghostWidth + GAP
        columnInRow += 1
    }

    return placements
}

/** A dashed rounded-rect outline for an incomplete task's slot. */
private fun DrawScope.drawGhostBrick(width: Float, ghostColor: Color) {
    drawRoundRect(
        color = ghostColor,
        size = Size(width, BRICK_HEIGHT),
        cornerRadius = CornerRadius(Dimens.brickCorner.value),
        style = Stroke(width = 1.4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))),
    )
}
