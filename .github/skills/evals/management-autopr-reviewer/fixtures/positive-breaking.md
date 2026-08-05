# Pull request snapshot

Title: `[AutoPR azure-resourcemanager-contosowidgets]-generated-from-SDK Generation`
Author: `app/azure-sdk-automation`
Base: `main`
Draft: `false`
Release plan: https://azsdk-releaseplan-dashboard-hveph5aqhhcfhtgu.westus-01.azurewebsites.net/?releaseplan=35926

Package: `azure-resourcemanager-contosowidgets`
Package version: `2.0.0`

Current CHANGELOG section:

```markdown
## 2.0.0 (Unreleased)

- Package api-version 2026-06-01.

### Breaking Changes

#### `models.Widget` was modified

* `java.lang.String status()` -> `models.WidgetStatus status()`
```

The status return-type change entered the main branch in an earlier beta, so
the current Java diff contains only unrelated additive APIs. This GA CHANGELOG
compares 2.0.0 with the previous GA release.
