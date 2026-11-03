import org.gradle.api.tasks.testing.Test

plugins {
    alias(mihonx.plugins.android.library)
    alias(mihonx.plugins.spotless)

    alias(libs.plugins.metro)
}

android {
    namespace = "tachiyomi.source.local"
}

kotlin {
    compilerOptions {
        optIn.add("kotlinx.serialization.ExperimentalSerializationApi")
    }
}

val performanceBenchmarkOutput = providers.environmentVariable("KOMIKITA_BENCHMARK_OUTPUT").orElse("")
tasks.withType<Test>().configureEach {
    val measuringPerformance = performanceBenchmarkOutput.get().isNotEmpty()
    inputs.property("performanceBenchmarkOutput", performanceBenchmarkOutput)
    outputs.upToDateWhen { !measuringPerformance }
    outputs.doNotCacheIf("Performance measurements must execute on the current host") {
        measuringPerformance
    }
}

dependencies {
    implementation(projects.sourceApi)
    implementation(projects.i18n)

    implementation(projects.core.archive)
    implementation(projects.core.common)
    implementation(projects.coreMetadata)
    implementation(projects.domain)

    implementation(libs.metro.runtime)

    implementation(libs.unifile)
    implementation(libs.bundles.serialization)

    implementation(libs.injekt)
    implementation(libs.jsoup)

    implementation(libs.kotlinx.datetime)

    testImplementation(libs.bundles.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testRuntimeOnly(libs.junit.platform.launcher)
}
