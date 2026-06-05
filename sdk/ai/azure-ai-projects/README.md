# Azure Projects client library for Java

The AI Projects client library is part of the Azure AI Foundry SDK and provides easy access to resources in your Azure AI Foundry Project. Use it to:

* **Create and run Agents** using the separate package `com.azure:azure-ai-agents`.
* **Enumerate AI Models** deployed to your Foundry Project using the `Deployments` operations.
* **Enumerate connected Azure resources** in your Foundry project using the `Connections` operations.
* **Upload documents and create Datasets** to reference them using the `Datasets` operations.
* **Generate datasets** for model, agent, evaluator, and traces scenarios using the preview `DataGenerationJobs` operations.
* **Register and manage model weights** as Foundry `ModelVersion` resources using the preview `Models` operations.
* **Create and dispatch routines** using the preview `Routines` operations.
* **Create and manage skills** using the preview `Skills` operations.
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
    <version>2.1.0</version>
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
DataGenerationJobsClient dataGenerationJobsClient = builder.buildDataGenerationJobsClient();
DatasetsClient datasetsClient = builder.buildDatasetsClient();
DeploymentsClient deploymentsClient = builder.buildDeploymentsClient();
EvaluationRulesClient evaluationRulesClient = builder.buildEvaluationRulesClient();
EvaluationTaxonomiesClient evaluationTaxonomiesClient = builder.buildEvaluationTaxonomiesClient();
EvaluatorsClient evaluatorsClient = builder.buildEvaluatorsClient();
IndexesClient indexesClient = builder.buildIndexesClient();
InsightsClient insightsClient = builder.buildInsightsClient();
ModelsClient modelsClient = builder.buildModelsClient();
RedTeamsClient redTeamsClient = builder.buildRedTeamsClient();
RoutinesClient routinesClient = builder.buildRoutinesClient();
SchedulesClient schedulesClient = builder.buildSchedulesClient();
SkillsClient skillsClient = builder.buildSkillsClient();
```

In the particular case of the `Evals` feature, this client library exposes [OpenAI's official SDK][openai_java_sdk] directly, so you can use the [official OpenAI docs][openai_api_docs] to access this feature.

```java com.azure.ai.projects.evalsServices
EvalService evalService = builder.buildOpenAIClient().evals();
EvalServiceAsync evalAsyncService = builder.buildOpenAIAsyncClient().evals();
```

When using Azure-specific evaluator models with OpenAI evaluations, use `EvaluationsHelper` to adapt the Azure model to
the OpenAI request type. This keeps application code from depending on the serialization details used by the OpenAI SDK.

```java com.azure.ai.projects.evaluationsHelper
Map<String, String> dataMapping = new LinkedHashMap<>();
dataMapping.put("query", "{{item.query}}");
dataMapping.put("response", "{{sample.output_text}}");

TestingCriterionAzureAIEvaluator coherenceEvaluator = new TestingCriterionAzureAIEvaluator("coherence",
    "builtin.coherence")
        .setInitializationParameters(Collections.singletonMap("deployment_name",
            BinaryData.fromObject("gpt-4o-mini")))
        .setDataMapping(dataMapping);

EvalCreateParams.TestingCriterion testingCriterion
    = EvaluationsHelper.toTestingCriterion(coherenceEvaluator);
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
| `ModelsClient` | `Models=V1Preview` |
| `RedTeamsClient` | `RedTeams=V1Preview` |
| `SchedulesClient` | `Schedules=V1Preview` |
| `SkillsClient` | `Skills=V1Preview` |

The `DataGenerationJobsClient`, `RoutinesClient`, `EvaluationRulesClient`, and `InsightsClient` also support the `Foundry-Features` header, but it is **not** automatically set. Instead, you can pass a `FoundryFeaturesOptInKeys` value when calling methods that accept it (e.g., `FoundryFeaturesOptInKeys.DATA_GENERATION_JOBS_V1_PREVIEW`, `FoundryFeaturesOptInKeys.ROUTINES_V1_PREVIEW`, `generateInsight()`, `getInsight()`, `listInsights()`, or `createOrUpdateEvaluationRule()`).

