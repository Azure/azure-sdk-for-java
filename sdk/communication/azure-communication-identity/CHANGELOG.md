# Release History

## 1.0.0-beta.4 (Unreleased)
### Added
- Added CommunicationIdentityClient and CommunicationIdentityClientAsync (originally was part of the azure-communication-aministration package).
- Added support for Azure Active Directory Authentication.
- Added ability to create a user and issue token for it at the same time.

### Breaking
- `pstn` token scope is removed.
- `revokeTokens` now revoke all the currently issued tokens instead of revoking tokens issued prior to a given time.
- `issueToken` returns an instance of `core.credential.AccessToken` instead of `CommunicationUserToken`.
