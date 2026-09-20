# Development rules

## Continuous integration

All builds and tests run through CI. A change is not ready to merge until the applicable CI build and test checks pass.

CI must at minimum run `flutter analyze` and `flutter test` for each pull request and protected-branch push touching `flutter/**`.

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
ci(flutter): run tests on pull requests
```

Use `feat`, `fix`, `docs`, `test`, `refactor`, `perf`, `build`, `ci`, `chore`, or `revert` as appropriate. Use `!` or a `BREAKING CHANGE:` footer for breaking changes.
