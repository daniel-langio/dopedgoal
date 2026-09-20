import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../models/goal.dart';

const _storageKey = 'dopedgoal.goals.v1';

/// Goals store for the "Halftone Goals" design — not a port of the Kotlin
/// domain layer. Persisted as one JSON blob in shared_preferences; fine for
/// this app's data size, not meant to scale to a real database.
class GoalsRepository extends ChangeNotifier {
  GoalsRepository() : _goals = [] {
    _load();
  }

  List<Goal> _goals;
  bool _loaded = false;

  /// True once the persisted state (if any) has been read and applied.
  bool get isLoaded => _loaded;

  Future<void> _load() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final raw = prefs.getString(_storageKey);
      if (raw != null) {
        final decoded = jsonDecode(raw) as List<dynamic>;
        _goals = decoded.map((g) => Goal.fromJson(g as Map<String, dynamic>)).toList();
      }
    } catch (_) {
      // Corrupt or unreadable state: keep whatever is already in memory rather than crash.
    } finally {
      _loaded = true;
      notifyListeners();
    }
  }

  Future<void> _persist() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_storageKey, jsonEncode(_goals.map((g) => g.toJson()).toList()));
  }

  List<Goal> get goals => List.unmodifiable(_goals);

  Goal goalById(String id) => _goals.firstWhere((g) => g.id == id);

  /// Every completed task across every goal, newest first — this is what the
  /// History screen shows, so the log always agrees with the goals' walls.
  List<HistoryEntry> get history {
    final entries = <HistoryEntry>[
      for (final goal in _goals)
        for (final task in goal.tasks)
          if (task.isComplete)
            HistoryEntry(
              task: task,
              goalName: goal.name,
              goalEmoji: goal.emoji,
              category: goal.category,
              completedAt: task.completedAt!,
            ),
    ];
    entries.sort((a, b) => b.completedAt.compareTo(a.completedAt));
    return entries;
  }

  int get totalBricksPlaced =>
      _goals.fold(0, (sum, g) => sum + g.placedCount);

  void addGoal(Goal goal) {
    _goals.insert(0, goal);
    notifyListeners();
    _persist();
  }

  /// Toggles one task's completion. Flipping it back off is allowed here —
  /// unlike the Kotlin core's permanent-brick model, this fresh domain treats
  /// a task like an ordinary checklist item.
  void toggleTask(String goalId, String taskId) {
    final goalIndex = _goals.indexWhere((g) => g.id == goalId);
    if (goalIndex == -1) return;
    final goal = _goals[goalIndex];

    final tasks = [
      for (final task in goal.tasks)
        if (task.id == taskId)
          (task.isComplete ? task.markIncomplete() : task.markComplete())
        else
          task,
    ];

    _goals[goalIndex] = goal.copyWith(tasks: tasks);
    notifyListeners();
    _persist();
  }
}
