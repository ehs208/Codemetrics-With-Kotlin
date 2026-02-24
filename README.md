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
- **AI-powered refactoring suggestions** via Claude, OpenAI, Gemini, or Codex (ChatGPT OAuth)
- **Project-wide complexity analysis** tool window with batch refactoring support
- **Refactoring diff viewer** with side-by-side comparison and one-click apply
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

### AI Refactoring Settings
<kbd>Settings/Preferences</kbd> > <kbd>Code Metrics</kbd> > <kbd>AI Refactoring</kbd> tab:
- **Provider**: Choose from Claude, OpenAI, Gemini, or Codex (ChatGPT OAuth login)
- **API Key**: Enter your API key (stored securely via IntelliJ PasswordSafe)
- **Model**: Select model per provider (editable dropdown with defaults)
- **Custom Prompt**: Add custom instructions appended to the auto-generated prompt
- **Reasoning Effort** (GPT-5.x only): none / low / medium / high / xhigh

## Usage
1. **Inline Hints**: Complexity scores appear as inlay hints next to methods and classes
2. **Tool Window**: Access project-wide complexity analysis via <kbd>View</kbd> > <kbd>Tool Windows</kbd> > <kbd>CodeMetrics</kbd>
   - Sortable by complexity score
   - Click to navigate to code
   - **Suggest Refactoring** for single methods, **Batch Refactoring** for multiple
3. **AI Refactoring**: Right-click on a complexity hint > **Suggest AI Refactoring**, or use the lightbulb (Alt+Enter) on complex methods
4. **Interactive**: Click on any complexity hint to see detailed breakdown
5. **Configuration**: Fine-tune visibility, thresholds, and AI provider settings

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

## Screenshots

<p align="center">
  <img src="screenshots/1.png" width="45%" alt="Code complexity hints in editor" />
  <img src="screenshots/2.png" width="45%" alt="Complexity analysis tool window" />
</p>

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
See [Release Workflow](.claude/release-workflow.md) for details.
