## Azure Cache for Redis: Azure AD with Lettuce client library

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
        <groupId>io.lettuce</groupId>
        <artifactId>lettuce-core</artifactId>
        <version>6.2.0.RELEASE</version>
    </dependency>
   ```

#### Samples Guidance
Familiarity with the [Lettuce](https://github.com/lettuce-io/lettuce-core) and [Azure Identity for Java](https://docs.microsoft.com/azure/developer/java/sdk/identity) client libraries is assumed.

* [Authenticate with Azure AD - Hello World](#authenticate-with-azure-ad-hello-world):
This sample is recommended for users getting started to use Azure AD authentication with Azure Cache for Redis.

* [Authenticate with Azure AD - Handle Reauthentication](#authenticate-with-azure-ad-handle-reauthentication):
This sample is recommended to users looking to build long-running applications that would like to handle reauthenticating with Azure AD upon token expiry.

* [Authenticate with Azure AD - Using Token Cache](#authenticate-with-azure-ad-using-token-cache):
This sample is recommended to users looking to build long-running applications that would like to handle reauthenticating with a Token cache caching a non expired Azure AD token.

##### Challenges
The Lettuce client handshake process is described below. The RESP 3 Protocol uses `HELLO` command, which currently isn't supported on the service side.
![image](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/images/lettuce-aad-handshake.png)

##### Raw Redis CLI Connection Behavior
```java

// Lettuce RESP 2 Protocol.

cache-name.redis.cache.windows.net:6379> AUTH user eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6ImpTMVhvMU9XRGpfNTJ2YndHTmd2UU8yVnpNYyIsImtpZCI6ImpTMVhvMU9XRGpfNTJ2YndHTmd2UU8yVnpNYyJ9.eyJhdWQiOiJodHRwczovLyouY2FjaGVpbmZyYS53aW5kb3dzLm5ldDoxMDIyNS9hcHBpZCIsImlzcyI6Imh0dHBzOi8vc3RzLndpbmRvd3MubmV0LzcyZjk4OGJmLTg2ZjEtNDFhZi05MWFiLTJkN2NkMDExZGI0Ny8iLCJpYXQiOjE2NTA4OTkxMzUsIm5iZiI6MTY1MDg5OTEzNSwiZXhwIjoxNjUwOTg1ODM1LCJhaW8iOiJFMlpnWU1pWmVIWVpZOC8xRHhjdjl2ZlhPKzdjQWdBPSIsImFwcGlkIjoiZmM1.....

OK


// Lettuce RESP 3 Protocol.
cache-name.redis.cache.windows.net:6379> HELLO 3 AUTH user eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6ImpTMVhvMU9XRGpfNTJ2YndHTmd2UU8yVnpNYyIsImtpZCI6ImpTMVhvMU9XRGpfNTJ2YndHTmd2UU8yVnpNYyJ9.eyJhdWQiOiJodHRwczovLyouY2FjaGVpbmZyYS53aW5kb3dzLm5ldDoxMDIyNS9hcHBpZCIsImlzcyI6Imh0dHBzOi8vc3RzLndpbmRvd3MubmV0LzcyZjk4OGJmLTg2ZjEtNDFhZi05MWFiLTJkN2NkMDExZGI0Ny8iLCJpYXQiOjE2NTA4OTkxMzUsIm5iZiI6MTY1MDg5OTEzNSwiZXhwIjoxNjUwOTg1ODM1LCJhaW8iOiJFMlpnWU1pWmVIWVpZOC8xRHhjdjl2ZlhPKzdjQWdBPSIsImFwcGlkIjoi.....

(error) WRONGPASS invalid username-password pair
```

##### Authenticate with Azure AD: Hello World
This sample is intended to assist in authenticating with Azure AD via the Lettuce client library. It focuses on displaying the logic required to fetch an Azure AD access token and to use it as password when setting up the Lettuce client instance.

##### Migration Guidance
When migrating your existing your application code, you need to replace the password input with the Azure AD token.
Integrate the logic in your application code to fetch an Azure AD access token via the Azure Identity library as shown below and replace it with the password configuring/retrieving logic in your application code.

##### Version 6.2.0.RELEASE or above
```xml
<dependency>
    <groupId>io.lettuce</groupId>
    <artifactId>lettuce-core</artifactId>
    <version>6.2.0.RELEASE</version>
