# Release History

## 1.0.4 (2021-08-27)
Added `azure-communication-networktraversal` package

## 1.0.3 (2021-06-28)
Updated `azure-communication-common` version

## 1.0.2 (2021-06-09)
Updated `azure-communication-common` version

## 1.0.1 (2021-05-27)
- Dependency versions updated.

### Bug Fixes
- Fixed bug with AzureKeyCredential authentication

## 1.0.0 (2021-03-29)
### Breaking Changes
- Updated `CommunicationCloudEnvironment(String environmentValue)` constructor to `CommunicationCloudEnvironment()`.
- Updated `public CommunicationCloudEnvironment fromString(String environmentValue)` to `public static CommunicationCloudEnvironment fromString(String environmentValue)`.
- Renamed `TokenRefresher.getTokenAsync()` to `TokenRefresher.getToken()`.

## 1.0.0-beta.6 (2021-03-09)
### Breaking Changes
- Renamed `CommunicationTokenRefreshOptions.getRefreshProactively()` to `CommunicationTokenRefreshOptions.isRefreshProactively()`
- Constructor for `CommunicationCloudEnvironment` has been removed and now to set an environment value, the `fromString()` method must be called
- `CommunicationCloudEnvironment`, `CommunicationTokenRefreshOptions `, `CommunicationUserIdentifier`, `MicrosoftTeamsUserIdentifier`,
`PhoneNumberIdentifier`, `UnknownIdentifier`, are all final classes now.

## 1.0.0-beta.5 (2021-03-02)
- Updated `azure-communication-common` version

## 1.0.0-beta.4 (2021-02-09)
### Breaking Changes
- Renamed `CommunicationUserCredential` to `CommunicationTokenCredential`
- Replaced constructor `CommunicationTokenCredential(TokenRefresher tokenRefresher, String initialToken, boolean refreshProactively)` and `CommunicationTokenCredential(TokenRefresher tokenRefresher)` with `CommunicationTokenCredential(CommunicationTokenRefreshOptions tokenRefreshOptions)`
- Renamed `PhoneNumber` to `PhoneNumberIdentifier`
- Renamed `CommunicationUser` to `CommunicationUserIdentifier `
- Renamed `CallingApplication` to `CallingApplicationIdentifier`

### Added
- Added `MicrosoftTeamsUserIdentifier`

## 1.0.0-beta.3 (2020-11-16)
Updated `azure-communication-common` version

## 1.0.0-beta.2 (2020-10-06)
Updated `azure-communication-common` version

## 1.0.0-beta.1 (2020-09-22)
This package contains common code for Azure Communication Service libraries. For more information, please see the [README][read_me].

This is a Public Preview version, so breaking changes are possible in subsequent releases as we improve the product. To provide feedback, please submit an issue in our [Azure SDK for Java GitHub repo](https://github.com/Azure/azure-sdk-for-java/issues).

<!-- LINKS -->
[read_me]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/communication/azure-communication-common/README.md
