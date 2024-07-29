// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch;

import com.azure.compute.batch.models.*;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.test.TestMode;

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

    @Test
    public void canCrudJob() {
        // CREATE
        String jobId = getStringIdWithUserNamePrefix("-Job-canCRUD");

        BatchPoolInfo poolInfo = new BatchPoolInfo();
        poolInfo.setPoolId(poolId);
        BatchJobCreateContent jobToCreate = new BatchJobCreateContent(jobId, poolInfo);

        batchClient.createJob(jobToCreate);

        try {
            // GET
            BatchJob job = batchClient.getJob(jobId);
            Assertions.assertNotNull(job);
            Assertions.assertNotNull(job.isAllowTaskPreemption());
            Assertions.assertEquals(-1, (int) job.getMaxParallelTasks());
            Assertions.assertEquals(jobId, job.getId());
            Assertions.assertEquals((Integer) 0, job.getPriority());

            // LIST
            PagedIterable<BatchJob> jobs = batchClient.listJobs();
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
            batchClient.replaceJob(jobId, replacementJob);

            job = batchClient.getJob(jobId);
            Assertions.assertEquals((Integer) 1, job.getPriority());

            // DELETE
            batchClient.deleteJob(jobId);
            try {
                batchClient.getJob(jobId);
                Assertions.assertTrue(true, "Shouldn't be here, the job should be deleted");
            } catch (Exception e) {
                if (!e.getMessage().contains("Status code 404")) {
                    throw e;
                }
            }

            sleepIfRunningAgainstService(1000);
        } finally {
            try {
                batchClient.deleteJob(jobId);
            } catch (Exception e) {
                // Ignore here
            }
        }
    }

    @Test
    public void canUpdateJobState() {
        // CREATE
        String jobId = getStringIdWithUserNamePrefix("-Job-CanUpdateState");
        BatchPoolInfo poolInfo = new BatchPoolInfo();
        poolInfo.setPoolId(poolId);

        BatchJobCreateContent jobToCreate = new BatchJobCreateContent(jobId, poolInfo);
        batchClient.createJob(jobToCreate);

        try {
            // GET
            BatchJob job = batchClient.getJob(jobId);
            Assertions.assertEquals(BatchJobState.ACTIVE, job.getState());

            // REPLACE
            Integer maxTaskRetryCount = 3;
            Integer priority = 500;
            job.setPriority(priority);
            job.setConstraints(new BatchJobConstraints().setMaxTaskRetryCount(maxTaskRetryCount));
            job.getPoolInfo().setPoolId(poolId);
            batchClient.replaceJob(jobId, job);

            job = batchClient.getJob(jobId);
            Assertions.assertEquals(priority, job.getPriority());
            Assertions.assertEquals(maxTaskRetryCount, job.getConstraints().getMaxTaskRetryCount());

            batchClient.disableJob(jobId, new BatchJobDisableContent(DisableBatchJobOption.REQUEUE));
            job = batchClient.getJob(jobId);
            Assertions.assertEquals(BatchJobState.DISABLING, job.getState());

            sleepIfRunningAgainstService(5 * 1000);

            job = batchClient.getJob(jobId);
            Assertions.assertTrue(job.getState() == BatchJobState.DISABLED || job.getState() == BatchJobState.DISABLING);
            Assertions.assertEquals(OnAllBatchTasksComplete.NO_ACTION, job.getOnAllTasksComplete());

            // UPDATE
            BatchJobUpdateContent jobUpdateContent = new BatchJobUpdateContent();
            jobUpdateContent.setOnAllTasksComplete(OnAllBatchTasksComplete.TERMINATE_JOB);
            batchClient.updateJob(jobId, jobUpdateContent);
            job = batchClient.getJob(jobId);
            Assertions.assertEquals(OnAllBatchTasksComplete.TERMINATE_JOB, job.getOnAllTasksComplete());

            batchClient.enableJob(jobId);
            job = batchClient.getJob(jobId);
            Assertions.assertEquals(BatchJobState.ACTIVE, job.getState());

            batchClient.terminateJob(jobId, new TerminateBatchJobOptions(), new BatchJobTerminateContent().setTerminationReason("myreason"));
            job = batchClient.getJob(jobId);
            Assertions.assertEquals(BatchJobState.TERMINATING, job.getState());

            sleepIfRunningAgainstService(2 * 1000);
            job = batchClient.getJob(jobId);
            Assertions.assertEquals(BatchJobState.COMPLETED, job.getState());
        } finally {
            try {
                batchClient.deleteJob(jobId);
            } catch (Exception e) {
                // Ignore here
            }
        }
    }

    @Test
    public void canCRUDJobWithPoolNodeCommunicationMode() {
        // CREATE
        String jobId = getStringIdWithUserNamePrefix("-Job-canCRUDWithPoolNodeComm");
        BatchNodeCommunicationMode targetMode = BatchNodeCommunicationMode.SIMPLIFIED;

        ImageReference imgRef = new ImageReference().setPublisher("Canonical").setOffer("UbuntuServer")
            .setSku("18.04-LTS").setVersion("latest");

        VirtualMachineConfiguration configuration = new VirtualMachineConfiguration(imgRef, "batch.node.ubuntu 18.04");

        BatchPoolSpecification poolSpec = new BatchPoolSpecification("STANDARD_D1_V2");
        poolSpec.setVirtualMachineConfiguration(configuration)
            .setTargetNodeCommunicationMode(targetMode);

        BatchPoolInfo poolInfo = new BatchPoolInfo();
        poolInfo.setAutoPoolSpecification(new BatchAutoPoolSpecification(BatchPoolLifetimeOption.JOB).setPool(poolSpec));

        BatchJobCreateContent jobToCreate = new BatchJobCreateContent(jobId, poolInfo);
        batchClient.createJob(jobToCreate);

        try {
            // GET
            BatchJob job = batchClient.getJob(jobId);
            Assertions.assertNotNull(job);
            Assertions.assertEquals(jobId, job.getId());
            Assertions.assertEquals(targetMode, job.getPoolInfo().getAutoPoolSpecification().getPool().getTargetNodeCommunicationMode());

            // DELETE
            batchClient.deleteJob(jobId);

            try {
                batchClient.getJob(jobId);
                Assertions.assertTrue(true, "Shouldn't be here, the job should be deleted");
            } catch (Exception err) {
                if (!err.getMessage().contains("Status code 404")) {
                    throw err;
                }
            }

            sleepIfRunningAgainstService(15 * 1000);
        } finally {
            try {
                batchClient.deleteJob(jobId);
            } catch (Exception e) {
                // Ignore here
            }
        }
    }

    @Test
    public void testDeserializationOfBatchJobStatistics() {
        // Simulated JSON response with numbers as strings
        String jsonResponse = "{"
            + "\"url\":\"https://example.com/stats\","
            + "\"startTime\":\"2022-01-01T00:00:00Z\","
            + "\"lastUpdateTime\":\"2022-01-01T01:00:00Z\","
            + "\"userCPUTime\":\"PT1H\","
            + "\"kernelCPUTime\":\"PT30M\","
            + "\"wallClockTime\":\"PT1H30M\","
            + "\"readIOps\":\"1000\","
            + "\"writeIOps\":\"500\","
            + "\"readIOGiB\":0.5,"
            + "\"writeIOGiB\":0.25,"
            + "\"numSucceededTasks\":\"10\","
            + "\"numFailedTasks\":\"2\","
            + "\"numTaskRetries\":\"3\","
            + "\"waitTime\":\"PT10M\""
            + "}";

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
            Assertions.assertEquals(1000, stats.getReadIOps());
            Assertions.assertEquals(500, stats.getWriteIOps());
            Assertions.assertEquals(0.5, stats.getReadIOGiB());
            Assertions.assertEquals(0.25, stats.getWriteIOGiB());
            Assertions.assertEquals(10, stats.getNumSucceededTasks());
            Assertions.assertEquals(2, stats.getNumFailedTasks());
            Assertions.assertEquals(3, stats.getNumTaskRetries());
            Assertions.assertEquals(Duration.parse("PT10M"), stats.getWaitTime());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
