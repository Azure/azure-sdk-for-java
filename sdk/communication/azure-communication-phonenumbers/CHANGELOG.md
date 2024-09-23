# Release History

## 1.1.17 (2024-09-23)

### Other Changes

#### Dependency Updates

- Upgraded `azure-communication-common` from `1.3.5` to version `1.3.7`.
- Upgraded `azure-core` from `1.51.0` to version `1.52.0`.


## 1.1.16 (2024-08-24)

### Other Changes

#### Dependency Updates

- Upgraded `azure-communication-common` from `1.3.4` to version `1.3.5`.
- Upgraded `azure-core` from `1.50.0` to version `1.51.0`.


## 1.1.15 (2024-07-26)

### Other Changes

#### Dependency Updates

- Upgraded `azure-communication-common` from `1.3.3` to version `1.3.4`.
- Upgraded `azure-core` from `1.49.1` to version `1.50.0`.


## 1.1.14 (2024-06-27)

### Other Changes

#### Dependency Updates

- Upgraded `azure-communication-common` from `1.3.2` to version `1.3.3`.
- Upgraded `azure-core` from `1.49.0` to version `1.49.1`.


## 1.1.13 (2024-05-28)

### Other Changes

#### Dependency Updates

- Upgraded `azure-communication-common` from `1.3.1` to version `1.3.2`.
- Upgraded `azure-core` from `1.48.0` to version `1.49.0`.


## 1.1.12 (2024-04-23)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.47.0` to version `1.48.0`.
- Upgraded `azure-communication-common` from `1.3.1` to version `1.3.2`.


## 1.1.11 (2024-03-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.46.0` to version `1.47.0`.


## 1.1.10 (2024-02-22)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.45.1` to version `1.46.0`.
- Upgraded `azure-communication-common` from `1.2.14` to version `1.3.0`.


## 1.1.9 (2023-12-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-communication-common` from `1.2.14` to version `1.2.15`.
- Upgraded `azure-core` from `1.45.0` to version `1.45.1`.


## 1.1.8 (2023-11-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.44.1` to version `1.45.0`.
- Upgraded `azure-communication-common` from `1.2.13` to version `1.2.14`.


## 1.1.7 (2023-10-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.43.0` to version `1.44.1`.
- Upgraded `azure-communication-common` from `1.2.12` to version `1.2.13`.


## 1.1.6 (2023-09-22)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.42.0` to version `1.43.0`.
- Upgraded `azure-communication-common` from `1.2.11` to version `1.2.12`.


## 1.1.5 (2023-08-18)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.41.0` to version `1.42.0`.
- Upgraded `azure-communication-common` from `1.2.10` to version `1.2.11`.


## 1.1.4 (2023-07-25)

### Other Changes

#### Dependency Updates

- Upgraded `azure-communication-common` from `1.2.9` to version `1.2.10`.
- Upgraded `azure-core` from `1.40.0` to version `1.41.0`.


## 1.1.3 (2023-06-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.39.0` to version `1.40.0`.
- Upgraded `azure-communication-common` from `1.2.8` to version `1.2.9`.


## 1.1.2 (2023-05-23)

### Other Changes

#### Dependency Updates

- Upgraded `azure-communication-common` from `1.2.6` to version `1.2.8`.
- Upgraded `azure-core` from `1.38.0` to version `1.39.0`.


## 1.1.1 (2023-04-21)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.37.0` to version `1.38.0`.


## 1.1.0 (2023-03-28)

### Features Added
- GA release of SIP routing client.
- GA release of Phone Numbers Browse API Methods.

## 1.1.0-beta.15 (2023-03-15)

### Features Added
- Added support for SIP routing API version `2023-03-01`, releasing SIP routing functionality from public preview to GA.
- Added environment variable `AZURE_TEST_DOMAIN` for SIP routing tests to support domain verification.

