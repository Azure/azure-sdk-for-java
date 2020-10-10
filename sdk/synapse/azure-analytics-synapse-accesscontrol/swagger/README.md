# Azure Synapse Analytics Access Control for Java

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

```yaml
branch: master
repo: https://github.com/Azure/azure-rest-api-specs/blob/$(branch)
```

```yaml
input-file:
    - $(repo)/specification/synapse/data-plane/Microsoft.Synapse/preview/2020-02-01-preview/roleAssignments.json
    - $(repo)/specification/synapse/data-plane/Microsoft.Synapse/preview/2020-02-01-preview/roles.json
java: true
output-folder: ../
generate-client-as-impl: true
generate-sync-async-clients: true
namespace: com.azure.analytics.synapse.accesscontrol
generate-client-interfaces: false
sync-methods: all
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
models-subpackage: models
custom-types-subpackage: models
context-client-method-parameter: false
required-parameter-client-methods: true
```
