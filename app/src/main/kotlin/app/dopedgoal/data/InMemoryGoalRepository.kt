package app.dopedgoal.data

import app.dopedgoal.core.Goal
import app.dopedgoal.core.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class InMemoryGoalRepository private constructor() {

    private val _goals = MutableStateFlow<MutableMap<String, Goal>>(mutableMapOf())

    val allGoals = _goals.map { it.values.toList().sortedByDescending { it.createdAt } }
    val activeGoals = _goals.map { it.values.filter { !it.isAchieved }.sortedByDescending { it.createdAt } }
    val completedGoals = _goals.map { it.values.filter { it.isAchieved }.sortedByDescending { it.completedAt ?: 0L } }

    fun goalWithTasks(goalId: String) = _goals.map { it[goalId] }

    suspend fun createGoal(goal: Goal) {
        _goals.value = (_goals.value + (goal.id to goal)).toMutableMap()
    }

    private fun updateGoalTasks(goals: MutableMap<String, Goal>, goalId: String, taskId: String, completedAt: Long?): MutableMap<String, Goal> {
        val goal = goals[goalId] ?: return goals
        val updatedTasks = goal.tasks.map { task ->
            if (task.id == taskId) task.copy(completedAt = completedAt) else task
        }
        return (goals + (goalId to goal.copy(tasks = updatedTasks))).toMutableMap()
    }

    suspend fun completeTask(taskId: String, completedAt: Long = System.currentTimeMillis()) {
        _goals.value = _goals.value.let { goals ->
            var foundGoalId: String? = null
            for (entry in goals) {
                if (entry.value.tasks.any { it.id == taskId }) {
                    foundGoalId = entry.key
                    break
                }
            }
            foundGoalId?.let { id -> updateGoalTasks(goals, id, taskId, completedAt) } ?: goals
        }.toMutableMap()
    }

    suspend fun uncompleteTask(taskId: String) {
        _goals.value = _goals.value.let { goals ->
            var foundGoalId: String? = null
            for (entry in goals) {
                if (entry.value.tasks.any { it.id == taskId }) {
                    foundGoalId = entry.key
                    break
                }
            }
            foundGoalId?.let { id -> updateGoalTasks(goals, id, taskId, null) } ?: goals
        }.toMutableMap()
    }

    suspend fun deleteGoal(goalId: String) {
        _goals.value = (_goals.value - goalId).toMutableMap()
    }

    companion object {
        @Volatile
        private var INSTANCE: InMemoryGoalRepository? = null

        fun getInstance(): InMemoryGoalRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = InMemoryGoalRepository()
                INSTANCE = instance
                instance
            }
        }
    }
}