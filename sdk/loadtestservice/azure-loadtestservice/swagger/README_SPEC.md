## Generate autorest code

```yaml
input-file:
  - https://github.com/Azure/azure-rest-api-specs/blob/main/specification/loadtestservice/data-plane/Microsoft.LoadTestService/preview/2021-07-01-preview/loadtestservice.json
java: true
output-folder: ../
partial-update: true
regenerate-pom: false
generate-sync-async-clients: true
generate-client-as-impl: true
generate-client-interfaces: false
add-context-parameter: true
low-level-client: true
sync-methods: all
generate-samples: true
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.loadtestservice
context-client-method-parameter: true
azure-arm: false
credential-types: tokencredential
credential-scopes: https://service.azure.com/.default
service-versions:
  - '2021-07-01-preview'
```
