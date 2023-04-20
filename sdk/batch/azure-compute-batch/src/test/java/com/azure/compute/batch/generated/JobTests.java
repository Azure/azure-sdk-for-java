// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch.generated;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.azure.compute.batch.JobClient;
import com.azure.compute.batch.models.AutoPoolSpecification;
import com.azure.compute.batch.models.BatchJob;
import com.azure.compute.batch.models.BatchJobDisableParameters;
import com.azure.compute.batch.models.BatchJobTerminateParameters;
import com.azure.compute.batch.models.BatchPool;
import com.azure.compute.batch.models.DisableJobOption;
import com.azure.compute.batch.models.ImageReference;
import com.azure.compute.batch.models.JobConstraints;
import com.azure.compute.batch.models.JobState;
import com.azure.compute.batch.models.NodeCommunicationMode;
import com.azure.compute.batch.models.OnAllTasksComplete;
import com.azure.compute.batch.models.PoolInformation;
import com.azure.compute.batch.models.PoolLifetimeOption;
import com.azure.compute.batch.models.PoolSpecification;
import com.azure.compute.batch.models.VirtualMachineConfiguration;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.test.TestMode;
import com.azure.core.util.BinaryData;
import com.azure.core.http.rest.RequestOptions;

//import com.azure.core.http.rest.RequestOptions;



public class JobTests extends BatchServiceClientTestBase {
	private static BatchPool livePool;
    static String poolId;
    private static JobClient jobClient;

