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
input-file: ./blob-2018-11-09.json
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
        $.get.parameters.splice(3, 0, { "$ref": path + "VersionId" });
        $.get.parameters.splice(8, 0, { "$ref": path + "EncryptionKey" });
        $.get.parameters.splice(9, 0, { "$ref": path + "EncryptionKeySha256" });
        $.get.parameters.splice(10, 0, { "$ref": path + "EncryptionAlgorithm" });
        $.get.description = "The Download operation reads or downloads a blob from the system, including its metadata and properties. You can also call Download to read a snapshot or version.";
        $.get.responses["200"].headers["x-ms-encryption-key-sha256"] = {
          "x-ms-client-name": "EncryptionKeySha256",
          "type": "string",
          "description": "The SHA-256 hash of the encryption key used to encrypt the blob. This header is only returned when the blob was encrypted with a customer-provided key."
        };
        $.head.parameters.splice(0, 0, { "$ref": path + "ContainerName" });
        $.head.parameters.splice(1, 0, { "$ref": path + "Blob" });
        $.head.parameters.splice(3, 0, { "$ref": path + "VersionId" });
        $.head.parameters.splice(6, 0, { "$ref": path + "EncryptionKey" });
        $.head.parameters.splice(7, 0, { "$ref": path + "EncryptionKeySha256" });
        $.head.parameters.splice(8, 0, { "$ref": path + "EncryptionAlgorithm" });
        $.head.responses["200"].headers["x-ms-encryption-key-sha256"] = {
          "x-ms-client-name": "EncryptionKeySha256",
          "type": "string",
          "description": "The SHA-256 hash of the encryption key used to encrypt the metadata. This header is only returned when the metadata was encrypted with a customer-provided key."
        };
        $.delete.description = "If the storage account's soft delete feature is disabled then, when a blob is deleted, it is permanently removed from the storage account. If the storage account's soft delete feature is enabled, then, when a blob is deleted, it is marked for deletion and becomes inaccessible immediately. However, the blob service retains the blob or snapshot for the number of days specified by the DeleteRetentionPolicy section of [Storage service properties] (Set-Blob-Service-Properties.md). After the specified number of days has passed, the blob's data is permanently removed from the storage account. Note that you continue to be charged for the soft-deleted blob's storage until it is permanently removed. Use the List Blobs API and specify the \"include=deleted\" query parameter to discover which blobs and snapshots have been soft deleted. You can then use the Undelete Blob API to restore a soft-deleted blob. All other operations on a soft-deleted blob or snapshot causes the service to return an HTTP status code of 404 (ResourceNotFound). If the storage account's automatic snapshot feature is enabled, then, when a blob is deleted, an automatic snapshot is created. The blob becomes inaccessible immediately. All other operations on the blob causes the service to return an HTTP status code of 404 (ResourceNotFound). You can access automatic snapshot using snapshot timestamp or version id. You can restore the blob by calling Put or Copy Blob API with automatic snapshot as source. Deleting automatic snapshot requires shared key or special SAS/RBAC permissions.";
        $.delete.parameters.splice(0, 0, { "$ref": path + "ContainerName" });
        $.delete.parameters.splice(1, 0, { "$ref": path + "Blob" });
        $.delete.parameters.splice(3, 0, { "$ref": path + "VersionId" });
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
      $.put.parameters.splice(12, 0, { "$ref": path + "EncryptionKey" });
      $.put.parameters.splice(13, 0, { "$ref": path + "EncryptionKeySha256" });
      $.put.parameters.splice(14, 0, { "$ref": path + "EncryptionAlgorithm" });
      $.put.responses["201"].headers["x-ms-version-id"] = {
        "x-ms-client-name": "VersionId",
        "type": "string",
        "description": "UTC date/time value generated by the service that identifies a version of the blob. This header is returned for requests made against version 2018-11-09 and above."
      };
      $.put.responses["201"].headers["x-ms-encryption-key-sha256"] = {
        "x-ms-client-name": "EncryptionKeySha256",
        "type": "string",
        "description": "The SHA-256 hash of the encryption key used to encrypt the blob. This header is only returned when the blob was encrypted with a customer-provided key."
      };
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
      $.put.parameters.splice(12, 0, { "$ref": path + "EncryptionKey" });
      $.put.parameters.splice(13, 0, { "$ref": path + "EncryptionKeySha256" });
      $.put.parameters.splice(14, 0, { "$ref": path + "EncryptionAlgorithm" });
      $.put.responses["201"].headers["x-ms-version-id"] = {
        "x-ms-client-name": "VersionId",
        "type": "string",
        "description": "UTC date/time value generated by the service that identifies a version of the blob. This header is returned for requests made against version 2018-11-09 and above."
      };
      $.put.responses["201"].headers["x-ms-encryption-key-sha256"] = {
        "x-ms-client-name": "EncryptionKeySha256",
        "type": "string",
        "description": "The SHA-256 hash of the encryption key used to encrypt the blob. This header is only returned when the blob was encrypted with a customer-provided key."
      };
    }
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
      $.put.parameters.splice(13, 0, { "$ref": path + "EncryptionKey" });
      $.put.parameters.splice(14, 0, { "$ref": path + "EncryptionKeySha256" });
      $.put.parameters.splice(15, 0, { "$ref": path + "EncryptionAlgorithm" });
      $.put.responses["201"].headers["x-ms-version-id"] = {
        "x-ms-client-name": "VersionId",
        "type": "string",
        "description": "UTC date/time value generated by the service that identifies a version of the blob. This header is returned for requests made against version 2018-11-09 and above."
      };
      $.put.responses["201"].headers["x-ms-encryption-key-sha256"] = {
        "x-ms-client-name": "EncryptionKeySha256",
        "type": "string",
        "description": "The SHA-256 hash of the encryption key used to encrypt the blob. This header is only returned when the blob was encrypted with a customer-provided key."
      };
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
      $.put.parameters.splice(5, 0, { "$ref": path + "EncryptionKey" });
      $.put.parameters.splice(6, 0, { "$ref": path + "EncryptionKeySha256" });
      $.put.parameters.splice(7, 0, { "$ref": path + "EncryptionAlgorithm" });
      $.put.responses["200"].headers["x-ms-encryption-key-sha256"] = {
        "x-ms-client-name": "EncryptionKeySha256",
        "type": "string",
        "description": "The SHA-256 hash of the encryption key used to encrypt the metadata. This header is only returned when the metadata was encrypted with a customer-provided key."
      };
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
      $.put.parameters.splice(4, 0, { "$ref": path + "EncryptionKey" });
      $.put.parameters.splice(5, 0, { "$ref": path + "EncryptionKeySha256" });
      $.put.parameters.splice(6, 0, { "$ref": path + "EncryptionAlgorithm" });
      $.put.responses["201"].headers["x-ms-version-id"] = {
        "x-ms-client-name": "VersionId",
        "type": "string",
        "description": "UTC date/time value generated by the service that identifies the version of the blob. This header is returned for requests made against version 2018-11-09 and above."
      };
      $.put.responses["201"].headers["x-ms-request-server-encrypted"] = {
        "x-ms-client-name": "IsServerEncrypted",
        "type": "boolean",
        "description": "True if the contents of the request are successfully encrypted using the specified algorithm, and false otherwise. For a snapshot request, this header is set to true when metadata was provided in the request and encrypted with a customer-provided key."
      };
      $.put.responses["201"].headers["x-ms-encryption-key-sha256"] = {
        "x-ms-client-name": "EncryptionKeySha256",
        "type": "string",
        "description": "The SHA-256 hash of the encryption key used to encrypt the source blob. This header is only returned when the blob was encrypted with a customer-provided key."
      };
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
      $.put.parameters.splice(8, 0, { "$ref": path + "EncryptionKey" });
      $.put.parameters.splice(9, 0, { "$ref": path + "EncryptionKeySha256" });
      $.put.parameters.splice(10, 0, { "$ref": path + "EncryptionAlgorithm" });
      $.put.responses["201"].headers["x-ms-encryption-key-sha256"] = {
        "x-ms-client-name": "EncryptionKeySha256",
        "type": "string",
        "description": "The SHA-256 hash of the encryption key used to encrypt the block. This header is only returned when the block was encrypted with a customer-provided key."
      };
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
      $.put.parameters.splice(9, 0, { "$ref":  path + "EncryptionKey" });
      $.put.parameters.splice(10, 0, { "$ref": path + "EncryptionKeySha256" });
      $.put.parameters.splice(11, 0, { "$ref": path + "EncryptionAlgorithm" });
      $.put.responses["201"].headers["x-ms-encryption-key-sha256"] = {
        "x-ms-client-name": "EncryptionKeySha256",
        "type": "string",
        "description": "The SHA-256 hash of the encryption key used to encrypt the block. This header is only returned when the block was encrypted with a customer-provided key."
      };
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
      $.put.parameters.splice(11, 0, { "$ref": path + "EncryptionKey" });
      $.put.parameters.splice(12, 0, { "$ref": path + "EncryptionKeySha256" });
      $.put.parameters.splice(13, 0, { "$ref": path + "EncryptionAlgorithm" });
      $.put.responses["201"].headers["x-ms-version-id"] = {
        "x-ms-client-name": "VersionId",
        "type": "string",
        "description": "UTC date/time value generated by the service that identifies a version of the blob. This header is returned for requests made against version 2018-11-09 and above."
      };
      $.put.responses["201"].headers["x-ms-encryption-key-sha256"] = {
        "x-ms-client-name": "EncryptionKeySha256",
        "type": "string",
        "description": "The SHA-256 hash of the encryption key used to encrypt the blob. This header is only returned when the blob was encrypted with a customer-provided key."
      };
      $.get.parameters.splice(0, 0, { "$ref": path + "ContainerName" });
      $.get.parameters.splice(1, 0, { "$ref": path + "Blob" });
      $.get.parameters.splice(3, 0, { "$ref": path + "VersionId" });
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
      $.put.parameters.splice(8, 0, { "$ref": path + "EncryptionKey" });
      $.put.parameters.splice(9, 0, { "$ref": path + "EncryptionKeySha256" });
      $.put.parameters.splice(10, 0, { "$ref": path + "EncryptionAlgorithm" });
      $.put.responses["201"].headers["x-ms-encryption-key-sha256"] = {
        "x-ms-client-name": "EncryptionKeySha256",
        "type": "string",
        "description": "The SHA-256 hash of the encryption key used to encrypt the pages. This header is only returned when the pages were encrypted with a customer-provided key."
      };
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
      $.get.parameters.splice(3, 0, { "$ref": path + "VersionId" });
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
      $.get.parameters.splice(3, 0, { "$ref": path + "VersionId" });
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
      $.put.parameters.splice(9, 0, { "$ref":  path + "EncryptionKey" });
      $.put.parameters.splice(10, 0, { "$ref": path + "EncryptionKeySha256" });
      $.put.parameters.splice(11, 0, { "$ref": path + "EncryptionAlgorithm" });
      $.put.responses["201"].headers["x-ms-encryption-key-sha256"] = {
        "x-ms-client-name": "EncryptionKeySha256",
        "type": "string",
        "description": "The SHA-256 hash of the encryption key used to encrypt the block. This header is only returned when the block was encrypted with a customer-provided key."
      };
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
      $.properties.IsPrefix = { "type": "boolean" };
    }
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

