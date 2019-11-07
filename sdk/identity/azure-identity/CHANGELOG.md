# Release History

## 1.0.0 (2019-10-25) - November 2019 SDK Release
**Breaking changes**

- The `getToken(TokenRequest tokenRequest)` methods on all the credentials are changed to `getToken(TokenRequestContext tokenRequestContext)`. 
- All credentials are moved from `com.azure.identity.credential` package to `com.azure.identity` package
- `DeviceCodeChallenge` is renamed to `DeviceCodeInfo`, with `int expiresIn()` replaced with `OffsetDateTime expiresOn()` returning the time of the device code expiration
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
