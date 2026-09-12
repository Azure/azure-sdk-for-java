# Pull request snapshot

Title: `[AutoPR azure-resourcemanager-contosowidgets]-generated-from-SDK Generation`
Author: `app/azure-sdk-automation`
Base: `main`
Draft: `false`
Release Plan link: https://azsdk-releaseplan-dashboard-hveph5aqhhcfhtgu.westus-01.azurewebsites.net/?releaseplan=35926

Package: `azure-resourcemanager-contosowidgets`
Package version: `1.3.0-beta.2`
API version: `2026-04-01-preview`

Changed Java:

```diff
+ public WidgetManager.DefinitionStages.WithCreateMode withCreateMode(CreateMode mode);
+ public final class CreateMode { ... }
```

Changed POM:

```diff
- <azure-core-management.version>1.18.0</azure-core-management.version>
+ <azure-core-management.version>1.19.0</azure-core-management.version>
```

CHANGELOG:

```markdown
## 1.3.0-beta.2 (Unreleased)

- Package api-version 2026-04-01-preview.
- Added create mode support.
```
