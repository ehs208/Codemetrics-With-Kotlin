# CodeMetrics-kt

<!-- Plugin description -->
Provides inlay indicators based on a customizable complexity calculation for **Java** and **Kotlin** files.

This plugin is based on the original [CodeMetrics](https://github.com/kisstkondoros/codemetrics-idea) by Tamas Kisst (MIT License) and has been extended to support **Kotlin** with updated UI compatibility for modern IntelliJ versions.

The plugin calculates cyclomatic complexity by parsing the AST and walking through each node, displaying scores inline with customizable thresholds and color coding.

## Features
- Real-time cyclomatic complexity calculation for **Java and Kotlin**
- Inline complexity hints in the editor
- Customizable thresholds and colors
- Click on hints to see detailed breakdowns
- Supports classes, methods, control flow statements, and lambda expressions

## Installation
- **Using IDE built-in plugin system**:  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "CodeMetrics With Kotlin"</kbd> > <kbd>Install</kbd>

- **Manual installation**:  
  Download the [latest release](https://github.com/ehs208/codemetrics-idea/releases/latest) and install via  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

## Configuration
<kbd>Settings/Preferences</kbd> > <kbd>Code Metrics</kbd> to customize:
- Complexity thresholds for different colors
- Which elements to measure
- Calculation weights for different constructs

## License
Licensed under the [MIT License](LICENSE).
<!-- Plugin description end -->
