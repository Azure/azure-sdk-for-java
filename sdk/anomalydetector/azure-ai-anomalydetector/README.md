# Azure Anomaly Detector client library for Java

Microsoft Azure Cognitive Services Anomaly Detector API enables you to monitor and detect abnormalities in your time series data with machine learning.

[Source code][source_code] | Package (Maven) | [API reference documentation][api_reference_doc] | [Product Documentation][product_documentation] | [Samples][sample_readme]

## Getting started

### Prerequisites

- Java Development Kit (JDK) with version 8 or above
- [Azure Subscription][azure_subscription]
- An existing Cognitive Services or Anomaly Detector resource.

For more information about creating the resource or how to get the location and sku information see [here][cognitive_resource_cli].

### Include the Package

[//]: # ({x-version-update-start;com.azure:azure-ai-anomalydetector;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-ai-anomalydetector</artifactId>
  <version>3.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})


### Authenticate the client
In order to interact with the Anomaly Detector service, you'll need to create an instance of the [`AnomalyDetectorClient`][anomaly_detector_client_class] class.  You will need an **endpoint** and an **API key** to instantiate a client object.  

#### Get API Key

You can obtain the endpoint and API key from the resource information in the [Azure Portal][azure_portal].

Alternatively, you can use the [Azure CLI][azure_cli] snippet below to get the API key from the Anomaly Detector resource.

```PowerShell
az cognitiveservices account keys list --resource-group <your-resource-group-name> --name <your-resource-name>
```

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
[samples]: src/samples/java/com/azure/ai/anomalydetector
[source_code]: src
[samples_code]: src/samples/
[azure_subscription]: https://azure.microsoft.com/free/
[api_reference_doc]: https://aka.ms/azsdk/net/docs/ref/anomalydetector
[product_documentation]: https://docs.microsoft.com/en-us/azure/cognitive-services/anomaly-detector/
[cognitive_resource_cli]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account-cli
[anomaly_detector_client_class]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/anomalydetector/azure-ai-anomalydetector/src/main/java/com/azure/ai/anomalydetector/AnomalyDetectorClient.java
[azure_cli]: https://docs.microsoft.com/cli/azure
[azure_portal]: https://portal.azure.com
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/identity/azure-identity
[DefaultAzureCredential]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/identity/azure-identity/README.md#defaultazurecredential
[custom_subdomain]: https://docs.microsoft.com/azure/cognitive-services/authentication#create-a-resource-with-a-custom-subdomain
[register_aad_app]: https://docs.microsoft.com/azure/cognitive-services/authentication#assign-a-role-to-a-service-principal
[aad_grant_access]: https://docs.microsoft.com/azure/cognitive-services/authentication#assign-a-role-to-a-service-principal

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fanomalydetector%2Fazure-ai-anomalydetector%2FREADME.png)
