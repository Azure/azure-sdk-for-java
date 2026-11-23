# Pull request snapshot

Title: `[AutoPR azure-resourcemanager-compute]-generated-from-SDK Generation`
Author: `app/azure-sdk-automation`
Base: `main`
Draft: `false`
Release Plan link: https://example.com/

PR: `Azure/azure-sdk-for-java#12345`
Head SHA: `2222222222222222222222222222222222222222`
Package: `azure-resourcemanager-compute`
Package version: `1.0.0-beta.1`
Prior workflow comment: none

New file `sdk/apimanagement/azure-resourcemanager-compute/pom.xml`:

```xml
<project>
  <groupId>com.azure.resourcemanager</groupId>
  <artifactId>azure-resourcemanager-compute</artifactId>
  <version>1.0.0-beta.1</version>
</project>
```

New file `sdk/apimanagement/azure-resourcemanager-compute/CHANGELOG.md`:

```markdown
# Release History

## 1.0.0-beta.1 (2026-10-13)

- Azure Resource Manager Compute client library for Java.
- Package api-version 2026-09-01-preview.
```

New file
`sdk/apimanagement/azure-resourcemanager-compute/src/main/java/com/azure/resourcemanager/compute/ComputeManager.java`:

```java
package com.azure.resourcemanager.compute;

public final class ComputeManager {
}
```

The PR description includes this excerpt from the upstream `tspconfig.yaml`
used for generation:

```yaml
parameters:
  service-dir:
    default: sdk/apimanagement
```
