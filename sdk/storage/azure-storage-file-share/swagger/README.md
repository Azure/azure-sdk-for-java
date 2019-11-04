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
custom-types: HandleItem,ShareFileHttpHeaders,ShareItem,ShareServiceProperties,ShareCorsRule,ShareProperties,Range,CopyStatusType,ShareSignedIdentifier,SourceModifiedAccessConditions,ShareErrorCode,StorageServiceProperties,ShareMetrics,ShareAccessPolicy,ShareFileDownloadHeaders,DeleteSnapshotsOptionType
```

### /{shareName}/{directory}?restype=directory
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{shareName}/{directory}?restype=directory"]
  transform: >
    $.put.responses["201"].headers["x-ms-file-creation-time"].format = "date-time";
    $.put.responses["201"].headers["x-ms-file-last-write-time"].format = "date-time";
    $.put.responses["201"].headers["x-ms-file-change-time"].format = "date-time";
    $.get.responses["200"].headers["x-ms-file-creation-time"].format = "date-time";
    $.get.responses["200"].headers["x-ms-file-last-write-time"].format = "date-time";
    $.get.responses["200"].headers["x-ms-file-change-time"].format = "date-time";
```

### /{shareName}/{directory}?restype=directory&comp=properties
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{shareName}/{directory}?restype=directory&comp=properties"]
  transform: >
    $.put.responses["200"].headers["x-ms-file-creation-time"].format = "date-time";
    $.put.responses["200"].headers["x-ms-file-last-write-time"].format = "date-time";
    $.put.responses["200"].headers["x-ms-file-change-time"].format = "date-time";
```

### /{shareName}/{directory}/{fileName}
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{shareName}/{directory}/{fileName}"]
  transform: >
    $.get.responses["200"].headers["Content-MD5"]["x-ms-client-name"] = "contentMd5";
    $.get.responses["206"].headers["Content-MD5"]["x-ms-client-name"] = "contentMd5";
    $.put.responses["201"].headers["x-ms-file-creation-time"].format = "date-time";
    $.put.responses["201"].headers["x-ms-file-last-write-time"].format = "date-time";
    $.put.responses["201"].headers["x-ms-file-change-time"].format = "date-time";
    $.get.responses["200"].headers["x-ms-file-creation-time"].format = "date-time";
    $.get.responses["200"].headers["x-ms-file-last-write-time"].format = "date-time";
    $.get.responses["200"].headers["x-ms-file-change-time"].format = "date-time";
    $.get.responses["206"].headers["x-ms-file-creation-time"].format = "date-time";
    $.get.responses["206"].headers["x-ms-file-last-write-time"].format = "date-time";
    $.get.responses["206"].headers["x-ms-file-change-time"].format = "date-time";
    $.head.responses["200"].headers["x-ms-file-creation-time"].format = "date-time";
    $.head.responses["200"].headers["x-ms-file-last-write-time"].format = "date-time";
    $.head.responses["200"].headers["x-ms-file-change-time"].format = "date-time";
```

### /{shareName}/{directory}/{fileName}?comp=properties
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{shareName}/{directory}/{fileName}?comp=properties"]
  transform: >
    $.put.responses["200"].headers["x-ms-file-creation-time"].format = "date-time";
    $.put.responses["200"].headers["x-ms-file-last-write-time"].format = "date-time";
    $.put.responses["200"].headers["x-ms-file-change-time"].format = "date-time";
```

### /{shareName}/{directory}/{fileName}?comp=range
``` yaml
directive:
- from: swagger-document
  where: $["x-ms-paths"]["/{shareName}/{directory}/{fileName}?comp=range"].put
  transform: >
    $.parameters[3]["x-ms-enum"] = {"name": "ShareFileRangeWriteType", "modelAsString": false};
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
