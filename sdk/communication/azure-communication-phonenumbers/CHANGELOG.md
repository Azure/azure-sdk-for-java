# Release History

## 1.0.0-beta.7 (Unreleased)
### Breaking Changes
- Renamed AcquiredPhoneNumber to PurchasedPhoneNumber.
- Renamed PhoneNumbersAsyncClient.getPhoneNumber and PhoneNumbersClient.getPhoneNumber to PhoneNumbersAsyncClient.getPurchasedPhoneNumber and PhoneNumbersClient.getPurchasedPhoneNumber.
- Renamed PhoneNumbersAsyncClient.getPhoneNumberWithResponse and PhoneNumbersClient.getPhoneNumberWithResponse to
PhoneNumbersAsyncClient.getPurchasedPhoneNumberWithResponse and PhoneNumbersClient.getPurchasedPhoneNumberWithResponse.
-Renamed PhoneNumbersAsyncClient.listPhoneNumbers and PhoneNumbersClient.listPhoneNumbers to PhoneNumbersAsyncClient.listPurchasedPhoneNumbers and PhoneNumbersClient.listPurchasedPhoneNumbers.

## 1.0.0-beta.6 (2021-03-09)
### Added
- Added PhoneNumbersClient and PhoneNumbersAsyncClient (originally was part of the azure.communication.administration package).
- Added support for Azure Active Directory Authentication.

### Breaking Changes
- PhoneNumberAsyncClient has been replaced with PhoneNumbersAsyncClient, which has the same functionality but different APIs. To learn more about how PhoneNumbersAsyncClient works, refer to the [README.md][https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/communication/azure-communication-phonenumbers/README.md].
- PhoneNumberClient has been replaced with PhoneNumbersClient, which has the same functionality but different APIs. To learn more about how PhoneNumbersClient works, refer to the [README.md][https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/communication/azure-communication-phonenumbers/README.md].