The `FoundryFeaturesOptInKeys` enum defines all known opt-in keys: `EVALUATIONS_V1_PREVIEW`, `SCHEDULES_V1_PREVIEW`, `RED_TEAMS_V1_PREVIEW`, `INSIGHTS_V1_PREVIEW`, `MEMORY_STORES_V1_PREVIEW`, `ROUTINES_V1_PREVIEW`, `TOOLBOXES_V1_PREVIEW`, `SKILLS_V1_PREVIEW`, `DATA_GENERATION_JOBS_V1_PREVIEW`, `MODELS_V1_PREVIEW`, `AGENTS_OPTIMIZATION_V1_PREVIEW`.

## Examples

The examples below show common operations for core AI Projects sub-clients. For complete runnable samples, see the [package samples][package_samples]. Additional preview samples are available for data generation jobs (`DataGenerationJobsSample`, `DataGenerationJobsAsyncSample`, and `DataGenerationJobWithEvaluationSample`), model management (`ModelsSample` and `ModelsAsyncSample`), and packaged skills (`SkillsPackageSample` and `SkillsPackageAsyncSample`).

### Connections operations

Connection operations allow you to enumerate the Azure resources connected to your AI Foundry Project. These connections can be seen in the "Management Center", in the "Connected resources" tab in your AI Foundry Project.

#### List connections

```java com.azure.ai.projects.ConnectionsSample.listConnections
PagedIterable<Connection> connections = connectionsClient.listConnections();
for (Connection connection : connections) {
    System.out.println("Connection name: " + connection.getName());
    System.out.println("Connection type: " + connection.getType());
    System.out.println("Connection credential type: " + connection.getCredential().getType());
    System.out.println("-------------------------------------------------");
}
```

#### Get a connection without credentials

```java com.azure.ai.projects.ConnectionsSample.getConnectionWithoutCredentials

String connectionName = Configuration.getGlobalConfiguration().get("TEST_CONNECTION_NAME", "");
Connection connection = connectionsClient.getConnection(connectionName);

System.out.printf("Connection name: %s%n", connection.getName());

```

#### Get a connection with credentials

```java com.azure.ai.projects.ConnectionsSample.getConnectionWithCredentials

String connectionName = Configuration.getGlobalConfiguration().get("TEST_CONNECTION_NAME", "");
Connection connection = connectionsClient.getConnectionWithCredentials(connectionName);

System.out.printf("Connection name: %s%n", connection.getName());
System.out.printf("Connection credentials: %s%n", connection.getCredential().getType());

```

#### Asynchronous connection operations

```java com.azure.ai.projects.ConnectionsAsyncSample.listConnections

return connectionsAsyncClient.listConnections()
    .doOnNext(connection -> System.out.printf("Connection name: %s%n", connection.getName()));

```

```java com.azure.ai.projects.ConnectionsAsyncSample.getConnectionWithoutCredentials

String connectionName = Configuration.getGlobalConfiguration().get("TEST_CONNECTION_NAME", "");
return connectionsAsyncClient.getConnection(connectionName)
    .doOnNext(connection -> System.out.printf("Connection name: %s%n", connection.getName()));

```

```java com.azure.ai.projects.ConnectionsAsyncSample.getConnectionWithCredentials

String connectionName = Configuration.getGlobalConfiguration().get("TEST_CONNECTION_NAME", "");
return connectionsAsyncClient.getConnectionWithCredentials(connectionName)
    .doOnNext(connection -> {
        System.out.printf("Connection name: %s%n", connection.getName());
        System.out.printf("Connection credentials: %s%n", connection.getCredential().getType());
    });

```

### Deployments operations

Deployment operations allow you to enumerate and inspect the models deployed to your AI Foundry Project.

#### List deployments

