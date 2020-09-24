# Azure Communication Administration client library for Java

Azure Communication Administration is used for managing users and tokens for Azure Communication Services.

<!-- [Source code][source] | [Package (Maven)][package] | [API reference documentation][api_documentation]
| [Product documentation][azconfig_docs] -->
## Getting started

### Prerequisites

- An Azure account with an active subscription. [Create an account for free](https://azure.microsoft.com/free/?WT.mc_id=A261C142F).
- [Java Development Kit (JDK)](https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable) version 8 or above.
- [Apache Maven](https://maven.apache.org/download.cgi).
- A deployed Communication Services resource.

### Include the package

[//]: # ({x-version-update-start;com.azure:azure-communication-administration;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-communication-administration</artifactId>
  <version>1.0.0-beta.1</version>
</dependency>
```

## Key concepts
To use the Admnistration SDK, a resource access key is required for authentication. 

Administration uses HMAC authentication with the resource access key. This is done via the 
CommunicationClientCredentials. The credentials must be provided to the CommunicationIdentityClientBuilder 
via the credential() function. Endpoint and httpClient must also be set via the endpoint()
and httpClient() functions respectively.

<!-- embedme ./src/samples/java/com/azure/communication/administration/ReadmeSamples.java#L29-L40 -->
```java
// Your can find your endpoint and access token from your resource in the Azure Portal
String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";
String accessToken = "SECRET";

// Create an HttpClient builder of your choice and customize it
HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();

CommunicationIdentityClient communicationIdentityClient = new CommunicationIdentityClientBuilder()
    .endpoint(endpoint)
    .credential(new CommunicationClientCredential(accessToken))
    .httpClient(httpClient)
    .buildClient();
```

## Examples

### Creating a new user
Use the `createUser` function to create a new user. `user.getId()` gets the
unique ID of the user that was created.

<!-- embedme ./src/samples/java/com/azure/communication/administration/ReadmeSamples.java#L53-L54 -->
```java
CommunicationUser user = communicationIdentityClient.createUser();
System.out.println("User id: " + user.getId());
```

### Issuing or Refreshing a token for an existing user
Use the `issueToken` function to issue or refresh a token for an existing user. The function
also takes in a list of communication token scopes. Scope options include:
- `chat` (Chat)
- `pstn` (Public switched telephone network)
- `voip` (Voice over IP)

<!-- embedme ./src/samples/java/com/azure/communication/administration/ReadmeSamples.java#L71-L74 -->
```java
List<String> scopes = new ArrayList<>(Arrays.asList("chat"));
CommunicationUserToken userToken = communicationIdentityClient.issueToken(user, scopes);
System.out.println("Token: " + userToken.getToken());
System.out.println("Expires On: " + userToken.getExpiresOn());
```

### Revoking all tokens for an existing user
Use the `revokeTokens` function to revoke all the issued tokens of a user.

<!-- embedme ./src/samples/java/com/azure/communication/administration/ReadmeSamples.java#L91-L92 -->
```java
// revoke tokens issued for the user prior to now
communicationIdentityClient.revokeTokens(user, OffsetDateTime.now());
```

### Deleting a user
Use the `deleteUser` function to delete a user.

<!-- embedme ./src/samples/java/com/azure/communication/administration/ReadmeSamples.java#L105-L106 -->
```java
// delete a previously created user
communicationIdentityClient.deleteUser(user);
```


## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a [Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.


## Troubleshooting

In progress.

## Next steps

Check out other client libraries for Azure communication service

<!-- LINKS -->
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[azconfig_docs]: https://docs.microsoft.com/azure/azure-app-configuration
[package]: https://search.maven.org/artifact/com.azure/azure-data-appconfiguration
[api_documentation]: https://aka.ms/java-docs
[source]: src



![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcommunication%2Fazure-communication-administration%2FREADME.png)