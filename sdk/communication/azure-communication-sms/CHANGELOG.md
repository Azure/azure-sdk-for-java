# Release History

## 1.1.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

#### Dependency updates
- Upgraded `azure-communication-common` to 1.0.4

## 1.0.3 (2021-06-09)
Updated `azure-communication-sms` version

## 1.0.2 (2021-05-27)
- Dependency versions updated.

## 1.0.1 (2021-05-11)
### Bug Fixes
- Fixing bugs to support idempotency.

## 1.0.0 (2021-03-29)
Updated `azure-communication-sms` version

## 1.0.0-beta.4 (2021-03-09)
### Added
- Added Azure Active Directory authentication support
- Support for creating SmsClient with TokenCredential.
- Added support for 1:N SMS messaging.
- Added support for tagging SMS messages.
- Send method series in SmsClient are idempotent under retry policy.
- Added `SmsOptions`

### Breaking Change
- Updated `public Mono<SendSmsResponse> sendMessage(PhoneNumberIdentifier from, PhoneNumberIdentifier to, String message)` to `public Mono<SendSmsResponse> send(String from, String to, String message)`.
- Updated `public Mono<Response<SendSmsResponse>> sendMessageWithResponse(PhoneNumberIdentifier from,List<PhoneNumberIdentifier> to, String message, SendSmsOptions smsOptions, Context context)` to `Mono<Response<SmsSendResult>> sendWithResponse(String from, String to, String message, SmsSendOptions options, Context context)`.
- Replaced `SendSmsResponse` with `SmsSendResult`.

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
[read_me]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/communication/azure-communication-sms/README.md
[documentation]: https://docs.microsoft.com/azure/communication-services/quickstarts/telephony-sms/send?pivots=programming-language-java
