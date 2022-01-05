## Generate autorest code

```yaml
input-file:
  - ../azure-rest-api-specs/specification/cognitiveservices/data-plane/Face/stable/v1.0/Face.json
java: true
output-folder: ../
partial-update: true
regenerate-pom: false
generate-sync-async-clients: true
generate-client-as-impl: true
generate-client-interfaces: false
add-context-parameter: true
artifact-id: azure-cognitiveservices-Face
low-level-client: true
sync-methods: all
generate-samples: true
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.cognitiveservices.Face
context-client-method-parameter: true
azure-arm: false
credential-types: tokencredential
credential-scopes: https://cognitiveservices.azure.com/.default
service-versions:
  - 1.0-preview
```
