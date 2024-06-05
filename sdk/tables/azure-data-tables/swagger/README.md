# Azure Cosmos Table

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
use: '@autorest/java@4.1.29'
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/58767be9a357c436ee99706edda49e9c4a8a4e05/specification/cosmos-db/data-plane/Microsoft.Tables/preview/2019-02-02/table.json
java: true
output-folder: ..\
generate-client-as-impl: true
namespace: com.azure.data.tables
generate-client-interfaces: false
enable-sync-stack: true
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
models-subpackage: implementation.models
context-client-method-parameter: true
service-interface-as-public: true
custom-strongly-typed-header-deserialization: true
generic-response-type: true
disable-client-builder: true
stream-style-serialization: true
```

### Add JSON-based Error Type

```yaml
directive:
- from: swagger-document
  where: $.definitions
  transform: >
    $.TableServiceJsonError = {
      "description": "Table service error.",
      "type": "object",
      "properties": {
        "odata.error": {
          "description": "The service error.",
          "$ref": "#/definitions/TableServiceOdataError"
        }
      }
    };
    
    $.TableServiceOdataError = {
      "description": "Table service OData error.",
      "type": "object",
      "properties": {
        "code": {
          "description": "The service error code.",
          "type": "string"
        },
        "message": {
          "description": "The service error message.",
          "$ref": "#/definitions/TableServiceOdataErrorMessage"
        }
      }
    };

    $.TableServiceOdataErrorMessage = {
      "description": "The service OData error message.",
      "type": "object",
      "properties": {
        "lang": {
          "description": "Language code of the error message.",
          "type": "string"
        },
        "value": {
          "description": "The error message",
          "type": "string"
        }
      }
    };
```

```yaml
directive:
- from: swagger-document
  where: $.paths
  transform: >
    let defaultResponse = {
      "description": "Failure",
      "schema": {
        "$ref": "#/definitions/TableServiceJsonError"
      }
    };
    
    $["/Tables"].get.responses.default = defaultResponse;
    $["/Tables"].post.responses.default = defaultResponse;
    $["/Tables('{table}')"].delete.responses.default = defaultResponse;
    $["/{table}()"].get.responses.default = defaultResponse;
    $["/{table}(PartitionKey='{partitionKey}',RowKey='{rowKey}')"].get.responses.default = defaultResponse;
    $["/{table}(PartitionKey='{partitionKey}',RowKey='{rowKey}')"].put.responses.default = defaultResponse;
    $["/{table}(PartitionKey='{partitionKey}',RowKey='{rowKey}')"].patch.responses.default = defaultResponse;
    $["/{table}(PartitionKey='{partitionKey}',RowKey='{rowKey}')"].delete.responses.default = defaultResponse;
    $["/{table}"].post.responses.default = defaultResponse;
```