### Other Changes
- Changed listTrunks and listRoutes methods to return PagedIterable for sync client and PagedFlux for async client.
- Moved SIP routing clients to com.azure.communication.phonenumbers.siprouting subpackage.
- Added `PhoneNumberAreaCode` public model.
- Removed `PhoneNumberOfferings`, `PhoneNumberLocalities` and `PhoneNumberCountries` from the models package. Since no public method exposed them, this is not a breaking change.

#### Dependency Updates

- Upgraded `azure-core` from `1.36.0` to version `1.37.0`.
- Upgraded `azure-communication-common` from `1.2.5` to version `1.2.6`.

## 1.0.20 (2023-03-13)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.36.0` to version `1.37.0`.
- Upgraded `azure-communication-common` from `1.2.5` to version `1.2.6`.

## 1.1.0-beta.14 (2023-02-14)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.35.0` to version `1.36.0`.
- Upgraded `azure-communication-common` from `1.2.4` to version `1.2.5`.


## 1.0.19 (2023-02-14)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.35.0` to version `1.36.0`.
- Upgraded `azure-communication-common` from `1.2.4` to version `1.2.5`.


## 1.0.18 (2023-01-13)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.34.0` to version `1.35.0`.
- Upgraded `azure-communication-common` from `1.2.3` to version `1.2.4`.

## 1.1.0-beta.13 (2023-01-10)
- Adds support for Azure Communication Services Phone Numbers Browse API Methods.

### Features Added
- Added support for API version `2022-12-01`, giving users the ability to: 
  - Get all supported countries
  - Get all supported localities given a country code.
  - Get all Toll-Free area codes from a given country code.
  - Get all Geographic area codes from a given country code / locality.
  - Get all offerings from a given country code.

## 1.1.0-beta.12 (2022-11-14)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` from `1.33.0` to version `1.34.0`
- Upgraded `azure-communication-common` from `1.2.2` to version `1.2.3`

## 1.0.17 (2022-11-09)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.33.0` to version `1.34.0`.
- Upgraded `azure-communication-common` from `1.2.2` to version `1.2.3`.

## 1.1.0-beta.11 (2022-10-18)

### Features Added

- Added SIP routing clients for handling Direct routing numbers.


### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.32.0` to version `1.33.0`
- Upgraded `azure-communication-common` from `1.2.1` to version `1.2.2`


## 1.0.16 (2022-10-18)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` from `1.32.0` to version `1.33.0`
- Upgraded `azure-communication-common` from `1.2.1` to version `1.2.2`

## 1.1.0-beta.10 (2022-09-13)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.31.0` to version `1.32.0`
- Upgraded `azure-communication-common` from `1.2.0` to version `1.2.1`

## 1.0.15 (2022-09-09)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.31.0` to version `1.32.0`.
- Upgraded `azure-communication-common` from `1.2.0` to version `1.2.1`.

## 1.1.0-beta.9 (2022-08-11)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.30.0` to version `1.31.0`
- Upgraded `azure-communication-common` from `1.1.5` to version `1.2.0`

## 1.0.14 (2022-08-11)

### Other Changes

#### Dependency Updates
- 
- Upgraded `azure-core` from `1.30.0` to version `1.31.0`.
- Upgraded `azure-communication-common` from `1.1.5` to version `1.2.0`.

## 1.1.0-beta.8 (2022-07-18)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.29.1` to version `1.30.0`
- Upgraded `azure-communication-common` from `1.1.4` to version `1.1.5`

## 1.0.13 (2022-07-14)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.29.1` to version `1.30.0`.
- Upgraded `azure-communication-common` from `1.1.4` to version `1.1.5`.

## 1.1.0-beta.7 (2022-06-13)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.28.0` to version `1.29.1`
- Upgraded `azure-communication-common` from `1.1.3` to version `1.1.4`
- Upgraded `azure-identity` from `1.5.1` to `1.5.2`

## 1.0.12 (2022-06-13)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.28.0` to version `1.29.1`
- Upgraded `azure-communication-common` from `1.1.3` to version `1.1.4`

