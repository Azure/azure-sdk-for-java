# Azure Communication CallingServer Service client library for Java

This package contains a Java SDK for Azure Communication CallingServer Service. Server Test

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
    <version>1.0.0-beta.6</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Examples

To be Determined

## Troubleshooting

If you recieve a CommunicationErrorException with the messagae: "Action is invalid when call is not in Established state." This usually means the call has ended. This can occur if the participants all leave
the call, or participants did not accept the call before the call timed out. 

If you fail to start a call because of an HMAC validation error, be sure your access key is correct, and
that you are passing in a valid conversation id.

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a [Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

## Next steps

- [Read more about CallingServer Call Automation in Azure Communication Services][call_automation_apis_overview]
- [Read more about CallingServer Call Recording in Azure Communication Services][call_recording_overview]
- For a basic guide on how to record and download calls with Event Grid please refer to the [Record and download calls with Event Grid][record_and_download_calls_with_event_grid].

<!-- LINKS -->
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[product_docs]: https://docs.microsoft.com/azure/communication-services/
[package]: https://search.maven.org/artifact/com.azure/azure-communication-callingserver
[api_documentation]: https://aka.ms/java-docs
[call_automation_apis_overview]:https://docs.microsoft.com/azure/communication-services/concepts/voice-video-calling/call-automation-apis
[call_recording_overview]:https://docs.microsoft.com/azure/communication-services/concepts/voice-video-calling/call-recording
[record_and_download_calls_with_event_grid]:https://docs.microsoft.com/azure/communication-services/quickstarts/voice-video-calling/download-recording-file-sample
[source]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/communication/azure-communication-callingserver/src
