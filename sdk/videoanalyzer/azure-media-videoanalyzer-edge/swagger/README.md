# Azure Java Autorest config file

> see https://aka.ms/autorest

## Configuration

```yaml
title: Microsoft Azure SDK for Azure Video Analyzer on IoT Edge - edge client library for Java
description: This package contains the edge client library for Azure Video Analyzer on IoT Edge.
generate-metadata: false
license-header: MICROSOFT_MIT_SMALL
output-folder: ../
source-code-folder-path: ./src/generated
java: true
require: https://github.com/Azure/azure-rest-api-specs/blob/60fcb275cbce38d343f9c35411786e672aba154e/specification/videoanalyzer/data-plane/readme.md
add-credentials: false
namespace: com.azure.media.videoanalyzer.edge
sync-methods: none
add-context-parameter: true
models-subpackage: models
custom-types-subpacakge: models
context-client-method-parameter: true
use: '@autorest/java@4.1.39'
model-override-setter-from-superclass: true
required-fields-as-ctor-args: true
customization-class: src/main/java/VideoAnalyzerEdgeCustomization.java
```

### discriminator vs default enum
```yaml
directive:
- from: AzureVideoAnalyzerSdkDefinitions.json
  where: $.definitions
  transform: >
    let definitionKeys = Object.keys($);
    for(let i = 0; i < definitionKeys.length; i++) {
      if(definitionKeys[i] === "MethodRequest") {
        delete $[definitionKeys[i]].properties.methodName;
        delete $[definitionKeys[i]].discriminator;
        delete $[definitionKeys[i]].properties["@apiVersion"].enum;
        delete $[definitionKeys[i]].properties["@apiVersion"]["x-ms-enum"];
      }
      else {
        if($[definitionKeys[i]]["x-ms-discriminator-value"]) {
          let definition = $[definitionKeys[i]];
          let value = definition["x-ms-discriminator-value"];
          delete definition["x-ms-discriminator-value"];
          if(!definition.properties) {
            definition.properties = {};
          }
          definition.properties.methodName = {
            "type": "string",
            "description": "method name",
            "readOnly": true
          };
          if(definition.required){
            definition.required.push("methodName");
          }
          else {
            definition.required = ["methodName"];
          }
        }
      }
    }
```

### Add AssetSink
```yaml
directive:
- from: AzureVideoAnalyzer.json
  where: $.definitions
  transform: >
    $.AssetSink = {
      "type": "object",
      "properties": {
        "assetContainerSasUrl": {
          "type": "string",
          "description": "An Azure Storage SAS Url which points to container, such as the one created for an Azure Media Services asset."
        },
        "segmentLength": {
          "type": "string",
          "description": "When writing media to an asset, wait until at least this duration of media has been accumulated on the Edge. Expressed in increments of 30 seconds, with a minimum of 30 seconds and a recommended maximum of 5 minutes."
        },
        "localMediaCachePath": {
          "type": "string",
          "description": "Path to a local file system directory for temporary caching of media before writing to an Asset. Used when the Edge device is temporarily disconnected from Azure."
        },
        "localMediaCacheMaximumSizeMiB": {
          "type": "string",
          "description": "Maximum amount of disk space that can be used for temporary caching of media."
        }
      },
      "required": [
        "@type",
        "assetContainerSasUrl",
        "localMediaCachePath",
        "localMediaCacheMaximumSizeMiB"
      ],
      "allOf": [
        {
          "$ref": "#/definitions/SinkNodeBase"
        }
      ],
      "description": "Enables a pipeline topology to record media to an Azure Media Services asset for subsequent playback.",
      "x-ms-discriminator-value": "#Microsoft.VideoAnalyzer.AssetSink"
    };
```
