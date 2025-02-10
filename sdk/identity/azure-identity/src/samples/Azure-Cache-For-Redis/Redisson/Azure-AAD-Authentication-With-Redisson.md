## Azure Cache for Redis: Microsoft Entra ID with Redisson client library

### Table of contents

- [Prerequisites](#prerequisites)
- [Authenticate with Microsoft Entra ID - Hello World](#authenticate-with-azure-ad-hello-world)
- [Authenticate with Microsoft Entra ID - Handle Reauthentication](#authenticate-with-azure-ad-handle-reauthentication)
- [Authenticate with Microsoft Entra ID - Using Token Cache](#authenticate-with-azure-ad-using-token-cache)
- [Troubleshooting](#troubleshooting)

#### Prerequisites
- Configuration of Role and Role Assignments is required before using the sample code in this document.
- Familiarity with the [Redisson](https://github.com/redisson/redisson) and [Azure Identity for Java](https://learn.microsoft.com/azure/developer/java/sdk/identity) client libraries is required.
- **Dependency Requirements:**
   ```xml
    <dependency>
        <groupId>com.azure</groupId>
        <artifactId>azure-identity</artifactId>
        <version>1.11.2</version> <!-- {x-version-update;com.azure:azure-identity;dependency} -->
    </dependency>
    
    <dependency>
        <groupId>org.redisson</groupId>
        <artifactId>redisson</artifactId>
        <version>3.27.0</version> <!-- {x-version-update;org.redisson:redisson;external_dependency} -->
    </dependency>
   ```

#### Samples Guidance
* [Authenticate with Microsoft Entra ID - Hello World](#authenticate-with-azure-ad-hello-world):
   This sample is recommended for users getting started to use Microsoft Entra authentication with Azure Cache for Redis.

* [Authenticate with Microsoft Entra ID - Handle Reauthentication](#authenticate-with-azure-ad-handle-reauthentication):
   This sample is recommended to users looking to build long-running applications and would like to handle reauthenticating with Microsoft Entra ID upon token expiry.

* [Authenticate with Microsoft Entra ID - Using Token Cache](#authenticate-with-azure-ad-using-token-cache):
  This sample is recommended to users looking to build long-running applications that would like to handle reauthenticating with a token cache. The token cache stores and proactively refreshes the Microsoft Entra access token 2 minutes before expiry and ensures a non-expired token is available for use when the cache is accessed.

#### Authenticate with Microsoft Entra ID: Hello World
This sample is intended to assist in authenticating with Microsoft Entra ID via the Redisson client library. It focuses on displaying the logic required to fetch a Microsoft Entra access token and to use it as password when setting up the Redisson client instance. It Further shows how to recreate and authenticate the Redisson Client instance when its connection is broken in error/exception scenarios.

##### Migration Guidance
When migrating your existing application code, replace the password input with the Microsoft Entra token.
Integrate the logic in your application code to fetch a Microsoft Entra access token via the Azure Identity library as shown below and replace it with the password configuring/retrieving logic in your application code.

```java
//Construct a Token Credential from Identity library, e.g. DefaultAzureCredential / ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();

// Fetch a Microsoft Entra token to be used for authentication.
String token = defaultAzureCredential
    .getToken(new TokenRequestContext()
        .addScopes("https://redis.azure.com/.default")).block().getToken();

String username = extractUsernameFromToken(token);

// Create Client Configuration
Config config = new Config();
config.useSingleServer()
    .setAddress("rediss://<HOST_NAME>:6380") // TODO: Replace Host Name with Azure Cache for Redis Host Name.
    .setKeepAlive(true) // Keep the connection alive.
    .setUsername(username) // Username is Required
    .setPassword(token) // Microsoft Entra access token as password is required.
    .setClientName("Reddison-Client");

RedissonClient redisson = Redisson.create(config);

// perform operations
RBuckets rBuckets =  redisson.getBuckets();
RBucket<String> bucket = redisson.getBucket("Az:key");
bucket.set("This is object value");

String objectValue = bucket.get();
System.out.println("stored object value: " + objectValue);

redisson.shutdown();

private static String extractUsernameFromToken(String token) {
    String[] parts = token.split("\\.");
    String base64 = parts[1];

    switch (base64.length() % 4) {
        case 2:
            base64 += "==";
            break;
        case 3:
            base64 += "=";
            break;
    }

    byte[] jsonBytes = Base64.getDecoder().decode(base64);
    String json = new String(jsonBytes, StandardCharsets.UTF_8);
    JsonObject jwt = JsonParser.parseString(json).getAsJsonObject();

    return jwt.get("oid").getAsString();
}
```

##### Supported Token Credentials for Microsoft Entra Authentication
**Note:** The samples in this doc use the Azure Identity library's `DefaultAzureCredential` to fetch a Microsoft Entra access token. The other supported `TokenCredential` implementations that can be used from [Azure Identity for Java](https://learn.microsoft.com/azure/developer/java/sdk/identity) are as follows:
* [Client Certificate Credential](https://learn.microsoft.com/azure/developer/java/sdk/identity-service-principal-auth#client-certificate-credential)
* [Client Secret Credential](https://learn.microsoft.com/azure/developer/java/sdk/identity-service-principal-auth#client-secret-credential)
* [Managed Identity Credential](https://learn.microsoft.com/azure/developer/java/sdk/identity-azure-hosted-auth#managed-identity-credential)
* [Username Password Credential](https://learn.microsoft.com/azure/developer/java/sdk/identity-user-auth#username-password-credential)
* [Azure CLI Credential](https://learn.microsoft.com/azure/developer/java/sdk/identity-dev-env-auth#azure-cli-credential)
* [Interactive Browser Credential](https://learn.microsoft.com/azure/developer/java/sdk/identity-user-auth#interactive-browser-credential)
* [Device Code Credential](https://learn.microsoft.com/azure/developer/java/sdk/identity-user-auth#device-code-credential)

#### Authenticate with Microsoft Entra ID: Handle Reauthentication
This sample is intended to assist in authenticating with Microsoft Entra ID via Redisson client library. It focuses on displaying the logic required to fetch a Microsoft Entra access token and to use it as password when setting up the Redisson client instance. It also shows how to recreate and authenticate the Redisson client instance when its connection is broken in error/exception scenarios.

##### Migration Guidance
When migrating your existing application code, replace the password input with the Microsoft Entra token.
Integrate the logic in your application code to fetch a Microsoft Entra access token via the Azure Identity library as shown below and replace it with the password configuring/retrieving logic in your application code.

```java

//Construct a Token Credential from Identity library, e.g. DefaultAzureCredential / ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();

// Fetch a Microsoft Entra token to be used for authentication. This token will be used as the password.
TokenRequestContext trc = new TokenRequestContext().addScopes("https://redis.azure.com/.default");
AccessToken accessToken = getAccessToken(defaultAzureCredential, trc);
String username = extractUsernameFromToken(accessToken.getToken());

// Create Redisson Client
// Host Name, Port, and Microsoft Entra token are required here.
// TODO: Replace <HOST_NAME> with Azure Cache for Redis Host name.
RedissonClient redisson = createRedissonClient("rediss://<HOST_NAME>:6380", username, accessToken);

int maxTries = 3;
int i = 0;

while (i < maxTries) {
    try {
        // perform operations
        RBuckets rBuckets = redisson.getBuckets();
        RBucket<String> bucket = redisson.getBucket("Az:key");
        bucket.set("This is object value");

        String objectValue = bucket.get();
        System.out.println("stored object value: " + objectValue);
        break;
    } catch (RedisException exception) {
        // TODO: Handle Exception as Required.
        exception.printStackTrace();

        // For Exceptions containing Invalid Username Password / Permissions not granted error messages, look at troubleshooting section at the end of document.

        if (redisson.isShutdown()) {
            AccessToken token = getAccessToken(defaultAzureCredential, trc);
            // Recreate the client with a fresh token non-expired token as password for authentication.
            redisson = createRedissonClient("rediss://<HOST_NAME>:6380", username, token);
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

private static String extractUsernameFromToken(String token) {
    String[] parts = token.split("\\.");
    String base64 = parts[1];

    switch (base64.length() % 4) {
        case 2:
        base64 += "==";
        break;
        case 3:
        base64 += "=";
        break;
    }

    byte[] jsonBytes = Base64.getDecoder().decode(base64);
    String json = new String(jsonBytes, StandardCharsets.UTF_8);
    JsonObject jwt = JsonParser.parseString(json).getAsJsonObject();

    return jwt.get("oid").getAsString();
}
```


#### Authenticate with Microsoft Entra ID: Using Token Cache
This sample is intended to assist in authenticating with Microsoft Entra ID via the Redisson client library. It focuses on displaying the logic required to fetch a Microsoft Entra access token using a token cache and to use it as password when setting up the Redisson client instance. It also shows how to recreate and authenticate the Redisson client instance using the cached access token when its connection is broken in error/exception scenarios. The token cache stores and proactively refreshes the Microsoft Entra access token 2 minutes before expiry and ensures a non-expired token is available for use when the cache is accessed.

##### Migration Guidance
When migrating your existing application code, replace the password input with the Microsoft Entra token.
Integrate the logic in your application code to fetch a Microsoft Entra access token via the Azure Identity library. Store the token in a token cache, as shown below. Replace the token with the password configuring/retrieving logic in your application code.

```java
//Construct a Token Credential from Identity library, e.g. DefaultAzureCredential / ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();

// Fetch a Microsoft Entra token to be used for authentication. This token will be used as the password.
TokenRequestContext trc = new TokenRequestContext().addScopes("https://redis.azure.com/.default");

// Instantiate the Token Refresh Cache, this cache will proactively refresh the access token 2 - 5 minutes before expiry.
TokenRefreshCache tokenRefreshCache = new TokenRefreshCache(defaultAzureCredential, trc);
AccessToken accessToken = tokenRefreshCache.getAccessToken();
String username = extractUsernameFromToken(accessToken.getToken());

// Create Redisson Client
// Host Name, Port, and Microsoft Entra token are required here.
// TODO: Replace <HOST_NAME> with Azure Cache for Redis Host name.
RedissonClient redisson = createRedissonClient("rediss://<HOST_NAME>:6380", username, accessToken);

int maxTries = 3;
int i = 0;

while (i < maxTries) {
    try {
        // perform operations
        RBuckets rBuckets = redisson.getBuckets();
        RBucket<String> bucket = redisson.getBucket("Az:key");
        bucket.set("This is object value");

        String objectValue = bucket.get().toString();
        System.out.println("stored object value: " + objectValue);
        break;
    } catch (RedisException exception) {
        // TODO: Handle Exception as Required.
        exception.printStackTrace();

        // For Exceptions containing Invalid Username Password / Permissions not granted error messages, look at troubleshooting section at the end of document.

        if (redisson.isShutdown()) {
            AccessToken token = tokenRefreshCache.getAccessToken();
            // Recreate the client with a fresh token non-expired token as password for authentication.
            redisson = createRedissonClient("rediss://<HOST_NAME>:6380", username, token);
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

private static String extractUsernameFromToken(String token) {
    String[] parts = token.split("\\.");
    String base64 = parts[1];

    switch (base64.length() % 4) {
        case 2:
        base64 += "==";
        break;
        case 3:
        base64 += "=";
        break;
    }

    byte[] jsonBytes = Base64.getDecoder().decode(base64);
    String json = new String(jsonBytes, StandardCharsets.UTF_8);
    JsonObject jwt = JsonParser.parseString(json).getAsJsonObject();

    return jwt.get("oid").getAsString();
}

/**
 * The token cache to store and proactively refresh the access token.
 */
public static class TokenRefreshCache {
    private final TokenCredential tokenCredential;
    private final TokenRequestContext tokenRequestContext;
    private final Timer timer;
    private volatile AccessToken accessToken;
    private final Duration maxRefreshOffset = Duration.ofMinutes(5);
    private final Duration baseRefreshOffset = Duration.ofMinutes(2);

    /**
     * Creates an instance of TokenRefreshCache
     * @param tokenCredential the token credential to be used for authentication.
     * @param tokenRequestContext the token request context to be used for authentication.
     */
    public TokenRefreshCache(TokenCredential tokenCredential, TokenRequestContext tokenRequestContext) {
        this.tokenCredential = tokenCredential;
        this.tokenRequestContext = tokenRequestContext;
        this.timer = new Timer();
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
            .minusSeconds(ThreadLocalRandom.current().nextLong(baseRefreshOffset.getSeconds(), maxRefreshOffset.getSeconds()))
            .toEpochSecond() - OffsetDateTime.now().toEpochSecond()) * 1000);
    }
}
```

#### Troubleshooting

##### Invalid Username Password Pair Error
In this error scenario, the username provided and the access token used as password are not compatible.
To mitigate this error, navigate to your Azure Cache for Redis resource in the Azure portal. Confirm that:
* In **Data Access Configuration**, you've assigned the required role to your user/service principal identity.
* Under **Authentication** -> **Microsoft Entra Authentication** category the **Enable Microsoft Entra Authentication** box is selected. If not, select it and select the **Save** button.

##### Permissions not granted / NOPERM Error
In this error scenario, the authentication was successful, but your registered user/service principal is not granted the RBAC permission to perform the action.
To mitigate this error, navigate to your Azure Cache for Redis resource in the Azure portal. Confirm that:
* In **Data Access Configuration**, you've assigned the appropriate role (Owner, Contributor, Reader) to your user/service principal identity.
* In the event you're using a custom role, ensure the permissions granted under your custom role include the one required for your target action.

##### Managed Identity not working from Local Development Machine
Managed identity does not work from a local development machine. To use managed identity, your code must be running
in an Azure VM (or another type of resource in Azure). To run locally with Entra ID authentication, you'll need to
use a service principal or user account. This is a common source of confusion, so ensure that when developing locally,
you configure your application to use a service principal or user credentials for authentication.
