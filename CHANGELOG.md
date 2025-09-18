# Changelog

## [Unreleased]

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
