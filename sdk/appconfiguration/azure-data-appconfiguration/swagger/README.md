# Azure App Configuration for Java

> see https://aka.ms/autorest

This is the AutoRest configuration file for App Configuration.

---
## Getting Started
To build the SDK for App Configuration, simply [Install AutoRest](https://aka.ms/autorest) and
in this folder, run:

> `autorest`

To see additional help and options, run:

> `autorest --help`

### Setup
```ps
npm install -g autorest
```

### Generation
```ps
cd <swagger-folder>
autorest
```

## Configuration
```yaml
namespace: com.azure.data.appconfiguration
input-file: 
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/68aa92c941547dffe3e0d980a529cdc8688faff3/specification/appconfiguration/data-plane/Microsoft.AppConfiguration/preview/2022-11-01-preview/appconfiguration.json
models-subpackage: implementation.models
custom-types-subpackage: models
custom-types: KeyValueFields,KeyValueFilter,SettingFields,SnapshotSettingFilter
customization-class: src/main/java/AppConfigCustomization.java
```

## Code Generation 
```yaml
output-folder: ..\
java: true
use: '@autorest/java@4.1.15'
enable-sync-stack: true
generate-client-interfaces: false
generate-client-as-impl: true
service-interface-as-public: true
required-fields-as-ctor-args: true
license-header: MICROSOFT_MIT_SMALL
disable-client-builder: true
add-context-parameter: true
context-client-method-parameter: true
generic-response-type: true
default-http-exception-type: com.azure.core.exception.HttpResponseException
```

### Renames
```yaml
directive:
  - rename-model:
      from: KeyValueFilter
      to: SnapshotSettingFilter
  - from: swagger-document
    where: $.parameters.KeyValueFields
    transform: >
      $.items["x-ms-enum"].name = "SettingFields"; 
```