</dependency>
```

```java
//Construct a Token Credential from Identity library, e.g. DefaultAzureCredential / ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();

// Build Redis URI with host and authentication details.
// TODO: Replace Host Name with Azure Cache for Redis Host Name.
RedisURI redisURI = RedisURI.Builder.redis("<HOST_NAME>") // Host Name is Required
    .withPort(6380) // Port is Required
    .withSsl(true) // SSL Connections are required.
    .withAuthentication(RedisCredentialsProvider.from(() -> new AzureRedisCredentials("USERNAME", defaultAzureCredential))) // Username and Token Credential are required.
    .withClientName("LettuceClient")
    .build();

// Create Lettuce Redis Client
RedisClient client = RedisClient.create(redisURI);

// Configure the client options.
client.setOptions(ClientOptions.builder()
    .socketOptions(SocketOptions.builder()
        .keepAlive(true) // Keep the connection alive to work with Azure Redis Cache
        .build())
    .protocolVersion(ProtocolVersion.RESP2) // Use RESP2 Protocol to ensure AUTH command is used for handshake.
    .build());

StatefulRedisConnection<String, String> connection = client.connect(StringCodec.UTF8);

// Create the connection, in this case we're using a sync connection, but you can create async / reactive connections as needed.
RedisStringCommands sync = connection.sync();
sync.set("Az:testKey", "testVal");
System.out.println(sync.get("Az:testKey").toString());


// Implementation of Redis Credentials used above.
/**
 * Redis Credential Implementation for Azure Redis for Cache
 */
public static class AzureRedisCredentials implements RedisCredentials {
    // Note: The Scopes value will change as the Azure AD Authentication support hits public preview and eventually GA's.
    private TokenRequestContext tokenRequestContext = new TokenRequestContext()
        .addScopes("https://*.cacheinfra.windows.net:10225/appid/.default");
    private TokenCredential tokenCredential;
    private final String username;

