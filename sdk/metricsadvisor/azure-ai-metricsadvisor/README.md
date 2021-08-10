# Azure Metrics Advisor client library for Java
Azure Metrics Advisor is a new Cognitive  Service that uses time series based decision AI to identify and assist
trouble shooting the incidents of online services, and monitor the business health by automating the slice and dice
of business dataFeedMetrics.

[Source code][source_code] | [Package (Maven)][mvn_package] | [API reference documentation][api_reference_doc] | [Product Documentation][product_documentation] | [Samples][samples]

## Getting started

### Prerequisites
- [Java Development Kit (JDK)][jdk_link] version 8 or later
- [Azure Subscription][azure_subscription]
- [Cognitive Services or Metrics Advisor account][metrics_advisor_account] to use this package.

### Include the Package
**Note:** This version targets Azure Metrics Advisor service API version v1.0.

[//]: # ({x-version-update-start;com.azure:azure-ai-metricsadvisor;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-ai-metricsadvisor</artifactId>
    <version>1.0.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

#### Create a Metrics Advisor resource

### Authenticate the client
In order to interact with the Metrics Advisor service, you will need to create an instance of the Metrics Advisor client.
Both the asynchronous and synchronous clients can be created by using `MetricsAdvisorClientBuilder`. Invoking `buildClient()`
will create the synchronous client, while invoking `buildAsyncClient` will create its asynchronous counterpart.

##### Looking up the endpoint
You can find the **endpoint** for your Metric Advisor resource in the [Azure Portal][azure_portal],
or [Azure CLI][azure_cli_endpoint].
```bash
# Get the endpoint for the resource
az cognitiveservices account show --name "resource-name" --resource-group "resource-group-name" --query "endpoint"
```

#### Create a MetricsAdvisor client using MetricsAdvisorKeyCredential
You will need two keys to authenticate the client:

- The subscription key to your Metrics Advisor resource. You can find this in the Keys and Endpoint section of your resource in the Azure portal.
- The API key for your Metrics Advisor instance. You can find this on the web portal for Metrics Advisor, in API keys on the left navigation menu.

Once you have the two keys and endpoint, you can use the `MetricsAdvisorKeyCredential` class to authenticate the clients as follows:

#### Create a Metrics Advisor client using MetricsAdvisorKeyCredential
<!-- embedme ./src/samples/java/com/azure/ai/metricsadvisor/ReadmeSamples.java#L65-L69-->
```java
MetricsAdvisorKeyCredential credential = new MetricsAdvisorKeyCredential("subscription_key", "api_key");
MetricsAdvisorClient metricsAdvisorClient = new MetricsAdvisorClientBuilder()
    .endpoint("{endpoint}")
    .credential(credential)
    .buildClient();
```

#### Create a Metrics Administration client using MetricsAdvisorKeyCredential
<!-- embedme ./src/samples/java/com/azure/ai/metricsadvisor/ReadmeSamples.java#L76-L81 -->
```java
MetricsAdvisorKeyCredential credential = new MetricsAdvisorKeyCredential("subscription_key", "api_key");
MetricsAdvisorAdministrationClient metricsAdvisorAdminClient =
    new MetricsAdvisorAdministrationClientBuilder()
        .endpoint("{endpoint}")
        .credential(credential)
        .buildClient();
```

#### Create a MetricsAdvisor client using Azure Service Directory
Azure SDK for Java supports an Azure Identity package, making it easy to get credentials from Microsoft identity
platform.

Authentication with AAD requires some initial setup:
* Add the Azure Identity package

[//]: # ({x-version-update-start;com.azure:azure-identity;dependency})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
    <version>1.3.5</version>
</dependency>
```
[//]: # ({x-version-update-end})
* [Register a new Azure Active Directory application][register_AAD_application]
* [Grant access][grant_access] to Metrics Advisor by assigning the `"Cognitive Services User"` role to your service principal.

After the setup, you can choose which type of [credential][azure_identity_credential_type] from azure.identity to use.
As an example, [DefaultAzureCredential][wiki_identity] can be used to authenticate the client:
Set the values of the client ID, tenant ID, and client secret of the AAD application as environment variables:
AZURE_CLIENT_ID, AZURE_TENANT_ID, AZURE_CLIENT_SECRET.

Authorization is easiest using [DefaultAzureCredential][wiki_identity]. It finds the best credential to use in its
running environment. For more information about using Azure Active Directory authorization with Metrics Advisor, please
refer to [the associated documentation][aad_authorization].
#### Create a Metrics Advisor client using AAD authentication
<!-- embedme ./src/samples/java/com/azure/ai/metricsadvisor/ReadmeSamples.java#L88-L92 -->
```java
TokenCredential credential = new DefaultAzureCredentialBuilder().build();
MetricsAdvisorClient metricsAdvisorClient = new MetricsAdvisorClientBuilder()
    .endpoint("{endpoint}")
    .credential(credential)
    .buildClient();
```

#### Create a Metrics Administration client using AAD authentication
<!-- embedme ./src/samples/java/com/azure/ai/metricsadvisor/ReadmeSamples.java#L99-L104 -->
```java
TokenCredential credential = new DefaultAzureCredentialBuilder().build();
MetricsAdvisorAdministrationClient metricsAdvisorAdminClient =
    new MetricsAdvisorAdministrationClientBuilder()
        .endpoint("{endpoint}")
        .credential(credential)
        .buildClient();
```

## Key concepts
### MetricsAdvisorClient
`MetricsAdvisorClient` helps with:

- Diagnose anomalies and incidents and help with root cause analysis of incidents.
- Retrieve original time series data and time series data enriched by the service.
- Send real time alerts through multiple notification hooks.
- Adjust anomaly/incident detection using feedback to tune your model.

### MetricsAdvisorAdministrationClient
`MetricsAdvisorAdministrationClient` allows you to

- Manage data feeds
- List available metrics and their detection configurations
- Fine tune anomaly detection configurations
- Configure anomaly alerting configurations
- Manage notification hooks

### Data feed
A data feed is what Metrics Advisor ingests from the user-specified data source such as Cosmos structure stream, SQL query result, and so on.
It contains rows of timestamps, zero or more dimensions, one or more Metrics. Therefore, multiple metrics could share the same data source and even the same data feed.

### Data Feed Metric
A metric is a quantifiable measure that is used to track and assess the status of a specific business process. It can be a combination of multiple time series values divided by dimensions, for example user count for a web vertical and en-us market.

### Data Feed Dimension
A dimension is one or more categorical values of the provided data feed. The combination of those values identifies a particular univariate time series, for example: country, language, tenant, and so on.

### Metric series
Metric series is a series of data points indexed (or listed or graphed) in time order. Most commonly, a time series is a sequence taken at successive equally spaced points in time. Therefore, it is a sequence of discrete-time data.

### Anomaly Detection Configuration
An anomaly detection configuration is a configuration supplied for a time series to identify if the data point is detected as an Anomaly. 
A metric can apply one or more detecting configurations. While a default detection configuration is automatically applied to each metric (named "Default"),
we can tune the detection modes used on our data by creating a customized anomaly detection configuration.

### Anomaly Incident
Incidents are generated for series when it has an anomaly depending on the applied Anomaly detection configurations.
Metrics Advisor service groups series of anomalies within a metric into an incident.

### Anomaly Alert
Anomaly Alerts can be configured to be triggered when certain anomalies are met. You can set multiple alerts with different settings. For example, you could create an anomalyAlert for anomalies with lower business impact, and another for more important alerts.

### Notification Hook
A notification hook is the entry point that allows the users to subscribe to real-time alerts. These alerts are sent over the internet, using a Hook.

## Examples

* [Add a data feed from a sample or data source](#add-a-data-feed-from-a-sample-or-data-source "Add a data feed from a sample or data source")
* [Check ingestion status](#check-ingestion-status "Check ingestion status")
* [Configure anomaly detection configuration](#configure-anomaly-detection-configuration "Configure anomaly detection configuration")
* [Add hooks for receiving anomaly alerts](#add-hooks-for-receiving-anomaly-alerts "Add hooks for receiving anomaly alerts")
* [Configure an anomaly alert configuration](#configure-an-anomaly-alert-configuration "Configure an anomaly alert configuration")
* [Query anomaly detection results](#query-anomaly-detection-results "Query anomaly detection results")

#### Add a data feed from a sample or data source
This example ingests the user specified `SQLServerDataFeedSource` data feed source data to the service.
<!-- embedme ./src/samples/java/com/azure/ai/metricsadvisor/ReadmeSamples.java#L111-L148 -->
```java
DataFeed dataFeed = new DataFeed()
    .setName("dataFeedName")
    .setSource(new MySqlDataFeedSource("conn-string", "query"))
    .setGranularity(new DataFeedGranularity().setGranularityType(DataFeedGranularityType.DAILY))
    .setSchema(new DataFeedSchema(
        Arrays.asList(
            new DataFeedMetric("cost"),
            new DataFeedMetric("revenue")
        )).setDimensions(
        Arrays.asList(
            new DataFeedDimension("city"),
            new DataFeedDimension("category")
        ))
    )
    .setIngestionSettings(new DataFeedIngestionSettings(OffsetDateTime.parse("2020-01-01T00:00:00Z")))
    .setOptions(new DataFeedOptions()
        .setDescription("data feed description")
        .setRollupSettings(new DataFeedRollupSettings()
            .setRollupType(DataFeedRollupType.AUTO_ROLLUP)));
final DataFeed createdSqlDataFeed = metricsAdvisorAdminClient.createDataFeed(dataFeed);

System.out.printf("Data feed Id : %s%n", createdSqlDataFeed.getId());
System.out.printf("Data feed name : %s%n", createdSqlDataFeed.getName());
System.out.printf("Is the query user is one of data feed administrator : %s%n", createdSqlDataFeed.isAdmin());
System.out.printf("Data feed created time : %s%n", createdSqlDataFeed.getCreatedTime());
System.out.printf("Data feed granularity type : %s%n",
    createdSqlDataFeed.getGranularity().getGranularityType());
System.out.printf("Data feed granularity value : %d%n",
    createdSqlDataFeed.getGranularity().getCustomGranularityValue());
System.out.println("Data feed related metric Ids:");
dataFeed.getMetricIds().forEach((metricId, metricName)
    -> System.out.printf("Metric Id : %s, Metric Name: %s%n", metricId, metricName));
System.out.printf("Data feed source type: %s%n", createdSqlDataFeed.getSourceType());

if (SQL_SERVER_DB == createdSqlDataFeed.getSourceType()) {
    System.out.printf("Data feed sql server query: %s%n",
        ((SqlServerDataFeedSource) createdSqlDataFeed.getSource()).getQuery());
}
```
#### Check ingestion status
This example checks the ingestion status of a previously provided data feed source.
<!-- embedme ./src/samples/java/com/azure/ai/metricsadvisor/ReadmeSamples.java#L155-L166 -->
```java
String dataFeedId = "3d48er30-6e6e-4391-b78f-b00dfee1e6f5";

metricsAdvisorAdminClient.listDataFeedIngestionStatus(
    dataFeedId,
    new ListDataFeedIngestionOptions(
        OffsetDateTime.parse("2020-01-01T00:00:00Z"),
        OffsetDateTime.parse("2020-09-09T00:00:00Z"))
).forEach(dataFeedIngestionStatus -> {
    System.out.printf("Message : %s%n", dataFeedIngestionStatus.getMessage());
    System.out.printf("Timestamp value : %s%n", dataFeedIngestionStatus.getTimestamp());
    System.out.printf("Status : %s%n", dataFeedIngestionStatus.getStatus());
});
```

#### Configure anomaly detection configuration
This example demonstrates how a user can configure an anomaly detection configuration for their data.
<!-- embedme ./src/samples/java/com/azure/ai/metricsadvisor/ReadmeSamples.java#L173-L203 -->
```java
String metricId = "3d48er30-6e6e-4391-b78f-b00dfee1e6f5";

ChangeThresholdCondition changeThresholdCondition = new ChangeThresholdCondition(
        20, 
        10, 
        true, 
        AnomalyDetectorDirection.BOTH, 
        new SuppressCondition(1, 2));

HardThresholdCondition hardThresholdCondition = new HardThresholdCondition(
        AnomalyDetectorDirection.DOWN, 
        new SuppressCondition(1, 1))
    .setLowerBound(5.0);

SmartDetectionCondition smartDetectionCondition = new SmartDetectionCondition(
        10.0, 
        AnomalyDetectorDirection.UP,
        new SuppressCondition(1, 2));

final AnomalyDetectionConfiguration anomalyDetectionConfiguration =
    metricsAdvisorAdminClient.createMetricAnomalyDetectionConfig(
        metricId,
        new AnomalyDetectionConfiguration("My dataPoint anomaly detection configuration")
            .setDescription("anomaly detection config description")
            .setWholeSeriesDetectionCondition(
                new MetricWholeSeriesDetectionCondition()
                    .setChangeThresholdCondition(changeThresholdCondition)
                    .setHardThresholdCondition(hardThresholdCondition)
                    .setSmartDetectionCondition(smartDetectionCondition)
                    .setConditionOperator(DetectionConditionOperator.OR))
    );
```

### Add hooks for receiving anomaly alerts
This example creates an email hook that receives anomaly incident alerts.
<!-- embedme ./src/samples/java/com/azure/ai/metricsadvisor/ReadmeSamples.java#L210-L222 -->
```java
NotificationHook emailNotificationHook = new EmailNotificationHook("email Hook")
    .setDescription("my email Hook")
    .setEmailsToAlert(new ArrayList<String>() {{ add("alertme@alertme.com"); }})
    .setExternalLink("https://adwiki.azurewebsites.net/articles/howto/alerts/create-hooks.html");

final NotificationHook notificationHook = metricsAdvisorAdminClient.createHook(emailNotificationHook);
EmailNotificationHook createdEmailHook = (EmailNotificationHook) notificationHook;
System.out.printf("Email Hook Id: %s%n", createdEmailHook.getId());
System.out.printf("Email Hook name: %s%n", createdEmailHook.getName());
System.out.printf("Email Hook description: %s%n", createdEmailHook.getDescription());
System.out.printf("Email Hook external Link: %s%n", createdEmailHook.getExternalLink());
System.out.printf("Email Hook emails to alert: %s%n",
    String.join(",", createdEmailHook.getEmailsToAlert()));
```

#### Configure an anomaly alert configuration
This example demonstrates how a user can configure an alerting configuration for detected anomalies in their data.
<!-- embedme ./src/samples/java/com/azure/ai/metricsadvisor/ReadmeSamples.java#L229-L249 -->
```java
String detectionConfigurationId1 = "9ol48er30-6e6e-4391-b78f-b00dfee1e6f5";
String detectionConfigurationId2 = "3e58er30-6e6e-4391-b78f-b00dfee1e6f5";
String hookId1 = "5f48er30-6e6e-4391-b78f-b00dfee1e6f5";
String hookId2 = "8i48er30-6e6e-4391-b78f-b00dfee1e6f5";

final AnomalyAlertConfiguration anomalyAlertConfiguration
    = metricsAdvisorAdminClient.createAlertConfig(
        new AnomalyAlertConfiguration("My anomaly alert config name")
            .setDescription("alert config description")
            .setMetricAlertConfigurations(
                Arrays.asList(
                    new MetricAlertConfiguration(detectionConfigurationId1,
                        MetricAnomalyAlertScope.forWholeSeries()),
                    new MetricAlertConfiguration(detectionConfigurationId2,
                        MetricAnomalyAlertScope.forWholeSeries())
                        .setAlertConditions(new MetricAnomalyAlertConditions()
                            .setSeverityRangeCondition(new SeverityCondition()
                                .setMaxAlertSeverity(AnomalySeverity.HIGH)))
                ))
            .setCrossMetricsOperator(MetricAnomalyAlertConfigurationsOperator.AND)
            .setIdOfHooksToAlert(Arrays.asList(hookId1, hookId2)));
```
#### Query anomaly detection results
This example demonstrates how a user can query alerts triggered for an anomaly detection configuration and get anomalies for that anomalyAlert.
<!-- embedme ./src/samples/java/com/azure/ai/metricsadvisor/ReadmeSamples.java#L256-L276 -->
```java
String alertConfigurationId = "9ol48er30-6e6e-4391-b78f-b00dfee1e6f5";
final OffsetDateTime startTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");
final OffsetDateTime endTime = OffsetDateTime.parse("2020-09-09T00:00:00Z");
metricsAdvisorClient.listAlerts(
    alertConfigurationId,
        startTime, endTime)
    .forEach(alert -> {
        System.out.printf("AnomalyAlert Id: %s%n", alert.getId());
        System.out.printf("AnomalyAlert created on: %s%n", alert.getCreatedTime());

        // List anomalies for returned alerts
        metricsAdvisorClient.listAnomalies(
            alertConfigurationId,
            alert.getId())
            .forEach(anomaly -> {
                System.out.printf("DataPoint Anomaly was created on: %s%n", anomaly.getCreatedTime());
                System.out.printf("DataPoint Anomaly severity: %s%n", anomaly.getSeverity().toString());
                System.out.printf("DataPoint Anomaly status: %s%n", anomaly.getStatus());
                System.out.printf("DataPoint Anomaly related series key: %s%n", anomaly.getSeriesKey().asMap());
            });
    });
```

## Troubleshooting
### General
Metrics Advisor clients raises `HttpResponseException` [exceptions][http_response_exception]. For example, if you try
to provide a non existing feedback Id an `HttpResponseException` would be raised with an error indicating the failure cause.
In the following code snippet, the error is handled
gracefully by catching the exception and display the additional information about the error.
<!-- embedme ./src/samples/java/com/azure/ai/metricsadvisor/ReadmeSamples.java#L283-L287 -->
```java
try {
    metricsAdvisorClient.getFeedback("non_existing_feedback_id");
} catch (HttpResponseException e) {
    System.out.println(e.getMessage());
}
```

### Enable client logging
Azure SDKs for Java offer a consistent logging story to help aid in troubleshooting application errors and expedite
their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help
locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

### Default HTTP Client
All client libraries by default use the Netty HTTP client. Adding the above dependency will automatically configure
the client library to use the Netty HTTP client. Configuring or changing the HTTP client is detailed in the
[HTTP clients wiki][http_clients_wiki].

## Next steps
For more details see the [samples README][samples_readme].

#### Async APIs
All the examples shown so far have been using synchronous APIs, but we provide full support for async APIs as well.
You'll need to use `MetricsAdvisorAsyncClient`
<!-- embedme ./src/samples/java/com/azure/ai/metricsadvisor/ReadmeSamples.java#L294-L298 -->
```java
MetricsAdvisorKeyCredential credential = new MetricsAdvisorKeyCredential("subscription_key", "api_key");
MetricsAdvisorAsyncClient metricsAdvisorAsyncClient = new MetricsAdvisorClientBuilder()
    .credential(credential)
    .endpoint("{endpoint}")
    .buildAsyncClient();
```

### Additional documentation

For more extensive documentation on Azure Cognitive Services Metrics Advisor, see the [Metrics Advisor documentation][metrics_advisor_doc].

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a [Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

<!-- LINKS -->
[aad_authorization]: https://docs.microsoft.com/azure/cognitive-services/authentication#authenticate-with-azure-active-directory
[api_reference_doc]: https://docs.microsoft.com/java/api/com.azure.ai.metricsadvisor?view=azure-java-preview
[azure_identity_credential_type]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity#credentials
[azure_cli]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account-cli?tabs=windows
[azure_cli_endpoint]: https://docs.microsoft.com/cli/azure/cognitiveservices/account?view=azure-cli-latest#az-cognitiveservices-account-show
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity#credentials
[azure_portal]: https://ms.portal.azure.com
[azure_subscription]: https://azure.microsoft.com/free
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[create_new_resource]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account?tabs=multiservice%2Cwindows#create-a-new-azure-cognitive-services-resource
[grant_access]: https://docs.microsoft.com/azure/cognitive-services/authentication#assign-a-role-to-a-service-principal
[http_clients_wiki]: https://github.com/Azure/azure-sdk-for-java/wiki/HTTP-clients
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[key]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account?tabs=multiservice%2Cwindows#get-the-keys-for-your-resource
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK
[metrics_advisor_account]: https://ms.portal.azure.com/#create/Microsoft.CognitiveServicesMetricsAdvisor
[metrics_advisor_doc]: https://docs.microsoft.com/azure/cognitive-services/Metrics-advisor/glossary
[mvn_package]: https://search.maven.org/search?q=a:azure-ai-metricsadvisor
[product_documentation]: https://docs.microsoft.com/azure/cognitive-services/metrics-advisor/overview
[register_AAD_application]: https://docs.microsoft.com/azure/cognitive-services/authentication#assign-a-role-to-a-service-principal
[source_code]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/metricsadvisor/azure-ai-metricsadvisor/src
[samples]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/metricsadvisor/azure-ai-metricsadvisor/src/samples
[samples_readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/metricsadvisor/azure-ai-metricsadvisor/src/samples/README.md
[wiki_identity]: https://github.com/Azure/azure-sdk-for-java/wiki/Identity-and-Authentication

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%metricsadvisor%2Fazure-ai-metricsadvisor%2FREADME.png)
