# Azure Synapse Spark client library for Java
Azure Synapse is a limitless analytics service that brings together enterprise data warehousing and Big Data analytics. It gives you the freedom to query data on your terms, using either serverless on-demand or provisioned resources—at scale. Azure Synapse brings these two worlds together with a unified experience to ingest, prepare, manage, and serve data for immediate BI and machine learning needs.

The Azure Synapse Analytics Spark client library enables programmatically managing Spark jobs.

[Source code][source_code] | [API reference documentation][api_documentation] | [Product documentation][azsynapse_docs] | [Samples][spark_samples]

## Getting started
### Adding the package to your project
Maven dependency for the Azure Synapse Spark client library. Add it to your project's POM file.

[//]: # ({x-version-update-start;com.azure:azure-analytics-synapse-spark;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-analytics-synapse-spark</artifactId>
    <version>1.0.0-beta.3</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Prerequisites
- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure subscription][azure_sub].
- An existing Azure Synapse workspace. If you need to create an Azure Synapse workspace, you can use the [Azure Portal][azure_portal] or [Azure CLI][azure_cli].
    ```Bash
    az synapse workspace create \
        --name <your-workspace-name> \
        --resource-group <your-resource-group-name> \
        --storage-account <your-storage-account-name> \
        --file-system <your-storage-file-system-name> \
        --sql-admin-login-user <your-sql-admin-user-name> \
        --sql-admin-login-password <your-sql-admin-user-password> \
        --location <your-workspace-location>
    ```

### Authenticate the client
In order to interact with the Azure Synapse service, you'll need to create an instance of the [SparkClient](#create-spark-client) class. You would need a **workspace endpoint** and **client secret credentials (client id, client secret, tenant id)** to instantiate a client object using the default `DefaultAzureCredential` examples shown in this document.

The `DefaultAzureCredential` way of authentication by providing client secret credentials is being used in this getting started section but you can find more ways to authenticate with [azure-identity][azure_identity].

