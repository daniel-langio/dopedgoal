import 'dart:math' as math;
import 'package:flutter/material.dart';
import '../theme.dart';

/// A simplified stand-in for the mockup's generative halftone brick wall.
///
/// The real design renders each brick as a unique procedural halftone/noise
/// texture (the same idea as this repo's Kotlin brick minting). Reproducing
/// that generator in Dart was explicitly out of scope for this pass, so this
/// paints a flat, coursed brick grid in the goal's category color instead —
/// same wall geometry (bottom-up, one brick per task), simpler fill.
class WallPreview extends StatelessWidget {
  const WallPreview({
    super.key,
    required this.total,
    required this.placed,
    required this.color,
    this.minBrickWidth = 56,
  });

  final int total;
  final int placed;
  final Color color;
  final double minBrickWidth;

  static const _brickAspect = 120 / 52;
  static const _gap = 3.0;

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(
      builder: (context, constraints) {
        final width = constraints.maxWidth;
        final cols = math.max(
          1,
          ((width + _gap) / (minBrickWidth + _gap)).floor(),
        );
        final brickWidth = (width - _gap * (cols - 1)) / cols;
        final brickHeight = brickWidth / _brickAspect;
        final rows = (total / cols).ceil().clamp(1, 1000);
        final height = rows * brickHeight + (rows - 1) * _gap;
        return SizedBox(
          width: width,
          height: height,
          child: CustomPaint(
            painter: _WallPainter(
              total: total,
              placed: placed,
              cols: cols,
              rows: rows,
              color: color,
              brickWidth: brickWidth,
              brickHeight: brickHeight,
              gap: _gap,
            ),
          ),
        );
      },
    );
  }
}

class _WallPainter extends CustomPainter {
  _WallPainter({
    required this.total,
    required this.placed,
    required this.cols,
    required this.rows,
    required this.color,
    required this.brickWidth,
    required this.brickHeight,
    required this.gap,
  });

  final int total;
  final int placed;
  final int cols;
  final int rows;
  final Color color;
  final double brickWidth;
  final double brickHeight;
  final double gap;

  @override
  void paint(Canvas canvas, Size size) {
    final fill = Paint()..style = PaintingStyle.fill;
    final dashed = Paint()
      ..style = PaintingStyle.stroke
      ..strokeWidth = 1.4
      ..color = AppColors.muted.withValues(alpha: 0.55);
    final mortar = Paint()
      ..style = PaintingStyle.stroke
      ..strokeWidth = 1
      ..color = Colors.black.withValues(alpha: 0.18);

    final base = HSLColor.fromColor(color);
    final rng = math.Random(total * 31 + cols);

    for (var p = 0; p < total; p++) {
      final row = rows - 1 - (p ~/ cols);
      final col = p % cols;
      final rect = Rect.fromLTWH(
        col * (brickWidth + gap),
        row * (brickHeight + gap),
        brickWidth,
        brickHeight,
      );
      final rrect = RRect.fromRectAndRadius(rect, const Radius.circular(3));

      if (p < placed) {
        final jitter = (rng.nextDouble() - 0.5) * 0.12;
        fill.color = base
            .withLightness((base.lightness + jitter).clamp(0.15, 0.85))
            .toColor();
        canvas.drawRRect(rrect, fill);
        canvas.drawRRect(rrect, mortar);
      } else {
        _drawDashedRRect(canvas, rrect, dashed);
      }
    }
  }

  void _drawDashedRRect(Canvas canvas, RRect rrect, Paint paint) {
    const dashLength = 4.0;
    const gapLength = 3.0;
    final path = Path()..addRRect(rrect);
    for (final metric in path.computeMetrics()) {
      var distance = 0.0;
      while (distance < metric.length) {
        final next = math.min(distance + dashLength, metric.length);
        canvas.drawPath(metric.extractPath(distance, next), paint);
        distance = next + gapLength;
      }
    }
  }

  @override
  bool shouldRepaint(covariant _WallPainter oldDelegate) {
    return oldDelegate.total != total ||
        oldDelegate.placed != placed ||
        oldDelegate.cols != cols ||
        oldDelegate.color != color;
  }
}
