# A commit message describing the skip-ci marker skipped CI

- Date: 2026-09-15
- Status: resolved
- Area: CI
- Related issue/PR: `ci/build-apk-dynamic-versioning`

## Symptoms

Opening the pull request for the APK release workflow produced **no checks at
all**. Not a failing run, not a queued run — nothing:

```text
$ gh pr checks 2
no checks reported on the 'ci/build-apk-dynamic-versioning' branch

$ gh api repos/OWNER/REPO/actions/runs?head_sha=05eece6 --jq .total_count
0
```

The previous pull request on the same repository had run CI normally, so this
looked like an account- or repository-level block.

## Cause

The commit message body contained the literal text `[skip ci]`, inside a
sentence *explaining* that the release commit carries that marker:

```text
The release commit carries [skip ci], without which its own push to main
would re-trigger this workflow and bump forever
```

GitHub matches the skip tokens anywhere in a commit message, not only at the
end, and not only in a subject line. It does not care that the occurrence is
prose. The run is skipped silently, with no run record and no annotation, which
is why this looks like infrastructure failure rather than a message problem.

The recognised tokens are `[skip ci]`, `[ci skip]`, `[no ci]`, `[skip actions]`
and `[actions skip]`.

## Resolution

Amended the commit message to describe the marker without spelling it: "carries
a skip-ci marker". Force-pushed with `--force-with-lease`, and CI scheduled
immediately.

Documentation files may contain the literal token freely — only commit messages
are scanned. The README and the release-automation note both spell it out, which
is fine and intentional.

Ruled out along the way, in case a future no-runs symptom looks similar:

- `gh api repos/OWNER/REPO/actions/permissions` → `enabled: true`,
  `allowed_actions: all`.
- Re-running an earlier successful run started immediately, proving the account
  had neither an Actions-minutes nor a storage block. That is the fastest way to
  separate "my file is wrong" from "the account is blocked".
- Both workflow files parsed identically under a YAML parser, and
  `build-apk.yml` being absent from `gh workflow list` was a red herring:
  a workflow only appears there once it has run or exists on the default branch.

## Verification

After amending, `gh run list --branch ci/build-apk-dynamic-versioning` showed a
CI run within seconds.

## Follow-up

Anyone writing about CI skip behaviour in a commit message will hit this again.
Prefer `skip-ci` unbracketed in prose. A `commit-msg` hook rejecting the
bracketed tokens outside a `chore(release):` subject would make it impossible to
reintroduce — worth adding if it recurs.
