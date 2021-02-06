# Release History

## 1.0.0-beta.4 (Unreleased)
### Added
- Added support for Azure Active Directory Authentication.

### Breaking Changes
- CommunicationIdentityClient and CommunicationIdentityAsyncClient is moved to a new package, `azure-communication-identity`.

## 1.0.0-beta.3 (2020-11-16)
### Added
- Support directly passing connection string to the CommunicationIdentityClientBuilder.
- Added support for sync and async long-running operations
    - beginCreateReservation
    - beginPurchaseReservation
    - beginReleasePhoneNumber

### Breaking Changes
- Removed credential(CommunicationClientCredential credential) and replaced with 
accessKey(String accessKey) within CommunicationIdentityClientBuilder.
- `PhoneNumberSearch` renamed to `PhoneNumberReservation`.
- `SearchStatus` renamed to `ReservationStatus`.
- `CreateSearchOptions` reanamed to `CreateReservationOptions`.
- `CreateSearchResponse` renamed to `CreateReservationResponse`.

#### PhoneNumberReservation
- `searchId` renamed to `reservationId`.
- `getSearchId` renamed to `getReservationId`.
- `setSearchId` renamed to `setReservationId`.

#### Phone Number Clients
- `getSearchId`renamed to `getReservationId`
- `getSearchByIdWithResponse`renamed to `getReservationByIdWithResponse`.
- `createSearchWithResponse`renamed to `createReservationWithResponse`.
- `listAllSearches`renamed to `listAllReservations`.
- `cancelSearch`renamed to `cancelReservation`.
- `cancelSearchWithResponse`renamed to `cancelReservationWithResponse`.
- Replaced`createSearch`with to `beginCreateReservation` which returns a poller for the long-running operation.
- Replaced `purchaseSearch`renamed to `beginPurchaseReservation` which returns a poller for the long-running operation.
- Replaced `releasePhoneNumber`renamed to `beginReleasePhoneNumber` which returns a poller for the long-running operation.


## 1.0.0-beta.2 (2020-10-06)
Added phone number administration. For more information, please see the [README][read_me] and [documentation][documentation].

## 1.0.0-beta.1 (2020-09-22)
This is the initial release of Azure Communication Administration, which manages users and tokens for Azure Communication Services. For more information, please see the [README][read_me] and [documentation][documentation].

This is a Public Preview version, so breaking changes are possible in subsequent releases as we improve the product. To provide feedback, please submit an issue in our [Azure SDK for Java GitHub repo](https://github.com/Azure/azure-sdk-for-java/issues).

<!-- LINKS -->
[read_me]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/communication/azure-communication-administration/README.md
[documentation]: https://docs.microsoft.com/azure/communication-services/quickstarts/access-tokens?pivots=programming-language-java
