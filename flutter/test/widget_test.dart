import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'package:dopedgoal_flutter/main.dart';

void main() {
  setUp(() {
    SharedPreferences.setMockInitialValues({});
  });

  testWidgets('Goals tab shows seeded goals and tab bar', (tester) async {
    await tester.pumpWidget(const DopedGoalApp());
    await tester.pumpAndSettle();

    expect(find.text('GOALS'), findsOneWidget);
    expect(find.text('GET STRONGER'), findsOneWidget);
    expect(find.text('History'), findsOneWidget);
    expect(find.text('Profile'), findsOneWidget);
  });

  testWidgets('Switching to History tab shows the completed-task log', (tester) async {
    await tester.pumpWidget(const DopedGoalApp());
    await tester.pumpAndSettle();

    await tester.tap(find.text('History'));
    await tester.pumpAndSettle();

    expect(find.text('HISTORY'), findsOneWidget);
    expect(find.text('Finish Duolingo streak — week 6'), findsOneWidget);
  });

  testWidgets('Completing a task in Goal Detail updates progress and History', (tester) async {
    await tester.pumpWidget(const DopedGoalApp());
    await tester.pumpAndSettle();

    expect(find.text('8/12 TASKS'), findsOneWidget);

    await tester.tap(find.text('GET STRONGER'));
    await tester.pumpAndSettle();

    final taskFinder = find.text('Run a 10K without stopping');
    await tester.dragUntilVisible(taskFinder, find.byType(ListView), const Offset(0, -150));
    await tester.pumpAndSettle();
    expect(taskFinder, findsOneWidget);
    await tester.tap(taskFinder);
    await tester.pumpAndSettle();

    // Struck-through now that it's complete.
    final textWidget = tester.widget<Text>(taskFinder);
    expect(textWidget.style?.decoration, TextDecoration.lineThrough);

    await tester.tap(find.byIcon(Icons.arrow_back_rounded));
    await tester.pumpAndSettle();

    expect(find.text('9/12 TASKS'), findsOneWidget);

    await tester.tap(find.text('History'));
    await tester.pumpAndSettle();

    expect(find.text('Run a 10K without stopping'), findsOneWidget);
  });
}
