// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch;

import com.azure.compute.batch.models.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.test.TestMode;


public class JobTests extends BatchServiceClientTestBase {
	private static BatchPool livePool;
    static String poolId;

    @Override
    protected void beforeTest() {
    	super.beforeTest();
        poolId = getStringIdWithUserNamePrefix("-testpool");
        if(getTestMode() == TestMode.RECORD) {
        	if (livePool == null) {
        		try {
					livePool = createIfNotExistIaaSPool(poolId);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                Assertions.assertNotNull(livePool);
        	}
        }
    }

    @Test
    public void canCrudJob() throws Exception {
    	 // CREATE
        String jobId = getStringIdWithUserNamePrefix("-Job-canCRUD");

        PoolInformation poolInfo = new PoolInformation();
        poolInfo.setPoolId(poolId);
        BatchJobCreateOptions jobCreateOptions = new BatchJobCreateOptions(jobId, poolInfo);

        batchClient.createJob(jobCreateOptions);

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

            Thread.sleep(1 * 1000);
        }
        finally {
            try {
                batchClient.deleteJob(jobId);
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
        poolInfo.setPoolId(poolId);

        BatchJobCreateOptions jobtoCreate = new BatchJobCreateOptions(jobId, poolInfo);
        batchClient.createJob(jobtoCreate);

        try {
            // GET
            BatchJob job = batchClient.getJob(jobId);
            Assertions.assertEquals(JobState.ACTIVE, job.getState());

            // REPLACE
            Integer maxTaskRetryCount = 3;
            Integer priority = 500;
            job.setPriority(priority);
            job.setConstraints(new JobConstraints().setMaxTaskRetryCount(maxTaskRetryCount));
            job.setPoolInfo(new PoolInformation().setPoolId(poolId));
            batchClient.replaceJob(jobId, job);

            job = batchClient.getJob(jobId);
            Assertions.assertEquals(priority, job.getPriority());
            Assertions.assertEquals(maxTaskRetryCount, job.getConstraints().getMaxTaskRetryCount());

            batchClient.disableJob(jobId, new BatchJobDisableOptions(DisableJobOption.REQUEUE));
            job = batchClient.getJob(jobId);
            Assertions.assertEquals(JobState.DISABLING, job.getState());

            Thread.sleep(5 * 1000);

            job = batchClient.getJob(jobId);
            Assertions.assertTrue(job.getState() == JobState.DISABLED || job.getState() == JobState.DISABLING);
            Assertions.assertEquals(OnAllTasksComplete.NO_ACTION, job.getOnAllTasksComplete());

            // UPDATE
            BatchJobUpdateOptions jobUpdateOptions = new BatchJobUpdateOptions();
            jobUpdateOptions.setOnAllTasksComplete(OnAllTasksComplete.TERMINATE_JOB);
            batchClient.updateJob(jobId, jobUpdateOptions);
            job = batchClient.getJob(jobId);
            Assertions.assertEquals(OnAllTasksComplete.TERMINATE_JOB, job.getOnAllTasksComplete());

            batchClient.enableJob(jobId);
            job = batchClient.getJob(jobId);
            Assertions.assertEquals(JobState.ACTIVE, job.getState());

            batchClient.terminateJob(jobId, null, new BatchJobTerminateOptions().setTerminateReason("myreason"), null);
            job = batchClient.getJob(jobId);
            Assertions.assertEquals(JobState.TERMINATING, job.getState());

            Thread.sleep(2 * 1000);
            job = batchClient.getJob(jobId);
            Assertions.assertEquals(JobState.COMPLETED, job.getState());
        }
        finally {
            try {
                batchClient.deleteJob(jobId);
            }
            catch (Exception e) {
                // Ignore here
            }
        }
    }

    @Test
    public void canCRUDJobWithPoolNodeCommunicationMode() throws Exception {
        // CREATE
        String jobId = getStringIdWithUserNamePrefix("-Job-canCRUDWithPoolNodeComm");
        NodeCommunicationMode targetMode = NodeCommunicationMode.SIMPLIFIED;

        ImageReference imgRef = new ImageReference().setPublisher("Canonical").setOffer("UbuntuServer")
                .setSku("18.04-LTS").setVersion("latest");

        VirtualMachineConfiguration configuration = new VirtualMachineConfiguration(imgRef, "batch.node.ubuntu 18.04");

        PoolSpecification poolSpec = new PoolSpecification("STANDARD_D1_V2");
        poolSpec.setVirtualMachineConfiguration(configuration)
                .setTargetNodeCommunicationMode(targetMode);

        PoolInformation poolInfo = new PoolInformation();
        poolInfo.setAutoPoolSpecification(new AutoPoolSpecification(PoolLifetimeOption.JOB).setPool(poolSpec));

        BatchJobCreateOptions jobCreateOptions = new BatchJobCreateOptions(jobId, poolInfo);
        batchClient.createJob(jobCreateOptions);

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

            threadSleepInRecordMode(15 * 1000);
        }
        finally {
            try {
                batchClient.deleteJob(jobId);
            }
            catch (Exception e) {
                // Ignore here
            }
        }
    }


}
