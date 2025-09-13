// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.compute.batch;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;

import com.azure.compute.batch.models.AllocationState;
import com.azure.compute.batch.models.AutoScaleRun;
import com.azure.compute.batch.models.AutoUserScope;
import com.azure.compute.batch.models.AutoUserSpecification;
import com.azure.compute.batch.models.BatchApplication;
import com.azure.compute.batch.models.BatchApplicationsListOptions;
import com.azure.compute.batch.models.BatchCertificate;
import com.azure.compute.batch.models.BatchCertificateFormat;
import com.azure.compute.batch.models.BatchCertificateGetOptions;
import com.azure.compute.batch.models.BatchCertificatesListOptions;
import com.azure.compute.batch.models.BatchCreateTaskCollectionResult;
import com.azure.compute.batch.models.BatchError;
import com.azure.compute.batch.models.BatchErrorException;
import com.azure.compute.batch.models.BatchJob;
import com.azure.compute.batch.models.BatchJobActionKind;
import com.azure.compute.batch.models.BatchJobConstraints;
import com.azure.compute.batch.models.BatchJobCreateParameters;
import com.azure.compute.batch.models.BatchJobDisableParameters;
import com.azure.compute.batch.models.BatchJobSchedule;
import com.azure.compute.batch.models.BatchJobScheduleConfiguration;
import com.azure.compute.batch.models.BatchJobScheduleCreateParameters;
import com.azure.compute.batch.models.BatchJobScheduleUpdateParameters;
import com.azure.compute.batch.models.BatchJobSpecification;
import com.azure.compute.batch.models.BatchJobTerminateOptions;
import com.azure.compute.batch.models.BatchJobTerminateParameters;
import com.azure.compute.batch.models.BatchJobUpdateParameters;
import com.azure.compute.batch.models.BatchJobsListOptions;
import com.azure.compute.batch.models.BatchMetadataItem;
import com.azure.compute.batch.models.BatchNode;
import com.azure.compute.batch.models.BatchNodeDeallocateOption;
import com.azure.compute.batch.models.BatchNodeDeallocateOptions;
import com.azure.compute.batch.models.BatchNodeDeallocateParameters;
import com.azure.compute.batch.models.BatchNodeDeallocationOption;
import com.azure.compute.batch.models.BatchNodeFile;
import com.azure.compute.batch.models.BatchNodeFileDeleteOptions;
import com.azure.compute.batch.models.BatchNodeFilePropertiesGetOptions;
import com.azure.compute.batch.models.BatchNodeFilesListOptions;
import com.azure.compute.batch.models.BatchNodeGetOptions;
import com.azure.compute.batch.models.BatchNodeRemoteLoginSettings;
import com.azure.compute.batch.models.BatchNodeRemoveParameters;
import com.azure.compute.batch.models.BatchNodeUserCreateParameters;
import com.azure.compute.batch.models.BatchNodesListOptions;
import com.azure.compute.batch.models.BatchPool;
import com.azure.compute.batch.models.BatchPoolCreateParameters;
import com.azure.compute.batch.models.BatchPoolEnableAutoScaleParameters;
import com.azure.compute.batch.models.BatchPoolEvaluateAutoScaleParameters;
import com.azure.compute.batch.models.BatchPoolInfo;
import com.azure.compute.batch.models.BatchPoolResizeParameters;
import com.azure.compute.batch.models.BatchPoolUpdateParameters;
import com.azure.compute.batch.models.BatchStartTask;
import com.azure.compute.batch.models.BatchTask;
import com.azure.compute.batch.models.BatchTaskConstraints;
import com.azure.compute.batch.models.BatchTaskCountsResult;
import com.azure.compute.batch.models.BatchTaskCreateParameters;
import com.azure.compute.batch.models.BatchTaskFilePropertiesGetOptions;
import com.azure.compute.batch.models.BatchTaskGroup;
import com.azure.compute.batch.models.BatchVmImageReference;
import com.azure.compute.batch.models.DisableBatchJobOption;
import com.azure.compute.batch.models.ElevationLevel;
import com.azure.compute.batch.models.ExitCodeRangeMapping;
import com.azure.compute.batch.models.ExitConditions;
import com.azure.compute.batch.models.ExitOptions;
import com.azure.compute.batch.models.UploadBatchServiceLogsResult;
import com.azure.compute.batch.models.UserIdentity;
import com.azure.compute.batch.models.VirtualMachineConfiguration;
import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;

