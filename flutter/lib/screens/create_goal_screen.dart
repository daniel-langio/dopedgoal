import 'dart:math';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../data/goals_repository.dart';
import '../models/goal.dart';
import '../theme.dart';
import '../widgets/app_shell.dart';
import '../widgets/settings_controls.dart';
import '../widgets/wall_preview.dart';

const _recommendedEmoji = ['🎯', '🔥', '💪', '📚', '🏃', '🎸', '🌱', '🧠'];
const _moreEmoji = [
  '🎨', '💰', '🧹', '🧘', '🍎', '💧', '😴', '📖', '🎵', '🚴', '🏋️', '🌍',
  '📷', '🛠️', '🎮', '🥴', '⚽', '🏆', '☕', '🧵', '🧪', '🐶', '🌙', '⭐',
];

class CreateGoalScreen extends StatefulWidget {
  const CreateGoalScreen({super.key});

  @override
  State<CreateGoalScreen> createState() => _CreateGoalScreenState();
}

class _CreateGoalScreenState extends State<CreateGoalScreen> {
  final _nameController = TextEditingController(text: 'Run a 5K');
  late final List<TextEditingController> _taskControllers = [
    TextEditingController(text: 'Run 1 mile without stopping'),
    TextEditingController(text: 'Buy proper running shoes'),
    TextEditingController(text: 'Follow a 5K training plan'),
    TextEditingController(text: 'Sign up for a local 5K race'),
  ];

  GoalCategory _category = GoalCategory.health;
  String _selectedEmoji = '🏃';
  bool _showPicker = false;
  double _cohesion = 0.6;
  String _seed = '5K-RUN-7F2Q';

  @override
  void dispose() {
    _nameController.dispose();
    for (final c in _taskControllers) {
      c.dispose();
    }
    super.dispose();
  }

  void _addTask() {
    setState(() => _taskControllers.add(TextEditingController()));
  }

  void _removeTask(int i) {
    setState(() => _taskControllers.removeAt(i).dispose());
  }

  void _reroll() {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
    final rng = Random();
    setState(() {
      _seed = List.generate(8, (_) => chars[rng.nextInt(chars.length)]).join();
    });
  }

