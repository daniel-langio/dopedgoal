# Release automation writes to main, and that has consequences

- Date: 2026-09-15
- Status: accepted-risk
- Area: CI, release
- Related issue/PR: `ci/build-apk-dynamic-versioning`

## Symptoms

Not a failure here yet. These are known hazards of the auto-versioning release
workflow, carried over from the same pattern running in another project, and
recorded up front so they are recognisable rather than surprising.

1. **Local pushes to `main` get rejected as behind.** The workflow commits a
   version bump as `github-actions[bot]`, so `main` moves without any human
   pushing. The rejection looks like a conflict but is not one.
2. **A failure late in the workflow means no release at all.** Tagging and
   publishing are the final step, after the APK is built and uploaded. In the
   reference project an artifact upload failed on a storage quota, and no
   release was cut even though analysis, tests and the build had all passed.
3. **Omitting `[skip ci]` loops forever.** The release commit pushes to `main`,
   which is the workflow's own trigger.

## Cause

Persisting the version bump requires writing to the branch that triggered the
workflow. That is inherent to deriving `versionName` from a file in the
repository rather than from tags alone.

## Resolution

Accepted, with mitigations rather than a redesign:

- The release commit carries `[skip ci]`, which is load-bearing.
- The release step runs `git pull --rebase` before pushing, so a human push that
  lands mid-build does not fail the release. Rebasing is safe here because the
  bot's commit touches `version.properties` and nothing else.
- Ordering is deliberate: the bump is persisted *after* a tested, buildable APK
  exists, so a broken commit is never tagged. Hazard 2 is the cost of that
  guarantee and is worth paying — a missing release is recoverable by re-running
  the workflow, a tagged broken release is not.
- `concurrency` uses `cancel-in-progress: false`. Cancelling a run mid-release
  could leave a tag without a release, or a bump without a tag.

Rejected: deriving `versionName` from the latest git tag instead of a committed
file. It removes the write to `main` entirely and would eliminate hazards 1 and
3 — worth revisiting if the bot commits become genuinely annoying — but it makes
the version invisible in a local checkout and unavailable to a local build.

## Verification

The bump logic was exercised offline against fix / ci / feat / `!:` /
`BREAKING CHANGE:` footer / non-conventional subjects, and produced
patch / patch / minor / major / major / patch respectively. A mid-prose mention
of `BREAKING CHANGE:` correctly stays a patch, because the pattern is anchored
per line.

Command injection was tested directly: commit messages containing a backtick
command substitution, `$(touch /tmp/PWNED)` and unbalanced quotes all bumped
normally without executing. The commit message reaches bash through the
`COMMIT_MESSAGE` environment variable rather than `${{ }}` interpolation, which
is what makes it inert — a GitHub expression is pasted into the script as raw
text *before* bash parses it, so an interpolated message with a backtick in it
would execute. This bit the reference project for real: a code span in a commit
message broke a build.

The release APK itself was built locally with `-PappVersionName=1.2.3
-PappVersionCode=42` and verified with `aapt2 dump badging`
(`versionCode='42' versionName='1.2.3'`) and `apksigner verify`
(`CN=Android Debug`, confirming the no-secrets fallback path).

## Follow-up

No release keystore is configured, so published APKs are currently debug-signed.
They install, but they cannot be upgraded by a later properly-signed build —
users would have to uninstall first. Configure the signing secrets before
sharing an APK with anyone outside the team. Setup is documented in the README.
