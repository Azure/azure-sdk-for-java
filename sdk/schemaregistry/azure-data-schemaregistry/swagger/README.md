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
```yaml
use: '@autorest/java@4.1.19'
input-file: https://github.com/Azure/azure-rest-api-specs/blob/84d7b8f05a60d12922341578434b512540563850/specification/schemaregistry/data-plane/Microsoft.EventHub/stable/2023-07-01/schemaregistry.json
java: true
output-folder: ../
namespace: com.azure.data.schemaregistry
generate-client-as-impl: true
service-interface-as-public: true
enable-sync-stack: true
generic-response-type: true
license-header: MICROSOFT_MIT_SMALL
context-client-method-parameter: true
models-subpackage: implementation.models
```

### Add Content-Type header to GetById operation

```yaml
directive:
    from: swagger-document
    where: $.paths["/$schemaGroups/{groupName}/schemas/{schemaName}:get-id"].post
    transform: >
        $.parameters.push({
          "name": "Content-Type",
          "in": "header",
          "description": "Content type of the schema.",
          "required": true,
          "type": "string",
          "enum": [
            "application/json; serialization=Avro",
            "application/json; serialization=Json",
            "text/plain; charset=utf-8"
          ],
          "x-ms-enum": {
            "name": "SchemaFormat",
            "modelAsString": true
           }});
```

### Add Content-Type header to Register operation

```yaml
directive:
    from: swagger-document
    where: $.paths["/$schemaGroups/{groupName}/schemas/{schemaName}"].put
    transform: >
        $.parameters.push({
          "name": "Content-Type",
          "in": "header",
          "description": "Content type of the schema.",
          "required": true,
          "type": "string"});
```

### Enrich Content-Type header in response headers for operations returning the schema

```yaml
directive:
    from: swagger-document
    where: $.paths["/$schemaGroups/$schemas/{id}"].get.responses["200"].headers
    transform: >
        $["Content-Type"]["enum"] = [
            "application/json; serialization=Avro",
            "application/json; serialization=Json",
            "text/plain; charset=utf-8"
           ];
        $["Content-Type"]["x-ms-enum"] = {
          "name": "SchemaFormat",
          "modelAsString": true
        };  
```

```yaml
directive:
    from: swagger-document
    where: $.paths["/$schemaGroups/{groupName}/schemas/{schemaName}/versions/{schemaVersion}"].get.responses["200"].headers
    transform: >
        $["Content-Type"]["enum"] = [
            "application/json; serialization=Avro",
            "application/json; serialization=Json",
            "text/plain; charset=utf-8"
           ];
        $["Content-Type"]["x-ms-enum"] = {
          "name": "SchemaFormat",
          "modelAsString": true
        };  
```

