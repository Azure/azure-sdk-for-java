# Azure Communication Identity client library for Java

The identity package is used for managing users and tokens for Azure Communication Services.

[Source code][source] | [Package (Maven)][package] | [API reference documentation][api_documentation]
| [Product documentation][product_docs]
<!-- Update the source and package link -->

## Getting started

### Prerequisites

- An Azure subscription. [Create an account for free](https://azure.microsoft.com/free/?WT.mc_id=A261C142F).
- [Java Development Kit (JDK)](https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable) version 8 or above.
- [Apache Maven](https://maven.apache.org/download.cgi).
- A Communication Services resource. You can use the [Azure Portal](https://docs.microsoft.com/azure/communication-services/quickstarts/create-communication-resource?tabs=windows&pivots=platform-azp) or the [Azure PowerShell](https://docs.microsoft.com/powershell/module/az.communication/new-azcommunicationservice) to set it up.

### Include the package

[//]: # ({x-version-update-start;com.azure:azure-communication-identity;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-communication-identity</artifactId>
  <version>1.0.0-beta.4</version>
</dependency>
```

## Authenticate the client

There are two forms of authentication to use the Identity SDK:

### Azure Active Directory Token Authentication
A `DefaultAzureCredential` object must be passed to the `CommunicationIdentityClientBuilder` via the credential() function. Endpoint and httpClient must also be set via the endpoint() and httpClient() functions respectively.

`AZURE_CLIENT_SECRET`, `AZURE_CLIENT_ID` and `AZURE_TENANT_ID` environment variables 
are needed to create a DefaultAzureCredential object.

<!-- embedme ./src/samples/java/com/azure/communication/identity/ReadmeSamples.java#L67-L76 -->
```java
String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";

// Create an HttpClient builder of your choice and customize it
HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();

CommunicationIdentityClient communicationIdentityClient = new CommunicationIdentityClientBuilder()
    .endpoint(endpoint)
    .credential(new DefaultAzureCredentialBuilder().build())
    .httpClient(httpClient)
    .buildClient();
```

### Access Key Authentication
Identity uses HMAC authentication with the resource access key.
The access key must be provided to the `CommunicationIdentityClientBuilder` via the accessKey() function. Endpoint and httpClient must also be set via the endpoint() and httpClient() functions respectively.

<!-- embedme ./src/samples/java/com/azure/communication/identity/ReadmeSamples.java#L24-L35 -->
```java
// You can find your endpoint and access key from your resource in the Azure Portal
String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";
String accessKey = "SECRET";

// Create an HttpClient builder of your choice and customize it
HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();

CommunicationIdentityClient communicationIdentityClient = new CommunicationIdentityClientBuilder()
    .endpoint(endpoint)
    .accessKey(accessKey)
    .httpClient(httpClient)
    .buildClient();
```

Alternatively, you can provide the entire connection string using the connectionString() function instead of providing the endpoint and access key. 
<!-- embedme ./src/samples/java/com/azure/communication/identity/ReadmeSamples.java#L49-L55 -->
```java
// Your can find your connection string from your resource in the Azure Portal
String connectionString = "<connection_string>";

CommunicationIdentityClient communicationIdentityClient = new CommunicationIdentityClientBuilder()
    .connectionString(connectionString)
    .httpClient(httpClient)
    .buildClient();
```

## Key concepts
`CommunicationIdentityClient` and `CommunicationIdentityAsyncClient` provide the functionalities to manage users and user tokens.

## Examples

### Creating a new user
Use the `createUser` function to create a new user. `user.getId()` gets the
unique ID of the user that was created.

<!-- embedme ./src/samples/java/com/azure/communication/identity/ReadmeSamples.java#L88-L89 -->
```java
CommunicationUserIdentifier user = communicationIdentityClient.createUser();
System.out.println("User id: " + user.getId());
```

Alternatively, use the `createUserWithToken` function to create a new user and issue a token for it. 
For this option, a list of communication tokens scopes must be defined. 
<!-- embedme ./src/samples/java/com/azure/communication/identity/ReadmeSamples.java#L100-L106 -->
```java
// Define a list of communication token scopes
List<CommunicationTokenScope> scopes = 
    new ArrayList<>(Arrays.asList(CommunicationTokenScope.CHAT));

CommunicationUserIdentifierWithTokenResult result = communicationIdentityClient.createUserWithToken(scopes);
System.out.println("User id: " + result.getUser().getId());
System.out.println("User token value: " + result.getUserToken().getToken());
```

### Issuing or Refreshing a token for an existing user
Use the `issueToken` function to issue or refresh a token for an existing user. The function
also takes in a list of `CommunicationIdentityTokenScope`. Scope options include:
- `chat` (Chat)
- `voip` (Voice over IP)

<!-- embedme ./src/samples/java/com/azure/communication/identity/ReadmeSamples.java#L119-L124 -->
```java
List<CommunicationTokenScope> scopes = 
    new ArrayList<>(Arrays.asList(CommunicationTokenScope.CHAT));

AccessToken userToken = communicationIdentityClient.issueToken(user, scopes);
System.out.println("User token value: " + userToken.getToken());
System.out.println("Expires at: " + userToken.getExpiresAt());
```

### Revoking all tokens for an existing user
Use the `revokeTokens` function to revoke all the issued tokens of a user.

<!-- embedme ./src/samples/java/com/azure/communication/identity/ReadmeSamples.java#L137-L138 -->
```java
// revoke tokens issued for the specified user
communicationIdentityClient.revokeTokens(user);
```

### Deleting a user
Use the `deleteUser` function to delete a user.

<!-- embedme ./src/samples/java/com/azure/communication/identity/ReadmeSamples.java#L147-L148 -->
```java
// delete a previously created user
communicationIdentityClient.deleteUser(user);
```

## Troubleshooting

All user token service operations will throw an exception on failure.
<!-- embedme ./src/samples/java/com/azure/communication/identity/ReadmeSamples.java#L156-L160 -->
```java
try {
    CommunicationUserIdentifier user = communicationIdentityClient.createUser();
} catch (RuntimeException ex) {
    System.out.println(ex.getMessage());
}
```

## Next steps

Check out other client libraries for Azure communication service

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a [Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

<!-- LINKS -->
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[product_docs]: https://docs.microsoft.com/azure/communication-services/
[package]: https://search.maven.org/artifact/com.azure/azure-communication-identity
[api_documentation]: https://aka.ms/java-docs
[source]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/communication/azure-communication-identity/src

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcommunication%2Fazure-communication-identity%2FREADME.png)
