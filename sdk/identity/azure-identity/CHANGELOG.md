# Release History

## 1.18.0-beta.1 (Unreleased)

- Added claims challenge support to `AzureDeveloperCliCredential`. Claims provided in `TokenRequestContext` are now passed to Azure Developer CLI via the `--claims` parameter, requiring azd CLI 1.18.1 or higher. Also enhanced error handling to extract user-friendly messages from JSON output and provide clear version compatibility warnings when the `--claims` flag is unsupported.
- Added claims challenge handling support to `AzureCliCredential`. When a token request includes claims, the credential will now throw a `CredentialUnavailableException` with instructions to use Azure PowerShell directly with the appropriate `-ClaimsChallenge` parameter.
- Added claims challenge handling support to `AzurePowerShellCredential`. When a token request includes claims, the credential will now throw a `CredentialUnavailableException` with instructions to use Azure PowerShell directly with the appropriate `-ClaimsChallenge` parameter.
- Added `AzureIdentityEnvVars` expandable string enum for type-safe environment variable names used in Azure Identity credentials.
- Added `requireEnvVars(AzureIdentityEnvVars... envVars)` method to `DefaultAzureCredentialBuilder` to enforce the presence of specific environment variables at build time. When configured, the credential will throw an `IllegalStateException` during `build()` if any of the specified environment variables are missing or empty.

### Features Added

### Breaking Changes

