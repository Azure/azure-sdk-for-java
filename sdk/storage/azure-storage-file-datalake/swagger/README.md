# Azure File Data Lake Storage for Java

> see https://aka.ms/autorest

### Setup

Increase max memory if you're using Autorest older than 3. Set the environment variable `NODE_OPTIONS` to `--max-old-space-size=8192`.

### Generation
```ps
cd <swagger-folder>
# You may need to repeat this command few times if you're getting "TypeError: Cannot read property 'filename' of undefined" error
autorest --use=@microsoft.azure/autorest.java@3.0.4 --use=jianghaolu/autorest.modeler#440af3935c504cea4410133e1fd940b78f6af749  --version=2.0.4280
```

### Code generation settings
``` yaml
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/storage-dataplane-preview/specification/storage/data-plane/Microsoft.StorageDataLake/stable/2020-02-10/DataLakeStorage.json
java: true
output-folder: ../
namespace: com.azure.storage.file.datalake
enable-xml: true
generate-client-interfaces: false
sync-methods: none
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
models-subpackage: implementation.models
custom-types: FileSystemInfo,FileSystemItem,FileSystemProperties,PathInfo,PathItem,PathProperties,ListFileSystemsOptions,PathHttpHeaders
custom-types-subpackage: models
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

### Change StorageErrorException to StorageException
``` yaml
directive:
- from: ServicesImpl.java
  where: $
  transform: >
    return $.
      replace(
        "com.azure.storage.file.datalake.implementation.models.StorageErrorException",
        "com.azure.storage.file.datalake.models.DataLakeStorageException"
      ).
      replace(
        /\@UnexpectedResponseExceptionType\(StorageErrorException\.class\)/g,
        "@UnexpectedResponseExceptionType(DataLakeStorageException.class)"
      );
- from: FileSystemsImpl.java
  where: $
  transform: >
    return $.
      replace(
        "com.azure.storage.file.datalake.implementation.models.StorageErrorException",
        "com.azure.storage.file.datalake.models.DataLakeStorageException"
      ).
      replace(
        /\@UnexpectedResponseExceptionType\(StorageErrorException\.class\)/g,
        "@UnexpectedResponseExceptionType(DataLakeStorageException.class)"
      );
- from: PathsImpl.java
  where: $
  transform: >
    return $.
      replace(
        "com.azure.storage.file.datalake.implementation.models.StorageErrorException",
        "com.azure.storage.file.datalake.models.DataLakeStorageException"
      ).
      replace(
        /\@UnexpectedResponseExceptionType\(StorageErrorException\.class\)/g,
        "@UnexpectedResponseExceptionType(DataLakeStorageException.class)"
      );
```


![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fstorage%2Fazure-storage-file-datalake%2Fswagger%2FREADME.png)

