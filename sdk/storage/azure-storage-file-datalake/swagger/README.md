# Azure File Data Lake Storage for Java

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
``` yaml
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/storage-dataplane-preview/specification/storage/data-plane/Microsoft.StorageDataLake/stable/2020-06-12/DataLakeStorage.json
java: true
output-folder: ../
namespace: com.azure.storage.file.datalake
enable-xml: true
generate-client-as-impl: true
generate-client-interfaces: false
service-interface-as-public: true
sync-methods: none
license-header: MICROSOFT_MIT_SMALL
context-client-method-parameter: true
optional-constant-as-enum: true
models-subpackage: implementation.models
custom-types: FileSystemInfo,FileSystemItem,FileSystemProperties,PathInfo,PathItem,PathProperties,ListFileSystemsOptions,PathHttpHeaders
custom-types-subpackage: models
customization-jar-path: target/azure-storage-file-datalake-customization-1.0.0-beta.1.jar
customization-class: com.azure.storage.file.datalake.customization.DataLakeStorageCustomization
```

### Adds FileSystem parameter to /{filesystem}?resource=filesystem
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{filesystem}?resource=filesystem"]
  transform: >
    let param = $.parameters[0];
    if (!param["$ref"].endsWith("FileSystem")) {
        const path = param["$ref"].replace(/[#].*$/, "#/parameters/FileSystem");
        $.parameters.splice(0, 0, { "$ref": path });
    }
```

### Adds FileSystem and Path parameter to /{filesystem}/{path}
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{filesystem}/{path}"]
  transform: >
    let param = $.parameters[0];
    if (!param["$ref"].endsWith("FileSystem")) {
        const fileSystemPath = param["$ref"].replace(/[#].*$/, "#/parameters/FileSystem");
        const pathPath = param["$ref"].replace(/[#].*$/, "#/parameters/Path");
        $.parameters.splice(0, 0, { "$ref": fileSystemPath });
        $.parameters.splice(1, 0, { "$ref": pathPath });
    }
```

### Adds FileSystem and Path parameter to /{filesystem}/{path}?action=append
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{filesystem}/{path}?action=append"].patch
  transform: >
    let param = $.parameters[0];
    if (!param["$ref"].endsWith("FileSystem")) {
        const fileSystemPath = param["$ref"].replace(/[#].*$/, "#/parameters/FileSystem");
        const pathPath = param["$ref"].replace(/[#].*$/, "#/parameters/Path");
        $.parameters.splice(0, 0, { "$ref": fileSystemPath });
        $.parameters.splice(1, 0, { "$ref": pathPath });
    }
```

### Adds FileSystem and Path parameter to /{filesystem}/{path}?action=setAccessControl
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{filesystem}/{path}?action=setAccessControl"].patch
  transform: >
    let param = $.parameters[0];
    if (!param["$ref"].endsWith("FileSystem")) {
        const fileSystemPath = param["$ref"].replace(/[#].*$/, "#/parameters/FileSystem");
        const pathPath = param["$ref"].replace(/[#].*$/, "#/parameters/Path");
        $.parameters.splice(0, 0, { "$ref": fileSystemPath });
        $.parameters.splice(1, 0, { "$ref": pathPath });
    }
```

### Adds FileSystem and Path parameter to /{filesystem}/{path}?action=setAccessControlRecursive
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{filesystem}/{path}?action=setAccessControlRecursive"].patch
  transform: >
    let param = $.parameters[0];
    if (!param["$ref"].endsWith("FileSystem")) {
        const fileSystemPath = param["$ref"].replace(/[#].*$/, "#/parameters/FileSystem");
        const pathPath = param["$ref"].replace(/[#].*$/, "#/parameters/Path");
        $.parameters.splice(0, 0, { "$ref": fileSystemPath });
        $.parameters.splice(1, 0, { "$ref": pathPath });
    }
```

### Adds FileSystem and Path parameter to /{filesystem}/{path}?action=flush
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{filesystem}/{path}?action=flush"].patch
  transform: >
    let param = $.parameters[0];
    if (!param["$ref"].endsWith("FileSystem")) {
        const fileSystemPath = param["$ref"].replace(/[#].*$/, "#/parameters/FileSystem");
        const pathPath = param["$ref"].replace(/[#].*$/, "#/parameters/Path");
        $.parameters.splice(0, 0, { "$ref": fileSystemPath });
        $.parameters.splice(1, 0, { "$ref": pathPath });
    }
```

### Make the body of append octet-stream /{filesystem}/{path}?action=append
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{filesystem}/{path}?action=append"]
  transform: >
    $.patch.consumes = ["application/octet-stream"];
```

### Adds FileSystem and Path parameter to /{filesystem}/{path}?comp=expiry
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{filesystem}/{path}?comp=expiry"].put
  transform: >
    let param = $.parameters[0];
    if (!param["$ref"].endsWith("FileSystem")) {
        const fileSystemPath = param["$ref"].replace(/[#].*$/, "#/parameters/FileSystem");
        const pathPath = param["$ref"].replace(/[#].*$/, "#/parameters/Path");
        $.parameters.splice(0, 0, { "$ref": fileSystemPath });
        $.parameters.splice(1, 0, { "$ref": pathPath });
    }
```

### Make ACL on Path Get Properties lower case
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{filesystem}/{path}"]["head"]["responses"]["200"]
  transform: >
      delete $.headers["x-ms-acl"]["x-ms-client-name"];
      $.headers["x-ms-acl"]["x-ms-client-name"] = "acl";
```

### Rename PathHTTPHeaders to PathHttpHeaders
``` yaml
directive:
- from: swagger-document
  where: $.parameters
  transform: >
    $.CacheControl["x-ms-parameter-grouping"].name = "path-http-headers";
    $.ContentDisposition["x-ms-parameter-grouping"].name = "path-http-headers";
    $.ContentEncoding["x-ms-parameter-grouping"].name = "path-http-headers";
    $.ContentLanguage["x-ms-parameter-grouping"].name = "path-http-headers";
    $.ContentMD5["x-ms-parameter-grouping"].name = "path-http-headers";
    $.ContentMD5["x-ms-client-name"] = "contentMd5";
    $.ContentType["x-ms-parameter-grouping"].name = "path-http-headers";
    $.TransactionalContentMD5["x-ms-parameter-grouping"].name = "path-http-headers";
```

### Make eTag in Path JsonProperty to etag
``` yaml
directive:
- from: Path.java
  where: $
  transform: >
    return $.replace('@JsonProperty(value = "eTag")\n    private String eTag;', '@JsonProperty(value = "etag")\n    private String eTag;');
```

### Delete FileSystem_ListPaths x-ms-pageable as autorest doesnt allow you to set the nextLinkName to be a header.
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{filesystem}?resource=filesystem"].get
  transform: >
    delete $["x-ms-pageable"];
```

### Adds FileSystem parameter to /{filesystem}?restype=container&comp=list&hierarchy
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{filesystem}?restype=container&comp=list&hierarchy"]
  transform: >
    let param = $.get.parameters[0];
    if (!param["$ref"].endsWith("FileSystem")) {
        const path = param["$ref"].replace(/[#].*$/, "#/parameters/FileSystem");
        $.get.parameters.splice(0, 0, { "$ref": path });
    }
```

### Delete Container_ListBlobHierarchySegment x-ms-pageable as autorest can't recognize the itemName for this
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{filesystem}?restype=container&comp=list&hierarchy"].get
  transform: >
    delete $["x-ms-pageable"];
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

### Adds FileSystem and Path parameter to /{filesystem}/{path}?comp=undelete
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{filesystem}/{path}?comp=undelete"].put
  transform: >
    let param = $.parameters[0];
    if (!param["$ref"].endsWith("FileSystem")) {
        const fileSystemPath = param["$ref"].replace(/[#].*$/, "#/parameters/FileSystem");
        const pathPath = param["$ref"].replace(/[#].*$/, "#/parameters/Path");
        $.parameters.splice(0, 0, { "$ref": fileSystemPath });
        $.parameters.splice(1, 0, { "$ref": pathPath });
    }
```

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fstorage%2Fazure-storage-file-datalake%2Fswagger%2FREADME.png)

