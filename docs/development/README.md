# Development notes

This folder records bugs and development struggles encountered while building Doped Goal. It is an engineering memory: future contributors should be able to understand what went wrong, why it happened, and how the solution was verified.

Create one Markdown file for each issue, named:

```text
YYYY-MM-DD-short-kebab-case-title.md
```

Use this template:

```md
# Short issue title

- Date: YYYY-MM-DD
- Status: investigating | resolved | accepted-risk
- Area: e.g. wall renderer, local persistence, CI
- Related issue/PR: optional link or identifier

## Symptoms

What a developer or user observed. Include error messages or screenshots only when safe to store.

## Cause

Confirmed root cause, or the best current hypothesis if still investigating.

## Resolution

The fix, workaround, or decision made. Note important alternatives that were rejected.

## Verification

Exact build, test, or manual check used to validate the result.

## Follow-up

Remaining risk, technical debt, or a clearly stated “None”.
```

## Notes

- [2026-09-15 — AGP 9 rejects the `kotlin-android` plugin](2026-09-15-agp9-rejects-kotlin-android-plugin.md) — resolved
- [2026-09-15 — Lint's `ObsoleteSdkInt` fix silently deletes the launcher icon](2026-09-15-adaptive-icon-anydpi-qualifier.md) — resolved
- [2026-09-15 — Porting `mint()` cannot reuse Python's RNG stream](2026-09-15-porting-python-rng-determinism.md) — investigating
- [2026-09-15 — Release automation writes to main](2026-09-15-release-automation-hazards.md) — accepted-risk
- [2026-09-15 — A commit message describing the skip-ci marker skipped CI](2026-09-15-skip-ci-in-commit-message-body.md) — resolved
