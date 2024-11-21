# Azure App Configuration for Java

> see https://aka.ms/autorest

This is the AutoRest configuration file for App Configuration.

---
## Getting Started
To build the SDK for App Configuration, simply [Install AutoRest](https://aka.ms/autorest) and
in this folder, run:

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

## Configuration
```yaml
namespace: com.azure.data.appconfiguration
input-file: 
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/c1af3ab8e803da2f40fc90217a6d023bc13b677f/specification/appconfiguration/data-plane/Microsoft.AppConfiguration/stable/2023-11-01/appconfiguration.json
models-subpackage: implementation.models
custom-types-subpackage: models
custom-types: SettingFields,ConfigurationSettingsFilter,CompositionType,SnapshotComposition,ConfigurationSnapshot,ConfigurationSnapshotStatus,SnapshotFields,SettingLabel,LabelFields,SettingLabelFields
customization-class: src/main/java/AppConfigCustomization.java
```

## Code Generation 
```yaml
output-folder: ..\
java: true
use: '@autorest/java@4.1.29'
enable-sync-stack: true
generate-client-interfaces: false
generate-client-as-impl: true
service-interface-as-public: true
required-fields-as-ctor-args: true
license-header: MICROSOFT_MIT_SMALL
disable-client-builder: true
add-context-parameter: true
context-client-method-parameter: true
generic-response-type: true
default-http-exception-type: com.azure.core.exception.HttpResponseException
stream-style-serialization: true
```

### Renames enums
```yaml
directive:
  - from: swagger-document
    where: $.definitions.Snapshot.properties.composition_type
    transform: >
      $["x-ms-enum"].name = "SnapshotComposition";
```

### Renames properties
```yaml
directive:
  - from: swagger-document
    where: $.definitions.Snapshot.properties
    transform: >
      $["items_count"]["x-ms-client-name"] = "item_count";
      $["created"]["x-ms-client-name"] = "createdAt"; 
      $["expires"]["x-ms-client-name"] = "expiresAt";
      $["size"]["x-ms-client-name"] = "sizeInBytes";
      $["etag"]["x-ms-client-name"] = "eTag";
      $["composition_type"]["x-ms-client-name"] = "snapshotComposition";
      $["status"]["x-ms-enum"].name = "ConfigurationSnapshotStatus";
  - from: swagger-document
    where: $.definitions.SnapshotUpdateParameters.properties
    transform: >
      $["status"]["x-ms-enum"].name = "ConfigurationSnapshotStatus";
```

### Renames
```yaml
directive:
  - rename-model:
      from: KeyValueFilter
      to: ConfigurationSettingsFilter
  - rename-model:
      from: Snapshot
      to: ConfigurationSnapshot
  - rename-model:
      from: Label
      to: SettingLabel
  - from: swagger-document
    where: $.parameters.KeyValueFields
    transform: >
      $.items["x-ms-enum"].name = "SettingFields";
  - from: swagger-document
    where: $.parameters.Status
    transform: >
      $.items["x-ms-enum"].name = "ConfigurationSnapshotStatus"
  - from: swagger-document
    where: $.parameters.LabelFields
    transform: >
      $.items["x-ms-enum"].name = "SettingLabelFields";
```

### Modify SettingField enums
```yaml
directive:
  - from: swagger-document
    where: $.parameters.KeyValueFields
    transform: >
      $.items["x-ms-enum"].values = [
        {
          "value": "key",
          "name": "key",
          "description": "Populates the 'key' from the service."
        },
        {
          "value": "label",
          "name": "label",
          "description": "Populates the 'label' from the service."
        },
        {
          "value": "value",
          "name": "value",
          "description": "Populates the 'value' from the service."
        },
        {
          "value": "content_type",
          "name": "content_type",
          "description": "Populates the 'content_type' from the service."
        },
        {
          "value": "etag",
          "name": "etag ",
          "description": "Populates the 'etag' from the service."
        },
        {
          "value": "last_modified",
          "name": "last_modified",
          "description": "Populates the 'last_modified' from the service."
        },
        {
          "value": "locked",
          "name": "is_read_only ",
          "description": "Populates the 'locked' from the service."
        },
        {
          "value": "tags",
          "name": "tags",
          "description": "Populates the 'tags' from the service."
        }
      ];
```

### Modify SnapshotField enums
```yaml
directive:
  - from: swagger-document
    where: $.parameters.SnapshotFields
    transform: >
      $.items["x-ms-enum"].values = [
        {
          "value": "name",
          "name": "name",
          "description": "Populates the snapshot 'name' from the service."
        },
        {
          "value": "status",
          "name": "status",
          "description": "Populates the snapshot 'status' from the service."
        },
        {
          "value": "filters",
          "name": "filters",
          "description": "Populates the snapshot 'filters' from the service."
        },
        {
          "value": "composition_type",
          "name": "snapshot_composition",
          "description": "Populates the snapshot 'composition_type' from the service."
        },
        {
          "value": "created",
          "name": "createdAt",
          "description": "Populates the snapshot 'created' from the service."
        },
        {
          "value": "expires",
          "name": "expiresAt",
          "description": "Populates the snapshot 'expires' from the service."
        },
        {
          "value": "retention_period",
          "name": "retention_period",
          "description": "Populates the snapshot 'retention_period' from the service."
        },
        {
          "value": "items_count",
          "name": "item_count ",
          "description": "Populates the snapshot 'items_count' from the service."
        },
        {
          "value": "size",
          "name": "sizeInBytes ",
          "description": "Populates the snapshot 'size' from the service."
        },
        {
          "value": "etag",
          "name": "etag ",
          "description": "Populates the snapshot `etag` from the service."
        },
        {
          "value": "tags",
          "name": "tags",
          "description": "Populates the snapshot `tags` from the service."
        }
      ];
```