## 1.0.11 (2022-05-17)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.27.0` to version `1.28.0`.
- Upgraded `azure-communication-common` from `1.1.2` to version `1.1.3`.

## 1.1.0-beta.5 (2022-05-11)

### Other Changes

#### Dependency updates
- Upgraded `azure-communication-common` to 1.1.3
- Upgraded `azure-core` to 1.28.0

## 1.1.0-beta.4 (2022-04-11)

### Other Changes

#### Dependency updates
- Upgraded `azure-communication-common` to 1.1.2
- Upgraded `azure-core` to 1.27.0
- Upgraded `azure-identity` to 1.5.0

## 1.1.0-beta.3 (2022-03-15)

### Features Added
- Added interfaces from `com.azure.core.client.traits` to `PhoneNumbersClientBuilder`
- Added `retryOptions` to `PhoneNumbersClientBuilder`
- Added environment variable `AZURE_USERAGENT_OVERRIDE`, that overrides the HTTP header `x-ms-useragent` on the tests
- Upgraded `azure-communication-common` to 1.1.1
- Upgraded `azure-identity` to 1.4.6

## 1.1.0-beta.2 (2022-02-17)

### Other Changes

#### Dependency updates
- Upgraded `azure-communication-common` to 1.0.8
- Upgraded `azure-core` to 1.25.0
- Upgraded `azure-identity` to 1.4.4

## 1.1.0-beta.1 (2022-01-24)

### Features Added
- Users can now purchase United Kingdom (GB) toll free and geographic phone numbers for PSTN Calling
- Users can now purchase Denmark (DK) toll free and geographic phone numbers for PSTN Calling

## 1.0.6 (2021-11-18)

### Other Changes

#### Dependency updates
- Upgraded `azure-communication-common` to 1.0.6
- Upgraded `azure-core` to 1.22.0
- Upgraded `azure-identity` to 1.4.1

## 1.0.5 (2021-10-06)

### Other Changes

#### Dependency updates
- Upgraded `azure-core` to 1.21.0

## 1.0.4 (2021-09-22)

### Other Changes

#### Dependency updates
- Upgraded `azure-communication-common` to 1.0.4
- Upgraded `azure-core` to 1.20.0

## 1.0.3 (2021-06-09)
Updated `azure-communication-phonenumbers` version

## 1.0.2 (2021-05-27)
- Dependency versions updated.

## 1.0.1 (2021-04-30)
### Bug fixes
- Remove dependency on unreleased azure-communication-common version

## 1.0.0 (2021-04-26)
- Update version

## 1.0.0-beta.7 (2021-03-29)
### Added
- Added `PollerFlux<PhoneNumberOperation, PhoneNumberSearchResult> beginSearchAvailablePhoneNumbers(String countryCode, PhoneNumberType phoneNumberType, PhoneNumberAssignmentType assignmentType, PhoneNumberCapabilities capabilities)` in PhoneNumbersAsyncClient.
- Added `PagedIterable<PurchasedPhoneNumber> listPurchasedPhoneNumbers()` in PhoneNumbersClient.
- Added `SyncPoller<PhoneNumberOperation, PhoneNumberSearchResult> beginSearchAvailablePhoneNumbers(String countryCode, PhoneNumberType phoneNumberType, PhoneNumberAssignmentType assignmentType, PhoneNumberCapabilities capabilities)` in PhoneNumbersClient.
- Added `SyncPoller<PhoneNumberOperation, PurchasePhoneNumbersResult> beginPurchasePhoneNumbers(String searchId)` in PhoneNumbersClient.
- Added `SyncPoller<PhoneNumberOperation, ReleasePhoneNumberResult> beginReleasePhoneNumber(String phoneNumber)` in PhoneNumbersClient.
- Added `SyncPoller<PhoneNumberOperation, PurchasedPhoneNumber> beginUpdatePhoneNumberCapabilities(String phoneNumber, PhoneNumberCapabilities capabilities)` in PhoneNumbersClient.
- Added `PurchasePhoneNumbersResult`.
- Added `ReleasePhoneNumbersResult`.

