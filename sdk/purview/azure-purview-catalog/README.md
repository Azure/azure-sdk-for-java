# Azure Document Translation client library for Java

Azure Purview Catalog is a fully managed cloud service whose users can discover the data sources they need and understand the data sources they find. At the same time, Data Catalog helps organizations get more value from their existing investments.

- Search for data using technical or business terms
- Browse associated technical, business, semantic, and operational metadata
- Identify the sensitivity level of data.

**Please rely heavily on the [service's documentation][catalog_product_documentation] and our [Low-Level client docs][low_level_client] to use this library**

[Source code][source_code] | [Package (Maven)][package] | [API reference documentation][api_reference_doc] | [Product Documentation][product_documentation] | [Samples][samples_readme]

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
- [Azure Subscription][azure_subscription]
- An existing Azure Purview account.

For more information about creating the account see [here][create_azure_purview_account].

### Include the Package

[//]: # ({x-version-update-start;com.azure:azure-analytics-purview-catalog;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-analytics-purview-catalog</artifactId>
  <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})


### Authenticate the client
In order to interact with the Azure Purview service, your client must present an Azure Active Directory bearer token to the service.

The simplest way of providing a bearer token is to use the `DefaultAzureCredential` authentication method by providing client secret credentials is being used in this getting started section but you can find more ways to authenticate with [azure-identity][azure_identity].

#### Create AnomalyDetectorClient with Azure Active Directory Credential

You can authenticate with Azure Active Directory using the [Azure Identity library][azure_identity]. Note that regional endpoints do not support AAD authentication. Create a [custom subdomain][custom_subdomain] for your resource in order to use this type of authentication.

To use the [DefaultAzureCredential][DefaultAzureCredential] provider shown below, or other credential providers provided with the Azure SDK, please include the `azure-identity` package:

[//]: # ({x-version-update-start;com.azure:azure-identity;dependency})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
    <version>1.1.0</version>
</dependency>
```

You will also need to [register a new AAD application][register_aad_app] and [grant access][aad_grant_access] to Anomaly Detector by assigning the `"Cognitive Services User"` role to your service principal.

Set the values of the client ID, tenant ID, and client secret of the AAD application as environment variables: AZURE_CLIENT_ID, AZURE_TENANT_ID, AZURE_CLIENT_SECRET.

##### Async client
<!-- embedme ./src/samples/java/com/azure/ai/anomalydetector/ReadmeSamples.java#L29-L43 -->
```java
String endpoint = "<anomaly-detector-resource-endpoint>";
HttpHeaders headers = new HttpHeaders()
    .put("Accept", ContentType.APPLICATION_JSON);

String defaultScope = "https://cognitiveservices.azure.com/.default";
HttpPipelinePolicy authPolicy = new BearerTokenAuthenticationPolicy(new DefaultAzureCredentialBuilder().build(),
    defaultScope);
AddHeadersPolicy addHeadersPolicy = new AddHeadersPolicy(headers);

HttpPipeline httpPipeline = new HttpPipelineBuilder().httpClient(HttpClient.createDefault())
    .policies(authPolicy, addHeadersPolicy).build();
AnomalyDetectorAsyncClient anomalyDetectorAsyncClient = new AnomalyDetectorClientBuilder()
    .pipeline(httpPipeline)
    .endpoint(endpoint)
    .buildAsyncClient();
```

##### Sync client
<!-- embedme ./src/samples/java/com/azure/ai/anomalydetector/ReadmeSamples.java#L50-L64 -->
```java
String endpoint = "<anomaly-detector-resource-endpoint>";
HttpHeaders headers = new HttpHeaders()
    .put("Accept", ContentType.APPLICATION_JSON);

String defaultScope = "https://cognitiveservices.azure.com/.default";
HttpPipelinePolicy authPolicy = new BearerTokenAuthenticationPolicy(new DefaultAzureCredentialBuilder().build(),
    defaultScope);
AddHeadersPolicy addHeadersPolicy = new AddHeadersPolicy(headers);

HttpPipeline httpPipeline = new HttpPipelineBuilder().httpClient(HttpClient.createDefault())
    .policies(authPolicy, addHeadersPolicy).build();
AnomalyDetectorClient anomalyDetectorClient = new AnomalyDetectorClientBuilder()
    .pipeline(httpPipeline)
    .endpoint(endpoint)
    .buildClient();
```

## Key concepts

## Examples
More examples can be found in [samples][samples_code].

## Troubleshooting

### Enabling Logging

Azure SDKs for Java offer a consistent logging story to help aid in troubleshooting application errors and expedite
their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help
locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

## Next steps

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a [Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

<!-- LINKS -->
[samples]: src/samples/java/com/azure/ai/translator
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/translatortext/azure-ai-translator/src
[samples_code]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/translatortext/azure-ai-translator/src/samples/
[azure_subscription]: https://azure.microsoft.com/free/
[api_reference_doc]: https://aka.ms/azsdk/net/docs/ref/translator
[product_documentation]: https://docs.microsoft.com/en-us/azure/cognitive-services/translator/
[cognitive_resource_cli]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account-cli
[batch_document_translation_client_class]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/translatortext/azure-ai-translator/src/main/java/com/azure/ai/translator/BatchDocumentTranslationClient.java
[azure_cli]: https://docs.microsoft.com/cli/azure
[azure_portal]: https://portal.azure.com
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/identity/azure-identity
[DefaultAzureCredential]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/identity/azure-identity/README.md#defaultazurecredential
[custom_subdomain]: https://docs.microsoft.com/azure/cognitive-services/authentication#create-a-resource-with-a-custom-subdomain
[register_aad_app]: https://docs.microsoft.com/azure/cognitive-services/authentication#assign-a-role-to-a-service-principal
[aad_grant_access]: https://docs.microsoft.com/azure/cognitive-services/authentication#assign-a-role-to-a-service-principal
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[package]: https://mvnrepository.com/artifact/com.azure/azure-ai-anomalydetector
[samples_readme]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/translatortext/azure-ai-translator/src/samples/README.md
[low_level_client]: https://github.com/Azure/azure-sdk-for-java/wiki/Low-Level-Client

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fanomalydetector%2Fazure-ai-anomalydetector%2FREADME.png)
