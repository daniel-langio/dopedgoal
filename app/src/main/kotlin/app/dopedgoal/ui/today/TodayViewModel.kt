package app.dopedgoal.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dopedgoal.core.Goal
import app.dopedgoal.data.InMemoryGoalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class TodayViewModel(private val repository: InMemoryGoalRepository) : ViewModel() {

    private val _nextBrick = MutableStateFlow<Pair<Goal, Int>?>(null)
    val nextBrick = _nextBrick

    val activeGoals = repository.activeGoals.distinctUntilChanged()
    val allGoals = repository.allGoals.distinctUntilChanged()

    init {
        loadNextBrick()
    }

    private fun loadNextBrick() {
        viewModelScope.launch {
            combine(
                activeGoals,
                allGoals,
            ) { activeGoals, allGoals ->
                // Find the next incomplete task across all active goals
                // Prefer the goal with the fewest placed bricks (most urgent)
                val candidate = activeGoals
                    .filter { !it.isAchieved }
                    .mapNotNull { goal ->
                        goal.tasks.firstOrNull { !it.isComplete }
                            ?.let { task -> goal to task }
                    }
                    .minByOrNull { (goal, _) -> goal.placedCount }

                if (candidate != null) {
                    val (goal, task) = candidate
                    val brickIndex = goal.tasks.indexOf(task)
                    _nextBrick.value = goal to brickIndex
                } else {
                    _nextBrick.value = null
                }
            }.collect { }
        }
    }

    fun completeTask(taskId: String) {
        viewModelScope.launch {
            repository.completeTask(taskId)
        }
    }

    fun uncompleteTask(taskId: String) {
        viewModelScope.launch {
            repository.uncompleteTask(taskId)
        }
    }
}