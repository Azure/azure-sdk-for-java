package com.azure.compute.batch.generated;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.azure.compute.batch.JobClient;
import com.azure.compute.batch.PoolClient;
import com.azure.compute.batch.TaskClient;
import com.azure.compute.batch.models.BatchPool;
import com.azure.compute.batch.models.BatchJob;
import com.azure.compute.batch.models.BatchTask;
import com.azure.compute.batch.models.NetworkConfiguration;
import com.azure.compute.batch.models.PoolInformation;
import com.azure.compute.batch.models.UserIdentity;
import com.azure.compute.batch.models.ApplicationPackageReference;
import com.azure.core.test.TestMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TaskTests extends BatchServiceClientTestBase {
	private static BatchPool livePool;
	private static String livePoolId;
	private static PoolClient poolClient;
	private static JobClient jobClient;
	private static TaskClient taskClient;
	 
	@Override
    protected void beforeTest() {
	   	super.beforeTest();
	   	poolClient = batchClientBuilder.buildPoolClient();
	   	jobClient = batchClientBuilder.buildJobClient();
	   	taskClient = batchClientBuilder.buildTaskClient();
	   	livePoolId = getStringIdWithUserNamePrefix("-testpool");
	    if(getTestMode() == TestMode.RECORD) {
	    	if (livePool == null) {
	    		try {
					livePool = createIfNotExistIaaSPool(livePoolId);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
               Assert.assertNotNull(livePool);
	    	}
       }
   }
	
	@Test
    public void testJobUser() throws Exception {
        String jobId = getStringIdWithUserNamePrefix("-testJobUser");
        String taskId = "mytask";

        PoolInformation poolInfo = new PoolInformation();
        poolInfo.setPoolId(livePoolId);
        BatchJob jobToAdd = new BatchJob().setId(jobId).setPoolInfo(poolInfo);
        
        jobClient.add(jobToAdd);

        try {
            // CREATE
            List<ApplicationPackageReference> apps = new ArrayList<>();
            apps.add(new ApplicationPackageReference("MSMPI"));
            BatchTask taskToAdd = new BatchTask();
            taskToAdd.setId(taskId).setCommandLine("cmd /c echo hello")
            		 .setUserIdentity(new UserIdentity().setUsername("test-user"))
            		 .setApplicationPackageReferences(apps);

            taskClient.add(jobId, taskToAdd);

            // GET
            BatchTask task = taskClient.get(jobId, taskId);
            Assert.assertNotNull(task);
            Assert.assertEquals(taskId, task.getId());
            Assert.assertEquals("test-user", task.getUserIdentity().getUsername());
            Assert.assertEquals("msmpi", task.getApplicationPackageReferences().get(0).getApplicationId());

        } finally {
            try {
                jobClient.delete(jobId);
            } catch (Exception e) {
                // Ignore here
            }
        }
    }
}
