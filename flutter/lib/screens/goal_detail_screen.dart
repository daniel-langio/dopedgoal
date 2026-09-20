import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../data/goals_repository.dart';
import '../models/goal.dart';
import '../theme.dart';
import '../widgets/app_shell.dart';
import '../widgets/wall_preview.dart';

class GoalDetailScreen extends StatelessWidget {
  const GoalDetailScreen({super.key, required this.goalId});

  final String goalId;

  @override
  Widget build(BuildContext context) {
    final repo = context.watch<GoalsRepository>();
    final matches = repo.goals.where((g) => g.id == goalId);
    final goal = matches.isEmpty ? null : matches.first;

    if (goal == null) {
      // The goal it pointed to no longer exists (deleted elsewhere) — back
      // out rather than show a broken detail page.
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (Navigator.of(context).canPop()) Navigator.of(context).pop();
      });
      return const Scaffold(backgroundColor: AppColors.bg, body: SizedBox.shrink());
    }

    return Scaffold(
      backgroundColor: AppColors.bg,
      body: Column(
        children: [
          SafeArea(
            bottom: false,
            child: Container(
              padding: const EdgeInsets.fromLTRB(16, 16, 16, 14),
              decoration: const BoxDecoration(
                border: Border(bottom: BorderSide(color: AppColors.border, width: 1.5)),
              ),
              child: Row(
                children: [
                  SquareIconButton(
                    icon: Icons.arrow_back_rounded,
                    semanticLabel: 'Back to goals',
                    onTap: () => Navigator.of(context).pop(),
                  ),
                  const SizedBox(width: 10),
                  Expanded(
                    child: Text(
                      goal.name,
                      overflow: TextOverflow.ellipsis,
                      style: appMono(fontSize: 15, fontWeight: FontWeight.w700, color: AppColors.ink),
                    ),
                  ),
                ],
              ),
            ),
          ),
          Expanded(
            child: ListView(
              padding: const EdgeInsets.all(16),
              children: [
                Container(
                  decoration: BoxDecoration(
                    color: Colors.white,
                    border: Border.all(
                      color: goal.isAchieved ? AppColors.gold : AppColors.border,
                      width: goal.isAchieved ? 2 : 1.5,
                    ),
                  ),
                  clipBehavior: Clip.antiAlias,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      Container(
                        color: AppColors.mortar,
                        child: WallPreview(
                          total: goal.total,
                          placed: goal.placedCount,
                          color: goal.category.dotColor,
                        ),
                      ),
                      Padding(
                        padding: const EdgeInsets.fromLTRB(16, 14, 16, 16),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Row(
                              children: [
                                Text(goal.emoji, style: const TextStyle(fontSize: 22, height: 1)),
                                const SizedBox(width: 8),
                                Expanded(
                                  child: Text(
                                    goal.name,
                                    style: appSerif(fontSize: 17, fontWeight: FontWeight.w700),
                                  ),
                                ),
                              ],
                            ),
                            const SizedBox(height: 9),
                            Row(
                              children: [
                                Container(
                                  width: 8,
                                  height: 8,
                                  decoration: BoxDecoration(color: goal.category.dotColor, shape: BoxShape.circle),
                                ),
                                const SizedBox(width: 6),
                                Text(goal.category.label, style: appMono(fontSize: 11)),
                                const Spacer(),
                                Text(
                                  '${goal.placedCount}/${goal.total} TASKS',
                                  style: appMono(fontSize: 11, letterSpacing: 0.4),
                                ),
                              ],
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 22),
                Text('TASKS', style: appMono(fontSize: 11, fontWeight: FontWeight.w700, letterSpacing: 0.9)),
                const SizedBox(height: 12),
                for (var i = 0; i < goal.tasks.length; i++) ...[
                  if (i > 0) const SizedBox(height: 8),
                  _TaskRow(
                    task: goal.tasks[i],
                    onTap: () => context.read<GoalsRepository>().toggleTask(goal.id, goal.tasks[i].id),
                  ),
                ],
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _TaskRow extends StatelessWidget {
  const _TaskRow({required this.task, required this.onTap});

  final Task task;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final done = task.isComplete;
    return Semantics(
      button: true,
      checked: done,
      label: task.name,
      child: InkWell(
        onTap: onTap,
        child: Container(
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 12),
          decoration: BoxDecoration(
            color: Colors.white,
            border: Border.all(color: done ? AppColors.gold : AppColors.border, width: done ? 2 : 1.5),
          ),
          child: Row(
            children: [
              Container(
                width: 22,
                height: 22,
                alignment: Alignment.center,
                decoration: BoxDecoration(
                  color: done ? AppColors.gold : Colors.transparent,
                  border: Border.all(color: done ? AppColors.gold : AppColors.ink, width: 1.5),
                  borderRadius: BorderRadius.circular(6),
                ),
                child: done ? const Icon(Icons.check, size: 15, color: Colors.white) : null,
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Text(
                  task.name,
                  style: appSerif(fontSize: 14, color: done ? AppColors.muted : AppColors.ink).copyWith(
                    decoration: done ? TextDecoration.lineThrough : TextDecoration.none,
                    decorationColor: AppColors.muted,
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
