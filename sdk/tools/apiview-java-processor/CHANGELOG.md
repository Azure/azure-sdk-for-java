# Release History

## 1.34.0 (Unreleased)

### Features Added

- Added direct Markdown output with `--renderer=markdown`; JSON remains the default.
- Added `Generate-ApiMarkdown.ps1` and SDK generation integration to write package-root `api.md` and `api.metadata.yml` files.

### Bugs Fixed

- Fail on source JAR processing errors instead of replacing API snapshots with incomplete output.

### Other Changes

- Migrated the Java APIView processor and its tests from Azure/azure-sdk-tools#16809.
