# Azure Synapse Analytics Spark Service for Java

> see https://aka.ms/autorest

This is the AutoRest configuration file for Synapse Analytics Spark Service.
---
## Getting Started

To build the SDK for Synapse Analytics Spark Service, simply [Install AutoRest](https://aka.ms/autorest) and in
this folder, run:

> `autorest`
To see additional help and options, run:

> `autorest --help`
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

## Java

```yaml
java: true
output-folder: ..\
use: '@autorest/java@4.1.34'
generate-client-as-impl: true
generate-sync-async-clients: true
namespace: com.azure.analytics.synapse.spark
generate-client-interfaces: false
service-interface-as-public: true
sync-methods: all
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
models-subpackage: models
context-client-method-parameter: true
required-parameter-client-methods: true
credential-types: tokencredential
credential-scopes: https://dev.azuresynapse.net/.default
require: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/bee724836ffdeb5458274037dc75f4d43576b5e3/specification/synapse/data-plane/readme.md
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
