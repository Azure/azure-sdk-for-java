## Azure Cache for Redis: Azure AD with Lettuce client library

### Table of contents

- [Prerequisites](#prerequisites)
- [Dependency Requirements](#dependency-requirements)
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

```

#### Authenticate with Azure AD: Using Token Cache
This sample is intended to assist in authenticating with Azure AD via Jedis client library. It focuses on displaying the logic required to fetch an Azure AD access token using a Token cache and to use it as password when setting up the Jedis instance. It also shows how to recreate and authenticate the Jedis instance using the cached access token when the client's connection is broken in error/exception scenarios. The Token Cache stores and proactively refreshes the Azure AD Access Token 2 minutes before expiry and ensures a non-expired token is available for use when the cache is accessed.

##### Migration Guidance
When migrating your existing your application code, you need to replace the password input with the Azure AD token.
Integrate the logic in your application code to fetch an Azure AD access token via the Azure Identity library and store it in a Token Cache as shown below and replace it with the password configuring/retrieving logic in your application code.

##### Version 6.2.0.RELEASE or above
```xml
<dependency>
  <groupId>io.lettuce</groupId>
  <artifactId>lettuce-core</artifactId>
  <version>6.2.0.RELEASE</version>
</dependency>
```

```java

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

