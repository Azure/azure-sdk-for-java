## Azure Cache For Redis Azure AD With Jedis Client Library

### Table of contents

- [Jedis Library](#jedis-library)
    - [Dependency Requirements](#dependency-requirements-jedis)
    - [Authenticate with Azure AD - Hello World](#authenticate-with-azure-ad-jedis-hello-world)
    - [Authenticate with Azure AD - Handle Re-Authentication](#authenticate-with-azure-ad-handle-re-authentication)
    - [Authenticate with Azure AD - Azure Jedis Wrapper](#authenticate-with-azure-ad-azure-jedis-wrapper)

### Jedis Library

#### Dependency Requirements Jedis
```
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


#### Authenticate with Azure AD Jedis Hello World
This sample is intended to assist in authenticating with Azure AD via Jedis client library. It focuses on displaying the logic required to fetch an Azure AD Access token and to use it as password when setting up the Jedis instance.

Familiarity with the Jedis and Azure Identity client libraries is assumed. If you're new to the Azure Identity library for Java, see the docs for [Azure Identity](https://docs.microsoft.com/azure/developer/java/sdk/identity) and [Jedis](https://www.javadoc.io/doc/redis.clients/jedis/latest/index.html) rather than this guide.

##### Migration Guidance
When migrating your existing your application code, you need to replace the password input with Azure Active Directory Token.
Integrate the logic in your application code to fetch an Azure AD Access Token via Identity SDK as shown below and replace it with the password configuring/retrieving logic in your application code.

**Note:** The below sample uses `ClientCertificateCredential` from our [Azure Identity](https://docs.microsoft.com/azure/developer/java/sdk/identity) SDK, the credential can be replaced with any of the other `TokenCredential` implementations offered by our [Azure Identity](https://docs.microsoft.com/azure/developer/java/sdk/identity) SDK.

```java

public static void main(String[] args) throws IOException {

        //Construct a Token Credential from Identity SDK, e.g. ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
        ClientCertificateCredential clientCertificateCredential = new ClientCertificateCredentialBuilder()
                .clientId("YOUR-CLIENT-ID")
                .pfxCertificate("YOUR-CERTIFICATE-PATH", "CERTIFICATE-PASSWORD")
                .tenantId("YOUR-TENANT-ID")
                .build();

        // Fetch an Azure AD token to be used for authentication. This token will be used as the password.
        String token = clientCertificateCredential
                .getToken(new TokenRequestContext()
                        .addScopes("https://*.cacheinfra.windows.net:10225/appid/.default")).block().getToken();


        // SSL connection is required for non 6379 ports.
        boolean useSsl = true; 
        String cacheHostname = "YOUR_HOST_NAME.redis.cache.windows.net";

        // Create Jedis client and connect to the Azure Cache for Redis over the TLS/SSL port using the access token as password.
        Jedis jedis = new Jedis(cacheHostname, 6380, DefaultJedisClientConfig.builder()
                .password(token)
                .ssl(useSsl)
                .build());

        // Set a value against your key in the Azure Redis Cache.
        jedis.set("Az:key", "testValue");
        System.out.println(jedis.get("Az:key"));

        // Close the Jedis Client
        jedis.close();
}
```

#### Authenticate with Azure AD Handle Re Authentication
This sample is intended to assist in authenticating with Azure AD via Jedis client library. It focuses on displaying the logic required to fetch an Azure AD Access token and to use it as password when setting up the Jedis instance. It Further shows how to recreate and authenticate the Jedis instance when its connection is broken in Error/Exception scenarios.

Familiarity with the Jedis and Azure Identity client libraries is assumed. If you're new to the Azure Identity library for Java, see the docs for [Azure Identity](https://docs.microsoft.com/azure/developer/java/sdk/identity) and [Jedis](https://www.javadoc.io/doc/redis.clients/jedis/latest/index.html) rather than this guide.


##### Migration Guidance
When migrating your existing your application code, you need to replace the password input with Azure Active Directory Token.
Integrate the logic in your application code to fetch an Azure AD Access Token via Identity SDK as shown below and replace the password configuring/retrieving logic in your application code.

**Note:** The below sample uses `ClientCertificateCredential` from our [Azure Identity](https://docs.microsoft.com/azure/developer/java/sdk/identity) SDK, the credential can be replaced with any of the other `TokenCredential` implementations offered by our [Azure Identity](https://docs.microsoft.com/azure/developer/java/sdk/identity) SDK.

```java
    public static void main(String[] args) {

        //Construct a Token Credential from Identity SDK, e.g. ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
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

        try {
            // Set a value against your key in the Azure Redis Cache.
            jedis.set("Az:key", "testValue");
            System.out.println(jedis.get("Az:key"));
        } catch (JedisException e) {
            // Handle The Exception as required in your application.
            e.printStackTrace();

            // Check if the client is broken, if it is then close and recreate it to create a new healthy connection.
            if (jedis.isBroken() || accessToken.isExpired()) {
                jedis.close();
                jedis = createJedisClient(cacheHostname, 6380,"USERNAME", getAccessToken(clientCertificateCredential, trc), useSsl);
            }
        }

        // Close the Jedis Client
        jedis.close();
    }

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

#### Authenticate with Azure AD Azure Jedis Wrapper
This sample is intended to assist in the migration from Jedis to `AzureJedisClientBuilder`. It focuses on side-by-side comparisons for similar operations between the two libraries.

Familiarity with the Jedis and Azure Identity client libraries is assumed. If you're new to the Azure Identity library for Java, see the docs for [Azure Identity](https://docs.microsoft.com/azure/developer/java/sdk/identity) and [Jedis](https://www.javadoc.io/doc/redis.clients/jedis/latest/index.html) rather than this guide.

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

See the following example of setting up the Azure Jedis client.

| Feature | Jedis | Microsoft Jedis |
|--|--|--|
| Connect to Redis Cache | Yes |Yes |
| Azure AD Authentication Support | No | Yes|
| Retry Failure | No| Yes |
| Re Authenticate | No |Yes |
| Handle Broken Connection | No |Yes |

See the following example of setting up the Azure Jedis client.

**Note:** The below sample uses `ClientCertificateCredential` from our [Azure Identity](https://docs.microsoft.com/azure/developer/java/sdk/identity) SDK, the credential can be replaced with any of the other `TokenCredential` implementations offered by our [Azure Identity](https://docs.microsoft.com/azure/developer/java/sdk/identity) SDK.

```java
public static void main(String[] args) throws IOException {

        ClientCertificateCredential clientCertificateCredential = new ClientCertificateCredentialBuilder()
            .clientId("<clientId>")
            .pfxCertificate("<Cert-File-Path>", "<Cert-Password-if-Applicable>")
            .tenantId("<tenantId>")
            .build();

        Jedis jedisClient = new AzureJedisClientBuilder()
            .cacheHostName("<cache host name>")
            .port(<port number>)
            .username("<username>")
            .credential(clientCertificateCredential)
            .build();

        jedisClient.set("Az:key", "sample");
        jedisClient.close();
}
```
