# Guide for Migrating to `Azure-Compute-Batch` from `Microsoft-Azure-Batch` (Java)

This guide is intended to assist customers in migrating to the new Java SDK package, `Azure-Compute-Batch` from the legacy `Microsoft-Azure-Batch` package. It provides side‐by‐side comparisons of similar operations between the two versions. Familiarity with the legacy client library is assumed. For newcomers, please refer to the [README for Azure-Compute-Batch](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/batch/azure-compute-batch/README.md) and the [legacy README for Microsoft-Azure-Batch](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/batch/microsoft-azure-batch/README.md).

To view the latest version of the package, [visit this link](https://central.sonatype.com/artifact/com.azure/azure-compute-batch/overview)

> **Note:** The legacy `Microsoft-Azure-Batch` package is deprecated. Upgrade to `Azure-Compute-Batch` for continued support and new features.

## Table of Contents

- [Overview](#overview)
  - [Migration Benefits](#migration-benefits)
  - [Azure-Compute-Batch Differences](#azure-compute-batch-differences)
- [Constructing the Clients](#constructing-the-clients)
  - [Authenticate with Microsoft Entra ID](#authenticate-with-microsoft-entra-id)
  - [Authenticate with Shared Key Credentials](#authenticate-with-shared-key-credentials)
- [Error Handling](#error-handling)
- [Operations Examples](#operations-examples)
  - [Pool Operations](#pool-operations)
    - [Create Pool](#create-pool)
    - [Get Pool](#get-pool)
    - [List Pools](#list-pools)
    - [Delete Pool](#delete-pool)
    - [Update Pool](#update-pool)
    - [Resize Pool](#resize-pool)
    - [Stop Resize Pool](#stop-resize-pool)
    - [Enable AutoScale Pool](#enable-autoscale-pool)
    - [Disable AutoScale Pool](#disable-autoscale-pool)
    - [Evaluate AutoScale Pool](#evaluate-autoscale-pool)
    - [List Pool Node Counts](#list-pool-node-counts)
    - [List Pool Usage Metrics](#list-pool-usage-metrics)
    - [List Supported Images](#list-supported-images)
    - [Remove Nodes](#remove-nodes)
  - [Job Operations](#job-operations)
    - [Create Job](#create-job)
    - [Get Job](#get-job)
    - [List Jobs](#list-jobs)
    - [Delete Job](#delete-job)
    - [Replace Job](#replace-job)
    - [Update Job](#update-job)
    - [Disable Job](#disable-job)
    - [Enable Job](#enable-job)
    - [List Job Preparation and Release Task Status](#list-job-preparation-and-release-task-status)
    - [Get Job Task Counts](#get-job-task-counts)
    - [Terminate Job](#terminate-job)
  - [Job Schedule Operations](#job-schedule-operations)
    - [Create Job Schedule](#create-job-schedule)
    - [Get Job Schedule](#get-job-schedule)
    - [List Job Schedules](#list-job-schedules)
    - [Delete Job Schedule](#delete-job-schedule)
    - [Replace Job Schedule](#replace-job-schedule)
    - [Update Job Schedule](#update-job-schedule)
    - [Disable Job Schedule](#disable-job-schedule)
    - [Enable Job Schedule](#enable-job-schedule)
    - [Terminate Job Schedule](#terminate-job-schedule)
  - [Task Operations](#task-operations)
    - [Create Tasks](#create-tasks)
    - [Get Task](#get-task)
    - [List Tasks](#list-tasks)
    - [Delete Task](#delete-task)
    - [Replace Task](#replace-task)
    - [Reactivate Task](#reactivate-task)
    - [Terminate Task](#terminate-task)
    - [File Operations for Tasks](#file-operations-for-tasks)
    - [List Task Files](#list-task-files)
    - [Get Task File](#get-task-file)
    - [Get Task File Properties](#get-task-file-properties)
  - [Node Operations](#node-operations)
    - [Get Node](#get-node)
    - [List Nodes](#list-nodes)
    - [Deallocate Node](#deallocate-node)
    - [Reimage Node](#reimage-node)
    - [Start Node](#start-node)
    - [Reboot Node](#reboot-node)
    - [Create Node User](#create-node-user)
    - [Delete Node User](#delete-node-user)
    - [Get Node File](#get-node-file)
    - [List Node Files](#list-node-files)
    - [Delete Node File](#delete-node-file)
    - [Get Node File Properties](#get-node-file-properties)
    - [Get Remote Login Settings](#get-remote-login-settings)
    - [Upload Node Logs](#upload-node-logs)
  - [Certificate Operations](#certificate-operations)
    - [Create Certificate](#create-certificate)
    - [Get Certificate](#get-certificate)
    - [List Certificates](#list-certificates)
    - [Delete Certificate](#delete-certificate)
    - [Cancel Delete Certificate](#cancel-delete-certificate)
  - [Application Operations](#application-operations)
    - [Get Application](#get-application)
    - [List Applications](#list-applications)

## Overview

### Migration Benefits

Migrating to `Azure-Compute-Batch` offers a more consistent and modern programming experience with benefits including:

- A consolidated client that exposes all operations directly.
- Both synchronous and asynchronous (Reactor-based) APIs.
- Improved authentication support via Microsoft Entra ID or shared key.
- Enhanced error handling with richer response details.

### Azure-Compute-Batch Differences

Key differences between the two packages:

- **Naming Changes:** Many classes and method names have been updated (for example, `CloudPool` is now `BatchPool`).
- **API Consolidation:** Instead of separate operation classes (e.g., `JobOperations`, `PoolOperations`), all operations are now methods on the synchronous client `BatchClient` (or the asynchronous client `BatchAsyncClient`).
- **Immediate Operations:** Creating a resource (such as a pool) immediately issues the operation instead of requiring a separate commit.
- **Reactive Support:** Asynchronous operations return Reactor types such as `Mono` and `Flux`.

## Constructing the Clients

We strongly recommend using Microsoft Entra ID for Batch account authentication. Some Batch capabilities require this method of authentication, including many of the security-related features discussed here. The service API authentication mechanism for a Batch account can be restricted to only Microsoft Entra ID using the [allowedAuthenticationModes](https://learn.microsoft.com/rest/api/batchmanagement/batch-account/create?view=rest-batchmanagement-2024-02-01&tabs=HTTP) property. When this property is set, API calls using Shared Key authentication will be rejected.

### Authenticate with Microsoft Entra ID

The preferred approach is to use the Azure Identity library’s `DefaultAzureCredential`. Here is a code snippet that builds an instance of the synchronous `BatchClient`:

```java com.azure.compute.batch.build-client
BatchClient batchClient = new BatchClientBuilder().credential(new DefaultAzureCredentialBuilder().build())
    .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT"))
    .buildClient();
```

You can also build an instance of the asynchronous client (`BatchAsyncClient`) if you want to perform any operations asynchronously:

```java com.azure.compute.batch.build-async-client
BatchAsyncClient batchAsyncClient = new BatchClientBuilder().credential(new DefaultAzureCredentialBuilder().build())
    .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT"))
    .buildAsyncClient();
```

### Authenticate with Shared Key Credentials

Alternatively, authenticate using an `AzureNamedKeyCredential`.

```java com.azure.compute.batch.build-sharedkey-client
Configuration localConfig = Configuration.getGlobalConfiguration();
String accountName = localConfig.get("AZURE_BATCH_ACCOUNT", "fakeaccount");
String accountKey = localConfig.get("AZURE_BATCH_ACCESS_KEY", "fakekey");
AzureNamedKeyCredential sharedKeyCreds = new AzureNamedKeyCredential(accountName, accountKey);

BatchClientBuilder batchClientBuilder = new BatchClientBuilder();
batchClientBuilder.credential(sharedKeyCreds);
// You can build both the sync and async clients with this configuration
BatchClient batchClientWithSharedKey = batchClientBuilder.buildClient();
BatchAsyncClient batchAsyncClientWithSharedKey = batchClientBuilder.buildAsyncClient();
```

## Error Handling

In `Azure-Compute-Batch`, server errors throw exceptions such as `BatchErrorException`, the custom Batch error object. For example:

```java com.azure.compute.batch.resize-pool.resize-pool-error
try {
    BatchPoolResizeParameters resizeParams
        = new BatchPoolResizeParameters().setTargetDedicatedNodes(1).setTargetLowPriorityNodes(1);
    batchClient.beginResizePool("fakepool", resizeParams);
} catch (BatchErrorException err) {
    BatchError error = err.getValue();
    Assertions.assertNotNull(error);
    Assertions.assertEquals("PoolNotFound", error.getCode());
    Assertions.assertTrue(error.getMessage().getValue().contains("The specified pool does not exist."));
    Assertions.assertNull(error.getValues());
}
```

## Operations Examples

The sections below compare common operations between the legacy SDK (Microsoft-Azure-Batch) and and this new SDK (Azure-Compute-Batch). While most of these examples use the synchronous `BatchClient`, you can call these operations on the asynchronous `BatchAsyncClient` as well.

### Pool Operations

#### Create Pool

Previously, in `Microsoft-Azure-Batch`, to create a pool, you would call the `createPool` method from the `PoolOperations` object.

```java
String poolId = "samplePoolId";

// Create a pool with 3 Small VMs
String POOL_VM_SIZE = "STANDARD_D1_V2";
int POOL_VM_COUNT = 2;
int POOL_LOW_PRI_VM_COUNT = 2;

// Create the pool if it doesn't exist
if (!batchClient.poolOperations().existsPool(poolId)) {
    ImageReference imgRef = new ImageReference().withPublisher("Canonical").withOffer("UbuntuServer")
        .withSku("22_04-lts").withVersion("latest");
    VirtualMachineConfiguration configuration = new VirtualMachineConfiguration();
    configuration.withNodeAgentSKUId("batch.node.ubuntu 22.04").withImageReference(imgRef);

    NetworkConfiguration netConfig = createNetworkConfiguration();
    PoolEndpointConfiguration endpointConfig = new PoolEndpointConfiguration();
    List<InboundNATPool> inbounds = new ArrayList<>();
    inbounds.add(new InboundNATPool().withName("testinbound").withProtocol(InboundEndpointProtocol.TCP)
        .withBackendPort(5000).withFrontendPortRangeStart(60000).withFrontendPortRangeEnd(60040));
    endpointConfig.withInboundNATPools(inbounds);
    netConfig.withEndpointConfiguration(endpointConfig).withEnableAcceleratedNetworking(true);

    PoolAddParameter addParameter = new PoolAddParameter().withId(poolId)
        .withTargetDedicatedNodes(POOL_VM_COUNT).withTargetLowPriorityNodes(POOL_LOW_PRI_VM_COUNT)
        .withVmSize(POOL_VM_SIZE).withVirtualMachineConfiguration(configuration)
        .withNetworkConfiguration(netConfig)
        .withTargetNodeCommunicationMode(NodeCommunicationMode.DEFAULT);
    batchClient.poolOperations().createPool(addParameter);
}
```

Going forward, Azure Batch has two SDKs: `Azure-Compute-Batch`[https://learn.microsoft.com/java/api/com.azure.compute.batch?view=azure-java-preview] (this one- also known as the dataplane SDK), which interacts directly with the Azure Batch service, and `Azure-ResourceManager-Batch` [https://learn.microsoft.com/java/api/com.azure.resourcemanager.batch?view=azure-java-stable], which interacts with ARM (Azure Resource Manager) and is known as the management plane SDK. Both SDKs support Batch Pool operations such as create, get, update, list, but only the `Azure-ResourceManager-Batch` SDK can create a pool with managed identities, and for that reason it is the recommended way to create a pool.

Here is how to [create a pool](https://learn.microsoft.com/java/api/com.azure.resourcemanager.batch.models.pools?view=azure-java-stable#com-azure-resourcemanager-batch-models-pools-define(java-lang-string)) using the `Azure-ResourceManager-Batch` SDK (recommended):

```java
Pool pool = batchManager.pools()
    .define(poolName)
    .withExistingBatchAccount(resourceGroup, batchAccountName)
    .withDisplayName(poolDisplayName)
    .withDeploymentConfiguration(
        new DeploymentConfiguration()
            .withVirtualMachineConfiguration(
                new VirtualMachineConfiguration()
                    .withImageReference(new ImageReference().withPublisher("Canonical")
                        .withOffer("UbuntuServer").withSku("22_04-lts").withVersion("latest"))
                    .withNodeAgentSkuId("batch.node.ubuntu 22.04")))
    .withScaleSettings(
        new ScaleSettings()
            .withFixedScale(
                new FixedScaleSettings()
                    .withResizeTimeout(Duration.parse("PT8M"))
                    .withTargetDedicatedNodes(1)
                    .withTargetLowPriorityNodes(1)
                    .withNodeDeallocationOption(ComputeNodeDeallocationOption.TASK_COMPLETION)))
    .withVmSize("Standard_D1")
    .create();
```

As mentioned, you can still create a pool using `Azure-Compute-Batch`, just without support for managed identities.

```java com.azure.compute.batch.create-pool.creates-a-simple-pool
batchClient.createPool(new BatchPoolCreateParameters("poolId", "STANDARD_DC2s_V2")
    .setVirtualMachineConfiguration(
        new VirtualMachineConfiguration(new BatchVmImageReference().setPublisher("Canonical")
            .setOffer("UbuntuServer")
            .setSku("22_04-lts")
            .setVersion("latest"), "batch.node.ubuntu 22.04"))
    .setTargetDedicatedNodes(1), null);
```

For more information on code snippets and samples relating to using the management plane SDK, please visit these links:
[Management Plane Code Snippets and Samples](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/batch/azure-resourcemanager-batch/SAMPLE.md)
[API documentation for com.azure.resourcemanager.batch](https://learn.microsoft.com/java/api/com.azure.resourcemanager.batch?view=azure-java-stable)
[azure-resourcemanager-batch SDK](https://central.sonatype.com/artifact/com.azure.resourcemanager/azure-resourcemanager-batch/overview)

Please note that the rest of the examples in this README will all be dataplane operations.

#### Get Pool

Previously, in `Microsoft-Azure-Batch`, to get a pool, you could call the `getPool` method from the `PoolOperations` object.

```java
CloudPool pool = batchClient.poolOperations().getPool("poolId");
```

With `Azure-Compute-Batch`, you can call `getPool` directly on the client.

```java com.azure.compute.batch.get-pool.pool-get
BatchPool pool = batchClient.getPool("poolId");
```

```java com.azure.compute.batch.pool.get-pool-async
batchAsyncClient.getPool("poolId").subscribe(asyncPool -> {
    // Use the pool here
    System.out.println("Pool ID: " + asyncPool.getId());
});
```

#### List Pools

Previously, in `Microsoft-Azure-Batch`, to get a list of pools, you could call the `listPools` method from the `PoolOperations` object.

```java
List<CloudPool> pools = new ArrayList<>(batchClient.poolOperations().listPools());
for (CloudPool pool : pools) {
    System.out.println(pool.getId());
}
```

With `Azure-Compute-Batch`, you can call `listPools` directly on the client.

```java com.azure.compute.batch.list-pools.pool-list
PagedIterable<BatchPool> poolList = batchClient.listPools();
```

#### Delete Pool

Previously, in `Microsoft-Azure-Batch`, to delete a pool, you could call the `deletePool` method from the `PoolOperations` object.

```java
batchClient.poolOperations().deletePool("poolId");
```

With `Azure-Compute-Batch`, you can call `beginDeletePool` directly on the client. It is also now an LRO (Long Running Operation).

Here are examples for the synchronous and asynchronous client of how to simply issue the operation:

```java com.azure.compute.batch.pool.delete-pool-simple
SyncPoller<BatchPool, Void> deletePoolPoller = batchClient.beginDeletePool("poolId");
```

```java com.azure.compute.batch.pool.delete-pool-async-simple
batchAsyncClient.beginDeletePool("poolId").subscribe();
```

Here are examples for the synchronous and asynchronous client of how to wait for the polling to finish and retrieve the final result:

```java com.azure.compute.batch.pool.delete-pool-complex
SyncPoller<BatchPool, Void> complexDeletePoolPoller = batchClient.beginDeletePool("poolId");
PollResponse<BatchPool> finalDeletePoolResponse = complexDeletePoolPoller.waitForCompletion();
```

```java com.azure.compute.batch.pool.async.delete-pool-async-complex
batchAsyncClient.beginDeletePool("poolId")
    .takeUntil(pollResponse -> pollResponse.getStatus().isComplete())
    .last()
    .subscribe(finalPollResponse -> {
        System.out.println("Pool deletion completed with status: " + finalPollResponse.getStatus());
    });
```

#### Update Pool

Previously, in `Microsoft-Azure-Batch`, to patch a pool, you could call the `patchPool` method from the `PoolOperations` object.

```java
// Update the pool's metadata.
List<MetadataItem> metadata = new ArrayList<>();
metadata.add(new MetadataItem("name", "value"));

// The null values indicate that StartTask, CertificateReferences, and ApplicationPackageReferences remain unchanged.
batchClient.poolOperations().patchPool("poolId", null, null, null, metadata);
```

With `Azure-Compute-Batch`, you can call `updatePool` directly on the client.

```java com.azure.compute.batch.update-pool.patch-the-pool
batchClient.updatePool("poolId",
    new BatchPoolUpdateParameters().setStartTask(new BatchStartTask("/bin/bash -c 'echo start task'")), null,
    null);
```

#### Resize Pool

Previously, in `Microsoft-Azure-Batch`, to resize a pool, you could call the `resizePool` method from the `PoolOperations` object.

```java
batchClient.poolOperations().resizePool(poolId, 1, 1);
```

With `Azure-Compute-Batch`, you can call `beginResizePool` directly on the client. It is also now an LRO (Long Running Operation).

```java com.azure.compute.batch.resize-pool.pool-resize
BatchPoolResizeParameters resizeParameters = new BatchPoolResizeParameters().setTargetDedicatedNodes(1).setTargetLowPriorityNodes(1);
SyncPoller<BatchPool, BatchPool> resizePoller = batchClient.beginResizePool("poolId", resizeParameters);

// Inspect first poll
PollResponse<BatchPool> resizeFirst = resizePoller.poll();
if (resizeFirst.getStatus() == LongRunningOperationStatus.IN_PROGRESS) {
    BatchPool poolDuringResize = resizeFirst.getValue();
}

// Wait for completion
resizePoller.waitForCompletion();

// Final pool after resize
BatchPool resizedPool = resizePoller.getFinalResult();
```

#### Stop Resize Pool

Previously, in `Microsoft-Azure-Batch`, to stop resizing a pool, you could call the `stopResizePool` method from the `PoolOperations` object.

```java
batchClient.poolOperations().stopResizePool("poolId");
```

With `Azure-Compute-Batch`, you can call `beginStopPoolResize` directly on the client. It is also now an LRO (Long Running Operation).

```java com.azure.compute.batch.stop-resize-pool.stop-pool-resize
SyncPoller<BatchPool, BatchPool> stopPoller = batchClient.beginStopPoolResize("poolId");

// First poll
PollResponse<BatchPool> stopFirst = stopPoller.poll();
if (stopFirst.getStatus() == LongRunningOperationStatus.IN_PROGRESS && stopFirst.getValue() != null) {
    AllocationState interim = stopFirst.getValue().getAllocationState();
}

// Wait for completion
stopPoller.waitForCompletion();
BatchPool stoppedPool = stopPoller.getFinalResult();
```

#### Enable AutoScale Pool

Previously, in `Microsoft-Azure-Batch`, to enable auto scale on a pool, you could call the `enableAutoScale` method from the `PoolOperations` object.

```java
batchClient.poolOperations().enableAutoScale("poolId", "$TargetDedicatedNodes=0;$TargetLowPriorityNodes=0;$NodeDeallocationOption=requeue");
```

With `Azure-Compute-Batch`, you can call `enablePoolAutoScale` directly on the client and pass in a `BatchPoolEnableAutoScaleParameters` object.

```java com.azure.compute.batch.enable-pool-auto-scale.pool-enable-autoscale
BatchPoolEnableAutoScaleParameters autoScaleParameters = new BatchPoolEnableAutoScaleParameters()
    .setAutoScaleEvaluationInterval(Duration.ofMinutes(6))
    .setAutoScaleFormula("$TargetDedicated = 1;");

batchClient.enablePoolAutoScale("poolId", autoScaleParameters);
```

#### Disable AutoScale Pool

Previously, in `Microsoft-Azure-Batch`, to disable auto scale on a pool, you could call the `disableAutoScale` method from the `PoolOperations` object.

```java
batchClient.poolOperations().disableAutoScale("poolId");
```

With `Azure-Compute-Batch`, you can call `disablePoolAutoScale` directly on the client.

```java com.azure.compute.batch.disable-pool-auto-scale.disable-pool-autoscale
batchClient.disablePoolAutoScale("poolId");
```

#### Evaluate AutoScale Pool

Previously, in `Microsoft-Azure-Batch`, to get the result of evaluating an autoscale formula on a pool, you could call the `evaluateAutoScale` method from the `PoolOperations` object.

```java
AutoScaleRun eval = batchClient.poolOperations().evaluateAutoScale("poolId", "$TargetDedicatedNodes=1;");
```

With `Azure-Compute-Batch`, you can call `evaluatePoolAutoScale` directly on the client.

```java com.azure.compute.batch.evaluate-pool-auto-scale.evaluate-pool-autoscale
BatchPoolEvaluateAutoScaleParameters evalParams = new BatchPoolEvaluateAutoScaleParameters("$TargetDedicated = 1;");
AutoScaleRun eval = batchClient.evaluatePoolAutoScale("poolId", evalParams);
```

#### List Pool Node Counts

Previously, in `Microsoft-Azure-Batch`, to list pool node counts, you could call the `listPoolNodeCounts` method from the `PoolOperations` object.

```java
batchClient.poolOperations().listPoolNodeCounts()
```

With `Azure-Compute-Batch`, you can call `listPoolNodeCounts` directly on the client.

```java com.azure.compute.batch.list-pool-node-counts.list-pool-node-counts
batchClient.listPoolNodeCounts();
```

#### List Pool Usage Metrics

Previously, in `Microsoft-Azure-Batch`, to list pool usage metrics, you could call the `listPoolUsageMetrics` method from the `PoolOperations` object.

```java
batchClient.poolOperations().listPoolUsageMetrics(startTime, endTime);
```

With `Azure-Compute-Batch`, you can call `listPoolUsageMetrics` directly on the client.

```java com.azure.compute.batch.list-pool-usage-metrics.list-pool-usage-metrics
batchClient.listPoolUsageMetrics();
```

#### List Supported Images

Previously, in `Microsoft-Azure-Batch`, to get a list of supported images, you could call the `listSupportedImages` method from the `PoolOperations` object.

```java
List<ImageInformation> images = batchClient.poolOperations().listSupportedImages();
```

With `Azure-Compute-Batch`, you can call `listSupportedImages` directly on the client.

```java com.azure.compute.batch.list-supported-images.list-supported-images
batchClient.listSupportedImages();
```

#### Remove Nodes

Previously, in `Microsoft-Azure-Batch`, to remove nodes from a pool, you could call the `listComputeNodes` method from the `PoolOperations` object.

```java
// List all compute nodes in the pool
List<ComputeNode> nodes = batchClient.computeNodeOperations().listComputeNodes(poolId);
String nodeIdB = nodes.get(1).id(); // get node ID

// Call removeNodeFromPool (single node version)
batchClient.poolOperations().removeNodeFromPool(
    poolId,
    nodeIdB,
    ComputeNodeDeallocationOption.TASK_COMPLETION
);
```

With `Azure-Compute-Batch`, you can call `beginRemoveNodes` directly on the client. It is also now an LRO (Long Running Operation).

```java com.azure.compute.batch.pool.remove-nodes
List<BatchNode> nodes = new ArrayList<>();
batchClient.listNodes("poolId").forEach(nodes::add);
String nodeIdB = nodes.get(1).getId();
BatchNodeRemoveParameters removeParams = new BatchNodeRemoveParameters(Collections.singletonList(nodeIdB))
        .setNodeDeallocationOption(BatchNodeDeallocationOption.TASK_COMPLETION);

SyncPoller<BatchPool, BatchPool> removePoller = batchClient.beginRemoveNodes("poolId", removeParams);

// First poll response
PollResponse<BatchPool> removeFirst = removePoller.poll();

// Final result
removePoller.waitForCompletion();
BatchPool poolAfterRemove = removePoller.getFinalResult();
```

### Job Operations

#### Create Job

Previously, in `Microsoft-Azure-Batch`, to create a job, you could call the `createJob` method from the `JobOperations` object.

```java
String jobId = "jobId";

PoolInformation poolInfo = new PoolInformation();
poolInfo.withPoolId(poolId);
batchClient.jobOperations().createJob(jobId, poolInfo);
```

With `Azure-Compute-Batch`, you can call `createJob` with a parameter of type `BatchJobCreateParameters` directly on the client.

```java com.azure.compute.batch.create-job.creates-a-basic-job
batchClient.createJob(
    new BatchJobCreateParameters("jobId", new BatchPoolInfo().setPoolId("poolId")).setPriority(0), null);
```

```java com.azure.compute.batch.create-job.creates-a-basic-job-async
batchAsyncClient.createJob(
    new BatchJobCreateParameters("jobId", new BatchPoolInfo().setPoolId("poolId")).setPriority(0))
    .subscribe(unused -> System.out.println("Job created successfully"));
```

#### Get Job

Previously, in `Microsoft-Azure-Batch`, to get an already created job, you could call the `getJob` method from the `JobOperations` object.

```java
CloudJob job = batchClient.jobOperations().getJob(jobId);
```

With `Azure-Compute-Batch`, you can call `getJob` directly on the client.

```java com.azure.compute.batch.get-job.job-get
BatchJob job = batchClient.getJob("jobId", null, null);
```

#### List Jobs

Previously, in `Microsoft-Azure-Batch`, to get a list of all the jobs on the account, you could call the `getJob` method from the `JobOperations` object.

```java
List<CloudJob> jobs = batchClient.jobOperations().listJobs();
```

With `Azure-Compute-Batch`, you can call `listJobs` directly on the client.

```java com.azure.compute.batch.list-jobs.job-list
PagedIterable<BatchJob> jobList = batchClient.listJobs(new BatchJobsListOptions());
```

#### Delete Job

Previously, in `Microsoft-Azure-Batch`, to delete a job, you could call the `deleteJob` method from the `JobOperations` object.

```java
batchClient.jobOperations().deleteJob("jobId");
```

With `Azure-Compute-Batch`, you can call `beginDeleteJob` directly on the client. It is also now an LRO (Long Running Operation).

```java com.azure.compute.batch.delete-job.job-delete
SyncPoller<BatchJob, Void> deleteJobPoller = batchClient.beginDeleteJob("jobId");

PollResponse<BatchJob> initialDeleteJobResponse = deleteJobPoller.poll();
if (initialDeleteJobResponse.getStatus() == LongRunningOperationStatus.IN_PROGRESS) {
    BatchJob jobDuringPoll = initialDeleteJobResponse.getValue();
}

// Wait for LRO to finish
deleteJobPoller.waitForCompletion();
PollResponse<BatchJob> finalDeleteJobResponse = deleteJobPoller.poll();
```

#### Replace Job

(Known as `UpdateJob` in `Microsoft-Azure-Batch`, `ReplaceJob` in `Azure-Compute-Batch`)

Previously, in `Microsoft-Azure-Batch`, to replace a job, you could call the `updateJob` method from the `JobOperations` object.

```java
PoolInformation poolInfo = new PoolInformation();
batchClient.jobOperations().updateJob(jobId, poolInfo, 1, null, null, null);
```

With `Azure-Compute-Batch`, you can call `replaceJob` directly on the client.

```java com.azure.compute.batch.replace-job.job-patch
batchClient.replaceJob("jobId",
    new BatchJob(new BatchPoolInfo().setPoolId("poolId")).setPriority(100)
        .setConstraints(
            new BatchJobConstraints().setMaxWallClockTime(Duration.parse("PT1H")).setMaxTaskRetryCount(-1)),
    null, null);
```

#### Update Job

(Known as `PatchJob` in `Microsoft-Azure-Batch`, `UpdateJob` in `Azure-Compute-Batch`)

Previously, in `Microsoft-Azure-Batch`, to update a job, you could call the `patchJob` method from the `JobOperations` object.

```java
batchClient.jobOperations().patchJob(jobId, OnAllTasksComplete.TERMINATE_JOB);
```

With `Azure-Compute-Batch`, you can call `updateJob` directly on the client.

```java com.azure.compute.batch.update-job.job-update
batchClient.updateJob("jobId",
    new BatchJobUpdateParameters().setPriority(100)
        .setConstraints(
            new BatchJobConstraints().setMaxWallClockTime(Duration.parse("PT1H")).setMaxTaskRetryCount(-1))
        .setPoolInfo(new BatchPoolInfo().setPoolId("poolId")),
    null, null);
```

#### Disable Job

Previously, in `Microsoft-Azure-Batch`, to update a job, you could call the `disableJob` method from the `JobOperations` object.

```java
batchClient.jobOperations().disableJob("jobId", DisableJobOption.REQUEUE.REQUEUE);
```

With `Azure-Compute-Batch`, you can call `beginDisableJob` directly on the client. It is also now an LRO (Long Running Operation).

```java com.azure.compute.batch.disable-job.job-disable
BatchJobDisableParameters disableParams = new BatchJobDisableParameters(DisableBatchJobOption.REQUEUE);
SyncPoller<BatchJob, BatchJob> disablePoller = batchClient.beginDisableJob("jobId", disableParams);

 // Inspect first poll
PollResponse<BatchJob> disableFirst = disablePoller.poll();
if (disableFirst.getStatus() == LongRunningOperationStatus.IN_PROGRESS) {
    BatchJob disableDuringPoll = disableFirst.getValue();
}

// Wait for completion of operation
disablePoller.waitForCompletion();
BatchJob disabledJob = disablePoller.getFinalResult();
```

#### Enable Job

Previously, in `Microsoft-Azure-Batch`, to update a job, you could call the `enableJob` method from the `JobOperations` object.

```java
batchClient.jobOperations().enableJob(jobId);
```

With `Azure-Compute-Batch`, you can call `beginEnableJob` directly on the client.

```java com.azure.compute.batch.enable-job.job-enable
SyncPoller<BatchJob, BatchJob> enablePoller = batchClient.beginEnableJob("jobId");

// Inspect first poll
PollResponse<BatchJob> enableFirst = enablePoller.poll();
if (enableFirst.getStatus() == LongRunningOperationStatus.IN_PROGRESS) {
    BatchJob enableDuringPoll = enableFirst.getValue();
}

// Wait for completion of operation
enablePoller.waitForCompletion();
BatchJob enabledJob = enablePoller.getFinalResult();
```

#### List Job Preparation and Release Task Status

Previously, in `Microsoft-Azure-Batch`, to list the execution status of the job preparation and job release task for a job, you could call the `listPreparationAndReleaseTaskStatus` method from the `JobOperations` object.

```java
batchClient.jobOperations().listPreparationAndReleaseTaskStatus("jobId");
```

With `Azure-Compute-Batch`, you can call `listJobPreparationAndReleaseTaskStatus` directly on the client.

```java com.azure.compute.batch.job.list-job-preparation-and-release-task-status
batchClient.listJobPreparationAndReleaseTaskStatus("jobId");
```

#### Get Job Task Counts

Previously, in `Microsoft-Azure-Batch`, to get the task counts for a job, you could call the `getTaskCounts` method from the `JobOperations` object.

```java
TaskCounts counts = batchClient.jobOperations().getTaskCounts("jobId");
```

With `Azure-Compute-Batch`, you can call `getJobTaskCounts` directly on the client.

```java com.azure.compute.batch.job.get-job-task-counts
BatchTaskCountsResult counts = batchClient.getJobTaskCounts("jobId");
```

#### Terminate Job

Previously, in `Microsoft-Azure-Batch`, to terminate a job, you could call the `terminateJob` method from the `JobOperations` object.

```java
batchClient.jobOperations().terminateJob("jobId");
```

With `Azure-Compute-Batch`, you can call `beginTerminateJob` directly on the client. It is also now an LRO (Long Running Operation).

Here are examples for the synchronous and asynchronous client of how to simply issue the operation:

```java com.azure.compute.batch.job.terminate-job.simple
BatchJobTerminateParameters terminateParams = new BatchJobTerminateParameters().setTerminationReason("ExampleReason");
BatchJobTerminateOptions terminateOptions = new BatchJobTerminateOptions().setParameters(terminateParams);

SyncPoller<BatchJob, BatchJob> terminatePoller = batchClient.beginTerminateJob("jobId", terminateOptions, null);
```

```java com.azure.compute.batch.job.terminate-job.async.simple
batchAsyncClient.beginTerminateJob("jobId", terminateOptions, null).subscribe();
```

Here are examples for the synchronous and asynchronous client of how to wait for the polling to finish and retrieve the final result:

```java com.azure.compute.batch.job.terminate-job.final
SyncPoller<BatchJob, BatchJob> terminateJobPoller = batchClient.beginTerminateJob("jobId", terminateOptions, null);
BatchJob terminatedJob = terminateJobPoller.waitForCompletion().getValue();
```

```java com.azure.compute.batch.job.terminate-job.async.final
batchAsyncClient.beginTerminateJob("jobId", terminateOptions, null)
    .takeUntil(response -> response.getStatus().isComplete())
    .last()
    .subscribe(finalResponse -> {
        BatchJob asyncTerminatedJob = finalResponse.getValue();
        System.out.println("Job termination completed. Final job state: " + asyncTerminatedJob.getState());
    });
```

Here are examples for the synchronous and asynchronous client of how to kick off the operation, poll for an intermediate result, and then retrieve the final result:

```java com.azure.compute.batch.job.terminate-job.poll-intermediate
terminateJobPoller = batchClient.beginTerminateJob("jobId", terminateOptions, null);

PollResponse<BatchJob> firstTerminateJobPoller = terminateJobPoller.poll();
if (firstTerminateJobPoller.getStatus() == LongRunningOperationStatus.IN_PROGRESS) {
    BatchJob inProgressJob = firstTerminateJobPoller.getValue();
    System.out.println("Current job state: " + inProgressJob.getState());
}

terminatedJob = terminatePoller.waitForCompletion().getValue();
```

```java com.azure.compute.batch.job.terminate-job.poll-intermediate
terminateJobPoller = batchClient.beginTerminateJob("jobId", terminateOptions, null);

PollResponse<BatchJob> firstTerminateJobPoller = terminateJobPoller.poll();
if (firstTerminateJobPoller.getStatus() == LongRunningOperationStatus.IN_PROGRESS) {
    BatchJob inProgressJob = firstTerminateJobPoller.getValue();
    System.out.println("Current job state: " + inProgressJob.getState());
}

terminatedJob = terminatePoller.waitForCompletion().getValue();
```

### Job Schedule Operations

#### Create Job Schedule

Previously, in `Microsoft-Azure-Batch`, to create a job schedule, you could call the `createJobSchedule` method from the `JobScheduleOperations` object.

```java
PoolInformation poolInfo = new PoolInformation();
poolInfo.withPoolId(poolId);

JobSpecification spec = new JobSpecification().withPriority(100).withPoolInfo(poolInfo);
Schedule schedule = new Schedule().withDoNotRunUntil(DateTime.now()).withDoNotRunAfter(DateTime.now().plusHours(5)).withStartWindow(Period.days(5));
batchClient.jobScheduleOperations().createJobSchedule("jobScheduleId", schedule, spec);
```

With `Azure-Compute-Batch`, you can call `createJobSchedule` directly on the client.

```java com.azure.compute.batch.create-job-schedule.creates-a-basic-job-schedule
batchClient.createJobSchedule(new BatchJobScheduleCreateParameters("jobScheduleId",
    new BatchJobScheduleConfiguration().setRecurrenceInterval(Duration.parse("PT5M")),
    new BatchJobSpecification(new BatchPoolInfo().setPoolId("poolId"))), null);
```

#### Get Job Schedule

Previously, in `Microsoft-Azure-Batch`, to get a job schedule, you could call the `getJobSchedule` method from the `JobScheduleOperations` object.

```java
CloudJobSchedule jobSchedule = batchClient.jobScheduleOperations().getJobSchedule("jobScheduleId");
```

With `Azure-Compute-Batch`, you can call `getJobSchedule` directly on the client.

```java com.azure.compute.batch.job-schedule.get-job-schedule
batchClient.getJobSchedule("jobScheduleId");
```

#### List Job Schedules

Previously, in `Microsoft-Azure-Batch`, to list all the job schedules on an account, you could call the `listJobSchedules` method from the `JobScheduleOperations` object.

```java
List<CloudJobSchedule> schedules = new ArrayList<>(batchClient.jobScheduleOperations().listJobSchedules());
for (CloudJobSchedule schedule : schedules) {
    System.out.println(schedule.getId());
}
```

With `Azure-Compute-Batch`, you can call `listJobSchedules` directly on the client.

```java com.azure.compute.batch.job-schedule.list-job-schedules
for (BatchJobSchedule schedule : batchClient.listJobSchedules()) {
    System.out.println(schedule.getId());
}
```

#### Delete Job Schedule

Previously, in `Microsoft-Azure-Batch`, to delete a job schedule, you could call the `deleteJobSchedule` method from the `JobScheduleOperations` object.

```java
batchClient.jobScheduleOperations().deleteJobSchedule("jobScheduleId");
```

With `Azure-Compute-Batch`, you can call `beginDeleteJobSchedule` directly on the client. It is also now an LRO (Long Running Operation).

```java com.azure.compute.batch.job-schedule.delete-job-schedule
SyncPoller<BatchJobSchedule, Void> jobScheduleDeletePoller = batchClient.beginDeleteJobSchedule("jobScheduleId");

PollResponse<BatchJobSchedule> initialJobScheduleDeleteResponse = jobScheduleDeletePoller.poll();
if (initialJobScheduleDeleteResponse.getStatus() == LongRunningOperationStatus.IN_PROGRESS) {
    BatchJobSchedule jobScheduleDuringPoll = initialJobScheduleDeleteResponse.getValue();
}

// Final response
jobScheduleDeletePoller.waitForCompletion();
PollResponse<BatchJobSchedule> finalJobScheduleDeleteResponse = jobScheduleDeletePoller.poll();
```

#### Replace Job Schedule

(Known as `UpdateJobSchedule` in `Microsoft-Azure-Batch`, `ReplaceJobSchedule` in `Azure-Compute-Batch`)

Previously, in `Microsoft-Azure-Batch`, to replace a job schedule, you could call the `updateJobSchedule` method from the `JobScheduleOperations` object.

```java
Schedule schedule = new Schedule();
JobSpecification jobSpecification = new JobSpecification();
batchClient.jobScheduleOperations().updateJobSchedule("jobScheduleId", schedule, jobSpecification);
```

With `Azure-Compute-Batch`, you can call `replaceJobSchedule` directly on the client.

```java com.azure.compute.batch.replace-job-schedule.job-schedule-patch
batchClient.replaceJobSchedule("jobScheduleId",
    new BatchJobSchedule(new BatchJobSpecification(new BatchPoolInfo().setPoolId("poolId")).setPriority(0)
        .setUsesTaskDependencies(false)
        .setConstraints(
            new BatchJobConstraints().setMaxWallClockTime(Duration.parse("P10675199DT2H48M5.4775807S"))
                .setMaxTaskRetryCount(0)))
                    .setSchedule(new BatchJobScheduleConfiguration()
                        .setDoNotRunUntil(OffsetDateTime.parse("2025-01-01T12:30:00Z"))),
    null, null);
```

#### Update Job Schedule

(Known as `PatchJobSchedule` in `Microsoft-Azure-Batch`, `UpdateJobSchedule` in `Azure-Compute-Batch`)

Previously, in `Microsoft-Azure-Batch`, to update a job schedule, you could call the `patchJobSchedule` method from the `JobScheduleOperations` object.

```java
LinkedList<MetadataItem> metadata = new LinkedList<MetadataItem>();
metadata.add((new MetadataItem()).withName("key1").withValue("value1"));
batchClient.jobScheduleOperations().patchJobSchedule(jobScheduleId, null, null, metadata);
```

With `Azure-Compute-Batch`, you can call `updateJobSchedule` directly on the client with a parameter of type `BatchJobScheduleUpdateParameters`.

```java BEGIN: com.azure.compute.batch.job-schedule.update-job-schedule
BatchJobScheduleUpdateParameters updateContent = new BatchJobScheduleUpdateParameters();
updateContent.getMetadata().add(new BatchMetadataItem("key", "value"));
batchClient.updateJobSchedule("jobScheduleId", updateContent);
```

#### Disable Job Schedule

Previously, in `Microsoft-Azure-Batch`, to disable a job schedule, you could call the `disableJobSchedule` method from the `JobScheduleOperations` object.

```java
batchClient.jobScheduleOperations().disableJobSchedule("jobScheduleId");
```

With `Azure-Compute-Batch`, you can call `disableJobSchedule` directly on the client.

```java com.azure.compute.batch.job-schedule.disable-job-schedule
batchClient.disableJobSchedule("jobScheduleId");
```

#### Enable Job Schedule

Previously, in `Microsoft-Azure-Batch`, to enable a job schedule, you could call the `enableJobSchedule` method from the `JobScheduleOperations` object.

```java
batchClient.jobScheduleOperations().enableJobSchedule("jobScheduleId");
```

With `Azure-Compute-Batch`, you can call `enableJobSchedule` directly on the client.

```java com.azure.compute.batch.job-schedule.enable-job-schedule
batchClient.enableJobSchedule("jobScheduleId");
```

#### Terminate Job Schedule

Previously, in `Microsoft-Azure-Batch`, to terminate a job schedule, you could call the `terminateJobSchedule` method from the `JobScheduleOperations` object.

```java
batchClient.jobScheduleOperations().terminateJobSchedule("jobScheduleId");
```

With `Azure-Compute-Batch`, you can call `beginTerminateJobSchedule` directly on the client. It is also now an LRO (Long Running Operation).

```java com.azure.compute.batch.job-schedule.terminate-job-schedule
SyncPoller<BatchJobSchedule, BatchJobSchedule> terminateJobSchedulePoller = batchClient.beginTerminateJobSchedule("jobScheduleId");
terminateJobSchedulePoller.waitForCompletion();
BatchJobSchedule jobSchedule = terminateJobSchedulePoller.getFinalResult();
```

### Task Operations

#### Create Tasks

Previously, in `Microsoft-Azure-Batch`, you could create a single task using `createTask`:

```java
// Single task
TaskAddParameter taskToAdd = new TaskAddParameter();
taskToAdd.withId(taskId).withCommandLine(String.format("/bin/bash -c 'set -e; set -o pipefail; cat %s'", BLOB_FILE_NAME)).withResourceFiles(files);
batchClient.taskOperations().createTask(jobId, taskToAdd);
```

You could also collection of tasks using `createTasks`:

```java
// Or multiple tasks
List<TaskAddParameter> tasksToAdd = new ArrayList<>();
for (int i=0; i<TASK_COUNT; i++) {
    TaskAddParameter addParameter = new TaskAddParameter();
    addParameter.withId(String.format("taskId%d", i)).withCommandLine(String.format("cmd /c echo hello %d",i));
    tasksToAdd.add(addParameter);
}
BatchClientParallelOptions option = new BatchClientParallelOptions(10);
Collection<BatchClientBehavior> behaviors = new HashSet<>();
behaviors.add(option);
batchClient.taskOperations().createTasks(jobId, tasksToAdd, behaviors);
```

With `Azure-Compute-Batch`, there are three ways to add a task to a job.

First, you can call `createTask` with a parameter of type `BatchTaskCreateParameters` to create a single task:

```java com.azure.compute.batch.create-task.creates-a-simple-task
String taskId = "ExampleTaskId";
BatchTaskCreateParameters taskToCreate = new BatchTaskCreateParameters(taskId, "echo hello world");
batchClient.createTask("jobId", taskToCreate);
```

Second, you can call `createTaskCollection` with a `BatchTaskGroup` parameter to create up to 100 tasks:

```java com.azure.compute.batch.create-task.creates-a-task-collection
List<BatchTaskCreateParameters> taskList = Arrays.asList(
    new BatchTaskCreateParameters("task1", "cmd /c echo Hello World"),
    new BatchTaskCreateParameters("task2", "cmd /c echo Hello World"));
BatchTaskGroup taskGroup = new BatchTaskGroup(taskList);
BatchCreateTaskCollectionResult result = batchClient.createTaskCollection("jobId", taskGroup);
```

Lastly you can call `createTasks`, which has no limit to the number of tasks:

```java com.azure.compute.batch.create-task.create-tasks
List<BatchTaskCreateParameters> tasks = new ArrayList<>();
for (int i = 0; i < 1000; i++) {
    tasks.add(new BatchTaskCreateParameters("task" + i, "cmd /c echo Hello World"));
}
batchClient.createTasks("jobId", tasks);
```

#### Get Task

Previously, in `Microsoft-Azure-Batch`, to get a task, you could call the `getTask` method from the `TaskOperations` object.

```java
CloudTask task = batchClient.taskOperations().getTask("jobId", "taskId");
```

With `Azure-Compute-Batch`, you can call `getTask` directly on the client.

```java com.azure.compute.batch.task.get-task
batchClient.getTask("jobId", "taskId");
```

#### List Tasks

Previously, in `Microsoft-Azure-Batch`, to list all the tasks on a job, you could call the `listTasks` method from the `TaskOperations` object.

```java
List<CloudTask> tasks = batchClient.taskOperations().listTasks("jobId");
```

With `Azure-Compute-Batch`, you can call `listTasks` directly on the client.

```java com.azure.compute.batch.task.list-tasks
batchClient.listTasks("jobId");
```

#### Delete Task

Previously, in `Microsoft-Azure-Batch`, to delete a task on a job, you could call the `deleteTask` method from the `TaskOperations` object.

```java
batchClient.taskOperations().deleteTask("jobId", "taskId");
```

With `Azure-Compute-Batch`, you can call `deleteTask` directly on the client.

```java com.azure.compute.batch.task.delete-task
batchClient.deleteTask("jobId", "taskId");
```

#### Replace Task

(Known as `UpdateTask` in `Microsoft-Azure-Batch`, `ReplaceTask` in `Azure-Compute-Batch`)

Previously, in `Microsoft-Azure-Batch`, to update a task, you could call the `updateTask` method from the `TaskOperations` object.

```java
TaskConstraints contraint = new TaskConstraints();
contraint.withMaxTaskRetryCount(5);
batchClient.taskOperations().updateTask("jobId", "taskId", contraint);
```

With `Azure-Compute-Batch`, you can call `replaceTask` directly on the client.

```java com.azure.compute.batch.replace-task.task-update
batchClient.replaceTask("jobId", "taskId",
    new BatchTask().setConstraints(new BatchTaskConstraints().setMaxWallClockTime(Duration.parse("PT1H"))
        .setRetentionTime(Duration.parse("PT1H"))
        .setMaxTaskRetryCount(3)),
    null, null);
```

#### Reactivate Task

Previously, in `Microsoft-Azure-Batch`, to reactivate a task, you could call the `reactivateTask` method from the `TaskOperations` object.

```java
batchClient.taskOperations().reactivateTask("jobId", "taskId");
```

With `Azure-Compute-Batch`, you can call `reactivateTask` directly on the client.

```java com.azure.compute.batch.reactivate-task.task-reactivate
batchClient.reactivateTask("jobId", "taskId", null, null);
```

#### Terminate Task

Previously, in `Microsoft-Azure-Batch`, to terminate a task, you could call the `terminateTask` method from the `TaskOperations` object.

```java
batchClient.taskOperations().terminateTask("jobId", "taskId");
```

With `Azure-Compute-Batch`, you can call `terminateTask` directly on the client.

```java com.azure.compute.batch.terminate-task.task-terminate
batchClient.terminateTask("jobId", "taskId", null, null);
```

### File Operations for Tasks

#### List Task Files

Previously, in `Microsoft-Azure-Batch`, to list the files in a task's directory on its compute node, you could call the `listFilesFromTask` method from the `FileOperations` object.

```java
// Using the legacy FileOperations class to list task files
List<NodeFile> files = batchClient.fileOperations().listFilesFromTask("jobId", "taskId");
for (NodeFile file : files) {
    System.out.println(file.name());
}
```

With `Azure-Compute-Batch`, you can call `listTaskFiles` directly on the client.

```java com.azure.compute.batch.task.list-task-files
PagedIterable<BatchNodeFile> files = batchClient.listTaskFiles("jobId", "taskId");
for (BatchNodeFile file : files) {
    System.out.println(file.getName());
}
```

#### Get Task File

Previously, in `Microsoft-Azure-Batch`, to download the specified file from the task's directory on its compute node, you could call the `getFileFromTask` method from the `FileOperations` object.

```java
ByteArrayOutputStream stream = new ByteArrayOutputStream();
batchClient.fileOperations().getFileFromTask(jobId, taskId, "stdout.txt", stream);
```

With `Azure-Compute-Batch`, you can call `getTaskFile` directly on the client.

```java com.azure.compute.batch.task.get-task-file
BinaryData fileContent = batchClient.getTaskFile("jobId", "taskId", "stdout.txt");
System.out.println(new String(fileContent.toBytes(), StandardCharsets.UTF_8));
```

#### Get Task File Properties

Previously, in `Microsoft-Azure-Batch`, to get the properties of the specified task file, you could call the `getFilePropertiesFromTask` method from the `FileOperations` object.

```java
FileProperties properties = batchClient.fileOperations().getFilePropertiesFromTask("jobId", "taskId", "stdout.txt");
```

With `Azure-Compute-Batch`, you can call `getTaskFileProperties` directly on the client.

```java com.azure.compute.batch.get-task-file-properties.file-get-properties-from-task
batchClient.getTaskFileProperties("jobId", "taskId", "wd\\testFile.txt",
    new BatchTaskFilePropertiesGetOptions());
```

### Node Operations

#### Get Node

Previously, in `Microsoft-Azure-Batch`, to get a compute node, you could call the `getComputeNode` method from the `ComputeNodeOperations` object.

```java
ComputeNode computeNode = batchClient.computeNodeOperations().getComputeNode("poolId", "nodeId");
```

With `Azure-Compute-Batch`, you can call `getNode` directly on the client.

```java com.azure.compute.batch.get-node.node-get
BatchNode node
    = batchClient.getNode("poolId", "tvm-1695681911_2-20161122t193202z", new BatchNodeGetOptions());
```

#### List Nodes

Previously, in `Microsoft-Azure-Batch`, to list the compute nodes on a pool, you could call the `listComputeNodes` method from the `ComputeNodeOperations` object.

```java
List<ComputeNode> nodeList = batchClient.computeNodeOperations().listComputeNodes("poolId");
```

With `Azure-Compute-Batch`, you can call `listNodes` directly on the client.

```java com.azure.compute.batch.list-nodes.node-list
PagedIterable<BatchNode> nodeList = batchClient.listNodes("poolId", new BatchNodesListOptions());
```

#### Deallocate Node

Previously, in `Microsoft-Azure-Batch`, to deallocate a compute node, you could call the `deallocateComputeNode` method from the `ComputeNodeOperations` object.

```java
ComputeNodeDeallocateOption deallocateOption = ComputeNodeDeallocateOption.TERMINATE;
batchClient.computeNodeOperations().deallocateComputeNode("poolId", "nodeId", deallocateOption);
```

With `Azure-Compute-Batch`, you can call `beginDeallocateNode` directly on the client. It is also now an LRO (Long Running Operation).

```java com.azure.compute.batch.node.deallocate-node
BatchNodeDeallocateParameters deallocateParams
    = new BatchNodeDeallocateParameters().setNodeDeallocateOption(BatchNodeDeallocateOption.TERMINATE);

BatchNodeDeallocateOptions deallocateOptions
    = new BatchNodeDeallocateOptions().setTimeOutInSeconds(Duration.ofSeconds(30))
        .setParameters(deallocateParams);
SyncPoller<BatchNode, BatchNode> deallocatePoller = batchClient.beginDeallocateNode("poolId", "nodeId", deallocateOptions);

 // Validate first poll response
PollResponse<BatchNode> firstPoll = deallocatePoller.poll();
if (firstPoll.getStatus() == LongRunningOperationStatus.IN_PROGRESS) {
    BatchNode nodeDuringPoll = firstPoll.getValue();
}

// Wait for operation to complete
deallocatePoller.waitForCompletion();
BatchNode deallocatedNode = deallocatePoller.getFinalResult();
```

#### Reimage Node

Previously, in `Microsoft-Azure-Batch`, to reimage a compute node, you could call the `reimageComputeNode` method from the `ComputeNodeOperations` object.

```java
batchClient.computeNodeOperations().reimageComputeNode("poolId", "nodeId");
```

With `Azure-Compute-Batch`, you can call `beginReimageNode` directly on the client. It is also now an LRO (Long Running Operation).

```java com.azure.compute.batch.node.reimage-node
SyncPoller<BatchNode, BatchNode> reimagePoller = batchClient.beginReimageNode("poolId", "nodeId");

// Retrieve the value while the operation is in progress
PollResponse<BatchNode> reimageFirst = reimagePoller.poll();
BatchNode nodeDuringReimage = reimageFirst.getValue();

// Wait until the the node is usable
reimagePoller.waitForCompletion();
BatchNode reimagedNode = reimagePoller.getFinalResult();
```

#### Start Node

Previously, in `Microsoft-Azure-Batch`, to start a compute node, you could call the `startComputeNode` method from the `ComputeNodeOperations` object.

```java
batchClient.computeNodeOperations().startComputeNode("poolId", "nodeId");
```

With `Azure-Compute-Batch`, you can call `beginStartNode` directly on the client. It is also now an LRO (Long Running Operation).

```java com.azure.compute.batch.node.start-node
SyncPoller<BatchNode, BatchNode> startPoller = batchClient.beginStartNode("poolId", "nodeId");

// First poll
PollResponse<BatchNode> startFirst = startPoller.poll();
BatchNode firstVal = startFirst.getValue();

// Final result
startPoller.waitForCompletion();
BatchNode startedNode = startPoller.getFinalResult();
```

#### Reboot Node

Previously, in `Microsoft-Azure-Batch`, to reboot a compute node, you could call the `rebootComputeNode` method from the `ComputeNodeOperations` object.

```java
List<ComputeNode> nodes = batchClient.computeNodeOperations().listComputeNodes(poolId);
String nodeIdA = nodes.get(0).id(); // get node ID
batchClient.computeNodeOperations().rebootComputeNode("poolId", nodeIdA);
```

With `Azure-Compute-Batch`, you can call `beginRebootNode` directly on the client. It is also now an LRO (Long Running Operation).

```java com.azure.compute.batch.node.reboot-node
List<BatchNode> listOfNodes = new ArrayList<>();
batchClient.listNodes("poolId").forEach(listOfNodes::add);
String nodeIdA = listOfNodes.get(0).getId();
SyncPoller<BatchNode, BatchNode> rebootPoller = batchClient.beginRebootNode("poolId", nodeIdA);

// First poll
PollResponse<BatchNode> rebootFirst = rebootPoller.poll();
if (rebootFirst.getStatus() == LongRunningOperationStatus.IN_PROGRESS) {
    BatchNode nodeDuringReboot = rebootFirst.getValue();
}

rebootPoller.waitForCompletion();
BatchNode rebootedNode = rebootPoller.getFinalResult();
```

#### Create Node User

(Known as `AddComputeNodeUser` in `Microsoft-Azure-Batch` and `CreateNodeUser` in `Azure-Compute-Batch`)

Previously, in `Microsoft-Azure-Batch`, to add a user account to the compute node, you could call the `addComputeNodeUser` method from the `ComputeNodeOperations` object.

```java
ComputeNodeUser user = new ComputeNodeUser();
user.withName("userName");
user.withPassword("userPassword");
batchClient.computeNodeOperations().addComputeNodeUser("poolId", "nodeId", user);
```

With `Azure-Compute-Batch`, you can call `createNodeUser` directly on the client.

```java com.azure.compute.batch.create-node-user.node-create-user
batchClient.createNodeUser("poolId", "tvm-1695681911_1-20161121t182739z",
    new BatchNodeUserCreateParameters("userName").setIsAdmin(false)
        .setExpiryTime(OffsetDateTime.parse("2017-08-01T00:00:00Z"))
        .setPassword("fakeTokenPlaceholder"),
    null);
```

#### Delete Node User

Previously, in `Microsoft-Azure-Batch`, to delete a user account from a node, you could call the `deleteComputeNodeUser` method from the `ComputeNodeOperations` object.

```java
batchClient.computeNodeOperations().deleteComputeNodeUser("poolId", "nodeId", "userName");
```

With `Azure-Compute-Batch`, you can call `deleteNodeUser` directly on the client.

```java com.azure.compute.batch.delete-node-user.node-delete-user
batchClient.deleteNodeUser("poolId", "tvm-1695681911_1-20161121t182739z", "userName", null);
```

#### Get Node File

Previously, in `Microsoft-Azure-Batch`, to get a file from a compute node, you could call the `getFileFromComputeNode` method from the `FileOperations` object.

```java
ByteArrayOutputStream stream = new ByteArrayOutputStream();
batchClient.fileOperations().getFileFromComputeNode("poolId", "nodeId", "fileName", stream);
```

With `Azure-Compute-Batch`, you can call `getNodeFile` directly on the client.

```java com.azure.compute.batch.node.get-node-file
BinaryData nodeFile = batchClient.getNodeFile("poolId", "nodeId", "filePath");
```

#### List Node Files

Previously, in `Microsoft-Azure-Batch`, to get the list of files from a compute node, you could call the `listFilesFromComputeNode` method from the `FileOperations` object.

```java
List<NodeFile> files = batchClient.fileOperations().listFilesFromComputeNode(poolId, nodeId, true, null);
```

With `Azure-Compute-Batch`, you can call `listNodeFiles` directly on the client.

```java com.azure.compute.batch.list-node-files.file-list-from-node
PagedIterable<BatchNodeFile> listNodeFilesResponse = batchClient.listNodeFiles("poolId", "tvm-1695681911_1-20161122t193202z",
    new BatchNodeFilesListOptions().setRecursive(false));
```

#### Delete Node File

Previously, in `Microsoft-Azure-Batch`, to delete a file from a compute node, you could call the `deleteFileFromComputeNode` method from the `FileOperations` object.

```java
batchClient.fileOperations().deleteFileFromComputeNode("jobId", "taskId", "fileName");
```

With `Azure-Compute-Batch`, you can call `deleteNodeFile` directly on the client.

```java com.azure.compute.batch.delete-node-file.file-delete-from-node
batchClient.deleteNodeFile("poolId", "tvm-1695681911_1-20161122t193202z",
    "workitems\\jobId\\job-1\\task1\\wd\\testFile.txt", new BatchNodeFileDeleteOptions().setRecursive(false));
```

#### Get Node File Properties

Previously, in `Microsoft-Azure-Batch`, to get the properties of the specified computer node file, you could call the `getFilePropertiesFromComputeNode` method from the `FileOperations` object.

```java
FileProperties properties = batchClient.fileOperations().getFilePropertiesFromComputeNode("jobId", "taskId", "fileName");
```

With `Azure-Compute-Batch`, you can call `getNodeFileProperties` directly on the client.

```java com.azure.compute.batch.get-node-file-properties.file-get-properties-from-node
batchClient.getNodeFileProperties("poolId", "nodeId", "workitems\\jobId\\job-1\\task1\\wd\\testFile.txt",
    new BatchNodeFilePropertiesGetOptions());
```

#### Get Remote Login Settings

Previously, in `Microsoft-Azure-Batch`, to get the settings required for remote login to a Compute Node., you could call the `getComputeNodeRemoteLoginSettings` method from the `ComputeNodeOperations` object.

```java
ComputeNodeGetRemoteLoginSettingsResult settings = batchClient.computeNodeOperations().getComputeNodeRemoteLoginSettings("poolId", "nodeId");
```

With `Azure-Compute-Batch`, you can call `getNodeRemoteLoginSettings` directly on the client.

```java com.azure.compute.batch.get-node-remote-login-settings.node-get-remote-login-settings
BatchNodeRemoteLoginSettings settings
    = batchClient.getNodeRemoteLoginSettings("poolId", "tvm-1695681911_1-20161121t182739z", null);
```

#### Upload Node Logs

Previously, in `Microsoft-Azure-Batch`, to upload Batch service log files from the compute node to Azure Blob Storage, you could call the `uploadBatchServiceLogs` method from the `ComputeNodeOperations` object.

```java
UploadBatchServiceLogsResult uploadBatchServiceLogsResult = batchClient.computeNodeOperations().uploadBatchServiceLogs(liveIaasPoolId, task.nodeInfo().nodeId(), outputSas, DateTime.now().minusMinutes(-10));
```

With `Azure-Compute-Batch`, you can call `uploadNodeLogs` directly on the client.

```java com.azure.compute.batch.upload-node-logs.upload-batch-service-logs
UploadBatchServiceLogsResult uploadNodeLogsResult
    = batchClient.uploadNodeLogs("poolId", "nodeId", null);
```

```java com.azure.compute.batch.upload-node-logs.upload-batch-service-logs-async
batchAsyncClient.uploadNodeLogs("poolId", "nodeId", null)
    .subscribe(logResult -> {
        System.out.println("Number of files uploaded: " + logResult.getNumberOfFilesUploaded());
        System.out.println("Log upload container URL: " + logResult.getVirtualDirectoryName());
    });
```

### Certificate Operations

Note: Certificate operations are deprecated. Please migrate to use Azure Key Vault. For more information, see [Migrate Batch account certificates to Azure Key Vault](https://learn.microsoft.com/azure/batch/batch-certificate-migration-guide)

#### Create Certificate From Cer

Previously, in `Microsoft-Azure-Batch`, to create a certificate, you could call the `createCertificateFromCer` method from the `CertificateOperations` object.

```java
Certificate cert = batchClient.getCertificateOperations().createCertificateFromCer("cerFilePath");
```

With `Azure-Compute-Batch`, you can call `createCertificate` directly on the client.

```java com.azure.compute.batch.create-certificate.certificate-create
batchClient.createCertificate(
    new BatchCertificate("0123456789abcdef0123456789abcdef01234567", "sha1", "U3dhZ2randomByb2Hash==".getBytes())
        .setCertificateFormat(BatchCertificateFormat.PFX)
        .setPassword("fakeTokenPlaceholder"),
    null);
```

#### Create Certificate

Previously, in `Microsoft-Azure-Batch`, to create a certificate, you could call the `createCertificate` method from the `CertificateOperations` object.

Method 1 (`createCertificate` takes in `InputStream` parameter):

```java
InputStream certStream = new FileInputStream("path/to/certificate.cer");
batchClient.certificateOperations().createCertificate(certStream);
```

Method 2 (`createCertificate` takes in `CertificateAddParameter`):

```java
CertificateAddParameter certParam = new CertificateAddParameter()
    .withThumbprint("your-thumbprint")
    .withThumbprintAlgorithm("sha1")
    .withData("base64-encoded-certificate-data")
    .withCertificateFormat(CertificateFormat.CER);

batchClient.createCertificate(certParam);
```

With `Azure-Compute-Batch`, you can call `createCertificate` directly on the client.

```java com.azure.compute.batch.create-certificate.certificate-create
batchClient.createCertificate(
    new BatchCertificate("0123456789abcdef0123456789abcdef01234567", "sha1", "U3dhZ2randomByb2Hash==".getBytes())
        .setCertificateFormat(BatchCertificateFormat.PFX)
        .setPassword("fakeTokenPlaceholder"),
    null);
```

#### Get Certificate

Previously, in `Microsoft-Azure-Batch`, to get a certificate, you could call the `getCertificate` method from the `CertificateOperations` object.

```java
String thumbprintAlgorithm = "sha1";
String thumbprint = "your-thumbprint"; 
Certificate cert = batchClient.certificateOperations().getCertificate(thumbprintAlgorithm, thumbprint);
```

With `Azure-Compute-Batch`, you can call `getCertificate` directly on the client.

```java com.azure.compute.batch.get-certificate.certificate-get
BatchCertificate certificateResponse = batchClient.getCertificate("sha1", "0123456789abcdef0123456789abcdef01234567",
    new BatchCertificateGetOptions());
```

#### List Certificates

Previously, in `Microsoft-Azure-Batch`, to list all certificates on the account, you could call the `listCertificates` method from the `CertificateOperations` object.

```java
batchClient.getCertificateOperations().listCertificates()
```

With `Azure-Compute-Batch`, you can call `listCertificates` directly on the client.

```java com.azure.compute.batch.list-certificates.certificate-list
PagedIterable<BatchCertificate> certificateList = batchClient.listCertificates(new BatchCertificatesListOptions());
```

#### Delete Certificate

Previously, in `Microsoft-Azure-Batch`, to delete a certificate, you could call the `deleteCertificate` method from the `CertificateOperations` object.

```java
String thumbprintAlgorithm = "sha1";
String thumbprint = "your-thumbprint";
batchClient.certificateOperations().deleteCertificate(thumbprintAlgorithm, thumbprint);
```

With `Azure-Compute-Batch`, you can call `beginDeleteCertificate` directly on the client. It is also now an LRO (Long Running Operation).

```java com.azure.compute.batch.certificate.delete-certificate
String thumbprintAlgorithm = "sha1";
String thumbprint = "your-thumbprint";
SyncPoller<BatchCertificate, Void> deleteCertificatePoller = batchClient.beginDeleteCertificate(thumbprintAlgorithm, thumbprint);
deleteCertificatePoller.waitForCompletion();
PollResponse<BatchCertificate> finalDeleteCertificateResponse = deleteCertificatePoller.poll();
```

#### Cancel Delete Certificate

Previously, in `Microsoft-Azure-Batch`, to cancel the deletion of a certificate, you could call the `cancelDeleteCertificate` method from the `CertificateOperations` object.

```java
String thumbprintAlgorithm = "sha1";
String thumbprint = "your-thumbprint";
batchClient.certificateOperations().cancelDeleteCertificate(thumbprintAlgorithm, thumbprint);
```

With `Azure-Compute-Batch`, you can call `cancelCertificateDeletion` directly on the client.

```java com.azure.compute.batch.cancel-certificate-deletion.certificate-cancel-delete
batchClient.cancelCertificateDeletion("sha1", "0123456789abcdef0123456789abcdef01234567", null);
```

### Application Operations

#### Get Application

Previously, in `Microsoft-Azure-Batch`, to get an application, you could call the `getApplication` method from the `ApplicationOperations` object.

```java
ApplicationSummary appSummary = batchClient.applicationOperations().getApplication("appId");
```

With `Azure-Compute-Batch`, you can call `getApplication` directly on the client.

```java com.azure.compute.batch.get-application.get-applications
BatchApplication application = batchClient.getApplication("my_application_id", null);
```

#### List Applications

Previously, in `Microsoft-Azure-Batch`, to list the applications on an account, you could call the `listApplications` method from the `ApplicationOperations` object.

```java
PagedList<ApplicationSummary> apps = batchClient.applicationOperations().listApplications();

for (ApplicationSummary app : apps) {
    System.out.println("Application ID: " + app.id());
}
```

With `Azure-Compute-Batch`, you can call `listApplications` directly on the client.

```java com.azure.compute.batch.list-applications.list-applications
PagedIterable<BatchApplication> applications = batchClient.listApplications(new BatchApplicationsListOptions());
```
