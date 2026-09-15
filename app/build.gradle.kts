import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// versionName lives in version.properties at the repo root so CI can bump it
// without rewriting Kotlin DSL. A -PappVersionName overrides it for one build.
val appVersionName: String = providers.gradleProperty("appVersionName").orNull
    ?: providers.fileContents(rootProject.layout.projectDirectory.file("version.properties"))
        .asText
        .map { text ->
            Properties().apply { load(text.reader()) }.getProperty("versionName")
                ?: error("version.properties is missing versionName")
        }
        .get()

// versionCode is the repository's commit count, passed in by CI. Android refuses
// to install an APK whose versionCode is not strictly greater than the installed
// one, so this has to climb on every build; commit count does that for free.
// Local builds get 1, which never needs to be monotonic.
val appVersionCode: Int = (providers.gradleProperty("appVersionCode").orNull ?: "1").toInt()

// Release signing. CI decodes a keystore from repository secrets when they are
// configured. Without them the release build falls back to the debug key, so the
// APK is still installable for testing — just not distributable.
val releaseKeystoreFile = providers.environmentVariable("DOPEDGOAL_KEYSTORE_FILE")
val releaseKeystorePassword = providers.environmentVariable("DOPEDGOAL_KEYSTORE_PASSWORD")
val releaseKeyAlias = providers.environmentVariable("DOPEDGOAL_KEY_ALIAS")
val releaseKeyPassword = providers.environmentVariable("DOPEDGOAL_KEY_PASSWORD")
// An env var set to the empty string is still "present" to Gradle, and CI sets
// these unconditionally, so test for blank rather than for presence.
val hasReleaseKeystore = listOf(
    releaseKeystoreFile, releaseKeystorePassword, releaseKeyAlias, releaseKeyPassword,
).all { !it.orNull.isNullOrBlank() }

android {
    namespace = "app.dopedgoal"
    compileSdk = 37

    defaultConfig {
        applicationId = "app.dopedgoal"
        minSdk = 26
        targetSdk = 37
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasReleaseKeystore) {
            create("release") {
                storeFile = file(releaseKeystoreFile.get())
                storePassword = releaseKeystorePassword.get()
                keyAlias = releaseKeyAlias.get()
                keyPassword = releaseKeyPassword.get()
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
    }

    lint {
        warningsAsErrors = true
        abortOnError = true
        checkDependencies = true
        // Dependency-currency checks reach the network, so an upstream release
        // alone would fail an otherwise unchanged build. Report, never block.
        informational += listOf(
            "AndroidGradlePluginVersion",
            "GradleDependency",
            "NewerVersionAvailable",
        )
        // The -v26 qualifier reads as redundant at minSdk 26, but dropping it
        // makes the resource merger discard the adaptive icons outright.
        // See docs/development/2026-09-15-adaptive-icon-anydpi-qualifier.md.
        disable += "ObsoleteSdkInt"
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation(project(":core"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
