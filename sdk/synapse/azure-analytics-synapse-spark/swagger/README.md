# Azure Synapse Analytics Spark Service for Java

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
autorest --java --use=C:/work/autorest.java
```

### Code generation settings
```yaml
branch: bee724836ffdeb5458274037dc75f4d43576b5e3
repo: https://github.com/Azure/azure-rest-api-specs/blob/$(branch)
```

```yaml
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
require: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/$(branch)/specification/synapse/data-plane/readme.md
tag: package-spark-2020-12-01
```

### Add x-ms-client-default to livyApiVersion
```yaml
directive:
- from: swagger-document
  where: $.parameters.LivyApiVersion
  transform: >
    $["x-ms-client-default"] = $.default;
```
