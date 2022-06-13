# Azure Logs Ingestion client library for Java

## Code generation settings

```yaml
java: true
use: '@autorest/java@4.0.61'
output-folder: ../
license-header: MICROSOFT_MIT_SMALL
input-file: https://github.com/Azure/azure-rest-api-specs/blob/main/specification/monitor/data-plane/ingestion/preview/2021-11-01-preview/DataCollectionRules.json
namespace: com.azure.monitor.ingestion
generate-client-interfaces: false
sync-methods: all
add-context-parameter: true
context-client-method-parameter: true
required-parameter-client-methods: false
generate-client-as-impl: true
models-subpackage: implementation.models
required-fields-as-ctor-args: true
model-override-setter-from-superclass: true
credential-types: tokencredential
client-side-validations: true
artifact-id: azure-monitor-logingestion
data-plane: true
```
