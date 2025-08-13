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
autorest README.md --java
```

### Code generation settings
``` yaml
tag: package-phonenumber-2025-06-01
use: '@autorest/java@4.1.52'
require: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/14800a01400c295af0bfa5886e5f4042e4f6c62e/specification/communication/data-plane/PhoneNumbers/readme.md
override-client-name: PhoneNumberAdminClient
custom-types: PurchasedPhoneNumber,BillingFrequency,PhoneNumberOperationStatus,PhoneNumberOperationStatusCodes,PhoneNumberOperationType,PhoneNumberAssignmentType,PhoneNumberCapabilities,PhoneNumberCapabilityType,PhoneNumberCost,PhoneNumberSearchResult,PhoneNumberType,PhoneNumberCapability,PhoneNumberAdministrativeDivision,PhoneNumberCountry,PhoneNumberLocality,PhoneNumberOffering,AreaCodeResult,AreaCodes,PhoneNumberAreaCode,OperatorDetails,OperatorInformation,OperatorInformationResult,OperatorInformationOptions,OperatorNumberType,PhoneNumbersReservation,AvailablePhoneNumber,AvailablePhoneNumberCost,PhoneNumberAvailabilityStatus,PhoneNumbersBrowseResult,BrowsePhoneNumbersOptions,PhoneNumbersReservationStatus
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
stream-style-serialization: true
customization-class: src/main/java/PhoneNumbersCustomization.java
```

### Set remove-empty-child-schemas
```yaml
modelerfour:
    remove-empty-child-schemas: true
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
      $["properties"]["error"].readOnly = true;
      $["properties"]["errorCode"].readOnly = true;
      $["properties"]["isAgreementToNotResellRequired"].readOnly = true;
```

### Rename PhoneNumberOperation to PhoneNumberRawOperation
``` yaml
directive:
    - rename-model:
        from: PhoneNumberOperation
        to: PhoneNumberRawOperation
```

### Add readonly attribute to AreaCodeResult properties
```yaml
directive:
  - from: swagger-document
    where: $.definitions.PhoneNumberAreaCode
    transform: >
      $["properties"]["areaCode"].readOnly = true;
```

### Add readonly attribute to AreaCodes properties
```yaml
directive:
  - from: swagger-document
    where: $.definitions.PhoneNumberAreaCodes
    transform: >
      $["properties"]["areaCodes"].readOnly = true;
      $["properties"]["nextLink"].readOnly = true;
```

### Add readonly attribute to PhoneNumberAdministrativeDivision properties
```yaml
directive:
  - from: swagger-document
    where: $.definitions.PhoneNumberAdministrativeDivision
    transform: >
      $["properties"]["abbreviatedName"].readOnly = true;
      $["properties"]["localizedName"].readOnly = true;
```

### Add readonly attribute to PhoneNumberAdministrativeDivision properties
```yaml
directive:
  - from: swagger-document
    where: $.definitions.PhoneNumberAdministrativeDivision
    transform: >
      $["properties"]["abbreviatedName"].readOnly = true;
      $["properties"]["localizedName"].readOnly = true;
```

### Add readonly attribute to PhoneNumberCountries properties
```yaml
directive:
  - from: swagger-document
    where: $.definitions.PhoneNumberCountries
    transform: >
      $["properties"]["countries"].readOnly = true;
      $["properties"]["nextLink"].readOnly = true;
```

### Add readonly attribute to PhoneNumberLocality properties
```yaml
directive:
  - from: swagger-document
    where: $.definitions.PhoneNumberLocality
    transform: >
      $["properties"]["administrativeDivision"].readOnly = true;
      $["properties"]["localizedName"].readOnly = true;
```

### Add readonly attribute to PhoneNumberLocalities properties
```yaml
directive:
  - from: swagger-document
    where: $.definitions.PhoneNumberLocalities
    transform: >
      $["properties"]["nextLink"].readOnly = true;
      $["properties"]["phoneNumberLocalities"].readOnly = true;
```

### Add readonly attribute to PhoneNumberOffering properties
```yaml
directive:
  - from: swagger-document
    where: $.definitions.PhoneNumberOffering
    transform: >
      $["properties"]["assignmentType"].readOnly = true;
      $["properties"]["availableCapabilities"].readOnly = true;
      $["properties"]["cost"].readOnly = true;
      $["properties"]["phoneNumberType"].readOnly = true;
