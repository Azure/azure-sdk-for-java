# Azure Synapse - Artifacts

> see https://aka.ms/autorest

### Setup
```ps
Fork and clone https://github.com/Azure/autorest.java 
git checkout v4
git submodule update --init --recursive
mvn package -Dlocal
npm install
npm install -g autorest
```

### Generation
```ps
cd <swagger-folder>
autorest --java --use=C:/work/autorest.java
```

### Code generation settings
``` yaml
input-file:
- https://github.com/Azure/azure-rest-api-specs/tree/master/specification/synapse/data-plane/Microsoft.Synapse/preview/2019-06-01-preview/artifacts.json
- https://github.com/Azure/azure-rest-api-specs/tree/master/specification/synapse/data-plane/Microsoft.Synapse/preview/2019-06-01-preview/workspace.json
- https://github.com/Azure/azure-rest-api-specs/tree/master/specification/synapse/data-plane/Microsoft.Synapse/preview/2019-06-01-preview/sqlPools.json
- https://github.com/Azure/azure-rest-api-specs/tree/master/specification/synapse/data-plane/Microsoft.Synapse/preview/2019-06-01-preview/bigDataPools.json
- https://github.com/Azure/azure-rest-api-specs/tree/master/specification/synapse/data-plane/Microsoft.Synapse/preview/2019-06-01-preview/integrationRuntimes.json
- https://github.com/Azure/azure-rest-api-specs/tree/master/specification/synapse/data-plane/Microsoft.Synapse/preview/2019-06-01-preview/entityTypes/DataFlow.json
- https://github.com/Azure/azure-rest-api-specs/tree/master/specification/synapse/data-plane/Microsoft.Synapse/preview/2019-06-01-preview/entityTypes/Dataset.json
- https://github.com/Azure/azure-rest-api-specs/tree/master/specification/synapse/data-plane/Microsoft.Synapse/preview/2019-06-01-preview/entityTypes/LinkedService.json
- https://github.com/Azure/azure-rest-api-specs/tree/master/specification/synapse/data-plane/Microsoft.Synapse/preview/2019-06-01-preview/entityTypes/Notebook.json
- https://github.com/Azure/azure-rest-api-specs/tree/master/specification/synapse/data-plane/Microsoft.Synapse/preview/2019-06-01-preview/entityTypes/Pipeline.json
- https://github.com/Azure/azure-rest-api-specs/tree/master/specification/synapse/data-plane/Microsoft.Synapse/preview/2019-06-01-preview/entityTypes/SparkJobDefinition.json
- https://github.com/Azure/azure-rest-api-specs/tree/master/specification/synapse/data-plane/Microsoft.Synapse/preview/2019-06-01-preview/entityTypes/SqlScript.json
- https://github.com/Azure/azure-rest-api-specs/tree/master/specification/synapse/data-plane/Microsoft.Synapse/preview/2019-06-01-preview/entityTypes/Trigger.json
- https://github.com/Azure/azure-rest-api-specs/tree/master/specification/synapse/data-plane/Microsoft.Synapse/preview/2019-06-01-preview/gitintegration.json
java: true
output-folder: ..\
required-parameter-client-methods: true
generate-client-as-impl: true
namespace: com.azure.synapse.artifacts
generate-client-interfaces: false
sync-methods: all
generate-sync-async-clients: true
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
context-client-method-parameter: true
credential-types: tokencredential
credential-scopes: https://dev.azuresynapse.net/.default
```

### Blob Event Types
```yaml
directive:
- from: swagger-document
  where: $.definitions.BlobEventTypes
  transform: >
    $.items["x-ms-enum"]["name"] = "BlobEventType";
```