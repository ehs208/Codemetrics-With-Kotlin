# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

## [0.3.1] - 2026-03-09
### Fixed
- Fix slow operation error when checking AI provider configuration inside read actions
- Preserve leading whitespace in AI refactored code (`.trim()` was stripping indentation)
- Instruct AI to strictly reduce cyclomatic complexity and avoid adding new control flow
- Reformat code after applying AI refactoring to restore project code style
- Simplify post-refactoring notification; removed unreliable complexity comparison that failed when AI renamed or split the method
- Restrict AI refactoring button to methods and functions only (for-loops, anonymous classes, etc. are no longer eligible)
- Prevent EDT freeze by caching `MetricsConfiguration` in the complexity list cell renderer

## [0.3.0]
### Added
- **AI refactoring** - get AI-powered refactoring suggestions for complex methods via Claude, OpenAI, Gemini, or Codex (ChatGPT login)
- **Diff viewer** - review AI suggestions side-by-side with original code, with explanation and one-click apply
- **Batch refactoring** - select multiple methods in the tool window and refactor them at once
- **Refactoring history** - new History tab in the tool window tracks all AI suggestions
- **Quick actions** - trigger refactoring from lightbulb (Alt+Enter) or right-click on complexity hints
- **Complexity re-measurement** - see complexity reduction after applying refactoring
- **AI configuration** - choose provider/model, set custom prompts, and configure reasoning effort

### Changed
- Tool window renamed from "CodeComplexity" to "CodeMetrics" with new icon
- Tool window now uses tabbed layout (Analysis + History)
- Method list supports multi-selection for batch operations

## [0.2.0] - 2026-02-20
### Added
- Complexity level indicators in inlay hints (e.g., "15 (High)" instead of just "15")
- Kotlin control flow visibility toggles (if, when, for, while, try expressions)
- Configuration validation with real-time feedback, visual indicators, and automatic correction
- Enhanced Complexity Analysis tool window with header, descriptions, and empty states

### Changed
- Professional terminology for complexity levels (Low, Normal, High, Extreme)
- Bold font for inlay hints to improve readability
- Simplified settings dialog labels (e.g., "Color: Low" instead of "Complexity color low")
- Minimum platform version to IntelliJ IDEA 2024.3 for better compatibility

### Fixed
- Settings dialog vertical whitespace in Basics and Miscellaneous tabs (implemented per-tab scrolling)
- Complexity hints now appear immediately for files already open when plugin loads
- PSI access thread safety issues with proper ReadAction wrapping
- Configuration validation with strict threshold ordering (low < normal < high < extreme)
- Division-by-zero errors in color interpolation when thresholds are equal

## [0.1.8] - 2025-09-18
### Added
- Complete Kotlin K2 compiler mode support declaration
- Modern IntelliJ Platform Gradle Plugin 2.9.0 compatibility

### Changed
- Updated to latest IntelliJ Platform Gradle Plugin (2.9.0)
- Migrated deprecated `ide()` method to modern `create()` API
- Improved document listener registration for better resource management

### Fixed
- Deprecated API usage warnings for document listener registration
- K2 mode compatibility issues resolved
- Extension point resolution for Kotlin plugin integration

## [0.1.7] - 2025-09-15
### Added
- Progress indicator for code complexity analysis

### Changed
- Use public FilenameIndex API instead of internal FileTypeIndex
- Dynamic color coding from MetricsConfiguration in tool window
- Improved settings UI layout consistency

### Fixed
- Threading issues with PSI access in background tasks
- Settings panel layout problems in dark mode


## [0.1.6] - 2025-09-14
- Replace hardcoded complexity thresholds (20, 10, 5) with dynamic values from MetricsConfiguration to ensure consistency with user settings across the plugin.

## [0.1.5] - 2025-09-07
- Add complexity analysis tool window for project-wide code overview

## [0.1.4] - 2025-09-07
- Implemented specialized handler for proper Kotlin Elvis operator (?:) processing

## [0.1.3] - 2025-08-17

### Changed
- Removed deprecated IntelliJ Platform API usages for future compatibility.

## [0.1.1] - 2025-08-16

### Changed
- Updated build configuration and GitHub Actions workflows for publishing process.

## [0.1.0] - 2025-08-15

### Added
- Initial release based on the original [CodeMetrics](https://github.com/kisstkondoros/codemetrics-idea) by Tamas Kisst (MIT License).
- Added **Kotlin language support** for code metrics calculation.
- Migrated from Maven to Gradle build system.
- Updated package structure to com.github.ehs208.codemetrics.
