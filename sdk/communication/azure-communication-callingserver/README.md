# Azure Communication CallingServer Service client library for Java

This package contains a Java SDK for Azure Communication CallingServer Service.

[Source code][source] | [Package (Maven)][package] | [API reference documentation][api_documentation]
| [Product documentation][product_docs]

## Getting started

### Prerequisites

- An Azure account with an active subscription. [Create an account for free](https://azure.microsoft.com/free/?WT.mc_id=A261C142F).
- [Java Development Kit (JDK)](https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable) version 8 or above.
- [Apache Maven](https://maven.apache.org/download.cgi).
- A deployed Communication Services resource. You can use the [Azure Portal](https://docs.microsoft.com/azure/communication-services/quickstarts/create-communication-resource?tabs=windows&pivots=platform-azp) or the [Azure PowerShell](https://docs.microsoft.com/powershell/module/az.communication/new-azcommunicationservice) to set it up.

### Include the package

[//]: # ({x-version-update-start;com.azure:azure-communication-callingserver;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-communication-callingserver</artifactId>
    <version>1.0.0-preview.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts

At a high level the Azure Communication CallingServer API will support 2 kinds of scenarios:

- In-call app: Contoso server app is a participant in the call.  

- Out-call app: Contoso server app is not a participant in the call - Server app can subscribe to events for calls between specific users or even all users belonging to the ACS azure resource.  

Based on if the Contoso app join a call or not, APIs can be divided into two categories:   

- In-call APIs: Contoso app is one of the participant in a call. It can be applicable for app to person (A2P) or person to app (P2A) case, or multi-party/group calls that server apps joined as a participant to provide audio/prompt.  

- Out-of-call APIs: Contoso app can invoke these set of APIs without joining a call. It is applicable for actions on P2P calls, A2P calls, P2A calls and group calls.  

Once you initialized a `CallClient` class, you can do the following chat operations:

### Create, Join, Answer, Reject, Transfer, Hangup and Delete a call

### Start, Stop, Pause, Resume, and Get a recording

### Add, Delete, Mute, Unmute participants


## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a [Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

## Examples

In progress.

## Troubleshooting

In progress.

## Next steps

Check out other client libraries for Azure Communication Services

<!-- LINKS -->
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[product_docs]: https://docs.microsoft.com/azure/communication-services/
[package]: https://search.maven.org/artifact/com.azure/azure-communication-callingserver
[api_documentation]: https://aka.ms/java-docs
[source]: https://github.com/Azure/azure-sdk-for-java-pr/tree/master/sdk/communication/azure-communication-callingserver/src