    @Override
    protected void beforeTest() {
    	super.beforeTest();
    	jobClient = batchClientBuilder.buildJobClient();
        poolId = getStringIdWithUserNamePrefix("-testpool");
        if(getTestMode() == TestMode.RECORD) {
        	if (livePool == null) {
        		try {
					livePool = createIfNotExistIaaSPool(poolId);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                Assert.assertNotNull(livePool);
        	}
        }
    }
    
    @Test
    public void testJobPut() throws Exception {
    	String jobId = getStringIdWithUserNamePrefix("-Job-canPut");
    	PoolInformation poolInfo = new PoolInformation();
        poolInfo.setPoolId(poolId);
        JobClient jobClient = batchClientBuilder.buildJobClient();
        BatchJob jobToAdd = new BatchJob();
        jobToAdd.setId(jobId);
        jobToAdd.setPoolInfo(poolInfo);
        
        jobClient.add(jobToAdd);
        
        try {
        	BatchJob getJob = jobClient.get(jobId);
        	jobToAdd.setPriority(500);
        	jobClient.update(jobId, getJob);
        }
        finally {
        	jobClient.delete(jobId);
        }
        
    }
    
    @Test
    public void canCrudJob() throws Exception {
    	 // CREATE
        String jobId = getStringIdWithUserNamePrefix("-Job-canCRUD");

        PoolInformation poolInfo = new PoolInformation();
        poolInfo.setPoolId(poolId);
        JobClient jobClient = batchClientBuilder.buildJobClient();
        BatchJob jobToAdd = new BatchJob();
        jobToAdd.setId(jobId);
        jobToAdd.setPoolInfo(poolInfo);

        jobClient.add(jobToAdd);

        try {
            // GET
            BatchJob job = jobClient.get(jobId);
            Assert.assertNotNull(job);
            Assert.assertNotNull(job.isAllowTaskPreemption());
            Assert.assertEquals(-1, (int) job.getMaxParallelTasks());
            Assert.assertEquals(jobId, job.getId());
            Assert.assertEquals((Integer) 0, job.getPriority());

            // LIST
            PagedIterable<BatchJob> jobs = jobClient.list();
            Assert.assertNotNull(jobs);


            boolean found = false;
            for (BatchJob batchJob : jobs) {
                if (batchJob.getId().equals(jobId)) {
                    found = true;
                    break;
                }
            }

            Assert.assertTrue(found);


            // UPDATE
            BatchJob updatedJob = jobToAdd;
            updatedJob.setPriority(1);
            jobClient.update(jobId, updatedJob);

            job = jobClient.get(jobId);
            Assert.assertEquals((Integer) 1, job.getPriority());

            // DELETE
            jobClient.delete(jobId);
            try {
            	jobClient.get(jobId);
                Assert.assertTrue("Shouldn't be here, the job should be deleted", true);
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
    
//    @Test
//    public void canUpdateJobState() throws Exception {
//        // CREATE
//        String jobId = getStringIdWithUserNamePrefix("-Job-CanUpdateState");
//        PoolInformation poolInfo = new PoolInformation();
//        poolInfo.setPoolId(poolId);
//
//        BatchJob jobToAdd = new BatchJob();
//        jobToAdd.setId(jobId);
//        jobToAdd.setPoolInfo(poolInfo);
//
//        jobClient.add(jobToAdd);
//
//        try {
//            // GET
//            BatchJob job = jobClient.get(jobId);
//            Assert.assertEquals(JobState.ACTIVE, job.getState());
//
//            // UPDATE
//            Integer maxTaskRetryCount = 3;
//            Integer priority = 500;
//            jobClient.update(jobId, new BatchJob().setPoolInfo(poolInfo).setPriority(priority).setConstraints(new JobConstraints().setMaxTaskRetryCount(maxTaskRetryCount)));
//
//            job = jobClient.get(jobId);
//            Assert.assertEquals(priority, job.getPriority());
//            Assert.assertEquals(maxTaskRetryCount, job.getConstraints().getMaxTaskRetryCount());
//
//            jobClient.disable(jobId, new BatchJobDisableParameters(DisableJobOption.REQUEUE));
//            job = jobClient.get(jobId);
//            Assert.assertEquals(JobState.DISABLING, job.getState());
//
//            Thread.sleep(5 * 1000);
//
//            job = jobClient.get(jobId);
//            Assert.assertEquals(JobState.DISABLED, job.getState());
//            Assert.assertEquals(OnAllTasksComplete.NO_ACTION, job.getOnAllTasksComplete());
//
//            jobClient.patch(jobId, new BatchJob().setOnAllTasksComplete(OnAllTasksComplete.TERMINATE_JOB));
//            job = jobClient.get(jobId);
//            Assert.assertEquals(OnAllTasksComplete.TERMINATE_JOB, job.getOnAllTasksComplete());
//
//            jobClient.enable(jobId);
//            job = jobClient.get(jobId);
//            Assert.assertEquals(JobState.ACTIVE, job.getState());
//
////            RequestOptions options = new RequestOptions();
////            options.setBody(BinaryData.fromObject(new BatchJobTerminateParameters().setTerminateReason("myreason")));
//
//            jobClient.terminate(jobId, null, null, null, null, null, null, null, null, new BatchJobTerminateParameters().setTerminateReason("myreason"));
//            job = jobClient.get(jobId);
//            Assert.assertEquals(JobState.TERMINATING, job.getState());
//
//            Thread.sleep(2 * 1000);
//            job = jobClient.get(jobId);
//            Assert.assertEquals(JobState.COMPLETED, job.getState());
//        }
//        finally {
//            try {
//                jobClient.delete(jobId);
//            }
//            catch (Exception e) {
//                // Ignore here
//            }
//        }
//    }
    
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

        BatchJob jobToAdd = new BatchJob();
        jobToAdd.setId(jobId);
        jobToAdd.setPoolInfo(poolInfo);

        jobClient.add(jobToAdd);

        try {
            // GET
            BatchJob job = jobClient.get(jobId);
            Assert.assertNotNull(job);
            Assert.assertEquals(jobId, job.getId());
            Assert.assertEquals(targetMode, job.getPoolInfo().getAutoPoolSpecification().getPool().getTargetNodeCommunicationMode());

            // DELETE
            jobClient.delete(jobId);
            Thread.sleep(8 * 1000);

            try {
            	jobClient.get(jobId);
                Assert.assertTrue("Shouldn't be here, the job should be deleted", true);
            } catch (Exception err) {
            	if (!err.getMessage().contains("Status code 404")) {
        			throw err;
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

    
}