```java com.azure.ai.projects.DeploymentsSample.listDeployments

PagedIterable<Deployment> deployments = deploymentsClient.listDeployments();
for (Deployment deployment : deployments) {
    System.out.printf("Deployment name: %s%n", deployment.getName());
}

```

#### Get a deployment

```java com.azure.ai.projects.DeploymentsSample.getDeployment

String deploymentName = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME", "");
Deployment deployment = deploymentsClient.getDeployment(deploymentName);

System.out.printf("Deployment name: %s%n", deployment.getName());
System.out.printf("Deployment type: %s%n", deployment.getType().getValue());

```

#### Asynchronous deployment operations

```java com.azure.ai.projects.DeploymentsAsyncSample.listDeployments

return deploymentsAsyncClient.listDeployments()
    .doOnNext(deployment -> System.out.printf("Deployment name: %s%n", deployment.getName()));

```

```java com.azure.ai.projects.DeploymentsAsyncSample.getDeployment

String deploymentName = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME", "");
return deploymentsAsyncClient.getDeployment(deploymentName)
    .doOnNext(deployment -> {
        System.out.printf("Deployment name: %s%n", deployment.getName());
        System.out.printf("Deployment type: %s%n", deployment.getType().getValue());
    });

```

### Datasets operations

Dataset operations allow you to create, enumerate, retrieve, update, and delete dataset versions in your AI Foundry Project.

#### Create a dataset version from a local file

```java com.azure.ai.projects.DatasetsSample.createDatasetWithFile

String datasetName = Configuration.getGlobalConfiguration().get("DATASET_NAME", "my-dataset");
String datasetVersionString = Configuration.getGlobalConfiguration().get("DATASET_VERSION", "1.0");

Path filePath = getPath("product_info.md");

FileDatasetVersion createdDatasetVersion = datasetsClient.createDatasetWithFile(datasetName, datasetVersionString, filePath);

System.out.println("Created dataset version: " + createdDatasetVersion.getId());

```

#### List datasets

```java com.azure.ai.projects.DatasetsSample.listDatasets

System.out.println("Listing all datasets (latest versions):");
datasetsClient.listLatestDatasetVersions().forEach(dataset -> {
    System.out.println("\nDataset name: " + dataset.getName());
    System.out.println("Dataset Id: " + dataset.getId());
    System.out.println("Dataset version: " + dataset.getVersion());
    System.out.println("Dataset type: " + dataset.getType());
    if (dataset.getDescription() != null) {
        System.out.println("Description: " + dataset.getDescription());
    }
});

```

#### List dataset versions

```java com.azure.ai.projects.DatasetsSample.listDatasetVersions

String datasetName = Configuration.getGlobalConfiguration().get("DATASET_NAME", "test");

System.out.println("Listing all versions of dataset: " + datasetName);
datasetsClient.listDatasetVersions(datasetName).forEach(version -> {
    System.out.println("\nDataset name: " + version.getName());
    System.out.println("Dataset version: " + version.getVersion());
    System.out.println("Dataset type: " + version.getType());
    if (version.getDataUrl() != null) {
        System.out.println("Data URI: " + version.getDataUrl());
    }
});

```

#### Get a dataset version

```java com.azure.ai.projects.DatasetsSample.getDataset

String datasetName = Configuration.getGlobalConfiguration().get("DATASET_NAME", "test");
String datasetVersion = Configuration.getGlobalConfiguration().get("DATASET_VERSION", "1");

DatasetVersion dataset = datasetsClient.getDatasetVersion(datasetName, datasetVersion);

System.out.println("Retrieved dataset:");
System.out.println("Name: " + dataset.getName());
System.out.println("Version: " + dataset.getVersion());
System.out.println("Type: " + dataset.getType());
if (dataset.getDataUrl() != null) {
    System.out.println("Data URI: " + dataset.getDataUrl());
}
if (dataset.getDescription() != null) {
    System.out.println("Description: " + dataset.getDescription());
}

```

