# Azure App Configuration for Java

> see https://aka.ms/autorest

### Setup
```ps
npm install -g autorest
```

### Generation
```ps
cd <swagger-folder>
autorest
```

### Code generation settings
``` yaml
use: '@autorest/java@4.1.15'
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/68aa92c941547dffe3e0d980a529cdc8688faff3/specification/appconfiguration/data-plane/Microsoft.AppConfiguration/preview/2022-11-01-preview/appconfiguration.json
java: true
output-folder: ..\
generate-client-as-impl: true
disable-client-builder: true
namespace: com.azure.data.appconfiguration
generate-client-interfaces: false
enable-sync-stack: true
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
models-subpackage: implementation.models
custom-types-subpackage: models
context-client-method-parameter: true
service-interface-as-public: true
generic-response-type: true
default-http-exception-type: com.azure.core.exception.HttpResponseException
stream-style-serialization: true
```
