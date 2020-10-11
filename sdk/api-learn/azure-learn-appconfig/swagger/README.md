# Azure App Configuration Tutorial for Java

> see https://aka.ms/autorest

### Code generation settings
``` yaml
input-file: appconfiguration.json
use: '@autorest/java@4.0.3'
java: true
output-folder: ..\
generate-client-as-impl: true
namespace: com.azure.learn.appconfig
generate-client-interfaces: false
sync-methods: none
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
models-subpackage: implementation.models
context-client-method-parameter: true
artifact-id: azure-learn-appconfig
credential-types: tokencredential
custom-types-subpackage: models
custom-types: ConfigurationSetting
required-fields-as-ctor-args: true
directive:
    - rename-model:
        from: KeyValue
        to: ConfigurationSetting
```
