## Generate autorest code

```yaml
input-files:
- /home/vsts/work/1/s/azure-rest-api-specs/specification/cognitiveservices/data-plane/FormRecognizer/preview/2022-01-30-preview/FormRecognizer.json
output-folder: ../
java: true
regenerate-pom: false
partial-update: true
generate-sync-async-clients: true
generate-client-as-impl: true
generate-client-interfaces: false
generate-builder-per-client: true
add-context-parameter: true
artifact-id: azure-cognitiveservices-formrecognizer
low-level-client: true
sync-methods: all
generate-samples: true
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.cognitiveservices.formrecognizer
context-client-method-parameter: true
azure-arm: false
service-versions:
- 2022-01-30-preview
```
