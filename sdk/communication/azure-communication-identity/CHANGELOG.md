# Release History

## 1.0.0-beta.5 (Unreleased)
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


