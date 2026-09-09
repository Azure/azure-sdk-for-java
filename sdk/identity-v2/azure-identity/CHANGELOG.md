# Release History

## 2.0.0-beta.1 (Unreleased)

### Features Added

- Added support for user-assigned managed identities on Azure Arc in `ManagedIdentityCredential` and `DefaultAzureCredential`.

### Breaking Changes

### Bugs Fixed

- Fixed `DefaultAzureCredential` failing after a credential successfully acquired a token.

### Other Changes

#### Dependency Updates

- Upgraded `msal4j` from `1.23.1` to `1.26.0`.
