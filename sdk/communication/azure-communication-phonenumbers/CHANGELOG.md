# Release History

## 1.1.0-beta.9 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.1.0-beta.8 (2022-07-18)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.29.1` to version `1.30.0`
- Upgraded `azure-communication-common` from `1.1.4` to version `1.1.5`

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


