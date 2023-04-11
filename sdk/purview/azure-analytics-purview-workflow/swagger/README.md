## Generate autorest code

```yaml
input-file:
- https://github.com/Azure/azure-rest-api-specs/blob/3a1ca5a2130250382e65e4585d8e88bfab6d93e4/specification/purview/data-plane/Azure.Analytics.Purview.Workflow/preview/2022-05-01-preview/purviewWorkflow.json
output-folder: ../
java: true
regenerate-pom: false
title: PurviewWorkflow
security-scopes: https://purview.azure.net/.default
data-plane: true
generate-tests: true
artifact-id: azure-analytics-purview-workflow
generate-samples: true
namespace: com.azure.analytics.purview.workflow
service-versions:
- 2022-05-01-preview
```
