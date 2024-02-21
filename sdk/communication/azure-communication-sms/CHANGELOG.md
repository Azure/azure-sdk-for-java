# Release History

## 1.1.20 (2023-12-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-communication-common` from `1.2.14` to version `1.2.15`.
- Upgraded `azure-core` from `1.45.0` to version `1.45.1`.


## 1.1.19 (2023-11-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.44.1` to version `1.45.0`.
- Upgraded `azure-communication-common` from `1.2.13` to version `1.2.14`.


## 1.1.18 (2023-10-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.43.0` to version `1.44.1`.
- Upgraded `azure-communication-common` from `1.2.12` to version `1.2.13`.


## 1.1.17 (2023-09-22)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.42.0` to version `1.43.0`.
- Upgraded `azure-communication-common` from `1.2.11` to version `1.2.12`.


## 1.1.16 (2023-08-18)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.41.0` to version `1.42.0`.
- Upgraded `azure-communication-common` from `1.2.10` to version `1.2.11`.


## 1.1.15 (2023-07-25)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.40.0` to version `1.41.0`.
- Upgraded `azure-communication-common` from `1.2.9` to version `1.2.10`.

## 1.1.14 (2023-06-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.39.0` to version `1.40.0`.
- Upgraded `azure-communication-common` from `1.2.8` to version `1.2.9`.


## 1.1.13 (2023-05-23)

### Other Changes

#### Dependency Updates

- Upgraded `azure-communication-common` from `1.2.6` to version `1.2.8`.
- Upgraded `azure-core` from `1.38.0` to version `1.39.0`.


## 1.1.12 (2023-04-21)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.37.0` to version `1.38.0`.


## 1.1.11 (2023-03-15)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.36.0` to version `1.37.0`.
- Upgraded `azure-communication-common` from `1.2.5` to version `1.2.6`.

## 1.1.10 (2023-02-14)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.35.0` to version `1.36.0`.
- Upgraded `azure-communication-common` from `1.2.4` to version `1.2.5`.

## 1.1.9 (2023-01-13)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.34.0` to version `1.35.0`.
- Upgraded `azure-communication-common` from `1.2.3` to version `1.2.4`.

## 1.1.8 (2022-11-14)

### Other Changes

#### Dependency updates
- Upgraded `azure-communication-common` to 1.2.3
- Upgraded `azure-core` to 1.34.0

## 1.1.7 (2022-10-18)

### Other Changes

#### Dependency updates
- Upgraded `azure-communication-common` to 1.2.2
- Upgraded `azure-core` to 1.33.0

## 1.1.6 (2022-09-13)

### Other Changes

#### Dependency updates
- Upgraded `azure-communication-common` to 1.2.1
- Upgraded `azure-core` to 1.32.0

## 1.1.5 (2022-08-11)

### Other Changes

#### Dependency updates
- Upgraded `azure-communication-common` to 1.2.0
- Upgraded `azure-core` to 1.31.0

## 1.1.4 (2022-07-18)

### Other Changes

#### Dependency updates
- Upgraded `azure-communication-common` to 1.1.5
- Upgraded `azure-core` to 1.30.0

## 1.1.3 (2022-06-13)

### Other Changes

#### Dependency updates
- Upgraded `azure-communication-common` to 1.1.4
- Upgraded `azure-core` to 1.29.1
- Upgraded `azure-identity` to 1.5.2

## 1.1.2 (2022-05-11)

### Other Changes

#### Dependency updates
- Upgraded `azure-communication-common` to 1.1.3
- Upgraded `azure-core` to 1.28.0
- Upgraded `azure-identity` to 1.5.1


## 1.1.1 (2022-04-13)

### Other Changes

#### Dependency updates
- Upgraded `azure-communication-common` to 1.1.2
- Upgraded `azure-core` to 1.27.0
- Upgraded `azure-identity` to 1.5.0

## 1.1.0 (2022-03-11)

### Features Added
- Added interfaces from `com.azure.core.client.traits` to `SmsClientBuilder`
- Added `retryOptions` to `SmsClientBuilder`
- Upgraded `azure-communication-common` to 1.1.1
- Upgraded `azure-core` to 1.26.0
- Upgraded `azure-identity` to 1.4.6

## 1.0.8 (2022-02-17)

### Other Changes

#### Dependency updates
- Upgraded `azure-communication-common` to 1.0.8
- Upgraded `azure-core` to 1.25.0
- Upgraded `azure-identity` to 1.4.4

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
