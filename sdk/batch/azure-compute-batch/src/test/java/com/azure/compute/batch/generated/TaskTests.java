package com.azure.compute.batch.generated;

import com.azure.compute.batch.FileClient;
import com.azure.compute.batch.models.*;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.storage.blob.BlobContainerClient;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.azure.compute.batch.JobClient;
import com.azure.compute.batch.PoolClient;
import com.azure.compute.batch.TaskClient;
import com.azure.core.test.TestMode;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
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
	
	@Test
    public void canCRUDTest() throws Exception {
        int TASK_COMPLETE_TIMEOUT_IN_SECONDS = 60; // 60 seconds timeout
        String STANDARD_CONSOLE_OUTPUT_FILENAME = "stdout.txt";
        String BLOB_FILE_NAME = "test.txt";
        String taskId = "mytask";
        File temp = File.createTempFile("tempFile", ".tmp");
        BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
        bw.write("This is an example");
        bw.close();
        temp.deleteOnExit();
        String jobId = getStringIdWithUserNamePrefix("-canCRUDTest");

        PoolInformation poolInfo = new PoolInformation();
        poolInfo.setPoolId(liveIaasPoolId);
        jobClient.add(new BatchJob().setId(jobId).setPoolInfo(poolInfo));

        String storageAccountName = Configuration.getGlobalConfiguration().get("STORAGE_ACCOUNT_NAME");
        String storageAccountKey = Configuration.getGlobalConfiguration().get("STORAGE_ACCOUNT_KEY");
        BlobContainerClient container = null;

        try {
            String sas = "";

            //The Storage operations run only in Record mode.
            // Playback mode is configured to test Batch operations only.
            if (getTestMode() == TestMode.RECORD) {
                // Create storage container
                container = createBlobContainer(storageAccountName, storageAccountKey, "testingtaskcreate");
                sas = uploadFileToCloud(container, BLOB_FILE_NAME, temp.getAbsolutePath());
            }

            // Associate resource file with task
            ResourceFile file = new ResourceFile();
            file.setFilePath(BLOB_FILE_NAME);
            file.setHttpUrl(sas);
            List<ResourceFile> files = new ArrayList<>();
            files.add(file);

            // CREATE
            BatchTask taskToAdd = new BatchTask().setId(taskId).setCommandLine(String.format("/bin/bash -c 'set -e; set -o pipefail; cat %s'", BLOB_FILE_NAME)).setResourceFiles(files);

            taskClient.add(jobId, taskToAdd);

            // GET
            BatchTask task = taskClient.get(jobId, taskId);
            Assert.assertNotNull(task);
            Assert.assertEquals(taskId, task.getId());

            // Verify default retention time
            Assert.assertEquals(Duration.ofDays(7), task.getConstraints().getRetentionTime());

            // TODO UPDATE - modifying taskToAdd vs creating new BatchTask instance
//            BatchTask taskToUpdate = new BatchTask().setId(taskId).setConstraints(new TaskConstraints().setMaxTaskRetryCount(5));
//            //taskToAdd.setConstraints(new TaskConstraints().setMaxTaskRetryCount(5));
//            taskClient.update(jobId, taskId, taskToUpdate);
//
//            task = taskClient.get(jobId, taskId);
//            Assert.assertEquals((Integer) 5, task.getConstraints().getMaxTaskRetryCount());

            // LIST
            PagedIterable<BatchTask> tasks = taskClient.list(jobId);
            Assert.assertNotNull(tasks);

            boolean found = false;
            for (BatchTask t : tasks) {
                if (t.getId().equals(taskId)) {
                    found = true;
                    break;
                }
            }

            Assert.assertTrue(found);

            if (waitForTasksToComplete(taskClient, jobId, TASK_COMPLETE_TIMEOUT_IN_SECONDS)) {
                // Get the task command output file
                task = taskClient.get(jobId, taskId);

                FileClient fileClient = batchClientBuilder.buildFileClient();
                BinaryData binaryData = fileClient.getFromTask(jobId, taskId, STANDARD_CONSOLE_OUTPUT_FILENAME);

                String fileContent = new String(binaryData.toBytes(), StandardCharsets.UTF_8);
                Assert.assertEquals("This is an example", fileContent);

                String outputSas = "";

                //The Storage operations run only in Record mode.
                // Playback mode is configured to test Batch operations only.
                if (getTestMode() == TestMode.RECORD) {
                    outputSas = generateContainerSasToken(container);
                }
                // UPLOAD LOG
                UploadBatchServiceLogsConfiguration logsConfiguration = new UploadBatchServiceLogsConfiguration(outputSas, OffsetDateTime.now().minusMinutes(-10));
                UploadBatchServiceLogsResult uploadBatchServiceLogsResult = batchClientBuilder.buildComputeNodesClient().uploadBatchServiceLogs(liveIaasPoolId, task.getNodeInfo().getNodeId(), logsConfiguration);

                Assert.assertNotNull(uploadBatchServiceLogsResult);
                Assert.assertTrue(uploadBatchServiceLogsResult.getNumberOfFilesUploaded() > 0);
                Assert.assertTrue(uploadBatchServiceLogsResult.getVirtualDirectoryName().toLowerCase().contains(liveIaasPoolId.toLowerCase()));
            }

            // DELETE
            taskClient.delete(jobId, taskId);
            try {
                taskClient.get(jobId, taskId);
                Assert.assertTrue("Shouldn't be here, the job should be deleted", true);
            }   //TODO Integrate BatchErrorException
            catch (Exception e) {
                if (!e.getMessage().contains("Status code 404")) {
                    throw e;
                }
                }
            }
        catch (Exception e) {
            throw e;
        }
        finally {
                try {
                    jobClient.delete(jobId);
                    container.deleteIfExists();
                } catch (Exception e) {
                    // Ignore here
                }
            }
    }
}
