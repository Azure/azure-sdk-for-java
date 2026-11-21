# Release History

## 1.0.0-beta.1 (Unreleased)

### Features Added

- Generate API review Markdown and metadata from built Java sources JARs without extra SDK-repository files.
- Run the same dependency-bundled plugin JAR directly to generate both files without Maven or an external POM.
- Detect repository-relative module paths with explicit overrides for sparse or exported checkouts.
- Preserve existing bundles if generation fails.
- Build and stage the executable tool from pinned SDK repository sources for API Review Hub.

### Bugs Fixed

- Support custom Maven sources artifact names without overriding their package identity.
- Isolate rendering state during parallel Maven execution.
- Fail script validation on Pester container and teardown errors.
