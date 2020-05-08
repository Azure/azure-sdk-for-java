# Release History

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

## 1.0.0 (2019-10-25) - November 2019 SDK Release
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
See the [documentation](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/identity/azure-identity/README.md)
for more details. User authentication will be added in an upcoming preview
release.

This release supports only global Azure Active Directory tenants, i.e. those
using the https://login.microsoftonline.com authentication endpoint.
