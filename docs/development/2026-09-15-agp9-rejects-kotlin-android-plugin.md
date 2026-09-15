# AGP 9 rejects the `kotlin-android` plugin during scaffolding

- Date: 2026-09-15
- Status: resolved
- Area: build configuration
- Related issue/PR: initial Android scaffold

## Symptoms

The very first `gradle wrapper` invocation failed before the wrapper could even
be generated, because Gradle still has to configure the projects:

```text
An exception occurred applying plugin request [id: 'org.jetbrains.kotlin.android', version: '2.3.20']
> Failed to apply plugin 'org.jetbrains.kotlin.android'.
   > The 'org.jetbrains.kotlin.android' plugin is no longer required for Kotlin
     support since AGP 9.0.
```

Two smaller variants of the same bootstrap problem appeared first: `gradle
wrapper` refuses to run at all without a `settings.gradle.kts`, and then refuses
again while `settings.gradle.kts` declares an `include(":core")` whose directory
does not exist yet.

## Cause

AGP 9 ships built-in Kotlin support and actively fails the build when the
standalone Kotlin Android plugin is also applied. Nearly every Android Compose
template still in circulation applies `org.jetbrains.kotlin.android`, so the
template is wrong against AGP 9.3.1.

## Resolution

`:app` applies only `com.android.application` plus
`org.jetbrains.kotlin.plugin.compose`. The `kotlin { jvmToolchain(21) }` block
was removed from `:app` as well, since the `kotlin` extension it configured is
not the one AGP 9 installs; AGP's built-in Kotlin derives its target from the
Android configuration. `:core` is a plain JVM module and still applies
`org.jetbrains.kotlin.jvm` normally, including its own `jvmToolchain(21)`.

Bootstrap order that works from an empty directory: write `settings.gradle.kts`,
`build.gradle.kts`, `gradle/libs.versions.toml` and every included module's
directory *first*, then run `gradle wrapper`.

## Verification

`./gradlew :core:test :app:assembleDebug` — BUILD SUCCESSFUL, debug APK produced,
core unit tests green.

## Follow-up

Kotlin 2.4.20 and AGP 9.4.0 are already published. The scaffold pins 2.3.20 /
9.3.1 because those versions are known-good here. Bump deliberately, in a change
of its own, so a failure is attributable.
