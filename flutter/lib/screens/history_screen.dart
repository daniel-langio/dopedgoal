import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../data/goals_repository.dart';
import '../models/goal.dart';
import '../theme.dart';
import '../widgets/app_shell.dart';
import '../widgets/wall_preview.dart';

class HistoryScreen extends StatelessWidget {
  const HistoryScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final history = context.watch<GoalsRepository>().history;
    return Scaffold(
      backgroundColor: AppColors.bg,
      appBar: const AppTopBar(title: 'History'),
      body: ListView.separated(
        padding: const EdgeInsets.all(16),
        itemCount: history.length,
        separatorBuilder: (_, _) => const SizedBox(height: 16),
        itemBuilder: (context, i) => _HistoryCard(entry: history[i]),
      ),
    );
  }
}

class _HistoryCard extends StatelessWidget {
  const _HistoryCard({required this.entry});

  final HistoryEntry entry;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
      decoration: BoxDecoration(
        color: Colors.white,
        border: Border.all(color: AppColors.border, width: 1.5),
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          Container(
            width: 64,
            height: 44,
            padding: const EdgeInsets.all(5),
            decoration: BoxDecoration(
              color: AppColors.mortar,
              borderRadius: BorderRadius.circular(8),
            ),
            child: WallPreview(
              total: 1,
              placed: 1,
              color: entry.category.dotColor,
              minBrickWidth: 40,
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  entry.task.name,
                  style: appSerif(fontSize: 15, fontWeight: FontWeight.w700, height: 1.25),
                ),
                const SizedBox(height: 4),
                Container(
                  padding: const EdgeInsets.fromLTRB(7, 3, 9, 3),
                  decoration: BoxDecoration(
                    color: AppColors.bg,
                    border: Border.all(color: AppColors.border),
                    borderRadius: BorderRadius.circular(99),
                  ),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text(entry.goalEmoji, style: const TextStyle(fontSize: 11)),
                      const SizedBox(width: 5),
                      Text(entry.goalName, style: appMono(fontSize: 10.5)),
                    ],
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  _relativeTime(entry.completedAt).toUpperCase(),
                  style: appMono(fontSize: 10, color: AppColors.muted.withValues(alpha: 0.7)),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

String _relativeTime(DateTime time) {
  final diff = DateTime.now().difference(time);
  if (diff.inMinutes < 60) return '${diff.inMinutes.clamp(1, 59)} minutes ago';
  if (diff.inHours < 24) return '${diff.inHours} hours ago';
  if (diff.inDays == 1) return 'Yesterday';
  if (diff.inDays < 7) return '${diff.inDays} days ago';
  final weeks = (diff.inDays / 7).floor();
  return weeks == 1 ? '1 week ago' : '$weeks weeks ago';
}
