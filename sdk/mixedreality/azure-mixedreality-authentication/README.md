# Azure Mixed Reality client library for Java

Mixed Reality services, like Azure Spatial Anchors, Azure Remote Rendering, and others, use the Mixed Reality security
token service (STS) for authentication. This package supports exchanging Mixed Reality account credentials for an access
token from the STS that can be used to access Mixed Reality services.

[Source code][source] | [Package (Maven)][package] | [API reference documentation][api_documentation]
| [Product documentation][product_docs]

![Mixed Reality service authentication diagram](https://docs.microsoft.com/azure/spatial-anchors/concepts/media/spatial-anchors-authentication-overview.png)

## Getting started

### Prerequisites

- You must have an [Azure subscription](https://azure.microsoft.com/free/).
- You must have an account with an [Azure Mixed Reality service](https://azure.microsoft.com/topic/mixed-reality/):
  - [Azure Remote Rendering](https://docs.microsoft.com/azure/remote-rendering/)
  - [Azure Spatial Anchors](https://docs.microsoft.com/azure/spatial-anchors/)
- [Java Development Kit (JDK)](https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable) version 8 or above.
- [Apache Maven](https://maven.apache.org/download.cgi).
- Familiarity with the authentication and credential concepts from [Azure.Identity](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity).

### Include the package

[//]: # ({x-version-update-start;com.azure:azure-mixedreality-authentication;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-mixedreality-authentication</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Authenticate the client

Mixed Reality services support a few different forms of authentication:

- Account Key authentication
  - Account keys enable you to get started quickly with using Mixed Reality services. But before you deploy your application
    to production, we recommend that you update your app to use Azure AD authentication.
- Azure Active Directory (AD) token authentication
  - If you're building an enterprise application and your company is using Azure AD as its identity system, you can use
    user-based Azure AD authentication in your app. You then grant access to your Mixed Reality accounts by using your
    existing Azure AD security groups. You can also grant access directly to users in your organization.
  - Otherwise, we recommend that you obtain Azure AD tokens from a web service that supports your app. We recommend this
    method for production applications because it allows you to avoid embedding the credentials for access to a Mixed
    Reality service in your client application.

See [here](https://docs.microsoft.com/azure/spatial-anchors/concepts/authentication) for detailed instructions and information.

## Key concepts

### MixedRealityStsClient

The `MixedRealityStsClient` is the client library used to access the Mixed Reality STS to get an access token.

Tokens obtained from the Mixed Reality STS have a lifetime of **24 hours**.

## Examples

### Create the client

For a synchronous client:

```java
AzureKeyCredential keyCredential = new AzureKeyCredential(accountKey);
MixedRealityStsClient client = new MixedRealityStsClientBuilder()
    .accountDomain(accountDomain)
    .accountId(accountId)
    .credential(keyCredential)
    .buildClient();
```

For an asynchronous client (note the call to `buildAsyncClient` instead of `buildClient`):

```java
AzureKeyCredential keyCredential = new AzureKeyCredential(accountKey);
MixedRealityStsAsyncClient client = new MixedRealityStsClientBuilder()
    .accountDomain(accountDomain)
    .accountId(accountId)
    .credential(keyCredential)
    .buildAsyncClient();
```

### Retrieve an access token

```java
AzureKeyCredential keyCredential = new AzureKeyCredential(accountKey);
MixedRealityStsClient client = new MixedRealityStsClientBuilder()
    .accountDomain(accountDomain)
    .accountId(accountId)
    .credential(keyCredential)
    .buildClient();

AccessToken token = client.getToken();
```

See the authentication examples [above](#authenticate-the-client) for more complex authentication scenarios.

#### Using the access token in a Mixed Reality client library

Some Mixed Reality client libraries might accept an access token in place of a credential. For example:

```java
// getMixedRealityAccessTokenFromWebService is a hypothetical method that retrieves
// a Mixed Reality access token from a web service. The web service would use the
// MixedRealityStsClient and credentials to obtain an access token to be returned
// to the client.
AccessToken accessToken = getMixedRealityAccessTokenFromWebService();

SpatialAnchorsAccount account = new SpatialAnchorsAccount(accountId, accountDomain);
SpatialAnchorsClient client = new SpatialAnchorsClient(account, accessToken);
```

Note: The `SpatialAnchorsClient` usage above is hypothetical and may not reflect the actual library. Consult the
documentation for the client library you're using to determine if and how this might be supported.

## Troubleshooting

Describe common errors and exceptions, how to "unpack" them if necessary, and include guidance for graceful handling and recovery.

Provide information to help developers avoid throttling or other service-enforced errors they might encounter. For example, provide guidance and examples for using retry or connection policies in the API.

If the package or a related package supports it, include tips for logging or enabling instrumentation to help them debug their code.

## Next steps

### Client libraries supporting authentication with Mixed Reality Authentication

Libraries supporting the Mixed Reality Authentication are coming soon.

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License
Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution.
For details, visit [https://cla.microsoft.com](https://cla.microsoft.com).

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the
PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this
once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/).
For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact
[opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

<!-- LINKS -->
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[product_docs]: https://azure.microsoft.com/topic/mixed-reality/
[package]: https://search.maven.org/artifact/com.azure/azure-mixedreality-authentication
[api_documentation]: https://aka.ms/java-docs
[source]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/mixedreality/azure-mixedreality-authentication

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fmixedreality%2Fazure-mixedreality-authentication%2FREADME.png)
