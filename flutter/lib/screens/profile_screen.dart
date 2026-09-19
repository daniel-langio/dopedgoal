import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../data/goals_repository.dart';
import '../theme.dart';
import '../widgets/app_shell.dart';
import '../widgets/settings_controls.dart';

class ProfileScreen extends StatefulWidget {
  const ProfileScreen({super.key});

  @override
  State<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> {
  bool _wallAbove = true;
  bool _animations = true;
  bool _reducedMotion = false;
  double _cohesion = 0.6;
  int _swatchIndex = 0;
  bool _dailyReminder = true;
  bool _goalCompletions = true;

  @override
  Widget build(BuildContext context) {
    final repo = context.watch<GoalsRepository>();
    return Scaffold(
      backgroundColor: AppColors.bg,
      appBar: const AppTopBar(title: 'Profile'),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Row(
            children: [
              Container(
                width: 56,
                height: 56,
                decoration: BoxDecoration(
                  color: AppColors.mortar,
                  shape: BoxShape.circle,
                  border: Border.all(color: AppColors.border, width: 1.5),
                ),
                alignment: Alignment.center,
                child: const Text('🎨', style: TextStyle(fontSize: 26)),
              ),
              const SizedBox(width: 14),
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('SAM OKAFOR', style: appSerif(fontSize: 18, fontWeight: FontWeight.w700)),
                  const SizedBox(height: 4),
                  Text(
                    '${repo.goals.length} GOALS · ${repo.totalBricksPlaced} BRICKS PLACED',
                    style: appMono(fontSize: 11),
                  ),
                ],
              ),
            ],
          ),
          _Section(
            title: 'Display',
            children: [
              SettingsRow(
                label: 'Wall display mode',
                trailing: TwoWaySegmented(
                  left: 'Above',
                  right: 'Inside',
                  leftSelected: _wallAbove,
                  onChanged: (v) => setState(() => _wallAbove = v),
                ),
              ),
              SettingsRow(
                label: 'Animations',
                trailing: AppToggle(
                  value: _animations,
                  semanticLabel: 'Animations',
                  onChanged: (v) => setState(() => _animations = v),
                ),
              ),
              SettingsRow(
                label: 'Reduced motion',
                trailing: AppToggle(
                  value: _reducedMotion,
                  semanticLabel: 'Reduced motion',
                  onChanged: (v) => setState(() => _reducedMotion = v),
                ),
              ),
            ],
          ),
          _Section(
            title: 'Wall style',
            children: [
              LabeledSlider(
                label: 'Default cohesion',
                value: _cohesion,
                onChanged: (v) => setState(() => _cohesion = v),
              ),
              const SizedBox(height: 4),
              Text('Style anchor color', style: appSerif(fontSize: 14)),
              const SizedBox(height: 9),
              SwatchRow(
                colors: AppColors.wallStyleSwatches,
                selectedIndex: _swatchIndex,
                onChanged: (i) => setState(() => _swatchIndex = i),
              ),
            ],
          ),
          _Section(
            title: 'Notifications',
            children: [
              SettingsRow(
                label: 'Daily reminder',
                trailing: AppToggle(
                  value: _dailyReminder,
                  semanticLabel: 'Daily reminder',
                  onChanged: (v) => setState(() => _dailyReminder = v),
                ),
              ),
              SettingsRow(
                label: 'Goal completions',
                trailing: AppToggle(
                  value: _goalCompletions,
                  semanticLabel: 'Goal completions',
                  onChanged: (v) => setState(() => _goalCompletions = v),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

class _Section extends StatelessWidget {
  const _Section({required this.title, required this.children});
  final String title;
  final List<Widget> children;

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(top: 18),
      padding: const EdgeInsets.only(top: 18),
      decoration: const BoxDecoration(
        border: Border(top: BorderSide(color: AppColors.border, width: 1.5)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SectionTitle(title),
          const SizedBox(height: 16),
          for (var i = 0; i < children.length; i++) ...[
            if (i > 0) const SizedBox(height: 16),
            children[i],
          ],
        ],
      ),
    );
  }
}
