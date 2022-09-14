# Azure Timezone

> see https://aka.ms/autorest

### Setup
> see https://github.com/Azure/autorest.java

### Generation
> see https://github.com/Azure/autorest.java/releases for the latest version of autorest
```ps
cd <swagger-folder>
mvn install
autorest --java --use:@autorest/java@4.0.x
```

### Code generation settings

## Java

``` yaml

title: TimezoneClient
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/specification/maps/data-plane/Timezone/preview/1.0/timezone.json
namespace: com.azure.maps.timezone
java: true
output-folder: ../
license-header: MICROSOFT_MIT_NO_CODEGEN
payload-flattening-threshold: 0
add-context-parameter: true
context-client-method-parameter: true
client-logger: true
generate-client-as-impl: true
sync-methods: all
generate-sync-async-clients: false
polling: {}
models-subpackage: implementation.models
custom-types-subpackage: models
custom-types: ErrorAdditionalInfo,ErrorDetail,ErrorResponse,ErrorResponseException,CountryRecord,IanaId,TimezoneWindows,TimezoneResult,TimezoneOptions,TimezoneNames,TimezoneId,TimezoneIanaVersionResult,ReferenceTime,TimeTransition
customization-jar-path: target/azure-maps-timezone-customization-1.0.0-beta.1.jar
customization-class: TimezoneCustomization
```