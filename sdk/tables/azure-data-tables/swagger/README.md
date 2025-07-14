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
use: '@autorest/java@4.1.52'
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/58767be9a357c436ee99706edda49e9c4a8a4e05/specification/cosmos-db/data-plane/Microsoft.Tables/preview/2019-02-02/table.json
java: true
output-folder: ..\
generate-client-as-impl: true
namespace: com.azure.data.tables
enable-sync-stack: true
license-header: MICROSOFT_MIT_SMALL
models-subpackage: implementation.models
custom-types: TableAccessPolicy,TableServiceCorsRule,TableServiceGeoReplication,TableServiceGeoReplicationStatus,TableServiceLogging,TableServiceMetrics,TableServiceProperties,TableServiceRetentionPolicy,TableServiceStatistics,TableSignedIdentifier
custom-types-subpackage: models
customization-class: src/main/java/TablesCustomization.java
disable-client-builder: true
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

### Map to remove translation layer

Modifications to `AccessPolicy` to generate in the shape of `TableAccessPolicy`.

```yaml
directive:
- rename-model:
    from: AccessPolicy
    to: TableAccessPolicy
- from: swagger-document
  where: $.definitions.TableAccessPolicy
  transform: >
    $.properties.Start["x-ms-client-name"] = "startsOn";
    $.properties.Expiry["x-ms-client-name"] = "expiresOn";
    $.properties.Permission["x-ms-client-name"] = "permissions";
```

Modifications to `SignedIdentifier` to generate in the shape of `TableSignedIdentifier`.

```yaml
directive:
- rename-model:
    from: SignedIdentifier
    to: TableSignedIdentifier
```

Modifications to `GeoReplication` to generate in the shape of `TableServiceGeoReplication`.

```yaml
directive:
- rename-model:
    from: GeoReplication
    to: TableServiceGeoReplication
- from: swagger-document
  where: $.definitions.TableServiceGeoReplication
  transform: >
    $.properties.Status["x-ms-enum"].name = "TableServiceGeoReplicationStatus";
```

Modifications to `TableServiceStats` to generate in the shape of `TableServiceStatistics`.

```yaml
directive:
- rename-model:
    from: TableServiceStats
    to: TableServiceStatistics
```

Modifications to `RetentionPolicy` to generate in the shape of `TableServiceRetentionPolicy`.

```yaml
directive:
- rename-model:
    from: RetentionPolicy
    to: TableServiceRetentionPolicy
- from: swagger-document
  where: $.definitions.TableServiceRetentionPolicy
  transform: >
    $.properties.Days["x-ms-client-name"] = "daysToRetain";
```

Modifications to `Metrics` to generate in the shape of `TableServiceMetrics`.

```yaml
directive:
- rename-model:
    from: Metrics
    to: TableServiceMetrics
- from: swagger-document
  where: $.definitions.TableServiceMetrics
  transform: >
    $.properties.IncludeAPIs["x-ms-client-name"] = "includeApis";
```

Modifications to `Logging` to generate in the shape of `TableServiceLogging`.

```yaml
directive:
- rename-model:
    from: Logging
    to: TableServiceLogging
- from: swagger-document
  where: $.definitions.TableServiceLogging
  transform: >
    $.properties.Version["x-ms-client-name"] = "analyticsVersion";
    $.properties.Delete["x-ms-client-name"] = "deleteLogged";
    $.properties.Read["x-ms-client-name"] = "readLogged";
    $.properties.Write["x-ms-client-name"] = "writeLogged";
```

Modifications to `CorsRule` to generate in the shape of `TableServiceCorsRule`.

```yaml
directive:
- rename-model:
    from: CorsRule
    to: TableServiceCorsRule
```