### Bugs Fixed
- Fixed `AzurePowerShellCredential` handling of XML header responses and `/Date(epochTime)/` time format parsing that previously caused `JsonParsingException`. [#46572](https://github.com/Azure/azure-sdk-for-java/pull/46572)
- Fixed `AzureDeveloperCliCredential` hanging when `AZD_DEBUG` environment variable is set by adding `--no-prompt` flag to the `azd auth token` command.

### Other Changes

## 1.17.0 (2025-08-08)

### Features Added
- GA release of beta features

### Bugs Fixed

- Handles the scenario to gracefully handle unavailability of Key Ring on Linux platforms. [#46333](https://github.com/Azure/azure-sdk-for-java/pull/46333)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.55.5` to version `1.56.0`.
- Upgraded `azure-core-http-netty` from `1.15.13` to version `1.16.0`.

## 1.17.0-beta.1 (2025-07-18)

### Features Added

- `VisualStudioCodeCredential` has been restored and now supports **broker authentication** using the Azure account signed in via Visual Studio Code. [#45715](https://github.com/Azure/azure-sdk-for-java/pull/45715)
- `DefaultAzureCredential` can be configured to use a specific credential type by setting the `AZURE_TOKEN_CREDENTIALS` environment variable. When set, it will only attempt authentication using the specified credential type. For example, setting `AZURE_TOKEN_CREDENTIALS=WorkloadIdentityCredential` will restrict authentication to workload identity only.
- Enhanced `AzurePowerShellCredential` token retrieval with tenantId support, cross-version SecureString handling, and improved compatibility and robustness. [#45851](https://github.com/Azure/azure-sdk-for-java/pull/45851)
- `DefaultAzureCredential` now supports authentication with the currently signed-in Windows account, provided the azure-identity-broker package is installed. This auth mechanism is added at the end of the DefaultAzureCredential credential chain. [#45891](https://github.com/Azure/azure-sdk-for-java/pull/45891)

### Breaking Changes

#### Behavioral Breaking Changes

- Removed `SharedTokenCacheCredential` from the `DefaultAzureCredential` authentication chain. [#45795](https://github.com/Azure/azure-sdk-for-java/pull/45795)

### Other Changes

- Deprecated `SharedTokenCacheCredential` and `SharedTokenCacheCredentialBuilder`. [#45795](https://github.com/Azure/azure-sdk-for-java/pull/45795)

#### Dependency Updates

- Upgraded `com.microsoft.azure:msal4j` from version `1.21.0` to version `1.22.0`. 

## 1.16.3 (2025-07-18)

### Features Added

- Enhanced `AzurePowerShellCredential` token retrieval with tenantId support, cross-version SecureString handling, and improved compatibility and robustness. [#45851](https://github.com/Azure/azure-sdk-for-java/pull/45851)

### Other Changes

#### Dependency Updates

- Upgraded `com.microsoft.azure:msal4j` from version `1.21.0` to version `1.22.0`. 

## 1.16.2 (2025-06-09)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.55.3` to version `1.55.4`.
- Upgraded `azure-core-http-netty` from `1.15.11` to version `1.15.12`.
- Updated `msal4j` from `1.20.1` to version `1.21.0`.


## 1.16.1 (2025-05-14)

### Features Added
- Added `AZURE_TOKEN_CREDENTIALS` environment variable to `DefaultAzureCredential` to allow for choosing groups of credentials.
  - `prod` for `EnvironmentCredential`, `WorkloadIdentityCredential`, and `ManagedIdentityCredential`.
  - `dev` for `SharedTokenCredential`, `IntelliJCredential`, `AzureCliCredential`, `AzurePowershellCredential`, and `AzureDeveloperCliCredential`.

## 1.16.0 (2025-05-06)

### Other Changes
- Marked `VisualStudioCodeCredential` and `VisualStudioCodeCredentialBuilder` as deprecated.[#44527](https://github.com/Azure/azure-sdk-for-java/issues/44527)
- Added deprecation message to `EnvironmentCredential` when a username/password is used. [#45185](https://github.com/Azure/azure-sdk-for-java/pull/45185) 

#### Dependency Updates

- Updated `msal4j` from `1.20.0` to version `1.20.1`.

## 1.16.0-beta.1 (2025-03-13)

### Features Added
- Added support to specify `subscription` ID or name on `AzureCliCredentialBuilder`. [#44123](https://github.com/Azure/azure-sdk-for-java/pull/44123)
- Log the client, object, or resource ID of the user-assigned managed identity. [#44305](https://github.com/Azure/azure-sdk-for-java/pull/44305)

### Other Changes

- Marked `UsernamePasswordCredential` and `UsernamePasswordCredentialBuilder` as deprecated. See https://aka.ms/azsdk/identity/mfa for details about MFA enforcement and migration guidance. [#44381](https://github.com/Azure/azure-sdk-for-java/pull/44381)  

## 1.15.4 (2025-03-11)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.55.2` to version `1.55.3`.
- Upgraded `azure-core-http-netty` from `1.15.10` to version `1.15.11`.
- Upgraded `azure-json` from `1.4.0` to version `1.5.0`.
- Upgraded `msal4j` from `1.19.0` to version `1.19.1`.

## 1.15.3 (2025-02-20)

### Other Changes

## Dependency Updates

- Upgraded `azure-core` from `1.55.1` to version `1.55.2`.
- Upgraded `azure-core-http-netty` from `1.15.9` to version `1.15.10`.

## 1.15.2 (2025-02-13)

### Other Changes

- Upgraded `azure-core` from `1.55.0` to version `1.55.1`.
- Upgraded `azure-core-http-netty` from `1.15.8` to version `1.15.9`.

## 1.15.1 (2025-02-07)

### Bugs Fixed

- Fixed an issue preventing scopes with underscores from working properly. [#44040](https://github.com/Azure/azure-sdk-for-java/pull/44040)

### Other Changes

- Upgraded `azure-core` from `1.54.1` to version `1.55.0`.
- Upgraded `azure-core-http-netty` from `1.15.7` to version `1.15.8`.
- Upgraded `msal4j` from `1.17.1` to version `1.19.0`.

## 1.15.0 (2025-01-10)

### Features Added
- Added missing `executorService` API to `ManagedIdentityCredentialBuilder`, cleaned up comments in other types for this method.

### Other Changes
- `@Deprecated` methods `DefaultAzureCredential.setIntelliJKeePassDatabasePath` and `IntelliJCredentialBuilder.keePassDatabasePath`. [#42437](https://github.com/Azure/azure-sdk-for-java/pull/42437)
- Changed Identity credentials to use `SharedExecutorService` threadpool instead of `ForkJoin`'s common pool by default. [#42468](https://github.com/Azure/azure-sdk-for-java/pull/42468)

## 1.14.2 (2024-11-15)

### Bugs Fixed
- Fixed issue in Managed Identity scopes [#42934](https://github.com/Azure/azure-sdk-for-java/pull/42934)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.54.0` to version `1.54.1`.
- Upgraded `azure-core-http-netty` from `1.15.6` to version `1.15.7`.
- Upgraded `jna-platform` from `5.6.0` to version `5.13.0`.

## 1.14.1 (2024-11-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.53.0` to version `1.54.0`.
- Upgraded `azure-core-http-netty` from `1.15.5` to version `1.15.6`.

## 1.14.0 (2024-10-03)

### Features Added
- Features now generally available from previous betas:
  - Added object id support in `ManagedIdentityCredential`. It can be configured via `ManagedIdentityCredentialBuilder`.
  - Added support for a client assertion in `OnBehalfOfCredential` [#40552](https://github.com/Azure/azure-sdk-for-java/pull/40552/files)

### Breaking Changes
- Breaking changes generally available from previous betas:
  - Removed support in `IntelliJCredential` for legacy Azure Toolkit for IntelliJ versions. Please upgrade to latest if you are using 3.52 or below.

### Bugs Fixed
- Fixed the request sent in `AzurePipelinesCredential` so it doesn't result in a redirect response when an invalid system access token is provided. 

### Other Changes
- Allow certain response headers to be logged in `AzurePipelinesCredential` for diagnostics and include them in the exception message.
- Mark `AzureAuthorityHosts.AZURE_GERMANY` deprecated as the Germany cloud closed in 2021. [#42148](https://github.com/Azure/azure-sdk-for-java/issues/42148)
- Using msal4j's managed identity implementation is now on by default. (Added in 1.13.0-beta.1) 

#### Dependency Updates

- Upgraded `azure-core` from `1.52.0` to version `1.53.0`.
- Upgraded `azure-core-http-netty` from `1.15.4` to version `1.15.5`.
- Upgraded `msal4j` from `1.17.1` to version `1.17.2`.

## 1.14.0-beta.2 (2024-09-20)

### Features Added
- Added object id support in `ManagedIdentityCredential`. It can be configured via `ManagedIdentityCredentialBuilder`.

### Breaking Changes
- Removed support in `IntelliJCredential` for legacy Azure Toolkit for IntelliJ versions. Please upgrade to latest if you are using 3.52 or below.

### Bugs Fixed
Fixed issue in `IntelliJCredential` blocking sign in [#39799](https://github.com/Azure/azure-sdk-for-java/issues/39799)

## 1.13.3 (2024-09-13)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.51.0` to version `1.52.0`.
- Upgraded `azure-core-http-netty` from `1.15.3` to version `1.15.4`.
- Upgraded `azure-json` from `1.2.0` to version `1.3.0`.
- Upgraded `msal4j` from `1.16.2` to version `1.17.1`.

## 1.13.2 (2024-08-02)

### Bugs Fixed
- Fixed bugs in `AzurePowerShellCredential` - Fixed break on Windows related to ordering of parameters, and fixed [#41234](https://github.com/Azure/azure-sdk-for-java/issues/41234) (previously shipped in beta)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.50.0` to version `1.51.0`.
- Upgraded `azure-core-http-netty` from `1.15.2` to version `1.15.3`.
- Upgraded `azure-json` from `1.1.0` to version `1.2.0`.
- Upgraded `msal4j` from `1.16.1` to version `1.16.2`.

## 1.14.0-beta.1 (2024-07-24)

### Bugs Fixed
- Fixed bugs in `AzurePowerShellCredential` - Fixed break on Windows related to ordering of parameters, and fixed [#41234](https://github.com/Azure/azure-sdk-for-java/issues/41234)

## 1.13.1 (2024-07-16)

### Features Added
- Added support in `EnvironmentCredential` (and thus `DefaultAzureCredential` when it chooses `EnvironmentCredential`) for using subject name / issuer authentication with client certificates by setting `AZURE_CLIENT_SEND_CERTIFICATE_CHAIN` to `1` or `true`. [#40013](https://github.com/Azure/azure-sdk-for-java/issues/40013)
### Bugs Fixed
- Fixed certificate type detection, which fixes using a PFX certificate without a password. [#37210](https://github.com/Azure/azure-sdk-for-java/issues/37210)
- Fix `PowershellCredential` issue when user had a profile [#41030](https://github.com/Azure/azure-sdk-for-java/pull/41030)
#### Dependency Updates
- Upgraded `azure-core` from `1.49.1` to `1.50.0`
- Upgraded `azure-core-http-netty` from `1.15.1` to `1.15.2`
- Upgraded `msal4j` from `1.16.0` to `1.16.1`

## 1.13.0 (2024-06-20)

### Features Added
- GA for `AzurePipelinesCredential`

### Bugs Fixed
- Fixed an issue which may block `AzurePowershellCredential` from functioning correctly. [#40552](https://github.com/Azure/azure-sdk-for-java/pull/40552/files)

## 1.13.0-beta.2 (2024-06-10)

### Features Added
- Added support for a client assertion in `OnBehalfOfCredential` [#40552](https://github.com/Azure/azure-sdk-for-java/pull/40552/files)

### Bugs Fixed
- Fixed an issue which may block `AzurePowershellCredential` from functioning correctly. [#40552](https://github.com/Azure/azure-sdk-for-java/pull/40552/files) 

## 1.12.2 (2024-06-10)

### Bugs Fixed
- Managed identity bug fixes

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.49.0` to version `1.49.1`.
- Upgraded `azure-core-http-netty` from `1.15.0` to version `1.15.1`.
- Upgraded `msal4j` from `1.15.0` to version `1.15.1`.

## 1.13.0-beta.1 (2024-05-23)

### Features Added
- Added `AzurePipelinesCredential` to support [Microsoft Entra Workload ID](https://learn.microsoft.com/azure/devops/pipelines/library/service-endpoints?view=azure-devops&tabs=yaml) in Azure Pipelines service connections.

### Other Changes
- Migrated Managed Identity authentication flow to utilize Msal4j MI implementation. 

## 1.12.1 (2024-05-02)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` from `1.48.0` to version `1.49.0`.
- Upgraded `azure-core-http-netty` from `1.14.2` to version `1.15.0`.

## 1.12.0 (2024-04-08)

### Features Added
- Added default sign-in experience for brokered authentication. See the documentation in the `azure-identity-broker` package for more information. [#39284](https://github.com/Azure/azure-sdk-for-java/pull/39284) 

### Breaking Changes
#### Behavioral Breaking Changes
- The timeout interval for IMDS probing has been adjusted from 0.5 seconds to 1 second. This update is designed to enhance the system's reliability without affecting user applications. [#38345](https://github.com/Azure/azure-sdk-for-java/issues/38345)
- The default retry logic of Managed Identity has been updated to retry 5 times, the retry delay increases exponentially, starting at 800 milliseconds and doubling with each subsequent retry, up to 5 retries. This change is designed to optimize the retry mechanism, reducing the likelihood of congestion and improving the overall stability of service connections under varying network conditions. [#38345](https://github.com/Azure/azure-sdk-for-java/issues/38345)
- The `DefaultAzureCredential` caches the last working credential by default and tries it directly on subsequent attempts. This change is designed to improve the performance of the `DefaultAzureCredential` by reducing the number of attempts required to acquire a token. [#36867](https://github.com/Azure/azure-sdk-for-java/issues/36867)

### Bugs Fixed
- Changed log level of an informational message in token acquisition. [#39063](https://github.com/Azure/azure-sdk-for-java/issues/39063)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` from `1.47.0` to version `1.48.0`.
- Upgraded `azure-core-http-netty` from `1.14.1` to version `1.14.2`.
- Upgraded `msal4j` from `1.14.3` to version `1.15.0`.
- Upgraded `msal4j-persistence-extension` from `1.2.0` to version `1.3.0`.

## 1.11.4 (2024-03-14)

### Other Changes

#### Dependency Updates
- Upgraded `msal4j` from `1.14.0` to version `1.14.3`.

## 1.11.3 (2024-03-01)

### Bugs fixed
- Fixed an issue where the broker dependency library was being probed for when it shouldn't be, resulting in an erronous error message. [#39002](https://github.com/Azure/azure-sdk-for-java/pull/39002)

#### Dependency Updates

- Upgraded `azure-core` from `1.46.0` to version `1.47.0`.
- Upgraded `azure-core-http-netty` from `1.14.0` to version `1.14.1`.


## 1.12.0-beta.1 (2024-02-12)

### Features Added
- Added expires_on parsing support to `AzureCliCredential`.([#38406](https://github.com/Azure/azure-sdk-for-java/pull/38406))
- Added caching support for working credential in `DefaultAzureCredential`. ([#38404](https://github.com/Azure/azure-sdk-for-java/pull/38404))

## 1.11.2 (2024-02-05)

### Bugs Fixed

- Lowered logging level of token cache misses from `ERROR` to `DEBUG`. ([#38502](https://github.com/Azure/azure-sdk-for-java/pull/38502))

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.45.1` to version `1.46.0`
- Upgraded `azure-core-http-netty` from `1.13.11` to version `1.14.0`

## 1.11.1 (2023-12-01)

### Bugs Fixed
- Fixed Azure Arc Managed Identity token retrieval issue.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.45.0` to version `1.45.1`
- Upgraded `azure-core-http-netty` from `1.13.10` to version `1.13.11`

## 1.11.0 (2023-11-07)

### Features added
- GA release of Web Account Manager (WAM) support for Azure Identity.

### Bugs fixed
- Cache streams used for client certificates [#37502](https://github.com/Azure/azure-sdk-for-java/pull/37502)
- Fix incorrect use of organizations tenant for sync calls on `AzureCliCredential` and `AzureDeveloperCliCredential` [#37457](https://github.com/Azure/azure-sdk-for-java/pull/37457)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.44.1` to version `1.45.0`.
- Upgraded `azure-core-http-netty` from `1.13.9` to version `1.13.10`

## 1.11.0-beta.2 (2023-10-27)

### Features Added
- Initial release of Web Account Manager (WAM) support for Azure Identity. This is a Windows-only auithentication broker.

### Other Changes
- Upgraded 'msal4j' from '1.13.9' to version '1.14.0'.

## 1.10.4 (2023-10-18)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.44.0` to version `1.44.1`.
- Upgraded `azure-core-http-netty` from `1.13.8` to version `1.13.9`.

## 1.10.3 (2023-10-10)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.43.0` to version `1.44.0`.
- Upgraded `azure-core-http-netty` from `1.13.7` to version `1.13.8`.

## 1.10.2 (2023-10-10)

### Bugs Fixed
- Bug fixes for developer credentials

## 1.11.0-beta.1 (2023-09-20)

### Features Added
- Added support for passing an InputStream containing a client cerfificate [#36747](https://github.com/Azure/azure-sdk-for-java/pull/36747)

### Bugs fixed
- Fixed flowing `HttpClientOptions` through credentials [#36382](https://github.com/Azure/azure-sdk-for-java/pull/36382)
- Fixed edge case in Docker where 403s erronously caused CredentialUnavailableExceptions [#36747](https://github.com/Azure/azure-sdk-for-java/pull/36747)

## 1.10.1 (2023-09-10)

### Other Changes
#### Dependency Updates
- Upgraded `azure-core` from `1.42.0` to version `1.43.0`.
- Upgraded `azure-core-http-netty` from `1.13.6` to version `1.13.7`.

## 1.10.0 (2023-08-09)

### Features Added
- Added `BrowserCustomizationOptions` to `InteractiveBrowserCredentialBuilder` to allow for customization of the browser window.

### Other Changes
- Renamed `enableSupportLogging` to `enableUnsafeSupportLogging`. This is a breaking change from 1.10.0-beta.1.
- `DefaultAzureCredential` will try all developer credentials. Previously if a developer credential attempted to acquire a token and failed, it would stop the chain. Deployed credentials are unaffected.

#### Dependency Updates
- Upgraded `azure-core` from `1.41.0` to version `1.42.0`.
- Upgraded `azure-core-http-netty` from `1.13.5` to version `1.13.6`.


## 1.10.0-beta.1 (2023-07-24)

### Features Added
- Added CAE Authentication support for Service principal authentication.
- Added the ability to log PII from MSAL using new `enableSupportLogging` API.

### Other Changes

#### Behavioral Breaking Change
- CAE Authentication is disabled by default. It needs to be enabled by invoking `setEnableCae` on `TokenRequestContext` class.

## 1.9.2 (2023-07-10)

### Bugs Fixed
- Azure CLI and Azure Developer CLI no longer pass `organizations` as a tenant value when retrieving a token. [#34387](https://github.com/Azure/azure-sdk-for-java/issues/34387)
- `WorkloadIdentityCredential` now uses the tenant value specified in `DefaultAzureCredential` when authenticating with Azure Identity. [#35619](https://github.com/Azure/azure-sdk-for-java/pull/35619)

### Other Changes
#### Dependency Updates
- Upgraded `azure-core` from `1.40.0` to version `1.41.0`.
- Upgraded `azure-core-http-netty` from `1.13.4` to version `1.13.5`.

## 1.9.1 (2023-06-06)

### Other Changes
#### Behavioral breaking change 
- Moved `AzureDeveloperCliCredential` to the end of the `DefaultAzureCredential` chain.

#### Dependency Updates
- Upgraded `azure-core` from `1.38.0` to version `1.39.0`.
- Upgraded `azure-core-http-netty` from `1.13.3` to version `1.13.4`.

## 1.9.0 (2023-05-08)

### Features Added

#### Features Generally Available from v1.9.0-beta3
- Added configurable timeout for developer credentials (Azure CLI, Azure Developer CLI)
- Added `WorkloadIdentityCredential` to authenticate using workload identity in Azure Kubernetes.

### Other Changes
- renamed `DefaultAzureCredential.processTimeout` to `credentialProcessTimeout`.

#### Dependency Updates
- Upgraded `azure-core` from `1.38.0` to version `1.39.0`.

## 1.8.3 (2023-05-01)

### Other Changes

#### Dependency Updates
- Upgraded `msal4j` from `1.13.7` to version `1.13.8`.
- Upgraded `msal4j-persistence-extension` from `1.1.0` to version `1.2.0`.

## 1.9.0-beta.3 (2023-04-13)

### Bugs Fixed
- Add `disableInstanceDiscovery` to `DefaultAzureCredentialBuilder`

### Other Changes
- Removed feature from previous betas to compute refresh values for managed identity tokens.

## 1.8.2 (2023-04-10)

### Bugs Fixed

- Fixed a bug in managed identity not properly URLEncoding a value. [#34375](https://github.com/Azure/azure-sdk-for-java/pull/34375)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` from `1.37.0` to version `1.38.0`.
- Upgraded `azure-core-http-netty` from `1.13.1` to version `1.13.2`.
- Upgraded `msal4j` from `1.13.5` to version `1.13.7`.

## 1.9.0-beta.2 (2023-03-16)

### Features Added
- Added CAE support to service principal authentication.
- Pass more detailed refresh policy for managed identity tokens to MSAL.
- Add configurable timeout for developer credentials (Azure CLI, Azure Developer CLI)

### Bugs Fixed
- Fixed detection logic for az/azd.

## 1.8.1 (2023-03-06)

#### Dependency Updates

- Upgraded `azure-core` from `1.36.0` to version `1.37.0`.
- Upgraded `azure-core-http-netty` from `1.13.0` to version `1.13.1`.
- Upgraded `msal4j` from `1.13.4` to version `1.13.5`.

## 1.9.0-beta.1 (2023-02-08)

### Features Added
- [[#32527]](https://github.com/Azure/azure-sdk-for-java/pull/32527) Added Azure Developer CLI Credential.
- Added support to disable instance discovery on Microsoft Entra ID credentials.
- `WorkloadIdentityCredential` and `DefaultAzureCredential` support Workload Identity Federation on Kubernetes. `DefaultAzureCredential` support requires environment variable configuration as set by the Workload Identity webhook.

## 1.8.0 (2023-02-03)

### Features Added

#### Features Generally Available from v1.8.0-beta1
- Added support to configure `clientOptions`, `httpLogOptions`, `retryPolicy`, `retryOptions` and `addPolicy` on Identity credentials.

#### Dependency Updates

- Upgraded `azure-core` from `1.35.0` to version `1.36.0`.
- Upgraded `azure-core-http-netty` from `1.12.8` to version `1.13.0`.
- Upgraded `msal4j` from `1.13.3` to version `1.13.4`.


## 1.8.0-beta.1 (2023-01-20)

### Features Added
- Added support to configure `clientOptions`, `httpLogOptions`, `retryPolicy`, `retryOptions` and `addPolicy` on Identity credentials.
- Added support to disable instance discovery on Microsoft Entra ID credentials.

## 1.7.3 (2023-01-06)

### Bugs Fixed
- No longer statically accessing environment variables. [#32781](https://github.com/Azure/azure-sdk-for-java/issues/32781)
- Use `ThreadLocalRandom` instead of `Random` to better enable static compilation. [#32744](https://github.com/Azure/azure-sdk-for-java/issues/32744)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` from `1.34.0` to version `1.35.0`.

## 1.7.2 (2022-12-09)

### Bugs Fixed
- Fixed MSI token `expires_in` parsing issue.

## 1.7.1 (2022-11-17)

### Features Added

- Added user-agent header to Identity requests

## 1.7.0 (2022-11-04)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` from `1.33.0` to version `1.34.0`.
- Upgraded `msal4j` from `1.13.2` to `1.13.3`

## 1.7.0-beta.2 (2022-10-13)

### Features Added
- `GetTokenSync` method implementation/support in Token Credentials.
- Read `AZURE_REGIONAL_AUTHORITY_NAME` from the environment to specify region for client credential types.

### Other Changes
#### Dependency Updates
- Upgraded `msal4j` from `1.13.1` to `1.13.2`

## 1.6.1 (2022-10-11)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` from `1.32.0` to version `1.33.0`.
- Upgraded `azure-core-http-netty` from `1.12.5` to version `1.12.6`.

## 1.7.0-beta.1 (2022-09-20)

### Features Added

- `EnvironmentCredential` will read the environment variable `AZURE_CLIENT_CERTIFICATE_PASSWORD` for a `pem`/`pfx` certificate specified by `AZURE_CLIENT_CERTIFICATE_PATH`.
-  Added support for in-memory token caching in `ManagedIdentityCredential`.

### Breaking Changes
- Removed `VisualStudioCodeCredential` from `DefaultAzureCredential` token chain. [Issue 27364](https://github.com/Azure/azure-sdk-for-java/issues/27364) tracks this.

## 1.6.0 (2022-09-19)

### Features Added
- Added `additionallyAllowedTenants` to the following credential builders to force explicit opt-in behavior for multi-tenant authentication:
    - `AuthorizationCodeCredentialBuilder`
    - `AzureCliCredentialBuilder`
    - `AzurePowerShellCredentialBuilder`
    - `ClientAssertionCredentialBuilder`
    - `ClientCertificateCredentialBuilder`
    - `ClientSecretCredentialBuilder`
    - `DefaultAzureCredentialBuilder`
    - `OnBehalfOfCredentialBuilder`
    - `UsernamePasswordCredentialBuilder`
    - `VisualStudioCodeCredentialBuilder`
    - `VisualStudioCredentialBuilder`

### Breaking Changes
- Credential types supporting multi-tenant authentication will now throw `ClientAuthenticationException` if the requested tenant ID doesn't match the credential's tenant ID, and is not included in the `additionallyAllowedTenants` option. Applications must now explicitly add additional tenants to the `additionallyAllowedTenants` list, or add '*' to list, to enable acquiring tokens from tenants other than the originally specified tenant ID. See [BREAKING_CHANGES.md](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/BREAKING_CHANGES.md#160).

- These beta features in version `1.6.0-beta.1` have been removed from this release and will be added back in version `1.7.0-beta.1`:
    - removed `VisualStudioCodeCredential` from `DefaultAzureCredential` token chain
    - `AZURE_CLIENT_CERTIFICATE_PASSWORD` support for `EnvironmentCredential`
    - in-memory token caching support for `ManagedIdentityCredential`.

### Other Changes

#### Dependency Updates

- Upgraded `msal4j` from `1.13.0` to `1.13.1`.

## 1.5.5 (2022-09-02)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.31.0` to version `1.32.0`.
- Upgraded `azure-core-http-netty` from `1.12.4` to version `1.12.5`.
- Upgraded `msal4j` from `1.12.0` to `1.13.0`.

## 1.6.0-beta.1 (2022-08-12)

### Features Added

- `EnvironmentCredential` will read the environment variable `AZURE_CLIENT_CERTIFICATE_PASSWORD` for a `pem`/`pfx` certificate specified by `AZURE_CLIENT_CERTIFICATE_PATH`. 
-  Added support for in-memory token caching in `ManagedIdentityCredential`. 

### Breaking Changes
- Removed `VisualStudioCodeCredential` from `DefaultAzureCredential` token chain. [Issue 27364](https://github.com/Azure/azure-sdk-for-java/issues/27364) tracks this.

### Other Changes
#### Dependency Updates
- Upgraded `msal4j` from `1.12.0` to version `1.13.0`.

## 1.5.4 (2022-08-08)

### Bugs Fixed

- Fixes IntelliJCredential [21150](https://github.com/Azure/azure-sdk-for-java/issues/21150)
- Fixes AzureCliCredential to properly respect tenant IDs.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.30.0` to version `1.31.0`.
- Upgraded `azure-core-http-netty` from `1.12.3` to version `1.12.4`.

## 1.5.3 (2022-06-30)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` dependency to 1.30.0

## 1.5.2 (2022-06-07)
### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to 1.29.1

## 1.5.1 (2022-05-06)

### Other Changes
#### Dependency Updates
- Upgraded `msal4j` dependency to 1.12.0
- Upgraded `azure-core` dependency to 1.28.0

## 1.5.0 (2022-04-05)

### Breaking Changes
- Removed `disableAuthorityValidationSafetyCheck` for GA, will reintroduce in next beta. This is not a breaking change from last GA.
- Replaced `identityLogOptions` setter with the `enableAccountIdentifierLogging` setter on the credential builders. This is not a breaking change from last GA.

### Other Changes
#### Dependency Updates
- Upgraded `azure-core` dependency to 1.27.0

### Bugs Fixed
Correctly use an `AppServiceMsiCredential` in the case both `IDENTITY_ENDPOINT` and `IDENTITY_HEADER` are set.

## 1.5.0-beta.2 (2022-03-21)

### Features Added
- Added ability to configure `IdentityLogOptions` on Credential Builders to make account Identifier logging configurable.
- Added the option `disableAuthoriyValidaionSafetyCheck` on Credential Builders.

### Other Changes
#### Dependency Updates
- Upgraded `azure-core` dependency to 1.26.0

## 1.4.6 (2022-03-08)

### Other Changes
#### Dependency Updates
- Upgraded `azure-core` dependency to 1.26.0


## 1.4.5 (2022-03-03)

### Other Changes
#### Behavioural Changes
- Logging level of false positive `ERROR` logs is changed to `VERBOSE`/`DEBUG` under `DefaultAzureCredential`


## 1.5.0-beta.1 (2022-02-17)

### Features Added
- Added `resourceId` to Managed Identity for Virtual Machines, App Service, and Service Bus.
- Added `ClientAssertionCredential` for client assertion based authentication flows.

### Other Changes
- Upgraded App Service Managed Identity endpoint to `2019-08-01`.

## 1.4.4 (2022-02-07)

### Other Changes
#### Dependency Updates
- Upgraded `azure-core` dependency to 1.25.0

## 1.4.3 (2022-01-11)

### Other Changes
#### Dependency Updates
- Upgraded `azure-core` dependency to 1.24.1


## 1.4.2 (2021-11-24)

### Bugs Fixed
- Fixes the edge case scenario when MSI Tokens return both `expires_on` and `expires_in` fields populated for `ManagedIdentityCredential`.

## 1.4.1 (2021-11-09)

### Other Changes
#### Dependency Updates
- Upgraded `azure-core` dependency to 1.22.0

#### Behavioral Changes
- The `ManagedIdentityCredential` reads value of AZURE_POD_IDENTITY_TOKEN_URL environment variable from AZURE_POD_IDENTITY_AUTHORITY_HOST now.

## 1.4.0 (2021-10-14)

### Features Added
- Added `tenantId` setter on `AzurePowerShellCredential` and `AzureCliCredential`

### Breaking Changes from 1.4.0-beta.1
Note the breaking changes below don't apply if you're upgrading from a previous released stable version.

- Removed 'AzureApplicationCredential' and 'AzureApplicationCredentialBuilder'
- Removed 'regionalAuthority' setter on `ClientSecretCredentialBuilder` and `ClientCertificateCredentialBuilder`
- Removed `RegionalAuthority` enum class.
- Removed `allowMultiTenantAuthentication` method from Credential Builders. The Multi Tenant Authentication is enabled by default now.

## 1.3.7 (2021-10-04)

### Other Changes
#### Dependency Updates
- Upgraded `azure-core` dependency to 1.21.0


## 1.4.0-beta.1 (2021-09-13)
### Features Added

- Added support to `ManagedIdentityCredential` for Bridge to Kubernetes local development authentication.
- Added regional STS support to client credential types.
    - Added the `RegionalAuthority` type, that allows specifying Azure regions.
    - Added `regionalAuthority()` setter to `ClientSecretCredentialBuilder` and `ClientCertificateCredentialBuilder`.
    - If instead of a region, `RegionalAuthority.AutoDiscoverRegion` is specified as the value for `regionalAuthority`, MSAL will be used to attempt to discover the region.
    - A region can also be specified through the `AZURE_REGIONAL_AUTHORITY_NAME` environment variable.
- Added `loginHint()` setter to `InteractiveBrowserCredentialBuilder` which allows a username to be pre-selected for interactive logins.
- Added support to consume `TenantId` challenges from `TokenRequestContext`.
- Added support for AKS Token Exchange support in `ManagedIdentityCredential`


## 1.3.6 (2021-09-08)

### Dependency Updates
- Upgraded `azure-core` dependency to 1.20.0

## 1.3.5 (2021-08-10)

### Dependency Updates
- Upgraded `azure-core` dependency to 1.19.0

## 1.3.4 (2021-07-28)

### Dependency Updates
- Dropped `KeePassJava2` dependency

## 1.3.3 (2021-07-07)

### Dependency Updates
- Pinned `json-smart` dependency to 2.4.7

## 1.3.2 (2021-07-07)

### Dependency Updates
- Upgraded `azure-core` dependency to 1.18.0


## 1.3.1 (2021-06-08)

### Dependency Updates
- Upgraded `azure-core` dependency to 1.17.0


## 1.3.0 (2021-05-11)

### Features Added
- Added `AzurePowerShellCredential` to support authentication using Powershell on development platforms.
- Added support to disable CP1 capability in `TokenCredentials` via configuration of environment variable `AZURE_IDENTITY_DISABLE_CP1`

### Dependency Updates
- Upgraded `azure-core` dependency to 1.16.0
- Upgraded `msal4j` dependency to 1.1.0


## 1.3.0-beta.2 (2021-03-10)
### New Features
- Added the support to enable and configure Persistent Token Cache via `TokenCachePersistenceOptions` API on `InteractiveBrowserCredentialBuilder`, `AuthorizationCodeCredentialBuilder`, `UsernamePasswordCredentialBuilder`, `DeviceCodeCredentialBuilderBuilder` `ClientSecretCredentialBuilder`, `ClientCertificateCredentialBuilder` and `SharedTokenCacheCredentialBuilder`.
- Added new APIs for authenticating users with `DeviceCodeCredential`,  `InteractiveBrowserCredential` and `UsernamePasswordCredential`.
    - Added method `authenticate` which pro-actively interacts with the user to authenticate if necessary and returns a serializable `AuthenticationRecord`
- Added following configurable options in classes `DeviceCodeCredentialBuilder` and `InteractiveBrowserCredentialBuilder`
    - `authenticationRecord` enables initializing a credential with an `AuthenticationRecord` returned from a prior call to `Authenticate`
    - `disableAutomaticAuthentication` disables automatic user interaction causing the credential to throw an `AuthenticationRequiredException` when interactive authentication is necessary.

### Dependency Updates
- Upgraded `azure-core` dependency to 1.14.0
- Upgraded `msal4j` dependency to 1.9.1
- Upgraded `msal4j-persistence-extension` to 1.1.0

## 1.3.0-beta.1 (2021-02-10)

### New Features
- Added the support to consume claims from `TokenRequestContext` send it as part of authentication request.

### Dependency Updates
- Upgraded `azure-core` dependency to 1.13.0
- Upgraded `msal4j` dependency to 1.8.1

## 1.2.3 (2021-02-09)

### Dependency Updates
- Upgraded `azure-core` dependency to 1.13.0
- Upgraded `msal4j` dependency to 1.8.1


## 1.2.2 (2021-01-12)

### Dependency Updates
- Upgraded `azure-core` dependency to 1.12.0


## 1.2.1 (2020-12-08)
### Dependency Updates
- Upgraded `azure-core` dependency to 1.11.0

## 1.2.0 (2020-11-09)

### New Features
- Added Azure Service Fabric Managed Identity support to `ManagedIdentityCredential`
- Added Azure Arc Managed Identity support to `ManagedIdentityCredential`
- Added support for Docker Containers in `DefaultAzureCredential`

### Fixes and improvements
- Prevent `VisualStudioCodeCredential` using invalid authentication data when no user is signed in to Visual Studio Code

### Dependency Updates
- Upgraded `azure-core` dependency to 1.10.0
- Upgraded `msal4j` dependency to 1.8.0


## 1.2.0-beta.2 (2020-10-06)

### New Features
- Added the methods `pfxCertificate(InputStream certificate, String clientCertificatePassword)` and `pemCertificate(InputStream certificate)` in `ClientCertificateCredentialBuilder`.
- Added `includeX5c(boolean)` method in `ClientCertificateCredentialBuilder` to enable subject name / issuer based authentication.
- Added a default `challengeConsumer` in `DeviceCodeCredentialBuilder` which prints the device code information to console. The `challengeConsumer` configuration is no longer required in `DeviceCodeCredentialBuilder`.

### Dependency Updates
- Upgraded `azure-core` dependency to 1.9.0
- Upgraded `jna-platform` dependency to 5.6.0
- Upgraded `msal4j` dependency to 1.7.1




## 1.2.0-beta.1 (2020-09-11)
- Added `InteractiveBrowserCredentialBuilder.redirectUrl(String)` to configure the redirect URL
- Deprecated `InteractiveBrowserCredentialBuilder.port(int)`
- Added support for App Service 2019 MSI Endpoint in `ManagedIdentityCredential`
- Added Shared Token cache support for MacOS Keychain, Gnome Keyring, and plain text for other Linux environments
- Added option to write to shared token cache from `InteractiveBrowserCredential`, `AuthorizationCodeCredential`, `UsernamePasswordCredential`, `DeviceCodeCredential` `ClientSecretCredential` and `ClientCertificateCredential`
- Added new APIs for authenticating users with `DeviceCodeCredential`,  `InteractiveBrowserCredential` and `UsernamePasswordCredential`.
    - Added method `authenticate` which pro-actively interacts with the user to authenticate if necessary and returns a serializable `AuthenticationRecord`
- Added following configurable options in classes `DeviceCodeCredentialBuilder` and `InteractiveBrowserCredentialBuilder`
    - `authenticationRecord` enables initializing a credential with an `AuthenticationRecord` returned from a prior call to `Authenticate`
    - `disableAutomaticAuthentication` disables automatic user interaction causing the credential to throw an `AuthenticationRequiredException` when interactive authentication is necessary.



## 1.1.0 (2020-08-10)
- Upgraded core dependency to 1.7.0
- Removed the default value of 0 for port in `InteractiveBrowserCredential`.

### Breaking Changes
- Removing Application Authentication APIs for GA release. These will be reintroduced in 1.2.0-beta.1.
  - Removed class `AuthenticationRecord`
  - Removed class `AuthenticationRequiredException`
  - Removed methods `allowUnencryptedCache()` and `enablePersistentCache()` from `ClientCertificateCredentialBuilder`, 
   `ClientSecretCredentialBuilder`, `InteractiveBrowserCredentialBuilder`, `DeviceCodeCredentialBuilder`,
    `UsernamePasswordCredentialBuilder` and `ClientCertificateCredentialBuilder`.
  - Removed methods `allowUnencryptedCache()` and `authenticationRecord(AuthenticationRecord)` from `SharedTokenCacheCredentialBuilder`.
  - Removed methods `authenticationRecord(AuthenticationRecord)` and `disableAutomaticAuthentication()` from `DeviceCodeCredentialBuilder` and `InteractiveBrowserCredentialBuilder`.
  - Removed methods `authenticate(TokenRequestContext)` and `authenticate()` from `DeviceCodeCredential`, `InteractiveBrowserCredential`
    and `UsernamePasswordCredential`.
    

## 1.1.0-beta.7 (2020-07-23)

### Features
- Added support for web apps (confidential apps) for `AuthorizationCodeCredential`. A client secret is required on the builder for web apps.
- Added support for user assigned managed identities for `DefaultAzureCredential` with `.managedIdentityClientId()`.
- Added`AzureAuthorityHosts` to access well knwon authority hosts.
- Added `getClientId()` method in `AuthenticationRecord`

### Breaking Changes
- Removed persistent caching support from `AuthorizationCodeCredential`.
- Removed `KnownAuthorityHosts`
- Removed `getCredentials()` method in `ChainedTokenCredential` & `DefaultAzureCredential`
- Changed return type of `serialize` method in `AuthenticationRecord` to `Mono<OutputStream>`.
- Changed method signatures`enablePersistentCache(boolean)` and `allowUnencryptedCache(boolean)` on credential builders to `enablePersistentCache()` and `allowUnencryptedCache()`


## 1.1.0-beta.6 (2020-07-10)
- Added `.getCredentials()` method to `DefaultAzureCredential` and `ChainedTokenCredential` and added option `.addAll(Collection<? extends TokenCredential>)` on `ChainedtokenCredentialBuilder`.
- Added logging information in credentials and improved error messages in `DefaultAzureCredential`.

## 1.1.0-beta.5 (2020-06-09)

### New Features
- Added option to write to shared token cache from `ClientSecretCredential`, `ClientCertificateCredential`.
- Added new developer credentials `IntelliJCredential`, `VsCodeCredential` and `AzureCliCredential`.
- New APIs for authenticating users with `DeviceCodeCredential`,  `InteractiveBrowserCredential` and `UsernamePasswordCredential`.
    - Added method `authenticate` which pro-actively interacts with the user to authenticate if necessary and returns a serializable `AuthenticationRecord`
- Added following configurable options in classes `DeviceCodeCredentialBuilder` and `InteractiveBrowserCredentialBuilder`
    - `authenticationRecord` enables initializing a credential with an `AuthenticationRecord` returned from a prior call to `Authenticate`
    - `disableAutomaticAuthentication` disables automatic user interaction causing the credential to throw an `AuthenticationRequiredException` when interactive authentication is necessary.

### Breaking Changes
- Removed support to exclude specific credentials in `DefaultAzureCredential` authentication flow.


## 1.1.0-beta.4 (2020-05-06)
- Added `IntelliJCredential` support in `DefaultAzureCredential`.
- Added `VsCodeCredential` support in `DefaultAzureCredential`.
- Added support to disable specific credentials in `DefaultAzureCredential` authentication flow.
- Added Shared Token cache support for MacOS Keychain, Gnome Keyring, and plain text for other Linux environments
- Added option to write to shared token cache from `InteractiveBrowserCredential`, `AuthorizationCodeCredential`, `UsernamePasswordCredential`, and `DeviceCodeCredential`

## 1.0.6 (2020-05-05)
- Upgraded `azure-core` dependency to 1.5.0
- Fix `MSIToken` expiry time parsing for Azure App Service platforms.

## 1.1.0-beta.3 (2020-04-07)
- Added `KnownAuthorityHosts` to enable quick references to public azure authority hosts.
- Added methods to allow credential configuration in `DefaultAzureCredentialBuilder`
- Added support for authority host to be read from `AZURE_AUTHORITY_HOST` environment variable.
- Added support for `ClientCertificateCredential` and `UserNamePasswordCredential` in EnvironmentCredential.

## 1.0.5 (2020-04-07)
- Upgraded `azure-core` dependency to 1.4.0

## 1.1.0-beta.2 (2020-03-11)

### Added
- Added 'authorityHost' set method in `DefaultAzureCredentialBuilder`
- Added `executorService` set method in all the credential builders except `ManagedIdentityCredentialBuilder`
- Added `authorityHost` set method to `DefaultAzureCredentialBuilder`
- Added `tokenRefreshOffset` set method in all the credential builders.
- Added `httpClient` set method in all the credential builders.
- Updated `DefaultAzureCredential` to enable authenticating through the Azure CLI

## 1.0.4 (2020-03-10)
- Upgraded `azure-core` dependency to 1.0.4

## 1.1.0-beta.1 (2020-02-12)
- All credential builders support setting a pipeline via `httpPipeline` method.
- SharedTokenCacheCredentialBuilder supports setting the tenant id via `tenantId` method.

## 1.0.3 (2020-01-13)
- Support datetime format `M/d/yyyy K:mm:ss a XXX` for token `expires_on` property on Windows App Services.

## 1.0.2 (2020-01-07)
- Fix MSI_ENDPOINT and MSI_SECRET environment variable lookup issue in `ManagedIdentityCredential` when running on App Service

## 1.0.0 (2019-10-25)
**Breaking changes**

- The `getToken(TokenRequest tokenRequest)` methods on all the credentials are changed to `getToken(TokenRequestContext tokenRequestContext)`. 
- All credentials are moved from `com.azure.identity.credential` package to `com.azure.identity` package
- `DeviceCodeChallenge` is renamed to r`DeviceCodeInfo`, with `int expiresIn()` replaced with `OffsetDateTime expiresOn()` returning the time of the device code expiration
- All methods containing `uri` is renamed to contain `url` for consistency

**Known issues**
- Support connecting to different clouds with `AZURE_CLOUD` environment variable ([#5741](https://github.com/Azure/azure-sdk-for-java/issues/5741))

## 1.0.0-preview.4 (2019-10-07)
**New features**

- A new credential `AuthorizationCodeCredential` is added.
- `DeviceCodeCredentialBuilder`, `InteractiveBrowserCredentialBuilder`, and `UsernamePasswordCredentialBuilder` now
supports single tenant apps with `.tenantId(String)` method.

**Breaking changes**

The `getToken(String... scopes)` methods on all the credentials are changed to `getToken(TokenRequest tokenRequest)`. 

## 1.0.0-preview.3 (2019-09-09)
**New features**

A new credential `SharedTokenCacheCredential` is added. It's currently only supported on Windows. This credential is capable of authenticating to Microsoft Entra ID if you are logged in in Visual Studio 2019.

## 1.0.0-preview.2 (2019-08-05)
**Breaking changes**

Credentials are now created through builders instead of setters. For example, in preview 1, a `ClientSecretCredential` can be created by
```java
ClientSecretCredential cred = new ClientSecretCredential()
        .tenantId(tenant)
        .clientId(clientId)
        .clientSecret(secret);
```

In preview 2, it needs to be created through its builder:
```java
ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
        .tenantId(tenant)
        .clientId(clientId)
        .clientSecret(secret);
        .build();
```

**New features**

3 new credentials are added in preview 2, including `DeviceCodeCredential`, `InteractiveBrowserCredential` and `UsernamePasswordCredential`.

`DeviceCodeCredential` is useful for IoT devices. `InteractiveBrowserCredential` and `UsernamePasswordCredential` are mainly used in developer scenarios, to login on a developer's computer.

**Deprecated or removed features**

No feature was deprecated or removed.

## 1.0.0-preview.1 (2019-06-28)
Version 1.0.0-preview.1 is a preview of our efforts in creating an authentication API for Azure SDK client libraries that is developer-friendly, idiomatic
to the Java ecosystem, and as consistent across different languages and platforms as possible. The principles that guide
our efforts can be found in the [Azure SDK Design Guidelines for Java](https://aka.ms/azsdk/guide/java).

For details on the Azure SDK for Java (July 2019 Preview) release, you can refer to the [release announcement](https://aka.ms/azure-sdk-preview1-java).

This release supports service principal and managed identity authentication.
See the [documentation](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md)
for more details. User authentication will be added in an upcoming preview
release.

This release supports only global Microsoft Entra tenants, i.e. those
using the https://login.microsoftonline.com authentication endpoint.