# Azure Monitor Ingestion client library for Java

## Code generation settings

### Manual Modifications

The following edits need to be made manually after code generation:
- Rollback the edits to `module-info` file

```yaml
java: true
use: '@autorest/java@4.1.27'
output-folder: ../
license-header: MICROSOFT_MIT_SMALL
input-file: https://github.com/Azure/azure-rest-api-specs/blob/main/specification/monitor/data-plane/ingestion/stable/2023-01-01/DataCollectionRules.json
namespace: com.azure.monitor.ingestion.implementation
implementation-subpackage: ""
generate-client-interfaces: false
sync-methods: all
add-context-parameter: true
context-client-method-parameter: true
required-parameter-client-methods: false
required-fields-as-ctor-args: true
credential-types: tokencredential
credential-scopes: https://monitor.azure.com//.default
client-side-validations: true
artifact-id: azure-monitor-ingestion
data-plane: true
enable-sync-stack: true
customization-class: src/main/java/MonitorIngestionCustomizations.java
stream-style-serialization: true
```
