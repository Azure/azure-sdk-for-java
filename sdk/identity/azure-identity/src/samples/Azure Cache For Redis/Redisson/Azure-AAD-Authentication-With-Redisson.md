## Azure Cache for Redis: Azure AD with Redisson client library

### Table of contents

- [Dependency Requirements](#dependency-requirements)
- [Authenticate with Azure AD - Hello World](#authenticate-with-azure-ad-hello-world)
- [Authenticate with Azure AD - Handle Re-Authentication](#authenticate-with-azure-ad-handle-re-authentication)

#### Dependency Requirements
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
Familiarity with the Jedis and Azure Identity client libraries is assumed. If you're new to the Azure Identity library for Java, see the docs for [Azure Identity](https://docs.microsoft.com/azure/developer/java/sdk/identity) and [Redisson](https://www.javadoc.io/doc/redis.clients/jedis/latest/index.html) rather than this guide.

[Authenticate with Azure AD - Hello World](#authenticate-with-azure-ad-hello-world)
This sample is recommended for users getting started to use Azure AD authentication with Azure Cache for Redis.

[Authenticate with Azure AD - Handle Re-Authentication](#authenticate-with-azure-ad-handle-re-authentication)
This sample is recommended users looking to build long-running applications and would like to handle re authenticating with Azure AD upon token expiry.

#### Authenticate with Azure AD Hello World.
This sample is intended to assist in authenticating with Azure AD via the Redisson client library. It focuses on displaying the logic required to fetch an Azure AD access token and to use it as password when setting up the Redisson Client instance. It Further shows how to recreate and authenticate the Redisson Client instance when its connection is broken in Error/Exception scenarios.

##### Migration Guidance
When migrating your existing your application code, you need to replace the password input with Azure Active Directory Token.
Integrate the logic in your application code to fetch an Azure AD access token via the Azure Identity library as shown below and replace it with the password configuring/retrieving logic in your application code.


**Note:** The below sample uses the [Azure Identity library](https://docs.microsoft.com/azure/developer/java/sdk/identity)'s `ClientCertificateCredential`. The credential can be replaced with any of the other `TokenCredential` implementations offered by the Azure Identity library.

```java
//Construct a Token Credential from Identity library, e.g. ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
ClientCertificateCredential clientCertificateCredential = new ClientCertificateCredentialBuilder()
        .clientId("YOUR-CLIENT-ID")
        .pfxCertificate("YOUR-CERTIFICATE-PATH", "CERTIFICATE-PASSWORD")
        .tenantId("YOUR-TENANT-ID")
        .build();

// Fetch an Azure AD token to be used for authentication.
String token = clientCertificateCredential
        .getToken(new TokenRequestContext()
                .addScopes("https://*.cacheinfra.windows.net:10225/appid/.default")).block().getToken();

// Create Client Configuration
Config config = new Config();
config.useSingleServer()
        .setAddress("redis://YOUR_HOST_NAME.cache.windows.net:6379")
        .setKeepAlive(true)
        .setUsername("Username")
        .setPassword(token)
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

#### Authenticate with Azure AD Handle Re Authentication
This sample is intended to assist in authenticating with Azure AD via Redisson client library. It focuses on displaying the logic required to fetch an Azure AD access token and to use it as password when setting up the Redisson client instance. It Further shows how to recreate and authenticate the Redisson client instance when its connection is broken in error/exception scenarios.

##### Migration Guidance
When migrating your existing your application code, you need to replace the password input with Azure Active Directory Token.
Integrate the logic in your application code to fetch an Azure AD access token via the Azure Identity library as shown below and replace it with the password configuring/retrieving logic in your application code.

**Note:** The below sample uses the [Azure Identity library](https://docs.microsoft.com/azure/developer/java/sdk/identity)'s `ClientCertificateCredential`. The credential can be replaced with any of the other `TokenCredential` implementations offered by the Azure Identity library.

```java
//Construct a Token Credential from Identity library, e.g. ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
ClientCertificateCredential clientCertificateCredential = getClientCertificateCredential();

// Fetch an Azure AD token to be used for authentication. This token will be used as the password.
TokenRequestContext trc = new TokenRequestContext().addScopes("https://*.cacheinfra.windows.net:10225/appid/.default");
AccessToken accessToken = getAccessToken(clientCertificateCredential, trc);

// Create Redisson Client
RedissonClient redisson = createRedissonClient("redis://YOUR_HOST_NAME.cache.windows.net:6379", "USERNAME", accessToken);

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
        // Handle Exception as Required.
        exception.printStackTrace();

        // If access token is expired, we need to create a new client with a valid token as password.
        if (accessToken.isExpired()) {
            redisson.shutdown();
            redisson = createRedissonClient("redis://YOUR_HOST_NAME.cache.windows.net:6379", "USERNAME", getAccessToken(clientCertificateCredential, trc));
        }
    } catch (Exception e) {
        // Handle Exception as required
        e.printStackTrace();
    }
    i++;
}

redisson.shutdown();
}

    // Helper Code
    private static ClientCertificateCredential getClientCertificateCredential() {
        return new ClientCertificateCredentialBuilder()
                .clientId("YOUR-CLIENT-ID")
                .pfxCertificate("YOUR-CERTIFICATE-PATH", "CERTIFICATE-PASSWORD")
                .tenantId("YOUR-TENANT-ID")
                .build();
    }

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
