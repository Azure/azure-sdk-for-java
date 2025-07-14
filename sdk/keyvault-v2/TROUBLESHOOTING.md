# Troubleshooting Azure Key Vault SDK Issues
The Azure Key Vault SDKs for Java use a common HTTP pipeline and authentication to create, update, and delete secrets, keys, and certificates in Key Vault and Managed HSM. This troubleshooting guide contains steps for diagnosing issues common to these SDKs.

For package-specific troubleshooting guides, see any of the following:

* [Troubleshooting guide for the Azure Key Vault Administration SDK][kv_admin_troubleshooting]
* [Troubleshooting guide for the Azure Key Vault Certificates SDK][kv_certs_troubleshooting]
* [Troubleshooting guide for the Azure Key Vault Keys SDK][kv_keys_troubleshooting]
* [Troubleshooting guide for the Azure Key Vault Secrets SDK][kv_secrets_troubleshooting]

## Table of Contents
* [Troubleshooting Authentication Issues](#troubleshooting-authentication-issues)
    * [HTTP 401 Errors](#http-401-errors)
        * [Frequent HTTP 401 Errors in Logs](#frequent-http-401-errors-in-logs)
        * [AKV10032: Invalid issuer](#akv10032--invalid-issuer)
    * [HTTP 403 Errors](#http-403-errors)
        * [Operation Not Permitted](#operation-not-permitted)
        * [Access Denied to First Party Service](#access-denied-to-first-party-service)
    * [Other authentication issues](#other-authentication-issues)
        * [Tenant authentication issues](#tenant-authentication-issues)
        * [Incorrect challenge resource](#incorrect-challenge-resource)
* [Other Service Errors](#other-service-errors)
    * [HTTP 429: Too Many Requests](#http-429--too-many-requests)
* [Support](#support)

## Troubleshooting Authentication Issues
### HTTP 401 Errors
HTTP 401 errors may indicate problems authenticating, but silent 401 errors are also an expected part of the Azure Key Vault authentication flow.

#### Frequent HTTP 401 Errors in Logs
Most often, this is expected. Azure Key Vault issues a challenge for initial requests that force authentication. You may see these errors most often during application startup, but may also see these periodically during the application's lifetime when authentication tokens are near expiration.

If you are not seeing subsequent exceptions from the Key Vault SDKs, authentication challenges are likely the cause. If you continuously see 401 errors without successful operations, there may be an issue with the authentication library that's being used. We recommend using the Azure SDK's [azure-identity] library for authentication.

#### AKV10032: Invalid issuer
You may see an error similar to:

```text
com.azure.v2.core.exception.HttpResponseException: Status code 401, "{"error":{"code":"Unauthorized","message":"AKV10032: Invalid issuer. Expected one of https://sts.windows.net/{tenant 1}/, found https://sts.windows.net/{tenant 2}/."}}"
```

This is most often caused by being logged into a different tenant than the Key Vault authenticates. See our [DefaultAzureCredential] documentation to see the order credentials are read. You may be logged into a different tenant for one credential that gets read before another credential. For example, you might be logged into Visual Studio under the wrong tenant even though you're logged into the Azure CLI under the correct tenant.

Automatic tenant discovery support has been added when referencing package `azure-identity` version 1.4.0 or newer, and any of the following Key Vault SDK package versions or newer:

| Package                                  | Minimum Version |
|------------------------------------------|-----------------|
| `azure-security-keyvault-administration` | 5.0.0-beta.1    |
| `azure-security-keyvault-certificates`   | 5.0.0-beta.1    |
| `azure-security-keyvault-keys`           | 5.0.0-beta.1    |
| `azure-security-keyvault-secrets`        | 5.0.0-beta.1    |

Upgrading to the package versions should resolve any "Invalid Issuer" errors as long as the application or user is a member of the resource's tenant.

### HTTP 403 Errors
HTTP 403 errors indicate the user is not authorized to perform a specific operation in Key Vault or Managed HSM.

#### Operation Not Permitted
You may see an error similar to:

```text
com.azure.v2.core.exception.HttpResponseException: Status code 403, {"error":{"code":"Forbidden","message":"Operation decrypt is not permitted on this key.","innererror":{"code":"KeyOperationForbidden"}}}
```

The operation and inner `code` may vary, but the rest of the text will indicate which operation is not permitted. This error indicates that the authenticated application or user does not have permissions to perform that operation, though the cause may vary.

1. Check that the application or user has the appropriate permissions:
    * [Access policies][access_policies] (Key Vault)
    * [Role-Based Access Control (RBAC)][rbac] (Key Vault and Managed HSM)
2. If the appropriate permissions are assigned to your application or user, make sure you are authenticating as that user.
   If using the [DefaultAzureCredential], a different credential might've been used than one you expected.
   [Enable logging][identity_logging] and you will see which credential the [DefaultAzureCredential] used as shown below, and why previously-attempted credentials were rejected.

   ```text
   [ERROR] c.azure.identity.EnvironmentCredential   : Azure Identity => ERROR in EnvironmentCredential: Missing required environment variable AZURE_CLIENT_ID
   [ERROR] c.azure.identity.EnvironmentCredential   : EnvironmentCredential authentication unavailable. Environment variables are not fully configured.
   [INFO] c.azure.identity.DefaultAzureCredential  : Azure Identity => Attempted credential EnvironmentCredential is unavailable.
   [ERROR] c.a.i.implementation.IdentityClient      : ManagedIdentityCredential authentication unavailable. Connection to IMDS endpoint cannot be established, connect timed out.
   [ERROR] c.a.identity.ManagedIdentityCredential   : Azure Identity => ERROR in getToken() call for scopes [https://management.core.windows.net//.default]: ManagedIdentityCredential authentication unavailable. Connection to IMDS endpoint cannot be established, connect timed out.
   [INFO] c.azure.identity.DefaultAzureCredential  : Azure Identity => Attempted credential ManagedIdentityCredential is unavailable.
   [ERROR] c.a.identity.SharedTokenCacheCredential  : Azure Identity => ERROR in getToken() call for scopes [https://management.core.windows.net//.default]: SharedTokenCacheCredential authentication unavailable. No accounts were found in the cache.
   [INFO] c.azure.identity.DefaultAzureCredential  : Azure Identity => Attempted credential SharedTokenCacheCredential is unavailable.
   [ERROR] com.azure.v2.identity.IntelliJCredential    : Azure Identity => ERROR in getToken() call for scopes [https://management.core.windows.net//.default]: Unrecognized field "tenantId" (class com.azure.v2.identity.implementation.IntelliJAuthMethodDetails), not marked as ignorable (4 known properties: "authMethod", "azureEnv", "accountEmail", "credFilePath"])
   ```

#### Access Denied to First Party Service
You may see an error similar to:

```text
com.azure.v2.core.exception.HttpResponseException: Status code 403, {"error":{"code":"Forbidden","message":"Access denied to first party service. ...","innererror":{"code":"AccessDenied"}}}
```

The error `message` may also contain the tenant ID (`tid`) and application ID (`appid`). This error may occur because:

1. You have the **Allow trust services** option enabled and are trying to access the Key Vault from a service not on
   [this list](https://docs.microsoft.com/azure/key-vault/general/overview-vnet-service-endpoints#trusted-services) of
   trusted services.
2. You are authenticated against a Microsoft Account (MSA) in Visual Studio or another credential provider. See
   [above](#operation-not-permitted) for troubleshooting steps.

### Other authentication issues

See our Azure Identity [troubleshooting guide][identity_troubleshooting] for general guidance on authentication errors.

#### Multi-tenant authentication issues

If a `ClientAuthenticationException` is thrown with a message similar to:

> The current credential is not configured to acquire tokens for tenant

See our [troubleshooting guide for multi-tenant authentication issues][identity_multitenant]. Read our [release notes](https://aka.ms/azsdk/blog/multi-tenant-guidance) for more information about this change.

#### Incorrect challenge resource

If an exception is thrown with a message similar to:

> The challenge resource '<my-key-vault>.vault.azure.net' does not match the requested domain. If you wish to disable this check for your client, pass 'true' to the SecretClientBuilder.disableChallengeResourceVerification() method when building it. See https://aka.ms/azsdk/blog/vault-uri for more information.

Check that the resource is expected - that you're not receiving an authentication challenge from an unknown host which may indicate an incorrect request URI. If the resource is correct but you're using a mock service or non-transparent proxy, use the `disableChallengeResourceVerification()` when creating your clients with a client builder, for example:

```java
import com.azure.v2.core.TokenCredential;
import com.azure.v2.identity.DefaultAzureCredentialBuilder;
import com.azure.v2.security.keyvault.secrets.SecretClient;
import com.azure.v2.security.keyvault.secrets.SecretClientBuilder;

public class MyClass {
    public static void main(String[] args) {
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        SecretClient client = new SecretClientBuilder()
            .vaultUrl("https://my-key-vault.vault.azure.net/")
            .credential(credential)
            .disableChallengeResourceVerification();
    }
}
```

Read our [release notes][release_notes_resource] for more information about this change.

## Other Service Errors
To troubleshoot additional HTTP service errors not described below, see [Azure Key Vault REST API Error Codes][kv_error_codes].

### HTTP 429: Too Many Requests
If you get an exception or see logs that describe HTTP 429, you may be making too many requests to Key Vault too quickly.

Possible solutions include:

1. Use a singleton for any `CertificateClient`, `KeyClient`, `CryptographyClient`, or `SecretClient` and their async counterparts in your application for a single Key Vault.
2. Use a single instance of [DefaultAzureCredential] or other credential you use to authenticate your clients for each Key Vault or Managed HSM endpoint you need to access.
3. You could cache a certificate, key, or secret in memory for a time to reduce calls to retrieve them.
4. Use [Azure App Configuration][azure_appconfiguration] for storing non-secrets and references to Key Vault secrets. Storing all app configuration in Key Vault will increase the likelihood of requests being throttled as more application instances are started.
5. If you are performing encryption or decryption operations, consider using wrap and unwrap operations for a symmetric key which this may also improve application throughput.

See our [Azure Key Vault throttling guide][throttling_guide] for more information.

## Support

For additional support, please search our [existing issues](https://github.com/Azure/azure-sdk-for-java/issues) or [open a new issue](https://github.com/Azure/azure-sdk-for-java/issues/new/choose). You may also find existing answers on community sites like [Stack Overflow](https://stackoverflow.com/questions/tagged/azure-keyvault+java).

[access_policies]: https://docs.microsoft.com/azure/key-vault/general/assign-access-policy
[azure_appconfiguration]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/appconfiguration/azure-data-appconfiguration/README.md
[azure-identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md
[DefaultAzureCredential]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md#defaultazurecredential
[kv_admin_troubleshooting]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/keyvault-v2/azure-security-keyvault-administration/TROUBLESHOOTING.md
[kv_certs_troubleshooting]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/keyvault-v2/azure-security-keyvault-certificates/TROUBLESHOOTING.md
[kv_error_codes]: https://docs.microsoft.com/azure/key-vault/general/rest-error-codes
[kv_keys_troubleshooting]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/keyvault-v2/azure-security-keyvault-keys/TROUBLESHOOTING.md
[kv_secrets_troubleshooting]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/keyvault-v2/azure-security-keyvault-secrets/TROUBLESHOOTING.md
[identity_logging]: https://docs.microsoft.com/azure/developer/java/sdk/logging-overview
[identity_troubleshooting]: https://github.com/Azure/azure-sdk-for-python/blob/main/sdk/identity/azure-identity/TROUBLESHOOTING.md
[identity_multitenant]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/TROUBLESHOOTING.md#troubleshoot-multi-tenant-authentication-issues
[rbac]: https://docs.microsoft.com/azure/key-vault/general/rbac-guide
[release_notes_resource]: https://aka.ms/azsdk/blog/vault-uri
[release_notes_tenant]: https://aka.ms/azsdk/blog/multi-tenant-guidance
[throttling_guide]: https://docs.microsoft.com/azure/key-vault/general/overview-throttling
