// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch;

import com.azure.compute.batch.models.*;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;

import reactor.core.publisher.Mono;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.test.SyncAsyncExtension;
import com.azure.core.test.annotation.SyncAsyncTest;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;

import java.io.IOException;
import java.io.StringReader;
import java.time.Duration;
import java.time.OffsetDateTime;

public class JobTests extends BatchClientTestBase {
    private static BatchPool livePool;
    static String poolId;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        poolId = getStringIdWithUserNamePrefix("-testpool");
        if (livePool == null) {
            try {
                livePool = createIfNotExistIaaSPool(poolId);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Assertions.assertNotNull(livePool);
        }
    }

    @SyncAsyncTest
    public void canCrudJob() {
        // Generate a jobId that is unique per test mode (sync vs async)
        String testModeSuffix = SyncAsyncExtension.execute(() -> "sync", () -> Mono.just("async"));
        // CREATE
        String jobId = getStringIdWithUserNamePrefix("-Job-canCRUD" + testModeSuffix);

        BatchPoolInfo poolInfo = new BatchPoolInfo();
        poolInfo.setPoolId(poolId);
        BatchJobCreateParameters jobToCreate = new BatchJobCreateParameters(jobId, poolInfo);

        SyncAsyncExtension.execute(() -> batchClient.createJob(jobToCreate),
            () -> batchAsyncClient.createJob(jobToCreate));

        // GET
        BatchJob job
            = SyncAsyncExtension.execute(() -> batchClient.getJob(jobId), () -> batchAsyncClient.getJob(jobId));
        Assertions.assertNotNull(job);
        Assertions.assertNotNull(job.isAllowTaskPreemption());
        Assertions.assertEquals(-1, (int) job.getMaxParallelTasks());
        Assertions.assertEquals(jobId, job.getId());
        Assertions.assertEquals((Integer) 0, job.getPriority());

        // LIST
        Iterable<BatchJob> jobs = SyncAsyncExtension.execute(() -> batchClient.listJobs(),
            () -> Mono.fromCallable(() -> batchAsyncClient.listJobs().toIterable()));
        Assertions.assertNotNull(jobs);

        boolean found = false;
        for (BatchJob batchJob : jobs) {
            if (batchJob.getId().equals(jobId)) {
                found = true;
                break;
            }
        }

        Assertions.assertTrue(found);

        // REPLACE
        BatchJob replacementJob = job;
        replacementJob.setPriority(1);
        SyncAsyncExtension.execute(() -> batchClient.replaceJob(jobId, replacementJob),
            () -> batchAsyncClient.replaceJob(jobId, replacementJob));

        job = SyncAsyncExtension.execute(() -> batchClient.getJob(jobId), () -> batchAsyncClient.getJob(jobId));
        Assertions.assertEquals((Integer) 1, job.getPriority());

        sleepIfRunningAgainstService(1000);

        // DELETE using LRO
        SyncPoller<BatchJob, Void> poller
            = setPlaybackSyncPollerPollInterval(SyncAsyncExtension.execute(() -> batchClient.beginDeleteJob(jobId),
                () -> Mono.fromCallable(() -> batchAsyncClient.beginDeleteJob(jobId).getSyncPoller())));

        // Validate initial poll result (job should be in DELETING state)
        PollResponse<BatchJob> initialResponse = poller.poll();
        if (initialResponse.getStatus() == LongRunningOperationStatus.IN_PROGRESS) {
            BatchJob jobDuringPoll = initialResponse.getValue();
            Assertions.assertNotNull(jobDuringPoll, "Expected job data during polling");
            Assertions.assertEquals(jobId, jobDuringPoll.getId());
            Assertions.assertEquals(BatchJobState.DELETING, jobDuringPoll.getState());
        }

        // Wait for LRO to finish
        poller.waitForCompletion();

        // Final result should be null after successful deletion
        PollResponse<BatchJob> finalResponse = poller.poll();
        Assertions.assertNull(finalResponse.getValue(), "Expected final result to be null after successful deletion");

        // Confirm job is no longer retrievable
        try {
            SyncAsyncExtension.execute(() -> batchClient.getJob(jobId), () -> batchAsyncClient.getJob(jobId));
            Assertions.fail("Expected job to be deleted.");
        } catch (HttpResponseException ex) {
            Assertions.assertEquals(404, ex.getResponse().getStatusCode());
        }
    }

