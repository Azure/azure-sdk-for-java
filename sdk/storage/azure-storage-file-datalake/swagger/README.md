# Azure File Data Lake Storage for Java

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
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/storage-dataplane-preview/specification/storage/data-plane/Microsoft.StorageDataLake/stable/2018-11-09/DataLakeStorage.json
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

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fstorage%2Fazure-storage-file-datalake%2Fswagger%2FREADME.png)

