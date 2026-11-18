# Pull request snapshot

Title: `[AutoPR azure-resourcemanager-aigateway]-generated-from-SDK Generation`
Author: `app/azure-sdk-automation`
Base: `main`
Draft: `false`
Release Plan link: https://example.com/release-plan/12345

PR: `Azure/azure-sdk-for-java#12345`
Head SHA: `2222222222222222222222222222222222222222`
Package: `azure-resourcemanager-aigateway`
Package version: `1.0.0-beta.1`

## Latest workflow comment

<!-- management-autopr-review -->
Head SHA: `1111111111111111111111111111111111111111`
Decision: `changes requested`

### Blocking

- `[MGMT-FOLDER]` New — `sdk/apimanagement/azure-resourcemanager-aigateway/pom.xml`
  places the new module under `apimanagement` instead of `aigateway`. Correct
  upstream `service-dir` to `sdk/aigateway` and regenerate before merging.

## Current PR diff and head

The base branch has no `azure-resourcemanager-aigateway` module. The current PR
adds it under `sdk/aigateway`; there are no files left under
`sdk/apimanagement/azure-resourcemanager-aigateway`.

New file `sdk/aigateway/azure-resourcemanager-aigateway/pom.xml`:

```xml
<project>
  <groupId>com.azure.resourcemanager</groupId>
  <artifactId>azure-resourcemanager-aigateway</artifactId>
  <version>1.0.0-beta.1</version>
</project>
```

New file `sdk/aigateway/azure-resourcemanager-aigateway/CHANGELOG.md`:

```markdown
# Release History

## 1.0.0-beta.1 (2026-10-13)

- Azure Resource Manager AI Gateway client library for Java.
- Package api-version 2026-09-01-preview.
```

New file
`sdk/aigateway/azure-resourcemanager-aigateway/src/main/java/com/azure/resourcemanager/aigateway/AIGatewayManager.java`:

```java
package com.azure.resourcemanager.aigateway;

public final class AIGatewayManager {
}
```
