# Release History

## 2.0.0-beta.1 (Unreleased)

### Features Added

- Added public `AccessTokenCache` in `com.azure.v2.core.credentials` for caching and proactively refreshing access tokens.

### Breaking Changes

### Bugs Fixed

- Aligned access token caching with `azure-core`, including tenant-aware context changes, request-context snapshots,
  and invalidation of unusable tokens after refresh failures.
- Fixed token expiry checks when credential-provided refresh times are later than token expiration.

### Other Changes
