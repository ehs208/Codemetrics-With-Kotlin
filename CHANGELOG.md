# Changelog

## [Unreleased]

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
