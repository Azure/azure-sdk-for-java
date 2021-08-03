# Azure Communication Service Common client library for Java

Azure Communication Common contains data structures commonly used for communicating with Azure Communication Services. 
It is intended to provide cross-cutting concerns, e.g. authentication. 

## Getting started

### Prerequisites

- An Azure account with an active subscription. [Create an account for free](https://azure.microsoft.com/free/?WT.mc_id=A261C142F).
- [Java Development Kit (JDK)](https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable) version 8 or above.
- [Apache Maven](https://maven.apache.org/download.cgi).
- A deployed Communication Services resource.

### Include the package

[//]: # ({x-version-update-start;com.azure:azure-communication-common;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-communication-common</artifactId>
    <version>1.0.3</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts

To work with Azure Communication Services, a resource access key is used for authentication. 

Azure Communication Service supports HMAC authentication with resource access key. To
apply HMAC authentication, construct CommunicationClientCredential with the access key and instantiate
a CommunicationIdentityClient to manage users and tokens.

### CommunicationTokenCredential

It is up to you the developer to first create valid user tokens with the Communication Identity SDK. Then you use these tokens with the `CommunicationTokenCredential`.

`CommunicationTokenCredential` authenticates a user with Communication Services, such as Chat or Calling. It optionally provides an auto-refresh mechanism to ensure a continuously stable authentication state during communications.

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a [Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

## Examples

In progress.

## Troubleshooting

In progress.

## Next steps

Check out other client libraries for Azure communication service

<!-- LINKS -->
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
