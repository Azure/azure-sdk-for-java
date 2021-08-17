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
#### Include the BOM file

Please include the azure-sdk-bom to your project to take dependency on the General Availability (GA) version of the library. In the following snippet, replace the {bom_version_to_target} placeholder with the version number.
To learn more about the BOM, see the [AZURE SDK BOM README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/boms/azure-sdk-bom/README.md).

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.azure</groupId>
            <artifactId>azure-sdk-bom</artifactId>
            <version>{bom_version_to_target}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```
and then include the direct dependency in the dependencies section without the version tag.

```xml
<dependencies>
  <dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-communication-identity</artifactId>
  </dependency>
</dependencies>
```

#### Include direct dependency
If you want to take dependency on a particular version of the library that is not present in the BOM,
add the direct dependency to your project as follows.


[//]: # ({x-version-update-start;com.azure:azure-communication-identity;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-communication-identity</artifactId>
  <version>1.1.1</version>
</dependency>
```

## Authenticate the client

There are two forms of authentication to use the Identity SDK:

### Azure Active Directory Token Authentication
A `DefaultAzureCredential` object must be passed to the `CommunicationIdentityClientBuilder` via the credential() function. Endpoint and httpClient must also be set via the endpoint() and httpClient() functions respectively.

`AZURE_CLIENT_SECRET`, `AZURE_CLIENT_ID` and `AZURE_TENANT_ID` environment variables
are needed to create a DefaultAzureCredential object.

<!-- embedme ./src/samples/java/com/azure/communication/identity/ReadmeSamples.java#L55-L61 -->
```java
// You can find your endpoint and access key from your resource in the Azure Portal
String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";

CommunicationIdentityClient communicationIdentityClient = new CommunicationIdentityClientBuilder()
    .endpoint(endpoint)
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

### AzureKeyCredential Authentication
Identity uses HMAC authentication with the resource access key.
The access key can be used to create an AzureKeyCredential and provided to the `CommunicationIdentityClientBuilder` via the credential() function. Endpoint and httpClient must also be set via the endpoint() and httpClient() functions respectively.

<!-- embedme ./src/samples/java/com/azure/communication/identity/ReadmeSamples.java#L21-L28 -->
```java
// You can find your endpoint and access key from your resource in the Azure Portal
String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";
AzureKeyCredential keyCredential = new AzureKeyCredential("<access-key>");

CommunicationIdentityClient communicationIdentityClient = new CommunicationIdentityClientBuilder()
    .endpoint(endpoint)
    .credential(keyCredential)
    .buildClient();
```

### Connection String Authentication
Alternatively, you can provide the entire connection string using the connectionString() function instead of providing the endpoint and access key.
<!-- embedme ./src/samples/java/com/azure/communication/identity/ReadmeSamples.java#L39-L44 -->
```java
// You can find your connection string from your resource in the Azure Portal
String connectionString = "<connection_string>";

CommunicationIdentityClient communicationIdentityClient = new CommunicationIdentityClientBuilder()
    .connectionString(connectionString)
    .buildClient();
```

## Key concepts

`CommunicationIdentityClient` and `CommunicationIdentityAsyncClient` provide the functionalities to manage users and user tokens.

## Examples

### Creating a new user
Use the `createUser` function to create a new user. `user.getId()` gets the
unique ID of the user that was created.

<!-- embedme ./src/samples/java/com/azure/communication/identity/ReadmeSamples.java#L73-L74 -->
```java
CommunicationUserIdentifier user = communicationIdentityClient.createUser();
System.out.println("User id: " + user.getId());
```

### Getting a token for an existing user
Use the `getToken` function to get a token for an existing user. The function
also takes in a list of `CommunicationIdentityTokenScope`. Scope options include:
- `chat` (Chat)
- `voip` (Voice over IP)

<!-- embedme ./src/samples/java/com/azure/communication/identity/ReadmeSamples.java#L102-L107 -->
```java
 // Define a list of communication token scopes
List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);

AccessToken userToken = communicationIdentityClient.getToken(user, scopes);
System.out.println("User token value: " + userToken.getToken());
System.out.println("Expires at: " + userToken.getExpiresAt());
```

### Create a new user and token in a single request
For convenience, use `createUserAndToken` to create a new user and issue a token with one function call. This translates into a single web request as opposed to creating a user first and then issuing a token.
<!-- embedme ./src/samples/java/com/azure/communication/identity/ReadmeSamples.java#L85-L90 -->
```java
// Define a list of communication token scopes
List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);

CommunicationUserIdentifierAndToken result = communicationIdentityClient.createUserAndToken(scopes);
System.out.println("User id: " + result.getUser().getId());
System.out.println("User token value: " + result.getUserToken().getToken());
```

### Revoking all tokens for an existing user
Use the `revokeTokens` function to revoke all the issued tokens of a user.

<!-- embedme ./src/samples/java/com/azure/communication/identity/ReadmeSamples.java#L120-L121 -->
```java
// revoke tokens issued for the specified user
communicationIdentityClient.revokeTokens(user);
```

### Deleting a user
Use the `deleteUser` function to delete a user.

<!-- embedme ./src/samples/java/com/azure/communication/identity/ReadmeSamples.java#L130-L131 -->
```java
// delete a previously created user
communicationIdentityClient.deleteUser(user);
```

## Troubleshooting

All user token service operations will throw an exception on failure.
<!-- embedme ./src/samples/java/com/azure/communication/identity/ReadmeSamples.java#L139-L143 -->
```java
try {
    CommunicationUserIdentifier user = communicationIdentityClient.createUser();
} catch (RuntimeException ex) {
    System.out.println(ex.getMessage());
}
```

## Next steps

Please take a look at the [samples][samples] directory for detailed examples of how to use this library to manage identities and tokens.

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
[samples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/communication/azure-communication-identity/src/samples/java/com/azure/communication/identity/ReadmeSamples.java
[source]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/communication/azure-communication-identity/src

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcommunication%2Fazure-communication-identity%2FREADME.png)
