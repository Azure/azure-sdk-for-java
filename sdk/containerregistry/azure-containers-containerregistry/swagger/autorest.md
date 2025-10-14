# Azure Container Registry for Java

> see https://aka.ms/autorest

This is the AutoRest configuration file for Container Registry.

---
## Getting Started
To build the SDK for Container Registry, simply [Install AutoRest](https://aka.ms/autorest) and
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

### Code generation settings
``` yaml
use: '@autorest/java@4.1.52'
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/c8d9a26a2857828e095903efa72512cf3a76c15d/specification/containerregistry/data-plane/Azure.ContainerRegistry/stable/2021-07-01/containerregistry.json
java: true
output-folder: ./..
generate-client-as-impl: true
namespace: com.azure.containers.containerregistry
generate-client-interfaces: false
license-header: MICROSOFT_MIT_SMALL
sync-methods: none
models-subpackage: implementation.models
custom-types: ArtifactArchitecture,ArtifactManifestOrder,ArtifactManifestPlatform,ArtifactOperatingSystem,ArtifactTagOrder,ContainerRepositoryProperties,OciAnnotations,OciDescriptor,OciImageManifest,RepositoryProperties
custom-types-subpackage: models
enable-sync-stack: true
disable-client-builder: true
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

### Remove "Authentication_GetAcrAccessTokenFromLogin" operation as the service team discourage using username/password to authenticate.
```yaml
directive:
  - from: swagger-document
    where: $["paths"]["/oauth2/token"]
    transform: >
      delete $.get
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

<!-- Java specific -->

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

<!-- Java specific -->

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

# Rename accept parameter
```yaml
directive:
    from: swagger-document
    where: $.paths["/v2/{name}/manifests/{reference}"].get
    transform: >
        $.parameters[2].name = "Accept";
```

# Change NextLink client name to nextLink
```yaml
directive:
  from: swagger-document
  where: $.parameters.NextLink
  transform: >
    $["x-ms-client-name"] = "nextLink"
```

# Updates to OCIManifest
```yaml
directive:
  from: swagger-document
  where: $.definitions.OCIManifest
  transform: >
    $["x-ms-client-name"] = "OciImageManifest";
    $.required = ["schemaVersion"];
    delete $["x-accessibility"];
    delete $["allOf"];
    $.properties["schemaVersion"] = {
          "type": "integer",
          "description": "Schema version"
        };
    $.properties.config["x-ms-client-name"] = "configuration";
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

# Replace ManifestWrapper with stream response to calculate SHA256

<!-- Java specific -->

```yaml
directive:
  from: swagger-document
  where: $.paths["/v2/{name}/manifests/{reference}"].get.responses["200"]
  transform: >
      $.schema = {
          "type": "string",
          "format": "binary"
      };
```

# Rename ArtifactBlobDescriptor to OciDescriptor
```yaml
directive:
  from: swagger-document
  where: $.definitions.Descriptor
  transform: >
    $["x-ms-client-name"] = "OciDescriptor";
    $.properties.size["x-ms-client-name"] = "sizeInBytes";      
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

# Rename created to createdOn in OciAnnotations
```yaml
directive:
  from: swagger-document
  where: $.definitions.Annotations
  transform: >
    $.properties["org.opencontainers.image.created"] = {
      "description": "Date and time on which the image was built (string, date-time as defined by https://tools.ietf.org/html/rfc3339#section-5.6)",
      "type": "string",
      "format": "date-time",
      "x-ms-client-name": "CreatedOn"
    };
```

# Remove security definitions
``` yaml
directive:
- from: swagger-document
  where: $.
  transform: >
    delete $["securityDefinitions"];
    delete $["security"];
```

# Remove stream response from `deleteBlob`

We don't care about the stream that is returned and we don't want to clean it up

```yaml
directive:
  - from: swagger-document
    where: $.paths["/v2/{name}/blobs/{digest}"]["delete"]
    transform: >
      delete $.responses["202"].schema;
```
