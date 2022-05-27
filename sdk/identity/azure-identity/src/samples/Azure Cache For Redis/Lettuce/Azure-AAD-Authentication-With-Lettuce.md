## Azure Cache For Redis Azure AD With Lettuce Client Library

### Table of contents

- [Lettuce Library](#lettuce-library)
    - [Dependency Requirements](#dependency-requirements)
    - [Authenticate with Azure AD - Hello World](#authenticate-with-azure-ad-hello-world)
    - [Authenticate with Azure AD - Handle Re-Authentication](#authenticate-with-azure-ad-handle-re-authentication)
    - [Authenticate with Azure AD - Azure Lettuce Client Wrapper](#authenticate-with-azure-ad-azure-lettuce-wrapper)


### Lettuce Library

#### Dependency Requirements
````xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
    <version>1.5.0</version>
</dependency>

<dependency>
    <groupId>io.lettuce</groupId>
    <artifactId>lettuce-core</artifactId>
    <version>6.1.8.RELEASE</version>
</dependency>
```

##### Challenges
![image](https://user-images.githubusercontent.com/5430778/165149837-370c340a-7bf4-4067-a8a7-3a1a7e049773.png)

##### Raw Redis CLI Connection Behavior
```java

// Lettuce RESP 2 Protocol.

walmart-demo.redis.cache.windows.net:6379> AUTH walmartdummyuser eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6ImpTMVhvMU9XRGpfNTJ2YndHTmd2UU8yVnpNYyIsImtpZCI6ImpTMVhvMU9XRGpfNTJ2YndHTmd2UU8yVnpNYyJ9.eyJhdWQiOiJodHRwczovLyouY2FjaGVpbmZyYS53aW5kb3dzLm5ldDoxMDIyNS9hcHBpZCIsImlzcyI6Imh0dHBzOi8vc3RzLndpbmRvd3MubmV0LzcyZjk4OGJmLTg2ZjEtNDFhZi05MWFiLTJkN2NkMDExZGI0Ny8iLCJpYXQiOjE2NTA4OTkxMzUsIm5iZiI6MTY1MDg5OTEzNSwiZXhwIjoxNjUwOTg1ODM1LCJhaW8iOiJFMlpnWU1pWmVIWVpZOC8xRHhjdjl2ZlhPKzdjQWdBPSIsImFwcGlkIjoiZmM1.....

OK


// Lettuce RESP 3 Protocol.
walmart-demo.redis.cache.windows.net:6379> HELLO 3 AUTH walmartdummyuser eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6ImpTMVhvMU9XRGpfNTJ2YndHTmd2UU8yVnpNYyIsImtpZCI6ImpTMVhvMU9XRGpfNTJ2YndHTmd2UU8yVnpNYyJ9.eyJhdWQiOiJodHRwczovLyouY2FjaGVpbmZyYS53aW5kb3dzLm5ldDoxMDIyNS9hcHBpZCIsImlzcyI6Imh0dHBzOi8vc3RzLndpbmRvd3MubmV0LzcyZjk4OGJmLTg2ZjEtNDFhZi05MWFiLTJkN2NkMDExZGI0Ny8iLCJpYXQiOjE2NTA4OTkxMzUsIm5iZiI6MTY1MDg5OTEzNSwiZXhwIjoxNjUwOTg1ODM1LCJhaW8iOiJFMlpnWU1pWmVIWVpZOC8xRHhjdjl2ZlhPKzdjQWdBPSIsImFwcGlkIjoi.....

(error) WRONGPASS invalid username-password pair



```

##### Authenticate with Azure AD Hello World
This sample is intended to assist in authenticating with Azure AD via Lettuce client library. It focuses on displaying the logic required to fetch an Azure AD Access token and to use it as password when setting up the Lettuce Redis Client instance.

Familiarity with the Jedis and Azure Identity client libraries is assumed. If you're new to the Azure Identity library for Java, see the docs for [Azure Identity](https://docs.microsoft.com/azure/developer/java/sdk/identity) and [Lettuce](https://www.javadoc.io/doc/redis.clients/jedis/latest/index.html) rather than this guide.


##### Migration Guidance
When migrating your existing your application code, you need to replace the password input with Azure Active Directory Token.
Integrate the logic in your application code to fetch an Azure AD Access Token via Identity SDK as shown below and replace the password configuring/retrieving logic in your application code.

**Note:** The below sample uses `ClientCertificateCredential` from our [Azure Identity](https://docs.microsoft.com/azure/developer/java/sdk/identity) SDK, the credential can be replaced with any of the other `TokenCredential` implementations offered by our [Azure Identity](https://docs.microsoft.com/azure/developer/java/sdk/identity) SDK.

##### Version 6.1.8.RELEASE or less
```xml
  <dependency>
      <groupId>io.lettuce</groupId>
      <artifactId>lettuce-core</artifactId>
      <version>6.1.8.RELEASE</version>
  </dependency>
```
```java

    public static void main(String[] args) {

        //Construct a Token Credential from Identity SDK, e.g. ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
        ClientCertificateCredential clientCertificateCredential = new ClientCertificateCredentialBuilder()
                .clientId("YOUR-CLIENT-ID")
                .pfxCertificate("YOUR-CERTIFICATE-PATH", "CERTIFICATE-PASSWORD")
                .tenantId("YOUR-TENANT-ID")
                .build();

        // Fetch an Azure AD token to be used for authentication. The Azure AD token will be used as password.
        String token = clientCertificateCredential
                .getToken(new TokenRequestContext()
                        .addScopes("https://*.cacheinfra.windows.net:10225/appid/.default")).block().getToken();


        // Build Redis URI with host and authentication details.
        RedisURI redisURI = RedisURI.Builder.redis("YOUR_HOST_NAME.cache.windows.net")
                .withPort(6379)
                .withSsl(false) // Targeting Non-SSL 6379 port.
                .withAuthentication("USERNAME", token)
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
    }

```

##### Version 6.2.0-BUILD-SNAPSHOT
```xml
<dependency>
  <groupId>io.lettuce</groupId>
  <artifactId>lettuce-core</artifactId>
  <version>6.2.0-BUILD-SNAPSHOT</version>
</dependency>
```

```java

    public static void main(String[] args) {
        //Construct a Token Credential from Identity SDK, e.g. ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
        ClientCertificateCredential clientCertificateCredential = new ClientCertificateCredentialBuilder()
                .clientId("YOUR-CLIENT-ID")
                .pfxCertificate("YOUR-CERTIFICATE-PATH", "CERTIFICATE-PASSWORD")
                .tenantId("YOUR-TENANT-ID")
                .build();

        // Build Redis URI with host and authentication details.
        RedisURI redisURI = RedisURI.Builder.redis("YOUR_HOST_NAME.cache.windows.net")
                .withPort(6379)
                .withSsl(false) // Targeting Non-SSL 6379 port.
                .withAuthentication(RedisCredentialsProvider.from(() -> new AzureRedisCredentials("USERNAME", clientCertificateCredential)))
                .withClientName("LettuceClient")
                .build();

        // Create Lettuce Redis Client
        RedisClient client = RedisClient.create(redisURI);

        // Confifgure the client options.
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


##### Authenticate with Azure AD Handle Re Authentication.
This sample is intended to assist in authenticating with Azure AD via Lettuce client library. It focuses on displaying the logic required to fetch an Azure AD Access token and to use it as password when setting up the Lettuce Redis Clientt instance. It Further shows how to recreate and authenticate the Lettuce Redis Client instance when its connection is broken in Error/Exception scenarios.

Familiarity with the Jedis and Azure Identity client libraries is assumed. If you're new to the Azure Identity library for Java, see the docs for [Azure Identity](https://docs.microsoft.com/azure/developer/java/sdk/identity) and [Jedis](https://www.javadoc.io/doc/redis.clients/jedis/latest/index.html) rather than this guide.


##### Migration Guidance
When migrating your existing your application code, you need to replace the password input with Azure Active Directory Token.
Integrate the logic in your application code to fetch an Azure AD Access Token via Identity SDK as shown below and replace the password configuring/retrieving logic in your application code.

**Note:** The below sample uses `ClientCertificateCredential` from our [Azure Identity](https://docs.microsoft.com/azure/developer/java/sdk/identity) SDK, the credential can be replaced with any of the other `TokenCredential` implementations offered by our [Azure Identity](https://docs.microsoft.com/azure/developer/java/sdk/identity) SDK.

##### Version 6.1.8.RELEASE or less
```xml
  <dependency>
      <groupId>io.lettuce</groupId>
      <artifactId>lettuce-core</artifactId>
      <version>6.1.8.RELEASE</version>
  </dependency>
```

```java

        public static void main(String[] args) {

        //Construct a Token Credential from Identity SDK, e.g. ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
        ClientCertificateCredential clientCertificateCredential = getClientCertificateCredential();

        // Fetch an AAD token to be used for authentication. This token will be used as the password.
        TokenRequestContext trc = new TokenRequestContext().addScopes("https://*.cacheinfra.windows.net:10225/appid/.default");
        AccessToken accessToken = getAccessToken(clientCertificateCredential, trc);

        RedisClient client = createLettuceRedisClient("YOUR_HOST_NAME.redis.cache.windows.net", 6379, "USERNAME", accessToken);
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

                if (accessToken.isExpired()) {
                    // Recreate the client with a fresh token non-expired token as password for authentication.
                    client = createLettuceRedisClient("YOUR_HOST_NAME.redis.cache.windows.net", 6379, "USERNAME", getAccessToken(clientCertificateCredential, trc));
                    connection = client.connect(StringCodec.UTF8);
                    sync = connection.sync();
                } else if (!connection.isOpen()) {
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
    }
    
    private static RedisClient createLettuceRedisClient(String hostName, int port, String username, AccessToken accessToken) {

        // Build Redis URI with host and authentication details.
        RedisURI redisURI = RedisURI.Builder.redis(hostName)
                .withPort(port)
                .withSsl(false) // Targeting Non-SSL 6379 port.
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


##### Version 6.2.0-BUILD-SNAPSHOT
```xml
  <dependency>
      <groupId>io.lettuce</groupId>
      <artifactId>lettuce-core</artifactId>
      <version>6.2.0-BUILD-SNAPSHOT</version>
  </dependency>
```
```java
    public static void main(String[] args) {

        //Construct a Token Credential from Identity SDK, e.g. ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
        ClientCertificateCredential clientCertificateCredential = getClientCertificateCredential();

        RedisClient client = createLettuceRedisClient("YOUR_HOST_NAME.redis.cache.windows.net", 6379, "USERNAME", clientCertificateCredential);
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
    }
    
    private static RedisClient createLettuceRedisClient(String hostName, int port, String username, TokenCredential tokenCredential) {

        // Build Redis URI with host and authentication details.
        RedisURI redisURI = RedisURI.Builder.redis(hostName)
                .withPort(port)
                .withSsl(false) // Targeting Non-SSL 6379 port.
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
    
    private static ClientCertificateCredential getClientCertificateCredential() {
        return new ClientCertificateCredentialBuilder()
                .clientId("YOUR-CLIENT-ID")
                .pfxCertificate("YOUR-CERTIFICATE-PATH", "CERTIFICATE-PASSWORD")
                .tenantId("YOUR-TENANT-ID")
                .build();
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

#### Authenticate with Azure AD Azure Lettuce Client Wrapper
This sample is intended to assist in the migration from RedisClient to `AzureLettuceRedisClient`. It focuses on side-by-side comparisons for similar operations between the two libraries.

Familiarity with the Jedis and Azure Identity client libraries is assumed. If you're new to the Azure Identity library for Java, see the docs for [Azure Identity](https://docs.microsoft.com/azure/developer/java/sdk/identity) and [Lettuce](https://www.javadoc.io/doc/redis.clients/jedis/latest/index.html) rather than this guide.

##### Migration benefits

A natural question to ask when considering whether or not to adopt a new version or library is what the benefits of doing so would be. Lettuce's Redis Client by itself doesn't support Azure AD authentication with token generation and cache reauthentication. Using `AzureLettuceRedisClient` will improve developer productivity and code maintainability.

##### Client instantiation

In Lettuce, you create a `RedisClient` object via a static method `create` which accepts the RedisURI configuration. For example:

```java

// Build Redis URI with host and authentication details.
RedisURI redisURI = RedisURI.Builder.redis("YOUR_HOST_NAME.cache.windows.net")
        .withPort(6379) 
        .withSsl(false) // Targeting Non-SSL 6379 port.
        .withAuthentication("USERNAME", "PASSWORD)
        .withClientName("LettuceClient")
        .build();

// Create Lettuce Redis Client
RedisClient client = RedisClient.create(redisURI);

// Confifgure the client options.
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

With `AzureLetttuceRedisClient`, client instances are created via builders. The builder accepts the:

- RedisURI
- Token Credential object that's used to generate a token
- Client Options
- Client Resources

See the following example of setting up the Azure Jedis client.

**Note:** The below sample uses `ClientCertificateCredential` from our [Azure Identity](https://docs.microsoft.com/azure/developer/java/sdk/identity) SDK, the credential can be replaced with any of the other `TokenCredential` implementations offered by our [Azure Identity](https://docs.microsoft.com/azure/developer/java/sdk/identity) SDK.


![image](https://user-images.githubusercontent.com/5430778/166561705-027b3fa7-384b-4939-a95f-ca75b6134d4f.png)


##### Version 6.1.8.RELEASE or less
```xml
  <dependency>
      <groupId>io.lettuce</groupId>
      <artifactId>lettuce-core</artifactId>
      <version>6.1.8.RELEASE</version>
  </dependency>
```

```java
    public static void main(String[] args) {

        //Construct a Token Credential from Identity SDK, e.g. ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
        ClientCertificateCredential clientCertificateCredential = new ClientCertificateCredentialBuilder()
                .clientId("YOUR-CLIENT-ID")
                .pfxCertificate("YOUR-CERTIFICATE-PATH", "CERTIFICATE-PASSWORD")
                .tenantId("YOUR-TENANT-ID")
                .build();

        // Fetch an Azure AD token to be used for authentication. The Azure AD token will be used as password.
        String token = clientCertificateCredential
                .getToken(new TokenRequestContext()
                        .addScopes("https://*.cacheinfra.windows.net:10225/appid/.default")).block().getToken();

        RedisClient client = new AzureLettuceRedisClientBuilder()
                .redisURI(RedisURI.Builder.redis("walmart-demo.redis.cache.windows.net")
                        .withPort(6379)
                        .withSsl(false)
                        .withClientName("LettuceClient")
                        .build())
                .tokenCredential(clientCertificateCredential)
                .username("USERNAME")
                .clientOptions(ClientOptions.builder()
                        .socketOptions(SocketOptions.builder()
                                .keepAlive(true)
                                .build())
                        .protocolVersion(ProtocolVersion.RESP2)
                        .build())
                .build();

        StatefulRedisConnection<String, String> connection = client.connect(StringCodec.UTF8);

        RedisStringCommands sync = connection.sync();
        try {
            sync.set("Az:testKey", "testVal");
            System.out.println(sync.get("Az:testKey").toString());
        } catch (RedisException e) {
            // Handle Exception as required.

            // Check if connection open, else open a new connection instance for further use.
            if (!connection.isOpen()) {
                connection = client.connect(StringCodec.UTF8);
                sync = connection.sync();
            }
        }
    }
```

##### Lettuce Version 6.2.0-BUILD-SNAPSHOT
```xml
  <dependency>
      <groupId>io.lettuce</groupId>
      <artifactId>lettuce-core</artifactId>
      <version>6.2.0-BUILD-SNAPSHOT</version>
  </dependency>
```

```java
public static void main(String[] args) throws IOException {

        //Construct a Token Credential from Identity SDK, e.g. ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
        ClientCertificateCredential clientCertificateCredential = new ClientCertificateCredentialBuilder()
                .clientId("YOUR-CLIENT-ID")
                .pfxCertificate("YOUR-CERTIFICATE-PATH", "CERTIFICATE-PASSWORD")
                .tenantId("YOUR-TENANT-ID")
                .build();
                
        RedisClient client = new AzureLettuceRedisClientBuilder()
                .redisURI(RedisURI.Builder.redis("walmart-demo.redis.cache.windows.net")
                        .withPort(6379)
                        .withSsl(false)
                        .withClientName("LettuceClient")
                        .build())
                .tokenCredential(clientCertificateCredential)
                .username("USERNAME")
                .clientOptions(ClientOptions.builder()
                        .socketOptions(SocketOptions.builder()
                                .keepAlive(true)
                                .build())
                        .protocolVersion(ProtocolVersion.RESP2)
                        .build())
                .build();

        StatefulRedisConnection<String, String> connection = client.connect(StringCodec.UTF8);

        RedisStringCommands sync = connection.sync();
        try {
            sync.set("Az:testKey", "testVal");
            System.out.println(sync.get("Az:testKey").toString());
        } catch (RedisException e) {
            if (!connection.isOpen()) {
                connection = client.connect(StringCodec.UTF8);
                sync = connection.sync();
            }
        }
}

// Part of Wrapper Source code, Added here for Reference.
public static class AzureRedisCredentials implements RedisCredentials {
    private TokenRequestContext tokenRequestContext = new TokenRequestContext()
            .addScopes("https://*.cacheinfra.windows.net:10225/appid/.default");
    private AccessTokenCache accessTokenCache;
    private final String username;

    public AzureRedisCredentials(String username, TokenCredential tokenCredential) {
        Objects.requireNonNull(username, "Username is required");
        Objects.requireNonNull(tokenCredential, "Token Credential is required");
        this.username = username;
        this.accessTokenCache = new AccessTokenCache(tokenCredential);
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
        return accessTokenCache
                .getToken(tokenRequestContext, false).block()
                .getAccessToken()
                .getToken()
                .toCharArray();
    }

    @Override
    public boolean hasPassword() {
        return accessTokenCache != null;
    }
}
```
