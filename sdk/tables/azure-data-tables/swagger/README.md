# Azure Cosmos Table

> see https://aka.ms/autorest

### Setup

```ps
npm install -g autorest
```

### Generation

```ps
cd <swagger-folder>
autorest
```

### Code generation settings

```yaml
use: '@autorest/java@4.1.15'
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/58767be9a357c436ee99706edda49e9c4a8a4e05/specification/cosmos-db/data-plane/Microsoft.Tables/preview/2019-02-02/table.json
java: true
output-folder: ..\
generate-client-as-impl: true
namespace: com.azure.data.tables
generate-client-interfaces: false
enable-sync-stack: true
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
models-subpackage: implementation.models
context-client-method-parameter: true
service-interface-as-public: true
custom-strongly-typed-header-deserialization: true
generic-response-type: true
disable-client-builder: true
stream-style-serialization: true
```
