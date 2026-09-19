import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'data/goals_repository.dart';
import 'theme.dart';
import 'widgets/app_shell.dart';

void main() {
  runApp(const DopedGoalApp());
}

class DopedGoalApp extends StatelessWidget {
  const DopedGoalApp({super.key});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (_) => GoalsRepository(),
      child: MaterialApp(
        title: 'Doped Goal',
        debugShowCheckedModeBanner: false,
        theme: buildAppTheme(),
        home: const AppShell(),
      ),
    );
  }
}
