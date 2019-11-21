# Azure File Storage for Java

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
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/storage-dataplane-preview/specification/storage/data-plane/Microsoft.FileStorage/preview/2019-02-02/file.json
java: true
output-folder: ../
namespace: com.azure.storage.file.share
enable-xml: true
generate-client-interfaces: false
sync-methods: none
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
models-subpackage: implementation.models
custom-types-subpackage: models
custom-types: HandleItem,ShareFileHttpHeaders,ShareItem,ShareServiceProperties,ShareCorsRule,ShareProperties,Range,CopyStatusType,ShareSignedIdentifier,SourceModifiedAccessConditions,ShareErrorCode,StorageServiceProperties,ShareMetrics,ShareAccessPolicy,ShareFileDownloadHeaders
```

### Query Parameters
``` yaml
directive:
- from: swagger-document
  where: $.parameters
  transform: >
    if (!$.DirectoryPath) {
        $.DirectoryPath = {
            "name": "directoryPath",
            "in": "path",
            "description": "The path of the target directory.",
            "required": true,
            "type": "string",
            "x-ms-parameter-location": "method",
            "x-ms-skip-url-encoding": false
        };
    }
    if (!$.FilePath) {
        $.FilePath = {
            "name": "filePath",
            "in": "path",
            "description": "The path of the target file.",
            "required": true,
            "type": "string",
            "x-ms-parameter-location": "method",
            "x-ms-skip-url-encoding": false
        };
    }
    if (!$.ShareName) {
        $.ShareName = {
            "name": "shareName",
            "in": "path",
            "description": "The name of the target share.",
            "required": true,
            "type": "string",
            "x-ms-parameter-location": "method"
        };
    }
```

### /{shareName}?restype=share
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{shareName}?restype=share"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ShareName")) {
        const path = param["$ref"].replace(/[#].*$/, "#/parameters/ShareName");
        $.put.parameters.splice(0, 0, { "$ref": path });
        $.get.parameters.splice(0, 0, { "$ref": path });
        $.delete.parameters.splice(0, 0, { "$ref": path });
    }
```

