## Token caching in the Azure Identity client library

*Token caching* is a feature provided by the Azure Identity library that allows apps to:

- Improve their resilience and performance.
- Reduce the number of requests made to Azure Active Directory (Azure AD) to obtain access tokens.
  
When an app needs to access a protected Azure resource, it typically needs to obtain an access token from Azure AD. Obtaining that token involves sending a request to Azure AD. Azure AD then validates the user's credentials and issues an access token.

Token caching, via the Azure Identity library, allows the app to store this access token [in memory](#in-memory-token-caching) or [on disk](#persistent-token-caching). The token can then be retrieved quickly and easily the next time the app needs to access the same resource. The app can avoid making another request to Azure AD, which reduces network traffic and improves resilience.

### In-memory token caching

*In-memory token caching* is the default option provided by the Azure Identity library. This caching approach allows apps to store access tokens in memory. With in-memory token caching enabled, the library first determines if a valid access token for the requested resource is already stored in memory. If a valid token is found, it's returned to the app without the need to make another request to Azure AD.

The in-memory token cache provided by the Azure Identity library:

- Is thread-safe.
- Can be used by multiple threads concurrently.

**Note:** When Azure Identity library credentials are used with Azure service libraries (for example, Azure Blob Storage), the in-memory token caching is active in the `HttpPipeline` layer as well. All `TokenCredential` implementations are supported there, including custom implementations external to the Azure Identity library.

### Persistent token caching

*Persistent disk token caching* is an opt-in feature in the Azure Identity library. The feature allows apps to cache access tokens in an encrypted, persistent storage mechanism. As indicated in the following table, the storage mechanism differs across operating systems.

| Operating system | Storage mechanism |
|------------------|-------------------|
| Linux            | Keyring           |
| macOS            | Keychain          |
| Windows          | Disk              |

With persistent disk token caching enabled, the library first determines if a valid access token for the requested resource is already stored in the persistent cache. If a valid token is found, it's returned to the app without the need to make another request to Azure AD. Additionally, the tokens are preserved across app runs, which:

- Makes the app more resilient to failures.
- Ensures the app can continue to function during an Azure AD outage or disruption.

#### Code sample

The sample showcases how to activate persistence token caching in the credentials offered by the Azure Identity library. You need to specify `TokenCachePersistenceOptions` on the credential builder to activate persistent token caching.

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

### Credentials supporting token caching

The following table indicates the state of in-memory and persistent caching in each credential type.

**Note:** In-memory caching is activated by default. Persistent token caching needs to be enabled as shown in [Code sample](#code-sample).

| Credential                     | In-memory token caching                                                | Persistent disk token caching |
|--------------------------------|------------------------------------------------------------------------|-------------------------------|
| `AuthorizationCodeCredential`  | Supported                                                              | Supported                     |
| `AzureCliCredential`           | Not Supported                                                          | Not Supported                 |
| `AzureDeveloperCliCredential`  | Not Supported                                                          | Not Supported                 |
| `AzurePowershellCredential`    | Not Supported                                                          | Not Supported                 |
| `ClientAssertionCredential`    | Supported                                                              | Supported                     |
| `ClientCertificateCredential`  | Supported                                                              | Supported                     |
| `ClientSecretCredential`       | Supported                                                              | Supported                     |
| `DefaultAzureCredential`       | Supported if the target credential in the credential chain supports it | Not Supported                 |
| `DeviceCodeCredential`         | Supported                                                              | Supported                     |
| `IntelliJCredential`           | Supported                                                              | Not Supported                 |
| `InteractiveBrowserCredential` | Supported                                                              | Supported                     |
| `ManagedIdentityCredential`    | Supported                                                              | Not Supported                 |
| `OnBehalfOfCredential`         | Supported                                                              | Supported                     |
| `UsernamePasswordCredential`   | Supported                                                              | Supported                     |
| `WorkloadIdentityCredential`   | Supported                                                              | Supported                     |
