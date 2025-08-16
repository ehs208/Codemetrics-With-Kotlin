# Contributing to CodeMetrics With Kotlin

Thank you for your interest in contributing to CodeMetrics With Kotlin! This document provides guidelines for contributing to this project.

## Getting Started

### Prerequisites
- IntelliJ IDEA (Community or Ultimate Edition)
- Java 17 or higher
- Kotlin plugin for IntelliJ IDEA

### Setting up the Development Environment

1. Fork and clone the repository:
   ```bash
   git clone https://github.com/ehs208/Codemetrics-With-Kotlin.git
   cd Codemetrics-With-Kotlin
   ```

2. Open the project in IntelliJ IDEA

3. Build the project:
   ```bash
   ./gradlew build
   ```

4. Run the plugin in a development environment:
   ```bash
   ./gradlew runIde
   ```

## How to Contribute

### Reporting Bugs
- Use the GitHub issue tracker
- Check if the issue already exists
- Provide detailed reproduction steps
- Include IntelliJ IDEA version and plugin version

### Suggesting Features
- Open a GitHub issue with the "enhancement" label
- Describe the feature clearly
- Explain the use case and benefits

### Code Contributions

#### Before You Start
- Check existing issues and PRs to avoid duplication
- For large changes, open an issue first to discuss
- Follow the existing code style and patterns

#### Making Changes
1. Create a new branch from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. Make your changes following these guidelines:
   - Write clear, self-documenting code
   - Follow Java and Kotlin coding conventions
   - Add comments for complex logic
   - Update documentation if needed

3. Test your changes:
   - Build the project successfully
   - Test the plugin functionality manually
   - Ensure no regressions in existing features

4. Commit your changes:
   ```bash
   git commit -m "descriptive commit message"
   ```

#### Pull Request Process
1. Push your branch to your fork
2. Create a Pull Request against the `main` branch
3. Fill out the PR template completely
4. Wait for code review and address feedback
5. Ensure CI checks pass

### Code Style Guidelines
- Use meaningful variable and method names
- Keep methods small and focused
- Follow IntelliJ IDEA's default formatting
- Use proper JavaDoc for public APIs
- Maintain consistency with existing codebase

### Project Structure
```
src/main/java/com/github/ehs208/codemetrics/
├── configuration/     # Plugin configuration and settings
├── core/             # Core complexity calculation logic
│   ├── parser/       # AST parsing and analysis
│   └── config/       # Configuration management
├── inlay/            # Editor integration and inlay hints
└── util/             # Utility classes
```

## Development Tips

### Debugging the Plugin
- Use IntelliJ IDEA's built-in debugging tools
- Set breakpoints in plugin code
- Use the development instance for testing

### Understanding the Codebase
- Start with `MetricsParser` - the main entry point
- Review `TreeWalker` for AST traversal logic
- Check `InlayManager` for editor integration

### Testing Different Languages
- Test with both Java and Kotlin files
- Verify complexity calculations are accurate
- Check inlay hints display correctly

## Community Guidelines

### Be Respectful
- Use welcoming and inclusive language
- Respect different viewpoints and experiences
- Focus on constructive feedback

### Communication
- Use clear and concise language
- Provide context for your contributions
- Ask questions if anything is unclear

## Getting Help
- Open an issue for questions about the codebase
- Check existing documentation and code comments
- Review similar IntelliJ plugins for reference

## Recognition
Contributors will be acknowledged in the project documentation and release notes.

Thank you for contributing to CodeMetrics With Kotlin!