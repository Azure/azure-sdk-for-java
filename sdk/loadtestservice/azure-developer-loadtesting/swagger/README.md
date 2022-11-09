## Generate autorest code

```yaml
# require: https://github.com/Azure/azure-rest-api-specs/blob/main/specification/loadtestservice/data-plane/readme.md
input-file: https://github.com/Azure/azure-rest-api-specs/blob/loadtest_stableapis/specification/loadtestservice/data-plane/Microsoft.LoadTestService/stable/2022-11-01/loadtestservice.json
java: true
data-plane: true
title: LoadTestingClient
package-version: 1.0.0
security: AADToken
security-scopes: https://cnt-prod.loadtesting.azure.com/.default
artifact-id: azure-developer-loadtesting
namespace: com.azure.developer.loadtesting
generate-builder-per-client: false
partial-update: true
output-folder: $(azure-sdk-for-java-folder)/sdk/loadtestservice/azure-developer-loadtesting
regenerate-pom: false
generate-models: false
generate-samples: false
generate-tests: false
service-versions:
- '2022-11-01'
directive:
- from: swagger-document
  where: '$.paths.*[?(@.tags=="Test")]'
  transform: >
    $["operationId"] = $["operationId"].replace("Test_", "LoadTestAdministration_");
- from: swagger-document
  where: '$.paths.*[?(@.tags=="TestRun")]'
  transform: >
    $["operationId"] = $["operationId"].replace("TestRun_", "LoadTestRun_");
```
