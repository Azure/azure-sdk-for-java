# Release History

## 1.5.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.4.8 (2023-07-14)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.40.0` to version `1.41.0`.
- Upgraded `azure-communication-common` from `1.2.9` to version `1.2.10`.

## 1.4.7 (2023-06-13)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.39.0` to version `1.40.0`.
- Upgraded `azure-communication-common` from `1.2.8` to version `1.2.9`.

## 1.4.6 (2023-05-16)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.38.0` to version `1.39.0`.
- Upgraded `azure-communication-common` from `1.2.6` to version `1.2.8`.

## 1.4.5 (2023-04-21)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.37.0` to version `1.38.0`.


## 1.4.4 (2023-03-08)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.36.0` to version `1.37.0`.
- Upgraded `azure-communication-common` from `1.2.5` to version `1.2.6`.

## 1.4.3 (2023-02-08)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.35.0` to version `1.36.0`.
- Upgraded `azure-communication-common` from `1.2.4` to version `1.2.5`.

## 1.4.2 (2023-01-11)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.34.0` to version `1.35.0`.
- Upgraded `azure-communication-common` from `1.2.3` to version `1.2.4`.

## 1.4.1 (2022-11-08)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.33.0` to version `1.34.0`.
- Upgraded `azure-communication-common` from `1.2.2` to version `1.2.3`.

## 1.4.0 (2022-10-12)

### Features Added
- Added support to customize the Communication Identity access token’s validity period:
  - Added methods that provide the ability to create a Communication Identity access token with custom expiration:
      - CommunicationIdentityClient:
        - `createUserAndToken(Iterable<CommunicationTokenScope> scopes, Duration tokenExpiresIn)`
        - `createUserAndTokenWithResponse(Iterable<CommunicationTokenScope> scopes, Duration tokenExpiresIn, Context context)`
        - `getToken(CommunicationUserIdentifier communicationUser, Iterable<CommunicationTokenScope> scopes, Duration tokenExpiresIn)`
        - `getTokenWithResponse(CommunicationUserIdentifier communicationUser, Iterable<CommunicationTokenScope> scopes, Duration tokenExpiresIn, Context context)`
      - CommunicationIdentityAsyncClient:
        - `createUserAndToken(Iterable<CommunicationTokenScope> scopes, Duration tokenExpiresIn)`
        - `createUserAndTokenWithResponse(Iterable<CommunicationTokenScope> scopes, Duration tokenExpiresIn)`
        - `getToken(CommunicationUserIdentifier communicationUser, Iterable<CommunicationTokenScope> scopes, Duration tokenExpiresIn)`
        - `getTokenWithResponse(CommunicationUserIdentifier communicationUser, Iterable<CommunicationTokenScope> scopes, Duration tokenExpiresIn)`
- Added a new API version `CommunicationIdentityServiceVersion.V2022_10_01` that is now the default API version.

#### Dependency Updates

- Upgraded `azure-core` from `1.32.0` to version `1.33.0`.
- Upgraded `azure-communication-common` from `1.2.1` to version `1.2.2`.

## 1.3.1 (2022-09-08)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.31.0` to version `1.32.0`.
- Upgraded `azure-communication-common` from `1.2.0` to version `1.2.1`.

## 1.3.0 (2022-08-10)

### Features Added
- Added `String getRawId()` and `static CommunicationIdentifier fromRawId(String rawId)` to `CommunicationIdentifier` to translate between a `CommunicationIdentifier` and its underlying canonical rawId representation. Developers can now use the rawId as an encoded format for identifiers to store in their databases or as stable keys in general.

#### Dependency Updates
- Upgraded `azure-core` from `1.30.0` to version `1.31.0`.
- Upgraded `azure-communication-common` from `1.1.5` to version `1.2.0`.

## 1.2.0 (2022-07-21)

### Features Added
- Added support to integrate communication as Teams user with Azure Communication Services:
    - Added `getTokenForTeamsUser(GetTokenForTeamsUserOptions options)` method that provides the ability to exchange an Azure AD access token of a Teams user for a Communication Identity access token to `CommunicationIdentityClient`.
