## What is this?

The "msi-auth-token-provider" jar is a library that enables :
* Azure VMs and container instances and
* Web Apps (funcitons included)
Retrieve authentication tokens for syatem/user assigned [managed identities](https://docs.microsoft.com/en-us/azure/active-directory/managed-identities-azure-resources/overview).

Thios is a light weight library that does not have many dependencies. The ohnly external library ti depends on is [`RxJava`](https://github.com/ReactiveX/RxJava/releases/tag/v1.2.4)

## Usage
### Dependency
Take a dependency on the jar in you pom file like follows
```xml
  <dependencies>
      <dependency>
          <groupId>com.microsoft.azure.msi_auth_token_provider</groupId>
          <artifactId>azure-authentication-msi-token-provider</artifactId>
          <version>1.0.0-beta</version>
      </dependency>
  </dependencies>
```

### Getting the token

Add the folowing import statement to get in all the classes in the jar

```java
import com.microsoft.azure.msiAuthTokenProvider.*;
```

#### Getting a token for system assigned identity
Use the following code to get the auth token for System assigned identity :

``` java
...
    MSICredentials credsProvider = MSICredentials.getMSICredentials();
    String token = credsProvider.getToken(null).toBlocking().value();
...
```

The `getToken` function returns a [Rx Single](http://reactivex.io/documentation/single.html). What i have shown above is how to use in a syncronous fashion.

#### Getting a token for user assigned identity
Use the following code to get the auth token for an User assigned identity :
```java
...
    MSICredentials credsProvider = MSICredentials.getMSICredentials();
    credsProvider.updateClientId(clientId);
    String token = credsProvider.getToken(null).toBlocking().value();
...            
```

Where `clientId` is retrieved from the User Assigned Identity (This is currently only supported from within the portal).