    /**
     * Create instance of Azure Redis Credentials
     * @param username the username to be used for authentication.
     * @param tokenCredential the token credential to be used to fetch requests.
     */
    public AzureRedisCredentials(String username, TokenCredential tokenCredential) {
        Objects.requireNonNull(username, "Username is required");
        Objects.requireNonNull(tokenCredential, "Token Credential is required");
        this.username = username;
        this.tokenCredential = tokenCredential;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean hasUsername() {
        return username != null;
    }

    @Override
    public char[] getPassword() {
        return tokenCredential
            .getToken(tokenRequestContext).block().getToken()
            .toCharArray();
    }

    @Override
    public boolean hasPassword() {
        return tokenCredential != null;
    }
}
```

##### Version 6.1.8.RELEASE or less
```xml
  <dependency>
      <groupId>io.lettuce</groupId>
      <artifactId>lettuce-core</artifactId>
      <version>6.1.8.RELEASE</version>
  </dependency>
```
```java
//Construct a Token Credential from Identity library, e.g. DefaultAzureCredential / ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();

// Fetch an Azure AD token to be used for authentication. The Azure AD token will be used as password.
// Note: The Scopes parameter will change as the Azure AD Authentication support hits public preview and eventually GA's.
String token = defaultAzureCredential
    .getToken(new TokenRequestContext()
        .addScopes("https://*.cacheinfra.windows.net:10225/appid/.default")).block().getToken();

// Build Redis URI with host and authentication details.
// TODO: Replace Host Name with Azure Cache for Redis Host Name.
RedisURI redisURI = RedisURI.Builder.redis("<HOST_NAME>") // Host Name is Required.
    .withPort(6380) //Port is Required.
    .withSsl(true) // SSL Connection is Required.
    .withAuthentication("<USERNAME>", token) // Username is Required.
    .withClientName("LettuceClient")
    .build();

// Create Lettuce Redis Client
RedisClient client = RedisClient.create(redisURI);

// Configure the client options.
client.setOptions(ClientOptions.builder()
    .socketOptions(SocketOptions.builder()
        .keepAlive(true) // Keep the connection alive to work with Azure Redis Cache
        .build())
    .protocolVersion(ProtocolVersion.RESP2) // Use RESP2 Protocol to ensure AUTH command is used for handshake.
    .build());

StatefulRedisConnection<String, String> connection = client.connect(StringCodec.UTF8);

// Create the connection, in this case we're using a sync connection, but you can create async / reactive connections as needed.
RedisStringCommands sync = connection.sync();
sync.set("Az:testKey", "testVal");
System.out.println(sync.get("Az:testKey").toString());
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

##### Authenticate with Azure AD: Handle Reauthentication
This sample is intended to assist in authenticating with Azure AD via Lettuce client library. It focuses on displaying the logic required to fetch an Azure AD access token and to use it as password when setting up the Lettuce Redis client instance. It also shows how to recreate and authenticate the Lettuce client instance when its connection is broken in error/exception scenarios.

##### Migration Guidance
When migrating your existing your application code, you need to replace the password input with the Azure AD token.
Integrate the logic in your application code to fetch an Azure AD access token via the Azure Identity library as shown below and replace it with the password configuring/retrieving logic in your application code.

##### Version 6.2.0.RELEASE or above
```xml
<dependency>
    <groupId>io.lettuce</groupId>
    <artifactId>lettuce-core</artifactId>
    <version>6.2.0.RELEASE</version>
</dependency>
```

```java

//Construct a Token Credential from Identity library, e.g. DefaultAzureCredential / ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();

// Host Name, Port, Username and Azure AD Token are required here.
// TODO: Replace <HOST_NAME> with Azure Cache for Redis Host name.
RedisClient client = createLettuceRedisClient("<HOST_NAME>", 6380, "<USERNAME>", defaultAzureCredential);
StatefulRedisConnection<String, String> connection = client.connect(StringCodec.UTF8);

int maxTries = 3;
int i = 0;
while (i < maxTries) {
    // Create the connection, in this case we're using a sync connection, but you can create async / reactive connections as needed.
    RedisStringCommands sync = connection.sync();
    try {
        sync.set("Az:testKey", "testVal");
        System.out.println(sync.get("Az:testKey").toString());
    } catch (RedisException e) {
        // TODO: Handle the Exception as required in your application.
        e.printStackTrace();

        // For Exceptions containing Invalid Username Password / Permissions not granted error messages, look at troubleshooting section at the end of document.

        if (!connection.isOpen()) {
            // Recreate the connection
            connection = client.connect(StringCodec.UTF8);
            sync = connection.sync();
        }
    } catch (Exception e) {
        // Handle the Exception as required in your application.
        e.printStackTrace();
    }
    i++;
}

// Helper Code
private static RedisClient createLettuceRedisClient(String hostName, int port, String username, TokenCredential tokenCredential) {

    // Build Redis URI with host and authentication details.
    RedisURI redisURI = RedisURI.Builder.redis(hostName)
        .withPort(port)
        .withSsl(true) // Targeting SSL Based 6380 port.
        .withAuthentication(RedisCredentialsProvider.from(() -> new AzureRedisCredentials("USERNAME", tokenCredential)))
        .withClientName("LettuceClient")
        .build();

    // Create Lettuce Redis Client
    RedisClient client = RedisClient.create(redisURI);

    // Configure the client options.
    client.setOptions(ClientOptions.builder()
        .socketOptions(SocketOptions.builder()
            .keepAlive(true) // Keep the connection alive to work with Azure Redis Cache
            .build())
        .protocolVersion(ProtocolVersion.RESP2) // Use RESP2 Protocol to ensure AUTH command is used for handshake.
        .build());

    return client;
}

/**
 * Redis Credential Implementation for Azure Redis for Cache
 */
public static class AzureRedisCredentials implements RedisCredentials {
    private TokenRequestContext tokenRequestContext = new TokenRequestContext()
        .addScopes("https://*.cacheinfra.windows.net:10225/appid/.default");
    private TokenCredential tokenCredential;
    private final String username;

    /**
     * Create instance of Azure Redis Credentials
     * @param username the username to be used for authentication.
     * @param tokenCredential the token credential to be used to fetch requests.
     */
    public AzureRedisCredentials(String username, TokenCredential tokenCredential) {
        Objects.requireNonNull(username, "Username is required");
        Objects.requireNonNull(tokenCredential, "Token Credential is required");
        this.username = username;
        this.tokenCredential = tokenCredential;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean hasUsername() {
        return username != null;
    }

    @Override
    public char[] getPassword() {
        return tokenCredential
            .getToken(tokenRequestContext).block().getToken()
            .toCharArray();
    }

    @Override
    public boolean hasPassword() {
        return tokenCredential != null;
    }
}
```

##### Version 6.1.8.RELEASE or less
```xml
  <dependency>
      <groupId>io.lettuce</groupId>
      <artifactId>lettuce-core</artifactId>
      <version>6.1.8.RELEASE</version>
  </dependency>
```

```java
//Construct a Token Credential from Identity library, e.g. DefaultAzureCredential / ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();

// Fetch an AAD token to be used for authentication. This token will be used as the password.
// Note: The Scopes parameter will change as the Azure AD Authentication support hits public preview and eventually GA's.
TokenRequestContext trc = new TokenRequestContext().addScopes("https://*.cacheinfra.windows.net:10225/appid/.default");
AccessToken accessToken = getAccessToken(defaultAzureCredential, trc);

// Host Name, Port, Username and Azure AD Token are required here.
// TODO: Replace <HOST_NAME> with Azure Cache for Redis Host name.
RedisClient client = createLettuceRedisClient("<HOST_NAME>", 6380, "<USERNAME>", accessToken);
StatefulRedisConnection<String, String> connection = client.connect(StringCodec.UTF8);

int maxTries = 3;
int i = 0;

while (i < maxTries) {
    // Create the connection, in this case we're using a sync connection, but you can create async / reactive connections as needed.
    RedisStringCommands sync = connection.sync();
    try {
        sync.set("Az:testKey", "testVal");
        System.out.println(sync.get("Az:testKey").toString());
        break;
    } catch (RedisException e) {
        // TODO: Handle the Exception as required in your application.
        e.printStackTrace();

        // For Exceptions containing Invalid Username Password / Permissions not granted error messages, look at troubleshooting section at the end of document.

        if (!connection.isOpen()) {
            // Recreate the client with a fresh token non-expired token as password for authentication.
            client = createLettuceRedisClient("<HOST_NAME>", 6380, "USERNAME", getAccessToken(defaultAzureCredential, trc));
            connection = client.connect(StringCodec.UTF8);
            sync = connection.sync();
        }
    } catch (Exception e) {
        // Handle the Exception as required in your application.
        e.printStackTrace();
    }
    i++;
}

// Helper code
private static RedisClient createLettuceRedisClient(String hostName, int port, String username, AccessToken accessToken) {

    // Build Redis URI with host and authentication details.
    RedisURI redisURI = RedisURI.Builder.redis(hostName)
        .withPort(port)
        .withSsl(true) // Targeting SSL based port
        .withAuthentication(username, accessToken.getToken())
        .withClientName("LettuceClient")
        .build();

    // Create Lettuce Redis Client
    RedisClient client = RedisClient.create(redisURI);

    // Configure the client options.
    client.setOptions(ClientOptions.builder()
        .socketOptions(SocketOptions.builder()
            .keepAlive(true) // Keep the connection alive to work with Azure Redis Cache
            .build())
        .protocolVersion(ProtocolVersion.RESP2) // Use RESP2 Protocol to ensure AUTH command is used for handshake.
        .build());

    return client;
}

private static AccessToken getAccessToken(TokenCredential tokenCredential, TokenRequestContext trc) {
    return tokenCredential.getToken(trc).block();
}

```

#### Authenticate with Azure AD: Using Token Cache
This sample is intended to assist in authenticating with Azure AD via the Lettuce client library. It focuses on displaying the logic required to fetch an Azure AD access token using a token cache and to use it as password when setting up the Lettuce instance. It also shows how to recreate and authenticate the Lettuce instance using the cached access token when the client's connection is broken in error/exception scenarios. The token cache stores and proactively refreshes the Azure AD access token 2 minutes before expiry and ensures a non-expired token is available for use when the cache is accessed.

##### Migration Guidance
When migrating your existing your application code, you need to replace the password input with the Azure AD token.
Integrate the logic in your application code to fetch an Azure AD access token via the Azure Identity library. Store the token in a token cache, as shown below. Replace the token with the password configuring/retrieving logic in your application code.

##### Version 6.2.0.RELEASE or above
```xml
<dependency>
  <groupId>io.lettuce</groupId>
  <artifactId>lettuce-core</artifactId>
  <version>6.2.0.RELEASE</version>
</dependency>
```

```java
//Construct a Token Credential from Identity library, e.g. DefaultAzureCredential / ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();

// Host Name, Port, Username and Azure AD Token are required here.
// TODO: Replace <HOST_NAME> with Azure Cache for Redis Host name.
RedisClient client = createLettuceRedisClient("<HOST_NAME>", 6380, "<USERNAME>", defaultAzureCredential);
StatefulRedisConnection<String, String> connection = client.connect(StringCodec.UTF8);

int maxTries = 3;
int i = 0;
while (i < maxTries) {
    // Create the connection, in this case we're using a sync connection, but you can create async / reactive connections as needed.
    RedisStringCommands sync = connection.sync();
    try {
        sync.set("Az:testKey", "testVal");
        System.out.println(sync.get("Az:testKey").toString());
    } catch (RedisException e) {
        // Handle the Exception as required in your application.
        e.printStackTrace();

        // For Exceptions containing Invalid Username Password / Permissions not granted error messages, look at troubleshooting section at the end of document.

        if (!connection.isOpen()) {
            // Recreate the connection
            connection = client.connect(StringCodec.UTF8);
            sync = connection.sync();
        }
    } catch (Exception e) {
        // Handle the Exception as required in your application.
        e.printStackTrace();
    }
    i++;
}

// Helper Code
private static RedisClient createLettuceRedisClient(String hostName, int port, String username, TokenCredential tokenCredential) {

    // Build Redis URI with host and authentication details.
    RedisURI redisURI = RedisURI.Builder.redis(hostName)
        .withPort(port)
        .withSsl(true) // Targeting SSL Based 6380 port.
        .withAuthentication(RedisCredentialsProvider.from(() -> new HandleReauthentication.AzureRedisCredentials("USERNAME", tokenCredential)))
        .withClientName("LettuceClient")
        .build();

    // Create Lettuce Redis Client
    RedisClient client = RedisClient.create(redisURI);

    // Configure the client options.
    client.setOptions(ClientOptions.builder()
        .socketOptions(SocketOptions.builder()
            .keepAlive(true) // Keep the connection alive to work with Azure Redis Cache
            .build())
        .protocolVersion(ProtocolVersion.RESP2) // Use RESP2 Protocol to ensure AUTH command is used for handshake.
        .build());

    return client;
}

/**
 * Redis Credential Implementation for Azure Redis for Cache
 */
public static class AzureRedisCredentials implements RedisCredentials {
    private TokenRequestContext tokenRequestContext = new TokenRequestContext()
        .addScopes("https://*.cacheinfra.windows.net:10225/appid/.default");
    private TokenCredential tokenCredential;
    private TokenRefreshCache refreshCache;
    private final String username;

    /**
     * Create instance of Azure Redis Credentials
     * @param username the username to be used for authentication.
     * @param tokenCredential the token credential to be used to fetch requests.
     */
    public AzureRedisCredentials(String username, TokenCredential tokenCredential) {
        Objects.requireNonNull(username, "Username is required");
        Objects.requireNonNull(tokenCredential, "Token Credential is required");
        this.username = username;
        this.tokenCredential = tokenCredential;
        this.refreshCache = new TokenRefreshCache(tokenCredential, tokenRequestContext, Duration.ofMinutes(2));
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean hasUsername() {
        return username != null;
    }

    @Override
    public char[] getPassword() {
        return refreshCache.getAccessToken()
            .getToken().toCharArray();
    }

    @Override
    public boolean hasPassword() {
        return tokenCredential != null;
    }
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

##### Version 6.1.8.RELEASE or less
```xml
  <dependency>
      <groupId>io.lettuce</groupId>
      <artifactId>lettuce-core</artifactId>
      <version>6.1.8.RELEASE</version>
  </dependency>
```

```java

//Construct a Token Credential from Identity library, e.g. DefaultAzureCredential / ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();

// Fetch an AAD token to be used for authentication. This token will be used as the password.
// Note: The Scopes parameter will change as the Azure AD Authentication support hits public preview and eventually GA's.
TokenRequestContext trc = new TokenRequestContext().addScopes("https://*.cacheinfra.windows.net:10225/appid/.default");

// Instantiate the Token Refresh Cache, this cache will proactively refresh the access token 2 minutes before expiry.
TokenRefreshCache tokenRefreshCache = new TokenRefreshCache(defaultAzureCredential, trc, Duration.ofMinutes(2));;
AccessToken accessToken = tokenRefreshCache.getAccessToken();

// Host Name, Port, Username and Azure AD Token are required here.
// TODO: Replace <HOST_NAME> with Azure Cache for Redis Host name.
RedisClient client = createLettuceRedisClient("<HOST_NAME>", 6380, "USERNAME", accessToken);
StatefulRedisConnection<String, String> connection = client.connect(StringCodec.UTF8);

int maxTries = 3;
int i = 0;

while (i < maxTries) {
    // Create the connection, in this case we're using a sync connection, but you can create async / reactive connections as needed.
    RedisStringCommands sync = connection.sync();
    try {
        sync.set("Az:testKey", "testVal");
        System.out.println(sync.get("Az:testKey").toString());
        break;
    } catch (RedisException e) {
        // Handle the Exception as required in your application.
        e.printStackTrace();

        // For Exceptions containing Invalid Username Password / Permissions not granted error messages, look at troubleshooting section at the end of document.

        if (!connection.isOpen()) {
            // Recreate the client with a fresh token non-expired token as password for authentication.
            client = createLettuceRedisClient("<HOST_NAME>", 6380, "USERNAME", tokenRefreshCache.getAccessToken());
            connection = client.connect(StringCodec.UTF8);
            sync = connection.sync();
        }
    } catch (Exception e) {
        // Handle the Exception as required in your application.
        e.printStackTrace();
    }
    i++;
}

// Helper code
private static RedisClient createLettuceRedisClient(String hostName, int port, String username, AccessToken accessToken) {

    // Build Redis URI with host and authentication details.
    RedisURI redisURI = RedisURI.Builder.redis(hostName)
        .withPort(port)
        .withSsl(true) // Targeting SSL based port
        .withAuthentication(username, accessToken.getToken())
        .withClientName("LettuceClient")
        .build();

    // Create Lettuce Redis Client
    RedisClient client = RedisClient.create(redisURI);

    // Configure the client options.
    client.setOptions(ClientOptions.builder()
        .socketOptions(SocketOptions.builder()
            .keepAlive(true) // Keep the connection alive to work with Azure Redis Cache
            .build())
        .protocolVersion(ProtocolVersion.RESP2) // Use RESP2 Protocol to ensure AUTH command is used for handshake.
        .build());

    return client;
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
     * @return the {@link AccessToken}
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
