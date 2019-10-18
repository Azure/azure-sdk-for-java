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
input-file: C:\azure-rest-api-specs\specification\storage\data-plane\Microsoft.StorageDataLake\stable\2018-11-09\DataLakeStorage.json
java: true
output-folder: ../
namespace: com.azure.storage.file.datalake
enable-xml: true
generate-client-interfaces: false
sync-methods: none
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
models-subpackage: implementation.models
custom-types: FileSystemInfo, FileSystemItem, FileSystemProperties, PathAccessConditions, PathInfo, PathItem, PathProperties, ListFileSystemsOptions
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
