package app.dopedgoal.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GoalTest {

    private fun brickFor(taskId: String) = Brick(
        taskId = taskId,
        material = Material.CLAY,
        paint = Paint(20f, 50f, 50f, 0.4f, PaintPattern.BANDED),
        wear = Wear(cracks = 1, chips = 0, kintsugi = false, stain = 0.2f),
        artifact = Artifact.NONE,
        aspect = 1f,
        seed = taskId.hashCode().toLong(),
    )

    private fun task(id: String, completedAt: Long? = null) =
        Task(id = id, goalId = "goal-1", name = "Task $id", brick = brickFor(id), completedAt = completedAt)

    private fun goal(tasks: List<Task>) = Goal(
        id = "goal-1",
        name = "Learn Kotlin",
        category = Category.LEARNING,
        emoji = "📚",
        cohesion = Cohesion.DEFAULT,
        style = WallStyle(Material.CLAY, 20f, 50f, 0.4f, PaintPattern.BANDED),
        banner = mintBanner(goalId = "goal-1", charge = "📚"),
        tasks = tasks,
        createdAt = 0L,
    )

    @Test
    fun `wall fills in completion order, not list order`() {
        val g = goal(
            listOf(
                task("a", completedAt = 300L),
                task("b"),
                task("c", completedAt = 100L),
                task("d", completedAt = 200L),
            )
        )
        assertEquals(listOf("c", "d", "a"), g.placedBricks.map { it.taskId })
        assertEquals(3, g.placedCount)
        assertEquals(4, g.total)
        assertFalse(g.isAchieved)
    }

    @Test
    fun `a goal is achieved only when every brick is placed`() {
        val g = goal(listOf(task("a", completedAt = 1L), task("b", completedAt = 2L)))
        assertTrue(g.isAchieved)
    }

    @Test
    fun `a wall needs at least one brick`() {
        assertFailsWith<IllegalArgumentException> { goal(emptyList()) }
    }

    @Test
    fun `a brick minted for another task cannot be attached`() {
        assertFailsWith<IllegalArgumentException> {
            Task(id = "a", goalId = "goal-1", name = "Mismatched", brick = brickFor("b"))
        }
    }

    @Test
    fun `fringe cannot hang from a swallowtail`() {
        assertFailsWith<IllegalArgumentException> {
            Banner(
                goalId = "goal-1",
                silhouette = Silhouette.SWALLOWTAIL,
                division = Division.PLAIN,
                cloth = Cloth(ClothKind.SILK, 200f, 50f, 40f),
                trim = Trim.FRINGE,
                fray = Fray(holes = 0, hem = 0f, fade = 0f),
                charge = Charge("📚"),
                seed = 1L,
            )
        }
    }

    @Test
    fun `cohesion stays inside its range`() {
        assertFailsWith<IllegalArgumentException> { Cohesion(1.4f) }
        assertEquals(0.6f, Cohesion.DEFAULT.value)
    }
}
