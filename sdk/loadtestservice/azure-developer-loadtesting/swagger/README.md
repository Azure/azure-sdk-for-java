## Generate autorest code

```yaml
java: true
regenerate-pom: false
title: LoadTestingClient
security: AADToken
security-scopes: https://cnt-prod.loadtesting.azure.com/.default
data-plane: true
generate-models: false
generate-samples: false
generate-tests: false
artifact-id: azure-developer-loadtesting
namespace: com.azure.developer.loadtesting
partial-update: true
output-folder: $(azure-sdk-for-java-folder)/sdk/loadtestservice/azure-developer-loadtesting
service-versions:
  - 2022-06-01-preview
directive:
  - from: swagger-document
    where: $.paths["/testruns/{testRunId}"].patch
    transform: '$["operationId"] = "TestRun_CreateOrUpdateTestRun";

      '
  - from: swagger-document
    where: $.paths.*[?(@.tags=="AppComponent")]
    transform: '$["operationId"] = $["operationId"].replace("AppComponent_", "LoadTestAdministration_");

      '
  - from: swagger-document
    where: $.paths.*[?(@.tags=="ServerMetrics")]
    transform: '$["operationId"] = $["operationId"].replace("ServerMetrics_", "LoadTestAdministration_");

      '
  - from: swagger-document
    where: $.paths.*[?(@.tags=="Test")]
    transform: '$["operationId"] = $["operationId"].replace("Test_", "LoadTestAdministration_");

      '
require: /mnt/vss/_work/1/s/azure-rest-api-specs/specification/loadtestservice/data-plane/readme.md
```
