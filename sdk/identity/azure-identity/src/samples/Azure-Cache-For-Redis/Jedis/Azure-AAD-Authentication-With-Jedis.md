## Azure Cache for Redis: Azure AD with Jedis client library

### Table of contents

- [Dependency Requirements](#dependency-requirements-jedis)
- [Authenticate with Azure AD - Hello World](#authenticate-with-azure-ad-hello-world)
- [Authenticate with Azure AD - Handle Reauthentication](#authenticate-with-azure-ad-handle-reauthentication)
- [Authenticate with Azure AD - Azure Jedis Wrapper](#authenticate-with-azure-ad-azure-jedis-wrapper)

#### Dependency Requirements Jedis
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
    <version>1.5.0</version>
</dependency>

<dependency>
    <groupId>redis.clients</groupId>
    <artifactId>jedis</artifactId>
    <version>4.0.1</version>
</dependency>
```

#### Samples Guidance

Familiarity with the [Jedis](https://www.javadoc.io/doc/redis.clients/jedis/latest/index.html) and [Azure Identity for Java](https://docs.microsoft.com/azure/developer/java/sdk/identity) client libraries is assumed.

[Authenticate with Azure AD - Hello World](#authenticate-with-azure-ad-hello-world)
This sample is recommended for users getting started to use Azure AD authentication with Azure Cache for Redis.

[Authenticate with Azure AD - Handle Re-Authentication](#authenticate-with-azure-ad-handle-re-authentication)
This sample is recommended to users looking to build long-running applications that would like to handle reauthenticating with Azure AD upon token expiry.

[Authenticate with Azure AD - Azure Jedis Wrapper](#authenticate-with-azure-ad-azure-jedis-wrapper)
This sample is recommended to users looking to build long-running applications that would like to integrate our recommended wrapper implementation in their application which handles reconnection and re-authentication on user's behalf.

**Note:** The below sample uses the Azure Identity library's `ClientCertificateCredential`. The credential can be replaced with any of the other `TokenCredential` implementations offered by the Azure Identity library.

#### Authenticate with Azure AD: Hello World
This sample is intended to assist in authenticating with Azure AD via the Jedis client library. It focuses on displaying the logic required to fetch an Azure AD access token and to use it as password when setting up the Jedis instance.

##### Migration Guidance
When migrating your existing your application code, you need to replace the password input with the Azure AD token.
Integrate the logic in your application code to fetch an Azure AD access token via the Azure Identity library as shown below and replace it with the password configuring/retrieving logic in your application code.

```java
//Construct a Token Credential from Identity library, e.g. ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
ClientCertificateCredential clientCertificateCredential = new ClientCertificateCredentialBuilder()
        .clientId("YOUR-CLIENT-ID")
        .pfxCertificate("YOUR-CERTIFICATE-PATH", "CERTIFICATE-PASSWORD")
        .tenantId("YOUR-TENANT-ID")
        .build();

// Fetch an Azure AD token to be used for authentication. This token will be used as the password.
String token = clientCertificateCredential
        .getToken(new TokenRequestContext()
                .addScopes("https://*.cacheinfra.windows.net:10225/appid/.default")).block().getToken();

// SSL connection is required for non 6379 ports. It is recommeded to use SSL connections.
boolean useSsl = true; 
String cacheHostname = "YOUR_HOST_NAME.redis.cache.windows.net";

// Create Jedis client and connect to the Azure Cache for Redis over the TLS/SSL port using the access token as password.
Jedis jedis = new Jedis(cacheHostname, 6380, DefaultJedisClientConfig.builder()
        .password(token)
        .user("<username>")
        .ssl(useSsl)
        .build());

// Set a value against your key in the Redis cache.
jedis.set("Az:key", "testValue");
System.out.println(jedis.get("Az:key"));

// Close the Jedis Client
jedis.close();
```

#### Authenticate with Azure AD: Handle Reauthentication
This sample is intended to assist in authenticating with Azure AD via Jedis client library. It focuses on displaying the logic required to fetch an Azure AD access token and to use it as password when setting up the Jedis instance. It also shows how to recreate and authenticate the Jedis instance when its connection is broken in error/exception scenarios.

##### Migration Guidance
When migrating your existing your application code, you need to replace the password input with the Azure AD token.
Integrate the logic in your application code to fetch an Azure AD access token via the Azure Identity library as shown below and replace it with the password configuring/retrieving logic in your application code.

```java

//Construct a Token Credential from Identity library, e.g. ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
ClientCertificateCredential clientCertificateCredential = new ClientCertificateCredentialBuilder()
    .clientId("YOUR-CLIENT-ID")
    .pfxCertificate("YOUR-CERTIFICATE-PATH", "CERTIFICATE-PASSWORD")
    .tenantId("YOUR-TENANT-ID")
    .build();

// Fetch an Azure AD token to be used for authentication. This token will be used as the password.
TokenRequestContext trc = new TokenRequestContext().addScopes("https://*.cacheinfra.windows.net:10225/appid/.default");
AccessToken accessToken = getAccessToken(clientCertificateCredential, trc);

// SSL connection is required for non 6379 ports.
boolean useSsl = true;
String cacheHostname = "YOUR_HOST_NAME.redis.cache.windows.net";

// Create Jedis client and connect to the Azure Cache for Redis over the TLS/SSL port using the access token as password.
Jedis jedis = createJedisClient(cacheHostname, 6380, "USERNAME", accessToken, useSsl);

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

        // Check if the client is broken, if it is then close and recreate it to create a new healthy connection.
        if (jedis.isBroken() || accessToken.isExpired()) {
            jedis.close();
            jedis = createJedisClient(cacheHostname, 6380, "USERNAME", getAccessToken(clientCertificateCredential, trc), useSsl);
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
```

#### Authenticate with Azure AD: Azure Jedis Wrapper
This sample is intended to assist in the migration from Jedis to `AzureJedisClientBuilder`. It focuses on side-by-side comparisons for similar operations between the two libraries.

##### Migration benefits

A natural question to ask when considering whether or not to adopt a new version or library is what the benefits of doing so would be. Jedis by itself doesn't support Azure AD authentication with token generation, failure retries, broken connection handling, and cache reauthentication. Using `AzureJedisClient` will improve developer productivity and code maintainability.

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
| Azure AD Authentication Support | No | Yes|
| Retry Failure | No| Yes |
| Reauthenticate | No |Yes |
| Handle Broken Connection | No |Yes |

See the following example of setting up the Azure Jedis client:

The wrapper code located here requires to be added/integrated in your application code for the below sample to work.


```java
//Construct a Token Credential from Identity library, e.g. ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
ClientCertificateCredential clientCertificateCredential = new ClientCertificateCredentialBuilder()
        .clientId("<clientId>")
        .pfxCertificate("<Cert-File-Path>", "<Cert-Password-if-Applicable>")
        .tenantId("<tenantId>")
        .build();

//Create Jedis Client using the builder as follows.
Jedis jedisClient = new AzureJedisClientBuilder()
        .cacheHostName("<cache host name>")
        .port(6380)
        .useSSL(true)
        .username("<username>")
        .credential(clientCertificateCredential)
        .build();

// Set a value against your key in the Redis cache.
jedisClient.set("Az:key", "sample");

// Close the Jedis Client
jedisClient.close();
```
