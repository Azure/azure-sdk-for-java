# Guide for Migrating to `Azure.Compute.Batch` from `Microsoft.Azure.Batch` (Java)

This guide is intended to assist customers in migrating to the new Java SDK package, `Azure.Compute.Batch` (Track 2), from the legacy `Microsoft.Azure.Batch` package (Track 1). It provides side‐by‐side comparisons of similar operations between the two versions. Familiarity with the legacy client library is assumed. For newcomers, please refer to the [README for Track 2](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/batch/azure-compute-batch/README.md) and the [legacy README for Track 1](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/batch/microsoft-azure-batch/README.md).

> **Note:** The legacy `Microsoft.Azure.Batch` package is deprecated. Upgrade to `Azure.Compute.Batch` for continued support and new features.

## Table of Contents

- [Overview](#overview)
  - [Migration Benefits](#migration-benefits)
  - [Azure.Compute.Batch Differences](#azurecomputebatch-differences)
- [Constructing the Clients](#constructing-the-clients)
  - [Authenticate with Microsoft Entra ID](#authenticate-with-microsoft-entra-id)
  - [Authenticate with Shared Key Credentials](#authenticate-with-shared-key-credentials)
- [Error Handling](#error-handling)
- [Operations Examples](#operations-examples)
  - [Pool Operations](#pool-operations)
    - [CreatePool](#createpool)
    - [GetPool](#getpool)
    - [ListPools](#listpools)
    - [DeletePool](#deletepool)
    - [UpdatePool](#updatepool)
    - [ResizePool](#resizepool)
    - [StopResizePool](#stopresizepool)
    - [EnableAutoScalePool](#enableautoscalepool)
    - [DisableAutoScalePool](#disableautoscalepool)
    - [EvaluateAutoScalePool](#evaluateautoscalepool)
    - [ListPoolNodeCounts](#listpoolnodecounts)
    - [ListPoolUsageMetrics](#listpoolusagemetrics)
    - [GetSupportedImages](#getsupportedimages)
  - [Job Operations](#job-operations)
    - [CreateJob](#createjob)
    - [GetJob](#getjob)
    - [ListJobs](#listjobs)
    - [DeleteJob](#deletejob)
    - [ReplaceJob](#replacejob)
    - [UpdateJob](#updatejob)
    - [DisableJob](#disablejob)
    - [EnableJob](#enablejob)
    - [List Job Preparation and Release Task Status](#list-job-preparation-and-release-task-status)
    - [Get Job Task Counts](#get-job-task-counts)
    - [TerminateJob](#terminatejob)
  - [Job Schedule Operations](#job-schedule-operations)
    - [CreateJobSchedule](#createjobschedule)
    - [GetJobSchedule](#getjobschedule)
    - [ListJobSchedules](#listjobschedules)
    - [DeleteJobSchedule](#deletejobschedule)
    - [ReplaceJobSchedule](#replacejobschedule)
    - [UpdateJobSchedule](#updatejobschedule)
    - [DisableJobSchedule](#disablejobschedule)
    - [EnableJobSchedule](#enablejobschedule)
    - [TerminateJobSchedule](#terminatejobschedule)
  - [Task Operations](#task-operations)
    - [AddTask](#addtask)
    - [GetTask](#gettask)
    - [ListTasks](#listtasks)
    - [DeleteTask](#deletetask)
    - [ReplaceTask](#replacetask)
    - [ReactivateTask](#reactivatetask)
    - [TerminateTask](#terminatetask)
    - [File Operations for Tasks](#file-operations-for-tasks)
    - [List Task Files](#listtaskfiles)
    - [Get Task File](#gettaskfile)
    - [Get Task File Properties](#gettaskfileproperties)
  - [Node Operations](#node-operations)
    - [GetComputeNode](#getcomputenode)
    - [ListComputeNodes](#listcomputenodes)
    - [RebootNode](#rebootnode)
    - [CreateComputeNodeUser](#createcomputenodeuser)
    - [DeleteComputeNodeUser](#deletecomputenodeuser)
    - [GetNodeFile](#getnodefile)
    - [ListNodeFiles](#listnodefiles)
    - [DeleteNodeFile](#deletenodefile)
    - [GetNodeFileProperties](#getnodefileproperties)
    - [GetRemoteLoginSettings](#getremoteloginsettings)
    - [UploadNodeLogs](#uploadnodelogs)
  - [Certificate Operations](#certificate-operations)
    - [CreateCertificate](#createcertificate)
    - [GetCertificate](#getcertificate)
    - [ListCertificates](#listcertificates)
    - [DeleteCertificate](#deletecertificate)
    - [CancelDeleteCertificate](#canceldeletecertificate)
  - [Application Operations](#application-operations)
    - [GetApplication](#getapplication)
    - [ListApplications](#listapplications)

## Overview

### Migration Benefits

Migrating to `Azure.Compute.Batch` offers a more consistent and modern programming experience with benefits including:

- A consolidated client that exposes all operations directly.
- Both synchronous and asynchronous (Reactor-based) APIs.
- Improved authentication support via Microsoft Entra ID or shared key.
- Enhanced error handling with richer response details.

### Azure.Compute.Batch Differences

Key differences between the two packages:

- **Naming Changes:** Many classes and method names have been updated (for example, `CloudPool` is now `BatchPool`).
- **API Consolidation:** Instead of separate operation classes (e.g., `JobOperations`, `PoolOperations`), all operations are now methods on the `BatchClient` (or `BatchAsyncClient`).
- **Immediate Operations:** Creating a resource (such as a pool) immediately issues the operation instead of requiring a separate commit.
- **Reactive Support:** Asynchronous operations return Reactor types such as `Mono` and `Flux`.

## Constructing the Clients

We strongly recommend using Microsoft Entra ID for Batch account authentication. Some Batch capabilities require this method of authentication, including many of the security-related features discussed here. The service API authentication mechanism for a Batch account can be restricted to only Microsoft Entra ID using the [allowedAuthenticationModes](https://learn.microsoft.com/rest/api/batchmanagement/batch-account/create?view=rest-batchmanagement-2024-02-01&tabs=HTTP) property. When this property is set, API calls using Shared Key authentication will be rejected.

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

#### CreatePool

Track 1:

```java
// Using Microsoft.Azure.Batch
CloudPool unboundPool = batchClient.poolOperations().createPool(
    "poolId",
    "standard_d2_v3",
    new VirtualMachineConfiguration(
        new ImageReference("MicrosoftWindowsServer", "WindowsServer", "2016-Datacenter-smalldisk", "latest"),
        "batch.node.windows amd64"),
    0);
unboundPool.commit();
```

Track 2:

```java com.azure.compute.batch.create-pool.creates-a-simple-pool
batchClient.createPool(new BatchPoolCreateParameters("poolId", "STANDARD_DC2s_V2")
    .setVirtualMachineConfiguration(
        new VirtualMachineConfiguration(new BatchVmImageReference().setPublisher("Canonical")
            .setOffer("UbuntuServer")
            .setSku("18_04-lts-gen2")
            .setVersion("latest"), "batch.node.ubuntu 18.04"))
    .setTargetDedicatedNodes(1), null);
```

#### GetPool

Track 1:

```java
CloudPool pool = batchClient.poolOperations().getPool("poolId");
```

Track 2:

```java com.azure.compute.batch.get-pool.pool-get
BatchPool response = batchClient.getPool("pool", null, null);
```

#### ListPools

Track 1:

```java
List<CloudPool> pools = new ArrayList<>(batchClient.poolOperations().listPools());
for (CloudPool pool : pools) {
    System.out.println(pool.getId());
}
```

Track 2:

```java com.azure.compute.batch.list-pools.pool-list
PagedIterable<BatchPool> poolList = batchClient.listPools(new BatchPoolsListOptions());
```

#### DeletePool

Track 1:

```java
batchClient.poolOperations().deletePool("poolId");
```

Track 2:

```java com.azure.compute.batch.delete-pool.pool-delete
batchClient.beginDeletePool("poolId");
```

#### UpdatePool

Track 1:

```java
// Update the pool's metadata.
List<MetadataItem> metadata = new ArrayList<>();
metadata.add(new MetadataItem("name", "value"));

// The null values indicate that StartTask, CertificateReferences, and ApplicationPackageReferences remain unchanged.
batchClient.poolOperations().patchPool("poolId", null, null, null, metadata);
```

Track 2:

```java com.azure.compute.batch.update-pool.patch-the-pool
batchClient.updatePool("poolId",
    new BatchPoolUpdateParameters().setStartTask(new BatchStartTask("/bin/bash -c 'echo start task'")), null,
    null);
```

#### ResizePool

Track 1:

```java
CloudPool pool = batchClient.poolOperations().getPool("poolId");
pool.resize(1, 0, Duration.ofMinutes(10));
```

Track 2:

```java com.azure.compute.batch.resize-pool.pool-resize
BatchPoolResizeParameters resizeContent = new BatchPoolResizeParameters()
    .setTargetDedicatedNodes(1)
    .setResizeTimeout(Duration.ofMinutes(10));

batchClient.resizePool("poolId", resizeContent);
```

#### StopResizePool

Track 1:

```java
pool.stopResize();
```

Track 2:

```java com.azure.compute.batch.stop-resize-pool.stop-pool-resize
batchClient.beginStopPoolResize("poolId");
```

#### EnableAutoScalePool

Track 1:

```java
batchClient.poolOperations().enableAutoScale("poolId", "$TargetDedicatedNodes=0;$TargetLowPriorityNodes=0;$NodeDeallocationOption=requeue");
```

Track 2:

```java com.azure.compute.batch.enable-pool-auto-scale.pool-enable-autoscale
BatchPoolEnableAutoScaleParameters autoScaleParameters = new BatchPoolEnableAutoScaleParameters()
    .setAutoScaleEvaluationInterval(Duration.ofMinutes(6))
    .setAutoScaleFormula("$TargetDedicated = 1;");

batchClient.enablePoolAutoScale("poolId", autoScaleParameters);
```

#### DisableAutoScalePool

Track 1:

```java
batchClient.poolOperations().disableAutoScale("poolId");
```

Track 2:

```java com.azure.compute.batch.disable-pool-auto-scale.disable-pool-autoscale
batchClient.disablePoolAutoScale("poolId");
```

#### EvaluateAutoScalePool

Track 1:

```java
AutoScaleRun eval = batchClient.poolOperations().evaluateAutoScale("poolId", "$TargetDedicatedNodes=1;");
```

Track 2:

```java com.azure.compute.batch.evaluate-pool-auto-scale.evaluate-pool-autoscale
BatchPoolEvaluateAutoScaleParameters evalParams = new BatchPoolEvaluateAutoScaleParameters("$TargetDedicated = 1;");
AutoScaleRun eval = batchClient.evaluatePoolAutoScale("poolId", evalParams);
```

#### ListPoolNodeCounts

Track 1:

```java
batchClient.poolOperations().listPoolNodeCounts()
```

Track 2:

```java com.azure.compute.batch.list-pool-node-counts.list-pool-node-counts
batchClient.listPoolNodeCounts();
```

#### ListPoolUsageMetrics

Track 1:

```java
batchClient.poolOperations().listPoolUsageMetrics(startTime, endTime);
```

Track 2:

```java com.azure.compute.batch.list-pool-usage-metrics.list-pool-usage-metrics
batchClient.listPoolUsageMetrics();
```

#### GetSupportedImages

Track 1:

```java
List<ImageInformation> images = batchClient.poolOperations().listSupportedImages();
```

Track 2:

```java com.azure.compute.batch.list-supported-images.list-supported-images
batchClient.listSupportedImages();
```

### Job Operations

#### CreateJob

Track 1:

```java
String jobId = "jobId";

PoolInformation poolInfo = new PoolInformation();
poolInfo.withPoolId(poolId);
batchClient.jobOperations().createJob(jobId, poolInfo);
```

Track 2:

```java com.azure.compute.batch.create-job.creates-a-basic-job
batchClient.createJob(
    new BatchJobCreateParameters("jobId", new BatchPoolInfo().setPoolId("poolId")).setPriority(0), null);
```

#### GetJob

Track 1:

```java
CloudJob job = batchClient.jobOperations().getJob(jobId);
```

Track 2:

```java com.azure.compute.batch.get-job.job-get
BatchJob job = batchClient.getJob("jobId", null, null);
```

#### ListJobs

Track 1:

```java
List<CloudJob> jobs = batchClient.jobOperations().listJobs();
```

Track 2:

```java com.azure.compute.batch.list-jobs.job-list
PagedIterable<BatchJob> jobList = batchClient.listJobs(new BatchJobsListOptions());
```

#### DeleteJob

Track 1:

```java
batchClient.jobOperations().deleteJob("jobId");
```

Track 2:

```java com.azure.compute.batch.delete-job.job-delete
batchClient.beginDeleteJob("jobId");
```

#### ReplaceJob

(Known as `UpdateJob` in Track 1, `ReplaceJob` in Track 2)

Track 1:

```java
PoolInformation poolInfo = new PoolInformation();
batchClient.jobOperations().updateJob(jobId, poolInfo, 1, null, null, null);
```

Track 2

```java com.azure.compute.batch.replace-job.job-patch
batchClient.replaceJob("jobId",
    new BatchJob(new BatchPoolInfo().setPoolId("poolId")).setPriority(100)
        .setConstraints(
            new BatchJobConstraints().setMaxWallClockTime(Duration.parse("PT1H")).setMaxTaskRetryCount(-1)),
    null, null);
```

#### UpdateJob

(Known as `PatchJob` in Track 1, `UpdateJob` in Track 2)

Track 1:

```java
batchClient.jobOperations().patchJob(jobId, OnAllTasksComplete.TERMINATE_JOB);
```

Track 2:

```java com.azure.compute.batch.update-job.job-update
batchClient.updateJob("jobId",
    new BatchJobUpdateParameters().setPriority(100)
        .setConstraints(
            new BatchJobConstraints().setMaxWallClockTime(Duration.parse("PT1H")).setMaxTaskRetryCount(-1))
        .setPoolInfo(new BatchPoolInfo().setPoolId("poolId")),
    null, null);
```

#### DisableJob

Track 1:

```java
CloudJob job = batchClient.getJobOperations().getJob("jobId");
job.disable(DisableJobOption.TERMINATE);
```

Track 2:

```java com.azure.compute.batch.disable-job.job-disable
BatchJobDisableParameters disableParams = new BatchJobDisableParameters(DisableBatchJobOption.REQUEUE);
batchClient.beginDisableJob("jobId", disableParams);
```

#### EnableJob

Track 1:

```java
batchClient.jobOperations().enableJob(jobId);
```

Track 2:

```java com.azure.compute.batch.enable-job.job-enable
batchClient.beginEnableJob("jobId");
```

#### List Job Preparation and Release Task Status

Track 1:

```java
List<JobPreparationAndReleaseTaskExecutionInformation> status =
    new ArrayList<>(batchClient.getJobOperations().listJobPreparationAndReleaseTaskStatus("jobId"));
for (JobPreparationAndReleaseTaskExecutionInformation info : status) {
    // Process info
}
```

Track 2:

```java com.azure.compute.batch.job.list-job-preparation-and-release-task-status
batchClient.listJobPreparationAndReleaseTaskStatus("jobId");
```

#### Get Job Task Counts

Track 1:

```java
TaskCounts counts = batchClient.getJobOperations().getTaskCounts("jobId");
```

Track 2:

```java com.azure.compute.batch.job.get-job-task-counts
BatchTaskCountsResult counts = batchClient.getJobTaskCounts("jobId");
```

#### TerminateJob

Track 1:

```java
batchClient.jobOperations().terminateJob("jobId");
```

Track 2:

```java com.azure.compute.batch.job.terminate-job
batchClient.beginTerminateJob("jobId");
```

### Job Schedule Operations

#### CreateJobSchedule

Track 1:

```java
String jobScheduleId = getStringIdWithUserNamePrefix("-JobSchedule-updateJobScheduleState");

PoolInformation poolInfo = new PoolInformation();
poolInfo.withPoolId(poolId);

JobSpecification spec = new JobSpecification().withPriority(100).withPoolInfo(poolInfo);
Schedule schedule = new Schedule().withDoNotRunUntil(DateTime.now()).withDoNotRunAfter(DateTime.now().plusHours(5)).withStartWindow(Period.days(5));
batchClient.jobScheduleOperations().createJobSchedule(jobScheduleId, schedule, spec);
```

Track 2:

```java com.azure.compute.batch.create-job-schedule.creates-a-basic-job-schedule
batchClient.createJobSchedule(new BatchJobScheduleCreateParameters("jobScheduleId",
    new BatchJobScheduleConfiguration().setRecurrenceInterval(Duration.parse("PT5M")),
    new BatchJobSpecification(new BatchPoolInfo().setPoolId("poolId"))), null);
```

#### GetJobSchedule

Track 1:

```java
CloudJobSchedule jobSchedule = batchClient.getJobScheduleOperations().getJobSchedule("jobScheduleId");
```

Track 2:

```java com.azure.compute.batch.job-schedule.get-job-schedule
batchClient.getJobSchedule("jobScheduleId");
```

#### ListJobSchedules

Track 1:

```java
List<CloudJobSchedule> schedules = new ArrayList<>(batchClient.jobScheduleOperations().listJobSchedules());
for (CloudJobSchedule schedule : schedules) {
    System.out.println(schedule.getId());
}
```

Track 2:

```java com.azure.compute.batch.job-schedule.list-job-schedules
for (BatchJobSchedule schedule : batchClient.listJobSchedules()) {
    System.out.println(schedule.getId());
}
```

#### DeleteJobSchedule

Track 1:

```java
batchClient.jobScheduleOperations().deleteJobSchedule("jobScheduleId");
```

Track 2:

```java com.azure.compute.batch.job-schedule.delete-job-schedule
batchClient.beginDeleteJobSchedule("jobScheduleId");
```

#### ReplaceJobSchedule

(Known as `UpdateJobSchedule` in Track 1, `ReplaceJobSchedule` in Track 2)

Track 1:

```java
Schedule schedule = new Schedule();
JobSpecification jobSpecification = new JobSpecification();
batchClient.jobScheduleOperations().updateJobSchedule("jobScheduleId", schedule, jobSpecification);
```

Track 2:

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

#### UpdateJobSchedule

(Known as `PatchJobSchedule` in Track 1, `UpdateJobSchedule` in Track 2)

Track 1:

```java
LinkedList<MetadataItem> metadata = new LinkedList<MetadataItem>();
metadata.add((new MetadataItem()).withName("key1").withValue("value1"));
batchClient.jobScheduleOperations().patchJobSchedule(jobScheduleId, null, null, metadata);
```

Track 2:

```java BEGIN: com.azure.compute.batch.job-schedule.update-job-schedule
import com.azure.compute.batch.models.BatchJobScheduleUpdateContent;

BatchJobScheduleUpdateContent updateContent = new BatchJobScheduleUpdateContent();
updateContent.getMetadata().add(new MetadataItem("key", "value"));
batchClient.updateJobSchedule("jobScheduleId", updateContent);
```

#### DisableJobSchedule

Track 1:

```java
batchClient.jobScheduleOperations().disableJobSchedule("jobScheduleId");
```

Track 2:

```java com.azure.compute.batch.job-schedule.disable-job-schedule
batchClient.disableJobSchedule("jobScheduleId");
```

#### EnableJobSchedule

Track 1:

```java
batchClient.jobScheduleOperations().enableJobSchedule("jobScheduleId");
```

Track 2:

```java com.azure.compute.batch.job-schedule.enable-job-schedule
batchClient.enableJobSchedule("jobScheduleId");
```

#### TerminateJobSchedule

Track 1:

```java
batchClient.jobScheduleOperations().terminateJobSchedule("jobScheduleId");
```

Track 2:

```java com.azure.compute.batch.job-schedule.terminate-job-schedule
batchClient.beginTerminateJobSchedule("jobScheduleId");
```

### Task Operations

#### AddTask

There are several options in Track 2.

Track 1 (Create a single task or collection of tasks):

```java
// Single task
TaskAddParameter taskToAdd = new TaskAddParameter();
taskToAdd.withId(taskId).withCommandLine(String.format("/bin/bash -c 'set -e; set -o pipefail; cat %s'", BLOB_FILE_NAME)).withResourceFiles(files);
batchClient.taskOperations().createTask(jobId, taskToAdd);

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

Track 2
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

#### GetTask

Track 1:

```java
CloudTask task = batchClient.getJobOperations().getTask("jobId", "taskId");
```

Track 2:

```java com.azure.compute.batch.task.get-task
batchClient.getTask("jobId", "taskId");
```

#### ListTasks

Track 1:

```java
List<CloudTask> tasks = batchClient.taskOperations().listTasks(jobId);
```

Track 2:

```java com.azure.compute.batch.task.list-tasks
batchClient.listTasks("jobId");
```

#### DeleteTask

Track 1:

```java
batchClient.taskOperations().deleteTask("jobId", "taskId");
```

Track 2:

```java com.azure.compute.batch.task.delete-task
batchClient.deleteTask("jobId", "taskId");
```

#### ReplaceTask

(Known as `UpdateTask` in Track 1, `ReplaceTask` in Track 2)

Track 1:

```java
TaskConstraints contraint = new TaskConstraints();
contraint.withMaxTaskRetryCount(5);
batchClient.taskOperations().updateTask("jobId", "taskId", contraint);
```

Track 2:

```java com.azure.compute.batch.replace-task.task-update
batchClient.replaceTask("jobId", "taskId",
    new BatchTask().setConstraints(new BatchTaskConstraints().setMaxWallClockTime(Duration.parse("PT1H"))
        .setRetentionTime(Duration.parse("PT1H"))
        .setMaxTaskRetryCount(3)),
    null, null);
```

#### ReactivateTask

Track 1:

```java
batchClient.taskOperations().reactivateTask("jobId", "taskId");
```

Track 2:

```java com.azure.compute.batch.reactivate-task.task-reactivate
batchClient.reactivateTask("jobId", "taskId", null, null);
```

#### TerminateTask

Track 1:

```java
batchClient.taskOperations().terminateTask("jobId", "taskId");
```

Track 2:

```java com.azure.compute.batch.terminate-task.task-terminate
batchClient.terminateTask("jobId", "taskId", null, null);
```

### File Operations for Tasks

#### ListTaskFiles

Track 1:

```java
// Using the legacy FileOperations class to list task files
List<NodeFile> files = batchClient.fileOperations().listFilesFromTask("jobId", "taskId");
for (NodeFile file : files) {
    System.out.println(file.name());
}
```

Track 2:

```java com.azure.compute.batch.task.list-task-files
PagedIterable<BatchNodeFile> files = batchClient.listTaskFiles("jobId", "taskId");
for (BatchNodeFile file : files) {
    System.out.println(file.getName());
}
```

#### GetTaskFile

Track 1:

```java
ByteArrayOutputStream stream = new ByteArrayOutputStream();
batchClient.fileOperations().getFileFromTask(jobId, taskId, "stdout.txt", stream);
```

Track 2:

```java com.azure.compute.batch.task.get-task-file
BinaryData fileContent = batchClient.getTaskFile("jobId", "taskId", "stdout.txt");
System.out.println(new String(fileContent.toBytes(), StandardCharsets.UTF_8));
```

#### GetTaskFileProperties

Track 1:

```java
FileProperties properties = batchClient.fileOperations().getFilePropertiesFromTask("jobId", "taskId", "stdout.txt");
```

Track 2:

```java com.azure.compute.batch.get-task-file-properties.file-get-properties-from-task
batchClient.getTaskFileProperties("jobId", "taskId", "wd\\testFile.txt",
    new BatchTaskFilePropertiesGetOptions());
```

### Node Operations

#### GetComputeNode

Track 1:

```java
ComputeNode computeNode = batchClient.computeNodeOperations().getComputeNode("poolId", "nodeId");
```

Track 2:

```java com.azure.compute.batch.get-node.node-get
BatchNode node
    = batchClient.getNode("poolId", "tvm-1695681911_2-20161122t193202z", new BatchNodeGetOptions());
```

#### ListComputeNodes

Track 1:

```java
List<ComputeNode> nodeList = batchClient.computeNodeOperations().listComputeNodes("poolId");
```

Track 2:

```java com.azure.compute.batch.list-nodes.node-list
PagedIterable<BatchNode> nodeList = batchClient.listNodes("poolId", new BatchNodesListOptions());
```

#### RebootNode

Track 1:

```java
batchClient.computeNodeOperations().rebootComputeNode("poolId", "nodeId");
```

Track 2:

```java com.azure.compute.batch.node.reboot-node
batchClient.beginRebootNode("poolId", "nodeId");
```

#### CreateComputeNodeUser

(Known as `AddComputeNodeUser` in Track 1 and `CreateComputeNodeUser` in Track 2)

Track 1:

```java
ComputeNodeUser user = new ComputeNodeUser();
user.withName("userName");
user.withPassword("userPassword");
batchClient.computeNodeOperations().addComputeNodeUser("poolId", "nodeId", user);
```

Track 2:

```java com.azure.compute.batch.create-node-user.node-create-user
batchClient.createNodeUser("poolId", "tvm-1695681911_1-20161121t182739z",
    new BatchNodeUserCreateParameters("userName").setIsAdmin(false)
        .setExpiryTime(OffsetDateTime.parse("2017-08-01T00:00:00Z"))
        .setPassword("fakeTokenPlaceholder"),
    null);
```

#### DeleteComputeNodeUser

Track 1:

```java
batchClient.computeNodeOperations().deleteComputeNodeUser("poolId", "nodeId", "userName");
```

Track 2:

```java com.azure.compute.batch.delete-node-user.node-delete-user
batchClient.deleteNodeUser("poolId", "tvm-1695681911_1-20161121t182739z", "userName", null);
```

#### GetNodeFile

Track 1:

```java
ByteArrayOutputStream stream = new ByteArrayOutputStream();
batchClient.fileOperations().getFileFromComputeNode("poolId", "nodeId", "fileName", stream);
```

Track 2:

```java com.azure.compute.batch.node.get-node-file
BinaryData nodeFile = batchClient.getNodeFile("poolId", "nodeId", "filePath");
```

#### ListNodeFiles

Track 1:

```java
List<NodeFile> files = batchClient.fileOperations().listFilesFromComputeNode(poolId, nodeId, true, null);
```

Track 2:

```java com.azure.compute.batch.list-node-files.file-list-from-node
PagedIterable<BatchNodeFile> listNodeFilesResponse = batchClient.listNodeFiles("poolId", "tvm-1695681911_1-20161122t193202z",
    new BatchNodeFilesListOptions().setRecursive(false));
```

#### DeleteNodeFile

Track 1:

```java
batchClient.fileOperations().deleteFileFromComputeNode("jobId", "taskId", "fileName");
```

Track 2:

```java com.azure.compute.batch.delete-node-file.file-delete-from-node
batchClient.deleteNodeFile("poolId", "tvm-1695681911_1-20161122t193202z",
    "workitems\\jobId\\job-1\\task1\\wd\\testFile.txt", new BatchNodeFileDeleteOptions().setRecursive(false));
```

#### GetNodeFileProperties

Track 1:

```java
FileProperties properties = batchClient.fileOperations().getFilePropertiesFromComputeNode("jobId", "taskId", "fileName");
```

Track 2:

```java com.azure.compute.batch.get-node-file-properties.file-get-properties-from-node
batchClient.getNodeFileProperties("poolId", "nodeId", "workitems\\jobId\\job-1\\task1\\wd\\testFile.txt",
    new BatchNodeFilePropertiesGetOptions());
```

#### GetRemoteLoginSettings

Track 1:

```java
ComputeNodeGetRemoteLoginSettingsResult settings = batchClient.computeNodeOperations().getComputeNodeRemoteLoginSettings("poolId", "nodeId");
```

Track 2:

```java com.azure.compute.batch.get-node-remote-login-settings.node-get-remote-login-settings
BatchNodeRemoteLoginSettings settings
    = batchClient.getNodeRemoteLoginSettings("poolId", "tvm-1695681911_1-20161121t182739z", null);
```

#### UploadNodeLogs

Track 1:

```java
String containerUrl = "https://storageaccount.blob.core.windows.net/container?sasToken=abc123";
DateTime startTime = new DateTime(2025, 4, 1, 0, 0, 0, DateTimeZone.UTC);
UploadBatchServiceLogsResult result = uploadBatchServiceLogs("poolId", "nodeId", containerUrl, startTime);
```

Track 2:

```java com.azure.compute.batch.upload-node-logs.upload-batch-service-logs
UploadBatchServiceLogsResult uploadNodeLogsResult
    = batchClient.uploadNodeLogs("poolId", "tvm-1695681911_1-20161121t182739z", null, null);
```

### Certificate Operations

Note: Certificate operations are deprecated.

#### CreateCertificateFromCer

Track 1:

```java
Certificate cert = batchClient.getCertificateOperations().createCertificateFromCer("cerFilePath");
```

Track 2:

```java com.azure.compute.batch.create-certificate.certificate-create
batchClient.createCertificate(
    new BatchCertificate("0123456789abcdef0123456789abcdef01234567", "sha1", "U3dhZ2randomByb2Hash==".getBytes())
        .setCertificateFormat(BatchCertificateFormat.PFX)
        .setPassword("fakeTokenPlaceholder"),
    null);
```

#### CreateCertificate

Track 1:

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

Track 2:

```java com.azure.compute.batch.create-certificate.certificate-create
batchClient.createCertificate(
    new BatchCertificate("0123456789abcdef0123456789abcdef01234567", "sha1", "U3dhZ2randomByb2Hash==".getBytes())
        .setCertificateFormat(BatchCertificateFormat.PFX)
        .setPassword("fakeTokenPlaceholder"),
    null);
```

#### GetCertificate

Track 1:

```java
String thumbprintAlgorithm = "sha1";
String thumbprint = "your-thumbprint"; 
Certificate cert = batchClient.certificateOperations().getCertificate(thumbprintAlgorithm, thumbprint);
```

Track 2:

```java com.azure.compute.batch.get-certificate.certificate-get
BatchCertificate certificateResponse = batchClient.getCertificate("sha1", "0123456789abcdef0123456789abcdef01234567",
    new BatchCertificateGetOptions());
```

#### ListCertificates

Track 1:

```java
batchClient.getCertificateOperations().listCertificates()
```

Track 2:

```java com.azure.compute.batch.list-certificates.certificate-list
PagedIterable<BatchCertificate> certificateList = batchClient.listCertificates(new BatchCertificatesListOptions());
```

#### DeleteCertificate

Track 1:

```java
String thumbprintAlgorithm = "sha1";
String thumbprint = "your-thumbprint";
batchClient.certificateOperations().deleteCertificate(thumbprintAlgorithm, thumbprint);
```

Track 2:

```java com.azure.compute.batch.certificate.delete-certificate
String thumbprintAlgorithm = "sha1";
String thumbprint = "your-thumbprint";
batchClient.beginDeleteCertificate(thumbprintAlgorithm, thumbprint);
```

#### CancelDeleteCertificate

Track 1:

```java
String thumbprintAlgorithm = "sha1";
String thumbprint = "your-thumbprint";
batchClient.certificateOperations().cancelDeleteCertificate(thumbprintAlgorithm, thumbprint);
```

Track 2:

```java com.azure.compute.batch.cancel-certificate-deletion.certificate-cancel-delete
batchClient.cancelCertificateDeletion("sha1", "0123456789abcdef0123456789abcdef01234567", null);
```

### Application Operations

#### GetApplication

Track 1:

```java
ApplicationSummary appSummary = batchClient.applicationOperations().getApplication("appId");
```

Track 2:

```java com.azure.compute.batch.get-application.get-applications
BatchApplication application = batchClient.getApplication("my_application_id", null);
```

#### ListApplications

Track 1:

```java
PagedList<ApplicationSummary> apps = batchClient.applicationOperations().listApplications();

for (ApplicationSummary app : apps) {
    System.out.println("Application ID: " + app.id());
}
```

Track 2:

```java com.azure.compute.batch.list-applications.list-applications
PagedIterable<BatchApplication> applications = batchClient.listApplications(new BatchApplicationsListOptions());
```
