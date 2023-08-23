// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch;

import com.azure.compute.batch.models.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
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
        JobClient jobClient = batchClientBuilder.buildJobClient();
        BatchJobCreateParameters jobCreateParameters = new BatchJobCreateParameters(jobId, poolInfo);

        jobClient.create(jobCreateParameters);

        try {
            // GET
            BatchJob job = jobClient.get(jobId);
            Assertions.assertNotNull(job);
            Assertions.assertNotNull(job.isAllowTaskPreemption());
            Assertions.assertEquals(-1, (int) job.getMaxParallelTasks());
            Assertions.assertEquals(jobId, job.getId());
            Assertions.assertEquals((Integer) 0, job.getPriority());

            // LIST
            PagedIterable<BatchJob> jobs = jobClient.list();
            Assertions.assertNotNull(jobs);


            boolean found = false;
            for (BatchJob batchJob : jobs) {
                if (batchJob.getId().equals(jobId)) {
                    found = true;
                    break;
                }
            }

            Assertions.assertTrue(found);


            // UPDATE
            BatchJob updatedJob = job;
            updatedJob.setPriority(1);
            jobClient.update(jobId, updatedJob);

            job = jobClient.get(jobId);
            Assertions.assertEquals((Integer) 1, job.getPriority());

            // DELETE
            jobClient.delete(jobId);
            try {
            	jobClient.get(jobId);
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
            	jobClient.delete(jobId);
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

        BatchJobCreateParameters jobtoCreate = new BatchJobCreateParameters(jobId, poolInfo);
        jobClient.create(jobtoCreate);

        try {
            // GET
            BatchJob job = jobClient.get(jobId);
            Assertions.assertEquals(JobState.ACTIVE, job.getState());

            // UPDATE
            Integer maxTaskRetryCount = 3;
            Integer priority = 500;
            job.setPriority(priority);
            job.setConstraints(new JobConstraints().setMaxTaskRetryCount(maxTaskRetryCount));
            job.setPoolInfo(new PoolInformation().setPoolId(poolId));
            jobClient.update(jobId, job);

            job = jobClient.get(jobId);
            Assertions.assertEquals(priority, job.getPriority());
            Assertions.assertEquals(maxTaskRetryCount, job.getConstraints().getMaxTaskRetryCount());

            jobClient.disable(jobId, new BatchJobDisableParameters(DisableJobOption.REQUEUE));
            job = jobClient.get(jobId);
            Assertions.assertEquals(JobState.DISABLING, job.getState());

            Thread.sleep(5 * 1000);

            job = jobClient.get(jobId);
            Assertions.assertTrue(job.getState() == JobState.DISABLED || job.getState() == JobState.DISABLING);
            Assertions.assertEquals(OnAllTasksComplete.NO_ACTION, job.getOnAllTasksComplete());

            //PATCH
            BatchJobUpdateParameters jobUpdateParameters = new BatchJobUpdateParameters();
            jobUpdateParameters.setOnAllTasksComplete(OnAllTasksComplete.TERMINATE_JOB);
            jobClient.patch(jobId, jobUpdateParameters);
            job = jobClient.get(jobId);
            Assertions.assertEquals(OnAllTasksComplete.TERMINATE_JOB, job.getOnAllTasksComplete());

            jobClient.enable(jobId);
            job = jobClient.get(jobId);
            Assertions.assertEquals(JobState.ACTIVE, job.getState());

            jobClient.terminate(jobId, null, null, new BatchJobTerminateParameters().setTerminateReason("myreason"), null);
            job = jobClient.get(jobId);
            Assertions.assertEquals(JobState.TERMINATING, job.getState());

            Thread.sleep(2 * 1000);
            job = jobClient.get(jobId);
            Assertions.assertEquals(JobState.COMPLETED, job.getState());
        }
        finally {
            try {
                jobClient.delete(jobId);
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

        BatchJobCreateParameters jobCreateParameters = new BatchJobCreateParameters(jobId, poolInfo);
        jobClient.create(jobCreateParameters);

        try {
            // GET
            BatchJob job = jobClient.get(jobId);
            Assertions.assertNotNull(job);
            Assertions.assertEquals(jobId, job.getId());
            Assertions.assertEquals(targetMode, job.getPoolInfo().getAutoPoolSpecification().getPool().getTargetNodeCommunicationMode());

            // DELETE
            jobClient.delete(jobId);

            try {
            	jobClient.get(jobId);
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
            	jobClient.delete(jobId);
            }
            catch (Exception e) {
                // Ignore here
            }
        }
    }


}
