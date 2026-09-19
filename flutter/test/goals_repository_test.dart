import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:dopedgoal_flutter/data/goals_repository.dart';
import 'package:dopedgoal_flutter/models/goal.dart';

void main() {
  setUp(() {
    SharedPreferences.setMockInitialValues({});
  });

  test('toggling a task persists across repository instances', () async {
    final repo1 = GoalsRepository();
    await pumpEventQueue();

    final goal = repo1.goals.first;
    final task = goal.tasks.firstWhere((t) => !t.isComplete);

    repo1.toggleTask(goal.id, task.id);
    await pumpEventQueue();

    final repo2 = GoalsRepository();
    await pumpEventQueue();

    final reloadedTask = repo2.goalById(goal.id).tasks.firstWhere((t) => t.id == task.id);
    expect(reloadedTask.isComplete, isTrue);
  });

  test('a newly added goal survives a fresh repository instance', () async {
    final repo1 = GoalsRepository();
    await pumpEventQueue();
    final before = repo1.goals.length;

    repo1.addGoal(Goal(
      id: 'test-goal',
      name: 'TEST GOAL',
      category: GoalCategory.personal,
      emoji: '🧪',
      cohesion: 0.5,
      wallSeed: 'TEST-0000',
      createdAt: DateTime.now(),
      tasks: [Task(id: 'test-task', name: 'A task')],
    ));
    await pumpEventQueue();

    final repo2 = GoalsRepository();
    await pumpEventQueue();
    expect(repo2.goals.length, before + 1);
    expect(repo2.goalById('test-goal').name, 'TEST GOAL');
  });
}
