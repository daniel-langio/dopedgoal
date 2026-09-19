import 'package:flutter/material.dart';
import '../theme.dart';

class SectionTitle extends StatelessWidget {
  const SectionTitle(this.label, {super.key});
  final String label;

  @override
  Widget build(BuildContext context) {
    return Text(
      label.toUpperCase(),
      style: appMono(fontSize: 11, fontWeight: FontWeight.w700, letterSpacing: 0.9),
    );
  }
}

class SettingsRow extends StatelessWidget {
  const SettingsRow({super.key, required this.label, required this.trailing});
  final String label;
  final Widget trailing;

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Expanded(child: Text(label, style: appSerif(fontSize: 14))),
        const SizedBox(width: 12),
        trailing,
      ],
    );
  }
}

class AppToggle extends StatelessWidget {
  const AppToggle({super.key, required this.value, required this.onChanged, required this.semanticLabel});
  final bool value;
  final ValueChanged<bool> onChanged;
  final String semanticLabel;

  @override
  Widget build(BuildContext context) {
    return Semantics(
      label: semanticLabel,
      toggled: value,
      child: GestureDetector(
        onTap: () => onChanged(!value),
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 120),
          width: 44,
          height: 24,
          padding: const EdgeInsets.all(2),
          decoration: BoxDecoration(
            color: value ? AppColors.accent : Colors.transparent,
            border: Border.all(color: value ? AppColors.accent : AppColors.ink, width: 1.5),
            borderRadius: BorderRadius.circular(99),
          ),
          child: AnimatedAlign(
            duration: const Duration(milliseconds: 120),
            alignment: value ? Alignment.centerRight : Alignment.centerLeft,
            child: Container(
              width: 16,
              height: 16,
              decoration: BoxDecoration(
                color: value ? Colors.white : AppColors.ink,
                shape: BoxShape.circle,
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class TwoWaySegmented extends StatelessWidget {
  const TwoWaySegmented({
    super.key,
    required this.left,
    required this.right,
    required this.leftSelected,
    required this.onChanged,
  });

  final String left;
  final String right;
  final bool leftSelected;
  final ValueChanged<bool> onChanged;

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        border: Border.all(color: AppColors.ink, width: 1.5),
        borderRadius: BorderRadius.circular(99),
      ),
      clipBehavior: Clip.antiAlias,
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          _segment(left, leftSelected, () => onChanged(true)),
          _segment(right, !leftSelected, () => onChanged(false)),
        ],
      ),
    );
  }

  Widget _segment(String text, bool selected, VoidCallback onTap) {
    return InkWell(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 7),
        color: selected ? AppColors.accent : Colors.transparent,
        child: Text(
          text.toUpperCase(),
          style: appMono(
            fontSize: 10,
            fontWeight: FontWeight.w600,
            color: selected ? Colors.white : AppColors.ink,
          ),
        ),
      ),
    );
  }
}

class LabeledSlider extends StatelessWidget {
  const LabeledSlider({
    super.key,
    required this.label,
    required this.value,
    required this.onChanged,
    this.leftHint = 'Wild',
    this.rightHint = 'Tame',
  });

  final String label;
  final double value;
  final ValueChanged<double> onChanged;
  final String leftHint;
  final String rightHint;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(label, style: appSerif(fontSize: 14)),
            Text(value.toStringAsFixed(1), style: appMono(fontSize: 12)),
          ],
        ),
        const SizedBox(height: 6),
        SliderTheme(
          data: SliderTheme.of(context).copyWith(
            trackHeight: 4,
            activeTrackColor: AppColors.accent,
            inactiveTrackColor: AppColors.border,
            thumbColor: AppColors.ink,
            overlayColor: AppColors.accent.withValues(alpha: 0.12),
            thumbShape: const RoundSliderThumbShape(enabledThumbRadius: 8),
          ),
          child: Slider(value: value, onChanged: onChanged, min: 0, max: 1),
        ),
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 2),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(leftHint.toUpperCase(), style: appMono(fontSize: 9)),
              Text(rightHint.toUpperCase(), style: appMono(fontSize: 9)),
            ],
          ),
        ),
      ],
    );
  }
}

class SwatchRow extends StatelessWidget {
  const SwatchRow({
    super.key,
    required this.colors,
    required this.selectedIndex,
    required this.onChanged,
  });

  final List<Color> colors;
  final int selectedIndex;
  final ValueChanged<int> onChanged;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        for (var i = 0; i < colors.length; i++)
          Padding(
            padding: const EdgeInsets.only(right: 8),
            child: Semantics(
              button: true,
              label: 'Style anchor ${i + 1}',
              selected: i == selectedIndex,
              child: InkWell(
                onTap: () => onChanged(i),
                customBorder: const CircleBorder(),
                child: Container(
                  width: 40,
                  height: 40,
                  alignment: Alignment.center,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    border: Border.all(
                      color: i == selectedIndex ? AppColors.ink : Colors.transparent,
                      width: 1.5,
                    ),
                  ),
                  child: Container(
                    width: 24,
                    height: 24,
                    decoration: BoxDecoration(
                      color: colors[i],
                      shape: BoxShape.circle,
                      border: Border.all(color: Colors.black.withValues(alpha: 0.15)),
                    ),
                  ),
                ),
              ),
            ),
          ),
      ],
    );
  }
}
