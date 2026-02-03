# Azure OnlineExperimentation client library for Java

Azure OnlineExperimentation client library for Java.

This package contains Microsoft Azure OnlineExperimentation client library.

## Documentation

Various documentation is available to help you get started

- [API reference documentation][docs]
- [Product documentation][product_documentation]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-analytics-onlineexperimentation;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-analytics-onlineexperimentation</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authentication

[Azure Identity][azure_identity] package provides the default implementation for authenticating the client.

## Key concepts

### Online Experimentation Workspace

[`Microsoft.OnlineExperimentation/workspaces`][az_exp_workspace] Azure resources work in conjunction with [Azure App Configuration][app_config] and [Azure Monitor][azure_monitor]. The Online Experimentation workspace handles management of metrics definitions and their continuous computation to monitor and evaluate experiment results.

### Experiment Metrics

Metrics are used to measure the impact of your online experiments. See the [samples][azure_exp_samples] for how to create and manage various types of experiment metrics.

## Examples

```java com.azure.analytics.onlineexperimentation.readme
// [Step 1] Initialize the SDK client
// The endpoint URL from the Microsoft.OnlineExperimentation/workspaces resource
String endpoint = System.getenv("AZURE_ONLINEEXPERIMENTATION_ENDPOINT");

System.out.printf("AZURE_ONLINEEXPERIMENTATION_ENDPOINT is %s%n", endpoint);

OnlineExperimentationClient client = new OnlineExperimentationClientBuilder()
        .endpoint(endpoint)
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildClient();

// [Step 2] Define the experiment metric
ExperimentMetric exampleMetric = new ExperimentMetric()
        .setLifecycle(LifecycleStage.ACTIVE)
        .setDisplayName("% users with LLM interaction who made a high-value purchase")
        .setDescription(
                "Percentage of users who received a response from the LLM and then made a purchase of $100 or more")
        .setCategories(Arrays.asList("Business"))
        .setDesiredDirection(DesiredDirection.INCREASE)
        .setDefinition(new EventRateMetricDefinition()
                .setEvent(new ObservedEvent().setEventName("ResponseReceived"))
                .setRateCondition("Revenue > 100"));

// [Optional][Step 2a] Validate the metric - checks for input errors without persisting anything.
System.out.println("Checking if the experiment metric definition is valid...");

ExperimentMetricValidationResult validationResult = client.validateMetric(exampleMetric);

System.out.printf("Experiment metric definition valid: %s.%n", validationResult.isValid());
if (validationResult.getDiagnostics() != null) {
    for (DiagnosticDetail detail : validationResult.getDiagnostics()) {
        // Inspect details of why the metric definition was rejected as Invalid.
        System.out.printf("- %s: %s%n", detail.getCode(), detail.getMessage());
    }
}

if (!validationResult.isValid()) {
    System.out.println("Metric validation failed. Exiting sample.");
    return;
}

// [Step 3] Create the experiment metric
String exampleMetricId = "sample_metric_id_" + UUID.randomUUID().toString().replace("-", "");

System.out.printf("Creating the experiment metric %s...%n", exampleMetricId);

// Create with If-None-Match to ensure no one else created this metric in the meantime
RequestConditions createConditions = new RequestConditions().setIfNoneMatch("*");
ExperimentMetric createdMetric = client.createOrUpdateMetric(exampleMetricId, exampleMetric, createConditions);

System.out.printf("Experiment metric %s created, etag: %s.%n", createdMetric.getId(), createdMetric.getETag());

// [Step 4] Deactivate the experiment metric and update the description.
ExperimentMetric updateRequest = new ExperimentMetric()
        .setLifecycle(LifecycleStage.INACTIVE) // pauses computation of this metric
        .setDescription("No longer need to compute this.");

// Update with If-Match to ensure no one else updated the metric in the meantime
RequestConditions updateConditions = new RequestConditions().setIfMatch(createdMetric.getETag());
ExperimentMetric updatedMetric = client.createOrUpdateMetric(exampleMetricId, updateRequest, updateConditions);

System.out.printf("Updated metric: %s, etag: %s.%n", updatedMetric.getId(), updatedMetric.getETag());

// [Step 5] Delete the experiment metric.
RequestConditions deleteConditions = new RequestConditions().setIfMatch(updatedMetric.getETag());
client.deleteMetric(exampleMetricId, deleteConditions);

System.out.printf("Deleted metric: %s.%n", exampleMetricId);
```

### Service API versions

The client library targets the latest service API version by default.
The service client builder accepts an optional service API version parameter to specify which API version to communicate.

#### Select a service API version

You have the flexibility to explicitly select a supported service API version when initializing a service client via the service client builder.
This ensures that the client can communicate with services using the specified API version.

When selecting an API version, it is important to verify that there are no breaking changes compared to the latest API version.
If there are significant differences, API calls may fail due to incompatibility.

Always ensure that the chosen API version is fully supported and operational for your specific use case and that it aligns with the service's versioning policy.

## Troubleshooting

If you encounter any bugs, please file issues via [Issues](https://github.com/Azure/azure-sdk-for-java/issues).

### Enable client logging

Azure SDKs for Java offer a consistent logging story to help aid in troubleshooting application errors and expedite their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

### Default HTTP Client

All client libraries by default use the Netty HTTP client. Add the above dependency to automatically configure the client library to use the Netty HTTP client. Configuring or changing the HTTP client is detailed in the [HTTP clients wiki][http_clients_wiki].

## Next steps

See the [samples][azure_exp_samples] page for common ways to setup metrics for evaluating experiment results.

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- LINKS -->
[product_documentation]: https://azure.microsoft.com/services/
[docs]: https://azure.github.io/azure-sdk-for-java/
[jdk]: https://learn.microsoft.com/azure/developer/java/fundamentals/
[azure_subscription]: https://azure.microsoft.com/free/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
[app_config]: https://learn.microsoft.com/azure/azure-app-configuration/overview
[azure_monitor]: https://learn.microsoft.com/azure/azure-monitor/overview
[azure_exp_samples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/onlineexperimentation/azure-resourcemanager-onlineexperimentation/SAMPLE.md
[az_exp_workspace]: https://learn.microsoft.com/azure/templates/microsoft.onlineexperimentation/workspaces
[http_clients_wiki]: https://learn.microsoft.com/azure/developer/java/sdk/http-client-pipeline#http-clients
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-in-Azure-SDK