public final class ReadmeSamples {
    public void readmeSamples() {
        // BEGIN: com.azure.compute.batch.build-client
        BatchClient batchClient = new BatchClientBuilder().credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT"))
            .buildClient();
        // END: com.azure.compute.batch.build-client

        // BEGIN: com.azure.compute.batch.build-async-client
        BatchAsyncClient batchAsyncClient = new BatchClientBuilder().credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT"))
            .buildAsyncClient();
        // END: com.azure.compute.batch.build-async-client

        // BEGIN: com.azure.compute.batch.build-sharedkey-client
        Configuration localConfig = Configuration.getGlobalConfiguration();
        String accountName = localConfig.get("AZURE_BATCH_ACCOUNT", "fakeaccount");
        String accountKey = localConfig.get("AZURE_BATCH_ACCESS_KEY", "fakekey");
        AzureNamedKeyCredential sharedKeyCreds = new AzureNamedKeyCredential(accountName, accountKey);

        BatchClientBuilder batchClientBuilder = new BatchClientBuilder();
        batchClientBuilder.credential(sharedKeyCreds);
        // You can build both the sync and async clients with this configuration
        BatchClient batchClientWithSharedKey = batchClientBuilder.buildClient();
        BatchAsyncClient batchAsyncClientWithSharedKey = batchClientBuilder.buildAsyncClient();
        // END: com.azure.compute.batch.build-sharedkey-client

        // BEGIN: com.azure.compute.batch.create-pool.creates-a-simple-pool
        batchClient.createPool(new BatchPoolCreateParameters("poolId", "STANDARD_DC2s_V2")
            .setVirtualMachineConfiguration(
                new VirtualMachineConfiguration(new BatchVmImageReference().setPublisher("Canonical")
                    .setOffer("UbuntuServer")
                    .setSku("22_04-lts")
                    .setVersion("latest"), "batch.node.ubuntu 22.04"))
            .setTargetDedicatedNodes(1), null);
        // END: com.azure.compute.batch.create-pool.creates-a-simple-pool

        // BEGIN: com.azure.compute.batch.create-job.creates-a-basic-job
        batchClient.createJob(
            new BatchJobCreateParameters("jobId", new BatchPoolInfo().setPoolId("poolId")).setPriority(0), null);
        // END: com.azure.compute.batch.create-job.creates-a-basic-job

        // BEGIN: com.azure.compute.batch.create-job.creates-a-basic-job-async
        batchAsyncClient.createJob(
            new BatchJobCreateParameters("jobId", new BatchPoolInfo().setPoolId("poolId")).setPriority(0))
            .subscribe(unused -> System.out.println("Job created successfully"));
        // END: com.azure.compute.batch.create-job.creates-a-basic-job-async

        // BEGIN: com.azure.compute.batch.create-task.creates-a-simple-task
        String taskId = "ExampleTaskId";
        BatchTaskCreateParameters taskToCreate = new BatchTaskCreateParameters(taskId, "echo hello world");
        batchClient.createTask("jobId", taskToCreate);
        // END: com.azure.compute.batch.create-task.creates-a-simple-task

        // BEGIN: com.azure.compute.batch.create-task.creates-a-task-with-exit-conditions
        batchClient.createTask("jobId", new BatchTaskCreateParameters("taskId", "cmd /c exit 3")
            .setExitConditions(new ExitConditions().setExitCodeRanges(Arrays
                .asList(new ExitCodeRangeMapping(2, 4, new ExitOptions().setJobAction(BatchJobActionKind.TERMINATE)))))
            .setUserIdentity(new UserIdentity().setAutoUser(
                new AutoUserSpecification().setScope(AutoUserScope.TASK).setElevationLevel(ElevationLevel.NON_ADMIN))),
            null);
        // END: com.azure.compute.batch.create-task.creates-a-task-with-exit-conditions

        // BEGIN: com.azure.compute.batch.resize-pool.resize-pool-error
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
        // END: com.azure.compute.batch.resize-pool.resize-pool-error

        // BEGIN: com.azure.compute.batch.get-pool.pool-get
        BatchPool pool = batchClient.getPool("poolId");
        // END: com.azure.compute.batch.get-pool.pool-get

        // BEGIN: com.azure.compute.batch.pool.get-pool-async
        batchAsyncClient.getPool("poolId").subscribe(asyncPool -> {
            // Use the pool here
            System.out.println("Pool ID: " + asyncPool.getId());
        });
        // END: com.azure.compute.batch.pool.get-pool-async

        // BEGIN: com.azure.compute.batch.list-pools.pool-list
        PagedIterable<BatchPool> poolList = batchClient.listPools();
        // END: com.azure.compute.batch.list-pools.pool-list

        // BEGIN: com.azure.compute.batch.pool.delete-pool-simple
        SyncPoller<BatchPool, Void> deletePoolPoller = batchClient.beginDeletePool("poolId");
        // END: com.azure.compute.batch.pool.delete-pool-simple

        // BEGIN: com.azure.compute.batch.pool.delete-pool-complex
        SyncPoller<BatchPool, Void> complexDeletePoolPoller = batchClient.beginDeletePool("poolId");
        PollResponse<BatchPool> finalDeletePoolResponse = complexDeletePoolPoller.waitForCompletion();
        // END: com.azure.compute.batch.pool.delete-pool-complex

        // BEGIN: com.azure.compute.batch.pool.delete-pool-async-simple
        batchAsyncClient.beginDeletePool("poolId").subscribe();
        // END: com.azure.compute.batch.pool.delete-pool-async-simple

        // BEGIN: com.azure.compute.batch.pool.async.delete-pool-async-complex
        batchAsyncClient.beginDeletePool("poolId")
            .takeUntil(pollResponse -> pollResponse.getStatus().isComplete())
            .last()
            .subscribe(finalPollResponse -> {
                System.out.println("Pool deletion completed with status: " + finalPollResponse.getStatus());
            });
        // END: com.azure.compute.batch.pool.async.delete-pool-async-complex

        // BEGIN: com.azure.compute.batch.update-pool.patch-the-pool
        batchClient.updatePool("poolId",
            new BatchPoolUpdateParameters().setStartTask(new BatchStartTask("/bin/bash -c 'echo start task'")), null,
            null);
        // END: com.azure.compute.batch.update-pool.patch-the-pool

        // BEGIN: com.azure.compute.batch.resize-pool.pool-resize
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
        // END: com.azure.compute.batch.resize-pool.pool-resize

        // BEGIN: com.azure.compute.batch.stop-resize-pool.stop-pool-resize
        SyncPoller<BatchPool, BatchPool> stopPoller = batchClient.beginStopPoolResize("poolId");

        // First poll
        PollResponse<BatchPool> stopFirst = stopPoller.poll();
        if (stopFirst.getStatus() == LongRunningOperationStatus.IN_PROGRESS && stopFirst.getValue() != null) {
            AllocationState interim = stopFirst.getValue().getAllocationState();
        }

        // Wait for completion
        stopPoller.waitForCompletion();
        BatchPool stoppedPool = stopPoller.getFinalResult();
        // END: com.azure.compute.batch.stop-resize-pool.stop-pool-resize

        // BEGIN: com.azure.compute.batch.enable-pool-auto-scale.pool-enable-autoscale
        BatchPoolEnableAutoScaleParameters autoScaleParameters = new BatchPoolEnableAutoScaleParameters()
            .setAutoScaleEvaluationInterval(Duration.ofMinutes(6))
            .setAutoScaleFormula("$TargetDedicated = 1;");

        batchClient.enablePoolAutoScale("poolId", autoScaleParameters);
        // END: com.azure.compute.batch.enable-pool-auto-scale.pool-enable-autoscale

        // BEGIN: com.azure.compute.batch.disable-pool-auto-scale.disable-pool-autoscale
        batchClient.disablePoolAutoScale("poolId");
        // END: com.azure.compute.batch.disable-pool-auto-scale.disable-pool-autoscale

        // BEGIN: com.azure.compute.batch.evaluate-pool-auto-scale.evaluate-pool-autoscale
        BatchPoolEvaluateAutoScaleParameters evalParams = new BatchPoolEvaluateAutoScaleParameters("$TargetDedicated = 1;");
        AutoScaleRun eval = batchClient.evaluatePoolAutoScale("poolId", evalParams);
        // END: com.azure.compute.batch.evaluate-pool-auto-scale.evaluate-pool-autoscale

        // BEGIN: com.azure.compute.batch.list-pool-node-counts.list-pool-node-counts
        batchClient.listPoolNodeCounts();
        // END: com.azure.compute.batch.list-pool-node-counts.list-pool-node-counts

        // BEGIN: com.azure.compute.batch.list-pool-usage-metrics.list-pool-usage-metrics
        batchClient.listPoolUsageMetrics();
        // END: com.azure.compute.batch.list-pool-usage-metrics.list-pool-usage-metrics

        // BEGIN: com.azure.compute.batch.list-supported-images.list-supported-images
        batchClient.listSupportedImages();
        // END: com.azure.compute.batch.list-supported-images.list-supported-images

        // BEGIN: com.azure.compute.batch.get-job.job-get
        BatchJob job = batchClient.getJob("jobId", null, null);
        // END: com.azure.compute.batch.get-job.job-get

        // BEGIN: com.azure.compute.batch.list-jobs.job-list
        PagedIterable<BatchJob> jobList = batchClient.listJobs(new BatchJobsListOptions());
        // END: com.azure.compute.batch.list-jobs.job-list

        // BEGIN: com.azure.compute.batch.delete-job.job-delete
        SyncPoller<BatchJob, Void> deleteJobPoller = batchClient.beginDeleteJob("jobId");

        PollResponse<BatchJob> initialDeleteJobResponse = deleteJobPoller.poll();
        if (initialDeleteJobResponse.getStatus() == LongRunningOperationStatus.IN_PROGRESS) {
            BatchJob jobDuringPoll = initialDeleteJobResponse.getValue();
        }

        // Wait for LRO to finish
        deleteJobPoller.waitForCompletion();
        PollResponse<BatchJob> finalDeleteJobResponse = deleteJobPoller.poll();
        // END: com.azure.compute.batch.delete-job.job-delete

        // BEGIN: com.azure.compute.batch.replace-job.job-patch
        batchClient.replaceJob("jobId",
            new BatchJob(new BatchPoolInfo().setPoolId("poolId")).setPriority(100)
                .setConstraints(
                    new BatchJobConstraints().setMaxWallClockTime(Duration.parse("PT1H")).setMaxTaskRetryCount(-1)),
            null, null);
        // END: com.azure.compute.batch.replace-job.job-patch

        // BEGIN: com.azure.compute.batch.update-job.job-update
        batchClient.updateJob("jobId",
            new BatchJobUpdateParameters().setPriority(100)
                .setConstraints(
                    new BatchJobConstraints().setMaxWallClockTime(Duration.parse("PT1H")).setMaxTaskRetryCount(-1))
                .setPoolInfo(new BatchPoolInfo().setPoolId("poolId")),
            null, null);
        // END: com.azure.compute.batch.update-job.job-update

        // BEGIN: com.azure.compute.batch.disable-job.job-disable
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
        // END: com.azure.compute.batch.disable-job.job-disable

        // BEGIN: com.azure.compute.batch.enable-job.job-enable
        SyncPoller<BatchJob, BatchJob> enablePoller = batchClient.beginEnableJob("jobId");

        // Inspect first poll
        PollResponse<BatchJob> enableFirst = enablePoller.poll();
        if (enableFirst.getStatus() == LongRunningOperationStatus.IN_PROGRESS) {
            BatchJob enableDuringPoll = enableFirst.getValue();
        }

        // Wait for completion of operation
        enablePoller.waitForCompletion();
        BatchJob enabledJob = enablePoller.getFinalResult();
        // END: com.azure.compute.batch.enable-job.job-enable

        // BEGIN: com.azure.compute.batch.job.list-job-preparation-and-release-task-status
        batchClient.listJobPreparationAndReleaseTaskStatus("jobId");
        // END: com.azure.compute.batch.job.list-job-preparation-and-release-task-status

        // BEGIN: com.azure.compute.batch.job.get-job-task-counts
        BatchTaskCountsResult counts = batchClient.getJobTaskCounts("jobId");
        // END: com.azure.compute.batch.job.get-job-task-counts

        // BEGIN: com.azure.compute.batch.job.terminate-job.simple
        BatchJobTerminateParameters terminateParams = new BatchJobTerminateParameters().setTerminationReason("ExampleReason");
        BatchJobTerminateOptions terminateOptions = new BatchJobTerminateOptions().setParameters(terminateParams);

        SyncPoller<BatchJob, BatchJob> terminatePoller = batchClient.beginTerminateJob("jobId", terminateOptions, null);
        // END: com.azure.compute.batch.job.terminate-job.simple

        // BEGIN: com.azure.compute.batch.job.terminate-job.final
        SyncPoller<BatchJob, BatchJob> terminateJobPoller = batchClient.beginTerminateJob("jobId", terminateOptions, null);
        BatchJob terminatedJob = terminateJobPoller.waitForCompletion().getValue();
        // END: com.azure.compute.batch.job.terminate-job.final

        // BEGIN: com.azure.compute.batch.job.terminate-job.poll-intermediate
        terminateJobPoller = batchClient.beginTerminateJob("jobId", terminateOptions, null);

        PollResponse<BatchJob> firstTerminateJobPoller = terminateJobPoller.poll();
        if (firstTerminateJobPoller.getStatus() == LongRunningOperationStatus.IN_PROGRESS) {
            BatchJob inProgressJob = firstTerminateJobPoller.getValue();
            System.out.println("Current job state: " + inProgressJob.getState());
        }

        terminatedJob = terminatePoller.waitForCompletion().getValue();
        // END: com.azure.compute.batch.job.terminate-job.poll-intermediate

        // BEGIN: com.azure.compute.batch.job.terminate-job.async.simple
        batchAsyncClient.beginTerminateJob("jobId", terminateOptions, null).subscribe();
        // END: com.azure.compute.batch.job.terminate-job.async.simple

        // BEGIN: com.azure.compute.batch.job.terminate-job.async.final
        batchAsyncClient.beginTerminateJob("jobId", terminateOptions, null)
            .takeUntil(response -> response.getStatus().isComplete())
            .last()
            .subscribe(finalResponse -> {
                BatchJob asyncTerminatedJob = finalResponse.getValue();
                System.out.println("Job termination completed. Final job state: " + asyncTerminatedJob.getState());
            });
        // END: com.azure.compute.batch.job.terminate-job.async.final

        // BEGIN: com.azure.compute.batch.job.terminate-job.async.poll-intermediate
        batchAsyncClient.beginTerminateJob("jobId", terminateOptions, null)
            .doOnNext(pollResponse -> {
                if (pollResponse.getStatus() == LongRunningOperationStatus.IN_PROGRESS) {
                    BatchJob inProgressJob = pollResponse.getValue();
                    System.out.println("Current job state: " + inProgressJob.getState());
                }
            })
            .takeUntil(response -> response.getStatus().isComplete())
            .last()
            .subscribe(finalResponse -> {
                BatchJob asyncTerminatedJob = finalResponse.getValue();
                System.out.println("Final job state: " + asyncTerminatedJob.getState());
            });
        // END: com.azure.compute.batch.job.terminate-job.async.poll-intermediate

        // BEGIN: com.azure.compute.batch.create-job-schedule.creates-a-basic-job-schedule
        batchClient.createJobSchedule(new BatchJobScheduleCreateParameters("jobScheduleId",
            new BatchJobScheduleConfiguration().setRecurrenceInterval(Duration.parse("PT5M")),
            new BatchJobSpecification(new BatchPoolInfo().setPoolId("poolId"))), null);
        // END: com.azure.compute.batch.create-job-schedule.creates-a-basic-job-schedule

        // BEGIN: com.azure.compute.batch.job-schedule.get-job-schedule
        batchClient.getJobSchedule("jobScheduleId");
        // END: com.azure.compute.batch.job-schedule.get-job-schedule

        // BEGIN: com.azure.compute.batch.job-schedule.list-job-schedules
        for (BatchJobSchedule schedule : batchClient.listJobSchedules()) {
            System.out.println(schedule.getId());
        }
        // END: com.azure.compute.batch.job-schedule.list-job-schedules

        // BEGIN: com.azure.compute.batch.job-schedule.delete-job-schedule
        SyncPoller<BatchJobSchedule, Void> jobScheduleDeletePoller = batchClient.beginDeleteJobSchedule("jobScheduleId");

        PollResponse<BatchJobSchedule> initialJobScheduleDeleteResponse = jobScheduleDeletePoller.poll();
        if (initialJobScheduleDeleteResponse.getStatus() == LongRunningOperationStatus.IN_PROGRESS) {
            BatchJobSchedule jobScheduleDuringPoll = initialJobScheduleDeleteResponse.getValue();
        }

        // Final response
        jobScheduleDeletePoller.waitForCompletion();
        PollResponse<BatchJobSchedule> finalJobScheduleDeleteResponse = jobScheduleDeletePoller.poll();
        // END: com.azure.compute.batch.job-schedule.delete-job-schedule

        // BEGIN: com.azure.compute.batch.replace-job-schedule.job-schedule-patch
        batchClient.replaceJobSchedule("jobScheduleId",
            new BatchJobSchedule(new BatchJobSpecification(new BatchPoolInfo().setPoolId("poolId")).setPriority(0)
                .setUsesTaskDependencies(false)
                .setConstraints(
                    new BatchJobConstraints().setMaxWallClockTime(Duration.parse("P10675199DT2H48M5.4775807S"))
                        .setMaxTaskRetryCount(0)))
                            .setSchedule(new BatchJobScheduleConfiguration()
                                .setDoNotRunUntil(OffsetDateTime.parse("2025-01-01T12:30:00Z"))),
            null, null);
        // END: com.azure.compute.batch.replace-job-schedule.job-schedule-patch

        // BEGIN: com.azure.compute.batch.job-schedule.update-job-schedule
        BatchJobScheduleUpdateParameters updateContent = new BatchJobScheduleUpdateParameters();
        updateContent.getMetadata().add(new BatchMetadataItem("key", "value"));
        batchClient.updateJobSchedule("jobScheduleId", updateContent);
        // END: com.azure.compute.batch.job-schedule.update-job-schedule

        // BEGIN: com.azure.compute.batch.job-schedule.disable-job-schedule
        batchClient.disableJobSchedule("jobScheduleId");
        // END: com.azure.compute.batch.job-schedule.disable-job-schedule

        // BEGIN: com.azure.compute.batch.job-schedule.enable-job-schedule
        batchClient.enableJobSchedule("jobScheduleId");
        // END: com.azure.compute.batch.job-schedule.enable-job-schedule

        // BEGIN: com.azure.compute.batch.job-schedule.terminate-job-schedule
        SyncPoller<BatchJobSchedule, BatchJobSchedule> terminateJobSchedulePoller = batchClient.beginTerminateJobSchedule("jobScheduleId");
        terminateJobSchedulePoller.waitForCompletion();
        BatchJobSchedule jobSchedule = terminateJobSchedulePoller.getFinalResult();
        // END: com.azure.compute.batch.job-schedule.terminate-job-schedule

        // BEGIN: com.azure.compute.batch.create-task.creates-a-task-collection
        List<BatchTaskCreateParameters> taskList = Arrays.asList(
            new BatchTaskCreateParameters("task1", "cmd /c echo Hello World"),
            new BatchTaskCreateParameters("task2", "cmd /c echo Hello World"));
        BatchTaskGroup taskGroup = new BatchTaskGroup(taskList);
        BatchCreateTaskCollectionResult result = batchClient.createTaskCollection("jobId", taskGroup);
        // END: com.azure.compute.batch.create-task.creates-a-task-collection

        // BEGIN: com.azure.compute.batch.create-task.create-tasks
        List<BatchTaskCreateParameters> tasks = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            tasks.add(new BatchTaskCreateParameters("task" + i, "cmd /c echo Hello World"));
        }
        batchClient.createTasks("jobId", tasks);
        // END: com.azure.compute.batch.create-task.create-tasks

