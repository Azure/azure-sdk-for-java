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
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/e23190a5bc64cd8526d08b6c2c1d616939bc88b3/specification/communication/data-plane/Microsoft.CommunicationServicesPhoneNumbers/stable/2021-03-07/phonenumbers.json
override-client-name: PhoneNumberAdminClient
custom-types: AcquiredPhoneNumber,BillingFrequency,CommunicationError,PhoneNumberOperation,PhoneNumberOperationStatus,PhoneNumberOperationStatusCodes,PhoneNumberOperationType,PhoneNumberUpdateRequest,PhoneNumberAssignmentType,PhoneNumberCapabilities,PhoneNumberCapabilitiesRequest,PhoneNumberCapabilityValue,PhoneNumberCost,PhoneNumberSearchRequest,PhoneNumberSearchResult,PhoneNumberType
custom-types-subpackage: models
models-subpackage: implementation.models
java: true
output-folder: ..\
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.communication.phonenumbers
generate-client-as-impl: true
sync-methods: all
context-client-method-parameter: true
```

### Add readonly attribute to AcquiredPhoneNumber properties
```yaml
directive:
  - from: swagger-document
    where: $.definitions.AcquiredPhoneNumber
    transform: >
      $["properties"]["id"].readOnly = true;
      $["properties"]["phoneNumber"].readOnly = true;
      $["properties"]["phoneNumberType"].readOnly = true;
      $["properties"]["countryCode"].readOnly = true;
      $["properties"]["capabilities"].readOnly = true;
      $["properties"]["assignmentType"].readOnly = true;
      $["properties"]["purchaseDate"].readOnly = true;
      $["properties"]["callbackUri"].readOnly = true;
      $["properties"]["applicationId"].readOnly = true;
      $["properties"]["cost"].readOnly = true;
```

### Add readonly attribute to PhoneNumberCost properties
```yaml
directive:
  - from: swagger-document
    where: $.definitions.PhoneNumberCost
    transform: >
      $["properties"]["amount"].readOnly = true;
      $["properties"]["currencyCode"].readOnly = true;
      $["properties"]["billingFrequency"].readOnly = true;
```

### Add readonly attribute to PhoneNumberOperation properties
```yaml $(java)
directive:
  - from: swagger-document
    where: $.definitions.PhoneNumberOperation
    transform: >
      $["properties"]["status"].readOnly = true;
      $["properties"]["resourceLocation"].readOnly = true;
      $["properties"]["createdDateTime"].readOnly = true;
      $["properties"]["error"].readOnly = true;
      $["properties"]["id"].readOnly = true;
      $["properties"]["operationType"].readOnly = true;
      $["properties"]["lastActionDateTime"].readOnly = true;
```

### Add readonly attribute to PhoneNumberSearchResult properties
```yaml
directive:
  - from: swagger-document
    where: $.definitions.PhoneNumberSearchResult
    transform: >
      $["properties"]["searchId"].readOnly = true;
      $["properties"]["phoneNumbers"].readOnly = true;
      $["properties"]["cost"].readOnly = true;
      $["properties"]["searchExpiresBy"].readOnly = true;
      $["properties"]["phoneNumberType"].readOnly = true;
      $["properties"]["assignmentType"].readOnly = true;
      $["properties"]["capabilities"].readOnly = true;
```
