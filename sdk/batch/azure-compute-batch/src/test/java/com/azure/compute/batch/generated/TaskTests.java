package com.azure.compute.batch.generated;

import com.azure.core.util.Configuration;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TaskTests extends BatchServiceClientTestBase {
	private static BatchPool livePool;
	private static String livePoolId;
	private static PoolClient poolClient;
	private static JobClient jobClient;
	private static TaskClient taskClient;
    private static String liveIaasPoolId;
	 
	@Override
    protected void beforeTest() {
	   	super.beforeTest();
	   	poolClient = batchClientBuilder.buildPoolClient();
	   	jobClient = batchClientBuilder.buildJobClient();
	   	taskClient = batchClientBuilder.buildTaskClient();
	   	livePoolId = getStringIdWithUserNamePrefix("-testpool");
        liveIaasPoolId = getStringIdWithUserNamePrefix("-testIaaSpool");
	    if(getTestMode() == TestMode.RECORD) {
	    	if (livePool == null) {
	    		try {
					livePool = createIfNotExistIaaSPool(livePoolId);
                    createIfNotExistIaaSPool(liveIaasPoolId);
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
	
//	@Test
//    public void canCRUDTest() throws Exception {
//        int TASK_COMPLETE_TIMEOUT_IN_SECONDS = 60; // 60 seconds timeout
//        String STANDARD_CONSOLE_OUTPUT_FILENAME = "stdout.txt";
//        String BLOB_FILE_NAME = "test.txt";
//        String taskId = "mytask";
//        File temp = File.createTempFile("tempFile", ".tmp");
//        BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
//        bw.write("This is an example");
//        bw.close();
//        temp.deleteOnExit();
//        String jobId = getStringIdWithUserNamePrefix("-canCRUDTest");
//
//        PoolInformation poolInfo = new PoolInformation();
//        poolInfo.setPoolId(liveIaasPoolId);
//        jobClient.add(new BatchJob().setId(jobId).setPoolInfo(poolInfo));
//
//        String storageAccountName = Configuration.getGlobalConfiguration().get("STORAGE_ACCOUNT_NAME");
//        String storageAccountKey = Configuration.getGlobalConfiguration().get("STORAGE_ACCOUNT_KEY");
//
//        try {
//            String sas = "";
//            CloudBlobContainer container = null;
//
//            //The Storage operations run only in Record mode.
//            // Playback mode is configured to test Batch operations only.
//            if (isRecordMode()) {
//                // Create storage container
//                container = createBlobContainer(storageAccountName, storageAccountKey, "testaddtask");
//                sas = uploadFileToCloud(container, BLOB_FILE_NAME, temp.getAbsolutePath());
//            }
//
//            // Associate resource file with task
//            ResourceFile file = new ResourceFile();
//            file.withFilePath(BLOB_FILE_NAME);
//            file.withHttpUrl(sas);
//            List<ResourceFile> files = new ArrayList<>();
//            files.add(file);
//
//            // CREATE
//            TaskAddParameter taskToAdd = new TaskAddParameter();
//            taskToAdd.withId(taskId).withCommandLine(String.format("/bin/bash -c 'set -e; set -o pipefail; cat %s'", BLOB_FILE_NAME)).withResourceFiles(files);
//
//            batchClient.taskOperations().createTask(jobId, taskToAdd);
//
//            // GET
//            CloudTask task = batchClient.taskOperations().getTask(jobId, taskId);
//            Assert.assertNotNull(task);
//            Assert.assertEquals(taskId, task.id());
//
//            // Verify default retention time
//            Assert.assertEquals(Period.days(7), task.constraints().retentionTime());
//
//            // UPDATE
//            TaskConstraints contraint = new TaskConstraints();
//            contraint.withMaxTaskRetryCount(5);
//            batchClient.taskOperations().updateTask(jobId, taskId, contraint);
//            task = batchClient.taskOperations().getTask(jobId, taskId);
//            Assert.assertEquals((Integer) 5, task.constraints().maxTaskRetryCount());
//
//            // LIST
//            List<CloudTask> tasks = batchClient.taskOperations().listTasks(jobId);
//            Assert.assertNotNull(tasks);
//            Assert.assertTrue(tasks.size() > 0);
//
//            boolean found = false;
//            for (CloudTask t : tasks) {
//                if (t.id().equals(taskId)) {
//                    found = true;
//                    break;
//                }
//            }
//
//            Assert.assertTrue(found);
//
//            if (waitForTasksToComplete(batchClient, jobId, TASK_COMPLETE_TIMEOUT_IN_SECONDS)) {
//                // Get the task command output file
//                task = batchClient.taskOperations().getTask(jobId, taskId);
//
//                ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                batchClient.fileOperations().getFileFromTask(jobId, task.id(), STANDARD_CONSOLE_OUTPUT_FILENAME, stream);
//                String fileContent = stream.toString("UTF-8");
//                Assert.assertEquals("This is an example", fileContent);
//
//                String outputSas = "";
//
//                //The Storage operations run only in Record mode.
//                // Playback mode is configured to test Batch operations only.
//                if(isRecordMode()) {
//                    outputSas = generateContainerSasToken(container);
//                }
//                // UPLOAD LOG
//                UploadBatchServiceLogsResult uploadBatchServiceLogsResult = batchClient.computeNodeOperations().uploadBatchServiceLogs(liveIaasPoolId, task.nodeInfo().nodeId(), outputSas, DateTime.now().minusMinutes(-10));
//                Assert.assertNotNull(uploadBatchServiceLogsResult);
//                Assert.assertTrue(uploadBatchServiceLogsResult.numberOfFilesUploaded() > 0);
//                Assert.assertTrue(uploadBatchServiceLogsResult.virtualDirectoryName().toLowerCase().contains(liveIaasPoolId.toLowerCase()));
//            }
//
//            // DELETE
//            batchClient.taskOperations().deleteTask(jobId, taskId);
//            try {
//                batchClient.taskOperations().getTask(jobId, taskId);
//                Assert.assertTrue("Shouldn't be here, the job should be deleted", true);
//            } catch (BatchErrorException err) {
//                if (!err.body().code().equals(BatchErrorCodeStrings.TaskNotFound)) {
//                    throw err;
//                }
//            }
//        } finally {
//            try {
//                batchClient.jobOperations().deleteJob(jobId);
//            } catch (Exception e) {
//                // Ignore here
//            }
//        }
//    }
}
