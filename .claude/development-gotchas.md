# Development Gotchas

## PSI Access Threading

**Rule**: Always use `ReadAction` when accessing PSI in background tasks.

```java
// ✅ Correct
ReadAction.run(() -> {
    PsiElement element = psiFile.findElementAt(offset);
    // ... PSI operations
});

// ❌ Wrong - Will throw exception!
PsiElement element = psiFile.findElementAt(offset); // Non-EDT thread
```

**Why**: IntelliJ throws exceptions if PSI is accessed from non-EDT threads without ReadAction.

---

## Deprecated API Migration

This codebase uses modern IntelliJ Platform APIs. Do NOT use deprecated patterns:

| ✅ Modern API | ❌ Deprecated API |
|--------------|-------------------|
| `project.getService()` | `ComponentManager.getComponent()` |
| `ApplicationManager.getApplication().getService()` | `ServiceManager.getService()` |
| Project services & startup activities | `ProjectComponent` |
| `addDocumentListener()` | Legacy listener registration |

**Example**:
```java
// ✅ Modern
MetricsConfiguration config = ApplicationManager.getApplication()
    .getService(MetricsConfiguration.class);

// ❌ Deprecated
MetricsConfiguration config = ServiceManager.getService(MetricsConfiguration.class);
```

---

## AST Processing Priority

IntelliJ PSI processes **node types before token types**.

### Kotlin Elvis Operator Example

For `status = value ?: default`:

1. `KtNodeTypes.BINARY_EXPRESSION` is matched **first** (node type)
2. Within the handler, check for `KtTokens.ELVIS` (token type)
3. Route to `kotlinElvisExpression` configuration
4. This ensures correct complexity weight is applied

**Why this matters**: Token-only matching won't work because node type matches first.

---

## K2 Mode Support

### Declaration
Plugin explicitly declares K2 compiler support:

```xml
<extensions defaultExtensionNs="org.jetbrains.kotlin">
  <supportsKotlinPluginMode supportsK2="true" />
</extensions>
```

### Compatibility
Both K1 and K2 modes are supported. No special handling needed - the plugin APIs are compatible with both.

---

## Common Development Issues

### GradleJvmSupportMatrix Error (Java 25)

**What you'll see**:
```
Error: GradleJvmSupportMatrix: IntelliJ 2024.3 cannot parse Java 25
```

**What's happening**: IntelliJ 2024.3 doesn't recognize Java 25 syntax, and Gradle complains about it when validating the JVM version you're using.

**Does it break the build?** Nope. The plugin builds and runs fine. It's just an IDE validation warning.

**How to fix it** (if it bothers you):
1. Open IntelliJ Settings → Build, Execution, Deployment → Gradle
2. Set **Gradle JVM** to `Java 21` (not your default JDK)
3. Click Apply and sync your project

**Or just ignore it** - the build works regardless. Your system JDK can be Java 25; Gradle will still compile everything correctly. This is purely an IDE configuration issue, not a plugin code bug.

---

## Version Management

### Where Version is Defined
- Primary: `gradle.properties` → `pluginVersion = X.Y.Z`
- Override: Git tags (format: `vX.Y.Z`)
- Build: Extracted from `GITHUB_REF_NAME` if available

### Critical Rule
**Contributors must NEVER change `pluginVersion` in gradle.properties**

- CI automatically rejects PRs with version changes
- Only maintainer updates version during release
- CI validation: compares PR version with main branch version

---

## Configuration

User-facing settings at **Settings → Code Metrics**:

### Thresholds
Configurable complexity levels:
- Low (green)
- Normal (yellow)
- High (orange)
- Extreme (red)

### Elements
Toggle metrics display for:
- Methods
- Classes
- Lambda expressions

### Weights
Customize complexity calculation for:
- Control flow statements
- Boolean operators
- Kotlin-specific constructs (Elvis, when, etc.)

---

## Common Issues

### Issue: "PSI access from non-EDT thread"
**Solution**: Wrap in `ReadAction`

### Issue: Changes not appearing in dev IDE
**Solution**:
```bash
./gradlew clean buildPlugin runIde
```

### Issue: Inlay hints not updating
**Solution**: Check `InlayManager` disposal and re-creation logic

### Issue: K2 mode compatibility error
**Solution**: Verify `<supportsKotlinPluginMode supportsK2="true"/>` is present
