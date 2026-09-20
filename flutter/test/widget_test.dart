import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'package:dopedgoal_flutter/main.dart';
import 'package:dopedgoal_flutter/widgets/goal_card.dart';

void main() {
  setUp(() {
    SharedPreferences.setMockInitialValues({});
  });

  testWidgets('App starts empty — no goals, tab bar still present', (tester) async {
    await tester.pumpWidget(const DopedGoalApp());
    await tester.pumpAndSettle();

    expect(find.text('GOALS'), findsOneWidget);
    expect(find.text('History'), findsOneWidget);
    expect(find.text('Profile'), findsOneWidget);
    expect(find.byType(GoalCard), findsNothing);
  });

  testWidgets('History tab starts empty', (tester) async {
    await tester.pumpWidget(const DopedGoalApp());
    await tester.pumpAndSettle();

    await tester.tap(find.text('History'));
    await tester.pumpAndSettle();

    expect(find.text('HISTORY'), findsOneWidget);
  });

  testWidgets('Creating a goal adds it to the list and completing a task updates progress and History', (tester) async {
    await tester.pumpWidget(const DopedGoalApp());
    await tester.pumpAndSettle();

    await tester.tap(find.byIcon(Icons.add_rounded));
    await tester.pumpAndSettle();

    final createButton = find.textContaining('Create goal');
    await tester.dragUntilVisible(createButton, find.byType(ListView), const Offset(0, -600));
    await tester.pumpAndSettle();
    await tester.tap(createButton);
    await tester.pumpAndSettle();

    expect(find.text('Run a 5K'), findsOneWidget);
    expect(find.text('0/4 TASKS'), findsOneWidget);

    await tester.tap(find.text('Run a 5K'));
    await tester.pumpAndSettle();

    final taskFinder = find.text('Run 1 mile without stopping');
    expect(taskFinder, findsOneWidget);
    await tester.tap(taskFinder);
    await tester.pumpAndSettle();

    final textWidget = tester.widget<Text>(taskFinder);
    expect(textWidget.style?.decoration, TextDecoration.lineThrough);

    await tester.tap(find.byIcon(Icons.arrow_back_rounded));
    await tester.pumpAndSettle();

    expect(find.text('1/4 TASKS'), findsOneWidget);

    await tester.tap(find.text('History'));
    await tester.pumpAndSettle();

    expect(find.text('Run 1 mile without stopping'), findsOneWidget);
  });
}
