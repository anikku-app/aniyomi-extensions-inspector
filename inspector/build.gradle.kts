import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask
import java.io.BufferedReader

plugins {
    application
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.shadow)
}

dependencies {
    // Dependencies of Aniyomi, some are duplicate from root build.gradle.kts
    // keeping it here for reference
    implementation(libs.injekt.core)
    implementation(libs.jsoup)
    implementation(libs.rxjava)
    implementation(libs.bundles.okhttp)

    // AndroidCompat
    implementation(project(":AndroidCompat"))
    implementation(project(":AndroidCompat:Config"))
}

val MainClass = "inspector.MainKt"
application {
    mainClass.set(MainClass)
}

sourceSets {
    main {
        resources {
            srcDir("src/main/resources")
        }
    }
}

// should be bumped with each stable release
val inspectorVersion = "v1.4.5"

// counts commit count on master
val inspectorRevision = runCatching {
    System.getenv("ProductRevision") ?: Runtime
        .getRuntime()
        .exec(arrayOf("git", "rev-list", "HEAD", "--count"))
        .run {
            waitFor()
            val output = inputStream.bufferedReader().use(BufferedReader::readText)
            destroy()
            "r" + output.trim()
        }
}.getOrDefault("r0")

val String.wrapped get() = """"$this""""

buildConfig {
    className("BuildConfig")
    packageName("inspector")

    useKotlinOutput()

    buildConfigField("String", "NAME", rootProject.name.wrapped)
    buildConfigField("String", "VERSION", inspectorVersion.wrapped)
    buildConfigField("String", "REVISION", inspectorRevision.wrapped)
}

tasks {
    shadowJar {
        dependencies {
            // Useless icu-related files
            exclude("com/ibm/icu/impl/data/icudt*/*/*")
        }
        manifest {
            attributes(
                mapOf(
                    "Main-Class" to MainClass,
                    "Implementation-Title" to rootProject.name,
                    "Implementation-Vendor" to "The Tachiyomi Open Source Project",
                    "Specification-Version" to inspectorVersion,
                    "Implementation-Version" to inspectorRevision,
                ),
            )
        }
        archiveBaseName.set(rootProject.name)
        archiveVersion.set(inspectorVersion)
        archiveClassifier.set(inspectorRevision)
    }

    withType<KotlinCompile> {
        compilerOptions {
            optIn = listOf(
                "kotlin.RequiresOptIn",
                "kotlinx.coroutines.ExperimentalCoroutinesApi",
                "kotlinx.coroutines.InternalCoroutinesApi",
                "kotlinx.serialization.ExperimentalSerializationApi",
                "kotlin.io.path.ExperimentalPathApi",
            )
        }
    }

    withType<ShadowJar> {
        destinationDirectory.set(File("$rootDir/inspector/build"))
        dependsOn("formatKotlin", "lintKotlin")
    }

    named("run") {
        dependsOn("formatKotlin", "lintKotlin")
    }

    withType<LintTask> {
        exclude("**/BuildConfig.kt")
        source(files("src/kotlin"))
    }

    withType<FormatTask> {
        exclude("**/BuildConfig.kt")
        source(files("src/kotlin"))
    }

    withType<ProcessResources> {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
}