### /{shareName}?restype=share&comp=filepermission
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{shareName}?restype=share&comp=filepermission"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ShareName")) {
        const path = param["$ref"].replace(/[#].*$/, "#/parameters/ShareName");
        $.put.parameters.splice(0, 0, { "$ref": path });
    }
    param = $.get.parameters[0];
    if (!param["$ref"].endsWith("ShareName")) {
        const path = param["$ref"].replace(/[#].*$/, "#/parameters/ShareName");
        $.get.parameters.splice(0, 0, { "$ref": path });
    }
```

### /{shareName}?restype=share&comp=properties
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{shareName}?restype=share&comp=properties"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ShareName")) {
        const path = param["$ref"].replace(/[#].*$/, "#/parameters/ShareName");
        $.put.parameters.splice(0, 0, { "$ref": path });
    }
```

### /{shareName}?restype=share&comp=snapshot
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{shareName}?restype=share&comp=snapshot"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ShareName")) {
        const path = param["$ref"].replace(/[#].*$/, "#/parameters/ShareName");
        $.put.parameters.splice(0, 0, { "$ref": path });
    }
```

### /{shareName}?restype=share&comp=metadata
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{shareName}?restype=share&comp=metadata"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ShareName")) {
        const path = param["$ref"].replace(/[#].*$/, "#/parameters/ShareName");
        $.put.parameters.splice(0, 0, { "$ref": path });
    }
```

### /{shareName}?restype=share&comp=acl
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{shareName}?restype=share&comp=acl"]
  transform: >
    let param = $.put.parameters[0];
    if (!param["$ref"].endsWith("ShareName")) {
        const path = param["$ref"].replace(/[#].*$/, "#/parameters/ShareName");
        $.put.parameters.splice(0, 0, { "$ref": path });
        $.get.parameters.splice(0, 0, { "$ref": path });
    }
```


### /{shareName}?restype=share&comp=stats
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{shareName}?restype=share&comp=stats"]
  transform: >
    let param = $.get.parameters[0];
    if (!param["$ref"].endsWith("ShareName")) {
        const path = param["$ref"].replace(/[#].*$/, "#/parameters/ShareName");
        $.get.parameters.splice(0, 0, { "$ref": path });
    }
```

### /{shareName}/{directoryPath}?restype=directory
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]
  transform: >
    if (!$["/{shareName}/{directoryPath}?restype=directory"]) {
        const op = $["/{shareName}/{directoryPath}?restype=directory"] = $["/{shareName}/{directory}?restype=directory"];
        const path = op.put.parameters[0].$ref.replace(/[#].*$/, "#/parameters/");
        op.put.parameters.splice(0, 0, { "$ref": path + "ShareName" });
        op.put.parameters.splice(1, 0, { "$ref": path + "DirectoryPath" });
        op.get.parameters.splice(0, 0, { "$ref": path + "ShareName" });
        op.get.parameters.splice(1, 0, { "$ref": path + "DirectoryPath" });
        op.delete.parameters.splice(0, 0, { "$ref": path + "ShareName" });
        op.delete.parameters.splice(1, 0, { "$ref": path + "DirectoryPath" });
        delete $["/{shareName}/{directory}?restype=directory"];
        op.put.responses["201"].headers["x-ms-file-creation-time"].format = "date-time";
        op.put.responses["201"].headers["x-ms-file-last-write-time"].format = "date-time";
        op.put.responses["201"].headers["x-ms-file-change-time"].format = "date-time";
        op.get.responses["200"].headers["x-ms-file-creation-time"].format = "date-time";
        op.get.responses["200"].headers["x-ms-file-last-write-time"].format = "date-time";
        op.get.responses["200"].headers["x-ms-file-change-time"].format = "date-time";
    }
```

### /{shareName}/{directoryPath}?restype=directory&comp=properties
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]
  transform: >
    if (!$["/{shareName}/{directoryPath}?restype=directory&comp=properties"]) {
        const op = $["/{shareName}/{directoryPath}?restype=directory&comp=properties"] = $["/{shareName}/{directory}?restype=directory&comp=properties"];
        const path = op.put.parameters[0].$ref.replace(/[#].*$/, "#/parameters/");
        op.put.parameters.splice(0, 0, { "$ref": path + "ShareName" });
        op.put.parameters.splice(1, 0, { "$ref": path + "DirectoryPath" });
        delete $["/{shareName}/{directory}?restype=directory&comp=properties"];
        op.put.responses["200"].headers["x-ms-file-creation-time"].format = "date-time";
        op.put.responses["200"].headers["x-ms-file-last-write-time"].format = "date-time";
        op.put.responses["200"].headers["x-ms-file-change-time"].format = "date-time";
    }
```

### /{shareName}/{directoryPath}?restype=directory&comp=metadata
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]
  transform: >
    if (!$["/{shareName}/{directoryPath}?restype=directory&comp=metadata"]) {
        const op = $["/{shareName}/{directoryPath}?restype=directory&comp=metadata"] = $["/{shareName}/{directory}?restype=directory&comp=metadata"];
        const path = op.put.parameters[0].$ref.replace(/[#].*$/, "#/parameters/");
        op.put.parameters.splice(0, 0, { "$ref": path + "ShareName" });
        op.put.parameters.splice(1, 0, { "$ref": path + "DirectoryPath" });
        delete $["/{shareName}/{directory}?restype=directory&comp=metadata"];
    }
```

### /{shareName}/{directoryPath}?restype=directory&comp=list
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]
  transform: >
    if (!$["/{shareName}/{directoryPath}?restype=directory&comp=list"]) {
        const op = $["/{shareName}/{directoryPath}?restype=directory&comp=list"] = $["/{shareName}/{directory}?restype=directory&comp=list"];
        const path = op.get.parameters[0].$ref.replace(/[#].*$/, "#/parameters/");
        op.get.parameters.splice(0, 0, { "$ref": path + "ShareName" });
        op.get.parameters.splice(1, 0, { "$ref": path + "DirectoryPath" });
        delete $["/{shareName}/{directory}?restype=directory&comp=list"];
    }
```

### /{shareName}/{directoryPath}?comp=listhandles
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]
  transform: >
    if (!$["/{shareName}/{directoryPath}?comp=listhandles"]) {
        const op = $["/{shareName}/{directoryPath}?comp=listhandles"] = $["/{shareName}/{directory}?comp=listhandles"];
        const path = op.get.parameters[0].$ref.replace(/[#].*$/, "#/parameters/");
        op.get.parameters.splice(0, 0, { "$ref": path + "ShareName" });
        op.get.parameters.splice(1, 0, { "$ref": path + "DirectoryPath" });
        delete $["/{shareName}/{directory}?comp=listhandles"];
    }
```

### /{shareName}/{directoryPath}?comp=forceclosehandles
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]
  transform: >
    if (!$["/{shareName}/{directoryPath}?comp=forceclosehandles"]) {
        const op = $["/{shareName}/{directoryPath}?comp=forceclosehandles"] = $["/{shareName}/{directory}?comp=forceclosehandles"];
        const path = op.put.parameters[0].$ref.replace(/[#].*$/, "#/parameters/");
        op.put.parameters.splice(0, 0, { "$ref": path + "ShareName" });
        op.put.parameters.splice(1, 0, { "$ref": path + "DirectoryPath" });
        delete $["/{shareName}/{directory}?comp=forceclosehandles"];
    }
```

### /{shareName}/{filePath}
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]
  transform: >
    if (!$["/{shareName}/{filePath}"]) {
        const op = $["/{shareName}/{filePath}"] = $["/{shareName}/{directory}/{fileName}"];
        const path = op.put.parameters[0].$ref.replace(/[#].*$/, "#/parameters/");
        op.put.parameters.splice(0, 0, { "$ref": path + "ShareName" });
        op.put.parameters.splice(1, 0, { "$ref": path + "FilePath" });
        op.get.parameters.splice(0, 0, { "$ref": path + "ShareName" });
        op.get.parameters.splice(1, 0, { "$ref": path + "FilePath" });
        op.get.responses["200"].headers["Content-MD5"]["x-ms-client-name"] = "contentMd5";
        op.get.responses["206"].headers["Content-MD5"]["x-ms-client-name"] = "contentMd5";
        delete op.get.responses["200"].headers["x-ms-content-md5"]["x-ms-client-name"];
        delete op.get.responses["206"].headers["x-ms-content-md5"]["x-ms-client-name"];
        op.get.responses["200"].headers["x-ms-content-md5"]["x-ms-client-name"] = "FileContentMd5";
        op.get.responses["206"].headers["x-ms-content-md5"]["x-ms-client-name"] = "FileContentMd5";
        op.head.parameters.splice(0, 0, { "$ref": path + "ShareName" });
        op.head.parameters.splice(1, 0, { "$ref": path + "FilePath" });
        delete op.head.responses.default.schema;
        op.delete.parameters.splice(0, 0, { "$ref": path + "ShareName" });
        op.delete.parameters.splice(1, 0, { "$ref": path + "FilePath" });
        delete $["/{shareName}/{directory}/{fileName}"];
        op.put.responses["201"].headers["x-ms-file-creation-time"].format = "date-time";
        op.put.responses["201"].headers["x-ms-file-last-write-time"].format = "date-time";
        op.put.responses["201"].headers["x-ms-file-change-time"].format = "date-time";
        op.get.responses["200"].headers["x-ms-file-creation-time"].format = "date-time";
        op.get.responses["200"].headers["x-ms-file-last-write-time"].format = "date-time";
        op.get.responses["200"].headers["x-ms-file-change-time"].format = "date-time";
        op.get.responses["206"].headers["x-ms-file-creation-time"].format = "date-time";
        op.get.responses["206"].headers["x-ms-file-last-write-time"].format = "date-time";
        op.get.responses["206"].headers["x-ms-file-change-time"].format = "date-time";
        op.head.responses["200"].headers["x-ms-file-creation-time"].format = "date-time";
        op.head.responses["200"].headers["x-ms-file-last-write-time"].format = "date-time";
        op.head.responses["200"].headers["x-ms-file-change-time"].format = "date-time";
    }
```

### /{shareName}/{filePath}?comp=properties
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]
  transform: >
    if (!$["/{shareName}/{filePath}?comp=properties"]) {
        const op = $["/{shareName}/{filePath}?comp=properties"] = $["/{shareName}/{directory}/{fileName}?comp=properties"];
        const path = op.put.parameters[0].$ref.replace(/[#].*$/, "#/parameters/");
        op.put.parameters.splice(0, 0, { "$ref": path + "ShareName" });
        op.put.parameters.splice(1, 0, { "$ref": path + "FilePath" });
        delete $["/{shareName}/{directory}/{fileName}?comp=properties"];
        op.put.responses["200"].headers["x-ms-file-creation-time"].format = "date-time";
        op.put.responses["200"].headers["x-ms-file-last-write-time"].format = "date-time";
        op.put.responses["200"].headers["x-ms-file-change-time"].format = "date-time";
    }
```

### /{shareName}/{filePath}?comp=metadata
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]
  transform: >
    if (!$["/{shareName}/{filePath}?comp=metadata"]) {
        const op = $["/{shareName}/{filePath}?comp=metadata"] = $["/{shareName}/{directory}/{fileName}?comp=metadata"];
        const path = op.put.parameters[0].$ref.replace(/[#].*$/, "#/parameters/");
        op.put.parameters.splice(0, 0, { "$ref": path + "ShareName" });
        op.put.parameters.splice(1, 0, { "$ref": path + "FilePath" });
        delete $["/{shareName}/{directory}/{fileName}?comp=metadata"];
    }
```

### /{shareName}/{filePath}?comp=range
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]
  transform: >
    if (!$["/{shareName}/{filePath}?comp=range"]) {
        const op = $["/{shareName}/{filePath}?comp=range"] = $["/{shareName}/{directory}/{fileName}?comp=range"];
        op.put.parameters[3]["x-ms-enum"].name = "ShareFileRangeWriteType";
        const path = op.put.parameters[0].$ref.replace(/[#].*$/, "#/parameters/");
        op.put.parameters.splice(0, 0, { "$ref": path + "ShareName" });
        op.put.parameters.splice(1, 0, { "$ref": path + "FilePath" });
        delete $["/{shareName}/{directory}/{fileName}?comp=range"];
    }
```
### /{shareName}/{filePath}?comp=range&fromURL
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]
  transform: >
    if (!$["/{shareName}/{filePath}?comp=range&fromURL"]) {
        const op = $["/{shareName}/{filePath}?comp=range&fromURL"] = $["/{shareName}/{directory}/{fileName}?comp=range&fromURL"];
        const path = op.put.parameters[0].$ref.replace(/[#].*$/, "#/parameters/");
        op.put.parameters.splice(0, 0, { "$ref": path + "ShareName" });
        op.put.parameters.splice(1, 0, { "$ref": path + "FilePath" });
        delete $["/{shareName}/{directory}/{fileName}?comp=range&fromURL"];
    }
```


### /{shareName}/{filePath}?comp=rangelist
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]
  transform: >
    if (!$["/{shareName}/{filePath}?comp=rangelist"]) {
        const op = $["/{shareName}/{filePath}?comp=rangelist"] = $["/{shareName}/{directory}/{fileName}?comp=rangelist"];
        const path = op.get.parameters[0].$ref.replace(/[#].*$/, "#/parameters/");
        op.get.parameters.splice(0, 0, { "$ref": path + "ShareName" });
        op.get.parameters.splice(1, 0, { "$ref": path + "FilePath" });
        delete $["/{shareName}/{directory}/{fileName}?comp=rangelist"];
    }
```

### /{shareName}/{filePath}?comp=copy
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]
  transform: >
    if (!$["/{shareName}/{filePath}?comp=copy"]) {
        const op = $["/{shareName}/{filePath}?comp=copy"] = $["/{shareName}/{directory}/{fileName}?comp=copy"];
        const path = op.put.parameters[0].$ref.replace(/[#].*$/, "#/parameters/");
        op.put.parameters.splice(0, 0, { "$ref": path + "ShareName" });
        op.put.parameters.splice(1, 0, { "$ref": path + "FilePath" });
        delete $["/{shareName}/{directory}/{fileName}?comp=copy"];
    }
```

### /{shareName}/{filePath}?comp=copy&copyid={CopyId}
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]
  transform: >
    if (!$["/{shareName}/{filePath}?comp=copy&copyid={CopyId}"]) {
        const op = $["/{shareName}/{filePath}?comp=copy&copyid={CopyId}"] = $["/{shareName}/{directory}/{fileName}?comp=copy&copyid={CopyId}"];
        const path = op.put.parameters[0].$ref.replace(/[#].*$/, "#/parameters/");
        op.put.parameters.splice(0, 0, { "$ref": path + "ShareName" });
        op.put.parameters.splice(1, 0, { "$ref": path + "FilePath" });
        delete $["/{shareName}/{directory}/{fileName}?comp=copy&copyid={CopyId}"];
    }
```

### /{shareName}/{filePath}?comp=listhandles
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]
  transform: >
    if (!$["/{shareName}/{filePath}?comp=listhandles"]) {
        const op = $["/{shareName}/{filePath}?comp=listhandles"] = $["/{shareName}/{directory}/{fileName}?comp=listhandles"];
        const path = op.get.parameters[0].$ref.replace(/[#].*$/, "#/parameters/");
        op.get.parameters.splice(0, 0, { "$ref": path + "ShareName" });
        op.get.parameters.splice(1, 0, { "$ref": path + "FilePath" });
        delete $["/{shareName}/{directory}/{fileName}?comp=listhandles"];
    }
```

### /{shareName}/{filePath}?comp=forceclosehandles
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]
  transform: >
    if (!$["/{shareName}/{filePath}?comp=forceclosehandles"]) {
        const op = $["/{shareName}/{filePath}?comp=forceclosehandles"] = $["/{shareName}/{directory}/{fileName}?comp=forceclosehandles"];
        const path = op.put.parameters[0].$ref.replace(/[#].*$/, "#/parameters/");
        op.put.parameters.splice(0, 0, { "$ref": path + "ShareName" });
        op.put.parameters.splice(1, 0, { "$ref": path + "FilePath" });
        delete $["/{shareName}/{directory}/{fileName}?comp=forceclosehandles"];
    }
```

### ShareProperties
``` yaml
directive:
- from: swagger-document
  where: $.definitions
  transform: >
    if (!$.ShareProperties.properties.Metadata) {
        const path = $.ShareItem.properties.Metadata.$ref;
        $.ShareProperties.properties.Metadata = { "$ref": path };
    }
    $.ShareProperties.properties.Etag["x-ms-client-name"] = "eTag";
```

### ShareUsageBytes
``` yaml
directive:
- from: swagger-document
  where: $.definitions.ShareStats.properties.ShareUsageBytes
  transform: >
    $.description = "The approximate size of the data stored in bytes, rounded up to the nearest gigabyte. Note that this value may not include all recently created or recently resized files.";
    $.format = "int64";
```

### ApiVersionParameter
``` yaml
directive:
- from: swagger-document
  where: $.parameters.ApiVersionParameter
  transform: >
    $.enum = [ "2019-02-02" ];
```

### Convert FileCreationTime and FileLastWriteTime to String to Support 'now'
``` yaml
directive:
- from: swagger-document
  where: $.parameters.FileCreationTime
  transform: >
    delete $.format;
- from: swagger-document
  where: $.parameters.FileLastWriteTime
  transform: >
    delete $.format;
```

### FileRangeWriteFromUrl Constant
``` yaml
directive:
- from: swagger-document
  where: $.parameters.FileRangeWriteFromUrl
  transform: >
    delete $.default;
    delete $["x-ms-enum"];
    $["x-ms-parameter-location"] = "method";
```

### Add the CustomFileAndDirectoryListingDeserializer attribute
``` yaml
directive:
- from: FilesAndDirectoriesListSegment.java
  where: $
  transform: >
    return $.
      replace(
        "import com.fasterxml.jackson.annotation.JsonProperty;",
        "import com.fasterxml.jackson.annotation.JsonProperty;\nimport com.fasterxml.jackson.databind.annotation.JsonDeserialize;").
      replace(
        "public final class FilesAndDirectoriesListSegment {",
        "@JsonDeserialize(using = CustomFileAndDirectoryListingDeserializer.class)\npublic final class FilesAndDirectoriesListSegment {");
```

### ShareErrorCode
``` yaml
directive:
- from: swagger-document
  where: $.definitions.ErrorCode
  transform: >
    $["x-ms-enum"].name = "ShareErrorCode";
```

### ShareServiceProperties, ShareMetrics, ShareCorsRule, and ShareRetentionPolicy
``` yaml
directive:
- from: swagger-document
  where: $.definitions
  transform: >
    if (!$.ShareServiceProperties) {
        $.ShareServiceProperties = $.StorageServiceProperties;
        delete $.StorageServiceProperties;
        $.ShareServiceProperties.xml = { "name": "StorageServiceProperties" };
    }
    if (!$.ShareMetrics) {
      $.ShareMetrics = $.Metrics;
      delete $.Metrics;
      $.ShareMetrics.xml = {"name": "Metrics"};
      $.ShareMetrics.properties.IncludeApis = $.ShareMetrics.properties.IncludeAPIs;
      delete $.ShareMetrics.properties.IncludeAPIs;
      $.ShareMetrics.properties.IncludeApis.xml = {"name": "IncludeAPIs"};
      $.ShareServiceProperties.properties.HourMetrics["$ref"] = "#/definitions/ShareMetrics";
      $.ShareServiceProperties.properties.MinuteMetrics["$ref"] = "#/definitions/ShareMetrics";
    }
    if (!$.ShareCorsRule) {
      $.ShareCorsRule = $.CorsRule;
      delete $.CorsRule;
      $.ShareCorsRule.xml = {"name": "CorsRule"};
      $.ShareServiceProperties.properties.Cors.items["$ref"] = "#/definitions/ShareCorsRule";
    }
    if (!$.ShareRetentionPolicy) {
      $.ShareRetentionPolicy = $.RetentionPolicy;
      delete $.RetentionPolicy;
      $.ShareRetentionPolicy.xml = {"name": "RetentionPolicy"};
      $.ShareMetrics.properties.RetentionPolicy["$ref"] = "#/definitions/ShareRetentionPolicy";
    }
- from: swagger-document
  where: $.parameters
  transform: >
    if (!$.ShareServiceProperties) {
        const props = $.ShareServiceProperties = $.StorageServiceProperties;
        props.name = "ShareServiceProperties";
        props.description = "The FileStorage properties.";
        props.schema = { "$ref": props.schema.$ref.replace(/[#].*$/, "#/definitions/ShareServiceProperties") };
        delete $.StorageServiceProperties;
    }
- from: swagger-document
  where: $["x-ms-paths"]["/?restype=service&comp=properties"]
  transform: >
    const param = $.put.parameters[0];
    if (param && param["$ref"] && param["$ref"].endsWith("StorageServiceProperties")) {
        const path = param["$ref"].replace(/[#].*$/, "#/parameters/ShareServiceProperties");
        $.put.parameters[0] = { "$ref": path };
    }
    const def = $.get.responses["200"].schema;
    if (def && def["$ref"] && def["$ref"].endsWith("StorageServiceProperties")) {
        const path = def["$ref"].replace(/[#].*$/, "#/definitions/ShareServiceProperties");
        $.get.responses["200"].schema = { "$ref": path };
    }
```

### ShareAccessPolicy and ShareSignedIdentifier
``` yaml
directive:
- from: swagger-document
  where: $.definitions
  transform: >
    if (!$.ShareSignedIdentifier) {
      $.ShareSignedIdentifier = $.SignedIdentifier;
      delete $.SignedIdentifier;
      $.ShareSignedIdentifier.xml = {"name": "SignedIdentifier"};
      $.SignedIdentifiers.items["$ref"] = "#/definitions/ShareSignedIdentifier";
    }
- from: swagger-document
  where: $.definitions
  transform: >
    if (!$.ShareAccessPolicy) {
      $.ShareAccessPolicy = $.AccessPolicy;
      delete $.AccessPolicy;
      $.ShareAccessPolicy.xml = {"name": "AccessPolicy"};
      $.ShareAccessPolicy.properties.StartsOn = $.ShareAccessPolicy.properties.Start;
      $.ShareAccessPolicy.properties.StartsOn.xml = {"name": "Start"};
      delete $.ShareAccessPolicy.properties.Start;
      $.ShareAccessPolicy.properties.ExpiresOn = $.ShareAccessPolicy.properties.Expiry;
      $.ShareAccessPolicy.properties.ExpiresOn.xml = {"name": "Expiry"};
      delete $.ShareAccessPolicy.properties.Expiry;
      $.ShareAccessPolicy.properties.Permissions = $.ShareAccessPolicy.properties.Permission;
      $.ShareAccessPolicy.properties.Permissions.xml = {"name": "Permission"};
      delete $.ShareAccessPolicy.properties.Permission;
    }
    $.ShareSignedIdentifier.properties.AccessPolicy["$ref"] = "#/definitions/ShareAccessPolicy";
```

### ShareServiceProperties Annotation Fix
``` yaml
directive:
- from: ShareServiceProperties.java
  where: $
  transform: >
    return $.replace('@JsonProperty(value = "Metrics")\n    private ShareMetrics hourMetrics;', '@JsonProperty(value = "HourMetrics")\n    private ShareMetrics hourMetrics;').
      replace('@JsonProperty(value = "Metrics")\n    private ShareMetrics minuteMetrics;', '@JsonProperty(value = "MinuteMetrics")\n    private ShareMetrics minuteMetrics;');
```

### Rename FileHTTPHeaders to FileHttpHeader and remove file prefix from properties
``` yaml
directive:
- from: swagger-document
  where: $.parameters
  transform: >
    $.FileCacheControl["x-ms-parameter-grouping"].name = "share-file-http-headers";
    $.FileCacheControl["x-ms-client-name"] = "cacheControl";
    $.FileContentDisposition["x-ms-parameter-grouping"].name = "share-file-http-headers";
    $.FileContentDisposition["x-ms-client-name"] = "contentDisposition";
    $.FileContentEncoding["x-ms-parameter-grouping"].name = "share-file-http-headers";
    $.FileContentEncoding["x-ms-client-name"] = "contentEncoding";
    $.FileContentLanguage["x-ms-parameter-grouping"].name = "share-file-http-headers";
    $.FileContentLanguage["x-ms-client-name"] = "contentLanguage";
    $.FileContentMD5["x-ms-parameter-grouping"].name = "share-file-http-headers";
    $.FileContentMD5["x-ms-client-name"] = "contentMd5";
    $.FileContentType["x-ms-parameter-grouping"].name = "share-file-http-headers";
    $.FileContentType["x-ms-client-name"] = "contentType";
```

### Change StorageErrorException to StorageException
``` yaml
directive:
- from: ServicesImpl.java
  where: $
  transform: >
    return $.
      replace(
        "com.azure.storage.file.share.implementation.models.StorageErrorException",
        "com.azure.storage.file.share.models.ShareStorageException"
      ).
      replace(
        /\@UnexpectedResponseExceptionType\(StorageErrorException\.class\)/g,
        "@UnexpectedResponseExceptionType(ShareStorageException.class)"
      );
- from: SharesImpl.java
  where: $
  transform: >
    return $.
      replace(
        "com.azure.storage.file.share.implementation.models.StorageErrorException",
        "com.azure.storage.file.share.models.ShareStorageException"
      ).
      replace(
        /\@UnexpectedResponseExceptionType\(StorageErrorException\.class\)/g,
        "@UnexpectedResponseExceptionType(ShareStorageException.class)"
      );
- from: DirectorysImpl.java
  where: $
  transform: >
    return $.
      replace(
        "com.azure.storage.file.share.implementation.models.StorageErrorException",
        "com.azure.storage.file.share.models.ShareStorageException"
      ).
      replace(
        /\@UnexpectedResponseExceptionType\(StorageErrorException\.class\)/g,
        "@UnexpectedResponseExceptionType(ShareStorageException.class)"
      );
- from: FilesImpl.java
  where: $
  transform: >
    return $.
      replace(
        "com.azure.storage.file.share.implementation.models.StorageErrorException",
        "com.azure.storage.file.share.models.ShareStorageException"
      ).
      replace(
        /\@UnexpectedResponseExceptionType\(StorageErrorException\.class\)/g,
        "@UnexpectedResponseExceptionType(ShareStorageException.class)"
      );
```
## Rename FileDownloadHeaders to ShareFileDownloadHeaders
``` yaml
directive: 
  - from: code-model-v1
    where: $..[?(@.serializedName=="File-Download-Headers")]
    transform: $.name.raw = 'Share-File-Download-Headers'
```


![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fstorage%2Fazure-storage-file-share%2Fswagger%2FREADME.png)

