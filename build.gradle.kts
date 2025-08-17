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

// ✅ 태그에서 버전 추출 (없으면 gradle.properties 값 사용)
val pluginVersion: String = System.getenv("GITHUB_REF_NAME")
    ?.takeIf { it.startsWith("v") }
    ?.removePrefix("v")
    ?.also { println("🔖 Version set from tag: $it") }
    ?: properties("pluginVersion")
        .also { println("📦 Version set from gradle.properties: $it") }

// ✅ Gradle project.version (optional, 빌드 캐시/출력 등에 필요)
version = pluginVersion

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
        version = pluginVersion   // ✅ 반드시 문자열로 지정
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

    publishPlugin {
        dependsOn("patchChangelog")
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
