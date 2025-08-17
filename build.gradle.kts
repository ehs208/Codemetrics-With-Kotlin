import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.0.20"
    id("org.jetbrains.intellij.platform") version "2.7.2"
    id("org.jetbrains.changelog") version "2.2.0"
}

group = properties("pluginGroup")

// 기본값 (SNAPSHOT) → GitHub Actions에서 태그 기반으로 override
version = properties("pluginVersion")

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity(properties("platformVersion"))
        bundledPlugins(
            properties("platformPlugins")
                .split(',')
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        )
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
        version = version.toString()
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
                    (getOrNull(version.toString()) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }

        ideaVersion {
            sinceBuild = "242"
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

    register("setVersionFromTag") {
        doLast {
            val tag = System.getenv("GITHUB_REF_NAME")
            if (tag != null && tag.startsWith("v")) {
                project.version = tag.removePrefix("v")
                println("🔖 Version set from tag: $version")
            }
        }
    }

    publishPlugin {
        dependsOn("patchChangelog")
        dependsOn("setVersionFromTag")
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
