## Azure Cache for Redis: Microsoft Entra ID with Lettuce client library

### Table of contents

- [Prerequisites](#prerequisites)
- [Authenticate with Microsoft Entra ID - Hello World](#authenticate-with-azure-ad-hello-world)
- [Authenticate with Microsoft Entra ID - Handle Reauthentication](#authenticate-with-azure-ad-handle-reauthentication)
- [Authenticate with Microsoft Entra ID - Using Token Cache](#authenticate-with-azure-ad-using-token-cache)
- [Troubleshooting](#troubleshooting)


#### Prerequisites
- Configuration of Role and Role Assignments is required before using the sample code in this document.
- Familiarity with the [Lettuce](https://github.com/lettuce-io/lettuce-core) and [Azure Identity for Java](https://learn.microsoft.com/azure/developer/java/sdk/identity) client libraries is required.
- **Dependency Requirements:**
   ```xml
    <dependency>
        <groupId>com.azure</groupId>
        <artifactId>azure-identity</artifactId>
        <version>1.11.2</version> <!-- {x-version-update;com.azure:azure-identity;dependency} -->
    </dependency>
    
    <dependency>
        <groupId>io.lettuce</groupId>
        <artifactId>lettuce-core</artifactId> 
        <version>6.3.1.RELEASE</version> <!-- {x-version-update;io.lettuce:lettuce-core;external_dependency} -->
    </dependency>
   ```

#### Samples Guidance
* [Authenticate with Microsoft Entra ID - Hello World](#authenticate-with-azure-ad-hello-world):
This sample is recommended for users getting started to use Microsoft Entra authentication with Azure Cache for Redis.

* [Authenticate with Microsoft Entra ID - Handle Reauthentication](#authenticate-with-azure-ad-handle-reauthentication):
This sample is recommended to users looking to build long-running applications that would like to handle reauthenticating with Microsoft Entra ID upon token expiry.

* [Authenticate with Microsoft Entra ID - Using Token Cache](#authenticate-with-azure-ad-using-token-cache):
This sample is recommended to users looking to build long-running applications that would like to handle reauthenticating with a token cache caching a non-expired Microsoft Entra token.

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

##### Authenticate with Microsoft Entra ID: Hello World
This sample is intended to assist in authenticating with Microsoft Entra ID via the Lettuce client library. It focuses on displaying the logic required to fetch a Microsoft Entra access token and to use it as password when setting up the Lettuce client instance.

##### Migration Guidance
When migrating your existing your application code, you need to replace the password input with the Microsoft Entra token.
Integrate the logic in your application code to fetch a Microsoft Entra access token via the Azure Identity library as shown below and replace it with the password configuring/retrieving logic in your application code.

##### Version 6.2.0.RELEASE or above
```xml
<dependency>
    <groupId>io.lettuce</groupId>
    <artifactId>lettuce-core</artifactId>
    <version>6.3.1.RELEASE</version> <!-- {x-version-update;io.lettuce:lettuce-core;external_dependency} -->
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
    .withAuthentication(RedisCredentialsProvider.from(() -> new AzureRedisCredentials(defaultAzureCredential))) // Username and Token Credential are required.
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
RedisStringCommands<String, String> sync = connection.sync();
sync.set("Az:testKey", "testVal");
System.out.println(sync.get("Az:testKey"));


// Implementation of Redis Credentials used above.
/**
 * Redis Credential Implementation for Azure Redis for Cache
 */
public static class AzureRedisCredentials implements RedisCredentials {
    // Note: The Scopes value will change as the Microsoft Entra authentication support hits public preview and eventually GA's.
    private TokenRequestContext tokenRequestContext = new TokenRequestContext()
        .addScopes("cca5fbb-b7e4-4009-81f1-37e38fd66d78/.default");
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

    /**
     * Create instance of Azure Redis Credentials
     * @param tokenCredential the token credential to be used to fetch requests.
     */
    public AzureRedisCredentials(TokenCredential tokenCredential) {
        Objects.requireNonNull(tokenCredential, "Token Credential is required");
        this.tokenCredential = tokenCredential;
        this.username = extractUsernameFromToken(tokenCredential.getToken(tokenRequestContext).block().getToken());
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

// Fetch a Microsoft Entra token to be used for authentication. The Microsoft Entra token will be used as password.
// Note: The Scopes parameter will change as the Microsoft Entra authentication support hits public preview and eventually GA's.
String token = defaultAzureCredential
    .getToken(new TokenRequestContext()
        .addScopes("cca5fbb-b7e4-4009-81f1-37e38fd66d78/.default")).block().getToken();

String username = extractUsernameFromToken(token);

// Build Redis URI with host and authentication details.
// TODO: Replace Host Name with Azure Cache for Redis Host Name.
RedisURI redisURI = RedisURI.Builder.redis("<HOST_NAME>") // Host Name is Required.
    .withPort(6380) //Port is Required.
    .withSsl(true) // SSL Connection is Required.
    .withAuthentication(username, token) // Username is Required.
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
RedisStringCommands<String, String> sync = connection.sync();
sync.set("Az:testKey", "testVal");
System.out.println(sync.get("Az:testKey"));

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

##### Authenticate with Microsoft Entra ID: Handle Reauthentication
This sample is intended to assist in authenticating with Microsoft Entra ID via the Lettuce client library. It focuses on displaying the logic required to fetch a Microsoft Entra access token and to use it as password when setting up the Lettuce Redis client instance. It also shows how to recreate and authenticate the Lettuce client instance when its connection is broken in error/exception scenarios.

##### Migration Guidance
When migrating your existing your application code, you need to replace the password input with the Microsoft Entra token.
Integrate the logic in your application code to fetch a Microsoft Entra access token via the Azure Identity library as shown below and replace it with the password configuring/retrieving logic in your application code.

##### Version 6.2.0.RELEASE or above
```xml
<dependency>
    <groupId>io.lettuce</groupId>
    <artifactId>lettuce-core</artifactId>
    <version>6.3.1.RELEASE</version> <!-- {x-version-update;io.lettuce:lettuce-core;external_dependency} -->
</dependency>
```

```java

//Construct a Token Credential from Identity library, e.g. DefaultAzureCredential / ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();

// Host Name, Port, and Microsoft Entra token are required here.
// TODO: Replace <HOST_NAME> with Azure Cache for Redis Host name.
RedisClient client = createLettuceRedisClient("<HOST_NAME>", 6380, defaultAzureCredential);
StatefulRedisConnection<String, String> connection = client.connect(StringCodec.UTF8);

int maxTries = 3;
int i = 0;
while (i < maxTries) {
    // Create the connection, in this case we're using a sync connection, but you can create async / reactive connections as needed.
    RedisStringCommands<String, String> sync = connection.sync();
    try {
        sync.set("Az:testKey", "testVal");
        System.out.println(sync.get("Az:testKey"));
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
private static RedisClient createLettuceRedisClient(String hostName, int port, TokenCredential tokenCredential) {

    // Build Redis URI with host and authentication details.
    RedisURI redisURI = RedisURI.Builder.redis(hostName)
        .withPort(port)
        .withSsl(true) // Targeting SSL Based 6380 port.
        .withAuthentication(RedisCredentialsProvider.from(() -> new AzureRedisCredentials(tokenCredential)))
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
    // Note: The Scopes parameter will change as the Microsoft Entra authentication support hits public preview and eventually GA's.
    private TokenRequestContext tokenRequestContext = new TokenRequestContext()
        .addScopes("https://redis.azure.com/.default");
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

    /**
     * Create instance of Azure Redis Credentials
     * @param tokenCredential the token credential to be used to fetch requests.
     */
    public AzureRedisCredentials(TokenCredential tokenCredential) {
        Objects.requireNonNull(tokenCredential, "Token Credential is required");
        this.tokenCredential = tokenCredential;
        this.username = extractUsernameFromToken(tokenCredential.getToken(tokenRequestContext).block().getToken());
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

// Fetch a Microsoft Entra token to be used for authentication. This token will be used as the password.
// Note: The Scopes parameter will change as the Microsoft Entra authentication support hits public preview and eventually GA's.
TokenRequestContext trc = new TokenRequestContext().addScopes("https://redis.azure.com/.default");
AccessToken accessToken = getAccessToken(defaultAzureCredential, trc);

// Host Name, Port, and Microsoft Entra token are required here.
// TODO: Replace <HOST_NAME> with Azure Cache for Redis Host name.
RedisClient client = createLettuceRedisClient("<HOST_NAME>", 6380, accessToken);
StatefulRedisConnection<String, String> connection = client.connect(StringCodec.UTF8);

int maxTries = 3;
int i = 0;

while (i < maxTries) {
    // Create the connection, in this case we're using a sync connection, but you can create async / reactive connections as needed.
    RedisStringCommands<String, String> sync = connection.sync();
    try {
        sync.set("Az:testKey", "testVal");
        System.out.println(sync.get("Az:testKey"));
        break;
    } catch (RedisException e) {
        // TODO: Handle the Exception as required in your application.
        e.printStackTrace();

        // For Exceptions containing Invalid Username Password / Permissions not granted error messages, look at troubleshooting section at the end of document.

        if (!connection.isOpen()) {
            // Recreate the client with a fresh token non-expired token as password for authentication.
            client = createLettuceRedisClient("<HOST_NAME>", 6380, getAccessToken(defaultAzureCredential, trc));
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
private static RedisClient createLettuceRedisClient(String hostName, int port, AccessToken accessToken) {

    // Build Redis URI with host and authentication details.
    RedisURI redisURI = RedisURI.Builder.redis(hostName)
        .withPort(port)
        .withSsl(true) // Targeting SSL based port
        .withAuthentication(extractUsernameFromToken(accessToken.getToken()), accessToken.getToken())
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
This sample is intended to assist in authenticating with Microsoft Entra ID via the Lettuce client library. It focuses on displaying the logic required to fetch a Microsoft Entra access token using a token cache and to use it as password when setting up the Lettuce instance. It also shows how to recreate and authenticate the Lettuce instance using the cached access token when the client's connection is broken in error/exception scenarios. The token cache stores and proactively refreshes the Microsoft Entra access token 2 minutes before expiry and ensures a non-expired token is available for use when the cache is accessed.

##### Migration Guidance
When migrating your existing your application code, you need to replace the password input with the Microsoft Entra token.
Integrate the logic in your application code to fetch a Microsoft Entra access token via the Azure Identity library. Store the token in a token cache, as shown below. Replace the token with the password configuring/retrieving logic in your application code.

##### Version 6.2.0.RELEASE or above
```xml
<dependency>
  <groupId>io.lettuce</groupId>
  <artifactId>lettuce-core</artifactId>
  <version>6.3.1.RELEASE</version> <!-- {x-version-update;io.lettuce:lettuce-core;external_dependency} -->
</dependency>
```

```java
//Construct a Token Credential from Identity library, e.g. DefaultAzureCredential / ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();

// Host Name, Port, and Microsoft Entra token are required here.
// TODO: Replace <HOST_NAME> with Azure Cache for Redis Host name.
String hostName = "<HOST_NAME>";

AzureRedisCredentials credentials = new AzureRedisCredentials(defaultAzureCredential);
RedisClient client = createLettuceRedisClient(hostName, 6380, RedisCredentialsProvider.from(() -> credentials));
StatefulRedisConnection<String, String> connection = client.connect(StringCodec.UTF8);

// Create the connection, in this case we're using a sync connection, but you can create async / reactive connections as needed.
RedisCommands<String, String> sync = connection.sync();

credentials.getTokenCache()
    .setLettuceInstanceToAuthenticate(sync);

int maxTries = 3;
int i = 0;
while (i < maxTries) {
    try {
        sync.set("Az:testKey", "testVal");
        System.out.println(sync.get("Az:testKey"));
        break;
    } catch (RedisException e) {
        // Handle the Exception as required in your application.
        e.printStackTrace();

        // For Exceptions containing Invalid Username Password / Permissions not granted error messages, look at troubleshooting section at the end of document.

        if (!connection.isOpen()) {
            // Recreate the connection
            connection = client.connect(StringCodec.UTF8);
            sync = connection.sync();


            credentials.getTokenCache()
                .setLettuceInstanceToAuthenticate(sync);
        }
    } catch (Exception e) {
        // Handle the Exception as required in your application.
        e.printStackTrace();
    }
    i++;
}

// Helper Code
private static RedisClient createLettuceRedisClient(String hostName, int port, RedisCredentialsProvider credentialsProvider) {

    // Build Redis URI with host and authentication details.
    RedisURI redisURI = RedisURI.Builder.redis(hostName)
        .withPort(port)
        .withSsl(true) // Targeting SSL Based 6380 port.
        .withAuthentication(credentialsProvider)
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
        .addScopes("https://redis.azure.com/.default");
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
        this.refreshCache = new TokenRefreshCache(tokenCredential, tokenRequestContext);
    }

    /**
     * Create instance of Azure Redis Credentials
     * @param tokenCredential the token credential to be used to fetch requests.
     */
    public AzureRedisCredentials(TokenCredential tokenCredential) {
        Objects.requireNonNull(tokenCredential, "Token Credential is required");
        this.tokenCredential = tokenCredential;
        this.username = extractUsernameFromToken(tokenCredential.getToken(tokenRequestContext).block().getToken());
        this.refreshCache = new TokenRefreshCache(tokenCredential, tokenRequestContext);
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

    public TokenRefreshCache getTokenCache() {
        return this.refreshCache;
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
    private final Duration maxRefreshOffset = Duration.ofMinutes(5);
    private final Duration baseRefreshOffset = Duration.ofMinutes(2);
    private RedisCommands<String, String> lettuceInstanceToAuthenticate;
    private String username;

    /**
     * Creates an instance of TokenRefreshCache
     * @param tokenCredential the token credential to be used for authentication.
     * @param tokenRequestContext the token request context to be used for authentication.
     */
    public TokenRefreshCache(TokenCredential tokenCredential, TokenRequestContext tokenRequestContext) {
        this.tokenCredential = tokenCredential;
        this.tokenRequestContext = tokenRequestContext;
        this.timer = new Timer(true);
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

            if (lettuceInstanceToAuthenticate != null && !CoreUtils.isNullOrEmpty(username)) {
                lettuceInstanceToAuthenticate.auth(username, accessToken.getToken());
                System.out.println("Refreshed Lettuce Connection with fresh access token, token expires at : "
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
     * Sets the Lettuce instance to proactively authenticate before token expiry.
     * @param lettuceInstanceToAuthenticate the instance to authenticate
     * @return the updated instance
     */
    public TokenRefreshCache setLettuceInstanceToAuthenticate(RedisCommands<String, String> lettuceInstanceToAuthenticate) {
        this.lettuceInstanceToAuthenticate = lettuceInstanceToAuthenticate;
        return this;
    }
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

// Fetch a Microsoft Entra token to be used for authentication. This token will be used as the password.
// Note: The Scopes parameter will change as the Microsoft Entra authentication support hits public preview and eventually GA's.
TokenRequestContext trc = new TokenRequestContext().addScopes("https://redis.azure.com/.default");

// Instantiate the Token Refresh Cache, this cache will proactively refresh the access token 2 minutes before expiry.
TokenRefreshCache tokenRefreshCache = new TokenRefreshCache(defaultAzureCredential, trc);
AccessToken accessToken = tokenRefreshCache.getAccessToken();

// Host Name, Port, and Microsoft Entra token are required here.
// TODO: Replace <HOST_NAME> with Azure Cache for Redis Host name.
RedisClient client = createLettuceRedisClient("<HOST_NAME>", 6380, accessToken);
StatefulRedisConnection<String, String> connection = client.connect(StringCodec.UTF8);

// Create the connection, in this case we're using a sync connection, but you can create async / reactive connections as needed.
RedisCommands<String, String> sync = connection.sync();
    
// Configure the lettuce instance for proactive authentication before token expires.
tokenRefreshCache.setLettuceInstanceToAuthenticate(sync);
    
int maxTries = 3;
int i = 0;

while (i < maxTries) {
    try {
        sync.set("Az:testKey", "testVal");
        System.out.println(sync.get("Az:testKey"));
        break;
    } catch (RedisException e) {
        // Handle the Exception as required in your application.
        e.printStackTrace();

        // For Exceptions containing Invalid Username Password / Permissions not granted error messages, look at troubleshooting section at the end of document.

        if (!connection.isOpen()) {
            // Recreate the client with a fresh token non-expired token as password for authentication.
            client = createLettuceRedisClient("<HOST_NAME>", 6380, tokenRefreshCache.getAccessToken());
            connection = client.connect(StringCodec.UTF8);
            sync = connection.sync();

            // Configure the lettuce instance for proactive authentication before token expires.
            tokenRefreshCache.setLettuceInstanceToAuthenticate(sync);
        }
    } catch (Exception e) {
        // Handle the Exception as required in your application.
        e.printStackTrace();
    }
    i++;
}

// Helper code
private static RedisClient createLettuceRedisClient(String hostName, int port, AccessToken accessToken) {

    // Build Redis URI with host and authentication details.
    RedisURI redisURI = RedisURI.Builder.redis(hostName)
        .withPort(port)
        .withSsl(true) // Targeting SSL based port
        .withAuthentication(extractUsernameFromToken(accessToken.getToken()), accessToken.getToken())
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
    private final Duration maxRefreshOffset = Duration.ofMinutes(5);
    private final Duration baseRefreshOffset = Duration.ofMinutes(2);
    private RedisCommands<String, String> lettuceInstanceToAuthenticate;
    private String username;

    /**
     * Creates an instance of TokenRefreshCache
     * @param tokenCredential the token credential to be used for authentication.
     * @param tokenRequestContext the token request context to be used for authentication.
     */
    public TokenRefreshCache(TokenCredential tokenCredential, TokenRequestContext tokenRequestContext) {
        this.tokenCredential = tokenCredential;
        this.tokenRequestContext = tokenRequestContext;
        this.timer = new Timer(true);
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
            username = extractUsernameFromToken(accessToken.getToken());
            System.out.println("Refreshed Token with Expiry: " + accessToken.getExpiresAt().toEpochSecond());

            if (lettuceInstanceToAuthenticate != null && !CoreUtils.isNullOrEmpty(username)) {
                lettuceInstanceToAuthenticate.auth(username, accessToken.getToken());
                System.out.println("Refreshed Lettuce Connection with fresh access token, token expires at : "
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
     * Sets the Lettuce instance to proactively authenticate before token expiry.
     * @param lettuceInstanceToAuthenticate the instance to authenticate
     * @return the updated instance
     */
    public TokenRefreshCache setLettuceInstanceToAuthenticate(RedisCommands<String, String> lettuceInstanceToAuthenticate) {
        this.lettuceInstanceToAuthenticate = lettuceInstanceToAuthenticate;
        return this;
    }
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
