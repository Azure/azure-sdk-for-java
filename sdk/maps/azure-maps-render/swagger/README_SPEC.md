## Generate autorest code

```yaml
input-file:
  - /home/vsts/work/1/s/azure-rest-api-specs/specification/maps/data-plane/Render/preview/1.0/render.json
java: true
output-folder: ../
partial-update: true
regenerate-pom: false
generate-sync-async-clients: true
generate-client-as-impl: true
generate-client-interfaces: false
add-context-parameter: true
artifact-id: azure-maps-render
low-level-client: true
sync-methods: all
generate-samples: true
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.maps.render
context-client-method-parameter: true
azure-arm: false
credential-types: tokencredential
credential-scopes: https://maps.azure.com/.default
service-versions:
  - '1.0'
```
