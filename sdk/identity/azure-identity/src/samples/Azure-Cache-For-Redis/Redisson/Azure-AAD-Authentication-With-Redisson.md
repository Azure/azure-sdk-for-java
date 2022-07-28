## Azure Cache for Redis: Azure AD with Redisson client library

### Table of contents

- [Prerequisites](#prerequisites)
- [Authenticate with Azure AD - Hello World](#authenticate-with-azure-ad-hello-world)
- [Authenticate with Azure AD - Handle Reauthentication](#authenticate-with-azure-ad-handle-reauthentication)
- [Authenticate with Azure AD - Using Token Cache](#authenticate-with-azure-ad-using-token-cache)
- [Troubleshooting](#troubleshooting)

#### Prerequisites
- Configuration of Role and Role Assignments is required before using the sample code in this document.
- **Dependency Requirements:**
   ```xml
    <dependency>
        <groupId>com.azure</groupId>
        <artifactId>azure-identity</artifactId>
        <version>1.5.0</version>
    </dependency>
    
    <dependency>
        <groupId>org.redisson</groupId>
        <artifactId>redisson</artifactId>
        <version>3.17.0</version>
    </dependency>
   ```

#### Samples Guidance
Familiarity with the [Redisson](https://github.com/redisson/redisson) and [Azure Identity for Java](https://docs.microsoft.com/azure/developer/java/sdk/identity) client libraries is assumed.

* [Authenticate with Azure AD - Hello World](#authenticate-with-azure-ad-hello-world):
   This sample is recommended for users getting started to use Azure AD authentication with Azure Cache for Redis.

* [Authenticate with Azure AD - Handle Reauthentication](#authenticate-with-azure-ad-handle-reauthentication):
   This sample is recommended to users looking to build long-running applications and would like to handle reauthenticating with Azure AD upon token expiry.

* [Authenticate with Azure AD - Using Token Cache](#authenticate-with-azure-ad-using-token-cache):
  This sample is recommended to users looking to build long-running applications that would like to handle reauthenticating with a Token cache. The Token Cache stores and proactively refreshes the Azure AD Access Token 2 minutes before expiry and ensures a non-expired token is available for use when the cache is accessed.

#### Authenticate with Azure AD: Hello World
This sample is intended to assist in authenticating with Azure AD via the Redisson client library. It focuses on displaying the logic required to fetch an Azure AD access token and to use it as password when setting up the Redisson client instance. It Further shows how to recreate and authenticate the Redisson Client instance when its connection is broken in error/exception scenarios.

##### Migration Guidance
When migrating your existing application code, replace the password input with the Azure AD token.
Integrate the logic in your application code to fetch an Azure AD access token via the Azure Identity library as shown below and replace it with the password configuring/retrieving logic in your application code.

```java
//Construct a Token Credential from Identity library, e.g. DefaultAzureCredential / ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();

// Fetch an Azure AD token to be used for authentication.
// Note: The Scopes parameter will change as the Azure AD Authentication support hits public preview and eventually GA's.
String token = defaultAzureCredential
    .getToken(new TokenRequestContext()
        .addScopes("https://*.cacheinfra.windows.net:10225/appid/.default")).block().getToken();

// Create Client Configuration
Config config = new Config();
config.useSingleServer()
    .setAddress("redis://<HOST_NAME>:6380") // TODO: Replace Host Name with Azure Cache for Redis Host Name.
    .setKeepAlive(true) // Keep the connection alive.
    .setUsername("<USERNAME>") // Username is Required
    .setPassword(token) // Azure AD Access Token as password is required.
    .setClientName("Reddison-Client");

RedissonClient redisson = Redisson.create(config);

// perform operations
RBuckets rBuckets =  redisson.getBuckets();
RBucket bucket = redisson.getBucket("Az:key");
bucket.set("This is object value");

String objectValue = bucket.get().toString();
System.out.println("stored object value: " + objectValue);

redisson.shutdown();
```

##### Supported Token Credentials for Azure AD Authentication
**Note:** The samples in this doc use the Azure Identity library's `DefaultAzureCredential` to fetch an Azure AD access token. The other supported `TokenCredential` implementations that can be used from [Azure Identity for Java](https://docs.microsoft.com/azure/developer/java/sdk/identity) are as follows:
* [Client Certificate Credential](https://docs.microsoft.com/azure/developer/java/sdk/identity-service-principal-auth#client-certificate-credential)
* [Client Secret Credential](https://docs.microsoft.com/azure/developer/java/sdk/identity-service-principal-auth#client-secret-credential)
* [Managed Identity Credential](https://docs.microsoft.com/azure/developer/java/sdk/identity-azure-hosted-auth#managed-identity-credential)
* [Username Password Credential](https://docs.microsoft.com/azure/developer/java/sdk/identity-user-auth#username-password-credential)
* [Azure CLI Credential](https://docs.microsoft.com/azure/developer/java/sdk/identity-dev-env-auth#azure-cli-credential)
* [Interactive Browser Credential](https://docs.microsoft.com/azure/developer/java/sdk/identity-user-auth#interactive-browser-credential)
* [Device Code Credential](https://docs.microsoft.com/azure/developer/java/sdk/identity-user-auth#device-code-credential)

#### Authenticate with Azure AD: Handle Reauthentication
This sample is intended to assist in authenticating with Azure AD via Redisson client library. It focuses on displaying the logic required to fetch an Azure AD access token and to use it as password when setting up the Redisson client instance. It also shows how to recreate and authenticate the Redisson client instance when its connection is broken in error/exception scenarios.

##### Migration Guidance
When migrating your existing application code, replace the password input with the Azure AD token.
Integrate the logic in your application code to fetch an Azure AD access token via the Azure Identity library as shown below and replace it with the password configuring/retrieving logic in your application code.

```java
//Construct a Token Credential from Identity library, e.g. DefaultAzureCredential / ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();

// Fetch an Azure AD token to be used for authentication. This token will be used as the password.
// Note: The Scopes parameter will change as the Azure AD Authentication support hits public preview and eventually GA's.
TokenRequestContext trc = new TokenRequestContext().addScopes("https://*.cacheinfra.windows.net:10225/appid/.default");
AccessToken accessToken = getAccessToken(defaultAzureCredential, trc);

// Create Redisson Client
// Host Name, Port, Username and Azure AD Token are required here.
// TODO: Replace <HOST_NAME> with Azure Cache for Redis Host name.
RedissonClient redisson = createRedissonClient("redis://<HOST_NAME>:6380", "<USERNAME>", accessToken);

int maxTries = 3;
int i = 0;

while (i < maxTries) {
    try {
        // perform operations
        RBuckets rBuckets = redisson.getBuckets();
        RBucket bucket = redisson.getBucket("Az:key");
        bucket.set("This is object value");

        String objectValue = bucket.get().toString();
        System.out.println("stored object value: " + objectValue);
        break;
    } catch (RedisException exception) {
        // TODO: Handle Exception as Required.
        exception.printStackTrace();

        // For Exceptions containing Invalid Username Password / Permissions not granted error messages, look at troubleshooting section at the end of document.

        if (redisson.isShutdown()) {
            // Recreate the client with a fresh token non-expired token as password for authentication.
            redisson = createRedissonClient("redis://<HOST_NAME>:6380", "<USERNAME>", getAccessToken(defaultAzureCredential, trc));
        }
    } catch (Exception e) {
        // Handle Exception as required
        e.printStackTrace();
    }
    i++;
}
redisson.shutdown();

// Helper Code
private static RedissonClient createRedissonClient(String address, String username, AccessToken accessToken) {

    Config config = new Config();
    config.useSingleServer()
        .setAddress(address)
        .setKeepAlive(true)
        .setUsername(username)
        .setPassword(accessToken.getToken())
        .setClientName("Reddison-Client");

    return Redisson.create(config);
}

private static AccessToken getAccessToken(TokenCredential tokenCredential, TokenRequestContext trc) {
    return tokenCredential.getToken(trc).block();
}
```


#### Authenticate with Azure AD: Using Token Cache
This sample is intended to assist in authenticating with Azure AD via the Redisson client library. It focuses on displaying the logic required to fetch an Azure AD access token using a token cache and to use it as password when setting up the Redisson client instance. It also shows how to recreate and authenticate the Redisson client instance using the cached access token when its connection is broken in error/exception scenarios. The token cache stores and proactively refreshes the Azure AD access token 2 minutes before expiry and ensures a non-expired token is available for use when the cache is accessed.

##### Migration Guidance
When migrating your existing application code, replace the password input with the Azure AD token.
Integrate the logic in your application code to fetch an Azure AD access token via the Azure Identity library. Store the token in a token cache, as shown below. Replace the token with the password configuring/retrieving logic in your application code.

```java
//Construct a Token Credential from Identity library, e.g. DefaultAzureCredential / ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();

// Fetch an Azure AD token to be used for authentication. This token will be used as the password.
// Note: The Scopes parameter will change as the Azure AD Authentication support hits public preview and eventually GA's.
TokenRequestContext trc = new TokenRequestContext().addScopes("https://*.cacheinfra.windows.net:10225/appid/.default");

// Instantiate the Token Refresh Cache, this cache will proactively refresh the access token 2 minutes before expiry.
TokenRefreshCache tokenRefreshCache = new TokenRefreshCache(defaultAzureCredential, trc, Duration.ofMinutes(2));;
AccessToken accessToken = tokenRefreshCache.getAccessToken();

// Create Redisson Client
// Host Name, Port, Username and Azure AD Token are required here.
// TODO: Replace <HOST_NAME> with Azure Cache for Redis Host name.
RedissonClient redisson = createRedissonClient("redis://<HOST_NAME>:6380", "<USERNAME>", accessToken);

int maxTries = 3;
int i = 0;

while (i < maxTries) {
    try {
        // perform operations
        RBuckets rBuckets = redisson.getBuckets();
        RBucket bucket = redisson.getBucket("Az:key");
        bucket.set("This is object value");

        String objectValue = bucket.get().toString();
        System.out.println("stored object value: " + objectValue);
        break;
    } catch (RedisException exception) {
        // TODO: Handle Exception as Required.
        exception.printStackTrace();

        // For Exceptions containing Invalid Username Password / Permissions not granted error messages, look at troubleshooting section at the end of document.

        if (redisson.isShutdown()) {
            // Recreate the client with a fresh token non-expired token as password for authentication.
            redisson = createRedissonClient("redis://<HOST_NAME>:6380", "<USERNAME>", tokenRefreshCache.getAccessToken());
        }
    } catch (Exception e) {
        // Handle Exception as required
        e.printStackTrace();
    }
    i++;
}
redisson.shutdown();


// Helper Code
private static RedissonClient createRedissonClient(String address, String username, AccessToken accessToken) {

    Config config = new Config();
    config.useSingleServer()
        .setAddress(address)
        .setKeepAlive(true)
        .setUsername(username)
        .setPassword(accessToken.getToken())
        .setClientName("Reddison-Client");

    return Redisson.create(config);
}

/**
 * The token cache to store and proactively refresh the access token.
 */
public static class TokenRefreshCache {
    private final TokenCredential tokenCredential;
    private final TokenRequestContext tokenRequestContext;
    private final Timer timer;
    private volatile AccessToken accessToken;
    private final Duration refreshOffset;

    /**
     * Creates an instance of TokenRefreshCache
     * @param tokenCredential the token credential to be used for authentication.
     * @param tokenRequestContext the token request context to be used for authentication.
     * @param refreshOffset the refresh offset to use to proactively fetch a new access token before expiry time.
     */
    public TokenRefreshCache(TokenCredential tokenCredential, TokenRequestContext tokenRequestContext, Duration refreshOffset) {
        this.tokenCredential = tokenCredential;
        this.tokenRequestContext = tokenRequestContext;
        this.timer = new Timer();
        this.refreshOffset = refreshOffset;
    }

    /**
     * Gets the cached access token.
     * @return the AccessToken
     */
    public AccessToken getAccessToken() {
        if (accessToken != null) {
            return  accessToken;
        } else {
            TokenRefreshTask tokenRefreshTask = new TokenRefreshTask();
            accessToken = tokenCredential.getToken(tokenRequestContext).block();
            timer.schedule(tokenRefreshTask, getTokenRefreshDelay());
            return accessToken;
        }
    }

    private class TokenRefreshTask extends TimerTask {
        // Add your task here
        public void run() {
            accessToken = tokenCredential.getToken(tokenRequestContext).block();
            System.out.println("Refreshed Token with Expiry: " + accessToken.getExpiresAt().toEpochSecond());
            timer.schedule(new TokenRefreshTask(), getTokenRefreshDelay());
        }
    }

    private long getTokenRefreshDelay() {
        return ((accessToken.getExpiresAt()
            .minusSeconds(refreshOffset.getSeconds()))
            .toEpochSecond() - OffsetDateTime.now().toEpochSecond()) * 1000;
    }
}
```

#### Troubleshooting

##### Invalid Username Password Pair Error
In this error scenario, the username provided and the access token used as password are not compatible.
To mitigate this error, navigate to your Azure Cache for Redis resource in the Azure portal. Confirm that:
* In **RBAC Rules**, you've assigned the required role to your user/service principal identity.
* In **Advanced settings**, the **AAD access authorization** box is selected. If not, select it and select the **Save** button.

##### Permissions not granted / NOPERM Error
In this error scenario, the authentication was successful, but your registered user/service principal is not granted the RBAC permission to perform the action.
To mitigate this error, navigate to your Azure Cache for Redis resource in the Azure portal. Confirm that:
* In **RBAC Rules**, you've assigned the appropriate role (Owner, Contributor, Reader) to your user/service principal identity.
* In the event you're using a custom role, ensure the permissions granted under your custom role include the one required for your target action.

