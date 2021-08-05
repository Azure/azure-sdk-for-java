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
branch: 3d6211cf28f83236cdf78e7cfc50efd3fb7cba72
repo: https://github.com/Azure/azure-rest-api-specs/blob/$(branch)
```

```yaml
input-file:
    - $(repo)/specification/synapse/data-plane/Microsoft.Synapse/stable/2020-12-01/sparkJob.json
java: true
output-folder: ..\
generate-client-as-impl: true
generate-sync-async-clients: true
namespace: com.azure.analytics.synapse.spark
generate-client-interfaces: false
sync-methods: all
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
models-subpackage: models
context-client-method-parameter: true
required-parameter-client-methods: true
credential-types: tokencredential
credential-scopes: https://dev.azuresynapse.net/.default
```