    @SyncAsyncTest
    public void canUpdateJobState() {
        // Generate a jobId that is unique per test mode (sync vs async)
        String testModeSuffix = SyncAsyncExtension.execute(() -> "sync", () -> Mono.just("async"));
        String jobId = getStringIdWithUserNamePrefix("-Job-CanUpdateState" + testModeSuffix);

        BatchPoolInfo poolInfo = new BatchPoolInfo().setPoolId(poolId);
        BatchJobCreateParameters jobToCreate = new BatchJobCreateParameters(jobId, poolInfo);

        // CREATE
        SyncAsyncExtension.execute(() -> batchClient.createJob(jobToCreate),
            () -> batchAsyncClient.createJob(jobToCreate));

        try {
            // GET
            BatchJob job
                = SyncAsyncExtension.execute(() -> batchClient.getJob(jobId), () -> batchAsyncClient.getJob(jobId));
            Assertions.assertEquals(BatchJobState.ACTIVE, job.getState());

            // REPLACE
            Integer maxTaskRetryCount = 3;
            Integer priority = 500;
            BatchJob replacementJob = job;
            replacementJob.setPriority(priority);
            replacementJob.setConstraints(new BatchJobConstraints().setMaxTaskRetryCount(maxTaskRetryCount));
            replacementJob.getPoolInfo().setPoolId(poolId);

            SyncAsyncExtension.execute(() -> batchClient.replaceJob(jobId, replacementJob),
                () -> batchAsyncClient.replaceJob(jobId, replacementJob));

            job = SyncAsyncExtension.execute(() -> batchClient.getJob(jobId), () -> batchAsyncClient.getJob(jobId));
            Assertions.assertEquals(priority, job.getPriority());
            Assertions.assertEquals(maxTaskRetryCount, job.getConstraints().getMaxTaskRetryCount());

            // DISABLE
            BatchJobDisableParameters disableParams = new BatchJobDisableParameters(DisableBatchJobOption.REQUEUE);

            SyncPoller<BatchJob, BatchJob> disablePoller = setPlaybackSyncPollerPollInterval(SyncAsyncExtension.execute(
                () -> batchClient.beginDisableJob(jobId, disableParams),
                () -> Mono.fromCallable(() -> batchAsyncClient.beginDisableJob(jobId, disableParams).getSyncPoller())));

            // Inspect first poll
            PollResponse<BatchJob> disableFirst = disablePoller.poll();
            if (disableFirst.getStatus() == LongRunningOperationStatus.IN_PROGRESS) {
                BatchJob disableDuringPoll = disableFirst.getValue();
                Assertions.assertNotNull(disableDuringPoll);
                Assertions.assertEquals(jobId, disableDuringPoll.getId());
                Assertions.assertEquals(BatchJobState.DISABLING, disableDuringPoll.getState());
            }

            disablePoller.waitForCompletion();

            BatchJob disabledJob = disablePoller.getFinalResult();
            Assertions.assertNotNull(disabledJob);
            Assertions.assertEquals(BatchJobState.DISABLED, disabledJob.getState());
            Assertions.assertEquals(BatchAllTasksCompleteMode.NO_ACTION, disabledJob.getAllTasksCompleteMode());

            // UPDATE
            BatchJobUpdateParameters updateParams
                = new BatchJobUpdateParameters().setAllTasksCompleteMode(BatchAllTasksCompleteMode.TERMINATE_JOB);
            SyncAsyncExtension.execute(() -> batchClient.updateJob(jobId, updateParams),
                () -> batchAsyncClient.updateJob(jobId, updateParams));

            job = SyncAsyncExtension.execute(() -> batchClient.getJob(jobId), () -> batchAsyncClient.getJob(jobId));
            Assertions.assertEquals(BatchAllTasksCompleteMode.TERMINATE_JOB, job.getAllTasksCompleteMode());

            // ENABLE
            SyncPoller<BatchJob, BatchJob> enablePoller
                = setPlaybackSyncPollerPollInterval(SyncAsyncExtension.execute(() -> batchClient.beginEnableJob(jobId),
                    () -> Mono.fromCallable(() -> batchAsyncClient.beginEnableJob(jobId).getSyncPoller())));

            // Inspect first poll
            PollResponse<BatchJob> enableFirst = enablePoller.poll();
            if (enableFirst.getStatus() == LongRunningOperationStatus.IN_PROGRESS) {
                BatchJob enableDuringPoll = enableFirst.getValue();
                Assertions.assertNotNull(enableDuringPoll);
                Assertions.assertEquals(jobId, enableDuringPoll.getId());
                Assertions.assertEquals(BatchJobState.ENABLING, enableDuringPoll.getState());
            }

            enablePoller.waitForCompletion();

            BatchJob enabledJob = enablePoller.getFinalResult();
            Assertions.assertNotNull(enabledJob);
            Assertions.assertEquals(BatchJobState.ACTIVE, enabledJob.getState());

            // TERMINATE
            BatchJobTerminateParameters terminateParams
                = new BatchJobTerminateParameters().setTerminationReason("myreason");
            BatchJobTerminateOptions terminateOptions = new BatchJobTerminateOptions().setParameters(terminateParams);

            SyncPoller<BatchJob, BatchJob> terminatePoller = setPlaybackSyncPollerPollInterval(SyncAsyncExtension
                .execute(() -> batchClient.beginTerminateJob(jobId, terminateOptions, null), () -> Mono.fromCallable(
                    () -> batchAsyncClient.beginTerminateJob(jobId, terminateOptions, null).getSyncPoller())));

            // Inspect the first poll
            PollResponse<BatchJob> first = terminatePoller.poll();
            if (first.getStatus() == LongRunningOperationStatus.IN_PROGRESS) {
                BatchJob pollingJob = first.getValue();
                Assertions.assertNotNull(pollingJob);
                Assertions.assertEquals(jobId, pollingJob.getId());
                Assertions.assertEquals(BatchJobState.TERMINATING, pollingJob.getState());
            }

            terminatePoller.waitForCompletion();

            BatchJob finalJob = terminatePoller.getFinalResult();
            Assertions.assertNotNull(finalJob);
            Assertions.assertEquals(BatchJobState.COMPLETED, finalJob.getState());

        } finally {
            // DELETE
            try {
                SyncPoller<BatchJob, Void> deletePoller = setPlaybackSyncPollerPollInterval(
                    SyncAsyncExtension.execute(() -> batchClient.beginDeleteJob(jobId),
                        () -> Mono.fromCallable(() -> batchAsyncClient.beginDeleteJob(jobId).getSyncPoller())));

                deletePoller.waitForCompletion();
            } catch (Exception e) {
                System.err.println("Cleanup failed for job: " + jobId);
                e.printStackTrace();
            }
        }
    }

