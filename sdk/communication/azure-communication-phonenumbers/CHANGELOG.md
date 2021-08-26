# Release History

## 1.1.0-beta.1 (2021-08-27)
- Using released version of azure-communication-common.

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


