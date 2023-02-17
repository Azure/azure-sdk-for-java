# Azure App Configuration for Java

> see https://aka.ms/autorest

### Setup
```ps
Fork and clone https://github.com/Azure/autorest.java 
git checkout main
git submodule update --init --recursive
mvn package -Dlocal
npm install
npm install -g autorest
```

### Generation
```ps
cd <swagger-folder>
autorest --java --use:@autorest/java@4.1.9 README.md
```

### Code generation settings
``` yaml
use: '@autorest/java@4.1.9'
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/3751704f5318f1175875c94b66af769db917f2d3/specification/appconfiguration/data-plane/Microsoft.AppConfiguration/preview/2022-11-01-preview/appconfiguration.json
java: true
output-folder: ..\
generate-client-as-impl: true
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
```
