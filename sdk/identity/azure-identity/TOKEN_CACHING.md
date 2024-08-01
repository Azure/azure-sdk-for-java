## Token caching in the Azure Identity client library

*Token caching* is a feature provided by the Azure Identity library that allows apps to:

- Improve their resilience and performance.
- Reduce the number of requests made to Microsoft Entra ID to obtain access tokens.
- Reduce the number of times the user is prompted to authenticate.

When an app needs to access a protected Azure resource, it typically needs to obtain an access token from Microsoft Entra ID. Obtaining that token involves sending a request to Microsoft Entra ID and may also involve prompting the user. Microsoft Entra ID then validates the credentials provided in the request and issues an access token.

Token caching, via the Azure Identity library, allows the app to store this access token [in memory](#in-memory-token-caching), where it's accessible to the current process, or [on disk](#persistent-token-caching) where it can be accessed across application or process invocations. The token can then be retrieved quickly and easily the next time the app needs to access the same resource. The app can avoid making another request to Microsoft Entra ID, which reduces network traffic and improves resilience. Additionally, in scenarios where the app is authenticating users, token caching also avoids prompting the user each time new tokens are requested.

### In-memory token caching

*In-memory token caching* is the default option provided by the Azure Identity library. This caching approach allows apps to store access tokens in memory. With in-memory token caching, the library first determines if a valid access token for the requested resource is already stored in memory. If a valid token is found, it's returned to the app without the need to make another request to Microsoft Entra ID. If a valid token isn't found, the library will automatically acquire a token by sending a request to Microsoft Entra ID. The in-memory token cache provided by the Azure Identity library is thread-safe.

**Note:** When Azure Identity library credentials are used with Azure service libraries (for example, Azure Blob Storage), the in-memory token caching is active in the `HttpPipeline` layer as well. All `TokenCredential` implementations are supported there, including custom implementations external to the Azure Identity library.

#### Caching cannot be disabled

As there are many levels of cache, it's not possible disable in-memory caching. However, the in-memory cache may be cleared by creating a new credential instance.

### Persistent token caching

*Persistent disk token caching* is an opt-in feature in the Azure Identity library. The feature allows apps to cache access tokens in an encrypted, persistent storage mechanism. As indicated in the following table, the storage mechanism differs across operating systems.

| Operating system | Storage mechanism |
|------------------|-------------------|
| Linux            | Keyring           |
| macOS            | Keychain          |
| Windows          | DPAPI             |

**Note:** The Linux platform allows for unencrypted persistent storage, which can be activated by setting `setUnencryptedStorageAllowed` to `true` on `TokenCachePersistenceOptions`. However, we do not recommend using this storage method due to its significantly lower security measures. In addition, tokens are not encrypted solely to the current user, which could potentially allow unauthorized access to the cache by individuals with machine access.

With persistent disk token caching enabled, the library first determines if a valid access token for the requested resource is already stored in the persistent cache. If a valid token is found, it's returned to the app without the need to make another request to Microsoft Entra ID. Additionally, the tokens are preserved across app runs, which:

- Makes the app more resilient to failures.
- Ensures the app can continue to function during a Microsoft Entra ID outage or disruption.
- Avoids having to prompt users to authenticate each time the process is restarted.

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

#### Silently authenticating a user with AuthenticationRecord and TokenCachePersistenceOptions
When authenticating a user via `InteractiveBrowserCredential`, `DeviceCodeCredential`, or `UsernamePasswordCredential`, an `AuthenticationRecord` can be persisted as well. The authentication record is:

- Returned from the `authenticate` API and contains data identifying an authenticated account.
- Needed to identify the appropriate entry in the persisted token cache to silently authenticate on subsequent executions.

There's no sensitive data in the `AuthenticationRecord`, so it can be persisted in a non-protected state.

Once an app has persisted an `AuthenticationRecord`, future authentications can be performed silently by setting `TokenCachePersistenceOptions` and `AuthenticationRecord` on the builder.

Here's an example of an app storing the `AuthenticationRecord` to the local file system after authenticating the user:

```java com.azure.identity.silentauthentication
String authenticationRecordPath = "path/to/authentication-record.json";
AuthenticationRecord authenticationRecord = null;
try {
    // If we have an existing record, deserialize it.
    if (Files.exists(new File(authenticationRecordPath).toPath())) {
         authenticationRecord = AuthenticationRecord.deserialize(new FileInputStream(authenticationRecordPath));
    }
} catch (FileNotFoundException e) {
    // Handle error as appropriate.
}

DeviceCodeCredentialBuilder builder = new DeviceCodeCredentialBuilder()
    .clientId(clientId)
    .tenantId(tenantId);
if (authenticationRecord != null) {
    // As we have a record, configure the builder to use it.
    builder.authenticationRecord(authenticationRecord);
}
DeviceCodeCredential credential = builder.build();
TokenRequestContext trc = new TokenRequestContext().addScopes("your-appropriate-scope");
if (authenticationRecord == null) {
    // We don't have a record, so we get one and store it. The next authentication will use it.
    credential.authenticate(trc).flatMap(record -> {
        try {
            return record.serializeAsync(new FileOutputStream(authenticationRecordPath));
        } catch (FileNotFoundException e) {
            return Mono.error(e);
        }
    }).subscribe();
}

// Now the credential can be passed to another service client or used directly.
AccessToken token = credential.getTokenSync(trc);

```

### Credentials supporting token caching

The following table indicates the state of in-memory and persistent caching in each credential type.

**Note:** In-memory caching is activated by default. Persistent token caching needs to be enabled as shown in this [code sample](#code-sample).

| Credential                     | In-memory token caching                                                 | Persistent disk token caching |
|--------------------------------|-------------------------------------------------------------------------|-------------------------------|
| `AuthorizationCodeCredential`  | Supported                                                               | Supported                     |
| `AzureCliCredential`           | Not Supported                                                           | Not Supported                 |
| `AzureDeveloperCliCredential`  | Not Supported                                                           | Not Supported                 |
| `AzurePipelinesCredential`     | Supported                                                               | Supported                     | 
| `AzurePowershellCredential`    | Not Supported                                                           | Not Supported                 |
| `ClientAssertionCredential`    | Supported                                                               | Supported                     |
| `ClientCertificateCredential`  | Supported                                                               | Supported                     |
| `ClientSecretCredential`       | Supported                                                               | Supported                     |
| `DefaultAzureCredential`       | Supported if the target credential in the credential chain supports it  | Not Supported                 |
| `DeviceCodeCredential`         | Supported                                                               | Supported                     |
| `EnvironmentCredential`        | Supported                                                               | Supported                     |
| `IntelliJCredential`           | Supported                                                               | Not Supported                 |
| `InteractiveBrowserCredential` | Supported                                                               | Supported                     |
| `ManagedIdentityCredential`    | Supported                                                               | Not Supported                 |
| `OnBehalfOfCredential`         | Supported                                                               | Supported                     |
| `UsernamePasswordCredential`   | Supported                                                               | Supported                     |
| `WorkloadIdentityCredential`   | Supported                                                               | Supported                     |
