# Release History

## 5.0.0-beta.1 (Unreleased)

- First release of the new Azure Key Vault Administration client library for Java.

### Features Added

### Breaking Changes

### Bugs Fixed

- Fixed a challenge resource verification error message that referenced `SecretClientBuilder`, which is not part of this package. The message now points to the client builder's `disableChallengeResourceVerification()` method.

### Other Changes
