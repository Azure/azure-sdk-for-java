# Azure Cosmos Table

> see https://aka.ms/autorest

### Setup
```ps
Fork and clone https://github.com/Azure/autorest.java
git checkout main
git submodule update --init --recursive
mvn package -Dlocal
npm install
npm install -g autorest
```

### Generation
```ps
cd <swagger-folder>
autorest
```

### Code generation settings
```yaml
use: '@autorest/java@4.1.2'
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/specification/cosmos-db/data-plane/Microsoft.Tables/preview/2019-02-02/table.json
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
enable-xml: true
custom-strongly-typed-header-deserialization: true
generic-response-type: true
```
