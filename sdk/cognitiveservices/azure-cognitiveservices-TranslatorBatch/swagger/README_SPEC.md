## Generate autorest code

```yaml
input-file:
  - /home/vsts/work/1/s/azure-rest-api-specs/specification/cognitiveservices/data-plane/TranslatorText/stable/v1.1/TranslatorBatch.json
java: true
output-folder: ../
partial-update: true
regenerate-pom: false
generate-sync-async-clients: true
generate-client-as-impl: true
generate-client-interfaces: false
add-context-parameter: true
artifact-id: azure-cognitiveservices-TranslatorBatch
low-level-client: true
sync-methods: all
generate-samples: true
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.cognitiveservices.TranslatorBatch
context-client-method-parameter: true
azure-arm: false
credential-types: tokencredential
credential-scopes: https://cognitiveservices.azure.com/.default
service-versions:
  - 'v1.1'
```