  void _save() {
    final tasks = _taskControllers
        .map((c) => c.text.trim())
        .where((t) => t.isNotEmpty)
        .toList();
    if (tasks.isEmpty || _nameController.text.trim().isEmpty) return;

    final goal = Goal(
      id: 'goal-${DateTime.now().microsecondsSinceEpoch}',
      name: _nameController.text.trim(),
      category: _category,
      emoji: _selectedEmoji,
      cohesion: _cohesion,
      wallSeed: _seed,
      createdAt: DateTime.now(),
      tasks: [
        for (var i = 0; i < tasks.length; i++)
          Task(id: 'new-$i-${DateTime.now().microsecondsSinceEpoch}', name: tasks[i]),
      ],
    );
    context.read<GoalsRepository>().addGoal(goal);
    Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    final taskCount = _taskControllers.length;
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
                  Text(
                    'NEW GOAL',
                    style: appMono(fontSize: 15, fontWeight: FontWeight.w700, color: AppColors.ink),
                  ),
                ],
              ),
            ),
          ),
          Container(
            width: double.infinity,
            color: AppColors.mortar,
            padding: const EdgeInsets.fromLTRB(16, 14, 16, 12),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text('$taskCount TASKS'.toUpperCase(), style: appMono(fontSize: 10.5, color: const Color(0xFF4D453D))),
                    Text('$taskCount BRICKS ON THIS WALL'.toUpperCase(), style: appMono(fontSize: 10.5, color: const Color(0xFF4D453D))),
                  ],
                ),
                const SizedBox(height: 9),
                WallPreview(total: taskCount == 0 ? 1 : taskCount, placed: 0, color: _category.dotColor),
              ],
            ),
          ),
          Expanded(
            child: ListView(
              padding: const EdgeInsets.fromLTRB(20, 22, 20, 110),
              children: [
                Text('GOAL', style: appMono(fontSize: 11, fontWeight: FontWeight.w700, letterSpacing: 0.9)),
                const SizedBox(height: 16),
                _Field(
                  label: 'Name',
                  child: TextField(
                    controller: _nameController,
                    onChanged: (_) => setState(() {}),
                    style: appSerif(fontSize: 15),
                    decoration: _inputDecoration('What do you want to achieve?'),
                  ),
                ),
                const SizedBox(height: 16),
                _Field(
                  label: 'Category',
                  child: Wrap(
                    spacing: 7,
                    runSpacing: 7,
                    children: [
                      for (final c in GoalCategory.values)
                        _Chip(
                          label: c.label,
                          selected: c == _category,
                          onTap: () => setState(() => _category = c),
                        ),
                    ],
                  ),
                ),
                const SizedBox(height: 16),
                _Field(
                  label: 'Emoji',
                  hint: 'Recommended · tap + for more',
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Wrap(
                        spacing: 8,
                        runSpacing: 8,
                        children: [
                          for (final e in _recommendedEmoji)
                            _EmojiButton(
                              emoji: e,
                              selected: e == _selectedEmoji,
                              onTap: () => setState(() {
                                _selectedEmoji = e;
                                _showPicker = false;
                              }),
                            ),
                          _EmojiButton.add(
                            expanded: _showPicker,
                            onTap: () => setState(() => _showPicker = !_showPicker),
                          ),
                        ],
                      ),
                      if (_showPicker) ...[
                        const SizedBox(height: 10),
                        Container(
                          padding: const EdgeInsets.all(12),
                          decoration: BoxDecoration(
                            color: Colors.white,
                            border: Border.all(color: AppColors.border, width: 1.5),
                          ),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Row(
                                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                children: [
                                  Text('ALL EMOJI', style: appMono(fontSize: 10.5)),
                                  InkWell(
                                    onTap: () => setState(() => _showPicker = false),
                                    child: const Icon(Icons.close_rounded, size: 16, color: AppColors.muted),
                                  ),
                                ],
                              ),
                              const SizedBox(height: 10),
                              Wrap(
                                spacing: 8,
                                runSpacing: 8,
                                children: [
                                  for (final e in _moreEmoji)
                                    _EmojiButton(
                                      emoji: e,
                                      selected: e == _selectedEmoji,
                                      onTap: () => setState(() {
                                        _selectedEmoji = e;
                                        _showPicker = false;
                                      }),
                                    ),
                                ],
                              ),
                            ],
                          ),
                        ),
                      ],
                    ],
                  ),
                ),
                const SizedBox(height: 16),
                LabeledSlider(
                  label: 'Cohesion',
                  value: _cohesion,
                  onChanged: (v) => setState(() => _cohesion = v),
                ),
                const SizedBox(height: 16),
                _Field(
                  label: 'Wall seed',
                  hint: 'Sets material & pattern',
                  child: Row(
                    children: [
                      Expanded(
                        child: Container(
                          padding: const EdgeInsets.symmetric(horizontal: 13, vertical: 11),
                          decoration: BoxDecoration(
                            color: Colors.white,
                            border: Border.all(color: AppColors.border, width: 1.5),
                          ),
                          child: Text(_seed, style: appMono(fontSize: 13, color: AppColors.ink)),
                        ),
                      ),
                      const SizedBox(width: 10),
                      SquareIconButton(icon: Icons.casino_outlined, semanticLabel: 'Generate a new seed', onTap: _reroll),
                    ],
                  ),
                ),
                const SizedBox(height: 26),
                Text('TASKS', style: appMono(fontSize: 11, fontWeight: FontWeight.w700, letterSpacing: 0.9)),
                const SizedBox(height: 16),
                for (var i = 0; i < _taskControllers.length; i++) ...[
                  if (i > 0) const SizedBox(height: 10),
                  _TaskRow(
                    index: i,
                    controller: _taskControllers[i],
                    onChanged: () => setState(() {}),
                    onDelete: () => _removeTask(i),
                  ),
                ],
                const SizedBox(height: 10),
                _DashedButton(label: '+ Add a task', onTap: _addTask),
                const SizedBox(height: 26),
                SizedBox(
                  width: double.infinity,
                  child: ElevatedButton(
                    onPressed: _save,
                    style: ElevatedButton.styleFrom(
                      backgroundColor: AppColors.accent,
                      foregroundColor: Colors.white,
                      padding: const EdgeInsets.symmetric(vertical: 15),
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                      elevation: 0,
                    ),
                    child: Text(
                      'Create goal — $taskCount bricks',
                      style: appMono(fontSize: 12.5, fontWeight: FontWeight.w700, color: Colors.white),
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

InputDecoration _inputDecoration(String hint) {
  return InputDecoration(
    hintText: hint,
    hintStyle: appSerif(fontSize: 15, color: AppColors.muted),
    filled: true,
    fillColor: Colors.white,
    contentPadding: const EdgeInsets.symmetric(horizontal: 13, vertical: 10),
    border: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: const BorderSide(color: AppColors.border, width: 1.5)),
    enabledBorder: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: const BorderSide(color: AppColors.border, width: 1.5)),
    focusedBorder: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: const BorderSide(color: AppColors.accent, width: 1.5)),
  );
}