        // BEGIN: com.azure.compute.batch.task.get-task
        batchClient.getTask("jobId", "taskId");
        // END: com.azure.compute.batch.task.get-task

        // BEGIN: com.azure.compute.batch.task.list-tasks
        batchClient.listTasks("jobId");
        // END: com.azure.compute.batch.task.list-tasks

        // BEGIN: com.azure.compute.batch.task.delete-task
        batchClient.deleteTask("jobId", "taskId");
        // END: com.azure.compute.batch.task.delete-task

        // BEGIN: com.azure.compute.batch.replace-task.task-update
        batchClient.replaceTask("jobId", "taskId",
            new BatchTask().setConstraints(new BatchTaskConstraints().setMaxWallClockTime(Duration.parse("PT1H"))
                .setRetentionTime(Duration.parse("PT1H"))
                .setMaxTaskRetryCount(3)),
            null, null);
        // END: com.azure.compute.batch.replace-task.task-update

        // BEGIN: com.azure.compute.batch.reactivate-task.task-reactivate
        batchClient.reactivateTask("jobId", "taskId", null, null);
        // END: com.azure.compute.batch.reactivate-task.task-reactivate

        // BEGIN: com.azure.compute.batch.terminate-task.task-terminate
        batchClient.terminateTask("jobId", "taskId", null, null);
        // END: com.azure.compute.batch.terminate-task.task-terminate

