# Azure Schema Registry for Java

> see https://aka.ms/autorest

### Setup

> see https://github.com/Azure/autorest.java

### Generation
> see https://github.com/Azure/autorest.java/releases for the latest version of autorest
```ps
cd <swagger-folder>
mvn install
autorest --java --use:@autorest/java@4.0.x
```

### Code generation settings
``` yaml
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/2f33c13f9d5964781f4b8954bf3909b5039fe129/specification/schemaregistry/data-plane/Microsoft.EventHub/stable/2021-10/schemaregistry.json
java: true
output-folder: ../
namespace: com.azure.data.schemaregistry
generate-client-as-impl: true
sync-methods: none
license-header: MICROSOFT_MIT_SMALL
context-client-method-parameter: true
models-subpackage: implementation.models
```


