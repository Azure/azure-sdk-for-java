# Azure.DigitalTwins.Core

## Code generation

Run `generate.ps1` in this directory to generate the code.

## AutoRest Configuration

> see <https://aka.ms/autorest>

### Code generation settings

``` yaml
tag: package-2020-03
require: 
  - https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/iothub/resource-manager/readme.md
```

``` yaml
output-folder: "./"
license-header: MICROSOFT_MIT_SMALL
use: '@autorest/java@4.0.4'
java:
    namespace: com.azure.resourcemanager.iothub
    fluent: true
```
