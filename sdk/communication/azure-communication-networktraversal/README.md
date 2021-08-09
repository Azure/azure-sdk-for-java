# Azure Communication Network Traversal Package client library for Java

Azure Communication Network Traversal is managing TURN credentials for Azure Communication Services.

It will provide TURN credentials to a user.

[Source code](https://github.com/Azure/azure-sdk-for-python/blob/master/sdk/communication) | [API reference documentation](https://github.com/Azure/azure-sdk-for-python/blob/master/sdk/communication)

# Getting started

### Prerequisites

- An Azure subscription. [Create an account for free](https://azure.microsoft.com/free/?WT.mc_id=A261C142F).
- [Java Development Kit (JDK)](https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable) version 8 or above.
- [Apache Maven](https://maven.apache.org/download.cgi).
- A Communication Services resource. You can use the [Azure Portal](https://docs.microsoft.com/azure/communication-services/quickstarts/create-communication-resource?tabs=windows&pivots=platform-azp) or the [Azure PowerShell](https://docs.microsoft.com/powershell/module/az.communication/new-azcommunicationservice) to set it up.

### Include the package

[//]: # ({x-version-update-start;com.azure:azure-communication-networktraversal;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-communication-networktraversal</artifactId>
  <version>1.1.1</version>
</dependency>
```

## Authenticate the client

There are two forms of authentication to use the Relay SDK:

### Azure Active Directory Token Authentication
A `DefaultAzureCredential` object must be passed to the `CommunicationRelayClientBuilder` via the credential() function. Endpoint and httpClient must also be set via the endpoint() and httpClient() functions respectively.

`AZURE_CLIENT_SECRET`, `AZURE_CLIENT_ID` and `AZURE_TENANT_ID` environment variables
are needed to create a DefaultAzureCredential object.

<!-- embedme ./src/samples/java/com/azure/communication/networktraversal/ReadmeSamples.java#L55-L61 -->
```java
// You can find your endpoint and access key from your resource in the Azure Portal
String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";

CommunicationRelayClient communicationRelayClient = new CommunicationRelayClientBuilder()
    .endpoint(endpoint)
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

### AzureKeyCredential Authentication
Network Traversal uses HMAC authentication with the resource access key.
The access key can be used to create an AzureKeyCredential and provided to the `CommunicationRelayClientBuilder` via the credential() function. Endpoint and httpClient must also be set via the endpoint() and httpClient() functions respectively.

<!-- embedme ./src/samples/java/com/azure/communication/networktraversal/ReadmeSamples.java#L21-L28 -->
```java
// You can find your endpoint and access key from your resource in the Azure Portal
String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";
AzureKeyCredential keyCredential = new AzureKeyCredential("<access-key>");

CommunicationRelayClient communicationRelayClient = new CommunicationRelayClientBuilder()
    .endpoint(endpoint)
    .credential(keyCredential)
    .buildClient();
```

### Connection String Authentication
Alternatively, you can provide the entire connection string using the connectionString() function instead of providing the endpoint and access key.
<!-- embedme ./src/samples/java/com/azure/communication/networktraversal/ReadmeSamples.java#L39-L44 -->
```java
// You can find your connection string from your resource in the Azure Portal
String connectionString = "<connection_string>";

CommunicationRelayClient communicationRelayClient = new CommunicationRelayClientBuilder()
    .connectionString(connectionString)
    .buildClient();
```

## Key concepts

`CommunicationRelayClient` and `CommunicationRelayAsyncClient` provide the functionalities to manage users and user tokens.

## Examples

### Getting a new Relay Configuration
Use the `createUser` function to create a new user from CommunicationIdentityClient
Use the `getRelayConfiguration` function to get a Relay Configuration

<!-- embedme ./src/samples/java/com/azure/communication/networktraversal/ReadmeSamples.java#L73-L74 -->
```java
CommunicationIdentityClient communicationIdentityClient = new CommunicationIdentityClientBuilder()
            .connectionString(connectionString)
            .buildClient();

CommunicationUserIdentifier user = communicationIdentityClient.createUser();
System.out.println("User id: " + user.getId());

CommunicationRelayConfiguration config = communicationRelayClient.getRelayConfiguration(user);
```

## Troubleshooting

All user token service operations will throw an exception on failure.
<!-- embedme ./src/samples/java/com/azure/communication/networktraversal/ReadmeSamples.java#L139-L143 -->
```java
try {
    CommunicationUserIdentifier user = communicationIdentityClient.createUser();
    CommunicationRelayClient communicationRelayClient = createCommunicationNetworkTraversalClient();
    CommunicationRelayConfiguration config = communicationRelayClient.getRelayConfiguration(user);
} catch (RuntimeException ex) {
    System.out.println(ex.getMessage());
}
```

## Next steps

Please take a look at the [samples][samples] directory for detailed examples of how to use this library to manage relay configuration

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
[api_documentation]: https://aka.ms/java-docs

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcommunication%2Fazure-communication-identity%2FREADME.png)