    @SyncAsyncTest
    public void canCRUDJobWithPoolNodeCommunicationMode() {
        String testModeSuffix = SyncAsyncExtension.execute(() -> "sync", () -> Mono.just("async"));
        String jobId = getStringIdWithUserNamePrefix("-Job-canCRUDWithPoolNodeComm" + testModeSuffix);
        BatchNodeCommunicationMode targetMode = BatchNodeCommunicationMode.SIMPLIFIED;

        BatchVmImageReference imgRef = new BatchVmImageReference().setPublisher("microsoftwindowsserver")
            .setOffer("windowsserver")
            .setSku("2022-datacenter-smalldisk");

        VirtualMachineConfiguration configuration = new VirtualMachineConfiguration(imgRef, "batch.node.windows amd64");

        BatchPoolSpecification poolSpec
            = new BatchPoolSpecification("STANDARD_D1_V2").setVirtualMachineConfiguration(configuration)
                .setTargetNodeCommunicationMode(targetMode);

        BatchPoolInfo poolInfo = new BatchPoolInfo()
            .setAutoPoolSpecification(new BatchAutoPoolSpecification(BatchPoolLifetimeOption.JOB).setPool(poolSpec));

        BatchJobCreateParameters jobToCreate = new BatchJobCreateParameters(jobId, poolInfo);

        // CREATE
        SyncAsyncExtension.execute(() -> batchClient.createJob(jobToCreate),
            () -> batchAsyncClient.createJob(jobToCreate));

        // GET
        BatchJob job
            = SyncAsyncExtension.execute(() -> batchClient.getJob(jobId), () -> batchAsyncClient.getJob(jobId));
        Assertions.assertNotNull(job);
        Assertions.assertEquals(jobId, job.getId());
        Assertions.assertEquals(targetMode,
            job.getPoolInfo().getAutoPoolSpecification().getPool().getTargetNodeCommunicationMode());

        // DELETE using LRO
        SyncPoller<BatchJob, Void> poller
            = setPlaybackSyncPollerPollInterval(SyncAsyncExtension.execute(() -> batchClient.beginDeleteJob(jobId),
                () -> Mono.fromCallable(() -> batchAsyncClient.beginDeleteJob(jobId).getSyncPoller())));

        // Wait for LRO to complete
        poller.waitForCompletion();

        // Confirm job is deleted
        try {
            SyncAsyncExtension.execute(() -> batchClient.getJob(jobId), () -> batchAsyncClient.getJob(jobId));
            Assertions.fail("Expected job to be deleted.");
        } catch (HttpResponseException ex) {
            Assertions.assertEquals(404, ex.getResponse().getStatusCode());
        }

        sleepIfRunningAgainstService(15000);
    }

