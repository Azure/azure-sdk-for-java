## Generate autorest code
``` yaml
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/cognitiveservices/data-plane/TranslatorText/preview/v1.0-preview.1/TranslatorBatch.json
java: true
output-folder: ../
namespace: com.azure.ai.documenttranslator
generate-client-interfaces: false
sync-methods: none
license-header: MICROSOFT_MIT_SMALL
low-level-client: true
credential-types: azurekeycredential
```
