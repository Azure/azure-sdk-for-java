# Azure Communication Phone Numbers library for Java

> see https://aka.ms/autorest
## Getting Started

To build the SDK for Communication Phone Numbers library, simply Install AutoRest and in this folder, run:

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
autorest README.md --java --v4 --use=@autorest/java@4.0.2
```

### Code generation settings
``` yaml
tag: package-phonenumber-2022-01-11-preview2
require: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/02fce64677f78021ba70d69bb8bd0d916d653927/specification/communication/data-plane/PhoneNumbers/readme.md
override-client-name: PhoneNumberAdminClient
custom-types: PurchasedPhoneNumber,BillingFrequency,PhoneNumberOperationStatus,PhoneNumberOperationStatusCodes,PhoneNumberOperationType,PhoneNumberAssignmentType,PhoneNumberCapabilities,PhoneNumberCapabilityType,PhoneNumberCost,PhoneNumberSearchResult,PhoneNumberType,PhoneNumberCapability
custom-types-subpackage: models
models-subpackage: implementation.models
java: true
output-folder: ..\
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.communication.phonenumbers
generate-client-as-impl: true
service-interface-as-public: true
sync-methods: all
context-client-method-parameter: true
```

### Add readonly attribute to PurchasedPhoneNumber properties
```yaml
directive:
  - from: swagger-document
    where: $.definitions.PurchasedPhoneNumber
    transform: >
      $["properties"]["id"].readOnly = true;
      $["properties"]["phoneNumber"].readOnly = true;
      $["properties"]["phoneNumberType"].readOnly = true;
      $["properties"]["countryCode"].readOnly = true;
      $["properties"]["capabilities"].readOnly = true;
      $["properties"]["assignmentType"].readOnly = true;
      $["properties"]["purchaseDate"].readOnly = true;
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

### Rename PhoneNumberOperation to PhoneNumberRawOperation
``` yaml
directive:
    - rename-model:
        from: PhoneNumberOperation
        to: PhoneNumberRawOperation
```