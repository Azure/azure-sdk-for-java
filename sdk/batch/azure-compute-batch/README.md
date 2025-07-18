# Azure Batch client library for Java

This README is based on the latest released version of the Azure Compute Batch SDK, otherwise known as the track 2 Azure Batch Data Plane SDK.

> The SDK supports features of the Azure Batch service starting from API version **2023-05-01.16.0**. We will be adding support for more new features and tweaking the API associated with Azure Batch service newer release.

## Documentation

Various documentation is available to help you get started

- [API reference documentation][docs]
- [Product documentation][product_documentation]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-compute-batch;current})

```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-compute-batch</artifactId>
    <version>1.0.0-beta.4</version>
</dependency>
```

[//]: # ({x-version-update-end})

### Authentication

You can authenticate with Microsoft Entra ID authentication.

## Key concepts

### Azure Batch Authentication

You need to create a Batch account through the [Azure portal](https://portal.azure.com) or Azure cli.

- The preferred method is to use Entra ID authentication to create the client. See this [document](https://learn.microsoft.com/azure/batch/batch-aad-auth) for details on authenticating to Batch with Entra ID.
For example:

```java com.azure.compute.batch.build-client
BatchClient batchClient = new BatchClientBuilder().credential(new DefaultAzureCredentialBuilder().build())
    .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT"))
    .buildClient();
```

You can also create a client using Shared Key Credentials:

```java com.azure.compute.batch.build-sharedkey-client
Configuration localConfig = Configuration.getGlobalConfiguration();
String accountName = localConfig.get("AZURE_BATCH_ACCOUNT", "fakeaccount");
String accountKey = localConfig.get("AZURE_BATCH_ACCESS_KEY", "fakekey");
AzureNamedKeyCredential sharedKeyCreds = new AzureNamedKeyCredential(accountName, accountKey);

BatchClientBuilder batchClientBuilder = new BatchClientBuilder();
batchClientBuilder.credential(sharedKeyCreds);
BatchClient batchClientWithSharedKey = batchClientBuilder.buildClient();
```

### Create a pool using an Azure Marketplace image

You can create a pool of Azure virtual machines which can be used to execute tasks.

```java com.azure.compute.batch.create-pool.creates-a-simple-pool
batchClient.createPool(new BatchPoolCreateParameters("mypool001", "STANDARD_DC2s_V2")
    .setVirtualMachineConfiguration(
        new VirtualMachineConfiguration(new BatchVmImageReference().setPublisher("Canonical")
            .setOffer("UbuntuServer")
            .setSku("18_04-lts-gen2")
            .setVersion("latest"), "batch.node.ubuntu 18.04"))
    .setTargetDedicatedNodes(1), null);
```

### Create a Job

You can create a job by using the recently created pool.

```java com.azure.compute.batch.create-job.creates-a-basic-job
batchClient.createJob(
    new BatchJobCreateParameters("jobId", new BatchPoolInfo().setPoolId("poolId")).setPriority(0), null);
```

### Create a Task

Create a simple task:

```java com.azure.compute.batch.create-task.creates-a-simple-task
String taskId = "ExampleTaskId";
BatchTaskCreateParameters taskToCreate = new BatchTaskCreateParameters(taskId, "echo hello world");
batchClient.createTask("jobId", taskToCreate);
```

Or create a more complex task, one with exit conditions:

```java com.azure.compute.batch.create-task.creates-a-task-with-exit-conditions
batchClient.createTask("jobId", new BatchTaskCreateParameters("taskId", "cmd /c exit 3")
    .setExitConditions(new ExitConditions().setExitCodeRanges(Arrays
        .asList(new ExitCodeRangeMapping(2, 4, new ExitOptions().setJobAction(BatchJobActionKind.TERMINATE)))))
    .setUserIdentity(new UserIdentity().setAutoUser(
        new AutoUserSpecification().setScope(AutoUserScope.TASK).setElevationLevel(ElevationLevel.NON_ADMIN))),
    null);
```

## Sample Code

You can find sample code that illustrates Batch usage scenarios in <https://github.com/azure/azure-batch-samples>

Error handling

When a call to the batch service fails the response from that call will contain a BatchError object in the body of the response.  In the AZURE-COMPUTE-BATCH SDK when an api method is called and a failure from the server occurs the sdk will throw a HttpResponseException exception.  You can use the helper method BatchError.fromException() to extract out the BatchError object.

```java com.azure.compute.batch.resize-pool.resize-pool-error
try {
    BatchPoolResizeParameters resizeParams
        = new BatchPoolResizeParameters().setTargetDedicatedNodes(1).setTargetLowPriorityNodes(1);
    batchClient.resizePool("fakepool", resizeParams);
} catch (BatchErrorException err) {
    BatchError error = err.getValue();
    Assertions.assertNotNull(error);
    Assertions.assertEquals("PoolNotFound", error.getCode());
    Assertions.assertTrue(error.getMessage().getValue().contains("The specified pool does not exist."));
    Assertions.assertNull(error.getValues());
}
```

## Help

If you encounter any bugs with these libraries, please file issues via [Issues](https://github.com/Azure/azure-sdk-for-java) or check out [StackOverflow for Azure Java SDK](https://stackoverflow.com/questions/tagged/azure-java-sdk).

## Troubleshooting

Please see [Troubleshooting common batch issues](https://learn.microsoft.com/troubleshoot/azure/hpc/batch/welcome-hpc-batch).

Consult the Full Documentation: The full documentation is available at <https://learn.microsoft.com/azure/batch/>.

Check the Error Code and Consult Documentation: The Batch service utilizes specific error codes that may be returned in the error response when a request fails due to various reasons. For a comprehensive list of error codes, their meanings, and detailed troubleshooting steps, refer to the Azure Batch service error codes documentation: <https://learn.microsoft.com/rest/api/batchservice/batch-status-and-error-codes>

Review Your Request Parameters: Errors such as InvalidPropertyValue and MissingRequiredProperty indicate that there might be a mistake in the request payload. Review your parameters to ensure they meet the API specifications.

Manage Resources: For errors related to limits and quotas (like AccountCoreQuotaReached), consider scaling down your usage or requesting an increase in your quota.

Check Azure Service Health: Sometimes, the issue may be with Azure services rather than your application. Check the Azure Status Dashboard for any ongoing issues that might be affecting Batch services.

Handle Transient Errors: Implement retry logic in your application to handle transient failures in Batch.

## Contributing

This project welcomes contributions and suggestions.
Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution.
For details, visit [Contributor License Agreements](https://opensource.microsoft.com/cla/).

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment).
Simply follow the instructions provided by the bot.
You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/).
For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.
