# Azure Blob Storage for Java

> see https://aka.ms/autorest

### Setup
```ps
cd C:\work
git clone --recursive https://github.com/Azure/autorest.java/
cd autorest.java
git checkout v3
npm install
cd ..
git clone --recursive https://github.com/jianghaolu/autorest.modeler/
cd autorest.modeler
git checkout headerprefixfix
npm install
```

### Generation
```ps
cd <swagger-folder>
autorest --use=C:/work/autorest.java --use=C:/work/autorest.modeler --version=2.0.4280
```

### Code generation settings
``` yaml
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/storage-dataplane-preview/specification/storage/data-plane/Microsoft.BlobStorage/preview/2019-02-02/blob.json
java: true
output-folder: ../
namespace: com.azure.storage.blob
enable-xml: true
generate-client-interfaces: false
sync-methods: none
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
```

### /{containerName}?restype=container
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}?restype=container"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
        const path = param["$ref"].replace(/[#].*$/, "#/parameters/ContainerName");
        $.put.parameters.splice(0, 0, { "$ref": path });
        $.get.parameters.splice(0, 0, { "$ref": path });
        $.delete.parameters.splice(0, 0, { "$ref": path });
    }
```

### /{containerName}?restype=container&comp=metadata
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}?restype=container&comp=metadata"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
        const path = param["$ref"].replace(/[#].*$/, "#/parameters/ContainerName");
        $.put.parameters.splice(0, 0, { "$ref": path });
    }
```

### /{containerName}?restype=container&comp=acl
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}?restype=container&comp=acl"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
        const path = param["$ref"].replace(/[#].*$/, "#/parameters/ContainerName");
        $.put.parameters.splice(0, 0, { "$ref": path });
        $.get.parameters.splice(0, 0, { "$ref": path });
    }
```

### /{containerName}?comp=lease&restype=container&acquire
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}?comp=lease&restype=container&acquire"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
        const path = param["$ref"].replace(/[#].*$/, "#/parameters/ContainerName");
        $.put.parameters.splice(0, 0, { "$ref": path });
    }
```

### /{containerName}?comp=lease&restype=container&release
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}?comp=lease&restype=container&release"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
        const path = param["$ref"].replace(/[#].*$/, "#/parameters/ContainerName");
        $.put.parameters.splice(0, 0, { "$ref": path });
    }
```

### /{containerName}?comp=lease&restype=container&renew
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}?comp=lease&restype=container&renew"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
        const path = param["$ref"].replace(/[#].*$/, "#/parameters/ContainerName");
        $.put.parameters.splice(0, 0, { "$ref": path });
    }
```

### /{containerName}?comp=lease&restype=container&break
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}?comp=lease&restype=container&break"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
        const path = param["$ref"].replace(/[#].*$/, "#/parameters/ContainerName");
        $.put.parameters.splice(0, 0, { "$ref": path });
    }
```

### /{containerName}?comp=lease&restype=container&change
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}?comp=lease&restype=container&change"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
        const path = param["$ref"].replace(/[#].*$/, "#/parameters/ContainerName");
        $.put.parameters.splice(0, 0, { "$ref": path });
    }
```

### /{containerName}?restype=container&comp=list&flat
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}?restype=container&comp=list&flat"]
  transform: >
    let param = $.get.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
        const path = param["$ref"].replace(/[#].*$/, "#/parameters/ContainerName");
        $.get.parameters.splice(0, 0, { "$ref": path });
    }
```

### /{containerName}?restype=container&comp=list&hierarchy
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}?restype=container&comp=list&hierarchy"]
  transform: >
    let param = $.get.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
        const path = param["$ref"].replace(/[#].*$/, "#/parameters/ContainerName");
        $.get.parameters.splice(0, 0, { "$ref": path });
    }
```

### /{containerName}?restype=account&comp=properties
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}?restype=account&comp=properties"]
  transform: >
    let param = $.get.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
        const path = param["$ref"].replace(/[#].*$/, "#/parameters/ContainerName");
        $.get.parameters.splice(0, 0, { "$ref": path });
    }
```

### /{containerName}/{blob}
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}/{blob}"]
  transform: >
    let param = $.get.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
        const path = param["$ref"].replace(/[#].*$/, "#/parameters/");
        $.get.parameters.splice(0, 0, { "$ref": path + "ContainerName" });
        $.get.parameters.splice(1, 0, { "$ref": path + "Blob" });
        $.get.description = "The Download operation reads or downloads a blob from the system, including its metadata and properties. You can also call Download to read a snapshot or version.";
        $.head.parameters.splice(0, 0, { "$ref": path + "ContainerName" });
        $.head.parameters.splice(1, 0, { "$ref": path + "Blob" });
        $.delete.description = "If the storage account's soft delete feature is disabled then, when a blob is deleted, it is permanently removed from the storage account. If the storage account's soft delete feature is enabled, then, when a blob is deleted, it is marked for deletion and becomes inaccessible immediately. However, the blob service retains the blob or snapshot for the number of days specified by the DeleteRetentionPolicy section of [Storage service properties] (Set-Blob-Service-Properties.md). After the specified number of days has passed, the blob's data is permanently removed from the storage account. Note that you continue to be charged for the soft-deleted blob's storage until it is permanently removed. Use the List Blobs API and specify the \"include=deleted\" query parameter to discover which blobs and snapshots have been soft deleted. You can then use the Undelete Blob API to restore a soft-deleted blob. All other operations on a soft-deleted blob or snapshot causes the service to return an HTTP status code of 404 (ResourceNotFound). If the storage account's automatic snapshot feature is enabled, then, when a blob is deleted, an automatic snapshot is created. The blob becomes inaccessible immediately. All other operations on the blob causes the service to return an HTTP status code of 404 (ResourceNotFound). You can access automatic snapshot using snapshot timestamp or version id. You can restore the blob by calling Put or Copy Blob API with automatic snapshot as source. Deleting automatic snapshot requires shared key or special SAS/RBAC permissions.";
        $.delete.parameters.splice(0, 0, { "$ref": path + "ContainerName" });
        $.delete.parameters.splice(1, 0, { "$ref": path + "Blob" });
    }
```

### /{containerName}/{blob}?PageBlob
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}/{blob}?PageBlob"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
      const path = param["$ref"].replace(/[#].*$/, "#/parameters/");
      $.put.parameters.splice(0, 0, { "$ref":  path + "ContainerName" });
      $.put.parameters.splice(1, 0, { "$ref":  path + "Blob" });
    }
```

### /{containerName}/{blob}?AppendBlob
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}/{blob}?AppendBlob"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
      const path = param["$ref"].replace(/[#].*$/, "#/parameters/");
      $.put.parameters.splice(0, 0, { "$ref":  path + "ContainerName" });
      $.put.parameters.splice(1, 0, { "$ref":  path + "Blob" });
    }
```

### /{containerName}/{blob}?comp=appendblock
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}/{blob}?comp=appendblock"]
  transform: >
    $.put.consumes = ["application/octet-stream"];