        // BEGIN: com.azure.compute.batch.task.list-task-files
        PagedIterable<BatchNodeFile> files = batchClient.listTaskFiles("jobId", "taskId");
        for (BatchNodeFile file : files) {
            System.out.println(file.getName());
        }
        // END: com.azure.compute.batch.task.list-task-files

        // BEGIN: com.azure.compute.batch.task.get-task-file
        BinaryData fileContent = batchClient.getTaskFile("jobId", "taskId", "stdout.txt");
        System.out.println(new String(fileContent.toBytes(), StandardCharsets.UTF_8));
        // END: com.azure.compute.batch.task.get-task-file

        // BEGIN: com.azure.compute.batch.get-task-file-properties.file-get-properties-from-task
        batchClient.getTaskFileProperties("jobId", "taskId", "wd\\testFile.txt",
            new BatchTaskFilePropertiesGetOptions());
        // END: com.azure.compute.batch.get-task-file-properties.file-get-properties-from-task

        // BEGIN: com.azure.compute.batch.get-node.node-get
        BatchNode node
            = batchClient.getNode("poolId", "tvm-1695681911_2-20161122t193202z", new BatchNodeGetOptions());
        // END: com.azure.compute.batch.get-node.node-get

        // BEGIN: com.azure.compute.batch.list-nodes.node-list
        PagedIterable<BatchNode> nodeList = batchClient.listNodes("poolId", new BatchNodesListOptions());
        // END: com.azure.compute.batch.list-nodes.node-list

