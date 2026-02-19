# Contributing to CodeMetrics With Kotlin

Thank you for your interest in contributing! This guide will help you get started.

## 🚀 Quick Start

### Prerequisites
- **Java 17+** (required)
- **IntelliJ IDEA 2024.2.1+** (Community or Ultimate)
- **Gradle JVM**: Java 21 recommended (to avoid validation warnings with newer JDK versions)
- Git

### Setup Development Environment

1. **Fork and clone**:
   ```bash
   git clone https://github.com/YOUR_USERNAME/codemetrics-idea.git
   cd codemetrics-idea
   ```

2. **Build the project**:
   ```bash
   ./gradlew build
   ```

3. **Run in development IDE**:
   ```bash
   ./gradlew runIde
   ```
   A new IntelliJ instance will launch with the plugin installed.

## 📝 Making Contributions

### 1. Create a Feature Branch

```bash
git checkout -b feature/your-feature-name
# or
git checkout -b fix/bug-description
```

**Branch naming conventions**:
- `feature/` - New features
- `fix/` - Bug fixes
- `docs/` - Documentation updates
- `refactor/` - Code refactoring

### 2. Make Your Changes

Write clean, readable code that follows the existing patterns in the codebase.

**Key guidelines**:
- ✅ Follow existing code style
- ✅ Keep methods small and focused
- ✅ Add comments for complex logic
- ✅ Test your changes thoroughly

### 3. Update CHANGELOG.md (Recommended)

Add your changes to the `[Unreleased]` section:

```markdown
## [Unreleased]
### Added
- Your new feature description

### Fixed
- Bug fix description

### Changed
- Any modifications to existing features
```

**Important**:
- ✅ Only add to the `[Unreleased]` section
- ❌ **DO NOT** create new version sections like `## [0.2.0]`
- ❌ **DO NOT** modify existing version sections

The maintainer will move `[Unreleased]` items to a versioned section during release.

### 4. Test Your Changes

```bash
# Build and verify
./gradlew build verifyPlugin

# Run in development IDE
./gradlew runIde
```

**Manual testing checklist**:
- [ ] Plugin loads without errors
- [ ] New feature works as expected
- [ ] Existing features still work (no regressions)
- [ ] Test with both Java and Kotlin files

### 5. Commit Your Changes

Use clear, descriptive commit messages:

```bash
git add .
git commit -m "feat: add support for switch expressions"
# or
git commit -m "fix: prevent NPE in TreeWalker for lambda expressions"
```

**Commit message format** (recommended):
- `feat:` - New feature
- `fix:` - Bug fix
- `docs:` - Documentation changes
- `refactor:` - Code refactoring
- `test:` - Test updates
- `chore:` - Build/config changes

### 6. Push and Create Pull Request

```bash
git push origin feature/your-feature-name
```

Then create a Pull Request on GitHub:
- **Base branch**: `main`
- **Title**: Clear, descriptive title
- **Description**: Explain what changed and why

## ⚠️ Important Rules

### What Contributors Should NOT Change

❌ **DO NOT modify `pluginVersion` in `gradle.properties`**
```properties
# DON'T CHANGE THIS:
pluginVersion = 0.1.9
```

The CI will **automatically reject** your PR if you change the version number. The maintainer handles version updates during releases.

❌ **DO NOT create new version sections in CHANGELOG.md**
```markdown
# DON'T DO THIS:
## [0.2.0] - 2025-02-20
### Added
- My feature
```

Only add to `[Unreleased]`.

### What You CAN Change

✅ Code files (`.java`, `.kt`)
✅ Resources (`.xml`, icons, etc.)
✅ Documentation (`README.md`, docs)
✅ `[Unreleased]` section in `CHANGELOG.md`
✅ Build configuration (if needed, explain in PR)

## 🔍 Code Review Process

