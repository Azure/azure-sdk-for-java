# Azure.DigitalTwins.Core

Run `generate.ps1` in this directory to generate the code.

## AutoRest Configuration

> see <https://aka.ms/autorest>

### Code generation settings

``` yaml
input-file: swagger/digitaltwins.json
output-folder: "./"
license-header: MICROSOFT_MIT_SMALL
use: '@autorest/java@4.0.1'
java:
    add-context-parameter: true
    namespace: com.azure.digitaltwins.core
    add-credentials: true
    sync-methods: none
```