```

### /{containerName}/{blob}?BlockBlob
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}/{blob}?BlockBlob"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
      const path = param["$ref"].replace(/[#].*$/, "#/parameters/");
      $.put.parameters.splice(0, 0, { "$ref":  path + "ContainerName" });
      $.put.parameters.splice(1, 0, { "$ref":  path + "Blob" });
    }
```

### /{containerName}/{blob}?comp=undelete
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}/{blob}?comp=undelete"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
      const path = param["$ref"].replace(/[#].*$/, "#/parameters/");
      $.put.parameters.splice(0, 0, { "$ref": path + "ContainerName" });
      $.put.parameters.splice(1, 0, { "$ref": path + "Blob" });
    }
```

### /{containerName}/{blob}?comp=properties&SetHTTPHeaders
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}/{blob}?comp=properties&SetHTTPHeaders"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
      const path = param["$ref"].replace(/[#].*$/, "#/parameters/");
      $.put.parameters.splice(0, 0, { "$ref": path + "ContainerName" });
      $.put.parameters.splice(1, 0, { "$ref": path + "Blob" });
    }
```

### /{containerName}/{blob}?comp=metadata
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}/{blob}?comp=metadata"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
      const path = param["$ref"].replace(/[#].*$/, "#/parameters/");
      $.put.parameters.splice(0, 0, { "$ref": path + "ContainerName" });
      $.put.parameters.splice(1, 0, { "$ref": path + "Blob" });
    }
```

### /{containerName}/{blob}?comp=lease&acquire
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}/{blob}?comp=lease&acquire"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
      const path = param["$ref"].replace(/[#].*$/, "#/parameters/");
      $.put.parameters.splice(0, 0, { "$ref": path + "ContainerName" });
      $.put.parameters.splice(1, 0, { "$ref": path + "Blob" });
    }
```

### /{containerName}/{blob}?comp=lease&release
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}/{blob}?comp=lease&release"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
      const path = param["$ref"].replace(/[#].*$/, "#/parameters/");
      $.put.parameters.splice(0, 0, { "$ref": path + "ContainerName" });
      $.put.parameters.splice(1, 0, { "$ref": path + "Blob" });
    }
```

### /{containerName}/{blob}?comp=lease&renew
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}/{blob}?comp=lease&renew"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
      const path = param["$ref"].replace(/[#].*$/, "#/parameters/");
      $.put.parameters.splice(0, 0, { "$ref": path + "ContainerName" });
      $.put.parameters.splice(1, 0, { "$ref": path + "Blob" });
    }
```

### /{containerName}/{blob}?comp=lease&change
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}/{blob}?comp=lease&change"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
      const path = param["$ref"].replace(/[#].*$/, "#/parameters/");
      $.put.parameters.splice(0, 0, { "$ref": path + "ContainerName" });
      $.put.parameters.splice(1, 0, { "$ref": path + "Blob" });
    }
```

### /{containerName}/{blob}?comp=lease&break
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}/{blob}?comp=lease&break"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
      const path = param["$ref"].replace(/[#].*$/, "#/parameters/");
      $.put.parameters.splice(0, 0, { "$ref": path + "ContainerName" });
      $.put.parameters.splice(1, 0, { "$ref": path + "Blob" });
    }
```

### /{containerName}/{blob}?comp=snapshot
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}/{blob}?comp=snapshot"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
      const path = param["$ref"].replace(/[#].*$/, "#/parameters/");
      $.put.parameters.splice(0, 0, { "$ref": path + "ContainerName" });
      $.put.parameters.splice(1, 0, { "$ref": path + "Blob" });
    }
```

### /{containerName}/{blob}?comp=copy
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}/{blob}?comp=copy"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
      const path = param["$ref"].replace(/[#].*$/, "#/parameters/");
      $.put.parameters.splice(0, 0, { "$ref": path + "ContainerName" });
      $.put.parameters.splice(1, 0, { "$ref": path + "Blob" });
      $.put.responses["202"].headers["x-ms-version-id"] = {
        "x-ms-client-name": "VersionId",
        "type": "string",
        "description": "UTC date/time value generated by the service that identifies the version of the blob. This header is returned for requests made against version 2018-11-09 and above."
      };
    }
```

### /{containerName}/{blob}?comp=copy&sync
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}/{blob}?comp=copy&sync"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
      const path = param["$ref"].replace(/[#].*$/, "#/parameters/");
      $.put.parameters.splice(0, 0, { "$ref": path + "ContainerName" });
      $.put.parameters.splice(1, 0, { "$ref": path + "Blob" });
      $.put.responses["202"].headers["x-ms-version-id"] = {
        "x-ms-client-name": "VersionId",
        "type": "string",
        "description": "UTC date/time value generated by the service that identifies the version of the blob. This header is returned for requests made against version 2018-11-09 and above."
      };
    }
```

### /{containerName}/{blob}?comp=copy&copyid={CopyId}
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}/{blob}?comp=copy&copyid={CopyId}"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
      const path = param["$ref"].replace(/[#].*$/, "#/parameters/");
      $.put.parameters.splice(0, 0, { "$ref": path + "ContainerName" });
      $.put.parameters.splice(1, 0, { "$ref": path + "Blob" });
    }
```

### /{containerName}/{blob}?comp=tier
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}/{blob}?comp=tier"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
      const path = param["$ref"].replace(/[#].*$/, "#/parameters/");
      $.put.parameters.splice(0, 0, { "$ref": path + "ContainerName" });
      $.put.parameters.splice(1, 0, { "$ref": path + "Blob" });
    }
```

### /{containerName}/{blob}?restype=account&comp=properties
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}/{blob}?restype=account&comp=properties"]
  transform: >
    let param = $.get.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
      const path = param["$ref"].replace(/[#].*$/, "#/parameters/");
      $.get.parameters.splice(0, 0, { "$ref": path + "ContainerName" });
      $.get.parameters.splice(1, 0, { "$ref": path + "Blob" });
    }
```

### /{containerName}/{blob}?comp=block
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}/{blob}?comp=block"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
      const path = param["$ref"].replace(/[#].*$/, "#/parameters/");
      $.put.parameters.splice(0, 0, { "$ref": path + "ContainerName" });
      $.put.parameters.splice(1, 0, { "$ref": path + "Blob" });
    }
```

### /{containerName}/{blob}?comp=block&fromURL
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}/{blob}?comp=block&fromURL"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
      const path = param["$ref"].replace(/[#].*$/, "#/parameters/");
      $.put.parameters.splice(0, 0, { "$ref":  path + "ContainerName" });
      $.put.parameters.splice(1, 0, { "$ref":  path + "Blob" });
    }
```

### /{containerName}/{blob}?comp=blocklist
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}/{blob}?comp=blocklist"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
      const path = param["$ref"].replace(/[#].*$/, "#/parameters/");
      $.put.parameters.splice(0, 0, { "$ref":  path + "ContainerName" });
      $.put.parameters.splice(1, 0, { "$ref":  path + "Blob" });
      $.get.parameters.splice(0, 0, { "$ref": path + "ContainerName" });
      $.get.parameters.splice(1, 0, { "$ref": path + "Blob" });
    }
```

### /{containerName}/{blob}?comp=page&update
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}/{blob}?comp=page&update"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
      const path = param["$ref"].replace(/[#].*$/, "#/parameters/");
      $.put.parameters.splice(0, 0, { "$ref": path + "ContainerName" });
      $.put.parameters.splice(1, 0, { "$ref": path + "Blob" });
    }
```

### /{containerName}/{blob}?comp=page&clear
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}/{blob}?comp=page&clear"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
      const path = param["$ref"].replace(/[#].*$/, "#/parameters/");
      $.put.parameters.splice(0, 0, { "$ref": path + "ContainerName" });
      $.put.parameters.splice(1, 0, { "$ref": path + "Blob" });
    }
```

### /{containerName}/{blob}?comp=page&clear
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}/{blob}?comp=page&clear"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
      const path = param["$ref"].replace(/[#].*$/, "#/parameters/");
      $.put.parameters.splice(0, 0, { "$ref": path + "ContainerName" });
      $.put.parameters.splice(1, 0, { "$ref": path + "Blob" });
    }
```

### /{containerName}/{blob}?comp=page&update&fromUrl
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}/{blob}?comp=page&update&fromUrl"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
      const path = param["$ref"].replace(/[#].*$/, "#/parameters/");
      $.put.parameters.splice(0, 0, { "$ref": path + "ContainerName" });
      $.put.parameters.splice(1, 0, { "$ref": path + "Blob" });
    }
```

### /{containerName}/{blob}?comp=pagelist
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}/{blob}?comp=pagelist"]
  transform: >
    let param = $.get.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
      const path = param["$ref"].replace(/[#].*$/, "#/parameters/");
      $.get.description = "The Get Page Ranges operation returns the list of valid page ranges for a page blob, version or snapshot of a page blob";
      $.get.parameters.splice(0, 0, { "$ref": path + "ContainerName" });
      $.get.parameters.splice(1, 0, { "$ref": path + "Blob" });
    }
```

### /{containerName}/{blob}?comp=pagelist&diff
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}/{blob}?comp=pagelist&diff"]
  transform: >
    let param = $.get.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
      const path = param["$ref"].replace(/[#].*$/, "#/parameters/");
      $.get.description = "[Update] The Get Page Ranges Diff operation returns the list of valid page ranges for a page blob that were changed between target blob and previous snapshot or version.";
      $.get.parameters.splice(0, 0, { "$ref": path + "ContainerName" });
      $.get.parameters.splice(1, 0, { "$ref": path + "Blob" });
    }
```

### /{containerName}/{blob}?comp=properties&Resize
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}/{blob}?comp=properties&Resize"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
      const path = param["$ref"].replace(/[#].*$/, "#/parameters/");
      $.put.parameters.splice(0, 0, { "$ref": path + "ContainerName" });
      $.put.parameters.splice(1, 0, { "$ref": path + "Blob" });
    }
```

### /{containerName}/{blob}?comp=properties&UpdateSequenceNumber
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}/{blob}?comp=properties&UpdateSequenceNumber"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
      const path = param["$ref"].replace(/[#].*$/, "#/parameters/");
      $.put.parameters.splice(0, 0, { "$ref": path + "ContainerName" });
      $.put.parameters.splice(1, 0, { "$ref": path + "Blob" });
    }
```

### /{containerName}/{blob}?comp=incrementalcopy
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}/{blob}?comp=incrementalcopy"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
      const path = param["$ref"].replace(/[#].*$/, "#/parameters/");
      $.put.parameters.splice(0, 0, { "$ref": path + "ContainerName" });
      $.put.parameters.splice(1, 0, { "$ref": path + "Blob" });
    }
```

### /{containerName}/{blob}?comp=appendblock
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}/{blob}?comp=appendblock"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
      const path = param["$ref"].replace(/[#].*$/, "#/parameters/");
      $.put.parameters.splice(0, 0, { "$ref":  path + "ContainerName" });
      $.put.parameters.splice(1, 0, { "$ref":  path + "Blob" });
    }
```

### /{containerName}/{blob}?comp=appendblock&fromUrl
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}/{blob}?comp=appendblock&fromUrl"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
      const path = param["$ref"].replace(/[#].*$/, "#/parameters/");
      $.put.parameters.splice(0, 0, { "$ref": path + "ContainerName" });
      $.put.parameters.splice(1, 0, { "$ref": path + "Blob" });
    }
```

### BlobItem
``` yaml
directive:
- from: swagger-document
  where: $.definitions.BlobItem
  transform: >
    if (!$.required.includes("VersionId")) {
      $.required.push("VersionId");
      $.properties.Metadata.type = "object";
      $.properties.Metadata.additionalProperties = { "type": "string" };
      delete $.properties.Metadata.$ref;
      $.properties.VersionId = { "type": "string" };
    }
    $.properties.IsPrefix = { "type": "boolean" };
```

### BlobMetadata
Deleting out Encryption until https://github.com/Azure/azure-sdk-for-java/issues/5000 is determined.
``` yaml
directive:
- from: swagger-document
  where: $.definitions.BlobMetadata
  transform: >
    delete $.properties
```

### BlobProperties
``` yaml
directive:
- from: swagger-document
  where: $.definitions.BlobProperties
  transform: >
    $.properties.CustomerProvidedKeySha256 = { "type": "string" }
```

### ListBlobsFlatSegmentResponse
``` yaml
directive:
- from: swagger-document
  where: $.definitions.ListBlobsFlatSegmentResponse
  transform: >
    if (!$.required.includes("Prefix")) {
      $.required.push("Prefix");
      $.required.push("Marker");
      $.required.push("MaxResults");
      $.required.push("Delimiter");
      $.required.push("NextMarker");
    }
```

### ListBlobsHierarchySegmentResponse
``` yaml
directive:
- from: swagger-document
  where: $.definitions.ListBlobsHierarchySegmentResponse
  transform: >
    if (!$.required.includes("Prefix")) {
      $.required.push("Prefix");
      $.required.push("Marker");
      $.required.push("MaxResults");
      $.required.push("Delimiter");
      $.required.push("NextMarker");
    }
```

### ListContainersSegmentResponse
``` yaml
directive:
- from: swagger-document
  where: $.definitions.ListContainersSegmentResponse
  transform: >
    if (!$.required.includes("Prefix")) {
      $.required.push("Prefix");
      $.required.push("MaxResults");
      $.required.push("NextMarker");
    }
```

### SignedIdentifier
``` yaml
directive:
- from: swagger-document
  where: $.definitions.SignedIdentifier
  transform: >
    if ($.xml) {
      delete $.xml;
    }
- from: swagger-document
  where: $.definitions.SignedIdentifiers
  transform: >
    $.items.xml = { "name": "SignedIdentifier" }
```

### KeyInfo
``` yaml
directive:
- from: swagger-document
  where: $.parameters.KeyInfo
  transform: >
    if ($["x-ms-parameter-location"]) {
      delete $["x-ms-parameter-location"];
    }
```

### Extra parameters
``` yaml
directive:
- from: swagger-document
  where: $.parameters
  transform: >
    if (!$.CacheControl) {
      $.CacheControl = {
        "name": "Cache-Control",
        "x-ms-client-name": "cacheControl",
        "in": "header",
        "required": false,
        "type": "string",
        "x-ms-parameter-location": "method",
        "description": "Cache control for given resource"
      };
    }
```

### /{containerName}/{blob}?comp=tags
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}/{blob}?comp=tags"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ContainerName")) {
      const path = param["$ref"].replace(/[#].*$/, "#/parameters/");
      $.put.parameters.splice(0, 0, { "$ref": path + "ContainerName" });
      $.put.parameters.splice(1, 0, { "$ref": path + "Blob" });
      $.get.parameters.splice(0, 0, { "$ref": path + "ContainerName" });
      $.get.parameters.splice(1, 0, { "$ref": path + "Blob" });
    }
```

### /{filesystem}/{path}?resource=directory&Create
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{filesystem}/{path}?resource=directory&Create"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("Filesystem")) {
      const path = param["$ref"].replace(/[#].*$/, "#/parameters/");
      $.put.parameters.splice(0, 0, { "$ref": path + "Filesystem" });
      $.put.parameters.splice(1, 0, { "$ref": path + "Path" });
    }
```

### /{filesystem}/{path}?DirectoryRename
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{filesystem}/{path}?DirectoryRename"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("Filesystem")) {
      const path = param["$ref"].replace(/[#].*$/, "#/parameters/");
      $.put.parameters.splice(0, 0, { "$ref": path + "Filesystem" });
      $.put.parameters.splice(1, 0, { "$ref": path + "Path" });
    }
```

### /{filesystem}/{path}?DirectoryDelete
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{filesystem}/{path}?DirectoryDelete"]
  transform: >
    let param = $.delete.parameters[0];
    if (!param["$ref"].endsWith("Filesystem")) {
      const path = param["$ref"].replace(/[#].*$/, "#/parameters/");
      $.delete.parameters.splice(0, 0, { "$ref": path + "Filesystem" });
      $.delete.parameters.splice(1, 0, { "$ref": path + "Path" });
    }
```

### /{filesystem}/{path}?FileRename
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{filesystem}/{path}?FileRename"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("Filesystem")) {
      const path = param["$ref"].replace(/[#].*$/, "#/parameters/");
      $.put.parameters.splice(0, 0, { "$ref": path + "Filesystem" });
      $.put.parameters.splice(1, 0, { "$ref": path + "Path" });
    }
```

### Add the CustomHierarchicalListingDeserializer attribute
``` yaml
directive:
- from: BlobHierarchyListSegment.java
  where: $
  transform: >
    return $.
      replace(
        "import com.fasterxml.jackson.annotation.JsonProperty;",
        "import com.fasterxml.jackson.annotation.JsonProperty;\nimport com.fasterxml.jackson.databind.annotation.JsonDeserialize;").
      replace(
        "public final class BlobHierarchyListSegment {",
        "@JsonDeserialize(using = CustomHierarchicalListingDeserializer.class)\npublic final class BlobHierarchyListSegment {");
```

### Add EncryptionKeySha256 to PageBlobUploadPagesFromURLHeaders
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}/{blob}?comp=page&update&fromUrl"].put.responses["201"].headers
  transform: >
    if (!$["x-ms-encryption-key-sha256"]) {
      $["x-ms-encryption-key-sha256"] = {
        "x-ms-client-name": "EncryptionKeySha256",
        "type": "string",
        "description": "The SHA-256 hash of the encryption key used to encrypt the pages. This header is only returned when the pages were encrypted with a customer-provided key."
      };
    }
```

### Add IsServerEncrypted to AppendBlobAppendBlockFromUrlHeaders
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}/{blob}?comp=appendblock&fromUrl"].put.responses["201"].headers
  transform: >
    if (!$["x-ms-request-server-encrypted"]) {
      $["x-ms-request-server-encrypted"] = {
        "x-ms-client-name": "IsServerEncrypted",
        "type": "boolean",
        "description": "The value of this header is set to true if the contents of the request are successfully encrypted using the specified algorithm, and false otherwise."
      };
    }
```

### Add EncryptionKeySha256 and IsServerEncrypted to PageBlobClearPagesHeaders
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{containerName}/{blob}?comp=page&clear"].put.responses["201"].headers
  transform: >
    if (!$["x-ms-request-server-encrypted"]) {
      $["x-ms-request-server-encrypted"] = {
        "x-ms-client-name": "IsServerEncrypted",
        "type": "boolean",
        "description": "The value of this header is set to true if the contents of the request are successfully encrypted using the specified algorithm, and false otherwise."
      };
    }
    if (!$["x-ms-encryption-key-sha256"]) {
      $["x-ms-encryption-key-sha256"] = {
        "x-ms-client-name": "EncryptionKeySha256",
        "type": "string",
        "description": "The SHA-256 hash of the encryption key used to encrypt the pages. This header is only returned when the pages were encrypted with a customer-provided key."
      };
    }
```
