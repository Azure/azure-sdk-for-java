## 1.12.0

### Behavioral Breaking Changes
- The timeout interval for IMDS probing has been adjusted from 0.5 seconds to 1 second. This update is designed to enhance the system's reliability without affecting user applications. [#38345](https://github.com/Azure/azure-sdk-for-java/issues/38345)
- The default retry logic of Managed Identity has been updated to retry 5 times, the retry delay increases exponentially, starting at 800 milliseconds and doubling with each subsequent retry, up to 5 retries. This change is designed to optimize the retry mechanism, reducing the likelihood of congestion and improving the overall stability of service connections under varying network conditions. [#38345](https://github.com/Azure/azure-sdk-for-java/issues/38345)
- The `DefaultAzureCredential` caches the last working credential by default and tries it directly on subsequent attempts. This change is designed to improve the performance of the `DefaultAzureCredential` by reducing the number of attempts required to acquire a token. [#36867](https://github.com/Azure/azure-sdk-for-java/issues/36867)

## 1.6.0

### Behavioral change to credential types supporting multi-tenant authentication

As of `azure-identity` 1.6.0, the default behavior of credentials supporting multi-tenant authentication has changed. Each of these credentials will throw an `ClientAuthenticationException` if the requested `tenantId` doesn't match the tenant ID originally configured on the credential. Apps must now do one of the following things:

- Add all IDs, of tenants from which tokens should be acquired, to the `additionallyAllowedTenants` list on the credential builder. For example:

```java
DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder()
    .additionallyAllowedTenants("<tenant_id_1>", "tenant_id_2>")
    .build();
```

- Add `*` to enable token acquisition from any tenant. This is the original behavior and is compatible previous versions supporting multi tenant authentication. For example:

```java
DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder()
            .additionallyAllowedTenants("*")
            .build();
```

Note: Credential types which do not require a `tenantId` on construction will only throw `ClientAuthenticationException` when the application has provided a value for `tenantId` on the credential builder. If no `tenantId` is specified when building the credential, the credential will acquire tokens for any requested `tenantId` regardless of the value of `additionallyAllowedTenants`.

More information on this change and the consideration behind it can be found [here](https://aka.ms/azsdk/blog/multi-tenant-guidance).
