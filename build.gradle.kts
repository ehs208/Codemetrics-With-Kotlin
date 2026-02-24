import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.0.20"
    id("org.jetbrains.intellij.platform") version "2.11.0"
    id("org.jetbrains.changelog") version "2.2.0"
}

group = properties("pluginGroup")

val pluginVersion: String = System.getenv("GITHUB_REF_NAME")
    ?.takeIf { it.startsWith("v") }
    ?.removePrefix("v")
    ?.also { println("🔖 Version set from tag: $it") }
    ?: properties("pluginVersion")
        .also { println("📦 Version set from gradle.properties: $it") }

version = pluginVersion

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        create(properties("platformType"), properties("platformVersion"))
        bundledPlugins(
            properties("platformPlugins")
                .split(',')
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        )
    }
}

intellijPlatform {
    pluginVerification {
        ides {
            create(properties("platformType"), properties("platformVersion"))
            create(IntelliJPlatformType.IntellijIdea, "261.21525.39")
        }
    }
}

kotlin {
    jvmToolchain(17)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

intellijPlatform {
    pluginConfiguration {
        name = properties("pluginName")
        version = pluginVersion
        description = file("README.md").readText().lines().run {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"
            if (!containsAll(listOf(start, end))) {
                throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
            }
            subList(indexOf(start) + 1, indexOf(end))
        }.joinToString("\n").let { markdownToHTML(it) }

        changeNotes = provider {
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }

        ideaVersion {
            sinceBuild = "243"
            untilBuild = provider { null }
        }
    }
}

changelog {
    groups.set(emptyList())
    repositoryUrl.set(properties("pluginRepositoryUrl"))
}

tasks {
    wrapper {
        gradleVersion = properties("gradleVersion")
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