class _Field extends StatelessWidget {
  const _Field({required this.label, required this.child, this.hint});
  final String label;
  final Widget child;
  final String? hint;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(label.toUpperCase(), style: appMono(fontSize: 11)),
            if (hint != null) Text(hint!.toUpperCase(), style: appMono(fontSize: 10)),
          ],
        ),
        const SizedBox(height: 7),
        child,
      ],
    );
  }
}

class _Chip extends StatelessWidget {
  const _Chip({required this.label, required this.selected, required this.onTap});
  final String label;
  final bool selected;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(99),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 13, vertical: 6),
        decoration: BoxDecoration(
          color: selected ? AppColors.accent : Colors.white,
          border: Border.all(color: selected ? AppColors.accent : AppColors.border, width: 1.5),
          borderRadius: BorderRadius.circular(99),
        ),
        child: Text(
          label,
          style: appMono(fontSize: 11, color: selected ? Colors.white : AppColors.muted),
        ),
      ),
    );
  }
}

class _EmojiButton extends StatelessWidget {
  const _EmojiButton({required this.emoji, required this.selected, required this.onTap})
      : isAdd = false,
        expanded = false;

  const _EmojiButton.add({required this.expanded, required this.onTap})
      : emoji = '+',
        selected = false,
        isAdd = true;

  final String emoji;
  final bool selected;
  final bool isAdd;
  final bool expanded;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return Semantics(
      button: true,
      label: isAdd ? 'More emoji' : 'Choose $emoji',
      selected: selected,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(10),
        child: Container(
          width: 38,
          height: 38,
          alignment: Alignment.center,
          decoration: BoxDecoration(
            color: selected ? const Color(0xFFF5F1EA) : Colors.white,
            border: Border.all(
              color: selected ? AppColors.accent : (isAdd ? const Color(0xFFB8B2AA) : AppColors.border),
              width: selected ? 2 : 1.5,
            ),
            borderRadius: BorderRadius.circular(10),
          ),
          child: isAdd
              ? Text('+', style: appMono(fontSize: 18, fontWeight: FontWeight.w700, color: AppColors.muted))
              : Text(emoji, style: const TextStyle(fontSize: 17)),
        ),
      ),
    );
  }
}

class _TaskRow extends StatelessWidget {
  const _TaskRow({
    required this.index,
    required this.controller,
    required this.onChanged,
    required this.onDelete,
  });

  final int index;
  final TextEditingController controller;
  final VoidCallback onChanged;
  final VoidCallback onDelete;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
      decoration: BoxDecoration(color: Colors.white, border: Border.all(color: AppColors.border, width: 1.5)),
      child: Row(
        children: [
          Text((index + 1).toString().padLeft(2, '0'), style: appMono(fontSize: 11)),
          const SizedBox(width: 10),
          Expanded(
            child: TextField(
              controller: controller,
              onChanged: (_) => onChanged(),
              style: appSerif(fontSize: 14),
              decoration: InputDecoration(
                border: InputBorder.none,
                isDense: true,
                hintText: 'Task ${index + 1}',
                hintStyle: appSerif(fontSize: 14, color: AppColors.muted),
              ),
            ),
          ),
          InkWell(
            onTap: onDelete,
            child: const Padding(
              padding: EdgeInsets.all(6),
              child: Icon(Icons.close_rounded, size: 16, color: AppColors.muted),
            ),
          ),
        ],
      ),
    );
  }
}

class _DashedButton extends StatelessWidget {
  const _DashedButton({required this.label, required this.onTap});
  final String label;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(12),
      child: DottedBorder(
        child: Container(
          width: double.infinity,
          padding: const EdgeInsets.symmetric(vertical: 12),
          alignment: Alignment.center,
          child: Text(label.toUpperCase(), style: appMono(fontSize: 11.5, letterSpacing: 0.7)),
        ),
      ),
    );
  }
}

/// Minimal dashed-border box — avoids pulling in a package for one button.
class DottedBorder extends StatelessWidget {
  const DottedBorder({super.key, required this.child});
  final Widget child;

  @override
  Widget build(BuildContext context) {
    return CustomPaint(
      painter: _DashedRectPainter(),
      child: child,
    );
  }
}

class _DashedRectPainter extends CustomPainter {
  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = const Color(0xFFB8B2AA)
      ..strokeWidth = 1.5
      ..style = PaintingStyle.stroke;
    final rrect = RRect.fromRectAndRadius(Offset.zero & size, const Radius.circular(12));
    final path = Path()..addRRect(rrect);
    const dashLength = 5.0, gapLength = 4.0;
    for (final metric in path.computeMetrics()) {
      var distance = 0.0;
      while (distance < metric.length) {
        final end = min(distance + dashLength, metric.length);
        canvas.drawPath(metric.extractPath(distance, end), paint);
        distance = end + gapLength;
      }
    }
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}