        // BEGIN: com.azure.compute.batch.node.reboot-node
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
        // END: com.azure.compute.batch.node.reboot-node

        // BEGIN: com.azure.compute.batch.create-node-user.node-create-user
        batchClient.createNodeUser("poolId", "tvm-1695681911_1-20161121t182739z",
            new BatchNodeUserCreateParameters("userName").setIsAdmin(false)
                .setExpiryTime(OffsetDateTime.parse("2017-08-01T00:00:00Z"))
                .setPassword("fakeTokenPlaceholder"),
            null);
        // END: com.azure.compute.batch.create-node-user.node-create-user

        // BEGIN: com.azure.compute.batch.delete-node-user.node-delete-user
        batchClient.deleteNodeUser("poolId", "tvm-1695681911_1-20161121t182739z", "userName", null);
        // END: com.azure.compute.batch.delete-node-user.node-delete-user

        // BEGIN: com.azure.compute.batch.node.get-node-file
        BinaryData nodeFile = batchClient.getNodeFile("poolId", "nodeId", "filePath");
        // END: com.azure.compute.batch.node.get-node-file

        // BEGIN: com.azure.compute.batch.list-node-files.file-list-from-node
        PagedIterable<BatchNodeFile> listNodeFilesResponse = batchClient.listNodeFiles("poolId", "tvm-1695681911_1-20161122t193202z",
            new BatchNodeFilesListOptions().setRecursive(false));
        // END: com.azure.compute.batch.list-node-files.file-list-from-node

