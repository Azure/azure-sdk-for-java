# Azure Communication Phone Numbers library for Java

> see https://aka.ms/autorest
## Getting Started

To build the SDK for Communication Phone Numbers library, simply Install AutoRest and in this folder, run:

### Setup
```ps
Fork and clone https://github.com/Azure/autorest.java
git checkout v4
git submodule update --init --recursive
mvn package -Dlocal
npm install
npm install -g autorest
```

### Generation
```ps
cd <swagger-folder>
autorest README.md --java --v4 --use=@autorest/java@4.0.2
```

### Code generation settings
``` yaml
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/21b4154a41d72275dbcf40e24d55b978e3a42c85/specification/communication/data-plane/Microsoft.CommunicationServicesAdministration/stable/2021-03-07/phonenumbers.json
override-client-name: PhoneNumberAdminClient
custom-types: AcquiredPhoneNumber,PhoneNumberUpdateRequest,PhoneNumberAssignmentType,PhoneNumberCapabilities,PhoneNumberCapabilitiesRequest,PhoneNumberCapabilityValue,PhoneNumberCost,PhoneNumberSearchRequest,PhoneNumberSearchResult,PhoneNumberType
custom-types-subpackage: models
models-subpackage: implementation.models
```

### Rename searchId to reservationId in CreateSearchResponse

``` yaml
directive:
  - from: swagger-document
    where: $.definitions.CreateSearchResponse.properties.searchId
    transform: >
      $["x-ms-client-name"] = "reservationId";
```
### Rename searchId to reservationId in PhoneNumberSearch 

``` yaml
directive:
  - from: swagger-document
    where: $.definitions.PhoneNumberSearch.properties.searchId
    transform: >
      $["x-ms-client-name"] = "reservationId";
```

### Rename PhoneNumberSearch to PhoneNumberReservation

``` yaml
directive:
    - rename-model:
        from: PhoneNumberSearch
        to: PhoneNumberReservation
```

### Rename CreateSearchOptions to CreateReservationOptions

``` yaml
directive:
    - rename-model:
        from: CreateSearchOptions
        to: CreateReservationOptions
```

### Rename CreateSearchResponse to CreateReservationResponse

``` yaml
directive:
    - rename-model:
        from: CreateSearchResponse
        to: CreateReservationResponse
```

### Code generation settings

``` yaml
java: true
output-folder: ..\
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.communication.phonenumbers
generate-client-as-impl: true
custom-types-subpackage: models
sync-methods: all
context-client-method-parameter: true
```