#### Create or update a dataset version

```java com.azure.ai.projects.DatasetsSample.createOrUpdateDataset

String datasetName = Configuration.getGlobalConfiguration().get("DATASET_NAME", "my-dataset");
String datasetVersion = Configuration.getGlobalConfiguration().get("DATASET_VERSION", "1.0");
String dataUri = Configuration.getGlobalConfiguration().get("DATA_URI", "https://example.com/data.txt");

// Create a new FileDatasetVersion with provided dataUri
FileDatasetVersion fileDataset = new FileDatasetVersion()
    .setDataUrl(dataUri)
    .setDescription("Sample dataset created via SDK");

// Create or update the dataset
FileDatasetVersion createdDataset = (FileDatasetVersion) datasetsClient.createOrUpdateDatasetVersion(
    datasetName,
    datasetVersion,
    fileDataset
);

System.out.println("Created/Updated dataset:");
System.out.println("Name: " + createdDataset.getName());
System.out.println("Version: " + createdDataset.getVersion());
System.out.println("Data URI: " + createdDataset.getDataUrl());

```

#### Start a pending upload for a dataset version

```java com.azure.ai.projects.DatasetsSample.pendingUploadSample

String datasetName = Configuration.getGlobalConfiguration().get("DATASET_NAME", "my-dataset");
String datasetVersion = Configuration.getGlobalConfiguration().get("DATASET_VERSION", "1.0");

// Create a pending upload request for the dataset
PendingUploadRequest request = new PendingUploadRequest();

// Get the pending upload response with blob reference
PendingUploadResponse response = datasetsClient.pendingUpload(datasetName, datasetVersion, request);

System.out.println("Pending upload initiated with ID: " + response.getPendingUploadId());
System.out.println("Blob URI: " + response.getBlobReference().getBlobUrl());

```

#### Delete a dataset version

```java com.azure.ai.projects.DatasetsSample.deleteDataset

String datasetName = Configuration.getGlobalConfiguration().get("DATASET_NAME", "my-dataset");
String datasetVersion = Configuration.getGlobalConfiguration().get("DATASET_VERSION", "1.0");

// Delete the specific version of the dataset
datasetsClient.deleteDatasetVersion(datasetName, datasetVersion);

System.out.println("Deleted dataset: " + datasetName + ", version: " + datasetVersion);

```

#### Asynchronous dataset operations

```java com.azure.ai.projects.DatasetsAsyncSample.createDatasetWithFile

String datasetName = Configuration.getGlobalConfiguration().get("DATASET_NAME", "my-dataset");
String datasetVersionString = Configuration.getGlobalConfiguration().get("DATASET_VERSION", "1.0");

Path filePath = getPath("product_info.md");

return datasetsAsyncClient.createDatasetWithFile(datasetName, datasetVersionString, filePath)
    .doOnNext(createdDatasetVersion ->
        System.out.println("Created dataset version: " + createdDatasetVersion.getId()));

```

```java com.azure.ai.projects.DatasetsAsyncSample.listDatasets

System.out.println("Listing all datasets (latest versions):");
return datasetsAsyncClient.listLatestDatasetVersions()
    .doOnNext(dataset -> {
        System.out.println("\nDataset name: " + dataset.getName());
        System.out.println("Dataset Id: " + dataset.getId());
        System.out.println("Dataset version: " + dataset.getVersion());
        System.out.println("Dataset type: " + dataset.getType());
        if (dataset.getDescription() != null) {
            System.out.println("Description: " + dataset.getDescription());
        }
    });

```

```java com.azure.ai.projects.DatasetsAsyncSample.listDatasetVersions

String datasetName = Configuration.getGlobalConfiguration().get("DATASET_NAME", "test");

System.out.println("Listing all versions of dataset: " + datasetName);
return datasetsAsyncClient.listDatasetVersions(datasetName)
    .doOnNext(version -> {
        System.out.println("\nDataset name: " + version.getName());
        System.out.println("Dataset version: " + version.getVersion());
        System.out.println("Dataset type: " + version.getType());
        if (version.getDataUrl() != null) {
            System.out.println("Data URI: " + version.getDataUrl());
        }
    });

```

