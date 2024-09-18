# Release History

## 1.3.7 (2024-09-18)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.15.3` to version `1.15.4`.
- Upgraded `azure-core` from `1.51.0` to version `1.52.0`.


## 1.3.6 (2024-08-24)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.15.2` to version `1.15.3`.
- Upgraded `azure-core` from `1.50.0` to version `1.51.0`.


## 1.3.5 (2024-07-26)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.15.1` to version `1.15.2`.
- Upgraded `azure-core` from `1.49.1` to version `1.50.0`.


## 1.3.4 (2024-06-27)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.15.0` to version `1.15.1`.
- Upgraded `azure-core` from `1.49.0` to version `1.49.1`.


## 1.3.3 (2024-05-28)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.48.0` to version `1.49.0`.
- Upgraded `azure-core-http-netty` from `1.14.2` to version `1.15.0`.


## 1.3.2 (2024-04-23)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.47.0` to version `1.48.0`.
- Upgraded `azure-core-http-netty` from `1.14.1` to version `1.14.2`.


## 1.3.1 (2024-03-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.46.0` to version `1.47.0`.
- Upgraded `azure-core-http-netty` from `1.14.0` to version `1.14.1`.


## 1.3.0 (2024-02-12)

### Features Added
- Added support for a new communication identifier `MicrosoftTeamsAppIdentifier`.

### Breaking Changes
- Introduction of `MicrosoftTeamsAppIdentifier` is a breaking change. It will impact any code that previously depended on the use of UnknownIdentifier with rawIDs starting with `28:orgid:`, `28:dod:`, or `28:gcch:`.

## 1.2.14 (2023-11-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.44.1` to version `1.45.0`.
- Upgraded `azure-core-http-netty` from `1.13.9` to version `1.13.10`.

## 1.2.13 (2023-10-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.43.0` to version `1.44.1`.
- Upgraded `azure-core-http-netty` from `1.13.7` to version `1.13.9`.

## 1.2.12 (2023-09-22)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` from `1.42.0` to version `1.43.0`.
- Upgraded `azure-core-http-netty` from `1.13.6` to version `1.13.7`.

## 1.2.11 (2023-08-18)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.41.0` to version `1.42.0`.
- Upgraded `azure-core-http-netty` from `1.13.5` to version `1.13.6`.

## 1.2.10 (2023-07-13)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.40.0` to version `1.41.0`.
- Upgraded `azure-core-http-netty` from `1.13.4` to version `1.13.5`.

## 1.2.9 (2023-06-13)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.39.0` to version `1.40.0`.
- Upgraded `azure-core-http-netty` from `1.13.3` to version `1.13.4`.

## 1.2.8 (2023-05-12)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.38.0` to version `1.39.0`.
- Upgraded `azure-core-http-netty` from `1.13.2` to version `1.13.3`.

## 1.2.7 (2023-04-21)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.1` to version `1.13.2`.
- Upgraded `azure-core` from `1.37.0` to version `1.38.0`.

## 2.0.0-beta.1 (2023-03-24)

### Features Added
- Added support for a new communication identifier `MicrosoftBotIdentifier`.

### Breaking Changes
- Introduction of `MicrosoftBotIdentifier` is a breaking change. It will affect code that relied on using `UnknownIdentifier` with a rawID starting with `28:`

## 1.2.6 (2023-03-07)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.36.0` to version `1.37.0`.
- Upgraded `azure-core-http-netty` from `1.13.0` to version `1.13.1`.

## 1.2.5 (2023-02-07)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.35.0` to version `1.36.0`.
- Upgraded `azure-core-http-netty` from `1.12.8` to version `1.13.0`.

## 1.2.4 (2023-01-10)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.34.0` to version `1.35.0`.
- Upgraded `azure-core-http-netty` from `1.12.7` to version `1.12.8`.

## 1.2.3 (2022-11-08)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.33.0` to version `1.34.0`.
- Upgraded `azure-core-http-netty` from `1.12.6` to version `1.12.7`.

## 1.2.2 (2022-10-11)

### Bug Fixes
- Fixed the logic of `PhoneNumberIdentifier` to always maintain the original phone number string whether it included the leading `+` sign or not.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.32.0` to version `1.33.0`.
- Upgraded `azure-core-http-netty` from `1.12.5` to version `1.12.6`.

## 1.2.1 (2022-09-07)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.31.0` to version `1.32.0`.
- Upgraded `azure-core-http-netty` from `1.12.4` to version `1.12.5`.

## 1.3.0-beta.1 (2022-08-12)

### Other Changes
- Opened the package to the new `azure-communication-rooms` library by modifying the `module-info.java` file.

## 1.2.0 (2022-08-09)

### Features Added
- Added `String getRawId()` and `static CommunicationIdentifier fromRawId(String rawId)` to `CommunicationIdentifier` to translate between a `CommunicationIdentifier` and its underlying canonical rawId representation. Developers can now use the rawId as an encoded format for identifiers to store in their databases or as stable keys in general.

#### Dependency Updates

- Upgraded `azure-core` from `1.30.0` to version `1.31.0`.
- Upgraded `azure-core-http-netty` from `1.12.3` to version `1.12.4`.

## 1.1.5 (2022-07-11)

### Features Added

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.29.1` to version `1.30.0`.
- Upgraded `azure-core-http-netty` from `1.12.2` to version `1.12.3`.

## 1.1.4 (2022-06-07)

### Features Added

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
