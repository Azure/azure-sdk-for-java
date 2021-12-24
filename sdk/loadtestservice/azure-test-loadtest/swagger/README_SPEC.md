## Generate autorest code

```yaml
input-file:
  - https://github.com/Azure/azure-rest-api-specs/blob/main/specification/loadtestservice/data-plane/Microsoft.LoadTestService/preview/2021-07-01-preview/loadtestservice.json
java: true
output-folder: ../
partial-update: true
regenerate-pom: false
title: LoadTestClient
generate-sync-async-clients: true
generate-client-as-impl: true
generate-client-interfaces: false
add-context-parameter: true
artifact-id: azure-test-loadtest
low-level-client: true
sync-methods: all
generate-samples: true
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.test.loadtest
context-client-method-parameter: true
azure-arm: false
credential-types: tokencredential
credential-scopes: https://loadtest.azure.com/.default
service-versions:
  - '2021-07-01-preview'
```