```

### Add readonly attribute to PhoneNumberOfferings properties
```yaml
directive:
  - from: swagger-document
    where: $.definitions.OfferingsResponse
    transform: >
      $["properties"]["nextLink"].readOnly = true;
      $["properties"]["phoneNumberOfferings"].readOnly = true;
```

### Add readonly attribute to PhoneNumberCountry properties
```yaml
directive:
  - from: swagger-document
    where: $.definitions.PhoneNumberCountry
    transform: >
      $["properties"]["localizedName"].readOnly = true;
      $["properties"]["countryCode"].readOnly = true;
```

### Add readonly attribute to OperatorDetails properties
```yaml
directive:
  - from: swagger-document
    where: $.definitions.OperatorDetails
    transform: >
      $["properties"]["name"].readOnly = true;
      $["properties"]["mobileNetworkCode"].readOnly = true;
      $["properties"]["mobileCountryCode"].readOnly = true;
```

### Add readonly attribute to OperatorInformation properties
```yaml
directive:
  - from: swagger-document
    where: $.definitions.OperatorInformation
    transform: >
      $["properties"]["phoneNumber"].readOnly = true;
      $["properties"]["numberType"].readOnly = true;
      $["properties"]["isoCountryCode"].readOnly = true;
      $["properties"]["operatorDetails"].readOnly = true;
      $["properties"]["nationalFormat"].readOnly = true;
      $["properties"]["internationalFormat"].readOnly = true;
```

### Add readonly attribute to OperatorInformationResult properties
```yaml
directive:
  - from: swagger-document
    where: $.definitions.OperatorInformationResult
    transform: >
      $["properties"]["values"].readOnly = true;
```

### Rename ReservationStatus to PhoneNumbersReservationStatus
``` yaml
directive:
  from: swagger-document
  where: $.definitions.PhoneNumbersReservation.properties.status.x-ms-enum
  transform: >
    $["name"] = "PhoneNumbersReservationStatus";
```

``` yaml
directive:
  from: swagger-document
  where: $.definitions.PhoneNumberSearchResult.properties.error.x-ms-enum
  transform: >
    $["name"] = "PhoneNumberSearchResultError";
```

``` yaml
directive:
  from: swagger-document
  where: $.parameters.Endpoint
  transform: >
    $["format"] = "";
```

### Rename AvailablePhoneNumberStatus to PhoneNumberAvailabilityStatus
```yaml
directive:
  from: swagger-document
  where: $.definitions.AvailablePhoneNumber.properties.status.x-ms-enum
  transform: >
    $["name"] = "PhoneNumberAvailabilityStatus";
```

### Add readonly attribute to AvailablePhoneNumber properties
```yaml
directive:
  - from: swagger-document
    where: $.definitions.AvailablePhoneNumber
    transform: >
      $["properties"]["assignmentType"].readOnly = true;
      $["properties"]["capabilities"].readOnly = true;
      $["properties"]["countryCode"].readOnly = true;
      $["properties"]["phoneNumberType"].readOnly = true;
      $["properties"]["status"].readOnly = true;
```

### Add readonly attribute to PhoneNumbersReservation properties
```yaml
directive:
  - from: swagger-document
    where: $.definitions.PhoneNumbersReservation
    transform: >
      $["properties"]["phoneNumbers"].readOnly = true;
```

### Replace type from PhoneNumberBrowseCapabilities to PhoneNumberCapabilties
```yaml
directive:
  - from: swagger-document
    where: $.definitions.PhoneNumbersBrowseRequest.properties.capabilities
    transform: >
      $.type = "object";
      $.$ref = "#/definitions/PhoneNumberCapabilities";
```

## Directives to add the countryCode to the PhoneNumbersBrowseRequest
 ``` yaml
 directive:
   - from: swagger-document
     where: $.definitions.PhoneNumbersBrowseRequest.properties
     transform: >
       $.countryCode = {
         "type": "string",
         "description": "The ISO 3166-2 country code, e.g. US.",
         "x-ms-mutability": ["read", "create", "update"]
       }
 ```

### Rename from PhoneNumbersBrowseRequest to BrowsePhoneNumbersOptions
``` yaml
directive:
    - rename-model:
        from: PhoneNumbersBrowseRequest
        to: BrowsePhoneNumbersOptions
```

### Set PhoneNumbersBrowseResult fields as readonly
```yaml
directive:
  - from: swagger-document
    where: $.definitions.PhoneNumbersBrowseResult
    transform: >
      $["properties"]["phoneNumbers"].readOnly = true;
```
