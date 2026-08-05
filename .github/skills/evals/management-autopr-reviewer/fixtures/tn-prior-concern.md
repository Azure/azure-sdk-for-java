# Pull request snapshot

Title: `[AutoPR azure-resourcemanager-contosowidgets]-generated-from-SDK Generation`
Author: `app/azure-sdk-automation`
Base: `main`
Draft: `false`
Release plan: https://example.invalid/releaseplan/14

Head SHA: `2222222222222222222222222222222222222222`

Prior workflow comment:

```markdown
<!-- management-autopr-review -->
- Head SHA: `1111111111111111111111111111111111111111`
- [MGMT-CHANGELOG] New - The 1.2.0-beta.1 release contains two package
  api-version lines. Which API version should this release target?
```

Current CHANGELOG still contains the same two API-version lines. The new Java
commit adds one unrelated generated model and does not address the question.
