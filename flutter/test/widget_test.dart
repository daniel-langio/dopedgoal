import 'package:flutter_test/flutter_test.dart';

import 'package:dopedgoal_flutter/main.dart';

void main() {
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
}
