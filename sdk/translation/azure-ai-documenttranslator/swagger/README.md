## Generate autorest code
``` yaml
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/specification/cognitiveservices/data-plane/TranslatorText/stable/v1.0/TranslatorBatch.json
use: '@autorest/java@4.1.52'
java: true
output-folder: ../
namespace: com.azure.ai.documenttranslator
generate-client-interfaces: false
sync-methods: all
license-header: MICROSOFT_MIT_SMALL
data-plane: true
credential-types: AzureKey
```
