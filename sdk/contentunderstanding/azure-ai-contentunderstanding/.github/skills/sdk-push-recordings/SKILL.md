---
name: sdk-push-recordings
description: |
  Push session recordings to Azure SDK Assets repository.
  
  This skill helps you:
  - Push new test recordings after RECORD mode
  - Update assets.json with new tag
  - Manage session records in external repo
  
  IMPORTANT: Run after successful RECORD mode tests.
  
  Trigger phrases: "push recordings", "push assets", "update session records"
---

# SDK Push Recordings

This skill pushes session recordings to the Azure SDK Assets repository after RECORD mode testing.

## 🎯 What This Skill Does

1. Validates local session recordings
2. Pushes recordings to Azure SDK Assets repo
3. Updates `assets.json` with new tag
4. Commits the updated `assets.json` (optional)

## 📋 Pre-requisites

- [ ] RECORD mode tests completed successfully
- [ ] Session recordings exist in `.assets` directory
- [ ] Git credentials configured for Azure SDK Assets repo
- [ ] `assets.json` file present in module directory

## 🔧 Usage

### Push Recordings

```bash
# Navigate to SDK module
cd sdk/{service}/{module}

# Push recordings to assets repo
test-proxy push -a assets.json
```

### Verify Push

```bash
# Check new tag in assets.json
cat assets.json

# Example output:
# {
#   "AssetsRepo": "Azure/azure-sdk-assets",
#   "AssetsRepoPrefixPath": "java",
#   "TagPrefix": "java/contentunderstanding/azure-ai-contentunderstanding",
#   "Tag": "java/contentunderstanding/azure-ai-contentunderstanding_abc123"
# }
```

## 📦 Assets Repository

### Repository Location

- **Main repo**: `Azure/azure-sdk-assets`
- **URL**: <https://github.com/Azure/azure-sdk-assets>

### Tag Format

```
{language}/{service}/{module}_{commit-hash}
```

Example: `java/contentunderstanding/azure-ai-contentunderstanding_7c2854bb8e`

## ⚠️ Important Notes

### Git Credentials

The test-proxy needs Git credentials to push to the assets repo:

```bash
# Ensure Git is configured
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"

# For Azure DevOps, use PAT or credential manager
```

### Large Recordings

If recordings are large, the push may take time:

- Be patient during upload
- Check network connection
- Verify disk space in `.assets`

### After Push

1. **Commit assets.json**: Include updated tag in your PR
2. **Verify PLAYBACK**: Run PLAYBACK tests to ensure recordings work
3. **Push PR changes**: Include assets.json in your commit

## 🔍 Troubleshooting

### Push Failed - Authentication

```bash
# Check Git credentials
git credential-manager get

# Or use HTTPS with PAT
export GIT_ASKPASS=/path/to/credential-helper
```

### Push Failed - No Changes

If no recordings changed, push will succeed but tag won't update.

### Missing .assets Directory

```bash
# Run RECORD mode first
mvn test -DAZURE_TEST_MODE=RECORD

# Then push
test-proxy push -a assets.json
```

## 🌐 Cross-Language Workflow

The push workflow is the same for all languages:

```bash
# 1. Run RECORD mode tests
# (language-specific command)

# 2. Push recordings (universal)
test-proxy push -a assets.json

# 3. Verify PLAYBACK mode
# (language-specific command)

# 4. Commit assets.json
git add assets.json
git commit -m "Update session recordings"
```

## ✅ Post-Push Checklist

- [ ] New tag visible in `assets.json`
- [ ] PLAYBACK tests pass with new recordings
- [ ] `assets.json` committed to your branch
- [ ] PR updated with recording changes
