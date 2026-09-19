import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../data/goals_repository.dart';
import '../theme.dart';
import '../widgets/app_shell.dart';
import '../widgets/goal_card.dart';
import 'create_goal_screen.dart';
import 'goal_detail_screen.dart';

class GoalsScreen extends StatelessWidget {
  const GoalsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final goals = context.watch<GoalsRepository>().goals;
    return Scaffold(
      backgroundColor: AppColors.bg,
      appBar: AppTopBar(
        title: 'Goals',
        action: SquareIconButton(
          icon: Icons.add_rounded,
          semanticLabel: 'New goal',
          onTap: () => Navigator.of(context).push(
            MaterialPageRoute(builder: (_) => const CreateGoalScreen()),
          ),
        ),
      ),
      body: ListView.separated(
        padding: const EdgeInsets.all(16),
        itemCount: goals.length,
        separatorBuilder: (_, _) => const SizedBox(height: 16),
        itemBuilder: (context, i) => GoalCard(
          goal: goals[i],
          onTap: () => Navigator.of(context).push(
            MaterialPageRoute(builder: (_) => GoalDetailScreen(goalId: goals[i].id)),
          ),
        ),
      ),
    );
  }
}
