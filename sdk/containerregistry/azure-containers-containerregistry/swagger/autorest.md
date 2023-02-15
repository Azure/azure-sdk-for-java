# Azure Containers ContainerRegistry APIs for Java

> see https://aka.ms/autorest

## Getting Started

To build the client SDK for ContainerRegistry simply [Install AutoRest](https://github.com/Azure/autorest/blob/master/docs/install/readme.md) and in this folder, run:

### Setup
```ps
You need to have the following installed on your machine:

Node.JS LTS
Java 8+
Maven 3.x
You need to have autorest installed through NPM:

npm i -g autorest
```

### Generation

There is one swagger for Container Registry APIs.

```ps
cd <swagger-folder>
autorest --java --use:@autorest/java@4.1.7 --use:@autorest/modelerfour@4.23.7
```

### Code generation settings
``` yaml
use: '@autorest/java@4.1.13'
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/c8d9a26a2857828e095903efa72512cf3a76c15d/specification/containerregistry/data-plane/Azure.ContainerRegistry/stable/2021-07-01/containerregistry.json
java: true
output-folder: ./..
generate-client-as-impl: true
namespace: com.azure.containers.containerregistry
generate-client-interfaces: false
license-header: MICROSOFT_MIT_SMALL
sync-methods: none
context-client-method-parameter: true
service-interface-as-public: true
models-subpackage: implementation.models
custom-types: ArtifactArchitecture,ArtifactManifestOrder,ArtifactManifestPlatform,ArtifactOperatingSystem,ArtifactTagOrder,ContainerRepositoryProperties,OciAnnotations,OciBlobDescriptor,OciManifest,RepositoryProperties
custom-types-subpackage: models
enable-sync-stack: true
generic-response-type: true
disable-client-builder: true
stream-style-serialization: true
```

### Set modelAsString flag for the enum values of ArtifactTagOrderBy
```yaml
directive:
- from: swagger-document
  where: $.definitions.TagOrderBy
  transform: >
    $["x-ms-enum"].name = "ArtifactTagOrder";
    $["x-ms-enum"].modelAsString = true;
```

### Set modelAsString flag for the enum values of ArtifactManifestOrderBy
```yaml
directive:
- from: swagger-document
  where: $.definitions.ManifestOrderBy
  transform: >
    $["x-ms-enum"].name = "ArtifactManifestOrder";
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

### Remove readonly for ManifestAttributes
```yaml
directive:
- from: swagger-document
  where: $.definitions.ManifestAttributes
  transform: >
    $["x-ms-client-name"] = "ArtifactManifestPropertiesInternal";
    delete $.properties.registry.readOnly;
    delete $.properties.imageName.readOnly;
    delete $.properties.manifest.readOnly;
```

### Set readonly flag to properties of ManifestAttributesBase and rename size to sizeInBytes
```yaml
directive:
- from: swagger-document
  where: $.definitions.ManifestAttributesBase
  transform: >
    delete $.properties.configMediaType;
    $.properties.imageSize["x-ms-client-name"] = "sizeInBytes";
    delete $.properties.digest.readOnly;
    delete $.properties.imageSize.readOnly;
    delete $.properties.createdTime.readOnly;
    delete $.properties.lastUpdateTime.readOnly;
    delete $.properties.architecture.readOnly;
    delete $.properties.os.readOnly;
    delete $.properties.references.readOnly;
    delete $.properties.tags.readOnly;
```

### Remove readonly for TagAttributes
```yaml
directive:
  - from: swagger-document
    where: $.definitions.TagAttributes
    transform: >
      $["x-ms-client-name"] = "ArtifactTagPropertiesInternal";
      delete $.properties.registry.readOnly;
      delete $.properties.imageName.readOnly;
      delete $.properties.tag.readOnly;
```

### Set readonly flag to properties of TagAttributesBase
```yaml
directive:
- from: swagger-document
  where: $.definitions.TagAttributesBase
  transform: >
    delete $.properties.signed;
    delete $.properties.name.readOnly;
    delete $.properties.digest.readOnly;
    delete $.properties.createdTime.readOnly;
    delete $.properties.lastUpdateTime.readOnly;
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
# Add content-type parameter
```yaml
directive:
    from: swagger-document
    where: $.paths["/v2/{name}/manifests/{reference}"].put
    transform: >
        $.parameters.push({
            "name": "Content-Type",
            "in": "header",
            "type": "string",
            "description": "The manifest's Content-Type."
        });
        delete $.responses["201"].schema;
```

# Change NextLink client name to nextLink
```yaml
directive:
  from: swagger-document
  where: $.parameters.NextLink
  transform: >
    $["x-ms-client-name"] = "nextLink"
```

# Updates to OciManifest
```yaml
directive:
  from: swagger-document
  where: $.definitions.OCIManifest
  transform: >
    $["x-ms-client-name"] = "OciManifest";
    delete $["x-accessibility"];
    delete $["allOf"];
    $.properties["schemaVersion"] = {
          "type": "integer",
          "description": "Schema version"
        };
```

# Take stream as manifest body
```yaml
directive:
  from: swagger-document
  where: $.parameters.ManifestBody
  transform: >
    $.schema = {
        "type": "string",
        "format": "binary"
      }
```

# Make ArtifactBlobDescriptor a public type
```yaml
directive:
  from: swagger-document
  where: $.definitions.Descriptor
  transform: >
    $["x-ms-client-name"] = "OciBlobDescriptor";
    delete $["x-accessibility"]
```

# Make OciAnnotations a public type
```yaml
directive:
  from: swagger-document
  where: $.definitions.Annotations
  transform: >
    $["x-ms-client-name"] = "OciAnnotations";
    delete $["x-accessibility"]
```
