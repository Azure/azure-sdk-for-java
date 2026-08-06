# Pull request snapshot

Title: `[AutoPR azure-resourcemanager-contosomaintenance]-generated-from-SDK Generation`
Author: `app/azure-sdk-automation`
Base: `main`
Draft: `false`
Release Plan link: https://azsdk-releaseplan-dashboard-hveph5aqhhcfhtgu.westus-01.azurewebsites.net/?releaseplan=35926

Generation evidence in CHANGELOG:

```markdown
## 1.2.0-beta.1 (2026-07-15)

- Package api-version 2026-06-01-preview.
- Package api-version 2026-08-01-preview.
- Added scheduled maintenance operations.
```

The dated `1.2.0-beta.1` section and its first API-version line already existed
on the base branch. This PR ran generation for a second API version on the same
branch, regenerated the same section, and added the second API-version line.

Changed Java adds operations from `2026-08-01-preview`.
