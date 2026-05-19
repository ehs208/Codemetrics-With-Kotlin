# CodeMetrics With Kotlin

IntelliJ IDEA plugin providing real-time cyclomatic complexity metrics as inlay hints for Java and Kotlin files.

**Version source**: `pluginVersion` in `gradle.properties`; release history in `CHANGELOG.md`
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

Tracked documentation:
- [README](README.md) - User-facing feature, setup, and usage overview
- [Contributing Guide](CONTRIBUTING.md) - Contributor workflow, architecture map, development tips, and release process
- [Changelog](CHANGELOG.md) - Release history

Local `.claude/` notes are intentionally ignored by git and are not canonical repository documentation.

### Architecture Map
- `src/main/java/com/github/ehs208/codemetrics/core/MetricsModel.java` - Hierarchical complexity metrics model
- `src/main/java/com/github/ehs208/codemetrics/core/parser/MetricsParser.java` - Complexity parsing entry point
- `src/main/java/com/github/ehs208/codemetrics/core/parser/TreeWalker.java` - AST traversal and metrics collection
- `src/main/java/com/github/ehs208/codemetrics/core/parser/HandlerRegistry.java` - Java/Kotlin PSI element handler registration
- `src/main/java/com/github/ehs208/codemetrics/inlay/InlayManager.java` - Editor inlay lifecycle management
- `src/main/java/com/github/ehs208/codemetrics/toolwindow/ComplexityToolWindowFactory.java` - CodeMetrics tool window with Analysis and History tabs
- `src/main/java/com/github/ehs208/codemetrics/configuration/EditorConfig.java` - Settings UI, including AI Refactoring settings

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
- Tag format: `v*` (for example, `vX.Y.Z`) triggers auto-publish
- Version updates: Update both `gradle.properties` and `CHANGELOG.md`
- Release process: See [CONTRIBUTING.md](CONTRIBUTING.md#maintainer-release-process)

---

## Plugin Distribution

- **Marketplace**: https://plugins.jetbrains.com/plugin/28221-codemetrics-with-kotlin
- **Platforms**: IntelliJ IDEA 2024.3+ (Community & Ultimate)
- **Languages**: Java, Kotlin (K1 & K2)
