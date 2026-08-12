# Pull request snapshot

Title: `[AutoPR azure-resourcemanager-contosowidgets]-generated-from-SDK Generation`
Author: `app/azure-sdk-automation`
Base: `main`
Draft: `false`
Release Plan link: https://azsdk-releaseplan-dashboard-hveph5aqhhcfhtgu.westus-01.azurewebsites.net/?releaseplan=35926

Head SHA: `2222222222222222222222222222222222222222`

Prior workflow comment:

```markdown
<!-- management-autopr-review -->
- Head SHA: `1111111111111111111111111111111111111111`
- [MGMT-API-VERSION-OVERLAP] New - The branch contains package output from
  API versions 2026-06-01-preview and 2026-08-01-preview. Which generation
  should remain?
```

Current CHANGELOG still contains the same two API-version lines. The new Java
commit adds one unrelated method in
`sdk/contosowidgets/azure-resourcemanager-contosowidgets/src/main/java/com/azure/resourcemanager/contosowidgets/WidgetManager.java`,
outside any `generated` path, and does not address the question.
