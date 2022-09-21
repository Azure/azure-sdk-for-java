## Generate autorest code

```yaml
require: https://github.com/Azure/azure-rest-api-specs/blob/9401446f22177696d920cf110893a0de7452ee9e/specification/loadtestservice/data-plane/readme.md
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
partial-update: true
service-versions:
- 2022-06-01-preview
directive:
- from: swagger-document
  where: '$.paths.*[?(@.tags=="AppComponent")]'
  transform: >
    $["operationId"] = $["operationId"].replace("AppComponent_", "LoadTestAdministration_");
- from: swagger-document
  where: '$.paths.*[?(@.tags=="ServerMetrics")]'
  transform: >
    $["operationId"] = $["operationId"].replace("ServerMetrics_", "LoadTestAdministration_");
- from: swagger-document
  where: '$.paths.*[?(@.tags=="Test")]'
  transform: >
    $["operationId"] = $["operationId"].replace("Test_", "LoadTestAdministration_");
```
