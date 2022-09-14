## Generate autorest code

```yaml
require: C:/Users/harshanb/source/repos/azure-rest-api-specs/specification/loadtestservice/data-plane/readme.md
output-folder: ../
java: true
regenerate-pom: false
title: LoadTestingClient
security: AADToken
security-scopes: https://loadtest.azure-dev.com/.default
data-plane: true
generate-tests: false
generate-models: true
artifact-id: azure-developer-loadtesting
generate-samples: false
namespace: com.azure.developer.loadtesting
partial-update: false
service-versions:
- 2022-06-01-preview
```
