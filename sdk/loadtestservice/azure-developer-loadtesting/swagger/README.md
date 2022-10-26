## Generate autorest code

```yaml
# require: https://github.com/Azure/azure-rest-api-specs/blob/main/specification/loadtestservice/data-plane/readme.md
input-file: https://github.com/Azure/azure-rest-api-specs/blob/loadtest_stableapis/specification/loadtestservice/data-plane/Microsoft.LoadTestService/stable/2022-11-01/loadtestservice.json
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
generate-builder-per-client: false
partial-update: true
output-folder: $(azure-sdk-for-java-folder)/sdk/loadtestservice/azure-developer-loadtesting
service-versions:
- '2022-11-01'
directive:
- from: swagger-document
  where: '$.parameters["TestIdQueryParameter"]'
  transform: >
    $["x-ms-parameter-location"] = "method";
- from: swagger-document
  where: '$.paths.*[?(@.tags=="AppComponent")]'
  transform: >
    $["operationId"] = $["operationId"].replace("AppComponent_", "LoadTestAdministration_").concat("AppComponent");
- from: swagger-document
  where: '$.paths.*[?(@.tags=="ServerMetrics")]'
  transform: >
    $["operationId"] = $["operationId"].replace("ServerMetrics_", "LoadTestAdministration_").concat("ServerMetrics");
- from: swagger-document
  where: '$.paths.*[?(@.tags=="Test")]'
  transform: >
    $["operationId"] = $["operationId"].replace("Test_", "LoadTestAdministration_").concat("Test");
- from: swagger-document
  where: '$.paths.*[?(@.operationId.includes("TestRun_Metric"))]'
  transform: >
    $["operationId"] = $["operationId"].replace("TestRun_Metric", "TestRun_GetMetric");
- rename-operation:
      from: LoadTestAdministration_GetByTestOrTestRunAppComponent
      to: LoadTestAdministration_GetAppComponentByTestOrTestRun
- rename-operation:
      from: LoadTestAdministration_GetByTestOrTestRunServerMetrics
      to: LoadTestAdministration_GetServerMetricsByTestOrTestRun
- rename-operation:
      from: LoadTestAdministration_GetDefaultMetricsServerMetrics
      to: LoadTestAdministration_GetDefaultServerMetrics
- rename-operation:
      from: LoadTestAdministration_ListSupportedResourceTypeServerMetrics
      to: LoadTestAdministration_ListSupportedResourceType
- rename-operation:
      from: LoadTestAdministration_ListTest
      to: LoadTestAdministration_ListTests
- rename-operation:
      from: LoadTestAdministration_UploadFileTest
      to: LoadTestAdministration_UploadFile
- rename-operation:
      from: LoadTestAdministration_GetFileTest
      to: LoadTestAdministration_GetFile
- rename-operation:
      from: LoadTestAdministration_DeleteFileTest
      to: LoadTestAdministration_DeleteFile
- rename-operation:
      from: LoadTestAdministration_GetAllFilesTest
      to: LoadTestAdministration_ListFiles
```
