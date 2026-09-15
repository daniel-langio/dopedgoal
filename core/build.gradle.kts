plugins {
    alias(libs.plugins.kotlin.jvm)
    // Lets `:app`'s checkDependencies lint pass actually analyze the domain module.
    alias(libs.plugins.android.lint)
}

lint {
    warningsAsErrors = true
    abortOnError = true
}

kotlin {
    jvmToolchain(21)
    sourceSets["main"].kotlin.srcDir("src/main/kotlin")
    sourceSets["test"].kotlin.srcDir("src/test/kotlin")
}

dependencies {
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit)
}

tasks.test {
    useJUnit()
}
