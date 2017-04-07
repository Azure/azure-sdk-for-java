/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch;

import com.microsoft.azure.batch.protocol.models.*;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Date;
import java.util.List;

public class JobTests extends BatchTestBase {
    private static CloudPool livePool;

    @BeforeClass
    public static void setup() throws Exception {
        createClient(AuthMode.SharedKey);
        String poolId = getStringWithUserNamePrefix("-testpool");
        livePool = createIfNotExistPaaSPool(poolId);
        Assert.assertNotNull(livePool);
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
        String jobId = getStringWithUserNamePrefix("-Job-" + (new Date()).toString().replace(' ', '-').replace(':', '-').replace('.', '-'));

        PoolInformation poolInfo = new PoolInformation();
        poolInfo.withPoolId(livePool.id());
        batchClient.jobOperations().createJob(jobId, poolInfo);

        try {
            // GET
            CloudJob job = batchClient.jobOperations().getJob(jobId);
            Assert.assertNotNull(job);
            Assert.assertEquals(job.id(), jobId);
            Assert.assertEquals(job.priority(), (Integer) 0);

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
            Assert.assertEquals(job.priority(), (Integer) 1);

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

            Thread.sleep(1000);
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
        String jobId = getStringWithUserNamePrefix("-Job-" + (new Date()).toString().replace(' ', '-').replace(':', '-').replace('.', '-'));

        PoolInformation poolInfo = new PoolInformation();
        poolInfo.withPoolId(livePool.id());
        batchClient.jobOperations().createJob(jobId, poolInfo);

        try {
            // GET
            CloudJob job = batchClient.jobOperations().getJob(jobId);
            Assert.assertEquals(job.state(), JobState.ACTIVE);

            batchClient.jobOperations().disableJob(jobId, DisableJobOption.REQUEUE.REQUEUE);
            job = batchClient.jobOperations().getJob(jobId);
            Assert.assertEquals(job.state(), JobState.DISABLING);

            Thread.sleep(2 * 1000);
            job = batchClient.jobOperations().getJob(jobId);
            Assert.assertEquals(job.state(), JobState.DISABLED);

            Assert.assertEquals(job.onAllTasksComplete(), OnAllTasksComplete.NO_ACTION);
            batchClient.jobOperations().patchJob(jobId, OnAllTasksComplete.TERMINATE_JOB);
            job = batchClient.jobOperations().getJob(jobId);
            Assert.assertEquals(job.onAllTasksComplete(), OnAllTasksComplete.TERMINATE_JOB);

            batchClient.jobOperations().enableJob(jobId);
            job = batchClient.jobOperations().getJob(jobId);
            Assert.assertEquals(job.state(), JobState.ACTIVE);

            batchClient.jobOperations().terminateJob(jobId, "myreason");
            job = batchClient.jobOperations().getJob(jobId);
            Assert.assertEquals(job.state(), JobState.TERMINATING);

            Thread.sleep(2 * 1000);
            job = batchClient.jobOperations().getJob(jobId);
            Assert.assertEquals(job.state(), JobState.COMPLETED);
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
