# Azure Javas Autorest config file

> see https://aka.ms/autorest

## Configuration

```yaml
package-name: "@azure/media-analytics-edge"
title: GeneratedClient
description: Example Client
generate-metadata: false
license-header: MICROSOFT_MIT_NO_VERSION
output-folder: ../
source-code-folder-path: ./src/generated
java: true
input-file:
    - C:\Azure-Media-LiveVideoAnalytics\src\Edge\Client\AzureVideoAnalyzer.Edge\preview\1.0\AzureVideoAnalyzer.json
    - C:\Azure-Media-LiveVideoAnalytics\src\Edge\Client\AzureVideoAnalyzer.Edge\preview\1.0\AzureVideoAnalyzerSdkDefinitions.json
add-credentials: false
namespace: com.azure.media.analytics
sync-methods: none
add-context-parameter: true
models-subpackage: models
custom-types-subpacakge: models
customization-class: com.azure.media.analytics.MethodRequestCustomizations
customization-jar-path: target/azure-media-analytics-customizations-1.0.0-beta.1.jar
context-client-method-parameter: true
use: '@autorest/java@4.0.22'
```
