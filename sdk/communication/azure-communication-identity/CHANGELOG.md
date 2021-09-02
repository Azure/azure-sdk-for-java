# Release History

## 1.1.2 (2021-08-30)
### Dependency updates
- Upgraded `azure-communication-common` to 1.0.4

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


