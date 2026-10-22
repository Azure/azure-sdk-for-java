# Release History

## 2.0.0-beta.1 (Unreleased)

### Features Added

- Added public `AccessTokenCache` in `com.azure.v2.core.credentials` for caching and proactively refreshing access tokens.

### Breaking Changes

### Bugs Fixed

- Aligned access token caching with `azure-core`, including tenant-aware context changes and refresh error handling that
  only falls back to a valid token for the cached context.

### Other Changes
