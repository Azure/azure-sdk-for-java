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
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/37b08248be630e7abece6a4baac27f44d607b0ba/specification/communication/data-plane/Microsoft.CommunicationServicesAdministration/stable/2021-03-07/phonenumbers.json
override-client-name: PhoneNumberAdminClient
custom-types: AcquiredPhoneNumber,BillingFrequency,CommunicationError,PhoneNumberOperationResult,PhoneNumberOperationStatus,PhoneNumberOperationStatusCodes,PhoneNumberOperationType,PhoneNumberUpdateRequest,PhoneNumberAssignmentType,PhoneNumberCapabilities,PhoneNumberCapabilitiesRequest,PhoneNumberCapabilityValue,PhoneNumberCost,PhoneNumberSearchRequest,PhoneNumberSearchResult,PhoneNumberType
custom-types-subpackage: models
models-subpackage: implementation.models
```

### Rename PhoneNumberSearch to PhoneNumberReservation

``` yaml
directive:
    - rename-model:
        from: PhoneNumberOperation
        to: PhoneNumberOperationResult
```

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
