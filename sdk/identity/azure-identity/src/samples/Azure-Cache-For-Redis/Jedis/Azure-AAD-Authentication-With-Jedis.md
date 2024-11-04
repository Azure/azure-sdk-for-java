## Azure Cache for Redis: Microsoft Entra ID with Jedis client library

### Table of contents

- [Prerequisites](#prerequisites)
- [Authenticate with Microsoft Entra ID - Hello World](#authenticate-with-azure-ad-hello-world)
- [Authenticate with Microsoft Entra ID - Handle Reauthentication](#authenticate-with-azure-ad-handle-reauthentication)
- [Authenticate with Microsoft Entra ID - Using Token Cache](#authenticate-with-azure-ad-using-token-cache)
- [Authenticate with Microsoft Entra ID - Azure Jedis Wrapper](#authenticate-with-azure-ad-azure-jedis-wrapper)
- [Troubleshooting](#troubleshooting)


#### Prerequisites
 - Configuration of Role and Role Assignments is required before using the sample code in this document.
 - Familiarity with the [Jedis](https://www.javadoc.io/doc/redis.clients/jedis/latest/index.html) and [Azure Identity for Java](https://learn.microsoft.com/azure/developer/java/sdk/identity) client libraries is assumed.
 - **Dependency Requirements:**
    ```xml
    <dependency>
        <groupId>com.azure</groupId>
        <artifactId>azure-identity</artifactId>
        <version>1.11.2</version> <!-- {x-version-update;com.azure:azure-identity;dependency} -->
    </dependency>
    
    <dependency>
        <groupId>redis.clients</groupId>
        <artifactId>jedis</artifactId>
        <version>5.1.0</version>  <!-- {x-version-update;redis.clients:jedis;external_dependency} -->
    </dependency>
    ```

#### Samples Guidance
* [Authenticate with Microsoft Entra ID - Hello World](#authenticate-with-azure-ad-hello-world):
    This sample is recommended for users getting started to use Microsoft Entra authentication with Azure Cache for Redis.

* [Authenticate with Microsoft Entra ID - Handle Reauthentication](#authenticate-with-azure-ad-handle-reauthentication):
  This sample is recommended to users looking to build long-running applications that would like to handle reauthenticating with Microsoft Entra ID upon token expiry.

* [Authenticate with Microsoft Entra ID - Using Token Cache](#authenticate-with-azure-ad-using-token-cache):
  This sample is recommended to users looking to build long-running applications that would like to integrate our recommended wrapper implementation in their application which handles reconnection and re-authentication on user's behalf.

* [Authenticate with Microsoft Entra ID - Azure Jedis Wrapper](#authenticate-with-azure-ad-azure-jedis-wrapper):
  This sample is recommended to users building long-running applications that would like to integrate our recommended wrapper implementation in their application which handles reconnection and reauthentication on user's behalf.


#### Authenticate with Microsoft Entra ID: Hello World
This sample is intended to assist in authenticating with Microsoft Entra ID via the Jedis client library. It focuses on displaying the logic required to fetch a Microsoft Entra access token and to use it as password when setting up the Jedis instance.

##### Migration Guidance
When migrating your existing your application code, you need to replace the password input with the Microsoft Entra token.
Integrate the logic in your application code to fetch a Microsoft Entra access token via the Azure Identity library as shown below and replace it with the password configuring/retrieving logic in your application code.

```java
//Construct a Token Credential from Identity library, e.g. DefaultAzureCredential / ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();

// Fetch a Microsoft Entra token to be used for authentication. This token will be used as the password.
String token = defaultAzureCredential
    .getToken(new TokenRequestContext()
        .addScopes("https://redis.azure.com/.default")).block().getToken();

// SSL connection is required.
boolean useSsl = true;
// TODO: Replace Host Name with Azure Cache for Redis Host Name.
String cacheHostname = "<HOST_NAME>";
String username = extractUsernameFromToken(token);

// Create Jedis client and connect to the Azure Cache for Redis over the TLS/SSL port using the access token as password.
// Note, Redis Cache Host Name and Port are required below
Jedis jedis = new Jedis(cacheHostname, 6380, DefaultJedisClientConfig.builder()
    .password(token) // Microsoft Entra access token as password is required.
    .user(username) // Username is Required
    .ssl(useSsl) // SSL Connection is Required
    .build());

// Set a value against your key in the Redis cache.
jedis.set("Az:key", "testValue");
System.out.println(jedis.get("Az:key"));

// Close the Jedis Client
jedis.close();


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
This sample is intended to assist in authenticating with Microsoft Entra ID via Jedis client library. It focuses on displaying the logic required to fetch a Microsoft Entra access token and to use it as password when setting up the Jedis instance. It also shows how to recreate and authenticate the Jedis instance when its connection is broken in error/exception scenarios.

##### Migration Guidance
When migrating your existing your application code, you need to replace the password input with the Microsoft Entra token.
Integrate the logic in your application code to fetch a Microsoft Entra access token via the Azure Identity library as shown below and replace it with the password configuring/retrieving logic in your application code.

```java
//Construct a Token Credential from Identity library, e.g. DefaultAzureCredential / ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();

// Fetch a Microsoft Entra token to be used for authentication. This token will be used as the password.
TokenRequestContext trc = new TokenRequestContext().addScopes("https://redis.azure.com/.default");
AccessToken accessToken = getAccessToken(defaultAzureCredential, trc);

// SSL connection is required.
boolean useSsl = true;
// TODO: Replace <HOST_NAME> with Azure Cache for Redis Host name.
String cacheHostname = "<HOST_NAME>";
String username = extractUsernameFromToken(accessToken.getToken());

// Create Jedis client and connect to the Azure Cache for Redis over the TLS/SSL port using the access token as password.
// Note: Cache Host Name, Port, Microsoft Entra access token and SSL connections are required below.
Jedis jedis = createJedisClient(cacheHostname, 6380, username, accessToken, useSsl);

int maxTries = 3;
int i = 0;

while (i < maxTries) {
    try {
        // Set a value against your key in the Redis cache.
        jedis.set("Az:key", "testValue");
        System.out.println(jedis.get("Az:key"));
        break;
    } catch (JedisException e) {
        // TODO: Handle The Exception as required in your application.
        e.printStackTrace();

        // For Exceptions containing Invalid Username Password / Permissions not granted error messages, look at troubleshooting section at the end of document.

        // Check if the client is broken, if it is then close and recreate it to create a new healthy connection.
        if (jedis.isBroken()) {
            jedis.close();
            accessToken = getAccessToken(defaultAzureCredential, trc);
            jedis = createJedisClient(cacheHostname, 6380, username, accessToken, useSsl);
        }
    }
    i++;
}

// Close the Jedis Client
jedis.close();

// Helper Code
private static Jedis createJedisClient(String cacheHostname, int port, String username, AccessToken accessToken, boolean useSsl) {
    return new Jedis(cacheHostname, port, DefaultJedisClientConfig.builder()
        .password(accessToken.getToken())
        .user(username)
        .ssl(useSsl)
        .build());
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
This sample is intended to assist in authenticating with Microsoft Entra ID via the Jedis client library. It focuses on displaying the logic required to fetch a Microsoft Entra access token using a token cache and to use it as password when setting up the Jedis instance. It also shows how to recreate and authenticate the Jedis instance using the cached access token when the client's connection is broken in error/exception scenarios. The token cache stores and proactively refreshes the Microsoft Entra access token 2 minutes before expiry and ensures a non-expired token is available for use when the cache is accessed.

##### Migration Guidance
When migrating your existing your application code, you need to replace the password input with the Microsoft Entra token.
Integrate the logic in your application code to fetch a Microsoft Entra access token via the Azure Identity library. Store the token in a token cache, as shown below. Replace the token with the password configuring/retrieving logic in your application code.

```java
//Construct a Token Credential from Identity library, e.g. DefaultAzureCredential / ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();

// Fetch a Microsoft Entra token to be used for authentication. This token will be used as the password.
TokenRequestContext trc = new TokenRequestContext().addScopes("https://redis.azure.com/.default");
TokenRefreshCache tokenRefreshCache = new TokenRefreshCache(defaultAzureCredential, trc);
AccessToken accessToken = tokenRefreshCache.getAccessToken();

// SSL connection is required.
boolean useSsl = true;
// TODO: Replace <HOST_NAME> with Azure Cache for Redis Host name.
String cacheHostname = "<HOST_NAME>";
String username = extractUsernameFromToken(accessToken.getToken());

// Create Jedis client and connect to the Azure Cache for Redis over the TLS/SSL port using the access token as password.
// Note: Cache Host Name, Port, Microsoft Entra access token and SSL connections are required below.
Jedis jedis = createJedisClient(cacheHostname, 6380, username, accessToken, useSsl);

// Configure the jedis instance for proactive authentication before token expires.
tokenRefreshCache
    .setJedisInstanceToAuthenticate(jedis);
    
int maxTries = 3;
int i = 0;

while (i < maxTries) {
    try {
        // Set a value against your key in the Redis cache.
        jedis.set("Az:key", "testValue");
        System.out.println(jedis.get("Az:key"));
        break;
    } catch (JedisException e) {
        // Handle The Exception as required in your application.
        e.printStackTrace();

        // For Exceptions containing Invalid Username Password / Permissions not granted error messages, look at troubleshooting section at the end of document.

        // Check if the client is broken, if it is then close and recreate it to create a new healthy connection.
        if (jedis.isBroken()) {
            jedis.close();
            accessToken = tokenRefreshCache.getAccessToken();
            jedis = createJedisClient(cacheHostname, 6380, username, accessToken, useSsl);
            
            // Configure the jedis instance for proactive authentication before token expires.
            tokenRefreshCache
                .setJedisInstanceToAuthenticate(jedis);
        }
    }
    i++;
}
// Close the Jedis Client
jedis.close();

// Helper Code
private static Jedis createJedisClient(String cacheHostname, int port, String username, AccessToken accessToken, boolean useSsl) {
    return new Jedis(cacheHostname, port, DefaultJedisClientConfig.builder()
        .password(accessToken.getToken())
        .user(username)
        .ssl(useSsl)
        .build());
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
    private Jedis jedisInstanceToAuthenticate;
    private String username;

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
            username = extractUsernameFromToken(accessToken.getToken());
            System.out.println("Refreshed Token with Expiry: " + accessToken.getExpiresAt().toEpochSecond());

            if (jedisInstanceToAuthenticate != null && !CoreUtils.isNullOrEmpty(username)) {
                jedisInstanceToAuthenticate.auth(username, accessToken.getToken());
                System.out.println("Refreshed Jedis Connection with fresh access token, token expires at : "
                    + accessToken.getExpiresAt().toEpochSecond());
            }
            timer.schedule(new TokenRefreshTask(), getTokenRefreshDelay());
        }
    }

    private long getTokenRefreshDelay() {
        return ((accessToken.getExpiresAt()
            .minusSeconds(ThreadLocalRandom.current().nextLong(baseRefreshOffset.getSeconds(), maxRefreshOffset.getSeconds()))
            .toEpochSecond() - OffsetDateTime.now().toEpochSecond()) * 1000);
    }

    /**
     * Sets the Jedis to proactively authenticate before token expiry.
     * @param jedisInstanceToAuthenticate the instance to authenticate
     * @return the updated instance
     */
    public TokenRefreshCache setJedisInstanceToAuthenticate(Jedis jedisInstanceToAuthenticate) {
        this.jedisInstanceToAuthenticate = jedisInstanceToAuthenticate;
        return this;
    }
}
```

#### Authenticate with Microsoft Entra ID: Azure Jedis Wrapper
This sample is intended to assist in the migration from Jedis to `AzureJedisClientBuilder`. It focuses on side-by-side comparisons for similar operations between the two libraries.

##### Migration benefits

A natural question to ask when considering whether or not to adopt a new version or library is what the benefits of doing so would be. Jedis by itself doesn't support Microsoft Entra authentication with token generation, failure retries, broken connection handling, and cache reauthentication. Using `AzureJedisClient` will improve developer productivity and code maintainability.

##### Client instantiation

In Jedis, you create a `Jedis` object via a public constructor. The constructor accepts the cache host name and port number. It authenticates using the access keys. For example:

```java
import redis.clients.jedis.Jedis;

Jedis jedis = new Jedis("<host name>", <port number>);
jedis.auth("<username>", "<token>");
jedis.set("key", "value");
jedis.close();
```

With `AzureJedisClient`, client instances are created via builders. The builder accepts the:

- Cache host name
- Port number to connect to
- Username set on the cache
- Optional retry options to configure retry
- Token credential object that's used to generate a token

The following table compares the capabilities of the Jedis and Azure Jedis clients.

| Feature | Jedis | Azure Jedis |
|--|--|--|
| Connect to Redis Cache | Yes |Yes |
| Microsoft Entra Authentication Support | No | Yes|
| Retry Failure | No| Yes |
| Reauthenticate | No |Yes |
| Handle Broken Connection | No |Yes |

See the following example of setting up the Azure Jedis client:

Note: The wrapper code located [here](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity/src/samples/Azure-Cache-For-Redis/Jedis/java/com/azure/jedis) must be incorporated into your application code for the below sample to work.


```java
//Construct a Token Credential from Identity library, e.g. DefaultAzureCredential / ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();

// Create Jedis Client using the builder as follows.
Jedis jedisClient = new AzureJedisClientBuilder()
    .cacheHostName("<HOST_NAME>") // TODO: Replace <HOST_NAME> with Azure Cache for Redis Host name.
    .port(6380) // Port is required.
    .useSSL(true) // SSL Connection is required.
    .credential(defaultAzureCredential) // A Token Credential is required to fetch Microsoft Entra access tokens.
    .build();

// Set a value against your key in the Redis cache.
jedisClient.set("Az:key", "sample");
System.out.println(jedisClient.get("Az:key"));

// Close the Jedis Client
jedisClient.close();
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
