# Azure Anomaly Detector client library for Java

[Anomaly Detector](https://learn.microsoft.com/azure/cognitive-services/Anomaly-Detector/overview) is an AI service with a set of APIs, which enables you to monitor and detect anomalies in your time series data with little machine learning (ML) knowledge, either batch validation or real-time inference.

[Source code][source_code] | [Package (Maven)][package] | [API reference documentation][api_reference_doc] | [Product Documentation][product_documentation] | [Samples][samples_readme]

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 11 or later.
- [Azure Subscription][azure_subscription]
- An existing Cognitive Services or Anomaly Detector resource.

For more information about creating the resource or how to get the location and sku information see 
[here][cognitive_resource_cli].

### Include the Package

[//]: # ({x-version-update-start;com.azure:azure-ai-anomalydetector;current})

```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-ai-anomalydetector</artifactId>
  <version>3.0.0-beta.5</version>
</dependency>
```

[//]: # ({x-version-update-end})

### Authenticate the client

In order to interact with the Anomaly Detector service, you'll need to create an instance of 
[`MultivariateClient`][multivariate_client_class], [`MultivariateAsyncClient`][multivariate_async_client_class],
[`UnivariateClient`][univariate_client_class], or [`UnivariateAsyncClient`][univariate_async_client_class]. You will
need an **endpoint** and an **API key** to instantiate a client object.  

#### Get API Key

You can obtain the endpoint and API key from the resource information in the [Azure Portal][azure_portal].

Alternatively, you can use the [Azure CLI][azure_cli] snippet below to get the API key from the Anomaly Detector resource.

```bash
az cognitiveservices account keys list --resource-group <your-resource-group-name> --name <your-resource-name>
```

#### Create AnomalyDetectorClient with Azure Active Directory Credential

You can authenticate with Azure Active Directory using the [Azure Identity library][azure_identity]. Note that regional
endpoints do not support AAD authentication. Create a [custom subdomain][custom_subdomain] for your resource in order
to use this type of authentication.

To use the [DefaultAzureCredential][DefaultAzureCredential] provider shown below, or other credential providers provided
with the Azure SDK, please include the `azure-identity` package:

[//]: # ({x-version-update-start;com.azure:azure-identity;dependency})

```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
    <version>1.13.3</version>
</dependency>
```

You will also need to [register a new AAD application][register_aad_app] and [grant access][aad_grant_access] to 
Anomaly Detector by assigning the `"Cognitive Services User"` role to your service principal.

Set the values of the client ID, tenant ID, and client secret of the AAD application as environment variables: 
AZURE_CLIENT_ID, AZURE_TENANT_ID, AZURE_CLIENT_SECRET.

##### Sync client

```java readme-sample-createAnomalyDetectorClient
String endpoint = Configuration.getGlobalConfiguration().get("AZURE_ANOMALY_DETECTOR_ENDPOINT");
String key = Configuration.getGlobalConfiguration().get("AZURE_ANOMALY_DETECTOR_API_KEY");

MultivariateClient multivariateClient = new AnomalyDetectorClientBuilder()
    .credential(new AzureKeyCredential(key))
    .endpoint(endpoint)
    .buildMultivariateClient();

UnivariateClient univariateClient = new AnomalyDetectorClientBuilder()
    .credential(new AzureKeyCredential(key))
    .endpoint(endpoint)
    .buildUnivariateClient();
```

## Key concepts

With the Anomaly Detector, you can either detect anomalies in one variable using **Univariate Anomaly Detection**, or 
detect anomalies in multiple variables with **Multivariate Anomaly Detection**.

|Feature  |Description  |
|---------|---------|
|Univariate Anomaly Detection | Detect anomalies in one variable, like revenue, cost, etc. The model was selected automatically based on your data pattern. |
|Multivariate Anomaly Detection| Detect anomalies in multiple variables with correlations, which are usually gathered from equipment or other complex system. The underlying model used is Graph attention network.|

### Univariate Anomaly Detection

The Univariate Anomaly Detection API enables you to monitor and detect abnormalities in your time series data without 
having to know machine learning. The algorithms adapt by automatically identifying and applying the best-fitting models
to your data, regardless of industry, scenario, or data volume. Using your time series data, the API determines
boundaries for anomaly detection, expected values, and which data points are anomalies.

Using the Anomaly Detector doesn't require any prior experience in machine learning, and the REST API enables you to
easily integrate the service into your applications and processes.

With the Univariate Anomaly Detection, you can automatically detect anomalies throughout your time series data, or as
they occur in real-time.

|Feature  |Description  |
|---------|---------|
| Streaming detection| Detect anomalies in your streaming data by using previously seen data points to determine if your latest one is an anomaly. This operation generates a model using the data points you send, and determines if the target point is an anomaly. By calling the API with each new data point you generate, you can monitor your data as it's created. |
| Batch detection | Use your time series to detect any anomalies that might exist throughout your data. This operation generates a model using your entire time series data, with each point analyzed with the same model.         |
| Change points detection | Use your time series to detect any trend change points that exist in your data. This operation generates a model using your entire time series data, with each point analyzed with the same model.    |

### Multivariate Anomaly Detection

The **Multivariate Anomaly Detection** APIs further enable developers by easily integrating advanced AI for detecting
anomalies from groups of metrics, without the need for machine learning knowledge or labeled data. Dependencies and 
inter-correlations between up to 300 different signals are now automatically counted as key factors. This new 
capability helps you to proactively protect your complex systems such as software applications, servers, factory 
machines, spacecraft, or even your business, from failures.

With the Multivariate Anomaly Detection, you can automatically detect anomalies throughout your time series data, or as
they occur in real-time. There are three processes to use Multivariate Anomaly Detection.

- **Training**: Use Train Model API to create and train a model, then use Get Model Status API to get the status and model metadata.
- **Inference**:
  - Use Async Inference API to trigger an asynchronous inference process and use Get Inference results API to get detection results on a batch of data.
  - You could also use Sync Inference API to trigger a detection on one timestamp every time.
- **Other operations**: List Model API and Delete Model API are supported in Multivariate Anomaly Detection model for model management.

### Thread safety

We guarantee that all client instance methods are thread-safe and independent of each other. This
ensures that the recommendation of reusing client instances is always safe, even across threads.

## Examples

The following section provides several code snippets covering some of the most common Anomaly Detector service tasks,
including:

- [Univariate Anomaly Detection - Batch detection](#batch-detection)
- [Univariate Anomaly Detection - Streaming detection](#streaming-detection)
- [Univariate Anomaly Detection - Detect change points](#detect-change-points)
- [Multivariate Anomaly Detection](#multivariate-anomaly-detection-sample)

### Create client

```java readme-sample-createAnomalyDetectorClient
String endpoint = Configuration.getGlobalConfiguration().get("AZURE_ANOMALY_DETECTOR_ENDPOINT");
String key = Configuration.getGlobalConfiguration().get("AZURE_ANOMALY_DETECTOR_API_KEY");

MultivariateClient multivariateClient = new AnomalyDetectorClientBuilder()
    .credential(new AzureKeyCredential(key))
    .endpoint(endpoint)
    .buildMultivariateClient();

UnivariateClient univariateClient = new AnomalyDetectorClientBuilder()
    .credential(new AzureKeyCredential(key))
    .endpoint(endpoint)
    .buildUnivariateClient();
```

### Batch detection

For batch detection in univariate anomaly detection, please go to this sample for better understanding the workflow:
[DetectAnomaliesEntireSeries.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/anomalydetector/azure-ai-anomalydetector/src/samples/java/com/azure/ai/anomalydetector/DetectAnomaliesEntireSeries.java)

### Streaming detection

For streaming/last detection in univariate anomaly detection, please go to this sample for better understanding the 
workflow: [DetectAnomaliesLastPoint.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/anomalydetector/azure-ai-anomalydetector/src/samples/java/com/azure/ai/anomalydetector/DetectAnomaliesLastPoint.java)

### Detect change points

For change points detection in univariate anomaly detection, please go to this sample for better understanding the
workflow: [DetectChangePoints.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/anomalydetector/azure-ai-anomalydetector/src/samples/java/com/azure/ai/anomalydetector/DetectChangePoints.java)

### Multivariate Anomaly Detection Sample

To see how to use Anomaly Detector library to conduct Multivariate Anomaly Detection, see this [MultivariateSample.java](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/anomalydetector/azure-ai-anomalydetector/src/samples/java/com/azure/ai/anomalydetector/MultivariateSample.java).

## Troubleshooting

### Enabling Logging

Azure SDKs for Java offer a consistent logging story to help aid in troubleshooting application errors and expedite
their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help
locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

## Next steps

These code samples show common scenario operations with the Azure Anomaly Detector library. More samples can be found
under the [samples](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/anomalydetector/azure-ai-anomalydetector/src/samples/java/com/azure/ai/anomalydetector) directory.

For more extensive documentation on Azure Anomaly Detector, see the [Anomaly Detector documentation](https://learn.microsoft.com/azure/cognitive-services/anomaly-detector/overview) on
docs.microsoft.com.

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License
Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. 
For details, visit [cla.microsoft.com][cla].

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate
the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to 
do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][code_of_conduct]. For more information see the 
[Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

<!-- LINKS -->
[cla]: https://cla.microsoft.com
[code_of_conduct]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[samples]: src/samples/java/com/azure/ai/anomalydetector
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/anomalydetector/azure-ai-anomalydetector/src
[samples_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/anomalydetector/azure-ai-anomalydetector/src/samples/
[azure_subscription]: https://azure.microsoft.com/free/
[api_reference_doc]: https://azure.github.io/azure-sdk-for-java/
[product_documentation]: https://docs.microsoft.com/azure/cognitive-services/anomaly-detector/
[cognitive_resource_cli]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account-cli
[multivariate_client_class]: https://github.com/Azure/azure-sdk-for-java/blob/8b2622353dd8c4a03a87c7e04a6f82bf0712bab5/sdk/anomalydetector/azure-ai-anomalydetector/src/main/java/com/azure/ai/anomalydetector/MultivariateClient.java
[multivariate_async_client_class]: https://github.com/Azure/azure-sdk-for-java/blob/8b2622353dd8c4a03a87c7e04a6f82bf0712bab5/sdk/anomalydetector/azure-ai-anomalydetector/src/main/java/com/azure/ai/anomalydetector/MultivariateAsyncClient.java
[univariate_client_class]: https://github.com/Azure/azure-sdk-for-java/blob/8b2622353dd8c4a03a87c7e04a6f82bf0712bab5/sdk/anomalydetector/azure-ai-anomalydetector/src/main/java/com/azure/ai/anomalydetector/UnivariateClient.java
[univariate_async_client_class]: https://github.com/Azure/azure-sdk-for-java/blob/8b2622353dd8c4a03a87c7e04a6f82bf0712bab5/sdk/anomalydetector/azure-ai-anomalydetector/src/main/java/com/azure/ai/anomalydetector/UnivariateAsyncClient.java
[azure_cli]: https://docs.microsoft.com/cli/azure
[azure_portal]: https://portal.azure.com
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity
[DefaultAzureCredential]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md#defaultazurecredential
[custom_subdomain]: https://docs.microsoft.com/azure/cognitive-services/authentication#create-a-resource-with-a-custom-subdomain
[register_aad_app]: https://docs.microsoft.com/azure/cognitive-services/authentication#assign-a-role-to-a-service-principal
[aad_grant_access]: https://docs.microsoft.com/azure/cognitive-services/authentication#assign-a-role-to-a-service-principal
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[package]: https://central.sonatype.com/artifact/com.azure/azure-ai-anomalydetector
[samples_readme]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/anomalydetector/azure-ai-anomalydetector/src/samples/README.md
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-in-Azure-SDK
![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fanomalydetector%2Fazure-ai-anomalydetector%2FREADME.png)
