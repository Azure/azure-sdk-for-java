# Azure Communication Network Traversal Package client library for Java

Azure Communication Network Traversal is managing TURN credentials for Azure Communication Services.

It will provide TURN credentials to a user.

[Source code](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/communication) | [API reference documentation](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/communication)

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
and then include the direct dependency in the dependencies section without the version tag as shown below.

```xml
<dependencies>
  <dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-security-attestation</artifactId>
  </dependency>
</dependencies>
```

#### Include direct dependency
If you want to take dependency on a particular version of the library that is not present in the BOM,
add the direct dependency to your project as follows.

[//]: # ({x-version-update-start;com.azure:azure-communication-networktraversal;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-communication-networktraversal</artifactId>
  <version>1.1.0-beta.2</version>
</dependency>
```

## Authenticate the client

There are two forms of authentication to use the Relay SDK:

### Azure Active Directory Token Authentication
A `DefaultAzureCredential` object can be passed to the `CommunicationRelayClientBuilder` via the credential() function. Endpoint must also be set via the endpoint() function.

`AZURE_CLIENT_SECRET`, `AZURE_CLIENT_ID` and `AZURE_TENANT_ID` environment variables
are needed to create a DefaultAzureCredential object.

```java readme-sample-createCommunicationRelayClientWithAAD
// You can find your endpoint and access key from your resource in the Azure Portal
String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";

CommunicationRelayClient communicationRelayClient = new CommunicationRelayClientBuilder()
    .endpoint(endpoint)
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

### AzureKeyCredential Authentication
Network Traversal uses HMAC authentication with the resource access key.
The access key can be used to create an AzureKeyCredential and provided to the `CommunicationRelayClientBuilder` via the credential() function. Endpoint must also be set via the endpoint() function.

```java readme-sample-createCommunicationNetworkTraversalClient
// You can find your endpoint and access key from your resource in the Azure Portal
String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";
AzureKeyCredential keyCredential = new AzureKeyCredential("<access-key>");

CommunicationRelayClient communicationRelayClient = new CommunicationRelayClientBuilder()
    .endpoint(endpoint)
    .credential(keyCredential)
    .buildClient();
```

```java readme-sample-createCommunicationNetworkTraversalAsyncClient
// You can find your endpoint and access key from your resource in the Azure Portal
String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";
AzureKeyCredential keyCredential = new AzureKeyCredential("<access-key>");

CommunicationRelayAsyncClient communicationRelayClient = new CommunicationRelayClientBuilder()
    .endpoint(endpoint)
    .credential(keyCredential)
    .buildAsyncClient();
```

### Connection String Authentication
Alternatively, you can provide the entire connection string using the connectionString() function instead of providing the endpoint and access key.

```java readme-sample-createCommunicationRelayClientWithConnectionString
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

```java readme-sample-getRelayConfigurationWithoutIdentity
CommunicationRelayClient communicationRelayClient = createCommunicationNetworkTraversalClient();
CommunicationRelayConfiguration config = communicationRelayClient.getRelayConfiguration();

System.out.println("Expires on:" + config.getExpiresOn());
List<CommunicationIceServer> iceServers = config.getIceServers();

for (CommunicationIceServer iceS : iceServers) {
    System.out.println("URLS: " + iceS.getUrls());
    System.out.println("Username: " + iceS.getUsername());
    System.out.println("Credential: " + iceS.getCredential());
    System.out.println("RouteType: " + iceS.getRouteType());
}
```

### Getting a new Relay Configuration providing a user

Use the `createUser` function to create a new user from CommunicationIdentityClient
Use the `getRelayConfiguration` function to get a Relay Configuration

```java readme-sample-getRelayConfiguration
CommunicationIdentityClient communicationIdentityClient = createCommunicationIdentityClient();

CommunicationUserIdentifier user = communicationIdentityClient.createUser();
System.out.println("User id: " + user.getId());

GetRelayConfigurationOptions options = new GetRelayConfigurationOptions();
options.setCommunicationUserIdentifier(user);

CommunicationRelayClient communicationRelayClient = createCommunicationNetworkTraversalClient();
CommunicationRelayConfiguration config = communicationRelayClient.getRelayConfiguration(options);

System.out.println("Expires on:" + config.getExpiresOn());
List<CommunicationIceServer> iceServers = config.getIceServers();

for (CommunicationIceServer iceS : iceServers) {
    System.out.println("URLS: " + iceS.getUrls());
    System.out.println("Username: " + iceS.getUsername());
    System.out.println("Credential: " + iceS.getCredential());
    System.out.println("RouteType: " + iceS.getRouteType());
}
```

### Getting a new Relay Configuration providing a Route Type

```java readme-sample-getRelayConfigurationWithRouteType

GetRelayConfigurationOptions options = new GetRelayConfigurationOptions();
options.setRouteType(RouteType.ANY);

CommunicationRelayClient communicationRelayClient = createCommunicationNetworkTraversalClient();
CommunicationRelayConfiguration config = communicationRelayClient.getRelayConfiguration(options);

System.out.println("Expires on:" + config.getExpiresOn());
List<CommunicationIceServer> iceServers = config.getIceServers();

for (CommunicationIceServer iceS : iceServers) {
    System.out.println("URLS: " + iceS.getUrls());
    System.out.println("Username: " + iceS.getUsername());
    System.out.println("Credential: " + iceS.getCredential());
    System.out.println("RouteType: " + iceS.getRouteType());
}
```

## Troubleshooting

All user token service operations will throw an exception on failure.

```java readme-sample-createUserTroubleshooting
try {
    CommunicationUserIdentifier user = communicationIdentityClient.createUser();
    GetRelayConfigurationOptions options = new GetRelayConfigurationOptions();
    options.setCommunicationUserIdentifier(user);

    CommunicationRelayClient communicationRelayClient = createCommunicationNetworkTraversalClient();
    CommunicationRelayConfiguration config = communicationRelayClient.getRelayConfiguration(options);
} catch (RuntimeException ex) {
    System.out.println(ex.getMessage());
}
```
Refer to the official documentation for more details and error codes (to be added).

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

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcommunication%2Fazure-communication-networktraversal%2FREADME.png)
