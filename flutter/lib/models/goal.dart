import 'package:flutter/material.dart';

enum GoalCategory {
  health,
  learning,
  career,
  creative,
  social,
  personal;

  static GoalCategory fromName(String name) =>
      GoalCategory.values.firstWhere((c) => c.name == name, orElse: () => GoalCategory.personal);
}

extension CategoryLabel on GoalCategory {
  String get label => switch (this) {
        GoalCategory.health => 'HEALTH',
        GoalCategory.learning => 'LEARNING',
        GoalCategory.career => 'CAREER',
        GoalCategory.creative => 'CREATIVE',
        GoalCategory.social => 'SOCIAL',
        GoalCategory.personal => 'PERSONAL',
      };

  Color get dotColor => switch (this) {
        GoalCategory.health => const Color(0xFFC96A5C),
        GoalCategory.learning => const Color(0xFF4A7FC4),
        GoalCategory.career => const Color(0xFF8A6BBF),
        GoalCategory.creative => const Color(0xFFC9639A),
        GoalCategory.social => const Color(0xFF5FA8A0),
        GoalCategory.personal => const Color(0xFF6BA368),
      };
}

/// One task on a goal. Its brick color is fixed at creation; completing the
/// task only decides whether the brick is showing on the wall yet.
class Task {
  Task({
    required this.id,
    required this.name,
    this.completedAt,
  });

  final String id;
  final String name;
  final DateTime? completedAt;

  bool get isComplete => completedAt != null;

  Task markComplete() => Task(id: id, name: name, completedAt: DateTime.now());
  Task markIncomplete() => Task(id: id, name: name);

  Map<String, dynamic> toJson() => {
        'id': id,
        'name': name,
        'completedAt': completedAt?.toIso8601String(),
      };

  factory Task.fromJson(Map<String, dynamic> json) => Task(
        id: json['id'] as String,
        name: json['name'] as String,
        completedAt: json['completedAt'] == null
            ? null
            : DateTime.parse(json['completedAt'] as String),
      );
}

/// A goal is a wall: a fixed list of tasks/bricks, built up as tasks complete.
class Goal {
  Goal({
    required this.id,
    required this.name,
    required this.category,
    required this.emoji,
    required this.cohesion,
    required this.wallSeed,
    required this.tasks,
    required this.createdAt,
  }) : assert(tasks.isNotEmpty, 'a wall needs at least one brick');

  final String id;
  final String name;
  final GoalCategory category;
  final String emoji;
  final double cohesion;
  final String wallSeed;
  final List<Task> tasks;
  final DateTime createdAt;

  int get total => tasks.length;
  int get placedCount => tasks.where((t) => t.isComplete).length;
  bool get isAchieved => placedCount == total;
  double get progress => total == 0 ? 0 : placedCount / total;

  Goal copyWith({List<Task>? tasks}) => Goal(
        id: id,
        name: name,
        category: category,
        emoji: emoji,
        cohesion: cohesion,
        wallSeed: wallSeed,
        createdAt: createdAt,
        tasks: tasks ?? this.tasks,
      );

  Map<String, dynamic> toJson() => {
        'id': id,
        'name': name,
        'category': category.name,
        'emoji': emoji,
        'cohesion': cohesion,
        'wallSeed': wallSeed,
        'createdAt': createdAt.toIso8601String(),
        'tasks': tasks.map((t) => t.toJson()).toList(),
      };

  factory Goal.fromJson(Map<String, dynamic> json) => Goal(
        id: json['id'] as String,
        name: json['name'] as String,
        category: GoalCategory.fromName(json['category'] as String),
        emoji: json['emoji'] as String,
        cohesion: (json['cohesion'] as num).toDouble(),
        wallSeed: json['wallSeed'] as String,
        createdAt: DateTime.parse(json['createdAt'] as String),
        tasks: (json['tasks'] as List<dynamic>)
            .map((t) => Task.fromJson(t as Map<String, dynamic>))
            .toList(),
      );
}

/// One completed task, as it shows up in History — a task plus the goal it
/// belongs to, so the log doesn't need to look the goal back up.
class HistoryEntry {
  HistoryEntry({
    required this.task,
    required this.goalName,
    required this.goalEmoji,
    required this.category,
    required this.completedAt,
  });

  final Task task;
  final String goalName;
  final String goalEmoji;
  final GoalCategory category;
  final DateTime completedAt;
}
