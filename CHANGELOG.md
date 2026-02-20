# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]
### Added
### Changed
### Fixed
### Removed

## [0.2.0] - 2026-02-20
### Added
- Complexity level indicators in inlay hints (e.g., "15 (High)" instead of just "15")
- Header and description section in Complexity Analysis tool window
- Empty state messages with helpful guidance in tool window
- Configuration validation to prevent invalid complexity thresholds
- Kotlin control flow visibility toggles (if, when, for, while, try expressions)
- Real-time input validation in settings with visual feedback (red border and tooltips)
- Notification dialog when settings are automatically corrected for invalid threshold values
- Input validation with user-friendly error dialogs in settings UI
- Automatic configuration validation when loading settings from disk

### Changed
- Complexity level descriptions now use professional terminology (Low, Normal, High, Extreme)
- Inlay hints now display in bold font for better readability
- Settings dialog labels simplified for clarity (e.g., "Color: Low" instead of "Complexity color low")
- Tool window status messages improved with better user feedback
- Improved complexity color calculation with division-by-zero prevention
- Reusable MetricsParser instance in ComplexityAnalysisService to reduce object allocation
- Minimum platform version updated to IntelliJ IDEA 2024.3 for better compatibility

### Fixed
- Settings dialog no longer shows excessive vertical whitespace in Basics and Miscellaneous tabs
- Settings panel now uses per-tab scrolling instead of outer scroll pane
- Complexity hints now appear immediately for files already open when plugin loads
- Settings dialog no longer shows error messages in wrong IDE window
- Invalid input in settings fields is now properly prevented and highlighted
- Kotlin property metrics toggle not being respected in handler registration
- Potential division-by-zero errors in color interpolation when thresholds are equal
- Silent acceptance of invalid numeric input in configuration fields
- Missing configuration validation in InlayManager and ComplexityAnalysisService
- Cascading validation logic ensuring strict threshold ordering (low < normal < high < extreme)

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
