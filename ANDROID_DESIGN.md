# Doped Goal — Android design direction

## Product feeling

**A small, calm workshop for finishing things.** Every task is a brick. Completing it physically and visibly builds a wall that belongs to one meaningful goal. The experience should feel tactile and rewarding, never urgent, competitive, or punitive.

The visual language is *handmade masonry + quiet field notes*: warm paper surfaces, dark ink, soft mortar, imperfect material texture, and one confident action per screen. It should not look like a loud arcade game or a conventional productivity dashboard.

## Design principles

1. **Show the reward before asking for effort.** A goal’s wall is always the largest visual object.
2. **One clear next action.** The home screen promotes only the next unplaced brick.
3. **Progress is additive.** No streaks, decay, red warning states, or “behind” language.
4. **Make commitments concrete.** A goal is created with a finite set of tasks; the preview turns that commitment into an object.
5. **Use variation as delight, not information.** Brick material, patina, artifacts, and banner styling are minted details. Completion state must remain clear without decoding decoration.

## Visual foundation

| Token | Value | Use |
| --- | --- | --- |
| Canvas | `#F4F0E9` | primary background |
| Paper | `#FFFCF7` | cards, sheets, dialogs |
| Ink | `#25211D` | primary text and controls |
| Quiet ink | `#756D65` | labels and secondary copy |
| Mortar light | `#D4D0CB` | wall canvas and ghost slots |
| Moss | `#506344` | positive/supporting status |
| Terracotta | `#A84C35` | warm accent, selected state |
| Gold | `#C49A2A` | rare material and celebration only |
| Danger | `#B33C34` | destructive action only |

Use Material 3 components as the accessibility and interaction base, styled with these tokens. Use a humanist sans for interface text (Roboto Flex / system sans) and a compact monospace face for labels, counters, and material metadata. Do **not** use a serif body font on Android: it becomes too dense at small sizes.

Spacing is an 8dp system. Standard horizontal gutter is 20dp; compact rows use 16dp. Main cards have 16dp corners. Brick corners remain 3–5dp so they feel materially different from UI containers.

## Information architecture

```text
Today / Home
├── Goal detail (wall + task list)
│   ├── Complete task → placement moment
│   ├── Brick detail
│   └── Goal complete celebration
├── Create goal
│   ├── Goal basics
│   └── Task builder + live wall preview
├── Archive
│   └── Completed goal detail
└── Settings
    ├── Appearance
    └── Motion & accessibility
```

The bottom navigation has three destinations: **Today**, **Goals**, and **Archive**. Settings is an overflow action from Today. Avoid a separate “rewards” tab—the accumulated work *is* the reward.

### Product decisions confirmed

- Users may keep and work on multiple active goals simultaneously.
- Tasks do not have a required completion sequence. A user may place any unfinished task from a goal at any time.
- A wall fills in the order tasks are completed: the next completed task occupies the next open brick position, regardless of its order in the task list. The task list itself stays in the order the user created it.
- The first release is local-first. Goals, task state, and minted visual seeds are stored on-device and work without an account or connection. Sync is a later, opt-in feature rather than a prerequisite for use.

## Core screens

### 1. Today — default launch screen

Purpose: turn “what should I do?” into one tap.

```text
  9:41                 [settings]
  Good afternoon, Maya
  A little work still counts.

  ┌─────────────────────────────────┐
  │  📚  Read chapter 3              │
  │      Learn Kotlin                │
  │                                  │
  │  [  Place this brick  ]          │
  └─────────────────────────────────┘

  IN PROGRESS                              2
  ┌─────────────────────────────────┐
  │  [banner]  Learn Kotlin   4 / 12 │
  │  ████░░░░░░░░  miniature wall    │
  └─────────────────────────────────┘
  ┌─────────────────────────────────┐
  │  [banner]  Run a 5K       1 / 8  │
  │  ██░░░░░░░░░░  miniature wall    │
  └─────────────────────────────────┘

                 [ + New goal ]
     Today             Goals          Archive
```

If there are no tasks, use a warm empty state: a single illustrated ghost brick and “Your first brick is waiting.” CTA: **Build a goal**. Never show a blank checklist.

The “next brick” card uses the actual minted brick as a 56dp leading visual. It is a suggested task, not a required next step: users can select any unfinished task from any active goal. Tapping the card opens the goal; tapping **Place this brick** completes it immediately after a brief confirmation only when the user has enabled it in settings. Default behavior: complete on tap and offer a 5-second **Undo** snackbar.

### 2. Goal detail — the wall is the interface

Purpose: make progress tangible and tasks easy to finish.

```text
  [←]  Learn Kotlin                       [•••]
       learning · 4 of 12 bricks

  ┌─────────────────────────────────┐
  │          ~~~ 📚 ~~~               │  banner
  │  ┌───┐ ┌───┐ ┌───┐               │
  │  └───┘ └───┘ └───┘  placed bricks │
  │    ┈ ┈ ┈ ┈ ┈ ┈ ┈ ┈  ghost bricks  │
  └─────────────────────────────────┘

  NEXT BRICK
  ○ Read chapter 3                 [Place]

  ALL BRICKS
  ✓ Install Android Studio              1
  ✓ Kotlin basics                       2
  ✓ Build a hello-world app             3
  ✓ Variables and types                 4
  ○ Read chapter 3                      5
  ○ Make a small quiz                   6
```

The banner plus wall occupies roughly 38% of a phone viewport on first view, then scrolls naturally with the task list. It is not permanently sticky on this screen; permanent stickiness leaves too little room for actual tasks.

