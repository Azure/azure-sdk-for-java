# Release History

## 2.0.0-beta.3 (2025-10-29)

### Other Changes

- Switched `azureCliSignedInUser()`'s implementation from using `az ad signed-in-user show` to decoding user information from the ARM access token obtained via `az account get-access-token`. This change was made because some tenants have conditional access policies (AADSTS530084) that block access to the Microsoft Graph API, which `az ad signed-in-user show` relies on.

## 2.0.0-beta.2 (2025-08-20)

### Features Added

- Added `getTokenCredentialForTest` to `TestUtilities`.

## 2.0.0-beta.1 (2025-08-12)

### Features Added

- Initial release of the `azure-resourcemanager-test` library.
