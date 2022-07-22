## Azure Cache for Redis: Azure AD with Jedis client library

### Table of contents

- [Prerequisites](#prerequisites)
- [Authenticate with Azure AD - Hello World](#authenticate-with-azure-ad-hello-world)
- [Authenticate with Azure AD - Handle Reauthentication](#authenticate-with-azure-ad-handle-reauthentication)
- [Authenticate with Azure AD - Using Token Cache](#authenticate-with-azure-ad-using-token-cache)
- [Authenticate with Azure AD - Azure Jedis Wrapper](#authenticate-with-azure-ad-azure-jedis-wrapper)
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
        <groupId>redis.clients</groupId>
        <artifactId>jedis</artifactId>
        <version>4.0.1</version>
    </dependency>
    ```

#### Samples Guidance

Familiarity with the [Jedis](https://www.javadoc.io/doc/redis.clients/jedis/latest/index.html) and [Azure Identity for Java](https://docs.microsoft.com/azure/developer/java/sdk/identity) client libraries is assumed.

* [Authenticate with Azure AD - Hello World](#authenticate-with-azure-ad-hello-world):
    This sample is recommended for users getting started to use Azure AD authentication with Azure Cache for Redis.

* [Authenticate with Azure AD - Handle Reauthentication](#authenticate-with-azure-ad-handle-reauthentication):
  This sample is recommended to users looking to build long-running applications that would like to handle reauthenticating with Azure AD upon token expiry.

* [Authenticate with Azure AD - Using Token Cache](#authenticate-with-azure-ad-using-token-cache):
  This sample is recommended to users looking to build long-running applications that would like to handle reauthenticating with a Token cache. The Token Cache stores and proactively refreshes the Azure AD Access Token 2 minutes before expiry and ensures a non-expired token is available for use when the cache is accessed.
  
* [Authenticate with Azure AD - Azure Jedis Wrapper](#authenticate-with-azure-ad-azure-jedis-wrapper):
 This sample is recommended to users looking to build long-running applications that would like to integrate our recommended wrapper implementation in their application which handles reconnection and re-authentication on user's behalf.
 

#### Authenticate with Azure AD: Hello World
This sample is intended to assist in authenticating with Azure AD via the Jedis client library. It focuses on displaying the logic required to fetch an Azure AD access token and to use it as password when setting up the Jedis instance.

##### Migration Guidance
When migrating your existing your application code, you need to replace the password input with the Azure AD token.
Integrate the logic in your application code to fetch an Azure AD access token via the Azure Identity library as shown below and replace it with the password configuring/retrieving logic in your application code.

```java

```

##### Supported Token Credentials for Azure AD Authentication
**Note:** The samples in this doc use the Azure Identity library's `DefaultAzureCredential` to fetch Azure AD Access Token. The other supported `TokenCredential` implementations that can be used from [Azure Identity for Java](https://docs.microsoft.com/azure/developer/java/sdk/identity) are as follows:
* [Client Certificate Credential](https://docs.microsoft.com/en-us/azure/developer/java/sdk/identity-service-principal-auth#client-certificate-credential)
* [Client Secret Credential](https://docs.microsoft.com/en-us/azure/developer/java/sdk/identity-service-principal-auth#client-secret-credential)
* [Managed Identity Credential](https://docs.microsoft.com/en-us/azure/developer/java/sdk/identity-azure-hosted-auth#managed-identity-credential)
* [Username Password Credential](https://docs.microsoft.com/en-us/azure/developer/java/sdk/identity-user-auth#username-password-credential)
* [Azure CLI Credential](https://docs.microsoft.com/en-us/azure/developer/java/sdk/identity-dev-env-auth#azure-cli-credential)
* [Interactive Browser Credential](https://docs.microsoft.com/en-us/azure/developer/java/sdk/identity-user-auth#interactive-browser-credential)
* [Device Code Credential](https://docs.microsoft.com/en-us/azure/developer/java/sdk/identity-user-auth#device-code-credential)

#### Authenticate with Azure AD: Handle Reauthentication
This sample is intended to assist in authenticating with Azure AD via Jedis client library. It focuses on displaying the logic required to fetch an Azure AD access token and to use it as password when setting up the Jedis instance. It also shows how to recreate and authenticate the Jedis instance when its connection is broken in error/exception scenarios.

##### Migration Guidance
When migrating your existing your application code, you need to replace the password input with the Azure AD token.
Integrate the logic in your application code to fetch an Azure AD access token via the Azure Identity library as shown below and replace it with the password configuring/retrieving logic in your application code.

```java

```

#### Authenticate with Azure AD: Using Token Cache
This sample is intended to assist in authenticating with Azure AD via Jedis client library. It focuses on displaying the logic required to fetch an Azure AD access token using a Token cache and to use it as password when setting up the Jedis instance. It also shows how to recreate and authenticate the Jedis instance using the cached access token when the client's connection is broken in error/exception scenarios. The Token Cache stores and proactively refreshes the Azure AD Access Token 2 minutes before expiry and ensures a non-expired token is available for use when the cache is accessed.

##### Migration Guidance
When migrating your existing your application code, you need to replace the password input with the Azure AD token.
Integrate the logic in your application code to fetch an Azure AD access token via the Azure Identity library and store it in a Token Cache as shown below and replace it with the password configuring/retrieving logic in your application code.

```java

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

Note: The wrapper code located [here](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity/src/samples/Azure-Cache-For-Redis/Jedis/java/com/azure/jedis) requires to be added/integrated in your application code for the below sample to work.


```java

```

#### Troubleshooting

##### Invalid Username Password Pair Error
In this error scenario, the username provided and the access token used as password are not compatible.
To mitigate this error, ensure that:
* On Portal, Under your `Redis Cache Resource` -> RBAC Rules, you've assigned the required role to your user/service principal identity.
* On Portal, Under your `Redis Cache Resource` -> Advanced settings -> AAD Access Authorization box is checked/enabled, if not enable it and press the Save button.

##### Permissions not granted / NOPERM Error
In this error scenario, the authentication was successful, but your registered user/service principal is not granted the RBAC permission to perform the action.
To mitigate this error, ensure that:
* On Portal, Under your `Redis Cache Resource` -> RBAC Rules, you've assigned the appropriate role (Owner, Contributor, Reader) to your user/service principal identity.
* In the event you're using a custom role, then ensure the permissions granted under your custom role include the one required for your target action.


