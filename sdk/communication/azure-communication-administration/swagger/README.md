# Azure Communication Administration library for Java

> see https://aka.ms/autorest
## Getting Started

To build the SDK for Communication Administration library, simply Install AutoRest and in this folder, run:

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
autorest README.md --java --v4 --use=@autorest/java@4.0.1 --tag=phonenumber
```

### Tag: phonenumber

These settings apply only when `--tag=phonenumber` is specified on the command line.

``` yaml $(tag) == 'phonenumber'
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/257f060be8b60d8468584682aa2d71b1faa5f82c/specification/communication/data-plane/Microsoft.CommunicationServicesAdministration/preview/2020-07-20-preview1/communicationservicesadministration.json
override-client-name: PhoneNumberAdminClient
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
namespace: com.azure.communication.administration
generate-client-as-impl: true
custom-types-subpackage: models
sync-methods: all
context-client-method-parameter: true
```