- Removed `CommunicationIdentityServiceVersion.V2021_10_31_PREVIEW`.
- Added a new API version `CommunicationIdentityServiceVersion.V2022_06_01` that is now the default API version.
- Added interfaces from `com.azure.core.client.traits` to `CommunicationIdentityClientBuilder`.
- Added `retryOptions` to `CommunicationIdentityClientBuilder`.

## 1.1.11 (2022-07-11)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.29.1` to version `1.30.0`.
- Upgraded `azure-communication-common` from `1.1.4` to version `1.1.5`.

## 1.1.10 (2022-06-10)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` from `1.28.0` to version `1.29.1`.
- Upgraded `azure-communication-common` from `1.1.3` to version `1.1.4`.

## 1.1.9 (2022-05-10)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.27.0` to version `1.28.0`.

## 1.1.8 (2022-04-08)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.26.0` to version `1.27.0`.
- Upgraded `azure-communication-common` from `1.1.1` to version `1.1.2`.

## 1.1.7 (2022-03-09)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.25.0` to version `1.26.0`.
- Upgraded `azure-communication-common` from `1.0.8` to version `1.1.1`.

## 1.1.6 (2022-02-09)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.24.1` to version `1.25.0`.
- Upgraded `azure-communication-common` from `1.0.7` to version `1.0.8`.

## 1.1.5 (2022-01-17)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.22.0` to version `1.24.1`.
- Upgraded `azure-communication-common` from `1.0.6` to version `1.0.7`.

## 1.1.4 (2021-11-11)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` to `1.22.0`.
- Upgraded `azure-communication-common` to `1.0.6`.

## 1.2.0-beta.1 (2021-10-29)

### Features Added
- Added support to integrate communication as Teams user with Azure Communication Services.

## 1.1.3 (2021-10-08)

### Other Changes

#### Dependency Updates

- Upgraded `azure-communication-common` to 1.0.5
- Upgraded `azure-core` to 1.21.0

## 1.1.2 (2021-09-23)

### Other Changes

#### Dependency Updates

- Upgraded `azure-communication-common` to 1.0.4
- Upgraded `azure-core` to 1.20.0

## 1.1.1 (2021-06-09)
Updated `azure-communication-identity` version

## 1.1.0 (2021-05-27)
### Added
- New exception `IdentityErrorResponseException` was added for more descriptive server error messages.
- Dependency versions updated.

## 1.0.0 (2021-03-29)
Updated `azure-communication-identity` version

## 1.0.0-beta.6 (2021-03-09)
### Added
- Added a retryPolicy() chain method to the `CommunicationIdentityClientBuilder`.

### Breaking
- `CommunicationIdentityClient.createUserWithToken` and `CommunicationIdentityAsyncClient.createUserWithToken` have been renamed to
`CommunicationIdentityClient.createUserAndToken` and `CommunicationIdentityAsyncClient.createUserAndToken`.
- `CommunicationIdentityClient.createUserWithTokenWithResponse` and `CommunicationIdentityAsyncClient.createUserWithTokenWithResponse` have been renamed to
`CommunicationIdentityClient.createUserAndTokenWithResponse` and `CommunicationIdentityAsyncClient.createUserAndTokenWithResponse`.
- `CommunicationUserIdentifierWithTokenResult` class has been renamed to `CommunicationUserIdentifierAndToken`.

## 1.0.0-beta.5 (2021-03-02)
### Breaking
- `CommunicationIdentityAsyncClient.issueToken` and `CommunicationIdentityClient.issueToken` is renamed to `CommunicationIdentityAsyncClient.getToken` and `CommunicationIdentityClient.getToken`.
- `CommunicationIdentityAsyncClient.issueTokenWithResponse` and `CommunicationIdentityClient.issueTokenWithResponse` is renamed to `CommunicationIdentityAsyncClient.getTokenWithReponse` and `CommunicationIdentityClient.getTokenWithReponse`.

## 1.0.0-beta.4 (2021-02-09)
### Breaking
- `pstn` token scope is removed.
- `revokeTokens` now revoke all the currently issued tokens instead of revoking tokens issued prior to a given time.
- `issueToken` returns an instance of `core.credential.AccessToken` instead of `CommunicationUserToken`.

### Added
- Added CommunicationIdentityClient and CommunicationIdentityAsyncClient (originally was part of the azure-communication-aministration package).
- Added support for Azure Active Directory Authentication.
- Added ability to create a user and issue token for it at the same time.


