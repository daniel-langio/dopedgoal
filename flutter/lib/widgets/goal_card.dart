import 'package:flutter/material.dart';
import '../models/goal.dart';
import '../theme.dart';
import 'wall_preview.dart';

class GoalCard extends StatelessWidget {
  const GoalCard({super.key, required this.goal});

  final Goal goal;

  @override
  Widget build(BuildContext context) {
    final done = goal.isAchieved;
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        border: Border.all(
          color: done ? AppColors.gold : AppColors.border,
          width: done ? 2 : 1.5,
        ),
      ),
      clipBehavior: Clip.antiAlias,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Stack(
            children: [
              Container(
                color: AppColors.mortar,
                child: WallPreview(
                  total: goal.total,
                  placed: goal.placedCount,
                  color: goal.category.dotColor,
                ),
              ),
              if (done)
                Positioned(
                  top: 10,
                  right: 10,
                  child: Container(
                    padding: const EdgeInsets.symmetric(horizontal: 9, vertical: 4),
                    decoration: BoxDecoration(
                      color: AppColors.gold,
                      borderRadius: BorderRadius.circular(99),
                    ),
                    child: Text(
                      '✦ DONE',
                      style: appMono(
                        fontSize: 10,
                        fontWeight: FontWeight.w700,
                        color: Colors.white,
                        letterSpacing: 0.5,
                      ),
                    ),
                  ),
                ),
            ],
          ),
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 14, 16, 16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Text(goal.emoji, style: const TextStyle(fontSize: 20, height: 1)),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Text(
                        goal.name,
                        style: appSerif(fontSize: 16, fontWeight: FontWeight.w700, height: 1.25),
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
                      decoration: BoxDecoration(
                        color: goal.category.dotColor,
                        shape: BoxShape.circle,
                      ),
                    ),
                    const SizedBox(width: 6),
                    Text(goal.category.label, style: appMono(fontSize: 11)),
                  ],
                ),
                const SizedBox(height: 13),
                Text(
                  '${goal.placedCount}/${goal.total} TASKS',
                  style: appMono(fontSize: 11, letterSpacing: 0.4),
                ),
                const SizedBox(height: 7),
                ClipRRect(
                  borderRadius: BorderRadius.circular(99),
                  child: LinearProgressIndicator(
                    value: goal.progress,
                    minHeight: 4,
                    backgroundColor: AppColors.border,
                    valueColor: AlwaysStoppedAnimation(
                      done ? AppColors.gold : AppColors.accent,
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
