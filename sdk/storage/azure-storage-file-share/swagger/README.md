# Azure File Storage for Java

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
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/storage-dataplane-preview/specification/storage/data-plane/Microsoft.FileStorage/preview/2020-10-02/file.json
java: true
output-folder: ../
namespace: com.azure.storage.file.share
enable-xml: true
generate-client-as-impl: true
generate-client-interfaces: false
service-interface-as-public: true
sync-methods: none
license-header: MICROSOFT_MIT_SMALL
context-client-method-parameter: true
models-subpackage: implementation.models
custom-types-subpackage: models
custom-types: HandleItem,ShareFileHttpHeaders,ShareServiceProperties,ShareCorsRule,Range,FileRange,ClearRange,ShareFileRangeList,CopyStatusType,ShareSignedIdentifier,SourceModifiedAccessConditions,ShareErrorCode,StorageServiceProperties,ShareMetrics,ShareAccessPolicy,ShareFileDownloadHeaders,LeaseDurationType,LeaseStateType,LeaseStatusType,PermissionCopyModeType,ShareAccessTier,ShareRootSquash,ShareRetentionPolicy,ShareProtocolSettings,ShareSmbSettings,SmbMultichannel
customization-jar-path: target/azure-storage-file-share-customization-1.0.0-beta.1.jar
customization-class: com.azure.storage.file.share.customization.ShareStorageCustomization
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

### Remove directoryPath from path params
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]
  transform: >
    for (const property in $)
    {
        if (property.includes("/{shareName}/{directory}/{fileName}"))
        {
            $[property]["parameters"] = $[property]["parameters"].filter(function(param) {return (typeof param['$ref'] === "undefined") || (false == param['$ref'].endsWith("#/parameters/DirectoryPath"))});
        }
    }
```

### /{shareName}/{directoryPath}?restype=directory
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]
  transform: >
        const op = $["/{shareName}/{directory}?restype=directory"];
        op.put.responses["201"].headers["x-ms-file-creation-time"].format = "date-time";
        op.put.responses["201"].headers["x-ms-file-last-write-time"].format = "date-time";
        op.put.responses["201"].headers["x-ms-file-change-time"].format = "date-time";
        op.get.responses["200"].headers["x-ms-file-creation-time"].format = "date-time";
        op.get.responses["200"].headers["x-ms-file-last-write-time"].format = "date-time";
        op.get.responses["200"].headers["x-ms-file-change-time"].format = "date-time";
```

### /{shareName}/{directoryPath}?restype=directory&comp=properties
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]
  transform: >
        const op = $["/{shareName}/{directory}?restype=directory&comp=properties"];
        op.put.responses["200"].headers["x-ms-file-creation-time"].format = "date-time";
        op.put.responses["200"].headers["x-ms-file-last-write-time"].format = "date-time";
        op.put.responses["200"].headers["x-ms-file-change-time"].format = "date-time";
```

### /{shareName}/{filePath}
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]
  transform: >
    if (!$["/{shareName}/{fileName}"]) {
        const op = $["/{shareName}/{fileName}"] = $["/{shareName}/{directory}/{fileName}"];
        const path = op.put.parameters[0].$ref.replace(/[#].*$/, "#/parameters/");
        op.get.responses["200"].headers["Content-MD5"]["x-ms-client-name"] = "contentMd5";
        op.get.responses["206"].headers["Content-MD5"]["x-ms-client-name"] = "contentMd5";
        delete op.get.responses["200"].headers["x-ms-content-md5"]["x-ms-client-name"];
        delete op.get.responses["206"].headers["x-ms-content-md5"]["x-ms-client-name"];
        op.get.responses["200"].headers["x-ms-content-md5"]["x-ms-client-name"] = "FileContentMd5";
        op.get.responses["206"].headers["x-ms-content-md5"]["x-ms-client-name"] = "FileContentMd5";
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
    if (!$["/{shareName}/{fileName}?comp=properties"]) {
        const op = $["/{shareName}/{fileName}?comp=properties"] = $["/{shareName}/{directory}/{fileName}?comp=properties"];
        delete $["/{shareName}/{directory}/{fileName}?comp=properties"];
        op.put.responses["200"].headers["x-ms-file-creation-time"].format = "date-time";
        op.put.responses["200"].headers["x-ms-file-last-write-time"].format = "date-time";
        op.put.responses["200"].headers["x-ms-file-change-time"].format = "date-time";
    }
```

### /{shareName}/{filePath}?comp=range
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]
  transform: >
    if (!$["/{shareName}/{fileName}?comp=range"]) {
        const op = $["/{shareName}/{fileName}?comp=range"] = $["/{shareName}/{directory}/{fileName}?comp=range"];
        op.put.parameters[3]["x-ms-enum"].name = "ShareFileRangeWriteType";
        delete $["/{shareName}/{directory}/{fileName}?comp=range"];
    }
```

``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]
  transform: >
    for (const property in $)
    {
        if (property.includes("/{shareName}/{directory}/{fileName}"))
        {
            var oldName = property;
            var newName = property.replace('/{shareName}/{directory}/{fileName}', '/{shareName}/{fileName}');
            $[newName] = $[oldName];
            delete $[oldName];
        }
    }
```

### SharePropertiesInternal
``` yaml
directive:
- from: swagger-document
  where: $.definitions
  transform: >
    if (!$.SharePropertiesInternal.properties.Metadata) {
        const path = $.ShareItemInternal.properties.Metadata.$ref;
        $.SharePropertiesInternal.properties.Metadata = { "$ref": path };
    }
    $.SharePropertiesInternal.properties.Etag["x-ms-client-name"] = "eTag";
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
    $.enum = [ "2019-07-07" ];
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


## Rename FileDownloadHeaders to ShareFileDownloadHeaders
``` yaml
directive: 
  - from: code-model-v1
    where: $..[?(@.serializedName=="File-Download-Headers")]
    transform: $.name.raw = 'Share-File-Download-Headers'
```

### FileRangeWriteFromUrl Constant
``` yaml
directive:
- from: swagger-document
  where: $.parameters.LeaseIdOptional
  transform: >
    delete $["x-ms-parameter-grouping"];
```

### ListSharesSegment x-ms-pageable itemName
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/?comp=list"].get
  transform: >
    $["x-ms-pageable"].itemName = "ShareItems";
```

### Delete Directory_ListFilesAndDirectoriesSegment x-ms-pageable as autorest does not currently support multiple return types for pageable
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{shareName}/{directory}?restype=directory&comp=list"].get
  transform: >
    delete $["x-ms-pageable"];
```

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fstorage%2Fazure-storage-file-share%2Fswagger%2FREADME.png)

