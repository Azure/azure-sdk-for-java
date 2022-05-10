# Azure Containers ContainerRegistry APIs for Java

> see https://aka.ms/autorest

## Getting Started

To build the client SDK for ContainerRegistry simply [Install AutoRest](https://github.com/Azure/autorest/blob/master/docs/install/readme.md) and in this folder, run:

### Setup
```ps
You need to have the following installed on your machine:

Node.JS v10.x - v13.x
Java 8+
Maven 3.x
You need to have autorest-beta installed through NPM:

npm i -g autorest
```

### Generation

There is one swagger for Container Registry APIs.

```ps
cd <swagger-folder>
autorest --java --use:@autorest/java@4.0.x
```

### Code generation settings
``` yaml
input-file: https://github.com/Azure/azure-rest-api-specs/blob/c8d9a26a2857828e095903efa72512cf3a76c15d/specification/containerregistry/data-plane/Azure.ContainerRegistry/stable/2021-07-01/containerregistry.json
java: true
output-folder: ./..
generate-client-as-impl: true
namespace: com.azure.containers.containerregistry
generate-client-interfaces: false
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
context-client-method-parameter: true
service-interface-as-public: true
models-subpackage: implementation.models
custom-types: ArtifactManifestOrderBy,ArtifactTagOrderBy,ArtifactArchitecture,ArtifactOperatingSystem,ArtifactManifestPlatform,RepositoryProperties,ContainerRepositoryProperties
custom-types-subpackage: models
```

### Set modelAsString flag for the enum values of ArtifactTagOrderBy
```yaml
directive:
- from: swagger-document
  where: $.definitions.TagOrderBy
  transform: >
    $["x-ms-enum"].name = ArtifactTagOrder
    $["x-ms-enum"].modelAsString = true;
```

### Set modelAsString flag for the enum values of ArtifactManifestOrderBy
```yaml
directive:
- from: swagger-document
  where: $.definitions.ManifestOrderBy
  transform: >
    $["x-ms-enum"].name = ArtifactManifestOrder
    $["x-ms-enum"].modelAsString = true;
```

### Update the field names for RepositoryChangeableAttributes
```yaml
directive:
- from: swagger-document
  where: $.definitions.RepositoryChangeableAttributes
  transform: >
    $["properties"]["deleteEnabled"]["x-ms-client-name"] = "deleteEnabled";
    $["properties"]["writeEnabled"]["x-ms-client-name"] = "writeEnabled";
    $["properties"]["listEnabled"]["x-ms-client-name"] = "listEnabled";
    $["properties"]["readEnabled"]["x-ms-client-name"] = "readEnabled";
```

### Delete Quarantine fields from the manifest attributes.
```yaml
directive:
- from: swagger-document
  where: $.definitions.ManifestChangeableAttributes
  transform: >
    $["properties"]["deleteEnabled"]["x-ms-client-name"] = "deleteEnabled";
    $["properties"]["writeEnabled"]["x-ms-client-name"] = "writeEnabled";
    $["properties"]["listEnabled"]["x-ms-client-name"] = "listEnabled";
    $["properties"]["readEnabled"]["x-ms-client-name"] = "readEnabled";
```

### Set readonly flag to properties of ManifestAttributesBase
```yaml
directive:
- from: swagger-document
  where: $.definitions.ManifestAttributesBase
  transform: >
      delete  $["properties"]["configMediaType"];
```

### Set readonly flag to properties of TagAttributesBase
```yaml
directive:
- from: swagger-document
  where: $.definitions.TagAttributesBase
  transform: >
      delete  $["properties"]["signed"];
```

### Update the field names for TagChangeableAttributes
```yaml
directive:
- from: swagger-document
  where: $.definitions.TagChangeableAttributes
  transform: >
    $["properties"]["deleteEnabled"]["x-ms-client-name"] = "deleteEnabled";
    $["properties"]["writeEnabled"]["x-ms-client-name"] = "writeEnabled";
    $["properties"]["listEnabled"]["x-ms-client-name"] = "listEnabled";
    $["properties"]["readEnabled"]["x-ms-client-name"] = "readEnabled";
```