        // BEGIN: com.azure.compute.batch.delete-node-file.file-delete-from-node
        batchClient.deleteNodeFile("poolId", "tvm-1695681911_1-20161122t193202z",
            "workitems\\jobId\\job-1\\task1\\wd\\testFile.txt", new BatchNodeFileDeleteOptions().setRecursive(false));
        // END: com.azure.compute.batch.delete-node-file.file-delete-from-node

        // BEGIN: com.azure.compute.batch.get-node-file-properties.file-get-properties-from-node
        batchClient.getNodeFileProperties("poolId", "nodeId", "workitems\\jobId\\job-1\\task1\\wd\\testFile.txt",
            new BatchNodeFilePropertiesGetOptions());
        // END: com.azure.compute.batch.get-node-file-properties.file-get-properties-from-node

        // BEGIN: com.azure.compute.batch.get-node-remote-login-settings.node-get-remote-login-settings
        BatchNodeRemoteLoginSettings settings
            = batchClient.getNodeRemoteLoginSettings("poolId", "tvm-1695681911_1-20161121t182739z", null);
        // END: com.azure.compute.batch.get-node-remote-login-settings.node-get-remote-login-settings

        // BEGIN: com.azure.compute.batch.upload-node-logs.upload-batch-service-logs
        UploadBatchServiceLogsResult uploadNodeLogsResult
            = batchClient.uploadNodeLogs("poolId", "nodeId", null);
        // END: com.azure.compute.batch.upload-node-logs.upload-batch-service-logs

