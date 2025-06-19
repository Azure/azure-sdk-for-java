// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch;

import com.azure.compute.batch.models.*;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;

import reactor.core.publisher.Mono;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.azure.core.test.SyncAsyncExtension;
import com.azure.core.test.TestMode;
import com.azure.core.test.annotation.SyncAsyncTest;

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

        try {
            // GET
            final BatchJob[] jobHolder = new BatchJob[1];
            jobHolder[0]
                = SyncAsyncExtension.execute(() -> batchClient.getJob(jobId), () -> batchAsyncClient.getJob(jobId));
            Assertions.assertNotNull(jobHolder[0]);
            Assertions.assertNotNull(jobHolder[0].isAllowTaskPreemption());
            Assertions.assertEquals(-1, (int) jobHolder[0].getMaxParallelTasks());
            Assertions.assertEquals(jobId, jobHolder[0].getId());
            Assertions.assertEquals((Integer) 0, jobHolder[0].getPriority());

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
            BatchJob replacementJob = jobHolder[0];
            replacementJob.setPriority(1);
            SyncAsyncExtension.execute(() -> batchClient.replaceJob(jobId, replacementJob),
                () -> batchAsyncClient.replaceJob(jobId, replacementJob));

            jobHolder[0]
                = SyncAsyncExtension.execute(() -> batchClient.getJob(jobId), () -> batchAsyncClient.getJob(jobId));
            Assertions.assertEquals((Integer) 1, jobHolder[0].getPriority());

            // DELETE
            SyncAsyncExtension.execute(() -> batchClient.deleteJob(jobId), () -> batchAsyncClient.deleteJob(jobId));
            try {
                SyncAsyncExtension.execute(() -> batchClient.getJob(jobId), () -> batchAsyncClient.getJob(jobId));
                Assertions.assertTrue(true, "Shouldn't be here, the job should be deleted");
            } catch (Exception e) {
                if (!e.getMessage().contains("Status code 404")) {
                    throw e;
                }
            }

            sleepIfRunningAgainstService(1000);
        } finally {
            try {
                SyncAsyncExtension.execute(() -> batchClient.deleteJob(jobId), () -> batchAsyncClient.deleteJob(jobId));
            } catch (Exception e) {
                // Ignore here
            }
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
            final BatchJob[] jobHolder = new BatchJob[1];
            jobHolder[0]
                = SyncAsyncExtension.execute(() -> batchClient.getJob(jobId), () -> batchAsyncClient.getJob(jobId));
            Assertions.assertEquals(BatchJobState.ACTIVE, jobHolder[0].getState());

            // REPLACE
            Integer maxTaskRetryCount = 3;
            Integer priority = 500;
            jobHolder[0].setPriority(priority);
            jobHolder[0].setConstraints(new BatchJobConstraints().setMaxTaskRetryCount(maxTaskRetryCount));
            jobHolder[0].getPoolInfo().setPoolId(poolId);

            SyncAsyncExtension.execute(() -> batchClient.replaceJob(jobId, jobHolder[0]),
                () -> batchAsyncClient.replaceJob(jobId, jobHolder[0]));

            jobHolder[0]
                = SyncAsyncExtension.execute(() -> batchClient.getJob(jobId), () -> batchAsyncClient.getJob(jobId));
            Assertions.assertEquals(priority, jobHolder[0].getPriority());
            Assertions.assertEquals(maxTaskRetryCount, jobHolder[0].getConstraints().getMaxTaskRetryCount());

            // DISABLE
            BatchJobDisableParameters disableParams = new BatchJobDisableParameters(DisableBatchJobOption.REQUEUE);
            SyncAsyncExtension.execute(() -> batchClient.disableJob(jobId, disableParams),
                () -> batchAsyncClient.disableJob(jobId, disableParams));

            jobHolder[0]
                = SyncAsyncExtension.execute(() -> batchClient.getJob(jobId), () -> batchAsyncClient.getJob(jobId));
            Assertions.assertEquals(BatchJobState.DISABLING, jobHolder[0].getState());

            sleepIfRunningAgainstService(5000);

            jobHolder[0]
                = SyncAsyncExtension.execute(() -> batchClient.getJob(jobId), () -> batchAsyncClient.getJob(jobId));
            Assertions.assertTrue(jobHolder[0].getState() == BatchJobState.DISABLED
                || jobHolder[0].getState() == BatchJobState.DISABLING);
            Assertions.assertEquals(BatchAllTasksCompleteMode.NO_ACTION, jobHolder[0].getAllTasksCompleteMode());

            // UPDATE
            BatchJobUpdateParameters updateParams
                = new BatchJobUpdateParameters().setAllTasksCompleteMode(BatchAllTasksCompleteMode.TERMINATE_JOB);
            SyncAsyncExtension.execute(() -> batchClient.updateJob(jobId, updateParams),
                () -> batchAsyncClient.updateJob(jobId, updateParams));

            jobHolder[0]
                = SyncAsyncExtension.execute(() -> batchClient.getJob(jobId), () -> batchAsyncClient.getJob(jobId));
            Assertions.assertEquals(BatchAllTasksCompleteMode.TERMINATE_JOB, jobHolder[0].getAllTasksCompleteMode());

            // ENABLE
            SyncAsyncExtension.execute(() -> batchClient.enableJob(jobId), () -> batchAsyncClient.enableJob(jobId));

            jobHolder[0]
                = SyncAsyncExtension.execute(() -> batchClient.getJob(jobId), () -> batchAsyncClient.getJob(jobId));
            Assertions.assertEquals(BatchJobState.ACTIVE, jobHolder[0].getState());

            // TERMINATE
            BatchJobTerminateParameters terminateParams
                = new BatchJobTerminateParameters().setTerminationReason("myreason");
            BatchJobTerminateOptions terminateOptions = new BatchJobTerminateOptions().setParameters(terminateParams);

            SyncAsyncExtension.execute(() -> batchClient.terminateJob(jobId, terminateOptions, null),
                () -> batchAsyncClient.terminateJob(jobId, terminateOptions, null));

            jobHolder[0]
                = SyncAsyncExtension.execute(() -> batchClient.getJob(jobId), () -> batchAsyncClient.getJob(jobId));
            Assertions.assertEquals(BatchJobState.TERMINATING, jobHolder[0].getState());

            sleepIfRunningAgainstService(2000);

            jobHolder[0]
                = SyncAsyncExtension.execute(() -> batchClient.getJob(jobId), () -> batchAsyncClient.getJob(jobId));
            Assertions.assertEquals(BatchJobState.COMPLETED, jobHolder[0].getState());

        } finally {
            try {
                SyncAsyncExtension.execute(() -> batchClient.deleteJob(jobId), () -> batchAsyncClient.deleteJob(jobId));
            } catch (Exception e) {
                // Ignore here
            }
        }
    }

    @SyncAsyncTest
    public void canCRUDJobWithPoolNodeCommunicationMode() {
        // Generate a jobId that is unique per test mode (sync vs async)
        String testModeSuffix = SyncAsyncExtension.execute(() -> "sync", () -> Mono.just("async"));
        String jobId = getStringIdWithUserNamePrefix(
            "-Job-canCRUDWithPoolNodeComm" + testModeSuffix + "-" + System.currentTimeMillis());
        BatchNodeCommunicationMode targetMode = BatchNodeCommunicationMode.SIMPLIFIED;

        BatchVmImageReference imgRef = new BatchVmImageReference().setPublisher("microsoftwindowsserver")
            .setOffer("windowsserver")
            .setSku("2022-datacenter-smalldisk-g2");

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

        try {
            // GET
            final BatchJob[] jobHolder = new BatchJob[1];
            jobHolder[0]
                = SyncAsyncExtension.execute(() -> batchClient.getJob(jobId), () -> batchAsyncClient.getJob(jobId));
            Assertions.assertNotNull(jobHolder[0]);
            Assertions.assertEquals(jobId, jobHolder[0].getId());
            Assertions.assertEquals(targetMode,
                jobHolder[0].getPoolInfo().getAutoPoolSpecification().getPool().getTargetNodeCommunicationMode());

            // DELETE
            SyncAsyncExtension.execute(() -> batchClient.deleteJob(jobId), () -> batchAsyncClient.deleteJob(jobId));

            try {
                SyncAsyncExtension.execute(() -> batchClient.getJob(jobId), () -> batchAsyncClient.getJob(jobId));
                Assertions.assertTrue(true, "Shouldn't be here, the job should be deleted");
            } catch (Exception err) {
                if (!err.getMessage().contains("Status code 404")) {
                    throw err;
                }
            }

            sleepIfRunningAgainstService(15000);
        } finally {
            try {
                SyncAsyncExtension.execute(() -> batchClient.deleteJob(jobId), () -> batchAsyncClient.deleteJob(jobId));
            } catch (Exception e) {
                // Ignore here
            }
        }
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