### Metadata split
``` yaml
directive:
- from: swagger-document
  where: $.definitions
  transform: >
    if (! $.ContainerMetadata ) {
      $.ContainerMetadata = {
        "type": "object",
        "xml": { "name": "Metadata" },
        "additionalProperties": { "type": "string" }
      };
      $.BlobMetadata = {
        "type": "object",
        "xml": { "name": "Metadata" },
        "properties": { "Encrypted": { "type": "string", "xml": { "attribute": true } } },
        "additionalProperties": { "type": "string" }
      };
      delete $.Metadata;
    }
- from: swagger-document
  where: $.definitions.ContainerItem
  transform: >
    let param = $.properties.Metadata;
    if (!param["$ref"].endsWith("ContainerMetadata")) {
      const path = param["$ref"].replace(/[#].*$/, "#/definitions/ContainerMetadata");
      $.properties.Metadata = { "$ref": path};
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
      $.ContentCrc64 = {
        "name": "x-ms-content-crc64",
        "x-ms-client-name": "transactionalContentCrc64",
        "in": "header",
        "required": false,
        "type": "string",
        "format": "byte",
        "x-ms-parameter-location": "method",
        "description": "Specify the transactional crc64 for the body, to be validated by the service."
      };
      $.EncryptionKey = {
        "name": "x-ms-encryption-key",
        "x-ms-client-name": "EncryptionKey",
        "type": "string",
        "in": "query",
        "required": false,
        "x-ms-parameter-location": "method",
        "description": "Optional. Specifies the encryption key to use to encrypt the data provided in the request. If not specified, encryption is performed with the root account encryption key.  For more information, see Encryption at Rest for Azure Storage Services."
      };
      $.EncryptionKeySha256 = {
        "name": "x-ms-encryption-key-sha256",
        "x-ms-client-name": "encryptionKeySha256",
        "type": "string",
        "in": "query",
        "required": false,
        "x-ms-parameter-location": "method",
        "description": "The SHA-256 hash of the provided encryption key. Must be provided if the x-ms-encryption-key header is provided."
      };
      $.EncryptionAlgorithm = {
        "name": "x-ms-encryption-algorithm",
        "x-ms-client-name": "EncryptionAlgorithm",
        "type": "string",
        "in": "query",
        "required": false,
        "enum": [ "AES256" ],
        "x-ms-enum": { "name": "EncryptionAlgorithmType", "modelAsString": false },
        "x-ms-parameter-location": "method",
        "description": "The algorithm used to produce the encryption key hash. Currently, the only accepted value is \"AES256\". Must be provided if the x-ms-encryption-key header is provided."
      };
      $.VersionId = {
        "name": "versionid",
        "x-ms-client-name": "versionId",
        "in": "query",
        "required": false,
        "type": "string",
        "x-ms-parameter-location": "method",
        "description": "The version ID parameter is an opaque DateTime value that, when present, specifies the blob version to retrieve."
      };
      $.XMsCacheControl = {
        "name": "x-ms-cache-control",
        "x-ms-client-name": "cacheControl",
        "in": "header",
        "required": false,
        "type": "string",
        "x-ms-parameter-location": "method",
        "x-ms-parameter-grouping": { "name": "directory-http-headers" },
        "description": "Cache control for given resource"
      };
      $.XMsContentType = {
        "name": "x-ms-content-type",
        "x-ms-client-name": "contentType",
        "in": "header",
        "required": false,
        "type": "string",
        "x-ms-parameter-location": "method",
        "x-ms-parameter-grouping": { "name": "directory-http-headers" },
        "description": "Content type for given resource"
      };
      $.XMsContentEncoding = {
        "name": "x-ms-content-encoding",
        "x-ms-client-name": "contentEncoding",
        "in": "header",
        "required": false,
        "type": "string",
        "x-ms-parameter-location": "method",
        "x-ms-parameter-grouping": { "name": "directory-http-headers" },
        "description": "Content encoding for given resource"
      };
      $.XMsContentLanguage = {
        "name": "x-ms-content-language",
        "x-ms-client-name": "contentLanguage",
        "in": "header",
        "required": false,
        "type": "string",
        "x-ms-parameter-location": "method",
        "x-ms-parameter-grouping": { "name": "directory-http-headers" },
        "description": "Content language for given resource"
      };
      $.XMsContentDisposition = {
        "name": "x-ms-content-disposition",
        "x-ms-client-name": "contentDisposition",
        "in": "header",
        "required": false,
        "type": "string",
        "x-ms-parameter-location": "method",
        "x-ms-parameter-grouping": { "name": "directory-http-headers" },
        "description": "Content disposition for given resource"
      };
    }
```
