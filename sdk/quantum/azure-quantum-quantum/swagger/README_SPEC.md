## Generate autorest code

```yaml
input-file:
  - /home/vsts/work/1/s/azure-rest-api-specs/specification/quantum/data-plane/Microsoft.Quantum/preview/2021-11-01-preview/quantum.json
java: true
output-folder: ../
partial-update: true
regenerate-pom: false
generate-sync-async-clients: true
generate-client-as-impl: true
generate-client-interfaces: false
add-context-parameter: true
artifact-id: azure-quantum-quantum
low-level-client: true
sync-methods: all
generate-samples: true
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.quantum.quantum
context-client-method-parameter: true
azure-arm: false
credential-types: tokencredential
credential-scopes: https://quantum.azure.com/.default
service-versions:
  - '2021-11-01-preview'
```
