# What is this?

The "msi-auth-token-provider" jar is a library that enables :
* Azure VMs and container instances and
* Web Apps (funcitons included)
Retrieve authentication tokens for syatem/user assigned [managed identities](https://docs.microsoft.com/azure/active-directory/managed-identities-azure-resources/overview).

This is a light weight library that does not have many dependencies. 

# Usage
## Dependency
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

## Getting the token

Add the folowing import statement to get in all the classes in the jar

```java
import com.microsoft.azure.msiAuthTokenProvider.*;
```

### Getting a token for system assigned identity
Use the following code to get the auth token for System assigned identity :

``` java
...
    MSICredentials credsProvider = MSICredentials.getMSICredentials();
    MSIToken token = credsProvider.getToken(null);
    String tokenValue = token.accessToken();
...
```

### Getting a token for user assigned identity

#### Using the client Id for the user assigned identity :
Use the following code to get the auth token for an User assigned identity :
```java
...
    MSICredentials credsProvider = MSICredentials.getMSICredentials();
    credsProvider.updateClientId(clientId);
    MSIToken token = credsProvider.getToken(null);
    String tokenValue = token.accessToken();
...            
```

Where `clientId` is retrieved from the User Assigned Identity (This is currently only supported from within the portal).

#### Using the object Id for the user assigned identity :
Use the following code to get the auth token for an User assigned identity :
```java
...
    MSICredentials credsProvider = MSICredentials.getMSICredentials();
    credsProvider.updateObjectId(objectId);
    MSIToken token = credsProvider.getToken(null);
    String tokenValue = token.accessToken();
...            
```

Where `objectId` is retrieved from the User Assigned Identity (This is currently only supported from within the portal).

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fauthorization%2Fmicrosoft-azure-authentication-msi-token-provider%2Freadme.png)
