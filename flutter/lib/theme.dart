import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

/// Halftone Goals palette — matches the paper/ink design mockup exactly.
class AppColors {
  static const bg = Color(0xFFF2EFE8);
  static const mortar = Color(0xFFC8C3BC);
  static const ink = Color(0xFF1A1714);
  static const muted = Color(0xFF7A7068);
  static const accent = Color(0xFF3B2F22);
  static const gold = Color(0xFFC49A2A);
  static const border = Color(0xFFE0DBD4);

  /// Selectable accent colors on the Profile "style anchor" swatch row.
  static const wallStyleSwatches = <Color>[
    Color(0xFFB5583F),
    Color(0xFF8A93A0),
    Color(0xFFC49A2A),
    Color(0xFF8F8577),
    Color(0xFF4F7F86),
    Color(0xFFC1A266),
  ];
}

/// Small, uppercase, letter-spaced monospace text — used for labels, tab
/// captions and section titles throughout the design.
TextStyle appMono({
  double fontSize = 11,
  FontWeight fontWeight = FontWeight.w400,
  Color color = AppColors.muted,
  double letterSpacing = 0.6,
}) {
  return GoogleFonts.jetBrainsMono(
    fontSize: fontSize,
    fontWeight: fontWeight,
    color: color,
    letterSpacing: letterSpacing,
  );
}

/// Serif body/heading text — the mockup's stack is Georgia/Times; Noto Serif
/// is the closest freely-licensed match available via google_fonts.
TextStyle appSerif({
  double fontSize = 15,
  FontWeight fontWeight = FontWeight.w400,
  Color color = AppColors.ink,
  double? height,
}) {
  return GoogleFonts.notoSerif(
    fontSize: fontSize,
    fontWeight: fontWeight,
    color: color,
    height: height,
  );
}

ThemeData buildAppTheme() {
  return ThemeData(
    useMaterial3: true,
    scaffoldBackgroundColor: AppColors.bg,
    colorScheme: ColorScheme.fromSeed(
      seedColor: AppColors.accent,
      brightness: Brightness.light,
      surface: AppColors.bg,
    ),
    textTheme: TextTheme(bodyMedium: appSerif()),
    splashFactory: NoSplash.splashFactory,
    highlightColor: Colors.transparent,
  );
}
