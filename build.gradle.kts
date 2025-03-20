import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.buildconfig) apply false
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlinter) apply false
    alias(libs.plugins.shadow) apply false
}

allprojects {
    group = "aniyomi"
    version = "1.0"
}

val projects = listOf(
    project(":AndroidCompat"),
    project(":AndroidCompat:Config"),
    project(":inspector"),
)

configure(projects) {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    val javaVersion = JavaVersion.VERSION_17

    java {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    tasks.withType<KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(javaVersion.toString()))
        }
    }

    dependencies {
        val libs = rootProject.libs
        val androidcompat = rootProject.androidcompat

        // Kotlin
        implementation(kotlin("stdlib"))
        implementation(kotlin("reflect"))

        // :AndroidCompat:Config dependency
        implementation(androidcompat.bundles.config)

        // :AndroidCompat dependency to get application content root
        implementation(androidcompat.appdirs)

        // APK parser
        implementation(libs.apkparser)

        // Coroutines
        implementation(libs.bundles.coroutines)

        // Dependency Injection
        implementation(libs.kodein.di)
        implementation(libs.injekt.core)

        // Dex2Jar
        implementation(libs.bundles.dex2jar)

        // JSoup
        implementation(libs.jsoup)

        // Logging
        implementation(libs.bundles.logging)

        // OkHttp3
        implementation(libs.bundles.okhttp)

        // ReactiveX
        implementation(libs.rxjava)

        // Serialization
        implementation(libs.bundles.serialization)
    }
}
