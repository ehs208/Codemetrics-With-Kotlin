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
- Customizable thresholds and colors
- Click on hints to see detailed breakdowns
- **Full Kotlin K2 compiler mode support**
- Supports classes, methods, control flow statements, and lambda expressions
- **Specialized handling for Kotlin constructs** (Elvis operator, when expressions, etc.)

## Installation
- **Using IDE built-in plugin system**:
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "CodeMetrics With Kotlin"</kbd> > <kbd>Install</kbd>

## Configuration
<kbd>Settings/Preferences</kbd> > <kbd>Code Metrics</kbd> to customize:
- Complexity thresholds for different colors (low/normal/high/extreme)
- Which elements to measure (methods, classes, lambda expressions)
- Calculation weights for different constructs
- **Kotlin-specific settings** (Elvis operator, when expressions)

## Usage
1. **Inline Hints**: Complexity scores appear as inlay hints next to methods and classes
2. **Tool Window**: Access project-wide complexity analysis via <kbd>View</kbd> > <kbd>Tool Windows</kbd> > <kbd>Code Complexity</kbd>
3. **Interactive**: Click on any complexity hint to see detailed breakdown

## Supported Platforms
- **IntelliJ IDEA** 2024.2.1+
- **Kotlin K1 and K2** compiler modes
- **Java** and **Kotlin** languages

## License
Licensed under the [MIT License](LICENSE).
<!-- Plugin description end -->
