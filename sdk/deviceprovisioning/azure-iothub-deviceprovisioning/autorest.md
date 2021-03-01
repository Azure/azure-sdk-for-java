# Azure.DeviceProvisoning

## Code generation

Run `generate.ps1` in this directory to generate the code.

## AutoRest Configuration

> see <https://aka.ms/autorest>

### Code generation settings

``` yaml
#When generating from the official specifications repository
#input-file: 

# A local copy of the swagger will be used for generation until the swagger gets into the specifications repo
#if you want to generate using local swagger copy:
# Using a slightly modified swagger until service team fixes a bug in their swagger.
input-file: $(this-folder)/swagger/2021-02-01-preview/service_modified.json

output-folder: "./"
license-header: MICROSOFT_MIT_SMALL
use: '@autorest/java@4.0.17'
java:
    add-context-parameter: true
    namespace: com.azure.deviceprovisioning
    add-credentials: true
    sync-methods: none
    client-side-validations: true
    generate-client-as-impl: true
    implementation-subpackage: implementation
    models-subpackage: implementation.models
    context-client-method-parameter: true
    custom-types-subpackage: models
    required-fields-as-ctor-args: true
```
