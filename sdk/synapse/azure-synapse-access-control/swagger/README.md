# Azure Synapse - Access Control

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
- https://github.com/Azure/azure-rest-api-specs/tree/master/specification/synapse/data-plane/Microsoft.Synapse/preview/2020-08-01-preview/checkAccessSynapseRbac.json
- https://github.com/Azure/azure-rest-api-specs/tree/master/specification/synapse/data-plane/Microsoft.Synapse/preview/2020-08-01-preview/roleDefinitions.json
- https://github.com/Azure/azure-rest-api-specs/tree/master/specification/synapse/data-plane/Microsoft.Synapse/preview/2020-08-01-preview/roleAssignments.json
java: true
output-folder: ..\
required-parameter-client-methods: true
generate-client-as-impl: true
namespace: com.azure.synapse.accesscontrol
generate-client-interfaces: false
sync-methods: all
generate-sync-async-clients: true
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
context-client-method-parameter: true
credential-types: tokencredential
credential-scopes: https://dev.azuresynapse.net/.default
```
