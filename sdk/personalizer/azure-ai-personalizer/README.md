# Azure Personalizer client library for Java

[Azure Personalizer](https://docs.microsoft.com/azure/cognitive-services/personalizer/)
is a cloud-based service that helps your applications choose the best content item to show your users. You can use the Personalizer service to determine what product to suggest to shoppers or to figure out the optimal position for an advertisement. After the content is shown to the user, your application monitors the user's reaction and reports a reward score back to the Personalizer service. This ensures continuous improvement of the machine learning model, and Personalizer's ability to select the best content item based on the contextual information it receives.

## Getting started

### Prerequisites
- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
- [Azure Subscription][azure_subscription]
- [Cognitive Services for Personalizer account][personalizer_account] to use this package.

### Install the package
Include the dependency in the dependencies' section. Ignoring the version tag without the version tag.

```xml
<dependencies>
  <dependency>
      <groupId>com.azure</groupId>
      <artifactId>azure-ai-personalizer</artifactId>
      <version>1.0.0-beta.1</version> <!-- {{x-version-update-start;com.azure:azure-ai-personalizer;current} -->
  </dependency>
</dependencies>
```

This table shows the relationship between SDK versions and supported API versions of the service:

|SDK version|Supported API version of service
|-|-
|1.0.0-beta.1 | 1.1-preview.3

## Key concepts
The [PersonalizerAdministrationClient][personalizer_admin_sync_client] and
[PersonalizerAdministrationAsyncClient][personalizer_admin_async_client] provide both synchronous and asynchronous operations to:
- Manage the machine learning model and learning settings for the Personalizer service.
- Manage the properties of the Personalizer service such as the [learning mode][learning_mode], [exploration percentage][exploration].
- Run counterfactual evaluations on prior historical event data.

The [PersonalizerClient][personalizer_sync_client] and
[PersonalizerAsyncClient][personalizer_async_client] provide both synchronous and asynchronous operations to:
- Rank a set of actions, then activate and reward the event. 
- Use [multi-slot personalization][multi_slot] when there are more than one slots.
- Manage the properties of the Personalizer service.
- Run counterfactual evaluations on prior historical event data.

## Examples
Please refer the sample scenarios outlined in the [examples][examples].

## Troubleshooting
Azure SDKs for Java offer a consistent logging story to help aid in troubleshooting application errors and expedite
their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help
locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

## Next steps

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a [Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information, see the [Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

<!-- LINKS -->
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[azure_subscription]: https://azure.microsoft.com/free
[personalizer_account]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account?tabs=multiservice%2Cwindows
[personalizer_admin_sync_client]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/personalizer/azure-ai-personalizer/src/main/java/com/azure/ai/personalizer/administration/PersonalizerAdministrationClient.java
[personalizer_admin_async_client]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/personalizer/azure-ai-personalizer/src/main/java/com/azure/ai/personalizer/administration/PersonalizerAdministrationAsyncClient.java
[personalizer_sync_client]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/personalizer/azure-ai-personalizer/src/main/java/com/azure/ai/personalizer/PersonalizerClient.java
[personalizer_async_client]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/personalizer/azure-ai-personalizer/src/main/java/com/azure/ai/personalizer/PersonalizerAsyncClient.java
[learning_mode]: https://docs.microsoft.com/azure/cognitive-services/personalizer/what-is-personalizer#learning-modes
[exploration]: https://docs.microsoft.com/azure/cognitive-services/personalizer/concepts-exploration
[multi_slot]: https://docs.microsoft.com/azure/cognitive-services/personalizer/concept-multi-slot-personalization
[examples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/personalizer/azure-ai-personalizer/src/samples#examples
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-in-Azure-SDK
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fpersonalizer%2Fazure-ai-personalizer%2FREADME.png)
