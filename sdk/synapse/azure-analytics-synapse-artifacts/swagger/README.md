# Azure Synapse Analytics Orchestration Service for Java

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
branch: main
repo: https://github.com/Azure/azure-rest-api-specs/blob/$(branch)
```

```yaml
java: true
use: '@autorest/java@4.1.29'
output-folder: ..\
generate-client-as-impl: true
generate-sync-async-clients: true
namespace: com.azure.analytics.synapse.artifacts
artifact-id: azure-analytics-synapse-artifacts
generate-client-interfaces: false
service-interface-as-public: true
sync-methods: all
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
models-subpackage: models
context-client-method-parameter: true
required-parameter-client-methods: true
security: AADToken
security-scopes: https://dev.azuresynapse.net/.default
custom-strongly-typed-header-deserialization: true
model-override-setter-from-superclass: true
require: https://github.com/Azure/azure-rest-api-specs/blob/02e1bd495ad6215aacec5d636a5ef7ad6f20281e/specification/synapse/data-plane/readme.md
tag: package-artifacts-composite-v7
```
