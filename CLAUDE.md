# CodeMetrics With Kotlin

IntelliJ IDEA plugin providing real-time cyclomatic complexity metrics as inlay hints for Java and Kotlin files.

**Version**: 0.2.0
**License**: MIT (Dual copyright: Tamas Kisst + ehs208)
**Based on**: [Original CodeMetrics](https://github.com/kisstkondoros/codemetrics-idea) by Tamas Kisst

---

## Quick Start

### Prerequisites
- Java 17+
- IntelliJ IDEA 2024.3+
- Gradle 9.0 (via wrapper)

### Commands
```bash
./gradlew build          # Build plugin
./gradlew runIde         # Run in dev IDE
./gradlew verifyPlugin   # Verify compatibility
./gradlew buildPlugin    # Create distribution ZIP
```

---

## Documentation

### Development
- [Architecture](.claude/architecture.md) - Core components, Kotlin support, plugin structure
- [Development Gotchas](.claude/development-gotchas.md) - PSI threading, APIs, common issues
- [Contributing Guide](CONTRIBUTING.md) - Full contributor workflow

### Maintainers
- [Release Workflow](.claude/release-workflow.md) - Release process, CI/CD, repository config

---

## Critical Rules

### For Contributors
- ❌ **Never change** `pluginVersion` in `gradle.properties`
- ❌ **Never create** version sections in `CHANGELOG.md`
- ✅ **Only add to** `[Unreleased]` section in `CHANGELOG.md`
- ✅ All PRs go to `main` branch

### For Maintainers
- **Current Maintainer**: ehs208
- Atomic commits by feature (avoid monolithic commits)
- Use conventional commit prefixes: `feat:`, `fix:`, `docs:`, `chore:`, `ci:`
- Tag format: `v*` (e.g., `v0.2.0`) triggers auto-publish
- Version updates: Update both `gradle.properties` and `CHANGELOG.md`
- Release process: See [Release Workflow](.claude/release-workflow.md)

---

## Plugin Distribution

- **Marketplace**: https://plugins.jetbrains.com/plugin/28221-codemetrics-with-kotlin
- **Platforms**: IntelliJ IDEA 2024.3+ (Community & Ultimate)
- **Languages**: Java, Kotlin (K1 & K2)