```java com.azure.ai.projects.DatasetsAsyncSample.getDataset

String datasetName = Configuration.getGlobalConfiguration().get("DATASET_NAME", "test");
String datasetVersion = Configuration.getGlobalConfiguration().get("DATASET_VERSION", "1");

return datasetsAsyncClient.getDatasetVersion(datasetName, datasetVersion)
    .doOnNext(dataset -> {
        System.out.println("Retrieved dataset:");
        System.out.println("Name: " + dataset.getName());
        System.out.println("Version: " + dataset.getVersion());
        System.out.println("Type: " + dataset.getType());
        if (dataset.getDataUrl() != null) {
            System.out.println("Data URI: " + dataset.getDataUrl());
        }
        if (dataset.getDescription() != null) {
            System.out.println("Description: " + dataset.getDescription());
        }
    });

```

```java com.azure.ai.projects.DatasetsAsyncSample.createOrUpdateDataset

String datasetName = Configuration.getGlobalConfiguration().get("DATASET_NAME", "my-dataset");
String datasetVersion = Configuration.getGlobalConfiguration().get("DATASET_VERSION", "1.0");
String dataUri = Configuration.getGlobalConfiguration().get("DATA_URI", "https://example.com/data.txt");

// Create a new FileDatasetVersion with provided dataUri
FileDatasetVersion fileDataset = new FileDatasetVersion()
    .setDataUrl(dataUri)
    .setDescription("Sample dataset created via SDK");

// Create or update the dataset
return datasetsAsyncClient.createOrUpdateDatasetVersion(
    datasetName,
    datasetVersion,
    fileDataset
).doOnNext(createdDataset -> {
    FileDatasetVersion fileDatasetVersion = (FileDatasetVersion) createdDataset;
    System.out.println("Created/Updated dataset:");
    System.out.println("Name: " + fileDatasetVersion.getName());
    System.out.println("Version: " + fileDatasetVersion.getVersion());
    System.out.println("Data URI: " + fileDatasetVersion.getDataUrl());
});

```

```java com.azure.ai.projects.DatasetsAsyncSample.pendingUploadSample

String datasetName = Configuration.getGlobalConfiguration().get("DATASET_NAME", "my-dataset");
String datasetVersion = Configuration.getGlobalConfiguration().get("DATASET_VERSION", "1.0");

// Create a pending upload request for the dataset
PendingUploadRequest request = new PendingUploadRequest();

// Get the pending upload response with blob reference
return datasetsAsyncClient.pendingUpload(datasetName, datasetVersion, request)
    .doOnNext(response -> {
        System.out.println("Pending upload initiated with ID: " + response.getPendingUploadId());
        System.out.println("Blob URI: " + response.getBlobReference().getBlobUrl());
    });

```

```java com.azure.ai.projects.DatasetsAsyncSample.deleteDataset

String datasetName = Configuration.getGlobalConfiguration().get("DATASET_NAME", "my-dataset");
String datasetVersion = Configuration.getGlobalConfiguration().get("DATASET_VERSION", "1.0");

// Delete the specific version of the dataset
return datasetsAsyncClient.deleteDatasetVersion(datasetName, datasetVersion)
    .doOnSuccess(unused ->
        System.out.println("Deleted dataset: " + datasetName + ", version: " + datasetVersion));

```

### Indexes operations

Index operations allow you to create and enumerate search indexes used by your AI Foundry Project.

#### Create or update an index version

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

#### List indexes

```java com.azure.ai.projects.IndexesListSample.listIndexes
indexesClient.listLatestIndexVersions().forEach(index -> {
    System.out.println("Index name: " + index.getName());
    System.out.println("Index version: " + index.getVersion());
    System.out.println("Index description: " + index.getDescription());
    System.out.println("-------------------------------------------------");
});
```