#### Create/Get credentials
To create/get client secret credentials you can use the [Azure Portal][azure_create_application_in_portal], [Azure CLI][azure_synapse_cli_full] or [Azure Cloud Shell](https://shell.azure.com/bash)

Here is an [Azure Cloud Shell](https://shell.azure.com/bash) snippet below to

 * Create a service principal and configure its access to Azure resources:

    ```Bash
    az ad sp create-for-rbac -n <your-application-name> --skip-assignment
    ```

    Output:

    ```json
    {
        "appId": "generated-app-ID",
        "displayName": "dummy-app-name",
        "name": "http://dummy-app-name",
        "password": "random-password",
        "tenant": "tenant-ID"
    }
    ```

#### Create Spark client
Once you've populated the **AZURE_CLIENT_ID**, **AZURE_CLIENT_SECRET**, and **AZURE_TENANT_ID** environment variables and replaced **your-workspace-endpoint** with the URI returned above, you can create Spark clients. For example, the following code creates SparkBatchClient:

```Java
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.analytics.synapse.spark.SparkBatchClient;
import com.azure.analytics.synapse.spark.SparkClientBuilder;

SparkBatchClient batchClient = new SparkClientBuilder()
    .endpoint("https://{YOUR_WORKSPACE_NAME}.dev.azuresynapse.net")
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildSparkBatchClient();
```

> NOTE: For using an asynchronous client use SparkBatchAsyncClient instead of SparkBatchClient and call `buildSparkBatchAsyncClient()`

## Key concepts

### Spark batch Client

The Spark batch client performs the interactions with the Azure Synapse service for getting, setting, updating, deleting, and listing Spark batch jobs. Asynchronous (SparkBatchAsyncClient) and synchronous (SparkBatchClient) clients exist in the SDK allowing for the selection of a client based on an application's use case.

## Examples
The Azure.Analytics.Synapse.Spark package supports synchronous and asynchronous APIs. The following section covers some of the most common Azure Synapse Analytics Spark job related tasks:

### Sync API
The following sections provide several code snippets covering some of the most common Azure Synapse Spark service tasks, including:

### Spark batch job examples
* [Create a Spark batch job](#create-a-spark-batch-job)
* [List role Spark batch jobs](#list-spark-batch-jobs)
* [Cancel a Spark batch job](#delete-a-spark-batch-job)

### Create a Spark batch job

`createSparkBatchJob` creates a Spark batch job.

```java
SparkBatchJobOptions options = new SparkBatchJobOptions()
    .setName(name)
    .setFile(file)
    .setClassName("WordCount")
    .setArguments(Arrays.asList(
        String.format("abfss://%s@%s.dfs.core.windows.net/samples/java/wordcount/shakespeare.txt", fileSystem, storageAccount),
        String.format("abfss://%s@%s.dfs.core.windows.net/samples/java/wordcount/result/", fileSystem, storageAccount)
    ))
    .setDriverMemory("28g")
    .setDriverCores(4)
    .setExecutorMemory("28g")
    .setExecutorCores(4)
    .setExecutorCount(2);

SparkBatchJob jobCreated = batchClient.createSparkBatchJob(options);
```

### List Spark batch jobs
`getSparkBatchJobs` enumerates the Spark batch jobs in the Synapse workspace.

```java
SparkBatchJobCollection jobs = batchClient.getSparkBatchJobs();
for (SparkBatchJob job : jobs.getSessions()) {
    System.out.println(job.getName());
}
```

### Cancel a Spark batch job

`cancelSparkBatchJob` cancels a Spark batch job by the given job ID.

```java
batchClient.cancelSparkBatchJob(jobId);
```

### Async API
The following sections provide several code snippets covering some of the most common asynchronous Azure Synapse Spark service tasks, including:

* [Create a Spark job asynchronously](#create-a-spark-batch-job-asynchronously)
* [Retrieve a Spark job asynchronously](#retrieve-a-spark-batch-job-asynchronously)
* [List Spark jobs asynchronously](#list-spark-batch-jobs-asynchronously)
* [Delete a Spark job asynchronously](#delete-a-spark-batch-job-asynchronously)

> Note : You should add `System.in.read()` or `Thread.sleep()` after the function calls in the main class/thread to allow async functions/operations to execute and finish before the main application/thread exits.

### Create a Spark job asynchronously

`createSparkBatchJob` creates a Spark batch job.

```java
String storageAccount = "<storage-account>";
String fileSystem = "<file-system>";
String name = "<job-name>";
String file = String.format("abfss://%s@%s.dfs.core.windows.net/samples/java/wordcount/wordcount.jar", fileSystem, storageAccount);
SparkBatchJobOptions options = new SparkBatchJobOptions()
    .setName(name)
    .setFile(file)
    .setClassName("WordCount")
    .setArguments(Arrays.asList(
        String.format("abfss://%s@%s.dfs.core.windows.net/samples/java/wordcount/shakespeare.txt", fileSystem, storageAccount),
        String.format("abfss://%s@%s.dfs.core.windows.net/samples/java/wordcount/result/", fileSystem, storageAccount)
    ))
    .setDriverMemory("28g")
    .setDriverCores(4)
    .setExecutorMemory("28g")
    .setExecutorCores(4)
    .setExecutorCount(2);

batchClient.createSparkBatchJob(options).subscribe(job -> System.out.printf("Job ID: %f\n", job.getId()));
```

### List Spark batch jobs asynchronously
`getSparkBatchJobs` enumerates the Spark batch jobs in the Synapse workspace.

```java
batchClient.getSparkBatchJobs().subscribe(jobs -> {
    for (SparkBatchJob job : jobs.getSessions()) {
        System.out.println(job.getName());
    }
});
```

### Cancel a Spark batch job asynchronously

`cancelSparkBatchJob` deletes a Spark batch job by the job ID.

```java
batchClient.cancelSparkBatchJob(jobId);
```

## Troubleshooting

### Default HTTP client
All client libraries by default use the Netty HTTP client. Adding the above dependency will automatically configure the client library to use the Netty HTTP client. Configuring or changing the HTTP client is detailed in the [HTTP clients wiki](https://github.com/Azure/azure-sdk-for-java/wiki/HTTP-clients).

### Default SSL library
All client libraries, by default, use the Tomcat-native Boring SSL library to enable native-level performance for SSL operations. The Boring SSL library is an Uber JAR containing native libraries for Linux / macOS / Windows, and provides better performance compared to the default SSL implementation within the JDK. For more information, including how to reduce the dependency size, refer to the [performance tuning][performance_tuning] section of the wiki.

## Next steps
Several Synapse Java SDK samples are available to you in the SDK's GitHub repository. These samples provide example code for additional scenarios commonly encountered while working with Azure Synapse Analytics.

###  Additional documentation
For more extensive documentation on Azure Synapse Analytics, see the [API reference documentation][azsynapse_rest].

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the Code of Conduct FAQ or contact <opencode@microsoft.com> with any additional questions or comments.

<!-- LINKS -->
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/synapse/azure-analytics-synapse-spark/src
[api_documentation]: https://azure.github.io/azure-sdk-for-java
[azsynapse_docs]: https://docs.microsoft.com/azure/synapse-analytics/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity
[maven]: https://maven.apache.org/
[azure_subscription]: https://azure.microsoft.com/
[azure_synapse]: https://docs.microsoft.com/azure/synapse-analytics/quickstart-create-workspace
[azure_cli]: https://docs.microsoft.com/azure/synapse-analytics/quickstart-create-workspace-cli
[rest_api]: https://docs.microsoft.com/rest/api/synapse/
[azsynapse_rest]: https://docs.microsoft.com/rest/api/synapse/
[azure_create_application_in_portal]: https://docs.microsoft.com/azure/active-directory/develop/howto-create-service-principal-portal
[azure_synapse_cli_full]: https://docs.microsoft.com/cli/azure/synapse?view=azure-cli-latest
[spark_samples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/synapse/azure-analytics-synapse-spark/src/samples/java/com/azure/analytics/synapse/spark
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning
[jdk]: https://docs.microsoft.com/java/azure/jdk/
[azure_sub]: https://azure.microsoft.com/free/
[azure_portal]: https://docs.microsoft.com/azure/synapse-analytics/quickstart-create-workspace