### Breaking Changes
- Renamed AcquiredPhoneNumber to PurchasedPhoneNumber.
- Renamed PhoneNumbersAsyncClient.getPhoneNumber and PhoneNumbersClient.getPhoneNumber to PhoneNumbersAsyncClient.getPurchasedPhoneNumber and PhoneNumbersClient.getPurchasedPhoneNumber.
- Renamed PhoneNumbersAsyncClient.getPhoneNumberWithResponse and PhoneNumbersClient.getPhoneNumberWithResponse to
PhoneNumbersAsyncClient.getPurchasedPhoneNumberWithResponse and PhoneNumbersClient.getPurchasedPhoneNumberWithResponse.
- Renamed PhoneNumbersAsyncClient.listPhoneNumbers and PhoneNumbersClient.listPhoneNumbers to PhoneNumbersAsyncClient.listPurchasedPhoneNumbers and PhoneNumbersClient.listPurchasedPhoneNumbers.
- Updated `PollerFlux<PhoneNumberOperation, Void> beginPurchasePhoneNumbers` to `PollerFlux<PhoneNumberOperation, PurchasePhoneNumbersResult> beginPurchasePhoneNumbers` in PhoneNumbersAsyncClient.
- Updated `PollerFlux<PhoneNumberOperation, Void> beginReleasePhoneNumber` to `public PollerFlux<PhoneNumberOperation, ReleasePhoneNumberResult> beginReleasePhoneNumber` in PhoneNumbersAsyncClient.
- Updated `SyncPoller<PhoneNumberOperation, Void> beginPurchasePhoneNumbers` to ` SyncPoller<PhoneNumberOperation, PurchasePhoneNumbersResult> beginPurchasePhoneNumbers` in PhoneNumbersClient.
- Updated `SyncPoller<PhoneNumberOperation, Void> beginReleasePhoneNumber` to `SyncPoller<PhoneNumberOperation, ReleasePhoneNumberResult> beginReleasePhoneNumber` in PhoneNumbersClient.
- Updated `PollerFlux<PhoneNumberOperation, PurchasedPhoneNumber> beginUpdatePhoneNumberCapabilities(String phoneNumber, PhoneNumberCapabilitiesRequest capabilitiesUpdateRequest)` to `PollerFlux<PhoneNumberOperation, PurchasedPhoneNumber> beginUpdatePhoneNumberCapabilities(String phoneNumber, PhoneNumberCapabilities capabilities)`.
- Updated `SyncPoller<PhoneNumberOperation, PurchasedPhoneNumber> beginUpdatePhoneNumberCapabilities(String phoneNumber, PhoneNumberCapabilitiesRequest capabilitiesUpdateRequest)` to `SyncPoller<PhoneNumberOperation, PurchasedPhoneNumber> beginUpdatePhoneNumberCapabilities(String phoneNumber, PhoneNumberCapabilities capabilities)`.
- Removed `CommunicationError`.
- Removed `PhoneNumberCapabilitiesRequest`.
- Moved `ReservationStatus` to the `models` folder.

## 1.0.0-beta.6 (2021-03-09)
### Added
- Added PhoneNumbersClient and PhoneNumbersAsyncClient (originally was part of the azure.communication.administration package).
- Added support for Azure Active Directory Authentication.

### Breaking Changes
- PhoneNumberAsyncClient has been replaced with PhoneNumbersAsyncClient, which has the same functionality but different APIs. To learn more about how PhoneNumbersAsyncClient works, refer to the [README.md][https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/communication/azure-communication-phonenumbers/README.md].
- PhoneNumberClient has been replaced with PhoneNumbersClient, which has the same functionality but different APIs. To learn more about how PhoneNumbersClient works, refer to the [README.md][https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/communication/azure-communication-phonenumbers/README.md].


