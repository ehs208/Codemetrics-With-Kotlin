# CodeMetrics With Kotlin

[![Version](https://img.shields.io/jetbrains/plugin/v/28221-codemetrics-with-kotlin.svg)](https://plugins.jetbrains.com/plugin/28221-codemetrics-with-kotlin)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/28221-codemetrics-with-kotlin.svg)](https://plugins.jetbrains.com/plugin/28221-codemetrics-with-kotlin)

<!-- Plugin description -->
Provides inlay indicators based on a customizable complexity calculation for **Java** and **Kotlin** files.

This plugin is based on the original [CodeMetrics](https://github.com/kisstkondoros/codemetrics-idea) by Tamas Kisst (MIT License) and has been extended to support **Kotlin** with updated UI compatibility for modern IntelliJ versions.

The plugin calculates cyclomatic complexity by parsing the AST and walking through each node, displaying scores inline with customizable thresholds and color coding.

## Features
- Real-time cyclomatic complexity calculation for **Java and Kotlin**
- Inline complexity hints in the editor with color-coded severity
- **Project-wide complexity analysis** tool window for code overview
- Customizable thresholds and colors with automatic validation
- Click on hints to see detailed breakdowns
- **Full Kotlin K2 compiler mode support**
- Supports classes, methods, control flow statements, properties, and lambda expressions
- **Granular Kotlin visibility controls** - toggle metrics for individual constructs (if, when, for, while, try, properties)
- **Specialized handling for Kotlin constructs** (Elvis operator, when expressions, etc.)
- Robust error handling with user-friendly validation messages

## Installation
- **Using IDE built-in plugin system**:
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "CodeMetrics With Kotlin"</kbd> > <kbd>Install</kbd>

## Configuration
<kbd>Settings/Preferences</kbd> > <kbd>Code Metrics</kbd> to customize:
- Complexity thresholds for different colors (low/normal/high/extreme)
- Which elements to measure (methods, classes, lambda expressions, properties)
- **Granular Kotlin visibility controls** (classes, functions, properties, lambdas, if/when/for/while/try)
- Calculation weights for different constructs
- **Kotlin-specific settings** (Elvis operator, when expressions)

**Note**: The plugin automatically validates configuration to prevent invalid threshold combinations.

## Usage
1. **Inline Hints**: Complexity scores appear as inlay hints next to methods and classes
2. **Tool Window**: Access project-wide complexity analysis via <kbd>View</kbd> > <kbd>Tool Windows</kbd> > <kbd>Code Complexity</kbd>
   - Sortable by complexity score
   - Click to navigate to code
3. **Interactive**: Click on any complexity hint to see detailed breakdown
4. **Configuration**: Fine-tune visibility and thresholds in settings - invalid values are automatically corrected

## Supported Platforms
- **IntelliJ IDEA** 2024.3+
- **Kotlin K1 and K2** compiler modes
- **Java** and **Kotlin** languages

## Test Samples

The [`complexity-test-samples/`](complexity-test-samples/) directory is a standalone Gradle project with comprehensive test cases:

- **Java**: Simple to extreme complexity (1-25+), lambda expressions, anonymous classes, nested conditions
- **Kotlin**: when/if/for/while/try expressions, Elvis operator, properties, sealed classes

Open `complexity-test-samples` as a separate project in IntelliJ IDEA to test the plugin.

## Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

### For Contributors

**Prerequisites**:
- Java 17+ (required for building)
- Java 21 recommended for Gradle JVM to avoid validation warnings with newer JDK versions

**Quick start**:
```bash
git clone https://github.com/YOUR_USERNAME/codemetrics-idea.git
cd codemetrics-idea
./gradlew runIde
```

See [CONTRIBUTING.md](CONTRIBUTING.md) for full setup instructions and development tips.

## License
Licensed under the [MIT License](LICENSE).
<!-- Plugin description end -->

---

## Development

### Building from Source
```bash
./gradlew build
```

### Running in Development IDE
```bash
./gradlew runIde
```

### Publishing (Maintainers Only)
```bash
# Update version in gradle.properties and CHANGELOG.md
# Then create a tag:
git tag v0.2.0
git push origin v0.2.0
# Auto-publishes to JetBrains Marketplace
```
