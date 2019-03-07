// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.batch;

import com.microsoft.azure.batch.protocol.models.*;
import org.junit.*;

import java.util.List;

public class JobTests extends BatchIntegrationTestBase {
    private static CloudPool livePool;
    static String poolId;

    @BeforeClass
    public static void setup() throws Exception {
        poolId = getStringIdWithUserNamePrefix("-testpool");
        if(isRecordMode()) {
            createClient(AuthMode.SharedKey);
            livePool = createIfNotExistPaaSPool(poolId);
            Assert.assertNotNull(livePool);
        }
    }

    @AfterClass
    public static void cleanup() throws Exception {
        try {
            //batchClient.poolOperations().deletePool(livePool.id());
        }
        catch (Exception e) {
            // ignore any clean up exception
        }
    }

    @Test
    public void canCRUDJob() throws Exception {
        // CREATE
        String jobId = getStringIdWithUserNamePrefix("-Job-canCRUD");

        PoolInformation poolInfo = new PoolInformation();
        poolInfo.withPoolId(poolId);
        batchClient.jobOperations().createJob(jobId, poolInfo);

        try {
            // GET
            CloudJob job = batchClient.jobOperations().getJob(jobId);
            Assert.assertNotNull(job);
            Assert.assertEquals(jobId, job.id());
            Assert.assertEquals((Integer) 0, job.priority());

            // LIST
            List<CloudJob> jobs = batchClient.jobOperations().listJobs();
            Assert.assertNotNull(jobs);
            Assert.assertTrue(jobs.size() > 0);

            boolean found = false;
            for (CloudJob j : jobs) {
                if (j.id().equals(jobId)) {
                    found = true;
                    break;
                }
            }

            Assert.assertTrue(found);


            // UPDATE
            batchClient.jobOperations().updateJob(jobId, poolInfo, 1, null, null, null);
            job = batchClient.jobOperations().getJob(jobId);
            Assert.assertEquals((Integer) 1, job.priority());

            // DELETE
            batchClient.jobOperations().deleteJob(jobId);
            try {
                batchClient.jobOperations().getJob(jobId);
                Assert.assertTrue("Shouldn't be here, the job should be deleted", true);
            } catch (BatchErrorException err) {
                if (!err.body().code().equals(BatchErrorCodeStrings.JobNotFound)) {
                    throw err;
                }
            }

            Thread.sleep(1 * 1000);
        }
        finally {
            try {
                batchClient.jobOperations().deleteJob(jobId);
            }
            catch (Exception e) {
                // Ignore here
            }
        }
    }

    @Test
    public void canUpdateJobState() throws Exception {
        // CREATE
        String jobId = getStringIdWithUserNamePrefix("-Job-CanUpdateState");
        PoolInformation poolInfo = new PoolInformation();
        poolInfo.withPoolId(poolId);

        batchClient.jobOperations().createJob(jobId, poolInfo);

        try {
            // GET
            CloudJob job = batchClient.jobOperations().getJob(jobId);
            Assert.assertEquals(JobState.ACTIVE, job.state());

            batchClient.jobOperations().disableJob(jobId, DisableJobOption.REQUEUE.REQUEUE);
            job = batchClient.jobOperations().getJob(jobId);
            Assert.assertEquals(JobState.DISABLING, job.state());

            Thread.sleep(5 * 1000);
            job = batchClient.jobOperations().getJob(jobId);
            Assert.assertEquals(JobState.DISABLED, job.state());

            Assert.assertEquals(OnAllTasksComplete.NO_ACTION, job.onAllTasksComplete());
            batchClient.jobOperations().patchJob(jobId, OnAllTasksComplete.TERMINATE_JOB);
            job = batchClient.jobOperations().getJob(jobId);
            Assert.assertEquals(OnAllTasksComplete.TERMINATE_JOB, job.onAllTasksComplete());

            batchClient.jobOperations().enableJob(jobId);
            job = batchClient.jobOperations().getJob(jobId);
            Assert.assertEquals(JobState.ACTIVE, job.state());

            batchClient.jobOperations().terminateJob(jobId, "myreason");
            job = batchClient.jobOperations().getJob(jobId);
            Assert.assertEquals(JobState.TERMINATING, job.state());

            Thread.sleep(2 * 1000);
            job = batchClient.jobOperations().getJob(jobId);
            Assert.assertEquals(JobState.COMPLETED, job.state());
        }
        finally {
            try {
                batchClient.jobOperations().deleteJob(jobId);
            }
            catch (Exception e) {
                // Ignore here
            }
        }
    }

}
