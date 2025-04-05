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

```java
import com.azure.compute.batch.BatchClient;
import com.azure.compute.batch.BatchClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;

BatchClient batchClient = new BatchClientBuilder()
    .endpoint("https://<your-batch-account>.<region>.batch.azure.com")
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

### Authenticate with Shared Key Credentials

Alternatively, authenticate using an `AzureNamedKeyCredential`.

```java
import com.azure.compute.batch.BatchClient;
import com.azure.compute.batch.BatchClientBuilder;
import com.azure.core.credential.AzureNamedKeyCredential;

AzureNamedKeyCredential credential = new AzureNamedKeyCredential("<your account>", "<BatchAccountKey>");
BatchClient batchClient = new BatchClientBuilder()
    .endpoint("https://<your-batch-account>.<region>.batch.azure.com")
    .credential(credential)
    .buildClient();
```

## Error Handling

In `Azure.Compute.Batch`, server errors throw exceptions such as `HttpResponseException` or `ClientAuthenticationException`. For example:

```java
import com.azure.core.exception.HttpResponseException;
import java.time.Duration;

try {
    batchClient.resizePool("poolID", new BatchPoolResizeContent()
        .setTargetDedicatedNodes(1)
        .setResizeTimeout(Duration.ofMinutes(10)));
} catch (HttpResponseException e) {
    System.err.println("Error resizing pool: " + e.getMessage());
    // Additional details available via e.getResponse()
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

```java
import com.azure.compute.batch.models.BatchPoolCreateContent;
import com.azure.compute.batch.models.ImageReference;
import com.azure.compute.batch.models.VirtualMachineConfiguration;

ImageReference imageReference = new ImageReference()
    .setPublisher("MicrosoftWindowsServer")
    .setOffer("WindowsServer")
    .setSku("2019-datacenter-smalldisk")
    .setVersion("latest");

VirtualMachineConfiguration vmConfig = new VirtualMachineConfiguration(imageReference, "batch.node.windows amd64");

BatchPoolCreateContent poolCreateContent = new BatchPoolCreateContent("poolId", "STANDARD_D1_v2")
    .setVirtualMachineConfiguration(vmConfig)
    .setTargetDedicatedNodes(2);

batchClient.createPool(poolCreateContent);
```

#### GetPool

Track 1:

```java
CloudPool pool = batchClient.poolOperations().getPool("poolId");
```

Track 2:

```java
BatchPool pool = batchClient.getPool("poolId");
System.out.println(pool.getId());
System.out.println(pool.getUrl());
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

```java
for (BatchPool pool : batchClient.getPools()) {
    System.out.println(pool.getId());
}
```

#### DeletePool

Track 1:

```java
batchClient.poolOperations().deletePool("poolId");
```

Track 2:

```java
batchClient.deletePool("poolId");
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

```java
import com.azure.compute.batch.models.BatchPoolUpdateContent;
import com.azure.compute.batch.models.MetadataItem;
import java.util.Collections;

MetadataItem metadataItem = new MetadataItem("name", "value");
BatchPoolUpdateContent updateContent = new BatchPoolUpdateContent();
updateContent.setMetadata(Collections.singletonList(metadataItem));

batchClient.updatePool("poolId", updateContent);
```

#### ResizePool

Track 1:

```java
CloudPool pool = batchClient.poolOperations().getPool("poolId");
pool.resize(1, 0, Duration.ofMinutes(10));
```

Track 2:

```java
import com.azure.compute.batch.models.BatchPoolResizeContent;
import java.time.Duration;

BatchPoolResizeContent resizeContent = new BatchPoolResizeContent()
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

```java
batchClient.stopPoolResize("poolId");
```

#### EnableAutoScalePool

Track 1:

```java
batchClient.poolOperations().enableAutoScale("poolId", "$TargetDedicatedNodes=0;$TargetLowPriorityNodes=0;$NodeDeallocationOption=requeue");
```

Track 2:

```java
import com.azure.compute.batch.models.BatchPoolEnableAutoScaleContent;
import java.time.Duration;

BatchPoolEnableAutoScaleContent autoScaleContent = new BatchPoolEnableAutoScaleContent()
    .setAutoScaleEvaluationInterval(Duration.ofMinutes(6))
    .setAutoScaleFormula("$TargetDedicated = 1;");

batchClient.enablePoolAutoScale("poolId", autoScaleContent);
```

#### DisableAutoScalePool

Track 1:

```java
batchClient.poolOperations().disableAutoScale("poolId");
```

Track 2:

```java
batchClient.disablePoolAutoScale("poolId");
```

#### EvaluateAutoScalePool

Track 1:

```java
AutoScaleRun eval = batchClient.poolOperations().evaluateAutoScale("poolId", "$TargetDedicatedNodes=1;");
```

Track 2:

```java
import com.azure.compute.batch.models.BatchPoolEvaluateAutoScaleContent;
import com.azure.compute.batch.models.AutoScaleRun;

BatchPoolEvaluateAutoScaleContent evalContent = new BatchPoolEvaluateAutoScaleContent("$TargetDedicated = 1;");
AutoScaleRun eval = batchClient.evaluatePoolAutoScale("poolId", evalContent);
```

#### ListPoolNodeCounts

Track 1:

```java
for (PoolNodeCounts counts : batchClient.poolOperations().listPoolNodeCounts()) {
    // Process counts
}
```

Track 2:

```java
for (BatchPoolNodeCounts counts : batchClient.getPoolNodeCounts()) {
    // Process counts
}
```

#### ListPoolUsageMetrics

Track 1:

```java
import org.joda.time.DateTime;
import com.microsoft.azure.batch.protocol.models.PoolUsageMetrics;
import com.microsoft.azure.batch.PagedList;

// For example, list pool usage metrics for January 1, 2023 to January 2, 2023.
DateTime startTime = DateTime.parse("2023-01-01T00:00:00.000Z");
DateTime endTime = DateTime.parse("2023-01-02T00:00:00.000Z");

PagedList<PoolUsageMetrics> metricsList = batchClient.poolOperations().listPoolUsageMetrics(startTime, endTime);
for (PoolUsageMetrics metrics : metricsList) {
    System.out.println("Pool ID: " + metrics.poolId());
    System.out.println("Total Core Hours: " + metrics.totalCoreHours());
}
```

Track 2:

```java
for (BatchPoolUsageMetrics metrics : batchClient.listPoolUsageMetrics()) {
    // Process metrics
}
```

#### GetSupportedImages

Track 1:

```java
List<ImageInformation> images = batchClient.poolOperations().listSupportedImages();
for (ImageInformation image : images) {
    // Process image
}
```

Track 2:

```java
for (BatchSupportedImage image : batchClient.listSupportedImages()) {
    // Process image
}
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

```java
import com.azure.compute.batch.models.BatchJobCreateContent;
import com.azure.compute.batch.models.BatchPoolInfo;

BatchJobCreateContent jobCreateContent = new BatchJobCreateContent("jobId", new BatchPoolInfo().setPoolId("poolId"));
batchClient.createJob(jobCreateContent);
```

#### GetJob

Track 1:

```java
CloudJob job = batchClient.jobOperations().getJob(jobId);
```

Track 2:

```java
BatchJob job = batchClient.getJob("jobId");
```

#### ListJobs

Track 1:

```java
List<CloudJob> jobs = batchClient.jobOperations().listJobs();
for (CloudJob job : jobs) {
    System.out.println(job.getId());
}
```

Track 2:

```java
for (BatchJob job : batchClient.listJobs()) {
    System.out.println(job.getId());
}
```

#### DeleteJob

Track 1:

```java
batchClient.jobOperations().deleteJob("jobId");
```

Track 2:

```java
batchClient.deleteJob("jobId");
```

#### ReplaceJob

(Known as `UpdateJob` in Track 1, `ReplaceJob` in Track 2)

Track 1:

```java
PoolInformation poolInfo = new PoolInformation();
batchClient.jobOperations().updateJob(jobId, poolInfo, 1, null, null, null);
```

Track 2

```java
job.setOnAllTasksComplete(OnAllBatchTasksComplete.TERMINATE_JOB);
batchClient.replaceJob("jobId", job);
```

#### UpdateJob

(Known as `PatchJob` in Track 1, `UpdateJob` in Track 2)

Track 1:

```java
batchClient.jobOperations().patchJob(jobId, OnAllTasksComplete.TERMINATE_JOB);
```

Track 2:

```java
import com.azure.compute.batch.models.BatchJobUpdateContent;

BatchJobUpdateContent updateContent = new BatchJobUpdateContent();
updateContent.getMetadata().add(new MetadataItem("name", "value"));
batchClient.updateJob("jobId", updateContent);
```

#### DisableJob

Track 1:

```java
CloudJob job = batchClient.getJobOperations().getJob("jobId");
job.disable(DisableJobOption.TERMINATE);
```

Track 2:

```java
import com.azure.compute.batch.models.BatchJobDisableContent;

BatchJobDisableContent disableContent = new BatchJobDisableContent(DisableBatchJobOption.REQUEUE);
batchClient.disableJob("jobId", disableContent);
```

#### EnableJob

Track 1:

```java
batchClient.jobOperations().enableJob(jobId);
```

Track 2:

```java
batchClient.enableJob("jobId");
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

```java
for (BatchJobPreparationAndReleaseTaskStatus status : batchClient.getJobPreparationAndReleaseTaskStatuses("jobId")) {
    // Process status
}
```

#### Get Job Task Counts

Track 1:

```java
TaskCounts counts = batchClient.getJobOperations().getTaskCounts("jobId");
```

Track 2:

```java
BatchTaskCountsResult counts = batchClient.getJobTaskCounts("jobId");
```

#### TerminateJob

Track 1:

```java
batchClient.jobOperations().terminateJob("jobId");
```

Track 2:

```java
batchClient.terminateJob("jobId");
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

```java
String jobScheduleId = "jobScheduleId";

BatchPoolInfo poolInfo = new BatchPoolInfo();
poolInfo.setPoolId(poolId);

BatchJobScheduleConfiguration schedule = new BatchJobScheduleConfiguration().setDoNotRunUntil(now())
    .setDoNotRunAfter(now().plusHours(5))
    .setStartWindow(Duration.ofDays(5));
BatchJobSpecification spec = new BatchJobSpecification(poolInfo).setPriority(100);
batchClient.createJobSchedule(new BatchJobScheduleCreateContent(jobScheduleId, schedule, spec));
```

#### GetJobSchedule

Track 1:

```java
CloudJobSchedule jobSchedule = batchClient.getJobScheduleOperations().getJobSchedule("jobScheduleId");
```

Track 2:

```java
BatchJobSchedule jobSchedule = batchClient.getJobSchedule("jobScheduleId");
System.out.println(jobSchedule.getId());
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

```java
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

```java
batchClient.deleteJobSchedule("jobScheduleId");
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

```java
BatchJobSchedule jobSchedule = batchClient.getJobSchedule("jobScheduleId");
jobSchedule.setSchedule(new BatchJobScheduleConfiguration()
    .setDoNotRunUntil(OffsetDateTime.parse("2026-08-18T00:00:00Z")));
batchClient.replaceJobSchedule("jobScheduleId", jobSchedule);
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

```java
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

```java
batchClient.disableJobSchedule("jobScheduleId");
```

#### EnableJobSchedule

Track 1:

```java
batchClient.jobScheduleOperations().enableJobSchedule("jobScheduleId");
```

Track 2:

```java
batchClient.enableJobSchedule("jobScheduleId");
```

#### TerminateJobSchedule

Track 1:

```java
batchClient.jobScheduleOperations().terminateJobSchedule("jobScheduleId");
```

Track 2:

```java
batchClient.terminateJobSchedule("jobScheduleId");
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

```java
import com.azure.compute.batch.models.BatchTaskCreateContent;

BatchTaskCreateContent taskContent = new BatchTaskCreateContent("taskId", "cmd /c echo Hello World");
batchClient.createTask("jobId", taskContent);
```

Create a task collection (100 tasks or less):

```java
import com.azure.compute.batch.models.BatchTaskGroup;
import com.azure.compute.batch.models.BatchTaskAddCollectionResult;

BatchTaskGroup taskGroup = new BatchTaskGroup(new BatchTaskCreateContent[] {
    new BatchTaskCreateContent("task1", "cmd /c echo Hello World"),
    new BatchTaskCreateContent("task2", "cmd /c echo Hello World")
});
BatchTaskAddCollectionResult result = batchClient.createTaskCollection("jobId", taskGroup);
```

Create multiple tasks (used for creating very large numbers of tasks):

```java
List<BatchTaskCreateContent> tasks = new ArrayList<>();
for (int i = 0; i < 1000; i++) {
    tasks.add(new BatchTaskCreateContent("task" + i, "cmd /c echo Hello World"));
}
batchClient.createTasks("jobId", tasks);
```

#### GetTask

Track 1:

```java
CloudTask task = batchClient.getJobOperations().getTask("jobId", "taskId");
```

Track 2:

```java
BatchTask task = batchClient.getTask("jobId", "taskId");
System.out.println(task.getId());
System.out.println(task.getState());
```

#### ListTasks

Track 1:

```java
List<CloudTask> tasks = batchClient.taskOperations().listTasks(jobId);
```

Track 2:

```java
batchClient.listTasks("jobId");
```

#### DeleteTask

Track 1:

```java
batchClient.taskOperations().deleteTask("jobId", "taskId");
```

Track 2:

```java
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

```java
BatchTask task = batchClient.getTask("jobId", "taskId");
task.setConstraints(new BatchTaskConstraints().setMaxTaskRetryCount(3));
batchClient.replaceTask("jobId", "taskId", task);
```

#### ReactivateTask

Track 1:

```java
batchClient.taskOperations().reactivateTask("jobId", "taskId");
```

Track 2:

```java
batchClient.reactivateTask("jobId", "taskId");
```

#### TerminateTask

Track 1:

```java
batchClient.taskOperations().terminateTask("jobId", "taskId");
```

Track 2:

```java
batchClient.terminateTask("jobId", "taskId");
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

```java
import com.azure.compute.batch.models.BatchNodeFile;
import com.azure.core.http.rest.PagedIterable;

// Directly listing task files from the client
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

```java
import com.azure.core.util.BinaryData;

// Directly retrieving the task file content from the client in Track 2
BinaryData fileContent = batchClient.getTaskFile(jobId, taskId, "stdout.txt");
System.out.println(new String(fileContent.toBytes(), StandardCharsets.UTF_8));
```

#### GetTaskFileProperties

Track 1:

```java
FileProperties properties = batchClient.fileOperations().getFilePropertiesFromTask("jobId", "taskId", "stdout.txt");
```

Track 2:

```java
import com.azure.compute.batch.models.FileResponseHeaderProperties;

// Directly getting task file properties from the client in Track 2
BatchFileProperties properties = batchClient.getTaskFileProperties("jobId", "taskId", "stdout.txt");
```

### Node Operations

#### GetComputeNode

Track 1:

```java
ComputeNode computeNode = batchClient.computeNodeOperations().getComputeNode("poolId", "nodeId");
```

Track 2:

```java
BatchNode node = batchClient.getNode("poolId", "nodeId");
```

#### ListComputeNodes

Track 1:

```java
List<ComputeNode> nodes = batchClient.computeNodeOperations().listComputeNodes("poolId");
```

Track 2:

```java
PagedIterable<BatchNode> nodes = batchClient.listNodes("poolId");
```

#### RebootNode

Track 1:

```java
batchClient.computeNodeOperations().rebootComputeNode("poolId", "nodeId");
```

Track 2:

```java
batchClient.rebootNode("poolId", "nodeId");
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

```java
import com.azure.compute.batch.models.BatchNodeUserCreateContent;

BatchNodeUserCreateContent userContent = new BatchNodeUserCreateContent("userName")
    .setPassword("userPassword");
batchClient.createNodeUser("poolId", "nodeId", userContent);
```

#### DeleteComputeNodeUser

Track 1:

```java
batchClient.computeNodeOperations().deleteComputeNodeUser("poolId", "nodeId", "userName");
```

Track 2:

```java
batchClient.deleteNodeUser("poolId", "nodeId", "userName");
```

#### GetNodeFile

Track 1:

```java
ByteArrayOutputStream stream = new ByteArrayOutputStream();
batchClient.fileOperations().getFileFromComputeNode("poolId", "nodeId", "fileName", stream);
```

Track 2:

```java
BinaryData fileContent = batchClient.getNodeFile("poolId", "nodeId", "filePath");
```

#### ListNodeFiles

Track 1:

```java
List<NodeFile> files = batchClient.fileOperations().listFilesFromComputeNode(poolId, nodeId, true, null);
```

Track 2:

```java
PagedIterable<BatchNodeFile> files = batchClient.listNodeFiles("jobId", "nodeId");
```

#### DeleteNodeFile

Track 1:

```java
batchClient.fileOperations().deleteFileFromComputeNode("jobId", "taskId", "fileName");
```

Track 2:

```java
batchClient.deleteNodeFile("jobId", "nodeId", "filePath");
```

#### GetNodeFileProperties

Track 1:

```java
FileProperties properties = batchClient.fileOperations().getFilePropertiesFromComputeNode("jobId", "taskId", "fileName");
```

Track 2:

```java
BatchFileProperties properties = batchClient.getNodeFileProperties("poolId", "nodeId", "filePath");
```

#### GetRemoteLoginSettings

Track 1:

```java
ComputeNodeGetRemoteLoginSettingsResult settings = batchClient.computeNodeOperations().getComputeNodeRemoteLoginSettings("poolId", "nodeId");
```

Track 2:

```java
BatchNodeRemoteLoginSettings settings = batchClient.getNodeRemoteLoginSettings("poolId", "nodeId");
```

#### UploadNodeLogs

Track 1:

```java
String containerUrl = "https://storageaccount.blob.core.windows.net/container?sasToken=abc123";
DateTime startTime = new DateTime(2025, 4, 1, 0, 0, 0, DateTimeZone.UTC);
UploadBatchServiceLogsResult result = uploadBatchServiceLogs("poolId", "nodeId", containerUrl, startTime);
```

Track 2:

```java
import com.azure.compute.batch.models.UploadBatchServiceLogsContent;
import com.azure.compute.batch.models.UploadBatchServiceLogsResult;
import java.time.OffsetDateTime;

UploadBatchServiceLogsContent uploadContent = new UploadBatchServiceLogsContent("containerUrl", OffsetDateTime.parse("2026-05-01T00:00:00Z"));
UploadBatchServiceLogsResult result = batchClient.uploadNodeLogs("poolId", "nodeId", uploadContent);
```

### Certificate Operations

Note: Certificate operations are deprecated.

#### CreateCertificateFromCer

Track 1:

```java
Certificate cert = batchClient.getCertificateOperations().createCertificateFromCer("cerFilePath");
```

Track 2:

```java
import com.azure.compute.batch.models.BatchCertificate;
import com.azure.compute.batch.models.BatchCertificateFormat;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

byte[] certData = Files.readAllBytes(Paths.get("certPath"));
BatchCertificate cerCertificate = new BatchCertificate("Thumbprint", "ThumbprintAlgorithm", 
    Base64.getEncoder().encodeToString(certData))
    .setCertificateFormat(BatchCertificateFormat.CER)
    .setPassword("");
batchClient.createCertificate(cerCertificate);
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

```java
byte[] base64DecodedBytes = Files.readAllBytes(Paths.get("path/to/your-certificate.cer"));
BatchCertificate cert = new BatchCertificate("your-thumbprint", "sha1", base64DecodedBytes)
    .setCertificateFormat(BatchCertificateFormat.CER); // or PFX
batchClient.createCertificate(cert);
```

#### GetCertificate

Track 1:

```java
String thumbprintAlgorithm = "sha1";
String thumbprint = "your-thumbprint"; 
Certificate cert = batchClient.certificateOperations().getCertificate(thumbprintAlgorithm, thumbprint);
```

Track 2:

```java
String thumbprintAlgorithm = "sha1";
String thumbprint = "your-thumbprint"; 
BatchCertificate cert = batchClient.getCertificate(thumbprintAlgorithm, thumbprint);
```

#### ListCertificates

Track 1:

```java
for (Certificate cert : batchClient.getCertificateOperations().listCertificates()) {
    System.out.println(cert.thumbprint());
}
```

Track 2:

```java
for (BatchCertificate cert : batchClient.listCertificates()) {
    System.out.println(cert.getThumbprint());
}
```

#### DeleteCertificate

Track 1:

```java
String thumbprintAlgorithm = "sha1";
String thumbprint = "your-thumbprint";
batchClient.certificateOperations().deleteCertificate(thumbprintAlgorithm, thumbprint);
```

Track 2:

```java
String thumbprintAlgorithm = "sha1";
String thumbprint = "your-thumbprint";
batchClient.deleteCertificate(thumbprintAlgorithm, thumbprint);
```

#### CancelDeleteCertificate

Track 1:

```java
String thumbprintAlgorithm = "sha1";
String thumbprint = "your-thumbprint";
batchClient.certificateOperations().cancelDeleteCertificate(thumbprintAlgorithm, thumbprint);
```

Track 2:

```java
batchClient.cancelCertificateDeletion("ThumbprintAlgorithm", "Thumbprint");
```

### Application Operations

#### GetApplication

Track 1:

```java
ApplicationSummary appSummary = batchClient.applicationOperations().getApplication("appId");
```

Track 2:

```java
BatchApplication application = batchClient.getApplication("appId");
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

```java
for (BatchApplication app : batchClient.listApplications()) {
    System.out.println(app.getId());
}
```
