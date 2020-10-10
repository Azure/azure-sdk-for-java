# Azure Synapse Analytics Spark Service for Java

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
branch: synapse_update_data_plane_specs
repo: https://github.com/idear1203/azure-rest-api-specs/blob/$(branch)
```

```yaml
input-file:
    - $(repo)/specification/synapse/data-plane/Microsoft.Synapse/preview/2019-11-01-preview/sparkJob.json
java: true
output-folder: ..\
generate-client-as-impl: true
generate-sync-async-clients: true
namespace: com.azure.analytics.synapse.spark
generate-client-interfaces: false
sync-methods: all
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
models-subpackage: implementation.models
custom-types-subpackage: models
context-client-method-parameter: true
required-parameter-client-methods: true
```
