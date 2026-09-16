package app.dopedgoal.ui.goaldetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dopedgoal.core.Goal
import app.dopedgoal.core.Task
import app.dopedgoal.data.InMemoryGoalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class GoalDetailViewModel(
    private val repository: InMemoryGoalRepository,
    private val goalId: String,
) : ViewModel() {

    private val _goal = MutableStateFlow<Goal?>(null)
    val goal = _goal

    private val _undoSnackbar = MutableStateFlow<UndoState?>(null)
    val undoSnackbar = _undoSnackbar

    init {
        loadGoal()
    }

    private fun loadGoal() {
        viewModelScope.launch {
            repository.goalWithTasks(goalId)
                .collect { _goal.value = it }
        }
    }

    fun completeTask(task: Task) {
        val completedAt = System.currentTimeMillis()
        viewModelScope.launch {
            repository.completeTask(task.id, completedAt)
            _undoSnackbar.value = UndoState(task.id, completedAt)
        }
    }

    fun undoLastCompletion() {
        _undoSnackbar.value?.let { state ->
            viewModelScope.launch {
                repository.uncompleteTask(state.taskId)
                _undoSnackbar.value = null
            }
        }
    }

    fun dismissUndo() {
        _undoSnackbar.value = null
    }

    data class UndoState(val taskId: String, val completedAt: Long)
}