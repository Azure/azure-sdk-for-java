# Release History

## 1.0.0-beta.5 (Unreleased)

## 1.0.0-beta.4 (Skipped)
### Added
- Added Azure Active Directory authentication support

## 1.0.0-beta.3 (2020-11-16)
### Added
- Support directly passing connection string to the SmsClientBuilder using connectionString().

### Breaking Change
- Removed credential(CommunicationClientCredential credential) and replaced with 
accessKey(String accessKey) within CommunicationIdentityClientBuilder.

## 1.0.0-beta.2 (2020-10-06)
Updated `azure-communication-sms` version

## 1.0.0-beta.1 (2020-09-22)
This is the initial release of Azure Communication Services for Telephony and SMS. For more information, please see the [README][read_me] and [documentation][documentation].

This is a Public Preview version, so breaking changes are possible in subsequent releases as we improve the product. To provide feedback, please submit an issue in our [Azure SDK for Java GitHub repo](https://github.com/Azure/azure-sdk-for-java/issues).

<!-- LINKS -->
[read_me]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/communication/azure-communication-sms/README.md
[documentation]: https://docs.microsoft.com/azure/communication-services/quickstarts/telephony-sms/send?pivots=programming-language-java