    @Test
    public void testDeserializationOfBatchJobStatistics() {
        // Simulated JSON response with numbers as strings
        String jsonResponse = "{" + "\"url\":\"https://example.com/stats\"," + "\"startTime\":\"2022-01-01T00:00:00Z\","
            + "\"lastUpdateTime\":\"2022-01-01T01:00:00Z\"," + "\"userCPUTime\":\"PT1H\","
            + "\"kernelCPUTime\":\"PT30M\"," + "\"wallClockTime\":\"PT1H30M\"," + "\"readIOps\":\"1000\","
            + "\"writeIOps\":\"500\"," + "\"readIOGiB\":0.5," + "\"writeIOGiB\":0.25," + "\"numSucceededTasks\":\"10\","
            + "\"numFailedTasks\":\"2\"," + "\"numTaskRetries\":\"3\"," + "\"waitTime\":\"PT10M\"" + "}";

        // Deserialize JSON response using JsonReader from JsonProviders
        try (JsonReader jsonReader = JsonProviders.createReader(new StringReader(jsonResponse))) {
            BatchJobStatistics stats = BatchJobStatistics.fromJson(jsonReader);

            // Assertions
            Assertions.assertNotNull(stats);
            Assertions.assertEquals("https://example.com/stats", stats.getUrl());
            Assertions.assertEquals(OffsetDateTime.parse("2022-01-01T00:00:00Z"), stats.getStartTime());
            Assertions.assertEquals(OffsetDateTime.parse("2022-01-01T01:00:00Z"), stats.getLastUpdateTime());
            Assertions.assertEquals(Duration.parse("PT1H"), stats.getUserCpuTime());
            Assertions.assertEquals(Duration.parse("PT30M"), stats.getKernelCpuTime());
            Assertions.assertEquals(Duration.parse("PT1H30M"), stats.getWallClockTime());
            Assertions.assertEquals(1000, stats.getReadIops());
            Assertions.assertEquals(500, stats.getWriteIops());
            Assertions.assertEquals(0.5, stats.getReadIoGiB());
            Assertions.assertEquals(0.25, stats.getWriteIoGiB());
            Assertions.assertEquals(10, stats.getSucceededTasksCount());
            Assertions.assertEquals(2, stats.getFailedTasksCount());
            Assertions.assertEquals(3, stats.getTaskRetriesCount());
            Assertions.assertEquals(Duration.parse("PT10M"), stats.getWaitTime());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
