# Release Workflow

## Contributor Workflow

### 1. Fork & Create Branch
```bash
git checkout -b feature/my-feature
```

### 2. Make Changes
- Write code
- Test with `./gradlew runIde`
- Add entry to `CHANGELOG.md` under `[Unreleased]` section

### 3. CHANGELOG.md Rules
**✅ Allowed:**
- Add changes to `[Unreleased]` section
- Use format: `### Added`, `### Fixed`, `### Changed`, `### Removed`

**❌ Forbidden:**
- Create new version sections (e.g., `## [0.2.0]`)
- Modify existing version sections
- Change `pluginVersion` in `gradle.properties`

**Example**:
```markdown
## [Unreleased]
### Added
- Support for Java switch expressions

### Fixed
- NullPointerException in Kotlin Elvis operator handler
```

### 4. Submit PR
- Create PR to `main` branch
- CI validates automatically:
  - Build succeeds
  - Version unchanged
  - CHANGELOG format correct

---

## Maintainer Release Process

### Commit Best Practices

Before releasing:
- Prefer **atomic commits** by feature
- Avoid monolithic commits combining unrelated changes
- Use conventional commit prefixes:
  - `feat:` - New features
  - `fix:` - Bug fixes
  - `docs:` - Documentation
  - `chore:` - Maintenance
  - `ci:` - CI/CD changes

### Step 1: Merge PRs
Review and merge contributor PRs to `main` via GitHub.

### Step 2: Test Accumulated Changes
```bash
git checkout main
git pull
./gradlew runIde  # Test plugin locally
```

Verify:
- All new features work
- No regressions
- Java and Kotlin files display complexity correctly
- Tool window functions properly

### Step 3: Prepare Release

#### Update CHANGELOG.md
```bash
vim CHANGELOG.md
```

**Move `[Unreleased]` → `[X.Y.Z]`:**
```markdown
# Before:
## [Unreleased]
### Added
- Feature A
- Feature B

## [0.1.9] - 2025-02-15

# After:
## [Unreleased]

## [0.2.0] - 2025-02-20
### Added
- Feature A
- Feature B

## [0.1.9] - 2025-02-15
```

#### Update Version
```bash
vim gradle.properties
# Change: pluginVersion = 0.2.0
```

#### Commit
```bash
git add CHANGELOG.md gradle.properties
git commit -m "chore: release v0.2.0"
git push origin main
```

### Step 4: Create Tag (Triggers Auto-Publish)
```bash
git tag v0.2.0
git push origin v0.2.0
```

**This triggers**:
1. GitHub Actions `publish.yml` workflow
2. Version verification (tag matches gradle.properties)
3. Build & verification
4. **Automatic publish to JetBrains Marketplace**
5. GitHub Release creation

### Step 5: Verification

Check workflow status:
```bash
# List recent workflow runs
gh run list --workflow=publish.yml

# View specific run details
gh run view <run-id>

# View logs if needed
gh run view <run-id> --log
```

Verify deployment:
```bash
# Open marketplace page
open https://plugins.jetbrains.com/plugin/28221-codemetrics-with-kotlin

# Check GitHub release
gh release view v0.2.0
```

---

## CI/CD Workflows

### CI (`.github/workflows/ci.yml`)

**Triggers**: PRs and pushes to `main`

**Validates**:
- ✅ Build succeeds
- ✅ Plugin verification passes
- ✅ Version unchanged (PRs only)
- ✅ CHANGELOG format (PRs only)

**Required Status Checks**:
- `build` - Must pass
- `validate-pr` - Must pass

### Publish (`.github/workflows/publish.yml`)

**Triggers**: Git tags matching `v*` pattern

**Steps**:
1. Checkout code at tag
2. Extract version from tag name
3. Verify `gradle.properties` version matches tag
4. Build plugin
5. Run `verifyPlugin`
6. Publish to JetBrains Marketplace (requires `PUBLISH_TOKEN` secret)
7. Create GitHub Release with changelog

**Secrets Required**:
- `PUBLISH_TOKEN` - JetBrains Marketplace API token

---

## Repository Configuration

### Branch Protection (main)

Use `gh` CLI to configure:

```bash
# Branch protection
gh api repos/OWNER/REPO/branches/main/protection \
  --method PUT \
  --input protection.json
```

**protection.json**:
```json
{
  "required_status_checks": {
    "strict": true,
    "contexts": ["build", "validate-pr"]
  },
  "enforce_admins": false,
  "required_pull_request_reviews": {
    "required_approving_review_count": 0
  },
  "allow_force_pushes": false,
  "allow_deletions": false
}
```

### Repository Settings

```bash
# Merge settings
gh api repos/OWNER/REPO --method PATCH \
  --field delete_branch_on_merge=true \
  --field allow_squash_merge=true \
  --field allow_rebase_merge=false

# Add labels
gh label create "kotlin" --description "Kotlin-related" --color "7F52FF"
gh label create "java" --description "Java-related" --color "B07219"

# Set topics (for discoverability)
gh api repos/OWNER/REPO/topics --method PUT \
  --input topics.json
```

**topics.json**:
```json
{
  "names": [
    "intellij-plugin",
    "kotlin",
    "java",
    "code-quality",
    "cyclomatic-complexity",
    "code-metrics"
  ]
}
```

---

## Troubleshooting

### Publish fails: "Version mismatch"
**Cause**: Tag version doesn't match `gradle.properties`
**Fix**:
```bash
# Delete bad tag
git tag -d v0.2.0
git push origin :refs/tags/v0.2.0

# Fix version in gradle.properties
vim gradle.properties

# Re-tag
git tag v0.2.0
git push origin v0.2.0
```

### CI fails: "Version change detected"
**Cause**: Contributor changed `pluginVersion`
**Fix**: Ask contributor to revert the change

### Marketplace publish pending
**Cause**: JetBrains approval process
**Fix**: Wait 1-2 hours, check status at marketplace