1. **CI Checks**: Your PR must pass automated checks:
   - ✅ Build succeeds
   - ✅ Plugin verification passes
   - ✅ Version unchanged (if gradle.properties modified)
   - ✅ CHANGELOG format valid (if modified)

2. **Maintainer Review**: The maintainer will review your code and may:
   - Ask questions
   - Request changes
   - Suggest improvements

3. **Merge**: Once approved, the maintainer will merge your PR

## 🏗️ Project Structure

```
src/main/java/com/github/ehs208/codemetrics/
├── configuration/         # Settings UI and configuration
│   └── EditorConfig.java
├── core/                  # Core complexity calculation
│   ├── MetricsModel.java
│   ├── parser/           # AST parsing
│   │   ├── MetricsParser.java
│   │   ├── TreeWalker.java
│   │   └── HandlerRegistry.java
│   └── config/           # Configuration management
│       └── MetricsConfiguration.java
├── inlay/                # Editor integration
│   ├── InlayManager.java
│   ├── EditorListener.java
│   └── InlayListenerManager.java
├── toolwindow/           # Complexity tool window
│   ├── ComplexityToolWindowFactory.java
│   └── ComplexityAnalysisService.java
└── util/                 # Utilities
```

**Key files**:
- `MetricsParser.java` - Entry point for complexity calculation
- `TreeWalker.java` - AST traversal logic
- `HandlerRegistry.java` - Maps PSI elements to complexity handlers
- `InlayManager.java` - Manages inlay hints in editor

## 💡 Development Tips

### Debugging the Plugin

1. Add breakpoints in your code
2. Run `./gradlew runIde`
3. IntelliJ debugger will attach automatically

### Understanding Complexity Calculation

The plugin calculates cyclomatic complexity by:
1. Parsing Java/Kotlin AST via IntelliJ PSI
2. Walking through each node (`TreeWalker`)
3. Using handlers to calculate complexity per element type
4. Displaying results as inlay hints

**Key insight**: Kotlin support uses dual registration in `HandlerRegistry`:
- Java elements: `JavaElementType.*`
- Kotlin elements: `KtNodeTypes.*` and `KtTokens.*`

### Testing Kotlin Features

Special handling exists for:
- **Elvis operator** (`?:`): Routed to `kotlinElvisExpression` config
- **When expressions**: Similar to Java switch
- **Lambda expressions**: Treated as methods

### Common Issues

**Problem**: "PSI access from non-EDT thread"
**Solution**: Wrap PSI operations in `ReadAction`:
```java
ReadAction.run(() -> {
    // PSI access here
});
```

**Problem**: Changes not appearing in dev IDE
**Solution**: Rebuild and restart:
```bash
./gradlew clean buildPlugin runIde
```

## 🐛 Reporting Bugs

1. **Search existing issues** to avoid duplicates
2. **Create a new issue** with:
   - Clear title and description
   - Steps to reproduce
   - Expected vs actual behavior
   - IntelliJ version and plugin version
   - Screenshots if applicable

## 💬 Suggesting Features

1. **Open a GitHub issue** with "enhancement" label
2. **Describe the feature**:
   - What problem does it solve?
   - How should it work?
   - Why is it valuable?
3. **Wait for feedback** before implementing

## 📚 Helpful Resources

- [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html)
- [PSI Viewer](https://plugins.jetbrains.com/plugin/227-psiviewer) - Inspect AST structure
- [Kotlin PSI](https://github.com/JetBrains/kotlin/tree/master/compiler/psi/src/org/jetbrains/kotlin/psi)
- [keepachangelog.com](https://keepachangelog.com/) - CHANGELOG format

## 🤝 Community Guidelines

- Be respectful and constructive
- Ask questions if anything is unclear
- Help others when you can
- Give credit where it's due

## 🎉 Recognition

All contributors are acknowledged in:
- GitHub contributors list
- Release notes
- Project documentation

---

**Questions?** Open an issue or check [CLAUDE.md](CLAUDE.md) for architecture details.

Thank you for contributing! 🚀
