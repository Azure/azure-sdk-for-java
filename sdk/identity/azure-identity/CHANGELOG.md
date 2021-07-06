# Release History

## 1.4.0-beta.1 (Unreleased)
### Features Added

- Added support to `ManagedIdentityCredential` for Bridge to Kubernetes local development authentication.
- Added regional STS support to client credential types.
    - Added the `RegionalAuthority` type, that allows specifying Azure regions.
    - Added `regionalAuthority()` setter to `ClientSecretCredentialBuilder` and `ClientCertificateCredentialBuilder`.
    - If instead of a region, `RegionalAuthority.AutoDiscoverRegion` is specified as the value for `regionalAuthority`, MSAL will be used to attempt to discover the region.
    - A region can also be specified through the `AZURE_REGIONAL_AUTHORITY_NAME` environment variable.
- Added `loginHint()` setter to `InteractiveBrowserCredentialBuilder` which allows a username to be pre-selected for interactive logins.

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

A new credential `SharedTokenCacheCredential` is added. It's currently only supported on Windows. This credential is capable of authenticating to Azure Active Directory if you are logged in in Visual Studio 2019.

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
our efforts can be found in the [Azure SDK Design Guidelines for Java](https://azuresdkspecs.z5.web.core.windows.net/JavaSpec.html).

For details on the Azure SDK for Java (July 2019 Preview) release, you can refer to the [release announcement](https://aka.ms/azure-sdk-preview1-java).

This release supports service principal and managed identity authentication.
See the [documentation](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md)
for more details. User authentication will be added in an upcoming preview
release.

This release supports only global Azure Active Directory tenants, i.e. those
using the https://login.microsoftonline.com authentication endpoint.
