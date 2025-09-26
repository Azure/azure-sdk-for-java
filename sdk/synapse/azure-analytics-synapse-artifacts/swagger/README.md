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
use: '@autorest/java@4.1.52'
output-folder: ..\
generate-client-as-impl: true
generate-sync-async-clients: true
namespace: com.azure.analytics.synapse.artifacts
artifact-id: azure-analytics-synapse-artifacts
sync-methods: all
license-header: MICROSOFT_MIT_SMALL
models-subpackage: models
required-parameter-client-methods: true
security: AADToken
security-scopes: https://dev.azuresynapse.net/.default
require: https://github.com/Azure/azure-rest-api-specs/blob/222af3670e36c5083cb0dc8a9c2677a8f77f8958/specification/synapse/data-plane/readme.md
tag: package-artifacts-composite-v7
```