        // BEGIN: com.azure.compute.batch.upload-node-logs.upload-batch-service-logs-async
        batchAsyncClient.uploadNodeLogs("poolId", "nodeId", null)
            .subscribe(logResult -> {
                System.out.println("Number of files uploaded: " + logResult.getNumberOfFilesUploaded());
                System.out.println("Log upload container URL: " + logResult.getVirtualDirectoryName());
            });
        // END: com.azure.compute.batch.upload-node-logs.upload-batch-service-logs-async

        // BEGIN: com.azure.compute.batch.create-certificate.certificate-create
        batchClient.createCertificate(
            new BatchCertificate("0123456789abcdef0123456789abcdef01234567", "sha1", "U3dhZ2randomByb2Hash==".getBytes())
                .setCertificateFormat(BatchCertificateFormat.PFX)
                .setPassword("fakeTokenPlaceholder"),
            null);
        // END: com.azure.compute.batch.create-certificate.certificate-create

        // BEGIN: com.azure.compute.batch.get-certificate.certificate-get
        BatchCertificate certificateResponse = batchClient.getCertificate("sha1", "0123456789abcdef0123456789abcdef01234567",
            new BatchCertificateGetOptions());
        // END: com.azure.compute.batch.get-certificate.certificate-get

        // BEGIN: com.azure.compute.batch.list-certificates.certificate-list
        PagedIterable<BatchCertificate> certificateList = batchClient.listCertificates(new BatchCertificatesListOptions());
        // END: com.azure.compute.batch.list-certificates.certificate-list

