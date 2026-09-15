package app.dopedgoal.core

/**
 * One task on a goal. Its brick is minted here, at creation, and kept forever —
 * completing the task only decides *where* on the wall the brick lands.
 */
data class Task(
    val id: String,
    val goalId: String,
    val name: String,
    val brick: Brick,
    val completedAt: Long? = null,
) {
    val isComplete: Boolean get() = completedAt != null

    init {
        require(name.isNotBlank()) { "a task needs a name" }
        require(brick.taskId == id) { "brick ${brick.taskId} was not minted for task $id" }
    }
}

enum class Category { HEALTH, LEARNING, CAREER, CREATIVE, SOCIAL, PERSONAL }

/**
 * A goal is a wall. Its task count is fixed at creation, so the wall has a
 * finite, visible end from the first brick onward.
 *
 * Tasks may be completed in any order. [placedCount] is what fills the wall
 * bottom-up; [tasks] stays in the order the user wrote them.
 */
data class Goal(
    val id: String,
    val name: String,
    val category: Category,
    val emoji: String,
    val cohesion: Cohesion,
    val style: WallStyle,
    val banner: Banner,
    val tasks: List<Task>,
    val createdAt: Long,
    val completedAt: Long? = null,
) {
    init {
        require(name.isNotBlank()) { "a goal needs a name" }
        require(tasks.isNotEmpty()) { "a wall needs at least one brick" }
        require(tasks.all { it.goalId == id }) { "every task must belong to goal $id" }
        require(tasks.map { it.id }.toSet().size == tasks.size) { "duplicate task id" }
        require(banner.goalId == id) { "banner was minted for ${banner.goalId}, not $id" }
    }

    val total: Int get() = tasks.size

    val placedCount: Int get() = tasks.count { it.isComplete }

    val isAchieved: Boolean get() = placedCount == total

    /** Bricks in the order they were actually placed — the wall's real build order. */
    val placedBricks: List<Brick>
        get() = tasks.filter { it.isComplete }
            .sortedBy { requireNotNull(it.completedAt) }
            .map { it.brick }
}