#### List index versions

```java com.azure.ai.projects.IndexesListVersionsSample.listIndexVersions

String indexName = Configuration.getGlobalConfiguration().get("INDEX_NAME", "my-index");

indexesClient.listIndexVersions(indexName).forEach(index -> {
    System.out.println("Index name: " + index.getName());
    System.out.println("Index version: " + index.getVersion());
    System.out.println("Index type: " + index.getType());
});

```

#### Get an index version

```java com.azure.ai.projects.IndexesGetSample.getIndex

String indexName = Configuration.getGlobalConfiguration().get("INDEX_NAME", "my-index");
String indexVersion = Configuration.getGlobalConfiguration().get("INDEX_VERSION", "1.0");

AIProjectIndex index = indexesClient.getIndexVersion(indexName, indexVersion);

System.out.println("Retrieved index:");
System.out.println("Name: " + index.getName());
System.out.println("Version: " + index.getVersion());
System.out.println("Type: " + index.getType());

```

#### Delete an index version

```java com.azure.ai.projects.IndexesDeleteSample.deleteIndex

String indexName = Configuration.getGlobalConfiguration().get("INDEX_NAME", "my-index");
String indexVersion = Configuration.getGlobalConfiguration().get("INDEX_VERSION", "1.0");

// Delete the index version
indexesClient.deleteIndexVersion(indexName, indexVersion);

System.out.println("Deleted index: " + indexName + ", version: " + indexVersion);

```

#### Asynchronous index operations

```java com.azure.ai.projects.IndexesAsyncSample.createOrUpdateIndex

String indexName = Configuration.getGlobalConfiguration().get("INDEX_NAME", "my-index");
String indexVersion = Configuration.getGlobalConfiguration().get("INDEX_VERSION", "2.0");
String aiSearchConnectionName = Configuration.getGlobalConfiguration().get("AI_SEARCH_CONNECTION_NAME", "");
String aiSearchIndexName = Configuration.getGlobalConfiguration().get("AI_SEARCH_INDEX_NAME", "");

return indexesAsyncClient.createOrUpdateIndexVersion(
    indexName,
    indexVersion,
    new AzureAISearchIndex()
        .setConnectionName(aiSearchConnectionName)
        .setIndexName(aiSearchIndexName)
).doOnNext(index -> System.out.println("Index created: " + index.getName()));

```

```java com.azure.ai.projects.IndexesAsyncSample.listIndexes

return indexesAsyncClient.listLatestIndexVersions()
    .doOnNext(index -> {
        System.out.println("Index name: " + index.getName());
        System.out.println("Index version: " + index.getVersion());
    });

```

```java com.azure.ai.projects.IndexesAsyncSample.listIndexVersions

String indexName = Configuration.getGlobalConfiguration().get("INDEX_NAME", "my-index");

return indexesAsyncClient.listIndexVersions(indexName)
    .doOnNext(index -> {
        System.out.println("Index name: " + index.getName());
        System.out.println("Index version: " + index.getVersion());
        System.out.println("Index type: " + index.getType());
    });

```

```java com.azure.ai.projects.IndexesAsyncSample.getIndex

String indexName = Configuration.getGlobalConfiguration().get("INDEX_NAME", "my-index");
String indexVersion = Configuration.getGlobalConfiguration().get("INDEX_VERSION", "1.0");

return indexesAsyncClient.getIndexVersion(indexName, indexVersion)
    .doOnNext(index -> {
        System.out.println("Retrieved index:");
        System.out.println("Name: " + index.getName());
        System.out.println("Version: " + index.getVersion());
        System.out.println("Type: " + index.getType());
    });

```

```java com.azure.ai.projects.IndexesAsyncSample.deleteIndex

String indexName = Configuration.getGlobalConfiguration().get("INDEX_NAME", "my-index");
String indexVersion = Configuration.getGlobalConfiguration().get("INDEX_VERSION", "1.0");

// Delete the index version
return indexesAsyncClient.deleteIndexVersion(indexName, indexVersion)
    .doOnSuccess(unused ->
        System.out.println("Deleted index: " + indexName + ", version: " + indexVersion));

```

