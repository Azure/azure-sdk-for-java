# Release History

## 1.2.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.1.5 (2022-07-11)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.29.1` to version `1.30.0`.
- Upgraded `azure-core-http-netty` from `1.12.2` to version `1.12.3`.

## 1.1.4 (2022-06-07)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.28.0` to version `1.29.1`.
- Upgraded `azure-core-http-netty` from `1.12.0` to version `1.12.2`.

## 1.1.3 (2022-05-09)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.27.0` to version `1.28.0`.
- Upgraded `azure-core-http-netty` from `1.11.9` to version `1.12.0`.

## 1.1.2 (2022-04-07)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.26.0` to version `1.27.0`.
- Upgraded `azure-core-http-netty` from `1.11.8` to version `1.11.9`.

## 1.1.1 (2022-03-09)

### Bug Fixes

- Added validation for `tokenRefresher` in `CommunicationTokenRefreshOptions` constructors

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.25.0` to version `1.26.0`.
- Upgraded `azure-core-http-netty` from `1.11.7` to version `1.11.8`.

## 1.1.0 (2022-02-23)

### Features Added

- Added new constructor with required param `tokenRefresher` for `CommunicationTokenRefreshOptions`
- Deprecated old constructor overloads in `CommunicationTokenRefreshOptions` and replaced by fluent setters
- Added fluent setters for optional properties:
    - Added `setRefreshProactively(boolean refreshProactively)` setter that allows setting whether the token should be proactively renewed prior to its expiry or on demand.
    - Added `setInitialToken(String initialToken)` setter that allows setting the optional serialized JWT token
- Added a synchronous token refresher getter `getTokenRefresherSync` for `CommunicationTokenRefreshOptions`
- Optimization added: When the proactive refreshing is enabled and the token refresher fails to provide a token that's not about to expire soon, the subsequent refresh attempts will be scheduled for when the token reaches half of its remaining lifetime until a token with long enough validity (>10 minutes) is obtained.

## 1.0.8 (2022-02-09)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.24.1` to version `1.25.0`.
- Upgraded `azure-core-http-netty` from `1.11.6` to version `1.11.7`.

## 1.0.7 (2022-01-14)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.22.0` to version `1.24.1`.
- Upgraded `azure-core-http-netty` from `1.11.2` to version `1.11.6`.

## 1.0.6 (2021-11-10)

### Other Changes

#### Dependency Updates

- Upgraded azure-core to 1.22.0.

## 1.0.5 (2021-10-07)

### Other Changes

#### Dependency Updates

- Upgraded azure-core to 1.21.0.

## 1.0.4 (2021-09-09)

### Dependency updates

- Added `azure-communication-networktraversal` package

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
- `CommunicationCloudEnvironment`, `CommunicationTokenRefreshOptions`, `CommunicationUserIdentifier`, `MicrosoftTeamsUserIdentifier`,
`PhoneNumberIdentifier`, `UnknownIdentifier`, are all final classes now.

## 1.0.0-beta.5 (2021-03-02)

- Updated `azure-communication-common` version

## 1.0.0-beta.4 (2021-02-09)

### Breaking Changes

- Renamed `CommunicationUserCredential` to `CommunicationTokenCredential`
- Replaced constructor `CommunicationTokenCredential(TokenRefresher tokenRefresher, String initialToken, boolean refreshProactively)` and `CommunicationTokenCredential(TokenRefresher tokenRefresher)` with `CommunicationTokenCredential(CommunicationTokenRefreshOptions tokenRefreshOptions)`
- Renamed `PhoneNumber` to `PhoneNumberIdentifier`
- Renamed `CommunicationUser` to `CommunicationUserIdentifier`
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
