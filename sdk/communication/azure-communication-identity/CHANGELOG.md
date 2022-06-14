# Release History

## 1.2.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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

- Upgraded `azure-communication-common` to 1.0.6
- Upgraded `azure-core` to 1.22.0

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


