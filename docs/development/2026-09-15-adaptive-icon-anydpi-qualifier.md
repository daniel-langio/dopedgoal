# Lint's `ObsoleteSdkInt` fix silently deletes the launcher icon

- Date: 2026-09-15
- Status: resolved
- Area: build configuration, resources
- Related issue/PR: initial Android scaffold

## Symptoms

`:app` sets `lint { warningsAsErrors = true }`. With `minSdk = 26`, lint flags
the conventional adaptive-icon folder as redundant:

```text
app/src/main/res/mipmap-anydpi-v26: Error: This folder configuration (v26) is
unnecessary; minSdkVersion is 26. Merge all the resources in this folder into
mipmap-anydpi. [ObsoleteSdkInt]
```

Applying that fix — renaming the folder to `mipmap-anydpi` — makes the build
fail one stage earlier, at resource linking:

```text
ERROR: AndroidManifest.xml: AAPT: error: resource mipmap/ic_launcher
(aka app.dopedgoal:mipmap/ic_launcher) not found.
```

## Cause

AGP re-versions adaptive-icon resources into a `-v26` qualifier of its own
accord. When the source folder is already `mipmap-anydpi`, that rewrite produces
an **empty** `mipmap-anydpi-v26` directory in
`app/build/intermediates/packaged_res/` — the two icon XML files are dropped
entirely rather than copied. AAPT2 then cannot resolve `@mipmap/ic_launcher`.

Confirmed not to be stale build state: the failure reproduces after
`rm -rf app/build`, and `packaged_res` is regenerated empty each time.

So lint and the resource merger disagree, and lint's advice is the wrong half.

## Resolution

Keep the conventional `res/mipmap-anydpi-v26/` layout and disable the single
check in `app/build.gradle.kts`:

```kotlin
lint {
    disable += "ObsoleteSdkInt"
}
```

Rejected alternative: raising `minSdk` above 26 to make the qualifier
non-redundant. That trades real device coverage for a cosmetic lint result.

Three other lint errors were genuine and were fixed rather than suppressed:
`RedundantLabel` (activity label duplicating the application label), and two
`UnusedResources` (`ic_launcher_round`, now referenced via `android:roundIcon`,
and an empty-state CTA string that will return with the Today screen).

The `GradleDependency`, `NewerVersionAvailable` and `AndroidGradlePluginVersion`
checks were moved to `informational` in the same block. They reach the network,
so under `warningsAsErrors` an upstream release alone would turn an otherwise
unchanged build red. They stay visible in the report without blocking CI.

## Verification

`./gradlew :app:lintDebug` after `rm -rf app/build` — BUILD SUCCESSFUL, no lint
errors.

## Follow-up

`ObsoleteSdkInt` is now off for the whole `:app` module, so a genuinely obsolete
`-v21`-style qualifier elsewhere would go unreported. Narrow it to the mipmap
folder with a `lint.xml` path-scoped `<issue>` entry if resource qualifiers
multiply.
