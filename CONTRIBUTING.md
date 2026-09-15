# Development rules

## Continuous integration

All builds and tests run through CI. A change is not ready to merge until the applicable CI build and test checks pass.

When the Android project is scaffolded, CI must at minimum run the Gradle build, unit tests, lint, and any available UI/instrumentation tests for each pull request and protected-branch push.

## Commit and pull-request titles

Use [Conventional Commits](https://www.conventionalcommits.org/) for every commit and pull-request title.

Format:

```text
type(optional-scope): short imperative summary
```

Examples:

```text
feat(goal): add local goal creation
fix(wall): preserve completion placement order
docs(development): document Canvas clipping issue
ci(android): run connected tests on pull requests
```

Use `feat`, `fix`, `docs`, `test`, `refactor`, `perf`, `build`, `ci`, `chore`, or `revert` as appropriate. Use `!` or a `BREAKING CHANGE:` footer for breaking changes.

## Development notes

Document every bug, unexpected implementation difficulty, and meaningful development struggle in [`docs/development`](docs/development/README.md). Add the note while resolving the issue—not as an optional retrospective.

Each note must identify the date, status, affected area, symptoms, root cause or current hypothesis, resolution/workaround, verification, and follow-up work. Do not put private credentials, personally identifiable information, or production data in a note.
