# Azure Javas Autorest config file

> see https://aka.ms/autorest

## Configuration

```yaml
package-name: "@azure/media-video-analyzer-edge"
title: GeneratedClient
description: This package contains Microsoft Azure SDK for Media Video Analyzer Edge.
generate-metadata: false
license-header: MICROSOFT_MIT_NO_VERSION
output-folder: ../
source-code-folder-path: ./src/generated
java: true
input-file:
    - C:\Azure-Media-LiveVideoAnalytics\src\Edge\Client\AzureVideoAnalyzer.Edge\preview\1.0\AzureVideoAnalyzer.json
    - C:\Azure-Media-LiveVideoAnalytics\src\Edge\Client\AzureVideoAnalyzer.Edge\preview\1.0\AzureVideoAnalyzerSdkDefinitions.json
add-credentials: false
namespace: com.azure.media.video.analyzer.edge
sync-methods: none
add-context-parameter: true
models-subpackage: models
custom-types-subpacakge: models
customization-class: MethodRequestCustomizations
customization-jar-path: target/azure-media-video-analyzer-customizations-1.0.0-beta.1.jar
context-client-method-parameter: true
use: '@autorest/java@4.0.22'
model-override-setter-from-superclass: true
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
        $[definitionKeys[i]].required = ["@apiVersion"];
        delete $[definitionKeys[i]].properties.methodName;
        delete $[definitionKeys[i]].discriminator;
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
            "readOnly": true,
            "enum": [value]
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
