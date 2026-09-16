package app.dopedgoal.ui.creategoal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateOf
import app.dopedgoal.core.Banner
import app.dopedgoal.core.Brick
import app.dopedgoal.core.Category
import app.dopedgoal.core.Cohesion
import app.dopedgoal.core.Goal
import app.dopedgoal.core.Task
import app.dopedgoal.core.WallStyle
import app.dopedgoal.core.mintBanner
import app.dopedgoal.core.mintBrick
import app.dopedgoal.core.mintWallStyle
import app.dopedgoal.core.seedOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.UUID

class CreateGoalViewModel : ViewModel() {

    // Step 1: Goal basics
    private var _name = mutableStateOf("")
    var name: String
        get() = _name.value
        private set(value) { _name.value = value }

    var category: Category = Category.PERSONAL
    var emoji: String = "🎯"
    var banner: Banner = mintBanner("temp", emoji)

    // Step 2: Tasks
    var tasks: MutableList<Task> = mutableListOf()
    var cohesion: Cohesion = Cohesion.DEFAULT
    private var _nextBrickIndex = 0

    // UI state
    var currentStep: Int = 1
        private set
    val canProceedToStep2 = MutableStateFlow(false)
    val canCreateGoal = MutableStateFlow(false)

    init {
        updateBanner()
        addTask() // Start with one task
    }

    fun updateName(newName: String) {
        name = newName
        updateBanner()
        updateCanCreateGoal()
    }

    fun updateCategory(newCategory: Category) {
        category = newCategory
    }

    fun updateEmoji(newEmoji: String) {
        emoji = newEmoji
        updateBanner()
    }

    fun updateCohesion(newCohesion: Cohesion) {
        cohesion = newCohesion
    }

    fun addTask() {
        val taskId = "task-${_nextBrickIndex}"
        _nextBrickIndex++
        val brick = mintBrick(taskId, tempWallStyle(), cohesion, emoji)
        val task = Task(
            id = taskId,
            goalId = "temp",
            name = "",
            brick = brick,
        )
        tasks.add(task)
        updateCanProceedToStep2()
        updateCanCreateGoal()
    }

    fun removeTask(index: Int) {
        if (tasks.size > 1) {
            tasks.removeAt(index)
            // Re-index remaining tasks
            tasks.forEachIndexed { index, task ->
                val newTaskId = "task-$index"
                val newBrick = mintBrick(newTaskId, tempWallStyle(), cohesion, emoji)
                tasks[index] = task.copy(
                    id = newTaskId,
                    brick = newBrick.copy(taskId = newTaskId),
                )
            }
            _nextBrickIndex = tasks.size
            updateCanProceedToStep2()
            updateCanCreateGoal()
        }
    }

    fun updateTaskName(index: Int, newName: String) {
        tasks[index] = tasks[index].copy(name = newName)
        updateCanCreateGoal()
    }

    fun proceedToStep2() {
        if (name.isNotBlank()) {
            currentStep = 2
        }
    }

    fun goBackToStep1() {
        currentStep = 1
    }

    fun buildGoal(): Goal {
        val goalId = UUID.randomUUID().toString()
        val wallStyle = mintWallStyle(goalId)
        val banner = mintBanner(goalId, emoji)

        // Re-mint all bricks with the real goal's wall style
        val finalTasks = tasks.mapIndexed { index, task ->
            val taskId = "task-$index"
            val brick = mintBrick(taskId, wallStyle, cohesion, emoji)
            Task(
                id = taskId,
                goalId = goalId,
                name = task.name,
                brick = brick,
            )
        }

        return Goal(
            id = goalId,
            name = name,
            category = category,
            emoji = emoji,
            cohesion = cohesion,
            style = wallStyle,
            banner = banner,
            tasks = finalTasks,
            createdAt = System.currentTimeMillis(),
        )
    }

    fun isLargeWallWarning(): Boolean {
        return tasks.size > 30
    }

    private fun updateBanner() {
        banner = mintBanner("temp", emoji)
    }

    private fun updateCanProceedToStep2() {
        canProceedToStep2.value = name.isNotBlank()
    }

    private fun updateCanCreateGoal() {
        canCreateGoal.value = name.isNotBlank() && tasks.any { it.name.isNotBlank() }
    }

    private fun tempWallStyle(): WallStyle {
        return mintWallStyle("temp")
    }

    companion object {
        @JvmStatic
        fun getDefaultEmoji(): String = "🎯"
    }
}