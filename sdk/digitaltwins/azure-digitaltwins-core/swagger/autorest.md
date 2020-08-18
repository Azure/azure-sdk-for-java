# Azure.DigitalTwins.Core

Run `generate.ps1` in this directory to generate the code.

## AutoRest Configuration

> see <https://aka.ms/autorest>

### Code generation settings

``` yaml
#When generating from the official specifications repository
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/digitaltwins/data-plane/Microsoft.DigitalTwins/preview/2020-05-31-preview/digitaltwins.json

#When generating from the local copy:
#input-file: 2020-05-31-preview/digitaltwins.json

output-folder: "../"
license-header: MICROSOFT_MIT_SMALL
use: '@autorest/java@4.0.1'
java:
    add-context-parameter: true
    namespace: com.azure.digitaltwins.core
    add-credentials: true
    sync-methods: none
```
