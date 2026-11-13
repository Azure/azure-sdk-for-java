<!-- Copyright (c) Microsoft Corporation. All rights reserved.
     Licensed under the MIT License. -->

# Release History

## 1.34.0 (Unreleased)

### Features Added

- Added direct Markdown output with `--renderer=markdown`; JSON remains the default.
- Added standalone `Generate-ApiMarkdown.ps1` to write `api.md` and `api.metadata.yml` to a build artifact directory.

### Bugs Fixed

- Fail on source JAR processing errors instead of replacing API snapshots with incomplete output.
- Preserve existing review bundles if generation fails after Maven cleanup.
- Recognize explicit builder protocols and check required methods independently for each builder.
- Finish and close compressed JSON output so it can be decompressed successfully.
- Detect prohibited packages in fully qualified public API types.
- Skip unrelated validation and source rewriting when building API review artifacts.

### Other Changes

- Migrated the Java APIView processor and its tests from Azure/azure-sdk-tools#16809.
- Added source license headers and Checkstyle validation alongside Spotless formatting.
