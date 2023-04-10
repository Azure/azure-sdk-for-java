## Token Caching in Azure Identity Client Library

Token caching is a feature provided by the Azure Identity client library that allows applications to improve their resilience, performance, and reduce the number of requests made to Azure Active Directory (Azure AD) to obtain access tokens.
When an application needs to access a protected resource, it typically needs to obtain an access token from Azure AD. This involves sending a request to Azure AD, which then validates the user's credentials and issues an access token.
Token caching allows the application to store this access token in memory or on disk so that it can be retrieved quickly and easily the next time the application needs to access the same resource. This eliminates the need for the application to make another request to Azure AD, which can reduce network traffic and improve resilience.
The Azure Identity client library offers both in-memory caching and persistent disk caching.

### In Memory Token Caching
In-memory token caching is the default option provided by the Azure Identity client library for Java, which allows applications to store access tokens in memory.
With in-memory token caching enabled, the library will first check if a valid access token for the requested resource is already stored in memory. If a valid token is found, it is returned to the application without the need to make another request to Azure AD, which can improve resilience, performance, and reduce latency. The in-memory token cache provided by the Azure Identity Java client library is thread-safe and can be used by multiple threads concurrently.
**Note:** When using Identity credentials with Azure Service SDKs like Azure Storage, KeyVault, and others, the in-memory token caching is active in the `HttpPipeline` layer as well. All TokenCredential implementations are supported there, including custom implementations external to the Identity library.

### Persistent Token Caching

Persistent disk token caching is an opt-in feature in the Azure Identity client library for Java, which allows applications to cache access tokens in an encrypted persistent storage on `Disk/KeyChain/KeyRing` for `Windows/Mac/Linux` platforms, respectively.
With persistent disk token caching enabled, the library will first check if a valid access token for the requested resource is already stored in the persistent cache. If a valid token is found, it is returned to the application without the need to make another request to Azure AD, which can improve resilience, performance, and reduce latency. Additionally, the tokens are preserved across application runs, which makes the application more resilient to failures and helps to ensure that it can continue to function even in the event of an outage or disruption to Azure AD.

#### Code Sample
The sample showcases how to activate persistence token caching the in credentials offered by Identity client library.
You need to specify `TokenCachePersistenceOptions` on the credential builder to activate persistent token caching.

```java 
TokenCachePersistenceOptions persistenceOptions = new TokenCachePersistenceOptions()
        .setName("your-cache-name"); // Optional
        
ClientSecretCredential clientCredential = new ClientSecretCredentialBuilder()
        .clientId("your-client-id")
        .clientSecret("your-client-secret")
        .tenantId("your-tenant-id")
        .tokenCachePersistenceOptions(persistenceOptions)
        .build();
```

### Credentials supporting Token Caching

The table below shows the credentials in the Identity client library that support in memory and persistent caching.

**Note:** In memory caching is activated by default, the persistent token caching needs to be enabled as displayed above.

| Credential | In Memory Token Caching | Persistent Disk Token Caching |  
|---|---|---|  
| `ClientSecretCredential` | Supported | Supported |
| `ClientCertificateCredential` | Supported | Supported |
| `ManagedIdentityCredential` | Supported | Not Supported |
| `ClientAssertionCredential` | Supported | Supported |
| `OnBehalfOfCredential` | Supported | Supported |
| `InteractiveBrowserCredential` | Supported | Supported |
| `DeviceCodeCredential` | Supported | Supported |
| `UsernamePasswordCredential` | Supported | Supported |
| `AuthorizationCodeCredential` | Supported | Supported |
| `IntelliJCredential` | Supported | Not Supported |
| `AzurePowershellCredential` | Not Supported | Not Supported |
| `AzureCliCredential` | Not Supported | Not Supported |
| `AzureDeveloperCliCredential` | Not Supported | Not Supported |
| `WorkloadIdentityCredential` | Supported | Supported |
| `DefaultAzureCredential` | Supported if the target credential in the DAC chain supports it | Not Supported

