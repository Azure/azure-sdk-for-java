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
import com.azure.core.test.TestMode;
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
        if (getTestMode() == TestMode.RECORD) {
            if (livePool == null) {
                try {
                    livePool = createIfNotExistIaaSPool(poolId);
                } catch (Exception e) {
                    // TODO (catch): Auto-generated catch block
                    e.printStackTrace();
                }
                Assertions.assertNotNull(livePool);
            }
        }
    }

    @SyncAsyncTest
    public void canCrudJob() {
        // Generate a jobId that is unique per test mode (sync vs async)
        String testModeSuffix = SyncAsyncExtension.execute(() -> "sync", () -> Mono.just("async"));
        // CREATE
        String jobId
            = getStringIdWithUserNamePrefix("-Job-canCRUD" + testModeSuffix + "-" + System.currentTimeMillis());

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
        String jobId
            = getStringIdWithUserNamePrefix("-Job-CanUpdateState" + testModeSuffix + "-" + System.currentTimeMillis());

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
            SyncAsyncExtension.execute(() -> batchClient.disableJob(jobId, disableParams),
                () -> batchAsyncClient.disableJob(jobId, disableParams));

            job = SyncAsyncExtension.execute(() -> batchClient.getJob(jobId), () -> batchAsyncClient.getJob(jobId));
            Assertions.assertEquals(BatchJobState.DISABLING, job.getState());

            sleepIfRunningAgainstService(5000);

            job = SyncAsyncExtension.execute(() -> batchClient.getJob(jobId), () -> batchAsyncClient.getJob(jobId));
            Assertions
                .assertTrue(job.getState() == BatchJobState.DISABLED || job.getState() == BatchJobState.DISABLING);
            Assertions.assertEquals(BatchAllTasksCompleteMode.NO_ACTION, job.getAllTasksCompleteMode());

            // UPDATE
            BatchJobUpdateParameters updateParams
                = new BatchJobUpdateParameters().setAllTasksCompleteMode(BatchAllTasksCompleteMode.TERMINATE_JOB);
            SyncAsyncExtension.execute(() -> batchClient.updateJob(jobId, updateParams),
                () -> batchAsyncClient.updateJob(jobId, updateParams));

            job = SyncAsyncExtension.execute(() -> batchClient.getJob(jobId), () -> batchAsyncClient.getJob(jobId));
            Assertions.assertEquals(BatchAllTasksCompleteMode.TERMINATE_JOB, job.getAllTasksCompleteMode());

            // ENABLE
            SyncAsyncExtension.execute(() -> batchClient.enableJob(jobId), () -> batchAsyncClient.enableJob(jobId));

            job = SyncAsyncExtension.execute(() -> batchClient.getJob(jobId), () -> batchAsyncClient.getJob(jobId));
            Assertions.assertEquals(BatchJobState.ACTIVE, job.getState());

            // TERMINATE
            BatchJobTerminateParameters terminateParams
                = new BatchJobTerminateParameters().setTerminationReason("myreason");
            BatchJobTerminateOptions terminateOptions = new BatchJobTerminateOptions().setParameters(terminateParams);

            SyncAsyncExtension.execute(() -> batchClient.terminateJob(jobId, terminateOptions, null),
                () -> batchAsyncClient.terminateJob(jobId, terminateOptions, null));

            job = SyncAsyncExtension.execute(() -> batchClient.getJob(jobId), () -> batchAsyncClient.getJob(jobId));
            Assertions.assertEquals(BatchJobState.TERMINATING, job.getState());

            sleepIfRunningAgainstService(2000);

            job = SyncAsyncExtension.execute(() -> batchClient.getJob(jobId), () -> batchAsyncClient.getJob(jobId));
            Assertions.assertEquals(BatchJobState.COMPLETED, job.getState());

        } finally {
            // DELETE
            try {
                SyncPoller<BatchJob, Void> deletePoller = setPlaybackSyncPollerPollInterval(
                    SyncAsyncExtension.execute(() -> batchClient.beginDeleteJob(jobId),
                        () -> Mono.fromCallable(() -> batchAsyncClient.beginDeleteJob(jobId).getSyncPoller())));

                deletePoller.waitForCompletion();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SyncAsyncTest
    public void canCRUDJobWithPoolNodeCommunicationMode() {
        String testModeSuffix = SyncAsyncExtension.execute(() -> "sync", () -> Mono.just("async"));
        String jobId = getStringIdWithUserNamePrefix(
            "-Job-canCRUDWithPoolNodeComm" + testModeSuffix + "-" + System.currentTimeMillis());
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
