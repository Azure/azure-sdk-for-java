## Generate autorest code

```yaml
input-file:
- https://github.com/Azure/azure-rest-api-specs/blob/afa158ef56a05f6603924f4a493817cec332b113/specification/purview/data-plane/Azure.Analytics.Purview.Workflow/preview/2023-10-01-preview/purviewWorkflow.json
output-folder: ../
java: true
use: '@autorest/java@4.1.52'
title: PurviewWorkflow
security-scopes: https://purview.azure.net/.default
data-plane: true
generate-tests: true
artifact-id: azure-analytics-purview-workflow
generate-samples: true
namespace: com.azure.analytics.purview.workflow
service-versions:
- 2023-10-01-preview
```