### Skills operations

Skills are a preview feature. The `SkillsClient` automatically sets the `Skills=V1Preview` opt-in flag on every request.

#### Create a skill

```java com.azure.ai.projects.SkillsSample.createSkillVersion

SkillInlineContent inlineContent = new SkillInlineContent(
    "Answers product support questions using company policy.",
    "You help answer product support questions using company policy and product guidance."
);

SkillVersion skillVersion = skillsClient.createSkillVersion("product-support-skill", inlineContent, true);

System.out.println("Created skill version: " + skillVersion.getName());
System.out.println("Version: " + skillVersion.getVersion());

```

#### Get a skill

```java com.azure.ai.projects.SkillsSample.getSkill

String skillName = "product-support-skill";
Skill skill = skillsClient.getSkill(skillName);

System.out.println("Skill name: " + skill.getName());
System.out.println("Description: " + skill.getDescription());
System.out.println("Default version: " + skill.getDefaultVersion());

```

#### Update a skill

```java com.azure.ai.projects.SkillsSample.updateSkill

String skillName = "product-support-skill";

Skill updated = skillsClient.updateSkill(skillName, "2");

System.out.println("Updated skill: " + updated.getName());
System.out.println("Default version: " + updated.getDefaultVersion());

```

#### List skills

```java com.azure.ai.projects.SkillsSample.listSkills

PagedIterable<Skill> skills = skillsClient.listSkills();
for (Skill skill : skills) {
    System.out.println("Skill name: " + skill.getName());
    System.out.println("Description: " + skill.getDescription());
    System.out.println("-------------------------------------------------");
}

```

#### Delete a skill

```java com.azure.ai.projects.SkillsSample.deleteSkill

String skillName = "product-support-skill";
skillsClient.deleteSkill(skillName);

System.out.println("Deleted skill: " + skillName);

```

#### Asynchronous skills operations

```java com.azure.ai.projects.SkillsAsyncSample.createSkillVersion

SkillInlineContent inlineContent = new SkillInlineContent(
    "Answers product support questions using company policy.",
    "You help answer product support questions using company policy and product guidance."
);

return skillsAsyncClient.createSkillVersion("product-support-skill", inlineContent, true)
    .doOnNext(skillVersion -> {
        System.out.println("Created skill version: " + skillVersion.getName());
        System.out.println("Version: " + skillVersion.getVersion());
    });

```

```java com.azure.ai.projects.SkillsAsyncSample.getSkill

String skillName = "product-support-skill";

return skillsAsyncClient.getSkill(skillName)
    .doOnNext(skill -> {
        System.out.println("Skill name: " + skill.getName());
        System.out.println("Description: " + skill.getDescription());
        System.out.println("Default version: " + skill.getDefaultVersion());
    });

```

```java com.azure.ai.projects.SkillsAsyncSample.updateSkill

String skillName = "product-support-skill";

return skillsAsyncClient.updateSkill(skillName, "2")
    .doOnNext(updated -> {
        System.out.println("Updated skill: " + updated.getName());
        System.out.println("Default version: " + updated.getDefaultVersion());
    });

```

```java com.azure.ai.projects.SkillsAsyncSample.listSkills

return skillsAsyncClient.listSkills()
    .doOnNext(skill -> {
        System.out.println("Skill name: " + skill.getName());
        System.out.println("Description: " + skill.getDescription());
        System.out.println("-------------------------------------------------");
    });

```

```java com.azure.ai.projects.SkillsAsyncSample.deleteSkill

String skillName = "product-support-skill";

return skillsAsyncClient.deleteSkill(skillName)
    .doOnSuccess(unused -> System.out.println("Deleted skill: " + skillName));

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
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/blob/main/docs/performance-tuning.md
