# Azure.DigitalTwins.Core

Run `generate.ps1` in this directory to generate the code.

## AutoRest Configuration

> see <https://aka.ms/autorest>

### Code generation settings

``` yaml
input-file: swagger/digitaltwins.json
output-folder: "./"
license-header: MICROSOFT_MIT_SMALL
java:
    add-context-parameter: true
    namespace: com.azure.digitaltwins.core
    add-credentials: true
    sync-methods: none
```
