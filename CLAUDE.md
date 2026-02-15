# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**CodeMetrics With Kotlin** is an IntelliJ IDEA plugin that provides real-time cyclomatic complexity metrics as inlay hints in Java and Kotlin files. The plugin calculates complexity by parsing the AST and walking through each node, displaying complexity scores inline with customizable thresholds and color coding.

**Current Version**: 0.1.9

Based on the original [CodeMetrics](https://github.com/kisstkondoros/codemetrics-idea) by Tamas Kisst (MIT License), this version has been extended to support Kotlin and updated for modern IntelliJ Platform compatibility.

## Quick Start

### Prerequisites
- **Java 17** or later (required)
- **IntelliJ IDEA 2024.2.1+**
- Gradle 9.0 (included via wrapper)

### Development Commands

```bash
# Build the plugin
./gradlew build

# Run plugin in development IDE instance
./gradlew runIde

# Verify plugin compatibility
./gradlew verifyPlugin

# Build plugin distribution ZIP
./gradlew buildPlugin
# Output: build/distributions/CodeMetrics-With-Kotlin-*.zip
```

## Architecture

### Core Components

- **MetricsModel** (`core/MetricsModel.java`): Central data model representing complexity metrics with hierarchical structure. Uses memoized computation for performance and supports different collector types (MAX for classes, SUM for methods).

- **MetricsParser** (`core/parser/MetricsParser.java`): Entry point that delegates to TreeWalker for AST parsing and complexity calculation.

- **TreeWalker** (`core/parser/TreeWalker.java`): Core AST traversal engine that uses HandlerRegistry to process different PSI element types.

- **HandlerRegistry** (`core/parser/HandlerRegistry.java`): Maps PSI element types to complexity handlers. Contains special logic for handling both Java and Kotlin constructs, including Elvis operator detection.

- **InlayManager** (`core/inlay/InlayManager.java`): Manages the lifecycle of inlay hints in the editor. Implemented as a project service that creates, updates, and disposes complexity hints based on configuration thresholds.

### Kotlin Support Architecture

The plugin supports both Java and Kotlin through dual handler registration:

- **Java Elements**: Registered using `JavaElementType` constants (e.g., `BINARY_EXPRESSION`, `IF_STATEMENT`)
- **Kotlin Elements**: Registered using `KtNodeTypes` and `KtTokens` (e.g., `KtNodeTypes.IF`, `KtTokens.ELVIS`)
- **Special Handling**: Kotlin's Elvis operator (`?:`) is detected within `KtNodeTypes.BINARY_EXPRESSION` and routed to `kotlinElvisExpression` configuration

### Tool Window Feature (v0.1.5+)

- **ComplexityToolWindowFactory** (`toolwindow/ComplexityToolWindowFactory.java`): Creates "Code Complexity" tool window
- **ComplexityAnalysisService** (`toolwindow/ComplexityAnalysisService.java`): Performs project-wide complexity analysis
- **ComplexityAnalysisPanel** (`toolwindow/ComplexityAnalysisPanel.java`): UI for displaying complexity metrics across entire project
- **Access**: View → Tool Windows → Code Complexity

### Plugin Integration

- **Plugin Definition**: `src/main/resources/META-INF/plugin.xml` defines services and extensions
- **Services**:
  - Application: `MetricsConfiguration` for global settings
  - Project: `InlayManager` for per-project inlay management
  - Startup: `EditorListener` for file monitoring initialization
- **Configuration UI**: `configuration/EditorConfig.java` provides tabbed settings interface
- **K2 Support**: Explicitly declared via `<supportsKotlinPluginMode supportsK2="true"/>`

## Key Files

- `build.gradle.kts`: Build configuration using IntelliJ Platform Gradle Plugin 2.9.0
- `gradle.properties`: Plugin metadata, version, and platform compatibility
- `plugin.xml`: Plugin definition, services, and extensions
- `CHANGELOG.md`: Version history following [keepachangelog.com](https://keepachangelog.com) format

## Contributing & Release Workflow

### For Contributors

1. **Fork & Create Branch**:
   ```bash
   git checkout -b feature/my-feature
   ```

2. **Make Changes**:
   - Write code
   - Add entry to `CHANGELOG.md` under `[Unreleased]` section
   - Do NOT modify version numbers or create new version sections

3. **Test Locally**:
   ```bash
   ./gradlew runIde
   ```

4. **Submit PR**:
   - Create PR to `main` branch
   - CI will automatically validate

### For Maintainers (Release Process)

1. **Merge PRs**: Review and merge contributor PRs to `main`

2. **Test Accumulated Changes**:
   ```bash
   git checkout main
   git pull
   ./gradlew runIde  # Test the plugin locally
   ```

3. **Prepare Release**:
   ```bash
   # Update CHANGELOG.md: Move [Unreleased] → [X.Y.Z]
   vim CHANGELOG.md

   # Update version in gradle.properties
   vim gradle.properties  # pluginVersion = 0.2.0

   # Commit
   git add CHANGELOG.md gradle.properties
   git commit -m "chore: release v0.2.0"
   git push origin main
   ```

4. **Create Tag** (triggers auto-publish):
   ```bash
   git tag v0.2.0
   git push origin v0.2.0
   ```

5. **Automatic Deployment**:
   - GitHub Actions publishes to JetBrains Marketplace
   - Creates GitHub Release with changelog

## CI/CD Workflows

### CI (`.github/workflows/ci.yml`)
- **Triggers**: PRs and pushes to `main`
- **Validates**:
  - CHANGELOG.md format (contributors should only modify `[Unreleased]`)
  - Build succeeds
  - Plugin verification passes

### Publish (`.github/workflows/publish.yml`)
- **Triggers**: Git tags matching `v*` pattern
- **Actions**:
  - Verifies version in `gradle.properties` matches tag
  - Builds and verifies plugin
  - Publishes to JetBrains Marketplace (requires `PUBLISH_TOKEN` secret)
  - Creates GitHub Release

## Configuration

- **User Settings**: Settings → Code Metrics
- **Thresholds**: Configurable complexity thresholds (low/normal/high/extreme)
- **Elements**: Toggle metrics for methods, classes, lambda expressions
- **Weights**: Customize complexity calculation for different constructs
- **Kotlin-specific**: Elvis operator, when expressions, etc.

## Important Gotchas

### PSI Access Threading
- **Always** use `ReadAction` when accessing PSI in background tasks
- IntelliJ will throw exceptions if PSI is accessed from non-EDT threads without ReadAction

### Deprecated API Migration
The codebase uses modern IntelliJ Platform APIs:
- ✅ `project.getService()` instead of `ComponentManager.getComponent()`
- ✅ `ApplicationManager.getApplication().getService()` instead of `ServiceManager.getService()`
- ✅ Project services and startup activities instead of `ProjectComponent`
- ✅ Modern document listener registration via `addDocumentListener()`

### AST Processing Priority
IntelliJ PSI processes node types before token types. For Kotlin's Elvis operator:
1. `KtNodeTypes.BINARY_EXPRESSION` is matched first
2. Within the handler, `KtTokens.ELVIS` is detected to route to Kotlin-specific configuration
3. This ensures `status = value ?: default` uses `kotlinElvisExpression` settings

### K2 Mode Support
- Plugin explicitly declares K2 compiler support
- Both K1 and K2 modes are supported via `<supportsKotlinPluginMode supportsK2="true"/>`

### Version Management
- Version is defined in `gradle.properties` (`pluginVersion`)
- Version can be overridden by git tags (format: `vX.Y.Z`)
- Build script automatically extracts version from `GITHUB_REF_NAME` if available

## Plugin Distribution

- **Marketplace**: https://plugins.jetbrains.com/plugin/28221-codemetrics-with-kotlin
- **Supported Platforms**: IntelliJ IDEA 2024.2.1+ (Community & Ultimate)
- **Supported Languages**: Java, Kotlin (K1 & K2)
- **License**: MIT