The suggested task is a distinct 64dp-high card, but it does not lock the rest of the list. All other tasks use 52dp rows with a clear completion icon and ordinal number. Tapping a placed task opens its brick detail. Tapping any incomplete task opens a bottom sheet with its name and a primary **Place brick** action. This reduces accidental completion while keeping the action immediate.

Use a running-bond wall, bottom-up fill order, and dashed ghost brick outlines. The next open wall slot is assigned at completion time, so its visual build order reflects the user’s actual completion order rather than the order tasks were created. Maintain at least 3:1 non-text contrast between ghosts and mortar. Do not place task text directly over patterned bricks.

### 3. Complete task — placement moment

Purpose: make completion satisfying without trapping the user in a celebration.

When placing a brick, dim surrounding UI very slightly and focus the wall. Animate only the newly completed brick:

1. Drop + squash: 0–750ms
2. Paint arrives: 750–1450ms
3. Artifact appears: 1450–2250ms
4. Cracks/chip/emblem resolve: 2250–3200ms

At any point, a visible **Skip** control ends the sequence and lands on the final state. The task list updates underneath the wall after the brick lands. Give a concise snackbar: “Brick placed · 5 of 12”.

For reduced motion, show the completed brick cross-fade in over 150ms, with no shake, splatter, or idle animation. Haptics are optional and must respect system settings: one light tick on landing, one success haptic when the wall completes.

### 4. Goal achieved

Purpose: recognize completion and preserve the artifact.

Show the completed banner and wall at full width, then:

> **Wall complete**
>
> You placed all 12 bricks for Learn Kotlin.

Actions: **View wall**, **Start another goal**, and secondary **Archive for now**. No confetti cannon; a restrained gold glint may pass once over the completed wall. Completed goals remain available in Archive and retain their exact minted bricks.

### 5. Create goal — guided builder

Purpose: make a finite, achievable wall with no intimidating form.

This is a two-stage flow, not one long dense page.

**Step 1: Give it a banner**

- Title: “What are you building?”
- Goal-name field; autofocus.
- Category chip group: Health, Learning, Career, Creative, Social, Personal.
- Emoji picker as a horizontal row plus “More emoji”.
- Live banner preview.
- Primary button: **Choose the bricks**.

**Step 2: Choose the bricks**

- Header shows the live ghost wall; it remains pinned below the top app bar while the task list is being edited.
- Supportive label: “Small, finishable steps work best.”
- Each task has an ordinal, one-line text field, and delete icon with a 48dp touch target.
- **Add a brick** appends a task, immediately adds an animated ghost slot, and focuses its field.
- Cohesion is disclosed in an “Wall character” section, not shown as technical lambda: `wild  ◯────●────  tame`; default 60%.
- Footer CTA: **Create goal · 6 bricks**. It is disabled until name and at least one nonblank task exist.

Warn only when a user attempts to create an unusually large wall (over 30 bricks): “This is a big wall. Want to split it into two goals?” Keep **Create anyway** available. This supports ADHD users without taking control away.

### 6. Goals and Archive

**Goals** is a vertically scrolling set of wall cards, grouped by category only when there are more than five active goals. Each card has a banner thumbnail, goal name, `placed / total`, and a small accurate wall strip. Long-press exposes archive/delete options; destructive actions require confirmation and must state that deleting removes the saved wall.

**Archive** is a calm gallery of completed walls, newest first. A completed wall card shows completion date and brick count. There is no score, rank, or comparative metric.

## Brick and banner rendering guidance

The renderer is custom Compose Canvas, while normal controls remain Material 3. A brick must be deterministic from its minted task seed—never recomputed based on screen size, current theme, or completion date.

- Render the material base first, then paint, degradation, artifact, emblem, and border.
- Preserve material visibility: common-material paint coverage ≤72%; precious-material coverage ≤18%.
- Roll precious material before applying cohesion. It is a delightful visual surprise, not a reward contingent on perfect behavior.
- Render ornaments at low visual weight; the emoji emblem and task state should survive at 48dp.
- At list-thumbnail size, simplify texture and artifacts to protect performance. Detail is for the goal wall and brick detail.
- Use the same banner seed on every surface for a goal. The banner is identity, not a random decoration per screen.

## Interaction and accessibility requirements

- All targets are at least 48 × 48dp; task text fields have visible focus states.
- Support Android dynamic type through 200%. At larger sizes, wall canvases may shrink in height but task labels must never truncate silently; wrap to two lines in detail views.
- Do not communicate placed/pending status only by color, texture, or animation. Pair it with `✓` / `○`, text, and content descriptions such as “Brick 5 of 12, incomplete: Read chapter 3.”
- Respect system dark theme. In dark mode use `#1C1916` canvas, `#28231F` surfaces, `#EEE8E0` ink, and `#2B2724` mortar. Keep brick art physically rich rather than simply inverting SVG colors.
- Respect reduced motion; default decorative idle animation is off on low-power devices.
- Make placement, undo, and goal-completion announcements available through TalkBack live regions.
- Never use overdue, failure, streak, or loss framing in copy.

## Recommended first-build scope

Build the Today screen, goal detail, two-step goal creation, static deterministic wall rendering, completion/undo, and archive first. Ship the full placement sequence, advanced artifact animation, theme variations, and brick-detail zoom after the core loop is stable.

The provided `create-goal.html`, `wall-animated.html`, and SVG catalogs are visual references for the renderer, not a UI kit to embed directly in Android. Translate their masonry detail into Canvas drawing primitives and keep Android’s navigation, input behavior, typography, and accessibility native.
