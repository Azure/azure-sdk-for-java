## Generate autorest code

```yaml
input-file:
  - /home/vsts/work/1/s/azure-rest-api-specs/specification/deviceupdate/data-plane/Microsoft.DeviceUpdate/preview/2021-06-01-preview/deviceupdate.json
java: true
output-folder: ../
partial-update: true
regenerate-pom: false
generate-sync-async-clients: true
generate-client-as-impl: true
generate-client-interfaces: false
add-context-parameter: true
artifact-id: azure-deviceupdate-deviceupdate
low-level-client: true
sync-methods: all
generate-samples: true
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.deviceupdate.deviceupdate
context-client-method-parameter: true
azure-arm: false
credential-types: tokencredential
credential-scopes: https://deviceupdate.azure.com/.default
service-versions:
  - '2021-06-01-preview'
```
