## Generate autorest code

```yaml
require: https://github.com/Azure/azure-rest-api-specs/blob/b9f54e34813eb04f86d8868a55de795699e6ada5/specification/loadtestservice/data-plane/readme.md
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
service-versions:
- '2022-11-01'
directive:
- from: swagger-document 
  where: $["paths"]["/tests/{testId}"].patch
  transform: $["operationId"] = "Test_CreateOrUpdateTest";
- from: swagger-document 
  where: $["paths"]["/tests/{testId}"].delete
  transform: $["operationId"] = "Test_DeleteTest";
- from: swagger-document 
  where: $["paths"]["/tests/{testId}"].get
  transform: $["operationId"] = "Test_GetTest";
- from: swagger-document 
  where: $["paths"]["/tests"].get
  transform: $["operationId"] = "Test_ListTests";
- from: swagger-document 
  where: $["paths"]["/tests/{testId}/files/{fileName}"].put
  transform: $["operationId"] = "Test_UploadTestFile";
- from: swagger-document 
  where: $["paths"]["/tests/{testId}/files/{fileName}"].get
  transform: $["operationId"] = "Test_GetTestFile";
- from: swagger-document 
  where: $["paths"]["/tests/{testId}/files/{fileName}"].delete
  transform: $["operationId"] = "Test_DeleteTestFile";
- from: swagger-document 
  where: $["paths"]["/tests/{testId}/files"].get
  transform: $["operationId"] = "Test_ListTestFiles";
- from: swagger-document 
  where: $["paths"]["/test-runs/{testRunId}"].delete
  transform: $["operationId"] = "TestRun_DeleteTestRun";
- from: swagger-document 
  where: $["paths"]["/test-runs/{testRunId}"].patch
  transform: $["operationId"] = "TestRun_CreateOrUpdateTestRun";
- from: swagger-document 
  where: $["paths"]["/test-runs/{testRunId}"].get
  transform: $["operationId"] = "TestRun_GetTestRun";
- from: swagger-document 
  where: $["paths"]["/test-runs/{testRunId}/files/{fileName}"].get
  transform: $["operationId"] = "TestRun_GetTestRunFile";
- from: swagger-document 
  where: $["paths"]["/test-runs"].get
  transform: $["operationId"] = "TestRun_ListTestRuns";
- from: swagger-document 
  where: $["paths"]["/test-runs/{testRunId}:stop"].post
  transform: $["operationId"] = "TestRun_StopTestRun";

- from: swagger-document
  where: '$.paths.*[?(@.tags=="Test")]'
  transform: >
    $["operationId"] = $["operationId"].replace("Test_", "LoadTestAdministration_");
- from: swagger-document
  where: '$.paths.*[?(@.tags=="TestRun")]'
  transform: >
    $["operationId"] = $["operationId"].replace("TestRun_", "LoadTestRun_");
```