        // BEGIN: com.azure.compute.batch.certificate.delete-certificate
        String thumbprintAlgorithm = "sha1";
        String thumbprint = "your-thumbprint";
        SyncPoller<BatchCertificate, Void> deleteCertificatePoller = batchClient.beginDeleteCertificate(thumbprintAlgorithm, thumbprint);
        deleteCertificatePoller.waitForCompletion();
        PollResponse<BatchCertificate> finalDeleteCertificateResponse = deleteCertificatePoller.poll();
        // END: com.azure.compute.batch.certificate.delete-certificate

        // BEGIN: com.azure.compute.batch.cancel-certificate-deletion.certificate-cancel-delete
        batchClient.cancelCertificateDeletion("sha1", "0123456789abcdef0123456789abcdef01234567", null);
        // END: com.azure.compute.batch.cancel-certificate-deletion.certificate-cancel-delete

        // BEGIN: com.azure.compute.batch.get-application.get-applications
        BatchApplication application = batchClient.getApplication("my_application_id", null);
        // END: com.azure.compute.batch.get-application.get-applications

        // BEGIN: com.azure.compute.batch.list-applications.list-applications
        PagedIterable<BatchApplication> applications = batchClient.listApplications(new BatchApplicationsListOptions());
        // END: com.azure.compute.batch.list-applications.list-applications

        // BEGIN: com.azure.compute.batch.node.deallocate-node
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
        // END: com.azure.compute.batch.node.deallocate-node

        // BEGIN: com.azure.compute.batch.node.reimage-node
        SyncPoller<BatchNode, BatchNode> reimagePoller = batchClient.beginReimageNode("poolId", "nodeId");

        // Retrieve the value while the operation is in progress
        PollResponse<BatchNode> reimageFirst = reimagePoller.poll();
        BatchNode nodeDuringReimage = reimageFirst.getValue();

        // Wait until the the node is usable
        reimagePoller.waitForCompletion();
        BatchNode reimagedNode = reimagePoller.getFinalResult();
        // END: com.azure.compute.batch.node.reimage-node

        // BEGIN: com.azure.compute.batch.node.start-node
        SyncPoller<BatchNode, BatchNode> startPoller = batchClient.beginStartNode("poolId", "nodeId");

        // First poll
        PollResponse<BatchNode> startFirst = startPoller.poll();
        BatchNode firstVal = startFirst.getValue();

        // Final result
        startPoller.waitForCompletion();
        BatchNode startedNode = startPoller.getFinalResult();
        // END: com.azure.compute.batch.node.start-node

        // BEGIN: com.azure.compute.batch.pool.remove-nodes
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
        // END: com.azure.compute.batch.pool.remove-nodes
    }
}
