# Architecture

## Core Components

### MetricsModel
**Location**: `core/MetricsModel.java`

Central data model representing complexity metrics with hierarchical structure. Uses memoized computation for performance and supports different collector types (MAX for classes, SUM for methods).

### MetricsParser
**Location**: `core/parser/MetricsParser.java`

Entry point that delegates to TreeWalker for AST parsing and complexity calculation.

### TreeWalker
**Location**: `core/parser/TreeWalker.java`

Core AST traversal engine that uses HandlerRegistry to process different PSI element types.

### HandlerRegistry
**Location**: `core/parser/HandlerRegistry.java`

Maps PSI element types to complexity handlers. Contains special logic for handling both Java and Kotlin constructs, including Elvis operator detection.

### InlayManager
**Location**: `core/inlay/InlayManager.java`

Manages the lifecycle of inlay hints in the editor. Implemented as a project service that creates, updates, and disposes complexity hints based on configuration thresholds.

---

## Kotlin Support

The plugin supports both Java and Kotlin through dual handler registration:

### Java Elements
Registered using `JavaElementType` constants:
- `BINARY_EXPRESSION`
- `IF_STATEMENT`
- `WHILE_STATEMENT`
- etc.

### Kotlin Elements
Registered using `KtNodeTypes` and `KtTokens`:
- `KtNodeTypes.IF`
- `KtNodeTypes.WHEN`
- `KtTokens.ELVIS`
- etc.

### Elvis Operator Special Handling

Kotlin's Elvis operator (`?:`) requires special processing:
1. `KtNodeTypes.BINARY_EXPRESSION` is matched first
2. Within the handler, `KtTokens.ELVIS` is detected
3. Routes to `kotlinElvisExpression` configuration
4. Ensures `status = value ?: default` uses correct complexity weight

---

## Tool Window (v0.1.5+)

### ComplexityToolWindowFactory
**Location**: `toolwindow/ComplexityToolWindowFactory.java`

Creates "Code Complexity" tool window accessible via **View → Tool Windows → Code Complexity**.

### ComplexityAnalysisService
**Location**: `toolwindow/ComplexityAnalysisService.java`

Performs project-wide complexity analysis in background with progress indicator.

### ComplexityAnalysisPanel
**Location**: `toolwindow/ComplexityAnalysisPanel.java`

UI component displaying sortable table of complexity metrics across entire project.

---

## Plugin Integration

### Plugin Definition
**File**: `src/main/resources/META-INF/plugin.xml`

Defines services and extensions:

```xml
<extensions defaultExtensionNs="com.intellij">
  <applicationService serviceImplementation="...MetricsConfiguration" />
  <projectService serviceImplementation="...InlayManager"/>
  <postStartupActivity implementation="...EditorListener"/>
  <toolWindow id="CodeComplexity" ... />
</extensions>
```

### Services

| Service | Scope | Purpose |
|---------|-------|---------|
| `MetricsConfiguration` | Application | Global settings |
| `InlayManager` | Project | Per-project inlay management |
| `EditorListener` | Startup | File monitoring initialization |

### Configuration UI
**Location**: `configuration/EditorConfig.java`

Provides tabbed settings interface at **Settings → Code Metrics**.

### K2 Support

Plugin explicitly declares K2 compiler support:

```xml
<extensions defaultExtensionNs="org.jetbrains.kotlin">
  <supportsKotlinPluginMode supportsK2="true" />
</extensions>
```

Both K1 and K2 modes are fully supported.

---

## Key Files

| File | Purpose |
|------|---------|
| `build.gradle.kts` | Build configuration (IntelliJ Platform Gradle Plugin 2.9.0) |
| `gradle.properties` | Plugin metadata, version, platform compatibility |
| `plugin.xml` | Plugin definition, services, extensions |
| `CHANGELOG.md` | Version history (keepachangelog.com format) |
