## Generate autorest code

```yaml
require: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/specification/purview/data-plane/Azure.Analytics.Purview.Account/readme.md
output-folder: ../
java: true
regenerate-pom: false
title: PurviewAccountClient
data-plane: true
generate-tests: true
artifact-id: azure-analytics-purview-account
generate-samples: true
namespace: com.azure.analytics.purview.account
service-versions:
- 2019-11-01-preview
```
