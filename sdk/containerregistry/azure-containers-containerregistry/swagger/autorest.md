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
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/40e8bf1504ed672e86027b240dddd9ca94a15d4c/specification/containerregistry/data-plane/Azure.ContainerRegistry/preview/2019-08-15-preview/containerregistry.json
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
custom-types: ContentProperties,DeleteRepositoryResult,ManifestOrderBy,TagOrderBy,RepositoryProperties,ArtifactArchitecture,ArtifactOperatingSystem
custom-types-subpackage: models
```

### Set readonly flag to properties of DeletedRepository
```yaml
directive:
- from: swagger-document
  where: $.definitions.DeletedRepository
  transform: >
    $["properties"]["manifestsDeleted"].readOnly = true;
    $["properties"]["tagsDeleted"].readOnly = true;
```

### Set readonly flag to properties of RepositoryAttributes
```yaml
directive:
- from: swagger-document
  where: $.definitions.RepositoryAttributes
  transform: >
    $["properties"]["imageName"].readOnly = true;
    $["properties"]["createdTime"].readOnly = true;
    $["properties"]["lastUpdateTime"].readOnly = true;
    $["properties"]["manifestCount"].readOnly = true;
    $["properties"]["tagCount"].readOnly = true;
    $["properties"]["changeableAttributes"].readOnly = true;
```

### Set readonly flag to properties of ManifestAttributesBase
```yaml
directive:
- from: swagger-document
  where: $.definitions.ManifestAttributesBase
  transform: >
    $["properties"]["digest"].readOnly = true;
    $["properties"]["imageSize"].readOnly = true;
    $["properties"]["createdTime"].readOnly = true;
    $["properties"]["lastUpdateTime"].readOnly = true;
    $["properties"]["architecture"].readOnly = true;
    $["properties"]["os"].readOnly = true;
    $["properties"]["tags"].readOnly = true;
    $["properties"]["changeableAttributes"].readOnly = true;
    $["properties"]["references"].readOnly = true;
```

### Set readonly flag to properties of ManifestAttributes
```yaml
directive:
- from: swagger-document
  where: $.definitions.ManifestAttributes
  transform: >
    $["properties"]["imageName"].readOnly = true;
    $["properties"]["manifest"].readOnly = true;
```

### Set readonly flag to properties of TagAttributesBase
```yaml
directive:
- from: swagger-document
  where: $.definitions.TagAttributesBase
  transform: >
    $["properties"]["name"].readOnly = true;
    $["properties"]["digest"].readOnly = true;
    $["properties"]["createdTime"].readOnly = true;
    $["properties"]["lastUpdateTime"].readOnly = true;
    $["properties"]["changeableAttributes"].readOnly = true;
```

### Set readonly flag to properties of TagAttributes
```yaml
directive:
- from: swagger-document
  where: $.definitions.TagAttributes
  transform: >
    $["properties"]["imageName"].readOnly = true;
    $["properties"]["tag"].readOnly = true;
```

### Set modelAsString flag for the enum values of TagOrderBy
```yaml
directive:
- from: swagger-document
  where: $.definitions.TagOrderBy
  transform: >
    $["x-ms-enum"].modelAsString = true;
```

### Set modelAsString flag for the enum values of ManifestOrderBy
```yaml
directive:
- from: swagger-document
  where: $.definitions.ManifestOrderBy
  transform: >
    $["x-ms-enum"].modelAsString = true;
```


