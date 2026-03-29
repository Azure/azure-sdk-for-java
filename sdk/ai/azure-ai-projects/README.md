# Azure Projects client library for Java

The AI Projects client library is part of the Azure AI Foundry SDK and provides easy access to resources in your Azure AI Foundry Project. Use it to:

* **Create and run Agents** using the separate package `com.azure.azure-ai-agents`.
* **Enumerate AI Models** deployed to your Foundry Project using the `Deployments` operations.
* **Enumerate connected Azure resources** in your Foundry project using the `Connections` operations.
* **Upload documents and create Datasets** to reference them using the `Datasets` operations.
* **Create and enumerate Search Indexes** using the `Indexes` operations.

The client library uses a single service version `v1` of the AI Foundry [data plane REST APIs](https://aka.ms/azsdk/azure-ai-projects/ga-rest-api-reference).

## Documentation

Various documentation is available to help you get started

- [API reference documentation][docs]
- [Product documentation][product_documentation]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-ai-projects;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-ai-projects</artifactId>
    <version>2.0.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authentication

[Azure Identity][azure_identity] package provides the default implementation for authenticating the client.

## Key concepts

The Azure AI Foundry provides a centralized spot to manage your AI Foundry resources. In order to access each feature you need to initialize your builder and access the corresponding sub-client like it's shown in the following code snippet:

```java com.azure.ai.projects.clientInitialization
AIProjectClientBuilder builder = new AIProjectClientBuilder();

ConnectionsClient connectionsClient = builder.buildConnectionsClient();
DatasetsClient datasetsClient = builder.buildDatasetsClient();
DeploymentsClient deploymentsClient = builder.buildDeploymentsClient();
EvaluationRulesClient evaluationRulesClient = builder.buildEvaluationRulesClient();
EvaluationTaxonomiesClient evaluationTaxonomiesClient = builder.buildEvaluationTaxonomiesClient();
EvaluatorsClient evaluatorsClient = builder.buildEvaluatorsClient();
IndexesClient indexesClient = builder.buildIndexesClient();
InsightsClient insightsClient = builder.buildInsightsClient();
RedTeamsClient redTeamsClient = builder.buildRedTeamsClient();
SchedulesClient schedulesClient = builder.buildSchedulesClient();
```

In the particular case of the `Evals` feature, this client library exposes [OpenAI's official SDK][openai_java_sdk] directly, so you can use the [official OpenAI docs][openai_api_docs] to access this feature.

```java com.azure.ai.projects.evalsServices
EvalService evalService = builder.buildOpenAIClient().evals();
EvalServiceAsync evalAsyncService = builder.buildOpenAIAsyncClient().evals();
```

For the Agents operation, you can use the `azure-ai-agents` package which is available as transitive dependency:

```java com.azure.ai.projects.agentsSubClients
AgentsClientBuilder agentsClientBuilder = new AgentsClientBuilder();

AgentsClient agentsClient = agentsClientBuilder.buildAgentsClient();
MemoryStoresClient memoryStoresClient = agentsClientBuilder.buildMemoryStoresClient();
ResponsesClient responsesClient = agentsClientBuilder.buildResponsesClient();
```

If you need a full OpenAI client as well, you can use the `AIProjectClientBuilder` to obtain one:

```java com.azure.ai.projects.openAIClient
OpenAIClient openAIClient = builder.buildOpenAIClient();
OpenAIClientAsync openAIClientAsync = builder.buildOpenAIAsyncClient();
```

### Preview operation groups and opt-in flags

Several operation groups in the AI Projects client library are in **preview** and require the `Foundry-Features` HTTP header for opt-in. The SDK automatically sets this header on every request for the following sub-clients:

| Sub-client | Opt-in flag |
|---|---|
| `EvaluatorsClient` | `Evaluations=V1Preview` |
| `EvaluationTaxonomiesClient` | `Evaluations=V1Preview` |
| `RedTeamsClient` | `RedTeams=V1Preview` |
| `SchedulesClient` | `Schedules=V1Preview` |

The `EvaluationRulesClient` and `InsightsClient` also support the `Foundry-Features` header, but it is **not** automatically set. Instead, you can pass a `FoundryFeaturesOptInKeys` value when calling their methods (e.g., `generateInsight()`, `getInsight()`, `listInsights()`, or `createOrUpdateEvaluationRule()`).

The `FoundryFeaturesOptInKeys` enum defines all known opt-in keys: `EVALUATIONS_V1_PREVIEW`, `SCHEDULES_V1_PREVIEW`, `RED_TEAMS_V1_PREVIEW`, `INSIGHTS_V1_PREVIEW`, `MEMORY_STORES_V1_PREVIEW`.

## Examples

### Connections operations

The code below shows some Connection operations, which allow you to enumerate the Azure Resources connected to your AI Foundry Projects. These connections can be seen in the "Management Center", in the "Connected resources" tab in your AI Foundry Project. For more samples see the [package samples][package_samples].

```java com.azure.ai.projects.ConnectionsSample.listConnections
PagedIterable<Connection> connections = connectionsClient.listConnections();
for (Connection connection : connections) {
    System.out.println("Connection name: " + connection.getName());
    System.out.println("Connection type: " + connection.getType());
    System.out.println("Connection credential type: " + connection.getCredential().getType());
    System.out.println("-------------------------------------------------");
}
```

### Indexes

The code below shows some Indexes operations to list and create indexes. For more samples see the [package samples][package_samples].

```java com.azure.ai.projects.IndexesListSample.listIndexes
indexesClient.listLatestIndexVersions().forEach(index -> {
    System.out.println("Index name: " + index.getName());
    System.out.println("Index version: " + index.getVersion());
    System.out.println("Index description: " + index.getDescription());
    System.out.println("-------------------------------------------------");
});
```

```java com.azure.ai.projects.IndexesGetSample.createOrUpdateIndex
String indexName = Configuration.getGlobalConfiguration().get("INDEX_NAME", "my-index");
String indexVersion = Configuration.getGlobalConfiguration().get("INDEX_VERSION", "2.0");
String aiSearchConnectionName = Configuration.getGlobalConfiguration().get("AI_SEARCH_CONNECTION_NAME", "");
String aiSearchIndexName = Configuration.getGlobalConfiguration().get("AI_SEARCH_INDEX_NAME", "");

AIProjectIndex index = indexesClient.createOrUpdateIndexVersion(
    indexName,
    indexVersion,
    new AzureAISearchIndex()
        .setConnectionName(aiSearchConnectionName)
        .setIndexName(aiSearchIndexName)
);

System.out.println("Index created: " + index.getName());
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

### Enable client logging

You can set the `AZURE_LOG_LEVEL` environment variable to view logging statements made in the client library. For
example, setting `AZURE_LOG_LEVEL=2` would show all informational, warning, and error log messages. The log levels can
be found here: [log levels][logLevels].

To log full HTTP request and response bodies (including headers), set:

```bash
export AZURE_LOG_LEVEL=verbose
export AZURE_HTTP_LOG_DETAIL_LEVEL=body_and_headers
```

### Default HTTP Client

All client libraries by default use the Netty HTTP client. Configuring or changing the HTTP client is detailed in the
[HTTP clients wiki](https://learn.microsoft.com/azure/developer/java/sdk/http-client-pipeline#http-clients).

### Default SSL library

All client libraries, by default, use the Tomcat-native Boring SSL library to enable native-level performance for SSL
operations. The Boring SSL library is an uber jar containing native libraries for Linux / macOS / Windows, and provides
better performance compared to the default SSL implementation within the JDK. For more information, including how to
reduce the dependency size, refer to the [performance tuning][performance_tuning] section of the wiki.

## Next steps

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- LINKS -->
[product_documentation]: https://learn.microsoft.com/azure/ai-studio/
[docs]: https://learn.microsoft.com/rest/api/aifoundry/aiproject/
[jdk]: https://learn.microsoft.com/azure/developer/java/fundamentals/
[azure_subscription]: https://azure.microsoft.com/free/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
[package_samples]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/ai/azure-ai-projects/src/samples/java/com/azure/ai/projects
[openai_java_sdk]: https://github.com/openai/openai-java
[openai_api_docs]: https://platform.openai.com/docs/overview
[logLevels]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/util/logging/LogLevel.java
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning
