# Azure Batch client library for Java

This README is based on the latest released version of the Azure Compute Batch SDK, otherwise known as the track 2 Azure Batch Data Plane SDK.

> The SDK supports features of the Azure Batch service starting from API version **2023-05-01.16.0**. We will be adding support for more new features and tweaking the API associated with Azure Batch service newer release.

## Table of Contents

- [Documentation](#documentation)
- [Prerequisites](#prerequisites)
  - [Adding the package to your product](#adding-the-package-to-your-product)
- [Key concepts](#key-concepts)
  - [Azure Batch Authentication](#azure-batch-authentication)
  - [Create a Pool](#create-a-pool)
  - [Create a Job](#create-a-job)
  - [Create a Task](#create-a-task)
- [All Methods](#all-methods)
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
    - [Get Supported Images](#get-supported-images)
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
    - [Get Compute Node](#get-compute-node)
    - [ListCompute Nodes](#list-compute-nodes)
    - [Deallocate Node](#deallocate-node)
    - [Reimage Node](#reimage-node)
    - [Start Node](#start-node)
    - [Reboot Node](#reboot-node)
    - [Create Compute Node User](#create-compute-node-user)
    - [Delete Compute Node User](#delete-compute-node-user)
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
    <version>1.0.0-beta.5</version>
</dependency>
```

[//]: # ({x-version-update-end})

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

### Create a Pool

You can create a pool of Azure virtual machines which can be used to execute tasks.

```java com.azure.compute.batch.create-pool.creates-a-simple-pool
batchClient.createPool(new BatchPoolCreateParameters("poolId", "STANDARD_DC2s_V2")
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
    batchClient.beginResizePool("fakepool", resizeParams);
} catch (BatchErrorException err) {
    BatchError error = err.getValue();
    Assertions.assertNotNull(error);
    Assertions.assertEquals("PoolNotFound", error.getCode());
    Assertions.assertTrue(error.getMessage().getValue().contains("The specified pool does not exist."));
    Assertions.assertNull(error.getValues());
}
```

## All Methods

### Authenticate with Microsoft Entra ID

The preferred approach is to use the Azure Identity library’s `DefaultAzureCredential`.

```java com.azure.compute.batch.build-client
BatchClient batchClient = new BatchClientBuilder().credential(new DefaultAzureCredentialBuilder().build())
    .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT"))
    .buildClient();
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
BatchClient batchClientWithSharedKey = batchClientBuilder.buildClient();
```

## Error Handling

In `Azure.Compute.Batch`, server errors throw exceptions such as `BatchErrorException`, the custom Batch error object. For example:

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

The sections below compare common operations between Track 1 (Microsoft.Azure.Batch) and Track 2 (Azure.Compute.Batch).

### Pool Operations

#### Create Pool

```java com.azure.compute.batch.create-pool.creates-a-simple-pool
batchClient.createPool(new BatchPoolCreateParameters("poolId", "STANDARD_DC2s_V2")
    .setVirtualMachineConfiguration(
        new VirtualMachineConfiguration(new BatchVmImageReference().setPublisher("Canonical")
            .setOffer("UbuntuServer")
            .setSku("18_04-lts-gen2")
            .setVersion("latest"), "batch.node.ubuntu 18.04"))
    .setTargetDedicatedNodes(1), null);
```

#### Get Pool

```java com.azure.compute.batch.get-pool.pool-get
BatchPool response = batchClient.getPool("pool", null, null);
```

#### List Pools

```java com.azure.compute.batch.list-pools.pool-list
PagedIterable<BatchPool> poolList = batchClient.listPools(new BatchPoolsListOptions());
```

#### Delete Pool

```java com.azure.compute.batch.delete-pool.pool-delete
SyncPoller<BatchPool, Void> deletePoolPoller = batchClient.beginDeletePool("poolId");

// First poll
PollResponse<BatchPool> initialDeletePoolResponse = deletePoolPoller.poll();
if (initialDeletePoolResponse.getStatus() == LongRunningOperationStatus.IN_PROGRESS) {
    BatchPool poolDuringPoll = initialDeletePoolResponse.getValue();
}

// Wait for LRO to finish
deletePoolPoller.waitForCompletion();
PollResponse<BatchPool> finalDeletePoolResponse = deletePoolPoller.poll();
```

#### Update Pool

```java com.azure.compute.batch.update-pool.patch-the-pool
batchClient.updatePool("poolId",
    new BatchPoolUpdateParameters().setStartTask(new BatchStartTask("/bin/bash -c 'echo start task'")), null,
    null);
```

#### Resize Pool

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

```java com.azure.compute.batch.enable-pool-auto-scale.pool-enable-autoscale
BatchPoolEnableAutoScaleParameters autoScaleParameters = new BatchPoolEnableAutoScaleParameters()
    .setAutoScaleEvaluationInterval(Duration.ofMinutes(6))
    .setAutoScaleFormula("$TargetDedicated = 1;");

batchClient.enablePoolAutoScale("poolId", autoScaleParameters);
```

#### Disable AutoScale Pool

```java com.azure.compute.batch.disable-pool-auto-scale.disable-pool-autoscale
batchClient.disablePoolAutoScale("poolId");
```

#### Evaluate AutoScale Pool

```java com.azure.compute.batch.evaluate-pool-auto-scale.evaluate-pool-autoscale
BatchPoolEvaluateAutoScaleParameters evalParams = new BatchPoolEvaluateAutoScaleParameters("$TargetDedicated = 1;");
AutoScaleRun eval = batchClient.evaluatePoolAutoScale("poolId", evalParams);
```

#### List Pool Node Counts

```java com.azure.compute.batch.list-pool-node-counts.list-pool-node-counts
batchClient.listPoolNodeCounts();
```

#### List Pool Usage Metrics

```java com.azure.compute.batch.list-pool-usage-metrics.list-pool-usage-metrics
batchClient.listPoolUsageMetrics();
```

#### Get Supported Images

```java com.azure.compute.batch.list-supported-images.list-supported-images
batchClient.listSupportedImages();
```

#### Remove Nodes

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

```java com.azure.compute.batch.create-job.creates-a-basic-job
batchClient.createJob(
    new BatchJobCreateParameters("jobId", new BatchPoolInfo().setPoolId("poolId")).setPriority(0), null);
```

#### Get Job

```java com.azure.compute.batch.get-job.job-get
BatchJob job = batchClient.getJob("jobId", null, null);
```

#### List Jobs

```java com.azure.compute.batch.list-jobs.job-list
PagedIterable<BatchJob> jobList = batchClient.listJobs(new BatchJobsListOptions());
```

#### Delete Job

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

```java com.azure.compute.batch.replace-job.job-patch
batchClient.replaceJob("jobId",
    new BatchJob(new BatchPoolInfo().setPoolId("poolId")).setPriority(100)
        .setConstraints(
            new BatchJobConstraints().setMaxWallClockTime(Duration.parse("PT1H")).setMaxTaskRetryCount(-1)),
    null, null);
```

#### Update Job

```java com.azure.compute.batch.update-job.job-update
batchClient.updateJob("jobId",
    new BatchJobUpdateParameters().setPriority(100)
        .setConstraints(
            new BatchJobConstraints().setMaxWallClockTime(Duration.parse("PT1H")).setMaxTaskRetryCount(-1))
        .setPoolInfo(new BatchPoolInfo().setPoolId("poolId")),
    null, null);
```

#### Disable Job

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

Track 2:

```java com.azure.compute.batch.job.list-job-preparation-and-release-task-status
batchClient.listJobPreparationAndReleaseTaskStatus("jobId");
```

#### Get Job Task Counts

Track 2:

```java com.azure.compute.batch.job.get-job-task-counts
BatchTaskCountsResult counts = batchClient.getJobTaskCounts("jobId");
```

#### Terminate Job

```java com.azure.compute.batch.job.terminate-job
BatchJobTerminateParameters terminateParams = new BatchJobTerminateParameters().setTerminationReason("ExampleReason");
BatchJobTerminateOptions terminateOptions = new BatchJobTerminateOptions().setParameters(terminateParams);
SyncPoller<BatchJob, BatchJob> terminatePoller = batchClient.beginTerminateJob("jobId", terminateOptions, null);

// Inspect the first poll
PollResponse<BatchJob> first = terminatePoller.poll();
if (first.getStatus() == LongRunningOperationStatus.IN_PROGRESS) {
    BatchJob pollingJob = first.getValue();
}

terminatePoller.waitForCompletion();
BatchJob terminatedJob = terminatePoller.getFinalResult();
```

### Job Schedule Operations

#### Create Job Schedule

```java com.azure.compute.batch.create-job-schedule.creates-a-basic-job-schedule
batchClient.createJobSchedule(new BatchJobScheduleCreateParameters("jobScheduleId",
    new BatchJobScheduleConfiguration().setRecurrenceInterval(Duration.parse("PT5M")),
    new BatchJobSpecification(new BatchPoolInfo().setPoolId("poolId"))), null);
```

#### Get Job Schedule

```java com.azure.compute.batch.job-schedule.get-job-schedule
batchClient.getJobSchedule("jobScheduleId");
```

#### List Job Schedules

```java com.azure.compute.batch.job-schedule.list-job-schedules
for (BatchJobSchedule schedule : batchClient.listJobSchedules()) {
    System.out.println(schedule.getId());
}
```

#### Delete Job Schedule

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

Track 2:

```java BEGIN: com.azure.compute.batch.job-schedule.update-job-schedule
import com.azure.compute.batch.models.BatchJobScheduleUpdateContent;

BatchJobScheduleUpdateContent updateContent = new BatchJobScheduleUpdateContent();
updateContent.getMetadata().add(new MetadataItem("key", "value"));
batchClient.updateJobSchedule("jobScheduleId", updateContent);
```

#### Disable Job Schedule

```java com.azure.compute.batch.job-schedule.disable-job-schedule
batchClient.disableJobSchedule("jobScheduleId");
```

#### Enable Job Schedule

```java com.azure.compute.batch.job-schedule.enable-job-schedule
batchClient.enableJobSchedule("jobScheduleId");
```

#### Terminate Job Schedule

```java com.azure.compute.batch.job-schedule.terminate-job-schedule
SyncPoller<BatchJobSchedule, BatchJobSchedule> terminateJobSchedulePoller = batchClient.beginTerminateJobSchedule("jobScheduleId");
terminateJobSchedulePoller.waitForCompletion();
BatchJobSchedule jobSchedule = terminateJobSchedulePoller.getFinalResult();
```

### Task Operations

#### Create Tasks

Create a single task:

```java com.azure.compute.batch.create-task.creates-a-basic-task
batchClient.createTask("jobId", new BatchTaskCreateParameters("task1", "cmd /c echo task1"), null);
```

Create a task collection (100 tasks or less):

```java com.azure.compute.batch.create-task.creates-a-task-collection
List<BatchTaskCreateParameters> taskList = Arrays.asList(
    new BatchTaskCreateParameters("task1", "cmd /c echo Hello World"),
    new BatchTaskCreateParameters("task2", "cmd /c echo Hello World"));
BatchTaskGroup taskGroup = new BatchTaskGroup(taskList);
BatchCreateTaskCollectionResult result = batchClient.createTaskCollection("jobId", taskGroup);
```

Create multiple tasks (used for creating very large numbers of tasks):

```java com.azure.compute.batch.create-task.create-tasks
List<BatchTaskCreateParameters> tasks = new ArrayList<>();
for (int i = 0; i < 1000; i++) {
    tasks.add(new BatchTaskCreateParameters("task" + i, "cmd /c echo Hello World"));
}
batchClient.createTasks("jobId", tasks);
```

#### Get Task

```java com.azure.compute.batch.task.get-task
batchClient.getTask("jobId", "taskId");
```

#### List Tasks

```java com.azure.compute.batch.task.list-tasks
batchClient.listTasks("jobId");
```

#### Delete Task

```java com.azure.compute.batch.task.delete-task
batchClient.deleteTask("jobId", "taskId");
```

#### Replace Task

```java com.azure.compute.batch.replace-task.task-update
batchClient.replaceTask("jobId", "taskId",
    new BatchTask().setConstraints(new BatchTaskConstraints().setMaxWallClockTime(Duration.parse("PT1H"))
        .setRetentionTime(Duration.parse("PT1H"))
        .setMaxTaskRetryCount(3)),
    null, null);
```

#### Reactivate Task

```java com.azure.compute.batch.reactivate-task.task-reactivate
batchClient.reactivateTask("jobId", "taskId", null, null);
```

#### Terminate Task

```java com.azure.compute.batch.terminate-task.task-terminate
batchClient.terminateTask("jobId", "taskId", null, null);
```

### File Operations for Tasks

#### List Task Files

```java com.azure.compute.batch.task.list-task-files
PagedIterable<BatchNodeFile> files = batchClient.listTaskFiles("jobId", "taskId");
for (BatchNodeFile file : files) {
    System.out.println(file.getName());
}
```

#### Get Task File

```java com.azure.compute.batch.task.get-task-file
BinaryData fileContent = batchClient.getTaskFile("jobId", "taskId", "stdout.txt");
System.out.println(new String(fileContent.toBytes(), StandardCharsets.UTF_8));
```

#### Get Task File Properties

```java com.azure.compute.batch.get-task-file-properties.file-get-properties-from-task
batchClient.getTaskFileProperties("jobId", "taskId", "wd\\testFile.txt",
    new BatchTaskFilePropertiesGetOptions());
```

### Node Operations

#### Get Compute Node

```java com.azure.compute.batch.get-node.node-get
BatchNode node
    = batchClient.getNode("poolId", "tvm-1695681911_2-20161122t193202z", new BatchNodeGetOptions());
```

#### List Compute Nodes

```java com.azure.compute.batch.list-nodes.node-list
PagedIterable<BatchNode> nodeList = batchClient.listNodes("poolId", new BatchNodesListOptions());
```

#### Deallocate Node

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

#### Create Compute Node User

```java com.azure.compute.batch.create-node-user.node-create-user
batchClient.createNodeUser("poolId", "tvm-1695681911_1-20161121t182739z",
    new BatchNodeUserCreateParameters("userName").setIsAdmin(false)
        .setExpiryTime(OffsetDateTime.parse("2017-08-01T00:00:00Z"))
        .setPassword("fakeTokenPlaceholder"),
    null);
```

#### Delete Compute Node User

```java com.azure.compute.batch.delete-node-user.node-delete-user
batchClient.deleteNodeUser("poolId", "tvm-1695681911_1-20161121t182739z", "userName", null);
```

#### Get Node File

```java com.azure.compute.batch.node.get-node-file
BinaryData nodeFile = batchClient.getNodeFile("poolId", "nodeId", "filePath");
```

#### List Node Files

```java com.azure.compute.batch.list-node-files.file-list-from-node
PagedIterable<BatchNodeFile> listNodeFilesResponse = batchClient.listNodeFiles("poolId", "tvm-1695681911_1-20161122t193202z",
    new BatchNodeFilesListOptions().setRecursive(false));
```

#### Delete Node File

```java com.azure.compute.batch.delete-node-file.file-delete-from-node
batchClient.deleteNodeFile("poolId", "tvm-1695681911_1-20161122t193202z",
    "workitems\\jobId\\job-1\\task1\\wd\\testFile.txt", new BatchNodeFileDeleteOptions().setRecursive(false));
```

#### Get Node File Properties

```java com.azure.compute.batch.get-node-file-properties.file-get-properties-from-node
batchClient.getNodeFileProperties("poolId", "nodeId", "workitems\\jobId\\job-1\\task1\\wd\\testFile.txt",
    new BatchNodeFilePropertiesGetOptions());
```

#### Get Remote Login Settings

```java com.azure.compute.batch.get-node-remote-login-settings.node-get-remote-login-settings
BatchNodeRemoteLoginSettings settings
    = batchClient.getNodeRemoteLoginSettings("poolId", "tvm-1695681911_1-20161121t182739z", null);
```

#### Upload Node Logs

```java com.azure.compute.batch.upload-node-logs.upload-batch-service-logs
UploadBatchServiceLogsResult uploadNodeLogsResult
    = batchClient.uploadNodeLogs("poolId", "tvm-1695681911_1-20161121t182739z", null, null);
```

### Certificate Operations

Note: Certificate operations are deprecated.

#### Create Certificate From Cer

Track 1:

```java com.azure.compute.batch.create-certificate.certificate-create
batchClient.createCertificate(
    new BatchCertificate("0123456789abcdef0123456789abcdef01234567", "sha1", "U3dhZ2randomByb2Hash==".getBytes())
        .setCertificateFormat(BatchCertificateFormat.PFX)
        .setPassword("fakeTokenPlaceholder"),
    null);
```

#### Create Certificate

```java com.azure.compute.batch.create-certificate.certificate-create
batchClient.createCertificate(
    new BatchCertificate("0123456789abcdef0123456789abcdef01234567", "sha1", "U3dhZ2randomByb2Hash==".getBytes())
        .setCertificateFormat(BatchCertificateFormat.PFX)
        .setPassword("fakeTokenPlaceholder"),
    null);
```

#### Get Certificate

```java com.azure.compute.batch.get-certificate.certificate-get
BatchCertificate certificateResponse = batchClient.getCertificate("sha1", "0123456789abcdef0123456789abcdef01234567",
    new BatchCertificateGetOptions());
```

#### List Certificates

```java com.azure.compute.batch.list-certificates.certificate-list
PagedIterable<BatchCertificate> certificateList = batchClient.listCertificates(new BatchCertificatesListOptions());
```

#### Delete Certificate

```java com.azure.compute.batch.certificate.delete-certificate
String thumbprintAlgorithm = "sha1";
String thumbprint = "your-thumbprint";
SyncPoller<BatchCertificate, Void> deleteCertificatePoller = batchClient.beginDeleteCertificate(thumbprintAlgorithm, thumbprint);
deleteCertificatePoller.waitForCompletion();
PollResponse<BatchCertificate> finalDeleteCertificateResponse = deleteCertificatePoller.poll();
```

#### Cancel Delete Certificate

```java com.azure.compute.batch.cancel-certificate-deletion.certificate-cancel-delete
batchClient.cancelCertificateDeletion("sha1", "0123456789abcdef0123456789abcdef01234567", null);
```

### Application Operations

#### Get Application

```java com.azure.compute.batch.get-application.get-applications
BatchApplication application = batchClient.getApplication("my_application_id", null);
```

#### List Applications

```java com.azure.compute.batch.list-applications.list-applications
PagedIterable<BatchApplication> applications = batchClient.listApplications(new BatchApplicationsListOptions());
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

<!-- LINKS -->
[product_documentation]: https://azure.microsoft.com/services/
[docs]: https://azure.github.io/azure-sdk-for-java/
[jdk]: https://learn.microsoft.com/java/azure/jdk/
[azure_subscription]: https://azure.microsoft.com/free/
