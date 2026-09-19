import 'package:flutter/foundation.dart';
import '../models/goal.dart';

/// In-memory store for goals. No persistence yet — this is scaffolding for
/// the new "Halftone Goals" design, not a port of the Kotlin domain layer.
class GoalsRepository extends ChangeNotifier {
  GoalsRepository() : _goals = _seedGoals();

  final List<Goal> _goals;

  List<Goal> get goals => List.unmodifiable(_goals);

  /// Every completed task across every goal, newest first — this is what the
  /// History screen shows, so the log always agrees with the goals' walls.
  List<HistoryEntry> get history {
    final entries = <HistoryEntry>[
      for (final goal in _goals)
        for (final task in goal.tasks)
          if (task.isComplete)
            HistoryEntry(
              task: task,
              goalName: goal.name,
              goalEmoji: goal.emoji,
              category: goal.category,
              completedAt: task.completedAt!,
            ),
    ];
    entries.sort((a, b) => b.completedAt.compareTo(a.completedAt));
    return entries;
  }

  int get totalBricksPlaced =>
      _goals.fold(0, (sum, g) => sum + g.placedCount);

  void addGoal(Goal goal) {
    _goals.insert(0, goal);
    notifyListeners();
  }

  static List<Goal> _seedGoals() {
    final now = DateTime.now();
    DateTime ago(Duration d) => now.subtract(d);

    return [
      Goal(
        id: 'goal-stronger',
        name: 'GET STRONGER',
        category: GoalCategory.health,
        emoji: '🏋️',
        cohesion: 0.6,
        wallSeed: 'IRON-7F2Q',
        createdAt: ago(const Duration(days: 20)),
        tasks: [
          Task(id: 't1', name: 'Buy a gym membership', completedAt: ago(const Duration(days: 6))),
          Task(id: 't2', name: 'Learn proper squat form', completedAt: ago(const Duration(days: 5))),
          Task(id: 't3', name: 'Complete first full workout', completedAt: ago(const Duration(days: 4))),
          Task(id: 't4', name: 'Meal prep for the week', completedAt: ago(const Duration(days: 3))),
          Task(id: 't5', name: 'Increase bench press weight', completedAt: ago(const Duration(days: 3, hours: 6))),
          Task(id: 't6', name: 'Track macros for three days straight', completedAt: ago(const Duration(days: 1))),
          Task(id: 't7', name: 'Leg day — 45 min', completedAt: ago(const Duration(hours: 5))),
          Task(id: 't8', name: 'Hit a 5-rep personal record', completedAt: ago(const Duration(hours: 1))),
          Task(id: 't9', name: 'Run a 10K without stopping'),
          Task(id: 't10', name: 'Try a new workout class'),
          Task(id: 't11', name: 'Sleep 8 hours for a full week'),
          Task(id: 't12', name: 'Complete a full month of consistent training'),
        ],
      ),
      Goal(
        id: 'goal-spanish',
        name: 'LEARN SPANISH',
        category: GoalCategory.learning,
        emoji: '📚',
        cohesion: 0.4,
        wallSeed: 'HOLA-3B9K',
        createdAt: ago(const Duration(days: 60)),
        tasks: [
          Task(id: 's1', name: 'Complete the Duolingo basics course', completedAt: ago(const Duration(days: 55))),
          Task(id: 's2', name: 'Learn the present tense conjugations', completedAt: ago(const Duration(days: 50))),
          Task(id: 's3', name: 'Memorize 100 common words', completedAt: ago(const Duration(days: 45))),
          Task(id: 's4', name: 'Watch a Spanish movie with subtitles', completedAt: ago(const Duration(days: 40))),
          Task(id: 's5', name: 'Have a 5-minute conversation exchange', completedAt: ago(const Duration(days: 35))),
          Task(id: 's6', name: 'Learn the past tense conjugations', completedAt: ago(const Duration(days: 30))),
          Task(id: 's7', name: 'Read a short story in Spanish', completedAt: ago(const Duration(days: 26))),
          Task(id: 's8', name: 'Complete unit 5 of the course', completedAt: ago(const Duration(days: 22))),
          Task(id: 's9', name: 'Practice with a native speaker for 30 minutes', completedAt: ago(const Duration(days: 18))),
          Task(id: 's10', name: 'Learn 50 more vocabulary words', completedAt: ago(const Duration(days: 14))),
          Task(id: 's11', name: 'Watch a full Spanish TV episode unsubbed', completedAt: ago(const Duration(days: 10))),
          Task(id: 's12', name: 'Write a journal entry in Spanish', completedAt: ago(const Duration(days: 7))),
          Task(id: 's13', name: 'Pass the intermediate placement test', completedAt: ago(const Duration(days: 4))),
          Task(id: 's14', name: 'Have a 15-minute unassisted conversation', completedAt: ago(const Duration(days: 1))),
          Task(id: 's15', name: 'Finish Duolingo streak — week 6', completedAt: ago(const Duration(hours: 2))),
        ],
      ),
      Goal(
        id: 'goal-clean',
        name: 'DEEP CLEAN THE APARTMENT',
        category: GoalCategory.personal,
        emoji: '🧹',
        cohesion: 0.7,
        wallSeed: 'TIDY-1A4M',
        createdAt: ago(const Duration(days: 10)),
        tasks: [
          Task(id: 'c1', name: 'Deep clean the kitchen', completedAt: ago(const Duration(days: 4))),
          Task(id: 'c2', name: 'Wash all the windows', completedAt: ago(const Duration(days: 2))),
          Task(id: 'c3', name: 'Organize the closet', completedAt: ago(const Duration(days: 1, hours: 3))),
          Task(id: 'c4', name: 'Declutter the bookshelf'),
          Task(id: 'c5', name: 'Steam clean the carpets'),
          Task(id: 'c6', name: 'Clean out the fridge'),
          Task(id: 'c7', name: 'Organize the garage'),
          Task(id: 'c8', name: 'Wipe down all baseboards'),
          Task(id: 'c9', name: 'Deep clean the bathroom'),
        ],
      ),
      Goal(
        id: 'goal-save',
        name: 'SAVE \$2,000',
        category: GoalCategory.career,
        emoji: '💰',
        cohesion: 0.5,
        wallSeed: 'BANK-9Q2X',
        createdAt: ago(const Duration(days: 15)),
        tasks: [
          Task(id: 'f1', name: 'Open a dedicated savings account', completedAt: ago(const Duration(days: 10))),
          Task(id: 'f2', name: 'Set up automatic transfers', completedAt: ago(const Duration(days: 8))),
          Task(id: 'f3', name: 'Sell unused items for extra cash', completedAt: ago(const Duration(days: 6))),
          Task(id: 'f4', name: 'Cut one recurring subscription', completedAt: ago(const Duration(days: 5))),
          Task(id: 'f5', name: 'Cook at home for a full month', completedAt: ago(const Duration(days: 3))),
          Task(id: 'f6', name: 'Transfer \$200 to savings', completedAt: ago(const Duration(days: 2))),
          Task(id: 'f7', name: 'Reach the \$1,000 halfway mark'),
          Task(id: 'f8', name: 'Pick up a side gig for a month'),
          Task(id: 'f9', name: 'Review and cut a second subscription'),
          Task(id: 'f10', name: 'Hit the full \$2,000 goal'),
        ],
      ),
      Goal(
        id: 'goal-portfolio',
        name: 'FINISH DESIGN PORTFOLIO',
        category: GoalCategory.creative,
        emoji: '🎨',
        cohesion: 0.3,
        wallSeed: 'FOLIO-5R8P',
        createdAt: ago(const Duration(days: 40)),
        tasks: [
          Task(id: 'p1', name: 'Pick the five best projects', completedAt: ago(const Duration(days: 38))),
          Task(id: 'p2', name: 'Write a personal bio', completedAt: ago(const Duration(days: 36))),
          Task(id: 'p3', name: 'Design the portfolio homepage', completedAt: ago(const Duration(days: 34))),
          Task(id: 'p4', name: 'Case study: brand identity project', completedAt: ago(const Duration(days: 32))),
          Task(id: 'p5', name: 'Case study: mobile app redesign', completedAt: ago(const Duration(days: 30))),
          Task(id: 'p6', name: 'Case study: website redesign', completedAt: ago(const Duration(days: 28))),
          Task(id: 'p7', name: 'Case study: packaging design', completedAt: ago(const Duration(days: 26))),
          Task(id: 'p8', name: 'Case study: illustration series', completedAt: ago(const Duration(days: 24))),
          Task(id: 'p9', name: 'Get feedback from three designers', completedAt: ago(const Duration(days: 21))),
          Task(id: 'p10', name: 'Revise layout based on feedback', completedAt: ago(const Duration(days: 18))),
          Task(id: 'p11', name: 'Optimize all images', completedAt: ago(const Duration(days: 16))),
          Task(id: 'p12', name: 'Write project descriptions', completedAt: ago(const Duration(days: 14))),
          Task(id: 'p13', name: 'Add a contact page', completedAt: ago(const Duration(days: 12))),
          Task(id: 'p14', name: 'Test on mobile and desktop', completedAt: ago(const Duration(days: 10))),
          Task(id: 'p15', name: 'Buy a custom domain', completedAt: ago(const Duration(days: 8))),
          Task(id: 'p16', name: 'Set up hosting', completedAt: ago(const Duration(days: 6))),
          Task(id: 'p17', name: 'Proofread all copy', completedAt: ago(const Duration(days: 4))),
          Task(id: 'p18', name: 'Add a downloadable resume', completedAt: ago(const Duration(days: 3))),
          Task(id: 'p19', name: 'Do a final design pass', completedAt: ago(const Duration(days: 2))),
          Task(id: 'p20', name: 'Upload final case study', completedAt: ago(const Duration(days: 1, hours: 2))),
        ],
      ),
      Goal(
        id: 'goal-meditate',
        name: 'MEDITATE DAILY',
        category: GoalCategory.health,
        emoji: '🧘',
        cohesion: 0.8,
        wallSeed: 'CALM-2K6Z',
        createdAt: ago(const Duration(days: 8)),
        tasks: [
          Task(id: 'm1', name: '10 min morning session', completedAt: ago(const Duration(days: 6))),
          Task(id: 'm2', name: '10 min morning session', completedAt: ago(const Duration(days: 5))),
          Task(id: 'm3', name: '10 min morning session', completedAt: ago(const Duration(days: 4))),
          Task(id: 'm4', name: '10 min morning session', completedAt: ago(const Duration(days: 3))),
          for (var i = 5; i <= 30; i++) Task(id: 'm$i', name: '10 min morning session'),
        ],
      ),
    ];
  }
}
