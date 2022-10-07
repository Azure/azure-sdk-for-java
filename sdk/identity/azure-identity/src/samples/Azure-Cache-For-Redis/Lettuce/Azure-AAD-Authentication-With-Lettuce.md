## Azure Cache for Redis: Azure AD with Lettuce client library

### Table of contents

- [Dependency Requirements](#dependency-requirements)
- [Authenticate with Azure AD - Hello World](#authenticate-with-azure-ad-hello-world)
- [Authenticate with Azure AD - Handle Reauthentication](#authenticate-with-azure-ad-handle-reauthentication)


#### Dependency Requirements
```xml
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

#### Samples Guidance
Familiarity with the [Lettuce](https://github.com/lettuce-io/lettuce-core) and [Azure Identity for Java](https://docs.microsoft.com/azure/developer/java/sdk/identity) client libraries is assumed.

[Authenticate with Azure AD - Hello World](#authenticate-with-azure-ad-hello-world)
This sample is recommended for users getting started to use Azure AD authentication with Azure Cache for Redis.

[Authenticate with Azure AD - Handle Reauthentication](#authenticate-with-azure-ad-handle-reauthentication)
This sample is recommended to users looking to build long-running applications that would like to handle reauthenticating with Azure AD upon token expiry.

**Note:** The below sample uses the Azure Identity library's `ClientCertificateCredential`. The credential can be replaced with any of the other `TokenCredential` implementations offered by the Azure Identity library.

##### Challenges
The Lettuce client handshake process is described below. The RESP 3 Protocol uses `HELLO` command, which currently isn't supported on the service side.
![image](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/images/lettuce-aad-handshake.png)

##### Raw Redis CLI Connection Behavior
```java

// Lettuce RESP 2 Protocol.

walmart-demo.redis.cache.windows.net:6379> AUTH walmartdummyuser eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6ImpTMVhvMU9XRGpfNTJ2YndHTmd2UU8yVnpNYyIsImtpZCI6ImpTMVhvMU9XRGpfNTJ2YndHTmd2UU8yVnpNYyJ9.eyJhdWQiOiJodHRwczovLyouY2FjaGVpbmZyYS53aW5kb3dzLm5ldDoxMDIyNS9hcHBpZCIsImlzcyI6Imh0dHBzOi8vc3RzLndpbmRvd3MubmV0LzcyZjk4OGJmLTg2ZjEtNDFhZi05MWFiLTJkN2NkMDExZGI0Ny8iLCJpYXQiOjE2NTA4OTkxMzUsIm5iZiI6MTY1MDg5OTEzNSwiZXhwIjoxNjUwOTg1ODM1LCJhaW8iOiJFMlpnWU1pWmVIWVpZOC8xRHhjdjl2ZlhPKzdjQWdBPSIsImFwcGlkIjoiZmM1.....

OK


// Lettuce RESP 3 Protocol.
walmart-demo.redis.cache.windows.net:6379> HELLO 3 AUTH walmartdummyuser eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6ImpTMVhvMU9XRGpfNTJ2YndHTmd2UU8yVnpNYyIsImtpZCI6ImpTMVhvMU9XRGpfNTJ2YndHTmd2UU8yVnpNYyJ9.eyJhdWQiOiJodHRwczovLyouY2FjaGVpbmZyYS53aW5kb3dzLm5ldDoxMDIyNS9hcHBpZCIsImlzcyI6Imh0dHBzOi8vc3RzLndpbmRvd3MubmV0LzcyZjk4OGJmLTg2ZjEtNDFhZi05MWFiLTJkN2NkMDExZGI0Ny8iLCJpYXQiOjE2NTA4OTkxMzUsIm5iZiI6MTY1MDg5OTEzNSwiZXhwIjoxNjUwOTg1ODM1LCJhaW8iOiJFMlpnWU1pWmVIWVpZOC8xRHhjdjl2ZlhPKzdjQWdBPSIsImFwcGlkIjoi.....

(error) WRONGPASS invalid username-password pair
```

##### Authenticate with Azure AD: Hello World
This sample is intended to assist in authenticating with Azure AD via the Lettuce client library. It focuses on displaying the logic required to fetch an Azure AD access token and to use it as password when setting up the Lettuce client instance.

##### Migration Guidance
When migrating your existing your application code, you need to replace the password input with the Azure AD token.
Integrate the logic in your application code to fetch an Azure AD access token via the Azure Identity library as shown below and replace it with the password configuring/retrieving logic in your application code.

##### Version 6.1.8.RELEASE or less
```xml
  <dependency>
      <groupId>io.lettuce</groupId>
      <artifactId>lettuce-core</artifactId>
      <version>6.1.8.RELEASE</version>
  </dependency>

<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
    <version>1.5.0</version>
</dependency>
```
```java
//Construct a Token Credential from Identity library, e.g. ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
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
```

##### Version 6.2.0-BUILD-SNAPSHOT
```xml
<dependency>
  <groupId>io.lettuce</groupId>
  <artifactId>lettuce-core</artifactId>
  <version>6.2.0-BUILD-SNAPSHOT</version>
</dependency>

<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
    <version>1.5.0</version>
</dependency>
```

```java

//Construct a Token Credential from Identity library, e.g. ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
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


// Implementation of Redis Credentials used above.
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


##### Authenticate with Azure AD: Handle Reauthentication
This sample is intended to assist in authenticating with Azure AD via Lettuce client library. It focuses on displaying the logic required to fetch an Azure AD access token and to use it as password when setting up the Lettuce Redis client instance. It also shows how to recreate and authenticate the Lettuce client instance when its connection is broken in error/exception scenarios.

##### Migration Guidance
When migrating your existing your application code, you need to replace the password input with the Azure AD token.
Integrate the logic in your application code to fetch an Azure AD access token via the Azure Identity library as shown below and replace it with the password configuring/retrieving logic in your application code.

##### Version 6.1.8.RELEASE or less
```xml
  <dependency>
      <groupId>io.lettuce</groupId>
      <artifactId>lettuce-core</artifactId>
      <version>6.1.8.RELEASE</version>
  </dependency>
```

```java
//Construct a Token Credential from Identity library, e.g. ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
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
    
    // Helper code
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
//Construct a Token Credential from Identity library, e.g. ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
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
    
    // Helper Code
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
